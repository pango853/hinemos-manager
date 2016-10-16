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
 * 真偽値監視の判定情報を保持するクラス<BR>
 * 
 * @version 4.0.0
 * @since 2.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class MonitorTruthValueInfo
extends MonitorJudgementInfo
{
	private static final long serialVersionUID = -8164764456414755011L;

	/**
	 * 真偽値。
	 * 
	 * @see com.clustercontrol.monitor.run.bean.TruthConstant
	 */
	private int m_truthValue;


	/**
	 * コンストラクタ。
	 */
	public MonitorTruthValueInfo() {
		super();
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param message メッセージ
	 * @param messageId メッセージID
	 * @param monitorId 監視項目ID
	 * @param priority 重要度
	 * @param truthValue 真偽値
	 * @param jobRun ジョブ実行
	 * @param jobId ジョブID
	 * @param jobFailurePriority ジョブ呼出の失敗時の重要度
	 */
	public MonitorTruthValueInfo(
			String message,
			String messageId,
			String monitorId,
			int priority,
			int truthValue) {

		setMessage(message);
		setMessageId(messageId);
		setMonitorId(monitorId);
		setPriority(priority);
		setTruthValue(truthValue);
	}


	/**
	 * コンストラクタ。
	 * 
	 * @param otherData コピー元の真偽値監視の判定情報
	 */
	public MonitorTruthValueInfo( MonitorTruthValueInfo otherData )
	{
		setMessage(otherData.getMessage());
		setMessageId(otherData.getMessageId());
		setMonitorId(otherData.getMonitorId());
		setPriority(otherData.getPriority());
		setTruthValue(otherData.getTruthValue());
	}

	/**
	 * 真偽値を返します。
	 * 
	 * @return 真偽値
	 * 
	 * @see com.clustercontrol.monitor.run.bean.TruthConstant
	 */
	public int getTruthValue() {
		return m_truthValue;
	}

	/**
	 * 真偽値を設定します。
	 * 
	 * @param truthValue 真偽値
	 * 
	 * @see com.clustercontrol.monitor.run.bean.TruthConstant
	 */
	public void setTruthValue(int truthValue) {
		this.m_truthValue = truthValue;
	}
}
