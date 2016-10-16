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
 * このクラスは、リポジトリプロパティ[デバイス]のクラスです。
 * NodeDataクラスのメンバ変数として利用されます。
 * @since 0.8
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class NodeDeviceInfo implements Serializable, Cloneable
{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -8539796929718344077L;


	/** メンバ変数 */
	private java.lang.String deviceType = "";
	private java.lang.String deviceDisplayName = "";
	private java.lang.Integer deviceIndex = new Integer(-1);
	private java.lang.String deviceName = "";
	private java.lang.Integer deviceSize = new Integer(0);
	private java.lang.String deviceSizeUnit = "";
	private java.lang.String deviceDescription = "";

	/**
	 * 空のコンストラクタです。setterで要素を追加して下さい。
	 */
	public NodeDeviceInfo()
	{
	}


	/**
	 * コンストラクトする際に、値をセットする事ができます。
	 * @param deviceType
	 * @param deviceIndex
	 * @param deviceName
	 * @param deviceDisplayName
	 * @param deviceSize
	 * @param deviceSizeUnit
	 * @param deviceDescription
	 */
	public NodeDeviceInfo(
			java.lang.String deviceType,
			java.lang.Integer deviceIndex,
			java.lang.String deviceName,
			java.lang.String deviceDisplayName,
			java.lang.Integer deviceSize,
			java.lang.String deviceSizeUnit,
			java.lang.String deviceDescription )
	{
		setDeviceType(deviceType);
		setDeviceIndex(deviceIndex);
		setDeviceName(deviceName);
		setDeviceDisplayName(deviceDisplayName);
		setDeviceSize(deviceSize);
		setDeviceSizeUnit(deviceSizeUnit);
		setDeviceDescription(deviceDescription);
	}

	/**
	 * NodeDeviceDataインスタンスのコピーを生成する時に利用します。
	 * @param otherData
	 */
	public NodeDeviceInfo( NodeDeviceInfo otherData )
	{
		setDeviceType(otherData.getDeviceType());
		setDeviceIndex(otherData.getDeviceIndex());
		setDeviceName(otherData.getDeviceName());
		setDeviceDisplayName(otherData.getDeviceDisplayName());
		setDeviceSize(otherData.getDeviceSize());
		setDeviceSizeUnit(otherData.getDeviceSizeUnit());
		setDeviceDescription(otherData.getDeviceDescription());

	}

	/**
	 * DeviceTypeのgetterです。
	 * @return String
	 */
	public java.lang.String getDeviceType()
	{
		return this.deviceType;
	}

	/**
	 * デバイス種別のsetterです。
	 * デバイス種別はnot nullです。disk, nic等をセットします。
	 * @param deviceType
	 */
	public void setDeviceType( java.lang.String deviceType )
	{
		this.deviceType = deviceType;
	}

	/**
	 * デバイスINDEXのgetterです。
	 * @return Integer
	 */
	public java.lang.Integer getDeviceIndex()
	{
		return this.deviceIndex;
	}

	/**
	 * デバイスINDEXのsetterです。デバイスINDEXはnot nullです。
	 * @param deviceIndex
	 */
	public void setDeviceIndex( java.lang.Integer deviceIndex )
	{
		this.deviceIndex = deviceIndex;
	}

	/**
	 * デバイス名のgetterです。
	 * @return String
	 */
	public java.lang.String getDeviceName()
	{
		return this.deviceName;
	}

	/**
	 * デバイス名のsetterです。デバイス名はnot nullです。
	 * @param deviceName
	 */
	public void setDeviceName( java.lang.String deviceName )
	{
		this.deviceName = deviceName;
	}

	/**
	 * デバイス表示名のgetterです。
	 * @return String
	 */
	public java.lang.String getDeviceDisplayName()
	{
		return this.deviceDisplayName;
	}

	/**
	 * デバイス表示名のsetterです。デバイス表示名はnot nullです。
	 * @param deviceDisplayName
	 */
	public void setDeviceDisplayName( java.lang.String deviceDisplayName )
	{
		this.deviceDisplayName = deviceDisplayName;
	}


	/**
	 * デバイスサイズの取得
	 * @return Integer
	 */
	public java.lang.Integer getDeviceSize() {
		return this.deviceSize;
	}


	/**
	 * デバイスサイズの設定
	 * @param deviceSize
	 */
	public void setDeviceSize(java.lang.Integer deviceSize) {
		this.deviceSize = deviceSize;
	}

	/**
	 * デバイスサイズの単位の取得
	 * @return String
	 */
	public java.lang.String getDeviceSizeUnit() {
		return this.deviceSizeUnit;
	}

	/**
	 * デバイスサイズの単位の設定
	 * @param deviceSizeUnit
	 */
	public void setDeviceSizeUnit(java.lang.String deviceSizeUnit) {
		this.deviceSizeUnit = deviceSizeUnit;
	}

	/**
	 * デバイス説明のsetterです。
	 * @return String
	 */
	public java.lang.String getDeviceDescription()
	{
		return this.deviceDescription;
	}

	/**
	 * デバイス説明のgetterです。
	 * @param deviceDescription
	 */
	public void setDeviceDescription( java.lang.String deviceDescription )
	{
		this.deviceDescription = deviceDescription;
	}

	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer("{");

		str.append(
				"deviceType=" + getDeviceType() + " " +
						"deviceIndex=" + getDeviceIndex() + " " +
						"deviceName=" + getDeviceName() + " " +
						"deviceDisplayName=" + getDeviceDisplayName() + " " +
						"seviceSize=" + getDeviceSize() + " " +
						"seviceSizeUnit=" + getDeviceSizeUnit() + " " +
						"deviceDescription=" + getDeviceDescription());
		str.append('}');

		return(str.toString());
	}

	@Override
	public boolean equals( Object pOther )
	{
		if( pOther instanceof NodeDeviceInfo )
		{
			NodeDeviceInfo lTest = (NodeDeviceInfo) pOther;
			boolean lEquals = true;

			if( this.deviceType == null )
			{
				lEquals = lEquals && ( lTest.deviceType == null );
			}
			else
			{
				lEquals = lEquals && this.deviceType.equals( lTest.deviceType );
			}
			if( this.deviceIndex == null )
			{
				lEquals = lEquals && ( lTest.deviceIndex == null );
			}
			else
			{
				lEquals = lEquals && this.deviceIndex.equals( lTest.deviceIndex );
			}
			if( this.deviceName == null )
			{
				lEquals = lEquals && ( lTest.deviceName == null );
			}
			else
			{
				lEquals = lEquals && this.deviceName.equals( lTest.deviceName );
			}
			if( this.deviceDisplayName == null )
			{
				lEquals = lEquals && ( lTest.deviceDisplayName == null );
			}
			else
			{
				lEquals = lEquals && this.deviceDisplayName.equals( lTest.deviceDisplayName );
			}
			if( this.deviceSize == null )
			{
				lEquals = lEquals && ( lTest.deviceSize == null );
			}
			else
			{
				lEquals = lEquals && this.deviceSize.equals( lTest.deviceSize );
			}
			if( this.deviceSizeUnit == null )
			{
				lEquals = lEquals && ( lTest.deviceSizeUnit == null );
			}
			else
			{
				lEquals = lEquals && this.deviceSizeUnit.equals( lTest.deviceSizeUnit );
			}
			if( this.deviceDescription == null )
			{
				lEquals = lEquals && ( lTest.deviceDescription == null );
			}
			else
			{
				lEquals = lEquals && this.deviceDescription.equals( lTest.deviceDescription );
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

		result = 37*result + ((this.deviceType != null) ? this.deviceType.hashCode() : 0);

		result = 37*result + ((this.deviceIndex != null) ? this.deviceIndex.hashCode() : 0);

		result = 37*result + ((this.deviceName != null) ? this.deviceName.hashCode() : 0);

		result = 37*result + ((this.deviceDisplayName != null) ? this.deviceDisplayName.hashCode() : 0);

		result = 37*result + ((this.deviceSize != null) ? this.deviceSize.hashCode() : 0);

		result = 37*result + ((this.deviceSizeUnit != null) ? this.deviceSizeUnit.hashCode() : 0);

		result = 37*result + ((this.deviceDescription != null) ? this.deviceDescription.hashCode() : 0);

		return result;
	}

	@Override
	public NodeDeviceInfo clone() {
		NodeDeviceInfo cloneInfo = new NodeDeviceInfo();

		try {
			cloneInfo = (NodeDeviceInfo)super.clone();
			cloneInfo.deviceType = this.deviceType;
			cloneInfo.deviceDisplayName = this.deviceDisplayName;
			cloneInfo.deviceIndex = this.deviceIndex;
			cloneInfo.deviceName = this.deviceName;
			cloneInfo.deviceSize = this.deviceSize;
			cloneInfo.deviceSizeUnit = this.deviceSizeUnit;
			cloneInfo.deviceDescription = this.deviceDescription;
		} catch (CloneNotSupportedException e) {
			// do nothing
		}

		return cloneInfo;
	}
}
