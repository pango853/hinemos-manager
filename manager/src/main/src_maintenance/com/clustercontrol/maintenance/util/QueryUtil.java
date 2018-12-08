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

package com.clustercontrol.maintenance.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaPersistenceConfig;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosPropertyNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.maintenance.model.HinemosPropertyEntity;
import com.clustercontrol.maintenance.model.MaintenanceInfoEntity;
import com.clustercontrol.maintenance.model.MaintenanceTypeMstEntity;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );


	public static MaintenanceTypeMstEntity getMaintenanceTypeMstPK(String typeId) throws MaintenanceNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MaintenanceTypeMstEntity entity = em.find(MaintenanceTypeMstEntity.class, typeId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MaintenanceNotFound e = new MaintenanceNotFound("MaintenanceTypeMstEntity.findByPrimaryKey"
					+ ", typeId = " + typeId);
			m_log.info("getMaintenanceTypeMstPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<MaintenanceTypeMstEntity> getAllMaintenanceTypeMst() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MaintenanceTypeMstEntity> list = em.createNamedQuery("MaintenanceTypeMstEntity.findAll"
				, MaintenanceTypeMstEntity.class).getResultList();
		return list;
	}

	public static MaintenanceInfoEntity getMaintenanceInfoPK(String maintenanceId) throws MaintenanceNotFound, InvalidRole {
		return getMaintenanceInfoPK(maintenanceId, ObjectPrivilegeMode.READ);
	}

	public static MaintenanceInfoEntity getMaintenanceInfoPK(String maintenanceId, ObjectPrivilegeMode mode) throws MaintenanceNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MaintenanceInfoEntity entity = null;
		try {
			entity = em.find(MaintenanceInfoEntity.class, maintenanceId, mode);
			if (entity == null) {
				MaintenanceNotFound e = new MaintenanceNotFound("MaintenanceInfoEntity.findByPrimaryKey"
						+ ", maintenanceId = " + maintenanceId);
				m_log.info("getMaintenanceInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getMaintenanceInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static List<MaintenanceInfoEntity> getAllMaintenanceInfoOrderByMaintenanceId() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MaintenanceInfoEntity> list = em.createNamedQuery("MaintenanceInfoEntity.findAllOrderByMaintenanceId"
				,MaintenanceInfoEntity.class).getResultList();
		return list;
	}

	public static List<MaintenanceInfoEntity> getMaintenanceInfoFindByCalendarId_NONE(String calendarId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MaintenanceInfoEntity> list
		= em.createNamedQuery("MaintenanceInfoEntity.findByCalendarId"
				,MaintenanceInfoEntity.class, ObjectPrivilegeMode.NONE)
				.setParameter("calendarId", calendarId)
				.getResultList();
		return list;
	}

	public static int deleteCalculatedDataByDateTime(Timestamp dateTime, int timeout) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		Query query = em.createNamedQuery("CalculatedDataEntity.deleteByDateTime")
				.setParameter("dateTime", dateTime);
		if (timeout > 0) {
			query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
		}
		return query.executeUpdate();
	}

	public static int deleteCalculatedDataByDateTimeCollectorid(Timestamp dateTime, int timeout) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		Query query = em.createNamedQuery("CalculatedDataEntity.deleteByDateTimeCollectorid")
				.setParameter("dateTime", dateTime);
		if (timeout > 0) {
			query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
		}
		return query.executeUpdate();
	}
	
	public static int deleteCalculatedDataByDateTimeAndOwnerRoleId(Timestamp dateTime, int timeout, String roleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		Query query = em.createNamedQuery("CalculatedDataEntity.deleteByDateTimeAndOwnerRoleId")
				.setParameter("dateTime", dateTime)
				.setParameter("ownerRoleId", roleId);
		if (timeout > 0) {
			query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
		}
		return query.executeUpdate();
	}

	public static int deleteCalculatedDataByDateTimeCollectoridAndOwnerRoleId(Timestamp dateTime, int timeout, String roleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		Query query = em.createNamedQuery("CalculatedDataEntity.deleteByDateTimeCollectoridAndOwnerRoleId")
				.setParameter("dateTime", dateTime)
				.setParameter("ownerRoleId", roleId);
		if (timeout > 0) {
			query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
		}
		return query.executeUpdate();
	}

	public static int deleteEventLogByGenerationDate(Timestamp generationDate, int timeout) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		Query query = em.createNamedQuery("EventLogEntity.deleteByGenerationDate")
				.setParameter("generationDate", generationDate);
		if (timeout > 0) {
			query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
		}
		return query.executeUpdate();
	}

	public static int deleteEventLogByGenerationDateConfigFlg(Timestamp generationDate, int timeout) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		Query query = em.createNamedQuery("EventLogEntity.deleteByGenerationDateConfigFlg")
				.setParameter("generationDate", generationDate);
		if (timeout > 0) {
			query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
		}
		return query.executeUpdate();
	}
	
	public static int deleteEventLogByGenerationDateAndOwnerRoleId(Timestamp generationDate, int timeout, String roleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		
		Query query = em.createNamedQuery("EventLogEntity.deleteByGenerationDateAndOwnerRoleId")
				.setParameter("generationDate", generationDate)
				.setParameter("ownerRoleId", roleId);
		if (timeout > 0) {
			query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
		}
		return query.executeUpdate();
	}

	public static int deleteEventLogByGenerationDateConfigFlgAndOwnerRoleId(Timestamp generationDate, int timeout, String roleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		Query query = em.createNamedQuery("EventLogEntity.deleteByGenerationDateConfigFlgAndOwnerRoleId")
				.setParameter("generationDate", generationDate)
				.setParameter("ownerRoleId", roleId);
		if (timeout > 0) {
			query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
		}
		return query.executeUpdate();
	}

	public static int createJobCompletedSessionsTable() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		int ret = em.createNamedQuery("JobCompletedSessionsEntity.createTable").executeUpdate();
		return ret;
	}

	public static int insertJobCompletedSessionsJobSessionJob(Timestamp startDate) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		int ret = em.createNamedQuery("JobCompletedSessionsEntity.insertJobSessionJob")
				.setParameter(1, startDate)
				.executeUpdate();
		return ret;
	}
	
	public static int insertJobCompletedSessionsJobSessionJobByOwnerRoleId(Timestamp startDate, String roleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		int ret = em.createNamedQuery("JobCompletedSessionsEntity.insertJobSessionJobByOwnerRoleId")
				.setParameter(1, startDate)
				.setParameter(2, roleId)
				.executeUpdate();
		return ret;
	}

	public static int dropJobCompletedSessionsTable() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		int ret = em.createNamedQuery("JobCompletedSessionsEntity.dropTable").executeUpdate();
		return ret;
	}

	public static int insertJobCompletedSessionsJobSessionJobByStatus(Timestamp startDate) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		int ret = em.createNamedQuery("JobCompletedSessionsEntity.insertJobSessionJobByStatus")
				.setParameter(1, startDate)
				.executeUpdate();
		return ret;
	}

	public static int insertJobCompletedSessionsJobSessionJobByStatusAndOwnerRoleId(Timestamp startDate, String roleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		int ret = em.createNamedQuery("JobCompletedSessionsEntity.insertJobSessionJobByStatusAndOwnerRoleId")
				.setParameter(1, startDate)
				.setParameter(2, roleId)
				.executeUpdate();
		return ret;
	}

	public static int deleteJobSessionByCompletedSessions() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		int ret = em.createNamedQuery("JobSessionEntity.deleteByJobCompletedSessions")
				.executeUpdate();
		return ret;
	}

	public static int deleteNotifyRelationInfoByCompletedSessions() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		int ret = em.createNamedQuery("NotifyRelationInfoEntity.deleteByJobCompletedSessions")
				.executeUpdate();
		return ret;
	}

	public static int deleteMonitorStatusByCompletedSessions() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		int ret = em.createNamedQuery("MonitorStatusEntity.deleteByJobCompletedSessions")
				.executeUpdate();
		return ret;
	}

	public static int deleteNotifyHistoryByCompletedSessions() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		int ret = em.createNamedQuery("NotifyHistoryEntity.deleteByJobCompletedSessions")
				.executeUpdate();
		return ret;
	}

	/**
	 * セッション一時テーブルからセッションIDを取得します。
	 * @return セッションIDリスト
	 */
	public static ArrayList<String> selectCompletedSession() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<?> list = em.createNativeQuery("select session_id from cc_job_completed_sessions").getResultList();

		ArrayList<String> sessionList = new ArrayList<String>();
		for(Object obj : list) {
			sessionList.add(obj.toString());
		}
		return sessionList;
	}

	/**
	 * 指定されたキーの共通設定情報を取得します。<br>
	 * @param key キー
	 * @return 共通設定情報
	 * @throws HinemosPropertyNotFound
	 */
	public static HinemosPropertyEntity getHinemosPropertyInfoPK(String key) throws HinemosPropertyNotFound, InvalidRole {
		return getHinemosPropertyInfoPK(key, ObjectPrivilegeMode.READ);
	}

	/**
	 * 指定されたキーの共通設定情報をアクセス権限チェックなしで取得します。<br>
	 * セキュリティ上、Hinemosマネージャ内でのみ使用してください。
	 * @param key キー
	 * @return 共通設定情報
	 * @throws HinemosPropertyNotFound
	 */
	public static HinemosPropertyEntity getHinemosPropertyInfoPK_NONE(String key) throws HinemosPropertyNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		HinemosPropertyEntity entity
		= em.createNamedQuery("HinemosPropertyEntity.findByKey"
				,HinemosPropertyEntity.class, ObjectPrivilegeMode.NONE)
				.setParameter("key", key)
				.getSingleResult();
		return entity;
	}

	/**
	 * 共通設定情報一覧を取得します。
	 * @return 共通設定情報リスト
	 */
	public static List<HinemosPropertyEntity> getAllHinemosPropertyOrderByKey() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<HinemosPropertyEntity> list = em.createNamedQuery("HinemosPropertyEntity.findAll",
						HinemosPropertyEntity.class, ObjectPrivilegeMode.READ)
				.getResultList();
		return list;
	}

	protected static HinemosPropertyEntity getHinemosPropertyInfoPK(String key, ObjectPrivilegeMode mode) throws HinemosPropertyNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		HinemosPropertyEntity entity = null;
		try {
			entity = em.find(HinemosPropertyEntity.class, key, mode);
			if (entity == null) {
				HinemosPropertyNotFound e = new HinemosPropertyNotFound("HinemosPropertyEntity.findByPrimaryKey" + ", key = " + key);
					m_log.info("getHinemosPropertyInfoPK() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getHinemosPropertyInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	/**
	 * 共通情報一覧を取得します。
	 *
	 * @return 共通情報一覧
	 */
	public static List<HinemosPropertyEntity> getAllHinemosProperty_None() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<HinemosPropertyEntity> list = em.createNamedQuery("HinemosPropertyEntity.findAll",
						HinemosPropertyEntity.class, ObjectPrivilegeMode.NONE)
				.getResultList();
		return list;
	}
}
