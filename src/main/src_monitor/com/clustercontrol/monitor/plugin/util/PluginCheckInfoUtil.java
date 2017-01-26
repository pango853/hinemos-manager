package com.clustercontrol.monitor.plugin.util;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.plugin.bean.MonitorPluginNumericInfo;
import com.clustercontrol.monitor.plugin.bean.MonitorPluginStringInfo;
import com.clustercontrol.monitor.plugin.bean.PluginCheckInfo;
import com.clustercontrol.monitor.plugin.model.MonitorPluginInfoEntity;
import com.clustercontrol.monitor.plugin.model.MonitorPluginNumericInfoEntity;
import com.clustercontrol.monitor.plugin.model.MonitorPluginNumericInfoEntityPK;
import com.clustercontrol.monitor.plugin.model.MonitorPluginStringInfoEntity;
import com.clustercontrol.monitor.plugin.model.MonitorPluginStringInfoEntityPK;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.util.QueryUtil;

public class PluginCheckInfoUtil {

	public boolean addCheckInfo(PluginCheckInfo info) throws MonitorNotFound, HinemosUnknown,
		InvalidRole {
		MonitorInfoEntity monitorEntity = QueryUtil.getMonitorInfoPK(info.getMonitorId());

		// 監視情報を設定
		MonitorPluginInfoEntity entity = new MonitorPluginInfoEntity(monitorEntity);

		// 監視情報(数値リスト)を設定
		ArrayList<MonitorPluginNumericInfo> monitorPluginNumericInfoList = info.getMonitorPluginNumericInfoList();
		if(monitorPluginNumericInfoList != null){
			MonitorPluginNumericInfo monitorPluginNumericInfo = null;
			for(int index=0; index<monitorPluginNumericInfoList.size(); index++){
				monitorPluginNumericInfo = monitorPluginNumericInfoList.get(index);

				if(monitorPluginNumericInfo != null){
					MonitorPluginNumericInfoEntity nEntity = new MonitorPluginNumericInfoEntity(
							monitorPluginNumericInfo.getMonitorId(),
							monitorPluginNumericInfo.getKey(),
							entity);
					nEntity.setValue(monitorPluginNumericInfo.getValue());
				}
			}
		}

		// 監視情報(文字列リスト)を設定
		ArrayList<MonitorPluginStringInfo> monitorPluginStringInfoList = info.getMonitorPluginStringInfoList();
		if(monitorPluginStringInfoList != null){
			MonitorPluginStringInfo monitorPluginStringInfo = null;
			for(int index=0; index<monitorPluginStringInfoList.size(); index++){
				monitorPluginStringInfo = monitorPluginStringInfoList.get(index);

				if(monitorPluginStringInfo != null){
					MonitorPluginStringInfoEntity sEntity = new MonitorPluginStringInfoEntity(
							monitorPluginStringInfo.getMonitorId(),
							monitorPluginStringInfo.getKey(),
							entity);
					sEntity.setValue(monitorPluginStringInfo.getValue());
				}
			}
		}
		return true;
	}

	public boolean modifyCheckInfo(PluginCheckInfo info) throws MonitorNotFound, HinemosUnknown,
		InvalidRole {
		MonitorInfoEntity monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(info.getMonitorId());

		// 監視情報を取得
		MonitorPluginInfoEntity pluginEntity = com.clustercontrol.monitor.plugin.util.QueryUtil.getMonitorPluginInfoPK(info.getMonitorId());

		// 監視情報を設定
		PluginCheckInfo plugin = info;
		monitorEntity.setMonitorPluginInfoEntity(pluginEntity);

		////
		// 監視情報(数値リスト)を設定
		////
		ArrayList<MonitorPluginNumericInfo> monitorPluginNumericInfoList = plugin.getMonitorPluginNumericInfoList();
		if(monitorPluginNumericInfoList != null){
			List<MonitorPluginNumericInfoEntityPK> monitorPluginNumericInfoEntityPKList = new ArrayList<MonitorPluginNumericInfoEntityPK>();
			for(MonitorPluginNumericInfo value : monitorPluginNumericInfoList){
				if(value != null){
					MonitorPluginNumericInfoEntity nEntity = null;
					MonitorPluginNumericInfoEntityPK entityPk = new MonitorPluginNumericInfoEntityPK(
							value.getMonitorId(),
							value.getKey());
					try {
						nEntity = com.clustercontrol.monitor.plugin.util.QueryUtil.getMonitorPluginNumericInfoEntity(entityPk);
					} catch (MonitorNotFound e) {
						nEntity = new MonitorPluginNumericInfoEntity(entityPk, pluginEntity);
					}
					nEntity.setValue(value.getValue());
					monitorPluginNumericInfoEntityPKList.add(entityPk);
				}
			}
			// 不要なMonitorPluginNumericInfoEntityを削除
			pluginEntity.deleteMonitorPluginNumericInfoEntities(monitorPluginNumericInfoEntityPKList);
		}


		////
		// 監視情報(文字列リスト)を設定
		////
		ArrayList<MonitorPluginStringInfo> monitorPluginStringInfoList = plugin.getMonitorPluginStringInfoList();
		if(monitorPluginStringInfoList != null){
			List<MonitorPluginStringInfoEntityPK> monitorPluginStringInfoEntityPKList = new ArrayList<MonitorPluginStringInfoEntityPK>();
			for(MonitorPluginStringInfo value : monitorPluginStringInfoList){
				if(value != null){
					MonitorPluginStringInfoEntity sEntity = null;
					MonitorPluginStringInfoEntityPK entityPk = new MonitorPluginStringInfoEntityPK(
							value.getMonitorId(),
							value.getKey());
					try {
						sEntity = com.clustercontrol.monitor.plugin.util.QueryUtil.getMonitorPluginStringInfoEntity(entityPk);
					} catch (MonitorNotFound e) {
						sEntity = new MonitorPluginStringInfoEntity(entityPk, pluginEntity);
					}
					sEntity.setValue(value.getValue());
					monitorPluginStringInfoEntityPKList.add(entityPk);
				}
			}
			// 不要なMonitorPluginStringInfoEntityを削除
			pluginEntity.deleteMonitorPluginStringInfoEntities(monitorPluginStringInfoEntityPKList);

		}

		return true;
	}
}
