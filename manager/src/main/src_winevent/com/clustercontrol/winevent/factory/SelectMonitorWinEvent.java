/*

 Copyright (C) 2013 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.winevent.factory;

import java.util.List;

import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.winevent.bean.WinEventCheckInfo;
import com.clustercontrol.winevent.model.MonitorWinEventCategoryInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventIdInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventKeywordInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventLogInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventSourceInfoEntity;

/**
 * Windowsイベント監視情報を検索するクラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class SelectMonitorWinEvent extends SelectMonitor{

	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.SelectMonitor#getCheckInfo()
	 */
	@Override
	protected WinEventCheckInfo getWinEventCheckInfo() throws MonitorNotFound {
		// Windowsイベント監視情報を取得
		MonitorWinEventInfoEntity entity = com.clustercontrol.winevent.util.QueryUtil.getMonitorWinEventInfoPK(m_monitorId);

		WinEventCheckInfo checkInfo = new WinEventCheckInfo();
		checkInfo.setMonitorId(m_monitorId);
		checkInfo.setMonitorTypeId(m_monitorTypeId);
		checkInfo.setLevelCritical(entity.isLevelCritical());
		checkInfo.setLevelWarning(entity.isLevelWarning());
		checkInfo.setLevelVerbose(entity.isLevelVerbose());
		checkInfo.setLevelError(entity.isLevelError());
		checkInfo.setLevelInformational(entity.isLevelInformational());

		List<MonitorWinEventLogInfoEntity> logs = com.clustercontrol.winevent.util.QueryUtil.getMonitorWinEventLogInfoByMonitorId(m_monitorId);
		for(MonitorWinEventLogInfoEntity log : logs){
			checkInfo.getLogName().add(log.getId().getLogName());
		}

		List<MonitorWinEventSourceInfoEntity> sources = com.clustercontrol.winevent.util.QueryUtil.getMonitorWinEventSourceInfoByMonitorId(m_monitorId);
		for(MonitorWinEventSourceInfoEntity source : sources){
			checkInfo.getSource().add(source.getId().getSource());
		}

		List<MonitorWinEventIdInfoEntity> ids = com.clustercontrol.winevent.util.QueryUtil.getMonitorWinEventIdInfoByMonitorId(m_monitorId);
		for(MonitorWinEventIdInfoEntity id : ids){
			checkInfo.getEventId().add(id.getId().getEventId());
		}

		List<MonitorWinEventCategoryInfoEntity> categories = com.clustercontrol.winevent.util.QueryUtil.getMonitorWinEventCategoryInfoByMonitorId(m_monitorId);
		for(MonitorWinEventCategoryInfoEntity category : categories){
			checkInfo.getCategory().add(category.getId().getCategory());
		}

		List<MonitorWinEventKeywordInfoEntity> keywords = com.clustercontrol.winevent.util.QueryUtil.getMonitorWinEventKeywordInfoByMonitorId(m_monitorId);
		for(MonitorWinEventKeywordInfoEntity keyword : keywords){
			checkInfo.getKeywords().add(keyword.getId().getKeyword());
		}

		return checkInfo;
	}
}
