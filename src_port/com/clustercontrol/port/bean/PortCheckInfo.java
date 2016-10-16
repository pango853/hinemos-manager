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

package com.clustercontrol.port.bean;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.bean.MonitorCheckInfo;

/**
 * port監視情報クラスです。
 * 
 * @version 2.4.0
 * @since 2.4.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class PortCheckInfo extends MonitorCheckInfo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8021840129680920477L;

	/** ポート番号 */
	private java.lang.Integer portNo;

	/** リトライ回数（回） */
	private java.lang.Integer runCount;

	/** リトライ間隔（ミリ秒） */
	private java.lang.Integer runInterval;

	/** タイムアウト（ミリ秒） */
	private java.lang.Integer timeout;

	/** 各サービスの監視を実装したクラス名 */
	private java.lang.String serviceId;

	public PortCheckInfo() {
	}

	// ポート番号
	public java.lang.Integer getPortNo() {
		return this.portNo;
	}

	public void setPortNo(java.lang.Integer portNo) {
		this.portNo = portNo;
	}

	// 各サービスの監視を実装したクラス名
	public java.lang.String getServiceId() {
		return this.serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public java.lang.Integer getRunCount() {
		return this.runCount;
	}

	public void setRunCount(java.lang.Integer runCount) {
		this.runCount = runCount;
	}

	public java.lang.Integer getRunInterval() {
		return this.runInterval;
	}

	public void setRunInterval(java.lang.Integer runInterval) {
		this.runInterval = runInterval;
	}

	public java.lang.Integer getTimeout() {
		return this.timeout;
	}

	public void setTimeout(java.lang.Integer timeout) {
		this.timeout = timeout;
	}
}
