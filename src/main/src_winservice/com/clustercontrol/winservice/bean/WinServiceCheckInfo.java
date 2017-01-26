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

package com.clustercontrol.winservice.bean;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.bean.MonitorCheckInfo;

/**
 * Windowsサービス監視設定情報のBeanクラス<BR>
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class WinServiceCheckInfo extends MonitorCheckInfo
{
	private static final long serialVersionUID = -4302206236474775644L;
	private java.lang.String serviceName = "";

	public WinServiceCheckInfo()
	{
	}
	/**
	 * Windowsサービス名を取得します。
	 * @return
	 */
	public java.lang.String getServiceName()
	{
		return this.serviceName;
	}
	/**
	 * Windowsサービス名を設定します。
	 * @param serviceName
	 */
	public void setServiceName( java.lang.String serviceName )
	{
		this.serviceName = serviceName;
	}
}
