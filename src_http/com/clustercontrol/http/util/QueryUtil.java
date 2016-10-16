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

package com.clustercontrol.http.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.http.model.MonitorHttpInfoEntity;
import com.clustercontrol.http.model.MonitorHttpScenarioInfoEntity;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static MonitorHttpInfoEntity getMonitorHttpInfoPK(String monitorId) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorHttpInfoEntity entity = em.find(MonitorHttpInfoEntity.class, monitorId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorHttpInfoEntity.findByPrimaryKey"
					+ ", monitorId = " + monitorId);
			m_log.info("getMonitorHttpInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static MonitorHttpScenarioInfoEntity getMonitorHttpScenarioInfoPK(String monitorId) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorHttpScenarioInfoEntity entity = em.find(MonitorHttpScenarioInfoEntity.class, monitorId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorHttpInfoEntity.findByPrimaryKey"
					+ ", monitorId = " + monitorId);
			m_log.info("getMonitorHttpInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}
}
