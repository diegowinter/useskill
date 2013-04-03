package br.ufpi.repositories.Implement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import br.com.caelum.vraptor.ioc.Component;
import br.ufpi.models.Action;
import br.ufpi.models.Fluxo;
import br.ufpi.models.Tarefa;
import br.ufpi.models.TipoConvidado;
import br.ufpi.models.vo.FluxoCountVO;
import br.ufpi.models.vo.FluxoVO;
import br.ufpi.models.vo.TarefaVO;
import br.ufpi.repositories.Repository;
import br.ufpi.repositories.TarefaRepository;
import br.ufpi.util.Paginacao;

@Component
public class TarefaRepositoryImpl extends Repository<Tarefa, Long> implements
		TarefaRepository {

	public TarefaRepositoryImpl(EntityManager entityManager) {
		super(entityManager);
	}

	@Override
	public Tarefa pertenceTeste(Long idTarefa, Long idTeste, Long idUsuario) {
		Query query = entityManager
				.createNamedQuery("Tarefa.pertence.Teste.Usuario");
		query.setParameter("teste", idTeste);
		query.setParameter("tarefa", idTarefa);
		query.setParameter("usuario", idUsuario);
		try {
			return (Tarefa) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}

	}

	@Override
	public Tarefa perteceTesteNaoRealizado(Long idTarefa, Long idTeste,
			Long idUsuario) {
		try {
			Query query = entityManager
					.createNamedQuery("Tarefa.pertence.Teste.Nao.Liberado.Usuario");
			query.setParameter("teste", idTeste);
			query.setParameter("tarefa", idTarefa);
			query.setParameter("usuario", idUsuario);
			return (Tarefa) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public String getRoteiro(Long idTarefa, Long idTeste) {
		Query query = entityManager
				.createNamedQuery("Tarefa.pertence.Teste.GetRoteiro");
		query.setParameter("teste", idTeste);
		query.setParameter("tarefa", idTarefa);
		try {
			return (String) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public String getTitulo(Long idTarefa, Long idTeste) {
		Query query = entityManager
				.createNamedQuery("Tarefa.pertence.Teste.GetNome");
		query.setParameter("teste", idTeste);
		query.setParameter("tarefa", idTarefa);
		try {
			return (String) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public TarefaVO getTarefaVO(Long idTarefa, Long idTeste) {
		Query query = entityManager
				.createNamedQuery("Tarefa.pertence.Teste.GetTarefaVO");
		query.setParameter("teste", idTeste);
		query.setParameter("tarefa", idTarefa);
		try {
			return (TarefaVO) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Fluxo> getFluxo(Long testeId, Long tarefaId, Long usarioId,
			Long usuarioCriadorId) {

		Query query = entityManager.createNamedQuery("Fluxo.obterfluxos");
		query.setParameter("teste", testeId);
		query.setParameter("tarefa", tarefaId);
		query.setParameter("usuarioCriador", usuarioCriadorId);
		query.setParameter("usuario", usarioId);
		try {
			return (List<Fluxo>) query.getResultList();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Fluxo getFluxo(Long testeId, Long tarefaId, Long usarioId,
			Long usuarioCriadorId, Long fluxoId) {

		Query query = entityManager.createNamedQuery("Fluxo.obterfluxo");
		query.setParameter("teste", testeId);
		query.setParameter("tarefa", tarefaId);
		query.setParameter("usuarioCriador", usuarioCriadorId);
		query.setParameter("usuario", usarioId);
		query.setParameter("fluxo", fluxoId);
		try {
			return (Fluxo) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Action> getAcoesFluxo(Long testeId, Long tarefaId,
			Long usarioId, Long usuarioCriadorId, Long fluxoId) {

		Query query = entityManager.createNamedQuery("Fluxo.obterActions");
		query.setParameter("teste", testeId);
		query.setParameter("tarefa", tarefaId);
		query.setParameter("usuarioCriador", usuarioCriadorId);
		query.setParameter("usuario", usarioId);
		query.setParameter("fluxo", fluxoId);
		try {
			return (List<Action>) query.getResultList();
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Paginacao<FluxoVO> getFluxos(Long tarefaId, Long testeId,
			Long usuarioDonoTeste, int quantidade, int numeroPagina) {
		String namedQuery = "Fluxo.getFluxos.Tarefa";
		Paginacao<FluxoVO> paginacao = new Paginacao<FluxoVO>();
		Query query = entityManager.createNamedQuery(namedQuery);
		query.setParameter("teste", testeId);
		query.setParameter("tarefa", tarefaId);
		query.setParameter("usuarioCriador", usuarioDonoTeste);
		query.setFirstResult(quantidade * (numeroPagina - 1));
		query.setMaxResults(quantidade);
		paginacao.setListObjects((List<FluxoVO>) query.getResultList());
		Query count = entityManager.createNamedQuery(namedQuery + ".Count");
		count.setParameter("tarefa", tarefaId);
		count.setParameter("teste", testeId);
		count.setParameter("usuarioCriador", usuarioDonoTeste);
		paginacao.setCount((Long) count.getSingleResult());
		return paginacao;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.ufpi.repositories.TarefaRepository#quantidadeAcoes(java.lang.Long)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<FluxoCountVO> quantidadeAcoesETempo(Long tarefa,
			TipoConvidado tipoConvidado) {
		Query query = entityManager.createNamedQuery("Fluxo.quantidade.Acoes");
		query.setParameter("tarefa", tarefa);
		query.setParameter("tipoConvidado", tipoConvidado);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FluxoVO> getFluxoUsuario(Long tarefaId, Long usuarioId) {
		Query query = entityManager.createNamedQuery("Fluxo.fluxosdoUsuario");
		query.setParameter("tarefa", tarefaId);
		query.setParameter("usuario", usuarioId);
		return query.getResultList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.ufpi.repositories.TarefaRepository#getNameUsuario(java.lang.Long)
	 */
	@Override
	public String getNameUsuario(Long fluxoId) {
		Query query = entityManager.createNamedQuery("Fluxo.getNomeUsuario");
		query.setParameter("fluxo", fluxoId);
		return (String) query.getSingleResult();
	}

	@Override
	public Map<TipoConvidado, Long> quantidadeFluxos(Long tarefa,
			boolean isFinished) {
		HashMap<TipoConvidado, Long> map = new HashMap<TipoConvidado, Long>();
		for (TipoConvidado tipoConvidado : TipoConvidado.values()) {
			Query query = entityManager
					.createNamedQuery("Fluxo.realizados");
			query.setParameter("tarefa", tarefa);
			query.setParameter("tipoConvidado", tipoConvidado);
			query.setParameter("isFinished", isFinished);
			map.put(tipoConvidado, (Long) query.getSingleResult());
		}
		return map;
	}

}
