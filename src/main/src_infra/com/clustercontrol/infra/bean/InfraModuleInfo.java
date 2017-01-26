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

import javax.persistence.EntityExistsException;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.infra.model.InfraManagementInfoEntity;
import com.clustercontrol.infra.model.InfraModuleInfoEntity;
import com.clustercontrol.infra.model.InfraModuleInfoEntityPK;
import com.clustercontrol.repository.bean.NodeInfo;
import com.clustercontrol.util.Messages;

@XmlType(namespace = "http://infra.ws.clustercontrol.com")
@XmlSeeAlso({FileTransferModuleInfo.class, CommandModuleInfo.class})
public abstract class InfraModuleInfo<E extends InfraModuleInfoEntity<?>> {
	private String moduleId;
	private String name;
	private boolean validFlg;
	private boolean stopIfFailFlg;
	private boolean precheckFlg;
	private List<String> okNodeList = new ArrayList<>();
	private List<String> ngNodeList = new ArrayList<>();
	
	public String getModuleId() {
		return moduleId;
	}
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isValidFlg() {
		return validFlg;
	}
	public void setValidFlg(boolean validFlg) {
		this.validFlg = validFlg;
	}

	public boolean isStopIfFailFlg() {
		return stopIfFailFlg;
	}
	public void setStopIfFailFlg(boolean stopIfFailFlg) {
		this.stopIfFailFlg = stopIfFailFlg;
	}
	
	public boolean isPrecheckFlg() {
		return precheckFlg;
	}
	public void setPrecheckFlg(boolean precheckFlg) {
		this.precheckFlg = precheckFlg;
	}
	
	public List<String> getOkNodeList() {
		return okNodeList;
	}
	
	public List<String> getNgNodeList() {
		return ngNodeList;
	}
	
	public void validate() throws InvalidSetting, InvalidRole {
		CommonValidator.validateId(Messages.getString("infra.management.module.id"), getModuleId(), 64);
		CommonValidator.validateString(Messages.getString("infra.management.module.name"), getName(), true, 1, 64);

		// validFlg : not implemented
		// proceedIfFailFlg : not implemented
		
		validateSub();
	}

	protected abstract void validateSub() throws InvalidSetting, InvalidRole;
	
	public void addCounterEntity(InfraManagementInfoEntity management) throws HinemosUnknown, EntityExistsException {
		JpaTransactionManager jtm = new JpaTransactionManager();
		try {
			E module = (E)getEntityClass().newInstance();

			module.setId(new InfraModuleInfoEntityPK(management.getManagementId(), getModuleId()));
			jtm.checkEntityExists(InfraModuleInfoEntity.class, module.getId());
			module.setName(getName());
			module.setOrderNo(management.getInfraModuleInfoEntities().size());
			module.setValidFlg(ValidConstant.booleanToType(isValidFlg()));
			module.setStopIfFailFlg(ValidConstant.booleanToType(isStopIfFailFlg()));
			module.setPrecheckFlg(ValidConstant.booleanToType(isPrecheckFlg()));

			HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
			em.persist(module);
			
			management.getInfraModuleInfoEntities().add(module);

			overwriteCounterEntity(management, module, em);
			
		} catch (InstantiationException | IllegalAccessException e) {
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (EntityExistsException e) {
			throw e;
		}
	}
	
	public void modifyCounterEntity(InfraManagementInfoEntity management, InfraModuleInfoEntity<?> module, Integer orderNo) throws HinemosUnknown {
		if (!module.getId().getModuleId().equals(this.getModuleId()))
			throw new HinemosUnknown("Not match moduleIds between web and db on modifying infra module.");
		
		module.setName(getName());
		module.setOrderNo(orderNo);
		module.setValidFlg(ValidConstant.booleanToType(isValidFlg()));
		module.setStopIfFailFlg(ValidConstant.booleanToType(isStopIfFailFlg()));
		module.setPrecheckFlg(ValidConstant.booleanToType(isPrecheckFlg()));
		
		try {
			HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
			overwriteCounterEntity(management, getEntityClass().cast(module), em);
		} catch(ClassCastException e) {
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}
	
	public abstract String getModuleTypeName();

	public abstract boolean canPrecheck(InfraManagementInfo management, NodeInfo node, AccessInfo access, String sessionId) throws HinemosUnknown, InvalidUserPass;

	public abstract ModuleNodeResult run(InfraManagementInfo management, NodeInfo node, AccessInfo access, String sessionId, String account) throws HinemosUnknown, InvalidUserPass;

	public abstract ModuleNodeResult check(InfraManagementInfo management, NodeInfo node, AccessInfo access, String sessionId, boolean verbose) throws HinemosUnknown, InvalidUserPass;
	
	protected abstract Class<E> getEntityClass();
	
	protected abstract void overwriteCounterEntity(InfraManagementInfoEntity management, E module, HinemosEntityManager em);
	
	public abstract void beforeRun(String sessionId) throws HinemosUnknown;
	
	public abstract void afterRun(String sessionId) throws HinemosUnknown;
}
