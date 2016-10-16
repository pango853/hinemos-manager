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

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * 実行情報を保持するクラス<BR>
 *
 * @version 4.1.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class RunInfo implements Serializable {
	private static final long serialVersionUID = -4752419336473888616L;

	/** ファシリティID */
	private String facilityId;
	/** セッションID */
	private String sessionId;
	/** 所属ジョブユニットのジョブID */
	private String jobunitId;
	/** ジョブID */
	private String jobId;
	/** コマンドタイプ */
	private Integer commandType = new Integer(0);
	/** コマンド */
	private String command;
	/** ユーザ種別 */
	private Integer specifyUser = new Integer(0);
	/** 実行ユーザ */
	private String user;
	/** 停止種別 */
	private Integer stopType = new Integer(0);

	/** scp(ssh)公開鍵 */
	private String publicKey;
	/** チェックサム */
	private String checkSum;

	/**
	 * コマンドを返します。
	 * 
	 * @return コマンド
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * コマンドを設定します。
	 * 
	 * @param command コマンド
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * コマンド種別を返します。
	 * 
	 * @return コマンド種別
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.CommandTypeConstant
	 */
	public Integer getCommandType() {
		return commandType;
	}

	/**
	 * コマンド種別を設定します。
	 * 
	 * @param commandType コマンド種別
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.CommandTypeConstant
	 */
	public void setCommandType(Integer commandType) {
		this.commandType = commandType;
	}

	/**
	 * 停止種別を返します。
	 * 
	 * @return 停止種別
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant
	 */
	public Integer getStopType() {
		return stopType;
	}

	/**
	 * 停止種別を設定します。
	 * 
	 * @param stopType 停止種別
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant
	 */
	public void setStopType(Integer stopType) {
		this.stopType = stopType;
	}

	/**
	 * ファシリティIDを返します。
	 * 
	 * @return ファシリティID
	 */
	public String getFacilityId() {
		return facilityId;
	}

	/**
	 * ファシリティIDを設定します。
	 * 
	 * @param facilityId ファシリティID
	 */
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * 所属ジョブユニットのジョブIDを返します。<BR>
	 * 
	 * @return 所属ジョブユニットのジョブID
	 */
	public String getJobunitId() {
		return jobunitId;
	}

	/**
	 * 所属ジョブユニットのジョブIDを設定します。<BR>
	 * 
	 * @param jobunitId 所属ジョブユニットのジョブID
	 */
	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	/**
	 * ジョブIDを返します。
	 * 
	 * @return ジョブID
	 */
	public String getJobId() {
		return jobId;
	}

	/**
	 * ジョブIDを設定します。
	 * 
	 * @param jobId ジョブID
	 */
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	/**
	 * セッションIDを返します。
	 * 
	 * @return セッションID
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * セッションIDを設定します。
	 * 
	 * @param sessionId セッションID
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * 公開キーを返します。
	 * 
	 * @return 公開キー
	 */
	public String getPublicKey() {
		return publicKey;
	}

	/**
	 * 公開キーを設定します。
	 * 
	 * @param publicKey 公開キー
	 */
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	/**
	 * ユーザ種別を返します。
	 * 
	 * @return ユーザ種別
	 */
	public Integer getSpecifyUser() {
		return specifyUser;
	}

	/**
	 * ユーザ種別を設定します。
	 * 
	 * @param specifyUser ユーザ種別
	 */
	public void setSpecifyUser(Integer specifyUser) {
		this.specifyUser = specifyUser;
	}

	/**
	 * 実行ユーザを返します。
	 * 
	 * @return 実行ユーザ
	 */
	public String getUser() {
		return user;
	}

	/**
	 * 実行ユーザを設定します。
	 * 
	 * @param user 実行ユーザ
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * チェックサムを返します。
	 * 
	 * @return チェックサム
	 */
	public String getCheckSum() {
		return checkSum;
	}

	/**
	 * チェックサムを設定します。
	 * 
	 * @param checkSum チェックサム
	 */
	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}

	@Override
	public boolean equals(Object o) {
		RunInfo other = (RunInfo)o;
		if (facilityId != null && other.facilityId == null) {
			return false;
		}
		if (facilityId == null && other.facilityId != null) {
			return false;
		}
		if (sessionId != null && other.sessionId == null) {
			return false;
		}
		if (sessionId == null && other.sessionId != null) {
			return false;
		}
		if (jobunitId != null && other.jobunitId == null) {
			return false;
		}
		if (jobunitId == null && other.jobunitId != null) {
			return false;
		}
		if (jobId != null && other.jobId == null) {
			return false;
		}
		if (jobId == null && other.jobId != null) {
			return false;
		}
		if (command != null && other.command == null) {
			return false;
		}
		if (command == null && other.command != null) {
			return false;
		}
		if (user != null && other.user == null) {
			return false;
		}
		if (user == null && other.user != null) {
			return false;
		}
		if (publicKey != null && other.publicKey == null) {
			return false;
		}
		if (publicKey == null && other.publicKey != null) {
			return false;
		}
		if (checkSum != null && other.checkSum == null) {
			return false;
		}
		if (checkSum == null && other.checkSum != null) {
			return false;
		}

		if (((facilityId == null && other.facilityId == null) ||
				facilityId.equals(other.facilityId))
				&&
				((sessionId == null && other.sessionId == null) ||
						sessionId.equals(other.sessionId))
						&&
						((jobunitId == null && other.jobunitId == null) ||
								jobunitId.equals(other.jobunitId))
								&&
								((jobId == null && other.jobId == null) ||
										jobId.equals(other.jobId))
										&&
										commandType.equals(other.commandType)
										&&
										((command == null && other.command == null) ||
												command.equals(other.command))
												&&
												((user == null && other.user == null) ||
														user.equals(other.user))
														&&
														((publicKey == null && other.publicKey == null) ||
																publicKey.equals(other.publicKey))
																&&
																((checkSum == null && other.checkSum == null) ||
																		checkSum.equals(other.checkSum))
																		&&
																		stopType == other.stopType
																		&&
																		specifyUser.equals(other.specifyUser)) {
			return true;
		}
		return false;
	}
}
