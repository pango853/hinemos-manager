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

package com.clustercontrol.performance.monitor.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.YesNoConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.AddMonitorNumericValueType;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.performance.monitor.bean.PerfCheckInfo;
import com.clustercontrol.performance.monitor.model.MonitorPerfInfoEntity;
import com.clustercontrol.performance.util.PerformanceProperties;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;


/**
 * リソース監視情報登録クラス
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class AddMonitorPerformance extends AddMonitorNumericValueType{

	private static Log m_log = LogFactory.getLog( AddMonitorPerformance.class );

	/**
	 *  リソース監視情報を登録します。
	 * @see com.clustercontrol.monitor.run.factory.AddMonitor#addCheckInfo()
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, HinemosUnknown, InvalidRole {
		m_log.debug("addCheckInfo() monitorId = " + m_monitorInfo.getMonitorId());

		MonitorInfoEntity monitorEntity = QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		// リソース監視情報を設定
		PerfCheckInfo perf = m_monitorInfo.getPerfCheckInfo();

		MonitorPerfInfoEntity entity = new MonitorPerfInfoEntity(monitorEntity);
		entity.setBreakdownFlg(perf.getBreakdownFlg());
		entity.setDeviceDisplayName(perf.getDeviceDisplayName());
		entity.setItemCode(perf.getItemCode());

		// 監視または収集が有効の場合にポーラに登録
		boolean result = true;
		if(m_monitorInfo.getMonitorFlg() == YesNoConstant.TYPE_YES
				|| m_monitorInfo.getCollectorFlg() == YesNoConstant.TYPE_YES){
			ModifyPollingSchedule poller = new ModifyPollingSchedule();
			result = poller.addSchedule(m_monitorInfo);
		}
		return result;
	}

	/**
	 * スケジュール実行の遅延時間を返します。
	 */
	@Override
	protected int getDelayTime() {
		//TODO 59に限定しない
		// 値取得開始時間（秒）設定が60以上のものは59秒に設定する
		int startSecond = PerformanceProperties.getProperties().getStartSecond();
		if(startSecond >= 60){
			startSecond = 59;
		}

		return startSecond;
	}

	/**
	 * スケジュール実行種別を返します。
	 */
	@Override
	protected TriggerType getTriggerType() {
		return TriggerType.CRON;
	}
}
