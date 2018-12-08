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
import com.clustercontrol.logfile.bean.LogfileCheckInfo;
import com.clustercontrol.logfile.util.ControlLogfileInfo;
import com.clustercontrol.monitor.run.factory.SelectMonitor;

/**
 * ログファイル監視情報を検索するクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class SelectMonitorLogfile extends SelectMonitor{

	@Override
	protected LogfileCheckInfo getLogfileCheckInfo() throws MonitorNotFound {

		// ログファイル監視情報を取得
		ControlLogfileInfo logfile = new ControlLogfileInfo(m_monitorId, m_monitorTypeId);
		return logfile.get();
	}
}