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
package com.clustercontrol.performance.monitor.bean;

import com.clustercontrol.bean.YesNoConstant;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.bean.MonitorCheckInfo;

/**
 * リソース監視情報のBeanクラス<BR>
 * 
 * @version 4.0.0
 * @since 2.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class PerfCheckInfo extends MonitorCheckInfo
{
	private static final long serialVersionUID = -2603517398821937560L;

	// 収集項目ID
	private java.lang.String itemCode;
	// デバイス表示名
	private java.lang.String deviceDisplayName;
	// 収集時に内訳のデータも収集するかのフラグ
	private java.lang.Integer breakdownFlg;

	public PerfCheckInfo()
	{
	}

	/**
	 * リポジトリ表示名を取得します。
	 * @return リポジトリ表示名
	 */
	public java.lang.String getDeviceDisplayName() {
		return deviceDisplayName;
	}

	/**
	 * リポジトリ表示名を設定します。
	 * @param deviceName リポジトリ表示名
	 */
	public void setDeviceDisplayName(java.lang.String deviceDisplayName) {
		this.deviceDisplayName = deviceDisplayName;
	}

	/**
	 * 収集項目コードを取得します。
	 * @return 収集項目コード
	 */
	public java.lang.String getItemCode() {
		return itemCode;
	}

	/**
	 * 収集項目コードを設定します。
	 * @param itemCode 収集項目コード
	 */
	public void setItemCode(java.lang.String itemCode) {
		this.itemCode = itemCode;
	}

	/**
	 * 内訳を取得するかのフラグを取得する。
	 * @return 内訳を取得するかのフラグ
	 */
	public java.lang.Integer getBreakdownFlg() {
		return breakdownFlg;
	}

	/**
	 * 内訳を取得するかのフラグを設定する。
	 * @param breakdownFlg 内訳を取得するかのフラグ
	 */
	public void setBreakdownFlg(java.lang.Integer breakdownFlg) {
		this.breakdownFlg = breakdownFlg;
	}

	/**
	 * 内訳を収集するか?
	 * @return
	 */
	public boolean isBreakdown(){
		if(this.breakdownFlg == null){
			return false;
		}

		switch (this.breakdownFlg.intValue()) {
		case YesNoConstant.TYPE_YES:
			return true;

		case YesNoConstant.TYPE_NO:
			return false;

		default:
			return false;
		}
	}
}
