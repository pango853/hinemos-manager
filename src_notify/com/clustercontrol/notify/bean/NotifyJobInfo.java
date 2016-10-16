/*

 Copyright (C) 2008 NTT DATA Corporation

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
 * 通知ジョブ情報を保持するクラスです。
 * 
 * @version 3.0.0
 * @since 3.0.0
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
public class NotifyJobInfo  extends NotifyInfoDetail
{
	private static final long serialVersionUID = -5089086735470199399L;

	/** 所属ジョブユニットのジョブID */
	private String infoJobunitId;
	private String warnJobunitId;
	private String criticalJobunitId;
	private String unknownJobunitId;

	/** ジョブID*/
	private String infoJobId;
	private String warnJobId;
	private String criticalJobId;
	private String unknownJobId;

	/** ジョブ実行失敗時重要度*/
	private Integer infoJobFailurePriority;
	private Integer warnJobFailurePriority;
	private Integer criticalJobFailurePriority;
	private Integer unknownJobFailurePriority;

	/** ジョブ実行ファシリティフラグ*/
	private Integer jobExecFacilityFlg;

	/**ジョブ実行ファシリティID*/
	private String jobExecFacility;

	/**ジョブ実行スコープ*/
	private String jobExecScope;

	/**
	 * コンストラクタ。
	 */
	public NotifyJobInfo() {
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param notifyId 通知ID
	 * @param priority 重要度
	 * @param validFlg 通知フラグ
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param jobFailurePriority ジョブ実行失敗時重要度
	 * @param jobExecFacilityFlg ジョブ実行ファシリティフラグ
	 * @param jobExecFacility    ジョブ実行ファシリティ
	 * 
	 * @see com.clustercontrol.bean.PriorityConstant
	 * @see com.clustercontrol.bean.ValidConstant
	 * @see com.clustercontrol.bean.EventConfirmConstant
	 */
	public NotifyJobInfo(
			String notifyId,

			Integer infoValidFlg,
			Integer warnValidFlg,
			Integer criticalValidFlg,
			Integer unknownValidFlg,

			String infoJobunitId,
			String warnJobunitId,
			String criticalJobunitId,
			String unknownJobunitId,

			String infoJobId,
			String warnJobId,
			String criticalJobId,
			String unknownJobId,

			Integer infoJobFailurePriority,
			Integer warnJobFailurePriority,
			Integer criticalJobFailurePriority,
			Integer unknownJobFailurePriority,

			Integer jobExecFacilityFlg,
			String jobExecFacility,
			String jobExecScope) {
		super(notifyId, infoValidFlg, warnValidFlg, criticalValidFlg, unknownValidFlg);


		setInfoJobunitId(infoJobunitId);
		setWarnJobunitId(warnJobunitId);
		setCriticalJobunitId(criticalJobunitId);
		setUnknownJobId(unknownJobId);

		setInfoJobId(infoJobId);
		setWarnJobId(warnJobId);
		setCriticalJobId(criticalJobId);
		setUnknownJobunitId(unknownJobunitId);

		setInfoJobFailurePriority(infoJobFailurePriority);
		setWarnJobFailurePriority(warnJobFailurePriority);
		setCriticalJobFailurePriority(criticalJobFailurePriority);
		setUnknownJobFailurePriority(unknownJobFailurePriority);

		setJobExecFacilityFlg(jobExecFacilityFlg);
		setJobExecFacility(jobExecFacility);
		setJobExecScope(jobExecScope);
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param otherData コピー元の通知情報
	 */
	public NotifyJobInfo( NotifyJobInfo otherData ) {
		super(otherData);

		setInfoJobunitId(otherData.getInfoJobunitId());
		setWarnJobunitId(otherData.getWarnJobunitId());
		setCriticalJobunitId(otherData.getCriticalJobunitId());
		setUnknownJobunitId(otherData.getUnknownJobunitId());

		setInfoJobId(otherData.getInfoJobId());
		setWarnJobId(otherData.getWarnJobId());
		setCriticalJobId(otherData.getCriticalJobId());
		setUnknownJobId(otherData.getUnknownJobId());

		setInfoJobFailurePriority(otherData.getInfoJobFailurePriority());
		setWarnJobFailurePriority(otherData.getWarnJobFailurePriority());
		setCriticalJobFailurePriority(otherData.getCriticalJobFailurePriority());
		setUnknownJobFailurePriority(otherData.getUnknownJobFailurePriority());

		setJobExecFacilityFlg(otherData.getJobExecFacilityFlg());
		setJobExecFacility(otherData.getJobExecFacility());
		setJobExecScope(otherData.getJobExecScope());
	}

	public String getInfoJobunitId() {
		return infoJobunitId;
	}

	public void setInfoJobunitId(String infoJobunitId) {
		this.infoJobunitId = infoJobunitId;
	}

	public String getWarnJobunitId() {
		return warnJobunitId;
	}

	public void setWarnJobunitId(String warnJobunitId) {
		this.warnJobunitId = warnJobunitId;
	}

	public String getCriticalJobunitId() {
		return criticalJobunitId;
	}

	public void setCriticalJobunitId(String criticalJobunitId) {
		this.criticalJobunitId = criticalJobunitId;
	}

	public String getUnknownJobunitId() {
		return unknownJobunitId;
	}

	public void setUnknownJobunitId(String unknownJobunitId) {
		this.unknownJobunitId = unknownJobunitId;
	}

	public String getInfoJobId() {
		return infoJobId;
	}

	public void setInfoJobId(String infoJobId) {
		this.infoJobId = infoJobId;
	}

	public String getWarnJobId() {
		return warnJobId;
	}

	public void setWarnJobId(String warnJobId) {
		this.warnJobId = warnJobId;
	}

	public String getCriticalJobId() {
		return criticalJobId;
	}

	public void setCriticalJobId(String criticalJobId) {
		this.criticalJobId = criticalJobId;
	}

	public String getUnknownJobId() {
		return unknownJobId;
	}

	public void setUnknownJobId(String unknownJobId) {
		this.unknownJobId = unknownJobId;
	}

	public Integer getInfoJobFailurePriority() {
		return infoJobFailurePriority;
	}

	public void setInfoJobFailurePriority(Integer infoJobFailurePriority) {
		this.infoJobFailurePriority = infoJobFailurePriority;
	}

	public Integer getWarnJobFailurePriority() {
		return warnJobFailurePriority;
	}

	public void setWarnJobFailurePriority(Integer warnJobFailurePriority) {
		this.warnJobFailurePriority = warnJobFailurePriority;
	}

	public Integer getCriticalJobFailurePriority() {
		return criticalJobFailurePriority;
	}

	public void setCriticalJobFailurePriority(Integer criticalJobFailurePriority) {
		this.criticalJobFailurePriority = criticalJobFailurePriority;
	}

	public Integer getUnknownJobFailurePriority() {
		return unknownJobFailurePriority;
	}

	public void setUnknownJobFailurePriority(Integer unknownJobFailurePriority) {
		this.unknownJobFailurePriority = unknownJobFailurePriority;
	}

	public Integer getJobExecFacilityFlg() {
		return jobExecFacilityFlg;
	}

	public void setJobExecFacilityFlg(Integer jobExecFacilityFlg) {
		this.jobExecFacilityFlg = jobExecFacilityFlg;
	}

	public String getJobExecFacility() {
		return jobExecFacility;
	}

	public void setJobExecFacility(String jobExecFacility) {
		this.jobExecFacility = jobExecFacility;
	}

	public String getJobExecScope() {
		return jobExecScope;
	}

	public void setJobExecScope(String jobExecScope) {
		this.jobExecScope = jobExecScope;
	}
}
