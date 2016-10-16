/*

 Copyright (C) 2009 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;


/**
 * 監視[一覧]ビューのフィルタ設定を格納するクラス<BR>
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class MonitorFilterInfo implements Serializable {

	private static final long serialVersionUID = -5376820531358677976L;

	private String monitorId = null;	// 監視項目ID
	private String monitorTypeId = null;// プラグインID
	private String description = null;	// 説明
	private String facilityId = null;	// ファシリティID
	private String calendarId = null;	// カレンダ
	private String regUser = null;		// 新規作成者
	private Long regFromDate = new Long(0);			// 作成日時(From)
	private Long regToDate = new Long(0);			// 作成日時(To)
	private String updateUser = null;	// 最終変更者
	private Long updateFromDate = new Long(0);		// 最終変更日時(From)
	private Long updateToDate = new Long(0);		// 最終変更日時(To)
	private Integer monitorFlg = -1;		// 監視有効フラグ(ValidConstant.TYPE_VALID)
	private Integer collectorFlg = -1;	// 監視無効フラグ(ValidConstant.TYPE_INVALID)
	private String ownerRoleId = null;	// オーナーロールID

	public String getMonitorId() {
		return monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}
	public String getMonitorTypeId() {
		return monitorTypeId;
	}
	public void setMonitorTypeId(String monitorTypeId) {
		this.monitorTypeId = monitorTypeId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	public String getCalendarId() {
		return calendarId;
	}
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}
	public String getRegUser() {
		return regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
	public String getUpdateUser() {
		return updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
	public Integer getMonitorFlg() {
		return monitorFlg;
	}
	public void setMonitorFlg(Integer monitorFlg) {
		this.monitorFlg = monitorFlg;
	}
	public Integer getCollectorFlg() {
		return collectorFlg;
	}
	public void setCollectorFlg(Integer collectorFlg) {
		this.collectorFlg = collectorFlg;
	}
	public Long getRegFromDate() {
		return regFromDate;
	}
	public void setRegFromDate(Long regFromDate) {
		this.regFromDate = regFromDate;
	}
	public Long getRegToDate() {
		return regToDate;
	}
	public void setRegToDate(Long regToDate) {
		this.regToDate = regToDate;
	}
	public Long getUpdateFromDate() {
		return updateFromDate;
	}
	public void setUpdateFromDate(Long updateFromDate) {
		this.updateFromDate = updateFromDate;
	}
	public Long getUpdateToDate() {
		return updateToDate;
	}
	public void setUpdateToDate(Long updateToDate) {
		this.updateToDate = updateToDate;
	}
	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

}
