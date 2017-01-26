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

package com.clustercontrol.monitor.run.bean;

import javax.xml.bind.annotation.XmlType;

/**
 * 監視情報のチェック条件情報を保持する抽象クラス<BR>
 * <p>
 * 各監視管理クラスで継承してください。
 * jaxbで利用するため、引数なしのコンストラクタが必要。
 * そのため、abstractにしない。
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class MonitorCheckInfo
implements java.io.Serializable
{

	private static final long serialVersionUID = 2945451890219694984L;

	/** 監視項目ID */
	private java.lang.String m_monitorId;

	/** 監視対象ID */
	private java.lang.String m_monitorTypeId;

	/**
	 * コンストラクタ。
	 */
	public MonitorCheckInfo(){
	}

	/**
	 * 監視項目IDを返します。
	 * 
	 * @return 監視項目ID
	 */
	public java.lang.String getMonitorId(){
		return this.m_monitorId;
	}

	/**
	 * 監視項目IDを設定します。
	 * 
	 * @param monitorId 監視項目ID
	 */
	public void setMonitorId( java.lang.String monitorId ){
		this.m_monitorId = monitorId;
	}

	/**
	 * 監視対象IDを返します。
	 * 
	 * @return 監視対象ID
	 */
	public java.lang.String getMonitorTypeId(){
		return this.m_monitorTypeId;
	}

	/**
	 * 監視対象IDを設定します。
	 * 
	 * @param monitorTypeId 監視対象ID
	 */
	public void setMonitorTypeId( java.lang.String monitorTypeId ){
		this.m_monitorTypeId = monitorTypeId;
	}
}
