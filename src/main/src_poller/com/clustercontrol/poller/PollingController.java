/*

Copyright (C) 2008 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

 */

package com.clustercontrol.poller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.EmptyJpaTransactionCallback;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.ObjectSharingService;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
import com.clustercontrol.plugin.impl.SharedTablePlugin;
import com.clustercontrol.poller.bean.PollerProtocolConstant;
import com.clustercontrol.poller.impl.Snmp4jPollerImpl;
import com.clustercontrol.poller.impl.WbemPollerImpl;
import com.clustercontrol.poller.session.PollingJobBean;
import com.clustercontrol.repository.bean.NodeInfo;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.sharedtable.DataTable;
import com.clustercontrol.sharedtable.DataTableNotFoundException;
import com.clustercontrol.sharedtable.SharedTable;

public class PollingController implements Serializable {
	private static Log m_log = LogFactory.getLog( PollingController.class );

	public final static String POLLER_MANAGER_JNDI_NAME = "PollerManager";

	private final static int STOPPED = 2;

	private final String m_quartzJndiName = "RAMScheduler";
	private final String m_quartzJobName;  // ポーラ名とする
	private final String m_quartzGroupName;  // ポーラグループとする

	private final String m_pollerGroup;
	private final String m_pollerName;

	private final String m_targetFacilityId;

	private final PollingControllerConfig m_pollingConfig;

	private final String m_tableGroup;
	private final String m_tableName;

	// ポーラの状態を示す
	volatile private int m_status;

	protected PollingController(
			String pollerGroup,
			String pollerName,
			String targetFacilityId,
			boolean indexCheckFlg,
			String tableGroup,
			String tableName)
					throws NotInitializedException, DataTableNotFoundException{

		m_pollerGroup = pollerGroup;
		m_pollerName = pollerName;
		m_targetFacilityId = targetFacilityId;
		m_pollingConfig = new PollingControllerConfig(indexCheckFlg);
		m_tableGroup = tableGroup;
		m_tableName = tableName;

		m_quartzJobName = m_pollerName;
		m_quartzGroupName = m_pollerGroup + "_POLLER";

		m_status = PollingController.STOPPED;

		// 書き込み対象テーブルが存在するか否かを確認する
		SharedTable sharedTable = SharedTablePlugin.getSharedTable();

		if(!sharedTable.containsDataTable(tableGroup, tableName)){
			// テーブルが存在しないため例外を投げる
			DataTableNotFoundException e = new DataTableNotFoundException(tableGroup, tableName);
			m_log.info("PollingController() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	private void scheduleJob(){
		m_log.debug("scheduleJob()");

		JpaTransactionManager jtm = new JpaTransactionManager();
		jtm.addCallback(new EmptyJpaTransactionCallback() {
			@Override
			public void postCommit() {
				// 収集間隔を取得する
				int interval = m_pollingConfig.getMinPollingInterval();
				
				// デバッグ出力
				m_log.debug("scheduleJob : " + interval);
				
				// 収集間隔が-1の場合は新規に登録しない
				if(interval == -1){
					try {
						// 既に登録されているジョブを削除する(登録されていない場合は何もおこらない)
						SchedulerPlugin.deleteJob(SchedulerType.RAM, m_quartzJobName, m_quartzGroupName);
						
						// ステータス変更
						m_status = PollingController.STOPPED;
					} catch (HinemosUnknown e) {
						m_log.warn(e.getMessage(), e);
					}
					return;
				}

				// CronTrigger でジョブをスケジューリング登録
				try{
					// EJBアクセス引数を定義する
					Serializable[] jdArgs = new Serializable[3];
					Class<? extends Serializable>[] jdArgsType = new Class[3];
					jdArgs[0] = POLLER_MANAGER_JNDI_NAME;
					jdArgsType[0] = String.class;
					jdArgs[1] = m_pollerGroup;
					jdArgsType[1] = String.class;
					jdArgs[2] = m_pollerName;
					jdArgsType[2] = String.class;
					
					String cronString = PollingInterval.parseCronExpression(interval);
					if(cronString == null){
						m_log.info("Illegal argument : interval = " + interval);
					} else {
						SchedulerPlugin.scheduleCronJob(SchedulerType.RAM, m_quartzJobName, m_quartzGroupName,
								new Date(), cronString, true, PollingJobBean.class.getName(), PollingJobBean.METHOD_NAME, jdArgsType, jdArgs);
					}
				} catch (HinemosUnknown e) {
					m_log.warn("scheduleJob() : pollerGroup = " + m_pollerGroup + ", pollerName = " + m_pollerName + ", " + e.getMessage(), e);
				}
			}
		});


	}

	/**
	 * ポーリングを開始する
	 * 既に指定の収集名が登録されている場合は設定を上書きする（ver3.2以降）
	 *
	 * @param collectorName 収集名
	 * @param interval 収集間隔
	 * @param maps 収集プロトコルとポーリング対象のマップ
	 */
	public void startPolling(
			String collectorName,
			int interval,
			HashMap<String, List<String>> maps) {
		m_log.debug("startPolling() collectorName = " + collectorName + ", interval = " + interval);

		try {
			PollerManager._lock.writeLock();
			
			// 指定の収集名が既に登録されている場合も、上書きする（ver3.2より）
			if(m_pollingConfig.containsCollectorName(collectorName)){
				m_log.debug(collectorName + "is already exist.");
			}

			try {
				// 書き込み対象テーブルが存在するか否かを確認する
				SharedTable stable = SharedTablePlugin.getSharedTable();

				if(!stable.containsCollectorName(m_tableGroup, m_tableName, collectorName)){
					// ない場合は新規に作成する
					stable.registerCollector(m_tableGroup, m_tableName, collectorName, interval);
				}
			} catch (DataTableNotFoundException e) {
				return;
			}

			// 指定の収集名で、収集間隔、収集プロトコルとポーリング対象のマップを登録する
			m_pollingConfig.putPollingTargets(collectorName, interval, maps);

			// ポーリングのスケジュールを設定
			scheduleJob();
			
			PollerManager.storeCache(m_pollerGroup, m_pollerName, this);
		} finally {
			PollerManager._lock.writeUnlock();
		}
	}

	/**
	 * ポーリングを終了する
	 * @param collectorName 収集名
	 * @throws FacilityNotFoundBK
	 */
	public void stopPolling(String collectorName){
		try {
			PollerManager._lock.writeLock();
			
			m_log.debug("stop polling : " + m_pollerGroup + ", " + m_pollerName + ", " + collectorName);

			// 指定の収集名で、収集間隔、ポーリング対象のリストを削除する
			boolean modifyFlg = m_pollingConfig.removePollingTargets(collectorName);

			// 最小収集間隔が変更されている場合はスケジューリング
			if(modifyFlg){
				scheduleJob();
			}

			try {
				// 書き込み対象テーブルの登録情報を削除する
				SharedTable stable = SharedTable.getInstance();

				stable.unregisterCollector(m_tableGroup, m_tableName, collectorName);
			} catch (DataTableNotFoundException e) {
				m_log.warn(e.getMessage(), e);
				return;
			}
			
			PollerManager.storeCache(m_pollerGroup, m_pollerName, this);
		} finally {
			PollerManager._lock.writeUnlock();
		}
	}

	/**
	 * 全ての収集名のポーリングを終了する
	 * @throws FacilityNotFoundBK
	 */
	public void stopPollingAll(){
		try {
			PollerManager._lock.writeLock();
			
			m_log.debug("stop polling : " + m_pollerGroup + ", " + m_pollerName);

			// 全ての収集間隔、ポーリング対象のリストを削除する
			boolean modifyFlg = m_pollingConfig.removePollingAllTargets();

			// 最小収集間隔が変更されている場合はスケジューリング
			if(modifyFlg){
				scheduleJob();
			}

			// 書き込み対象テーブルを削除する
			SharedTable sharedtable = SharedTablePlugin.getSharedTable();
			if(sharedtable != null){
				sharedtable.removeDataTable(m_tableGroup, m_tableName);
			}
			
			PollerManager.storeCache(m_pollerGroup, m_pollerName, this);
		} finally {
			PollerManager._lock.writeUnlock();
		}
	}

	/**
	 * ポーリングを実行する
	 */
	public void run() {
		// デバッグログ出力
		m_log.debug("run start : " + m_pollerGroup + "  " + m_pollerName);

		SharedTable sharedTable = null;
		// 共有テーブルを取得する
		sharedTable = SharedTablePlugin.getSharedTable();

		List<Integer> intervals = null;
		HashMap<String, List<String>> pollingTargetMap = null;
		
		try {

			Set<String> stCollectorNames =
					new HashSet<String>(sharedTable.getCollectorNames(m_tableGroup, m_tableName));
			for(String collectorName : stCollectorNames){
				if(m_pollingConfig.containsCollectorName(collectorName)){
					m_log.debug("run : " + m_pollerGroup + "  " + m_pollerName + " : " + collectorName + " is exist");
				}else{
					m_log.info("run : " + m_pollerGroup + "  " + m_pollerName + " : " + collectorName + " is not exist");
				}
			}

			Set<String> pcCollectoName =
					new HashSet<String>(m_pollingConfig.getCollectorNames());
			pcCollectoName.removeAll(stCollectorNames);
			
			try {
				PollerManager._lock.writeLock();
				
				boolean modifyFlg = false;
				for(String collectorName : pcCollectoName){
					// 指定の収集名で、収集間隔、ポーリング対象のリストを削除する
					modifyFlg = modifyFlg | m_pollingConfig.removePollingTargets(collectorName);
				}

				// 最小収集間隔が変更されている場合はスケジューリング
				if(modifyFlg){
					scheduleJob();
				}
			} finally {
				PollerManager._lock.writeUnlock();
			}

		} catch (DataTableNotFoundException e) {
			m_log.warn(e.getMessage(), e);
			// 書き込み対象テーブルを取得できないため終了する
			return;
		}

		// 共有テーブルが保持しているデータホルダのうち有効である収集間隔のセットを取得
		Set<Integer> holderIntervals;
		try {
			holderIntervals = sharedTable.getIntervals(m_tableGroup, m_tableName);
		} catch (DataTableNotFoundException e) {
			m_log.warn(e.getMessage(), e);
			// 書き込み対象テーブルを取得できないため終了する
			return;
		}

		// 更新すべきテーブルがない場合は処理終了
		if(holderIntervals.size() <= 0){
			return;
		}

		// 現在時刻を取得
		long now = System.currentTimeMillis();

		// 今のタイミングで更新すべきテーブルを特定するため、
		// 更新の必要のある収集間隔のリストを取得する
		intervals = m_pollingConfig.getCurrentRefreshIntervals(now);

		ArrayList<Integer> tmpList = new ArrayList<Integer>(intervals);

		// 全エンティティ内を調べるループをまわすためテンポラリのリストを作成
		Iterator<Integer> itr = tmpList.iterator();
		while(itr.hasNext()){
			int interval = itr.next();
			// 存在しない場合は収集対象から削除する
			if(!holderIntervals.contains(interval)){
				m_log.debug("Remove : " + interval);
				intervals.remove(new Integer(interval));
			}
		}

		// 更新すべきテーブルがない場合は処理終了
		if(intervals.size() <= 0){
			return;
		}

		// ポーリング対象の中から今のタイミングで収集すべきものを抽出する
		pollingTargetMap = m_pollingConfig.getCurrentTargetMap(now);
		
		PollerManager.storeCache(m_pollerGroup, m_pollerName, this);

		m_log.debug("run() pollingTargetMap : " + pollingTargetMap);
		// 収集すべきポーリング対象がない場合は処理終了
		if(pollingTargetMap.size() <= 0){
			m_log.debug("polling targets are nothing.");
			return;
		}

		// ポーリングにより取得した値を格納するテーブル
		DataTable dataTable = null;

		NodeInfo nodeInfo = null;
		try {
			nodeInfo = NodeProperty.getProperty(m_targetFacilityId);
		} catch (FacilityNotFound e1) {
			// ポーリングは実行しない
			// 取得値が空のテーブルを作成する
			dataTable = new DataTable();
		}
		if(nodeInfo == null) {
			m_log.info("facility not found 2. FacilityId = " + m_targetFacilityId);
			// ポーリングは実行しない
			// 取得値が空のテーブルを作成する
			dataTable = new DataTable();
		} else if (nodeInfo.getFacilityId() == null || "".equals(nodeInfo.getFacilityId())){
			m_log.info("invalid FacilityId. FacilityId = " + nodeInfo.getFacilityId());
			// ポーリングは実行しない
			// 取得値が空のテーブルを作成する
			dataTable = new DataTable();
		} else if (!nodeInfo.isValid()){
			m_log.debug("FacilityId = " + nodeInfo.getFacilityId() + " is disable.");
			// ポーリングは実行しない
			// 取得値が空のテーブルを作成する
			dataTable = new DataTable();
			// 管理対象フラグがOFFの場合、ポーリング処理直後に管理対象フラグがONになった場合に
			// 5.0.0までの実装では集計処理側で単純に「集計タイミングでの」管理対象フラグを見て、
			// 結果として上でセットした空のデータを読んでしまうことで、不明となることへの対策。
			// 
			// 「管理対象フラグがOFFの状態で最後にポーリングの処理を通過した時間」をノード単位で記録しておくことで、
			// 集計処理時にもしも「不明」となった場合には、ここでセットした時間と監視間隔をもとに、
			// 通知をスキップするか否かを決定する。
			setLastPollingSkipTime(nodeInfo.getFacilityId());
		}

		// ポーリングを行い値を収集
		if(dataTable == null){
			dataTable = polling(nodeInfo, pollingTargetMap, -1, -1);
		}
		m_log.debug("dataTable" + dataTable);

		// 収集値を共有テーブルに格納する
		for(int interval : intervals){
			// 新規収集した情報をテーブルの先頭に挿入する
			m_log.debug("insert " + m_tableGroup + ", " + m_tableName + ", " + interval);
			try {
				// テーブルホルダに収集値を挿入する
				// IPアドレスを識別キーとすることで、ファシリティで定義されているIPアドレスが変更になった場合に
				// 変更前と変更後の収集値が同じテーブルホルダに格納されることを防ぐ
				sharedTable.insertDataTable(
						m_tableGroup,
						m_tableName,
						interval,
						dataTable,
						nodeInfo.getAvailableIpAddress());
			} catch (DataTableNotFoundException e) {
				m_log.warn(e.getMessage(), e);
			}
		}

		// デバッグログ出力
		m_log.debug("run end   : " + m_pollerGroup + "  " + m_pollerName);
	}

	private DataTable polling(
			NodeInfo nodeInfo,
			HashMap<String, List<String>> pollingTargetMap,
			int retries,
			int timeout){
		DataTable dataTable = new DataTable();
		DataTable tmpDataTable = null;

		// *****************************
		// SNMPのpollerの設定
		// *****************************
		if(pollingTargetMap.get(PollerProtocolConstant.PROTOCOL_SNMP) != null) {
			tmpDataTable = Snmp4jPollerImpl.getInstance().polling(
					nodeInfo.getAvailableIpAddress(),
					nodeInfo.getSnmpPort(),
					SnmpVersionConstant.stringToSnmpType(nodeInfo.getSnmpVersion()),
					nodeInfo.getSnmpCommunity(),
					retries == -1 ? nodeInfo.getSnmpRetryCount() : retries,
							timeout == -1 ? nodeInfo.getSnmpTimeout() : timeout,
									pollingTargetMap.get(PollerProtocolConstant.PROTOCOL_SNMP),
					nodeInfo.getSnmpSecurityLevel(),
					nodeInfo.getSnmpUser(),
					nodeInfo.getSnmpAuthPassword(),
					nodeInfo.getSnmpPrivPassword(),
					nodeInfo.getSnmpAuthProtocol(),
					nodeInfo.getSnmpPrivProtocol());

			dataTable.putAll(tmpDataTable);
		}

		// *****************************
		// WBEMのpollerの設定
		// *****************************
		if(pollingTargetMap.get(PollerProtocolConstant.PROTOCOL_WBEM) != null) {
			WbemPollerImpl poller = new WbemPollerImpl();
			tmpDataTable = poller.polling(
					nodeInfo.getAvailableIpAddress(),
					nodeInfo.getWbemPort(),
					nodeInfo.getWbemProtocol(),
					nodeInfo.getWbemUser(),
					nodeInfo.getWbemUserPassword(),
					nodeInfo.getWbemNameSpace(),
					retries == -1 ? nodeInfo.getWbemRetryCount() : retries,
							timeout == -1 ? nodeInfo.getWbemTimeout() : timeout,
									pollingTargetMap.get(PollerProtocolConstant.PROTOCOL_WBEM));
			dataTable.putAll(tmpDataTable);
		}

		// *****************************
		// オプションのpollerの設定
		//	pollingTargetMapのKEYにより識別する
		//	SNMP	SNMPポーラ(非オプション機能)
		//	WBEM	WBEMポーラ(非オプション機能)
		//	VM.		VM管理オプション(VmPollerImplInterface)	(ex. VM.XEN)
		//	CLOUD.	クラウド管理オプション(ICloudPoller)		(ex. CLOUD.AWS)
		// *****************************
		for (Entry<String, List<String>> entry : pollingTargetMap.entrySet()) {
			String pollerProtocol = entry.getKey();
			List<String> pollingTarget = entry.getValue();
			////
			// 非オプション機能の場合は除く
			////
			if (pollerProtocol.equals(PollerProtocolConstant.PROTOCOL_SNMP) ||
					pollerProtocol.equals(PollerProtocolConstant.PROTOCOL_WBEM)) {
				continue;
			}
			// プロトコルとターゲットの文字列の出力
			if (m_log.isDebugEnabled()) {
				m_log.debug("pollerProtocol : " + pollerProtocol);
				m_log.debug("pollingTarget : " + pollingTarget.toString());
			}
			////
			// 各種オプションでの監視用ポーラ取得箇所
			// IPollerインターフェースを継承したポーラが、ポーラーのプロトコル名をキーにして事前にObjectSharingServiceに
			// 登録されている場合、そのインスタンスを取り出して監視を実行する。
			// 各種オプションのJARファイル内で、Plugin機構により登録されている前提。
			// 2013年8月末時点で、VM・クラウドの2オプションで本機構を使用している
			////
			if(pollerProtocol != null){

				IPoller poller = null;
				try {
					poller = ObjectSharingService.objectRegistry().get(IPoller.class, pollerProtocol);
				} catch (Exception e) {
					m_log.warn("polling() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				}
				// pollerが取得できない場合、未登録のポーリングメソッドなのでログだけ出してスルー
				if (poller == null) {
					m_log.warn("polling : unknown pollerProtocol. facilityId = " + nodeInfo.getFacilityId() + ", protocol = " + pollerProtocol);
					continue;
				}
				// ポーラーの実体のメソッドを呼び出して、実際にポーリングを行う
				// もし独自のオプション製品で、nodeinfoとpollingTarget以外のパラメタが必要な場合、
				// 第3引数のObjectに突っ込むことで対応する。
				// その場合は独自のオプション用にpollerProtocolの内容で分岐する必要がある。
				// （できれば本体側に手を入れずにすむよう、第三引数は使わず、ポーラ内で情報を取ることが望ましいが・・・）
				tmpDataTable = poller.polling(
						nodeInfo,
						pollingTarget,
						null);
				if (tmpDataTable != null) {
					dataTable.putAll(tmpDataTable);
				}
			}
		}

		return dataTable;
	}

	/**
	 * 指定のOIDに対してポーリングを実行します。
	 *
	 * @param targetValues ポーリング対象
	 * @return 収集したMIB値が格納されたデータテーブル
	 */
	public DataTable polling(HashMap<String, List<String>> targetMaps, int retries, int timeout){
		// 各ポーラーで共通で使用するDataTableを定義する
		DataTable dataTable = new DataTable();

		NodeInfo nodeInfo;
		try {
			nodeInfo = NodeProperty.getProperty(m_targetFacilityId);
		} catch (FacilityNotFound e) {
			// ポーリングは実行しない
			return dataTable;
		}

		if(nodeInfo == null) {
			m_log.info("facility not found 5. FacilityId = " + null);
			// ポーリングは実行しない
			return dataTable;
		} else if (nodeInfo.getFacilityId() == null || "".equals(nodeInfo.getFacilityId())){
			m_log.info("invalid FacilityId. FacilityId = " + nodeInfo.getFacilityId());
			// ポーリングは実行しない
			return dataTable;
		}

		dataTable.putAll(polling(nodeInfo, targetMaps, retries, timeout));

		return dataTable;
	}

	/**
	 * ポーラグループ名を返します。
	 * @return ポーラグループ名
	 */
	public String getPollerGroup() {
		return m_pollerGroup;
	}

	/**
	 * ポーラ名を返します。
	 * @return ポーラ名
	 */
	public String getPollerName() {
		return m_pollerName;
	}

	/**
	 * ポーリング設定を返します。
	 * @return ポーリング設定
	 */
	public PollingControllerConfig getPollingConfig() {
		return m_pollingConfig;
	}

	public String getQuartzJndiName() {
		return m_quartzJndiName;
	}

	/**
	 * ポーラの実行状態を返します。
	 * @return 実行状態
	 */
	public int getStatus() {
		return m_status;
	}
	
	
	/**
	 * 指定されたノードの管理対象フラグの直近の状態を見て、リソース監視による通知をスキップすべきか否かを判定します。
	 * @param facilityId 対象となるノードのfacilityId
	 * @param interval 対象となる監視項目の監視間隔
	 * @return リソース監視を行うために十分な時間、管理対象フラグが有効になっていなかった場合、trueが返る
	 */
	public static boolean skipResourceMonitorNotifyByNodeFlagHistory(String facilityId, int interval) {
		long currentTime = System.currentTimeMillis();
		long lastPollingSkipTime = getLastPollingSkipTime(facilityId);
		if (currentTime - lastPollingSkipTime < interval * 1000 * 2 /* 2回分 */) {
			// 管理対象フラグをOFFとしていた期間が、監視間隔 * 2 よりも直近で存在している、あるいは
			// 直近でマネージャを再起動したなどの状態にあたるため、通知をスキップ
			m_log.info("skipResourceMonitorNotifyByNodeFlagHistory() : skip resource monitor notify because of node flag change timing. "
					+ "facilityId = " + facilityId + ", currentTime = " + currentTime + ", lastPollingSkipTime = " + lastPollingSkipTime);
			return true;
		}
		// 管理対象フラグがOFFだった期間がかなり昔 or そのノードについて何の情報も登録されていない状態
		// なので、通知はスキップしない
		return false;
	}
	
	/**
	 * 指定されたノードの管理対象フラグの直近の状態を見て、プロセス監視による通知をスキップすべきか否かを判定します。
	 * 監視結果が「不明」となった場合以外には本関数は呼び出さないでください。
	 * @param facilityId 対象となるノードのfacilityId
	 * @param interval 対象となる監視項目の監視間隔
	 * @return 通知をスキップすべき状況（プロセス監視を行うために十分な時間、管理対象フラグが有効になっていなかったなど）の場合、trueが返る。<br>
	 * 通知をスキップするべきでなければ、falseが返る。
	 */
	public static boolean skipProcessMonitorNotifyByNodeFlagHistory(String facilityId, int interval) {
		long currentTime = System.currentTimeMillis();
		long lastPollingSkipTime = getLastPollingSkipTime(facilityId);
		if (currentTime - lastPollingSkipTime < interval * 1000 ) {
			// 管理対象フラグをOFFとしていた期間が、監視間隔よりも直近で存在している、あるいは
			// 直近でマネージャを再起動したなどの状態にあたるため、通知をスキップ
			m_log.info("skipProcessMonitorNotifyByNodeFlagHistory() : skip process monitor notify because of node flag change timing. "
					+ "facilityId = " + facilityId + ", currentTime = " + currentTime + ", lastPollingSkipTime = " + lastPollingSkipTime);
			return true;
		}
		// 管理対象フラグがOFFだった期間がかなり昔 or そのノードについて何の情報も登録されていない状態
		// なので、通知はスキップしない
		return false;
	}
	
	/**
	 * 最も直近に管理対象フラグがたっていなかったことによりポーリングがスキップされた時間を返す。
	 * @param facilityId
	 * @return
	 */
	public static long getLastPollingSkipTime(String facilityId) {
		HashMap<String, Long> lastPollingDisableTimeMap = getLastPollingSkipTimeMap();
		Long lastPollingDisableTime = lastPollingDisableTimeMap.get(facilityId);
		
		if (lastPollingDisableTime != null) {
			return lastPollingDisableTime;
		} else { 
			// キャッシュそのものが存在しない、キャッシュが異常、またはキャッシュ中にそのファシリティIDの情報が格納されていない場合、
			// 従来通り、不明が出ても何の抑制もしないように、遠い過去(0L)を返す = 遥か昔の時点から管理対象がONであったことを示す 
			return 0L;
		}
	}
	
	/**
	 * 指定されたノードのポーリング処理を「管理対象フラグが立っていないこと」を理由にスキップした場合に呼び出す。<br>
	 * （呼び出しにより、このファシリティIDのポーリングが最後にスキップされた時刻として現在時刻がセットされる）
	 * @param facilityId 対象となるfacilityId
	 */
	public static void setLastPollingSkipTime(String facilityId) {
		// キャッシュされたmapそのものをいじって再度キャッシュ機構に格納するため、
		// 変更途中でのレースを防ぐ必要があり、synchronizedする
		synchronized (AbstractCacheManager.KEY_POLLING_SKIP_TIME) {
			HashMap<String, Long> lastPollingSkipTimeMap = getLastPollingSkipTimeMap();
			
			// mapに当該ファシリティIDをキーとして、現在時刻をセット
			lastPollingSkipTimeMap.put(facilityId, System.currentTimeMillis());
			
			// キャッシュを再度キャッシュマネージャに格納
			// （キャッシュマネージャから取り出したキャッシュは参照ではなくコピーなので、storeは省略不可）
			ICacheManager cm = CacheManagerFactory.instance().create();
			cm.store(AbstractCacheManager.KEY_POLLING_SKIP_TIME, lastPollingSkipTimeMap);
			
			if (m_log.isDebugEnabled()) {
				m_log.debug("setLastPollingSkipTime() : current map size = " + lastPollingSkipTimeMap.size());
				m_log.debug("setLastPollingSkipTime() : current map entry = " + lastPollingSkipTimeMap.entrySet());
			}
		}
	}
	
	/**
	 * 指定されたノードのポーリング処理の最終スキップ時刻の情報をリセットする。
	 * 本関数は、ノードが削除された際に呼び出す。
	 * @param facilityId 削除対象のfacilityId
	 */
	public static void removeLastPollingSkipTime(String facilityId) {
		// キャッシュされたmapそのものをいじって再度キャッシュ機構に格納するため、
		// 変更途中でのレースを防ぐ必要があり、synchronizedする
		synchronized (AbstractCacheManager.KEY_POLLING_SKIP_TIME) {
			HashMap<String, Long> lastPollingSkipTimeMap = getLastPollingSkipTimeMap();
			
			// mapに当該ファシリティの情報がある場合には削除する
			lastPollingSkipTimeMap.remove(facilityId);
			
			// キャッシュを再度キャッシュマネージャに格納
			// （キャッシュマネージャから取り出したキャッシュは参照ではなくコピーなので、storeは省略不可）
			ICacheManager cm = CacheManagerFactory.instance().create();
			cm.store(AbstractCacheManager.KEY_POLLING_SKIP_TIME, lastPollingSkipTimeMap);
			
			if (m_log.isDebugEnabled()) {
				m_log.debug("setLastPollingSkipTime() : current map size = " + lastPollingSkipTimeMap.size());
				m_log.debug("setLastPollingSkipTime() : current map entry = " + lastPollingSkipTimeMap.entrySet());
			}
		}
	}
	
	/**
	 * キャッシュ化されたポーリング最終スキップ時刻のmapを取得する。キャッシュ済みのmapがない場合は新規作成された空のマップが返る
	 */
	@SuppressWarnings("unchecked")
	private static HashMap<String, Long> getLastPollingSkipTimeMap() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_POLLING_SKIP_TIME);
		if (cache != null) {
			try {
				return (HashMap<String, Long>)cache;
			} catch (ClassCastException e) {
				m_log.warn("getLastPollingDisableTimeMap() : can't cast cache to hashmap. (cache class = " + cache.getClass() + ")", e);
			}
		}
		return new HashMap<String, Long>();
	}
}
