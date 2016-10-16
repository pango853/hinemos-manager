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

package com.clustercontrol.http.bean;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.bean.YesNoConstant;
import com.clustercontrol.monitor.run.bean.MonitorCheckInfo;

/**
 * HTTP監視設定情報のBeanクラス<BR>
 * 
 * @version 2.1.0
 * @since 2.1.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class HttpCheckInfo extends MonitorCheckInfo {

	private static final long serialVersionUID = 5802612741291625279L;

	/** URL */
	private String m_requestUrl;

	/** URL置換 */
	private Integer m_urlReplace = YesNoConstant.TYPE_NO;

	/** タイムアウト（ミリ秒） */
	private Integer m_timeout;

	/** プロキシ設定 */
	private Integer m_proxySet = YesNoConstant.TYPE_NO;

	/** プロキシ ホスト */
	private String m_proxyHost;

	/** プロキシ ポート */
	private Integer m_proxyPort = new Integer(0);


	public HttpCheckInfo(){
	}

	public String getProxyHost() {
		return m_proxyHost;
	}

	public void setProxyHost(String host) {
		m_proxyHost = host;
	}

	public Integer getProxyPort() {
		return m_proxyPort;
	}

	public void setProxyPort(Integer port) {
		m_proxyPort = port;
	}

	public Integer getProxySet() {
		return m_proxySet;
	}

	public void setProxySet(Integer set) {
		m_proxySet = set;
	}

	public String getRequestUrl() {
		return m_requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.m_requestUrl = requestUrl;
	}

	public Integer getTimeout() {
		return m_timeout;
	}

	public void setTimeout(Integer timeout) {
		this.m_timeout = timeout;
	}

	public Integer getUrlReplace() {
		return m_urlReplace;
	}

	public void setUrlReplace(Integer replace) {
		m_urlReplace = replace;
	}
}
