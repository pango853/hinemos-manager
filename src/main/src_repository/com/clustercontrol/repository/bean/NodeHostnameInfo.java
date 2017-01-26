/*

Copyright (C) since 2009 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.repository.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * このクラスは、リポジトリプロパティ[ホスト名]のクラスです。
 * NodeDataクラスのメンバ変数として利用されます。
 * @since 0.8
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class NodeHostnameInfo implements Serializable, Cloneable
{

	private static final long serialVersionUID = -6789634530851364708L;
	private java.lang.String hostname = "";

	/**
	 * 空のコンストラクタです。setterで要素を追加して下さい。
	 */
	public NodeHostnameInfo()
	{
	}


	/**
	 * コンストラクトする際に、値をセットする事ができます。
	 * @param hostname
	 */
	public NodeHostnameInfo(java.lang.String hostname )
	{
		setHostname(hostname);
	}

	/**
	 * NodeHostnameDataインスタンスのコピーを生成する時に利用します。
	 * @param otherData
	 */
	public NodeHostnameInfo( NodeHostnameInfo otherData )
	{
		setHostname(otherData.getHostname());
	}

	/**
	 * ホスト名のgetterです。
	 * @return String
	 */
	public java.lang.String getHostname()
	{
		return this.hostname;
	}

	/**
	 * ホスト名のsetterです。
	 * not nullです。
	 * @param hostname
	 */
	public void setHostname( java.lang.String hostname )
	{
		this.hostname = hostname;
	}

	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer("{");

		str.append("hostname=" + getHostname());
		str.append('}');

		return(str.toString());
	}

	@Override
	public boolean equals( Object pOther )
	{
		if( pOther instanceof NodeHostnameInfo )
		{
			NodeHostnameInfo lTest = (NodeHostnameInfo) pOther;
			boolean lEquals = true;

			if( this.hostname == null )
			{
				lEquals = lEquals && ( lTest.hostname == null );
			}
			else
			{
				lEquals = lEquals && this.hostname.equals( lTest.hostname );
			}

			return lEquals;
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		int result = 17;

		result = 37*result + ((this.hostname != null) ? this.hostname.hashCode() : 0);

		return result;
	}

	@Override
	public NodeHostnameInfo clone() {

		NodeHostnameInfo cloneInfo = new NodeHostnameInfo();
		try {
			cloneInfo = (NodeHostnameInfo)super.clone();
			cloneInfo.hostname = this.hostname;
		} catch (CloneNotSupportedException e) {
			//do nothing
		}

		return cloneInfo;
	}
}
