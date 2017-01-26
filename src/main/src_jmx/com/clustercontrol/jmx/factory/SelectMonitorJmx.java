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

import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jmx.bean.JmxCheckInfo;
import com.clustercontrol.jmx.util.ControlJmxInfo;
import com.clustercontrol.monitor.run.factory.SelectMonitor;

/**
 * JMX 監視 数値監視情報を検索するクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class SelectMonitorJmx extends SelectMonitor{
	@Override
	protected JmxCheckInfo getJmxCheckInfo() throws MonitorNotFound {
		// JMX 監視情報を取得
		ControlJmxInfo jmx = new ControlJmxInfo(m_monitorId, m_monitorTypeId);
		return jmx.get();
	}
}
