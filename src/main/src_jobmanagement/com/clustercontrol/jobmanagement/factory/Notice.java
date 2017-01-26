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

package com.clustercontrol.jobmanagement.factory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.bean.YesNoConstant;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.util.Messages;

/**
 * 監視管理に通知するクラスです。
 *
 * @version 3.0.0
 * @since 2.0.0
 */
public class Notice {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( Notice.class );

	/**
	 * セッションID、ジョブユニットID、ジョブIDからジョブ通知情報を取得し、<BR>
	 * ジョブ通知情報と終了状態を基に、ログ出力情報作成し、監視管理に通知します。
	 * 
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param type 終了状態
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.bean.EndStatusConstant
	 * @see com.clustercontrol.bean.JobConstant
	 * @see com.clustercontrol.monitor.message.LogOutputNotifyInfo
	 */
	public void notify(String sessionId, String jobunitId, String jobId, Integer type) throws JobInfoNotFound, InvalidRole {
		m_log.debug("notify() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", type=" + type);

		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		JobInfoEntity job = sessionJob.getJobInfoEntity();
		Integer priority = getPriority(type, job);

		// 通知先の指定において以下の場合は通知しない。
		// 1.重要度がnull、もしくはPriorityConstant.TYPE_NONE（空欄）
		// 2.通知IDがnull、もしくは0文字
		if (priority == null || priority == PriorityConstant.TYPE_NONE
				|| job.getNotifyGroupId() == null
				|| job.getNotifyGroupId().isEmpty()) {
			return;
		}

		//通知する

		//通知情報作成
		OutputBasicInfo info = new OutputBasicInfo();
		//プラグインID
		info.setPluginId(HinemosModuleConstant.JOB);
		//アプリケーション
		info.setApplication(Messages.getString("job.management"));
		//監視項目ID
		info.setMonitorId(sessionId);
		//メッセージID、メッセージ、オリジナルメッセージ
		if(type == EndStatusConstant.TYPE_BEGINNING){
			info.setMessageId("001");
			String jobType = JobConstant.typeToString(job.getJobType());
			String jobName = job.getJobName();
			String[] args1 = {jobType,jobName,jobId,sessionId};
			info.setMessage(Messages.getString("message.job.38", args1));
		} else if(type == EndStatusConstant.TYPE_NORMAL){
			info.setMessageId("002");
			String jobType = JobConstant.typeToString(job.getJobType());
			String jobName = job.getJobName();
			String[] args1 = {jobType,jobName,jobId,sessionId};
			info.setMessage(Messages.getString("message.job.39", args1));
		} else if(type == EndStatusConstant.TYPE_WARNING){
			info.setMessageId("003");
			String jobType = JobConstant.typeToString(job.getJobType());
			String jobName = job.getJobName();
			String[] args1 = {jobType,jobName,jobId,sessionId};
			info.setMessage(Messages.getString("message.job.40", args1));
		} else if(type == EndStatusConstant.TYPE_ABNORMAL){
			info.setMessageId("004");
			String jobType = JobConstant.typeToString(job.getJobType());
			String jobName = job.getJobName();
			String[] args1 = {jobType,jobName,jobId,sessionId};
			info.setMessage(Messages.getString("message.job.41", args1));
		}
		if(job.getJobType() == JobConstant.TYPE_JOB){
			//ファシリティID
			info.setFacilityId(job.getFacilityId());
			//スコープ
			info.setScopeText(sessionJob.getScopeText());
		} else {
			//ファシリティID
			info.setFacilityId("");
			//スコープ
			info.setScopeText("");
		}
		//重要度
		m_log.debug("priority = " + priority);
		info.setPriority(priority);
		//発生日時
		info.setGenerationDate(new Date().getTime());

		JobSessionJobEntity entity = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		List<JobSessionNodeEntity> list = entity.getJobSessionNodeEntities();
		List<String> facilityId = new ArrayList<String>();
		List<String> jobMessage = new ArrayList<String>();
		for (JobSessionNodeEntity node : list) {
			facilityId.add(node.getId().getFacilityId());
			jobMessage.add(node.getMessage());
			m_log.debug("Notice.notify  >>>info.setJobFacilityId() = : " + node.getId().getFacilityId());
			m_log.debug("Notice.notify  >>>info.setJobMessage() = : " + node.getMessage());
		}
		info.setJobFacilityId(facilityId);
		info.setJobMessage(jobMessage);

		try{
			//メッセージ送信
			if (m_log.isDebugEnabled()) {
				m_log.debug("sending message"
						+ " : priority=" + info.getPriority()
						+ " pluginId=" + info.getPluginId()
						+ " monitorId=" + info.getMonitorId()
						+ " facilityId=" + info.getFacilityId() + ")");
			}

			// 通知処理
			new NotifyControllerBean().notify(info, job.getNotifyGroupId());
		} catch (Exception e) {
			m_log.warn("notify() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
	}

	private Integer getPriority(Integer type, JobInfoEntity job) {
		Integer priority = null;
		switch (type) {
		case EndStatusConstant.TYPE_BEGINNING:
			priority = job.getBeginPriority();
			break;
		case EndStatusConstant.TYPE_NORMAL:
			priority = job.getNormalPriority();
			break;
		case EndStatusConstant.TYPE_WARNING:
			priority = job.getWarnPriority();
			break;
		case EndStatusConstant.TYPE_ABNORMAL:
			priority = job.getAbnormalPriority();
			break;
		default:
			break;
		}
		return priority;
	}

	/**
	 * 遅延通知
	 * セッションID、ジョブユニットID、ジョブIDからジョブ通知情報を取得し、<BR>
	 * ジョブ通知情報と開始遅延フラグを基に、ログ出力情報作成し、監視管理に通知します。
	 * 
	 * @param sessionId セッションID
	 * @param jobId ジョブID
	 * @param startDelay 開始遅延フラグ（true：開始遅延、false：終了遅延）
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.bean.EndStatusConstant
	 * @see com.clustercontrol.bean.JobConstant
	 * @see com.clustercontrol.monitor.message.LogOutputNotifyInfo
	 */
	public void delayNotify(String sessionId, String jobunitId, String jobId, boolean startDelay) throws JobInfoNotFound, InvalidRole {
		m_log.debug("delayNotify() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", startDelay=" + startDelay);

		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		JobInfoEntity job = sessionJob.getJobInfoEntity();

		// 通知先の指定において以下の場合は通知しない
		// 1.通知IDがnull、もしくは0文字
		if(job.getNotifyGroupId() == null || job.getNotifyGroupId().isEmpty()){
			return;
		}

		//通知する

		//通知情報作成
		OutputBasicInfo info = new OutputBasicInfo();
		//プラグインID
		info.setPluginId(HinemosModuleConstant.JOB);
		//アプリケーション
		info.setApplication(Messages.getString("job.management"));
		//監視項目ID
		info.setMonitorId(sessionId);

		//メッセージID、メッセージ、オリジナルメッセージ
		StringBuilder message = new StringBuilder();
		if(startDelay){
			info.setMessageId("101");
			String jobType = JobConstant.typeToString(job.getJobType());
			String jobName = job.getJobName();
			String[] args1 = {jobType,jobName,jobId,sessionId};
			message.append(Messages.getString("message.job.55", args1));

			//操作
			if(job.getStartDelayOperation() == YesNoConstant.TYPE_YES){
				int type = job.getStartDelayOperationType();

				String[] args2 = {OperationConstant.typeToString(type)};
				message.append(" " + Messages.getString("message.job.57", args2));

				if(type == OperationConstant.TYPE_STOP_SKIP){
					info.setMessageId("102");
					String[] args3 = {job.getStartDelayOperationEndValue().toString()};
					message.append(" " + Messages.getString("message.job.58", args3));
				} else if(type == OperationConstant.TYPE_STOP_WAIT){
					info.setMessageId("103");
				}
			}
			info.setMessage(message.toString());
		} else {
			info.setMessageId("201");
			String jobType = JobConstant.typeToString(job.getJobType());
			String jobName = job.getJobName();
			String[] args1 = {jobType,jobName,jobId,sessionId};
			message.append(Messages.getString("message.job.56", args1));

			//操作
			if(job.getEndDelayOperation() == YesNoConstant.TYPE_YES){
				int type = job.getEndDelayOperationType();

				String[] args2 = {OperationConstant.typeToString(type)};
				message.append(" " + Messages.getString("message.job.57", args2));

				if(type == OperationConstant.TYPE_STOP_AT_ONCE){
					info.setMessageId("202");
				} else if(type == OperationConstant.TYPE_STOP_SUSPEND){
					info.setMessageId("203");
				} else if(type == OperationConstant.TYPE_STOP_SET_END_VALUE){
					info.setMessageId("204");
				}
			}
			info.setMessage(message.toString());
		}

		if(job.getJobType() == JobConstant.TYPE_JOB){
			//ファシリティID
			info.setFacilityId(job.getFacilityId());
			//スコープ
			info.setScopeText(sessionJob.getScopeText());
		} else {
			//ファシリティID
			info.setFacilityId("");
			//スコープ
			info.setScopeText("");
		}
		//重要度
		if(startDelay) {
			info.setPriority(job.getStartDelayNotifyPriority());
		} else {
			info.setPriority(job.getEndDelayNotifyPriority());
		}
		//発生日時
		info.setGenerationDate(new Date().getTime());

		try {
			// 通知処理
			new NotifyControllerBean().notify(info, job.getNotifyGroupId());
		} catch (Exception e) {
			m_log.warn("delayNotify() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
	}

	/**
	 * 多重度通知
	 * セッションID、ジョブユニットID、ジョブIDからジョブ通知情報を取得し、<BR>
	 * ジョブ通知情報を基に、ログ出力情報作成し、監視管理に通知します。
	 * 
	 * @param sessionId セッションID
	 * @param jobId ジョブID
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.bean.EndStatusConstant
	 * @see com.clustercontrol.bean.JobConstant
	 * @see com.clustercontrol.monitor.message.LogOutputNotifyInfo
	 */
	public void multiplicityNotify(String sessionId, String jobunitId, String jobId, int operationType) throws JobInfoNotFound, InvalidRole {
		m_log.debug("multiplicityNotify() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", type=" + operationType);

		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		JobInfoEntity job = sessionJob.getJobInfoEntity();

		// 通知先の指定において以下の場合は通知しない
		// 1.通知IDがnull、もしくは0文字
		if(job.getNotifyGroupId() == null || job.getNotifyGroupId().length() == 0){
			return;
		}

		//通知する

		//通知情報作成
		OutputBasicInfo info = new OutputBasicInfo();
		//プラグインID
		info.setPluginId(HinemosModuleConstant.JOB);
		//アプリケーション
		info.setApplication(Messages.getString("job.management"));
		//監視項目ID
		info.setMonitorId(sessionId);

		//メッセージID、メッセージ、オリジナルメッセージ
		switch (operationType) {
		case StatusConstant.TYPE_WAIT:
			info.setMessageId("301");
			break;
		case StatusConstant.TYPE_RUNNING:
			info.setMessageId("302");
			break;
		case StatusConstant.TYPE_END:
			info.setMessageId("303");
			break;
		default:
			info.setMessageId("304");
			break;
		}
		info.setMessage(Messages.getString("message.job.86") + "(" + StatusConstant.typeToString(operationType) + ")");

		if(job.getJobType() == JobConstant.TYPE_JOB){
			//ファシリティID
			info.setFacilityId(job.getFacilityId());
			//スコープ
			info.setScopeText(sessionJob.getScopeText());
		} else {
			//ファシリティID
			info.setFacilityId("");
			//スコープ
			info.setScopeText("");
		}
		//重要度
		info.setPriority(job.getMultiplicityNotifyPriority());
		//発生日時
		info.setGenerationDate(new Date().getTime());

		try {
			// 通知処理
			new NotifyControllerBean().notify(info, job.getNotifyGroupId());
		} catch (Exception e) {
			m_log.warn("multiplicityNotify() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
	}
}
