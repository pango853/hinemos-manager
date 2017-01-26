/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.notify.bean;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://notify.ws.clustercontrol.com")
public class NotifyInfoDetail implements java.io.Serializable {
	private static final long serialVersionUID = 8895768556582532893L;

	/** 通知ID。 */
	private String notifyId;

	/**
	 * 通知フラグ。
	 * @see com.clustercontrol.bean.ValidConstant
	 */
	private Integer infoValidFlg;
	private Integer warnValidFlg;
	private Integer criticalValidFlg;
	private Integer unknownValidFlg;

	public NotifyInfoDetail() {
	}

	public NotifyInfoDetail(NotifyInfoDetail otherData) {
		this.infoValidFlg = otherData.getInfoValidFlg();
		this.warnValidFlg = otherData.getWarnValidFlg();
		this.criticalValidFlg = otherData.getCriticalValidFlg();
		this.unknownValidFlg = otherData.getUnknownValidFlg();
	}

	public NotifyInfoDetail(String notifyId, Integer infoValidFlg,
			Integer warnValidFlg, Integer criticalValidFlg,
			Integer unknownValidFlg) {
		this.infoValidFlg = infoValidFlg;
		this.warnValidFlg = warnValidFlg;
		this.criticalValidFlg = criticalValidFlg;
		this.unknownValidFlg = unknownValidFlg;
	}

	/**
	 * 通知IDを返します
	 * @return
	 */
	public String getNotifyId() {
		return notifyId;
	}

	/**
	 * 通知IDを設定します。
	 * @param id
	 */
	public void setNotifyId(String id) {
		notifyId = id;
	}

	public Integer getInfoValidFlg() {
		return infoValidFlg;
	}

	public void setInfoValidFlg(Integer infoValidFlg) {
		this.infoValidFlg = infoValidFlg;
	}

	public Integer getWarnValidFlg() {
		return warnValidFlg;
	}

	public void setWarnValidFlg(Integer warnValidFlg) {
		this.warnValidFlg = warnValidFlg;
	}

	public Integer getCriticalValidFlg() {
		return criticalValidFlg;
	}

	public void setCriticalValidFlg(Integer criticalValidFlg) {
		this.criticalValidFlg = criticalValidFlg;
	}

	public Integer getUnknownValidFlg() {
		return unknownValidFlg;
	}

	public void setUnknownValidFlg(Integer unknownValidFlg) {
		this.unknownValidFlg = unknownValidFlg;
	}
}
