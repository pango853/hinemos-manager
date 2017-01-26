/*

Copyright (C) since 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.performance.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.repository.bean.FacilityTreeItem;

/**
 * 
 * グラフ描画/性能実績データのエクスポートに必要なヘッダ情報を保持するクラス。
 * 実際の性能データは含まない。
 * 
 * @version 4.0.0
 * @since 4.0.0
 *
 */
@XmlType(namespace = "http://collector.ws.clustercontrol.com")
public class PerformanceDataSettings implements Serializable {

	private static final long serialVersionUID = 7091478303814119387L;

	/** 収集が有効か */
	private Integer status = -1;

	/** 監視ID */
	private String monitorId = null;

	/** 収集対象のファシリティID */
	private String facilityId = null;

	/** 収集対象のファシリティIDをトップとするツリー */
	private FacilityTreeItem facilityTreeItem = null;

	/** 収集対象のファシリティIDリスト */
	private ArrayList<String> targetFacilityIdList = null;

	/** 監視種別(プラグインID)*/
	private String monitorTypeId = null;

	/** 監視間隔 */
	private Integer interval = -1;

	/** 監視対象の収集ID */
	private String targetItemCode = null; // 監視設定に対して1つだけ指定。リソース監視はサブ項目も監視できるためitemCodeListが必要。

	/** 監視対象のデバイス */
	private String targetDisplayName = null; // リソース監視は特定のデバイスの監視を行う場合がある

	/** 収集名マップ */
	private HashMap<String, String> itemNameMap = null; // 複数存在するのはリソース監視のみ

	/** 収集している収集項目IDのリスト(親となる収集項目IDも含む) */
	private ArrayList<CollectorItemParentInfo> itemCodeList = null;

	/** 収集単位 */
	private String measure = null;

	/**
	 * 最新の収集データの日付時刻
	 */
	private Long latestDate = null;

	/**
	 * 最古の収集データの日付時刻
	 */
	private Long oldestDate = null;

	/**
	 * デフォルトコンストラクタ
	 */
	public PerformanceDataSettings() {
	}

	/* setter/getter */


	public Integer getStatus() {
		return status;
	}

	public ArrayList<CollectorItemParentInfo> getItemCodeList() {
		return itemCodeList;
	}

	public void setItemCodeList(ArrayList<CollectorItemParentInfo> itemCodeList) {
		this.itemCodeList = itemCodeList;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}


	public String getMonitorTypeId() {
		return monitorTypeId;
	}

	public void setMonitorTypeId(String monitorTypeId) {
		this.monitorTypeId = monitorTypeId;
	}

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}


	public HashMap<String, String> getItemNameMap() {
		return itemNameMap;
	}

	public void setItemNameMap(HashMap<String, String> itemNameMap) {
		this.itemNameMap = itemNameMap;
	}

	public String getTargetItemCode() {
		return targetItemCode;
	}

	public void setTargetItemCode(String targetItemCode) {
		this.targetItemCode = targetItemCode;
	}

	public String getTargetDisplayName() {
		return targetDisplayName;
	}

	public void setTargetDisplayName(String targetDisplayName) {
		this.targetDisplayName = targetDisplayName;
	}


	public String getMeasure() {
		return measure;
	}
	public void setMeasure(String measure) {
		this.measure = measure;
	}

	/**
	 * 指定した収集項目ID(ItemCode)に該当する収集項目名を取得する
	 * @param itemCode
	 * @return
	 */
	public String getItemName(String itemCode) {
		String itemName = null;
		if (itemNameMap != null){
			itemName = itemNameMap.get(itemCode);
		}
		return itemName;
	}

	/**
	 * 指定した収集項目ID(ItemCode)に該当する収集項目名を設定する
	 * @param itemCode
	 * @param itemName
	 */
	public void setItemName(String itemCode, String itemName) {
		if(itemNameMap == null){
			itemNameMap = new HashMap<String, String>();
		}
		itemNameMap.put(itemCode, itemName);
	}

	/**
	 * 最新の収集データの日付時刻を返します。
	 * 
	 * facilityIdがスコープである場合、監視項目IDをキーとして最新の収集データの日付時刻を返します。
	 * facilityIdがスコープである場合、監視項目IDとfacilityIdをキーとして最新の収集データの日付時刻を返します。
	 * 
	 * @return
	 */
	public Long getLatestDate() {
		return latestDate;
	}

	public void setLatestDate(Long latestDate) {
		this.latestDate = latestDate;
	}

	/**
	 * 最古の収集データの日付時刻を返します。
	 * 
	 * facilityIdがスコープである場合、監視項目IDをキーとして最古の収集データの日付時刻を返します。
	 * facilityIdがスコープである場合、監視項目IDとfacilityIdをキーとして最古の収集データの日付時刻を返します。
	 * 
	 * @return
	 */
	public Long getOldestDate() {
		return oldestDate;
	}

	public void setOldestDate(Long oldestDate) {
		this.oldestDate = oldestDate;
	}

	public void setFacilityTreeItem(FacilityTreeItem treeItem) {
		this.facilityTreeItem = treeItem;
	}

	public FacilityTreeItem getFacilityTreeItem() {
		return facilityTreeItem;
	}

	public ArrayList<String> getTargetFacilityIdList() {
		return targetFacilityIdList;
	}

	public void setTargetFacilityIdList(ArrayList<String> targetFacilityIdList) {
		this.targetFacilityIdList = targetFacilityIdList;
	}

}
