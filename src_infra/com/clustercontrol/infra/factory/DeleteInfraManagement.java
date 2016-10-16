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

package com.clustercontrol.infra.factory;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.infra.model.InfraManagementInfoEntity;
import com.clustercontrol.notify.session.NotifyControllerBean;

/**
 * 環境構築機能情報を削除する。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class DeleteInfraManagement {
	/** ログ出力のインスタンス。 */
	private static Logger m_log = Logger.getLogger( DeleteInfraManagement.class );

	/**
	 * 環境構築情報を削除します。
	 */
	public boolean delete(String managementId) throws InfraManagementNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("delete() : start");
		
		m_log.debug(String.format("delete() : managementId = %s", managementId));

		// 監視情報を取得
		InfraManagementInfoEntity entity = null;
		try {
			HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
			entity = em.find(InfraManagementInfoEntity.class, managementId, ObjectPrivilegeMode.MODIFY);
			if (entity == null) {
				InfraManagementNotFound e = new InfraManagementNotFound("MonitorInfoEntity.findByPrimaryKey" + ", managementId = " + managementId);
				m_log.info("delete() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("delete() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}

		// 監視グループ情報を削除
		new NotifyControllerBean().deleteNotifyRelation(entity.getNotifyGroupId());

		// 監視情報を削除
		entity.removeSelf();

		m_log.debug("delete() : end");
		
		return true;
	}
}
