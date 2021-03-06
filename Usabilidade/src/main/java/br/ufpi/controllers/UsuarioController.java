package br.ufpi.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import br.com.caelum.vraptor.Delete;
import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Put;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.Validator;
import br.com.caelum.vraptor.validator.Validations;
import br.ufpi.annotation.AcessoLivre;
import br.ufpi.annotation.Logado;
import br.ufpi.componets.TesteView;
import br.ufpi.componets.UsuarioLogado;
import br.ufpi.componets.ValidateComponente;
import br.ufpi.models.Usuario;
import br.ufpi.repositories.UsuarioRepository;
import br.ufpi.util.BaseUrl;
import br.ufpi.util.Criptografa;
import br.ufpi.util.EmailUtils;

@Resource
public class UsuarioController extends BaseController {

	private final UsuarioRepository usuarioRepository;
	private final HttpServletRequest request;

	public UsuarioController(Result result, Validator validator,
			TesteView testeView, UsuarioLogado usuarioLogado,
			ValidateComponente validateComponente,
			UsuarioRepository usuarioRepository, HttpServletRequest request) {
		super(result, validator, testeView, usuarioLogado, validateComponente);
		this.usuarioRepository = usuarioRepository;
		this.request = request;
	}

	@Get("/usuarios")
	@Logado
	public List<Usuario> index() {
		return usuarioRepository.findAll();
	}

	@Post("/usuarios")
	@AcessoLivre
	public void create(Usuario usuario, String confirmaSenha) {
		nullValidate(usuario);
		validator.validate(usuario);
		if (usuarioRepository.isContainsEmail(usuario.getEmail())) {
			validator.checking(new Validations() {
				{
					that(false, "email.cadastrado", "email.cadastrado");
				}
			});
		}
		
		//validar confirmacao de senha
		validateSenha(usuario.getSenha(), confirmaSenha);
		validator.onErrorUsePageOf(this).newUsuario();
		
		usuario.setRepository(usuarioRepository);
		usuario.criptografarSenhaGerarConfimacaoEmail(true);
		usuario.setEmailConfirmado(true);
		usuarioRepository.create(usuario);
		
		BaseUrl.getInstance(request);
		EmailUtils emailUtils = new EmailUtils();
		emailUtils.enviarEmailConfirmacao(usuario);
		result.redirectTo(LoginController.class).login(usuario.getEmail());
	}

	@AcessoLivre
	@Get("/usuarios/new")
	public Usuario newUsuario() {
		return new Usuario();
	}

	@Logado
	@Put("/usuarios")
	public void update(Usuario usuario) {
		validate(usuario);
		Usuario usuarioNovo = usuarioRepository.find(usuario.getId());
		if (!usuario.getEmail().equals(usuarioNovo.getEmail())) {
			if (usuarioRepository.isContainsEmail(usuario.getEmail())) {
				validator.checking(new Validations() {
					{
						that(false, "email.cadastrado", "email.cadastrado");
					}
				});
			}
		}
		validator.onErrorUsePageOf(this).edit(usuarioNovo);
		usuarioNovo.setNome(usuario.getNome());
		usuarioNovo.setTelefones(usuario.getTelefones());
		if (!usuario.getEmail().equals(usuarioNovo.getEmail())) {
			usuarioNovo.setEmail(usuario.getEmail());
			usuario.criptografarSenhaGerarConfimacaoEmail(true);
			EmailUtils emailUtils = new EmailUtils();
			emailUtils.enviarEmailConfirmacao(usuario);
		}
		validator.validate(usuarioNovo);
		validator.onErrorUsePageOf(this).edit(usuarioNovo);
		usuarioRepository.update(usuarioNovo);
		usuarioLogado.setUsuario(usuarioNovo);
		result.redirectTo(LoginController.class).logado(1);
	}

	@Logado
	@Get("/usuarios/{usuario.id}/edit")
	public Usuario edit(Usuario usuario) {
		validate(usuario);
		Usuario usuario1 = usuarioRepository.find(usuario.getId());
		result.include("contador", usuario1.getTelefones().size());
		return usuario1;
	}
	
	@Logado
	@Get("/usuario/alterarsenha")
	public void alterarsenha() {
	}

	@Post("/usuario/alterarSenha")
	public void alterarSenha(String senhaAntiga, String senha, String confirmacaoSenha) {
		validateComponente.validarString(senha, "login.novasenha");
		validateComponente.validarString(confirmacaoSenha, "login.confirmanovasenha");
		validateComponente.validarString(senhaAntiga, "login.senhaantiga");
		
		validateSenha(senha, confirmacaoSenha);
		validator.onErrorRedirectTo(this).alterarsenha();
			
		String senhaCriptografada = Criptografa.criptografar(senhaAntiga);
		if (senhaCriptografada.equals(usuarioLogado.getUsuario().getSenha())) {
			Usuario usuarioAtualizar = usuarioRepository.find(usuarioLogado.getUsuario().getId());
			usuarioAtualizar.setSenha(Criptografa.criptografar(senha));
			usuarioRepository.update(usuarioAtualizar);
			usuarioLogado.setUsuario(usuarioAtualizar);
			result.include("sucesso", "login.alterarsenha.sucesso");
			result.redirectTo(this).alterarsenha();
			// Gerar mensagem que o usuario conseguiu alterar senha com sucesso
		} else {
			// Gerar mensagem de senha antiga não esta igual
			validateComponente.gerarErro("usuario.confirmaSenha.errado","login.alterarsenha.erroantigadiferente");
		}
		
		validator.onErrorRedirectTo(this).alterarsenha();
			
	}

	@Logado
	@Get("/usuarios/{usuario.id}")
	public Usuario show(Usuario usuario) {
		validate(usuario);
		return usuarioRepository.find(usuario.getId());
	}

	@Logado
	@Delete("/usuarios/{usuario.id}")
	public void destroy(Usuario usuario) {
		validate(usuario);
		usuarioRepository.destroy(usuarioRepository.find(usuario.getId()));
		result.redirectTo(this).index();
	}

	/**
	 * Usado pra analisar se usuario da section é o mesmo que e pra ter os dados
	 * alterados
	 * 
	 * @param usuario
	 */
	private void validate(Usuario usuario) {
		if (!usuario.getId().equals(usuarioLogado.getUsuario().getId())){
			validateComponente.redirecionarHome("usuario.nao.pertimidido");
		}
	}
	
	private boolean validateSenha(String senha, String confirmaSenha){
		boolean validate = true;
		if(senha.length()<6){
			validateComponente.gerarErro("senha.curta", "senha.curta");
			validate = false;
		}
		if(!confirmaSenha.equals(senha)){
			validateComponente.gerarErro("usuario.confirmaSenha.errado","usuario.confirmaSenha.errado");
			validate = false;
		}
		return validate;
	}
	
	private void nullValidate(Usuario usuario) {
		validateComponente.validarString(usuario.getNome(), "usuario.nome");
		validateComponente.validarString(usuario.getEmail(), "usuario.email");
		validateComponente.validarString(usuario.getSenha(), "usuario.senha");
	}

}