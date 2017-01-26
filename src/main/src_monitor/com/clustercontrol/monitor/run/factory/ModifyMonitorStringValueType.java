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

import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.bean.MonitorStringValueInfo;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfoEntity;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfoEntityPK;
import com.clustercontrol.monitor.run.util.QueryUtil;

/**
 * 文字列監視の判定情報を変更する抽象クラス<BR>
 * <p>
 * 文字列監視を行う各監視管理クラスで継承してください。
 *
 * @version 4.0.0
 * @since 2.1.0
 */
abstract public class ModifyMonitorStringValueType extends ModifyMonitor{

	/**
	 * 監視情報より文字列監視の判定情報を取得し、変更します。
	 * <p>
	 * <ol>
	 * <li>監視情報より判定情報を削除します。</li>
	 * <li>判定情報を作成し、監視情報に設定します。</li>
	 * </ol>
	 * @throws MonitorNotFound
	 * @throws EntityExistsException
	 * @throws InvalidRole
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorInfoBean
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorStringValueInfoBean
	 */
	@Override
	protected boolean modifyJudgementInfo() throws MonitorNotFound, EntityExistsException, InvalidRole {

		MonitorInfoEntity monitorEntity = QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		// 文字列監視判定情報を設定
		ArrayList<MonitorStringValueInfo> valueList = m_monitorInfo.getStringValueInfo();
		if(valueList == null){
			return true;
		}

		List<MonitorStringValueInfoEntityPK> monitorStringValueInfoEntityPkList = new ArrayList<MonitorStringValueInfoEntityPK>();

		int orderNo = 0;
		for(MonitorStringValueInfo value : valueList){
			if(value != null){
				MonitorStringValueInfoEntity entity = null;
				MonitorStringValueInfoEntityPK entityPk = new MonitorStringValueInfoEntityPK(
						m_monitorInfo.getMonitorId(),
						Integer.valueOf(++orderNo));
				try {
					entity = QueryUtil.getMonitorStringValueInfoPK(entityPk);
				} catch (MonitorNotFound e) {
					entity = new MonitorStringValueInfoEntity(entityPk, monitorEntity);
				}
				entity.setCaseSensitivityFlg(ValidConstant.booleanToType(value.getCaseSensitivityFlg()));
				entity.setDescription(value.getDescription());
				entity.setMessage(value.getMessage());
				entity.setPattern(value.getPattern());
				entity.setPriority(Integer.valueOf(value.getPriority()));
				entity.setProcessType(Integer.valueOf(value.getProcessType()));
				entity.setValidFlg(ValidConstant.booleanToType(value.isValidFlg()));
				monitorStringValueInfoEntityPkList.add(entityPk);
			}
		}
		// 不要なMonitorStringValueInfoEntityを削除
		monitorEntity.deleteMonitorStringValueInfoEntities(monitorStringValueInfoEntityPkList);

		return true;
	}

}
