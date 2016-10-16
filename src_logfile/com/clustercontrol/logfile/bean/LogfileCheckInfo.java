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

package com.clustercontrol.logfile.bean;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.bean.MonitorCheckInfo;

/**
 * ログファイル監視設定情報のBeanクラス<BR>
 * 
 * @version 4.1.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class LogfileCheckInfo extends MonitorCheckInfo {

	private static final long serialVersionUID = -5875124203155944914L;

	/** ログファイル名 */
	private String m_logfile;

	/** ディレクトリ */
	private String m_directory;

	/** ファイル名 */
	private String m_fileName;

	/** ファイルエンコーディング */
	private String m_fileEncoding;

	/** ファイル改行コード */
	private String m_fileReturnCode;
	
	public LogfileCheckInfo(){
	}

	public String getLogfile() {
		return m_logfile;
	}

	public void setLogfile(String logfile) {
		this.m_logfile = logfile;
	}

	/**
	 * ディレクトリ
	 **/
	public String getDirectory() {
		return m_directory;
	}
	/**
	 * ディレクトリ
	 **/
	public void setDirectory(String directory) {
		this.m_directory = directory;
	}
	/**
	 *ファイル名
	 **/
	public String getFileName() {
		return m_fileName;
	}
	/**
	 *ファイル名
	 **/
	public void setFileName(String fileName) {
		this.m_fileName = fileName;
	}

	/**
	 * ファイルエンコーディング
	 */
	public String getFileEncoding() {
		return m_fileEncoding;
	}

	/**
	 * ファイルエンコーディング
	 * @param fileEncoding
	 */
	public void setFileEncoding(String fileEncoding) {
		this.m_fileEncoding = fileEncoding;
	}

	/**
	 * ファイル改行コード
	 */
	public String getFileReturnCode() {
		return m_fileReturnCode;
	}

	/**
	 * ファイル改行コード
	 * @param fileReturnCode
	 */
	public void setFileReturnCode(String fileReturnCode) {
		this.m_fileReturnCode = fileReturnCode;
	}
}
