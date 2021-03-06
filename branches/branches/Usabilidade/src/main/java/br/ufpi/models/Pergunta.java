/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufpi.models;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 * 
 * @author Cleiton
 */
@NamedQueries({
		@NamedQuery(name = "Pergunta.pertence.teste.usuario", query = "select perguntas from Teste as t left join t.satisfacao lef join t.satisfacao.perguntas as perguntas where t.id= :teste and t.usuarioCriador.id= :usuario and perguntas.id= :pergunta"),
//		@NamedQuery(name = "Pergunta.pertence.tarefa.usuario", query = "select perguntas from Teste as t lef join t.tarefas as tarefas lef join tarefas.questionario left join tarefas.questionario.perguntas as perguntas where t.id= :teste and t.usuarioCriador.id= :usuario and perguntas.id= :pergunta and tarefas.id= :tarefa and t.liberado= false"),
		@NamedQuery(name = "Pergunta.pertence.teste.usuario.Liberado", query = "select perguntas from Teste as t left join t.satisfacao lef join t.satisfacao.perguntas as perguntas where t.id= :teste and t.usuarioCriador.id= :usuario and perguntas.id= :pergunta and t.liberado= :liberado"),
		@NamedQuery(name = "Pergunta.delete.Alternativas", query = "delete Alternativa  a where a.pergunta.id= :pergunta"),
		@NamedQuery(name = "Pergunta.getQuestionario", query = "select p.questionario from Pergunta as p left join p.questionario as Questionario where p.id= :pergunta") })
@Entity
public class Pergunta implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String titulo;
	@Column(columnDefinition = "TEXT")
	private String texto;
	/**
	 * True para pergunta objetiva
	 */
	private Boolean tipoRespostaAlternativa;
	@OneToMany(mappedBy = "pergunta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Alternativa> alternativas;
	@ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.MERGE)
	private Questionario questionario;
	@OneToMany(mappedBy = "pergunta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<RespostaAlternativa> respostaAlternativas;
	@OneToMany(mappedBy = "pergunta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<RespostaEscrita> respostaEscritas;
	private boolean responderFim;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<Alternativa> getAlternativas() {
		return alternativas;
	}

	public void setAlternativas(List<Alternativa> alternativas) {
		this.alternativas = alternativas;
	}

	public Questionario getQuestionario() {
		return questionario;
	}

	public void setQuestionario(Questionario questionario) {
		this.questionario = questionario;
	}

	public List<RespostaAlternativa> getRespostaAlternativas() {
		return respostaAlternativas;
	}

	public void setRespostaAlternativas(
			List<RespostaAlternativa> respostaAlternativas) {
		this.respostaAlternativas = respostaAlternativas;
	}

	public List<RespostaEscrita> getRespostaEscritas() {
		return respostaEscritas;
	}

	public void setRespostaEscritas(List<RespostaEscrita> respostaEscritas) {
		this.respostaEscritas = respostaEscritas;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public Boolean getTipoRespostaAlternativa() {
		return tipoRespostaAlternativa;
	}

	public void setTipoRespostaAlternativa(Boolean tipoRespostaAlternativa) {
		this.tipoRespostaAlternativa = tipoRespostaAlternativa;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public boolean isResponderFim() {
		return responderFim;
	}

	public void setResponderFim(boolean responderFim) {
		this.responderFim = responderFim;
	}

	public Pergunta clone() {
		Pergunta pergunta = new Pergunta();
		pergunta.setQuestionario(this.getQuestionario());
		pergunta.setAlternativas(this.getAlternativas());
		pergunta.setRespostaAlternativas(this.getRespostaAlternativas());

		return pergunta;

	}

	@Override
	public String toString() {
		return "Pergunta [id=" + id + ", titulo=" + titulo + ", texto=" + texto
				+ ", tipoRespostaAlternativa=" + tipoRespostaAlternativa
				+ ", alternativas=" + alternativas + ", questionario="
				+ questionario + ", respostaAlternativas="
				+ respostaAlternativas + ", respostaEscritas="
				+ respostaEscritas + "]";
	}

}
