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
import java.util.List;

import javax.persistence.EntityExistsException;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.bean.MonitorNumericValueInfo;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfoEntity;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfoEntityPK;
import com.clustercontrol.monitor.run.util.QueryUtil;

/**
 * 数値監視の判定情報を変更する抽象クラス<BR>
 * <p>
 * 数値監視を行う各監視管理クラスで継承してください。
 *
 * @version 4.0.0
 * @since 2.0.0
 */
abstract public class ModifyMonitorNumericValueType extends ModifyMonitor{

	/**
	 * 監視情報より数値監視の判定情報を取得し、変更します。
	 * <p>
	 * <ol>
	 * <li>監視情報より判定情報を削除します。</li>
	 * <li>判定情報を作成し、監視情報に設定します。</li>
	 * </ol>
	 * 
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorInfoBean
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorNumericValueInfoBean
	 */
	@Override
	protected boolean modifyJudgementInfo() throws MonitorNotFound, EntityExistsException, InvalidRole {

		MonitorInfoEntity monitorEntity = QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		// 数値監視判定情報を設定
		ArrayList<MonitorNumericValueInfo> valueList = m_monitorInfo.getNumericValueInfo();

		List<MonitorNumericValueInfoEntityPK> monitorNumericValueInfoEntityPkList = new ArrayList<MonitorNumericValueInfoEntityPK>();

		for(MonitorNumericValueInfo value : valueList){
			if(value != null){
				MonitorNumericValueInfoEntityPK entityPk = new MonitorNumericValueInfoEntityPK(
						m_monitorInfo.getMonitorId(),
						value.getPriority());
				MonitorNumericValueInfoEntity entity = null;
				try {
					entity = QueryUtil.getMonitorNumericValueInfoPK(entityPk);
				} catch (MonitorNotFound e) {
					// 新規登録
					entity = new MonitorNumericValueInfoEntity(entityPk, monitorEntity);
				}
				entity.setMessage(value.getMessage());
				entity.setMessageId(value.getMessageId());
				entity.setThresholdLowerLimit(Double.valueOf(value.getThresholdLowerLimit()));
				entity.setThresholdUpperLimit(Double.valueOf(value.getThresholdUpperLimit()));
				monitorNumericValueInfoEntityPkList.add(entityPk);
			}
		}
		// 不要なMonitorNumericValueInfoEntityを削除
		monitorEntity.deleteMonitorNumericValueInfoEntities(monitorNumericValueInfoEntityPkList);

		return true;
	}

}
