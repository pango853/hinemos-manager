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

package com.clustercontrol.jmx.bean;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.bean.MonitorCheckInfo;

/**
 * JMX 監視設定情報のBeanクラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class JmxCheckInfo extends MonitorCheckInfo {

	private static final long serialVersionUID = 5802612741291625279L;



	/**  */
	private String authUser;

	/**  */
	private String authPassword;

	/**  */
	private Integer port;

	/**  */
	private String masterId;

	public JmxCheckInfo(){
	}


	public String getAuthUser() {
		return authUser;
	}

	public void setAuthUser(String authUser) {
		this.authUser = authUser;
	}


	public String getAuthPassword() {
		return authPassword;
	}

	public void setAuthPassword(String authPassword) {
		this.authPassword = authPassword;
	}


	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}


	public String getMasterId() {
		return masterId;
	}

	public void setMasterId(String masterId) {
		this.masterId = masterId;
	}
}
