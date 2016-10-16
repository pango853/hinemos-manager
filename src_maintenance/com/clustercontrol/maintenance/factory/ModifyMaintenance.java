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

import java.sql.Timestamp;
import java.util.Date;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.maintenance.bean.MaintenanceInfo;
import com.clustercontrol.maintenance.model.MaintenanceInfoEntity;
import com.clustercontrol.maintenance.model.MaintenanceTypeMstEntity;
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.notify.session.NotifyControllerBean;

/**
 * メンテナンス情報を変更するためのクラスです。
 * 
 * @version 4.0.0
 * @since 2.2.0
 *
 */
public class ModifyMaintenance {

	/**
	 * @param info
	 * @param name
	 * @return
	 * @throws MaintenanceNotFound
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean modifyMaintenance(MaintenanceInfo info, String name)
			throws MaintenanceNotFound, NotifyNotFound, InvalidRole, HinemosUnknown {

		//メンテナンス情報を取得
		MaintenanceInfoEntity entity = QueryUtil.getMaintenanceInfoPK(info.getMaintenanceId(), ObjectPrivilegeMode.MODIFY);

		//メンテナンス情報を更新
		entity.setDescription(info.getDescription());
		MaintenanceTypeMstEntity maintenanceTypeMstEntity = null;
		if (info.getTypeId() != null) {
			try {
				maintenanceTypeMstEntity = QueryUtil.getMaintenanceTypeMstPK(info.getTypeId());
			} catch (MaintenanceNotFound e) {
			}
		}
		entity.relateToMaintenanceTypeMstEntity(maintenanceTypeMstEntity);
		entity.setDataRetentionPeriod(info.getDataRetentionPeriod());
		entity.setCalendarId(info.getCalendarId());
		entity.setScheduleType(info.getSchedule().getType());
		entity.setMonth(info.getSchedule().getMonth());
		entity.setDay(info.getSchedule().getDay());
		entity.setWeek(info.getSchedule().getWeek());
		entity.setHour(info.getSchedule().getHour());
		entity.setMinute(info.getSchedule().getMinute());
		entity.setNotifyGroupId(info.getNotifyGroupId());
		entity.setApplication(info.getApplication());
		entity.setValidFlg(info.getValidFlg());
		entity.setOwnerRoleId(info.getOwnerRoleId());
		entity.setRegUser(info.getRegUser());
		entity.setRegDate(new Timestamp(info.getRegDate()));
		entity.setUpdateUser(name);
		entity.setUpdateDate(new Timestamp(new Date().getTime()));

		new NotifyControllerBean().modifyNotifyRelation(info.getNotifyId(), info.getNotifyGroupId());

		return true;
	}

}
