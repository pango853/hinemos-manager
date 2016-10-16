/*

Copyright (C) 2007 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.maintenance.factory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.maintenance.model.MaintenanceInfoEntity;
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.notify.session.NotifyControllerBean;

/**
 * メンテナンス情報を削除するためのクラスです。
 * 
 * @version 4.0.0
 * @since 2.2.0
 *
 */
public class DeleteMaintenance {

	/**
	 * @param maintenanceId
	 * @return
	 * @throws MaintenanceNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean deleteMaintenance(String maintenanceId)
			throws MaintenanceNotFound, InvalidRole, HinemosUnknown {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		// 削除対象を検索
		MaintenanceInfoEntity entity = QueryUtil.getMaintenanceInfoPK(maintenanceId, ObjectPrivilegeMode.MODIFY);

		//通知情報の削除
		new NotifyControllerBean().deleteNotifyRelation(entity.getNotifyGroupId());

		//メンテナンス情報の削除
		entity.unchain();	// 削除前処理
		em.remove(entity);

		return true;
	}

}
