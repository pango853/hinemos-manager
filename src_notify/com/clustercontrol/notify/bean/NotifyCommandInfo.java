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

package com.clustercontrol.notify.bean;

import javax.xml.bind.annotation.XmlType;

/**
 * 通知コマンド情報を保持するクラスです。
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
public class NotifyCommandInfo  extends NotifyInfoDetail
{
	private static final long serialVersionUID = 5947279803743475747L;

	/**実行コマンド*/
	private String  infoCommand;
	private String  warnCommand;
	private String  criticalCommand;
	private String  unknownCommand;

	/**実効ユーザ*/
	private String infoEffectiveUser;
	private String warnEffectiveUser;
	private String criticalEffectiveUser;
	private String unknownEffectiveUser;

	/**コマンド実行時に環境変数を読み込むか否かのフラグ*/
	private Integer setEnvironment;

	/**コマンド実行のタイムアウト値*/
	private Long timeout;

	/**
	 * コンストラクタ。
	 */
	public NotifyCommandInfo(){
		super();
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param notifyId 通知ID
	 * @param priority 重要度
	 */
	public NotifyCommandInfo(
			String notifyId,
			Integer infoValidFlg,
			Integer warnValidFlg,
			Integer criticalValidFlg,
			Integer unknownValidFlg,

			String infoCommand,
			String warnCommand,
			String criticalCommand,
			String unknownCommand,

			String infoEffectiveUser,
			String warnEffectiveUser,
			String criticalEffectiveUser,
			String unknownEffectiveUser,

			Integer setEnvironment,
			Long timeout) {

		super(notifyId, infoValidFlg, warnValidFlg, criticalValidFlg, unknownValidFlg);

		setInfoCommand(infoCommand);
		setWarnCommand(warnCommand);
		setCriticalCommand(criticalCommand);
		setUnknownCommand(unknownCommand);

		setInfoEffectiveUser(infoEffectiveUser);
		setWarnEffectiveUser(warnEffectiveUser);
		setCriticalEffectiveUser(criticalEffectiveUser);
		setUnknownEffectiveUser(unknownEffectiveUser);

		setSetEnvironment(setEnvironment);
		setTimeout(timeout);
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param otherData コピー元の通知情報
	 */
	public NotifyCommandInfo( NotifyCommandInfo otherData ) {
		super(otherData);

		setInfoCommand(otherData.getInfoCommand());
		setInfoEffectiveUser(otherData.getInfoEffectiveUser());

		setWarnCommand(otherData.getWarnCommand());
		setWarnEffectiveUser(otherData.getWarnEffectiveUser());

		setCriticalCommand(otherData.getCriticalCommand());
		setCriticalEffectiveUser(otherData.getCriticalEffectiveUser());

		setUnknownCommand(otherData.getUnknownCommand());
		setUnknownEffectiveUser(otherData.getUnknownEffectiveUser());

		setSetEnvironment(otherData.getSetEnvironment());
		setTimeout(otherData.getTimeout());
	}

	public String getInfoCommand() {
		return infoCommand;
	}

	public void setInfoCommand(String infoCommand) {
		this.infoCommand = infoCommand;
	}

	public String getWarnCommand() {
		return warnCommand;
	}

	public void setWarnCommand(String warnCommand) {
		this.warnCommand = warnCommand;
	}

	public String getCriticalCommand() {
		return criticalCommand;
	}

	public void setCriticalCommand(String criticalCommand) {
		this.criticalCommand = criticalCommand;
	}

	public String getUnknownCommand() {
		return unknownCommand;
	}

	public void setUnknownCommand(String unknownCommand) {
		this.unknownCommand = unknownCommand;
	}

	public String getInfoEffectiveUser() {
		return infoEffectiveUser;
	}

	public void setInfoEffectiveUser(String infoEffectiveUser) {
		this.infoEffectiveUser = infoEffectiveUser;
	}

	public String getWarnEffectiveUser() {
		return warnEffectiveUser;
	}

	public void setWarnEffectiveUser(String warnEffectiveUser) {
		this.warnEffectiveUser = warnEffectiveUser;
	}

	public String getCriticalEffectiveUser() {
		return criticalEffectiveUser;
	}

	public void setCriticalEffectiveUser(String criticalEffectiveUser) {
		this.criticalEffectiveUser = criticalEffectiveUser;
	}

	public String getUnknownEffectiveUser() {
		return unknownEffectiveUser;
	}

	public void setUnknownEffectiveUser(String unknownEffectiveUser) {
		this.unknownEffectiveUser = unknownEffectiveUser;
	}

	public Integer getSetEnvironment() {
		return setEnvironment;
	}

	public void setSetEnvironment(Integer setEnvironment) {
		this.setEnvironment = setEnvironment;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}
}
