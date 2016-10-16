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

package com.clustercontrol.monitor.run.factory;

import java.util.ArrayList;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.bean.MonitorNumericValueInfo;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfoEntity;
import com.clustercontrol.monitor.run.util.QueryUtil;

/**
 * 数値監視の判定情報を作成する抽象クラス<BR>
 * <p>
 * 数値監視を行う各監視管理クラスで継承してください。
 *
 * @version 4.0.0
 * @since 2.0.0
 */
abstract public class AddMonitorNumericValueType extends AddMonitor{

	/**
	 * 数値監視の判定情報を作成し、監視情報に設定します。
	 * 
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorInfoBean
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorNumericValueInfoBean
	 */
	@Override
	protected boolean addJudgementInfo() throws MonitorNotFound, InvalidRole {

		MonitorInfoEntity monitorEntity = QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		ArrayList<MonitorNumericValueInfo> valueList = m_monitorInfo.getNumericValueInfo();
		// 数値監視判定情報を設定
		MonitorNumericValueInfo value = null;
		for(int index=0; index<valueList.size(); index++){
			value = valueList.get(index);
			if(value != null){
				MonitorNumericValueInfoEntity entity = new MonitorNumericValueInfoEntity(
						monitorEntity,
						Integer.valueOf(value.getPriority()));
				entity.setMessage(value.getMessage());
				entity.setMessageId(value.getMessageId());
				entity.setThresholdLowerLimit(Double.valueOf(value.getThresholdLowerLimit()));
				entity.setThresholdUpperLimit(Double.valueOf(value.getThresholdUpperLimit()));
			}
		}

		return true;
	}

}
