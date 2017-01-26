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

package com.clustercontrol.snmptrap.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.snmptrap.model.MonitorTrapInfoEntity;
import com.clustercontrol.snmptrap.model.MonitorTrapValueInfoEntity;
import com.clustercontrol.snmptrap.model.MonitorTrapValueInfoEntityPK;
import com.clustercontrol.snmptrap.model.MonitorTrapVarbindPatternInfoEntity;
import com.clustercontrol.snmptrap.model.MonitorTrapVarbindPatternInfoEntityPK;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static MonitorTrapInfoEntity getMonitorTrapInfoPK(String monitorId) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorTrapInfoEntity entity = em.find(MonitorTrapInfoEntity.class, monitorId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorTrapInfoEntity.findByPrimaryKey, "
					+ "monitorId = " + monitorId);
			m_log.info("getMonitorTrapInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}
	
	public static MonitorTrapValueInfoEntity getMonitorTrapValueInfoPK(MonitorTrapValueInfoEntityPK pk) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorTrapValueInfoEntity entity = em.find(MonitorTrapValueInfoEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorTrapInfoEntity.findByPrimaryKey, "
					+ "pk = " + pk);
			m_log.info("getMonitorTrapValueInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static MonitorTrapVarbindPatternInfoEntity getMonitorTrapVarbindPatternInfoPK(MonitorTrapVarbindPatternInfoEntityPK pk) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorTrapVarbindPatternInfoEntity entity = em.find(MonitorTrapVarbindPatternInfoEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorTrapVarbindPatternInfoEntity.findByPrimaryKey, "
					+ "pk = " + pk);
			m_log.info("MonitorTrapVarbindPatternInfoEntityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}
}
