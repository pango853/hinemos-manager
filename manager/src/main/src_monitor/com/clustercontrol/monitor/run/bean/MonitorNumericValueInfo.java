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

package com.clustercontrol.monitor.run.bean;

import javax.xml.bind.annotation.XmlType;

/**
 * 数値監視の判定情報を保持するクラス<BR>
 * 
 * @version 4.0.0
 * @since 2.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class MonitorNumericValueInfo
extends MonitorJudgementInfo
{
	private static final long serialVersionUID = -7307483294508379452L;

	/** 閾値下限。 */
	private Double m_thresholdLowerLimit;

	/** 閾値上限。 */
	private Double m_thresholdUpperLimit;


	/**
	 * コンストラクタ。
	 */
	public MonitorNumericValueInfo() {
		super();
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param message メッセージ
	 * @param messageId メッセージID
	 * @param monitorId 監視項目ID
	 * @param priority 重要度
	 * @param thresholdLowerLimit 閾値下限
	 * @param thresholdUpperLimit 閾値上限
	 */
	public MonitorNumericValueInfo(
			String message,
			String messageId,
			String monitorId,
			int priority,
			double thresholdLowerLimit,
			double thresholdUpperLimit) {

		setMessage(message);
		setMessageId(messageId);
		setMonitorId(monitorId);
		setPriority(priority);
		setThresholdLowerLimit(thresholdLowerLimit);
		setThresholdUpperLimit(thresholdUpperLimit);
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param otherData コピー元の数値監視の判定情報
	 */
	public MonitorNumericValueInfo( MonitorNumericValueInfo otherData ) {

		setMessage(otherData.getMessage());
		setMessageId(otherData.getMessageId());
		setMonitorId(otherData.getMonitorId());
		setPriority(otherData.getPriority());
		setThresholdLowerLimit(otherData.getThresholdLowerLimit());
		setThresholdUpperLimit(otherData.getThresholdUpperLimit());
	}

	/**
	 * 閾値下限を返します。
	 * 
	 * @return 閾値下限
	 */
	public Double getThresholdLowerLimit() {
		return this.m_thresholdLowerLimit;
	}

	/**
	 * 閾値下限を設定します。
	 * 
	 * @param thresholdLowerLimit 閾値下限
	 */
	public void setThresholdLowerLimit( Double thresholdLowerLimit ) {
		this.m_thresholdLowerLimit = thresholdLowerLimit;
	}

	/**
	 * 閾値上限を返します。
	 * 
	 * @return 閾値上限
	 */
	public Double getThresholdUpperLimit() {
		return this.m_thresholdUpperLimit;
	}

	/**
	 * 閾値上限を設定します。
	 * 
	 * @param thresholdUpperLimit 閾値上限
	 */
	public void setThresholdUpperLimit( Double thresholdUpperLimit ) {
		this.m_thresholdUpperLimit = thresholdUpperLimit;
	}
}
