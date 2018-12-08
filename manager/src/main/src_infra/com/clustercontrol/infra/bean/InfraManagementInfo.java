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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.notify.bean.NotifyRelationInfo;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.util.Messages;

@XmlType(namespace = "http://infra.ws.clustercontrol.com")
public class InfraManagementInfo {
	private static Log m_log = LogFactory.getLog( InfraManagementInfo.class );
	
	private String managementId;
	private String name;
	private String description;
	private String facilityId;
	private String scope;
	private String ownerRoleId;
	private boolean validFlg;

	private String notifyGroupID;

	private int startPriority;
	private int normalPriorityRun;
	private int abnormalPriorityRun;
	private int normalPriorityCheck;
	private int abnormalPriorityCheck;
	
	private List<InfraModuleInfo<?>> moduleList = new ArrayList<>();
	
	private List<NotifyRelationInfo> notifyRelationList = new ArrayList<>();
	
	private long regDate;
	private long updateDate;
	private String regUser;
	private String updateUser;
	
	public String getManagementId() {
		return managementId;
	}
	public void setManagementId(String managementId) {
		this.managementId = managementId;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
	
	public boolean isValidFlg() {
		return validFlg;
	}
	public void setValidFlg(boolean validFlg) {
		this.validFlg = validFlg;
	}
	
	public int getStartPriority() {
		return startPriority;
	}
	public void setStartPriority(int startPriority) {
		this.startPriority = startPriority;
	}
	
	public int getNormalPriorityRun() {
		return normalPriorityRun;
	}
	public void setNormalPriorityRun(int normalPriorityRun) {
		this.normalPriorityRun = normalPriorityRun;
	}
	
	public int getAbnormalPriorityRun() {
		return abnormalPriorityRun;
	}
	public void setAbnormalPriorityRun(int abnormalPriorityRun) {
		this.abnormalPriorityRun = abnormalPriorityRun;
	}
	
	public int getNormalPriorityCheck() {
		return normalPriorityCheck;
	}
	public void setNormalPriorityCheck(int normalPriorityCheck) {
		this.normalPriorityCheck = normalPriorityCheck;
	}
	
	public int getAbnormalPriorityCheck() {
		return abnormalPriorityCheck;
	}
	public void setAbnormalPriorityCheck(int abnormalPriorityCheck) {
		this.abnormalPriorityCheck = abnormalPriorityCheck;
	}
	
	public List<InfraModuleInfo<?>> getModuleList() {
		return this.moduleList;
	}
	public void setModuleList(List<InfraModuleInfo<?>> moduleList) {
		this.moduleList = moduleList;
	}

	public List<NotifyRelationInfo> getNotifyRelationList() {
		return notifyRelationList;
	}
	public void setNotifyRelationList(List<NotifyRelationInfo> notifyRelationList) {
		this.notifyRelationList = notifyRelationList;
	}

	public long getRegDate() {
		return this.regDate;
	}
	public void setRegDate(long regDate) {
		this.regDate = regDate;
	}

	public String getRegUser() {
		return this.regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
	
	public long getUpdateDate() {
		return this.updateDate;
	}
	public void setUpdateDate(long updateDate) {
		this.updateDate = updateDate;
	}

	public String getUpdateUser() {
		return this.updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
	
	@XmlTransient
	public String getNotifyGroupId() {
		return notifyGroupID;
	}
	public void setNotifyGroupID(String notifyGroupID) {
		this.notifyGroupID = notifyGroupID;
	}
	
	public void validate() throws InvalidSetting, InvalidRole {

		CommonValidator.validateId(Messages.getString("infra.management.id"), getManagementId(), 64);
		CommonValidator.validateString(Messages.getString("infra.management.name"), getName(), true, 1, 64);
		CommonValidator.validateString(Messages.getString("infra.management.description"), getDescription(), false, 0, 256);
		CommonValidator.validateOwnerRoleId(getOwnerRoleId(), true, getManagementId(), HinemosModuleConstant.INFRA);

		// facilityId
		if(getFacilityId() == null || "".equals(getFacilityId())){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.hinemos.3"));
			m_log.info("validate() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}else{
			try {
				FacilityTreeCache.validateFacilityId(getFacilityId(), getOwnerRoleId(), false);
			} catch (FacilityNotFound e) {
				throw new InvalidSetting(e.getMessage(), e);
			}
		}
		
		// scope : not implemented

		// validFlg : not implemented
		// notifyGroupID : not implemented

		// startPriority : not implemented
		// normalPriorityRun : not implemented
		// abnormalPriorityRun : not implemented
		// normalPriorityCheck : not implemented
		// abnormalPriorityCheck : not implemented
		
		// notifyId
		if(getNotifyRelationList() != null){
			for(NotifyRelationInfo relation: getNotifyRelationList()){
				CommonValidator.validateNotifyId(relation.getNotifyId(), true, getOwnerRoleId());
			}
		}
		
		for (InfraModuleInfo<?> module: getModuleList()) {
			module.validate();
		}
	}
	
	@Override
	public String toString() {
		return "InfraManagementInfo [managementId=" + managementId + ", name="
				+ name + ", description=" + description + ", facilityId="
				+ facilityId + ", scope=" + scope + ", ownerRoleId="
				+ ownerRoleId + ", validFlg=" + validFlg + ", notifyGroupID="
				+ notifyGroupID + ", startPriority=" + startPriority
				+ ", normalPriorityRun=" + normalPriorityRun
				+ ", abnormalPriorityRun=" + abnormalPriorityRun
				+ ", normalPriorityCheck=" + normalPriorityCheck
				+ ", abnormalPriorityCheck=" + abnormalPriorityCheck
				+ ", infraModuleList=" + moduleList + ", nris=" + notifyRelationList
				+ ", regDate=" + regDate + ", updateDate=" + updateDate
				+ ", regUser=" + regUser + ", updateUser=" + updateUser + "]";
	}
}
