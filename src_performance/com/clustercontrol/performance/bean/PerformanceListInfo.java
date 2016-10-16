/*

Copyright (C) since 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.performance.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * 
 * 性能[一覧]ビュー表示に必要な情報を保持するクラス。
 * 
 * @version 4.0.0
 * @since 4.0.0
 *
 *
 */
@XmlType(namespace = "http://collector.ws.clustercontrol.com")
public class PerformanceListInfo implements Serializable {

	private static final long serialVersionUID = 5389619858059848797L;

	private Integer collectorFlg = 0;
	private String monitorId = null;
	private String monitorTypeId = null;
	private String description = null;
	private String facilityId = null;
	private String scopeText = null;
	private Integer runInterval = 0;

	/**
	 * 最新の収集データの日付時刻
	 */
	private Long latestDate = null;

	/**
	 * 最古の収集データの日付時刻
	 */
	private Long oldestDate = null;

	public PerformanceListInfo() {
	}

	public Integer getCollectorFlg() {
		return collectorFlg;
	}


	public void setCollectorFlg(Integer collectorFlg) {
		this.collectorFlg = collectorFlg;
	}


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


	public String getScopeText() {
		return scopeText;
	}


	public void setScopeText(String scopeText) {
		this.scopeText = scopeText;
	}


	public Integer getRunInterval() {
		return runInterval;
	}


	public void setRunInterval(Integer runInterval) {
		this.runInterval = runInterval;
	}


	/**
	 * 最新の収集データの日付時刻を返します。
	 * 
	 * facilityIdがスコープである場合、監視項目IDをキーとして最新の収集データの日付時刻を返します。
	 * facilityIdがスコープである場合、監視項目IDとfacilityIdをキーとして最新の収集データの日付時刻を返します。
	 * 
	 * @return
	 */
	public Long getLatestDate() {
		return latestDate;
	}

	public void setLatestDate(Long latestDate) {
		this.latestDate = latestDate;
	}

	/**
	 * 最古の収集データの日付時刻を返します。
	 * 
	 * facilityIdがスコープである場合、監視項目IDをキーとして最古の収集データの日付時刻を返します。
	 * facilityIdがスコープである場合、監視項目IDとfacilityIdをキーとして最古の収集データの日付時刻を返します。
	 * 
	 * @return
	 */
	public Long getOldestDate() {
		return oldestDate;
	}

	public void setOldestDate(Long oldestDate) {
		this.oldestDate = oldestDate;
	}


}
