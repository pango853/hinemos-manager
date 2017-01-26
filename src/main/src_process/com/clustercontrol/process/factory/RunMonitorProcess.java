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

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType;
import com.clustercontrol.plugin.impl.SharedTablePlugin;
import com.clustercontrol.poller.NotInitializedException;
import com.clustercontrol.poller.PollerManager;
import com.clustercontrol.poller.PollingController;
import com.clustercontrol.poller.PollingControllerConfig;
import com.clustercontrol.poller.bean.PollerProtocolConstant;
import com.clustercontrol.process.entity.MonitorProcessPollingMstData;
import com.clustercontrol.process.entity.MonitorProcessPollingMstPK;
import com.clustercontrol.process.model.MonitorProcessInfoEntity;
import com.clustercontrol.process.util.PollingDataManager;
import com.clustercontrol.process.util.ProcessProperties;
import com.clustercontrol.process.util.QueryUtil;
import com.clustercontrol.sharedtable.DataTable;
import com.clustercontrol.sharedtable.DataTableNotFoundException;
import com.clustercontrol.sharedtable.SharedTable;
import com.clustercontrol.sharedtable.TableEntry;
import com.clustercontrol.util.Messages;

/**
 * プロセス監視を実行するクラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class RunMonitorProcess extends RunMonitorNumericValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorProcess.class );

	private static final String MESSAGE_ID_INFO = "001";
	private static final String MESSAGE_ID_WARNING = "002";
	private static final String MESSAGE_ID_CRITICAL = "003";
	private static final String MESSAGE_ID_UNKNOWN = "004";

	/** プロセス監視情報 */
	private MonitorProcessInfoEntity m_process = null;

	/** コマンド */
	private String m_command = "";

	/** 引数 */
	private String m_param = "";

	/** SNMP収集値の共有テーブル */
	private SharedTable m_sst = null;

	/** メッセージ */
	private String m_message = null;

	/** オリジナルメッセージ */
	private String m_messageOrg = null;

	// collectメソッドがスレッドセーフではないため、同期をとるために使用
	private static Object m_syncObj = new Object();

	/**
	 * コンストラクタ
	 * @throws HinemosUnknown
	 */
	public RunMonitorProcess() throws HinemosUnknown {
		super();

		// 収集値の共有テーブルをルックアップ
		setSharedTable();
	}

	/**
	 * マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッド
	 * 
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#runMonitorInfo()
	 * @see com.clustercontrol.monitor.run.util.CallableTask
	 */
	@Override
	protected RunMonitor createMonitorInstance() throws HinemosUnknown {
		return new RunMonitorProcess();
	}

	/**
	 * プロセス数を取得します。<BR>
	 * 
	 * 
	 * @param facilityId ファシリティID
	 * @return 値取得に成功した場合、true
	 */
	@Override
	public boolean collect(String facilityId) {
		// 引数のコマンド,パラメータに一致したプロセス数
		int count = 0;

		// 監視開始時刻を設定
		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}
		m_value = 0;

		// メッセージを設定
		m_message = "";
		m_messageOrg = Messages.getString("command") + " : " + m_command + ", "
				+ Messages.getString("param") + " : " + m_param;

		// コマンド,パラメータのパターンを取得
		Pattern pCommand = null;
		Pattern pParam = null;
		Matcher m = null;

		// 収得時間の最小値を格納
		long mostOldPollingTime = Long.MAX_VALUE;

		try {
			// 大文字・小文字を区別しない場合
			if(ValidConstant.typeToBoolean(m_process.getCaseSensitivityFlg())){
				pCommand = Pattern.compile(m_command, Pattern.CASE_INSENSITIVE);
				pParam = Pattern.compile(m_param, Pattern.CASE_INSENSITIVE);
			}
			// 大文字・小文字を区別する場合
			else{
				pCommand = Pattern.compile(m_command);
				pParam = Pattern.compile(m_param);
			}
		} catch (PatternSyntaxException e) {
			m_log.info("collect() command, parameter PatternSyntax error : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			m_message = Messages.getString("message.process.4");
			return false;
		}

		// プロセス一覧が格納されたデータテーブルを取得
		DataTable table = null;
		try {
			table = m_sst.getDataTable(m_monitorTypeId, facilityId, m_monitorId, 0);
		} catch (DataTableNotFoundException e) {
			// 以下のエラーチェックで処理
		} catch (NotInitializedException e) {
			// 以下のエラーチェックで処理
		}

		// テーブルが存在するか否かを示すフラグ
		boolean isGetDateTable = true;
		// テーブルが存在しない場合は作成
		if(table == null){
			// マルチスレッド対応（同時に呼ばれる可能性があるためsynchronized化し、再度getDataTableを試みる）
			synchronized (m_syncObj) {
				try {
					setSharedTable();
					table = m_sst.getDataTable(m_monitorTypeId, facilityId, m_monitorId, 0);
				} catch (DataTableNotFoundException e) {
					isGetDateTable = false;
				} catch (NotInitializedException e) {
					isGetDateTable = false;
				}

				if(!isGetDateTable){
					m_log.debug("Create Table (" + facilityId + ", " + m_monitorId + ")");
					m_sst.createDataTable(m_monitorTypeId, facilityId, 1);
					try {
						// 実行間隔
						int runInterval = m_runInterval;
						// 監視対象スコープ
						String parentFacilityId = m_facilityId;//m_monitor.getFacilityId();

						// スケジュールも停止している可能性があるため再度登録
						new ModifyPollingSchedule().addSchedule(m_monitorTypeId, m_monitorId, parentFacilityId, runInterval);
						table = m_sst.getDataTable(m_monitorTypeId, facilityId, m_monitorId, 0);

						// なおテーブルが存在しない場合はエラー
						if(table == null){
							m_log.info("collect() DataTable create failure. (" + facilityId + ", " + m_monitorId + ")");

							// 異常終了（メッセージ：「値を取得できませんでした」）
							m_message = Messages.getString("message.process.6");
							return false;
						}
						
						// カレンダー非稼働後の初回の可能性があるため、ポーリングを発動する
					 	m_log.info("try polling after rescheduling poller. (" + facilityId + ", " + m_monitorId + ")");
					 	PollerManager manager = PollerManager.getInstnace();
					 	PollingController poller = manager.getPoller(m_monitorTypeId, facilityId);
					 	PollingControllerConfig config = poller.getPollingConfig();
					 	HashMap<String, List<String>> pollingTargetMap = config.getCurrentTargetMap(System.currentTimeMillis());
					 	table = poller.polling(pollingTargetMap, -1, -1);
					} catch (DataTableNotFoundException e) {
						// 異常終了（メッセージ：「値を取得できませんでした」）
						m_message = Messages.getString("message.process.6");
						return false;
					} catch (NotInitializedException e) {
						// 異常終了（メッセージ：「値を取得できませんでした」）
						m_message = Messages.getString("message.process.6");
						return false;
					}
				}
			}
		}

		// リポジトリ,DBから設定情報を取得する(プラットフォームID, サブプラットフォームID, 収集方法など)
		PollingDataManager dataManager = new PollingDataManager(facilityId);
		String collectMethod = dataManager.getCollectMethod();

		// SharedTableから取得するためのキーをDBのデータから作成する
		List<String> pollingTargets = new ArrayList<String>();	// 再収集用リスト

		// 収集方法がSNMPの場合
		if(collectMethod.equals(PollerProtocolConstant.PROTOCOL_SNMP)) {
			// SharedTableから取得するためのキーをDBのデータから作成する
			String runName = "";
			String runParam = "";
			String runPath = "";

			// cc_monitor_process_polling_mst から variable_id = "name" を取得
			MonitorProcessPollingMstData pollingBean;
			pollingBean = ProcessMasterCache.getMonitorProcessPollingMst(
					(new MonitorProcessPollingMstPK(
							collectMethod,
							dataManager.getPlatformId(),
							dataManager.getSubPlatformId(),
							"name")));
			if (pollingBean == null) {
				m_log.info("collect() pollingBean (name) is null");
				return false;
			}
			runName = PollerProtocolConstant.getEntryKey(collectMethod, pollingBean.getPollingTarget());
			m_log.debug("collect() runName : " + runName);
			pollingTargets.add(pollingBean.getPollingTarget());

			// cc_monitor_process_polling_mst から variable_id = "param" を取得
			pollingBean = ProcessMasterCache.getMonitorProcessPollingMst(new MonitorProcessPollingMstPK(
					collectMethod,
					dataManager.getPlatformId(),
					dataManager.getSubPlatformId(),
					"param"));
			if (pollingBean == null) {
				m_log.info("collect() pollingBean (param) is null");
				return false;
			}
			runParam = PollerProtocolConstant.getEntryKey(collectMethod, pollingBean.getPollingTarget());
			m_log.debug("collect() runParam : " + runParam);
			pollingTargets.add(pollingBean.getPollingTarget());

			// cc_monitor_process_polling_mst から variable_id = "path" を取得
			pollingBean = ProcessMasterCache.getMonitorProcessPollingMst(
					new MonitorProcessPollingMstPK(
							collectMethod,
							dataManager.getPlatformId(),
							dataManager.getSubPlatformId(),
							"path")
					);
			if (pollingBean == null){
				m_log.info("collect() pollingBean (path) is null");
				return false;
			}
			runPath = PollerProtocolConstant.getEntryKey(collectMethod, pollingBean.getPollingTarget());
			m_log.debug("collect() runPath : " + runPath);
			pollingTargets.add(pollingBean.getPollingTarget());

			// 起動名を指定してサブツリーの値を一度に取得
			Set<TableEntry> valueSetName = table.getValueSetStartWith(runName);
			Set<TableEntry> valueSetParam = table.getValueSetStartWith(runParam);
			Set<TableEntry> valueSetCommand = table.getValueSetStartWith(runPath);

			/*
			 * ・valueSetName、valueSetParam、valueSetCommandはそのファシリティ（ノード）の
			 * プロセス全体が入っている。(Set)これがNULLの時は"不明を返す"
			 * ・valueObjName、valueObjParam、valueObjCommand はここのプロセスの
			 * 情報と時刻のペアが入っている。(ValueObject)これがNULLの時は"プロセス数0"
			 * ・valueName、valueParam、valueCommandはSNMPで取得した内容自身(String)
			 * これがNULLの時は"不明を返す"
			 */
			if (valueSetName == null || valueSetParam == null || valueSetCommand == null) {
				// テーブルがなく生成した場合は、直後に値がないのは当然であるため、ログレベルを下げて出力
				if(isGetDateTable == false){
					m_log.debug("collect()  FacilityID : "
							+ facilityId
							+ ", "
							+ "valueSetName(Set) or valueSetParam(Set) or valueSetPath(Set) is null , SNMP Polling failed");
				} else {
					m_log.info("collect()  FacilityID : "
							+ facilityId
							+ ", "
							+ "valueSetName(Set) or valueSetParam(Set) or valueSetPath(Set) is null , SNMP Polling failed");
				}

				// 「値を取得できませんでした」メッセージでイベント通知
				// ポーリングに失敗して値が取得できていない。
				// 生存期間超過でSnmpSharedTableが削除されてしまった。など。
			} else {
				m_log.debug("Name    : " + valueSetName.size());
				m_log.debug("Param   : " + valueSetParam.size());
				m_log.debug("Command : " + valueSetCommand.size());

				TableEntry valueObjCommand = null;
				TableEntry valueObjParam = null;
				TableEntry valueObjName = null;
				String valueCommand = null;
				String valueParam = null;
				String valueName = null;

				Iterator<TableEntry> itr = valueSetName.iterator();
				while (itr.hasNext()) {
					valueObjName = itr.next();

					if (valueObjName != null) {
						// 取得時間がmostOldPollingTimeより小さい場合は、置き換える
						if (mostOldPollingTime > valueObjName.getDate())
							mostOldPollingTime = valueObjName.getDate();

						valueName = (String) valueObjName.getValue();
						if (valueName == null) {
							m_log.info("collect()  FacilityID : "
									+ facilityId
									+ ", "
									+ "valueName(String) is null. What snmp happened?");
							m_message = Messages.getString("message.process.6");
							break;  // while文を抜ける
							// return false;
						}
					} else {
						m_log.debug("collect()  FacilityID : "
								+ facilityId
								+ ", "
								+ "valueObjName(ValueObject) is null. What snmp happened?");
						continue;// 中身がnullなら次のチェックを行う
					}

					// インデックス付きの起動パスOIDを取得
					String key = valueObjName.getKey();
					String index = key.substring(key.lastIndexOf("."));

					// 起動パスOIDを指定して値を取得
					valueObjCommand = table.getValue(runPath + index);
					if (valueObjCommand != null) {
						valueCommand = (String) valueObjCommand.getValue();
						if (valueCommand == null) {
							m_log.info("collect()  FacilityID : "
									+ facilityId
									+ ", "
									+ "valueCommand(String) is null. What snmp happened?");
							m_message = Messages.getString("message.process.6");
							break;  // while文を抜ける
						}
					} else {
						m_log.debug("collect()  FacilityID : "
								+ facilityId
								+ ", "
								+ "valueObjCommand(ValueObject) is null. What snmp happened?");
						continue; // 中身がnullなら次のチェックを行う
					}

					// Windowsの場合を考慮して、valueCommandとvalueNameを連結する
					if (valueCommand.length() == 0) {
						// パスが取得できない場合はコマンド名
						// パスがnull OR 空文字
						valueCommand = valueName;
					} else if (!valueCommand.startsWith("/")
							&& valueCommand.endsWith("\\")
							&& !valueCommand.equals(valueName)) {
						// 条件・・・・
						// パスが'/'以外の文字で始まり
						// パスが'\'で終わっていて
						// パスとコマンド名が違う

						valueCommand = valueCommand + valueName;
					}

					m = pCommand.matcher(valueCommand);

					// 起動パスが、指定したコマンドと一致する場合
					if (m.matches()) {
						m_log.debug("collect()   FacilityID : " + facilityId
								+ ", " + "valueObjCommand: " + valueObjCommand);

						// 起動パラメータOIDを指定して値を取得
						valueObjParam = table.getValue(runParam + index);
						m_log.debug("collect()  FacilityID : " + facilityId
								+ ", " + " valueObjParam: " + valueObjParam);

						if (valueObjParam != null) {
							valueParam = (String) valueObjParam.getValue();
							if (valueParam == null) {
								m_log.info("collect()  FacilityID : "
										+ facilityId
										+ ", "
										+ "valueParam(String) is null. What snmp happened?");
								m_message = Messages.getString("message.process.6");
								break;  // while文を抜ける
							}
						} else {
							m_log.debug("collect()  FacilityID : "
									+ facilityId
									+ ", "
									+ "valueObjCommand(Object) is null. What snmp happened?");
							continue;// 中身がnullなら次のチェックを行う
						}

						// 起動パラメータが、指定した引数と一致する場合
						m = pParam.matcher(valueParam);
						if (m.matches()) {
							m_log.debug("collect()  FacilityID : " + facilityId
									+ ", " + "valueObjParam : " + valueObjParam);

							// SNMPポーラ収集時刻を取得
							Date pollingDate = new Date(valueObjCommand.getDate());

							// 収集時刻がSNMPポーラー収集許容時間よりも前だった場合、値取得失敗
							int tolerance = ProcessProperties.getProperties()
									.getStartSecond()
									+ ProcessProperties.getProperties()
									.getValidSecond();

							Calendar cal = Calendar.getInstance();
							cal.setTime(m_now);
							cal.add(Calendar.SECOND, -tolerance);
							if (cal.getTime().compareTo(pollingDate) > 0) {
								// 監視開始時刻を設定
								if (m_now != null) {
									m_nodeDate = m_now.getTime();
								}
								String[] args = { DateFormat
										.getDateTimeInstance().format(
												pollingDate) };
								// 「取得値が古いためチェックは行われませんでした」メッセージ
								m_message = Messages.getString("message.process.7", args);
								break;  // while文を抜ける
							}

							// ノードの値取得時刻を設定
							m_nodeDate = valueObjCommand.getDate();

							// オリジナルメッセージにコマンド名＋引数を与える設定
							if(ProcessProperties.getProperties().isDetailedDisplay())
								m_messageOrg = m_messageOrg + "\n" + index + " : " + valueCommand + " " + valueParam;

							count++;
						}
					}
				}

				// カウントが0の場合の収集時刻判定
				// 取得された最も古い時間と現在の時間を比較する
				if (count == 0) {
					// 収集時刻がSNMPポーラー収集許容時間よりも前だった場合、値取得失敗
					long tolerance = (ProcessProperties.getProperties()
							.getStartSecond() + ProcessProperties
							.getProperties().getValidSecond()) * 1000;

					if (m_now != null && (m_now.getTime() - mostOldPollingTime - tolerance > 0)) {
						// 監視開始時刻を設定
						m_nodeDate = m_now.getTime();

						Date mostOldPollingDate = new Date(mostOldPollingTime);

						String[] args = { DateFormat.getDateTimeInstance()
								.format(mostOldPollingDate) };
						// 「取得値が古いためチェックは行われませんでした」で通知
						m_message = Messages.getString("message.process.7", args);
						return false;
					}
				}

				// 正常終了
				m_value = count;
				m_message = Messages.getString("process.number") + " : "
						+ NumberFormat.getNumberInstance().format(m_value);
				return true;
			}
		}

		// 収集方法がWBEMの場合
		else if(collectMethod.equals(PollerProtocolConstant.PROTOCOL_WBEM)){
			// SharedTableから取得するためのキーをDBのデータから作成する
			String runParam = "";

			// cc_monitor_process_polling_mst から variable_id = "param" を取得
			MonitorProcessPollingMstData pollingBean =
					ProcessMasterCache.getMonitorProcessPollingMst(
							new MonitorProcessPollingMstPK(
									collectMethod,
									dataManager.getPlatformId(),
									dataManager.getSubPlatformId(),
									"param"));
			if (pollingBean == null) {
				m_log.info("pollingBean is null (param)");
				return false;
			}

			runParam = PollerProtocolConstant.getEntryKey(collectMethod, pollingBean.getPollingTarget());
			m_log.debug("collect() runParam : " + runParam);
			pollingTargets.add(pollingBean.getPollingTarget());

			// 起動名を指定してサブツリーの値を一度に取得
			Set<TableEntry> valueSetParam = table.getValueSetStartWith(runParam);

			/*
			 * ・valueSetParamはそのファシリティ（ノード）のプロセス全体が入っている。(Set)これがNULLの時は"不明を返す"
			 * ・valueObjParam はここのプロセスの情報と時刻のペアが入っている。(ValueObject)これがNULLの時は"プロセス数0"
			 * ・valueParamはWBEMで取得した内容自身(Vector)これがNULLの時は"不明を返す"
			 */
			if (valueSetParam == null) {

				if(isGetDateTable == false){
					m_log.debug("collect()  FacilityID : "
							+ facilityId
							+ ", "
							+ "valueSetParam(Set) is null , WBEM Polling failed");
				} else {
					m_log.info("collect()  FacilityID : "
							+ facilityId
							+ ", "
							+ "valueSetParam(Set) is null , WBEM Polling failed");
				}

				// 「値を取得できませんでした」メッセージでイベント通知
				// ポーリングに失敗して値が取得できていない。
				// 生存期間超過でSnmpSharedTableが削除されてしまった。など。
			} else {
				m_log.debug("Param   : " + valueSetParam.size());

				TableEntry valueObjParam = null;
				Vector<String> valueParam = null;

				String command = null;

				Iterator<TableEntry> itrParam = valueSetParam.iterator();

				// SharedTableの内容に不整合がある否かをチェックするフラグ。
				while (itrParam.hasNext()) {
					valueObjParam = itrParam.next();

					if(valueObjParam != null) {

						// 取得時間がmostOldPollingTimeより小さい場合は、置き換える
						if (mostOldPollingTime > valueObjParam.getDate())
							mostOldPollingTime = valueObjParam.getDate();

						valueParam = (Vector<String>) valueObjParam.getValue();

						if (valueParam == null) {
							m_log.info("collect() FacilityID : "
									+ facilityId
									+ ", "
									+ "valueParam(Vector) is null. What wbem happened?");
							m_message = Messages.getString("message.process.6");
							break;  // while文を抜ける
						}

						command = valueParam.get(0).toString();
						m_log.debug("command : " + command);
					}
					else {
						m_log.debug("collect()  FacilityID : "
								+ facilityId
								+ ", "
								+ "valueParam(Vector) is null. What wbem happened?");
						continue;// 中身がnullなら次のチェックを行う
					}

					// 監視設定のコマンド部分と比較
					m = pCommand.matcher(command);

					// 起動パスが、指定したコマンドと一致する場合
					if (m.matches()) {
						m_log.debug("collect()   FacilityID : " + facilityId
								+ ", " + "command: " + command);

						// 引数の作成
						String param = "";

						for(int i=1; i < valueParam.size(); i++) {

							param = param + valueParam.get(i);

							if(i+1 < valueParam.size()) {
								param = param + " ";
							}

							m_log.debug("param : " + param);
						}

						// 監視設定の起動パラメータが、指定した引数と一致する場合
						m = pParam.matcher(param);
						if (m.matches()) {
							m_log.debug("collect()  FacilityID : " + facilityId
									+ ", " + "param : " + param);

							// WBEMポーラ収集時刻を取得
							Date pollingDate = new Date(valueObjParam.getDate());

							// 収集時刻がWBEMポーラー収集許容時間よりも前だった場合、値取得失敗
							int tolerance = ProcessProperties.getProperties().getStartSecond()
									+ ProcessProperties.getProperties().getValidSecond();

							Calendar cal = Calendar.getInstance();
							cal.setTime(m_now);
							cal.add(Calendar.SECOND, -tolerance);
							if (cal.getTime().compareTo(pollingDate) > 0) {
								// 監視開始時刻を設定
								if (m_now != null) {
									m_nodeDate = m_now.getTime();
								}

								String[] args = { DateFormat
										.getDateTimeInstance().format(
												pollingDate) };
								// 「取得値が古いためチェックは行われませんでした」メッセージ
								m_message = Messages.getString("message.process.7", args);
								break;  // while文を抜ける
							}

							// ノードの値取得時刻を設定
							m_nodeDate = valueObjParam.getDate();

							// オリジナルメッセージにコマンド名＋引数を設定
							if(ProcessProperties.getProperties().isDetailedDisplay())
								m_messageOrg = m_messageOrg + "\n" + command + " " + param;

							count++;
						}
					}
				}

				// カウントが0の場合の収集時刻判定
				// 取得された最も古い時間と現在の時間を比較する
				if (count == 0) {
					// 収集時刻がSNMPポーラー収集許容時間よりも前だった場合、値取得失敗
					long tolerance = (ProcessProperties.getProperties()
							.getStartSecond() + ProcessProperties
							.getProperties().getValidSecond()) * 1000;

					if (m_now != null && (m_now.getTime() - mostOldPollingTime - tolerance > 0)) {
						// 監視開始時刻を設定
						m_nodeDate = m_now.getTime();

						Date mostOldPollingDate = new Date(mostOldPollingTime);

						String[] args = { DateFormat.getDateTimeInstance()
								.format(mostOldPollingDate) };
						// 「取得値が古いためチェックは行われませんでした」で通知
						m_message = Messages.getString("message.process.7", args);
						return false;
					}
				}

				// 正常終了
				m_value = count;
				m_message = Messages.getString("process.number") + " : "
						+ NumberFormat.getInstance().format(m_value);
				return true;
			}
		}

		// 最大リトライ回数超過。「タイムアウトしました」メッセージでイベント通知
		m_message = Messages.getString("message.process.8");
		return false;
	}

	/**
	 * プロセス監視情報を取得します。<BR>
	 * @see com.clustercontrol.monitor.run.factory.OperationNumericValueInfo#setMonitorAdditionInfo()
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {

		// プロセス監視情報を取得
		m_process = QueryUtil.getMonitorProcessInfoPK(m_monitorId);

		// プロセス監視情報を設定
		m_command = m_process.getCommand();
		if(m_process.getParam() != null){
			m_param = m_process.getParam();
		}
	}

	/**
	 * ノード用メッセージIDを取得します。<BR>
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessageId(int)
	 */
	@Override
	public String getMessageId(int id) {

		if(id == PriorityConstant.TYPE_INFO){
			return MESSAGE_ID_INFO;
		}
		else if(id == PriorityConstant.TYPE_WARNING){
			return MESSAGE_ID_WARNING;
		}
		else if(id == PriorityConstant.TYPE_CRITICAL){
			return MESSAGE_ID_CRITICAL;
		}
		else{
			return MESSAGE_ID_UNKNOWN;
		}
	}

	/**
	 * ノード用メッセージを取得します。<BR>
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessage(int)
	 */
	@Override
	public String getMessage(int id) {
		return m_message;
	}

	/**
	 * ノード用オリジナルメッセージを取得します。<BR>
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessageOrg(int)
	 */
	@Override
	public String getMessageOrg(int id) {
		return m_messageOrg;
	}

	/**
	 * スコープ用メッセージIDを取得します。<BR>
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
	 * 収集値の共有テーブルをルックアップします。
	 * 
	 */
	private void setSharedTable() {

		// SNMP収集値の共有テーブルをルックアップ
		m_sst = SharedTablePlugin.getSharedTable();
	}

	@Override
	protected boolean setMonitorInfo(String monitorTypeId, String monitorId) throws MonitorNotFound, HinemosUnknown{
		boolean ret = super.setMonitorInfo(monitorTypeId, monitorId);

		if(!m_isInCalendarTerm && m_isInNextCalendarTerm){
			// 次回が稼働日の場合はスケジュールを再作成する
			m_log.info("pre-schedule : monitorId = " + m_monitorId + ", facilityId = " + m_facilityId);
			new ModifyPollingSchedule().addSchedule(m_monitorTypeId, m_monitorId, m_facilityId, m_runInterval);
		}

		return ret;
	}

	@Override
	public int getCheckResult(boolean arg) {
		// FIXME 5.1では下記の機構は不要になるよう設計変更をすること
		// （センシティブな修正なので、細かくコメントを記載します）
		//
		// 基本的には規程クラスの RunMonitorNumericValueType.getCheckResult() の値を返すが、
		// 不明の場合に限っては、ノードの管理対象フラグの直近の状態を見て、監視結果をキャンセルする（通知しない）。
		// （ポーリング時点(00秒)では管理対象フラグがOFFで、集計時点(30秒)で管理対象フラグがONになった場合の対策）
		int retVal = super.getCheckResult(arg);
		if (retVal == PriorityConstant.TYPE_UNKNOWN) {
			if (PollingController.skipProcessMonitorNotifyByNodeFlagHistory(m_nodeId, m_runInterval) == true) {
				// ログ監視などで、フィルタにひっかからなかった場合には本関数が-2を返すことを利用し、強制的に-2を返却する
				retVal = -2;
			}
		}
		return retVal;
	}
}
