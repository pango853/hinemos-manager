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

import java.io.Serializable;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.performance.bean.CollectedDataSet;

/**
 * 複数の収集済み性能値のリスト（CollectedDataInfo型のリスト）をまとめて保持するクラス
 * ファシリティIDと収集項目の２つをキーとして、収集済み性能値のリストを取得することが出来ます。
 * @version 1.0
 * @since 1.0
 */
@XmlType(namespace = "http://collector.ws.clustercontrol.com")
public class CollectedDataSet implements Serializable {
	private static final long serialVersionUID = -2016025252868760081L;
	private HashMap<FacilityIdItemCodeInfo, CollectedDataList> dataMap;

	/**
	 * コンストラクター
	 *
	 */
	public CollectedDataSet(){
		dataMap = new HashMap<FacilityIdItemCodeInfo, CollectedDataList>();
	}

	/**
	 * 指定のファシリティIDと収集項目IDをキーに収集済み性能値データのリストを登録します。
	 * 
	 * @param facilityID ファシリティID
	 * @param itemCode 収集項目コード
	 * @param data 性能値データのリスト
	 */
	public void setCollectedDataList(String facilityID, CollectorItemInfo collectorItemInfo, CollectedDataList data){
		FacilityIdItemCodeInfo keyInfo = new FacilityIdItemCodeInfo(facilityID, collectorItemInfo);

		CollectedDataList colArray = new CollectedDataList();
		for (int i = 0; i < data.size(); i++) {
			colArray.add(data.get(i));
		}
		dataMap.put(keyInfo, colArray);
	}

	/**
	 * 現在の性能値データのセットに、
	 * 指定のCollectedDataSetの収集済み性能値データを追加します。
	 * 同じ facilityID, itemCode のデータがある場合は上書きされます。
	 * 
	 * @param dataSet 追加する性能値データ
	 */
	public void addCollectedDataList(CollectedDataSet dataSet){
		for(FacilityIdItemCodeInfo keyInfo : dataSet.dataMap.keySet()){
			// 移し替えもとのデータを取得する
			CollectedDataList dataList = dataSet.getCollectedDataList(keyInfo.getFacilityId(), keyInfo.getItemInfo());

			// データをセットする
			setCollectedDataList(keyInfo.getFacilityId(), keyInfo.getItemInfo(), dataList);
		}
	}

	/**
	 * 指定のファシリティIDと収集項目IDをキーに収集済み性能値情報のリストを取得します。
	 * 
	 * @param facilityID ファシリティID
	 * @param itemCode 収集項目コード
	 * @return 性能値データのリスト
	 */
	public CollectedDataList getCollectedDataList(String facilityID, CollectorItemInfo key){
		FacilityIdItemCodeInfo keyInfo = new FacilityIdItemCodeInfo(facilityID, key);

		if(dataMap == null || dataMap.get(keyInfo) == null){
			// 空のリストを生成して返す
			return new CollectedDataList();
		}

		CollectedDataList dataList = dataMap.get(keyInfo);
		if(dataList != null){
			return dataList;
		} else {
			// 空のリストを生成して返す
			return new CollectedDataList();
		}
	}

	/**
	 * 指定のファシリティIDで登録されている収集済みデータリストの数を取得します。
	 * 
	 * @param facilityID ファシリティID
	 * @return 指定のファシリティIDで登録されている収集済みデータリストの数
	 */
	public int getDataListNum(String facilityID){
		int num = 0;
		for(FacilityIdItemCodeInfo keyInfo : dataMap.keySet()){
			if(facilityID.equals(keyInfo.getFacilityId())){
				num++;
			}
		}
		return num;
	}

	// webサービス(jaxb)のためgetterとsetterを用意しておく
	@Deprecated
	public HashMap<FacilityIdItemCodeInfo, CollectedDataList> getDataMap() {
		return dataMap;
	}

	@Deprecated
	public void setDataMap(
			HashMap<FacilityIdItemCodeInfo, CollectedDataList> dataMap) {
		this.dataMap = dataMap;
	}
}
