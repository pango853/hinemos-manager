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
 * 実行指示情報を保持するクラス<BR>
 * @version 2.0.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class RunInstructionInfo extends RunInfo implements Serializable {
	private static final long serialVersionUID = -4324117941296918253L;

	/** 実行種別 */
	private String runType;
	/** 入力ファイル */
	private String inputFile;
	/** ファイルリスト取得パス */
	private String filePath;

	/**
	 * 入力ファイルを返します。
	 * 
	 * @return 入力ファイル
	 */
	public String getInputFile() {
		return inputFile;
	}

	/**
	 * 入力ファイルを設定します。
	 * 
	 * @param inputFile 入力ファイル
	 */
	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	/**
	 * 実行種別を返します。
	 * 
	 * @return 実行種別
	 */
	public String getRunType() {
		return runType;
	}

	/**
	 * 実行種別を設定します。
	 * 
	 * @param runType 実行種別
	 */
	public void setRunType(String runType) {
		this.runType = runType;
	}

	/**
	 * ファイルリスト取得パスを返します。
	 * 
	 * @return ファイルリスト取得パス
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * ファイルリスト取得パスを設定します。
	 * 
	 * @param filePath ファイルリスト取得パス
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public boolean equals(Object otherObject) {
		if (otherObject == null || !(otherObject instanceof RunInstructionInfo)) {
			return false;
		}
		RunInstructionInfo rInfo = (RunInstructionInfo) otherObject;
		if (rInfo.runType != null && this.runType == null) {
			return false;
		}
		if (rInfo.runType == null && this.runType != null) {
			return false;
		}
		if (rInfo.inputFile != null && this.inputFile == null) {
			return false;
		}
		if (rInfo.inputFile == null && this.inputFile != null) {
			return false;
		}
		if (rInfo.filePath != null && this.filePath == null) {
			return false;
		}
		if (rInfo.filePath == null && this.filePath != null) {
			return false;
		}

		if ((rInfo.runType == null && this.runType == null) ||
				(rInfo.runType.equals(this.runType))) {
			if ((rInfo.inputFile == null && this.inputFile == null) ||
					(rInfo.inputFile.equals(this.inputFile))) {
				if ((rInfo.filePath == null && this.filePath == null) ||
						(rInfo.filePath.equals(this.filePath))) {
					return super.equals(rInfo);
				}
			}
		}
		return false;
	}

	public static void main(String args[]) {
		RunInstructionInfo a = new RunInstructionInfo();
		a.setCheckSum("checksum");
		a.setCommand("command");
		a.setCommandType(1);
		a.setFacilityId("facilityId");
		a.setFilePath("filePath");
		a.setInputFile("inputFile");
		a.setJobId("jobId");
		a.setJobunitId("jobunitId");
		a.setPublicKey("publicKey");
		a.setRunType("runType");
		a.setSessionId("sessionId");
		a.setUser("user");

		RunInstructionInfo b = new RunInstructionInfo();
		b.setCheckSum("checksum");
		b.setCommand("command");
		b.setCommandType(1);
		b.setFacilityId("facilityId");
		b.setFilePath("filePath");
		b.setInputFile("inputFile");
		b.setJobId("jobId");
		b.setJobunitId("jobunitId");
		b.setPublicKey("publicKey");
		b.setRunType("runType");
		b.setSessionId("sessionId");
		b.setUser("user");

		System.out.println("a.equals(b)=" + a.equals(b));
	}
}
