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

package com.clustercontrol.http.factory;

import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.http.bean.HttpScenarioCheckInfo;
import com.clustercontrol.http.util.ControlHttpScenarioInfo;
import com.clustercontrol.monitor.run.factory.SelectMonitor;

/**
 * HTTP監視 数値監視情報を検索するクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class SelectMonitorHttpScenario extends SelectMonitor{
	@Override
	protected HttpScenarioCheckInfo getHttpScenarioCheckInfo() throws MonitorNotFound {
		// HTTP監視情報を取得
		ControlHttpScenarioInfo http = new ControlHttpScenarioInfo(m_monitorId, m_monitorTypeId);
		return http.get();
	}
}
