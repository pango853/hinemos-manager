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

package com.clustercontrol.performance.session;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CollectorNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.bean.MonitorInfo;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.performance.bean.CollectedDataInfo;
import com.clustercontrol.performance.bean.CollectedDataList;
import com.clustercontrol.performance.bean.CollectedDataSet;
import com.clustercontrol.performance.bean.CollectorItemInfo;
import com.clustercontrol.performance.bean.CollectorItemParentInfo;
import com.clustercontrol.performance.bean.PerformanceDataSettings;
import com.clustercontrol.performance.bean.PerformanceFilterInfo;
import com.clustercontrol.performance.bean.PerformanceListInfo;
import com.clustercontrol.performance.model.CalculatedDataEntity;
import com.clustercontrol.performance.monitor.model.CollectorItemCodeMstEntity;
import com.clustercontrol.performance.util.ExportCollectedDataFile;
import com.clustercontrol.performance.util.QueryUtil;
import com.clustercontrol.performance.util.code.CollectorItemCodeTable;
import com.clustercontrol.performance.util.code.CollectorItemTreeItem;
import com.clustercontrol.repository.bean.FacilityTreeItem;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 *　性能管理機能の管理を行うコントローラクラス
 * クライアントからの Entity Bean へのアクセスは、このSession Bean を介して行います。
 * 
 * @version 4.0.0
 * @since 1.0.0
 *
 */
public class CollectorControllerBean {

	//	ログ出力
	private static Log m_log = LogFactory.getLog(CollectorControllerBean.class);

	/**
	 * 性能[一覧]に表示するためのリストを取得します。
	 * 以下の条件のうちいずれかを満たす監視設定を取得します。
	 * 
	 * ・収集が有効になっていること
	 * ・収集データが1件以上存在すること
	 * 
	 * @return
	 * @throws HinemosUnknown
	 */
	public ArrayList<PerformanceListInfo> getPerformanceList() throws HinemosUnknown {
		m_log.debug("getPerformanceList()");

		JpaTransactionManager jtm = null;
		ArrayList<PerformanceListInfo> ret = new ArrayList<PerformanceListInfo>();
		long start = System.currentTimeMillis();

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			ArrayList<MonitorInfo> monitorList = new SelectMonitor().getPerformanceMonitorList();
			m_log.info(" getPerformanceList start " + (System.currentTimeMillis()-start) + "ms");

			// 収集項目ID一覧、収集項目IDに対して存在するデバイス表示名を設定
			HashMap<String, Date> oldMap = new HashMap<String, Date>();
			HashMap<String, Date> lateMap = new HashMap<String, Date>();

			// JPQLではLATERALに対応していないため、直接SQLを実行する
			Connection conn = null;
			JpaTransactionManager tm = null;
			String sql =
					"SELECT c.monitor_id, a.min, a.max "
					+ "FROM setting.cc_monitor_info c, LATERAL "
					+ "(SELECT min(date_time) as min, max(date_time) as max "
					+ "FROM log.cc_calculated_data WHERE c.monitor_id = log.cc_calculated_data.collectorid)a";
			try {
				tm = new JpaTransactionManager();
				conn = tm.getEntityManager().unwrap(java.sql.Connection.class);
				conn.setAutoCommit(false);
				Statement stmt = conn.createStatement();
				ResultSet result = stmt.executeQuery(sql);
				while (result.next()) {
					String collectorId = result.getString("monitor_id");
					Date oldDate = result.getTimestamp("min");
					Date lateDate = result.getTimestamp("max");
					if (oldDate == null) {
						continue;
					}
					if (lateDate == null) {
						continue; // ここには到達しないはずだが。。。
					}
					oldMap.put(collectorId, oldDate);
					lateMap.put(collectorId, lateDate);
				}
				
				if (! tm.isNestedEm()) {
					conn.commit();
				}
			} catch (Exception e) {
				m_log.warn(e);
				if (conn != null) {
					try {
						conn.rollback();
					} catch (SQLException e1) {
						m_log.warn(e1);
					}
				}
			} finally {
				if (tm != null) {
					tm.close();
				}
			}

			for (MonitorInfo monitorInfo : monitorList){

				m_log.debug("getPerformanceList() target monitorId = " + monitorInfo.getMonitorId());

				if (
					monitorInfo.getMonitorType() == MonitorTypeConstant.TYPE_NUMERIC ||
					monitorInfo.getMonitorType() == MonitorTypeConstant.TYPE_SCENARIO
					) {
					m_log.debug("getPerformanceList() add monitorId = " + monitorInfo.getMonitorId());

					PerformanceListInfo perfListInfo = new PerformanceListInfo();
					perfListInfo.setCollectorFlg(monitorInfo.getCollectorFlg());
					String collectorId = monitorInfo.getMonitorId();
					perfListInfo.setMonitorId(monitorInfo.getMonitorId());
					perfListInfo.setMonitorTypeId(monitorInfo.getMonitorTypeId());
					perfListInfo.setDescription(monitorInfo.getDescription());
					perfListInfo.setFacilityId(monitorInfo.getFacilityId());
					perfListInfo.setScopeText(monitorInfo.getScope());
					perfListInfo.setRunInterval(monitorInfo.getRunInterval());

					if (lateMap.get(collectorId) != null) {
						perfListInfo.setLatestDate(lateMap.get(collectorId).getTime());
					} else {
						perfListInfo.setLatestDate(null);
					}
					if (oldMap.get(collectorId) != null) {
						perfListInfo.setOldestDate(oldMap.get(collectorId).getTime());
					} else {
						perfListInfo.setOldestDate(null);
					}
					ret.add(perfListInfo);
				}
			}
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getPerformanceList() "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			jtm.close();
		}

		long end = System.currentTimeMillis();
		m_log.info("getPerformanceList end " + (end-start) + "ms");
		return ret;

	}


	/**
	 * 性能[一覧]に表示するためのリストを取得します。
	 * 以下の条件のうちいずれかを満たす監視設定を取得します。
	 * 
	 * ・収集が有効になっていること
	 * ・収集データが1件以上存在すること
	 * ・フィルタ条件に合致すること
	 * 
	 * @param condition
	 * @return
	 * @throws HinemosUnknown
	 */
	public ArrayList<PerformanceListInfo> getPerformanceList(PerformanceFilterInfo condition) throws HinemosUnknown {
		m_log.debug("getPerformanceList() condition");
		if(m_log.isDebugEnabled()){
			if(condition != null){
				m_log.debug("getPerformanceList() " +
						"monitorId = " + condition.getMonitorId() +
						", monitorTypeId = " + condition.getMonitorTypeId() +
						", description = " + condition.getDescription() +
						", oldestFromDate = " + condition.getOldestFromDate() +
						", oldestToDate = " + condition.getOldestToDate() +
						", latestFromDate = " + condition.getLatestFromDate() +
						", latestToDate = " + condition.getLatestToDate());
			}
		}

		ArrayList<PerformanceListInfo> filterList = new ArrayList<PerformanceListInfo>();
		// 条件未設定の場合は空のリストを返却する
		if(condition == null){
			m_log.debug("getPerformanceList() condition is null");
			return filterList;
		}

		// DBではなくJavaAPにてフィルタリングを行う
		for(PerformanceListInfo info : getPerformanceList()){


			// monitorId
			if(condition.getMonitorId() != null && !"".equals(condition.getMonitorId())){
				if(info.getMonitorId() == null || (info.getMonitorId() != null && !info.getMonitorId().matches(".*" + condition.getMonitorId() + ".*"))){
					m_log.debug("getPerformanceList() continue : monitorId target = " + info.getMonitorId() + ", filter = " + condition.getMonitorId());
					continue;
				}
			}

			// monitorTypeId
			if(condition.getMonitorTypeId() != null && !"".equals(condition.getMonitorTypeId())){
				if(info.getMonitorTypeId() == null || (info.getMonitorTypeId() != null && !info.getMonitorTypeId().matches(".*" + condition.getMonitorTypeId() + ".*"))){
					m_log.debug("getPerformanceList() continue : monitorTypeId target = " + info.getMonitorTypeId() + ", filter = " + condition.getMonitorTypeId());
					continue;
				}
			}

			// description
			if(condition.getDescription() != null && !"".equals(condition.getDescription())){
				if(info.getDescription() == null || (info.getDescription() != null && !info.getDescription().matches(".*" + condition.getDescription() + ".*"))){
					m_log.debug("getPerformanceList() continue : description target = " + info.getDescription() + ", filter = " + condition.getDescription());
					continue;
				}
			}

			// oldestFromDate
			if(condition.getOldestFromDate() > 0){
				if(info.getOldestDate() == null || !(info.getOldestDate() >= condition.getOldestFromDate())){
					m_log.debug("getPerformanceList() continue : oldestFromDate target = " + info.getOldestDate() + ", filter = " + condition.getOldestFromDate());
					continue;
				}
			}
			// oldestToDate
			if(condition.getOldestToDate() > 0){
				if(info.getOldestDate() == null || !(info.getOldestDate() <= condition.getOldestToDate())){
					m_log.debug("getPerformanceList() continue : oldestToDate target = " + info.getOldestDate() + ", filter = " + condition.getOldestToDate());
					continue;
				}
			}

			// latestFromDate
			if(condition.getLatestFromDate() > 0){
				if(info.getLatestDate() == null || !(info.getLatestDate() >= condition.getLatestFromDate())){
					m_log.debug("getPerformanceList() continue : latestFromDate target = " + info.getLatestDate() + ", filter = " + condition.getLatestFromDate());
					continue;
				}
			}
			// latestToDate
			if(condition.getLatestToDate() > 0){
				if(info.getLatestDate() == null || !(info.getLatestDate() <= condition.getLatestToDate())){
					m_log.debug("getPerformanceList() continue : latestToDate target = " + info.getLatestDate() + ", filter = " + condition.getLatestToDate());
					continue;
				}
			}

			m_log.debug("getPerformanceList() add display list : target = " + info.getMonitorId());
			filterList.add(info);
		}

		return filterList;
	}


	/**
	 * 性能実績収集に必要な基本情報を取得します。
	 * 
	 * @param monitorId 監視項目ID
	 * @return グラフ描画に必要な基本情報
	 * @throws HinemosUnknown
	 *
	 * @return
	 */
	public PerformanceDataSettings getPerformanceGraphInfo(String monitorId) throws HinemosUnknown {
		m_log.debug("getPerformanceGraphInfo() : monitorId = " + monitorId);

		JpaTransactionManager jtm = null;
		PerformanceDataSettings perfDataSettings = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();

			// 対象の監視設定の取得
			MonitorInfoEntity monitorInfoEntity
			= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(monitorId);
			// ファシリティツリーの取得(facilityId is null の場合を考えて、監視設定より取得)
			// ログインユーザにより可視範囲を決めるため、オーナーロールを引数とする箇所は null を与える
			FacilityTreeItem facilityTree = FacilitySelector.getFacilityTree(
					monitorInfoEntity.getFacilityId(), Locale.getDefault(), false, Boolean.TRUE, null);

			// ログインユーザにより可視範囲を決めるため、オーナーロールを引数とする箇所は null を与える
			ArrayList<String> targetfacilityIdList
			= FacilitySelector.getNodeFacilityIdList(monitorInfoEntity.getFacilityId(),null,
					RepositoryControllerBean.ALL, false, true);

			// 戻り値の初期化
			perfDataSettings = new PerformanceDataSettings();

			// 監視ID
			perfDataSettings.setMonitorId(monitorId);
			// 監視IDに指定されたファシリティID
			perfDataSettings.setFacilityId(monitorInfoEntity.getFacilityId());
			// ファシリティツリー
			perfDataSettings.setFacilityTreeItem(facilityTree);
			// ファシリティIDリスト
			perfDataSettings.setTargetFacilityIdList(targetfacilityIdList);
			// 監視間隔=自動描画間隔
			perfDataSettings.setInterval(monitorInfoEntity.getRunInterval());
			// 表示する単位
			perfDataSettings.setMeasure(monitorInfoEntity.getMeasure());
			// 収集有効/無効フラグ
			perfDataSettings.setStatus(monitorInfoEntity.getCollectorFlg());
			// 監視種別
			perfDataSettings.setMonitorTypeId(monitorInfoEntity.getMonitorTypeId());

			ArrayList<CollectorItemParentInfo> itemCodeList = null;
			List<Object[]> itemCodeResultList = null;
			// 収集項目ID一覧、収集項目IDに対して存在するデバイス表示名を設定
			if ((HinemosModuleConstant.MONITOR_PERFORMANCE).equals(monitorInfoEntity.getMonitorTypeId())){
				itemCodeResultList = em
						.createNamedQuery("CalculatedDataEntity.getCollectorItemParentListByCollectorItemCode")
						.setParameter("collectorid", monitorId)
						.getResultList();
			} else if ((HinemosModuleConstant.MONITOR_HTTP_SCENARIO).equals(monitorInfoEntity.getMonitorTypeId())) {
				itemCodeResultList = em
						.createNamedQuery("CalculatedDataEntity.getCollectorItemParentListByHttpScenario")
						.setParameter("collectorid", monitorId)
						.getResultList();
			} else {
				itemCodeResultList = em
						.createNamedQuery("CalculatedDataEntity.getCollectorItemParentListByMonitorInfo")
						.setParameter("collectorid", monitorId)
						.getResultList();
			}
			if (itemCodeResultList != null) {
				itemCodeList = new ArrayList<CollectorItemParentInfo>();
				for (Object[] itemCodeResult : itemCodeResultList) {
					CollectorItemParentInfo itemCodeData = new CollectorItemParentInfo();
					itemCodeData.setParentItemCode((String)itemCodeResult[0]);
					itemCodeData.setItemCode((String)itemCodeResult[1]);
					itemCodeData.setDisplayName((String)itemCodeResult[2]);
					itemCodeData.setCollectorId((String)itemCodeResult[3]);
					itemCodeList.add(itemCodeData);
				}
			}
			perfDataSettings.setItemCodeList(itemCodeList);

			// 収集項目IDに該当する収集項目名を設定（リソース監視以外は固定）
			if ((HinemosModuleConstant.MONITOR_PERFORMANCE).equals(
					monitorInfoEntity.getMonitorTypeId())){
				// リソース監視の場合
				for (CollectorItemParentInfo itemInfo : perfDataSettings.getItemCodeList()){
					CollectorItemCodeMstEntity mst
					= com.clustercontrol.performance.monitor.util.QueryUtil.getCollectorItemCodeMstPK(itemInfo.getItemCode());

					// 表示名
					perfDataSettings.setItemName(itemInfo.getItemCode(), mst.getItemName());

					m_log.debug("getPerformanceGraphInfo() : MON_PRF itemCode = " + itemInfo.getItemCode() + ", itemName = " + mst.getItemName());
				}

				MonitorInfo monitorInfo = new MonitorSettingControllerBean()
				.getMonitor(monitorId, HinemosModuleConstant.MONITOR_PERFORMANCE);

				// 監視対象のItemCode
				perfDataSettings.setTargetItemCode(monitorInfo.getPerfCheckInfo().getItemCode());
				// 監視対象のデバイス
				perfDataSettings.setTargetDisplayName(monitorInfo.getPerfCheckInfo().getDeviceDisplayName());

			}
			else if ((HinemosModuleConstant.MONITOR_HTTP_SCENARIO).equals(monitorInfoEntity.getMonitorTypeId())) {
				if (!perfDataSettings.getItemCodeList().isEmpty()) {
					// リソース監視の場合
					for (CollectorItemParentInfo itemInfo : perfDataSettings.getItemCodeList()){
						perfDataSettings.setItemName(itemInfo.getItemCode(), itemInfo.getDisplayName());
					}

					// 監視対象のItemCode
					perfDataSettings.setTargetItemCode("0");
					// 監視対象のデバイス
					perfDataSettings.setTargetDisplayName(perfDataSettings.getItemNameMap().get("0"));

					m_log.debug("getPerformanceGraphInfo() : MON_XXX itemCode = " + "0" + ", itemName = " + perfDataSettings.getItemNameMap().get("0"));
				}
				else{
					m_log.info("getPerformanceGraphInfo() itemCode size = 0");
				}
			}
			else {
				// リソース監視以外は監視設定で取得された値を指定(リソース監視は1つのItemCodeを持つ)
				if(perfDataSettings.getItemCodeList().size() > 0){
					CollectorItemParentInfo itemInfo = perfDataSettings.getItemCodeList().get(0);

					// 表示名
					perfDataSettings.setItemName(itemInfo.getItemCode(), monitorInfoEntity.getItemName());
					// 監視対象のItemCode
					perfDataSettings.setTargetItemCode(itemInfo.getItemCode());
					// 監視対象のデバイス
					perfDataSettings.setTargetDisplayName(itemInfo.getDisplayName());

					m_log.debug("getPerformanceGraphInfo() : MON_XXX itemCode = " + itemInfo.getItemCode() + ", itemName = " + monitorInfoEntity.getItemName());
				}
				else{
					m_log.info("getPerformanceGraphInfo() itemCode size = 0");
				}
			}

			// 最新・最古の収集時刻を設定
			List<CalculatedDataEntity> entityList = QueryUtil.getLatestDateByCollectorid(monitorId, 1);
			if(entityList == null || entityList.size() == 0){
				perfDataSettings.setLatestDate(null);
			}
			else{
				CalculatedDataEntity entity = entityList.get(0);
				perfDataSettings.setLatestDate(entity.getId().getDateTime().getTime());
			}

			entityList = QueryUtil.getOldestDateByCollectorid(monitorId, 1);
			if(entityList == null || entityList.size() == 0){
				perfDataSettings.setOldestDate(null);
			}
			else{
				CalculatedDataEntity entity = entityList.get(0);
				perfDataSettings.setOldestDate(entity.getId().getDateTime().getTime());
			}
			jtm.commit();

			m_log.debug("getPerformanceGraphInfo() : LatestDate = " + perfDataSettings.getLatestDate());
			m_log.debug("getPerformanceGraphInfo() : OldestDate = " + perfDataSettings.getOldestDate());
		} catch (CollectorNotFound e) {
			jtm.rollback();
			// 指定の収集IDが存在しない場合はnullを返す
		} catch (MonitorNotFound e) {
			jtm.rollback();
			// 指定の収集IDが存在しない場合はnullを返す
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e){
			m_log.warn("getPerformanceGraphInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return perfDataSettings;
	}

	/**
	 * 実績収集で収集されたデータを取得します。
	 * @param facilityIdList　ファシリティIDのリスト
	 * @param itemInfoList　収集項目のリスト
	 * @param startDate　取得したい始点の時刻
	 * @param endDate　　取得した終点の時刻
	 * @return　収集データのデータセット
	 * @throws HinemosUnknown
	 *
	 */
	public CollectedDataSet getRecordCollectedData(
			ArrayList<String> facilityIdList,
			ArrayList<CollectorItemInfo> itemInfoList,
			Date startDate,
			Date endDate) throws HinemosUnknown{

		m_log.debug("getRecordCollectedData() facilityIdList size = " + facilityIdList.size() + ", itemInfoList size = " + itemInfoList.size());

		JpaTransactionManager jtm = null;
		CollectedDataSet ret = new CollectedDataSet();

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for(String facilityId : facilityIdList){
				m_log.debug("getRecordCollectedData() facilityId = " + facilityId);

				for(CollectorItemInfo itemInfo : itemInfoList){
					m_log.debug("getRecordCollectedData() facilityId = " + facilityId + ", itemInfo = " + itemInfo.getItemCode());

					// 収集項目をDBから取得します
					List<CalculatedDataEntity> dataList = QueryUtil.getCalculatedDataByFilter(
							itemInfo.getCollectorId(),
							itemInfo.getItemCode(),
							itemInfo.getDisplayName(),
							facilityId,
							startDate,
							endDate);
					CollectedDataList collectedDataList = new CollectedDataList();
					for (CalculatedDataEntity entity : dataList) {
						Date d = null;
						if (entity.getId().getDateTime() != null) {
							d = new Date(entity.getId().getDateTime().getTime());
						}
						CollectedDataInfo data;
						data = new CollectedDataInfo(d==null?null:d.getTime(), (float)entity.getValue());

						collectedDataList.add(data);
					}

					m_log.debug("getRecordCollectedData() size = " + dataList.size());

					ret.setCollectedDataList(facilityId, itemInfo, collectedDataList);
				}
			}
			jtm.commit();
		} catch (Exception e){
			m_log.warn("getRecordCollectedData() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		return ret;
	}

	/**
	 * 収集項目コードの一覧を取得します
	 * 
	 * @return 収集項目IDをキーとしCollectorItemTreeItemが格納されているHashMap
	 */
	public Map<String, CollectorItemTreeItem> getItemCodeMap() throws HinemosUnknown {
		m_log.debug("getItemCodeMap()");

		JpaTransactionManager jtm = null;
		Map<String, CollectorItemTreeItem> map = null;
		
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			map = CollectorItemCodeTable.getItemCodeMap();
			jtm.commit();
		} catch (Exception e){
			m_log.warn("getItemCodeMap() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return map;
	}

	/**
	 * 指定のファシリティで収集可能な項目のリストを返します
	 * デバイス別の収集項目があり、ノード毎に登録されているデバイス情報が異なるため、
	 * 取得可能な収集項目はファシリティ毎に異なる。
	 * 
	 * @param facilityId ファシリティID
	 * @return 指定のファシリティで収集可能な項目のリスト
	 * @throws HinemosUnknown
	 */
	public List<CollectorItemInfo> getAvailableCollectorItemList(String facilityId) throws HinemosUnknown {
		m_log.debug("getAvailableCollectorItemList() facilityId = " + facilityId);

		JpaTransactionManager jtm = null;
		List<CollectorItemInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = CollectorItemCodeTable.getAvailableCollectorItemList(facilityId);
			jtm.commit();
		} catch (HinemosUnknown e){
			jtm.rollback();
			throw e;
		} catch (Exception e){
			m_log.warn("getAvailableCollectorItemList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return list;
	}

	/**
	 * 指定した監視項目ID(収集ID)とファシリティID(ノード、スコープ)の性能実績データファイル(csv形式)に出力するファイルパスのリストを返却する。
	 * このメソッドは作成するファイル名を返却し、CSV出力処理は別スレッドで動作する。
	 * 
	 * @param monitorId 監視項目ID
	 * @param facilityId ファシリティID(ノードorスコープ)
	 * @param header ヘッダをファイルに出力するか否か
	 * @param archive ファイルをアーカイブするか否か
	 * 
	 * @return Hinemos マネージャサーバ上に出力されたファイル名
	 * @throws HinemosUnknown
	 */
	public List<String> createPerfFile(String monitorId, String facilityId, boolean header, boolean archive) throws HinemosUnknown{
		m_log.debug("createPerfFile() monitorId = "
				+ monitorId + ", facilityId = " + facilityId + ", header = " + header + ", archive = " + archive);

		JpaTransactionManager jtm = null;
		List<String> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

			list = ExportCollectedDataFile.create(monitorId, facilityId, header, archive, userId);
			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("createPerfFile() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return list;
	}

	/**
	 * 指定したリストのファイルパスを削除する
	 * 
	 * @param filepathList
	 * @throws HinemosUnknown
	 */
	public void deletePerfFile(ArrayList<String> fileNameList) throws HinemosUnknown{
		m_log.debug("deletePerformanceFile()");
		ExportCollectedDataFile.deleteFile(fileNameList);
	}


}