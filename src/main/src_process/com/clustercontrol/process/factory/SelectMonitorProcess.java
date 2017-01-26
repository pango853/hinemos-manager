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

package com.clustercontrol.process.factory;

import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.process.bean.ProcessCheckInfo;
import com.clustercontrol.process.model.MonitorProcessInfoEntity;
import com.clustercontrol.process.util.QueryUtil;

/**
 * プロセス監視判定情報を検索するクラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class SelectMonitorProcess extends SelectMonitor{

	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.SelectMonitor#getCheckInfo()
	 */
	@Override
	protected ProcessCheckInfo getProcessCheckInfo() throws MonitorNotFound {

		// プロセス監視情報を取得
		MonitorProcessInfoEntity entity = QueryUtil.getMonitorProcessInfoPK(m_monitorId);

		ProcessCheckInfo process = new ProcessCheckInfo();
		process.setMonitorTypeId(m_monitorTypeId);
		process.setMonitorId(m_monitorId);
		process.setCommand(entity.getCommand());
		process.setParam(entity.getParam());
		process.setCaseSensitivityFlg(ValidConstant.typeToBoolean(entity.getCaseSensitivityFlg()));

		return process;
	}
}
