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

package com.clustercontrol.ping.factory;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.AddMonitor;
import com.clustercontrol.monitor.run.factory.AddMonitorNumericValueType;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.ping.bean.PingCheckInfo;
import com.clustercontrol.ping.model.MonitorPingInfoEntity;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * ping監視情報を登録するクラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class AddMonitorPing extends AddMonitorNumericValueType{

	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.AddMonitor#addCheckInfo()
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {

		MonitorInfoEntity monitorEntity = QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		// ping監視情報を設定
		PingCheckInfo ping = m_monitorInfo.getPingCheckInfo();

		MonitorPingInfoEntity entity = new MonitorPingInfoEntity(monitorEntity);
		entity.setRunCount(ping.getRunCount());
		entity.setRunInterval(ping.getRunInterval());
		entity.setTimeout(ping.getTimeout());

		return true;
	}

	/**
	 * スケジュール実行の遅延時間を返します。
	 */
	@Override
	protected int getDelayTime() {
		return AddMonitor.getDelayTimeBasic(m_monitorInfo);
	}

	/**
	 * スケジュール実行種別を返します。
	 */
	@Override
	protected TriggerType getTriggerType() {
		return TriggerType.SIMPLE;
	}
}
