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

import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.bean.MonitorStringValueInfo;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfoEntity;
import com.clustercontrol.monitor.run.util.QueryUtil;

/**
 * 文字列監視の判定情報を作成する抽象クラス<BR>
 * <p>
 * 文字列監視を行う各監視管理クラスで継承してください。
 *
 * @version 4.0.0
 * @since 2.1.0
 */
abstract public class AddMonitorStringValueType extends AddMonitor{

	/**
	 * 文字列監視の判定情報を作成し、監視情報に設定します。
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorInfoBean
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorStringValueInfoBean
	 */
	@Override
	protected boolean addJudgementInfo() throws MonitorNotFound, InvalidRole {

		MonitorInfoEntity monitorEntity = QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		ArrayList<MonitorStringValueInfo> valueList = m_monitorInfo.getStringValueInfo();

		// 文字列監視判定情報を設定
		MonitorStringValueInfo value = null;
		for(int index=0; index<valueList.size(); index++){
			value = valueList.get(index);

			if(value != null){
				MonitorStringValueInfoEntity entity = new MonitorStringValueInfoEntity(
						monitorEntity,
						Integer.valueOf(index+1));
				entity.setCaseSensitivityFlg(ValidConstant.booleanToType(value.getCaseSensitivityFlg()));
				entity.setDescription(value.getDescription());
				entity.setMessage(value.getMessage());
				entity.setPattern(value.getPattern());
				entity.setPriority(Integer.valueOf(value.getPriority()));
				entity.setProcessType(Integer.valueOf(value.getProcessType()));
				entity.setValidFlg(ValidConstant.booleanToType(value.isValidFlg()));
			}
		}
		return true;
	}
}
