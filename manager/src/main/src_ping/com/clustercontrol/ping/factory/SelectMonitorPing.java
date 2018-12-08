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

import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.ping.bean.PingCheckInfo;
import com.clustercontrol.ping.model.MonitorPingInfoEntity;
import com.clustercontrol.ping.util.QueryUtil;

/**
 * ping監視判定情報を検索するクラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class SelectMonitorPing extends SelectMonitor{

	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.SelectMonitor#getCheckInfo()
	 */
	@Override
	protected PingCheckInfo getPingCheckInfo() throws MonitorNotFound {

		// ping監視情報を取得
		MonitorPingInfoEntity entity = QueryUtil.getMonitorPingInfoPK(m_monitorId);

		PingCheckInfo ping = new PingCheckInfo();
		ping.setMonitorTypeId(m_monitorTypeId);
		ping.setMonitorId(m_monitorId);
		ping.setRunCount(entity.getRunCount());
		ping.setRunInterval(entity.getRunInterval());
		ping.setTimeout(entity.getTimeout());

		return ping;
	}
}
