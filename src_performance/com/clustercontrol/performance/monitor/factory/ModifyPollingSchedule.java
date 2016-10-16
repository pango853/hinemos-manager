package com.clustercontrol.performance.monitor.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.bean.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.performance.monitor.bean.PerfCheckInfo;
import com.clustercontrol.performance.util.PollingDataManager;
import com.clustercontrol.plugin.impl.SharedTablePlugin;
import com.clustercontrol.poller.NotInitializedException;
import com.clustercontrol.poller.PollerManager;
import com.clustercontrol.poller.PollingController;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.sharedtable.DataTableNotFoundException;
import com.clustercontrol.sharedtable.SharedTable;
import com.clustercontrol.util.apllog.AplLogger;

public class ModifyPollingSchedule {

	private static Log m_log = LogFactory.getLog( ModifyPollingSchedule.class );

	/**
	 * スケジュールに登録します。
	 * 
	 * @param monitorTypeId
	 * @param monitorId
	 * @param parentFacilityId
	 * @param interval
	 * @return
	 */
	public boolean addSchedule(MonitorInfo info) {
		m_log.debug("addSchedule() monitorId = " + info.getMonitorId() + ", parentFacilityId = " + info.getFacilityId());

		String monitorTypeId = info.getMonitorTypeId();
		String monitorId = info.getMonitorId();
		String parentFacilityId = info.getFacilityId();
		int interval = info.getRunInterval();

		if(parentFacilityId != null && !"".equals(parentFacilityId)){

			try{
				// ファシリティIDの配下全ての一覧を取得
				ArrayList<String> facilityList
				= new RepositoryControllerBean().getExecTargetFacilityIdList(parentFacilityId, info.getOwnerRoleId());

				if(m_log.isDebugEnabled()){
					for (String facilityId : facilityList) {
						m_log.debug("addSchedule() add schedule target facilityId = " + facilityId);
					}
				}

				// ファシリティ毎に登録
				boolean ret = true;
				for(String facilityId : facilityList){
					m_log.debug("addSchedule() target facilityId = " + facilityId);

					if(facilityId != null && !"".equals(facilityId)){
						ret = ret & addNodeSchedule(facilityId, monitorId, monitorTypeId, interval, info.getPerfCheckInfo());
					}
				}
				return ret;

			}catch(FacilityNotFound e){
				AplLogger apllog = new AplLogger("PERF", "perf");
				String[] args = { parentFacilityId };
				apllog.put("SYS", "014", args);
			}catch(HinemosUnknown e){
				AplLogger apllog = new AplLogger("PERF", "perf");
				String[] args = { parentFacilityId };
				apllog.put("SYS", "014", args);
			}
		}
		return false;

	}

	/**
	 * ノード別のポーラを登録する
	 * 
	 * @param facilityId
	 * @param monitorId
	 * @param monitorTypeId
	 * @param interval
	 * @param perfCheckInfo
	 * @return
	 * @throws FacilityNotFound
	 */
	public boolean addNodeSchedule(String facilityId, String monitorId, String monitorTypeId, int interval, PerfCheckInfo perfCheckInfo)
			throws FacilityNotFound {
		m_log.debug("addNodeSchedule() facilityId = " + facilityId + ", monitorId = " + monitorId + ", monitorTypeId = " + monitorTypeId + ", interval = " + interval);

		// 収集値の共有テーブルをルックアップ
		SharedTable sst = SharedTablePlugin.getSharedTable();

		// テーブル生成
		// テーブルが存在しない場合は生成する
		// 一部の項目で時系列情報が必要なためページサイズは2を指定する
		if(sst.containsTable(HinemosModuleConstant.PERFORMANCE, facilityId, 2) == false){
			sst.createDataTable(HinemosModuleConstant.PERFORMANCE, facilityId, 2);
		}

		// ポーラを生成する
		PollerManager manager = PollerManager.getInstnace();

		// ポーリングを開始する
		try {
			// ポーラを取得
			PollingController poller = manager.getPoller(monitorTypeId, facilityId);
			// ない場合は生成する
			if(poller == null){
				m_log.debug("addSchedule() create poller");
				poller = manager.createPoller(monitorTypeId, facilityId, false, HinemosModuleConstant.PERFORMANCE, facilityId);
			}
			// ある場合はTableHolderにアクセスして最終参照日時を更新する
			else{
				try{
					sst.getLastDataTables(HinemosModuleConstant.PERFORMANCE, facilityId, monitorId, 2);
					m_log.info("addSchedule() update last reference date : " + HinemosModuleConstant.PERFORMANCE + ", " + facilityId + ", " + monitorId);
				} catch (NotInitializedException e) {
					m_log.debug("addSchedule()", e);
				} catch (DataTableNotFoundException e) {
					m_log.debug("addSchedule()", e);
				}
			}

			// リポジトリ,DBから設定情報を取得する(プラットフォームID, サブプラットフォームID, 収集方法など)
			if(perfCheckInfo == null){
				m_log.info("addSchedule() perfCheckInfo is null");
				return false;
			}
			PollingDataManager dataManager =
					new PollingDataManager(facilityId,
							perfCheckInfo.getItemCode(),
							perfCheckInfo.isBreakdown());

			String collectMethod = dataManager.getCollectMethod();
			List<String> pollingTargets = dataManager.getPollingTargets();

			// 取得したプラットフォームにおいて、リソース監視で何が使われているかを取得
			HashMap<String, List<String>> map = new HashMap<String, List<String>>();
			map.put(collectMethod, pollingTargets);

			m_log.debug("addSchedule() start poller");
			poller.startPolling(monitorId, interval, map);
		} catch (NotInitializedException e) {
			return false;
		} catch (DataTableNotFoundException e) {
			return false;
		}

		return true;
	}

	/**
	 * スケジュールの登録を削除する
	 * 
	 * @param monitorTypeId
	 * @param monitorId
	 * @param parentFacilityId
	 * @return
	 */
	public boolean deleteSchedule(String monitorTypeId, String monitorId, String parentFacilityId) {
		m_log.debug("deleteSchedule() monitorId = " + monitorId);

		// ポーラの削除
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
						sharedTable.unregisterCollector(HinemosModuleConstant.PERFORMANCE, facilityId, monitorId);
					} catch (DataTableNotFoundException e) {
						m_log.debug("deleteSchedule() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
					}
				}
				return true;

			} catch (FacilityNotFound e) {
				AplLogger apllog = new AplLogger("PERF", "perf");
				String[] args = { parentFacilityId };
				apllog.put("SYS", "015", args);
			} catch (MonitorNotFound e) {
				AplLogger apllog = new AplLogger("PERF", "perf");
				String[] args = { parentFacilityId };
				apllog.put("SYS", "015", args);
			} catch (InvalidRole e) {
				AplLogger apllog = new AplLogger("PERF", "perf");
				String[] args = { parentFacilityId };
				apllog.put("SYS", "015", args);
			} catch (HinemosUnknown e){
				AplLogger apllog = new AplLogger("PERF", "perf");
				String[] args = { parentFacilityId };
				apllog.put("SYS", "015", args);
			}
		}
		return false;
	}

}
