/*

Copyright (C) since 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.bean.YesNoConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobInvalid;
import com.clustercontrol.jobmanagement.bean.JobCommandInfo;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobEndStatusInfo;
import com.clustercontrol.jobmanagement.bean.JobFileCheck;
import com.clustercontrol.jobmanagement.bean.JobFileInfo;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.bean.JobObjectInfo;
import com.clustercontrol.jobmanagement.bean.JobSchedule;
import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.jobmanagement.model.JobFileCheckEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobScheduleEntity;
import com.clustercontrol.notify.bean.NotifyRelationInfo;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.util.Messages;

/**
 * ジョブ管理の入力チェッククラス
 * 
 * @since 4.0
 */
public class JobValidator {
	private static Log m_log = LogFactory.getLog( JobValidator.class );
	// ジョブの試行回数が未設定時のデフォルト値
	private static final Integer DEFAULT_JOB_RETRY_CNT = 10;

	/**
	 * ジョブスケジュールのvalidate
	 * 
	 * @param jobSchedule
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public static void validateJobSchedule(JobSchedule jobSchedule) throws InvalidSetting, HinemosUnknown, InvalidRole {

		String id = jobSchedule.getId();
		// scheduleId
		CommonValidator.validateId(Messages.getString("schedule.id"), id, 64);

		// scheduleName
		CommonValidator.validateString(Messages.getString("schedule.name"), jobSchedule.getName(), true, 1, 64);

		// ownerRoleId
		CommonValidator.validateOwnerRoleId(jobSchedule.getOwnerRoleId(), true, jobSchedule.getId(), HinemosModuleConstant.JOB_SCHEDULE);

		// jobid
		validateJobId(jobSchedule.getJobunitId(),jobSchedule.getJobId(), jobSchedule.getOwnerRoleId());

		// calenderId
		CommonValidator.validateCalenderId(jobSchedule.getCalendarId(), false, jobSchedule.getOwnerRoleId());
		/**
		 * スケジュール設定
		 */
		//p分かq分毎に繰り返し実行の場合
		if(jobSchedule.getScheduleType() == ScheduleConstant.TYPE_REPEAT){
			//「p分から」のバリデート
			Integer pMinute = jobSchedule.getFromXminutes();
			if (pMinute != null) {
				if (pMinute < 0 || 60 <= pMinute) {
					InvalidSetting e = new InvalidSetting(Messages.getString("message.job.94"));
					m_log.info("validateJobSchedule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} else {
				// 分は必須。
				InvalidSetting e = new InvalidSetting(Messages.getString("message.job.94"));
				m_log.info("validateJobSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			//「q分毎に」のバリデート
			Integer qMinute = jobSchedule.getEveryXminutes();
			if (qMinute != null) {
				if (qMinute <= 0 || 60 < qMinute || qMinute <= pMinute ||
						!(qMinute == 5 || qMinute == 10 || qMinute == 15 ||
						qMinute == 20 || qMinute == 30 || qMinute == 60)) { 
					InvalidSetting e = new InvalidSetting(Messages.getString("message.job.95"));
					m_log.info("validateJobSchedule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} else {
				// 分は必須。
				InvalidSetting e = new InvalidSetting(Messages.getString("message.job.95"));
				m_log.info("validateSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage() + qMinute);
				throw e;
			}
		}
		//上記以外の場合
		else {
			//3つに当てはまらない場合
			if (jobSchedule.getScheduleType() != ScheduleConstant.TYPE_DAY
					&& jobSchedule.getScheduleType() != ScheduleConstant.TYPE_WEEK) {
				InvalidSetting e = new InvalidSetting("unknown schedule type");
				m_log.info("validateJobSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			//曜日の場合
			if (jobSchedule.getScheduleType() == ScheduleConstant.TYPE_WEEK) {
				if (jobSchedule.getWeek() == null ||
						jobSchedule.getWeek() < 0 || 7 < jobSchedule.getWeek()) {
					InvalidSetting e = new InvalidSetting(Messages.getString("message.job.37"));
					m_log.info("validateJobSchedule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}

			/*
			 * 時のバリデート
			 * 「時」は、
			 * 「*」または、「00」 - 「48」が設定可能
			 * 「*」は「null」としてDBに格納される
			 */
			if (jobSchedule.getHour() != null) {
				if (jobSchedule.getHour() < 0 || 48 < jobSchedule.getHour()) {
					InvalidSetting e = new InvalidSetting(Messages.getString("message.job.28"));
					m_log.info("validateJobSchedule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			//分のバリデート
			if (jobSchedule.getMinute() != null) {
				if (jobSchedule.getMinute() < 0 || 60 < jobSchedule.getMinute()) {
					String[] args = {"0","59"};
					InvalidSetting e = new InvalidSetting(Messages.getString("message.hinemos.8",args));
					m_log.info("validateJobSchedule()  "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} else {
				// 分は必須。
				InvalidSetting e = new InvalidSetting(Messages.getString("message.job.29"));
				m_log.info("validateJobSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			//48:01以上は設定できない
			if (jobSchedule.getHour() != null && jobSchedule.getMinute() != null) {
				if (jobSchedule.getHour() == 48) {
					if (jobSchedule.getMinute() != 0) {
						String[] args = {"00:00","48:00"};
						InvalidSetting e = new InvalidSetting(Messages.getString("message.hinemos.8",args));
						m_log.info("validateJobSchedule()  "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
				}
			}
		}
	}

	/**
	 * 実行契機[ファイルチェック]のvalidate
	 * @param jobFileCheck
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public static void validateJobFileCheck(JobFileCheck jobFileCheck) throws InvalidSetting, HinemosUnknown, InvalidRole {
		String id = jobFileCheck.getId();
		// scheduleId
		CommonValidator.validateId(Messages.getString("schedule.id"), id, 64);
		// scheduleName
		CommonValidator.validateString(Messages.getString("schedule.name"), jobFileCheck.getName(), true, 1, 64);
		// ownerRoleId
		CommonValidator.validateOwnerRoleId(jobFileCheck.getOwnerRoleId(), true, jobFileCheck.getId(), HinemosModuleConstant.JOB_FILE_CHECK);
		// jobid
		validateJobId(jobFileCheck.getJobunitId(),jobFileCheck.getJobId(), jobFileCheck.getOwnerRoleId());
		// calenderId
		CommonValidator.validateCalenderId(jobFileCheck.getCalendarId(), false, jobFileCheck.getOwnerRoleId());

		// 実行するファシリティIDのチェック
		if(jobFileCheck.getFacilityId() == null || "".equals(jobFileCheck.getFacilityId())){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.hinemos.3"));
			m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}else{
			//ファシリティIDが正しい形式かチェック
			if(!SystemParameterConstant.isParam(
					jobFileCheck.getFacilityId(),
					SystemParameterConstant.FACILITY_ID)){
				try {
					FacilityTreeCache.validateFacilityId(jobFileCheck.getFacilityId(), jobFileCheck.getOwnerRoleId(), false);
				} catch (FacilityNotFound e) {
					InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. FacilityId = " + jobFileCheck.getFacilityId()
							+ ", JobFileCheck  = " + jobFileCheck.getId());
					m_log.info("validateJobFileCheck() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				} catch (InvalidRole e) {
					throw e;
				} catch (Exception e) {
					m_log.warn("validateJobFileCheck() add job unknown error. FacilityId = " + jobFileCheck.getFacilityId()
							+ ", JobFileCheck  = " + jobFileCheck.getId()  + " : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown("add job unknown error. FacilityId = " + jobFileCheck.getFacilityId() + ", JobFileCheck  = " + jobFileCheck.getId(), e);
				}
			}
		}

		//ディレクトリ
		if(jobFileCheck.getDirectory() == null || jobFileCheck.getDirectory().equals("")){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.job.92"));
			m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		//ファイル名
		if(jobFileCheck.getFileName() == null || jobFileCheck.getFileName().equals("")){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.job.90"));
			m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(Messages.getString("directory"), jobFileCheck.getDirectory(), true, 1, 1024);
		CommonValidator.validateString(Messages.getString("file.name"), jobFileCheck.getFileName(), true, 1, 64);
	}

	/**
	 * ジョブIDの存在チェック
	 * @param jobunitId
	 * @param jobId
	 * @param isFlag true:参照権限関係無しに全件検索 false : 通常時
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateJobId (String jobunitId, String jobId,Boolean isFlag) throws InvalidSetting, InvalidRole {

		try {
			//参照権限関係無しに全件検索する場合
			if (isFlag) {
				QueryUtil.getJobMstPK_NONE(new JobMstEntityPK(jobunitId, jobId));
			}
			//参照権限あり
			else {
				QueryUtil.getJobMstPK(jobunitId, jobId);
			}
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.info("validateJobId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			InvalidSetting e1 = new InvalidSetting(Messages.getString("message.job.1") +
					" Target job is not exist! jobunitId = " + jobunitId +
					", jobId = " + jobId);
			throw e1;
		}
	}

	/**
	 * ジョブIDの存在チェック(オーナーロールIDによるチェック）
	 * @param jobunitId
	 * @param jobId
	 * @param ownerRoleId
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateJobId (String jobunitId, String jobId, String ownerRoleId) throws InvalidSetting, InvalidRole {

		try {
			QueryUtil.getJobMstPK_OR(jobunitId, jobId, ownerRoleId);
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.info("validateJobId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			InvalidSetting e1 = new InvalidSetting(Messages.getString("message.job.1") +
					" Target job is not exist! jobunitId = " + jobunitId +
					", jobId = " + jobId);
			throw e1;
		}
	}

	/**
	 * ジョブを登録、変更、削除した際に各種参照に問題がないか
	 * INSERT, UPDATE, DELETE直後に実行する。
	 * 
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	public static void validateJobMaster() throws InvalidSetting, HinemosUnknown, JobInfoNotFound, InvalidRole {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		m_log.debug("validateJobMaster()");

		// ジョブスケジュールの参照
		m_log.debug("validateJobMaster() jobschedule check start");
		try{
			//ジョブスケジュール
			Collection<JobScheduleEntity> jobScheduleList =
					em.createNamedQuery("JobScheduleEntity.findAll",
							JobScheduleEntity.class, ObjectPrivilegeMode.NONE).getResultList();

			if (jobScheduleList == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobScheduleEntity.findAll");
				m_log.info("validateJobMaster() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				throw je;
			}
			for(JobScheduleEntity jobSchedule : jobScheduleList){
				String jobunitId = jobSchedule.getJobunitId();
				String jobId = jobSchedule.getJobId();

				m_log.debug("validateJobMaster() target jobschedule " + jobSchedule.getScheduleId() +
						", jobunitId = " + jobunitId + ", jobId = " + jobId);
				try{
					// jobunitId,jobidの存在チェック
					//true : 参照権限関係なしに全件検索する場合
					validateJobId(jobunitId,jobId,true);

					String[] args = {jobSchedule.getScheduleId()};
					m_log.debug(Messages.getString("message.job.81", args));
				} catch (InvalidSetting e) {
					// 削除対象のジョブツリーの中にジョブスケジュールからの参照がある
					String[] args = {jobSchedule.getScheduleId(), jobunitId, jobId};
					m_log.info("validateJobMaster() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw new InvalidSetting(Messages.getString("message.job.82", args));
				}
			}
			//ジョブファイルチェック
			Collection<JobFileCheckEntity> jobFileChekckList =
					em.createNamedQuery("JobFileCheckEntity.findAll",
							JobFileCheckEntity.class, ObjectPrivilegeMode.NONE).getResultList();

			if (jobFileChekckList == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobFileCheckEntity.findAll");
				m_log.info("validateJobMaster() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				throw je;
			}
			for(JobFileCheckEntity jobFileCheck : jobFileChekckList){
				String jobunitId = jobFileCheck.getJobunitId();
				String jobId = jobFileCheck.getJobId();

				m_log.debug("validateJobMaster() target jobfileCheck " + jobFileCheck.getScheduleId() +
						", jobunitId = " + jobunitId + ", jobId = " + jobId);
				try{
					// jobunitId,jobidの存在チェック
					validateJobId(jobunitId,jobId,true);

					String[] args = {jobFileCheck.getScheduleId()};
					m_log.debug(Messages.getString("message.job.96", args));
				} catch (InvalidSetting e) {
					// 削除対象のジョブツリーの中にジョブファイルチェックからの参照がある
					String[] args = {jobFileCheck.getScheduleId(), jobunitId, jobId};
					m_log.info("validateJobMaster() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw new InvalidSetting(Messages.getString("message.job.97", args));
				}
			}
		} catch (InvalidSetting e) {
			throw e;
		} catch (JobInfoNotFound e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			HinemosUnknown e1 = new HinemosUnknown(e.getMessage(), e);
			m_log.warn("validateJobMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e1;
		}
	}

	/**
	 * ジョブ定義のvalidate
	 * INSERT, UPDATE前に実行する。
	 * 
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws JobInvalid
	 */
	public static void validateJobUnit(JobTreeItem item) throws InvalidSetting, InvalidRole, HinemosUnknown, JobInvalid {
		validateJobInfo(item);
		validateDuplicateJobId(item);
		validateWaitRule(item);
		validateReferJob(item);
	}

	private static void validateJobInfo(JobTreeItem item) throws InvalidSetting, InvalidRole, HinemosUnknown{

		if(item == null || item.getData() == null){
			m_log.warn("validateJobInfo is null");
			return ;
		}

		JobInfo jobInfo = item.getData();

		// ジョブID
		String jobId = jobInfo.getId();
		CommonValidator.validateId(Messages.getString("job.id"), jobId, 64);

		// ジョブユニットID
		String jobunitId = jobInfo.getJobunitId();
		CommonValidator.validateId(Messages.getString("jobunit.id"), jobunitId, 64);

		// ジョブ名
		String jobName = jobInfo.getName();
		CommonValidator.validateString(Messages.getString("job.name"), jobName, true, 1, 64);

		// 説明
		String description = jobInfo.getDescription();
		CommonValidator.validateString(Messages.getString("description"), description, true, 0, 256);

		// ownerRoleId
		CommonValidator.validateOwnerRoleId(jobInfo.getOwnerRoleId(), true,
				new JobMstEntityPK(jobunitId, jobId), HinemosModuleConstant.JOB);

		// ジョブユニットの場合は、jobIdとjobunitIdは一緒。
		if (jobInfo.getType() == JobConstant.TYPE_JOBUNIT) {
			if (!jobId.equals(jobunitId)) {
				InvalidSetting e = new InvalidSetting("jobType is TYPE_JOBUNIT, but jobId != jobunitId");
				m_log.info("validateJobUnit() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		// ジョブの場合は、ファシリティIDの存在チェック
		if (jobInfo.getType() == JobConstant.TYPE_JOB) {
			JobCommandInfo command = jobInfo.getCommand();

			// 実行するファシリティIDのチェック
			if(command.getFacilityID() == null || "".equals(command.getFacilityID())){
				InvalidSetting e = new InvalidSetting(Messages.getString("message.hinemos.3"));
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}else{

				// ジョブ変数でない場合は、ファシリティIDのチェックを行う
				if(!SystemParameterConstant.isParam(
						command.getFacilityID(),
						SystemParameterConstant.FACILITY_ID)){
					try {
						FacilityTreeCache.validateFacilityId(command.getFacilityID(), jobInfo.getOwnerRoleId(), false);
					} catch (FacilityNotFound e) {
						InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. FacilityId = " + command.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
						m_log.info("validateJobUnit() : "
								+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
						throw e1;
					} catch (InvalidRole e) {
						throw e;
					} catch (Exception e) {
						m_log.warn("validateJobUnit() add job unknown error. FacilityId = " + command.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId + " : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						throw new HinemosUnknown("add job unknown error. FacilityId = " + command.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId, e);

					}
				}
				try {
					// 試行回数の未設定時はデフォルト値を設定
					if (command.getMessageRetry() == null) {
						command.setMessageRetry(DEFAULT_JOB_RETRY_CNT);
					}
					if (command.getCommandRetry() == null) {
						command.setCommandRetry(DEFAULT_JOB_RETRY_CNT);
					}

					// 試行回数のチェック
					CommonValidator.validateInt(
							Messages.getString("job.retries"),
							command.getMessageRetry(), 1,
							DataRangeConstant.SMALLINT_HIGH);

					if (command.getCommandRetryFlg() == YesNoConstant.TYPE_YES) {
						CommonValidator.validateInt(
								Messages.getString("job.retries"),
								command.getCommandRetry(), 1,
								DataRangeConstant.SMALLINT_HIGH);
					}
				} catch (Exception e) {
					m_log.info("validateJobUnit() add job retry error. FacilityId = " + command.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId + " : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw e;
				}
			}
		} else if (jobInfo.getType() == JobConstant.TYPE_FILEJOB) {
			JobFileInfo file = jobInfo.getFile();

			// 送信元ファシリティID(ノード)
			if(file.getSrcFacilityID() == null || "".equals(file.getSrcFacilityID())){
				InvalidSetting e = new InvalidSetting(Messages.getString("message.hinemos.2"));
				m_log.info("validateJobUnit() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}else{
				try {
					FacilityTreeCache.validateFacilityId(file.getSrcFacilityID(), jobInfo.getOwnerRoleId(), true);
				} catch (FacilityNotFound e) {
					InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. Src FacilityId = " + file.getSrcFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
					m_log.info("validateJobUnit() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				} catch (InvalidRole e) {
					throw e;
				} catch (InvalidSetting e) {
					InvalidSetting e1 = new InvalidSetting("Src FacilityId is not node. Src FacilityId = " + file.getSrcFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
					m_log.info("validateJobUnit() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw new HinemosUnknown("add file transfer job unknown error. Src FacilityId = " + file.getSrcFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId, e);
				} catch (Exception e) {
					m_log.warn("validateJobUnit() add file transfer job unknown error. Src FacilityId = " + file.getSrcFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId + " : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown("add file transfer job unknown error. Src FacilityId = " + file.getSrcFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId, e);
				}
			}

			// 受信先ファシリティID(ノード/スコープ)
			if(file.getDestFacilityID() == null || "".equals(file.getDestFacilityID())){
				throw new InvalidSetting(Messages.getString("message.hinemos.3"));
			}else{
				try {
					FacilityTreeCache.validateFacilityId(file.getDestFacilityID(), jobInfo.getOwnerRoleId(), false);
				} catch (FacilityNotFound e) {
					InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. Dest FacilityId = " + file.getDestFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
					m_log.info("validateJobUnit() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				} catch (InvalidRole e) {
					throw e;
				} catch (Exception e) {
					m_log.warn("validateJobUnit() add file transfer job unknown error. Dest FacilityId = " + file.getDestFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId + " "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown("add file transfer job unknown error. Dest FacilityId = " + file.getDestFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId, e);
				}
			}

			// 停止[コマンド]が選択されていないか
			if(jobInfo.getWaitRule().getEnd_delay_operation() == YesNoConstant.TYPE_YES && jobInfo.getWaitRule().getEnd_delay_operation_type() == OperationConstant.TYPE_STOP_AT_ONCE){
				InvalidSetting e = new InvalidSetting(Messages.getString("message.job.85"));
				m_log.info("validateJobUnit() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			try {
				// 試行回数の未設定時はデフォルト値を設定
				if (file.getMessageRetry() == null) {
					file.setMessageRetry(DEFAULT_JOB_RETRY_CNT);
				}
				if (file.getCommandRetry() == null) {
					file.setCommandRetry(DEFAULT_JOB_RETRY_CNT);
				}

				// 試行回数のチェック
				CommonValidator.validateInt(Messages.getString("job.retries"),
						file.getMessageRetry(), 1,
						DataRangeConstant.SMALLINT_HIGH);

				if (file.getCommandRetryFlg() == YesNoConstant.TYPE_YES) {
					CommonValidator.validateInt(
							Messages.getString("job.retries"),
							file.getCommandRetry(), 1,
							DataRangeConstant.SMALLINT_HIGH);
				}
			} catch (Exception e) {
				m_log.info("validateJobUnit() add file transfer job retry error. Dest FacilityId = " + file.getDestFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId + " "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw e;
			}

		}else if (jobInfo.getType() == JobConstant.TYPE_REFERJOB) {
			if ( jobInfo.getReferJobId() == null || jobInfo.getReferJobId().equals("")) {
				InvalidSetting e = new InvalidSetting(Messages.getString("message.job.100"));
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			if ( jobInfo.getReferJobUnitId() == null || jobInfo.getReferJobUnitId().equals("")) {
				InvalidSetting e = new InvalidSetting(Messages.getString("message.job.99"));
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		int type = jobInfo.getType();
		if (type == JobConstant.TYPE_JOB ||
				type == JobConstant.TYPE_JOBNET ||
				type == JobConstant.TYPE_JOBUNIT ||
				type == JobConstant.TYPE_FILEJOB) {
			ArrayList<JobEndStatusInfo> endStatusList = item.getData().getEndStatus();
			if (endStatusList == null) {
				String message = "JobEndStatus is null [" + item.getData().getId() + "]";
				m_log.info(message);
				throw new InvalidSetting(message);
			}
			if (endStatusList.size() != 3) {
				String message = "the number of JobEndStatus is too few [" + item.getData().getId() + "] " +
						endStatusList.size();
				m_log.info(message);
				throw new InvalidSetting(message);
			}

			if (jobInfo.getBeginPriority() == null
					|| jobInfo.getNormalPriority() == null
					|| jobInfo.getWarnPriority() == null
					|| jobInfo.getAbnormalPriority() == null) {
				String message = "the priorities of JobInfo less than 4 [" + jobInfo.getId() + "]";
				m_log.info(message);
				throw new InvalidSetting(message);
			}

			// 通知の権限チェック
			if (jobInfo.getNotifyRelationInfos() != null) {
				for(NotifyRelationInfo notifyInfo : jobInfo.getNotifyRelationInfos()){
					CommonValidator.validateNotifyId(notifyInfo.getNotifyId(), true, jobInfo.getOwnerRoleId());
				}
			}
		}

		if (type == JobConstant.TYPE_JOB ||
				type == JobConstant.TYPE_FILEJOB ||
				type == JobConstant.TYPE_JOBNET) {

			// カレンダの権限チェック
			if (jobInfo.getWaitRule().getCalendar() == YesNoConstant.TYPE_YES) {
				CommonValidator.validateCalenderId(jobInfo.getWaitRule().getCalendarId(), true, jobInfo.getOwnerRoleId());
			}
		}
		//子JobTreeItemを取得
		for(JobTreeItem child : item.getChildren()){
			validateJobInfo(child);
		}
	}

	/**
	 * 重複しているIDをチェック
	 * @param item
	 * @return
	 * @throws JobInvalid
	 */
	private static void validateDuplicateJobId(JobTreeItem item) throws JobInvalid {
		if(item == null || item.getData() == null) {
			return;
		}

		ArrayList<String> jobList = getJobIdList(item);
		Collections.sort(jobList);
		for (int i = 0; i < jobList.size() - 1; i++) {
			if (jobList.get(i).equals(jobList.get(i + 1))) {
				Object[] args = {item.getData().getJobunitId(), jobList.get(i)};
				JobInvalid e = new JobInvalid(Messages.getString("message.job.65", args));
				m_log.info("findDuplicateJobId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
	}

	private static ArrayList<String> getJobIdList(JobTreeItem item) {
		if(item == null || item.getData() == null) {
			return new ArrayList<String>();
		}
		ArrayList<String> ret = new ArrayList<String>();
		ret.add(item.getData().getId());
		for (JobTreeItem child : item.getChildren()) {
			ret.addAll(getJobIdList(child));
		}
		return ret;
	}

	/**
	 * ジョブツリーアイテムのジョブ待ち条件情報をチェックする
	 * 
	 * @param item ジョブ待ち条件情報をチェックするジョブツリーアイテム
	 */
	private static void validateWaitRule(JobTreeItem item) throws JobInvalid{
		if(item == null || item.getData() == null) {
			return;
		}
		//ジョブID取得
		String jobId = item.getData().getId();
		//待ち条件情報を取得する
		JobWaitRuleInfo waitRule = item.getData().getWaitRule();
		if(waitRule != null && waitRule instanceof JobWaitRuleInfo &&
				waitRule.getObject() != null && waitRule.getObject().size() > 0){
			for (JobObjectInfo objectInfo : waitRule.getObject()) {
				m_log.debug("objectInfo=" + objectInfo);

				if(objectInfo.getType() != JudgmentObjectConstant.TYPE_TIME && objectInfo.getType() != JudgmentObjectConstant.TYPE_START_MINUTE){
					m_log.debug("Not Time and Not Delay");
					//判定対象のジョブIDが同一階層に存在するかチェック
					boolean find = false;
					String targetJobId = objectInfo.getJobId();
					for(JobTreeItem child : item.getParent().getChildren()){
						//ジョブIDをチェック
						JobInfo childInfo = child.getData();
						if(childInfo != null && childInfo instanceof JobInfo &&
								!jobId.equals(childInfo.getId())){
							if(targetJobId.equals(childInfo.getId())){
								find = true;
								break;
							}
						}
					}
					if(!find){
						String args[] = {jobId, targetJobId};
						JobInvalid ji = new JobInvalid(Messages.getString("message.job.59", args));
						m_log.info("checkWaitRule() : " + ji.getClass().getSimpleName() + ", " + ji.getMessage());
						throw ji;
					}
				}
			}
		}
		//子JobTreeItemを取得
		for(JobTreeItem child : item.getChildren()){
			validateWaitRule(child);
		}
		return;
	}

	/**
	 * 参照ジョブにて指定された参照先のジョブ情報をチェックする
	 * 
	 * @param item 参照先のジョブ情報をチェックするジョブツリーアイテム
	 */
	private static void validateReferJob(JobTreeItem item) throws JobInvalid{
		if(item == null || item.getData() == null) {
			return;
		}

		//配下に存在する参照ジョブのみを取得する
		ArrayList<JobInfo> referJobList = JobUtil.findReferJob(item);
		m_log.trace("ReferJob count : " + referJobList.size());
		for (JobInfo referJob : referJobList) {
			String referJobId = referJob.getReferJobId();
			m_log.trace("ReferJobID : " + referJobId);
			//参照先ジョブが存在しているか調べる
			if(!JobUtil.isExistJob(item, referJobId)) {
				//参照先ジョブが存在しないため、メッセージ出力
				String args[] = {referJob.getId(), referJobId};
				throw new JobInvalid(Messages.getString("message.job.98", args));
			}
		}
		return;
	}
}
