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

package com.clustercontrol.snmp.bean;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.monitor.run.bean.MonitorCheckInfo;

/**
 * SNMP監視定義情報のBeanクラス<BR>
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class SnmpCheckInfo extends MonitorCheckInfo
{

	private static final long serialVersionUID = 2473546630003987318L;
	private java.lang.String communityName;
	private java.lang.Integer convertFlg = Integer.valueOf(ConvertValueConstant.TYPE_DELTA);
	private java.lang.String snmpOid;
	private java.lang.Integer snmpPort;
	private java.lang.String snmpVersion = SnmpVersionConstant.STRING_V1;

	public SnmpCheckInfo()
	{
	}
	/**
	 *コミュニティ名を取得します。<BR>
	 * @return コミュニティ名
	 */
	public java.lang.String getCommunityName()
	{
		return this.communityName;
	}
	/**
	 *コミュニティ名を設定します。
	 */
	public void setCommunityName( java.lang.String communityName )
	{
		this.communityName = communityName;
	}
	/**
	 * ConvertFlg(そのまま、差をとる)を取得します。
	 * @return ConvertFlg
	 * @see com.clustercontrol.bean.ConvertValueConstant
	 */
	public java.lang.Integer getConvertFlg()
	{
		return this.convertFlg;
	}
	/**
	 * ConvertFlg(そのまま、差をとる)を設定します。
	 * @param convertFlg
	 * @see com.clustercontrol.bean.ConvertValueConstant
	 */
	public void setConvertFlg( java.lang.Integer convertFlg )
	{
		this.convertFlg = convertFlg;
	}
	/**
	 * 監視対象のOIDを取得します。<BR>
	 * @return　OID
	 */
	public java.lang.String getSnmpOid()
	{
		return this.snmpOid;
	}
	/**
	 * 監視対象のOIDを設定します。<BR>
	 * @param snmpOid
	 */
	public void setSnmpOid( java.lang.String snmpOid )
	{
		this.snmpOid = snmpOid;
	}
	/**
	 * SMNPのポートを取得します。<BR>
	 * @return ポート番号
	 */
	public java.lang.Integer getSnmpPort()
	{
		return this.snmpPort;
	}
	/**
	 * SNMPのポートを設定します。<BR>
	 * @param snmpPort
	 */
	public void setSnmpPort( java.lang.Integer snmpPort )
	{
		this.snmpPort = snmpPort;
	}
	/**
	 * SNMPのバージョンを取得します。<BR>
	 * @return
	 */
	public java.lang.String getSnmpVersion()
	{
		return this.snmpVersion;
	}
	/**
	 * SNMPバージョンを設定します。<BR>
	 * @param snmpVersion
	 */
	public void setSnmpVersion( java.lang.String snmpVersion )
	{
		this.snmpVersion = snmpVersion;
	}
}
