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

import com.clustercontrol.bean.ProcessConstant;
import com.clustercontrol.bean.ValidConstant;

/**
 * 文字列監視の判定情報を保持するクラス<BR>
 * 
 * @version 4.0.0
 * @since 2.1.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class MonitorStringValueInfo
extends MonitorJudgementInfo
{
	private static final long serialVersionUID = -72068372783747219L;

	/** 説明。 */
	private String m_description;

	/**
	 * 処理タイプ。
	 * 
	 * @see com.clustercontrol.bean.ProcessConstant
	 */
	private int m_processType = ProcessConstant.TYPE_YES;

	/** パターンマッチ表現。 */
	private String m_pattern;

	/** 大文字・小文字を区別するか否かのフラグ。
	 * 
	 * @see com.clustercontrol.bean.ValidConstant
	 */
	private boolean m_caseSensitivityFlg = ValidConstant.BOOLEAN_INVALID;

	/**
	 * 有効/無効。
	 * 
	 * @see com.clustercontrol.bean.ValidConstant
	 */
	private boolean m_validFlg = ValidConstant.BOOLEAN_VALID;


	/**
	 * コンストラクタ。
	 */
	public MonitorStringValueInfo() {
		super();
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param monitorId 監視項目ID
	 * @param description 説明
	 * @param processType 処理タイプ
	 * @param pattern パターンマッチ表現
	 * @param priority 重要度
	 * @param messageId メッセージID
	 * @param message メッセージ
	 * @param notifyId 通知ID
	 * @param validFlg 有効/無効
	 */
	public MonitorStringValueInfo(
			String monitorId,
			String description,
			int processType,
			String pattern,
			int priority,
			String message,
			boolean caseSensitivityFlg,
			boolean validFlg) {

		setMonitorId(monitorId);
		setDescription(description);
		setProcessType(processType);
		setPattern(pattern);
		setPriority(priority);
		setMessage(message);
		setCaseSensitivityFlg(caseSensitivityFlg);
		setValidFlg(validFlg);
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param otherData コピー元の文字列監視の判定情報
	 */
	public MonitorStringValueInfo(MonitorStringValueInfo otherData) {

		setMonitorId(otherData.getMonitorId());
		setDescription(otherData.getDescription());
		setProcessType(otherData.getProcessType());
		setPattern(otherData.getPattern());
		setPriority(otherData.getPriority());
		setMessage(otherData.getMessage());
		setCaseSensitivityFlg(otherData.getCaseSensitivityFlg());
		setValidFlg(otherData.isValidFlg());
	}


	/**
	 * 説明を返します。
	 * 
	 * @return 説明
	 */
	public String getDescription() {
		return m_description;
	}

	/**
	 * 説明を設定します。
	 * 
	 * @param description 説明
	 */
	public void setDescription(String description) {
		this.m_description = description;
	}

	/**
	 * パターンマッチ表現を返します。
	 * 
	 * @return パターンマッチ表現
	 */
	public String getPattern() {
		return m_pattern;
	}

	/**
	 * パターンマッチ表現を設定します。
	 * 
	 * @param pattern パターンマッチ表現
	 */
	public void setPattern(String pattern) {
		this.m_pattern = pattern;
	}

	/**
	 * 処理タイプを返します。
	 * 
	 * @return 処理タイプ
	 * 
	 * @see com.clustercontrol.bean.ProcessConstant
	 */
	public int getProcessType() {
		return m_processType;
	}

	/**
	 * 処理タイプを設定します。
	 * 
	 * @param processType 処理タイプ
	 * 
	 * @see com.clustercontrol.bean.ProcessConstant
	 */
	public void setProcessType(int processType) {
		this.m_processType = processType;
	}

	/**
	 * 大文字・小文字を区別するか否かのフラグを返します。
	 * 
	 * @return 大文字・小文字を区別するか否かのフラグ
	 */
	public boolean getCaseSensitivityFlg() {
		return m_caseSensitivityFlg;
	}

	/**
	 * 大文字・小文字を区別するか否かのフラグを設定します。
	 * 
	 * @param processType 処理タイプ
	 */
	public void setCaseSensitivityFlg(boolean caseSensitivityFlg) {
		this.m_caseSensitivityFlg = caseSensitivityFlg;
	}

	/**
	 * 有効/無効を返します。
	 * 
	 * @return 有効/無効
	 * 
	 * @see com.clustercontrol.bean.ValidConstant
	 */
	public boolean isValidFlg() {
		return m_validFlg;
	}

	/**
	 * 有効/無効を設定します。
	 * 
	 * @param validFlg 有効/無効
	 * 
	 * @see com.clustercontrol.bean.ValidConstant
	 */
	public void setValidFlg(boolean validFlg) {
		this.m_validFlg = validFlg;
	}
}
