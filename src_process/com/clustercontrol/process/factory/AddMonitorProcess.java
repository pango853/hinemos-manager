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
import com.clustercontrol.bean.YesNoConstant;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.AddMonitorNumericValueType;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;
import com.clustercontrol.process.bean.ProcessCheckInfo;
import com.clustercontrol.process.model.MonitorProcessInfoEntity;
import com.clustercontrol.process.util.ProcessProperties;

/**
 * プロセス監視情報を登録するクラス<BR>
 *
 * @version 4.1.0
 * @since 2.0.0
 */
public class AddMonitorProcess extends AddMonitorNumericValueType{

	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.AddMonitor#addCheckInfo()
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {

		MonitorInfoEntity monitorEntity = QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		// プロセス監視情報を設定
		ProcessCheckInfo process = m_monitorInfo.getProcessCheckInfo();

		MonitorProcessInfoEntity entity = new MonitorProcessInfoEntity(monitorEntity);
		entity.setCommand(process.getCommand());
		entity.setParam(process.getParam());
		entity.setCaseSensitivityFlg(ValidConstant.booleanToType(process.getCaseSensitivityFlg()));

		// 監視または収集が有効の場合にポーラに登録
		boolean result = true;
		if(m_monitorInfo.getMonitorFlg() == YesNoConstant.TYPE_YES
				|| m_monitorInfo.getCollectorFlg() == YesNoConstant.TYPE_YES){
			ModifyPollingSchedule poller = new ModifyPollingSchedule();
			result = poller.addSchedule(
					m_monitorInfo.getMonitorTypeId(),
					m_monitorInfo.getMonitorId(),
					m_monitorInfo.getFacilityId(),
					m_monitorInfo.getRunInterval());
		}

		return result;
	}

	/**
	 * スケジュール実行の遅延時間を返します。
	 */
	@Override
	protected int getDelayTime() {
		// 値取得開始時間（秒）設定が60以上のものは59秒に設定する
		int startSecond = ProcessProperties.getProperties().getStartSecond();
		if(startSecond >= 60){
			startSecond = 59;
		}

		return startSecond;
	}

	/**
	 * スケジュール実行種別を返します。
	 */
	@Override
	protected TriggerType getTriggerType() {
		return TriggerType.CRON;
	}
}
