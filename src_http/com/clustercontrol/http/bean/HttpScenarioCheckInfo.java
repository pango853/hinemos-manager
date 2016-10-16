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
package com.clustercontrol.http.bean;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.bean.MonitorCheckInfo;



/**
 * HTTP監視(scenario)設定情報のBeanクラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class HttpScenarioCheckInfo extends MonitorCheckInfo {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2190059551064944093L;

	private String authType;
	private String authUser;
	private String authPassword;
	private boolean proxyFlg;
	private String proxyUrl;
	private Integer proxyPort;
	private String proxyUser;
	private String proxyPassword;
	private boolean monitoringPerPageFlg;
	private String userAgent;
	private Integer connectTimeout;
	private Integer requestTimeout;

	private List<Page> pages = new ArrayList<>();

	public HttpScenarioCheckInfo() {
	}


	public String getAuthType() {
		return this.authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}


	public String getAuthUser() {
		return this.authUser;
	}

	public void setAuthUser(String authUser) {
		this.authUser = authUser;
	}


	public String getAuthPassword() {
		return this.authPassword;
	}

	public void setAuthPassword(String authPassword) {
		this.authPassword = authPassword;
	}

	public boolean getProxyFlg() {
		return this.proxyFlg;
	}

	public void setProxyFlg(boolean proxyFlg) {
		this.proxyFlg = proxyFlg;
	}
	
	public String getProxyUrl() {
		return this.proxyUrl;
	}

	public void setProxyUrl(String proxyUrl) {
		this.proxyUrl = proxyUrl;
	}


	public Integer getProxyPort() {
		return this.proxyPort;
	}

	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}


	public String getProxyUser() {
		return this.proxyUser;
	}

	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}


	public String getProxyPassword() {
		return this.proxyPassword;
	}

	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}


	public boolean getMonitoringPerPageFlg() {
		return this.monitoringPerPageFlg;
	}

	public void setMonitoringPerPageFlg(boolean monitoringPerPageFlg) {
		this.monitoringPerPageFlg = monitoringPerPageFlg;
	}


	public String getUserAgent() {
		return this.userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}


	public Integer getConnectTimeout() {
		return this.connectTimeout;
	}

	public void setConnectTimeout(Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
	}


	public List<Page> getPages() {
		return pages;
	}


	public void setPages(List<Page> pages) {
		this.pages = pages;
	}


	public Integer getRequestTimeout() {
		return requestTimeout;
	}


	public void setRequestTimeout(Integer requestTimeout) {
		this.requestTimeout = requestTimeout;
	}
}