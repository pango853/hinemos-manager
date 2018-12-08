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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.bean.YesNoConstant;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.http.util.CallableTaskHttpScenario;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType;
import com.clustercontrol.monitor.run.util.ParallelExecution;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.performance.bean.CollectedDataErrorTypeConstant;
import com.clustercontrol.performance.bean.CollectorItemInfo;
import com.clustercontrol.performance.bean.Node;
import com.clustercontrol.performance.bean.Sample;
import com.clustercontrol.performance.monitor.bean.PerfCheckInfo;
import com.clustercontrol.performance.monitor.model.MonitorPerfInfoEntity;
import com.clustercontrol.performance.monitor.util.CallablePerfTask;
import com.clustercontrol.performance.monitor.util.QueryUtil;
import com.clustercontrol.performance.util.PerformanceDataUtil;
import com.clustercontrol.performance.util.PollingDataManager;
import com.clustercontrol.performance.util.code.CollectorItemCodeTable;
import com.clustercontrol.poller.NotInitializedException;
import com.clustercontrol.poller.PollingController;
import com.clustercontrol.repository.bean.NodeDeviceInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.Messages;

/**
 * リソース監視の閾値判定クラス
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class RunMonitorPerformance extends RunMonitorNumericValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorPerformance.class );

	private static final String MESSAGE_ID_INFO = "001";
	private static final String MESSAGE_ID_WARNING = "002";
	private static final String MESSAGE_ID_CRITICAL = "003";
	private static final String MESSAGE_ID_UNKNOWN = "004";

	/** 閾値情報 */
	private MonitorPerfInfoEntity m_perf = null;

	/** 収集項目名 */
	private String m_itemName = null;

	/** デバイス情報 */
	private NodeDeviceInfo m_deviceData = null;

	/**
	 * コンストラクタ
	 *
	 */
	public RunMonitorPerformance() {
		super();
	}

	/**
	 * マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッド
	 *
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#runMonitorInfo()
	 * @see com.clustercontrol.monitor.run.util.CallableTask
	 */
	@Override
	protected RunMonitor createMonitorInstance() {
		return new RunMonitorPerformance();
	}

	/**
	 * [リソース監視用]監視を実行します。（並列処理）
	 *
	 * リソース監視では1つのファシリティIDに対して、複数の収集項目ID及び、デバイスに対するリソースを監視・収集します。
	 * この動作に対応するため、独自のrunMonitorInfoを実装します。
	 *
	 */
	@Override
	protected boolean runMonitorInfo() throws FacilityNotFound, MonitorNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("runMonitorInfo()");

		m_now = new Date(System.currentTimeMillis());

		m_priorityMap = new HashMap<Integer, ArrayList<String>>();
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_INFO),		new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_WARNING),	new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_CRITICAL),	new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_UNKNOWN),	new ArrayList<String>());

		try
		{
			// 監視基本情報を設定
			boolean run = this.setMonitorInfo(m_monitorTypeId, m_monitorId);
			if(!run){
				// 処理終了
				return true;
			}

			// 判定情報を設定
			setJudgementInfo();

			// チェック条件情報を設定
			setCheckInfo();

			// ファシリティIDの配下全ての一覧を取得
			// 有効/無効フラグがtrueとなっているファシリティIDを取得する
			ArrayList<String> facilityList = new RepositoryControllerBean().getExecTargetFacilityIdList(m_facilityId, m_monitor.getOwnerRoleId());
			if (facilityList.size() == 0) {
				return true;
			}

			m_log.debug("monitor start : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

			String facilityId = null;

			/**
			 * 監視の実行
			 */
			// ファシリティIDの数だけ、各監視処理を実行する
			Iterator<String> itr = facilityList.iterator();

			ExecutorCompletionService<ArrayList<MonitorRunResultInfo>> ecs = new ExecutorCompletionService<ArrayList<MonitorRunResultInfo>>(ParallelExecution.instance().getExecutorService());
			int taskCount = 0;
			
			while(itr.hasNext()){
				facilityId = itr.next();
				if(facilityId != null && !"".equals(facilityId)){

					// マルチスレッド実行用に、RunMonitorのインスタンスを新規作成する
					// インスタンスを新規作成するのは、共通クラス部分に監視結果を保持するため
					RunMonitorPerformance runMonitor = new RunMonitorPerformance();

					// 監視実行に必要な情報を再度セットする
					runMonitor.m_monitorTypeId = this.m_monitorTypeId;
					runMonitor.m_monitorId = this.m_monitorId;
					runMonitor.m_now = this.m_now;
					runMonitor.setMonitorInfo(m_monitorTypeId, m_monitorId);
					runMonitor.m_priorityMap = this.m_priorityMap;
					runMonitor.setJudgementInfo();
					runMonitor.setCheckInfo();
					
					ecs.submit(new CallablePerfTask(runMonitor, facilityId));
					taskCount++;
				}
				else {
					facilityList.remove(facilityId);
				}
			}
			/**
			 * 監視結果の集計
			 */
			ArrayList<MonitorRunResultInfo> resultList = null;

			m_log.debug("total start : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

			// 収集値の入れ物を作成
			Sample sample = null;
			if(m_monitor.getCollectorFlg() == ValidConstant.TYPE_VALID){
				sample = new Sample(m_monitorId, new Date());
			}
			
			ArrayList<String> notifyFacilityIdList = new ArrayList<String>();
			ArrayList<MonitorRunResultInfo> notifyResultList = new ArrayList<MonitorRunResultInfo>();
			for (int i = 0; i < taskCount; i++) {
				Future<ArrayList<MonitorRunResultInfo>> future = ecs.take();
				resultList = future.get();	// 監視結果を取得
				
				for(MonitorRunResultInfo result : resultList){
					m_nodeDate = result.getNodeDate();
					
					facilityId = result.getFacilityId();
					
					// 監視結果を通知
					if(result.getMonitorFlg()){
						notifyFacilityIdList.add(facilityId);
						notifyResultList.add(result);
					}

					// 個々の収集値の登録
					if(sample != null && result.getCollectorFlg()){
						if(result.isCollectorResult()){
							sample.set(facilityId,result.getItemCode(), result.getDisplayName(), result.getValue(), CollectedDataErrorTypeConstant.NOT_ERROR);
						}else{
							sample.set(facilityId,result.getItemCode(), result.getDisplayName(), result.getValue(), CollectedDataErrorTypeConstant.UNKNOWN);
						}
					}

					// 監視が完了したファシリティIDをfacilityListCompleteに追加する

				}
			}

			notify(notifyFacilityIdList, notifyResultList);

			// 収集値をまとめて登録
			if(sample != null){
				PerformanceDataUtil.put(sample);
			}

			m_log.debug("monitor end : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

			return true;

		} catch (InterruptedException e) {
			m_log.info("runMonitorInfo() monitorTypeId = " + m_monitorTypeId + ", monitorId = " + m_monitorId + " : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			return false;
		} catch (ExecutionException e) {
			m_log.info("runMonitorInfo() monitorTypeId = " + m_monitorTypeId + ", monitorId = " + m_monitorId + " : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			m_log.error(e);
			return false;
		}
	}

	/**
	 * リソース監視はcollectList()でファシリティ毎の処理を動作するように変更
	 */
	@Override
	public boolean collect(String facilityId) {
		m_log.debug("collect() monitorTypeId = " + m_monitorTypeId + ",monitorId = " + m_monitorId  + ", facilityId = " + facilityId);
		return true;
	}

	/**
	 * 監視の実態。対象ノードのfacilityId毎に呼ばれる
	 *
	 */
	public ArrayList<MonitorRunResultInfo> collectList(String facilityId) throws FacilityNotFound {
		m_log.debug("collectList() monitorTypeId = " + m_monitorTypeId + ",monitorId = " + m_monitorId  + ", facilityId = " + facilityId);

		ArrayList<MonitorRunResultInfo> resultList = new ArrayList<MonitorRunResultInfo>();
		MonitorRunResultInfo result = null;

		////
		// ターゲットのItemCodeList/deviceType/deviceListを取得
		////
		boolean breakdownFlg = false;
		switch (m_perf.getBreakdownFlg().intValue()) {
		case YesNoConstant.TYPE_YES:
			breakdownFlg = true;
			break;
		case YesNoConstant.TYPE_NO:
			breakdownFlg = false;
			break;
		default:
			break;
		}
		PollingDataManager dataManager = new PollingDataManager(facilityId,m_perf.getItemCode(),breakdownFlg);
		ArrayList<String> itemCodeList = dataManager.getItemCodeList();
		List<? extends NodeDeviceInfo> deviceList = dataManager.getDeviceList();

		////
		// SharedTableの取得
		////
		Node node = new Node(
				facilityId,
				dataManager.getFacilityName(),
				dataManager.getPlatformId(),
				dataManager.getSubPlatformId(),
				deviceList);
		boolean fetchFlg = node.fetchSharedTable(m_monitorId);

		// 2つ分のSharedTableのfetchが失敗した場合
		if(!fetchFlg){
			m_log.debug("collectList() SharedTable is not update. Create or replace node poller. " +
					"monitorTypeId = " + m_monitorTypeId + ",monitorId = " + m_monitorId  + ", facilityId = " + facilityId);

			// スケジューラの再登録は条件にかまわず実施する
			ModifyPollingSchedule schedule = new ModifyPollingSchedule();
			PerfCheckInfo perfCheckInfo = new PerfCheckInfo();
			perfCheckInfo.setBreakdownFlg(m_perf.getBreakdownFlg());
			perfCheckInfo.setDeviceDisplayName(m_perf.getDeviceDisplayName());
			perfCheckInfo.setItemCode(m_perf.getItemCode());
			perfCheckInfo.setMonitorId(m_perf.getMonitorId());
			perfCheckInfo.setMonitorTypeId(m_monitor.getMonitorTypeId());
			schedule.addNodeSchedule(facilityId, m_monitorId, m_monitor.getMonitorTypeId(), m_monitor.getRunInterval(), perfCheckInfo);
			return resultList;
		}

		////
		// 全てのItemCode/deviceに対して処理を実施
		////
		for(String itemCode : itemCodeList){
			CollectorItemInfo itemInfo = null;
			boolean ret = false;
			Integer checkResult = null;

			// もしターゲットのdisplayNameがALLだったら、全てのデバイスに対して処理する
			if(m_perf.getDeviceDisplayName() != null && (PollingDataManager.ALL_DEVICE_NAME).equals(m_perf.getDeviceDisplayName())){
				m_log.debug("collectList() monitorId = " + m_monitorId + ", itemCode = " + itemCode + " , displayName = [" + PollingDataManager.ALL_DEVICE_NAME + "]");

				// 対象のデバイスがなければ何もしない
				if(deviceList != null && deviceList.size() > 0){
					for(NodeDeviceInfo deviceInfo : deviceList){

						ret = false; // デバイス毎に判定結果を初期化
						result = new MonitorRunResultInfo();
						result.setFacilityId(facilityId);
						itemInfo = new CollectorItemInfo(m_monitorId, itemCode, deviceInfo.getDeviceDisplayName());
						m_itemName = CollectorItemCodeTable.getFullItemName(itemInfo.getItemCode(), itemInfo.getDisplayName());
						try{
							m_value = node.calcValue(itemInfo);
							if(!Double.isNaN(m_value)){
								ret = true;
							} else {
								// FIXME 5.1では下記の機構は不要になるよう設計変更をすること（センシティブな修正なので、細かくコメントを記載します）
								//
								// 不明となった場合、実は直前のポーリング時に管理対象フラグがたっていなかった可能性がある。
								// そのため管理対象フラグが無効だった最後の時刻から、この情報を通知すべきか否かを検討する
								// リソース監視なので、2回分以上前のタイミングかを確認する
								if (PollingController.skipResourceMonitorNotifyByNodeFlagHistory(facilityId, m_runInterval)) {
									break;
								}
							}
						} catch (NotInitializedException e) {
							m_value = Double.NaN;
							ret = false;
						}
						result.setValue(m_value);
						result.setCollectorResult(ret);
						checkResult = getCheckResult(ret);

						if(m_perf.getItemCode().equals(itemInfo.getItemCode())){
							result.setMonitorFlg(true);
						}else{
							result.setMonitorFlg(false);
						}
						m_deviceData = deviceInfo;

						result.setCollectorFlg(true);
						if (m_now != null) {
							result.setNodeDate(m_now.getTime());
						}
						result.setMessage(getMessage(checkResult));
						result.setMessageOrg(getMessageOrg(checkResult));
						result.setMessageId(getMessageId(checkResult));
						result.setPriority(checkResult);
						result.setNotifyGroupId(getNotifyGroupId());
						result.setItemCode(itemInfo.getItemCode());
						result.setDisplayName(itemInfo.getDisplayName());
						resultList.add(result);
					}
				}else{
					// ALL_DEVICE指定の監視項目に該当デバイスが存在しないノードが割り当てられていることをログに残す
					m_log.info("collectList() monitorId = " + m_monitorId + ", itemCode = " + itemCode + " , displayName = [" + PollingDataManager.ALL_DEVICE_NAME + "]. " +
							"but target facility does not hove device. facilityId = " + facilityId);
				}
			}
			// もし特定のdisplayNameが指定されていたら、デバイスを特定して処理する
			else if(!"".equals(m_perf.getDeviceDisplayName())){
				m_log.debug("collectList() monitorId = " + m_monitorId + ", itemCode = " + itemCode + " , displayName = [" + m_perf.getDeviceDisplayName() + "]");

				// 対象のデバイスを特定する
				if(deviceList != null && deviceList.size() > 0){

					result = new MonitorRunResultInfo();
					result.setFacilityId(facilityId);
					itemInfo = new CollectorItemInfo(m_monitorId, itemCode, m_perf.getDeviceDisplayName());
					m_itemName = CollectorItemCodeTable.getFullItemName(itemInfo.getItemCode(), itemInfo.getDisplayName());

					try{
						m_value = node.calcValue(itemInfo);
						if(!Double.isNaN(m_value)){
							ret = true;
						} else {
							// FIXME 5.1では下記の機構は不要になるよう設計変更をすること（センシティブな修正なので、細かくコメントを記載します）
							//
							// 不明となった場合、実は直前のポーリング時に管理対象フラグがたっていなかった可能性がある。
							// そのため管理対象フラグが無効だった最後の時刻から、この情報を通知すべきか否かを検討する
							// リソース監視なので、2回分以上前のタイミングかを確認する
							if (PollingController.skipResourceMonitorNotifyByNodeFlagHistory(facilityId, m_runInterval)) {
								break;
							}
						}
					} catch (NotInitializedException e) {
						m_value = Double.NaN;
						ret = false;
					}
					result.setValue(m_value);
					result.setCollectorResult(ret);
					checkResult = getCheckResult(ret);

					if(m_perf.getItemCode().equals(itemInfo.getItemCode())){
						result.setMonitorFlg(true);
					}else{
						result.setMonitorFlg(false);
					}

					NodeDeviceInfo targetDeviceInfo = null;
					for(NodeDeviceInfo deviceInfo : deviceList){
						if(m_perf.getDeviceDisplayName().equals(deviceInfo.getDeviceDisplayName())){
							targetDeviceInfo = deviceInfo;
							break;
						}
					}
					m_deviceData = targetDeviceInfo;

					result.setCollectorFlg(true);
					if (m_now != null) {
						result.setNodeDate(m_now.getTime());
					}
					result.setMessage(getMessage(checkResult));
					result.setMessageId(getMessageId(checkResult));
					result.setMessageOrg(getMessageOrg(checkResult));
					result.setPriority(checkResult);
					result.setNotifyGroupId(getNotifyGroupId());
					result.setItemCode(itemInfo.getItemCode());
					result.setDisplayName(itemInfo.getDisplayName());
					resultList.add(result);

				}

			}
			// デバイスがない場合
			else{
				m_log.debug("collectList() monitorId = " + m_monitorId + ", itemCode = " + itemCode + " , displayName = " + m_perf.getDeviceDisplayName());

				result = new MonitorRunResultInfo();
				result.setFacilityId(facilityId);
				itemInfo = new CollectorItemInfo(m_monitorId, itemCode, "");
				m_itemName = CollectorItemCodeTable.getFullItemName(itemInfo.getItemCode(), itemInfo.getDisplayName());
				try{
					m_value = node.calcValue(itemInfo);
					if(!Double.isNaN(m_value)){
						ret = true;
					} else {
						// FIXME 5.1では下記の機構は不要になるよう設計変更をすること（センシティブな修正なので、細かくコメントを記載します）
						// 
						// 不明となった場合、実は直前のポーリング時に管理対象フラグがたっていなかった可能性がある。
						// そのため管理対象フラグが無効だった最後の時刻から、この情報を通知すべきか否かを検討する
						// リソース監視なので、2回分以上前のタイミングかを確認する
						if (PollingController.skipResourceMonitorNotifyByNodeFlagHistory(facilityId, m_runInterval)) {
							break;
						}
					}
				} catch (NotInitializedException e) {
					m_value = Double.NaN;
					ret = false;
				}
				result.setValue(m_value);
				result.setCollectorResult(ret);
				checkResult = getCheckResult(ret);

				if(m_perf.getItemCode().equals(itemInfo.getItemCode())){
					result.setMonitorFlg(true);
				}else{
					result.setMonitorFlg(false);
				}
				m_deviceData = null;

				result.setCollectorFlg(true);
				if (m_now != null) {
					result.setNodeDate(m_now.getTime());
				}
				result.setMessage(getMessage(checkResult));
				result.setMessageId(getMessageId(checkResult));
				result.setMessageOrg(getMessageOrg(checkResult));
				result.setPriority(checkResult);
				result.setNotifyGroupId(getNotifyGroupId());
				result.setItemCode(itemInfo.getItemCode());
				result.setDisplayName(itemInfo.getDisplayName());
				resultList.add(result);
			}
		}

		// 結果リストを返却
		return resultList;
	}

	private void notify(ArrayList<String> notifyFacilityIdList,
			ArrayList<MonitorRunResultInfo> notifyResultList) {
		if(m_monitor.getMonitorFlg() == ValidConstant.TYPE_INVALID){
			return;
		}
		// 通知IDが指定されていない場合、通知しない
		String notifyGroupId = m_monitor.getNotifyGroupId();
		if(notifyGroupId == null || "".equals(notifyGroupId)){
			return;
		}

		ArrayList<OutputBasicInfo> outputBasicInfoList = new ArrayList<OutputBasicInfo>(notifyFacilityIdList.size());
		for (int i = 0; i < notifyFacilityIdList.size(); i++) {
			String facilityId = notifyFacilityIdList.get(i);
			MonitorRunResultInfo resultInfo = notifyResultList.get(i);

			// 通知情報を設定
			OutputBasicInfo outputBasicInfo = new OutputBasicInfo();
			outputBasicInfo.setPluginId(m_monitorTypeId);
			outputBasicInfo.setMonitorId(m_monitorId);
			outputBasicInfo.setApplication(m_monitor.getApplication());

			// 通知抑制用のサブキーを設定。
			if(resultInfo.getDisplayName() != null && !"".equals(resultInfo.getDisplayName())){
				// 監視結果にデバイス名を含むものは、デバイス名をサブキーとして設定。
				outputBasicInfo.setSubKey(resultInfo.getDisplayName());
			}

			outputBasicInfo.setFacilityId(facilityId);
			outputBasicInfo.setScopeText(facilityId);
			outputBasicInfo.setPriority(resultInfo.getPriority());
			outputBasicInfo.setMessageId(resultInfo.getMessageId());
			outputBasicInfo.setMessage(resultInfo.getMessage());
			outputBasicInfo.setMessageOrg(resultInfo.getMessageOrg());
			outputBasicInfo.setGenerationDate(resultInfo.getNodeDate());

			outputBasicInfoList.add(outputBasicInfo);
		}
		new NotifyControllerBean().notify(outputBasicInfoList, m_monitor.getNotifyGroupId());
	}

	/**
	 *
	 */
	@Override
	protected void notify(
			boolean isNode, String facilityId,
			int result,
			Date generationDate,
			MonitorRunResultInfo resultInfo) throws HinemosUnknown {

		// for debug
		if (m_log.isDebugEnabled()) {
			m_log.debug("notify() isNode = " + isNode + ", facilityId = " + facilityId
					+ ", result = " + result + ", generationDate = " + generationDate.toString()
					+ ", resultInfo = " + resultInfo.getMessage());
		}

		// 監視無効の場合、通知しない
		if(m_monitor.getMonitorFlg() == ValidConstant.TYPE_INVALID){
			m_log.debug("notify() isNode = " + isNode + ", facilityId = " + facilityId
					+ ", result = " + result + ", generationDate = " + generationDate.toString()
					+ ", resultInfo = " + resultInfo.getMessage()
					+ ", monitorFlg is false");
			return;
		}

		// 通知IDが指定されていない場合、通知しない
		String notifyGroupId = resultInfo.getNotifyGroupId();
		if(notifyGroupId == null || "".equals(notifyGroupId)){
			return;
		}

		// 通知情報を設定
		OutputBasicInfo notifyInfo = new OutputBasicInfo();
		notifyInfo.setPluginId(m_monitorTypeId);
		notifyInfo.setMonitorId(m_monitorId);
		notifyInfo.setApplication(m_monitor.getApplication());

		// 通知抑制用のサブキーを設定。
		if(resultInfo.getDisplayName() != null && !"".equals(resultInfo.getDisplayName())){
			// 監視結果にデバイス名を含むものは、デバイス名をサブキーとして設定。
			notifyInfo.setSubKey(resultInfo.getDisplayName());
		}

		String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
		notifyInfo.setFacilityId(facilityId);
		notifyInfo.setScopeText(facilityPath);

		int priority = resultInfo.getPriority();
		String messageId = resultInfo.getMessageId();
		String message = resultInfo.getMessage();
		String messageOrg = resultInfo.getMessageOrg();

		notifyInfo.setPriority(priority);
		notifyInfo.setMessageId(messageId);
		notifyInfo.setMessage(message);
		notifyInfo.setMessageOrg(messageOrg);

		if(generationDate == null){
			notifyInfo.setGenerationDate(null);
		}
		else{
			notifyInfo.setGenerationDate(generationDate.getTime());
		}

		// for debug
		if (m_log.isDebugEnabled()) {
			m_log.debug("notify() priority = " + priority
					+ " , messageId = " + messageId
					+ " , message = " + message
					+ " , messageOrg = " + messageOrg
					+ ", generationDate = " + generationDate);
		}

		// ログ出力情報を送信
		if (m_log.isDebugEnabled()) {
			m_log.debug("sending message"
					+ " : priority=" + notifyInfo.getPriority()
					+ " generationDate=" + notifyInfo.getGenerationDate() + " pluginId=" + notifyInfo.getPluginId()
					+ " monitorId=" + notifyInfo.getMonitorId() + " facilityId=" + notifyInfo.getFacilityId()
					+ " subKey=" + notifyInfo.getSubKey()
					+ ")");
		}
		new NotifyControllerBean().notify(notifyInfo, notifyGroupId);
	}


	/**
	 * リソース監視情報を設定します。
	 * @see com.clustercontrol.monitor.run.factory.OperationNumericValueInfo#setMonitorAdditionInfo()
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {
		// 性能管理閾値監視情報を取得
		m_perf = QueryUtil.getMonitorPerfInfoPK(m_monitorId);
	}

	/**
	 * メッセージIDを取得します。
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessageId(int)
	 */
	@Override
	public String getMessageId(int result) {

		if(result == PriorityConstant.TYPE_INFO){
			return MESSAGE_ID_INFO;
		}
		else if(result == PriorityConstant.TYPE_WARNING){
			return MESSAGE_ID_WARNING;
		}
		else if(result == PriorityConstant.TYPE_CRITICAL){
			return MESSAGE_ID_CRITICAL;
		}
		else{
			return MESSAGE_ID_UNKNOWN;
		}
	}

	/**
	 * スコープ用メッセージIDを取得します。
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#getMessageIdForScope(int)
	 */
	@Override
	protected String getMessageIdForScope(int priority) {

		if(priority == PriorityConstant.TYPE_INFO){
			return MESSAGE_ID_INFO;
		}
		else if(priority == PriorityConstant.TYPE_WARNING){
			return MESSAGE_ID_WARNING;
		}
		else if(priority == PriorityConstant.TYPE_CRITICAL){
			return MESSAGE_ID_CRITICAL;
		}
		else{
			return MESSAGE_ID_UNKNOWN;
		}
	}

	/**
	 * メッセージを取得します。
	 */
	@Override
	public String getMessage(int result) {
		String valueString;
		if(Double.isNaN(m_value)){
			valueString = Messages.getString("message.performance.1");
		} else {
			valueString = NumberFormat.getNumberInstance().format(m_value);
		}
		return m_itemName + " : " + valueString;
	}

	/**
	 * メッセージを取得します。
	 */
	@Override
	protected String getMessageForScope(int result){
		String valueString;
		if(Double.isNaN(m_value)){
			valueString = Messages.getString("time.out");
		} else {
			valueString = NumberFormat.getNumberInstance().format(m_value);
		}
		return m_itemName + " : " + valueString;
	}

	/**
	 * オリジナルメッセージを取得します。
	 */
	@Override
	public String getMessageOrg(int result) {
		String valueString;
		if(Double.isNaN(m_value)){
			valueString = "NaN";
		} else {
			valueString = NumberFormat.getNumberInstance().format(m_value);

			m_log.debug("RunMonitorPerf messageOrg : " + valueString);

			// デバイス情報を付加
			if(m_deviceData != null) {

				valueString = valueString + "\n" +
						Messages.getString("device.name") + " : " + m_deviceData.getDeviceName() + "\n" +
						Messages.getString("device.index") + " : " + m_deviceData.getDeviceIndex();

				m_log.debug("RunMonitorPerf add DeviceInfo : " + valueString);
			}
		}
		return m_itemName + " : " + valueString;
	}

	/**
	 * オリジナルメッセージを取得します。
	 */
	@Override
	protected String getMessageOrgForScope(int result){
		String valueString;
		if(Double.isNaN(m_value)){
			valueString = "NaN";
		} else {
			valueString = NumberFormat.getNumberInstance().format(m_value);
		}
		return m_itemName + " : " + valueString;
	}


	@Override
	protected boolean setMonitorInfo(String monitorTypeId, String monitorId) throws MonitorNotFound, HinemosUnknown{
		boolean ret = super.setMonitorInfo(monitorTypeId, monitorId);

		// 次回が稼働日の場合はスケジュールを再作成する
		if(!m_isInCalendarTerm && m_isInNextCalendarTerm){
			try{
				RepositoryControllerBean repository = new RepositoryControllerBean();
				if(m_perf == null){
					setCheckInfo();
				}

				PerfCheckInfo perfCheckInfo = new PerfCheckInfo();
				perfCheckInfo.setBreakdownFlg(m_perf.getBreakdownFlg());
				perfCheckInfo.setDeviceDisplayName(m_perf.getDeviceDisplayName());
				perfCheckInfo.setItemCode(m_perf.getItemCode());
				perfCheckInfo.setMonitorId(m_perf.getMonitorId());
				perfCheckInfo.setMonitorTypeId(m_monitor.getMonitorTypeId());

				ModifyPollingSchedule schedule = new ModifyPollingSchedule();
				if(repository.isNode(m_facilityId)){
					// ノードの場合
					m_log.info("pre-schedule : monitorId = " + m_monitorId + ", facilityId = " + m_facilityId);
					schedule.addNodeSchedule(m_facilityId, m_monitorId, m_monitor.getMonitorTypeId(), m_monitor.getRunInterval(), perfCheckInfo);
				}
				else{
					// スコープの場合
					ArrayList<String> facilityList = repository.getExecTargetFacilityIdList(m_facilityId, m_monitor.getOwnerRoleId());
					if (facilityList.size() == 0) {
						m_log.info("pre-schedule : monitorId = " + m_monitorId + ", facilityId is null");
						return true;
					}

					for (String facilityId : facilityList) {
						m_log.info("pre-schedule : monitorId = " + m_monitorId + ", facilityId = " + facilityId);
						schedule.addNodeSchedule(facilityId, m_monitorId, m_monitor.getMonitorTypeId(), m_monitor.getRunInterval(), perfCheckInfo);
					}
				}

			} catch (FacilityNotFound e) {
				m_log.warn("setMonitorInfo() : fail to addSchedule . m_monitorId = " + m_monitorId, e);
			} catch (InvalidRole e) {
				m_log.warn("setMonitorInfo() : fail to addSchedule . m_monitorId = " + m_monitorId, e);
			}
		}

		return ret;
	}

}