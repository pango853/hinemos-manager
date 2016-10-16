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

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitorNumericValueType;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.performance.monitor.bean.PerfCheckInfo;
import com.clustercontrol.performance.monitor.model.MonitorPerfInfoEntity;
import com.clustercontrol.performance.monitor.util.QueryUtil;
import com.clustercontrol.performance.util.PerformanceProperties;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * リソース監視情報更新クラス
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class ModifyMonitorPerformance extends ModifyMonitorNumericValueType{

	private static Log m_log = LogFactory.getLog( ModifyMonitorPerformance.class );

	/**
	 * リソース監視判定情報を更新します。
	 * @see com.clustercontrol.monitor.run.factory.ModifyMonitor#modifyCheckInfo()
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, HinemosUnknown, InvalidRole {
		m_log.debug("modifyCheckInfo() monitorId = " + m_monitorInfo.getMonitorId());

		MonitorInfoEntity monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		// 閾値監視情報を取得
		MonitorPerfInfoEntity entity = QueryUtil.getMonitorPerfInfoPK(m_monitorInfo.getMonitorId());
		PerfCheckInfo perfCheck = m_monitorInfo.getPerfCheckInfo();

		// 閾値監視情報を設定
		entity.setItemCode(perfCheck.getItemCode());
		entity.setDeviceDisplayName(perfCheck.getDeviceDisplayName());
		entity.setBreakdownFlg(perfCheck.getBreakdownFlg());
		monitorEntity.setMonitorPerfInfoEntity(entity);

		boolean result = true;

		// ポーラを停止
		ModifyPollingSchedule poller = new ModifyPollingSchedule();
		result = poller.deleteSchedule(
				m_monitor.getMonitorTypeId(),
				m_monitor.getMonitorId(),
				m_monitor.getFacilityId());

		// ポーラに登録
		result = result & poller.addSchedule(m_monitorInfo);

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
