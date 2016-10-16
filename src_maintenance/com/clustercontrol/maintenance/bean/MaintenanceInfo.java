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

package com.clustercontrol.maintenance.bean;

import java.io.Serializable;
import java.util.Collection;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.commons.bean.Schedule;
import com.clustercontrol.notify.bean.NotifyRelationInfo;

/**
 * メンテナンス情報のデータクラスです。
 * 
 * @version 4.0.0
 * @since 2.2.0
 *
 */
@XmlType(namespace = "http://maintenance.ws.clustercontrol.com")
public class MaintenanceInfo implements Serializable{
	private static final long serialVersionUID = 4890593575884133961L;
	private String maintenanceId;
	private String description;
	private String typeId;
	private Integer dataRetentionPeriod;
	private String calendarId;
	private String notifyGroupId;
	private String application;
	private Integer validFlg;
	private String regUser;
	private Long regDate;
	private String updateUser;
	private Long updateDate;
	private String ownerRoleId;

	/** スケジュール */
	private Schedule schedule;

	/**通知*/
	private Collection<NotifyRelationInfo> notifyId;

	public MaintenanceInfo() {
		super();
		notifyId = null;
	}

	/**
	 * 
	 * @return
	 */
	public Collection<NotifyRelationInfo> getNotifyId() {
		return notifyId;
	}

	/**
	 * 
	 * @param notifyId
	 */
	public void setNotifyId(Collection<NotifyRelationInfo> notifyId) {
		this.notifyId = notifyId;
	}

	public String getMaintenanceId() {
		return maintenanceId;
	}

	public void setMaintenanceId(String maintenanceId) {
		this.maintenanceId = maintenanceId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public Integer getDataRetentionPeriod() {
		return dataRetentionPeriod;
	}

	public void setDataRetentionPeriod(Integer dataRetentionPeriod) {
		this.dataRetentionPeriod = dataRetentionPeriod;
	}

	public String getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	public String getNotifyGroupId() {
		return notifyGroupId;
	}

	public void setNotifyGroupId(String notifyGroupId) {
		this.notifyGroupId = notifyGroupId;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public Integer getValidFlg() {
		return validFlg;
	}

	public void setValidFlg(Integer validFlg) {
		this.validFlg = validFlg;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public Long getRegDate() {
		return regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Long getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}


	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

}
