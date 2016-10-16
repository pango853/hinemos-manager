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
import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.bean.YesNoConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.JobSessionDuplicate;
import com.clustercontrol.jobmanagement.bean.DelayNotifyConstant;
import com.clustercontrol.jobmanagement.bean.EndStatusCheckConstant;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobParamTypeConstant;
import com.clustercontrol.jobmanagement.bean.JobSessionRequestMessage;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.jobmanagement.model.JobEndInfoEntity;
import com.clustercontrol.jobmanagement.model.JobEndMstEntity;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobParamInfoEntity;
import com.clustercontrol.jobmanagement.model.JobParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntityPK;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobStartJobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobStartJobMstEntity;
import com.clustercontrol.jobmanagement.util.ParameterUtil;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.notify.bean.NotifyRelationInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.plugin.impl.AsyncWorkerPlugin;
import com.clustercontrol.repository.bean.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 * ジョブの実行用情報を作成するクラスです。
 *
 * @version 1.0.0
 * @since 2.0.0
 */
public class CreateJobSession {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( CreateJobSession.class );

	/** ツリートップのID */
	public static final String TOP_JOBUNIT_ID = "ROOT";
	public static final String TOP_JOB_ID = "TOP";

	/**
	 * ジョブの実行用情報を階層に従い作成します。<BR>
	 *
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param info ログ出力情報
	 * @param triggerInfo 実行契機情報
	 * @return セッションID
	 * @throws FacilityNotFound
	 * @throws JobMasterNotFound
	 * @throws JobInfoNotFound
	 * @throws HinemosUnknown
	 * @throws JobSessionDuplicate
	 * @throws InvalidRole
	 *
	 */
	public static String makeSession(String jobunitId, String jobId, OutputBasicInfo info, JobTriggerInfo triggerInfo)
			throws HinemosUnknown {
		String sessionId = CreateSessionId.create();
		m_log.debug("makeSession() sessionId=" + sessionId);
		
		JobSessionRequestMessage message = new JobSessionRequestMessage(sessionId, jobunitId, jobId, info, triggerInfo);
		AsyncWorkerPlugin.addTask(CreateJobSessionTaskFactory.class.getSimpleName(), message, true);

		return sessionId;
	}
	


	/**
	 * ジョブの実行用情報を作成します。
	 * <p>
	 * <ol>
	 * <li>セッションジョブを作成します。</li>
	 * <li>ジョブリレーション情報を作成します。</li>
	 * <li>ジョブ情報を作成します。</li>
	 * <li>ジョブ待ち条件情報を作成します。</li>
	 * <li>ジョブコマンド情報を作成します。</li>
	 * </ol>
	 *
	 * @param job ファイル転送ジョブのジョブマスタ
	 * @param sessionId セッションID
	 * @return
	 * @throws JobInfoNotFound
	 * @throws FacilityNotFound
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	public static JobSessionJobEntityPK createJobSessionJob(JobMstEntity job, String sessionId, OutputBasicInfo info, boolean first, JobTriggerInfo triggerInfo)
			throws JobInfoNotFound, FacilityNotFound, EntityExistsException, HinemosUnknown, JobMasterNotFound, InvalidRole {

		JpaTransactionManager jtm = new JpaTransactionManager();
		HinemosEntityManager em = jtm.getEntityManager();

		String jobunitId = job.getId().getJobunitId();
		String jobId = job.getId().getJobId();
		JobMstEntity referJob = null;

		//JobSessionEntityを設定
		JobSessionEntity jobSessionEntity = em.find(JobSessionEntity.class, sessionId, ObjectPrivilegeMode.READ);
		//親ジョブを設定
		JobSessionJobEntity parentJobSessionJobEntity = null;
		try {
			parentJobSessionJobEntity = QueryUtil.getJobSessionJobPK(sessionId,
					job.getParentJobunitId(),
					job.getParentJobId());
		} catch (JobInfoNotFound e) {
			parentJobSessionJobEntity = QueryUtil.getJobSessionJobPK(sessionId, TOP_JOBUNIT_ID, TOP_JOB_ID);
		}
		//参照ジョブの場合、UnitIdとjobID以外を参照先のジョブとして設定する
		if(job.getJobType() == JobConstant.TYPE_REFERJOB){
			referJob = job;
			//参照先のジョブマスタを取得する
			job = QueryUtil.getJobMstPK(job.getReferJobUnitId(), job.getReferJobId());
		}

		//JobSessionJobを作成
		JobSessionJobEntity jobSessionJobEntity = new JobSessionJobEntity(
				jobSessionEntity,
				jobunitId,
				jobId);
		// 重複チェック
		jtm.checkEntityExists(JobSessionJobEntity.class, jobSessionJobEntity.getId());
		jobSessionJobEntity.setParentJobunitId(parentJobSessionJobEntity.getId().getJobunitId());
		jobSessionJobEntity.setParentJobId(parentJobSessionJobEntity.getId().getJobId());
		jobSessionJobEntity.setStartDate(null);
		jobSessionJobEntity.setEndDate(null);
		jobSessionJobEntity.setEndStatus(null);
		jobSessionJobEntity.setResult(null);
		jobSessionJobEntity.setEndStausCheckFlg(EndStatusCheckConstant.NO_WAIT_JOB);
		jobSessionJobEntity.setDelayNotifyFlg(DelayNotifyConstant.NONE);
		jobSessionJobEntity.setEndValue(null);

		//実行状態・終了値を設定
		if (first) {
			// 最上位の保留とスキップは無視する。
			jobSessionJobEntity.setStatus(StatusConstant.TYPE_WAIT);
		} else if(job.getSuspend() != null && job.getSuspend() == YesNoConstant.TYPE_YES) {
			//保留
			jobSessionJobEntity.setStatus(StatusConstant.TYPE_RESERVING);
		} else if(job.getSkip() != null && job.getSkip() == YesNoConstant.TYPE_YES) {
			//スキップ
			jobSessionJobEntity.setStatus(StatusConstant.TYPE_SKIP);
		} else {
			//保留・スキップ以外
			jobSessionJobEntity.setStatus(StatusConstant.TYPE_WAIT);
		}

		//JobInfoEntityを作成
		JobInfoEntity jobInfoEntity = new JobInfoEntity(jobSessionJobEntity);
		// 重複チェック
		jtm.checkEntityExists(JobInfoEntity.class, jobInfoEntity.getId());
		jobInfoEntity.setJobName(job.getJobName());
		jobInfoEntity.setDescription(job.getDescription());
		jobInfoEntity.setJobType(job.getJobType());
		jobInfoEntity.setRegDate(job.getRegDate());
		jobInfoEntity.setUpdateDate(job.getUpdateDate());
		jobInfoEntity.setRegUser(job.getRegUser());
		jobInfoEntity.setUpdateUser(job.getUpdateUser());

		//ジョブネット・ジョブ・ファイル転送ジョブの場合
		//待ち条件を設定
		if(job.getJobType() == JobConstant.TYPE_JOBNET ||
				job.getJobType() == JobConstant.TYPE_JOB ||
				job.getJobType() == JobConstant.TYPE_FILEJOB) {
			//参照ジョブの場合、待ち条件のみ参照元を使用する
			JobMstEntity tmp = null;
			if (referJob != null) {
				tmp = referJob;
			} else {
				tmp = job;
			}
			jobInfoEntity.setConditionType(tmp.getConditionType());
			jobInfoEntity.setUnmatchEndFlg(tmp.getUnmatchEndFlg());
			jobInfoEntity.setUnmatchEndStatus(tmp.getUnmatchEndStatus());
			jobInfoEntity.setUnmatchEndValue(tmp.getUnmatchEndValue());
			jobInfoEntity.setSkipEndStatus(job.getSkipEndStatus());
			jobInfoEntity.setSkipEndValue(job.getSkipEndValue());
			jobInfoEntity.setCalendarId(job.getCalendarId());
			jobInfoEntity.setCalendarEndStatus(job.getCalendarEndStatus());
			jobInfoEntity.setCalendarEndValue(job.getCalendarEndValue());
			jobInfoEntity.setStartDelaySession(job.getStartDelaySession());
			jobInfoEntity.setStartDelaySessionValue(job.getStartDelaySessionValue());
			jobInfoEntity.setStartDelayTime(job.getStartDelayTime());
			jobInfoEntity.setStartDelayTimeValue(job.getStartDelayTimeValue());
			jobInfoEntity.setStartDelayConditionType(job.getStartDelayConditionType());
			jobInfoEntity.setStartDelayNotify(job.getStartDelayNotify());
			jobInfoEntity.setStartDelayNotifyPriority(job.getStartDelayNotifyPriority());
			jobInfoEntity.setStartDelayOperation(job.getStartDelayOperation());
			jobInfoEntity.setStartDelayOperationType(job.getStartDelayOperationType());
			jobInfoEntity.setStartDelayOperationEndStatus(job.getStartDelayOperationEndStatus());
			jobInfoEntity.setStartDelayOperationEndValue(job.getStartDelayOperationEndValue());
			jobInfoEntity.setEndDelaySession(job.getEndDelaySession());
			jobInfoEntity.setEndDelaySessionValue(job.getEndDelaySessionValue());
			jobInfoEntity.setEndDelayJob(job.getEndDelayJob());
			jobInfoEntity.setEndDelayJobValue(job.getEndDelayJobValue());
			jobInfoEntity.setEndDelayTime(job.getEndDelayTime());
			jobInfoEntity.setEndDelayTimeValue(job.getEndDelayTimeValue());
			jobInfoEntity.setEndDelayConditionType(job.getEndDelayConditionType());
			jobInfoEntity.setEndDelayNotify(job.getEndDelayNotify());
			jobInfoEntity.setEndDelayNotifyPriority(job.getEndDelayNotifyPriority());
			jobInfoEntity.setEndDelayOperation(job.getEndDelayOperation());
			jobInfoEntity.setEndDelayOperationType(job.getEndDelayOperationType());
			jobInfoEntity.setEndDelayOperationEndStatus(job.getEndDelayOperationEndStatus());
			jobInfoEntity.setEndDelayOperationEndValue(job.getEndDelayOperationEndValue());

			jobInfoEntity.setMultiplicityNotify(job.getMultiplicityNotify());
			jobInfoEntity.setMultiplicityNotifyPriority(job.getMultiplicityNotifyPriority());
			jobInfoEntity.setMultiplicityOperation(job.getMultiplicityOperation());
			jobInfoEntity.setMultiplicityEndValue(job.getMultiplicityEndValue());
		}

		//実行コマンドを設定
		if(job.getJobType() == JobConstant.TYPE_JOB){
			jobInfoEntity.setFacilityId(job.getFacilityId());
			jobInfoEntity.setProcessMode(job.getProcessMode());
			if (triggerInfo.getJobCommand()) {
				jobInfoEntity.setStartCommand(triggerInfo.getJobCommandText());
			} else {
				jobInfoEntity.setStartCommand(job.getStartCommand());
			}
			jobInfoEntity.setStopType(job.getStopType());
			jobInfoEntity.setStopCommand(job.getStopCommand());
			jobInfoEntity.setSpecifyUser(job.getSpecifyUser());
			jobInfoEntity.setEffectiveUser(job.getEffectiveUser());
			jobInfoEntity.setMessageRetryEndFlg(job.getMessageRetryEndFlg());
			jobInfoEntity.setMessageRetryEndValue(job.getMessageRetryEndValue());
			jobInfoEntity.setArgumentJobId(job.getArgumentJobId());
			jobInfoEntity.setArgument(job.getArgument());
			jobInfoEntity.setMessageRetry(job.getMessageRetry());
			jobInfoEntity.setCommandRetryFlg(job.getCommandRetryFlg());
			jobInfoEntity.setCommandRetry(job.getCommandRetry());
		}

		//ファイル転送情報を設定
		if(job.getJobType() == JobConstant.TYPE_FILEJOB){
			jobInfoEntity.setProcessMode(job.getProcessMode());
			jobInfoEntity.setSrcFacilityId(job.getSrcFacilityId());
			jobInfoEntity.setDestFacilityId(job.getDestFacilityId());
			jobInfoEntity.setSrcFile(job.getSrcFile());
			jobInfoEntity.setSrcWorkDir(job.getSrcWorkDir());
			jobInfoEntity.setDestDirectory(job.getDestDirectory());
			jobInfoEntity.setDestWorkDir(job.getDestWorkDir());
			jobInfoEntity.setCompressionFlg(job.getCompressionFlg());
			jobInfoEntity.setCheckFlg(job.getCheckFlg());
			jobInfoEntity.setSpecifyUser(job.getSpecifyUser());
			jobInfoEntity.setEffectiveUser(job.getEffectiveUser());
			jobInfoEntity.setMessageRetry(job.getMessageRetry());
			jobInfoEntity.setMessageRetryEndFlg(job.getMessageRetryEndFlg());
			jobInfoEntity.setMessageRetryEndValue(job.getMessageRetryEndValue());
			jobInfoEntity.setCommandRetryFlg(job.getCommandRetryFlg());
			jobInfoEntity.setCommandRetry(job.getCommandRetry());
		}

		// 待ち条件を設定
		// first:最上位のジョブ以外は待ち条件、制御(suspend,skip,calendar)、開始遅延、終了遅延を設定する。
		if (!first) {
			jobInfoEntity.setSuspend(job.getSuspend());
			jobInfoEntity.setSkip(job.getSkip());
			jobInfoEntity.setCalendar(job.getCalendar());
			jobInfoEntity.setStartDelay(job.getStartDelay());
			jobInfoEntity.setEndDelay(job.getEndDelay());
			//参照ジョブの場合、待ち条件のみ参照元を使用する
			JobMstEntity tmp = null;
			if (referJob != null) {
				tmp = referJob;
			} else {
				tmp = job;
			}
			//待ち条件－時刻
			if (triggerInfo.getJobWaitTime()) {
				jobInfoEntity.setStartTime(null);
			} else {
				jobInfoEntity.setStartTime(tmp.getStartTime());
			}

			//待ち条件－セッション開始時の時間（分）
			if (triggerInfo.getJobWaitMinute()) {
				jobInfoEntity.setStartMinute(null);
			} else {
				jobInfoEntity.setStartMinute(tmp.getStartMinute());
			}

			//JobStartJobInfoEntityを作成
			List<JobStartJobMstEntity> jobStartJobMstEntityList = tmp.getJobStartJobMstEntities();
			if (jobStartJobMstEntityList != null) {
				for(JobStartJobMstEntity jobStartJobMstEntity : jobStartJobMstEntityList) {
					// インスタンス生成
					JobStartJobInfoEntity jobStartJobInfoEntity
					= new JobStartJobInfoEntity(
							jobInfoEntity,
							jobStartJobMstEntity.getId().getTargetJobunitId(),
							jobStartJobMstEntity.getId().getTargetJobId(),
							jobStartJobMstEntity.getId().getTargetJobType(),
							jobStartJobMstEntity.getId().getTargetJobEndValue());
					// 重複チェック
					jtm.checkEntityExists(JobStartJobInfoEntity.class, jobStartJobInfoEntity.getId());
				}
			}
		} else {
			jobInfoEntity.setSuspend(YesNoConstant.TYPE_NO);
			jobInfoEntity.setSkip(YesNoConstant.TYPE_NO);
			jobInfoEntity.setCalendar(YesNoConstant.TYPE_NO);
			jobInfoEntity.setStartDelay(YesNoConstant.TYPE_NO);
			jobInfoEntity.setEndDelay(YesNoConstant.TYPE_NO);
		}

		// JobParamInfoEntityを作成
		// ジョブ変数は最上位のジョブのみ設定する。
		if (first) {
			JobMstEntity jobunit = QueryUtil.getJobMstPK(jobunitId, jobunitId);
			List<JobParamMstEntity> jobParamMstEntityList = jobunit.getJobParamMstEntities();
			if (jobParamMstEntityList != null) {
				for(JobParamMstEntity jobParamMstEntity : jobParamMstEntityList) {
					// インスタンス生成
					JobParamInfoEntity jobParamInfoEntity
					= new JobParamInfoEntity(
							jobInfoEntity,
							jobParamMstEntity.getId().getParamId());
					// 重複チェック
					jtm.checkEntityExists(JobParamInfoEntity.class, jobParamInfoEntity.getId());
					jobParamInfoEntity.setParamType(jobParamMstEntity.getParamType());
					jobParamInfoEntity.setDescription(jobParamMstEntity.getDescription());
					//パラメータ値を取得
					String value = null;
					if(jobParamMstEntity.getParamType() == JobParamTypeConstant.TYPE_SYSTEM_JOB){
						//システム変数（ジョブ）
						// システム変数に格納する情報が、どの機能のものかチェック
						int functionType = ParameterUtil.checkFunctionType(jobParamMstEntity.getId().getParamId());
						String paramId = jobParamMstEntity.getId().getParamId();
						if (functionType == ParameterUtil.TYPE_JOB
								&& SystemParameterConstant.FILENAME.equals(paramId)) {
							// ジョブ管理(FILENAME)の情報を取得する場合
							value = triggerInfo.getFilename();
						} else if (functionType == ParameterUtil.TYPE_JOB
								&& SystemParameterConstant.DIRECTORY.equals(paramId)) {
								// ジョブ管理(DIRECTORY)の情報を取得する場合
								value = triggerInfo.getDirectory();
						} else if (functionType == ParameterUtil.TYPE_JOB) {
							value = ParameterUtil.getJobParameterValue(sessionId, jobParamMstEntity.getId().getParamId());
						} else if (functionType == ParameterUtil.TYPE_MONITOR){
							// 監視管理の情報を取得する場合
							value = ParameterUtil.getParameterValue(jobParamMstEntity.getId().getParamId(), info);
						}
					} else if(jobParamMstEntity.getParamType() == JobParamTypeConstant.TYPE_USER) {
						//ユーザ変数
						value = jobParamMstEntity.getValue();
					}
					jobParamInfoEntity.setValue(value);
				}
			}
		}

		//ファシリティパスを設定(JobSessionJobEntity)
		if(job.getFacilityId() != null){
			//ファシリティID取得
			String facilityId = job.getFacilityId();
			if(job.getJobType() == JobConstant.TYPE_JOB
					&& SystemParameterConstant.isParam(facilityId, SystemParameterConstant.FACILITY_ID)){
				facilityId = ParameterUtil.getSessionParameterValue(sessionId,
						SystemParameterConstant.FACILITY_ID);
			}
			String scopePath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
			jobSessionJobEntity.setScopeText(scopePath);
		} else {
			jobSessionJobEntity.setScopeText(null);
		}

		//JobSessionNodeEntityを作成
		if(job.getJobType() == JobConstant.TYPE_JOB){
			//ファシリティID取得
			String facilityId = job.getFacilityId();
			if(SystemParameterConstant.isParam(facilityId, SystemParameterConstant.FACILITY_ID)){
				facilityId = ParameterUtil.getSessionParameterValue(sessionId,
						SystemParameterConstant.FACILITY_ID);
			}
			ArrayList<String> nodeIdList = new ArrayList<String>();
			if(facilityId != null){
				//ノードのファシリティIDリスト取得
				nodeIdList = new RepositoryControllerBean().getExecTargetFacilityIdList(facilityId, jobSessionJobEntity.getOwnerRoleId());
			}
			if(nodeIdList != null){
				for(String nodeId : nodeIdList){
					//ノード名を取得
					NodeInfo nodeInfo = new RepositoryControllerBean().getNode(nodeId);
					// インスタンス生成
					JobSessionNodeEntity jobSessionNodeEntity = new JobSessionNodeEntity(jobSessionJobEntity, nodeId);
					// 重複チェック
					jtm.checkEntityExists(JobSessionNodeEntity.class, jobSessionNodeEntity.getId());
					jobSessionNodeEntity.setStatus(StatusConstant.TYPE_WAIT);
					jobSessionNodeEntity.setStartDate(null);
					jobSessionNodeEntity.setEndDate(null);
					jobSessionNodeEntity.setEndValue(null);
					jobSessionNodeEntity.setMessage(null);
					jobSessionNodeEntity.setRetryCount(0);
					jobSessionNodeEntity.setResult(null);
					jobSessionNodeEntity.setNodeName(nodeInfo.getFacilityName());
				}
			}
		}

		jobInfoEntity.setBeginPriority(job.getBeginPriority());
		jobInfoEntity.setNormalPriority(job.getNormalPriority());
		jobInfoEntity.setWarnPriority(job.getWarnPriority());
		jobInfoEntity.setAbnormalPriority(job.getAbnormalPriority());
		String infoNotifyGroupId = NotifyGroupIdGenerator.generate(jobInfoEntity);
		jobInfoEntity.setNotifyGroupId(infoNotifyGroupId);

		// 取得したマスタ情報の通知グループIDで、通知関連情報を取得する
		List<NotifyRelationInfo> ct = new NotifyControllerBean().getNotifyRelation(job.getNotifyGroupId());
		// JobNoticeInfo用の通知グループIDで、通知関連テーブルのコピーを作成する
		for (NotifyRelationInfo relation : ct) {
			relation.setNotifyGroupId(infoNotifyGroupId);
		}
		// JobからNotifyRelationInfoは１件のみ登録すればよい。
		new NotifyControllerBean().addNotifyRelation(ct);


		//ファイル転送を行うジョブネットの実行用情報を作成
		// 通知の設定が済んでいる必要があるため、この位置で設定を行う
		if(job.getJobType() == JobConstant.TYPE_FILEJOB){
			if (CreateHulftJob.isHulftMode()) {
				new CreateHulftJob().createHulftDetailJob(jobInfoEntity);
			} else {
				// 参照ジョブの場合も通知マスタ取得のために、参照先のファイル転送ジョブのIDを引数で渡す
				new CreateFileJob().createGetFileListJob(jobInfoEntity, job.getId().getJobId());
			}
		}

		//JobEndInfoEntityの作成
		List<JobEndMstEntity> jobEndMstEntityList = job.getJobEndMstEntities();
		if (jobEndMstEntityList != null) {
			for(JobEndMstEntity jobEndMstEntity : jobEndMstEntityList) {
				// インスタンス生成
				JobEndInfoEntity jobEndInfoEntity
				= new JobEndInfoEntity(jobInfoEntity, jobEndMstEntity.getId().getEndStatus());
				// 重複チェック
				jtm.checkEntityExists(JobEndInfoEntity.class, jobEndInfoEntity.getId());
				jobEndInfoEntity.setEndValue(jobEndMstEntity.getEndValue());
				jobEndInfoEntity.setEndValueFrom(jobEndMstEntity.getEndValueFrom());
				jobEndInfoEntity.setEndValueTo(jobEndMstEntity.getEndValueTo());
			}
		}

		// 子供を再帰的に作成
		List<JobMstEntity> childJobMstEntities
		= em.createNamedQuery("JobMstEntity.findByParentJobunitIdAndJobId", JobMstEntity.class)
		.setParameter("parentJobunitId", job.getId().getJobunitId())
		.setParameter("parentJobId", job.getId().getJobId())
		.getResultList();
		if (childJobMstEntities != null) {
			for(JobMstEntity childJob : childJobMstEntities) {
				createJobSessionJob(childJob, sessionId, info, false, triggerInfo);
			}
		}
		return jobSessionJobEntity.getId();
	}
}
