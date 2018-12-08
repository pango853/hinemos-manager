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

package com.clustercontrol.monitor.run.util;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfoEntity;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfoEntityPK;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfoEntity;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfoEntityPK;
import com.clustercontrol.monitor.run.model.MonitorTruthValueInfoEntity;
import com.clustercontrol.monitor.run.model.MonitorTruthValueInfoEntityPK;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static MonitorInfoEntity getMonitorInfoPK(String monitorId) throws MonitorNotFound, InvalidRole {
		return getMonitorInfoPK(monitorId, ObjectPrivilegeMode.READ);
	}

	public static MonitorInfoEntity getMonitorInfoPK(String monitorId, ObjectPrivilegeMode mode) throws MonitorNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorInfoEntity entity = null;
		try {
			entity = em.find(MonitorInfoEntity.class, monitorId, mode);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorInfoEntity.findByPrimaryKey"
						+ ", monitorId = " + monitorId);
				m_log.info("getMonitorInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getMonitorInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static MonitorInfoEntity getMonitorInfoPK_NONE(String monitorId) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorInfoEntity entity
		= em.find(MonitorInfoEntity.class, monitorId, ObjectPrivilegeMode.NONE);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorInfoEntity.findByPrimaryKey"
					+ ", monitorId = " + monitorId);
			m_log.info("getMonitorInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<MonitorInfoEntity> getAllMonitorInfo() throws HinemosUnknown {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorInfoEntity> list
		= em.createNamedQuery("MonitorInfoEntity.findAll", MonitorInfoEntity.class)
		.getResultList();
		return list;
	}

	public static List<MonitorInfoEntity> getMonitorInfoByOwnerRoleId_NONE(String roleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorInfoEntity> list
		= em.createNamedQuery("MonitorInfoEntity.findByOwnerRoleId", MonitorInfoEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("ownerRoleId", roleId)
		.getResultList();
		return list;
	}

	public static List<MonitorInfoEntity> getMonitorInfoByFacilityId_NONE(String facilityId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorInfoEntity> list
		= em.createNamedQuery("MonitorInfoEntity.findByFacilityId", MonitorInfoEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("facilityId", facilityId)
		.getResultList();
		return list;
	}

	public static List<MonitorInfoEntity> getMonitorInfoByMonitorTypeId(String monitorTypeId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorInfoEntity> list
		= em.createNamedQuery("MonitorInfoEntity.findByMonitorTypeId", MonitorInfoEntity.class)
		.setParameter("monitorTypeId", monitorTypeId)
		.getResultList();
		return list;
	}
	
	public static List<MonitorInfoEntity> getMonitorInfoByMonitorTypeId_NONE(String monitorTypeId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorInfoEntity> list
		= em.createNamedQuery("MonitorInfoEntity.findByMonitorTypeId", MonitorInfoEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("monitorTypeId", monitorTypeId)
		.getResultList();
		return list;
	}

	public static MonitorNumericValueInfoEntity getMonitorNumericValueInfoPK(MonitorNumericValueInfoEntityPK pk) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorNumericValueInfoEntity entity = em.find(MonitorNumericValueInfoEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorNumericValueInfoEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getMonitorNumericValueInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static MonitorNumericValueInfoEntity getMonitorNumericValueInfoPK(String monitorId, Integer priority) throws MonitorNotFound {
		return getMonitorNumericValueInfoPK(new MonitorNumericValueInfoEntityPK(monitorId, priority));
	}

	public static MonitorStringValueInfoEntity getMonitorStringValueInfoPK(MonitorStringValueInfoEntityPK pk) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorStringValueInfoEntity entity = em.find(MonitorStringValueInfoEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorStringValueInfoEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getMonitorStringValueInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static MonitorStringValueInfoEntity getMonitorStringValueInfoPK(String monitorId, Integer orderNo) throws MonitorNotFound {
		return getMonitorStringValueInfoPK(new MonitorStringValueInfoEntityPK(monitorId, orderNo));
	}

	public static MonitorTruthValueInfoEntity getMonitorTruthValueInfoPK(MonitorTruthValueInfoEntityPK pk) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorTruthValueInfoEntity entity = em.find(MonitorTruthValueInfoEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorTruthValueInfoEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getMonitorTruthValueInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}
	public static List<MonitorInfoEntity> getMonitorInfoEntityFindByCalendarId_NONE(String calendarId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorInfoEntity> monitorInfoEntityList
		= em.createNamedQuery("MonitorInfoEntity.findByCalendarId", MonitorInfoEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("calendarId", calendarId).getResultList();
		return monitorInfoEntityList;
	}

	public static MonitorTruthValueInfoEntity getMonitorTruthValueInfoPK(String monitorId, Integer priority, Integer truthValue) throws MonitorNotFound {
		return getMonitorTruthValueInfoPK(new MonitorTruthValueInfoEntityPK(monitorId, priority, truthValue));
	}
	public static int deleteCalculatedDataByCollectorid(String collectorid) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		return em.createNamedQuery("CalculatedDataEntity.deleteByCollectorid")
				.setParameter("collectorid", collectorid)
				.executeUpdate();
	}

	public static List<MonitorInfoEntity> getMonitorInfoByFilter(
			String monitorId,
			String monitorTypeId,
			String description,
			String calendarId,
			String regUser,
			Long regFromDate,
			Long regToDate,
			String updateUser,
			Long updateFromDate,
			Long updateToDate,
			Integer monitorFlg,
			Integer collectorFlg,
			String ownerRoleId) {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		StringBuffer sbJpql = new StringBuffer();
		sbJpql.append("SELECT a FROM MonitorInfoEntity a WHERE true = true");
		// monitorId
		if(monitorId != null && !"".equals(monitorId)) {
			sbJpql.append(" AND a.monitorId like :monitorId");
		}
		// monitorTypeId
		if(monitorTypeId != null && !"".equals(monitorTypeId)) {
			sbJpql.append(" AND a.monitorTypeId like :monitorTypeId");
		}
		// description
		if(description != null && !"".equals(description)) {
			sbJpql.append(" AND a.description like :description");
		}
		// calendarId
		if(calendarId != null && !"".equals(calendarId)) {
			sbJpql.append(" AND a.calendarId like :calendarId");
		}
		// regUser
		if(regUser != null && !"".equals(regUser)) {
			sbJpql.append(" AND a.regUser like :regUser");
		}
		// regFromDate
		if (regFromDate > 0) {
			sbJpql.append(" AND a.regDate >= :regFromDate");
		}
		// regToDate
		if (regToDate > 0){
			sbJpql.append(" AND a.regDate <= :regToDate");
		}
		// updateUser
		if(updateUser != null && !"".equals(updateUser)) {
			sbJpql.append(" AND a.updateUser like :updateUser");
		}
		// updateFromDate
		if(updateFromDate > 0) {
			sbJpql.append(" AND a.updateDate >= :updateFromDate");
		}
		// updateToDate
		if(updateToDate > 0) {
			sbJpql.append(" AND a.updateDate <= :updateToDate");
		}
		// monitorFlg
		if(monitorFlg >= 0) {
			sbJpql.append(" AND a.monitorFlg = :monitorFlg");
		}
		// collectorFlg
		if(collectorFlg >= 0) {
			sbJpql.append(" AND a.collectorFlg = :collectorFlg");
		}
		// ownerRoleId
		if(ownerRoleId != null && !"".equals(ownerRoleId)) {
			sbJpql.append(" AND a.ownerRoleId = :ownerRoleId");
		}
		TypedQuery<MonitorInfoEntity> typedQuery = em.createQuery(sbJpql.toString(), MonitorInfoEntity.class);

		// monitorId
		if(monitorId != null && !"".equals(monitorId)) {
			typedQuery = typedQuery.setParameter("monitorId", monitorId);
		}
		// monitorTypeId
		if(monitorTypeId != null && !"".equals(monitorTypeId)) {
			typedQuery = typedQuery.setParameter("monitorTypeId", monitorTypeId);
		}
		// description
		if(description != null && !"".equals(description)) {
			typedQuery = typedQuery.setParameter("description", description);
		}
		// calendarId
		if(calendarId != null && !"".equals(calendarId)) {
			typedQuery = typedQuery.setParameter("calendarId", calendarId);
		}
		// regUser
		if(regUser != null && !"".equals(regUser)) {
			typedQuery = typedQuery.setParameter("regUser", regUser);
		}
		// regFromDate
		if (regFromDate > 0) {
			typedQuery = typedQuery.setParameter("regFromDate", new Timestamp(regFromDate));
		}
		// regToDate
		if (regToDate > 0){
			typedQuery = typedQuery.setParameter("regToDate", new Timestamp(regToDate));
		}
		// updateUser
		if(updateUser != null && !"".equals(updateUser)) {
			typedQuery = typedQuery.setParameter("updateUser", updateUser);
		}
		// updateFromDate
		if(updateFromDate > 0) {
			typedQuery = typedQuery.setParameter("updateFromDate", new Timestamp(updateFromDate));
		}
		// updateToDate
		if(updateToDate > 0) {
			typedQuery = typedQuery.setParameter("updateToDate", new Timestamp(updateToDate));
		}
		// monitorFlg
		if(monitorFlg >= 0) {
			typedQuery = typedQuery.setParameter("monitorFlg", monitorFlg);
		}
		// collectorFlg
		if(collectorFlg >= 0) {
			typedQuery = typedQuery.setParameter("collectorFlg", collectorFlg);
		}
		// ownerRoleId
		if(ownerRoleId != null && !"".equals(ownerRoleId)) {
			typedQuery = typedQuery.setParameter("ownerRoleId", ownerRoleId);
		}
		return typedQuery.getResultList();
	}

}
