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

package com.clustercontrol.snmp.factory;

import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.snmp.bean.SnmpCheckInfo;
import com.clustercontrol.snmp.util.ControlSnmpInfo;

/**
 * SNMP監視 数値監視設定を検索するファクトリークラス<BR>
 *
 * @version 2.1.0
 * @since 2.0.0
 */
public class SelectMonitorSnmp extends SelectMonitor{

	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.SelectMonitor#getCheckInfo()
	 */
	@Override
	protected SnmpCheckInfo getSnmpCheckInfo() throws MonitorNotFound {

		// SNMP監視情報を取得
		ControlSnmpInfo snmp = new ControlSnmpInfo(m_monitorId, m_monitorTypeId);
		return snmp.get();
	}
}
