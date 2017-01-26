/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.custom.bean;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.fault.CustomInvalid;
import com.clustercontrol.custom.bean.CustomConstant;
import com.clustercontrol.monitor.run.bean.MonitorCheckInfo;

/**
 * コマンド監視特有の設定情報を保持するBeanクラス<BR />
 * 1つの監視設定に対して、1つのインスタンスが対応する。<BR />
 * 
 * @since 4.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class CustomCheckInfo extends MonitorCheckInfo {

	private static final long serialVersionUID = -2657779605967474349L;

	// コマンドの実行種別
	private CustomConstant.CommandExecType execType;

	// 実行する特定ノードのファシリティID(type=INDIVIDUALの場合はnull）
	private String selectedFacilityId;

	//実効ユーザ種別
	private Integer specifyUser;

	// 実効ユーザ
	private String effectiveUser;

	// コマンド文字列
	private String command;

	// タイムアウト時間（ミリ秒）
	private Integer timeout;

	/**
	 * non-argumentコンストラクタ for JAXB
	 */
	public CustomCheckInfo() {

	}

	/**
	 * コンストラクタ
	 * @throws CustomInvalid コマンド監視設定が不正な場合
	 */
	public CustomCheckInfo(String monitorTypeId, String monitorId, CustomConstant.CommandExecType execType, String selectedFacilityId, Integer specifyUser, String effectiveUser, String command, Integer timeout) throws CustomInvalid {
		setMonitorTypeId(monitorTypeId);
		setMonitorId(monitorId);
		this.execType = execType;
		this.selectedFacilityId = selectedFacilityId;
		this.specifyUser = specifyUser;
		this.effectiveUser = effectiveUser;
		this.command = command;
		this.timeout = timeout;

	}

	/**
	 * コマンド監視設定の実行種別を返す。<br/>
	 * @return 実行種別
	 */
	public CustomConstant.CommandExecType getCommandExecType() {
		return execType;
	}

	/**
	 * コマンド監視設定の実行種別をセットする。<br/>
	 * @param execType 実行種別
	 */
	public void setCommandExecType(CustomConstant.CommandExecType execType) {
		this.execType = execType;
	}

	/**
	 * コマンド監視設定の特定ノードのファシリティIDを返す。<br/>
	 * @return 特定ノードのファシリティID
	 */
	public String getSelectedFacilityId() {
		return selectedFacilityId;
	}

	/**
	 * コマンド監視設定の特定ノードのファシリティIDをセットする。<br/>
	 * @param selectedFacilityId 特定ノードのファシリティID
	 */
	public void setSelectedFacilityId(String selectedFacilityId) {
		this.selectedFacilityId = selectedFacilityId;
	}

	/**
	 * 実効ユーザ種別を設定する。<br/>
	 * @param specifyUser 実効ユーザ種別
	 */
	public void setSpecifyUser(Integer specifyUser) {
		this.specifyUser = specifyUser;
	}

	/**
	 * 実効ユーザ種別を返す。<br/>
	 * @return 実効ユーザ種別
	 */
	public Integer getSpecifyUser() {
		return specifyUser;
	}

	/**
	 * コマンド監視設定の実効ユーザを返す。<br/>
	 * @return 実効ユーザ
	 */
	public String getEffectiveUser() {
		return effectiveUser;
	}

	/**
	 * コマンド監視設定の実効ユーザをセットする。<br/>
	 * @param effectiveUser 実効ユーザ
	 */
	public void setEffectiveUser(String effectiveUser) {
		this.effectiveUser = effectiveUser;
	}

	/**
	 * コマンド監視設定のコマンド文字列を返す。<br/>
	 * @return コマンド文字列
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * コマンド監視設定のコマンド文字列をセットする。<br/>
	 * @param command コマンド文字列
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * コマンド実行のタイムアウト時間[msec]を返す。<br/>
	 * @return タイムアウト時間[msec]
	 */
	public Integer getTimeout() {
		return timeout;
	}

	/**
	 * コマンド実行のタイムアウト時間[msec]をセットする。<br/>
	 * @param timeout タイムアウト時間[msec]
	 */
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	@Override
	public String toString() {
		return this.getClass().getCanonicalName() + " [execType = " + execType
				+ ", selectedFacilityId = " + selectedFacilityId
				+ ", effectiveUser = " + effectiveUser
				+ ", command = " + command
				+ ", timeout = " + timeout
				+ "]";
	}

}
