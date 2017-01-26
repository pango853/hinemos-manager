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

package com.clustercontrol.notify.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.jobmanagement.util.JobValidator;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.notify.bean.ExecFacilityConstant;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.entity.NotifyJobInfoData;
import com.clustercontrol.notify.model.NotifyJobInfoEntity;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * ジョブを呼出すクラス<BR>
 *
 * @version 3.0.0
 * @since 3.0.0
 */
public class RunJob implements DependDbNotifier {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( RunJob.class );

	/** 実行失敗通知用 */
	private NotifyJobInfoData m_jobInfo;

	/**
	 * ジョブ管理機能の指定されたジョブを呼出します。
	 * <p>含まれているログ出力情報を基にジョブを呼出します。<BR>
	 * ジョブの呼出に失敗した場合は、ログ出力情報の呼出失敗時の重要度で、監視管理機能のイベントへ通知します。
	 * 
	 * @param outputInfo　通知・抑制情報
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.JobTriggerInfo
	 * @see com.clustercontrol.monitor.factory.OutputEventLog#insertEventLog(LogOutputInfo, int)
	 */
	@Override
	public synchronized void notify(NotifyRequestMessage message) {
		if(m_log.isDebugEnabled()){
			m_log.debug("notify() " + message);
		}

		exectuteJob(message.getOutputInfo(), message.getNotifyId());
	}

	/**
	 * ジョブ管理機能の指定されたジョブを呼出します。
	 * <p>含まれているログ出力情報を基にジョブを呼出します。<BR>
	 * ジョブの呼出に失敗した場合は、ログ出力情報の呼出失敗時の重要度で、監視管理機能のイベントへ通知します。
	 * 
	 * @param outputInfo　通知・抑制情報
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.JobTriggerInfo
	 * @see com.clustercontrol.monitor.factory.OutputEventLog#insertEventLog(LogOutputInfo, int)
	 */
	private void exectuteJob(
			OutputBasicInfo outputInfo,
			String notifyId) {

		if(m_log.isDebugEnabled()){
			m_log.debug("notify() " + outputInfo);
		}

		/*
		 * 実行
		 */
		NotifyJobInfoEntity jobInfo = null;
		try {
			jobInfo = QueryUtil.getNotifyJobInfoPK(notifyId);

			
			
			// 実行対象のジョブが存在するかのチェック(存在しない場合はInternalイベントを出力して終了)
			try{
				JobValidator.validateJobId(getJobunitId(jobInfo, outputInfo.getPriority()), getJobId(jobInfo, outputInfo.getPriority()),false);
			} catch (InvalidRole | InvalidSetting e) {
				// 参照権限がない場合
				// 実行対象のジョブが存在しない場合の処理
				int outputPriority = outputInfo.getPriority(); 
				int failurePriority = 0;
				if (outputPriority == PriorityConstant.TYPE_INFO) {
					failurePriority = jobInfo.getInfoJobFailurePriority();
				} else if (outputPriority == PriorityConstant.TYPE_WARNING) {
					failurePriority = jobInfo.getWarnJobFailurePriority();
				} else if (outputPriority == PriorityConstant.TYPE_CRITICAL) {
					failurePriority = jobInfo.getCriticalJobFailurePriority();
				} else if (outputPriority == PriorityConstant.TYPE_UNKNOWN) {
					failurePriority = jobInfo.getUnknownJobFailurePriority();
				} else {
					m_log.warn("unknown priority " + outputPriority);
				}
				
				AplLogger apllog = new AplLogger("NOTIFY", "notify");
				String[] args = { notifyId, outputInfo.getMonitorId(), getJobunitId(jobInfo, outputInfo.getPriority()), getJobId(jobInfo, outputInfo.getPriority()) };
				apllog.put("SYS", "008", args, null, failurePriority);
				return;
			}


			// 通知設定が「固定スコープ」となっていた場合は、ジョブに渡すファシリティIDを変更する
			if(jobInfo.getJobExecFacilityFlg() == ExecFacilityConstant.TYPE_FIX) {
				outputInfo.setFacilityId(jobInfo.getJobExecFacilityId());
			}

			// ジョブの実行契機を作成
			JobTriggerInfo triggerInfo = new JobTriggerInfo();
			triggerInfo.setTrigger_type(JobTriggerTypeConstant.TYPE_MONITOR);
			triggerInfo.setTrigger_info(outputInfo.getMonitorId()+"-"+outputInfo.getPluginId()); // 「監視項目ID_プラグインID」形式で格納

			// ジョブ実行
			new JobControllerBean().runJob(
					getJobunitId(jobInfo, outputInfo.getPriority()),
					getJobId(jobInfo, outputInfo.getPriority()), outputInfo,
					triggerInfo);
		}
		catch (Exception e) {
			if (!(e instanceof InvalidSetting
					|| e instanceof FacilityNotFound
					|| e instanceof HinemosUnknown
					|| e instanceof JobInfoNotFound
					|| e instanceof JobMasterNotFound)) {
				m_log.warn("exectuteJob() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
			if(jobInfo != null){
				m_jobInfo = new NotifyJobInfoData(
						jobInfo.getId().getNotifyId(),
						outputInfo.getPriority(),
						getJobFailurePriority(jobInfo, outputInfo.getPriority()),
						getJobunitId(jobInfo, outputInfo.getPriority()),
						getJobId(jobInfo, outputInfo.getPriority()),
						getJobRun(jobInfo, outputInfo.getPriority()),
						jobInfo.getJobExecFacilityFlg(),
						jobInfo.getJobExecFacilityId());
			}
			internalErrorNotify(notifyId, null, e.getMessage() + " : " + m_jobInfo);
		}
	}

	private Integer getJobRun(NotifyJobInfoEntity jobInfo, int priority) {
		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			return jobInfo.getInfoJobRun();
		case PriorityConstant.TYPE_WARNING:
			return jobInfo.getWarnJobRun();
		case PriorityConstant.TYPE_CRITICAL:
			return jobInfo.getCriticalJobRun();
		case PriorityConstant.TYPE_UNKNOWN:
			return jobInfo.getUnknownJobRun();

		default:
			break;
		}
		return null;
	}

	private String getJobId(NotifyJobInfoEntity jobInfo, int priority) {
		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			return jobInfo.getInfoJobId();
		case PriorityConstant.TYPE_WARNING:
			return jobInfo.getWarnJobId();
		case PriorityConstant.TYPE_CRITICAL:
			return jobInfo.getCriticalJobId();
		case PriorityConstant.TYPE_UNKNOWN:
			return jobInfo.getUnknownJobId();

		default:
			break;
		}
		return null;
	}

	private String getJobunitId(NotifyJobInfoEntity jobInfo, int priority) {
		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			return jobInfo.getInfoJobunitId();
		case PriorityConstant.TYPE_WARNING:
			return jobInfo.getWarnJobunitId();
		case PriorityConstant.TYPE_CRITICAL:
			return jobInfo.getCriticalJobunitId();
		case PriorityConstant.TYPE_UNKNOWN:
			return jobInfo.getUnknownJobunitId();

		default:
			break;
		}
		return null;
	}

	private Integer getJobFailurePriority(NotifyJobInfoEntity jobInfo,
			int priority) {
		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			return jobInfo.getInfoJobFailurePriority();
		case PriorityConstant.TYPE_WARNING:
			return jobInfo.getWarnJobFailurePriority();
		case PriorityConstant.TYPE_CRITICAL:
			return jobInfo.getCriticalJobFailurePriority();
		case PriorityConstant.TYPE_UNKNOWN:
			return jobInfo.getUnknownJobFailurePriority();

		default:
			break;
		}
		return null;
	}

	/**
	 * 通知失敗時の内部エラー通知を定義します
	 */
	@Override
	public void internalErrorNotify(String notifyId, String msgID, String detailMsg) {
		// FIXME
		//ジョブ失敗時の重要度を設定
		OutputBasicInfo outputInfo = new OutputBasicInfo();
		if(m_jobInfo != null){
			outputInfo.setPriority(m_jobInfo.getJobFailurePriority());
			String[] args1 = { m_jobInfo.getJobId() };
			outputInfo.setMessage(Messages.getString("message.monitor.41", args1));
		} else {
			outputInfo.setPriority(PriorityConstant.TYPE_CRITICAL);
			String[] args1 = { "unknown job id." };
			outputInfo.setMessage(Messages.getString("message.monitor.41", args1));
		}
		outputInfo.setMessageId("200");	// FIXME メッセージIDを精査する
		outputInfo.setMessageOrg(detailMsg);

		new OutputEvent().insertEventLog(outputInfo, ConfirmConstant.TYPE_UNCONFIRMED);

		m_jobInfo = null;
	}
}

