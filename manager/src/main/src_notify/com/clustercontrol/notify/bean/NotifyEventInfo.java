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

import com.clustercontrol.monitor.bean.EventConfirmConstant;


/**
 * 通知イベント情報を保持するクラス
 * 
 * @version 3.0.0
 * @since 2.0.0
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
public class NotifyEventInfo  extends NotifyInfoDetail
{
	private static final long serialVersionUID = -5089086735470199399L;

	/**
	 * イベント通知状態。
	 * @see com.clustercontrol.bean.EventConfirmConstant
	 */
	private Integer infoEventNormalState = new Integer(EventConfirmConstant.TYPE_UNCONFIRMED);
	private Integer warnEventNormalState = new Integer(EventConfirmConstant.TYPE_UNCONFIRMED);
	private Integer criticalEventNormalState = new Integer(EventConfirmConstant.TYPE_UNCONFIRMED);
	private Integer unknownEventNormalState = new Integer(EventConfirmConstant.TYPE_UNCONFIRMED);


	/**
	 * コンストラクタ。
	 */
	public NotifyEventInfo() {
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param notifyId 通知ID
	 * @param eventNormalFlg イベント通知フラグ
	 * @param eventNormalState イベント通知状態
	 * 
	 * @see com.clustercontrol.bean.PriorityConstant
	 * @see com.clustercontrol.bean.ValidConstant
	 * @see com.clustercontrol.bean.EventConfirmConstant
	 */
	public NotifyEventInfo(
			String notifyId,
			Integer infoEventNormalFlg,
			Integer warnEventNormalFlg,
			Integer criticalEventNormalFlg,
			Integer unknownEventNormalFlg,

			Integer infoEventNormalState,
			Integer warnEventNormalState,
			Integer criticalEventNormalState,
			Integer unknownEventNormalState
			) {
		super(notifyId, infoEventNormalFlg, warnEventNormalFlg,
				criticalEventNormalFlg, unknownEventNormalFlg);

		this.infoEventNormalState = infoEventNormalState;
		this.warnEventNormalState = warnEventNormalState;
		this.criticalEventNormalState = criticalEventNormalState;
		this.unknownEventNormalState = unknownEventNormalState;
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param otherData コピー元の通知情報
	 */
	public NotifyEventInfo( NotifyEventInfo otherData ) {
		super(otherData);
		this.infoEventNormalState = otherData.getInfoEventNormalState();
		this.warnEventNormalState = otherData.getWarnEventNormalState();
		this.criticalEventNormalState = otherData.getCriticalEventNormalState();
		this.unknownEventNormalState = otherData.getUnknownEventNormalState();
	}

	public Integer getInfoEventNormalState() {
		return infoEventNormalState;
	}

	public void setInfoEventNormalState(Integer eventInfoNormalState) {
		this.infoEventNormalState = eventInfoNormalState;
	}

	public Integer getWarnEventNormalState() {
		return warnEventNormalState;
	}

	public void setWarnEventNormalState(Integer warnEventNormalState) {
		this.warnEventNormalState = warnEventNormalState;
	}

	public Integer getCriticalEventNormalState() {
		return criticalEventNormalState;
	}

	public void setCriticalEventNormalState(Integer criticalEventNormalState) {
		this.criticalEventNormalState = criticalEventNormalState;
	}

	public Integer getUnknownEventNormalState() {
		return unknownEventNormalState;
	}

	public void setUnknownEventNormalState(Integer unknownEventNormalState) {
		this.unknownEventNormalState = unknownEventNormalState;
	}
}