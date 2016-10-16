/*

 Copyright (C) 2008 NTT DATA Corporation

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

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.monitor.bean.StatusExpirationConstant;
import com.clustercontrol.monitor.bean.StatusValidPeriodConstant;

/**
 * 通知ステータス情報を保持するクラス
 * @version 3.0.0
 * @since 3.0.0
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
public class NotifyStatusInfo extends NotifyInfoDetail {
	private static final long serialVersionUID = -8851213616872328323L;

	/**
	 * 存続期間経過後の処理フラグ。
	 * @see com.clustercontrol.bean.StatusExpirationConstant
	 */
	private java.lang.Integer m_statusInvalidFlg = new Integer(StatusExpirationConstant.TYPE_UPDATE);

	/**
	 * ステータス情報更新時重要度。
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	private java.lang.Integer m_statusUpdatePriority = new Integer(PriorityConstant.TYPE_WARNING);

	/**
	 * ステータス情報の存続期間。
	 * @see com.clustercontrol.bean.StatusValidPeriodConstant
	 */
	private java.lang.Integer m_statusValidPeriod = new Integer(StatusValidPeriodConstant.TYPE_MIN_10);

	/**
	 * コンストラクタ。
	 */
	public NotifyStatusInfo() {
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param notifyId 通知ID
	 * @param priority 重要度
	 * @param validFlg ステータス通知フラグ
	 * @param invalidFlg 存続期間経過後の処理フラグ
	 * @param updatePriority ステータス情報更新時重要度
	 * @param validPeriod ステータス情報の存続期間
	 * 
	 * @see com.clustercontrol.bean.PriorityConstant
	 * @see com.clustercontrol.bean.ValidConstant
	 * @see com.clustercontrol.bean.StatusExpirationConstant
	 * @see com.clustercontrol.bean.StatusValidPeriodConstant
	 */
	public NotifyStatusInfo(
			String notifyId,
			Integer infoValidFlg,
			Integer warnValidFlg,
			Integer criticalValidFlg,
			Integer unknownValidFlg,

			Integer invalidFlg,
			Integer updatePriority,
			Integer validPeriod) {
		super(notifyId, infoValidFlg, warnValidFlg, criticalValidFlg, unknownValidFlg);

		setStatusInvalidFlg(invalidFlg);
		setStatusUpdatePriority(updatePriority);
		setStatusValidPeriod(validPeriod);
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param otherData コピー元の通知情報
	 */
	public NotifyStatusInfo(NotifyStatusInfo otherData) {
		super(otherData);
		setStatusInvalidFlg(otherData.getStatusInvalidFlg());
		setStatusUpdatePriority(otherData.getStatusUpdatePriority());
		setStatusValidPeriod(otherData.getStatusValidPeriod());
	}

	/**
	 * 存続期間経過後の処理フラグを返します。
	 * 
	 * @return 存続期間経過後の処理フラグ
	 * 
	 * @see com.clustercontrol.bean.StatusExpirationConstant
	 */
	public Integer getStatusInvalidFlg() {
		return this.m_statusInvalidFlg;
	}

	/**
	 * 存続期間経過後の処理フラグを設定します。
	 * 
	 * @param statusInvalidFlg 存続期間経過後の処理フラグ
	 * 
	 * @see com.clustercontrol.bean.StatusExpirationConstant
	 */
	public void setStatusInvalidFlg( Integer statusInvalidFlg ) {
		this.m_statusInvalidFlg = statusInvalidFlg;
	}

	/**
	 * ステータス情報更新時重要度を返します。
	 * 
	 * @return ステータス情報更新時重要度
	 * 
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public Integer getStatusUpdatePriority() {
		return this.m_statusUpdatePriority;
	}

	/**
	 * ステータス情報更新時重要度を設定します。
	 * 
	 * @param statusUpdatePriority ステータス情報更新時重要度
	 * 
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public void setStatusUpdatePriority( Integer statusUpdatePriority ) {
		this.m_statusUpdatePriority = statusUpdatePriority;
	}


	/**
	 * ステータス有効期間を返します。
	 * @return　ステータス有効期間
	 */
	public java.lang.Integer getStatusValidPeriod() {
		return m_statusValidPeriod;
	}


	/**
	 * ステータス有効期間を設定します。
	 * 
	 * @param validPeriod ステータス有効期間
	 */
	public void setStatusValidPeriod(java.lang.Integer validPeriod) {
		m_statusValidPeriod = validPeriod;
	}


}
