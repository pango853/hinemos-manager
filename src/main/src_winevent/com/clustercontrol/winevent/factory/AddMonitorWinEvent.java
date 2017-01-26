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

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.AddMonitorStringValueType;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;
import com.clustercontrol.winevent.bean.WinEventCheckInfo;
import com.clustercontrol.winevent.model.MonitorWinEventCategoryInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventCategoryInfoEntityPK;
import com.clustercontrol.winevent.model.MonitorWinEventIdInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventIdInfoEntityPK;
import com.clustercontrol.winevent.model.MonitorWinEventInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventKeywordInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventKeywordInfoEntityPK;
import com.clustercontrol.winevent.model.MonitorWinEventLogInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventLogInfoEntityPK;
import com.clustercontrol.winevent.model.MonitorWinEventSourceInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventSourceInfoEntityPK;

/**
 * Windowsイベント監視情報を登録するクラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class AddMonitorWinEvent extends AddMonitorStringValueType{

	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.AddMonitor#addCheckInfo()
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {

		MonitorInfoEntity monitorEntity = QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		// Windowsイベント監視設定を新規登録する
		WinEventCheckInfo checkInfo = m_monitorInfo.getWinEventCheckInfo();
		MonitorWinEventInfoEntity entity = new MonitorWinEventInfoEntity(monitorEntity);
		entity.setLevelCritical(checkInfo.isLevelCritical());
		entity.setLevelWarning(checkInfo.isLevelWarning());
		entity.setLevelVerbose(checkInfo.isLevelVerbose());
		entity.setLevelError(checkInfo.isLevelError());
		entity.setLevelInformational(checkInfo.isLevelInformational());

		List<MonitorWinEventLogInfoEntity> logs = new ArrayList<MonitorWinEventLogInfoEntity>();
		for(String logName : checkInfo.getLogName()){
			MonitorWinEventLogInfoEntity log = new MonitorWinEventLogInfoEntity(new MonitorWinEventLogInfoEntityPK(m_monitorInfo.getMonitorId(), logName), entity);
			logs.add(log);
		}
		entity.setMonitorWinEventLogInfoEntities(logs);

		List<MonitorWinEventSourceInfoEntity> sources = new ArrayList<MonitorWinEventSourceInfoEntity>();
		for(String sourceName : checkInfo.getSource()){
			MonitorWinEventSourceInfoEntity source = new MonitorWinEventSourceInfoEntity(new MonitorWinEventSourceInfoEntityPK(m_monitorInfo.getMonitorId(), sourceName), entity);
			sources.add(source);
		}
		entity.setMonitorWinEventSourceInfoEntities(sources);

		List<MonitorWinEventIdInfoEntity> ids = new ArrayList<MonitorWinEventIdInfoEntity>();
		for(Integer eventId : checkInfo.getEventId()){
			MonitorWinEventIdInfoEntity id = new MonitorWinEventIdInfoEntity(new MonitorWinEventIdInfoEntityPK(m_monitorInfo.getMonitorId(), eventId), entity);
			ids.add(id);
		}
		entity.setMonitorWinEventIdInfoEntities(ids);

		List<MonitorWinEventCategoryInfoEntity> categories = new ArrayList<MonitorWinEventCategoryInfoEntity>();
		for(Integer categoryNumber : checkInfo.getCategory()){
			MonitorWinEventCategoryInfoEntity category = new MonitorWinEventCategoryInfoEntity(new MonitorWinEventCategoryInfoEntityPK(m_monitorInfo.getMonitorId(), categoryNumber), entity);
			categories.add(category);
		}
		entity.setMonitorWinEventCategoryInfoEntities(categories);

		List<MonitorWinEventKeywordInfoEntity> keywords = new ArrayList<MonitorWinEventKeywordInfoEntity>();
		for(Long keywordNumber : checkInfo.getKeywords()){
			MonitorWinEventKeywordInfoEntity keyword = new MonitorWinEventKeywordInfoEntity(new MonitorWinEventKeywordInfoEntityPK(m_monitorInfo.getMonitorId(), keywordNumber), entity);
			keywords.add(keyword);
		}
		entity.setMonitorWinEventKeywordInfoEntities(keywords);

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
}
