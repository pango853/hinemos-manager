/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.snmptrap.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.snmptrap.bean.TrapValueInfo;
import com.clustercontrol.snmptrap.bean.VarBindPattern;
import com.clustercontrol.snmptrap.model.MonitorTrapValueInfoEntity;
import com.clustercontrol.snmptrap.model.MonitorTrapVarbindPatternInfoEntity;
import com.clustercontrol.snmptrap.bean.TrapCheckInfo;
import com.clustercontrol.snmptrap.model.MonitorTrapInfoEntity;
import com.clustercontrol.snmptrap.util.QueryUtil;

/**
 * SNMPTRAP監視情報を検索するクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class SelectMonitorTrap extends SelectMonitor {

	private static Log m_log = LogFactory.getLog( SelectMonitorTrap.class );

	@Override
	protected TrapCheckInfo getTrapCheckInfo() throws MonitorNotFound {
		m_log.debug("getTrapCheckInfo() : start");

		MonitorTrapInfoEntity trapInfoEntity = QueryUtil.getMonitorTrapInfoPK(m_monitorId);

		if(m_log.isDebugEnabled()){
			m_log.debug("getTrapCheckInfo() : " +
					" MonitorId = " + trapInfoEntity.getMonitorId() +
					",CommunityName = " + trapInfoEntity.getCommunityName() +
					",CommunityCheck = " + trapInfoEntity.getCommunityCheck() +
					",CharsetConvert = " + trapInfoEntity.getCharsetConvert() +
					",CharsetName = " + trapInfoEntity.getCharsetName() +
					",NotifyofReceivingUnspecifiedFlg = " + trapInfoEntity.getNotifyofReceivingUnspecifiedFlg() +
					",PriorityUnspecified = " + trapInfoEntity.getPriorityUnspecified() +
					",TrapValueInfos = " + trapInfoEntity.getMonitorTrapValueInfoEntities());
		}

		TrapCheckInfo checkInfo = new TrapCheckInfo();
		checkInfo.setMonitorId(m_monitorId);
		checkInfo.setMonitorTypeId(m_monitorTypeId);
		checkInfo.setCommunityName(trapInfoEntity.getCommunityName());
		checkInfo.setCommunityCheck(trapInfoEntity.getCommunityCheck());
		checkInfo.setCharsetConvert(trapInfoEntity.getCharsetConvert());
		checkInfo.setCharsetName(trapInfoEntity.getCharsetName());
		checkInfo.setNotifyofReceivingUnspecifiedFlg(ValidConstant.typeToBoolean(trapInfoEntity.getNotifyofReceivingUnspecifiedFlg()));
		checkInfo.setPriorityUnspecified(trapInfoEntity.getPriorityUnspecified());

		List<TrapValueInfo> valueInfoList = new ArrayList<>();
		for (MonitorTrapValueInfoEntity valueInfoEntity: trapInfoEntity.getMonitorTrapValueInfoEntities()) {
			TrapValueInfo valueInfo = new TrapValueInfo();

			if (valueInfoEntity.getDescription() != null) {
				valueInfo.setDescription(valueInfoEntity.getDescription());
			}
			valueInfo.setGenericId(valueInfoEntity.getId().getGenericId());
			valueInfo.setLogmsg(valueInfoEntity.getLogmsg());
			valueInfo.setMib(valueInfoEntity.getId().getMib());
			valueInfo.setSpecificId(valueInfoEntity.getId().getSpecificId());
			valueInfo.setTrapOid(valueInfoEntity.getId().getTrapOid());
			valueInfo.setUei(valueInfoEntity.getUei());
			valueInfo.setProcessingVarbindType(valueInfoEntity.getProcessingVarbindType());
			valueInfo.setVersion(valueInfoEntity.getVersion());
			valueInfo.setPriorityAnyVarbind(valueInfoEntity.getPriorityAnyVarbind());
			valueInfo.setFormatVarBinds(valueInfoEntity.getFormatVarBinds());
			valueInfo.setValidFlg(ValidConstant.typeToBoolean(valueInfoEntity.getValidFlg()));

			List<MonitorTrapVarbindPatternInfoEntity> varbindPatternEntityList = new ArrayList<>(valueInfoEntity.getMonitorTrapVarbindPatternInfoEntities());
			Collections.sort(varbindPatternEntityList, new Comparator<MonitorTrapVarbindPatternInfoEntity>() {
				@Override
				public int compare(MonitorTrapVarbindPatternInfoEntity o1, MonitorTrapVarbindPatternInfoEntity o2) {
					return o1.getId().getOrderNo().compareTo(o2.getId().getOrderNo());
				}
			});

			List<VarBindPattern> vps = new ArrayList<>();
			for (MonitorTrapVarbindPatternInfoEntity vpe: varbindPatternEntityList) {
				VarBindPattern vp = new VarBindPattern();
				vp.setPattern(vpe.getPattern());
				vp.setDescription(vpe.getDescription());
				vp.setCaseSensitivityFlg(ValidConstant.typeToBoolean(vpe.getCaseSensitivityFlg()));
				vp.setProcessType(vpe.getProcessType());
				vp.setValidFlg(ValidConstant.typeToBoolean(vpe.getValidFlg()));
				vp.setPriority(vpe.getPriority());
				vps.add(vp);
			}
			valueInfo.setVarBindPatterns(vps);

			valueInfoList.add(valueInfo);
		}
		checkInfo.setTrapValueInfos(valueInfoList);

		m_log.debug("getTrapCheckInfo() : end");
		return checkInfo;
	}
}
