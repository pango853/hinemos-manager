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

package com.clustercontrol.monitor.run.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.custom.bean.CustomCheckInfo;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.http.bean.HttpCheckInfo;
import com.clustercontrol.http.bean.HttpScenarioCheckInfo;
import com.clustercontrol.jmx.bean.JmxCheckInfo;
import com.clustercontrol.logfile.bean.LogfileCheckInfo;
import com.clustercontrol.monitor.bean.MonitorFilterInfo;
import com.clustercontrol.monitor.plugin.bean.PluginCheckInfo;
import com.clustercontrol.monitor.run.bean.MonitorInfo;
import com.clustercontrol.monitor.run.bean.MonitorNumericValueInfo;
import com.clustercontrol.monitor.run.bean.MonitorStringValueInfo;
import com.clustercontrol.monitor.run.bean.MonitorTruthValueInfo;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfoEntity;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfoEntity;
import com.clustercontrol.monitor.run.model.MonitorTruthValueInfoEntity;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.notify.bean.NotifyRelationInfo;
import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.performance.monitor.bean.PerfCheckInfo;
import com.clustercontrol.ping.bean.PingCheckInfo;
import com.clustercontrol.port.bean.PortCheckInfo;
import com.clustercontrol.process.bean.ProcessCheckInfo;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.snmp.bean.SnmpCheckInfo;
import com.clustercontrol.snmptrap.bean.TrapCheckInfo;
import com.clustercontrol.sql.bean.SqlCheckInfo;
import com.clustercontrol.util.apllog.AplLogger;
import com.clustercontrol.winevent.bean.WinEventCheckInfo;
import com.clustercontrol.winservice.bean.WinServiceCheckInfo;

/**
 * 監視情報を検索する抽象クラス<BR>
 * <p>
 * 監視種別（真偽値，数値，文字列）の各クラスで継承してください。
 *
 * @version 4.1.0
 * @since 2.0.0
 */
public class SelectMonitor {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectMonitor.class );

	/** 監視情報のローカルコンポーネント。 */
	protected MonitorInfoEntity m_monitor;

	/** 監視対象ID。 */
	protected String m_monitorTypeId;

	/** 監視項目ID。 */
	protected String m_monitorId;

	/**
	 * 引数で指定された監視情報を返します。
	 * <p>
	 * <ol>
	 * <li>引数で指定された監視情報を取得します。</li>
	 * <li>Quartzより、有効/無効を取得します。</li>
	 * <li>監視情報より判定情報を取得します。各監視種別（真偽値，数値，文字列）のサブクラスで実装します（{@link #getJudgementInfo()}）。</li>
	 * <li>監視情報よりチェック条件を取得します。各監視管理のサブクラスで実装します（{@link #getCheckInfo()}）。</li>
	 * </ol>
	 * 
	 * @param monitorTypeId 監視対象ID
	 * @param monitorId 監視項目ID
	 * @return 監視情報
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorInfoBean
	 * @see com.clustercontrol.monitor.run.factory.SelectJobKick#getValid(String, String)
	 * @see #getJudgementInfo()
	 * @see #getCheckInfo()
	 */

	public MonitorInfo getMonitor(String monitorTypeId, String monitorId)
			throws MonitorNotFound, HinemosUnknown, InvalidRole {

		m_monitorTypeId = monitorTypeId;
		m_monitorId = monitorId;

		MonitorInfo bean = null;
		try
		{
			// 監視情報を取得
			m_monitor = QueryUtil.getMonitorInfoPK(m_monitorId);

			bean = getMonitorInfoBean(m_monitor);

		} catch (MonitorNotFound e) {
			outputLog("010");
			throw e;
		} catch (InvalidRole e) {
			outputLog("010");
			throw e;
		} catch (HinemosUnknown e) {
			outputLog("010");
			throw e;
		}

		return bean;
	}

	/**
	 * 判定情報を返します。
	 * <p>
	 * @return 判定情報（{@link com.clustercontrol.monitor.run.bean.MonitorJudgementInfo}のリスト）
	 */
	protected ArrayList<MonitorNumericValueInfo> getNumericValueInfo() {
		// 数値監視判定情報を取得
		Collection<MonitorNumericValueInfoEntity> ct = m_monitor.getMonitorNumericValueInfoEntities();
		ArrayList<MonitorNumericValueInfo> valueList = new ArrayList<MonitorNumericValueInfo>();

		for (MonitorNumericValueInfoEntity entity : ct) {
			MonitorNumericValueInfo value = new MonitorNumericValueInfo(
					entity.getMessage(),
					entity.getMessageId(),
					entity.getId().getMonitorId(),
					entity.getId().getPriority(),
					entity.getThresholdLowerLimit(),
					entity.getThresholdUpperLimit());
			valueList.add(value);
		}
		return valueList;
	}

	/**
	 * 判定情報を返します。
	 * <p>
	 * @return 判定情報（{@link com.clustercontrol.monitor.run.bean.MonitorJudgementInfo}のリスト）
	 */
	protected ArrayList<MonitorStringValueInfo> getStringValueInfo() {

		// 文字列監視判定情報を取得
		List<MonitorStringValueInfoEntity> ct = m_monitor.getMonitorStringValueInfoEntities();
		
		// Entityを直接ソートすると、データが変更されたと判定されてしまうため別のリストにつめなおす
		List<MonitorStringValueInfoEntity> entityList = new ArrayList<>(ct);
		Collections.sort(entityList, new MonitorStringValueInfoComparator());
		ArrayList<MonitorStringValueInfo> valueList = new ArrayList<MonitorStringValueInfo>();

		for (MonitorStringValueInfoEntity entity : entityList) {
			MonitorStringValueInfo value = new MonitorStringValueInfo(
					entity.getId().getMonitorId(),
					entity.getDescription(),
					entity.getProcessType().intValue(),
					entity.getPattern(),
					entity.getPriority().intValue(),
					entity.getMessage(),
					ValidConstant.typeToBoolean(entity.getCaseSensitivityFlg()),
					ValidConstant.typeToBoolean(entity.getValidFlg().intValue())
					);
			valueList.add(value);
		}

		return valueList;
	}

	/**
	 * 判定情報を返します。
	 * <p>
	 * @return 判定情報（{@link com.clustercontrol.monitor.run.bean.MonitorJudgementInfo}のリスト）
	 */
	protected ArrayList<MonitorTruthValueInfo> getTruthValueInfo() {
		// 真偽値監視判定情報を取得
		Collection<MonitorTruthValueInfoEntity> ct = m_monitor.getMonitorTruthValueInfoEntities();

		ArrayList<MonitorTruthValueInfo> valueList = new ArrayList<MonitorTruthValueInfo>();

		for (MonitorTruthValueInfoEntity entity : ct) {
			MonitorTruthValueInfo value = new MonitorTruthValueInfo(
					entity.getMessage(),
					entity.getMessageId(),
					entity.getId().getMonitorId(),
					entity.getId().getPriority(),
					entity.getId().getTruthValue());
			valueList.add(value);
		}
		return valueList;
	}

	/**
	 * チェック条件情報を返します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 * 
	 * @return チェック条件情報
	 * @throws MonitorNotFound
	 */
	protected HttpCheckInfo getHttpCheckInfo() throws MonitorNotFound {
		return null;
	}

	/**
	 * チェック条件情報を返します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 * 
	 * @return チェック条件情報
	 * @throws MonitorNotFound
	 */
	protected HttpScenarioCheckInfo getHttpScenarioCheckInfo() throws MonitorNotFound {
		return null;
	}

	/**
	 * チェック条件情報を返します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 * 
	 * @return チェック条件情報
	 * @throws MonitorNotFound
	 */
	protected JmxCheckInfo getJmxCheckInfo() throws MonitorNotFound {
		return null;
	}

	/**
	 * チェック条件情報を返します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 * 
	 * @return チェック条件情報
	 * @throws MonitorNotFound
	 */
	protected PerfCheckInfo getPerfCheckInfo() throws MonitorNotFound {
		return null;
	}

	/**
	 * チェック条件情報を返します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 * 
	 * @return チェック条件情報
	 * @throws MonitorNotFound
	 */
	protected PingCheckInfo getPingCheckInfo() throws MonitorNotFound {
		return null;
	}

	/**
	 * チェック条件情報を返します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 * 
	 * @return チェック条件情報
	 * @throws MonitorNotFound
	 */
	protected PluginCheckInfo getPluginCheckInfo() throws MonitorNotFound {
		return null;
	}

	/**
	 * チェック条件情報を返します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 * 
	 * @return チェック条件情報
	 * @throws MonitorNotFound
	 */
	protected PortCheckInfo getPortCheckInfo() throws MonitorNotFound {
		return null;
	}

	/**
	 * チェック条件情報を返します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 * 
	 * @return チェック条件情報
	 * @throws MonitorNotFound
	 */
	protected ProcessCheckInfo getProcessCheckInfo() throws MonitorNotFound {
		return null;
	}

	/**
	 * チェック条件情報を返します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 * 
	 * @return チェック条件情報
	 * @throws MonitorNotFound
	 */
	protected SnmpCheckInfo getSnmpCheckInfo() throws MonitorNotFound {
		return null;
	}

	/**
	 * チェック条件情報を返します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 * 
	 * @return チェック条件情報
	 * @throws MonitorNotFound
	 */
	protected SqlCheckInfo getSqlCheckInfo() throws MonitorNotFound {
		return null;
	}

	/**
	 * チェック条件情報を返します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 * 
	 * @return チェック条件情報
	 * @throws MonitorNotFound
	 */
	protected TrapCheckInfo getTrapCheckInfo() throws MonitorNotFound {
		return null;
	}

	/**
	 * チェック条件情報(コマンド監視)を返す。
	 * @return チェック条件情報(コマンド監視)
	 * @throws MonitorNotFound
	 */
	protected CustomCheckInfo getCommandCheckInfo() throws MonitorNotFound {
		return null;
	}

	/**
	 * チェック条件情報(ログファイル監視)を返す。
	 * @return チェック条件情報(ログファイル監視)
	 * @throws MonitorNotFound
	 */
	protected LogfileCheckInfo getLogfileCheckInfo() throws MonitorNotFound {
		return null;
	}

	/**
	 * チェック条件情報(Windowsサービス監視)を返す。
	 * @return チェック条件情報(Windowsサービス監視)
	 * @throws MonitorNotFound
	 */
	protected WinServiceCheckInfo getWinServiceCheckInfo() throws MonitorNotFound {
		return null;
	}

	/**
	 * チェック条件情報(Windowsイベント監視)を返す。
	 * @return チェック条件情報(Windowsイベント監視)
	 * @throws MonitorNotFound
	 */
	protected WinEventCheckInfo getWinEventCheckInfo() throws MonitorNotFound {
		return null;
	}

	/**
	 * 監視情報一覧を返します。
	 * <p>
	 * <ol>
	 * <li>引数で指定された監視対象の監視情報を取得します。</li>
	 * <li>１監視情報をテーブルのカラム順（{@link com.clustercontrol.monitor.run.bean.MonitorTabelDefine}）に、リスト（{@link ArrayList}）にセットします。</li>
	 * <li>この１監視情報を保持するリストを、監視情報一覧を保持するリスト（{@link ArrayList}）に格納し返します。<BR>
	 *  <dl>
	 *  <dt>監視情報一覧（Objectの2次元配列）</dt>
	 *  <dd>{ 監視情報1 {カラム1の値, カラム2の値, … }, 監視情報2{カラム1の値, カラム2の値, …}, … }</dd>
	 *  </dl>
	 * </li>
	 * </ol>
	 * 
	 * @param monitorTypeId 監視対象ID
	 * @return 監視情報一覧（Objectの2次元配列）
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @see com.clustercontrol.monitor.run.bean.MonitorTabelDefine
	 * @see #collectionToArray(Collection)
	 */
	public ArrayList<MonitorInfo> getMonitorList(String monitorTypeId) throws MonitorNotFound, InvalidRole, HinemosUnknown {

		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		try
		{
			// 監視情報一覧を取得
			List<MonitorInfoEntity> ct = QueryUtil.getMonitorInfoByMonitorTypeId(monitorTypeId);

			for (MonitorInfoEntity info : ct) {
				list.add(getMonitorInfoBean(info));
			}
		} catch (MonitorNotFound e) {
			outputLog("011");
			throw e;
		} catch (InvalidRole e) {
			outputLog("011");
			throw e;
		} catch (HinemosUnknown e) {
			outputLog("011");
			throw e;
		}
		return list;
	}
	
	/**
	 * 監視情報一覧を返します。
	 * <p>
	 * <ol>
	 * <li>引数で指定された監視対象の監視情報を取得します。</li>
	 * <li>１監視情報をテーブルのカラム順（{@link com.clustercontrol.monitor.run.bean.MonitorTabelDefine}）に、リスト（{@link ArrayList}）にセットします。</li>
	 * <li>この１監視情報を保持するリストを、監視情報一覧を保持するリスト（{@link ArrayList}）に格納し返します。<BR>
	 *  <dl>
	 *  <dt>監視情報一覧（Objectの2次元配列）</dt>
	 *  <dd>{ 監視情報1 {カラム1の値, カラム2の値, … }, 監視情報2{カラム1の値, カラム2の値, …}, … }</dd>
	 *  </dl>
	 * </li>
	 * </ol>
	 * 
	 * @param monitorTypeId 監視対象ID
	 * @return 監視情報一覧（Objectの2次元配列）
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @see com.clustercontrol.monitor.run.bean.MonitorTabelDefine
	 * @see #collectionToArray(Collection)
	 */
	public ArrayList<MonitorInfo> getMonitorListObjectPrivilegeModeNONE(String monitorTypeId) throws MonitorNotFound, InvalidRole, HinemosUnknown {

		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		try
		{
			// 監視情報一覧を取得
			List<MonitorInfoEntity> ct = QueryUtil.getMonitorInfoByMonitorTypeId_NONE(monitorTypeId);

			for (MonitorInfoEntity info : ct) {
				list.add(getMonitorInfoBean(info));
			}
		} catch (MonitorNotFound e) {
			outputLog("011");
			throw e;
		} catch (InvalidRole e) {
			outputLog("011");
			throw e;
		} catch (HinemosUnknown e) {
			outputLog("011");
			throw e;
		}
		return list;
	}

	/**
	 * 監視情報一覧を返します。
	 * <p>
	 * <ol>
	 * <li>全ての監視対象の監視情報を取得します。</li>
	 * <li>１監視情報をテーブルのカラム順（{@link com.clustercontrol.monitor.run.bean.MonitorTabelDefine}）に、リスト（{@link ArrayList}）にセットします。</li>
	 * <li>この１監視情報を保持するリストを、監視情報一覧を保持するリスト（{@link ArrayList}）に格納し返します。<BR>
	 *  <dl>
	 *  <dt>監視情報一覧（Objectの2次元配列）</dt>
	 *  <dd>{ 監視情報1 {カラム1の値, カラム2の値, … }, 監視情報2{カラム1の値, カラム2の値, …}, … }</dd>
	 *  </dl>
	 * </li>
	 * </ol>
	 * 
	 * @return 監視情報一覧（Objectの2次元配列）
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @see com.clustercontrol.monitor.run.bean.MonitorTabelDefine
	 * @see #collectionToArray(Collection)
	 */
	public ArrayList<MonitorInfo> getMonitorList() throws MonitorNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("getMonitorList() : start");
		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		try
		{
			// 監視情報一覧を取得
			List<MonitorInfoEntity> ct = QueryUtil.getAllMonitorInfo();

			for (MonitorInfoEntity info : ct) {
				list.add(getMonitorInfoBean(info));

				// for debug
				if(m_log.isDebugEnabled()){
					m_log.debug("getMonitorList() : " +
							"monitorId = " + info.getMonitorId() +
							", monitorTypeId = " + info.getMonitorTypeId() +
							", monitorType = " + info.getMonitorType() +
							", description = " + info.getDescription() +
							", facilityId = " + info.getFacilityId() +
							", runInterval = " + info.getRunInterval() +
							", calendarId = " + info.getCalendarId() +
							", failurePriority = " + info.getFailurePriority() +
							", notifyGroupId = " + info.getNotifyGroupId() +
							", application = " + info.getApplication() +
							", monitorFlg = " + info.getMonitorFlg() +
							", collectorFlg = " + info.getCollectorFlg() +
							", regDate = " + info.getRegDate() +
							", updateDate = " + info.getUpdateDate() +
							", regUser = " + info.getRegUser() +
							", updateUser = " + info.getUpdateUser());
				}
			}
		} catch (MonitorNotFound e) {
			outputLog("011");
			throw e;
		} catch (HinemosUnknown e) {
			outputLog("011");
			throw e;
		}

		m_log.debug("getMonitorList() : end");
		return list;
	}

	/**
	 * 監視情報一覧を返します。
	 * <p>
	 * <ol>
	 * <li>全ての監視対象の監視情報を取得します。</li>
	 * <li>１監視情報をテーブルのカラム順（{@link com.clustercontrol.monitor.run.bean.MonitorTabelDefine}）に、リスト（{@link ArrayList}）にセットします。</li>
	 * <li>この１監視情報を保持するリストを、監視情報一覧を保持するリスト（{@link ArrayList}）に格納し返します。<BR>
	 *  <dl>
	 *  <dt>監視情報一覧（Objectの2次元配列）</dt>
	 *  <dd>{ 監視情報1 {カラム1の値, カラム2の値, … }, 監視情報2{カラム1の値, カラム2の値, …}, … }</dd>
	 *  </dl>
	 * </li>
	 * </ol>
	 * 
	 * @return 監視情報一覧（Objectの2次元配列）
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @see com.clustercontrol.monitor.run.bean.MonitorTabelDefine
	 * @see #collectionToArray(Collection)
	 */
	public ArrayList<MonitorInfo> getPerformanceMonitorList() throws MonitorNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("getMonitorList() : start");
		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		try
		{
			// 監視情報一覧を取得
			List<MonitorInfoEntity> ct = QueryUtil.getAllMonitorInfo();

			for (MonitorInfoEntity info : ct) {
				list.add(getMonitorInfoBean(info));

				// for debug
				if(m_log.isDebugEnabled()){
					m_log.debug("getMonitorList() : " +
							"monitorId = " + info.getMonitorId() +
							", monitorTypeId = " + info.getMonitorTypeId() +
							", monitorType = " + info.getMonitorType() +
							", description = " + info.getDescription() +
							", facilityId = " + info.getFacilityId() +
							", runInterval = " + info.getRunInterval() +
							", calendarId = " + info.getCalendarId() +
							", failurePriority = " + info.getFailurePriority() +
							", notifyGroupId = " + info.getNotifyGroupId() +
							", application = " + info.getApplication() +
							", monitorFlg = " + info.getMonitorFlg() +
							", collectorFlg = " + info.getCollectorFlg() +
							", regDate = " + info.getRegDate() +
							", updateDate = " + info.getUpdateDate() +
							", regUser = " + info.getRegUser() +
							", updateUser = " + info.getUpdateUser());
				}
			}
		} catch (MonitorNotFound e) {
			outputLog("011");
			throw e;
		} catch (InvalidRole e) {
			outputLog("011");
			throw e;
		} catch (HinemosUnknown e) {
			outputLog("011");
			throw e;
		}

		m_log.debug("getMonitorList() : end");
		return list;
	}

	/**
	 * 指定したフィルタにマッチする監視情報一覧を返します。
	 * @param condition
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getMonitorList(MonitorFilterInfo condition) throws HinemosUnknown, InvalidRole, MonitorNotFound {
		m_log.debug("getMonitorList() condition ");
		if(m_log.isDebugEnabled()){
			if(condition != null){
				m_log.debug("getMonitorList() " +
						"monitorId = " + condition.getMonitorId() +
						", monitorTypeId = " + condition.getMonitorTypeId() +
						", description = " + condition.getDescription() +
						", facilityId = " + condition.getFacilityId() +
						", calendarId = " + condition.getCalendarId() +
						", regUser = " + condition.getRegUser() +
						", regFromDate = " + condition.getRegFromDate() +
						", regToDate = " + condition.getRegToDate() +
						", updateUser = " + condition.getUpdateUser() +
						", updateFromDate = " + condition.getUpdateFromDate() +
						", updateToDate = " + condition.getUpdateToDate() +
						", monitorFlg = " + condition.getMonitorFlg() +
						", collectorFlg = " + condition.getCollectorFlg() +
						", ownerRoleId = " + condition.getOwnerRoleId());
			}
		}

		ArrayList<MonitorInfo> filterList = new ArrayList<MonitorInfo>();
		// 条件未設定の場合は空のリストを返却する
		if(condition == null){
			m_log.debug("getMonitorList() condition is null");
			return filterList;
		}

		// facilityId以外の条件で監視設定情報を取得
		List<MonitorInfoEntity> entityList = QueryUtil.getMonitorInfoByFilter(
				condition.getMonitorId(),
				condition.getMonitorTypeId(),
				condition.getDescription(),
				condition.getCalendarId(),
				condition.getRegUser(),
				condition.getRegFromDate(),
				condition.getRegToDate(),
				condition.getUpdateUser(),
				condition.getUpdateFromDate(),
				condition.getUpdateToDate(),
				condition.getMonitorFlg(),
				condition.getCollectorFlg(),
				condition.getOwnerRoleId());

		// facilityIdのみJavaで抽出する。
		for(MonitorInfoEntity entity : entityList){
			// facilityId
			if(condition.getFacilityId() != null && !"".equals(condition.getFacilityId()) && entity.getFacilityId() != null){
				// FacilitySelector.getNodeFacilityIdListの第一引数が登録ノード全ての場合は、空リストを返す。そのため、下記のifを追加。
				if (!FacilityConstant.STRING_COMPOSITE.equals(entity.getFacilityId())) {
					ArrayList<String> searchIdList = FacilitySelector.getNodeFacilityIdList(entity.getFacilityId(), entity.getOwnerRoleId(), RepositoryControllerBean.ALL, false, true);

					if(!searchIdList.contains(condition.getFacilityId())){
						m_log.debug("getMonitorList() continue : collectorFlg target = " + entity.getFacilityId() + ", filter = " + condition.getFacilityId());
						continue;
					}
				}
			}

			m_log.debug("getMonitorList() add display list : target = " + entity.getMonitorId());
			filterList.add(getMonitorInfoBean(entity));
		}
		return filterList;
	}

	/**
	 * MonitorInfoEntityからMonitorInfoBeanへ変換
	 * 
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	private MonitorInfo getMonitorInfoBean(MonitorInfoEntity monitorEntity) throws MonitorNotFound, InvalidRole, HinemosUnknown {
		MonitorInfo info = new MonitorInfo();
		m_monitor = monitorEntity;
		m_monitorTypeId = monitorEntity.getMonitorTypeId();
		m_monitorId = monitorEntity.getMonitorId();

		// スコープの取得
		String facilityPath = new RepositoryControllerBean().getFacilityPath(m_monitor.getFacilityId(), null);

		List<NotifyRelationInfo> notifyId = NotifyRelationCache.getNotifyList(m_monitor.getNotifyGroupId());
		info = new MonitorInfo(
				m_monitor.getApplication(),
				m_monitor.getCalendarId(),
				m_monitor.getDescription(),
				facilityPath,
				m_monitor.getFacilityId(),
				m_monitor.getFailurePriority(),
				m_monitorId,
				m_monitorTypeId,
				m_monitor.getMonitorType(),
				m_monitor.getRegDate()==null?null:m_monitor.getRegDate().getTime(),
				m_monitor.getRegUser(),
				m_monitor.getRunInterval(),
				notifyId,
				m_monitor.getUpdateDate()==null?null:m_monitor.getUpdateDate().getTime(),
				m_monitor.getUpdateUser(),
				m_monitor.getMonitorFlg(),
				m_monitor.getCollectorFlg(),
				m_monitor.getItemName(),
				m_monitor.getMeasure(),
				m_monitor.getOwnerRoleId(),
				getNumericValueInfo(),
				getStringValueInfo(),
				getTruthValueInfo(),
				getHttpCheckInfo(),
				getHttpScenarioCheckInfo(),
				getPerfCheckInfo(),
				getPingCheckInfo(),
				getPluginCheckInfo(),
				getPortCheckInfo(),
				getProcessCheckInfo(),
				getSnmpCheckInfo(),
				getSqlCheckInfo(),
				getTrapCheckInfo(),
				getCommandCheckInfo(),
				getLogfileCheckInfo(),
				getWinServiceCheckInfo(),
				getWinEventCheckInfo(),
				getJmxCheckInfo());

		return info;
	}

	/**
	 * アプリケーションログにログを出力します。
	 * 
	 * @param index アプリケーションログのインデックス
	 */
	private void outputLog(String index) {
		AplLogger apllog = new AplLogger("MON", "mon");
		String[] args = {m_monitorTypeId, m_monitorId };
		apllog.put("SYS", index, args);
	}

	/**
	 * 文字列監視用のフィルタ条件のリストのOrderNo順のソート処理用
	 * 
	 *
	 */
	class MonitorStringValueInfoComparator implements java.util.Comparator<MonitorStringValueInfoEntity>{
		/**
		 * orderNoの昇順でソート
		 */
		@Override
		public int compare(MonitorStringValueInfoEntity o1, MonitorStringValueInfoEntity o2) {
			return (o1.getId().getOrderNo() - o2.getId().getOrderNo());
		}
	}
}