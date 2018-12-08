/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 */

package com.clustercontrol.performance.util.code;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.YesNoConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.bean.MonitorInfo;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.performance.monitor.factory.ModifyPollingSchedule;

/**
 * リソース監視機能用性能値収集を再実行するクラス
 *
 */
public class PerformanceRestartManager {
	private static Log m_log = LogFactory.getLog( PerformanceRestartManager.class );

	/**
	 * 状態が「実行中」である収集を再開します
	 */
	public void restartAll() {
		m_log.info("MONITOR_PERFORMANCE : poller restart!!");

		long start = System.currentTimeMillis();
		int size = 0;
		try{
			// 監視一覧を取得
			ArrayList<MonitorInfo> monitorInfoList = new MonitorSettingControllerBean().getPerformanceList();
			size = monitorInfoList.size();

			// リソース監視なら再スケジュール
			ModifyPollingSchedule poller = new ModifyPollingSchedule();
			for(MonitorInfo monitorInfo : monitorInfoList){
				// ポーラに登録
				m_log.debug("restartAll() target monitorId = " + monitorInfo.getMonitorId()
						+ ", MonitorFlg = " + monitorInfo.getMonitorFlg()
						+ ", CollectorFlg = " + monitorInfo.getCollectorFlg());

				boolean status = true;
				if(monitorInfo.getMonitorFlg() == YesNoConstant.TYPE_YES
						|| monitorInfo.getCollectorFlg() == YesNoConstant.TYPE_YES){
					status = poller.addSchedule(monitorInfo);
				}

				if(!status){
					m_log.info("restartAll() monitorId = " + monitorInfo.getMonitorId());
				}
			}

		} catch (HinemosUnknown e) {
			m_log.debug("restartAll() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		} catch (MonitorNotFound e) {
			m_log.debug("restartAll() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		} catch (Exception e) {
			m_log.warn("restartAll() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
		long end = System.currentTimeMillis();
		m_log.info("restartAll() : " + (end - start) + "ms. PerfMonitor size=" + size);
	}

}
