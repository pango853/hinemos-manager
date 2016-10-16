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
import java.util.HashSet;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.performance.monitor.entity.CollectorScopeSnapData;
import com.clustercontrol.poller.NotInitializedException;


/**
 * 各ファシリティから再帰的に性能値の算出をするのに必要なメソッドを定義したアブストラクトクラス
 * 
 * @version 1.0
 * @since 1.0
 */
@XmlType(namespace = "http://collector.ws.clustercontrol.com")
public abstract class Facility extends CollectorScopeSnapData implements Serializable, Comparable<Facility> {

	private static final long serialVersionUID = -6422403780813208737L;

	/** 種別が「ノード」であることを表現する定数 */
	public static final String NODE = "node";

	private HashSet<Facility> m_parents = null;
	private HashMap<CollectorItemInfo, Double> m_calcTempValues;

	public Facility(String facilityID, String facilityName, String type) {
		setFacilityId(facilityID);
		setFacilityName(facilityName);
		setFacilityType(type);
		m_parents = new HashSet<Facility>();
		m_calcTempValues = new HashMap<CollectorItemInfo, Double>();
	}

	/**
	 * 親要素を追加します。
	 * @param parent 親要素
	 */
	public void addParents(Facility parent){

		this.getParents().add(parent);
	}

	/**
	 *  このファシリティの以下のスコープに含まれるノードの数を返します。
	 * @return ノードの数
	 */
	protected int getNodeCount(){
		return 0;
	}

	/**
	 * 計算済み性能値を返します。
	 * @return 計算済み性能値
	 */
	abstract public double calcValue(final CollectorItemInfo itemInfo) throws NotInitializedException;

	/**
	 * 親のファシリティを取得します。
	 * @return Parents 親のファシリティ
	 */
	public HashSet<Facility> getParents() {
		return m_parents;
	}

	/**
	 * このファシリティと指定されたファシリティの順序を比較します。
	 * @param f 比較対象のファシリティ
	 * @return このオブジェクトが指定されたオブジェクトより小さい場合は負の整数、
	 *         等しい場合はゼロ、大きい場合は正の整数
	 */
	@Override
	public int compareTo(Facility f){
		return getFacilityId().compareTo(f.getFacilityId());
	}

	/**
	 * バッファに保持されている前回収集時の性能値を取得する。
	 * 
	 * @param item 収集項目PK
	 * @return 前回収集時の性能値
	 */
	public double getCalcValueBuffer(CollectorItemInfo item){
		Double value = m_calcTempValues.get(item);

		if(value == null){
			return Double.NaN;
		} else {
			return value.doubleValue();
		}
	}

	/**
	 * 性能値をバッファとして保持する。
	 * 
	 * @param item 収集項目PK
	 * @return 前回収集時の性能値
	 */
	public void setCalcValueBuffer(CollectorItemInfo item, double data){
		m_calcTempValues.put(item, new Double(data));
	}
}
