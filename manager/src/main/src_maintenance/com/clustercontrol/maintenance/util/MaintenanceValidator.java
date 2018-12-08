/*

Copyright (C) since 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.maintenance.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.maintenance.bean.MaintenanceInfo;
import com.clustercontrol.maintenance.bean.MaintenanceTypeMst;
import com.clustercontrol.maintenance.factory.SelectMaintenanceTypeMst;
import com.clustercontrol.util.Messages;
import com.clustercontrol.notify.bean.NotifyRelationInfo;

/**
 * 履歴削除の入力チェッククラス
 * 
 * @since 4.0
 */
public class MaintenanceValidator {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( MaintenanceValidator.class );

	/**
	 * メンテナンス情報の妥当性チェック
	 * 
	 * @param maintenanceInfo
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateMaintenanceInfo(MaintenanceInfo maintenanceInfo) throws InvalidSetting, InvalidRole {

		// maintenanceId
		if (maintenanceInfo.getMaintenanceId() == null ||
				"".equals(maintenanceInfo.getMaintenanceId())) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.maintenance.9"));
			m_log.info("validateMaintenanceInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateId(Messages.getString("maintenance.id"), maintenanceInfo.getMaintenanceId(), 64);

		// ownerRoleId
		CommonValidator.validateString(Messages.getString("owner.role.id"), maintenanceInfo.getOwnerRoleId(), true, 1, 64);

		// schedule
		CommonValidator.validateScheduleHour(maintenanceInfo.getSchedule());

		// calendarId
		CommonValidator.validateCalenderId(maintenanceInfo.getCalendarId(), false, maintenanceInfo.getOwnerRoleId());

		maintenanceInfo.getSchedule();

		// notifyId
		if(maintenanceInfo.getNotifyId() != null){
			for(NotifyRelationInfo notifyRelationInfo : maintenanceInfo.getNotifyId()){
				CommonValidator.validateNotifyId(notifyRelationInfo.getNotifyId(), true, maintenanceInfo.getOwnerRoleId());
			}
		}

		// typeId
		String typeId = maintenanceInfo.getTypeId();
		if (typeId == null || "".equals(typeId)) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.maintenance.16"));
			m_log.info("validateMaintenanceInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		SelectMaintenanceTypeMst select = new SelectMaintenanceTypeMst();
		boolean flag = true;
		try {
			for (MaintenanceTypeMst mst : select.getMaintenanceTypeList()) {
				if (typeId.equals(mst.getType_id())) {
					flag = false;
					break;
				}
			}
			if (flag) {
				InvalidSetting e = new InvalidSetting(Messages.getString("message.maintenance.16"));
				m_log.info("validateMaintenanceInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (InvalidSetting e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("validateMaintenanceInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}

		// dataRetentionPeriod
		if(maintenanceInfo.getDataRetentionPeriod() == null){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.maintenance.17"));
			m_log.info("validateMaintenanceInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(Messages.getString("maintenance.retention.period"),
				maintenanceInfo.getDataRetentionPeriod(), -32768, 32767);

		// description
		CommonValidator.validateString(Messages.getString("description"),
				maintenanceInfo.getDescription(), false, 0, 256);

		// application
		if (maintenanceInfo.getApplication() == null ||
				"".equals(maintenanceInfo.getApplication())) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.10"));
			m_log.info("validateMaintenanceInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(Messages.getString("application"),
				maintenanceInfo.getApplication(), true, 0, 64);
	}
}
