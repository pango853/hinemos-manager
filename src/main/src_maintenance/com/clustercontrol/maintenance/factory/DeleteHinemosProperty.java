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

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosPropertyNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.maintenance.model.HinemosPropertyEntity;
import com.clustercontrol.maintenance.util.QueryUtil;

/**
 * 共通設定情報を削除するためのクラスです。
 * 
 * @version 5.0.0
 * @since 5.0.0
 *
 */
public class DeleteHinemosProperty {

	/**
	 * 共通設定情報を削除します。
	 * 
	 * @param key
	 * @return
	 * @throws MaintenanceNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean deleteHinemosProperty(String key)
			throws HinemosPropertyNotFound, InvalidRole, HinemosUnknown {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		// 削除対象を検索
		HinemosPropertyEntity entity = QueryUtil.getHinemosPropertyInfoPK(key);

		//共通設定情報の削除
		em.remove(entity);

		return true;
	}

}
