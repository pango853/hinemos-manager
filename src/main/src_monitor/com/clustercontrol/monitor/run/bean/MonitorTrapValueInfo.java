/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.run.bean;

import javax.xml.bind.annotation.XmlType;

/**
 * SNMPTRAP監視の判定情報を保持するクラス<BR>
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class MonitorTrapValueInfo extends MonitorJudgementInfo {

	private static final long serialVersionUID = -825594950855580142L;

	private String mib;
	private String trapOid;
	private int genericId;
	private int specificId;
	private String uei;
	private boolean validFlg;
	private String logmsg;
	private String descr;

	/**
	 * JAXBのため、デフォルトコンストラクタが必要。
	 */
	public MonitorTrapValueInfo() {}

	/**
	 * コンストラクタ
	 * 
	 * @param monitorId
	 * @param mib
	 * @param trapOid
	 * @param genericId
	 * @param specificId
	 * @param validFlg
	 * @param priority
	 * @param logmsg
	 * @param descr
	 */
	public MonitorTrapValueInfo(
			String monitorId,
			String mib,
			String trapOid,
			int genericId,
			int specificId,
			String uei,
			boolean validFlg,
			int priority,
			String logmsg,
			String descr) {

		setMonitorId(monitorId);
		setMib(mib);
		setTrapOid(trapOid);
		setGenericId(genericId);
		setSpecificId(specificId);
		setUei(uei);
		setValidFlg(validFlg);
		setPriority(priority);
		setLogmsg(logmsg);
		setDescr(descr);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param otherData コピー元のSNMPTRAP監視の判定情報
	 */
	public MonitorTrapValueInfo(MonitorTrapValueInfo otherData) {
		setMonitorId(otherData.getMonitorId());
		setMib(otherData.getMib());
		setTrapOid(otherData.getTrapOid());
		setGenericId(otherData.getGenericId());
		setSpecificId(otherData.getSpecificId());
		setUei(uei);
		setValidFlg(otherData.isValidFlg());
		setPriority(otherData.getPriority());
		setLogmsg(otherData.getLogmsg());
		setDescr(otherData.getDescr());
	}

	public String getMib() {
		return mib;
	}


	public void setMib(String mib) {
		this.mib = mib;
	}


	public String getTrapOid() {
		return trapOid;
	}


	public void setTrapOid(String trapOid) {
		this.trapOid = trapOid;
	}


	public int getGenericId() {
		return genericId;
	}


	public void setGenericId(int genericId) {
		this.genericId = genericId;
	}


	public int getSpecificId() {
		return specificId;
	}


	public void setSpecificId(int specificId) {
		this.specificId = specificId;
	}

	public String getUei() {
		return uei;
	}

	public void setUei(String uei) {
		this.uei = uei;
	}

	public boolean isValidFlg() {
		return validFlg;
	}


	public void setValidFlg(boolean validFlg) {
		this.validFlg = validFlg;
	}


	public String getLogmsg() {
		return logmsg;
	}


	public void setLogmsg(String logmsg) {
		this.logmsg = logmsg;
	}


	public String getDescr() {
		return descr;
	}


	public void setDescr(String descr) {
		this.descr = descr;
	}


}
