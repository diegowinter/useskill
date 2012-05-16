package br.ufpi.controllers;

import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.Validator;
import br.ufpi.annotation.Logado;
import br.ufpi.componets.SessionFluxoTarefa;
import br.ufpi.componets.TesteSession;
import br.ufpi.componets.TesteView;
import br.ufpi.componets.UsuarioLogado;
import br.ufpi.models.Convidado;
import br.ufpi.models.Teste;
import br.ufpi.repositories.ConvidadoRepository;

/**
 * @author Cleiton
 * 
 */
@Path(value = "teste/participar")
@Resource
public class TesteParticiparController extends BaseController{
	private final ConvidadoRepository convidadoRepository;
	private final SessionFluxoTarefa fluxoTarefa;
	private final TesteSession testeSession;

	
	public TesteParticiparController(Result result, Validator validator,
			TesteView testeView, UsuarioLogado usuarioLogado,
			ConvidadoRepository convidadoRepository,
			SessionFluxoTarefa fluxoTarefa, TesteSession testeSession) {
		super(result, validator, testeView, usuarioLogado);
		this.convidadoRepository = convidadoRepository;
		this.fluxoTarefa = fluxoTarefa;
		this.testeSession = testeSession;
	}

	@Logado
	@Post
	public void negar(Long testeId) {
		Convidado convidado = new Convidado(usuarioLogado.getUsuario().getId(),
				testeId);
		convidado.setRealizou(false);
		convidadoRepository.update(convidado);
		result.redirectTo(LoginController.class).logado();
	}

	/**
	 * O Usuario aceita o Teste
	 * @param testeId
	 */
	@Post
	public void aceitar(Long testeId) {
		usuarioConvidado(testeId);
		Teste teste = testeSession.getTeste();
		fluxoTarefa.criarLista(teste);
		}

	@Logado
	@Get
	public void termino() {
	
	}

	private void usuarioConvidado(Long testeId) {
		if (testeId != null) {
			this.usuarioLogado.getUsuario();
			Teste testeConvidado = convidadoRepository.getTesteConvidado(
					testeId, usuarioLogado.getUsuario().getId());
			if (testeConvidado != null) {
				testeSession.setTeste(testeConvidado);
			} else {
				result.notFound();
			}
		}
	}

}
