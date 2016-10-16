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
import com.clustercontrol.custom.util.QueryUtil;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitorNumericValueType;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * コマンド監視の特有設定に対する変更処理実装クラス<br/>
 * @since 4.0
 */
public class ModifyCustom extends ModifyMonitorNumericValueType {

	/**
	 * コマンド監視特有の設定情報を更新する。<br/>
	 * また、コマンド監視設定が変更されたことを影響するエージェントに通知する。<br/>
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole {

		MonitorInfoEntity monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());
		// Local Variables
		CustomCheckInfo checkInfo = null;

		checkInfo = m_monitorInfo.getCustomCheckInfo();

		// 変更前の実行対象の一覧を取得する
		MonitorCustomInfoEntity entity = QueryUtil.getMonitorCustomInfoPK(m_monitorInfo.getMonitorId());

		// コマンド監視設定を更新する
		entity.setExecuteType(
				checkInfo.getCommandExecType() == CustomConstant.CommandExecType.INDIVIDUAL ? CustomConstant._execIndividual : CustomConstant._execSelected);
		entity.setSelectedFacilityId(checkInfo.getSelectedFacilityId());
		entity.setSpecifyUser(checkInfo.getSpecifyUser());
		entity.setEffectiveUser(checkInfo.getEffectiveUser());
		entity.setCommand(checkInfo.getCommand());
		entity.setTimeout(checkInfo.getTimeout());
		monitorEntity.setMonitorCustomInfoEntity(entity);
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
