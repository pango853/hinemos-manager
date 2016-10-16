/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.jobmanagement.model.JobEndInfoEntity;
import com.clustercontrol.jobmanagement.model.JobEndInfoEntityPK;
import com.clustercontrol.jobmanagement.model.JobFileCheckEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobScheduleEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntityPK;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntityPK;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static JobMstEntity getJobMstPK(JobMstEntityPK pk, ObjectPrivilegeMode mode) throws JobMasterNotFound, InvalidRole {
		JpaTransactionManager jtm = null;
		JobMstEntity jobMst = null;
		try {
			jtm = new JpaTransactionManager();
			HinemosEntityManager em = jtm.getEntityManager();
			jobMst = em.find(JobMstEntity.class, pk, mode);
			if (jobMst == null) {
				JobMasterNotFound je = new JobMasterNotFound("JobMstEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getJobMstPK() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				je.setJobunitId(pk.getJobunitId());
				je.setJobId(pk.getJobId());
				throw je;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getJobMstPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return jobMst;
	}

	public static JobMstEntity getJobMstPK_NONE(JobMstEntityPK pk) throws JobMasterNotFound {
		JobMstEntity jobMst = null;
		try {
			jobMst = getJobMstPK(pk, ObjectPrivilegeMode.NONE);
		} catch (InvalidRole e) {
			// NONE（オブジェクト権限チェックなし）のため、ここは通らない。
		}
		return jobMst;
	}

	public static JobMstEntity getJobMstPK(String jobunitId, String jobId) throws JobMasterNotFound, InvalidRole {
		return getJobMstPK(new JobMstEntityPK(jobunitId, jobId), ObjectPrivilegeMode.READ);
	}

	public static JobMstEntity getJobMstPK_OR(String jobunitId, String jobId, String ownerRoleId) throws JobMasterNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		JobMstEntity jobMst = null;
		try {
			JobMstEntityPK pk = new JobMstEntityPK(jobunitId, jobId);
			jobMst = em.find_OR(JobMstEntity.class, pk, ObjectPrivilegeMode.READ, ownerRoleId);
			if (jobMst == null) {
				JobMasterNotFound je = new JobMasterNotFound("JobMstEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getJobMstPK_OR() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				je.setJobunitId(pk.getJobunitId());
				je.setJobId(pk.getJobId());
				throw je;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getJobMstPK_OR() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return jobMst;
	}

	public static List<JobMstEntity> getJobMstEnityFindByJobunitId(
			String jobunitId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		return em.createNamedQuery("JobMstEntity.findByJobunitId", JobMstEntity.class)
					.setParameter("jobunitId", jobunitId).getResultList();
	}

	public static JobSessionEntity getJobSessionPK(String sessionId) throws JobInfoNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		JobSessionEntity session = em.find(JobSessionEntity.class, sessionId, ObjectPrivilegeMode.READ);
		if (session == null) {
			JobInfoNotFound je = new JobInfoNotFound("JobSessionEntity.findByPrimaryKey"
					+ ", sessionId = " + sessionId);
			m_log.info("getJobSessionPK() : "
					+ je.getClass().getSimpleName() + ", " + je.getMessage());
			je.setSessionId(sessionId);
			throw je;
		}
		return session;
	}

	public static JobSessionJobEntity getJobSessionJobPK(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntityPK sessionJobPk = new JobSessionJobEntityPK(sessionId, jobunitId, jobId);
		JobSessionJobEntity sessionJob = null;
		try {
			sessionJob = em.find(JobSessionJobEntity.class, sessionJobPk, ObjectPrivilegeMode.READ);
			if (sessionJob == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobSessionJobEntity.findByPrimaryKey"
						+ ", " + sessionJobPk.toString());
				m_log.info("getJobSessionJobPK() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				je.setSessionId(sessionId);
				je.setJobunitId(jobunitId);
				je.setJobId(jobId);
				throw je;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getJobSessionJobPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return sessionJob;
	}

	public static JobEndInfoEntity getJobEndInfoPK (String sessionId, String jobunitId, String jobId, Integer endStatus) throws JobInfoNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		JobEndInfoEntityPK endInfoNormalPk = new JobEndInfoEntityPK(sessionId, jobunitId, jobId, endStatus);
		JobEndInfoEntity endInfo = em.find(JobEndInfoEntity.class, endInfoNormalPk, ObjectPrivilegeMode.READ);
		if (endInfo == null) {
			JobInfoNotFound je = new JobInfoNotFound("JobEndInfoEntity.findByPrimaryKey"
					+ ", " + endInfoNormalPk.toString());
			m_log.info("getJobEndInfoPK() : "
					+ je.getClass().getSimpleName() + ", " + je.getMessage());
			je.setSessionId(sessionId);
			je.setJobunitId(jobunitId);
			je.setJobId(jobId);
			je.setEndStatus(endStatus);
			throw je;
		}
		return endInfo;
	}

	public static JobSessionNodeEntity getJobSessionNodePK(String sessionId, String jobunitId, String jobId, String facilityId) throws JobInfoNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		//セッションノード取得
		JobSessionNodeEntityPK sessionNodePk
		= new JobSessionNodeEntityPK(sessionId, jobunitId, jobId, facilityId);
		JobSessionNodeEntity sessionNode = em.find(JobSessionNodeEntity.class, sessionNodePk, ObjectPrivilegeMode.READ);
		if (sessionNode == null) {
			JobInfoNotFound je = new JobInfoNotFound("JobSessionNodeEntity.findByPrimaryKey"
					+ ", " + sessionNodePk.toString());
			m_log.info("endNodeSetStatus() : "
					+ je.getClass().getSimpleName() + ", " + je.getMessage());
			je.setSessionId(sessionId);
			je.setJobunitId(jobunitId);
			je.setJobId(jobId);
			je.setFacilityId(facilityId);
			throw je;
		}
		return sessionNode;
	}

	public static List<JobMstEntity> getJobMstEntityFindByCalendarId(String calendarId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブ
		List<JobMstEntity> jobMstList
		= em.createNamedQuery("JobMstEntity.findByCalendarId", JobMstEntity.class)
		.setParameter("calendarId", calendarId).getResultList();
		return jobMstList;
	}

	public static List<JobMstEntity> getJobMstEntityFindByOwnerRoleId_NONE(String roleId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブ
		List<JobMstEntity> jobMstList
		= em.createNamedQuery("JobMstEntity.findByOwnerRoleId", JobMstEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("ownerRoleId", roleId).getResultList();
		return jobMstList;
	}

	public static List<JobSessionJobEntity> getChildJobSessionJob(String sessionId, String parentJobunitId, String parentJobId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブ
		List<JobSessionJobEntity> jobSessionJobList
		= em.createNamedQuery("JobSessionJobEntity.findChild", JobSessionJobEntity.class)
		.setParameter("sessionId", sessionId)
		.setParameter("parentJobunitId", parentJobunitId)
		.setParameter("parentJobId", parentJobId).getResultList();
		return jobSessionJobList;
	}

	public static List<JobSessionJobEntity> getJobSessionJobByParentStatus(String sessionId, String parentJobunitId, String parentJobId, int status){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブ
		List<JobSessionJobEntity> jobSessionJobList
		= em.createNamedQuery("JobSessionJobEntity.findByParentStatus", JobSessionJobEntity.class)
		.setParameter("sessionId", sessionId)
		.setParameter("parentJobunitId", parentJobunitId)
		.setParameter("parentJobId", parentJobId)
		.setParameter("status", status).getResultList();
		return jobSessionJobList;
	}
	

	public static List<JobSessionJobEntity> getJobSessionJobBySessionId(String sessionId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブ
		List<JobSessionJobEntity> jobSessionJobList
		= em.createNamedQuery("JobSessionJobEntity.findByJobSessionId", JobSessionJobEntity.class)
		.setParameter("sessionId", sessionId).getResultList();
		return jobSessionJobList;
	}

	public static List<JobSessionJobEntity> getChildJobSessionJobOrderByStartDate(String sessionId, String parentJobunitId, String parentJobId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブ
		List<JobSessionJobEntity> jobSessionJobList
		= em.createNamedQuery("JobSessionJobEntity.findByStartDate", JobSessionJobEntity.class)
		.setParameter("sessionId", sessionId)
		.setParameter("parentJobunitId", parentJobunitId)
		.setParameter("parentJobId", parentJobId).getResultList();
		return jobSessionJobList;
	}

	public static List<JobScheduleEntity> getJobScheduleEntityFindByCalendarId_NONE(String calendarId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブスケジュール
		List<JobScheduleEntity> jobScheduleList
		= em.createNamedQuery("JobScheduleEntity.findByCalendarId", JobScheduleEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("calendarId", calendarId).getResultList();
		return jobScheduleList;
	}

	public static List<JobScheduleEntity> getJobScheduleEntityFindByOwnerRoleId_NONE(String roleId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブスケジュール
		List<JobScheduleEntity> jobScheduleList
		= em.createNamedQuery("JobScheduleEntity.findByOwnerRoleId", JobScheduleEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("ownerRoleId", roleId).getResultList();
		return jobScheduleList;
	}

	public static List<JobFileCheckEntity> getJobFileCheckEntityFindByCalendarId_NONE(String calendarId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブファイルチェック
		List<JobFileCheckEntity> jobFileCheckList
		= em.createNamedQuery("JobFileCheckEntity.findByCalendarId", JobFileCheckEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("calendarId", calendarId).getResultList();
		return jobFileCheckList;
	}

	public static List<JobFileCheckEntity> getJobFileCheckEntityFindByOwnerRoleId_NONE(String roleId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブファイルチェック
		List<JobFileCheckEntity> jobFileCheckList
		= em.createNamedQuery("JobFileCheckEntity.findByOwnerRoleId", JobFileCheckEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("ownerRoleId", roleId).getResultList();
		return jobFileCheckList;
	}
}
