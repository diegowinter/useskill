/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufpi.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Cleiton
 */
@Entity
public class Fluxo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataInicio;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataFim;
    @OneToMany(mappedBy = "fluxo",cascade=CascadeType.ALL,fetch=FetchType.LAZY)
    private List<Action> acoes;
    @ManyToOne(optional = false, cascade = CascadeType.REFRESH,fetch=FetchType.LAZY)
    private Usuario usuario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Action> getAcoes() {
        return acoes;
    }

    public void setAcoes(List<Action> acoes) {
        this.acoes = acoes;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

	@Override
	public String toString() {
		return "FluxoComponente [id=" + id + ", dataInicio=" + dataInicio + ", dataFim="
				+ dataFim + "]";
	}

    
}
