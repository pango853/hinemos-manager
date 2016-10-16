/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.snmptrap.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.bean.PriorityConstant;

/**
 * トラップのマッチング情報を保持する。
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class TrapValueInfo implements Serializable{
	private static final long serialVersionUID = 5737352544693128254L;

	private String mib;
	private int version;
	private String uei;
	private String trapOid;
	private Integer genericId = new Integer(0);
	private Integer specificId = new Integer(0);
	private String logmsg;
	private String description;
	private int processingVarbindType = MonitorTrapConstant.PROC_VARBIND_ANY;
	private int priorityAnyVarbind = PriorityConstant.TYPE_UNKNOWN;
	private String formatVarBinds;
	private boolean validFlg;

	private List<VarBindPattern> varBindPatterns = new ArrayList<>();

	public TrapValueInfo(){
	}

	/**
	 * 説明を取得します。<BR>
	 * @return 説明
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * 説明を設定します。<BR>
	 * @param descr
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * GenericIdを取得します。<BR>
	 * @return geniricId
	 */
	public Integer getGenericId() {
		return genericId;
	}
	/**
	 * GenericIdを設定します。<BR>
	 * @param genericId
	 */
	public void setGenericId(Integer genericId) {
		this.genericId = genericId;
	}
	/**
	 * ログメッセージを取得します。<BR>
	 * @return ログメッセージ
	 */
	public String getLogmsg() {
		return logmsg;
	}
	/**
	 * ログメッセージを設定します。<BR>
	 * @param logmsg
	 */
	public void setLogmsg(String logmsg) {
		this.logmsg = logmsg;
	}
	/**
	 * MIBを取得します。<BR>
	 * @return
	 */
	public String getMib() {
		return mib;
	}
	/**
	 * MIBを設定します。<BR>
	 * @param mib
	 */
	public void setMib(String mib) {
		this.mib = mib;
	}
	/**
	 * SpecificIdを取得します。<BR>
	 * @return
	 */
	public Integer getSpecificId() {
		return specificId;
	}
	/**
	 * SepecificIdを設定します。<BR>
	 * @param specificId
	 */
	public void setSpecificId(Integer specificId) {
		this.specificId = specificId;
	}
	/**
	 * トラップOID名を取得します。<BR>
	 * @return トラップOID名
	 */
	public String getTrapOid() {
		return trapOid;
	}
	/**
	 * トラップOID名を設定します。<BR>
	 * @param trapOid
	 */
	public void setTrapOid(String trapOid) {
		this.trapOid = trapOid;
	}
	/**
	 * UEIを取得します。
	 * @return　uei
	 */
	public String getUei() {
		return uei;
	}
	/**
	 * UEIを設定します。<BR>
	 * @param uei
	 */
	public void setUei(String uei) {
		this.uei = uei;
	}
	/**
	 * Varbind の処理種別を取得します。<BR>
	 * @return OIDの重要度
	 */
	public int getProcessingVarbindType() {
		return processingVarbindType;
	}
	/**
	 * Varbind の処理種別を設定します。<BR>
	 * @param processingVarbindType
	 */
	public void setProcessingVarbindType(int processingVarbindType) {
		this.processingVarbindType = processingVarbindType;
	}

	/**
	 * Mib のバージョンを取得する
	 * @return
	 */
	public int getVersion() {
		return version;
	}
	/**
	 * Mib のバージョンを設定する
	 * @param version
	 */
	public void setVersion(int version) {
		this.version = version;
	}
	/**
	 * 
	 * @return
	 */
	public int getPriorityAnyVarbind() {
		return priorityAnyVarbind;
	}
	/**
	 * @param priorityAnyVarbind
	 */
	public void setPriorityAnyVarbind(int priorityAnyVarbind) {
		this.priorityAnyVarbind = priorityAnyVarbind;
	}
	/**
	 * @return
	 */
	public String getFormatVarBinds() {
		return formatVarBinds;
	}
	/**
	 * @param formatVarbind
	 */
	public void setFormatVarBinds(String formatVarBinds) {
		this.formatVarBinds = formatVarBinds;
	}
	/**
	 * @return
	 */
	public boolean getValidFlg() {
		return validFlg;
	}
	/**
	 * @param validFlg
	 */
	public void setValidFlg(boolean validFlg) {
		this.validFlg = validFlg;
	}

	public List<VarBindPattern> getVarBindPatterns() {
		return varBindPatterns;
	}

	public void setVarBindPatterns(List<VarBindPattern> varBindPatterns) {
		this.varBindPatterns = varBindPatterns;
	}

	@Override
	public String toString() {
		return "TrapValueInfo [mib=" + mib + ", version=" + version + ", uei="
				+ uei + ", trapOid=" + trapOid + ", genericId=" + genericId
				+ ", specificId=" + specificId + ", logmsg=" + logmsg
				+ ", description=" + description + ", processingVarbindType="
				+ processingVarbindType + ", priorityAnyVarbind="
				+ priorityAnyVarbind + ", formatVarBind=" + formatVarBinds
				+ ", validFlg=" + validFlg + ", varBindPatterns="
				+ varBindPatterns + "]";
	}
}
