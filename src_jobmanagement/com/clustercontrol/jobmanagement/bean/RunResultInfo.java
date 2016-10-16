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
import java.util.List;

import javax.xml.bind.annotation.XmlType;

/**
 * 実行結果情報を保持するクラス<BR>
 *
 * @version 2.0.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class RunResultInfo extends RunInfo implements Serializable {
	private static final long serialVersionUID = -5920913289024178396L;

	/** 実行状態 */
	private Integer status = new Integer(0);
	/** 時刻 */
	private Long time = new Long(0);
	/** ファイルリスト */
	private List<String> fileList;
	/** 終了値 */
	private Integer endValue = new Integer(0);
	/** メッセージ */
	private String message;
	/** エラーメッセージ */
	private String errorMessage;

	/**
	 * 時刻を返します。
	 * 
	 * @return 時刻
	 */
	public Long getTime() {
		return time;
	}

	/**
	 * 時刻を設定します。
	 * 
	 * @param time 時刻
	 */
	public void setTime(Long time) {
		this.time = time;
	}

	/**
	 * 終了値を返します。
	 * 
	 * @return 終了値
	 */
	public Integer getEndValue() {
		return endValue;
	}

	/**
	 * 終了値を設定します。
	 * 
	 * @param endValue 終了値
	 */
	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}

	/**
	 * エラーメッセージを返します。
	 * 
	 * @return エラーメッセージ
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * エラーメッセージを設定します。
	 * 
	 * @param errorMessage エラーメッセージ
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * メッセージを返します。
	 * 
	 * @return メッセージ
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * メッセージを設定します。
	 * 
	 * @param message メッセージ
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 実行状態を返します。
	 * 
	 * @return 実行状態
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.RunStatusConstant
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * 実行状態を設定します。
	 * 
	 * @param status 実行状態
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.RunStatusConstant
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * ファイルリストを返します。
	 * 
	 * @return ファイルリスト
	 */
	public List<String> getFileList() {
		return fileList;
	}

	/**
	 * ファイルリストを設定します。
	 * 
	 * @param fileList ファイルリスト
	 */
	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}
}
