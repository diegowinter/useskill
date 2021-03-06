/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufpi.models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Transient;

import br.ufpi.models.vo.ActionVO;

/**
 * 
 * @author Cleiton
 * @changes Matheus
 * 
 */
@Entity
public class Action implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String sActionType;
	private Long sTime;
	private Long sRealTime;
	private int sTimezoneOffset;
	private String sUrl;
	
	@Lob
	@Column(length = 10000000)
	private String sContent;
	@Lob
	@Column(length = 10000000)
	private String sXPath;
	
	private String sTag;
	private String sTagIndex;
	private String sId;
	private String sClass;
	private String sName;
	private int sPosX;
	private int sPosY;
	
	@Transient
	private boolean melhorCaminho = false;
	@Transient
	private boolean obrigatorio = false;
	@Transient
	private boolean acaoEspecialista = false;
	@Transient
	private boolean acaoRepetida = false;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the fluxo
	 */


	/**
	 * @return the sActionType
	 */
	public String getsActionType() {
		return sActionType;
	}

	/**
	 * @param sActionType
	 *            the sActionType to set
	 */
	public void setsActionType(String sActionType) {
		this.sActionType = sActionType;
	}

	/**
	 * @return the sTime
	 */
	public Long getsTime() {
		return sTime;
	}

	/**
	 * @param sTime
	 *            the sTime to set
	 */
	public void setsTime(Long sTime) {
		this.sTime = sTime;
	}

	/**
	 * @return the sUrl
	 */
	public String getsUrl() {
		return sUrl;
	}

	/**
	 * @param sUrl
	 *            the sUrl to set
	 */
	public void setsUrl(String sUrl) {
		this.sUrl = sUrl;
	}

	/**
	 * @return the sContent
	 */
	public String getsContent() {
		return sContent;
	}

	/**
	 * @param sContent
	 *            the sContent to set
	 */
	public void setsContent(String sContent) {
		this.sContent = sContent;
	}

	/**
	 * @return the sTag
	 */
	public String getsTag() {
		return sTag;
	}

	/**
	 * @param sTag
	 *            the sTag to set
	 */
	public void setsTag(String sTag) {
		this.sTag = sTag;
	}

	/**
	 * @return the sTagIndex
	 */
	public String getsTagIndex() {
		return sTagIndex;
	}

	/**
	 * @param sTagIndex
	 *            the sTagIndex to set
	 */
	public void setsTagIndex(String sTagIndex) {
		this.sTagIndex = sTagIndex;
	}


	/**
	 * @return the sPosX
	 */
	public int getsPosX() {
		return sPosX;
	}

	/**
	 * @param sPosX the sPosX to set
	 */
	public void setsPosX(int sPosX) {
		this.sPosX = sPosX;
	}

	/**
	 * @return the sPosY
	 */
	public int getsPosY() {
		return sPosY;
	}

	/**
	 * @param sPosY the sPosY to set
	 */
	public void setsPosY(int sPosY) {
		this.sPosY = sPosY;
	}

	public String getsId() {
		return sId;
	}

	public void setsId(String sId) {
		this.sId = sId;
	}

	public String getsClass() {
		return sClass;
	}

	public void setsClass(String sClass) {
		this.sClass = sClass;
	}

	public String getsName() {
		return sName;
	}

	public void setsName(String sName) {
		this.sName = sName;
	}

	/**
	 * @return the sXPath
	 */
	public String getsXPath() {
		return sXPath;
	}

	/**
	 * @param sXPath the sXPath to set
	 */
	public void setsXPath(String sXPath) {
		this.sXPath = sXPath;
	}
	
	public ActionVO toVO(){
		return new ActionVO(this.sActionType, this.sUrl, this.sXPath);
	}
	
	@Override
	public String toString() {
		return "Action [id=" + id 
				+ ", sActionType=" + sActionType + ", sTime=" + sTime
				+ ", sUrl=" + sUrl + ", sTag="
				+ sTag + ", sTagIndex=" + sTagIndex + ", sId=" + sId 
				+  ", sClass=" + sClass + ", sName=" + sName + ", sPosX=" 
				+ sPosX	+ ", sPosY=" + sPosY + ", sXPath=" + sXPath + "]";
	}

	/**
	 * @return the sRealTime
	 */
	public Long getsRealTime() {
		return sRealTime;
	}

	/**
	 * @param sRealTime the sRealTime to set
	 */
	public void setsRealTime(Long sRealTime) {
		this.sRealTime = sRealTime;
	}

	/**
	 * @return the sTimezoneOffset
	 */
	public int getsTimezoneOffset() {
		return sTimezoneOffset;
	}

	/**
	 * @param sTimezoneOffset the sTimezoneOffset to set
	 */
	public void setsTimezoneOffset(int sTimezoneOffset) {
		this.sTimezoneOffset = sTimezoneOffset;
	}

	/**
	 * @return the melhorCaminho
	 */
	public boolean isMelhorCaminho() {
		return melhorCaminho;
	}

	/**
	 * @param melhorCaminho the melhorCaminho to set
	 */
	public void setMelhorCaminho(boolean melhorCaminho) {
		this.melhorCaminho = melhorCaminho;
	}

	/**
	 * @return the obrigatorio
	 */
	public boolean isObrigatorio() {
		return obrigatorio;
	}

	/**
	 * @param obrigatorio the obrigatorio to set
	 */
	public void setObrigatorio(boolean obrigatorio) {
		this.obrigatorio = obrigatorio;
	}
	
	public String shortContent(){
		if(this.sContent.length() > 100){
			return this.sContent.substring(0, 97) + "...";
		}else{
			return this.sContent;
		}
	}
	
	public String getDescricaoElemento(){
		String elemento = "";
		if(this.getsXPath() != null && !this.getsXPath().isEmpty()){
			if(this.getsTag() != null && this.getsTagIndex() != null && !this.getsTag().isEmpty() && !this.getsTagIndex().isEmpty()){
				elemento += this.getsTag() + "[" + this.getsTagIndex() + "]";
			}
			if(this.getsId() != null && !this.getsId().isEmpty()){
				elemento += " id="+this.getsId();
			}
			if(this.getsClass() != null && !this.getsClass().isEmpty()){
				elemento += " class="+this.getsClass();
			}
			if(this.getsName() != null && !this.getsName().isEmpty()){
				elemento += " name="+this.getsName();
			}
		}
		return elemento;
	}

	/**
	 * @return the acaoEspecialista
	 */
	public boolean isAcaoEspecialista() {
		return acaoEspecialista;
	}

	/**
	 * @param acaoEspecialista the acaoEspecialista to set
	 */
	public void setAcaoEspecialista(boolean acaoEspecialista) {
		this.acaoEspecialista = acaoEspecialista;
	}

	/**
	 * @return the acaoRepetida
	 */
	public boolean getAcaoRepetida() {
		return acaoRepetida;
	}

	/**
	 * @param acaoRepetida the acaoRepetida to set
	 */
	public void setAcaoRepetida(boolean acaoRepetida) {
		this.acaoRepetida = acaoRepetida;
	}
}
