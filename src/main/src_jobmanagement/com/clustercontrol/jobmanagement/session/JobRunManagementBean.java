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

package com.clustercontrol.jobmanagement.session;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobOperationInfo;
import com.clustercontrol.jobmanagement.bean.JobSessionRequestMessage;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.bean.RunResultInfo;
import com.clustercontrol.jobmanagement.factory.CreateJobSession;
import com.clustercontrol.jobmanagement.factory.JobOperationJudgment;
import com.clustercontrol.jobmanagement.factory.OperateForceStopOfJob;
import com.clustercontrol.jobmanagement.factory.OperateMaintenanceOfJob;
import com.clustercontrol.jobmanagement.factory.JobSessionImpl;
import com.clustercontrol.jobmanagement.factory.JobSessionJobImpl;
import com.clustercontrol.jobmanagement.factory.JobSessionNodeImpl;
import com.clustercontrol.jobmanagement.factory.OperateSkipOfJob;
import com.clustercontrol.jobmanagement.factory.OperateStartOfJob;
import com.clustercontrol.jobmanagement.factory.OperateStopOfJob;
import com.clustercontrol.jobmanagement.factory.OperateSuspendOfJob;
import com.clustercontrol.jobmanagement.factory.OperateWaitOfJob;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntityPK;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.util.apllog.AplLogger;


/**
 * ジョブ管理機能の実行管理を行う Session Bean クラス<BR>
 * 
 */
public class JobRunManagementBean {
	/** ログ出力のインスタンス<BR> */
	private static Log m_log = LogFactory.getLog( JobRunManagementBean.class );

	/** Quartzに設定するグループ名<BR> */
	public static final String GROUP_NAME = "JOB_MANAGEMENT";

	// 二重起動を防ぐためのセマフォ
	private static final Semaphore duplicateExec = new Semaphore(1);

	/**
	 * Quartzからのコールバックメソッド<BR>
	 * <P>
	 * Quartzから定周期で呼び出されます。<BR>
	 * <BR>
	 * 実行状態が実行中のセッションをチェックし、実行可能なジョブを開始する。<BR>
	 * 実行状態が待機のセッションをチェックし、ジョブを開始する。<BR>
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * 
	 * トランザクション開始はユーザが制御する。
	 * また、追加実装により、トランザクションの入れ子が予期せず生じることを避けるため、Neverを採用する。
	 * 
	 * @see com.clustercontrol.jobmanagement.factory.OperationJob#runJob()
	 * @see com.clustercontrol.jobmanagement.session.JobRunManagementBean#runWaitJob()
	 */
	public void run() throws HinemosUnknown, JobInfoNotFound, InvalidRole {
		if (duplicateExec.tryAcquire()) {
			try {
				runSub();
			} finally {
				duplicateExec.release();
			}
		} else {
			m_log.warn("runningCheck is busy !!");
		}
	}
	
	public static ILock getLock (String sessionId) {
		ILockManager lm = LockManagerFactory.instance().create();
		return lm.create(JobSessionImpl.class.getName() + "-" + sessionId);
	}

	private void runSub() throws HinemosUnknown, JobInfoNotFound, InvalidRole {
		m_log.trace("run() start");
		JpaTransactionManager jtm = null;
		List<String> unendSessionIdList = null;
		HashMap<String, List<JobSessionNodeEntityPK>> sessionNodeMap = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 実行中のジョブセッションをチェック。（待ち条件に時刻が入っていることがあるので。）
			unendSessionIdList = new JobSessionImpl().getRunUnendSession();

			// エージェントタイムアウトをチェック
			sessionNodeMap = new JobSessionNodeImpl().checkTimeoutAll();
			jtm.commit();
		} catch (JobInfoNotFound e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("run() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		// 実行中のジョブセッションをチェック。（待ち条件に時刻が入っていることがあるので。）
		long from = System.currentTimeMillis();
		for (String sessionId : unendSessionIdList) {
			m_log.trace("run() unendSessionId=" + sessionId);
			if (!JobSessionJobImpl.checkRemoveForceCheck(sessionId)) {
				if (JobSessionJobImpl.isSkipCheck(sessionId)) {
					continue;
				}
			}

			ILock lock = getLock(sessionId);
			try {
				lock.writeLock();
				
				try{
					jtm = new JpaTransactionManager();
					jtm.begin();
					new JobSessionImpl().runningCheck(sessionId);
					jtm.commit();
				} catch (JobInfoNotFound e) {
					jtm.rollback();
					throw e;
				} catch (HinemosUnknown e){
					jtm.rollback();
					throw e;
				} catch (InvalidRole e){
					jtm.rollback();
					throw e;
				} catch (Exception e) {
					m_log.warn("run() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					jtm.rollback();
					throw new HinemosUnknown(e.getMessage(), e);
				} finally {
					jtm.close();
				}
			} finally {
				lock.writeUnlock();
			}
		}
		long to = System.currentTimeMillis();
		long time1 = to - from;

		// エージェントタイムアウトをチェック
		from = System.currentTimeMillis();
		int nodeCount = 0;
		for (String sessionId : sessionNodeMap.keySet()) {

			ILock lock = getLock(sessionId);
			try {
				lock.writeLock();
				
				try{
					jtm = new JpaTransactionManager();
					jtm.begin();
					for (JobSessionNodeEntityPK pk : sessionNodeMap.get(sessionId)) {
						new JobSessionNodeImpl().checkTimeout(pk);
						nodeCount ++;
					}
					jtm.commit();
				} catch (JobInfoNotFound e) {
					jtm.rollback();
					throw e;
				} catch (Exception e) {
					m_log.warn("run() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					jtm.rollback();
					throw new HinemosUnknown(e.getMessage(), e);
				} finally {
					jtm.close();
				}
			} finally {
				lock.writeUnlock();
			}
		}
		to = System.currentTimeMillis();
		long time2 = to - from;

		String message = "runningCheck(" + unendSessionIdList.size() + "): " + time1 + "ms" +
				", checkTimeout(" + sessionNodeMap.size() + "," + nodeCount + "): " + time2 + "ms";
		long total = time1 + time2;
		if (total > 30 * 1000) {
			m_log.warn(message + "!");
		} else if (total > 10 * 1000){
			m_log.info(message);
		} else {
			m_log.debug(message);
		}
	}

	/**
	 * ジョブを実行します。<BR>
	/**
	 * CreateJobSessionTaskFactoryから呼ばれる
	 * 
	 */
	public static void makeSession(JobSessionRequestMessage message) {
		String sessionId = message.getSessionId();
		String jobunitId = message.getJobunitId();
		String jobId = message.getJobId();
		OutputBasicInfo info = message.getOutputBasicInfo();
		JobTriggerInfo triggerInfo = message.getTriggerInfo();
		
		JpaTransactionManager jtm = null;

		ILock lock = getLock(sessionId);
		try {
			lock.writeLock();
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();
	
				JobMstEntity job = QueryUtil.getJobMstPK(jobunitId, jobId);
				//JobSessionを作成
				JobSessionEntity jobSessionEntity = new JobSessionEntity(sessionId);
				// 重複チェック
				jtm.checkEntityExists(JobSessionEntity.class, jobSessionEntity.getSessionId());
				jobSessionEntity.setJobunitId(jobunitId);
				jobSessionEntity.setJobId(job.getId().getJobId());
				jobSessionEntity.setScheduleDate(new Timestamp(System.currentTimeMillis()));
				jobSessionEntity.setOperationFlg(0);
				jobSessionEntity.setTriggerType(triggerInfo.getTrigger_type());
				jobSessionEntity.setTriggerInfo(triggerInfo.getTrigger_info());
	
				m_log.trace("jobSessionEntity SessionId : " + jobSessionEntity.getSessionId());
				m_log.trace("jobSessionEntity JobUnitId : " + jobSessionEntity.getJobunitId());
				m_log.trace("jobSessionEntity JobId : " + jobSessionEntity.getJobId());
	
				// 最上位のジョブセッション作成
				// インスタンス生成
				JobSessionJobEntity jobSessionJobEntity
				= new JobSessionJobEntity(jobSessionEntity, CreateJobSession.TOP_JOBUNIT_ID, CreateJobSession.TOP_JOB_ID);
				// 重複チェック
				jtm.checkEntityExists(JobSessionJobEntity.class, jobSessionJobEntity.getId());
				jobSessionJobEntity.setStatus(StatusConstant.TYPE_WAIT);
				// ジョブセッション作成(このジョブは待ち条件は無視する。)
				CreateJobSession.createJobSessionJob(job, sessionId, info, true, triggerInfo);
	
				jtm.commit();
			} catch (Exception e) {
				m_log.warn("makeSession() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				AplLogger apllog = new AplLogger("JOB", "job");
				String[] args = {jobId};
				apllog.put("SYS", "003", args);
				return;
			} finally {
				jtm.close();
			}
			
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();
				//実行
				new JobSessionJobImpl().startJob(sessionId, jobunitId, jobId);
				jtm.commit();
			} catch (Exception e) {
				m_log.warn("makeSession() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				AplLogger apllog = new AplLogger("JOB", "job");
				String[] args = {jobId};
				apllog.put("SYS", "003", args);
				jtm.rollback();
				return;
			} finally {
				jtm.close();
			}
		} finally {
			lock.writeUnlock();
		}
	}

	public void operation(JobOperationInfo property) throws HinemosUnknown, JobInfoNotFound, InvalidRole, IllegalStateException {
		String sessionId = property.getSessionId();
		String jobunitId = property.getJobunitId();
		String jobId = property.getJobId();
		String facilityId = property.getFacilityId();
		Integer control = property.getControl();
		Integer endStatus = property.getEndStatus();
		Integer endValue = property.getEndValue();

		m_log.info("operationJob() " + "jobId=" + jobId + ", facilityId= " + facilityId +
				", control=" + control + ", endStatus=" + endStatus + ", endValue=" + endValue);

		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		if (sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_FILEJOB) {
			if (facilityId != null && facilityId.length() > 0) {
				// ノード詳細に対する操作
				operationJob(sessionId, jobunitId, jobId + "_" + facilityId, control, endStatus, endValue);
			} else {
				// ジョブ詳細に対する操作
				operationJob(sessionId, jobunitId, jobId, control, endStatus, endValue);
			}
		} else if (facilityId == null || facilityId.length() == 0) {
			// ジョブ詳細に対する操作
			operationJob(sessionId, jobunitId, jobId, control, endStatus, endValue);
		} else {
			// ノード詳細に対する操作
			operationNode(sessionId, jobunitId, jobId, facilityId, control, endValue);
		}
	}

	/**
	 * ノードの操作を行います。<BR>
	 * 
	 * @param property ジョブ操作用プロパティ
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws IllegalStateException
	 * @throws InvalidRole
	 */
	private void operationNode(String sessionId, String jobunitId, String jobId, String facilityId, Integer control, Integer endValue)
			throws HinemosUnknown, JobInfoNotFound, IllegalStateException, InvalidRole {
		m_log.info("operationJob() " + "jobId=" + jobId + ", control=" + control + ", endValue=" + endValue);
		JpaTransactionManager jtm = null;

		int status = 0;
		int jobType = 0;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			try {
				//セッションIDとジョブIDから、セッションジョブを取得
				JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

				//ジョブタイプを取得
				if(sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_FILEJOB){
					jobId = jobId + "_" + facilityId;

					//セッションIDとジョブIDから、セッションジョブを取得
					JobSessionJobEntity childSessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

					//実行状態を取得
					status = childSessionJob.getStatus();

					//ジョブタイプを取得
					jobType = JobOperationJudgment.TYPE_JOBNET;
				} else {
					//セッションIDとジョブIDから、セッションジョブを取得
					JobSessionNodeEntity sessionNode = QueryUtil.getJobSessionNodePK(sessionId, jobunitId, jobId, facilityId);

					//実行状態を取得
					status = sessionNode.getStatus();

					//ジョブタイプを取得
					jobType = JobOperationJudgment.TYPE_NODE;
				}
			} catch (Exception e) {
				m_log.warn("operationJob() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}

			//ジョブタイプとジョブの実行状態から操作可能かチェック
			if(JobOperationJudgment.judgment(control, jobType, status)){
				if(control == OperationConstant.TYPE_START_AT_ONCE){
					//開始[即時]
					try {
						new OperateStartOfJob().startNode(sessionId, jobunitId, jobId, facilityId);
					} catch (JobInfoNotFound e) {
						AplLogger apllog = new AplLogger("JOB", "job");
						String[] args = {sessionId, jobId, facilityId};
						apllog.put("SYS", "007", args);
						throw e;
					} catch (InvalidRole e) {
						AplLogger apllog = new AplLogger("JOB", "job");
						String[] args = {sessionId, jobId, facilityId};
						apllog.put("SYS", "007", args);
						throw e;
					}
				} else if(control == OperationConstant.TYPE_STOP_AT_ONCE){
					try {
						//停止[コマンド]
						new OperateStopOfJob().stopNode(sessionId, jobunitId, jobId, facilityId);
					} catch (JobInfoNotFound e) {
						AplLogger apllog = new AplLogger("JOB", "job");
						String[] args = {sessionId, jobId, facilityId};
						apllog.put("SYS", "011", args);
						throw e;
					} catch (InvalidRole e) {
						AplLogger apllog = new AplLogger("JOB", "job");
						String[] args = {sessionId, jobId, facilityId};
						apllog.put("SYS", "011", args);
						throw e;
					} catch (HinemosUnknown e) {
						AplLogger apllog = new AplLogger("JOB", "job");
						String[] args = {sessionId, jobId, facilityId};
						apllog.put("SYS", "011", args);
						throw e;
					}
				} else if(control == OperationConstant.TYPE_STOP_MAINTENANCE){
					//停止[状態変更]
					if(endValue == null){
						throw new NullPointerException();
					}
					try {
						new OperateMaintenanceOfJob().maintenanceNode(sessionId, jobunitId, jobId, facilityId, endValue);
					} catch (JobInfoNotFound e) {
						AplLogger apllog = new AplLogger("JOB", "job");
						String[] args = {sessionId, jobId, facilityId};
						apllog.put("SYS", "015", args);
						throw e;
					} catch (HinemosUnknown e) {
						AplLogger apllog = new AplLogger("JOB", "job");
						String[] args = {sessionId, jobId, facilityId};
						apllog.put("SYS", "015", args);
						throw e;
					} catch (InvalidRole e) {
						AplLogger apllog = new AplLogger("JOB", "job");
						String[] args = {sessionId, jobId, facilityId};
						apllog.put("SYS", "015", args);
						throw e;
					} catch (NullPointerException e) {
						AplLogger apllog = new AplLogger("JOB", "job");
						String[] args = {sessionId, jobId, facilityId};
						apllog.put("SYS", "015", args);
						m_log.warn("operationJob() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						throw new HinemosUnknown(e.getMessage(), e);
					}
				} else if(control == OperationConstant.TYPE_STOP_FORCE){
					//停止[強制]
					if(endValue == null){
						throw new NullPointerException();
					}
					try {
						new OperateForceStopOfJob().forceStopNode(sessionId, jobunitId, jobId, facilityId, endValue);//修正予定
					} catch (JobInfoNotFound e) {
						AplLogger apllog = new AplLogger("JOB", "job");
						String[] args = {sessionId, jobId, facilityId};
						apllog.put("SYS", "018", args);
						throw e;
					} catch (HinemosUnknown e) {
						AplLogger apllog = new AplLogger("JOB", "job");
						String[] args = {sessionId, jobId, facilityId};
						apllog.put("SYS", "018", args);
						throw e;
					} catch (InvalidRole e) {
						AplLogger apllog = new AplLogger("JOB", "job");
						String[] args = {sessionId, jobId, facilityId};
						apllog.put("SYS", "018", args);
						throw e;
					} catch (NullPointerException e) {
						AplLogger apllog = new AplLogger("JOB", "job");
						String[] args = {sessionId, jobId, facilityId};
						apllog.put("SYS", "018", args);
						m_log.warn("operationJob() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						throw new HinemosUnknown(e.getMessage(), e);
					}
				} else {
					m_log.warn("operationNode() : unknown control. " + control);
				}
			} else {
				IllegalStateException e = new IllegalStateException();
				m_log.info("operationJob() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw e;
			}

			jtm.commit();
		} catch (IllegalStateException e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (JobInfoNotFound e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("operationJob() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	/**
	 * ジョブの操作を行います。<BR>
	 * 
	 * @param property ジョブ操作用プロパティ
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws IllegalStateException
	 * @throws InvalidRole
	 */
	private void operationJob(String sessionId, String jobunitId, String jobId, Integer control,
			Integer endStatus, Integer endValue)
					throws HinemosUnknown, JobInfoNotFound, InvalidRole {
		m_log.info("operationJob() " + "jobId=" + jobId + ", control=" + control +
				", endStatus=" + endStatus + ", endValue=" + endValue);
		JpaTransactionManager jtm = null;

		int status = 0;
		int jobType = 0;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			try {
				//セッションIDとジョブIDから、セッションジョブを取得
				JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

				//実行状態を取得
				status = sessionJob.getStatus();

				if(sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_JOB){
					jobType = JobOperationJudgment.TYPE_JOB;
				} else{
					jobType = JobOperationJudgment.TYPE_JOBNET;
				}
			} catch (Exception e) {
				m_log.warn("operationJob() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}

			//ジョブタイプとジョブの実行状態から操作可能かチェック
			if(!JobOperationJudgment.judgment(control, jobType, status)){
				IllegalStateException e = new IllegalStateException("illegal status " + status);
				m_log.info("operationJob() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw e;
			}

			if(control == OperationConstant.TYPE_START_AT_ONCE){
				//開始[即時]
				try {
					new OperateStartOfJob().startJob(sessionId, jobunitId, jobId);
				} catch (JobInfoNotFound e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId, null};
					apllog.put("SYS", "007", args);
					throw e;
				} catch (InvalidRole e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId, null};
					apllog.put("SYS", "007", args);
					throw e;
				}
			} else if(control == OperationConstant.TYPE_START_SUSPEND){
				//開始[中断解除]
				try {
					new OperateSuspendOfJob().releaseSuspendJob(sessionId, jobunitId, jobId);
				} catch (JobInfoNotFound e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId};
					apllog.put("SYS", "008", args);
					throw e;
				} catch (InvalidRole e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId};
					apllog.put("SYS", "008", args);
					throw e;
				}
			} else if(control == OperationConstant.TYPE_START_WAIT){
				//開始[保留解除]
				try {
					new OperateWaitOfJob().releaseWaitJob(sessionId, jobunitId, jobId);
				} catch (JobInfoNotFound e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId, null};
					apllog.put("SYS", "009", args);
					throw e;
				} catch (InvalidRole e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId, null};
					apllog.put("SYS", "009", args);
					throw e;
				}
			} else if(control == OperationConstant.TYPE_START_SKIP){
				//開始[スキップ解除]
				try {
					new OperateSkipOfJob().releaseSkipJob(sessionId, jobunitId, jobId);
				} catch (JobInfoNotFound e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId, null};
					apllog.put("SYS", "010", args);
					throw e;
				} catch (InvalidRole e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId, null};
					apllog.put("SYS", "010", args);
					throw e;
				}
			} else if(control == OperationConstant.TYPE_STOP_AT_ONCE){
				try {
					//停止[コマンド]
					new OperateStopOfJob().stopJob(sessionId, jobunitId, jobId);
				} catch (JobInfoNotFound e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId, null};
					apllog.put("SYS", "011", args);
					throw e;
				} catch (InvalidRole e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId, null};
					apllog.put("SYS", "011", args);
					throw e;
				} catch (HinemosUnknown e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId, null};
					apllog.put("SYS", "011", args);
					throw e;
				}
			} else if(control == OperationConstant.TYPE_STOP_SUSPEND){
				try {
					//停止[中断]
					new OperateSuspendOfJob().suspendJob(sessionId, jobunitId, jobId);
				} catch (InvalidRole e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId};
					apllog.put("SYS", "012", args);
					throw e;
				} catch (JobInfoNotFound e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId};
					apllog.put("SYS", "012", args);
					throw e;
				}
			} else if(control == OperationConstant.TYPE_STOP_WAIT){
				try {
					//停止[保留]
					new OperateWaitOfJob().waitJob(sessionId, jobunitId, jobId);
				} catch (JobInfoNotFound e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId};
					apllog.put("SYS", "012", args);
					throw e;
				} catch (InvalidRole e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId};
					apllog.put("SYS", "012", args);
					throw e;
				}
			} else if(control == OperationConstant.TYPE_STOP_SKIP){
				//停止[スキップ]
				try {
					new OperateSkipOfJob().skipJob(sessionId, jobunitId, jobId);
				} catch (JobInfoNotFound e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId};
					apllog.put("SYS", "013", args);
					throw e;
				} catch (InvalidRole e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId};
					apllog.put("SYS", "013", args);
					throw e;
				} catch (NullPointerException e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId};
					apllog.put("SYS", "013", args);
					m_log.warn("operationJob() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				}
			} else if(control == OperationConstant.TYPE_STOP_MAINTENANCE){
				//停止[状態変更]
				if(endValue == null){
					throw new NullPointerException();
				}
				try {
					new OperateMaintenanceOfJob().maintenanceJob(sessionId, jobunitId, jobId,
							StatusConstant.TYPE_MODIFIED, endStatus, endValue);
				} catch (JobInfoNotFound e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId, null};
					apllog.put("SYS", "015", args);
					throw e;
				} catch (HinemosUnknown e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId, null};
					apllog.put("SYS", "015", args);
					throw e;
				} catch (InvalidRole e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId, null};
					apllog.put("SYS", "015", args);
					throw e;
				} catch (NullPointerException e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId, null};
					apllog.put("SYS", "015", args);
					m_log.warn("operationJob() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				}
			} else if(control == OperationConstant.TYPE_STOP_FORCE){
				//停止[強制]
				if(endValue == null){
					throw new NullPointerException();
				}
				try {
					new OperateForceStopOfJob().forceStopJob(sessionId, jobunitId, jobId, endStatus, endValue);
				} catch (JobInfoNotFound e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId, null};
					apllog.put("SYS", "018", args);
					throw e;
				} catch (HinemosUnknown e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId, null};
					apllog.put("SYS", "018", args);
					throw e;
				} catch (InvalidRole e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId, null};
					apllog.put("SYS", "018", args);
					throw e;
				} catch (NullPointerException e) {
					AplLogger apllog = new AplLogger("JOB", "job");
					String[] args = {sessionId, jobId, null};
					apllog.put("SYS", "018", args);
					m_log.warn("operationJob() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				}
			} else {
				m_log.warn("operationJob() : unknown control. " + control);
			}

			jtm.commit();
		} catch (IllegalStateException e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (JobInfoNotFound e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("operationJob() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	/**
	 * ノード終了処理を行います。<BR>
	 * 
	 * @param info 実行結果情報
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.jobmanagement.factory.OperationJob#endNode(RunResultInfo)
	 */
	public boolean endNode(RunResultInfo info) throws HinemosUnknown, JobInfoNotFound, InvalidRole {
		m_log.trace("endNode() : sessionId=" + info.getSessionId() + ", jobId=" + info.getJobId() + ", facilityId=" + info.getFacilityId());
		JpaTransactionManager jtm = null;

		boolean result = false;

		ILock lock = getLock(info.getSessionId());
		try {
			lock.writeLock();
			try {
				jtm = new JpaTransactionManager();
				JobSessionNodeImpl nodeImpl = new JobSessionNodeImpl();
				jtm.begin();
				result = nodeImpl.endNode(info);
				jtm.commit();
			} catch (JobInfoNotFound e) {
				jtm.rollback();
				throw e;
			} catch (HinemosUnknown e) {
				jtm.rollback();
				throw e;
			} catch (InvalidRole e) {
				jtm.rollback();
				throw e;
			} catch (EntityExistsException e) {
				jtm.rollback();
				m_log.warn("endNode() : " + e.getMessage(), e);
				throw new HinemosUnknown(e.getMessage(), e);
			} catch (Exception e) {
				m_log.warn("endNode() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} finally {
				jtm.close();
			}
		} finally {
			lock.writeUnlock();
		}
		return result;
	}

}
