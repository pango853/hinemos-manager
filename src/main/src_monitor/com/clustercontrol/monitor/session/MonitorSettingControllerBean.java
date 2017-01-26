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

package com.clustercontrol.monitor.session;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.scheduler.TriggerSchedulerException;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.custom.factory.AddCustom;
import com.clustercontrol.custom.factory.DeleteCustom;
import com.clustercontrol.custom.factory.ModifyCustom;
import com.clustercontrol.custom.factory.SelectCustom;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.hinemosagent.factory.AddMonitorAgent;
import com.clustercontrol.hinemosagent.factory.DeleteMonitorAgent;
import com.clustercontrol.hinemosagent.factory.ModifyMonitorAgent;
import com.clustercontrol.hinemosagent.factory.SelectMonitorAgent;
import com.clustercontrol.http.factory.AddMonitorHttp;
import com.clustercontrol.http.factory.AddMonitorHttpScenario;
import com.clustercontrol.http.factory.AddMonitorHttpString;
import com.clustercontrol.http.factory.DeleteMonitorHttp;
import com.clustercontrol.http.factory.DeleteMonitorHttpScenario;
import com.clustercontrol.http.factory.ModifyMonitorHttp;
import com.clustercontrol.http.factory.ModifyMonitorHttpScenario;
import com.clustercontrol.http.factory.ModifyMonitorHttpString;
import com.clustercontrol.http.factory.SelectMonitorHttp;
import com.clustercontrol.http.factory.SelectMonitorHttpScenario;
import com.clustercontrol.jmx.factory.AddMonitorJmx;
import com.clustercontrol.jmx.factory.DeleteMonitorJmx;
import com.clustercontrol.jmx.factory.ModifyMonitorJmx;
import com.clustercontrol.jmx.factory.SelectMonitorJmx;
import com.clustercontrol.logfile.factory.AddMonitorLogfileString;
import com.clustercontrol.logfile.factory.DeleteMonitorLogfile;
import com.clustercontrol.logfile.factory.ModifyMonitorLogfileString;
import com.clustercontrol.logfile.factory.SelectMonitorLogfile;
import com.clustercontrol.monitor.bean.MonitorFilterInfo;
import com.clustercontrol.monitor.plugin.factory.AddMonitorPluginNumeric;
import com.clustercontrol.monitor.plugin.factory.AddMonitorPluginString;
import com.clustercontrol.monitor.plugin.factory.AddMonitorPluginTruth;
import com.clustercontrol.monitor.plugin.factory.DeleteMonitorPlugin;
import com.clustercontrol.monitor.plugin.factory.ModifyMonitorPluginNumeric;
import com.clustercontrol.monitor.plugin.factory.ModifyMonitorPluginString;
import com.clustercontrol.monitor.plugin.factory.ModifyMonitorPluginTruth;
import com.clustercontrol.monitor.plugin.factory.SelectMonitorPlugin;
import com.clustercontrol.monitor.run.bean.MonitorInfo;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.factory.AddMonitor;
import com.clustercontrol.monitor.run.factory.DeleteMonitor;
import com.clustercontrol.monitor.run.factory.ModifyMonitor;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.monitor.run.util.MonitorChangedNotificationCallback;
import com.clustercontrol.monitor.run.util.MonitorValidator;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.performance.monitor.factory.AddMonitorPerformance;
import com.clustercontrol.performance.monitor.factory.DeleteMonitorPerformance;
import com.clustercontrol.performance.monitor.factory.ModifyMonitorPerformance;
import com.clustercontrol.performance.monitor.factory.SelectMonitorPerformance;
import com.clustercontrol.ping.factory.AddMonitorPing;
import com.clustercontrol.ping.factory.DeleteMonitorPing;
import com.clustercontrol.ping.factory.ModifyMonitorPing;
import com.clustercontrol.ping.factory.SelectMonitorPing;
import com.clustercontrol.port.factory.AddMonitorPort;
import com.clustercontrol.port.factory.DeleteMonitorPort;
import com.clustercontrol.port.factory.ModifyMonitorPort;
import com.clustercontrol.port.factory.SelectMonitorPort;
import com.clustercontrol.process.factory.AddMonitorProcess;
import com.clustercontrol.process.factory.DeleteMonitorProcess;
import com.clustercontrol.process.factory.ModifyMonitorProcess;
import com.clustercontrol.process.factory.SelectMonitorProcess;
import com.clustercontrol.snmp.factory.AddMonitorSnmp;
import com.clustercontrol.snmp.factory.AddMonitorSnmpString;
import com.clustercontrol.snmp.factory.DeleteMonitorSnmp;
import com.clustercontrol.snmp.factory.ModifyMonitorSnmp;
import com.clustercontrol.snmp.factory.ModifyMonitorSnmpString;
import com.clustercontrol.snmp.factory.SelectMonitorSnmp;
import com.clustercontrol.snmptrap.factory.AddMonitorTrap;
import com.clustercontrol.snmptrap.factory.DeleteMonitorTrap;
import com.clustercontrol.snmptrap.factory.ModifyMonitorTrap;
import com.clustercontrol.snmptrap.factory.SelectMonitorTrap;
import com.clustercontrol.sql.factory.AddMonitorSql;
import com.clustercontrol.sql.factory.AddMonitorSqlString;
import com.clustercontrol.sql.factory.DeleteMonitorSql;
import com.clustercontrol.sql.factory.ModifyMonitorSql;
import com.clustercontrol.sql.factory.ModifyMonitorSqlString;
import com.clustercontrol.sql.factory.SelectMonitorSql;
import com.clustercontrol.systemlog.factory.AddMonitorSystemlogString;
import com.clustercontrol.systemlog.factory.DeleteMonitorSystemlog;
import com.clustercontrol.systemlog.factory.ModifyMonitorSystemlogString;
import com.clustercontrol.systemlog.factory.SelectMonitorSystemlog;
import com.clustercontrol.systemlog.util.SystemlogCache;
import com.clustercontrol.winevent.factory.AddMonitorWinEvent;
import com.clustercontrol.winevent.factory.DeleteMonitorWinEvent;
import com.clustercontrol.winevent.factory.ModifyMonitorWinEvent;
import com.clustercontrol.winevent.factory.SelectMonitorWinEvent;
import com.clustercontrol.winservice.factory.AddMonitorWinService;
import com.clustercontrol.winservice.factory.DeleteMonitorWinService;
import com.clustercontrol.winservice.factory.ModifyMonitorWinService;
import com.clustercontrol.winservice.factory.SelectMonitorWinService;

/**
 * 監視設定を制御するSesison Bean<BR>
 *
 */
public class MonitorSettingControllerBean {

	// Logger
	private static Log m_log = LogFactory.getLog( MonitorSettingControllerBean.class );

	/**
	 * 監視設定情報をマネージャに登録します。<BR>
	 *
	 * @param info
	 * @return
	 * @throws MonitorDuplicate
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public boolean addMonitor(MonitorInfo info) throws MonitorDuplicate, InvalidSetting, InvalidRole, HinemosUnknown{
		m_log.debug("addMonitor()");

		JpaTransactionManager jtm = null;
		boolean flag = false;

		try {
			jtm = new JpaTransactionManager();

			//入力チェック
			try{
				MonitorValidator.validateMonitorInfo(info);
			} catch (InvalidRole e) {
				throw e;
			} catch (InvalidSetting e) {
				throw e;
			} catch (Exception e) {
				m_log.warn("addMonitor() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw new HinemosUnknown(e.getMessage(), e);
			}

			String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

			AddMonitor addMonitor = null;
			String monitorTypeId = info.getMonitorTypeId();
			int monitorType = info.getMonitorType();
			m_log.debug("addMonitor() monitorTypeId = " + monitorTypeId + ", monitorType = " + monitorType);

			// Hinemos エージェント監視
			if(HinemosModuleConstant.MONITOR_AGENT.equals(monitorTypeId)){
				addMonitor = new AddMonitorAgent();
			}
			// HTTP 監視
			else if (HinemosModuleConstant.MONITOR_HTTP_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_HTTP_S.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_HTTP_SCENARIO.equals(monitorTypeId)) {
				// 数値監視
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					addMonitor = new AddMonitorHttp();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					addMonitor = new AddMonitorHttpString();
				}
				// シナリオ
				else if(MonitorTypeConstant.TYPE_SCENARIO == monitorType){
					addMonitor = new AddMonitorHttpScenario();
				}
				// それ以外
				else{
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.info("addMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			// ログファイル 監視
			else if(HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorTypeId)){
				addMonitor =  new AddMonitorLogfileString();
			}
			// リソース 監視
			else if(HinemosModuleConstant.MONITOR_PERFORMANCE.equals(monitorTypeId)){
				addMonitor = new AddMonitorPerformance();
			}
			// ping監視
			else if(HinemosModuleConstant.MONITOR_PING.equals(monitorTypeId)){
				addMonitor = new AddMonitorPing();
			}
			// ポート監視
			else if(HinemosModuleConstant.MONITOR_PORT.equals(monitorTypeId)){
				addMonitor = new AddMonitorPort();
			}
			// プロセス監視
			else if(HinemosModuleConstant.MONITOR_PROCESS.equals(monitorTypeId)){
				addMonitor = new AddMonitorProcess();
			}
			// SNMPTRAP監視
			else if(HinemosModuleConstant.MONITOR_SNMPTRAP.equals(monitorTypeId)){
				addMonitor = new AddMonitorTrap();
			}
			// SNMP監視
			else if (HinemosModuleConstant.MONITOR_SNMP_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_SNMP_S.equals(monitorTypeId)) {
				// 数値監視
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					addMonitor = new AddMonitorSnmp();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					addMonitor = new AddMonitorSnmpString();
				}
				// それ以外
				else{
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.info("addMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			// システムログ監視
			else if(HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorTypeId)){
				addMonitor = new AddMonitorSystemlogString();
			}
			// SQL監視
			else if (HinemosModuleConstant.MONITOR_SQL_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_SQL_S.equals(monitorTypeId)) {
				// 数値監視
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					addMonitor = new AddMonitorSql();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					addMonitor = new AddMonitorSqlString();
				}
				// それ以外
				else{
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.info("addMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			// コマンド監視
			else if(HinemosModuleConstant.MONITOR_CUSTOM.equals(monitorTypeId)){
				addMonitor = new AddCustom();
			}
			// Windowsサービス監視
			else if(HinemosModuleConstant.MONITOR_WINSERVICE.equals(monitorTypeId)){
				addMonitor = new AddMonitorWinService();
			}
			// Windowsイベント監視
			else if(HinemosModuleConstant.MONITOR_WINEVENT.equals(monitorTypeId)){
				addMonitor = new AddMonitorWinEvent();
			}
			// JMX 監視
			else if(HinemosModuleConstant.MONITOR_JMX.equals(monitorTypeId)){
				addMonitor = new AddMonitorJmx();
			}
			// Other(Pluginで追加する汎用的な監視)
			else{

				// 数値
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					addMonitor = new AddMonitorPluginNumeric();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					addMonitor = new AddMonitorPluginString();
				}
				// 真偽値
				else if(MonitorTypeConstant.TYPE_TRUTH == monitorType){
					addMonitor = new AddMonitorPluginTruth();
				}

				if(addMonitor == null){
					HinemosUnknown e = new HinemosUnknown("Unknown monitorTypeId = " + monitorTypeId + ", monitorType = " + monitorType);
					m_log.info("addMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}

			try {
				jtm.begin();

				flag = addMonitor.add(info, loginUser);

				jtm.addCallback(new MonitorChangedNotificationCallback(monitorTypeId));
				jtm.commit();
			} catch (MonitorDuplicate e) {
				jtm.rollback();
				throw e;
			} catch (MonitorNotFound e) {
				m_log.info("addMonitor " + e.getClass().getName() + ", " + e.getMessage());
				jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} catch (TriggerSchedulerException e) {
				m_log.info("addMonitor " + e.getClass().getName() + ", " + e.getMessage());
				jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} catch (HinemosUnknown e) {
				jtm.rollback();
				throw e;
			} catch (InvalidRole e) {
				jtm.rollback();
				throw e;
			} catch (ObjectPrivilege_InvalidRole e) {
				jtm.rollback();
				throw new InvalidRole(e.getMessage(), e);
			} catch (Exception e) {
				m_log.warn("addMonitor() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} finally {
				if (flag) {
					try {
						// コミット後にキャッシュクリア
						NotifyRelationCache.refresh();

						if (HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorTypeId)) {
							SystemlogCache.refresh();
						}
					} catch (Exception e) {
						m_log.warn("addMonitor() transaction failure. : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						throw new HinemosUnknown(e.getMessage(), e);
					}
				}
			}
		} finally {
			jtm.close();
		}
		return flag;
	}


	/**
	 * 監視設定情報を更新します。<BR>
	 *
	 * @param info
	 * @return
	 * @throws MonitorNotFound
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public boolean modifyMonitor(MonitorInfo info) throws MonitorNotFound, InvalidSetting, InvalidRole, HinemosUnknown {
		m_log.debug("modifyMonitor()");

		JpaTransactionManager jtm = null;
		boolean flag = false;

		try {
			jtm = new JpaTransactionManager();

			//入力チェック
			try{
				MonitorValidator.validateMonitorInfo(info);
			} catch (InvalidSetting e) {
				throw e;
			} catch (InvalidRole e) {
				throw e;
			} catch (Exception e) {
				m_log.warn("modifyMonitor() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw new HinemosUnknown(e.getMessage(), e);
			}

			String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

			ModifyMonitor modMonitor = null;
			String monitorTypeId = info.getMonitorTypeId();
			int monitorType = info.getMonitorType();
			m_log.debug("modifyMonitor() monitorTypeId = " + monitorTypeId + ", monitorType = " + monitorType);


			// Hinemos エージェント監視
			if(HinemosModuleConstant.MONITOR_AGENT.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorAgent();
			}
			// HTTP 監視
			else if (HinemosModuleConstant.MONITOR_HTTP_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_HTTP_S.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_HTTP_SCENARIO.equals(monitorTypeId)) {
				// 数値監視
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					modMonitor = new ModifyMonitorHttp();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					modMonitor = new ModifyMonitorHttpString();
				}
				// シナリオ
				else if(MonitorTypeConstant.TYPE_SCENARIO == monitorType){
					modMonitor = new ModifyMonitorHttpScenario();
				}
				// それ以外
				else{
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.info("modifyMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			// ログファイル 監視
			else if(HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorTypeId)){
				modMonitor =  new ModifyMonitorLogfileString();
			}
			// リソース 監視
			else if(HinemosModuleConstant.MONITOR_PERFORMANCE.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorPerformance();
			}
			// ping監視
			else if(HinemosModuleConstant.MONITOR_PING.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorPing();
			}
			// ポート監視
			else if(HinemosModuleConstant.MONITOR_PORT.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorPort();
			}
			// プロセス監視
			else if(HinemosModuleConstant.MONITOR_PROCESS.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorProcess();
			}
			// SNMPTRAP監視
			else if(HinemosModuleConstant.MONITOR_SNMPTRAP.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorTrap();
			}
			// SNMP監視
			else if (HinemosModuleConstant.MONITOR_SNMP_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_SNMP_S.equals(monitorTypeId)) {
				// 数値監視
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					modMonitor = new ModifyMonitorSnmp();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					modMonitor = new ModifyMonitorSnmpString();
				}
				// それ以外
				else{
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.info("modifyMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			// システムログ監視
			else if(HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorSystemlogString();
			}
			// SQL監視
			else if (HinemosModuleConstant.MONITOR_SQL_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_SQL_S.equals(monitorTypeId)) {
				// 数値監視
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					modMonitor = new ModifyMonitorSql();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					modMonitor = new ModifyMonitorSqlString();
				}
				// それ以外
				else{
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.info("modifyMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			// コマンド監視
			else if(HinemosModuleConstant.MONITOR_CUSTOM.equals(monitorTypeId)){
				modMonitor = new ModifyCustom();
			}
			// Windowsサービス監視
			else if(HinemosModuleConstant.MONITOR_WINSERVICE.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorWinService();
			}
			// Windowsイベント監視
			else if(HinemosModuleConstant.MONITOR_WINEVENT.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorWinEvent();
			}
			// JMX 監視
			else if(HinemosModuleConstant.MONITOR_JMX.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorJmx();
			}
			// Other
			else{

				// 数値
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					modMonitor = new ModifyMonitorPluginNumeric();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					modMonitor = new ModifyMonitorPluginString();
				}
				// 真偽値
				else if(MonitorTypeConstant.TYPE_TRUTH == monitorType){
					modMonitor = new ModifyMonitorPluginTruth();
				}

				if(modMonitor == null){
					HinemosUnknown e = new HinemosUnknown("Unknown monitorTypeId = " + monitorTypeId + ", monitorType = " + monitorType);
					m_log.info("modifyMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}

			// 監視設定情報を更新
			try {
				jtm.begin();

				flag = modMonitor.modify(info, loginUser);

				jtm.addCallback(new MonitorChangedNotificationCallback(monitorTypeId));
				jtm.commit();
			} catch (MonitorNotFound e) {
				jtm.rollback();
				throw e;
			} catch (TriggerSchedulerException e) {
				m_log.info("modifyMonitor " + e.getClass().getName() + ", " + e.getMessage());
				jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} catch (HinemosUnknown e) {
				jtm.rollback();
				throw e;
			} catch (InvalidRole e) {
				jtm.rollback();
				throw e;
			} catch (ObjectPrivilege_InvalidRole e) {
				jtm.rollback();
				throw new InvalidRole(e.getMessage(), e);
			} catch (Exception e) {
				m_log.warn("modifyMonitor() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} finally {
				if (flag) {
					try {
						// コミット後にキャッシュクリア
						NotifyRelationCache.refresh();

						if (HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorTypeId)) {
							SystemlogCache.refresh();
						}
					} catch (Exception e) {
						m_log.warn("modifyMonitor() transaction failure. : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						throw new HinemosUnknown(e.getMessage(), e);
					}
				}
			}
		} finally {
			jtm.close();
		}
		return flag;
	}


	/**
	 *
	 * 監視設定情報をマネージャから削除します。<BR>
	 *
	 * @param monitorIdList
	 * @param monitorTypeId
	 * @return
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public boolean deleteMonitor(List<String> monitorIdList, String monitorTypeId) throws MonitorNotFound, HinemosUnknown, InvalidRole {
		m_log.debug("deleteMonitor() monitorId = " + monitorIdList + ", monitorTypeId = " + monitorTypeId);

		JpaTransactionManager jtm = null;

		// null チェック
		if(monitorIdList == null || monitorIdList.isEmpty()){
			HinemosUnknown e = new HinemosUnknown("monitorIdList is null or empty.");
			m_log.info("deleteMonitor() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		if(monitorTypeId == null || "".equals(monitorTypeId)){
			HinemosUnknown e = new HinemosUnknown("monitorTypeId is null or empty.");
			m_log.info("deleteMonitor() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		DeleteMonitor deleteMonitor = null;
		// Hinemos エージェント監視
		if(HinemosModuleConstant.MONITOR_AGENT.equals(monitorTypeId)){
			deleteMonitor = new DeleteMonitorAgent();
		}
		// HTTP 監視
		else if (HinemosModuleConstant.MONITOR_HTTP_N.equals(monitorTypeId)
				|| HinemosModuleConstant.MONITOR_HTTP_S.equals(monitorTypeId)) {
			deleteMonitor = new DeleteMonitorHttp();
		}
		// HTTP シナリオ監視
		else if (HinemosModuleConstant.MONITOR_HTTP_SCENARIO.equals(monitorTypeId)) {
			deleteMonitor = new DeleteMonitorHttpScenario();
		}
		// ログファイル 監視
		else if(HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorTypeId)){
			deleteMonitor =  new DeleteMonitorLogfile();
		}
		// リソース 監視
		else if(HinemosModuleConstant.MONITOR_PERFORMANCE.equals(monitorTypeId)){
			deleteMonitor = new DeleteMonitorPerformance();
		}
		// ping監視
		else if(HinemosModuleConstant.MONITOR_PING.equals(monitorTypeId)){
			deleteMonitor = new DeleteMonitorPing();
		}
		// ポート監視
		else if(HinemosModuleConstant.MONITOR_PORT.equals(monitorTypeId)){
			deleteMonitor = new DeleteMonitorPort();
		}
		// プロセス監視
		else if(HinemosModuleConstant.MONITOR_PROCESS.equals(monitorTypeId)){
			deleteMonitor = new DeleteMonitorProcess();
		}
		// SNMPTRAP監視
		else if(HinemosModuleConstant.MONITOR_SNMPTRAP.equals(monitorTypeId)){
			deleteMonitor = new DeleteMonitorTrap();
		}
		// SNMP監視
		else if (HinemosModuleConstant.MONITOR_SNMP_N.equals(monitorTypeId)
				|| HinemosModuleConstant.MONITOR_SNMP_S.equals(monitorTypeId)) {
			deleteMonitor = new DeleteMonitorSnmp();
		}
		// システムログ監視
		else if(HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorTypeId)){
			deleteMonitor = new DeleteMonitorSystemlog();
		}
		// SQL監視
		else if (HinemosModuleConstant.MONITOR_SQL_N.equals(monitorTypeId)
				|| HinemosModuleConstant.MONITOR_SQL_S.equals(monitorTypeId)) {
			deleteMonitor = new DeleteMonitorSql();
		}
		// コマンド監視
		else if(HinemosModuleConstant.MONITOR_CUSTOM.equals(monitorTypeId)){
			deleteMonitor = new DeleteCustom();
		}
		// Windowsサービス監視
		else if(HinemosModuleConstant.MONITOR_WINSERVICE.equals(monitorTypeId)){
			deleteMonitor = new DeleteMonitorWinService();
		}
		// Windowsイベント監視
		else if(HinemosModuleConstant.MONITOR_WINEVENT.equals(monitorTypeId)){
			deleteMonitor = new DeleteMonitorWinEvent();
		}
		// JMX 監視
		else if(HinemosModuleConstant.MONITOR_JMX.equals(monitorTypeId)){
			deleteMonitor = new DeleteMonitorJmx();
		}
		// Other
		else{
			deleteMonitor = new DeleteMonitorPlugin();
		}

		// 監視設定情報を削除
		boolean flag = false;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for (String monitorId : monitorIdList) {
				flag = deleteMonitor.delete(monitorTypeId, monitorId);
			}

			jtm.addCallback(new MonitorChangedNotificationCallback(monitorTypeId));
			jtm.commit();
		} catch (MonitorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (TriggerSchedulerException e) {
			m_log.info("deleteMonitor " + e.getClass().getName() + ", " + e.getMessage());
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("deleteMonitor() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (flag) {
				try {
					// コミット後にキャッシュクリア
					NotifyRelationCache.refresh();

					if (HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorTypeId)) {
						SystemlogCache.refresh();
					}
				} catch (Exception e) {
					m_log.warn("deleteMonitor() transaction failure. : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				} finally {
					jtm.close();
				}
			}
			jtm.close();
		}
		return flag;
	}


	/**
	 * 監視情報を取得します。<BR>
	 *
	 * @param monitorId
	 * @param monitorTypeId
	 * @return
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public MonitorInfo getMonitor(String monitorId, String monitorTypeId) throws MonitorNotFound, HinemosUnknown, InvalidRole {
		m_log.debug("getMonitor() monitorId = " + monitorId + ", monitorTypeId = " + monitorTypeId);

		JpaTransactionManager jtm = null;

		// null チェック
		if(monitorId == null || "".equals(monitorId)){
			HinemosUnknown e = new HinemosUnknown("monitorId is null or empty.");
			m_log.info("getMonitor() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		if(monitorTypeId == null || "".equals(monitorTypeId)){
			HinemosUnknown e = new HinemosUnknown("monitorTypeId is null or empty.");
			m_log.info("getMonitor() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		SelectMonitor selectMonitor = null;
		// Hinemos エージェント監視
		if(HinemosModuleConstant.MONITOR_AGENT.equals(monitorTypeId)){
			selectMonitor = new SelectMonitorAgent();
		}
		// HTTP 監視
		else if (HinemosModuleConstant.MONITOR_HTTP_N.equals(monitorTypeId)
				|| HinemosModuleConstant.MONITOR_HTTP_S.equals(monitorTypeId)) {
			selectMonitor = new SelectMonitorHttp();
		}
		// HTTP 監視
		else if (HinemosModuleConstant.MONITOR_HTTP_SCENARIO.equals(monitorTypeId)) {
			selectMonitor = new SelectMonitorHttpScenario();
		}
		// ログファイル 監視
		else if(HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorTypeId)){
			selectMonitor =  new SelectMonitorLogfile();
		}
		// リソース 監視
		else if(HinemosModuleConstant.MONITOR_PERFORMANCE.equals(monitorTypeId)){
			selectMonitor = new SelectMonitorPerformance();
		}
		// ping監視
		else if(HinemosModuleConstant.MONITOR_PING.equals(monitorTypeId)){
			selectMonitor = new SelectMonitorPing();
		}
		// ポート監視
		else if(HinemosModuleConstant.MONITOR_PORT.equals(monitorTypeId)){
			selectMonitor = new SelectMonitorPort();
		}
		// プロセス監視
		else if(HinemosModuleConstant.MONITOR_PROCESS.equals(monitorTypeId)){
			selectMonitor = new SelectMonitorProcess();
		}
		// SNMPTRAP監視
		else if(HinemosModuleConstant.MONITOR_SNMPTRAP.equals(monitorTypeId)){
			selectMonitor = new SelectMonitorTrap();
		}
		// SNMP監視
		else if (HinemosModuleConstant.MONITOR_SNMP_N.equals(monitorTypeId)
				|| HinemosModuleConstant.MONITOR_SNMP_S.equals(monitorTypeId)) {
			selectMonitor = new SelectMonitorSnmp();
		}
		// システムログ監視
		else if(HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorTypeId)){
			selectMonitor = new SelectMonitorSystemlog();
		}
		// SQL監視
		else if (HinemosModuleConstant.MONITOR_SQL_N.equals(monitorTypeId)
				|| HinemosModuleConstant.MONITOR_SQL_S.equals(monitorTypeId)) {
			selectMonitor = new SelectMonitorSql();
		}
		// コマンド監視
		else if(HinemosModuleConstant.MONITOR_CUSTOM.equals(monitorTypeId)){
			selectMonitor = new SelectCustom();
		}
		// Windowsサービス監視
		else if(HinemosModuleConstant.MONITOR_WINSERVICE.equals(monitorTypeId)){
			selectMonitor = new SelectMonitorWinService();
		}
		// Windowsイベント監視
		else if(HinemosModuleConstant.MONITOR_WINEVENT.equals(monitorTypeId)){
			selectMonitor = new SelectMonitorWinEvent();
		}
		// JMXイベント監視
		else if(HinemosModuleConstant.MONITOR_JMX.equals(monitorTypeId)){
			selectMonitor = new SelectMonitorJmx();
		}
		// Other
		else{
			selectMonitor = new SelectMonitorPlugin();
		}

		// 監視設定情報を取得
		MonitorInfo info = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			info = selectMonitor.getMonitor(monitorTypeId, monitorId);
			jtm.commit();
		} catch (MonitorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getMonitor() "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return info;
	}

	/**
	 * 監視設定一覧を取得する
	 *
	 * @return
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getMonitorList() throws InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;
		ArrayList<MonitorInfo> list = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			long start = System.currentTimeMillis();
			list = new SelectMonitor().getMonitorList();
			long end = System.currentTimeMillis();
			m_log.info("getMonitorList " + (end-start) + "ms");

			jtm.commit();
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getMonitorList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return list;
	}

	/**
	 * Hinemos Agent監視一覧リストを返します。
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getAgentList() throws MonitorNotFound, InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = null;

		// Hinemos Agent監視一覧を取得
		SelectMonitorAgent agent = new SelectMonitorAgent();
		ArrayList<MonitorInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = agent.getMonitorList(HinemosModuleConstant.MONITOR_AGENT);
			jtm.commit();
		} catch (MonitorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getAgentList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			jtm.close();
		}

		// null check
		if(list == null){
			list = new ArrayList<MonitorInfo>();
		}
		return list;
	}

	/**
	 * HTTP監視一覧リストを取得します。<BR>
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getHttpList() throws MonitorNotFound, InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = null;

		// HTTP監視一覧を取得
		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectMonitorHttp http = new SelectMonitorHttp();
			list.addAll(http.getMonitorList(HinemosModuleConstant.MONITOR_HTTP_N));
			list.addAll(http.getMonitorList(HinemosModuleConstant.MONITOR_HTTP_S));
			SelectMonitorHttpScenario scenario = new SelectMonitorHttpScenario();
			list.addAll(scenario.getMonitorList(HinemosModuleConstant.MONITOR_HTTP_SCENARIO));
			jtm.commit();
		} catch (MonitorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getHttpList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			jtm.close();
		}

		return list;
	}
	/**
	 * JMX監視一覧リストを取得します。<BR>
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getJmxList() throws MonitorNotFound, InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = null;

		// HTTP監視一覧を取得
		SelectMonitorJmx jmx = new SelectMonitorJmx();
		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list.addAll(jmx.getMonitorList(HinemosModuleConstant.MONITOR_JMX));
			jtm.commit();
		} catch (MonitorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getJmxList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			jtm.close();
		}

		return list;
	}

	/**
	 * ログファイル監視一覧リストを返します。
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getLogfileList() throws MonitorNotFound, InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;

		SelectMonitorLogfile logfile = new SelectMonitorLogfile();
		ArrayList<MonitorInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = logfile.getMonitorList(HinemosModuleConstant.MONITOR_LOGFILE);
			jtm.commit();
		} catch (MonitorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getLogfileList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			jtm.close();
		}

		// null check
		if(list == null){
			list = new ArrayList<MonitorInfo>();
		}
		return list;
	}

	/**
	 * リソース監視一覧リストを返します。
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getPerformanceList() throws MonitorNotFound, InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;

		// リソース監視一覧を取得
		SelectMonitorPerformance selectPerf = new SelectMonitorPerformance();
		ArrayList<MonitorInfo> list;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = selectPerf.getMonitorList(HinemosModuleConstant.MONITOR_PERFORMANCE);
			jtm.commit();
		} catch (MonitorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getPerformanceList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			jtm.close();
		}

		// null check
		if(list == null){
			list = new ArrayList<MonitorInfo>();
		}
		return list;
	}

	/**
	 * ping監視一覧リストを取得します。<BR>
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getPingList() throws MonitorNotFound, InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = null;

		// ping監視一覧を取得
		SelectMonitorPing ping = new SelectMonitorPing();
		ArrayList<MonitorInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = ping.getMonitorList(HinemosModuleConstant.MONITOR_PING);
			jtm.commit();
		} catch (MonitorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getPingList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			jtm.close();
		}

		// null check
		if(list == null){
			list = new ArrayList<MonitorInfo>();
		}
		return list;
	}

	/**
	 * port監視一覧リストを返します。
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getPortList() throws MonitorNotFound, InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;

		// port監視一覧を取得
		SelectMonitorPort port = new SelectMonitorPort();
		ArrayList<MonitorInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = port.getMonitorList(HinemosModuleConstant.MONITOR_PORT);
			jtm.commit();
		} catch (MonitorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getPortList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			jtm.close();
		}

		// null check
		if(list == null){
			list = new ArrayList<MonitorInfo>();
		}
		return list;
	}

	/**
	 * プロセス監視一覧リストを取得します。<BR>
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getProcessList() throws MonitorNotFound, InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;

		// プロセス監視一覧を取得
		SelectMonitorProcess process = new SelectMonitorProcess();
		ArrayList<MonitorInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = process.getMonitorList(HinemosModuleConstant.MONITOR_PROCESS);
			jtm.commit();
		} catch (MonitorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getProcessList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			jtm.close();
		}

		// null check
		if(list == null){
			list = new ArrayList<MonitorInfo>();
		}
		return list;
	}

	/**
	 * SNMPTRAP監視一覧リストを取得します。<BR>
	 *
	 * @return rrayList<MonitorInfo>
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getTrapList() throws MonitorNotFound, InvalidRole, HinemosUnknown{
		m_log.debug("getTrapList() : start");

		JpaTransactionManager jtm = null;

		// SNMPTRAP監視一覧を取得
		SelectMonitorTrap trap = new SelectMonitorTrap();
		ArrayList<MonitorInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = trap.getMonitorList(HinemosModuleConstant.MONITOR_SNMPTRAP);
			jtm.commit();
		} catch (MonitorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getTrapList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			jtm.close();
		}

		m_log.debug("getTrapList() : end");
		// null check
		if(list == null){
			list = new ArrayList<MonitorInfo>();
		}
		return list;
	}

	/**
	 * SNMP監視一覧リストを取得します。<BR>
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getSnmpList() throws MonitorNotFound, InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = null;

		// SNMP監視一覧を取得
		SelectMonitorSnmp snmp = new SelectMonitorSnmp();
		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list.addAll(snmp.getMonitorList(HinemosModuleConstant.MONITOR_SNMP_N));
			list.addAll(snmp.getMonitorList(HinemosModuleConstant.MONITOR_SNMP_S));
			jtm.commit();
		} catch (MonitorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getSnmpList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			jtm.close();
		}

		return list;
	}

	/**
	 * SQL監視一覧リストを取得します。
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getSqlList() throws MonitorNotFound, InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;

		// SQL監視一覧を取得
		SelectMonitorSql sql = new SelectMonitorSql();
		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list.addAll(sql.getMonitorList(HinemosModuleConstant.MONITOR_SQL_N));
			list.addAll(sql.getMonitorList(HinemosModuleConstant.MONITOR_SQL_S));
			jtm.commit();
		} catch (MonitorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getSqlList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			jtm.close();
		}

		return list;
	}

	/**
	 * システムログ監視一覧リストを返します。
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getSystemlogList() throws MonitorNotFound, InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;

		// システムログ監視一覧を取得
		SelectMonitorSystemlog systemlog = new SelectMonitorSystemlog();
		ArrayList<MonitorInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = systemlog.getMonitorList(HinemosModuleConstant.MONITOR_SYSTEMLOG);
			jtm.commit();
		} catch (MonitorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getSystemlogList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			jtm.close();
		}

		// null check
		if(list == null){
			list = new ArrayList<MonitorInfo>();
		}
		return list;
	}

	/**
	 * システムログ監視一覧リストを返します。
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getSystemlogMonitorCache() throws MonitorNotFound, InvalidRole, HinemosUnknown{
		ArrayList<MonitorInfo> list = null;
		try {
			list = SystemlogCache.getSystemlogList();
		} catch (MonitorNotFound e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("getSystemlogList " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		// null check
		if(list == null){
			list = new ArrayList<MonitorInfo>();
		}
		return list;
	}

	/**
	 * 既存のコマンド監視の一覧を返す
	 * @return コマンド監視の設定情報一覧
	 * @throws MonitorNotFound 一覧にIDが存在するが、詳細情報が存在しなかった場合
	 * @throws HinemosUnknown 予期せぬエラーが発生した場合
	 */
	public ArrayList<MonitorInfo> getCustomList() throws MonitorNotFound, InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = null;
		SelectCustom selector = new SelectCustom();
		ArrayList<MonitorInfo> list = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = selector.getMonitorList(HinemosModuleConstant.MONITOR_CUSTOM);
			jtm.commit();
		} catch (MonitorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getCustomList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			jtm.close();
		}

		// null check
		if(list == null){
			list = new ArrayList<MonitorInfo>();
		}
		return list;
	}


	/**
	 * Windowsサービス監視一覧リストを取得します。<BR>
	 *
	 * @return MonitorInfoのリスト
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getWinServiceList() throws MonitorNotFound, InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;

		// Windowsサービス監視一覧を取得
		SelectMonitorWinService winService = new SelectMonitorWinService();
		ArrayList<MonitorInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = winService.getMonitorList(HinemosModuleConstant.MONITOR_WINSERVICE);
			jtm.commit();
		} catch (MonitorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getWinServiceList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			jtm.close();
		}

		// null check
		if(list == null){
			list = new ArrayList<MonitorInfo>();
		}
		return list;
	}

	/**
	 * Windowsイベント監視一覧リストを取得します。<BR>
	 *
	 * @return MonitorInfoのリスト
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getWinEventList() throws MonitorNotFound, InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;

		// Windowsイベント監視一覧を取得
		SelectMonitorWinEvent winEvent = new SelectMonitorWinEvent();
		ArrayList<MonitorInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = winEvent.getMonitorList(HinemosModuleConstant.MONITOR_WINEVENT);
			jtm.commit();
		} catch (MonitorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getWinEventList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			jtm.close();
		}

		// null check
		if(list == null){
			list = new ArrayList<MonitorInfo>();
		}
		return list;
	}

	/**
	 *
	 * 監視設定の監視を有効化/無効化します。<BR>
	 *
	 * @param monitorId
	 * @param monitorTypeId
	 * @throws HinemosUnknown
	 * @throws MonitorNotFound
	 */
	public void setStatusMonitor(String monitorId, String monitorTypeId, boolean validFlag) throws HinemosUnknown, MonitorNotFound, InvalidRole {
		// null check
		if(monitorId == null || "".equals(monitorId)){
			HinemosUnknown e = new HinemosUnknown("target monitorId is null or empty.");
			m_log.info("setStatusMonitor() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		if(monitorTypeId == null || "".equals(monitorTypeId)){
			HinemosUnknown e = new HinemosUnknown("target monitorTypeId is null or empty.");
			m_log.info("setStatusMonitor() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		MonitorInfo info = getMonitor(monitorId, monitorTypeId);
		try{
			// オブジェクト権限チェック
			QueryUtil.getMonitorInfoPK(monitorId, ObjectPrivilegeMode.MODIFY);

			if (validFlag) {
				if(info.getMonitorFlg() != ValidConstant.TYPE_VALID){
					info.setMonitorFlg(ValidConstant.TYPE_VALID);
					modifyMonitor(info);
				}
			} else {
				if(info.getMonitorFlg() != ValidConstant.TYPE_INVALID){
					info.setMonitorFlg(ValidConstant.TYPE_INVALID);
					modifyMonitor(info);
				}
			}
		} catch (InvalidSetting e) {
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (MonitorNotFound e) {
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(e.getMessage(), e);
		} catch (InvalidRole e) {
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		}
	}

	/**
	 *
	 * 監視設定の収集を有効化/無効化します。<BR>
	 *
	 * @param monitorId
	 * @param monitorTypeId
	 * @throws HinemosUnknown
	 * @throws MonitorNotFound
	 */
	public void setStatusCollector(String monitorId, String monitorTypeId, boolean validFlag) throws HinemosUnknown, MonitorNotFound, InvalidRole {
		// null check
		if(monitorId == null || "".equals(monitorId)){
			HinemosUnknown e = new HinemosUnknown("target monitorId is null or empty.");
			m_log.info("setStatusCollector() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		if(monitorTypeId == null || "".equals(monitorTypeId)){
			HinemosUnknown e = new HinemosUnknown("target monitorTypeId is null or empty.");
			m_log.info("setStatusCollector() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		MonitorInfo info = getMonitor(monitorId, monitorTypeId);
		try{
			// オブジェクト権限チェック
			QueryUtil.getMonitorInfoPK(monitorId, ObjectPrivilegeMode.MODIFY);

			if (validFlag) {
				if(info.getMonitorType() != MonitorTypeConstant.TYPE_NUMERIC &&
						info.getMonitorType() != MonitorTypeConstant.TYPE_SCENARIO){
					m_log.debug("setStatusMonitor() : monitorId = " + monitorId + " is not numeric and scenario.");
					return;
				}

				if(info.getCollectorFlg() != ValidConstant.TYPE_VALID){
					info.setCollectorFlg(ValidConstant.TYPE_VALID);
					modifyMonitor(info);
				}
			} else {
				if(info.getCollectorFlg() != ValidConstant.TYPE_INVALID){
					info.setCollectorFlg(ValidConstant.TYPE_INVALID);
					modifyMonitor(info);
				}
			}
		} catch (InvalidSetting e) {
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (MonitorNotFound e) {
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(e.getMessage(), e);
		} catch (InvalidRole e) {
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		}
	}

	/**
	 * 監視設定一覧の取得
	 *
	 * @param condition フィルタ条件
	 * @return 監視設定一覧
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getMonitorList(MonitorFilterInfo condition) throws InvalidRole, HinemosUnknown {
		m_log.debug("getMonitorList(MonitorFilterInfo) : start");

		JpaTransactionManager jtm = null;

		ArrayList<MonitorInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = new SelectMonitor().getMonitorList(condition);
			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (MonitorNotFound e) {
			m_log.info("getMonitorList " + e.getClass().getName() + ", " + e.getMessage());
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getMonitorList(condition) : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			jtm.close();
		}

		m_log.debug("getMonitorList(condition) : end");
		return list;
	}
}
