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
import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.scheduler.TriggerSchedulerException;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitor;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;
import com.clustercontrol.snmptrap.bean.TrapCheckInfo;
import com.clustercontrol.snmptrap.bean.TrapValueInfo;
import com.clustercontrol.snmptrap.bean.VarBindPattern;
import com.clustercontrol.snmptrap.model.MonitorTrapInfoEntity;
import com.clustercontrol.snmptrap.model.MonitorTrapValueInfoEntity;
import com.clustercontrol.snmptrap.model.MonitorTrapValueInfoEntityPK;
import com.clustercontrol.snmptrap.model.MonitorTrapVarbindPatternInfoEntity;
import com.clustercontrol.snmptrap.model.MonitorTrapVarbindPatternInfoEntityPK;
import com.clustercontrol.snmptrap.util.CharsetUtil;
import com.clustercontrol.snmptrap.util.QueryUtil;

/**
 * SNMPTRAP監視情報を変更するクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class ModifyMonitorTrap extends ModifyMonitor {

	private static Log m_log = LogFactory.getLog( ModifyMonitorTrap.class );

	/**
	 * SNMPトラップ監視設定の変更(変更時に文字コードをチェック)
	 */
	@Override
	protected boolean modifyMonitorInfo(String user) throws MonitorNotFound, TriggerSchedulerException, HinemosUnknown, InvalidRole {
		CharsetUtil.checkCharset(m_monitorInfo);

		return super.modifyMonitorInfo(user);
	}

	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole {
		m_log.debug("modifyCheckInfo() : start");

		// SNMPTRAP監視情報を取得
		TrapCheckInfo checkInfo = m_monitorInfo.getTrapCheckInfo();
		if(m_log.isDebugEnabled()){
			m_log.debug("modifyCheckInfo() : " +
					" MonitorId = " + checkInfo.getMonitorId() +
					",CommunityName = " + checkInfo.getCommunityName() +
					",CommunityCheck = " + checkInfo.getCommunityCheck() +
					",CharsetConvert = " + checkInfo.getCharsetConvert() +
					",CharsetName = " + checkInfo.getCharsetName() +
					",NotifyofReceivingUnspecifiedFlg = " + checkInfo.getNotifyofReceivingUnspecifiedFlg() +
					",PriorityUnspecified = " + checkInfo.getPriorityUnspecified() +
					",TrapValueInfos = " + checkInfo.getTrapValueInfos()
					);
		}

		MonitorTrapInfoEntity trapInfoEntity = QueryUtil.getMonitorTrapInfoPK(m_monitorInfo.getMonitorId());

		trapInfoEntity.setMonitorId(checkInfo.getMonitorId());
		trapInfoEntity.setCharsetConvert(checkInfo.getCharsetConvert());
		trapInfoEntity.setCharsetName(checkInfo.getCharsetName());
		trapInfoEntity.setCommunityCheck(checkInfo.getCommunityCheck());
		trapInfoEntity.setCommunityName(checkInfo.getCommunityName());
		trapInfoEntity.setNotifyofReceivingUnspecifiedFlg(ValidConstant.booleanToType(checkInfo.getNotifyofReceivingUnspecifiedFlg()));
		trapInfoEntity.setPriorityUnspecified(checkInfo.getPriorityUnspecified());

		List<TrapValueInfo> trapValueList = new ArrayList<TrapValueInfo>(checkInfo.getTrapValueInfos());
		
		List<MonitorTrapValueInfoEntityPK> monitorTrapValueInfoEntityPkList = new ArrayList<MonitorTrapValueInfoEntityPK>();
		for (TrapValueInfo trapValue : trapValueList) {
			if (trapValue != null) {
				if (!trapValue.getTrapOid().startsWith(".")) {
					trapValue.setTrapOid("." + trapValue.getTrapOid());
				}
				MonitorTrapValueInfoEntity entity = null;
				MonitorTrapValueInfoEntityPK entityPk = new MonitorTrapValueInfoEntityPK(
						checkInfo.getMonitorId(),
						trapValue.getMib(),
						trapValue.getTrapOid(), 
						trapValue.getGenericId(),
						trapValue.getSpecificId());
				try {
					entity = QueryUtil.getMonitorTrapValueInfoPK(entityPk);
				} catch (MonitorNotFound e) {
					entity = new MonitorTrapValueInfoEntity(trapInfoEntity, trapValue.getMib(), trapValue.getTrapOid(), trapValue.getGenericId(), trapValue.getSpecificId());
				}
				
				entity.setDescription(trapValue.getDescription());
				entity.setFormatVarBinds(trapValue.getFormatVarBinds());
				entity.setLogmsg(trapValue.getLogmsg());
				entity.setPriorityAnyVarbind(trapValue.getPriorityAnyVarbind());
				entity.setProcessingVarbindType(trapValue.getProcessingVarbindType());
				entity.setUei(trapValue.getUei());
				entity.setValidFlg(ValidConstant.booleanToType(trapValue.getValidFlg()));
				entity.setVersion(trapValue.getVersion());
				
				List<VarBindPattern> varbindList = new ArrayList<VarBindPattern>(trapValue.getVarBindPatterns());
				List<MonitorTrapVarbindPatternInfoEntityPK> monitorTrapVarbindPatternInfoEntityPkList = new ArrayList<MonitorTrapVarbindPatternInfoEntityPK>();
				int orderNo = 0;
				for (VarBindPattern varbind : varbindList) {
					if (varbind != null) {
						MonitorTrapVarbindPatternInfoEntity varbindEntity = null;
						MonitorTrapVarbindPatternInfoEntityPK varbindEntityPk = new MonitorTrapVarbindPatternInfoEntityPK(
								checkInfo.getMonitorId(),
								trapValue.getMib(),
								trapValue.getTrapOid(), 
								trapValue.getGenericId(),
								trapValue.getSpecificId(),
								Integer.valueOf(orderNo));
						try {
							varbindEntity = QueryUtil.getMonitorTrapVarbindPatternInfoPK(varbindEntityPk);
						} catch (MonitorNotFound e) {
							varbindEntity = new MonitorTrapVarbindPatternInfoEntity(entity, orderNo);
						}
						orderNo++;
						
						varbindEntity.setCaseSensitivityFlg(ValidConstant.booleanToType(varbind.getCaseSensitivityFlg()));
						varbindEntity.setDescription(varbind.getDescription());
						varbindEntity.setPattern(varbind.getPattern());
						varbindEntity.setPriority(varbind.getPriority());
						varbindEntity.setProcessType(varbind.getProcessType());
						varbindEntity.setValidFlg(ValidConstant.booleanToType(varbind.getValidFlg()));
						monitorTrapVarbindPatternInfoEntityPkList.add(varbindEntityPk);
					}
				}
				entity.deleteMonitorTrapVarbindPatternInfoEntities(monitorTrapVarbindPatternInfoEntityPkList);
				
				monitorTrapValueInfoEntityPkList.add(entityPk);
			}
		}
		trapInfoEntity.deleteMonitorTrapValueInfoEntities(monitorTrapValueInfoEntityPkList);

		m_log.debug("modifyCheckInfo() : end");
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

	@Override
	protected boolean modifyJudgementInfo() throws MonitorNotFound, EntityExistsException, InvalidRole {
		return true;
	}
}
