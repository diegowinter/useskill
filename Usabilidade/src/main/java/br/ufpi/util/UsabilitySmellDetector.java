package br.ufpi.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.TransformerUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYCoordinate;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jgrapht.Graph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.alg.cycle.SzwarcfiterLauerSimpleCycles;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedPseudograph;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import br.ufpi.datamining.models.ActionDataMining;
import br.ufpi.datamining.models.PageViewActionDataMining;
import br.ufpi.datamining.models.aux.BarChart;
import br.ufpi.datamining.models.aux.SessionResultDataMining;
import br.ufpi.datamining.models.aux.StackedAreaChart;
import br.ufpi.datamining.models.aux.TaskSmellAnalysis;
import br.ufpi.datamining.models.aux.XYSerie;
import br.ufpi.datamining.models.enums.SessionClassificationDataMiningEnum;
import br.ufpi.datamining.repositories.ActionDataMiningRepository;


public class UsabilitySmellDetector {
	private static final Logger log = Logger.getLogger(UsabilitySmellDetector.class.getName());
	
	public static final int NUMBER_DEFAULT = -1;
	public static final long TIME_DEFAULT = -1; 
	public static final double RATE_DEFAULT = -1.0;
	
	public static final double FENCE_TYPE_INNER = 1.5;
	public static final double FENCE_TYPE_OUTER = 3.0;
	
	
	//*********************************************************************************************
	//************* MÉTODOS DE GERAÇÃO DE GRÁFICOS A PARTIR DAS MÉTRICAS DOS SMELLS ***************
	//*********************************************************************************************
	
	/**
	 * Gera o gráfico de quantidade de ações das sessões completas de cada tarefa. Todos os tipos 
	 * de ações são contabilizadas, inclusive aquelas não realizadas diretamente pelo usuário 
	 * (como o carregamento de páginas, por exemplo).
	 *
	 * @param	tasks	uma lista de tarefas a serem analisadas
	 * @return			o gráfico de quantidade de ações correspondente às tarefas analisadas
	 */
	public StackedAreaChart generateTaskActionCountChart (List<TaskSmellAnalysis> tasks) throws IOException{
		List<XYSerie> series = new ArrayList<XYSerie>();
		for (TaskSmellAnalysis task : tasks) {
			List<Double> taskActionCountDataset = new ArrayList<Double>();
			for (SessionResultDataMining session : task.getSessions()) {
				if (session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS)) {
					taskActionCountDataset.add((double)session.getActions().size());
				}
			}			
			series.add(getDatasetDistributionSerie(task.getName(), taskActionCountDataset));
		}
		return new StackedAreaChart("Gráfico da Quantidade de Ações", "Proporção de sessões da tarefa (%)", "Quantidade de ações", series);
	}
	
	/**
	 * Gera o gráfico de duração das sessões completas de cada tarefa. A duração é apresentada em 
	 * minutos no gráfico retornado como saída para facilitar a visualização.
	 *
	 * @param	tasks	uma lista de tarefas a serem analisadas
	 * @return			o gráfico de duração correspondente às tarefas analisadas
	 */
	public StackedAreaChart generateTaskTimeChart (List<TaskSmellAnalysis> tasks) throws IOException{
		List<XYSerie> series = new ArrayList<XYSerie>();
		for (TaskSmellAnalysis task : tasks) {
			List<Double> taskTimeDataset = new ArrayList<Double>();
			for (SessionResultDataMining session : task.getSessions()) {
				if (session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS)) {
					taskTimeDataset.add((double)session.getTime());
				}
			}			
			XYSerie taskTimeSerie = new XYSerie(task.getName());
			for (XYCoordinate coordinate : getDatasetDistributionSerie(task.getName(), taskTimeDataset).getCoordinates()) {
				taskTimeSerie.addCoordinate(new XYCoordinate(coordinate.getX(), TimeUnit.MILLISECONDS.toMinutes(Math.round(coordinate.getY()))));
			}
			series.add(taskTimeSerie);
		}
		return new StackedAreaChart("Gráfico de Duração", "Proporção de sessões da tarefa (%)", "Tempo de duração (min)", series);
	}
	
	/**
	 * Gera o gráfico de quantidade de camadas das sessões completas de cada tarefa. A quantidade de camadas é determinada 
	 * pelo número de urls diferentes na sessão, portanto, páginas diferentes com urls iguais são contabilizadas como sendo 
	 * uma única página.
	 *
	 * @param	tasks	uma lista de tarefas a serem analisadas
	 * @return			o gráfico de quantidade de camadas correspondente às tarefas analisadas
	 */
	public StackedAreaChart generateTaskLayerCountChart (List<TaskSmellAnalysis> tasks) {
		List<XYSerie> series = new ArrayList<XYSerie>();
		for (TaskSmellAnalysis task : tasks) {
			List<Double> taskLayerCountDataset = new ArrayList<Double>();
			for (SessionResultDataMining session : task.getSessions()) {			
				if (session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS)) {
					Set<String> uniqueUrls = new HashSet<String>();
					for (PageViewActionDataMining action : session.getActions()) {
						uniqueUrls.add(action.getLocal());
					}
					taskLayerCountDataset.add((double)uniqueUrls.size());
				}
			}
			series.add(getDatasetDistributionSerie(task.getName(), taskLayerCountDataset));
		}
		return new StackedAreaChart("Gráfico do Número de Camadas", "Proporção de amostras (%)", "Número de camadas", series);
	}
	
	//FIM - Métodos para geração de gráficos das métricas dos smells
	
	//TODO testar e anotar
	//TODO ver qual o resultado se forem consideradas apenas as sessoes comepletas
	public BarChart generateActionRepetitionCountChart (List<TaskSmellAnalysis> tasks) {
		
		int top = 10;
		
		Map<String, Double> mostRepeatedActions = new HashMap<String, Double>(top);
		
		for (int i = 0; i < top; i++) {
			mostRepeatedActions.put(String.valueOf(i), 0.0);
		}
		
		for (TaskSmellAnalysis task : tasks) {
			
			Set<String> repeatedActions = new HashSet<String>();
			for (SessionResultDataMining session : task.getSessions()) {
				String previousAction = null;
				for (PageViewActionDataMining action : getOrderedActions(session)) {
					if (action.getPageViewActionUnique().equals(previousAction)) {
						repeatedActions.add(previousAction);
					}
					previousAction = action.getPageViewActionUnique();
				}
			}
			
			//Mapeamento de cada acao do conjunto com a lista de numeros de ocorrencia da mesma em cada sessao
			Map<String, List<Integer>> actionsRepetitionMapping = new HashMap<String, List<Integer>>();
			
			//Preenche o mapeamento com o maior numero de ocorrencias seguidas de cada acao em cada sessao
			for (String repeatedAction : repeatedActions) {
				actionsRepetitionMapping.put(repeatedAction, new ArrayList<Integer>());
				for (SessionResultDataMining session : task.getSessions()) {
					List<PageViewActionDataMining> actions = getOrderedActions(session);
					int repetition = 0, maxRepetition = 0;
					for (PageViewActionDataMining action : actions) {
						if (action.getPageViewActionUnique().equals(repeatedAction))
							repetition++;
						else
							repetition = 0;
						
						if (repetition > maxRepetition)
							maxRepetition = repetition;
					}
					actionsRepetitionMapping.get(repeatedAction).add(maxRepetition);
				}
			}
					
			//Preenche o mapeamento de acoes detectadas, desconsiderando as sessoes com numero de ocorrencias igual a zero
			for (String key : actionsRepetitionMapping.keySet()) {
				actionsRepetitionMapping.get(key).removeAll(Arrays.asList(0));
				double median = median(actionsRepetitionMapping.get(key).toArray(new Integer[actionsRepetitionMapping.get(key).size()]));
				String minKey = getMinKey(mostRepeatedActions);
				if (median > mostRepeatedActions.get(minKey)) {
					mostRepeatedActions.remove(minKey);
					mostRepeatedActions.put(key, median);
				}
			}
						
		}
		
		return new BarChart("Gráfico das Ações Mais Repetidas", "Proporção de amostras (%)", "Número de repetições", getSortedMap(mostRepeatedActions));
	}
	
	//Testar recebendo as acoes, em vez das sessoes, como entrada
	//Para isso separa-las por usuario e ordena-las, para cada usuario, por tempo
	public BarChart generateActionRepetitionCountChart2 (List<ActionDataMining> actions, int maxChartActionCount) {
		Map<String, List<ActionDataMining>> usersActions = new HashMap<String, List<ActionDataMining>>();
		for (ActionDataMining action : actions) {
			if (action.getsActionType().equals("mouseover"))
				continue;
			if (usersActions.get(action.getsUsername()) == null) {
				usersActions.put(action.getsUsername(), new ArrayList<ActionDataMining>());
				usersActions.get(action.getsUsername()).add(action);
			} else {
				usersActions.get(action.getsUsername()).add(action);
			}
		}
		for (List<ActionDataMining> userActions : usersActions.values()) {
			Collections.sort(userActions, new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					Long t1 = ((ActionDataMining) o1).getsTime();
					Long t2 = ((ActionDataMining) o2).getsTime();
					return t1 < t2 ? -1 : (t1 > t2 ? +1 : 0);
				}
			});
		}
		Map<String, List<Integer>> actionsRepetitions = new HashMap<String, List<Integer>>();
		for (List<ActionDataMining> userActions : usersActions.values()) {
			int repetitionCount = 1;
			String currentAction = "";
			String previousAction = "";
			for (int i = 1; i < userActions.size(); i++) {
				currentAction = getActionId(userActions.get(i));
				previousAction = getActionId(userActions.get(i-1));
				if (currentAction.equals(previousAction)) {
					repetitionCount++;
				} else {
					if (actionsRepetitions.get(previousAction) == null) {
						actionsRepetitions.put(previousAction, new ArrayList<Integer>());
						actionsRepetitions.get(previousAction).add(repetitionCount);
					} else {
						actionsRepetitions.get(previousAction).add(repetitionCount);
					}
					repetitionCount = 1;
				}
			}
			if (actionsRepetitions.get(currentAction) == null) {
				actionsRepetitions.put(currentAction, new ArrayList<Integer>());
				actionsRepetitions.get(currentAction).add(repetitionCount);
			} else {
				actionsRepetitions.get(currentAction).add(repetitionCount);
			}
		}
		Map<String, Double> mostRepeatedActions = new HashMap<String, Double>();
		for (int i = 0; i < maxChartActionCount; i++) {
			mostRepeatedActions.put(String.valueOf(i), 0.0);
		}
		for (String key : actionsRepetitions.keySet()) {
			double median = median(actionsRepetitions.get(key).toArray(new Integer[actionsRepetitions.get(key).size()]));
			String minKey = getMinKey(mostRepeatedActions);
			if (median > mostRepeatedActions.get(minKey)) {
				mostRepeatedActions.remove(minKey);
				mostRepeatedActions.put(key, median);
			}
		}
		return new BarChart("Gráfico das Ações Mais Repetidas", "Proporção de amostras (%)", "Número de repetições", getSortedMap(mostRepeatedActions));
	}
	
	//*********************************************************************************************
	//************************** MÉTODOS DE DETECÇÃO DE USABILITY SMELLS **************************
	//*********************************************************************************************
	
	
	//*********************************************************************************************
	//************************************ MÉTODOS AUXILIARES *************************************
	//*********************************************************************************************
	
	/**
	 * Retorna uma série de pontos (x,y) reprentando a distribuição de cada valor do conjunto de 
	 * dados de entrada em relação ao todo. Os dados são ordenados em ordem crescente para 
	 * facilitar a visualização. Independente da quantidade de dados de entrada, sempre são 
	 * retornados 100 pontos (x,y), permitindo que múltiplas séries sejam plotadas em um mesmo 
	 * gráfico. Se não houver valores no conjunto de entrada, o retorno é nulo.
	 *
	 * @param	serieKey	uma chave para identificar a série
	 * @param	dataset		um conjunto de valores
	 * @return				a série (x,y) representando a distribuição dos valores de entrada
	 */
	private XYSerie getDatasetDistributionSerie (String serieKey, List<Double> dataset) {
		if (dataset.size() == 0)
			return null;
		List<Double> formattedDataset = new ArrayList<Double>(dataset);
		XYSerie distribution = new XYSerie(serieKey);
		int proportionCount = 1;
		while (formattedDataset.size() < 100)
			formattedDataset.addAll(new ArrayList<Double>(dataset));
		Collections.sort(formattedDataset);
		if (formattedDataset.size() == 100) {
			for (int i = 0; i < formattedDataset.size(); i++) {
				distribution.addCoordinate(new XYCoordinate(proportionCount, formattedDataset.get(i)));
				proportionCount++;
			}
		} else {
			if (formattedDataset.size() < 100) {
				distribution.addCoordinate(new XYCoordinate(proportionCount, formattedDataset.get(0)/2));
				proportionCount++;
			}		
			for (int i = 1; i < formattedDataset.size(); i++) {
				double sampleProportion = ((double)(i+1)/formattedDataset.size())*100;
				if (sampleProportion > proportionCount) {
					distribution.addCoordinate(new XYCoordinate(proportionCount, (formattedDataset.get(i)+formattedDataset.get(i-1))/2));
					proportionCount++;
				} else if (sampleProportion == proportionCount) {
					distribution.addCoordinate(new XYCoordinate(proportionCount, formattedDataset.get(i)));
					proportionCount++;
				}
			}
		} 
		return distribution;
	}
	
	/**
	 * @param	map	um mapa de elementos
	 * @return		a chave do elemento com menor valor no mapa de entrada
	 */
	private String getMinKey (Map<String, Double> map) {
		String minKey = "";
		double minValue = Double.MAX_VALUE;
		for (Entry<String, Double> entry : map.entrySet()) {
			if (entry.getValue() < minValue) {
				minValue = entry.getValue();
				minKey = entry.getKey();
			}
		}
		return minKey;
	}
	
	/**
	 * @param	session	uma sessão de usuário
	 * @return			a lista de ações da sessão de entrada ordenadas pela data/hora de execução
	 */
	private List<PageViewActionDataMining> getOrderedActions (SessionResultDataMining session) {
		List<PageViewActionDataMining> actions = new ArrayList<PageViewActionDataMining>(session.getActions());
		Collections.sort(actions, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				Long t1 = ((PageViewActionDataMining) o1).getTime();
				Long t2 = ((PageViewActionDataMining) o2).getTime();
				return t1 < t2 ? -1 : (t1 > t2 ? +1 : 0);
			}
		});
		return actions;
	}
	
	/**
	 * @param	map	um mapa de elementos
	 * @return		o mapa de entrada ordenado pelo valor
	 */
	private Map<String, Double> getSortedMap (Map<String, Double> map) {
		List<Map.Entry<String, Double>> entries = new ArrayList<Map.Entry<String, Double>>(map.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> a, Map.Entry<String, Double> b){
				return a.getValue().compareTo(b.getValue());
			}
		});
		Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Map.Entry<String, Double> entry : entries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	
	/**
	 * @param	action	uma ação
	 * @return			o identificador único da ação de entrada
	 */
	private String getActionId (ActionDataMining action) {
		return action.getsUrl() + " | " + action.getsXPath() + " | " + action.getsActionType();
	}
	
	//FIM - Métodos auxiliares
	
	private void executeCommand(final String command) throws IOException {
        
        final ArrayList<String> commands = new ArrayList<String>();
        commands.add("/bin/bash");
        commands.add("-c");
        commands.add(command);
        
        BufferedReader br = null;        
        
        try {                        
            final ProcessBuilder p = new ProcessBuilder(commands);
            final Process process = p.start();
            final InputStream is = process.getInputStream();
            final InputStreamReader isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            
            String line;            
            while((line = br.readLine()) != null) {
                System.out.println("Retorno do comando = [" + line + "]");
            }
        } catch (IOException ioe) {
            log.severe("Erro ao executar comando shell" + ioe.getMessage());
            throw ioe;
        } finally {
            secureClose(br);
        }
    }
    
    private void secureClose(final Closeable resource) {
        try {
            if (resource != null) {
                resource.close();
            }
        } catch (IOException ex) {
            log.severe("Erro = " + ex.getMessage());
        }
    }
    
//    private void displayImage() throws IOException {
//        BufferedImage img=ImageIO.read(new File("grafo.png"));
//        ImageIcon icon=new ImageIcon(img);
//        JFrame frame=new JFrame();
//        frame.setLayout(new FlowLayout());
//        frame.setSize(img.getWidth(),img.getHeight()+50);
//        JLabel lbl=new JLabel();
//        lbl.setIcon(icon);
//        frame.add(lbl);
//        frame.setVisible(true);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//    }
	
//	private void show(Graph<String, DefaultWeightedEdge> g) throws IOException{
//		
//		List<String> vertices = new ArrayList<String>();
//		vertices.add(g.edgeSet().toString().replace(" ", "").replace("[", "").replace("]", ""));
//		Path file = Paths.get("vertices.txt");
//		Files.write(file, vertices, Charset.forName("UTF-8"));
//		
//        executeCommand("python graph_drawer.py vertices.txt");
//        executeCommand("eog grafo.png");
//		
////        displayImage();
//	}
	
	private void saveGraph(Graph<String, DefaultWeightedEdge> graph, String graphName, String folderName) throws IOException{
		List<String> vertices = new ArrayList<String>();
		String edges = graph.edgeSet().toString().replace(" ", "");
		vertices.add(edges.substring(1, edges.length()-1));
		Path file = Paths.get("vertices.txt");
		Files.write(file, vertices, Charset.forName("UTF-8"));
		
        executeCommand("python graph_drawer.py vertices.txt");
        new File("grafo.png").renameTo(new File(folderName + "/" + graphName + ".png"));
	}
	
	public List<TaskSmellAnalysis> detectLaboriousTasks(List<TaskSmellAnalysis> tasks, int maxActionCount, long maxTime) throws IOException{
		
		List<TaskSmellAnalysis> laboriousTasks = new ArrayList<TaskSmellAnalysis>();
		
		for (TaskSmellAnalysis task : tasks) {
			
			//Calcula o valor ideal para o atributo
			if (maxActionCount == NUMBER_DEFAULT) {
				//Constroi uma lista com todos os tamanhos de sessao
				List<Long> sessionSizes = new ArrayList<Long>();
				for (SessionResultDataMining session : task.getSessions()) {
					if (session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS)) {
						sessionSizes.add((long)session.getActions().size());
					}
				}
				//Calcula a barreira externa e define como valor otimo
				maxActionCount = (int)upperOuterFence(sessionSizes);
			}
			
			//Calcula o valor ideal para o atributo
			if (maxTime == TIME_DEFAULT) {
				//Constroi uma lista com todos os tempos de sessao
				List<Long> sessionTimes = new ArrayList<Long>();
				for (SessionResultDataMining session : task.getSessions()) {
					if (session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS)) {
						sessionTimes.add(session.getTime());
					}
				}
				//Calcula a barreira externa e define como valor otimo
				maxTime = upperOuterFence(sessionTimes);
			}
			
			//Cria um diretorio para armazenar os resultados das analises
			String folderName = task.getName() + "_LS_" + maxActionCount + "actions_" + maxTime + "milis";
			File dir = new File(folderName);
			dir.mkdir();
			
			//Cria uma variavel para armazenar o log
			StringBuilder log = new StringBuilder();
			
			List<SessionResultDataMining> laboriousSessions = new ArrayList<SessionResultDataMining>();
			int laboriousSessionCount = 0;
			int actionId[] = {1};
			Map<String, String> idMapping = new LinkedHashMap<String, String>();
			
			//Percorre cada sessao
			for (SessionResultDataMining session : task.getSessions()) {
				//TODO perguntar pro matheus se a quantidade de acoes de uma sessao pode ser = 0
				//e discutir sobre o estabelecimento de um limiar minimo para considerar a analise das sessoes, como um modo de eliminar o lixo
				if (session.getActions().size() > 0) {
					//Transforma a sessao em um grafo
					DirectedPseudograph<String, DefaultWeightedEdge> g = createGraphFromSession(session, idMapping, actionId);
					
					//Filtra as sessoes problematicas
					if (session.getActions().size() > maxActionCount && session.getTime() > maxTime) {
						laboriousSessions.add(session);
						laboriousSessionCount++;
						log.append("\nDetectada sessão " + session.getId() + "\n");
						log.append("Número de ações: " + session.getActions().size() + "\n");
						Long millis = session.getTime();
						log.append("Duração da sessão:" + String.format("%d min, %d sec", 
								TimeUnit.MILLISECONDS.toMinutes(millis),
								TimeUnit.MILLISECONDS.toSeconds(millis) - 
								TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
								) + "\n");
						
						saveGraph(g, session.getId(), folderName);
						
						//TODO conversar sobre a existencia de uma sessao de referencia (sequencia de acoes que devem ser realizadas para concluir a tarefa)
						
					}
				}
			}
			
			log.append("\nTotal de sessões: " + task.getSessions().size() + "\n");
			log.append("Sessões com grande esforço: " + laboriousSessionCount + "\n");
			
			//Salva o log em arquivo
			saveToFile(log.toString(), folderName + "/log.txt");
			
			//Cria um arquivo contendo todas os ids usados nos grafos e as respectivas ações que eles representam
			StringBuilder idMappingLog = new StringBuilder();
			for (Map.Entry<String, String> entry : idMapping.entrySet()) {
				idMappingLog.append(entry.getValue() + ";" + entry.getKey() + "\n");
			}
			saveToFile(idMappingLog.toString(), folderName + "/idMapping.csv");
			laboriousTasks.add(new TaskSmellAnalysis(task.getName(), laboriousSessions));
			maxActionCount = NUMBER_DEFAULT;
			maxTime = TIME_DEFAULT;
		}
		
		return laboriousTasks;		
	}
		
	public List<SessionResultDataMining> detectCyclicSessions(List<SessionResultDataMining> sessions, double maxCycleRate) throws IOException{
		
		int actionId[] = {1};
		Map<String, String> idMapping = new LinkedHashMap<String, String>();

		//Cria uma lista com todos os tamnhos de sessao
		List<Long> sessionSizes = new ArrayList<Long>();
		for (SessionResultDataMining session : sessions) {			
			if (session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS))
				sessionSizes.add((long)session.getActions().size());
		}
		//Determina um numero minimo de acoes para
		long lowerActionFence = lowerInnerFence(sessionSizes);
		
		//Define a barreira interna como valor otimo
		if (maxCycleRate == RATE_DEFAULT)
			maxCycleRate = 0.9;
		
		//Cria um diretorio para armazenar os resultados das analises
		String folderName = "CS_" + maxCycleRate + "rate";
		File dir = new File(folderName);
		dir.mkdir();
		
		//Cria uma variavel para armazenar o log
		StringBuilder log = new StringBuilder();
		
		List<SessionResultDataMining> cyclicSessions = new ArrayList<SessionResultDataMining>();
		
		//Percorre cada sessao
		for (SessionResultDataMining session : sessions) {
			//Exclui as sessoes de renicio e muito pequenas dos resultados
			if (session.getActions().size() > lowerActionFence && session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS)
					/*&& !session.getClassification().equals(SessionClassificationDataMiningEnum.REPEAT)*/
					/*&& session.getActions().size() > 0*/){
				//Transforma a sessao em um grafo
				DirectedPseudograph<String, DefaultWeightedEdge> g = createGraphFromSession(session, idMapping, actionId);
				
				//Encontra o conjunto de vertices contidos em ao menos um ciclo
				CycleDetector<String, DefaultWeightedEdge> cycleFinder = new CycleDetector<String, DefaultWeightedEdge>(g);
				Set<String> verticesContainedInCycles = cycleFinder.findCycles();
				
				//Conta a quantidade de acoes da sessao que correspondem aos vertices contidos em ciclos
				int actionCount = 0;
				for (String vertex : verticesContainedInCycles) {
					for (PageViewActionDataMining action : session.getActions()) {
						if (idMapping.get(action.getPageViewActionUnique()).equals(vertex)) {
							actionCount++;
						}
					}
				}
				
				//Calcula a proporcao entre a quantidade de vezes em que as acoes contidas em ciclos sao realizadas
				//e a quantidade total de acoes da sessao e verifica se essa proporcao nao excede o limiar
				double rate = (double) actionCount/session.getActions().size();
				if (rate > maxCycleRate) {
					log.append("\nDetectada sessão " + session.getId() + "\n");
					log.append("Taxa de ações cíclicas: " + (rate*100) + "%\n");
					cyclicSessions.add(session);
					saveGraph(g, session.getId(), folderName);
				}
			}
			
		}
		
		log.append("\n" + ((double)cyclicSessions.size()/sessions.size()*100) + "% de sessões cíclicas detectadas: "
				+ cyclicSessions.size() + " em um total de " + sessions.size() + "\n");
		
		//Salva o log em arquivo
		saveToFile(log.toString(), folderName + "/log.txt");
				
		//Cria um arquivo contendo todas os ids usados nos grafos e as respectivas ações que eles representam
		StringBuilder idMappingLog = new StringBuilder();
		for (Map.Entry<String, String> entry : idMapping.entrySet()) {
			idMappingLog.append(entry.getValue() + ";" + entry.getKey() + "\n");
		}
		saveToFile(idMappingLog.toString(), folderName + "/idMapping.csv");
		
		return cyclicSessions;
	}
	
	public List<String> detectLonelyActions(List<ActionDataMining> actions, double maxOccurrenceRate, int minNumberOfOccurrences) throws IOException{
			
		//Armazena todo o conjunto de urls diferentes da aplicacao
		List<ActionDataMining> filteredActions = getFilteredActions(actions);
		Collection<String> allUrls = CollectionUtils.collect(filteredActions, TransformerUtils.invokerTransformer("getsUrl"));
		Set<String> uniqueUrls = new HashSet<String>(allUrls);
		
		//Define o numero minimo de acoes como sendo a media do numero de acoes nas urls
		if (minNumberOfOccurrences == NUMBER_DEFAULT) {
			List<Long> sizes = new ArrayList<Long>();
			for (String url : uniqueUrls) {
				sizes.add((long)getUrlActions(filteredActions, url).size());
			}
			minNumberOfOccurrences = (int)longMedian(sizes);
		}

		//Cria uma variavel para armazenar o log
		StringBuilder log = new StringBuilder();
		
		//Cria um diretorio para armazenar os resultados das analises e define se a taxa de ocorrencia deve ser realculada
		String folderName = "LA_" + maxOccurrenceRate + "rate" + minNumberOfOccurrences + "occurrences";
		boolean recalcOcurrenceRate = false;
		if (maxOccurrenceRate == RATE_DEFAULT) {
			folderName = folderName.replace("-1.0", "VARIABLE");
			recalcOcurrenceRate = true;
		}
		File dir = new File(folderName);
		dir.mkdir();
		
		//Armazena as acoes detectadas
		List<String> lonelyActions = new ArrayList<String>();
		
		//Percorre o conjunto de urls
		for (String url : uniqueUrls) {
			
			//Constroi uma lista de ids unicos representando as acoes que ocorrem na url
			List<String> actionIds = getUrlActions(filteredActions, url);
			Map<String, String> actionsMapContent = getUrlActionsContent(filteredActions, url);
			
			//Verifica se a quantidade de acoes que ocorrem na url eh maior que o limiar e passa para a proxima iteracao (url) caso contrario
			if (actionIds.size() < minNumberOfOccurrences)
				continue;
			
			//Cria uma estrutura do tipo Multiset que armazena os ids diferentes e quantidade de ocorrencias de cada um
			Multiset<String> idsFrequencyMapping = HashMultiset.create();
			idsFrequencyMapping.addAll(actionIds);
			
			//Recalcula a taxa de ocorrencia para cada url
			if (recalcOcurrenceRate) {
				//Percorre o conjunto de ids unicos caculando a taxa de ocorrencia da acao que cada um  representa
				//e adcionando essas taxas em uma lista para calcular a barreira externa
//				List<Double> frequencyRates = new ArrayList<Double>();
//				for (String id : idsFrequencyMapping.elementSet()) {
//					double rate = (double)idsFrequencyMapping.count(id)/actionIds.size();
//					frequencyRates.add(rate);
//				}
//				maxOccurrenceRate = fences(frequencyRates, FENCE_TYPE_OUTER)[1];
				if (idsFrequencyMapping.size() > 1) {
					List<Double> frequencyRates = new ArrayList<Double>();
					for (String id : idsFrequencyMapping.elementSet()) {
						double rate = (double)idsFrequencyMapping.count(id)/actionIds.size();
						frequencyRates.add(rate);
					}
					Collections.sort(frequencyRates);
					maxOccurrenceRate = 5*frequencyRates.get(frequencyRates.size()-2);
				} else {
					maxOccurrenceRate = 0;
				}
			}
			
			String detectedActionId = "";
			double detectedActionRate = 0;
			int detectedActionCount = 0; 
			
			//Percorre o conjunto de ids unicos caculando a taxa de ocorrencia da acao que cada um  representa
			//e verificando se essa taxa é maior que o limiar passado como parametro
			for (String id : idsFrequencyMapping.elementSet()) {
				double rate = (double)idsFrequencyMapping.count(id)/actionIds.size();
				if (rate > maxOccurrenceRate){
					detectedActionId = id;
					detectedActionRate = rate;
					detectedActionCount++;
				}
				if (detectedActionCount > 1)
					break;
			}
			if (detectedActionCount == 1) {
				log.append("\nDetectada ação solitária: " + detectedActionId + " / content: " + actionsMapContent.get(detectedActionId) + "\n");
				log.append("Quantidade de ocorrências: " + idsFrequencyMapping.count(detectedActionId) + "\n");
				log.append("Taxa de ocorrência: " + (detectedActionRate*100) + "%\n");
				lonelyActions.add(detectedActionId);
			}
		}
		
		//Salva o log em arquivo
		saveToFile(log.toString(), folderName + "/log.txt");
		
		return lonelyActions;
		
	}
	
	public List<SessionResultDataMining> detectTooManyLayers(List<SessionResultDataMining> sessions, int maxNumberOfLayers) throws IOException{
		
		if (maxNumberOfLayers == NUMBER_DEFAULT) {
			maxNumberOfLayers = 25;
		}
		
		//Cria um diretorio para armazenar os resultados das analises
		String folderName = "TML_" + maxNumberOfLayers + "layers";
		File dir = new File(folderName);
		dir.mkdir();
		
		//Cria uma variavel para armazenar o log
		StringBuilder log = new StringBuilder();
		
		//Cria uma lista apenas com as sessoes bem sucedidas
		List<SessionResultDataMining> successSessions = new ArrayList<SessionResultDataMining>();
		for (SessionResultDataMining session : sessions) {
			if (session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS))
				successSessions.add(session);
		}
		
		List<Double> sessionsUrlCount = new ArrayList<Double>();
		List<SessionResultDataMining> detectedSessions = new ArrayList<SessionResultDataMining>();
		
		for (SessionResultDataMining session : successSessions) {
			Set<String> uniqueUrls = new HashSet<String>();
			for (PageViewActionDataMining action : session.getActions()) {
				uniqueUrls.add(action.getLocal());
			}
			sessionsUrlCount.add((double)uniqueUrls.size());
			if (uniqueUrls.size() > maxNumberOfLayers) {
				detectedSessions.add(session);
			}
		}
		
		if (median(sessionsUrlCount) > maxNumberOfLayers)
			log.append("\nSmell detectado: " + ((double)detectedSessions.size()/successSessions.size()*100) + "% das sessões afetadas\n");
		else
			log.append("\nSmell não detectado: " + ((double)detectedSessions.size()/successSessions.size()*100) + "% das sessões afetadas\n");
		log.append("Média de URLs da tarefa: " + (median(sessionsUrlCount)) + "\n");
		log.append("Lista de sessões detectadas:\n");
		for (SessionResultDataMining session : detectedSessions) {
			log.append("\n" + session.getId());
		}
		
		//Salva o log em arquivo
		saveToFile(log.toString(), folderName + "/log.txt");
		
		return detectedSessions;
	}
	
	public List<String> detectUndescriptiveElement (List<ActionDataMining> actions, int maxNumberOfAttempts) throws IOException {
		
		//Cria um diretorio para armazenar os resultados das analises
		String folderName = "UE_" + maxNumberOfAttempts + "attempts";
		File dir = new File(folderName);
		dir.mkdir();
		
		//Cria uma variavel para armazenar o log
		StringBuilder log = new StringBuilder();
		
		List<String> ids = new ArrayList<String>();
		List<String> undescriptiveElements = new ArrayList<String>();
		
		for (ActionDataMining action : actions) {
			if (action.getsActionType().equals("mouseover")) {
				ids.add(action.getsXPath() + " " + action.getsUrl());
			}
		}
		
		Multiset<String> elementsFrequencyMapping = HashMultiset.create();
		elementsFrequencyMapping.addAll(ids);
		
		for (String id : elementsFrequencyMapping.elementSet()) {
			if (elementsFrequencyMapping.count(id) > maxNumberOfAttempts) {
				log.append("\nDetectado elemento pouco descritivo: " + id + "\n");
				log.append("Quantidade de tentativas de tooltip: " + elementsFrequencyMapping.count(id) + "\n");
				undescriptiveElements.add(id);
			}
		}
		
		//Salva o log em arquivo
		saveToFile(log.toString(), folderName + "/log.txt");
		
		return undescriptiveElements;
	}
	
	
	/**
	 * Detecta o smell Missing Feedback
	 *
	 * @param sessions					lista de sessões relativas a uma tarefa.
	 * @param maxNumberOfRepetitions	número máximo de repetições sequenciais tolerado para uma ação que não contenha o smell.
	 * @param minNumberOfOcurrences		número mínimo de sessões contendo uma determinada ação para que ela possa ser considerada na análise.
	 */
	public Map<String, Double> detectMissingFeedback (List<SessionResultDataMining> sessions, int maxNumberOfRepetitions, int minNumberOfOcurrences) throws FileNotFoundException {
		//Cria diretorio para armazenar os resultados
		String folderName = "MF_" + maxNumberOfRepetitions + "repetitions";
		File dir = new File(folderName);
		dir.mkdir();
		
		//Armazena o log
		StringBuilder log = new StringBuilder();
		
		//Conjunto das acoes que se repetem em sequencia ao menos uma vez
		Set<String> repeatedActions = new HashSet<String>();
		
		//Percorre cada sessao adicionando ao conjunto as acoes correspondentes
		for (SessionResultDataMining session : sessions) {
			List<PageViewActionDataMining> actions = getOrderedActions(session);
			String previousAction = null;
			for (PageViewActionDataMining action : actions) {
				if (action.getPageViewActionUnique().equals(previousAction)) {
					repeatedActions.add(previousAction);
				}
				previousAction = action.getPageViewActionUnique();
			}
		}
		
		//Mapeamento de cada acao do conjunto com a lista de numeros de ocorrencia da mesma em cada sessao
		Map<String, List<Integer>> actionMapping = new HashMap<String, List<Integer>>();
		
		//Preenche o mapeamento com o maior numero de ocorrencias seguidas de cada acao em cada sessao
		for (String repeatedAction : repeatedActions) {
			actionMapping.put(repeatedAction, new ArrayList<Integer>());
			for (SessionResultDataMining session : sessions) {
				List<PageViewActionDataMining> actions = getOrderedActions(session);
				int repetition = 0, maxRepetition = 0;
				for (PageViewActionDataMining action : actions) {
					if (action.getPageViewActionUnique().equals(repeatedAction))
						repetition++;
					else
						repetition = 0;
					
					if (repetition > maxRepetition)
						maxRepetition = repetition;
				}
				actionMapping.get(repeatedAction).add(maxRepetition);
			}
		}
		
		//Mapeamento das acoes detectadas com o smell e mediana do seu respectivo numero de ocorrencias na tarefa
		Map<String, Double> detectedActions = new HashMap<String, Double>();
		
		//Preenche o mapeamento de acoes detectadas, desconsiderando as sessoes com numero de ocorrencias igual a zero
		for (String key : actionMapping.keySet()) {
			actionMapping.get(key).removeAll(Arrays.asList(0));
			if (actionMapping.get(key).size() >= minNumberOfOcurrences) {
				double median = median(actionMapping.get(key).toArray(new Integer[actionMapping.get(key).size()]));
				if (median > maxNumberOfRepetitions) {
					detectedActions.put(key, median);
					log.append("\nAção detectada: " + key + "\nNúmero de repetições: " + median + "\n");
				}
			}
		}
		
		//Salva o log em arquivo
		saveToFile(log.toString(), folderName + "/log.txt");
		
		return detectedActions;
		
	}
	
	//TODO eu nao sei o que era pra ser isso mas vou transformar na deteccao de uma acao que ocorre muito pouco em comparacao com as outras da mesma pagina
	public PageViewActionDataMining detectInvisibleAction(SessionResultDataMining referenceSession, List<SessionResultDataMining> userSessions){
		
		//Extrai a lista de acoes da sessao de referencia e ordena pelo tempo
		List<PageViewActionDataMining> referenceSessionActions = referenceSession.getActions();
		Collections.sort(referenceSessionActions, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				Long t1 = ((PageViewActionDataMining) o1).getTime();
				Long t2 = ((PageViewActionDataMining) o2).getTime();
				return t1 < t2 ? -1 : (t1 > t2 ? +1 : 0);
			}
		});
		
		//Conta em quantas sessoes cada acao da sessao de referencia eh realizada incorretamente
		int incorrectActionCount[] = new int[referenceSessionActions.size()];
		
		//Percorre cada sessao de usuario para fazer a comparacao das acoes com as da sessao de referencia
		for (SessionResultDataMining userSession : userSessions) {
			//Extrai a lista de acoes de cada sessao de usuario e ordena pelo tempo
			List<PageViewActionDataMining> userSessionActions = userSession.getActions();
			Collections.sort(userSessionActions, new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					Long t1 = ((PageViewActionDataMining) o1).getTime();
					Long t2 = ((PageViewActionDataMining) o2).getTime();
					return t1 < t2 ? -1 : (t1 > t2 ? +1 : 0);
				}
			});
			
			//Compara a acao da sessao de referencia com a acao da sessao de usuario na mesma posicao
			//e incrementa o contador, na posicao correspondente, caso sejam diferentes
			for (int i = 0; i < incorrectActionCount.length; i++) {
				if (!userSessionActions.get(i).getPageViewActionUnique()
						.equals(referenceSessionActions.get(i).getPageViewActionUnique())) {
					incorrectActionCount[i] += 1;
				}
			}
			
		}
		
		//Verifica a contagem e retorna a primeira acao da sessao de referencia que eh realizada incorretamente
		//em mais de 50% das sessoes
		for (int i = 0; i < incorrectActionCount.length; i++) {
			if (incorrectActionCount[i] > (userSessions.size()/2)) {
				System.out.println("A ação " + referenceSession.getActions().get(i).getPageViewActionUnique() +
						" não foi realizada no momento correto em " + (incorrectActionCount[i]/userSessions.size()) +
						"% das sessões");
				return referenceSession.getActions().get(i);
			}
		}
		
		//Senao encontrar nenhuma acao que satisfacam as condicoes estabelecidas acima, retorna nulo
		System.out.println("Não foi encontrada nenhuma ação invisível, o retorno é nulo");
		return null;
	}
	
	public StackedAreaChart generateTaskCycleRateChart (List<TaskSmellAnalysis> tasks) throws IOException {
		
		List<XYSerie> series = new ArrayList<XYSerie>();
		for (TaskSmellAnalysis task : tasks) {
			Map<String, String> idMapping = new HashMap<String, String>();
			int[] actionId = {1};
			//Cria uma lista com todas as taxas de ciclo das sessoes
			List<Double> taskCycleRateDataset = new ArrayList<Double>();
			for (SessionResultDataMining session : task.getSessions()) {			
				//Calcula a proporcao entre o numero de vertices que aparecem em ciclos e o 
				//total de vertices da sessao e adiciona essa proporcao na lista
				if (session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS)) {
					//Calcula a taxa de ciclos da sessao
					taskCycleRateDataset.add(sessionCycleRate(session, idMapping, actionId)*100);
				}
			}
//			Collections.sort(taskCycleRateDataset);
			series.add(getDatasetDistributionSerie(task.getName(), taskCycleRateDataset));
		}
		
		//saveDatasetChart(taskName, "", "Proporção de amostras (%)", "Taxa cíclica (%)", datasets);	
		return new StackedAreaChart("Gráfico de Taxa de Ciclos", "Proporção de sessões da tarefa (%)", "Taxa de ciclos (%)", series);
	}
	
	public BarChart generateActionOccurrenceRateChart (List<ActionDataMining> actions) {
		
		List<ActionDataMining> filteredActions = getFilteredActions(actions);
		Collection<String> allUrls = CollectionUtils.collect(filteredActions, TransformerUtils.invokerTransformer("getsUrl"));
		Set<String> uniqueUrls = new HashSet<String>(allUrls);
		
		List<Integer> sizes = new ArrayList<Integer>();
		for (String url : uniqueUrls) {
			sizes.add(getUrlActions(filteredActions, url).size());
		}
		int minNumberOfOccurrences = (int) median(sizes.toArray(new Integer[0]));
		
		Map<String, Double> mostFrequentActions = new LinkedHashMap<String, Double>();
		for (int i = 0; i < 10; i++) {
			mostFrequentActions.put(String.valueOf(i), 0.0);
		}
			
		for (String url : uniqueUrls) {
			List<String> actionIds = getUrlActions(filteredActions, url);				
			if (actionIds.size() < minNumberOfOccurrences)
				continue;
			Multiset<String> idsFrequencyMapping = HashMultiset.create();
			idsFrequencyMapping.addAll(actionIds);
			for (String id : idsFrequencyMapping.elementSet()) {
				double rate = (double)idsFrequencyMapping.count(id)/actionIds.size()*100;
				String minKey = getMinKey(mostFrequentActions);	
				if (rate > mostFrequentActions.get(minKey)){
					mostFrequentActions.remove(minKey);
					mostFrequentActions.put(id, rate);
				}
			}
		}
		
		return new BarChart("Gráfico das Ações Mais Frequentes", "Ações", "Taxa de ocorrência (%)", getSortedMap(mostFrequentActions));
	}
	
	private DirectedPseudograph<String, DefaultWeightedEdge> createGraphFromSession(SessionResultDataMining session, Map<String, String> idMapping, int actionId[]){
		
		String lastAction = null;
		
		//Pega a lista de acoes da sessao e ordena pelo tempo
		List<PageViewActionDataMining> actions = session.getActions();
		Collections.sort(actions, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				Long t1 = ((PageViewActionDataMining) o1).getTime();
				Long t2 = ((PageViewActionDataMining) o2).getTime();
				return t1 < t2 ? -1 : (t1 > t2 ? +1 : 0);
			}
		});
		
		//Cria um grafo para representar a sessao e adiciona o primeiro no de acordo com o mapeamento de ids
		DirectedPseudograph<String, DefaultWeightedEdge> g =
				new DirectedPseudograph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		if(idMapping.containsKey(actions.get(0).getPageViewActionUnique())){
			lastAction = idMapping.get(actions.get(0).getPageViewActionUnique());
			g.addVertex(lastAction);
		}
		else{
			idMapping.put(actions.get(0).getPageViewActionUnique(), String.valueOf(actionId[0]));
			lastAction = idMapping.get(actions.get(0).getPageViewActionUnique());
			g.addVertex(lastAction);
			actionId[0] += 1;
		}
		
		//Percorre a lista de acoes, a partir da segunda, e vai adicionando ao grafo e ligando com as anteriores
		for (PageViewActionDataMining action : actions.subList(1, actions.size())) {
			
			if (idMapping.containsKey(action.getPageViewActionUnique()))
				g.addVertex(idMapping.get(action.getPageViewActionUnique()));
			else{
				idMapping.put(action.getPageViewActionUnique(), String.valueOf(actionId[0]));
				g.addVertex(idMapping.get(action.getPageViewActionUnique()));
				actionId[0] += 1;
			}
							
			g.addEdge(lastAction, idMapping.get(action.getPageViewActionUnique()));
			lastAction = idMapping.get(action.getPageViewActionUnique());
			
		}
		
		return g;
		
	}
	
	private double sessionCycleRate (SessionResultDataMining session, Map<String, String> idMapping, int[] actionId) {
		//Transforma a sessao em um grafo
		DirectedPseudograph<String, DefaultWeightedEdge> g = createGraphFromSession(session, idMapping, actionId);
		
//		SzwarcfiterLauerSimpleCycles<String, DefaultWeightedEdge> cycleFinder = new SzwarcfiterLauerSimpleCycles<String, DefaultWeightedEdge>(g);
//		//Cria um conjunto com todos os vertices que fazem parte de ciclos nessa sessao
//		Set<String> verticesContainedInCycles = new HashSet<String>();
//		for (List<String> cycleVertices : cycleFinder.findSimpleCycles()) {
//			for (String vertice : cycleVertices) {
//				verticesContainedInCycles.add(vertice);
//			}
//		}
		
		CycleDetector<String, DefaultWeightedEdge> cycleFinder = new CycleDetector<String, DefaultWeightedEdge>(g);
		Set<String> verticesContainedInCycles = cycleFinder.findCycles();
		
		int actionCount = 0;
		for (String vertex : verticesContainedInCycles) {
			for (PageViewActionDataMining action : session.getActions()) {
				if (idMapping.get(action.getPageViewActionUnique()).equals(vertex)) {
					actionCount++;
				}
			}
		}
		
		return (double) actionCount/session.getActions().size();
	}
	
	private double longMedian (List<Long> values) {
		double median;
		if (values.size()%2==0)
			median = (double)(values.get(values.size()/2) + values.get(values.size()/2-1))/2;
		else
			median = (double)values.get(values.size()/2);		
		return median;
	}
	
	private double median (Integer values[]) {
		Integer sortedValues[] = Arrays.copyOf(values, values.length);
		Arrays.sort(sortedValues);
		if (sortedValues.length == 1)
			return sortedValues[0];
		double median;
		if (sortedValues.length%2==0)
			median = (double)(sortedValues[sortedValues.length/2] + sortedValues[sortedValues.length/2-1])/2;
		else
			median = (double)sortedValues[sortedValues.length/2];		
		return median;
	}
	
	private long upperOuterFence (List<Long> values){		
		List<Long> numbers = new ArrayList<Long>(values);	
		Collections.sort(numbers, new Comparator<Long>() {
			@Override
			public int compare(Long n1, Long n2) {
				return n1.compareTo(n2);
			}
		});
//		double mean = mean(sessionSizes);
		double q1 = longMedian(numbers.subList(0, numbers.size()/2-1));
		double q3;
		if (numbers.size()%2==0)
			q3 = longMedian(numbers.subList(numbers.size()/2, numbers.size()-1));
		else
			q3 = longMedian(numbers.subList(numbers.size()/2+1, numbers.size()-1));
		return (long)(q3 + (q3-q1)*3);
	}
	
	private long lowerInnerFence (List<Long> values){		
		List<Long> numbers = new ArrayList<Long>(values);	
		Collections.sort(numbers, new Comparator<Long>() {
			@Override
			public int compare(Long n1, Long n2) {
				return n1.compareTo(n2);
			}
		});
//		double mean = mean(sessionSizes);
		double q1 = longMedian(numbers.subList(0, numbers.size()/2-1));
		double q3;
		if (numbers.size()%2==0)
			q3 = longMedian(numbers.subList(numbers.size()/2, numbers.size()-1));
		else
			q3 = longMedian(numbers.subList(numbers.size()/2+1, numbers.size()-1));
		return (long)(q1 - (q3-q1)*1.5);
	}
	
	private double median (List<Double> values) {
		if (values.size()%2==0)
			return (values.get(values.size()/2) + values.get(values.size()/2-1))/2;
		else
			return values.get(values.size()/2);		
	}
	
	private double[] fences (List<Double> dataset, double fenceType){		
		double[] fences = new double[2];
		List<Double> orderedDataset = new ArrayList<Double>(dataset);	
		Collections.sort(orderedDataset, new Comparator<Double>() {
			@Override
			public int compare(Double n1, Double n2) {
				return n1.compareTo(n2);
			}
		});
		double q1 = median(orderedDataset.subList(0, orderedDataset.size()/2-1));
		double q3;
		if (orderedDataset.size()%2==0)
			q3 = median(orderedDataset.subList(orderedDataset.size()/2, orderedDataset.size()-1));
		else
			q3 = median(orderedDataset.subList(orderedDataset.size()/2+1, orderedDataset.size()-1));
		fences[0] = (q1 - (q3-q1)*fenceType);
		fences[1] = (q3 + (q3-q1)*fenceType);
		return fences;
	}
	
	private List<Double> standardScores (List<Double> values) {
		List<Double> scores = new ArrayList<Double>();
		
		double average = 0;
		for (Double value : values)
			average += value;
		average = average/values.size();
		
		double variance = 0;
		for (Double value : values)
			variance += Math.pow((value - average), 2);
		variance = variance/(values.size()-1);
		
		double standardDeviation = Math.sqrt(variance);
		
		for (Double value : values) {
			scores.add((value-average)/standardDeviation);
		}
		
		return scores;
	}
	
	private void saveToFile (String text, String name) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(new File(name));
		writer.write(text);
		writer.close();	
	}
	
	//Remove a parte do jsessionid da lista de acoes
	private List<ActionDataMining> getFilteredActions (List<ActionDataMining> actions) {
		List<ActionDataMining> filteredActions = new ArrayList<ActionDataMining>(actions);
		for (ActionDataMining action : filteredActions) {
			if (action.getsUrl().contains("jsessionid"))
				action.setsUrl(action.getsUrl().split(";jsessionid")[0]);
		}
		return filteredActions;
	}
	
	//Constroi uma lista de ids unicos (Tipo de Acao + XPath + URL) para representar cada uma das acoes que ocorrem na url
	private List<String> getUrlActions(List<ActionDataMining> actions, String url) {
		List<String> actionIds = new ArrayList<String>();
		for (ActionDataMining action : actions) {
			if (action.getsUrl().equals(url) && !action.getsActionType().equals("mouseover") && !action.getsActionType().equals("onload")) {
				actionIds.add(url + " | " + action.getsXPath() + " | " + action.getsActionType());
//				actionIds.add(action.getsActionType() + " " + action.getsXPath() + " " + url);
			}
		}
		return actionIds;
	}
	
	private Map<String, String> getUrlActionsContent(List<ActionDataMining> actions, String url) {
		Map<String, String> actionContent = new HashMap<String, String>();
		for (ActionDataMining action : actions) {
			if (action.getsUrl().equals(url) && !action.getsActionType().equals("mouseover") && !action.getsActionType().equals("onload")) {
				String actionId = url + " | " + action.getsXPath() + " | " + action.getsActionType();
				if (actionContent.get(actionId) == null) {
					actionContent.put(actionId, action.getsContent());
					System.out.println(actionId + " -> " + action.getsContent());
				}
			}
		}
		return actionContent;
	}
	
}