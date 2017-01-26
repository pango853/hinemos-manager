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
import java.util.Date;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.infra.bean.InfraManagementInfo;
import com.clustercontrol.infra.bean.InfraModuleInfo;
import com.clustercontrol.infra.model.InfraManagementInfoEntity;
import com.clustercontrol.notify.bean.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;

/**
 * 環境構築機能情報を追加する。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class AddInfraManagement {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( AddInfraManagement.class );

	public boolean add(InfraManagementInfo webEntity, String user) throws HinemosUnknown, NotifyDuplicate, InvalidRole, InvalidSetting, EntityExistsException {
		m_log.debug("add() : start");

		// 環境情報を登録
		if(m_log.isDebugEnabled())
			m_log.debug("add() : " + webEntity.toString());
		
		Timestamp now = new Timestamp(new Date().getTime());
		JpaTransactionManager jtm = new JpaTransactionManager();
		
		InfraManagementInfoEntity entity = new InfraManagementInfoEntity(webEntity.getManagementId(), webEntity.getName(), webEntity.getFacilityId());
		jtm.checkEntityExists(InfraManagementInfoEntity.class, entity.getManagementId());
		entity.setDescription(webEntity.getDescription());
		entity.setValidFlg(ValidConstant.booleanToType(webEntity.isValidFlg()));
		entity.setNotifyGroupId(NotifyGroupIdGenerator.generate(entity));
		entity.setOwnerRoleId(webEntity.getOwnerRoleId());
		entity.setStartPriority(webEntity.getStartPriority());
		entity.setAbnormalPriorityCheck(webEntity.getAbnormalPriorityCheck());
		entity.setAbnormalPriorityRun(webEntity.getAbnormalPriorityRun());
		entity.setNormalPriorityCheck(webEntity.getNormalPriorityCheck());
		entity.setNormalPriorityRun(webEntity.getNormalPriorityRun());
		entity.setRegDate(now);
		entity.setRegUser(user);
		entity.setUpdateDate(now);
		entity.setUpdateUser(user);
		
		if (webEntity.getNotifyRelationList() != null) {
			for (NotifyRelationInfo webRelation: webEntity.getNotifyRelationList()) {
				webRelation.setNotifyGroupId(entity.getNotifyGroupId());
			}
			new NotifyControllerBean().addNotifyRelation(webEntity.getNotifyRelationList());
		}
		
		for (InfraModuleInfo<?> webModule: webEntity.getModuleList()) {
			webModule.addCounterEntity(entity);
		}
		
		m_log.debug("add() : end");
		return true;
	}
}
