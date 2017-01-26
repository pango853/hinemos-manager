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

package com.clustercontrol.performance.bean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.performance.util.CalculationMethod;
import com.clustercontrol.performance.util.CollectorMasterCache;
import com.clustercontrol.plugin.impl.SharedTablePlugin;
import com.clustercontrol.poller.NotInitializedException;
import com.clustercontrol.repository.bean.NodeDeviceInfo;
import com.clustercontrol.sharedtable.DataTable;
import com.clustercontrol.sharedtable.DataTableNotFoundException;
import com.clustercontrol.sharedtable.SharedTable;

/**
 * Facilityのノード実装クラス
 * 種別がノードであるファシリティの性能値を保持します。
 * ScopeTreeクラスの内部で使用することを目的としたクラスです。
 * 
 * @version 4.0.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://collector.ws.clustercontrol.com")
public class Node extends Facility {
	private static final long serialVersionUID = -5989756829710363991L;

	//	ログ出力
	private static Log m_log = LogFactory.getLog( Node.class );

	private static Object modifyLock = new Object();

	private static SharedTable _sharedTable = null;

	/**
	 * キー ： デバイス種別
	 * 値　　：　デバイス情報のリスト
	 */
	private HashMap<String, HashMap<String, NodeDeviceInfo>> m_deviceMap;

	private String platformId;
	private String subPlatformId;

	private DataTable currentTable = null;
	private DataTable previousTable = null;

	private Long lastCollectTime = new Long(0);;   // 最終更新時刻

	/**
	 * コンストラクター
	 * 
	 * @param facilityID
	 * @param faclityName
	 * @param deviceDataList このノードに含まれる全てのデバイス情報
	 */
	public Node(final String facilityId, final String faclityName, final String platformId, final String subPlatformId,
			final List<? extends NodeDeviceInfo> deviceInfoList){
		super(facilityId, faclityName, Facility.NODE);

		this.platformId = platformId;
		this.subPlatformId = subPlatformId;
		m_log.debug("Node() : create Node " + facilityId + ", " + faclityName + ", " + this.platformId + ", " + this.subPlatformId);

		m_deviceMap = new HashMap<String, HashMap<String, NodeDeviceInfo>>();

		for(NodeDeviceInfo deviceInfo : deviceInfoList){

			String deviceType = deviceInfo.getDeviceType();

			if (m_deviceMap.containsKey(deviceType) == false) {
				m_deviceMap.put(deviceType, new HashMap<String, NodeDeviceInfo>());
			}

			// デバイスタイプごとのマップを取得
			HashMap<String, NodeDeviceInfo> mapByDeviceType = m_deviceMap.get(deviceType);

			// リポジトリ表示名をキーにしてデバイス情報を設定
			mapByDeviceType.put(deviceInfo.getDeviceDisplayName(), deviceInfo);
		}

		initialize();
	}

	/**
	 * 初期化
	 */
	public void initialize() {
		synchronized (modifyLock) {
			// 既に初期化済みの場合は何もしない。
			if(_sharedTable != null){
				return;
			}

			_sharedTable = SharedTablePlugin.getSharedTable();
		}
	}

	/**
	 * 性能値を戻します。
	 * 
	 * @param  itemCode 収集項目コード
	 * @param  deviceIndex デバイス番号(現在は未使用)
	 * @param  deviceName デバイス名
	 * @return 性能値
	 */
	@Override
	public double calcValue(final CollectorItemInfo itemInfo) throws NotInitializedException{
		NodeDeviceInfo deviceData = null;

		// 収集項目から利用されるデバイス種別を特定
		String deviceType = CollectorMasterCache.getDeviceType(itemInfo.getItemCode());

		// デバッグ出力
		m_log.debug("DeviceType : " + deviceType);

		if(deviceType != null && !"".equals(deviceType)){
			HashMap<String, NodeDeviceInfo> deviceTypeMap = m_deviceMap.get(deviceType);

			// デバッグ出力
			if(m_log.isDebugEnabled()){
				Iterator<String> itr = deviceTypeMap.keySet().iterator();
				while(itr.hasNext()){
					String key = itr.next();
					m_log.debug(deviceTypeMap.get(key));
				}
			}

			if(deviceTypeMap != null){
				deviceData = deviceTypeMap.get(itemInfo.getDisplayName());
			}
		}

		double value = Double.NaN;
		if(currentTable != null && previousTable != null){
			value = CalculationMethod.getPerformance(platformId, subPlatformId, itemInfo, deviceData, currentTable, previousTable);
		} else {
			NotInitializedException e = new NotInitializedException("fetch error.");
			m_log.info("calcValue() currentTable = null and previousTable = null : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		m_log.debug("calcValue() : " + getFacilityId() + " " +
				itemInfo.getItemCode() + " " + itemInfo.getDisplayName() + "  " + value);

		// 性能値が算出できた場合はバッファに保存する
		if(!Double.isNaN(value)){
			setCalcValueBuffer(new CollectorItemInfo(
					itemInfo.getCollectorId(),
					itemInfo.getItemCode(),
					itemInfo.getDisplayName()), value);
		}

		return value;
	}


	/**
	 * 指定のmonitorIdのsharedTableを取得する
	 * 
	 * @param monitorId
	 * @return 最新のデータが取得できているか
	 */
	public boolean fetchSharedTable(String monitorId){
		m_log.debug("fetchSharedTable() monitorId = " + monitorId);

		boolean success = false;
		try{
			// 2枚のテーブル取得が可能か否か
			fetchMibValue(monitorId);
			success = true;
		}catch (NotInitializedException e) {
			// 2枚のテーブル取得がない場合にNotInitializedException発生。
			m_log.debug("fetchSharedTable()", e);
		}catch (DataTableNotFoundException e) {
			// データテーブルがない場合にDataTableNotFoundException発生。
			m_log.debug("fetchSharedTable()", e);
		}
		return success;
	}



	/**
	 * 
	 * @return
	 */
	public Long getLastCollectTime() {
		return lastCollectTime;
	}

	/**
	 * ポーラーから収集値を取得します。
	 * 
	 * @param oids 収集対象のOIDの配列
	 * @param interval 収集間隔
	 * @return 収集時刻（全てのノードで一致させるために最後に収集された値の収集時刻とする）
	 * @throws NotInitializedException
	 */
	private long fetchMibValue(String monitorId) throws NotInitializedException, DataTableNotFoundException {
		m_log.debug("fetchMibValue() monitorId = " + monitorId);

		try {
			if(_sharedTable == null){
				initialize();
			}

			// 2枚のテーブルを取得する間に更新される可能性があるためまとめて取得
			List<DataTable> tables = _sharedTable.getLastDataTables(
					HinemosModuleConstant.PERFORMANCE, getFacilityId(), monitorId, 2);

			this.currentTable = tables.get(0);
			this.previousTable = tables.get(1);

			this.lastCollectTime = currentTable.getLastModify();
		} catch (DataTableNotFoundException e) {
			String facilityId = getFacilityId();
			m_log.debug("create table : " + " monitorId = " + monitorId + ", facilityId = " + facilityId, e);

			// テーブルが存在しない場合は生成する
			_sharedTable = null;
			currentTable = null;
			previousTable = null;
			initialize();
			throw e;
		} catch (NotInitializedException e) {
			// 2回分のポーリングが実行できていない場合
			String facilityId = getFacilityId();
			m_log.debug(e.getMessage() + " monitorId = " + monitorId + ", facilityId = " + facilityId);
			_sharedTable = null;
			currentTable = null;
			previousTable = null;
			throw e;
		}

		m_log.debug("fetchMibValue() end :");
		return lastCollectTime;
	}

	/**
	 * 自分自身を返す。
	 * @return HashSet
	 */
	public HashSet<Facility> getNode(HashSet<Facility>  nodeSet){
		nodeSet.add(this);
		return nodeSet;
	}

	/**
	 * 自分自身をカウントして返す。
	 */
	@Override
	protected int getNodeCount(){
		return 1;
	}
}
