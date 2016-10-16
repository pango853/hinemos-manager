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

import javax.xml.bind.annotation.XmlType;

/**
 * 監視結果を性能値を保持するクラス<BR>
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
@XmlType(namespace = "http://collector.ws.clustercontrol.com")
public class PerfData implements Serializable {

	private static final long serialVersionUID = -9053625437686726677L;

	private final String facilityId;    //ファシリティID
	private final String itemCode;      //収集項目コード
	private final String displayName;    //リポジトリ表示名
	private final double value;         //性能値
	private final int errorType;        //値が不正な場合のエラーパターン

	/**
	 * コンストラクタ
	 * @param itemCode
	 * @param displayName
	 * @param facilityId
	 * @param value
	 * @param errorType
	 */
	public PerfData(String facilityId, String itemCode, String displayName,
			double value, int errorType) {
		super();

		this.facilityId = facilityId;
		this.itemCode = itemCode;
		this.displayName = displayName;
		this.value = value;
		this.errorType = errorType;
	}

	/**
	 * 
	 * @return
	 */
	public String getItemCode() {
		return itemCode;
	}

	/**
	 * 
	 * @return
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * 
	 * @return
	 */
	public String getFacilityId() {
		return facilityId;
	}

	/**
	 * 
	 * @return
	 */
	public double getValue() {
		return value;
	}

	/**
	 * 
	 * @return
	 */
	public int getErrorType() {
		return errorType;
	}
}
