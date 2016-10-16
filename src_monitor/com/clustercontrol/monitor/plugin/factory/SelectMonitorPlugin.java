package com.clustercontrol.monitor.plugin.factory;

import java.util.ArrayList;

import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.plugin.bean.MonitorPluginNumericInfo;
import com.clustercontrol.monitor.plugin.bean.MonitorPluginStringInfo;
import com.clustercontrol.monitor.plugin.bean.PluginCheckInfo;
import com.clustercontrol.monitor.plugin.model.MonitorPluginInfoEntity;
import com.clustercontrol.monitor.plugin.model.MonitorPluginNumericInfoEntity;
import com.clustercontrol.monitor.plugin.model.MonitorPluginStringInfoEntity;
import com.clustercontrol.monitor.run.factory.SelectMonitor;

public class SelectMonitorPlugin extends SelectMonitor {

	@Override
	protected PluginCheckInfo getPluginCheckInfo() throws MonitorNotFound {

		// 監視情報を取得
		MonitorPluginInfoEntity entity = m_monitor.getMonitorPluginInfoEntity();

		PluginCheckInfo plugin = new PluginCheckInfo();
		plugin.setMonitorTypeId(m_monitorTypeId);
		plugin.setMonitorId(m_monitorId);

		//MonitorPluginStringInfoを設定
		ArrayList<MonitorPluginStringInfo> stringInfoList = new ArrayList<MonitorPluginStringInfo>();
		for(MonitorPluginStringInfoEntity stringInfoEntity: entity.getMonitorPluginStringInfoEntities()){
			MonitorPluginStringInfo stringInfo = new MonitorPluginStringInfo();
			stringInfo.setMonitorId(stringInfoEntity.getId().getMonitorId());
			stringInfo.setKey(stringInfoEntity.getId().getKey());
			stringInfo.setValue(stringInfoEntity.getValue());
			stringInfoList.add(stringInfo);
		}
		plugin.setMonitorPluginStringInfoList(stringInfoList);

		//MonitorPluginNumricInfoを設定
		ArrayList<MonitorPluginNumericInfo> numericInfoList = new ArrayList<MonitorPluginNumericInfo>();
		for(MonitorPluginNumericInfoEntity numericInfoEntity: entity.getMonitorPluginNumericInfoEntities()){
			MonitorPluginNumericInfo numericInfo = new MonitorPluginNumericInfo();
			numericInfo.setMonitorId(numericInfoEntity.getId().getMonitorId());
			numericInfo.setKey(numericInfoEntity.getId().getKey());
			numericInfo.setValue(numericInfoEntity.getValue());
			numericInfoList.add(numericInfo);
		}
		plugin.setMonitorPluginNumericInfoList(numericInfoList);

		return plugin;
	}
}
