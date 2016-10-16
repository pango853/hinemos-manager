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

package com.clustercontrol.process.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.plugin.impl.SharedTablePlugin;
import com.clustercontrol.poller.NotInitializedException;
import com.clustercontrol.poller.PollerManager;
import com.clustercontrol.poller.PollingController;
import com.clustercontrol.process.util.PollingDataManager;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.sharedtable.DataTableNotFoundException;
import com.clustercontrol.sharedtable.SharedTable;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * ポーリングを共通ポーラーに登録するクラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class ModifyPollingSchedule {

	private static Log m_log = LogFactory.getLog( ModifyPollingSchedule.class );

	/**
	 * ポーリングに登録します。<BR>
	 * 
	 * @param parentFacilityId ファシリティID
	 * @param interval 収集間隔(秒)
	 */
	public boolean addSchedule(String monitorTypeId, String monitorId, String parentFacilityId, int interval) {
		m_log.debug("addSchedule() monitorId = " + monitorId + ", parentFacilityId = " + parentFacilityId);

		if(parentFacilityId != null && !"".equals(parentFacilityId)){

			try{
				// SNMP収集値の共有テーブルをルックアップ
				SharedTable sst = SharedTablePlugin.getSharedTable();
				MonitorInfoEntity entity = QueryUtil.getMonitorInfoPK_NONE(monitorId);
				// ファシリティIDの配下全ての一覧を取得
				RepositoryControllerBean repository = new RepositoryControllerBean();
				ArrayList<String> facilityList = repository.getExecTargetFacilityIdList(parentFacilityId, entity.getOwnerRoleId());

				if(m_log.isDebugEnabled()){
					for (String facilityId : facilityList) {
						m_log.debug("addSchedule() add schedule target facilityId = " + facilityId);
					}
				}

				// ファシリティ毎に登録
				String facilityId = null;
				for(int index=0; index<facilityList.size(); index++){
					facilityId = facilityList.get(index);
					if(facilityId != null && !"".equals(facilityId)){
						// テーブル生成
						// テーブルが存在しない場合は生成する
						// 時系列情報を必要としないためページサイズは1を指定する
						if(sst.containsTable(monitorTypeId, facilityId, 1) == false){
							sst.createDataTable(monitorTypeId, facilityId, 1);
						}

						// ポーラを生成する
						PollerManager manager = PollerManager.getInstnace();

						// ポーリングを開始する
						try {
							// ポーラを取得
							PollingController poller = manager.getPoller(monitorTypeId, facilityId);
							// ない場合は生成する
							if(poller == null){
								poller = manager.createPoller(monitorTypeId, facilityId, true, monitorTypeId, facilityId);
							}

							// リポジトリ,DBから設定情報を取得する(プラットフォームID, サブプラットフォームID, 収集方法など)
							PollingDataManager dataManager = new PollingDataManager(facilityId);
							String collectMethod = dataManager.getCollectMethod();

							// 取得したプラットフォームにおいて、プロセス監視で何が使われているかを取得
							HashMap<String, List<String>> map = new HashMap<String, List<String>>();
							map.put(collectMethod, dataManager.getPollingTargets(collectMethod));

							poller.startPolling(monitorId, interval, map);
						} catch (NotInitializedException e) {
						} catch (DataTableNotFoundException e) {
						}
					}
				}
				return true;

			}catch(MonitorNotFound e){
				AplLogger apllog = new AplLogger("PROC", "proc");
				String[] args = { parentFacilityId };
				apllog.put("SYS", "001", args);
			}catch(HinemosUnknown e){
				AplLogger apllog = new AplLogger("PROC", "proc");
				String[] args = { parentFacilityId };
				apllog.put("SYS", "001", args);
			}
		}
		return false;
	}

	/**
	 * SNMPポーリングを停止します。<BR>
	 * 
	 * @param parentFacilityId ファシリティID
	 * @param interval 収集間隔(秒)
	 */
	protected boolean deleteSchedule(String monitorTypeId, String monitorId, String parentFacilityId) {

		if(parentFacilityId != null && !"".equals(parentFacilityId)){

			try{
				// ファシリティIDの配下全ての一覧を取得
				MonitorInfoEntity entity = QueryUtil.getMonitorInfoPK_NONE(monitorId);
				RepositoryControllerBean repository = new RepositoryControllerBean();
				ArrayList<String> facilityList = repository.getNodeFacilityIdList(parentFacilityId, entity.getOwnerRoleId(), RepositoryControllerBean.ALL);

				//配下にノードがないということはノードの可能性があるので指定されたIDをセット
				if (facilityList.size() == 0) {
					if(repository.isNode(parentFacilityId)){
						facilityList.add(parentFacilityId);
					}
				}

				// ノード毎にポーリングを停止する
				String facilityId = null;
				for(int index=0; index<facilityList.size(); index++){
					facilityId = facilityList.get(index);

					// ポーラを生成する
					PollerManager manager = PollerManager.getInstnace();

					// ポーリングを停止する
					PollingController poller = manager.getPoller(monitorTypeId, facilityId);
					if(poller != null){
						poller.stopPolling(monitorId);
					}

					// テーブルから参照登録を削除する
					SharedTable sharedTable = SharedTablePlugin.getSharedTable();

					try {
						sharedTable.unregisterCollector(monitorTypeId, facilityId, monitorId);
					} catch (DataTableNotFoundException e) {
					}
				}
				return true;

			} catch (MonitorNotFound e) {
				AplLogger apllog = new AplLogger("PROC", "proc");
				String[] args = { parentFacilityId };
				apllog.put("SYS", "002", args);
			} catch (FacilityNotFound e) {
				AplLogger apllog = new AplLogger("PROC", "proc");
				String[] args = { parentFacilityId };
				apllog.put("SYS", "002", args);
			} catch (InvalidRole e) {
				AplLogger apllog = new AplLogger("PROC", "proc");
				String[] args = { parentFacilityId };
				apllog.put("SYS", "002", args);
			} catch (HinemosUnknown e) {
				AplLogger apllog = new AplLogger("PROC", "proc");
				String[] args = { parentFacilityId };
				apllog.put("SYS", "002", args);
			}
		}
		return false;
	}
}