/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

 */

package com.clustercontrol.performance.monitor.factory;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.bean.MonitorInfo;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.notify.bean.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.performance.monitor.bean.PerfCheckInfo;
import com.clustercontrol.performance.monitor.model.MonitorPerfInfoEntity;
import com.clustercontrol.performance.monitor.util.QueryUtil;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * リソース監視判定情報検索クラス
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class SelectMonitorPerformance extends SelectMonitor{

	private static Log m_log = LogFactory.getLog( SelectMonitorPerformance.class );

	/**
	 * リソース監視情報を取得します。
	 * @see com.clustercontrol.monitor.run.factory.SelectMonitor#getCheckInfo()
	 */
	@Override
	protected PerfCheckInfo getPerfCheckInfo() throws MonitorNotFound {

		m_log.debug("getPerfCheckInfo() monitorId = " + m_monitorId);

		// リソース監視情報を取得
		MonitorPerfInfoEntity entity = QueryUtil.getMonitorPerfInfoPK(m_monitorId);

		PerfCheckInfo perfCheck = new PerfCheckInfo();
		perfCheck.setMonitorTypeId(m_monitorTypeId);
		perfCheck.setMonitorId(m_monitorId);
		perfCheck.setItemCode(entity.getItemCode());
		perfCheck.setDeviceDisplayName(entity.getDeviceDisplayName());
		perfCheck.setBreakdownFlg(entity.getBreakdownFlg());

		return perfCheck;
	}


	/**
	 * リソース監視情報を取得
	 * 
	 * @param monitorTypeId 監視対象ID
	 * @param monitorId 監視項目ID
	 * @param isGetAll 全監視情報取得フラグ
	 * @return MonitorInfo
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	@Override
	public MonitorInfo getMonitor(String monitorTypeId, String monitorId) throws MonitorNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("getMonitor() monitorTypeId = " + monitorTypeId + ", monitorId = " + monitorId);

		m_monitorTypeId = monitorTypeId;
		m_monitorId = monitorId;

		MonitorInfo bean = null;
		try
		{
			// 監視情報を取得
			m_monitor = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_NONE(m_monitorId);

			// スコープの取得
			String facilityPath = new RepositoryControllerBean().getFacilityPath(m_monitor.getFacilityId(), null);


			//通知情報の取得
			List<NotifyRelationInfo> notifyId = new NotifyControllerBean().getNotifyRelation(m_monitor.getNotifyGroupId());

			bean = new MonitorInfo(
					m_monitor.getApplication(),
					m_monitor.getCalendarId(),
					m_monitor.getDescription(),
					facilityPath,
					m_monitor.getFacilityId(),
					m_monitor.getFailurePriority(),
					m_monitorId,
					m_monitorTypeId,
					m_monitor.getMonitorType(),
					m_monitor.getRegDate()==null?null:m_monitor.getRegDate().getTime(),
					m_monitor.getRegUser(),
					m_monitor.getRunInterval(),
					notifyId,
					m_monitor.getUpdateDate()==null?null:m_monitor.getUpdateDate().getTime(),
					m_monitor.getUpdateUser(),
					m_monitor.getMonitorFlg(),
					m_monitor.getCollectorFlg(),
					m_monitor.getItemName(),
					m_monitor.getMeasure(),
					m_monitor.getOwnerRoleId(),
					getNumericValueInfo(),
					getStringValueInfo(),
					getTruthValueInfo(),
					getHttpCheckInfo(),
					getHttpScenarioCheckInfo(),
					getPerfCheckInfo(),
					getPingCheckInfo(),
					getPluginCheckInfo(),
					getPortCheckInfo(),
					getProcessCheckInfo(),
					getSnmpCheckInfo(),
					getSqlCheckInfo(),
					getTrapCheckInfo(),
					getCommandCheckInfo(),
					getLogfileCheckInfo(),
					getWinServiceCheckInfo(),
					getWinEventCheckInfo(),
					getJmxCheckInfo());

		} catch (MonitorNotFound e) {
			outputLog("010");
			throw e;
		} catch (InvalidRole e) {
			outputLog("010");
			throw e;
		} catch (HinemosUnknown e) {
			outputLog("010");
			throw e;
		}

		return bean;
	}

	/**
	 * アプリケーションログにログを出力
	 * 
	 * @param index アプリケーションログのインデックス
	 */
	private void outputLog(String index) {
		AplLogger apllog = new AplLogger("PERF", "perf");
		String[] args = {m_monitorTypeId, m_monitorId };
		apllog.put("SYS", index, args);
	}
}
