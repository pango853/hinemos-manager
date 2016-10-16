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
 * このクラスはスコープ詳細のクラスです。
 * スコープの詳細を保持するために使用されます。
 * @since 0.8
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class ScopeInfo extends FacilityInfo implements Serializable
{

	private static final long serialVersionUID = 6977438041410019813L;

	public ScopeInfo() {
		super();
		setFacilityType(FacilityConstant.TYPE_SCOPE);
	}

	/**
	 * コンストラクトする際に、値をセットする事ができます。
	 * @param facilityId
	 * @param facilityName
	 * @param facilityType
	 * @param description
	 * @param displaySortOrder
	 * @param valid
	 * @param createUserId
	 * @param createDatetime
	 * @param modifyUserId
	 * @param modifyDatetime
	 * @param builtInFlg
	 */
	public ScopeInfo( String facilityId, String facilityName, Integer facilityType, String description, Integer displaySortOrder, String iconImage, Boolean valid, Boolean autoDeviceSearch, String createUserId, Long createDatetime, String modifyUserId, Long modifyDatetime, Boolean builtInFlg, String ownerRoleId, Boolean notReferFlg )
	{
		super(facilityId, facilityName, facilityType, description, displaySortOrder, iconImage,
				valid, autoDeviceSearch, createUserId, createDatetime, modifyUserId, modifyDatetime, builtInFlg, ownerRoleId, notReferFlg);
	}

	/**
	 * ScopeDataインスタンスのコピーを生成する時に利用します。
	 * @param otherData
	 */
	public ScopeInfo( ScopeInfo otherData )
	{
		super(otherData);
	}

}
