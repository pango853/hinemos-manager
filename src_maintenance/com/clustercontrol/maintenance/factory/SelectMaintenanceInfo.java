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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.commons.bean.Schedule;
import com.clustercontrol.maintenance.bean.MaintenanceInfo;
import com.clustercontrol.maintenance.model.MaintenanceInfoEntity;
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.notify.session.NotifyControllerBean;

/**
 * 
 * メンテナンス情報検索クラスです。
 * 
 * @since	4.0.0
 * @version	2.2.0
 *
 */
public class SelectMaintenanceInfo {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectMaintenanceInfo.class );

	/**
	 * @param maintenanceId
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws MaintenanceNotFound
	 */
	public MaintenanceInfo getMaintenanceInfo(String maintenanceId)
			throws HinemosUnknown, InvalidRole, MaintenanceNotFound {

		MaintenanceInfo info = null;
		// メンテナンス情報を取得
		MaintenanceInfoEntity entity = QueryUtil.getMaintenanceInfoPK(maintenanceId);
		info = getMaintenanceInfoBean(entity);
		return info;
	}


	/**
	 * 
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<MaintenanceInfo> getMaintenanceList() throws InvalidRole, HinemosUnknown {
		m_log.debug("getMaintenanceList() : start");

		// メンテナンス情報一覧を取得
		ArrayList<MaintenanceInfo> list = new ArrayList<MaintenanceInfo>();

		List<MaintenanceInfoEntity> ct = QueryUtil.getAllMaintenanceInfoOrderByMaintenanceId();
		for(MaintenanceInfoEntity entity : ct){
			list.add(getMaintenanceInfoBean(entity));
			// for debug
			if(m_log.isDebugEnabled()){
				m_log.debug("getMaintenanceList() : " +
						"maintenanceId = " + entity.getMaintenanceId() +
						", description = " + entity.getDescription() +
						", type_id = " + (entity.getMaintenanceTypeMstEntity() == null ? null :
							entity.getMaintenanceTypeMstEntity().getTypeId()) +
							", dataRetentionPeriod = " + entity.getDataRetentionPeriod() +
							", calendar_id = " + entity.getCalendarId() +
							", schedule = " + entity.getScheduleType() +
							", month = " + entity.getMonth() +
							", day = " + entity.getDay() +
							", week = " + entity.getWeek() +
							", hour = " + entity.getHour() +
							", minute = " + entity.getMinute() +
							", notifyGroupId = " + entity.getNotifyGroupId() +
							", application = " + entity.getApplication() +
							", valid_flg = " + entity.getValidFlg() +
							", regUser = " + entity.getRegUser() +
							", regDate = " + entity.getRegDate() +
							", updateUser = " + entity.getUpdateUser() +
							", updateDate = " + entity.getUpdateDate());
			}
		}

		return list;
	}

	/**
	 * MaintenanceInfoEntityからMaintenanceInfoBeanへ変換
	 */
	private MaintenanceInfo getMaintenanceInfoBean(MaintenanceInfoEntity entity)
			throws InvalidRole, HinemosUnknown {

		MaintenanceInfo info = new MaintenanceInfo();
		info.setMaintenanceId(entity.getMaintenanceId());
		info.setDescription(entity.getDescription());
		info.setTypeId(entity.getMaintenanceTypeMstEntity() == null ? null :
			entity.getMaintenanceTypeMstEntity().getTypeId());
		info.setDataRetentionPeriod(entity.getDataRetentionPeriod());
		info.setCalendarId(entity.getCalendarId());
		info.setSchedule(new Schedule(entity.getScheduleType(),
				entity.getMonth(), entity.getDay(), entity.getWeek(),
				entity.getHour(), entity.getMinute()));

		info.setNotifyGroupId(entity.getNotifyGroupId());
		info.setApplication(entity.getApplication());
		info.setValidFlg(entity.getValidFlg());
		info.setOwnerRoleId(entity.getOwnerRoleId());
		info.setRegUser(entity.getRegUser());
		if(entity.getRegDate() == null){
			info.setRegDate(null);
		}
		else{
			info.setRegDate(entity.getRegDate().getTime());
		}
		info.setUpdateUser(entity.getUpdateUser());
		if(entity.getUpdateDate() == null){
			info.setUpdateDate(null);
		}
		else{
			info.setUpdateDate(entity.getUpdateDate().getTime());
		}

		//通知情報の取得
		info.setNotifyId(new NotifyControllerBean().getNotifyRelation(info.getNotifyGroupId()));

		return info;
	}
}
