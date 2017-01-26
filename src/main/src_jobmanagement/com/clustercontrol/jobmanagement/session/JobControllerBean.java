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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PluginConstant;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.calendar.bean.CalendarInfo;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.commons.session.CheckFacility;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobInvalid;
import com.clustercontrol.fault.JobKickDuplicate;
import com.clustercontrol.fault.JobMasterDuplicate;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.JobSessionDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.OtherUserGetLock;
import com.clustercontrol.fault.UpdateTimeNotLatest;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobFileCheck;
import com.clustercontrol.jobmanagement.bean.JobForwardFile;
import com.clustercontrol.jobmanagement.bean.JobHistoryFilter;
import com.clustercontrol.jobmanagement.bean.JobHistoryList;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.bean.JobKick;
import com.clustercontrol.jobmanagement.bean.JobNodeDetail;
import com.clustercontrol.jobmanagement.bean.JobOperationInfo;
import com.clustercontrol.jobmanagement.bean.JobPlan;
import com.clustercontrol.jobmanagement.bean.JobPlanFilter;
import com.clustercontrol.jobmanagement.bean.JobSchedule;
import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.factory.CreateJobSession;
import com.clustercontrol.jobmanagement.factory.FullJob;
import com.clustercontrol.jobmanagement.factory.JobOperationProperty;
import com.clustercontrol.jobmanagement.factory.JobSessionImpl;
import com.clustercontrol.jobmanagement.factory.JobSessionJobImpl;
import com.clustercontrol.jobmanagement.factory.ModifyJob;
import com.clustercontrol.jobmanagement.factory.ModifyJobKick;
import com.clustercontrol.jobmanagement.factory.SelectJob;
import com.clustercontrol.jobmanagement.factory.SelectJobKick;
import com.clustercontrol.jobmanagement.model.JobEditEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntityPK;
import com.clustercontrol.jobmanagement.util.JobValidator;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.jobmanagement.util.SendTopic;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * ジョブ管理機能の管理を行う Session Bean クラス<BR>
 * <p>クライアントからの Entity Bean へのアクセスは、Session Bean を介して行います。<BR>
 *
 */
public class JobControllerBean implements CheckFacility {
	/** ログ出力のインスタンス<BR> */
	private static Log m_log = LogFactory.getLog( JobControllerBean.class );

	private Random random = new Random();

	/**
	 * セパレータ文字列を取得する。<BR>
	 *
	 * @return セパレータ文字列
	 */
	public String getSeparator() {
		return SelectJob.SEPARATOR;
	}

	/**
	 * ジョブツリー情報を取得する。<BR>
	 *
	 * @param ownerRoleId オーナーロールID（オーナーロールIDで絞込みをしない場合は未設定）
	 * @param treeOnly true=ジョブ情報を含まない, false=ジョブ情報含む
	 * @param locale ロケール情報
	 * @return ジョブツリー情報{@link com.clustercontrol.jobmanagement.bean.JobTreeItem}の階層オブジェクト
	 * @throws NotifyNotFound
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws UserNotFound
	 * @throws InvalidRole
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getJobTree(boolean, Locale)
	 */
	public JobTreeItem getJobTree(String ownerRoleId, boolean treeOnly, Locale locale) throws NotifyNotFound, HinemosUnknown, JobMasterNotFound, UserNotFound, InvalidRole {
		m_log.debug("getJobTree() : treeOnly=" + treeOnly + ", locale=" + locale);

		JpaTransactionManager jtm = null;
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		JobTreeItem item = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			//ジョブツリーを取得
			SelectJob select = new SelectJob();
			long start = System.currentTimeMillis();
			item = select.getJobTree(ownerRoleId, treeOnly, locale, loginUser);
			long end = System.currentTimeMillis();
			m_log.info("getJobTree " + (end - start) + "ms");
			jtm.commit();
		} catch (NotifyNotFound e) {
			jtm.rollback();
			throw e;
		} catch (JobMasterNotFound e) {
			jtm.rollback();
			throw e;
		} catch (UserNotFound e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getJobTree() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		removeParent(item);
		return item;
	}

	/**
	 * webサービスでは双方向の参照を保持することができないので、
	 * 親方向への参照を消す。
	 * クライアント側で参照を付与する。
	 * @param jobTreeItem
	 */
	private void removeParent(JobTreeItem jobTreeItem) {
		jobTreeItem.setParent(null);
		for (JobTreeItem child : jobTreeItem.getChildren()) {
			removeParent(child);
		}
	}

	/**
	 * removeParentの逆操作
	 */
	private void setParent(JobTreeItem jobTreeItem) {
		for (JobTreeItem child : jobTreeItem.getChildren()) {
			child.setParent(jobTreeItem);
			setParent(child);
		}
	}

	/**
	 * ジョブ情報詳細を取得する。<BR>
	 *
	 * @param jobInfo ジョブ情報(ツリー情報のみ)
	 * @return ジョブ情報(Full)
	 * @throws JobMasterNotFound
	 * @throws UserNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getJobFull(JobInfo jobInfo)
	 */
	public JobInfo getJobFull(JobInfo jobInfo) throws JobMasterNotFound, UserNotFound, HinemosUnknown, InvalidRole {

		m_log.debug("getJobFull() : jobunitId=" + jobInfo.getJobunitId() +
				", jobId=" + jobInfo.getId());

		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			//ジョブツリーを取得
			jobInfo = FullJob.getJobFull(jobInfo);

			jtm.commit();
		} catch (JobMasterNotFound e) {
			jtm.rollback();
			throw e;
		} catch (UserNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getJobFull() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e );
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return jobInfo;
	}

	/**
	 * ジョブ情報詳細を取得する。<BR>
	 *
	 * @param jobInfo ジョブ情報(ツリー情報のみ)
	 * @return ジョブ情報(Full)
	 * @throws UserNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getJobFull(JobInfo jobInfo)
	 */
	public List<JobInfo> getJobFullList(List<JobInfo> jobList) throws UserNotFound, HinemosUnknown, InvalidRole {

		m_log.debug("getJobFullList() : size=" + jobList.size());

		List<JobInfo> ret = new ArrayList<JobInfo>();
		
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			//ジョブ詳細を取得
			for (JobInfo info : jobList) {
				try {
					ret.add(FullJob.getJobFull(info));
				} catch (JobMasterNotFound e) {
					m_log.debug("getJobFullList : " + e.getMessage());
				}
			}

			jtm.commit();
		} catch (UserNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getJobFull() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e );
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return ret;
	}

	
	
	/**
	 * ジョブユニット情報を登録する。<BR>
	 *
	 * トランザクション開始はユーザが制御する。
	 * (replaceJobunitおよびregisterJobunitをRequiredにより別トランザクションで実行するため)
	 * また、追加実装により、トランザクションの入れ子が予期せず生じることを避けるため、Neverを採用する。
	 *
	 * @param item ジョブツリー情報{@link com.clustercontrol.jobmanagement.bean.JobTreeItem}の階層オブジェクト
	 * @return ジョブユニットの最終更新日時
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws JobInvalid
	 * @throws InvalidRole 
	 * @throws InvalidSetting 
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJob#registerJob(JobTreeItem, String)
	 */
	public Long registerJobunit(JobTreeItem jobunit) throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidRole, InvalidSetting {
		long start = System.currentTimeMillis();
		m_log.debug("registerJob() start");
		JpaTransactionManager jtm = null;
		Long lastUpdateTime = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin(true);

			// ジョブはプロパティ値が一部不完全なので、DBより参照して完全なものとする。
			FullJob.setJobTreeFull(jobunit);
			setParent(jobunit);
			setJobunitIdAll(jobunit, jobunit.getData().getJobunitId()); // ジョブユニットIDを更新

			// Validate
			long beforeValidate = System.currentTimeMillis();
			JobValidator.validateJobUnit(jobunit);
			m_log.debug(String.format("validateJobUnit: %d ms", System.currentTimeMillis() - beforeValidate));

			int type = jobunit.getData().getType();
			if (type != JobConstant.TYPE_JOBUNIT){
				String jobId = jobunit.getData().getId();
				String message = "There is not jobunit but job(" + type + ", " + jobId + ") " ;
				m_log.info("registerJob() : " + message);
				throw new JobInvalid(message);
			}

			m_log.debug("registerJob() : registerJobunit : jobunit " + jobunit.getData().getJobunitId());

			String jobunitId = jobunit.getData().getJobunitId();
			String jobId = jobunit.getData().getId();
			boolean replace = false;
			try{
				// jobunitIdとjobIdが既に存在するか
				//true : 参照権限関係なしに全件検索する場合
				long beforeValidateJobId = System.currentTimeMillis();
				JobValidator.validateJobId(jobunitId, jobId,true);
				m_log.debug(String.format("validateJobId: %d ms", System.currentTimeMillis() - beforeValidateJobId));

				replace = true;
				m_log.info("registerJob() : jobunit " + jobunitId + " is exist");
			} catch (InvalidSetting e) {
				m_log.info("registerJob() : jobunit " + jobunitId + " is new jobunit");
			}

			// 登録 or 置換
			if(replace){
				lastUpdateTime = replaceJobunit(jobunit);
			} else{
				lastUpdateTime = registerNewJobunit(jobunit);
			}
			
			releaseEditLockAfterJobEdit(jobunitId);
			
			long beforeCommit = System.currentTimeMillis();
			jtm.commit();
			m_log.debug(String.format("jtm.commit: %d ms", System.currentTimeMillis() - beforeCommit));
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (JobInvalid e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (InvalidSetting e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("registerJob() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e );
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		m_log.debug(String.format("registerJobunit: %d ms", System.currentTimeMillis() - start));
		
		return lastUpdateTime;
	}

	/**
	 * ジョブユニット単位でジョブツリー情報を置換する。<BR>
	 *
	 * @param jobunit
	 * @return ジョブユニットの最終更新日時
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws UserNotFound
	 * @throws InvalidSetting
	 * @throws JobInvalid
	 * @throws InvalidRole
	 */
	private Long replaceJobunit(JobTreeItem jobunit) throws HinemosUnknown, JobMasterNotFound, UserNotFound, InvalidSetting, JobInvalid, InvalidRole {
		m_log.debug("replaceJobunit() : jobunitId = " + jobunit.getData().getJobunitId());
		JpaTransactionManager jtm = null;
		Long lastUpdateTime = null; // ジョブの更新日時
		
		try{
			jtm = new JpaTransactionManager();
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();

			long start = System.currentTimeMillis();

			List<JobInfo> newList = tree2List(jobunit);

			// old job
			List<JobMstEntity> oldEntityList =
					em.createNamedQuery("JobMstEntity.findByJobunitId", JobMstEntity.class, ObjectPrivilegeMode.MODIFY)
					.setParameter("jobunitId", jobunit.getData().getJobunitId()).getResultList();

			// オブジェクト権限チェック(変更対象のリストが空だった場合)
			if(oldEntityList == null || oldEntityList.size() == 0) {
				ObjectPrivilege_InvalidRole e = new ObjectPrivilege_InvalidRole(
						"targetClass = " + JobMstEntity.class.getSimpleName());
				m_log.info("replaceJobunit() : object privilege invalid. jobunitId = "
						+ jobunit.getData().getJobunitId());

				throw e;
			}

			List<JobInfo> oldList = new ArrayList<JobInfo>();
			for (JobMstEntity oldEntity : oldEntityList) {
				JobInfo job = new JobInfo();
				job.setId(oldEntity.getId().getJobId());
				job.setJobunitId(oldEntity.getId().getJobunitId());
				job.setName(oldEntity.getJobName());
				job.setType(oldEntity.getJobType());
				job.setPropertyFull(false);
				JobInfo fullJob = FullJob.getJobFull(job);
				fullJob.setParentId(oldEntity.getParentJobId());
				oldList.add(fullJob);
			}

			// modify
			ModifyJob modify = new ModifyJob();
			String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			start = System.currentTimeMillis();
			lastUpdateTime = modify.replaceJobunit(oldList, newList, loginUser);
			m_log.debug(String.format("modify.replaceJobunit: %d ms", System.currentTimeMillis() - start));

			// 登録後のvalidate
			JobValidator.validateJobMaster();

			long end = System.currentTimeMillis();
			m_log.info("replaceJobunit " + (end - start) + "ms");
			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (JobMasterNotFound e) {
			jtm.rollback();
			throw e;
		} catch (UserNotFound e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (JobInvalid e) {
			jtm.rollback();
			throw e;
		} catch (InvalidSetting e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("replaceJobunit() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		
		return lastUpdateTime;
	}

	// JobTreeItemをList<JobInfo>に変更するメソッド
	private List<JobInfo> tree2List(JobTreeItem tree) {
		ArrayList<JobInfo> list = new ArrayList<JobInfo>();
		JobInfo info = tree.getData();
		JobTreeItem parent = tree.getParent();
		if (parent != null) {
			info.setParentId(parent.getData().getId());
		}
		list.add(info);
		for (JobTreeItem child : tree.getChildren()) {
			list.addAll(tree2List(child));
		}
		return list;
	}

	/**
	 * ジョブユニット単位でジョブツリー情報を登録する。登録のみで置換ではないため、既に登録済みの定義と重複がある場合は例外が発生する。<BR>
	 *
	 * @param jobunit
	 * @param validCheck
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws JobInvalid
	 * @throws InvalidSetting
	 * @throws JobMasterDuplicate
	 * @throws InvalidRole
	 */
	private Long registerNewJobunit(JobTreeItem jobunit)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidSetting, JobMasterDuplicate, InvalidRole {
		m_log.debug("registerNewJobunit() : jobunitId = " + jobunit.getData().getJobunitId());

		JpaTransactionManager jtm = null;
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		Long lastUpdateTime = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();

			// 登録
			ModifyJob modify = new ModifyJob();
			lastUpdateTime = modify.registerJobunit(jobunit, loginUser);

			String jobunitId = jobunit.getData().getJobunitId();
			JobEditEntity jobEditEntity = em.find(JobEditEntity.class, jobunitId, ObjectPrivilegeMode.READ);
			if (jobEditEntity == null) {
				// ジョブユニットの新規作成
				jobEditEntity = new JobEditEntity(jobunitId);
			}

			// FullJobのキャッシュを削除する。
			// 一応実施。
			FullJob.removeCache(jobunitId);

			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (JobMasterNotFound e) {
			jtm.rollback();
			throw e;
		} catch (JobMasterDuplicate e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (JobInvalid e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("registerNewJobunit() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		
		return lastUpdateTime;
	}

	/**
	 * ジョブユニット単位でジョブツリー情報を削除する。<BR>
	 *
	 *
	 * @param jobunitId
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws InvalidSetting
	 * @throws JobInvalid
	 * @throws InvalidRole 
	 */
	public void deleteJobunit(String jobunitId) throws HinemosUnknown, JobMasterNotFound, InvalidSetting, JobInvalid, InvalidRole {
		m_log.debug("deleteJobunit() : jobunitId = " + jobunitId);
		deleteJobunit(jobunitId, true);
	}

	/**
	 * ジョブユニット単位でジョブツリー情報を削除する。<BR>
	 *
	 *
	 * @param jobunitId
	 * @param validCheck
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws InvalidSetting
	 * @throws JobInvalid
	 * @throws InvalidRole 
	 */
	public void deleteJobunit(String jobunitId, boolean refCheck) throws HinemosUnknown, JobMasterNotFound, InvalidSetting, JobInvalid, InvalidRole {
		m_log.debug("deleteJobunit() : jobunitId = " + jobunitId + ", refCheck = " + refCheck);

		JpaTransactionManager jtm = null;
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 削除
			ModifyJob modify = new ModifyJob();
			modify.deleteJobunit(jobunitId, loginUser);

			// 登録後のvalidate
			if(refCheck){
				JobValidator.validateJobMaster();
			}

			// キャッシュの削除
			FullJob.removeCache(jobunitId);
			
			//編集ロックの削除
			releaseEditLockAfterJobEdit(jobunitId);

			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (JobMasterNotFound e) {
			jtm.rollback();
			throw e;
		} catch (InvalidSetting e) {
			jtm.rollback();
			throw e;
		} catch (JobInvalid e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteJobunit() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	/**
	 * ジョブ操作開始用プロパティを返します。<BR>
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param facilityId ファシリティID
	 * @param locale ロケール情報
	 * @return ジョブ操作開始用プロパティ
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.jobmanagement.factory.JobOperationProperty#getStartProperty(String, String, String, String, Locale)
	 */
	public ArrayList<String> getAvailableStartOperation(String sessionId, String jobunitId, String jobId, String facilityId, Locale locale) throws InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = null;
		ArrayList<String> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			JobOperationProperty jobProperty = new JobOperationProperty();
			list = jobProperty.getAvailableStartOperation(sessionId, jobunitId, jobId, facilityId, locale);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getAvailableStartOperation() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return list;
	}

	/**
	 * ジョブ操作停止用プロパティを返します。<BR>
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param facilityId ファシリティID
	 * @param locale ロケール情報
	 * @return ジョブ操作停止用プロパティ
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.jobmanagement.factory.JobOperationProperty#getStopProperty(String, String, String, String, Locale)
	 */
	public ArrayList<String> getAvailableStopOperation(String sessionId, String jobunitId,  String jobId, String facilityId, Locale locale) throws InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = null;
		ArrayList<String> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			JobOperationProperty jobProperty = new JobOperationProperty();
			list = jobProperty.getAvailableStopOperation(sessionId, jobunitId, jobId, facilityId, locale);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getAvailableStopOperation() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return list;
	}

	/**
	 * ジョブを実行します。<BR>
	 *
	 * @param jobunitId
	 * @param jobId ジョブID
	 * @param triggerInfo 実行契機情報
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws JobInfoNotFound
	 * @throws JobSessionDuplicate
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	public String runJob(String jobunitId, String jobId, JobTriggerInfo triggerInfo) throws FacilityNotFound, HinemosUnknown, JobMasterNotFound, JobInfoNotFound, JobSessionDuplicate, InvalidRole, InvalidSetting {
		m_log.debug("runJob(jobunitId,jobId,triggerInfo) : jobId=" + jobId);
		return runJob(jobunitId, jobId, null, triggerInfo);
	}

	/**
	 * ジョブを実行します。<BR>
	 *
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param info ログ出力情報
	 * @param triggerInfo 実行契機情報
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws JobMasterNotFound
	 * @throws JobSessionDuplicate
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	public String runJob(String jobunitId, String jobId, OutputBasicInfo info, JobTriggerInfo triggerInfo)
			throws FacilityNotFound, HinemosUnknown, JobInfoNotFound, JobMasterNotFound, JobSessionDuplicate, InvalidRole, InvalidSetting {
		m_log.debug("runJob(jobunitId,jobId,info,triggerInfo) : jobId=" + jobId);

		if (info == null) {
			// triggerInfo の実行契機種別が「TRIGGER_MANUAL」の場合、ユーザ名を登録
			if (triggerInfo.getTrigger_type() == JobTriggerTypeConstant.TYPE_MANUAL) {
				String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
				m_log.info("runJob(jobunitId,jobId,info,triggerInfo) : pri.getName()=" + loginUser);
				triggerInfo.setTrigger_info(loginUser);
			}
		}

		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin(true);

			// テスト実行時のオブジェクト権限チェック
			if (triggerInfo.getJobWaitTime() || triggerInfo.getJobWaitMinute() || triggerInfo.getJobCommand()) {
				QueryUtil.getJobMstPK(jobunitId, jobId);
			}

			// 起動コマンド
			if (triggerInfo.getJobCommand()) {
				String startCommand = triggerInfo.getJobCommandText();
				CommonValidator.validateString(Messages.getString("起動コマンドを置換する文字"), startCommand, true, 1, 1024);
			}

			// ジョブの実行権限チェック
			QueryUtil.getJobMstPK(new JobMstEntityPK(jobunitId,  jobunitId), ObjectPrivilegeMode.EXEC);

			jtm.commit();
		} catch (HinemosUnknown e) {
			AplLogger apllog = new AplLogger("JOB", "job");
			String[] args = {jobId};
			apllog.put("SYS", "003", args);
			jtm.rollback();
			throw e;
		} catch (JobMasterNotFound e) {
			AplLogger apllog = new AplLogger("JOB", "job");
			String[] args = {jobId};
			apllog.put("SYS", "003", args);
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (InvalidSetting e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("runJob() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			AplLogger apllog = new AplLogger("JOB", "job");
			String[] args = {jobId};
			apllog.put("SYS", "003", args);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		
		return CreateJobSession.makeSession(jobunitId, jobId, info, triggerInfo);
	}

	/**
	 * ジョブをスケジュール実行します。<BR>
	 * Quartzからスケジュール実行時に呼び出されます。<BR>
	 *
	 * トランザクション開始はユーザが制御する。
	 * また、追加実装により、トランザクションの入れ子が予期せず生じることを避けるため、Neverを採用する。
	 *
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param calendarId カレンダID
	 * @param triggerInfo 実行契機情報
	 * @throws FacilityNotFound
	 * @throws CalendarNotFound
	 * @throws JobMasterNotFound
	 * @throws JobInfoNotFound
	 * @throws HinemosUnknown
	 * @throws JobSessionDuplicate
	 * @throws InvalidRole
	 * @see com.clustercontrol.calendar.ejb.session.CalendarControllerBean#isRun(String, Date)}
	 * @see com.clustercontrol.jobmanagement.session.JobControllerBean#runJob(String, String, JobTriggerInfo)
	 */
	public void scheduleRunJob(String jobunitId, String jobId, String calendarId, JobTriggerInfo triggerInfo)
			throws FacilityNotFound, CalendarNotFound, JobMasterNotFound, JobInfoNotFound, HinemosUnknown, JobSessionDuplicate, InvalidRole {
		m_log.debug("scheduleRunJob() : jobId=" + jobId + ", calendarId=" + calendarId);


		boolean failure = true;

		try {
			//カレンダをチェック
			boolean check = false;
			if(calendarId != null && calendarId.length() > 0){
				//カレンダによる実行可/不可のチェック
				if(new CalendarControllerBean().isRun(calendarId, new Date().getTime())){
					check = true;
				}
			}else{
				check = true;
			}

			if(!check) {
				failure = false;
				return;
			}
			//ジョブ実行
			runJob(jobunitId, jobId, triggerInfo);
			failure = false;

		} catch (FacilityNotFound e) {
			throw e;
		} catch (CalendarNotFound e) {
			throw e;
		} catch (JobMasterNotFound e) {
			throw e;
		} catch (JobInfoNotFound e) {
			throw e;
		} catch (JobSessionDuplicate e) {
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("scheduleRunJob() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (failure) {
				AplLogger apllog = new AplLogger(HinemosModuleConstant.JOB, "job");
				String[] args = { jobId, triggerInfo.getTrigger_info() };
				apllog.put("SYS", "016", args);
			}
		}
	}

	/**
	 * ジョブ操作を行います。<BR>
	 *
	 * @param property ジョブ操作用プロパティ
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @see com.clustercontrol.jobmanagement.session.JobRunManagementBean#operationJob(Property)
	 */
	public void operationJob(JobOperationInfo property) throws HinemosUnknown, JobInfoNotFound, InvalidRole {
		m_log.debug("operationJob()");

		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			
			ILock lock = JobRunManagementBean.getLock(property.getSessionId());
			try {
				lock.writeLock();
				
				jtm.begin();
				
				try {
					// ジョブの実行権限チェック
					QueryUtil.getJobMstPK(new JobMstEntityPK(property.getJobunitId(), property.getJobunitId()), ObjectPrivilegeMode.EXEC);
				} catch (JobMasterNotFound e) {

					/*
					 * ジョブのマスタが存在しない場合
					 * 特権ロールかオーナーロールに所属するユーザのみ実行可能とする
					 */
					// 操作ユーザの所属ロールにオーナーロールが存在するかを確認
					JobSessionJobEntity entity = QueryUtil.getJobSessionJobPK(property.getSessionId(), property.getJobunitId(), property.getJobunitId());

					// 特権ロールに所属しているかを確認
					Boolean isAdministrator = (Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR);
					if (!isAdministrator) {
						/*
						 *  特権ロールに所属していない場合
						 */

						// ログインユーザから、所属ロールの一覧を取得
						String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
						// ユーザ情報が取得できない場合は、InvalidRole
						if (loginUser == null || "".equals(loginUser.trim())) {
							String message = "operationJob() : login user not get, session id = " + property.getSessionId();
							m_log.warn(message);
							throw new InvalidRole(message);
						}

						List<String> roleIdList = UserRoleCache.getRoleIdList(loginUser);

						boolean existsflg = false;
						for (String roleId : roleIdList) {
							m_log.debug("operationJob() :  userRoleId = " + roleId);
							if (roleId.equals(entity.getOwnerRoleId())) {
								existsflg = true;
								break;
							}
						}
						if (! existsflg) {
							// オーナーロールとして存在しない場合
							String message = "operationJob() : not owner role, session id = " + property.getSessionId();
							m_log.warn(message);
							throw new InvalidRole(message);
						}
					}
				}

				new JobRunManagementBean().operation(property);
				jtm.commit();
			} finally {
				lock.writeUnlock();
			}
			// ジョブの状態が変化した後に、runUnendSessionを実行してもらいたいので、
			// forceCheckのフラグを立てる。
			JobSessionJobImpl.addForceCheck(property.getSessionId());
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (JobInfoNotFound e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
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
	 * ジョブ履歴一覧情報を返します。<BR>
	 *
	 * @param property 履歴フィルタ用プロパティ
	 * @param histories 表示履歴数
	 * @return ジョブ履歴一覧情報（Objectの2次元配列）
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getHistoryList(Property, int)
	 */
	public JobHistoryList getJobHistoryList(JobHistoryFilter property, int histories) throws JobInfoNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("getHistoryList()");

		JpaTransactionManager jtm = null;
		String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		JobHistoryList list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			SelectJob select = new SelectJob();
			list = select.getHistoryList(userId, property, histories);

			jtm.commit();
		} catch (JobInfoNotFound e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getHistoryList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		return list;
	}

	/**
	 * ジョブ詳細一覧情報を返します。<BR>
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @return ジョブ詳細一覧情報（Objectの2次元配列）
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getDetailList(String)
	 */
	public JobTreeItem getJobDetailList(String sessionId) throws JobInfoNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("getDetailList() : sessionId=" + sessionId);

		JpaTransactionManager jtm = null;
		JobTreeItem item = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			SelectJob select = new SelectJob();
			item = select.getDetailList(sessionId);

			jtm.commit();
		} catch (JobInfoNotFound e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getDetailList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		removeParent(item);
		return item;
	}

	/**
	 * ノード詳細一覧情報を返します。<BR>
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param locale ロケール情報
	 * @return ノード詳細一覧情報（Objectの2次元配列）
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getNodeDetailList(String, String, String, Locale)
	 */
	public ArrayList<JobNodeDetail> getNodeDetailList(String sessionId, String jobunitId, String jobId, Locale locale) throws JobInfoNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("getNodeDetailList() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		JpaTransactionManager jtm = null;
		ArrayList<JobNodeDetail> list;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectJob select = new SelectJob();
			list = select.getNodeDetailList(sessionId, jobunitId, jobId, locale);
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
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getNodeDetailList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		return list;
	}

	/**
	 * ファイル転送一覧情報を返します。<BR>
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @return ファイル転送一覧情報（Objectの2次元配列）
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getForwardFileList(String, String)
	 */
	public ArrayList<JobForwardFile> getForwardFileList(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("getForwardFileList() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		JpaTransactionManager jtm = null;
		ArrayList<JobForwardFile> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectJob select = new SelectJob();
			list = select.getForwardFileList(sessionId, jobunitId, jobId);
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
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getForwardFileList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		return list;
	}

	/**
	 * ジョブ[実行契機]スケジュール情報を登録します。<BR>
	 *
	 * @param info ジョブ[実行契機]スケジュール情報
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws JobKickDuplicate
	 * @throws InvalidRole
	 *
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#addSchedule(JobSchedule, String)
	 */
	public void addSchedule(JobSchedule info) throws HinemosUnknown, InvalidSetting, JobKickDuplicate, InvalidRole {
		m_log.debug("addSchedule() : scheduleId=" + info.getId() + ",  Schedule = " + info + ", ScheduleType = " + info.getScheduleType() );

		m_log.debug("addSchedule() : CreateTime=" + info.getCreateTime() + ",  UpdateTime = " + info.getUpdateTime());

		JpaTransactionManager jtm = null;
		// 新規登録ユーザ、最終変更ユーザを設定
		String loginUser =(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		// DBにスケジュール情報を保存
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			// 入力チェック
			JobValidator.validateJobSchedule(info);
			ModifyJobKick modify = new ModifyJobKick();
			modify.addSchedule(info, loginUser);
			jtm.commit();
		} catch (InvalidSetting e) {
			jtm.rollback();
			throw e;
		} catch (JobKickDuplicate e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("addSchedule() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	/**
	 * ジョブ[実行契機]ファイルチェック情報を登録します。<BR>
	 *
	 * @param info ジョブ[実行契機]ファイルチェック情報
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws JobKickDuplicate
	 * @throws InvalidRole
	 *
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#addSchedule(JobFileCheck, String)
	 */
	public void addFileCheck(JobFileCheck info) throws HinemosUnknown, InvalidSetting, JobKickDuplicate, InvalidRole {
		m_log.debug("addFileCheck() : scheduleId=" + info.getId() + ",  FacilityID = " + info.getFacilityId()
				+ ", FilePath = " + info.getDirectory() + ",  EventType = " + info.getEventType());

		m_log.debug("addFileCheck() : CreateTime=" + info.getCreateTime() + ",  UpdateTime = " + info.getUpdateTime());
		JpaTransactionManager jtm = null;
		// 新規登録ユーザ、最終変更ユーザを設定
		String loginUser =(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		// DBにスケジュール情報を保存
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			// 入力チェック
			JobValidator.validateJobFileCheck(info);
			ModifyJobKick modify = new ModifyJobKick();
			modify.addFileCheck(info, loginUser);
			jtm.commit();
			clearJobKickCache();
			SendTopic.putFileCheck(null);
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidSetting e) {
			jtm.rollback();
			throw e;
		} catch (JobKickDuplicate e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("addFileCheck() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}
	/**
	 * ジョブ[実行契機]スケジュール情報を変更します。<BR>
	 *
	 * @param info ジョブ[実行契機]スケジュール情報
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 *
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#modifySchedule(JobSchedule, String)
	 */
	public void modifySchedule(JobSchedule info) throws HinemosUnknown, InvalidSetting, JobInfoNotFound, InvalidRole {
		m_log.debug("modifySchedule() : scheduleId=" + info.getId());
		JpaTransactionManager jtm = null;
		// 最終変更ユーザを設定
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		// DBにスケジュール情報を保存
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			// 入力チェック
			JobValidator.validateJobSchedule(info);
			ModifyJobKick modify = new ModifyJobKick();
			modify.modifySchedule(info, loginUser);
			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidSetting e) {
			jtm.rollback();
			throw e;
		} catch (JobInfoNotFound e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("modifySchedule() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	/**
	 * ジョブ[実行契機]ファイルチェック情報を変更します。<BR>
	 *
	 * @param info ジョブ[実行契機]ファイルチェック情報
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 *
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#modifySchedule(JobSchedule, String)
	 */
	public void modifyFileCheck(JobFileCheck info) throws HinemosUnknown, InvalidSetting, JobInfoNotFound, InvalidRole {
		m_log.debug("modifyFileCheck() : scheduleId=" + info.getId());
		JpaTransactionManager jtm = null;
		// 最終変更ユーザを設定
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		// DBにスケジュール情報を保存
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			// 入力チェック
			JobValidator.validateJobFileCheck(info);
			ModifyJobKick modify = new ModifyJobKick();
			modify.modifyFileCheck(info, loginUser);
			jtm.commit();
			clearJobKickCache();
			SendTopic.putFileCheck(null);
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidSetting e) {
			jtm.rollback();
			throw e;
		} catch (JobInfoNotFound e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("modifyFileCheck() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	/**
	 * ジョブ[実行契機]スケジュール情報を削除します。<BR>
	 *
	 * @param scheduleIdList スケジュールIDリスト
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 *
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#deleteSchedule(String)
	 */
	public void deleteSchedule(List<String> scheduleIdList) throws HinemosUnknown, JobInfoNotFound, InvalidRole {
		m_log.debug("deleteSchedule() : scheduleId=" + scheduleIdList);

		JpaTransactionManager jtm = null;

		// DBのスケジュール情報を削除
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for(String scheduleId : scheduleIdList) {
				ModifyJobKick modify = new ModifyJobKick();
				modify.deleteSchedule(scheduleId);
			}
			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (JobInfoNotFound e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("deleteSchedule() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	/**
	 *  ジョブ[実行契機]ファイルチェック情報を削除します。<BR>
	 *
	 * @param scheduleIdList スケジュールIDリスト
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 *
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#deleteSchedule(String)
	 */
	public void deleteFileCheck(List<String> scheduleIdList) throws HinemosUnknown, JobInfoNotFound, InvalidRole {
		m_log.debug("deleteFileCheck() : scheduleId=" + scheduleIdList);

		JpaTransactionManager jtm = null;
		// DBのファイルチェック情報を削除
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			for(String scheduleId : scheduleIdList) {
				ModifyJobKick modify = new ModifyJobKick();
				modify.deleteFileCheck(scheduleId);
			}
			jtm.commit();
			clearJobKickCache();
			SendTopic.putFileCheck(null);
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (JobInfoNotFound e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("deleteFileCheck() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	/**
	 * ジョブ[実行契機]一覧情報を返します。<BR>
	 *
	 * @return ジョブ[実行契機]一覧情報
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @see com.clustercontrol.jobmanagement.factory.SelectJobKick#getJobKickList()
	 */
	public ArrayList<JobKick> getJobKickList() throws JobMasterNotFound, HinemosUnknown, InvalidRole  {
		m_log.debug("getJobKickList()");

		JpaTransactionManager jtm = null;
		ArrayList<JobKick> list;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			SelectJobKick select = new SelectJobKick();
			list = select.getJobKickList((String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));
			jtm.commit();
		} catch (JobMasterNotFound e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getJobKickList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return list;
	}
	/**
	 * 実行契機IDと一致するジョブスケジュール情報を返します
	 * @param jobKickId
	 * @return
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public JobSchedule getJobSchedule(String jobKickId) throws JobMasterNotFound, InvalidRole, HinemosUnknown{
		m_log.info("getJobSchedule()");

		JpaTransactionManager jtm = null;
		JobSchedule jobSchedule;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			m_log.info("getJobSchedule JobKickID:" + jobKickId);
			SelectJobKick select = new SelectJobKick();
			jobSchedule = select.getJobSchedule(jobKickId);
			jtm.commit();
		} catch (JobMasterNotFound e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getJobSchedule() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return jobSchedule;
	}
	/**
	 * 実行契機IDと一致するジョブファイルチェック情報を返します
	 * @param jobKickId
	 * @return
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public JobFileCheck getJobFileCheck(String jobKickId) throws JobMasterNotFound, InvalidRole, HinemosUnknown{
		m_log.debug("getJobFileCheck()");

		JpaTransactionManager jtm = null;
		JobFileCheck jobFileCheck;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			SelectJobKick select = new SelectJobKick();
			jobFileCheck = select.getJobFileCheck(jobKickId);
			jtm.commit();
		} catch (JobMasterNotFound e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getJobFileCheck() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return jobFileCheck;
	}

	private static final ILock _lock;
	
	@SuppressWarnings("unchecked")
	private static ArrayList<JobKick> getJobKickCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_JOB_KICK);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_JOB_KICK + " : " + cache);
		return cache == null ? null : (ArrayList<JobKick>)cache;
	}
	
	private static void storeJobKickCache(ArrayList<JobKick> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_JOB_KICK + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_JOB_KICK, newCache);
	}
	
	private static void removeJobKickCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("remove cache " + AbstractCacheManager.KEY_JOB_KICK);
		cm.remove(AbstractCacheManager.KEY_JOB_KICK);
	}
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(JobControllerBean.class.getName());
		
		try {
			_lock.writeLock();
			
			List<JobKick> jobKickCache = getJobKickCache();
			if (jobKickCache == null) {	// not null when clustered
				removeJobKickCache();
			}
		} finally {
			_lock.writeUnlock();
		}
	}

	public static void clearJobKickCache () {
		m_log.info("clearJobKickCache()");
		
		try {
			_lock.writeLock();
			
			removeJobKickCache();
		} finally {
			_lock.writeUnlock();
		}
	}
	
	/**
	 *
	 * <注意！> このメソッドはAgentユーザ以外で呼び出さないこと！
	 * <注意！> キャッシュの都合上、Agentユーザ以外から呼び出すと、正常に動作しません。
	 *
	 * ファシリティIDが一致するジョブファイルチェック情報を返します。
	 * Hinemosエージェントで利用します。
	 * @param facilityIdList
	 * @return
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<JobFileCheck> getJobFileCheck(ArrayList<String> facilityIdList)
			throws JobMasterNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("getJobFileCheck for Agent");

		ArrayList<JobFileCheck> ret = new ArrayList<JobFileCheck>();
		JobControllerBean bean = new JobControllerBean();

		ArrayList<JobKick> jobKickList = null;
		try {
			_lock.readLock();
			
			List<JobKick> jobKickCache = getJobKickCache();
			if (jobKickCache != null) {
				jobKickList = new ArrayList<JobKick>(jobKickCache);
			}
		} finally {
			_lock.readUnlock();
		}
		
		if (jobKickList == null) {
			try {
				_lock.writeLock();
				
				jobKickList = bean.getJobKickList();
				storeJobKickCache(jobKickList);
			} finally {
				_lock.writeUnlock();
			}
		}

		for (JobKick jobKick : jobKickList) {
			// タイプがスケジュールではなく、ファイルチェックであることを確認。
			int type = jobKick.getType();
			if (type != JobTriggerTypeConstant.TYPE_FILECHECK) {
				continue;
			}
			JobFileCheck jobFileCheck = new JobControllerBean().getJobFileCheck(jobKick.getId());

			// ファイルチェック対象のファシリティIDであることを確認。
			boolean flag = false;
			for (String facilityId : facilityIdList) {
				if(new RepositoryControllerBean().containsFaciliyId(jobFileCheck.getFacilityId(), facilityId, jobFileCheck.getOwnerRoleId())) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				continue;
			}

			// カレンダを作成
			String calendarId = jobFileCheck.getCalendarId();
			CalendarInfo calendarInfo = null;
			try {
				calendarInfo = new CalendarControllerBean().getCalendarFull(calendarId);
			} catch (CalendarNotFound e) {
				m_log.warn("CalendarNotFound " + e.getMessage() + " id=" + calendarId);
			}
			jobFileCheck.setCalendarInfo(calendarInfo);
			ret.add(jobFileCheck);
		}

		return ret;
	}

	/**
	 * 指定されたジョブ[実行契機]スケジュールまたは、ファイルチェックの有効/無効を変更します
	 *
	 * @param scheduleId
	 * @param validFlag
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws JobMasterNotFound
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	public void setJobKickStatus(String scheduleId, boolean validFlag) throws HinemosUnknown, InvalidSetting, JobMasterNotFound, JobInfoNotFound, InvalidRole {
		m_log.info("setJobKickStatus() scheduleId = " + scheduleId + ", validFlag = " + validFlag);

		JpaTransactionManager jtm = null;

		// null check
		if(scheduleId == null || "".equals(scheduleId)){
			HinemosUnknown e = new HinemosUnknown("target scheduleId is null or empty.");
			m_log.info("setJobKickStatus() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			SelectJobKick select = new SelectJobKick();
			//ジョブ[実行契機]一覧からIDと一致するスケジュール情報または、ファイルチェック情報を取得する
			JobKick jobKick = select.getJobKick(scheduleId);
			//有効無効切り替え
			if(validFlag){
				jobKick.setValid(ValidConstant.TYPE_VALID);
			}
			else{
				jobKick.setValid(ValidConstant.TYPE_INVALID);
			}
			//スケジュールの場合
			if(jobKick instanceof JobSchedule){
				JobSchedule jobSchedule = (JobSchedule) jobKick;
				m_log.info("setJobKickStatus() JobSchedule Change Valid : "
						+ " scheduleId= " + scheduleId
						+ " valid=" + jobSchedule.getValid());
				modifySchedule(jobSchedule);
			}
			//ファイルチェックの場合
			else if (jobKick instanceof JobFileCheck){
				JobFileCheck jobFileCheck = (JobFileCheck) jobKick;
				m_log.info("setJobKickStatus() JobFileCheck Change Valid : "
						+ " scheduleId= " + scheduleId
						+ " valid=" + jobFileCheck.getValid());
				modifyFileCheck(jobFileCheck);
			} else {
				m_log.warn("unknown type " + jobKick.getType());
			}
			jtm.commit();

			if (jobKick instanceof JobFileCheck) {
				clearJobKickCache();
				SendTopic.putFileCheck(null);
			}
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (JobMasterNotFound e) {
			jtm.rollback();
			throw e;
		} catch (JobInfoNotFound e) {
			jtm.rollback();
			throw e;
		} catch (InvalidSetting e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("setJobKickStatus() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	/**
	 * セッションジョブ情報を返します。<BR>
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @return ジョブツリー情報{@link com.clustercontrol.jobmanagement.bean.JobTreeItem}
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getSessionJobInfo(String, String, String)
	 */
	public JobTreeItem getSessionJobInfo(String sessionId, String jobunitId, String jobId) throws HinemosUnknown, JobInfoNotFound, InvalidRole {
		m_log.debug("getSessionJobInfo() : sessionId=" + sessionId + ", jobId=" + jobId);

		JpaTransactionManager jtm = null;
		JobTreeItem item = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			SelectJob select = new SelectJob();
			item = select.getSessionJobInfo(sessionId, jobunitId, jobId);
			item.getData().setPropertyFull(true);
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
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getSessionJobInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		return item;
	}

	/**
	 * ジョブ[スケジュール予定]の一覧情報を返します。<BR>
	 *
	 * @return ジョブ[スケジュール予定]の一覧情報
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @see com.clustercontrol.jobmanagement.factory.SelectJobKick#getPlanList(JobPlanFilter,int)
	 */
	public ArrayList<JobPlan> getPlanList(JobPlanFilter filter, int plans) throws JobMasterNotFound, InvalidRole, HinemosUnknown  {
		m_log.debug("getPlanList()");

		JpaTransactionManager jtm = null;
		ArrayList<JobPlan> list ;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectJobKick select = new SelectJobKick();
			list = select.getPlanList((String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),filter,plans);
			jtm.commit();
		} catch (JobMasterNotFound e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getPlanList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return list;
	}

	/**
	 * ファシリティIDが使用されているかチェックします。<BR>
	 * <P>
	 * 使用されていたら、UsedFacility がスローされる。<BR>
	 *
	 * @param facilityId ファシリティ
	 * @throws UsedFacility
	 *
	 * @see com.clustercontrol.commons.session.CheckFacility#isUseFacilityId(java.lang.String)
	 * @see com.clustercontrol.bean.PluginConstant;
	 */
	@Override
	public void isUseFacilityId(String facilityId) throws UsedFacility {

		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();

			Collection<JobMstEntity> ct = null;

			// ファシリティIDが使用されているジョブコマンドマスタを取得する。
			ct = em.createNamedQuery("JobMstEntity.findByFacilityId", JobMstEntity.class)
					.setParameter("facilityId", facilityId)
					.getResultList();
			if(ct != null && ct.size() > 0) {
				UsedFacility e = new UsedFacility(PluginConstant.TYPE_JOBMANAGEMENT);
				m_log.info("isUseFacilityId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			jtm.commit();
		} catch (UsedFacility e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("isUseFacilityId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
		} finally {
			jtm.close();
		}
	}

	/**
	 * ジョブユニットIDをすべて更新する。<BR>
	 *
	 * @param item ジョブツリーアイテム
	 * @param jobunitID ジョブユニットID
	 */
	private void setJobunitIdAll(JobTreeItem item, String jobunitId) {
		if (item == null || item.getData() == null) {
			return;
		}

		if (item.getData().getType() == JobConstant.TYPE_JOBUNIT) {
			jobunitId = item.getData().getJobunitId();
		}

		m_log.trace("setJobunitIdAll() : jobId = " + item.getData().getId() +
				", old jobunitId = " + item.getData().getJobunitId() +
				", new jobunitId = " + jobunitId);
		item.getData().setJobunitId(jobunitId);
		for (JobTreeItem child : item.getChildren()) {
			setJobunitIdAll(child, jobunitId);
		}
	}

	/**
	 * ジョブユニットの最終更新日時を取得する<BR>
	 *
	 * @return HashMap<String, Long>
	 *
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<Long> getUpdateTime(List<String> jobunitIdList) throws InvalidRole, HinemosUnknown {
		return getUpdateDateFromJobMstList(jobunitIdList);
	}

	/**
	 * 編集ロックを取得する<BR>
	 *
	 * @param jobunitId
	 * @param updateTime
	 * @param forceFlag
	 * @return
	 * @throws HinemosUnknown
	 * @throws OtherUserGetLock
	 * @throws JobInvalid
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	public Integer getEditLock(String jobunitId, Long updateTime, boolean forceFlag, String userName, String ipAddress) throws HinemosUnknown, OtherUserGetLock, UpdateTimeNotLatest, JobMasterNotFound, JobInvalid, InvalidRole {
		JobEditEntity entity = null;
		JpaTransactionManager jtm = null;
		Integer editSession = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();

			// ジョブマスタの最終更新日時を取得する
			List<String> jobunitIdList = new ArrayList<String>();
			jobunitIdList.add(jobunitId);
			Long mstUpdateTime = null;
			
			if (getUpdateDateFromJobMstList(jobunitIdList).size() > 0) {
				mstUpdateTime = getUpdateDateFromJobMstList(jobunitIdList).get(0);
			}

			if (mstUpdateTime != null && !mstUpdateTime.equals(updateTime)) {
				// 更新日時がずれている
				m_log.warn("getEditLock() : update time is not latest, jobunitId=" + jobunitId
						+ ", manager=" + mstUpdateTime + ", client=" + updateTime);
				String mstUpdateTimeStr = null;
				if (mstUpdateTime != null) {
					mstUpdateTimeStr = new Timestamp(mstUpdateTime).toString();
				}
				String updateTimeStr = null;
				if (updateTime != null) {
					updateTimeStr = new Timestamp(updateTime).toString();
				}
				throw new UpdateTimeNotLatest(Messages.getString("message.job.102",
						new String[]{mstUpdateTimeStr, updateTimeStr}));
			} else if (mstUpdateTime == null && updateTime != null){
				// マスタ側を削除済みでクライアント側に残骸が残っている
				m_log.warn("getEditLock() : update time is not latest, jobunitId=" + jobunitId
						+ ", manager=" + mstUpdateTime + ", client=" + updateTime);
				throw new UpdateTimeNotLatest(Messages.getString("message.job.116"));
			}

			// ロックを取得できるかどうか確認する
			entity = em.find(JobEditEntity.class, jobunitId, ObjectPrivilegeMode.READ);
			if (entity == null) {
				// ロックを新規作成する
				entity = new JobEditEntity(jobunitId);
			}

			if (entity.getEditSession() != null) {
				// 別のユーザがロックを取得している
				m_log.debug("getEditLock() : other user gets EditLock, jobunitId=" + jobunitId
						+ ", username=" + entity.getLockUser() + ", ipaddr=" + entity.getLockIpAddress());

				if (forceFlag) {
					// 強制的に取得する場合
					m_log.debug("getEditLock() : get EditLock forcely, jobunitId=" + jobunitId);
				} else {
					// 別のユーザがロックを取得していることを通知する
					String message = Messages.getString("message.job.101",
							new String[]{jobunitId, entity.getLockUser(), entity.getLockIpAddress()}) +
							"\n" +
							Messages.getString("message.job.106");
					OtherUserGetLock e = new OtherUserGetLock(message);
					m_log.info("getEditLock() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}

			// ロックを取得する
			entity.setLockIpAddress(ipAddress);
			entity.setLockUser(userName);
			editSession = random.nextInt();
			entity.setEditSession(editSession);
			m_log.info("getEditLock() : get edit lock(jobunitid="+ jobunitId + ", user=" + userName + ", ipAddress=" + ipAddress + ", editSession=" + editSession +")");

			jtm.commit();
		} catch (OtherUserGetLock e) {
			jtm.rollback();
			throw e;
		} catch (UpdateTimeNotLatest e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getEditLock() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return editSession;
	}

	/**
	 * 編集ロックの正当性を確認する<BR>
	 *
	 * @param jobunitId
	 * @param updateTime
	 * @param userName
	 * @param ipAddress
	 * @return
	 * @throws HinemosUnknown
	 * @throws JobInvalid
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	public void checkEditLock(String jobunitId, Integer editSession) throws HinemosUnknown, OtherUserGetLock,  JobMasterNotFound, JobInvalid, InvalidRole  {
		JobEditEntity entity = null;
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();

			// ロックを取得できるかどうか確認する
			List<JobEditEntity> list = em.createNamedQuery("JobEditEntity.findByEditSession", JobEditEntity.class)
					.setParameter("editSession", editSession)
					.getResultList();
			if (list == null || list.size() == 0) {
				m_log.warn("checkEditLock() : editLock is null, jobunitId="+jobunitId);
				throw new OtherUserGetLock(Messages.getString("message.job.105", new String[]{jobunitId}));
			}
			entity = list.get(0);
			if (entity == null) {
				m_log.warn("checkEditLock() : editLock is invalid, editSession="+editSession);
			}

			// 編集ロックを取得している
			m_log.debug("checkEditLock() : editLock is valid, jobunitId="+jobunitId+",db editSession="+entity.getEditSession()+",user editSession="+editSession);
			jtm.commit();
		} catch (OtherUserGetLock e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("checkEditLock() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	/**
	 * 編集ロックを開放する<BR>
	 *
	 * @param jobunitId
	 * @param updateTime
	 * @param userName
	 * @param ipAddress
	 * @throws HinemosUnknown
	 */
	public void releaseEditLock(Integer editSession, String userName, String ipAddress) throws HinemosUnknown {
		JobEditEntity entity = null;
		JpaTransactionManager jtm = null;

		m_log.debug("releaseEditLock() : editSession="+editSession);

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();

			// ロックを開放できるかどうか確認する
			List<JobEditEntity> list = em.createNamedQuery("JobEditEntity.findByEditSession", JobEditEntity.class)
					.setParameter("editSession", editSession)
					.getResultList();
			if (list == null || list.size() == 0) {
				m_log.warn("releaseEditLock() : editLock is invalid, editSession="+editSession);
				return;
			}

			entity = list.get(0);
			if (entity == null) {
				m_log.warn("releaseEditLock() : editLock is invalid, editSession="+editSession);
				return;
			}

			m_log.debug("releaseEditLock() : editLock is valid, editSession="+editSession);

			// 編集ロックを開放する
			entity.setLockUser("");
			entity.setLockIpAddress("");
			entity.setEditSession(null);

			m_log.info("releaseEditLock() : release edit lock(jobunitid="+ entity.getJobunitId() +
					", user=" + userName + ", ipAddress=" + ipAddress + ", editSession=" + editSession +")");

			jtm.commit();
		} catch (Exception e) {
			m_log.warn("releaseEditLock() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}
	
	/*
	 * ジョブユニット登録もしくは削除後に編集ロックを解除する
	 * それ以外では呼ばないこと
	 */
	private void releaseEditLockAfterJobEdit(String jobunitId) throws HinemosUnknown {
		JobEditEntity entity = null;
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			// 編集ロックを解除
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(JobEditEntity.class, jobunitId, ObjectPrivilegeMode.READ);
			if (entity != null) {
				entity.setEditSession(null);
				entity.setLockIpAddress(null);
				entity.setLockUser(null);
			}
			m_log.info("registerJob() : release edit lock, jobunit = " + jobunitId);
			
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("releaseEditLock() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	private List<Long> getUpdateDateFromJobMstList(List<String> jobunitIdList) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<JobMstEntity> ct = null;
		List<Long> updateDateList = new ArrayList<Long>();

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();
			ct = em.createNamedQuery("JobMstEntity.findByJobType", JobMstEntity.class)
					.setParameter("jobType", JobConstant.TYPE_JOBUNIT)
					.getResultList();
			HashMap<String, JobMstEntity> map = new HashMap<String, JobMstEntity> ();
			for (JobMstEntity entity : ct) {
				map.put(entity.getId().getJobunitId(), entity);
			}
			for (String jobunitId : jobunitIdList) {
				Long time = null;
				if (map.get(jobunitId) != null &&
					map.get(jobunitId).getUpdateDate() != null) {
					time = map.get(jobunitId).getUpdateDate().getTime();
				}
				updateDateList.add(time);
			}
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getJobunitUpdateTime() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return updateDateList;
	}
}
