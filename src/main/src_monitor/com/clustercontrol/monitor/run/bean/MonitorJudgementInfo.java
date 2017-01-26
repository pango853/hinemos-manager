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
 * 監視情報の判定情報を保持する抽象クラス<BR>
 * <p>
 * 監視種別（真偽値，数値，文字列）の各クラスで継承してください。
 * jaxbで利用するため、引数なしのコンストラクタが必要。
 * そのため、abstractにしない。
 * @version 3.0.0
 * @since 2.1.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class MonitorJudgementInfo
implements java.io.Serializable
{
	private static final long serialVersionUID = 2684510388370616270L;

	/** 監視対象ID。 */
	private String m_monitorTypeId = "";

	/** 監視項目ID。 */
	private String m_monitorId = "";

	/**
	 * 重要度。
	 * 
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	private int m_priority;

	/** メッセージID。 */
	private String m_messageId = "";

	/** メッセージ。 */
	private String m_message = "";


	/**
	 * メッセージを返します。
	 * 
	 * @return メッセージ
	 */
	public String getMessage() {
		return m_message;
	}

	/**
	 * メッセージを設定します。
	 * 
	 * @param message メッセージ
	 */
	public void setMessage(String message) {
		this.m_message = message;
	}

	/**
	 * メッセージIDを返します。
	 * 
	 * @return メッセージID
	 */
	public String getMessageId() {
		return m_messageId;
	}

	/**
	 * メッセージIDを設定します。
	 * 
	 * @param messageId メッセージID
	 */
	public void setMessageId(String messageId) {
		this.m_messageId = messageId;
	}

	/**
	 * 監視項目IDを返します。
	 * 
	 * @return 監視項目ID
	 */
	public String getMonitorId() {
		return m_monitorId;
	}

	/**
	 * 監視項目IDを設定します。
	 * 
	 * @param monitorId 監視項目ID
	 */
	public void setMonitorId(String monitorId) {
		this.m_monitorId = monitorId;
	}

	/**
	 * 監視対象IDを返します。
	 * 
	 * @return 監視対象ID
	 */
	public String getMonitorTypeId() {
		return m_monitorTypeId;
	}

	/**
	 * 監視対象IDを設定します。
	 * 
	 * @param monitorTypeId 監視対象ID
	 */
	public void setMonitorTypeId(String monitorTypeId) {
		this.m_monitorTypeId = monitorTypeId;
	}

	/**
	 * 重要度を返します。
	 * 
	 * @return 重要度
	 * 
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public int getPriority() {
		return m_priority;
	}

	/**
	 * 重要度を設定します。
	 * 
	 * @param priority 重要度
	 * 
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public void setPriority(int priority) {
		this.m_priority = priority;
	}
}
