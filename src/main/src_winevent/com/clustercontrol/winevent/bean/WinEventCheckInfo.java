/*

Copyright (C) 2013 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.winevent.bean;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.bean.MonitorCheckInfo;

/**
 * Windowsイベント監視特有の設定情報を保持するクラス<BR />
 * 
 * @since 4.1
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class WinEventCheckInfo extends MonitorCheckInfo {

	private static final long serialVersionUID = -9120176010178698902L;

	private boolean levelCritical;		// 重大
	private boolean levelWarning;		// 警告
	private boolean levelVerbose;		// 詳細
	private boolean levelError;			// エラー
	private boolean levelInformational;	// 情報
	private List<String> logName;		// ログ (Ex. Application, System, Microsoft Internet Explorer)
	private List<String> source;		// ソース (Ex. Cmd)
	private List<Integer> eventId;		// イベントID (Ex. 1, 3, 5, 1000)
	private List<Integer> category;		// タスクのカテゴリ(数値) (Ex. ログオン)
	private List<Long> keywords;		// キーワード（数値） (Ex. 成功の監査、クラシック)

	/**
	 * non-argumentコンストラクタ for JAXB
	 */
	public WinEventCheckInfo() {

	}

	/**
	 * コンストラクタ
	 */
	public WinEventCheckInfo(String monitorTypeId, String monitorId) {
		setMonitorTypeId(monitorTypeId);
		setMonitorId(monitorId);
	}

	/**
	 * レベル（重大）を対象とするか否かを返す。<br/>
	 * 
	 * @return レベル（重大）を対象とするか否か
	 */
	public boolean isLevelCritical() {
		return levelCritical;
	}

	/**
	 * レベル（重大）を対象とするか否かをセットする。<br/>
	 * 
	 * @param levelCritical
	 *            レベル（重大）を対象とするか否か
	 */
	public void setLevelCritical(boolean levelCritical) {
		this.levelCritical = levelCritical;
	}

	/**
	 * レベル（警告）を対象とするか否かを返す。<br/>
	 * 
	 * @return レベル（警告）を対象とするか否か
	 */
	public boolean isLevelWarning() {
		return levelWarning;
	}

	/**
	 * レベル（警告）を対象とするか否かをセットする。<br/>
	 * 
	 * @param levelWarning
	 *            レベル（警告）を対象とするか否か
	 */
	public void setLevelWarning(boolean levelWarning) {
		this.levelWarning = levelWarning;
	}

	/**
	 * レベル（詳細）を対象とするか否かを返す。<br/>
	 * 
	 * @return レベル（詳細）を対象とするか否か
	 */
	public boolean isLevelVerbose() {
		return levelVerbose;
	}

	/**
	 * レベル（詳細）を対象とするか否かをセットする。<br/>
	 * 
	 * @param levelVerbose
	 *            レベル（詳細）を対象とするか否か
	 */
	public void setLevelVerbose(boolean levelVerbose) {
		this.levelVerbose = levelVerbose;
	}

	/**
	 * レベル（エラー）を対象とするか否かを返す。<br/>
	 * 
	 * @return レベル（エラー）を対象とするか否か
	 */
	public boolean isLevelError() {
		return levelError;
	}

	/**
	 * レベル（エラー）を対象とするか否かをセットする。<br/>
	 * 
	 * @param levelError
	 *            レベル（エラー）を対象とするか否か
	 */
	public void setLevelError(boolean levelError) {
		this.levelError = levelError;
	}

	/**
	 * レベル（情報）を対象とするか否かを返す。<br/>
	 * 
	 * @return レベル（情報）を対象とするか否か
	 */
	public boolean isLevelInformational() {
		return levelInformational;
	}

	/**
	 * レベル（情報）を対象とするか否かをセットする。<br/>
	 * 
	 * @param levelInformational
	 *            レベル（情報）を対象とするか否か
	 */
	public void setLevelInformational(boolean levelInformational) {
		this.levelInformational = levelInformational;
	}

	public List<String> getLogName(){
		if (logName == null) {
			logName = new ArrayList<String>();
		}
		return logName;
	}

	public void setLogName(List<String> logName){
		this.logName = logName;
	}

	public List<String> getSource(){
		if (source == null) {
			source = new ArrayList<String>();
		}
		return source;
	}

	public void setSource(List<String> source){
		this.source = source;
	}

	public List<Integer> getEventId() {
		if (eventId == null) {
			eventId = new ArrayList<Integer>();
		}
		return eventId;
	}

	public void setEventId(List<Integer> eventId){
		this.eventId = eventId;
	}

	public List<Integer> getCategory(){
		if (category == null) {
			category = new ArrayList<Integer>();
		}
		return category;
	}

	public void setCategory(List<Integer> category){
		this.category = category;
	}

	public List<Long> getKeywords() {
		if (keywords == null) {
			keywords = new ArrayList<Long>();
		}
		return keywords;
	}

	public void setKeywords(List<Long> keywords) {
		this.keywords = keywords;
	}



	@Override
	public String toString() {
		return this.getClass().getCanonicalName()
				+ " [critical=" + levelCritical
				+ ", warning=" + levelWarning
				+ ", verbose=" + levelVerbose
				+ ", error=" + levelError
				+ ", informational=" + levelInformational + "]";
	}

}
