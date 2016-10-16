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

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.ObjectPrivilegeUtil;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInvalid;
import com.clustercontrol.fault.JobMasterDuplicate;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobEndStatusInfo;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.bean.JobObjectInfo;
import com.clustercontrol.jobmanagement.bean.JobParameterInfo;
import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.model.JobEndMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobStartJobMstEntity;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.notify.bean.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;

/**
 * ジョブツリー情報を登録するクラスです。
 *
 * @version 4.1.0
 * @since 1.0.0
 */
public class ModifyJob {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( ModifyJob.class );

	public Long replaceJobunit(List<JobInfo> oldList, List<JobInfo> newList, String userId)
			throws JobInvalid, JobMasterNotFound, EntityExistsException, HinemosUnknown, JobMasterDuplicate, InvalidRole {
		//ジョブユニットのジョブIDを取得
		String jobunitId = newList.get(0).getJobunitId();

		// ジョブマスタ変更
		long start = System.currentTimeMillis();

		HashSet<JobInfo>delJobs = new HashSet<JobInfo>(oldList);
		HashSet<JobInfo>newJobs = new HashSet<JobInfo>(newList);
		delJobs.removeAll(newJobs);

		long timeJobToDelete = System.currentTimeMillis();
		m_log.info("Find jobs to delete " + (timeJobToDelete - start) + "ms");

		HashSet<JobInfo> addJobs = newJobs;
		addJobs.removeAll(new HashSet<JobInfo>(oldList));

		long timeJobToAdd = System.currentTimeMillis();
		m_log.info("Find jobs to add " + (timeJobToAdd - timeJobToDelete) + "ms");
		m_log.info("oldList=" + oldList.size() + ", newList=" + newList.size() +
				", delJobs=" + delJobs.size() + ", addJobs=" + addJobs.size());

		JpaTransactionManager jtm = new JpaTransactionManager();
		for (JobInfo delJob : delJobs) {
			String jobId = delJob.getId();
			JobMstEntity entity = QueryUtil.getJobMstPK(delJob.getJobunitId(), delJob.getId());
			deleteJob(entity);
			FullJob.removeCache(jobunitId, jobId);
		}

		jtm.flush();

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());

		// ジョブユニットを最初に登録する必要があるため。
		for (JobInfo addJob : addJobs) {
			int type = addJob.getType();
			if (type == JobConstant.TYPE_JOBUNIT) {
				String jobId = addJob.getId();
				String parentJobId = addJob.getParentId();
				if (jobunitId.equals(jobId)) {
					parentJobId = CreateJobSession.TOP_JOB_ID;
				}
				createJobMasterData(addJob, jobunitId, parentJobId, userId, timestamp);
				addJobs.remove(addJob);
				break;
			}
		}
		for (JobInfo addJob : addJobs) {
			String jobId = addJob.getId();
			String parentJobId = addJob.getParentId();
			if (jobunitId.equals(jobId)) {
				parentJobId = CreateJobSession.TOP_JOB_ID;
			}
			createJobMasterData(addJob, jobunitId, parentJobId, userId, timestamp);
		}

		// ジョブユニットの最終更新日時の更新
		String jobId = newList.get(0).getId();
		JobMstEntity entity = QueryUtil.getJobMstPK(jobunitId, jobId);
		entity.setUpdateDate(timestamp);
		m_log.info("Left tasks in replaceJobunit " + (System.currentTimeMillis() - timeJobToAdd) + "ms");
		
		// ジョブユニットの最終更新日時を返す
		return timestamp.getTime();
	}

	/**
	 * ジョブツリー情報を登録します。
	 * <p>
	 * <ol>
	 * <li>全ジョブマスタ情報を取得します。</li>
	 * <li>取得したジョブマスタ情報の数だけ以下の処理を行います</li>
	 *   <ol>
	 *   <li>ジョブリレーションマスタを削除します。</li>
	 *   <li>ジョブコマンドマスタを削除します。</li>
	 *   <li>ジョブファイル転送マスタを削除します。</li>
	 *     <ol>
	 *     <li>ジョブ待ち条件ジョブマスタを削除します。</li>
	 *     <li>ジョブ待ち条件時刻マスタを削除します。</li>
	 *     <li>ジョブ待ち条件マスタを削除します。</li>
	 *     </ol>
	 *   <li>ジョブ通知マスタを削除します。</li>
	 *   <li>ジョブ終了状態マスタを削除します。</li>
	 *   <li>ジョブ変数マスタを削除します。</li>
	 *   <li>ジョブマスタを削除します。</li>
	 *   </ol>
	 *   <li>ジョブツリー情報を基に、ジョブマスタを作成します。</li>
	 * </ol>
	 *
	 * @param item ジョブツリー情報
	 * @param userId ユーザID
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws JobInvalid
	 * @throws JobMasterDuplicate
	 * @throws InvalidRole
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJob#createJobMaster(JobTreeItem, String)
	 */
	public Long registerJobunit(JobTreeItem jobunit, String userId)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, JobMasterDuplicate, InvalidRole {

		//ジョブユニットのジョブIDを取得
		String jobunitId = jobunit.getData().getJobunitId();

		int msec = Calendar.getInstance().get(Calendar.MILLISECOND);
		m_log.debug("registerJobunit() start : " + msec);

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		
		// ジョブマスタ作成
		createJobMaster(jobunit, jobunitId, CreateJobSession.TOP_JOB_ID, userId, timestamp);

		m_log.debug("registerJobunit() End : " + msec);
		
		// ジョブユニットの最終更新日時を返す
		return timestamp.getTime();
	}

	/**
	 * 指定されたジョブユニットを削除する
	 *
	 * @param jobunitId 削除対象ジョブユニットのジョブID
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws JobInvalid
	 *
	 */
	public void deleteJobunit(String jobunitId, String userId) throws HinemosUnknown, JobMasterNotFound, JobInvalid, ObjectPrivilege_InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		// 引数で指定されたジョブユニットIDを持つジョブユニットを取得
		Collection<JobMstEntity> ct =
				em.createNamedQuery("JobMstEntity.findByJobunitId", JobMstEntity.class, ObjectPrivilegeMode.MODIFY)
				.setParameter("jobunitId", jobunitId).getResultList();

		// オブジェクト権限チェック(削除対象のリストが空だった場合)
		if (ct == null || ct.size() == 0) {
			ObjectPrivilege_InvalidRole e = new ObjectPrivilege_InvalidRole(
					"targetClass = " + JobMstEntity.class.getSimpleName());
			m_log.info("deleteJobunit() : object privilege invalid. jobunitId = " + jobunitId);

			throw e;
		}

		for (JobMstEntity jobMstEntity : ct) {
			deleteJob(jobMstEntity);
		}

		// オブジェクト権限情報を削除する。
		ObjectPrivilegeUtil.deleteObjectPrivilege(HinemosModuleConstant.JOB, jobunitId);
	}

	private void deleteJob(JobMstEntity jobMstEntity) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		if (jobMstEntity != null) {
			String jobunitId = jobMstEntity.getId().getJobunitId();
			String jobId = jobMstEntity.getId().getJobId();
			// ジョブユニットに紐づく通知を削除
			// "JOB_MST-junit-job-0"
			em.createNamedQuery("NotifyRelationInfoEntity.deleteByNotifyGroupId", Integer.class)
			.setParameter("notifyGroupId", HinemosModuleConstant.JOB_MST +
					"-" + jobunitId + "-" + jobId + "-0")
					.executeUpdate();
			em.remove(jobMstEntity);
		}
	}

	/**
	 * ジョブマスタを作成します。 再帰呼び出しを行います。
	 * <p>
	 * <ol>
	 * <li>ジョブツリー情報を基に、ジョブマスタを作成します。</li>
	 * <li>ジョブツリー情報の配下のジョブツリー情報を取得します。</li>
	 * <li>取得した配下のジョブツリー情報の数、以下の処理を行います。</li>
	 * <ol>
	 * <li>ジョブツリー情報を基に、ジョブマスタを作成します。</li>
	 * </ol>
	 * </ol>
	 *
	 * @param item
	 *            ジョブツリー情報
	 * @param user
	 *            ユーザID
	 * @throws HinemosUnknown
	 * @throws JobMasterDuplicate
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJob#createJobMasterData(JobTreeItem,
	 *      String)
	 */
	private void createJobMaster(JobTreeItem item, String jobunitId, String parentId, String user, Timestamp updateDate)
			throws HinemosUnknown, JobMasterDuplicate, JobMasterNotFound, InvalidRole {

		//ジョブマスタデータ作成
		createJobMasterData(item.getData(), jobunitId, parentId, user, updateDate);

		//子JobTreeItemを取得
		for (JobTreeItem child : item.getChildrenArray()) {
			//ジョブマスタ作成
			createJobMaster(child, jobunitId, item.getData().getId(), user, updateDate);
		}
	}

	/**
	 * ジョブマスタを作成します。
	 * <p>
	 * <ol>
	 * <li>ジョブツリー情報を基に、ジョブマスタを作成します。</li>
	 * <li>ジョブツリー情報を基に、ジョブリレーションマスタを作成します。</li>
	 * <li>ジョブツリー情報を基に、ジョブ待ち条件マスタを作成します。</li>
	 * <li>ジョブツリー情報を基に、ジョブ待ち条件ジョブマスタを作成します。</li>
	 * <li>ジョブツリー情報を基に、ジョブ待ち条件時刻マスタを作成します。</li>
	 * <li>ジョブツリー情報を基に、ジョブコマンドマスタを作成します。</li>
	 * <li>ジョブツリー情報を基に、ジョブファイル転送マスタを作成します。</li>
	 * <li>ジョブツリー情報を基に、ジョブ通知マスタを作成します。</li>
	 * <li>ジョブツリー情報を基に、ジョブ終了状態マスタを作成します。</li>
	 * <li>ジョブツリー情報を基に、ジョブ変数マスタを作成します。</li>
	 * </ol>
	 *
	 * @param item ジョブツリー情報
	 * @param user ユーザID
	 * @throws HinemosUnknown
	 * @throws JobMasterDuplicate
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	private void createJobMasterData(JobInfo info, String jobunitId, String parentId, String user, Timestamp updateDate)
			throws HinemosUnknown, JobMasterDuplicate, JobMasterNotFound, InvalidRole {

		m_log.debug("createJobMasterData");
		JpaTransactionManager jtm = new JpaTransactionManager();

		// ジョブユニット単位で再帰的に登録するため、親となるジョブユニットIDは変わらない
		// ただし、TOPの親ジョブユニットIDはTOPとする
		String parentJobunitId = null;
		if (CreateJobSession.TOP_JOB_ID.equals(parentId)) {
			parentJobunitId = CreateJobSession.TOP_JOBUNIT_ID;
		} else {
			parentJobunitId = jobunitId;
		}

		//判定対象ジョブのジョブユニットも同じ
		String waitJobunitId = jobunitId;

		if(info.getCreateUser() == null) {
			info.setCreateUser(user);
		}

		if(info.getCreateTime() == null) {
			info.setCreateTime(updateDate.getTime());
		}

		try {
			//ジョブ作成
			// インスタンス生成
			JobMstEntity jobMst = new JobMstEntity(jobunitId, info.getId(), info.getType());
			// 重複チェック
			jtm.checkEntityExists(JobMstEntity.class, jobMst.getId());
			jobMst.setDescription(info.getDescription());
			jobMst.setJobName(info.getName());
			jobMst.setRegDate(new Timestamp(info.getCreateTime()));
			jobMst.setRegUser(info.getCreateUser());
			jobMst.setUpdateDate(updateDate);
			jobMst.setUpdateUser(user);
			jobMst.setParentJobunitId(parentJobunitId);
			jobMst.setParentJobId(parentId);
			if(info.getType() == JobConstant.TYPE_JOBUNIT){
				jobMst.setOwnerRoleId(info.getOwnerRoleId());
			} else {
				QueryUtil.getJobMstPK(jobunitId, jobunitId);
			}

			//待ち条件作成
			if(info.getWaitRule() != null){
				m_log.debug("info.getWaitRule = " + info.getWaitRule());
				JobWaitRuleInfo waitRule = info.getWaitRule();
				Timestamp startDelayTime = null;
				Timestamp endDelayTime = null;
				if (waitRule.getStart_delay_time_value() != null) {
					startDelayTime = new Timestamp(waitRule.getStart_delay_time_value());
				}
				if (waitRule.getEnd_delay_time_value() != null) {
					endDelayTime = new Timestamp(waitRule.getEnd_delay_time_value());
				}
				jobMst.setConditionType(waitRule.getCondition());
				jobMst.setSuspend(waitRule.getSuspend());
				jobMst.setSkip(waitRule.getSkip());
				jobMst.setSkipEndStatus(waitRule.getSkipEndStatus());
				jobMst.setSkipEndValue(waitRule.getSkipEndValue());
				jobMst.setUnmatchEndFlg(waitRule.getEndCondition());
				jobMst.setUnmatchEndStatus(waitRule.getEndStatus());
				jobMst.setUnmatchEndValue(waitRule.getEndValue());
				jobMst.setCalendar(waitRule.getCalendar());
				jobMst.setCalendarId(waitRule.getCalendarId());
				jobMst.setCalendarEndStatus(waitRule.getCalendarEndStatus());
				jobMst.setCalendarEndValue(waitRule.getCalendarEndValue());
				jobMst.setStartDelay(waitRule.getStart_delay());
				jobMst.setStartDelaySession(waitRule.getStart_delay_session());
				jobMst.setStartDelaySessionValue(waitRule.getStart_delay_session_value());
				jobMst.setStartDelayTime(waitRule.getStart_delay_time());
				jobMst.setStartDelayTimeValue(startDelayTime);
				jobMst.setStartDelayConditionType(waitRule.getStart_delay_condition_type());
				jobMst.setStartDelayNotify(waitRule.getStart_delay_notify());
				jobMst.setStartDelayNotifyPriority(waitRule.getStart_delay_notify_priority());
				jobMst.setStartDelayOperation(waitRule.getStart_delay_operation());
				jobMst.setStartDelayOperationType(waitRule.getStart_delay_operation_type());
				jobMst.setStartDelayOperationEndStatus(waitRule.getStart_delay_operation_end_status());
				jobMst.setStartDelayOperationEndValue(waitRule.getStart_delay_operation_end_value());
				jobMst.setEndDelay(waitRule.getEnd_delay());
				jobMst.setEndDelaySession(waitRule.getEnd_delay_session());
				jobMst.setEndDelaySessionValue(waitRule.getEnd_delay_session_value());
				jobMst.setEndDelayJob(waitRule.getEnd_delay_job());
				jobMst.setEndDelayJobValue(waitRule.getEnd_delay_job_value());
				jobMst.setEndDelayTime(waitRule.getEnd_delay_time());
				jobMst.setEndDelayTimeValue(endDelayTime);
				jobMst.setEndDelayConditionType(waitRule.getEnd_delay_condition_type());
				jobMst.setEndDelayNotify(waitRule.getEnd_delay_notify());
				jobMst.setEndDelayNotifyPriority(waitRule.getEnd_delay_notify_priority());
				jobMst.setEndDelayOperation(waitRule.getEnd_delay_operation());
				jobMst.setEndDelayOperationType(waitRule.getEnd_delay_operation_type());
				jobMst.setEndDelayOperationEndStatus(waitRule.getEnd_delay_operation_end_status());
				jobMst.setEndDelayOperationEndValue(waitRule.getEnd_delay_operation_end_value());
				jobMst.setMultiplicityNotify(waitRule.getMultiplicityNotify());
				jobMst.setMultiplicityNotifyPriority(waitRule.getMultiplicityNotifyPriority());
				jobMst.setMultiplicityOperation(waitRule.getMultiplicityOperation());
				jobMst.setMultiplicityEndValue(waitRule.getMultiplicityEndValue());
				if(waitRule.getObject() != null){
					for(JobObjectInfo objectInfo : waitRule.getObject()) {
						if(objectInfo != null){
							if(objectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_END_STATUS){
								// インスタンス生成
								JobStartJobMstEntity jobStartJobMstEntity
								= new JobStartJobMstEntity(
										jobMst,
										waitJobunitId,
										objectInfo.getJobId(),
										JudgmentObjectConstant.TYPE_JOB_END_STATUS,
										objectInfo.getValue());
								// 重複チェック
								jtm.checkEntityExists(JobStartJobMstEntity.class, jobStartJobMstEntity.getId());
							}
							else if(objectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_END_VALUE){
								// インスタンス生成
								JobStartJobMstEntity jobStartJobMstEntity
								= new JobStartJobMstEntity(
										jobMst,
										waitJobunitId,
										objectInfo.getJobId(),
										JudgmentObjectConstant.TYPE_JOB_END_VALUE,
										objectInfo.getValue());
								// 重複チェック
								jtm.checkEntityExists(JobStartJobMstEntity.class, jobStartJobMstEntity.getId());
							}
							else if(objectInfo.getType() == JudgmentObjectConstant.TYPE_TIME) {
								jobMst.setStartTime(objectInfo.getTime()==null?null:new Timestamp(objectInfo.getTime()));
							}
							else if(objectInfo.getType() == JudgmentObjectConstant.TYPE_START_MINUTE) {
								m_log.debug("objectInfo.getType = " + objectInfo.getType());
								m_log.debug("objectInfo.getStartMinute = " + objectInfo.getStartMinute());
								jobMst.setStartMinute(objectInfo.getStartMinute());
							}
						}
					}
				}
			}

			//実行コマンド作成
			if(info.getCommand() != null){
				jobMst.setFacilityId(info.getCommand().getFacilityID());
				jobMst.setProcessMode(info.getCommand().getProcessingMethod());
				jobMst.setStartCommand(info.getCommand().getStartCommand());
				jobMst.setStopType(info.getCommand().getStopType());
				jobMst.setStopCommand(info.getCommand().getStopCommand());
				jobMst.setSpecifyUser(info.getCommand().getSpecifyUser());
				jobMst.setEffectiveUser(info.getCommand().getUser());
				jobMst.setMessageRetry(info.getCommand().getMessageRetry());
				jobMst.setMessageRetryEndFlg(info.getCommand().getMessageRetryEndFlg());
				jobMst.setMessageRetryEndValue(info.getCommand().getMessageRetryEndValue());
				jobMst.setArgumentJobId("");
				jobMst.setArgument("");
				jobMst.setCommandRetryFlg(info.getCommand().getCommandRetryFlg());
				jobMst.setCommandRetry(info.getCommand().getCommandRetry());
			}

			//ファイル転送作成
			if(info.getFile() != null){
				jobMst.setProcessMode(info.getFile().getProcessingMethod());
				jobMst.setSrcFacilityId(info.getFile().getSrcFacilityID());
				jobMst.setDestFacilityId(info.getFile().getDestFacilityID());
				jobMst.setSrcFile(info.getFile().getSrcFile());
				jobMst.setSrcWorkDir(info.getFile().getSrcWorkDir());
				jobMst.setDestDirectory(info.getFile().getDestDirectory());
				jobMst.setDestWorkDir(info.getFile().getDestWorkDir());
				jobMst.setCompressionFlg(info.getFile().getCompressionFlg());
				jobMst.setCheckFlg(info.getFile().getCheckFlg());
				jobMst.setSpecifyUser(info.getFile().getSpecifyUser());
				jobMst.setEffectiveUser(info.getFile().getUser());
				jobMst.setMessageRetry(info.getFile().getMessageRetry());
				jobMst.setMessageRetryEndFlg(info.getFile().getMessageRetryEndFlg());
				jobMst.setMessageRetryEndValue(info.getFile().getMessageRetryEndValue());
				jobMst.setCommandRetryFlg(info.getFile().getCommandRetryFlg());
				jobMst.setCommandRetry(info.getFile().getCommandRetry());
			}

			//通知メッセージを取得
			jobMst.setBeginPriority(info.getBeginPriority());
			jobMst.setNormalPriority(info.getNormalPriority());
			jobMst.setWarnPriority(info.getWarnPriority());
			jobMst.setAbnormalPriority(info.getAbnormalPriority());

			//notifyGroupIdを更新
			String notifyGroupId = NotifyGroupIdGenerator.generate(info);
			jobMst.setNotifyGroupId(notifyGroupId);

			if (info.getNotifyRelationInfos() != null) {
				for (NotifyRelationInfo notifyRelationInfo : info.getNotifyRelationInfos()) {
					notifyRelationInfo.setNotifyGroupId(notifyGroupId);
				}
			}
			new NotifyControllerBean().addNotifyRelation(info.getNotifyRelationInfos());

			//終了状態を取得
			if(info.getEndStatus() != null){
				for (JobEndStatusInfo endInfo : info.getEndStatus()) {
					if(endInfo != null){
						// インスタンス生成
						JobEndMstEntity jobEndMstEntity = new JobEndMstEntity(jobMst, endInfo.getType());
						// 重複チェック
						jtm.checkEntityExists(JobEndMstEntity.class, jobEndMstEntity.getId());
						jobEndMstEntity.setEndValue(endInfo.getValue());
						jobEndMstEntity.setEndValueFrom(endInfo.getStartRangeValue());
						jobEndMstEntity.setEndValueTo(endInfo.getEndRangeValue());
					}
				}
			}

			//パラメータを取得
			if(info.getParam() != null){
				for(JobParameterInfo paramInfo : info.getParam()){
					if(paramInfo != null){
						// インスタンス生成
						JobParamMstEntity jobParamMstEntity = new JobParamMstEntity(jobMst, paramInfo.getParamId());
						// 重複チェック
						jtm.checkEntityExists(JobParamMstEntity.class, jobParamMstEntity.getId());
						jobParamMstEntity.setDescription(paramInfo.getDescription());
						jobParamMstEntity.setParamType(paramInfo.getType());
						jobParamMstEntity.setValue(paramInfo.getValue());
					}
				}
			}

			//参照ジョブを設定
			if(info.getType() == JobConstant.TYPE_REFERJOB){
				if(info.getReferJobUnitId() != null){
					jobMst.setReferJobUnitId(info.getReferJobUnitId());
				}
				if(info.getReferJobId() != null){
					jobMst.setReferJobId(info.getReferJobId());
				}
			}

		} catch (HinemosUnknown e) {
			throw e;
		} catch (EntityExistsException e) {
			JobMasterDuplicate jmd = new JobMasterDuplicate(e.getMessage(), e);
			m_log.info("createJobMasterData() : "
					+ jmd.getClass().getSimpleName() + ", " + jmd.getMessage());
			throw jmd;
		}
	}
}
