/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jmx.factory;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jmx.util.ControlJmxInfo;
import com.clustercontrol.monitor.run.factory.AddMonitor;
import com.clustercontrol.monitor.run.factory.AddMonitorNumericValueType;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * JMX 監視 数値監視用をマネージャに登録するクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class AddMonitorJmx extends AddMonitorNumericValueType{

	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {

		// JMX 監視情報を追加
		ControlJmxInfo jmx = new ControlJmxInfo(m_monitorInfo.getMonitorId(), m_monitorInfo.getMonitorTypeId());
		return jmx.add(m_monitorInfo.getJmxCheckInfo());
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
