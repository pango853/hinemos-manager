package com.clustercontrol.monitor.plugin.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.plugin.model.MonitorPluginInfoEntity;
import com.clustercontrol.monitor.plugin.model.MonitorPluginNumericInfoEntity;
import com.clustercontrol.monitor.plugin.model.MonitorPluginNumericInfoEntityPK;
import com.clustercontrol.monitor.plugin.model.MonitorPluginStringInfoEntity;
import com.clustercontrol.monitor.plugin.model.MonitorPluginStringInfoEntityPK;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static MonitorPluginInfoEntity getMonitorPluginInfoPK(String monitorId) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorPluginInfoEntity entity = em.find(MonitorPluginInfoEntity.class, monitorId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorPluginInfoEntity.findByPrimaryKey"
					+ ", monitorId = " + monitorId);
			m_log.info("getMonitorPluginInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}


	public static MonitorPluginNumericInfoEntity getMonitorPluginNumericInfoEntity(MonitorPluginNumericInfoEntityPK pk) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorPluginNumericInfoEntity entity = em.find(MonitorPluginNumericInfoEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorPluginNumericInfoEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getMonitorPluginNumericInfoEntity() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static MonitorPluginStringInfoEntity getMonitorPluginStringInfoEntity(MonitorPluginStringInfoEntityPK pk) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorPluginStringInfoEntity entity = em.find(MonitorPluginStringInfoEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorPluginStringInfoEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getMonitorPluginStringInfoEntity() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

}
