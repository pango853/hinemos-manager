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
import com.clustercontrol.monitor.run.factory.AddMonitor;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;
import com.clustercontrol.snmptrap.bean.TrapCheckInfo;
import com.clustercontrol.snmptrap.bean.TrapValueInfo;
import com.clustercontrol.snmptrap.bean.VarBindPattern;
import com.clustercontrol.snmptrap.model.MonitorTrapInfoEntity;
import com.clustercontrol.snmptrap.model.MonitorTrapValueInfoEntity;
import com.clustercontrol.snmptrap.model.MonitorTrapVarbindPatternInfoEntity;
import com.clustercontrol.snmptrap.util.CharsetUtil;

/**
 * SNMPTRAP監視情報を登録するクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class AddMonitorTrap extends AddMonitor {

	private static Log m_log = LogFactory.getLog( AddMonitorTrap.class );

	/**
	 * SNMPトラップ監視設定の追加(追加時に文字コードをチェック)
	 */
	@Override
	protected boolean addMonitorInfo(String user) throws MonitorNotFound, TriggerSchedulerException, EntityExistsException, HinemosUnknown, InvalidRole {
		CharsetUtil.checkCharset(m_monitorInfo);

		return super.addMonitorInfo(user);
	}

	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {

		MonitorInfoEntity monitorEntity = QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		m_log.debug("addCheckInfo() : start");

		// SNMPTRAP監視情報を登録
		TrapCheckInfo trap = m_monitorInfo.getTrapCheckInfo();
		if(m_log.isDebugEnabled()){
			m_log.debug("addCheckInfo() : " +
					" MonitorId = " + trap.getMonitorId() +
					",CommunityName = " + trap.getCommunityName() +
					",CommunityCheck = " + trap.getCommunityCheck() +
					",CharsetConvert = " + trap.getCharsetConvert() +
					",CharsetName = " + trap.getCharsetName() +
					",NotifyofReceivingUnspecifiedFlg = " + trap.getNotifyofReceivingUnspecifiedFlg() +
					",PriorityUnspecified = " + trap.getPriorityUnspecified() +
					",TrapValueInfos = " + trap.getTrapValueInfos()
					);
		}

		MonitorTrapInfoEntity trapEntity = new MonitorTrapInfoEntity(monitorEntity);
		trapEntity.setMonitorId(trap.getMonitorId());
		trapEntity.setCharsetConvert(trap.getCharsetConvert());
		trapEntity.setCharsetName(trap.getCharsetName());
		trapEntity.setCommunityCheck(trap.getCommunityCheck());
		trapEntity.setCommunityName(trap.getCommunityName());
		trapEntity.setNotifyofReceivingUnspecifiedFlg(ValidConstant.booleanToType(trap.getNotifyofReceivingUnspecifiedFlg()));
		trapEntity.setPriorityUnspecified(trap.getPriorityUnspecified());

		List<MonitorTrapValueInfoEntity> valueEntities = new ArrayList<MonitorTrapValueInfoEntity>();
		for (TrapValueInfo valueInfo: trap.getTrapValueInfos()) {
			String trapOid = valueInfo.getTrapOid();
			if (!trapOid.startsWith("."))
				trapOid = "." + trapOid;
			MonitorTrapValueInfoEntity valueEntity = new MonitorTrapValueInfoEntity(trapEntity, valueInfo.getMib(), trapOid, valueInfo.getGenericId(), valueInfo.getSpecificId());
			valueEntity.setDescription(valueInfo.getDescription());
			valueEntity.setFormatVarBinds(valueInfo.getFormatVarBinds());
			valueEntity.setLogmsg(valueInfo.getLogmsg());
			valueEntity.setPriorityAnyVarbind(valueInfo.getPriorityAnyVarbind());
			valueEntity.setProcessingVarbindType(valueInfo.getProcessingVarbindType());
			valueEntity.setUei(valueInfo.getUei());
			valueEntity.setValidFlg(ValidConstant.booleanToType(valueInfo.getValidFlg()));
			valueEntity.setVersion(valueInfo.getVersion());

			int orderNo = 0;
			List<MonitorTrapVarbindPatternInfoEntity> patternEntities = new ArrayList<MonitorTrapVarbindPatternInfoEntity>();
			for (VarBindPattern varbindPattern: valueInfo.getVarBindPatterns()) {
				MonitorTrapVarbindPatternInfoEntity patternEntity = new MonitorTrapVarbindPatternInfoEntity(valueEntity, orderNo);
				patternEntity.setPattern(varbindPattern.getPattern());
				patternEntity.setDescription(varbindPattern.getDescription());
				patternEntity.setCaseSensitivityFlg(ValidConstant.booleanToType(varbindPattern.getCaseSensitivityFlg()));
				patternEntity.setProcessType(varbindPattern.getProcessType());
				patternEntity.setValidFlg(ValidConstant.booleanToType(varbindPattern.getValidFlg()));
				patternEntity.setPriority(varbindPattern.getPriority());
				patternEntities.add(patternEntity);
				orderNo++;
			}
			valueEntity.setMonitorTrapVarbindPatternInfoEntities(patternEntities);

			valueEntities.add(valueEntity);
		}
		trapEntity.setMonitorTrapValueInfoEntities(valueEntities);

		m_log.debug("addCheckInfo() : end");
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
	protected boolean addJudgementInfo() throws MonitorNotFound, InvalidRole {
		return true;
	}
}
