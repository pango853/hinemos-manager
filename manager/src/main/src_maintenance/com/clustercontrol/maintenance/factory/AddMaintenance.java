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

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.maintenance.bean.MaintenanceInfo;
import com.clustercontrol.maintenance.model.MaintenanceInfoEntity;
import com.clustercontrol.maintenance.model.MaintenanceTypeMstEntity;
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.notify.session.NotifyControllerBean;

/**
 * メンテナンス情報を登録するためのクラスです。
 * 
 * @version 4.0.0
 * @since 2.2.0
 *
 */
public class AddMaintenance {

	private static Log m_log = LogFactory.getLog( AddMaintenance.class );

	/**
	 * @param data
	 * @param name
	 * @return
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 */
	public boolean addMaintenance(MaintenanceInfo data, String name)
			throws EntityExistsException, InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = new JpaTransactionManager();

		// Entityクラスのインスタンス生成
		MaintenanceTypeMstEntity maintenanceTypeMstEntity = null;
		if (data.getTypeId() != null) {
			try {
				maintenanceTypeMstEntity = QueryUtil.getMaintenanceTypeMstPK(data.getTypeId());
			} catch (MaintenanceNotFound e) {
			}
		}
		try {
			// インスタンス生成
			MaintenanceInfoEntity entity = new MaintenanceInfoEntity(data.getMaintenanceId(),
					maintenanceTypeMstEntity);
			// 重複チェック
			jtm.checkEntityExists(MaintenanceInfoEntity.class, entity.getMaintenanceId());
			entity.setDescription(data.getDescription());
			entity.setDataRetentionPeriod(data.getDataRetentionPeriod());
			entity.setCalendarId(data.getCalendarId());
			entity.setScheduleType(data.getSchedule().getType());
			entity.setMonth(data.getSchedule().getMonth());
			entity.setDay(data.getSchedule().getDay());
			entity.setWeek(data.getSchedule().getWeek());
			entity.setHour(data.getSchedule().getHour());
			entity.setMinute(data.getSchedule().getMinute());
			entity.setNotifyGroupId(data.getNotifyGroupId());
			entity.setApplication(data.getApplication());
			entity.setValidFlg(data.getValidFlg());
			entity.setOwnerRoleId(data.getOwnerRoleId());
			entity.setRegUser(name);
			entity.setRegDate(new Timestamp(new Date().getTime()));
			entity.setUpdateUser(name);
			entity.setUpdateDate(new Timestamp(new Date().getTime()));
		} catch (EntityExistsException e){
			m_log.info("addMaintenance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e;
		}

		if(data.getNotifyId() != null){
			new NotifyControllerBean().addNotifyRelation(data.getNotifyId());
		}

		return true;

	}

}
