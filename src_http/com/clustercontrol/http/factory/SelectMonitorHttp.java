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
import com.clustercontrol.http.bean.HttpCheckInfo;
import com.clustercontrol.http.util.ControlHttpInfo;
import com.clustercontrol.monitor.run.factory.SelectMonitor;

/**
 * HTTP監視 数値監視情報を検索するクラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class SelectMonitorHttp extends SelectMonitor{

	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.SelectMonitor#getCheckInfo()
	 */
	@Override
	protected HttpCheckInfo getHttpCheckInfo() throws MonitorNotFound {

		// HTTP監視情報を取得
		ControlHttpInfo http = new ControlHttpInfo(m_monitorId, m_monitorTypeId);
		return http.get();
	}
}
