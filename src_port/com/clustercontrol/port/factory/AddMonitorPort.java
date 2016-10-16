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

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.AddMonitor;
import com.clustercontrol.monitor.run.factory.AddMonitorNumericValueType;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;
import com.clustercontrol.port.bean.PortCheckInfo;
import com.clustercontrol.port.model.MonitorPortInfoEntity;
import com.clustercontrol.port.model.MonitorProtocolMstEntity;
import com.clustercontrol.port.util.QueryUtil;

/**
 * port監視情報登録クラス
 *
 * @version 4.0.0
 * @since 2.4.0
 */
public class AddMonitorPort extends AddMonitorNumericValueType{

	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.AddMonitor#addCheckInfo()
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {

		MonitorInfoEntity monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		PortCheckInfo port = m_monitorInfo.getPortCheckInfo();
		MonitorProtocolMstEntity monitorProtocolMstEntity = null;
		try {
			monitorProtocolMstEntity = QueryUtil.getMonitorProtocolMstPK(port.getServiceId());
		} catch (MonitorNotFound e) {
		}

		// port監視情報を設定
		MonitorPortInfoEntity entity = new MonitorPortInfoEntity(monitorEntity, monitorProtocolMstEntity);
		entity.setPortNumber(port.getPortNo());
		entity.setRunCount(port.getRunCount());
		entity.setRunInterval(port.getRunInterval());
		entity.setTimeout(port.getTimeout());
		return true;
	}

	/**
	 * スケジュール実行の遅延時間を返します。
	 */
	@Override
	protected int getDelayTime() {
		return AddMonitor.getDelayTimeBasic(m_monitorInfo);
	}

	/**
	 * スケジュール実行種別を返します。
	 */
	@Override
	protected TriggerType getTriggerType() {
		return TriggerType.SIMPLE;
	}
}
