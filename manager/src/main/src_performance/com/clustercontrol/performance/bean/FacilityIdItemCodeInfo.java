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

import javax.xml.bind.annotation.XmlType;

/**
 * ファシリティIDと収集項目を保持するクラス
 */
@XmlType(namespace = "http://collector.ws.clustercontrol.com")
public class FacilityIdItemCodeInfo implements Serializable {
	private static final long serialVersionUID = 3065258340736998660L;
	private String facilityId = null;
	private CollectorItemInfo itemInfo= null;

	public FacilityIdItemCodeInfo(){}

	public FacilityIdItemCodeInfo(String facilityId, CollectorItemInfo itemInfo){
		setFacilityId(facilityId);
		setItemInfo(itemInfo);
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public CollectorItemInfo getItemInfo() {
		return itemInfo;
	}

	public void setItemInfo(CollectorItemInfo itemInfo) {
		this.itemInfo = itemInfo;
	}


	/**
	 * このオブジェクトと他のオブジェクトが等しいかどうかを示します。
	 */
	@Override
	public boolean equals( Object pOther )
	{
		if( pOther instanceof FacilityIdItemCodeInfo )
		{
			FacilityIdItemCodeInfo lTest = (FacilityIdItemCodeInfo) pOther;
			boolean lEquals = true;

			if( this.facilityId == null )
			{
				lEquals = lEquals && ( lTest.facilityId == null );
			}
			else
			{
				lEquals = lEquals && this.facilityId.equals( lTest.facilityId );
			}

			if( this.itemInfo == null )
			{
				lEquals = lEquals && ( lTest.itemInfo == null );
			}
			else
			{
				lEquals = lEquals && this.itemInfo.equals( lTest.itemInfo );
			}

			return lEquals;
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37*result + ((this.facilityId != null) ? this.facilityId.hashCode() : 0);

		result = 37*result + ((this.itemInfo != null) ? this.itemInfo.hashCode() : 0);

		return result;
	}
}
