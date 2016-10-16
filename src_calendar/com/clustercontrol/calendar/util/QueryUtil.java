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

package com.clustercontrol.calendar.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.calendar.model.CalDetailInfoEntity;
import com.clustercontrol.calendar.model.CalDetailInfoEntityPK;
import com.clustercontrol.calendar.model.CalInfoEntity;
import com.clustercontrol.calendar.model.CalPatternDetailInfoEntity;
import com.clustercontrol.calendar.model.CalPatternDetailInfoEntityPK;
import com.clustercontrol.calendar.model.CalPatternInfoEntity;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static CalInfoEntity getCalInfoPK(String calendarId) throws CalendarNotFound, InvalidRole {
		return getCalInfoPK(calendarId, ObjectPrivilegeMode.READ);
	}

	public static CalInfoEntity getCalInfoPK(String calendarId, ObjectPrivilegeMode mode) throws CalendarNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CalInfoEntity entity = null;
		try {
			entity = em.find(CalInfoEntity.class, calendarId, mode);
			if (entity == null) {
				CalendarNotFound e = new CalendarNotFound("CalInfoEntity.findByPrimaryKey, " +
						"calendarId = " + calendarId);
				m_log.info("getCalInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getCalInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static CalInfoEntity getCalInfoPK_NONE(String calendarId) throws CalendarNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CalInfoEntity entity = null;
		try {
			entity = em.find(CalInfoEntity.class, calendarId, ObjectPrivilegeMode.NONE);
			if (entity == null) {
				CalendarNotFound e = new CalendarNotFound("CalInfoEntity.findByPrimaryKey, " +
						"calendarId = " + calendarId);
				m_log.info("getCalInfoPK_NONE() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			// NONE（オブジェクト権限チェックなし）のため、ここは通らない。
		}
		return entity;
	}

	public static CalInfoEntity getCalInfoPK_OR(String calendarId, String ownerRoleId) throws CalendarNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CalInfoEntity entity = null;
		try {
			entity = em.find_OR(CalInfoEntity.class, calendarId, ownerRoleId);
			if (entity == null) {
				CalendarNotFound e = new CalendarNotFound("CalInfoEntity.findByPrimaryKey, " +
						"calendarId = " + calendarId);
				m_log.info("getCalInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getCalInfoPK_OR() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}


	public static CalDetailInfoEntity getCalDetailInfoPK(String calendarId, Integer orderNo) throws CalendarNotFound {
		return getCalDetailInfoPK(new CalDetailInfoEntityPK(calendarId, orderNo));
	}

	public static CalDetailInfoEntity getCalDetailInfoPK(CalDetailInfoEntityPK pk) throws CalendarNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CalDetailInfoEntity entity = em.find(CalDetailInfoEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			CalendarNotFound e = new CalendarNotFound("CalDetailInfoEntity.findByPrimaryKey, "
					+ pk.toString());
			m_log.info("getCalDetailInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static CalPatternInfoEntity getCalPatternInfoPK(String calPatternId) throws CalendarNotFound, InvalidRole {
		return getCalPatternInfoPK(calPatternId, ObjectPrivilegeMode.READ);
	}

	public static CalPatternInfoEntity getCalPatternInfoPK(String calPatternId, ObjectPrivilegeMode mode) throws CalendarNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CalPatternInfoEntity entity = null;
		try {
			entity = em.find(CalPatternInfoEntity.class, calPatternId, mode);
			if (entity == null) {
				CalendarNotFound e = new CalendarNotFound("CalPatternInfoEntity.findByPrimaryKey, " +
						"calPatternId = " + calPatternId);
				m_log.info("getCalPatternInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getCalPatternInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static CalPatternInfoEntity getCalPatternInfoPK_OR(String calPatternId, String ownerRoleId) throws CalendarNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CalPatternInfoEntity entity = null;
		try {
			entity = em.find_OR(CalPatternInfoEntity.class, calPatternId, ownerRoleId);
			if (entity == null) {
				CalendarNotFound e = new CalendarNotFound("CalPatternInfoEntity.findByPrimaryKey, " +
						"calPatternId = " + calPatternId);
				m_log.info("getCalPatternInfoPK_OR() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getCalPatternInfoPK_OR() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static CalPatternDetailInfoEntity getCalPatternDetailInfoPK(String calendarId,
			Integer yearNo, Integer monthNo, Integer dayNo) throws CalendarNotFound {
		return getCalPatternDetailInfoPK(new CalPatternDetailInfoEntityPK(calendarId, yearNo, monthNo, dayNo));
	}

	public static CalPatternDetailInfoEntity getCalPatternDetailInfoPK(CalPatternDetailInfoEntityPK pk) throws CalendarNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CalPatternDetailInfoEntity entity = em.find(CalPatternDetailInfoEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			CalendarNotFound e = new CalendarNotFound("CalPatternDetailInfoEntity.findByPrimaryKey, "
					+ pk.toString());
			m_log.info("getCalPatternDetailInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<CalInfoEntity> getAllCalInfo() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalInfoEntity> list
		= em.createNamedQuery("CalInfoEntity.findAll", CalInfoEntity.class).getResultList();
		return list;
	}

	public static List<CalInfoEntity> getAllCalInfo_OR(String ownerRoleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalInfoEntity> list
		= em.createNamedQuery_OR("CalInfoEntity.findAll", CalInfoEntity.class, ownerRoleId)
		.getResultList();
		return list;
	}

	public static List<CalInfoEntity> getCalInfoFindByOwnerRoleId_NONE(String roleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalInfoEntity> list
		= em.createNamedQuery("CalInfoEntity.findByOwnerRoleId", CalInfoEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("ownerRoleId", roleId)
		.getResultList();
		return list;
	}

	public static List<CalDetailInfoEntity> getAllCalDetailInfo() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalDetailInfoEntity> list
		= em.createNamedQuery("CalDetailInfoEntity.findAll", CalDetailInfoEntity.class).getResultList();
		return list;
	}

	public static List<CalDetailInfoEntity> getCalDetailByCalendarId(String calendarId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalDetailInfoEntity> list
		= em.createNamedQuery("CalDetailInfoEntity.findByCalendarId", CalDetailInfoEntity.class)
		.setParameter("calendarId", calendarId)
		.getResultList();
		return list;
	}

	public static List<CalDetailInfoEntity> getCalDetailByCalPatternId(String calPatternId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalDetailInfoEntity> list
		= em.createNamedQuery("CalDetailInfoEntity.findByCalPatternId", CalDetailInfoEntity.class)
		.setParameter("calPatternId", calPatternId)
		.getResultList();
		return list;
	}

	public static List<CalPatternInfoEntity> getAllCalPatternInfo() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalPatternInfoEntity> list
		= em.createNamedQuery("CalPatternInfoEntity.findAll", CalPatternInfoEntity.class).getResultList();
		return list;
	}

	public static List<CalPatternInfoEntity> getAllCalPatternInfo_OR(String ownerRoleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalPatternInfoEntity> list
		= em.createNamedQuery_OR("CalPatternInfoEntity.findAll", CalPatternInfoEntity.class, ownerRoleId)
		.getResultList();
		return list;
	}

	public static List<CalPatternInfoEntity> getCalPatternInfoFindByOwnerRoleId_NONE(String roleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalPatternInfoEntity> list
		= em.createNamedQuery("CalPatternInfoEntity.findByOwnerRoleId", CalPatternInfoEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("ownerRoleId", roleId)
		.getResultList();
		return list;
	}

	public static List<CalPatternDetailInfoEntity> getCalPatternDetailByCalPatternId(String calPatternId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalPatternDetailInfoEntity> list
		= em.createNamedQuery("CalPatternDetailInfoEntity.findByCalendarPatternId", CalPatternDetailInfoEntity.class)
		.setParameter("calPatternId", calPatternId)
		.getResultList();
		return list;
	}
}
