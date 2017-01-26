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

package com.clustercontrol.notify.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.notify.model.MonitorStatusEntity;
import com.clustercontrol.notify.model.MonitorStatusEntityPK;
import com.clustercontrol.notify.model.NotifyCommandInfoEntity;
import com.clustercontrol.notify.model.NotifyCommandInfoEntityPK;
import com.clustercontrol.notify.model.NotifyEventInfoEntity;
import com.clustercontrol.notify.model.NotifyEventInfoEntityPK;
import com.clustercontrol.notify.model.NotifyHistoryEntity;
import com.clustercontrol.notify.model.NotifyHistoryEntityPK;
import com.clustercontrol.notify.model.NotifyInfoEntity;
import com.clustercontrol.notify.model.NotifyJobInfoEntity;
import com.clustercontrol.notify.model.NotifyJobInfoEntityPK;
import com.clustercontrol.notify.model.NotifyLogEscalateInfoEntity;
import com.clustercontrol.notify.model.NotifyLogEscalateInfoEntityPK;
import com.clustercontrol.notify.model.NotifyMailInfoEntity;
import com.clustercontrol.notify.model.NotifyMailInfoEntityPK;
import com.clustercontrol.notify.model.NotifyRelationInfoEntity;
import com.clustercontrol.notify.model.NotifyStatusInfoEntity;
import com.clustercontrol.notify.model.NotifyStatusInfoEntityPK;
import com.clustercontrol.notify.monitor.model.StatusInfoEntity;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static NotifyInfoEntity getNotifyInfoPK(String notifyId) throws NotifyNotFound, InvalidRole {
		return getNotifyInfoPK(notifyId, ObjectPrivilegeMode.READ);
	}

	public static NotifyInfoEntity getNotifyInfoPK(String notifyId, ObjectPrivilegeMode mode) throws NotifyNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NotifyInfoEntity entity = null;
		try {
			entity = em.find(NotifyInfoEntity.class, notifyId, mode);
			if (entity == null) {
				NotifyNotFound e = new NotifyNotFound("NotifyInfoEntity.findByPrimaryKey"
						+ "notifyId = " + notifyId);
				m_log.info("getNotifyInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getNotifyInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static NotifyInfoEntity getNotifyInfoPK_OR(String notifyId, String ownerRoleId) throws NotifyNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NotifyInfoEntity entity = null;
		try {
			entity = em.find_OR(NotifyInfoEntity.class, notifyId, ownerRoleId);
			if (entity == null) {
				NotifyNotFound e = new NotifyNotFound("NotifyInfoEntity.findByPrimaryKey"
						+ "notifyId = " + notifyId);
				m_log.info("getNotifyInfoPK_OR() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getNotifyInfoPK_OR() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static List<NotifyInfoEntity> getAllNotifyInfo() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NotifyInfoEntity> list
		= em.createNamedQuery("NotifyInfoEntity.findAll", NotifyInfoEntity.class).getResultList();
		return list;
	}

	/**
	 *  オブジェクト権限チェックなし
	 * @return
	 */
	public static List<NotifyInfoEntity> getAllNotifyInfo_NONE() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NotifyInfoEntity> list
		= em.createNamedQuery("NotifyInfoEntity.findAll", NotifyInfoEntity.class, ObjectPrivilegeMode.NONE).getResultList();
		return list;
	}

	public static List<NotifyInfoEntity> getAllNotifyInfoOrderByNotifyId() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NotifyInfoEntity> list
		= em.createNamedQuery("NotifyInfoEntity.findAllOrderByNotifyId", NotifyInfoEntity.class).getResultList();
		return list;
	}

	public static List<NotifyInfoEntity> getAllNotifyInfoOrderByNotifyId_OR(String ownerRoleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NotifyInfoEntity> list
		= em
		.createNamedQuery_OR("NotifyInfoEntity.findAllOrderByNotifyId", NotifyInfoEntity.class, ownerRoleId)
		.getResultList();
		return list;
	}

	public static List<NotifyInfoEntity> getNotifyInfoFindByOwnerRoleId_NONE(String roleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NotifyInfoEntity> list
		= em.createNamedQuery("NotifyInfoEntity.findByOwnerRoleId", NotifyInfoEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("ownerRoleId", roleId)
		.getResultList();
		return list;
	}
	
	public static List<NotifyInfoEntity> getNotifyInfoFindByCalendarId_NONE(String calendarId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NotifyInfoEntity> list
		= em.createNamedQuery("NotifyInfoEntity.findByCalendarId", NotifyInfoEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("calendarId", calendarId)
		.getResultList();
		return list;
	}

	public static NotifyCommandInfoEntity getNotifyCommandInfoPK(NotifyCommandInfoEntityPK pk) throws NotifyNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NotifyCommandInfoEntity entity = em.find(NotifyCommandInfoEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			NotifyNotFound e = new NotifyNotFound("NotifyCommandInfoEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNotifyCommandInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NotifyCommandInfoEntity getNotifyCommandInfoPK(String notifyId) throws NotifyNotFound {
		return getNotifyCommandInfoPK(new NotifyCommandInfoEntityPK(notifyId));
	}

	public static NotifyEventInfoEntity getNotifyEventInfoPK(NotifyEventInfoEntityPK pk) throws NotifyNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NotifyEventInfoEntity entity = em.find(NotifyEventInfoEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			NotifyNotFound e = new NotifyNotFound("NotifyEventInfoEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNotifyEventInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NotifyEventInfoEntity getNotifyEventInfoPK(String notifyId) throws NotifyNotFound {
		return getNotifyEventInfoPK(new NotifyEventInfoEntityPK(notifyId));
	}

	public static NotifyJobInfoEntity getNotifyJobInfoPK(NotifyJobInfoEntityPK pk) throws NotifyNotFound {
		JpaTransactionManager jtm = null;
		NotifyJobInfoEntity entity = null;
		try {
			jtm = new JpaTransactionManager();
			
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(NotifyJobInfoEntity.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				NotifyNotFound e = new NotifyNotFound("NotifyJobInfoEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getNotifyJobInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (NotifyNotFound e) {
			throw e;
		} finally {
			jtm.close();
		}
		return entity;
	}

	public static NotifyJobInfoEntity getNotifyJobInfoPK(String notifyId) throws NotifyNotFound {
		return getNotifyJobInfoPK(new NotifyJobInfoEntityPK(notifyId));
	}

	public static List<NotifyJobInfoEntity> getNotifyJobInfoByJobExecFacilityId(String facilityId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NotifyJobInfoEntity> list
		= em.createNamedQuery("NotifyJobInfoEntity.findByJobExecFacilityId", NotifyJobInfoEntity.class)
		.setParameter("facilityId", facilityId)
		.getResultList();
		return list;
	}

	public static NotifyLogEscalateInfoEntity getNotifyLogEscalateInfoPK(NotifyLogEscalateInfoEntityPK pk) throws NotifyNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NotifyLogEscalateInfoEntity entity = em.find(NotifyLogEscalateInfoEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			NotifyNotFound e = new NotifyNotFound("NotifyLogEscalateInfoEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNotifyLogEscalateInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NotifyLogEscalateInfoEntity getNotifyLogEscalateInfoPK(String notifyId) throws NotifyNotFound {
		return getNotifyLogEscalateInfoPK(new NotifyLogEscalateInfoEntityPK(notifyId));
	}

	public static List<NotifyLogEscalateInfoEntity> getNotifyLogEscalateInfoByEscalateFacilityId(String facilityId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NotifyLogEscalateInfoEntity> list
		= em.createNamedQuery("NotifyLogEscalateInfoEntity.findByEscalateFacilityId", NotifyLogEscalateInfoEntity.class)
		.setParameter("facilityId", facilityId)
		.getResultList();
		return list;
	}

	public static NotifyMailInfoEntity getNotifyMailInfoPK(NotifyMailInfoEntityPK pk) throws NotifyNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NotifyMailInfoEntity entity = em.find(NotifyMailInfoEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			NotifyNotFound e = new NotifyNotFound("NotifyMailInfoEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNotifyMailInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NotifyMailInfoEntity getNotifyMailInfoPK(String notifyId) throws NotifyNotFound {
		return getNotifyMailInfoPK(new NotifyMailInfoEntityPK(notifyId));
	}

	public static NotifyStatusInfoEntity getNotifyStatusInfoPK(NotifyStatusInfoEntityPK pk) throws NotifyNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NotifyStatusInfoEntity entity = em.find(NotifyStatusInfoEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			NotifyNotFound e = new NotifyNotFound("NotifyStatusInfoEntity.findByPrimaryKey"
					+ pk.toString());
			// 対応する通知設定が存在しない（Internalメッセージ）場合にも呼ばれるのでdebugとする
			m_log.debug("getNotifyStatusInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NotifyStatusInfoEntity getNotifyStatusInfoPK(String notifyId) throws NotifyNotFound {
		return getNotifyStatusInfoPK(new NotifyStatusInfoEntityPK(notifyId));
	}

	public static NotifyHistoryEntity getNotifyHistoryPK(NotifyHistoryEntityPK pk) throws NotifyNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NotifyHistoryEntity entity = em.find(NotifyHistoryEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			NotifyNotFound e = new NotifyNotFound("NotifyHistoryEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.debug("getNotifyHistoryPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NotifyHistoryEntity getNotifyHistoryPK(String facilityId,
			String pluginId,
			String monitorId,
			String notifyId,
			String subKey) throws NotifyNotFound {
		return getNotifyHistoryPK(new NotifyHistoryEntityPK(facilityId,
				pluginId,
				monitorId,
				notifyId,
				subKey));
	}

	public static List<NotifyHistoryEntity> getNotifyHistoryByNotifyId(String notifyId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NotifyHistoryEntity> list
		= em.createNamedQuery("NotifyHistoryEntity.findByNotifyId", NotifyHistoryEntity.class)
		.setParameter("notifyId", notifyId)
		.getResultList();
		return list;
	}

	public static List<NotifyRelationInfoEntity> getAllNotifyRelationInfo() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NotifyRelationInfoEntity> list
		=  em.createNamedQuery("NotifyRelationInfoEntity.findAll", NotifyRelationInfoEntity.class)
		.getResultList();
		return list;
	}

	/**
	 * オブジェクト権限チェックなし
	 * @return
	 */
	public static List<NotifyRelationInfoEntity> getAllNotifyRelationInfoWithoutJob() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NotifyRelationInfoEntity> list
		=  em.createNamedQuery("NotifyRelationInfoEntity.findAllWithoutJob", NotifyRelationInfoEntity.class, ObjectPrivilegeMode.NONE)
		.getResultList();
		return list;
	}

	public static List<NotifyRelationInfoEntity> getNotifyRelationInfoByNotifyGroupId(String notifyGroupId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NotifyRelationInfoEntity> list
		=  em.createNamedQuery("NotifyRelationInfoEntity.findByNotifyGroupId", NotifyRelationInfoEntity.class)
		.setParameter("notifyGroupId", notifyGroupId)
		.getResultList();
		m_log.debug("queryUtil " + list.size() + "," + notifyGroupId);
		return list;
	}

	public static List<NotifyRelationInfoEntity> getNotifyRelationInfoByNotifyId(String notifyId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NotifyRelationInfoEntity> list
		=  em.createNamedQuery("NotifyRelationInfoEntity.findByNotifyId", NotifyRelationInfoEntity.class)
		.setParameter("notifyId", notifyId)
		.getResultList();
		return list;
	}


	public static List<NotifyHistoryEntity> getNotifyHistoryByPluginIdAndMonitorId(String pluginId, String monitorId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NotifyHistoryEntity> list =
				em.createNamedQuery("NotifyHistoryEntity.findByPluginIdAndMonitorId", NotifyHistoryEntity.class)
				.setParameter("pluginId", pluginId)
				.setParameter("monitorId", monitorId)
				.getResultList();
		return list;
	}
	
	public static List<NotifyHistoryEntity> getNotifyHistoryByPluginIdAndMonitorIdAndFacilityId(
			String pluginId, String monitorId, String facilityId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NotifyHistoryEntity> list =
				em.createNamedQuery("NotifyHistoryEntity.findByPluginIdAndMonitorIdAndFacilityId", NotifyHistoryEntity.class)
				.setParameter("pluginId", pluginId)
				.setParameter("monitorId", monitorId)
				.setParameter("facilityId", facilityId)
				.getResultList();
		return list;
	}

	public static List<MonitorStatusEntity> getAllMonitorStatus() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorStatusEntity> list
		=  em.createNamedQuery("MonitorStatusEntity.findAll", MonitorStatusEntity.class)
		.getResultList();
		return list;
	}

	public static MonitorStatusEntity getMonitorStatusPK(MonitorStatusEntityPK pk) throws NotifyNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorStatusEntity entity = em.find(MonitorStatusEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			NotifyNotFound e = new NotifyNotFound("MonitorStatusEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getMonitorStatusPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static MonitorStatusEntity getMonitorStatusPK(String facilityId,
			String pluginId,
			String monitorId,
			String subKey) throws NotifyNotFound {
		return getMonitorStatusPK(new MonitorStatusEntityPK(facilityId,
				pluginId,
				monitorId,
				subKey));
	}

	public static List<MonitorStatusEntity> getMonitorStatusByPluginIdAndMonitorId(String pluginId, String monitorId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorStatusEntity> list =
				em.createNamedQuery("MonitorStatusEntity.findByPluginIdAndMonitorId", MonitorStatusEntity.class)
				.setParameter("pluginId", pluginId)
				.setParameter("monitorId", monitorId)
				.getResultList();
		return list;
	}

	public static List<StatusInfoEntity> getStatusInfoByPluginIdAndMonitorId(String pluginId,
			String monitorId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<StatusInfoEntity> list =
				em.createNamedQuery("StatusInfoEntity.findByPluginIdAndMonitorId", StatusInfoEntity.class)
				.setParameter("pluginId", pluginId)
				.setParameter("monitorId", monitorId)
				.getResultList();
		return list;
	}
}
