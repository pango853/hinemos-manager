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

package com.clustercontrol.performance.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.bind.annotation.XmlType;

/**
 * 監視結果を性能値のセットとして保持するクラス<BR>
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
@XmlType(namespace = "http://collector.ws.clustercontrol.com")
public class Sample implements Serializable {

	private static final long serialVersionUID = 1457415635709692572L;

	private String collectorId = null;
	private Date dateTime = null;
	private ArrayList<PerfData> perfDataList = null;

	/**
	 * コンストラクタ
	 * @param dateTime
	 * @param collectorId
	 */
	public Sample(String collectorId, Date dateTime) {
		super();
		this.dateTime = dateTime;
		this.collectorId = collectorId;
	}

	/**
	 * 性能値のセット
	 * @param facilityId
	 * @param itemCode
	 * @param displayName
	 * @param value
	 * @param errorType	//値が不正な場合のエラーパターン
	 */
	public void set(String facilityId, String itemCode,String displayName,double value,Integer errorType){

		if(perfDataList == null)
			perfDataList = new ArrayList<PerfData>();

		perfDataList.add(new PerfData(facilityId, itemCode, displayName, value, errorType));
	}

	/**
	 * 収集時刻
	 * @return
	 */
	public Date getDateTime() {
		return dateTime;
	}

	/**
	 * 収集ID(=監視ID)
	 * @return
	 */
	public String getCollectorId() {
		return collectorId;
	}

	/**
	 * 性能情報のリスト
	 * @return
	 */
	public ArrayList<PerfData> getPerfDataList() {
		if(perfDataList == null)
			perfDataList = new ArrayList<PerfData>();

		return perfDataList;
	}
}
