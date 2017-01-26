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

package com.clustercontrol.ping.bean;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.bean.MonitorCheckInfo;

/**
 * ping監視設定情報のBeanクラス<BR>
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class PingCheckInfo extends MonitorCheckInfo
{
	private static final long serialVersionUID = 7072152458225855619L;
	/** 実行回数 */
	private java.lang.Integer runCount;
	/** 実行間隔（秒） */
	private java.lang.Integer runInterval;
	/** タイムアウト（ミリ秒） */
	private java.lang.Integer timeout;


	public PingCheckInfo()
	{
	}

	/**
	 * 実行回数（一回のチェックで何個パケットを投げるか？）を取得します。
	 *
	 */
	public java.lang.Integer getRunCount()
	{
		return this.runCount;
	}
	/**
	 * 実行回数（一回のチェックで何個パケットを投げるか？）をセットします。
	 * @param runCount
	 */
	public void setRunCount( java.lang.Integer runCount )
	{
		this.runCount = runCount;
	}

	/**
	 * 実行間隔（秒）を取得します。
	 * 
	 */
	public java.lang.Integer getRunInterval()
	{
		return this.runInterval;
	}
	/**
	 * 実行間隔（秒）をセットします。
	 * @param runInterval
	 */
	public void setRunInterval( java.lang.Integer runInterval )
	{
		this.runInterval = runInterval;
	}
	/**
	 * タイムアウト（ミリ秒）を取得します。
	 * @return
	 */
	public java.lang.Integer getTimeout()
	{
		return this.timeout;
	}
	/**
	 * タイムアウト（ミリ秒）をセットします。
	 * @param timeout
	 */
	public void setTimeout( java.lang.Integer timeout )
	{
		this.timeout = timeout;
	}
}
