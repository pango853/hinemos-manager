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

package com.clustercontrol.port.factory;

import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.port.bean.PortCheckInfo;
import com.clustercontrol.port.model.MonitorPortInfoEntity;
import com.clustercontrol.port.util.QueryUtil;

/**
 * port監視判定情報検索クラス
 *
 * @version 4.0.0
 * @since 2.4.0
 */
public class SelectMonitorPort extends SelectMonitor{

	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.SelectMonitor#getCheckInfo()
	 */
	@Override
	protected PortCheckInfo getPortCheckInfo() throws MonitorNotFound {

		// port監視情報を取得
		MonitorPortInfoEntity entity = QueryUtil.getMonitorPortInfoPK(m_monitorId);

		PortCheckInfo port = new PortCheckInfo();
		port.setMonitorTypeId(m_monitorTypeId);
		port.setMonitorId(m_monitorId);
		port.setPortNo(entity.getPortNumber());  //ポート番号
		port.setRunCount(entity.getRunCount());
		port.setRunInterval(entity.getRunInterval());
		port.setTimeout(entity.getTimeout());
		port.setServiceId(entity.getMonitorProtocolMstEntity().getServiceId());

		return port;
	}
}
