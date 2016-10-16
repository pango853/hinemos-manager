/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.snmptrap.bean;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.bean.MonitorCheckInfo;

/**
 * SNMPTRAP監視のチェック条件のBean<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class TrapCheckInfo extends MonitorCheckInfo {
	private static final long serialVersionUID = -6268421338333854249L;

	/** コミュニティ名チェックの有無 */
	private Integer communityCheck = new Integer(0);
	/** コミュニティ名 */
	private String communityName;
	/** 文字コード変換の有無 */
	private Integer charsetConvert = new Integer(0);
	/** 文字コード/文字セット名 */
	private String charsetName;
	/** 未指定のトラップ受信時に通知する */
	private boolean notifyofReceivingUnspecifiedFlg;
	/** 未指定のトラップ受信時に通知するが有効な場合の通知レベル */
	private int priorityUnspecified;
	/** トラップ情報のリスト */
	private List<TrapValueInfo> trapValueInfos = new ArrayList<>();

	/**
	 * デフォルトコンストラクタ
	 */
	public TrapCheckInfo() {
	}

	/**
	 * コンストラクタ
	 * @param communityName
	 * @param communityCheck
	 * @param charsetConvert
	 * @param charsetName
	 * @param notifyofReceivingUnspecifiedFlg
	 * @param priorityUnspecified
	 */
	public TrapCheckInfo(
			String communityName,
			Integer communityCheck,
			Integer charsetConvert,
			String charsetName,
			boolean notifyofReceivingUnspecifiedFlg,
			int priorityUnspecified
			) {
		super();
		this.communityName = communityName;
		this.communityCheck = communityCheck;
		this.charsetConvert = charsetConvert;
		this.charsetName = charsetName;
		this.notifyofReceivingUnspecifiedFlg = notifyofReceivingUnspecifiedFlg;
		this.priorityUnspecified = priorityUnspecified;
	}



	public String getCommunityName() {
		return communityName;
	}

	public void setCommunityName(String communityName) {
		this.communityName = communityName;
	}

	public Integer getCommunityCheck() {
		return communityCheck;
	}

	public void setCommunityCheck(Integer communityCheck) {
		this.communityCheck = communityCheck;
	}

	public Integer getCharsetConvert() {
		return charsetConvert;
	}

	public void setCharsetConvert(Integer charsetConvert) {
		this.charsetConvert = charsetConvert;
	}

	public String getCharsetName() {
		return charsetName;
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}

	public boolean getNotifyofReceivingUnspecifiedFlg() {
		return notifyofReceivingUnspecifiedFlg;
	}
	public void setNotifyofReceivingUnspecifiedFlg(
			boolean notifyofReceivingUnspecifiedFlg) {
		this.notifyofReceivingUnspecifiedFlg = notifyofReceivingUnspecifiedFlg;
	}

	public int getPriorityUnspecified() {
		return priorityUnspecified;
	}
	public void setPriorityUnspecified(int priorityUnspecified) {
		this.priorityUnspecified = priorityUnspecified;
	}

	public List<TrapValueInfo> getTrapValueInfos() {
		return trapValueInfos;
	}

	public void setTrapValueInfos(List<TrapValueInfo> trapValueInfos) {
		this.trapValueInfos = trapValueInfos;
	}
}