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
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.SchedulerException;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.scheduler.QuartzUtil;
import com.clustercontrol.commons.util.EmptyJpaTransactionCallback;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobKickDuplicate;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.jobmanagement.bean.JobFileCheck;
import com.clustercontrol.jobmanagement.bean.JobSchedule;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.bean.QuartzConstant;
import com.clustercontrol.jobmanagement.model.JobFileCheckEntity;
import com.clustercontrol.jobmanagement.model.JobScheduleEntity;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;

/**
 * スケジュール情報を操作するクラスです。
 *
 * @version 2.4.0
 * @since 1.0.0
 */
public class ModifyJobKick {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( ModifyJobKick.class );

	/**
	 * スケジュール情報をDBに反映し、スケジューラにジョブを登録します。<BR>
	 *
	 * @param info スケジュール情報
	 * @param user ユーザID
	 * @throws JobKickDuplicate
	 * @throws InvalidSetting
	 * @throws ParseException
	 * @throws SchedulerException
	 *
	 * @see com.clustercontrol.jobmanagement.bean.QuartzConstant
	 * @see com.clustercontrol.jobmanagement.bean.JobTriggerInfo
	 * @see com.clustercontrol.jobmanagement.util.QuartzUtil#getQuartzManager()
	 */
	public void addSchedule(final JobSchedule info, String loginUser) throws HinemosUnknown, JobKickDuplicate {
		m_log.debug("addSchedule() : id=" + info.getId() + ", jobId=" + info.getJobId());
		JpaTransactionManager jtm = new JpaTransactionManager();
		//最終更新日時を設定
		Timestamp now = new Timestamp(new Date().getTime());
		// DBにスケジュール情報を保存
		try {
			// IDの重複チェック
			String id = info.getId();
			jtm.checkEntityExists(JobScheduleEntity.class, id);
			jtm.checkEntityExists(JobFileCheckEntity.class, id);
			JobScheduleEntity jobScheduleEntity = new JobScheduleEntity(info.getId());
			jobScheduleEntity.setScheduleName(info.getName());
			jobScheduleEntity.setJobunitId(info.getJobunitId());
			jobScheduleEntity.setJobId(info.getJobId());
			if (!"".equals(info.getCalendarId())) {
				jobScheduleEntity.setCalendarId(info.getCalendarId());
			}
			jobScheduleEntity.setScheduleType(info.getScheduleType());
			jobScheduleEntity.setWeek(info.getWeek());
			jobScheduleEntity.setHour(info.getHour());
			jobScheduleEntity.setMinute(info.getMinute());
			jobScheduleEntity.setFromXMinutes(info.getFromXminutes());
			jobScheduleEntity.setEveryXMinutes(info.getEveryXminutes());
			jobScheduleEntity.setValidFlg(info.getValid());
			jobScheduleEntity.setOwnerRoleId(info.getOwnerRoleId());
			jobScheduleEntity.setRegDate(now);
			jobScheduleEntity.setUpdateDate(now);
			jobScheduleEntity.setRegUser(loginUser);
			jobScheduleEntity.setUpdateUser(loginUser);
		} catch (EntityExistsException e) {
			m_log.info("addSchedule() JobScheduleEntity.create() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new JobKickDuplicate(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addSchedule() JobScheduleEntity.create() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		
		jtm.addCallback(new EmptyJpaTransactionCallback() {
			@Override
			public void postCommit() {
				//実行契機情報の作成
				JobTriggerInfo triggerInfo = new JobTriggerInfo();
				triggerInfo.setTrigger_type(JobTriggerTypeConstant.TYPE_SCHEDULE);
				triggerInfo.setTrigger_info(info.getName()+"("+info.getId()+")");
				
				//JobDetailに呼び出すメソッドの引数を設定
				Serializable[] jdArgs = new Serializable[QuartzConstant.ARGS_NUM];
				Class<? extends Serializable>[] jdArgsType = new Class[QuartzConstant.ARGS_NUM];
				//ジョブユニットIDを設定
				jdArgs[QuartzConstant.INDEX_JOBUNIT_ID] = info.getJobunitId();
				jdArgsType[QuartzConstant.INDEX_JOBUNIT_ID] = String.class;
				
				//ジョブIDを設定
				jdArgs[QuartzConstant.INDEX_JOB_ID] = info.getJobId();
				jdArgsType[QuartzConstant.INDEX_JOB_ID] = String.class;
				
				//カレンダIDを設定
				jdArgs[QuartzConstant.INDEX_CALENDAR_ID] = info.getCalendarId();
				jdArgsType[QuartzConstant.INDEX_CALENDAR_ID] = String.class;
				
				//実行契機情報を設定
				jdArgs[QuartzConstant.INDEX_TRIGGER_INFO] = triggerInfo;
				jdArgsType[QuartzConstant.INDEX_TRIGGER_INFO] = JobTriggerInfo.class;
				
				//Cron表記へ変換
				String cronString = QuartzUtil.getCronString(info.getScheduleType(),
						info.getWeek(),info.getHour(),info.getMinute(),
						info.getFromXminutes(),info.getEveryXminutes());
				
				m_log.trace("CronString =" + cronString);
				
				// スケジュール定義を登録
				try {
					SchedulerPlugin.scheduleCronJob(SchedulerType.DBMS, info.getId(), QuartzConstant.GROUP_NAME, new Date(System.currentTimeMillis() + 15 * 1000), cronString,
							true, JobControllerBean.class.getName(), QuartzConstant.METHOD_NAME, jdArgsType, jdArgs);
					if (! ValidConstant.typeToBoolean(info.getValid())) {
						SchedulerPlugin.pauseJob(SchedulerType.DBMS, info.getId(), QuartzConstant.GROUP_NAME);
					}
				} catch (HinemosUnknown e) {
					m_log.error(e);
				}
			}
		});
	}
	/**
	 * ファイルチェック情報をDBに反映します。<BR>
	 *
	 * @param info ファイルチェック情報
	 * @param user ユーザID
	 * @throws JobKickDuplicate
	 * @throws InvalidSetting
	 * @throws ParseException
	 * @throws SchedulerException
	 *
	 * @see com.clustercontrol.jobmanagement.bean.QuartzConstant
	 * @see com.clustercontrol.jobmanagement.bean.JobTriggerInfo
	 * @see com.clustercontrol.jobmanagement.util.QuartzUtil#getQuartzManager()
	 */
	public void addFileCheck(JobFileCheck info, String loginUser) throws HinemosUnknown, JobKickDuplicate {
		m_log.debug("addFileCheck() : id=" + info.getId() + ", jobId=" + info.getJobId());
		JpaTransactionManager jtm = new JpaTransactionManager();
		//最終更新日時を設定
		Timestamp now = new Timestamp(new Date().getTime());
		// DBにファイルチェック情報を保存
		try {
			// IDの重複チェック
			String id = info.getId();
			jtm.checkEntityExists(JobScheduleEntity.class, id);
			jtm.checkEntityExists(JobFileCheckEntity.class, id);

			JobFileCheckEntity jobFileCheckEntity = new JobFileCheckEntity(info.getId());
			jobFileCheckEntity.setScheduleName(info.getName());
			jobFileCheckEntity.setJobunitId(info.getJobunitId());
			jobFileCheckEntity.setJobId(info.getJobId());
			if (!"".equals(info.getCalendarId())) {
				jobFileCheckEntity.setCalendarId(info.getCalendarId());
			}
			jobFileCheckEntity.setFacilityId(info.getFacilityId());
			jobFileCheckEntity.setFileName(info.getFileName());
			jobFileCheckEntity.setDirectory(info.getDirectory());
			jobFileCheckEntity.setEventType(info.getEventType());
			jobFileCheckEntity.setModifyType(info.getModifyType());

			jobFileCheckEntity.setValidFlg(info.getValid());
			jobFileCheckEntity.setOwnerRoleId(info.getOwnerRoleId());
			jobFileCheckEntity.setRegDate(now);
			jobFileCheckEntity.setUpdateDate(now);
			jobFileCheckEntity.setRegUser(loginUser);
			jobFileCheckEntity.setUpdateUser(loginUser);
		} catch (EntityExistsException e) {
			m_log.info("addFileCheck() JobFileCheckEntity.create() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new JobKickDuplicate(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addFileCheck() JobFileCheckEntity.create() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		//実行契機情報の作成
		JobTriggerInfo triggerInfo = new JobTriggerInfo();
		triggerInfo.setTrigger_type(JobTriggerTypeConstant.TYPE_FILECHECK);
		triggerInfo.setTrigger_info(info.getName()+"("+info.getId()+")");

		//JobDetailに呼び出すメソッドの引数を設定
		Object[] jdArgs = new Object[QuartzConstant.ARGS_NUM];
		Class<?>[] jdArgsType = new Class[QuartzConstant.ARGS_NUM];
		//ジョブユニットIDを設定
		jdArgs[QuartzConstant.INDEX_JOBUNIT_ID] = info.getJobunitId();
		jdArgsType[QuartzConstant.INDEX_JOBUNIT_ID] = String.class;

		//ジョブIDを設定
		jdArgs[QuartzConstant.INDEX_JOB_ID] = info.getJobId();
		jdArgsType[QuartzConstant.INDEX_JOB_ID] = String.class;

		//カレンダIDを設定
		jdArgs[QuartzConstant.INDEX_CALENDAR_ID] = info.getCalendarId();
		jdArgsType[QuartzConstant.INDEX_CALENDAR_ID] = String.class;

		//実行契機情報を設定
		jdArgs[QuartzConstant.INDEX_TRIGGER_INFO] = triggerInfo;
		jdArgsType[QuartzConstant.INDEX_TRIGGER_INFO] = JobTriggerInfo.class;

	}
	/**
	 * DBのスケジュール情報を変更します。<BR>
	 * @param info スケジュール情報
	 * @param loginUser ユーザID
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 */
	public void modifySchedule(final JobSchedule info, String loginUser) throws HinemosUnknown, JobInfoNotFound, ObjectPrivilege_InvalidRole{
		m_log.debug("modifySchedule() : id=" + info.getId() + ", jobId=" + info.getJobId());
		JpaTransactionManager jtm = new JpaTransactionManager();
		HinemosEntityManager em = jtm.getEntityManager();
		//最終更新日時を設定
		Timestamp now = new Timestamp(new Date().getTime());
		// DBにスケジュール情報を保存
		try {
			JobScheduleEntity bean  = em.find(JobScheduleEntity.class, info.getId(),
					ObjectPrivilegeMode.MODIFY);
			if (bean == null) {
				JobInfoNotFound e = new JobInfoNotFound("JobScheduleEntity.findByPrimaryKey");
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			bean.setScheduleId(info.getId());
			bean.setScheduleName(info.getName());
			bean.setJobunitId(info.getJobunitId());
			bean.setJobId(info.getJobId());
			if ("".equals(info.getCalendarId())) {
				bean.setCalendarId(null);
			} else {
				bean.setCalendarId(info.getCalendarId());
			}
			bean.setScheduleType(info.getScheduleType());
			bean.setWeek(info.getWeek());
			bean.setHour(info.getHour());
			bean.setMinute(info.getMinute());
			bean.setFromXMinutes(info.getFromXminutes());
			bean.setEveryXMinutes(info.getEveryXminutes());
			bean.setValidFlg(info.getValid());
			bean.setOwnerRoleId(info.getOwnerRoleId());
			/*
			 * 作成時刻は変更時に更新されるはずがないので、再度登録しない
			 * bean.setRegDate(new Timestamp(info.getCreateTime()));
			 * 作成ユーザは変更時に更新されるはずがないので、再度登録しない
			 * bean.setRegUser(info.getCreateUser());
			 */
			bean.setUpdateDate(now);
			bean.setUpdateUser(loginUser);
		} catch (JobInfoNotFound e) {
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("modifySchedule() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		
		jtm.addCallback(new EmptyJpaTransactionCallback() {
			@Override
			public void postCommit() {
				//実行契機情報の作成
				JobTriggerInfo triggerInfo = new JobTriggerInfo();
				triggerInfo.setTrigger_type(JobTriggerTypeConstant.TYPE_SCHEDULE);
				triggerInfo.setTrigger_info(info.getName()+"("+info.getId()+")");
				
				//JobDetailに呼び出すメソッドの引数を設定
				Serializable[] jdArgs = new Serializable[QuartzConstant.ARGS_NUM];
				Class<? extends Serializable>[] jdArgsType = new Class[QuartzConstant.ARGS_NUM];
				//ジョブユニットIDを設定
				jdArgs[QuartzConstant.INDEX_JOBUNIT_ID] = info.getJobunitId();
				jdArgsType[QuartzConstant.INDEX_JOBUNIT_ID] = String.class;
				
				//ジョブIDを設定
				jdArgs[QuartzConstant.INDEX_JOB_ID] = info.getJobId();
				jdArgsType[QuartzConstant.INDEX_JOB_ID] = String.class;
				
				//カレンダIDを設定
				jdArgs[QuartzConstant.INDEX_CALENDAR_ID] = info.getCalendarId();
				jdArgsType[QuartzConstant.INDEX_CALENDAR_ID] = String.class;
				
				//実行契機情報を設定
				jdArgs[QuartzConstant.INDEX_TRIGGER_INFO] = triggerInfo;
				jdArgsType[QuartzConstant.INDEX_TRIGGER_INFO] = JobTriggerInfo.class;
				
				//Cron表記へ変換
				String cronString = QuartzUtil.getCronString(info.getScheduleType(),
						info.getWeek(),info.getHour(),info.getMinute(),
						info.getFromXminutes(),info.getEveryXminutes());
				
				m_log.trace("CronString =" + cronString);
				
				// スケジュール定義を登録
				try {
					SchedulerPlugin.scheduleCronJob(SchedulerType.DBMS, info.getId(), QuartzConstant.GROUP_NAME, new Date(System.currentTimeMillis() + 15 * 1000), cronString,
							true, JobControllerBean.class.getName(), QuartzConstant.METHOD_NAME, jdArgsType, jdArgs);
					if (! ValidConstant.typeToBoolean(info.getValid())) {
						SchedulerPlugin.pauseJob(SchedulerType.DBMS, info.getId(), QuartzConstant.GROUP_NAME);
					}
				} catch (HinemosUnknown e) {
					m_log.error(e);
				}
			}
		});


	}
	/**
	 * DBのファイルチェック情報を変更します。<BR>
	 * @param info ファイルチェック情報
	 * @param loginUser ユーザID
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 */
	public void modifyFileCheck(JobFileCheck info, String loginUser) throws HinemosUnknown, JobInfoNotFound, ObjectPrivilege_InvalidRole{
		m_log.debug("modifyFileCheck() : scheduleId=" + info.getId());
		JpaTransactionManager jtm = new JpaTransactionManager();
		HinemosEntityManager em = jtm.getEntityManager();
		//最終更新日時を設定
		Timestamp now = new Timestamp(new Date().getTime());
		// DBにファイルチェック情報を保存
		try {
			JobFileCheckEntity bean  = em.find(JobFileCheckEntity.class, info.getId(),
					ObjectPrivilegeMode.MODIFY);
			if (bean == null) {
				JobInfoNotFound e = new JobInfoNotFound("JobFileCheckEntity.findByPrimaryKey");
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			bean.setScheduleId(info.getId());
			bean.setScheduleName(info.getName());
			bean.setJobunitId(info.getJobunitId());
			bean.setJobId(info.getJobId());
			if ("".equals(info.getCalendarId())) {
				bean.setCalendarId(null);
			} else {
				bean.setCalendarId(info.getCalendarId());
			}
			bean.setFacilityId(info.getFacilityId());
			bean.setDirectory(info.getDirectory());
			bean.setFileName(info.getFileName());
			bean.setEventType(info.getEventType());
			bean.setModifyType(info.getModifyType());
			bean.setValidFlg(info.getValid());
			bean.setOwnerRoleId(info.getOwnerRoleId());
			/*
			 * 作成時刻は変更時に更新されるはずがないので、再度登録しない
			 * bean.setRegDate(new Timestamp(info.getCreateTime()));
			 * 作成ユーザは変更時に更新されるはずがないので、再度登録しない
			 * bean.setRegUser(info.getCreateUser());
			 */
			bean.setUpdateDate(now);
			bean.setUpdateUser(loginUser);
		} catch (JobInfoNotFound e) {
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("modifyFileCheck() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		//実行契機情報の作成
		JobTriggerInfo triggerInfo = new JobTriggerInfo();
		triggerInfo.setTrigger_type(JobTriggerTypeConstant.TYPE_FILECHECK);
		triggerInfo.setTrigger_info(info.getName()+"("+info.getId()+")");

		//JobDetailに呼び出すメソッドの引数を設定
		Object[] jdArgs = new Object[QuartzConstant.ARGS_NUM];
		Class<?>[] jdArgsType = new Class[QuartzConstant.ARGS_NUM];
		//ジョブユニットIDを設定
		jdArgs[QuartzConstant.INDEX_JOBUNIT_ID] = info.getJobunitId();
		jdArgsType[QuartzConstant.INDEX_JOBUNIT_ID] = String.class;

		//ジョブIDを設定
		jdArgs[QuartzConstant.INDEX_JOB_ID] = info.getJobId();
		jdArgsType[QuartzConstant.INDEX_JOB_ID] = String.class;

		//カレンダIDを設定
		jdArgs[QuartzConstant.INDEX_CALENDAR_ID] = info.getCalendarId();
		jdArgsType[QuartzConstant.INDEX_CALENDAR_ID] = String.class;

		//実行契機情報を設定
		jdArgs[QuartzConstant.INDEX_TRIGGER_INFO] = triggerInfo;
		jdArgsType[QuartzConstant.INDEX_TRIGGER_INFO] = JobTriggerInfo.class;
	}
	/**
	 * スケジュール情報を基にQuartzに登録したジョブを削除します。
	 *
	 * @param scheduleId スケジュールID
	 * @throws JobInfoNotFound
	 * @throws SchedulerException
	 *
	 * @see com.clustercontrol.jobmanagement.bean.QuartzConstant
	 * @see com.clustercontrol.jobmanagement.util.QuartzUtil#getQuartzManager()
	 * @see com.clustercontrol.quartzmanager.ejb.session.QuartzManager#deleteSchedule(java.lang.String, java.lang.String)
	 */
	public void deleteSchedule(final String scheduleId) throws HinemosUnknown, JobInfoNotFound, ObjectPrivilege_InvalidRole {
		// スケジュール定義を削除
		m_log.debug("deleteSchedule() : id=" + scheduleId);

		JpaTransactionManager jtm = new JpaTransactionManager();
		HinemosEntityManager em = jtm.getEntityManager();
		// DBのスケジュール情報を削除
		try {
			//削除対象を検索
			JobScheduleEntity jobScheduleEntity = em.find(JobScheduleEntity.class, scheduleId,
					ObjectPrivilegeMode.MODIFY);
			if (jobScheduleEntity == null) {
				JobInfoNotFound e = new JobInfoNotFound("JobScheduleEntity.findByPrimaryKey");
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			//削除
			em.remove(jobScheduleEntity);
		} catch (JobInfoNotFound e) {
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteSchedule() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		//CronTriggerを削除
		jtm.addCallback(new EmptyJpaTransactionCallback() {
			@Override
			public void postCommit() {
				try {
					SchedulerPlugin.deleteJob(SchedulerType.DBMS, scheduleId, QuartzConstant.GROUP_NAME);
				} catch (HinemosUnknown e) {
					m_log.error(e);
				}
			}
		});

	}
	public void deleteFileCheck(String scheduleId) throws HinemosUnknown, JobInfoNotFound, ObjectPrivilege_InvalidRole {
		// ファイルチェック定義を削除
		m_log.debug("deleteFileCheck() : id=" + scheduleId);
		JpaTransactionManager jtm = null;

		jtm = new JpaTransactionManager();
		HinemosEntityManager em = jtm.getEntityManager();

		// DBのファイルチェック情報を削除
		try {
			//削除対象を検索
			JobFileCheckEntity jobFileCheckEntity = em.find(JobFileCheckEntity.class, scheduleId,
					ObjectPrivilegeMode.MODIFY);
			if (jobFileCheckEntity == null) {
				JobInfoNotFound e = new JobInfoNotFound("JobFileCheckEntity.findByPrimaryKey");
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			//削除
			em.remove(jobFileCheckEntity);
		} catch (JobInfoNotFound e) {
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteFileCheck() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}
}
