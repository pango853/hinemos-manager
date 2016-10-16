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

package com.clustercontrol.infra.factory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.infra.bean.InfraManagementInfo;
import com.clustercontrol.infra.bean.InfraModuleInfo;
import com.clustercontrol.infra.model.InfraManagementInfoEntity;
import com.clustercontrol.infra.model.InfraModuleInfoEntity;
import com.clustercontrol.notify.bean.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;

/**
 * 環境構築情報を更新する。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class ModifyInfraManagement {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( AddInfraManagement.class );

	public boolean modify(InfraManagementInfo webEntity, String user) throws InfraManagementNotFound, NotifyDuplicate, NotifyNotFound, InvalidRole, HinemosUnknown, InvalidSetting {
		m_log.debug("modify() : start");
		
		m_log.debug("modify() : " + webEntity.toString());
		
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		InfraManagementInfoEntity entity = em.find(InfraManagementInfoEntity.class, webEntity.getManagementId(), ObjectPrivilegeMode.MODIFY);
		if (entity == null) {
			InfraManagementNotFound e = new InfraManagementNotFound(
					webEntity.getManagementId(),
					"InfraManagementInfoEntity.findByPrimaryKey, " + "managementId = " + webEntity.getManagementId());
			m_log.info("InfraManagementInfoEntity.findByPrimaryKey : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		entity.setName(webEntity.getName());
		entity.setDescription(webEntity.getDescription());
		entity.setFacilityId(webEntity.getFacilityId());
		entity.setValidFlg(ValidConstant.booleanToType(webEntity.isValidFlg()));
		entity.setNotifyGroupId(NotifyGroupIdGenerator.generate(entity));
		entity.setStartPriority(webEntity.getStartPriority());
		entity.setNormalPriorityRun(webEntity.getNormalPriorityRun());
		entity.setAbnormalPriorityRun(webEntity.getAbnormalPriorityRun());
		entity.setNormalPriorityCheck(webEntity.getNormalPriorityCheck());
		entity.setAbnormalPriorityCheck(webEntity.getAbnormalPriorityRun());
		
		
		if (webEntity.getNotifyRelationList() != null) {
			for (NotifyRelationInfo webRelation: webEntity.getNotifyRelationList()) {
				m_log.info("groupId=" + entity.getNotifyGroupId());
				webRelation.setNotifyGroupId(entity.getNotifyGroupId());
			}
			new NotifyControllerBean().modifyNotifyRelation(webEntity.getNotifyRelationList(), entity.getNotifyGroupId());
		}
		
		
		List<InfraModuleInfo<?>> webModuleList = new ArrayList<InfraModuleInfo<?>>(webEntity.getModuleList());
		List<InfraModuleInfoEntity<?>> moduleList = new ArrayList<InfraModuleInfoEntity<?>>(entity.getInfraModuleInfoEntities());
		
		int orderNo = 0;
		Iterator<InfraModuleInfo<?>> webModuleIter = webModuleList.iterator();
		while (webModuleIter.hasNext()) {
			InfraModuleInfo<?> mi = webModuleIter.next();
			
			Iterator<InfraModuleInfoEntity<?>> moduleIter = moduleList.iterator();
			while (moduleIter.hasNext()) {
				InfraModuleInfoEntity<?> module = moduleIter.next();
				if (mi.getModuleId().equals(module.getId().getModuleId())) {
					mi.modifyCounterEntity(entity, module, orderNo);
					
					webModuleIter.remove();
					moduleIter.remove();
					break;
				}
			}
			orderNo++;
		}
		
		for (InfraModuleInfo<?> webModule: webModuleList) {
			webModule.addCounterEntity(entity);
		}
		
		for (InfraModuleInfoEntity<?> module: moduleList) {
			entity.getInfraModuleInfoEntities().remove(module);
			module.removeSelf();
		}
		
		Timestamp now = new Timestamp(new Date().getTime());
		
		entity.setUpdateDate(now);
		entity.setUpdateUser(user);
		
		m_log.debug("modify() : end");
		return true;
	}
}
