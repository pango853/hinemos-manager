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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.jobmanagement.bean.JobCommandInfo;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobEndStatusInfo;
import com.clustercontrol.jobmanagement.bean.JobFileInfo;
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
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 * ジョブ情報を検索するクラスです。
 *
 * @version 4.1.0
 * @since 1.0.0
 */
public class FullJob {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( FullJob.class );

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(FullJob.class.getName());
		
		init();
	}
	
	public static void init() {
		try {
			_lock.writeLock();
			
			Map<String, Map<String, JobInfo>> jobInfoCache = getJobInfoCache();
			if (jobInfoCache == null) {	// not null if clustered
				storeJobInfoCache(new HashMap<String, Map<String, JobInfo>>());
			}
		} finally {
			_lock.writeUnlock();
		}
		
		try {
			_lock.writeLock();
			
			Map<String, Map<String, JobMstEntity>> jobMstCache = getJobMstCache();
			if (jobMstCache == null) {	// not null if clustered
				storeJobMstCache(new HashMap<String, Map<String,JobMstEntity>>());
			}
		} finally {
			_lock.writeUnlock();
		}
	}
	
	/**
	 * 高速化のためのキャッシュ
	 * <jobunitId, <jobId, jobInfo>>
	 */
	@SuppressWarnings("unchecked")
	private static HashMap<String, Map<String, JobInfo>> getJobInfoCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_JOB_INFO);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_JOB_INFO + " : " + cache);
		return cache == null ? null : (HashMap<String, Map<String, JobInfo>>)cache;
	}
	
	private static void storeJobInfoCache(HashMap<String, Map<String, JobInfo>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_JOB_INFO + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_JOB_INFO, newCache);
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, Map<String, JobMstEntity>> getJobMstCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_JOB_MST);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_JOB_MST + " : " + cache);
		return cache == null ? null : (HashMap<String, Map<String, JobMstEntity>>)cache;
	}
	
	private static void storeJobMstCache(HashMap<String, Map<String, JobMstEntity>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_JOB_MST + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_JOB_MST, newCache);
	}

	// deleteJobunitから呼ばれる
	public static void removeCache(String jobunitId) {
		m_log.debug("removeCache " + jobunitId);
		
		try {
			_lock.writeLock();
			
			HashMap<String, Map<String,JobInfo>> jobInfoCache = getJobInfoCache();
			if (jobInfoCache.remove(jobunitId) != null) {
				storeJobInfoCache(jobInfoCache);
			}
			
			HashMap<String, Map<String,JobMstEntity>> jobMstCache = getJobMstCache();
			if (jobMstCache.remove(jobunitId) != null) {
				storeJobMstCache(jobMstCache);
			}
		} finally {
			_lock.writeUnlock();
		}
	}

	// replaceJobunitから呼ばれる。
	public static void removeCache(String jobunitId, String jobId) {
		m_log.debug("removeCache " + jobunitId + ", " + jobId);
		
		try {
			_lock.writeLock();
			
			HashMap<String, Map<String,JobInfo>> jobInfoCache = getJobInfoCache();
			Map<String, JobInfo> jobInfoUnitCache = jobInfoCache.get(jobunitId);
			if (jobInfoUnitCache != null) {
				if (jobInfoUnitCache.remove(jobId) != null) {
					storeJobInfoCache(jobInfoCache);
				}
			}
			
			HashMap<String, Map<String,JobMstEntity>> jobMstCache = getJobMstCache();
			Map<String, JobMstEntity> jobMstUnitCache = jobMstCache.get(jobunitId);
			if (jobMstUnitCache != null) {
				if (jobMstUnitCache.remove(jobId) != null) {
					storeJobMstCache(jobMstCache);
				}
			}
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * ジョブ情報{@link com.clustercontrol.jobmanagement.bean.JobInfo}を作成します。<BR>
	 * ジョブマスタを基に、ジョブ情報を作成します。
	 *
	 * @param job ジョブマスタ
	 * @param treeOnly treeOnly true=ジョブ情報を含まない, false=ジョブ情報含む
	 * @return ジョブ情報
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws UserNotFound
	 * @throws InvalidRole
	 */
	public static JobInfo getJobFull(JobInfo jobInfo) throws HinemosUnknown, JobMasterNotFound, UserNotFound, InvalidRole {
		m_log.debug("createJobData() : " + jobInfo.getJobunitId() + ", " + jobInfo.getId() + "," + jobInfo.isPropertyFull());
		if (jobInfo.isPropertyFull()) {
			return jobInfo;
		}

		String jobunitId = jobInfo.getJobunitId();
		String jobId = jobInfo.getId();
		
		
		try {
			_lock.readLock();
			
			Map<String, Map<String,JobInfo>> jobInfoCache = getJobInfoCache();
			Map<String, JobInfo> jobInfoUnitCache = jobInfoCache.get(jobunitId);
			if (jobInfoUnitCache != null) {
				JobInfo ret = jobInfoUnitCache.get(jobId);
				if (ret != null) {
					m_log.debug("cache hit " + jobunitId + "," + jobId + ", hit=" + jobInfoUnitCache.size());
					return ret;
				}
			}
		} finally {
			_lock.readUnlock();
		}
		
		try {
			_lock.writeLock();
			
			HashMap<String, Map<String,JobInfo>> jobInfoCache = getJobInfoCache();
			Map<String, JobInfo> jobInfoUnitCache = new HashMap<String, JobInfo>();
			jobInfoCache.put(jobunitId, jobInfoUnitCache);
			
			loadJobMstEntityFromDb(jobunitId);
			
			m_log.debug("createJobData() : " + jobunitId + ", " + jobId);
			JobMstEntity jobMstEntity = getJobMstEntityFromLocal(jobunitId, jobId);
			if (jobMstEntity == null) {
				jobMstEntity = QueryUtil.getJobMstPK(jobunitId, jobId);
			}

			jobInfo.setDescription(jobMstEntity.getDescription());
			jobInfo.setOwnerRoleId(jobMstEntity.getOwnerRoleId());

			if (jobMstEntity.getRegDate() != null) {
				jobInfo.setCreateTime(jobMstEntity.getRegDate().getTime());
			}
			if (jobMstEntity.getUpdateDate() != null) {
				jobInfo.setUpdateTime(jobMstEntity.getUpdateDate().getTime());
			}
			jobInfo.setCreateUser(jobMstEntity.getRegUser());
			jobInfo.setUpdateUser(jobMstEntity.getUpdateUser());

			setJobWaitRule(jobInfo, jobMstEntity);

			switch (jobMstEntity.getJobType()) {
			case JobConstant.TYPE_JOB:
				setJobCommand(jobInfo, jobMstEntity);
				break;
			case JobConstant.TYPE_FILEJOB:
				setJobFile(jobInfo, jobMstEntity);
				break;
			case JobConstant.TYPE_JOBUNIT:
				setJobParam(jobInfo, jobMstEntity);
				break;
			case JobConstant.TYPE_REFERJOB:
				jobInfo.setReferJobUnitId(jobMstEntity.getReferJobUnitId());
				jobInfo.setReferJobId(jobMstEntity.getReferJobId());
				break;
			default:
				break;
			}
			if (jobInfo.getType() != JobConstant.TYPE_REFERJOB) {
				setJobNotifications(jobInfo, jobMstEntity);
				setJobEndStatus(jobInfo, jobMstEntity);
			}

			jobInfo.setPropertyFull(true);
			
			jobInfoUnitCache.put(jobId, jobInfo);
			jobInfoCache.put(jobunitId, jobInfoUnitCache);
			
			storeJobInfoCache(jobInfoCache);
		} finally {
			_lock.writeUnlock();
		}
		
		return jobInfo;
	}

	/**
	 * ローカルからジョブの情報を読み込む。
	 * @param jobunitId
	 * @param jobId
	 * @return
	 */
	private static JobMstEntity getJobMstEntityFromLocal(String jobunitId,
			String jobId) {
		Map<String, Map<String, JobMstEntity>> jobMstCache = getJobMstCache();
		Map<String, JobMstEntity> jobMstUnitCache = jobMstCache.get(jobunitId);
		if (jobMstUnitCache == null) {
			return null;
		}
		return jobMstUnitCache.get(jobId);
	}

	/**
	 * JobUnitに属するJobのデータをDBから読み込む。
	 * @param jobunitId
	 */
	private static void loadJobMstEntityFromDb(String jobunitId) {
		HashMap<String, Map<String, JobMstEntity>> jobMstCache = getJobMstCache();
		Map<String, JobMstEntity> jobMstUnitCache = jobMstCache.get(jobunitId);
		if (jobMstUnitCache != null) {
			return;
		}
		
		List<JobMstEntity> jobMstEntityList = QueryUtil.getJobMstEnityFindByJobunitId(jobunitId);
		jobMstUnitCache = new HashMap<String, JobMstEntity>();
		for (JobMstEntity jobMstEntity : jobMstEntityList) {
			jobMstUnitCache.put(jobMstEntity.getId().getJobId(), jobMstEntity);
		}
		jobMstCache.put(jobunitId, jobMstUnitCache);
		
		storeJobMstCache(jobMstCache);
	}

	/**
	 * jobMstEntityの情報に基づき、Jobパラメータ関連の情報をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 */
	private static void setJobParam(JobInfo jobInfo, JobMstEntity jobMstEntity) {
		ArrayList<JobParameterInfo> paramList = new ArrayList<JobParameterInfo>();
		Collection<JobParamMstEntity> params = jobMstEntity.getJobParamMstEntities();
		if(params != null){
			for (JobParamMstEntity param : params) {
				JobParameterInfo paramInfo = new JobParameterInfo();
				paramInfo.setParamId(param.getId().getParamId());
				paramInfo.setType(param.getParamType());
				paramInfo.setDescription(param.getDescription());
				paramInfo.setValue(param.getValue());
				paramList.add(paramInfo);
			}
			/*
			 * ソート処理
			 */
			Collections.sort(paramList);
		}
		jobInfo.setParam(paramList);
	}

	/**
	 * jobMstEntityの情報に基づき、Job終了状態をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 */
	private static void setJobEndStatus(JobInfo jobInfo, JobMstEntity jobMstEntity) {
		//終了状態を取得
		ArrayList<JobEndStatusInfo> endList = new ArrayList<JobEndStatusInfo>();
		Collection<JobEndMstEntity> ends = jobMstEntity.getJobEndMstEntities();
		if(ends != null){
			for (JobEndMstEntity end : ends) {
				JobEndStatusInfo endInfo = new JobEndStatusInfo();
				endInfo.setType(end.getId().getEndStatus());
				endInfo.setValue(end.getEndValue());
				endInfo.setStartRangeValue(end.getEndValueFrom());
				endInfo.setEndRangeValue(end.getEndValueTo());
				endList.add(endInfo);
			}
			/*
			 * ソート処理
			 */
			Collections.sort(endList);
		}
		jobInfo.setEndStatus(endList);
	}

	/**
	 * jobMstEntityの情報に基づき、Jobの通知情報をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	private static void setJobNotifications(JobInfo jobInfo, JobMstEntity jobMstEntity)
			throws InvalidRole, HinemosUnknown {
		jobInfo.setBeginPriority(jobMstEntity.getBeginPriority());
		jobInfo.setNormalPriority(jobMstEntity.getNormalPriority());
		jobInfo.setWarnPriority(jobMstEntity.getWarnPriority());
		jobInfo.setAbnormalPriority(jobMstEntity.getAbnormalPriority());

		//通知情報の取得
		ArrayList<NotifyRelationInfo> nriList = new ArrayList<NotifyRelationInfo>();
		nriList = new NotifyControllerBean().getNotifyRelation(jobMstEntity.getNotifyGroupId());
		if (nriList != null) {
			Collections.sort(nriList);
			jobInfo.setNotifyRelationInfos(nriList);
		}
	}

	/**
	 * jobMstEntityの情報に基づき、Jobのファイル情報をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 * @throws HinemosUnknown
	 */
	private static void setJobFile(JobInfo jobInfo, JobMstEntity jobMstEntity)
			throws HinemosUnknown {
		JobFileInfo fileInfo = new JobFileInfo();
		fileInfo.setProcessingMethod(jobMstEntity.getProcessMode());
		fileInfo.setSrcFacilityID(jobMstEntity.getSrcFacilityId());
		fileInfo.setDestFacilityID(jobMstEntity.getDestFacilityId());
		fileInfo.setSrcFile(jobMstEntity.getSrcFile());
		fileInfo.setSrcWorkDir(jobMstEntity.getSrcWorkDir());
		fileInfo.setDestDirectory(jobMstEntity.getDestDirectory());
		fileInfo.setDestWorkDir(jobMstEntity.getDestWorkDir());
		fileInfo.setCompressionFlg(jobMstEntity.getCompressionFlg());
		fileInfo.setCheckFlg(jobMstEntity.getCheckFlg());
		fileInfo.setSpecifyUser(jobMstEntity.getSpecifyUser());
		fileInfo.setUser(jobMstEntity.getEffectiveUser());
		fileInfo.setMessageRetry(jobMstEntity.getMessageRetry());
		fileInfo.setMessageRetryEndFlg(jobMstEntity.getMessageRetryEndFlg());
		fileInfo.setMessageRetryEndValue(jobMstEntity.getMessageRetryEndValue());
		fileInfo.setCommandRetry(jobMstEntity.getCommandRetry());
		fileInfo.setCommandRetryFlg(jobMstEntity.getCommandRetryFlg());
		//ファシリティパスを取得
		fileInfo.setSrcScope(new RepositoryControllerBean().getFacilityPath(jobMstEntity.getSrcFacilityId(), null));
		fileInfo.setDestScope(new RepositoryControllerBean().getFacilityPath(jobMstEntity.getDestFacilityId(), null));
		jobInfo.setFile(fileInfo);
	}

	/**
	 * jobMstEntityの情報に基づき、Jobのコマンド情報をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 * @throws HinemosUnknown
	 */
	private static void setJobCommand(JobInfo jobInfo, JobMstEntity jobMstEntity)
			throws HinemosUnknown {
		JobCommandInfo commandInfo = new JobCommandInfo();
		commandInfo.setFacilityID(jobMstEntity.getFacilityId());
		commandInfo.setProcessingMethod(jobMstEntity.getProcessMode());
		commandInfo.setStartCommand(jobMstEntity.getStartCommand());
		commandInfo.setStopType(jobMstEntity.getStopType());
		commandInfo.setStopCommand(jobMstEntity.getStopCommand());
		commandInfo.setSpecifyUser(jobMstEntity.getSpecifyUser());
		commandInfo.setUser(jobMstEntity.getEffectiveUser());
		commandInfo.setMessageRetry(jobMstEntity.getMessageRetry());
		commandInfo.setMessageRetryEndFlg(jobMstEntity.getMessageRetryEndFlg());
		commandInfo.setMessageRetryEndValue(jobMstEntity.getMessageRetryEndValue());
		commandInfo.setCommandRetry(jobMstEntity.getCommandRetry());
		commandInfo.setCommandRetryFlg(jobMstEntity.getCommandRetryFlg());
		//ファシリティパスを取得
		commandInfo.setScope(new RepositoryControllerBean().getFacilityPath(jobMstEntity.getFacilityId(), null));
		jobInfo.setCommand(commandInfo);
	}

	/**
	 * jobMstEntityの情報に基づき、Jobの待ち条件をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	private static void setJobWaitRule(JobInfo jobInfo, JobMstEntity jobMstEntity)
			throws JobMasterNotFound, InvalidRole {
		//待ち条件を取得
		JobWaitRuleInfo waitRule = null;
		//待ち条件を取得
		waitRule = new JobWaitRuleInfo();
		//ジョブネット・ジョブ・ファイル転送ジョブの場合
		//待ち条件を設定
		if(jobMstEntity.getJobType() == JobConstant.TYPE_JOBNET ||
				jobMstEntity.getJobType() == JobConstant.TYPE_JOB||
				jobMstEntity.getJobType() == JobConstant.TYPE_FILEJOB ||
				jobMstEntity.getJobType() == JobConstant.TYPE_REFERJOB){
			waitRule.setSuspend(jobMstEntity.getSuspend());
			waitRule.setCondition(jobMstEntity.getConditionType());
			waitRule.setEndCondition(jobMstEntity.getUnmatchEndFlg());
			waitRule.setEndStatus(jobMstEntity.getUnmatchEndStatus());
			waitRule.setEndValue(jobMstEntity.getUnmatchEndValue());
			waitRule.setSkip(jobMstEntity.getSkip());
			waitRule.setSkipEndStatus(jobMstEntity.getSkipEndStatus());
			waitRule.setSkipEndValue(jobMstEntity.getSkipEndValue());
			waitRule.setCalendar(jobMstEntity.getCalendar());
			waitRule.setCalendarId(jobMstEntity.getCalendarId());
			waitRule.setCalendarEndStatus(jobMstEntity.getCalendarEndStatus());
			waitRule.setCalendarEndValue(jobMstEntity.getCalendarEndValue());

			waitRule.setStart_delay(jobMstEntity.getStartDelay());
			waitRule.setStart_delay_session(jobMstEntity.getStartDelaySession());
			waitRule.setStart_delay_session_value(jobMstEntity.getStartDelaySessionValue());
			waitRule.setStart_delay_time(jobMstEntity.getStartDelayTime());
			if (jobMstEntity.getStartDelayTimeValue() != null) {
				waitRule.setStart_delay_time_value(jobMstEntity.getStartDelayTimeValue().getTime());
			}
			waitRule.setStart_delay_condition_type(jobMstEntity.getStartDelayConditionType());
			waitRule.setStart_delay_notify(jobMstEntity.getStartDelayNotify());
			waitRule.setStart_delay_notify_priority(jobMstEntity.getStartDelayNotifyPriority());
			waitRule.setStart_delay_operation(jobMstEntity.getStartDelayOperation());
			waitRule.setStart_delay_operation_type(jobMstEntity.getStartDelayOperationType());
			waitRule.setStart_delay_operation_end_status(jobMstEntity.getStartDelayOperationEndStatus());
			waitRule.setStart_delay_operation_end_value(jobMstEntity.getStartDelayOperationEndValue());

			waitRule.setEnd_delay(jobMstEntity.getEndDelay());
			waitRule.setEnd_delay_session(jobMstEntity.getEndDelaySession());
			waitRule.setEnd_delay_session_value(jobMstEntity.getEndDelaySessionValue());
			waitRule.setEnd_delay_job(jobMstEntity.getEndDelayJob());
			waitRule.setEnd_delay_job_value(jobMstEntity.getEndDelayJobValue());
			waitRule.setEnd_delay_time(jobMstEntity.getEndDelayTime());
			if (jobMstEntity.getEndDelayTimeValue() != null) {
				waitRule.setEnd_delay_time_value(jobMstEntity.getEndDelayTimeValue().getTime());
			}
			waitRule.setEnd_delay_condition_type(jobMstEntity.getEndDelayConditionType());
			waitRule.setEnd_delay_notify(jobMstEntity.getEndDelayNotify());
			waitRule.setEnd_delay_notify_priority(jobMstEntity.getEndDelayNotifyPriority());
			waitRule.setEnd_delay_operation(jobMstEntity.getEndDelayOperation());
			waitRule.setEnd_delay_operation_type(jobMstEntity.getEndDelayOperationType());
			waitRule.setEnd_delay_operation_end_status(jobMstEntity.getEndDelayOperationEndStatus());
			waitRule.setEnd_delay_operation_end_value(jobMstEntity.getEndDelayOperationEndValue());
			waitRule.setMultiplicityNotify(jobMstEntity.getMultiplicityNotify());
			waitRule.setMultiplicityNotifyPriority(jobMstEntity.getMultiplicityNotifyPriority());
			waitRule.setMultiplicityOperation(jobMstEntity.getMultiplicityOperation());
			waitRule.setMultiplicityEndValue(jobMstEntity.getMultiplicityEndValue());
		}

		//待ち条件（ジョブ）を取得
		Collection<JobStartJobMstEntity> startJobList = jobMstEntity.getJobStartJobMstEntities();
		ArrayList<JobObjectInfo> objectList = new ArrayList<JobObjectInfo>();
		if(startJobList != null && startJobList.size() > 0){
			for (JobStartJobMstEntity startJob : startJobList){
				if(startJob != null){
					JobObjectInfo objectInfo = new JobObjectInfo();
					objectInfo.setJobId(startJob.getId().getTargetJobId());
					//対象ジョブを取得
					JobMstEntity targetJob = QueryUtil.getJobMstPK(startJob.getId().getTargetJobunitId(), startJob.getId().getTargetJobId());
					objectInfo.setJobName(targetJob.getJobName());
					objectInfo.setType(startJob.getId().getTargetJobType());
					objectInfo.setValue(startJob.getId().getTargetJobEndValue());
					objectList.add(objectInfo);
				}
			}
		}

		//待ち条件（時刻）を取得
		if (jobMstEntity.getStartTime() != null) {
			JobObjectInfo objectInfo = new JobObjectInfo();
			objectInfo.setType(JudgmentObjectConstant.TYPE_TIME);
			objectInfo.setTime(jobMstEntity.getStartTime().getTime());
			objectList.add(objectInfo);
		}
		m_log.debug("job.getStartMinute() = " + jobMstEntity.getStartMinute());
		//待ち条件（セッション開始時の時間（分））を取得
		if (jobMstEntity.getStartMinute() != null) {
			JobObjectInfo objectInfo = new JobObjectInfo();
			objectInfo.setType(JudgmentObjectConstant.TYPE_START_MINUTE);
			objectInfo.setStartMinute(jobMstEntity.getStartMinute());
			objectList.add(objectInfo);
		}

		/*
		 * ソート処理
		 */
		Collections.sort(objectList);
		waitRule.setObject(objectList);
		jobInfo.setWaitRule(waitRule);
	}

	/**
	 * ジョブツリー配下のジョブを全てfullProperty=trueにする。
	 * @param job
	 * @throws JobMasterNotFound
	 * @throws NotifyNotFound
	 * @throws UserNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public static void setJobTreeFull (JobTreeItem job)
			throws JobMasterNotFound, NotifyNotFound, UserNotFound, InvalidRole, HinemosUnknown {
		JobInfo jobInfo = job.getData();
		if (!jobInfo.isPropertyFull()) {
			job.setData(getJobFull(jobInfo));
		}
		if (job.getChildren() == null) {
			return;
		}
		for (JobTreeItem childJob : job.getChildren()) {
			setJobTreeFull(childJob);
		}
	}
}
