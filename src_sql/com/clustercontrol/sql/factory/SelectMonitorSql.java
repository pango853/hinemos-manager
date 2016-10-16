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

package com.clustercontrol.sql.factory;

import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.sql.bean.SqlCheckInfo;
import com.clustercontrol.sql.util.ControlSqlInfo;

/**
 * SQL監視 数値監視を検索するファクトリークラス<BR>
 *
 * @version 2.1.0
 * @since 2.0.0
 */
public class SelectMonitorSql extends SelectMonitor{

	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.SelectMonitor#getCheckInfo()
	 */
	@Override
	protected SqlCheckInfo getSqlCheckInfo() throws MonitorNotFound {

		// SQL監視情報を取得
		ControlSqlInfo sql = new ControlSqlInfo(m_monitorId, m_monitorTypeId);
		return sql.get();
	}
}
