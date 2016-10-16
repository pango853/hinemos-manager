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
package com.clustercontrol.performance.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * 性能[一覧]ビューのフィルタ設定を格納するクラス<BR>
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
@XmlType(namespace = "http://collector.ws.clustercontrol.com")
public class PerformanceFilterInfo implements Serializable {

	private static final long serialVersionUID = 9544616724374727L;

	private String monitorId = null;
	private String monitorTypeId = null;
	private String description = null;
	private Long oldestFromDate = new Long(0);
	private Long oldestToDate = new Long(0);
	private Long latestFromDate = new Long(0);
	private Long latestToDate = new Long(0);

	public PerformanceFilterInfo(){}

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
	public Long getOldestFromDate() {
		return oldestFromDate;
	}
	public void setOldestFromDate(Long oldestFromDate) {
		this.oldestFromDate = oldestFromDate;
	}
	public Long getOldestToDate() {
		return oldestToDate;
	}
	public void setOldestToDate(Long oldestToDate) {
		this.oldestToDate = oldestToDate;
	}
	public Long getLatestFromDate() {
		return latestFromDate;
	}
	public void setLatestFromDate(Long latestFromDate) {
		this.latestFromDate = latestFromDate;
	}
	public Long getLatestToDate() {
		return latestToDate;
	}
	public void setLatestToDate(Long latestToDate) {
		this.latestToDate = latestToDate;
	}



}
