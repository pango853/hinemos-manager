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

package com.clustercontrol.infra.bean;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://infra.ws.clustercontrol.com")
public class InfraCheckResult {
	public static final int result_ok = 0;
	public static final int result_ng = 1;
	
	private String managementId;
	private String moduleId;
	private String nodeId;
	
	private int result;
	
	public String getManagementId() {
		return managementId;
	}
	public void setManagementId(String managementId) {
		this.managementId = managementId;
	}
	
	public String getModuleId() {
		return moduleId;
	}
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
}
