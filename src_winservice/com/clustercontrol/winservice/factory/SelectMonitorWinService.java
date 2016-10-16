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

package com.clustercontrol.winservice.factory;

import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.winservice.bean.WinServiceCheckInfo;
import com.clustercontrol.winservice.model.MonitorWinserviceInfoEntity;
import com.clustercontrol.winservice.util.QueryUtil;

/**
 * Windowsサービス監視判定情報を検索するクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class SelectMonitorWinService extends SelectMonitor{

	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.SelectMonitor#getCheckInfo()
	 */
	@Override
	protected WinServiceCheckInfo getWinServiceCheckInfo() throws MonitorNotFound {

		// Windowsサービス監視情報を取得
		MonitorWinserviceInfoEntity entity = QueryUtil.getMonitorWinserviceInfoPK(m_monitorId);

		WinServiceCheckInfo winService = new WinServiceCheckInfo();
		winService.setMonitorTypeId(m_monitorTypeId);
		winService.setMonitorId(m_monitorId);
		winService.setServiceName(entity.getServiceName());

		return winService;
	}
}
