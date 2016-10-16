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

package com.clustercontrol.monitor.run.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.ObjectSharingService;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hinemosagent.factory.RunMonitorAgent;
import com.clustercontrol.http.factory.RunMonitorHttp;
import com.clustercontrol.http.factory.RunMonitorHttpScenario;
import com.clustercontrol.http.factory.RunMonitorHttpString;
import com.clustercontrol.jmx.factory.RunMonitorJmx;
import com.clustercontrol.monitor.plugin.factory.RunMonitorPluginSample;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.performance.monitor.factory.RunMonitorPerformance;
import com.clustercontrol.ping.factory.RunMonitorPing;
import com.clustercontrol.port.factory.RunMonitorPort;
import com.clustercontrol.process.factory.RunMonitorProcess;
import com.clustercontrol.snmp.factory.RunMonitorSnmp;
import com.clustercontrol.snmp.factory.RunMonitorSnmpString;
import com.clustercontrol.sql.factory.RunMonitorSql;
import com.clustercontrol.sql.factory.RunMonitorSqlString;
import com.clustercontrol.winservice.factory.RunMonitorWinService;

/**
 *  * Quartzから呼びだされて監視を実行するSession Bean<BR>
 * 
 * 
 */
public class MonitorRunManagementBean {

	// Logger
	private static Log m_log = LogFactory.getLog( MonitorRunManagementBean.class );

	/**
	 * Quartzからのコールバックメソッド
	 * 
	 * トランザクション開始はユーザが制御する。
	 * また、追加実装により、トランザクションの入れ子が予期せず生じることを避けるため、Neverを採用する。
	 * 
	 * @param monitorTypeId 監視監視対象ID
	 * @param monitorId 監視項目ID
	 * @param monitorType 監視判定タイプ
	 * @throws FacilityNotFound
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 * @version 4.0.0
	 * @since 4.0.0
	 */
	public void run(String monitorTypeId, String monitorId, Integer monitorType) throws FacilityNotFound, MonitorNotFound, HinemosUnknown {
		m_log.debug("run() monitorTypeId = " + monitorTypeId + ", monitorId = " + monitorId + ", monitorType = " + monitorType);

		JpaTransactionManager jtm = null;

		// null チェック
		if(monitorId == null || "".equals(monitorId)){
			HinemosUnknown e = new HinemosUnknown("monitorId is null or empty.");
			m_log.warn("run() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			throw e;

		}
		if(monitorTypeId == null || "".equals(monitorTypeId)){
			HinemosUnknown e = new HinemosUnknown("monitorTypeId is null or empty.");
			m_log.warn("run() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			throw e;
		}

		RunMonitor runMonitor = null;
		try {
			// Hinemos エージェント監視
			if(HinemosModuleConstant.MONITOR_AGENT.equals(monitorTypeId)){
				runMonitor = new RunMonitorAgent();
			}
			// HTTP 監視
			else if (HinemosModuleConstant.MONITOR_HTTP_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_HTTP_S.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_HTTP_SCENARIO.equals(monitorTypeId)
					) {
				// 数値監視
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					runMonitor = new RunMonitorHttp();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					runMonitor = new RunMonitorHttpString();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_SCENARIO == monitorType){
					runMonitor = new RunMonitorHttpScenario();
				}
				// それ以外
				else{
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.warn("run() : " + e.getClass().getSimpleName() +
							", " + e.getMessage(), e);
					throw e;
				}
			}
			// ログファイル 監視
			else if(HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorTypeId)){
				return;
			}
			// リソース 監視
			else if(HinemosModuleConstant.MONITOR_PERFORMANCE.equals(monitorTypeId)){
				runMonitor = new RunMonitorPerformance();
			}
			// ping監視
			else if(HinemosModuleConstant.MONITOR_PING.equals(monitorTypeId)){
				runMonitor = new RunMonitorPing();
			}
			// ポート監視
			else if(HinemosModuleConstant.MONITOR_PORT.equals(monitorTypeId)){
				runMonitor = new RunMonitorPort();
			}
			// プロセス監視
			else if(HinemosModuleConstant.MONITOR_PROCESS.equals(monitorTypeId)){
				runMonitor = new RunMonitorProcess();
			}
			// SNMPTRAP監視
			else if(HinemosModuleConstant.MONITOR_SNMPTRAP.equals(monitorTypeId)){
				return;
			}
			// SNMP監視
			else if (HinemosModuleConstant.MONITOR_SNMP_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_SNMP_S.equals(monitorTypeId)) {
				// 数値監視
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					runMonitor = new RunMonitorSnmp();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					runMonitor = new RunMonitorSnmpString();
				}
				// それ以外
				else{
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.warn("run() : " + e.getClass().getSimpleName() +
							", " + e.getMessage(), e);
					throw e;
				}
			}
			// システムログ監視
			else if(HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorTypeId)){
				return;
			}
			// SQL監視
			else if (HinemosModuleConstant.MONITOR_SQL_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_SQL_S.equals(monitorTypeId)) {
				// 数値監視
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					runMonitor = new RunMonitorSql();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					runMonitor = new RunMonitorSqlString();
				}
				// それ以外
				else{
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.warn("run() : " + e.getClass().getSimpleName() +
							", " + e.getMessage(), e);
					throw e;
				}
			}
			// コマンド監視
			else if(HinemosModuleConstant.MONITOR_CUSTOM.equals(monitorTypeId)){
				return;
			}
			// Windowsサービス監視
			else if(HinemosModuleConstant.MONITOR_WINSERVICE.equals(monitorTypeId)){
				runMonitor = new RunMonitorWinService();
			}
			// Windowsイベント監視
			else if(HinemosModuleConstant.MONITOR_WINEVENT.equals(monitorTypeId)){
				return;
			}
			// JMX監視
			else if(HinemosModuleConstant.MONITOR_JMX.equals(monitorTypeId)){
				runMonitor = new RunMonitorJmx();
			}
			// Other
			else{
				try {
					runMonitor = ObjectSharingService.objectRegistry().get(RunMonitor.class, monitorTypeId + "." + monitorType);
				} catch (Exception e) {
					m_log.warn("run() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw e;
				}

				if(runMonitor == null){
					//監視のサンプル実装
					runMonitor = new RunMonitorPluginSample();

					/*
					HinemosUnknown e = new HinemosUnknown("Unknown monitorTypeId = " + monitorTypeId + ", monitorType = " + monitorType);
					m_log.warn("run() : " + e.getClass().getSimpleName() +
							", " + e.getMessage(), e);
					throw e;
					*/
				}
			}
		} catch (Exception e) {
			m_log.warn("run() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		if(runMonitor != null){
			try{
				// トランザクションがすでに開始されている場合は処理終了
				jtm = new JpaTransactionManager();
				jtm.begin(true);

				runMonitor.run(monitorTypeId, monitorId);

				jtm.commit();
			}catch(FacilityNotFound e){
				jtm.rollback();
				throw e;
			}catch(MonitorNotFound e){
				jtm.rollback();
				throw e;
			}catch(HinemosUnknown e){
				jtm.rollback();
				throw e;
			}catch(Exception e){
				m_log.warn("run() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} finally {
				jtm.close();
			}
		}
	}
}
