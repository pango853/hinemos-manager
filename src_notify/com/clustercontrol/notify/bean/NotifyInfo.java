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

package com.clustercontrol.notify.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * 通知情報を保持するクラスです。
 *
 * @version 2.0.0
 * @since 2.0.0
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
public class NotifyInfo implements Serializable
{
	private static final long serialVersionUID = -4587823656729541121L;

	/** 通知ID。 */
	private String notifyId;

	/** 説明。 */
	private String description;

	/**
	 * 通知タイプ
	 * @see com.clustercontrol.bean.NotifyTypeConstant
	 */
	private Integer notifyType;

	/**
	 * 初回通知までのカウント
	 */
	private Integer initialCount;

	/**
	 * 有効にした直後は通知しない
	 */
	private Integer notFirstNotify;
	
	/**
	 * 再通知種別
	 */
	private Integer renotifyType;

	/**
	 * 再通知抑制期間
	 */
	private Integer renotifyPeriod;

	/**
	 * オーナーロールID
	 */
	private String ownerRoleId;

	/** 作成日時。 */
	private Long regDate;

	/** 最終変更日時。 */
	private Long updateDate;

	/** 作成ユーザ　*/
	private String regUser;

	/** 最終更新ユーザ*/
	private String updateUser;

	/**有効無効フラグ*/
	private Integer validFlg;

	/** カレンダID */
	private String calendarId;

	/** 通知イベント情報。 */
	private NotifyCommandInfo notifyCommandInfo;
	private NotifyEventInfo notifyEventInfo;
	private NotifyJobInfo notifyJobInfo;
	private NotifyLogEscalateInfo notifyLogEscalateInfo;
	private NotifyMailInfo notifyMailInfo;
	private NotifyStatusInfo notifyStatusInfo;

	/**
	 * コンストラクタ。
	 */
	public NotifyInfo() {
	}

	/**
	 * コンストラクタ。
	 *
	 * @param notifyId 通知ID
	 * @param description 説明
	 * @param notiftType 通知タイプ
	 * @param setInitialCount 初回通知が実行されるまでの同一重要度カウンタ
	 * @param renotifyType 再通知種別
	 * @param renotifyPeriod 再通知抑制期間（分）
	 * @param regDate 作成日時
	 * @param updateDate 最終変更日時
	 * @param regUser 作成ユーザ
	 * @param updateUser 最終更新ユーザ
	 * @param validFlg 有効無効フラグ
	 * @param calendarId カレンダID
	 * @param notifyInfoDetail<NotifyInfoDetail> 通知情報詳細
	 *
	 * @see com.clustercontrol.bean.ValidConstant
	 * @see com.clustercontrol.bean.ExclusionConstant
	 * @see com.clustercontrol.bean.StatusExpirationConstant
	 * @see com.clustercontrol.bean.PriorityConstant
	 * @see com.clustercontrol.bean.StatusValidPeriodConstant
	 */
	public NotifyInfo(
			String notifyId,
			String description,
			Integer notifyType,
			Integer setInitialCount,
			Integer notFirstNotify,
			Integer renotifyType,
			Integer renotifyPeriod,
			Long regDate,
			Long updateDate,
			String regUser,
			String updateUser,
			Integer validFlg,
			String calendarId,
			String ownerRoleId,
			NotifyCommandInfo notifyCommandInfo,
			NotifyEventInfo notifyEventInfo,
			NotifyJobInfo notifyJobInfo,
			NotifyLogEscalateInfo notifyLogEscalatenfo,
			NotifyMailInfo notifyMailInfo,
			NotifyStatusInfo notifyStatusInfo) {

		setNotifyId(notifyId);
		setDescription(description);
		setNotifyType(notifyType);
		setInitialCount(setInitialCount);
		setNotFirstNotify(notFirstNotify);
		setRenotifyType(renotifyType);
		setRenotifyPeriod(renotifyPeriod);
		setRegDate(regDate);
		setUpdateDate(updateDate);
		setRegUser(regUser);
		setUpdateUser(updateUser);
		setValidFlg(validFlg);
		setCalendarId(calendarId);
		setOwnerRoleId(ownerRoleId);
		setNotifyCommandInfo(notifyCommandInfo);
		setNotifyEventInfo(notifyEventInfo);
		setNotifyJobInfo(notifyJobInfo);
		setNotifyLogEscalateInfo(notifyLogEscalatenfo);
		setNotifyMailInfo(notifyMailInfo);
		setNotifyStatusInfo(notifyStatusInfo);
	}


	/**
	 * コンストラクタ。
	 *
	 * @param otherData コピー元の通知情報
	 */
	public NotifyInfo( NotifyInfo otherData ) {

		setNotifyId(otherData.getNotifyId());
		setDescription(otherData.getDescription());
		setNotifyType(otherData.getNotifyType());
		setInitialCount(otherData.getInitialCount());
		setRenotifyType(otherData.getRenotifyType());
		setRenotifyPeriod(otherData.getRenotifyPeriod());
		setOwnerRoleId(otherData.getOwnerRoleId());
		setRegDate(otherData.getRegDate());
		setUpdateDate(otherData.getUpdateDate());
		setRegUser(otherData.getRegUser());
		setUpdateUser(otherData.getUpdateUser());
		setNotifyCommandInfo(otherData.getNotifyCommandInfo());
		setNotifyEventInfo(otherData.getNotifyEventInfo());
		setNotifyJobInfo(otherData.getNotifyJobInfo());
		setNotifyLogEscalateInfo(otherData.getNotifyLogEscalateInfo());
		setNotifyMailInfo(otherData.getNotifyMailInfo());
		setNotifyStatusInfo(otherData.getNotifyStatusInfo());
	}

	/**
	 * 通知IDを返します。
	 *
	 * @return 通知ID
	 */
	public String getNotifyId() {
		return this.notifyId;
	}

	/**
	 * 通知IDを設定します。
	 *
	 * @param notifyId 通知ID
	 */
	public void setNotifyId( String notifyId ) {
		this.notifyId = notifyId;
	}

	/**
	 * 説明を返します。
	 *
	 * @return 説明
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * 説明を設定します。
	 *
	 * @param description 説明
	 */
	public void setDescription( String description ) {
		this.description = description;
	}


	/**
	 * 通知タイプを取得します。
	 * @return
	 */
	public Integer getNotifyType(){

		return this.notifyType;
	}

	/**
	 * 通知タイプを設定します。
	 *
	 * @param notifyType
	 */
	public void setNotifyType(Integer notifyType){
		this.notifyType=notifyType;
	}

	/**
	 * 初回通知するまでのカウント（この数以上同じ重要度の監視結果が連続した場合に始めて通知）を返します。
	 *
	 * @return initialCount 初回通知するまでのカウント
	 */
	public Integer getInitialCount() {
		return this.initialCount;
	}

	/**
	 * 初回通知を設定します。
	 *
	 * @param initialCount 抑制期間（分）
	 */
	public void setInitialCount( Integer initialCount ) {
		this.initialCount = initialCount;
	}

	/**
	 * 有効にした直後は通知しない
	 *
	 * @return initialCount 初回通知するまでのカウント
	 */
	public Integer getNotFirstNotify() {
		return this.notFirstNotify;
	}

	/**
	 * 有効にした直後は通知しない。
	 *
	 * @param initialCount 抑制期間（分）
	 */
	public void setNotFirstNotify( Integer notFirstNotify ) {
		this.notFirstNotify = notFirstNotify;
	}

	
	
	/**
	 * 作成日時を返します。
	 *
	 * @return 作成日時
	 */
	public Long getRegDate() {
		return this.regDate;
	}

	/**
	 * 作成日時を設定します。
	 *
	 * @param regDate 作成日時
	 */
	public void setRegDate( Long regDate ) {
		this.regDate = regDate;
	}

	/**
	 * 最終変更日時を返します。
	 *
	 * @return 最終変更日時
	 */
	public Long getUpdateDate() {
		return this.updateDate;
	}

	/**
	 * 最終変更日時を設定します。
	 *
	 * @param updateDate 最終変更日時
	 */
	public void setUpdateDate( Long updateDate ) {
		this.updateDate = updateDate;
	}

	/**
	 * 登録ユーザを返します。
	 *
	 * @return　登録ユーザ
	 */
	public String getRegUser(){
		return this.regUser;
	}

	/**
	 * 登録ユーザを設定します。
	 *
	 * @param regUser
	 */
	public void setRegUser(String regUser){
		this.regUser=regUser;

	}


	/**
	 * 最終更新ユーザを返します。
	 *
	 * @return　最終更新ユーザ
	 */
	public String getUpdateUser(){

		return this.updateUser;
	}

	/**
	 * 最終更新ユーザを設定します。
	 *
	 * @param regUser
	 */
	public void setUpdateUser(String updateUser){
		this.updateUser=updateUser;

	}

	/**
	 * 有効無効フラグを返します。
	 *
	 * @return 有効無効フラグ
	 */
	public Integer getValidFlg() {
		return validFlg;
	}

	/**
	 * 有効無効フラグを設定します。
	 * @param vaildFlg
	 */
	public void setValidFlg(Integer validFlg) {
		this.validFlg = validFlg;
	}

	/**
	 * カレンダIDを返します。
	 * @return カレンダID
	 */
	public String getCalendarId() {
		return calendarId;
	}

	/**
	 * カレンダIDを設定します。
	 * @param calendarId
	 */
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	/**
	 * 再通知種別を返します。
	 * @return 再通知種別
	 */
	public Integer getRenotifyType() {
		return renotifyType;
	}

	/**
	 * 再通知種別を設定します。
	 * @param m_renotify_type 再通知種別
	 */
	public void setRenotifyType(Integer renotifyType) {
		this.renotifyType = renotifyType;
	}

	/**
	 * 再通知抑制期間を返します。
	 * @return 再通知抑制期間
	 */
	public Integer getRenotifyPeriod() {
		return renotifyPeriod;
	}

	/**
	 * 再通知抑制期間を設定します。
	 * @param m_renotify_period 再通知抑制期間
	 */
	public void setRenotifyPeriod(Integer renotifyPeriod) {
		this.renotifyPeriod = renotifyPeriod;
	}

	/**
	 * オーナーロールIDを返します。
	 * @return オーナーロールID
	 */
	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	/**
	 * オーナーロールIDを設定します。
	 * @param ownerRoleId オーナーロールID
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public NotifyCommandInfo getNotifyCommandInfo() {
		return notifyCommandInfo;
	}

	public void setNotifyCommandInfo(
			NotifyCommandInfo notifyCommandInfo) {
		this.notifyCommandInfo = notifyCommandInfo;
	}

	public NotifyEventInfo getNotifyEventInfo() {
		return notifyEventInfo;
	}

	public void setNotifyEventInfo(NotifyEventInfo notifyEventInfo) {
		this.notifyEventInfo = notifyEventInfo;
	}

	public NotifyJobInfo getNotifyJobInfo() {
		return notifyJobInfo;
	}

	public void setNotifyJobInfo(NotifyJobInfo notifyJobInfo) {
		this.notifyJobInfo = notifyJobInfo;
	}

	public NotifyLogEscalateInfo getNotifyLogEscalateInfo() {
		return notifyLogEscalateInfo;
	}

	public void setNotifyLogEscalateInfo(
			NotifyLogEscalateInfo notifyLogEscalateInfo) {
		this.notifyLogEscalateInfo = notifyLogEscalateInfo;
	}

	public NotifyMailInfo getNotifyMailInfo() {
		return notifyMailInfo;
	}

	public void setNotifyMailInfo(NotifyMailInfo notifyMailInfo) {
		this.notifyMailInfo = notifyMailInfo;
	}

	public NotifyStatusInfo getNotifyStatusInfo() {
		return notifyStatusInfo;
	}

	public void setNotifyStatusInfo(NotifyStatusInfo notifyStatusInfo) {
		this.notifyStatusInfo = notifyStatusInfo;
	}
}
