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

package com.clustercontrol.mbean;

import java.util.Collections;
import java.util.List;

import javax.persistence.Cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.persistence.internal.jpa.CacheImpl;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.jobmanagement.util.JobMultiplicityCache;
import com.clustercontrol.notify.model.MonitorStatusEntity;
import com.clustercontrol.notify.model.NotifyHistoryEntity;
import com.clustercontrol.plugin.impl.AsyncWorkerPlugin;
import com.clustercontrol.plugin.impl.SchedulerInfo;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.WebServiceCorePlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
import com.clustercontrol.plugin.impl.SharedTablePlugin;
import com.clustercontrol.plugin.impl.SnmpTrapPlugin;
import com.clustercontrol.plugin.impl.SystemLogPlugin;
import com.clustercontrol.poller.PollerManager;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.selfcheck.SelfCheckTaskSubmitter;
import com.clustercontrol.selfcheck.monitor.JobRunSessionMonitor;
import com.clustercontrol.selfcheck.monitor.TableSizeMonitor;

public class Manager implements ManagerMXBean {

	private static final Log log = LogFactory.getLog(Manager.class);

	@Override
	public String getValidAgentStr() {
		String str = "";
		String lineSeparator = System.getProperty("line.separator");

		List<String> validAgent = AgentConnectUtil.getValidAgent();
		Collections.sort(validAgent);

		for (String facilityId : validAgent) {
			String agentString = AgentConnectUtil.getAgentString(facilityId);
			if (agentString == null) {
				continue;
			}
			str += facilityId + ", " + agentString + lineSeparator;
		}
		return str;
	}

	@Override
	public String getPollerInfoStr() {
		return PollerManager.getInstnace().getPollerDebugInfo();
	}

	@Override
	public String getSharedTableInfoStr() {
		return SharedTablePlugin.getSharedTable().getTableListDebugInfo();
	}

	@Override
	public String getSchedulerInfoStr() throws HinemosUnknown {
		String str = "";
		String lineSeparator = System.getProperty("line.separator");

		str += "----- " + SchedulerType.DBMS + " -----" + lineSeparator;
		List<SchedulerInfo> dbms = SchedulerPlugin.getSchedulerList(SchedulerType.DBMS);
		for (SchedulerInfo trigger : dbms) {
			str += lineSeparator;

			String start = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", trigger.startTime);
			String prev = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", trigger.previousFireTime);
			String next = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", trigger.nextFireTime);

			str += String.format("%s (in %s) :", trigger.name, trigger.group) + lineSeparator;
			str += String.format("   start fire time - %s", start) + lineSeparator;
			str += String.format("   last fire time  - %s", prev) + lineSeparator;
			str += String.format("   next fire time  - %s", next) + lineSeparator;
			str += String.format("   is paused       - %s", trigger.isPaused) + lineSeparator;
		}

		str += lineSeparator;

		str += "----- " + SchedulerType.RAM + " -----" + lineSeparator;
		List<SchedulerInfo> ram = SchedulerPlugin.getSchedulerList(SchedulerType.RAM);
		for (SchedulerInfo trigger : ram) {
			str += lineSeparator;

			String start = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", trigger.startTime);
			String prev = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", trigger.previousFireTime);
			String next = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", trigger.nextFireTime);

			str += String.format("%s (in %s) :", trigger.name, trigger.group) + lineSeparator;
			str += String.format("   start fire time - %s", start) + lineSeparator;
			str += String.format("   last fire time  - %s", prev) + lineSeparator;
			str += String.format("   next fire time  - %s", next) + lineSeparator;
			str += String.format("   is paused       - %s", trigger.isPaused) + lineSeparator;
		}

		return str;
	}

	@Override
	public String getSelfCheckLastFireTimeStr() {
		return String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", SelfCheckTaskSubmitter.lastMonitorDate);
	}

	@Override
	public String getSyslogStatistics() {
		String str = "";
		str += "[Syslog Statistics]" + System.getProperty("line.separator");
		str += "received : " + SystemLogPlugin.getReceivedCount() + System.getProperty("line.separator");
		str += "queued : " + SystemLogPlugin.getQueuedCount() + System.getProperty("line.separator");
		str += "discarded : " + SystemLogPlugin.getDiscardedCount() + System.getProperty("line.separator");
		str += "notified : " + SystemLogPlugin.getNotifiedCount() + System.getProperty("line.separator");
		return str;
	}

	@Override
	public String getSnmpTrapStatistics() {
		String str = "";
		str += "[SnmpTrap Statistics]" + System.getProperty("line.separator");
		str += "received : " + SnmpTrapPlugin.getReceivedCount() + System.getProperty("line.separator");
		str += "queued : " + SnmpTrapPlugin.getQueuedCount() + System.getProperty("line.separator");
		str += "discarded : " + SnmpTrapPlugin.getDiscardedCount() + System.getProperty("line.separator");
		str += "notified : " + SnmpTrapPlugin.getNotifiedCount() + System.getProperty("line.separator");
		return str;
	}

	@Override
	public String getAsyncWorkerStatistics() throws HinemosUnknown {
		String str = "";
		str += "[AsyncWorker Statistics]" + System.getProperty("line.separator");
		for (String worker : AsyncWorkerPlugin.getWorkerList()) {
			str += "queued tasks [" + worker + "] : " + AsyncWorkerPlugin.getTaskCount(worker) + System.getProperty("line.separator");
		}
		return str;
	}

	@Override
	public String resetNotificationLogger() throws HinemosUnknown {
		log.info("resetting notification counter...");

		JpaTransactionManager tm = null;
		HinemosEntityManager em = null;

		try {
			tm = new JpaTransactionManager();
			tm.begin();

			// EJB Container内の抑制情報を初期化
			em = tm.getEntityManager();
			em.createNamedQuery("MonitorStatusEntity.deleteAll", MonitorStatusEntity.class).executeUpdate();
			em.createNamedQuery("NotifyHistoryEntity.deleteAll", NotifyHistoryEntity.class).executeUpdate();

			tm.commit();
		} catch (Exception e) {
			log.warn("notify counter reset failure...", e);
			tm.rollback();
			throw new HinemosUnknown("notify counter reset failure...", e);
		} finally {
			if (tm != null) {
				tm.close();
			}
		}

		log.info("notify counter resetted successfully.");
		return null;
	}

	@Override
	public void printJpaCacheAll() {
		JpaTransactionManager tm = null;
		HinemosEntityManager em = null;
		try {
			tm = new JpaTransactionManager();
			tm.begin();
			em = tm.getEntityManager();
			Cache cache = em.getEntityManagerFactory().getCache();
			CacheImpl jpaCache = (CacheImpl) cache;
			jpaCache.print();
			tm.commit();
		} catch (Exception e) {
			log.warn("printJpaCache failure...", e);
		} finally {
			if (tm != null) {
				tm.close();
			}
		}
	}

	@Override
	public String getJobQueueStr() {
		return JobMultiplicityCache.getJobQueueStr();
	}

	@Override
	public void printFacilityTreeCacheAll() {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/* メイン処理 */
			FacilityTreeCache.printCache();

			jtm.commit();
		} catch (Exception e) {
			log.warn("printFacilityTreeCacheAll failure...", e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	@Override
	public void refreshFacilityTreeCache() {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/* メイン処理 */
			FacilityTreeCache.refresh();

			jtm.commit();
		} catch (Exception e) {
			log.warn("refreshFacilityTreeCache failure...", e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	@Override
	public long getJobRunSessionCount() {
		return JobRunSessionMonitor.getJobRunSessionCount();
	}

	@Override
	public int getSnmpTrapQueueSize() {
		return SnmpTrapPlugin.getQueuedCount();
	}

	@Override
	public int getSyslogQueueSize() {
		return SystemLogPlugin.getQueuedCount();
	}

	@Override
	public int getWebServiceQueueSize() {
		return WebServiceCorePlugin.getQueueSize();
	}

	@Override
	public long getTablePhysicalSize(String tableName) {
		return TableSizeMonitor.getTableSize(tableName);
	}

	@Override
	public long getTableRecordCount(String tableName) {
		return TableSizeMonitor.getTableCount(tableName);
	}
}
