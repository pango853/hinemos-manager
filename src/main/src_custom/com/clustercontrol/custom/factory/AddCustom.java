/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.custom.factory;

import com.clustercontrol.custom.bean.CustomCheckInfo;
import com.clustercontrol.custom.bean.CustomConstant;
import com.clustercontrol.custom.model.MonitorCustomInfoEntity;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.AddMonitorNumericValueType;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * コマンド監視の特有設定に対する登録処理実装クラス<br/>
 * @since 4.0
 */
public class AddCustom extends AddMonitorNumericValueType {

	/**
	 * コマンド監視特有の設定情報を登録する。<br/>
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {

		MonitorInfoEntity monitorEntity = QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		// Local Variables
		CustomCheckInfo checkInfo = null;

		// コマンド監視設定を登録する
		checkInfo = m_monitorInfo.getCustomCheckInfo();
		MonitorCustomInfoEntity entity = new MonitorCustomInfoEntity(monitorEntity);
		entity.setCommand(checkInfo.getCommand());
		entity.setSpecifyUser(checkInfo.getSpecifyUser());
		entity.setEffectiveUser(checkInfo.getEffectiveUser());
		entity.setExecuteType(
				checkInfo.getCommandExecType() == CustomConstant.CommandExecType.INDIVIDUAL ? CustomConstant._execIndividual : CustomConstant._execSelected);
		if (checkInfo.getCommandExecType() == CustomConstant.CommandExecType.SELECTED) {
			entity.setSelectedFacilityId(checkInfo.getSelectedFacilityId());
		}
		entity.setTimeout(checkInfo.getTimeout());

		return true;
	}

	/**
	 * スケジュール実行の遅延時間を返します。
	 */
	@Override
	protected int getDelayTime() {
		return 0;
	}

	/**
	 * スケジュール実行種別を返します。
	 */
	@Override
	protected TriggerType getTriggerType() {
		return TriggerType.NONE;
	}

}
