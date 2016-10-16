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

package com.clustercontrol.logfile.factory;

import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.logfile.util.ControlLogfileInfo;
import com.clustercontrol.monitor.run.factory.ModifyMonitorStringValueType;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * ログファイル監視情報をマネージャで変更するクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 * 
 */
public class ModifyMonitorLogfileString extends ModifyMonitorStringValueType{

	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.ModifyMonitor#modifyCheckInfo()
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound {

		// ログファイル監視情報を変更
		ControlLogfileInfo logfile = new ControlLogfileInfo(m_monitorInfo.getMonitorId(), m_monitorInfo.getMonitorTypeId());

		return logfile.modify(m_monitorInfo.getLogfileCheckInfo());
	}

	/**
	 * スケジュール実行の遅延時間を返します。
	 */
	@Override
	protected int getDelayTime() {
		return 0;
	}

	/**
	 * スケジュール実行種別を返します。
	 */
	@Override
	protected TriggerType getTriggerType() {
		return TriggerType.NONE;
	}
}
