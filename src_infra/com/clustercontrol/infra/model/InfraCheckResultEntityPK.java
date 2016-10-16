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
package com.clustercontrol.infra.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;


/**
 * The primary key class for the cc_monitor_trap_value_info database table.
 * 
 */
@Embeddable
public class InfraCheckResultEntityPK implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String managementId;
	private String moduleId;
	private String nodeId;
	
	public InfraCheckResultEntityPK() {
	}

	public InfraCheckResultEntityPK(String managementId, String moduleId, String nodeId) {
		this.setManagementId(managementId);
		this.setModuleId(moduleId);
		this.setNodeId(nodeId);
	}
	
	@Column(name="management_id")
	public String getManagementId() {
		return managementId;
	}
	public void setManagementId(String managementId) {
		this.managementId = managementId;
	}
	
	@Column(name="module_id")
	public String getModuleId() {
		return moduleId;
	}
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	
	@Column(name="node_id")
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((nodeId == null) ? 0 : nodeId.hashCode());
		result = prime * result
				+ ((managementId == null) ? 0 : managementId.hashCode());
		result = prime * result
				+ ((moduleId == null) ? 0 : moduleId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InfraCheckResultEntityPK other = (InfraCheckResultEntityPK) obj;
		if (nodeId == null) {
			if (other.nodeId != null)
				return false;
		} else if (!nodeId.equals(other.nodeId))
			return false;
		if (managementId == null) {
			if (other.managementId != null)
				return false;
		} else if (!managementId.equals(other.managementId))
			return false;
		if (moduleId == null) {
			if (other.moduleId != null)
				return false;
		} else if (!moduleId.equals(other.moduleId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "InfraCheckResultEntityPK [managementId=" + managementId
				+ ", moduleId=" + moduleId + ", facilityId=" + nodeId + "]";
	}
}
