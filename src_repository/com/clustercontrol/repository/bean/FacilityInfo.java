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
 * このクラスはファシリティのクラスです。
 * スコープの詳細を保持するために使用されます。
 * ノードの詳細で利用する場合は、サブクラスのNodeDataを利用して下さい。
 * @since 0.8
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class FacilityInfo implements Serializable, Cloneable
{
	private static final long serialVersionUID = -7839051961494125310L;
	private String facilityId = "";
	private String facilityName = "";
	private Integer facilityType = new Integer(1);
	private String description = "";
	private Integer displaySortOrder = new Integer(100);
	private String iconImage = "";
	private Boolean valid = Boolean.TRUE;
	private String createUserId = "";
	private Long createDatetime = System.currentTimeMillis();
	private String modifyUserId = "";
	private Long modifyDatetime = System.currentTimeMillis();
	/**スコープがビルトインかのフラグ*/
	private Boolean builtInFlg = false;
	/** オーナーロールID */
	private String ownerRoleId = "";

	/** 参照フラグ（true：参照権限のないスコープ） */
	private Boolean notReferFlg = true;

	/**
	 * 空のコンストラクタです。
	 * ファシリティ種別は0に、管理対象は1に、表示ソート順は200に設定されます。
	 * それ以外の値はnullに設定されます。
	 */
	public FacilityInfo()
	{
		// 0 - scope, 1 - node
		setFacilityType(1);

		// 0 - disable, 1 - enable
		setValid(Boolean.TRUE);

		// 100 - node, 200 - scope
		// 10000 - display_sort_order, 11000 - REGISTERED, 12000 - UNREGISTERED
		setDisplaySortOrder(100);
	}

	/**
	 * コンストラクトする際に、値をセットする事ができます。
	 * @param facilityId
	 * @param facilityName
	 * @param facilityType
	 * @param description
	 * @param displaySortOrder
	 * @param valid
	 * @param autoDeviceSearch
	 * @param createUserId
	 * @param createDatetime
	 * @param modifyUserId
	 * @param modifyDatetime
	 * @param builtInFlg
	 * @param ownerRoleId
	 * @param notReferFlg
	 */
	public FacilityInfo( String facilityId, String facilityName, Integer facilityType, String description, Integer displaySortOrder, String iconImage, Boolean valid, Boolean autoDeviceSearch, String createUserId, Long createDatetime, String modifyUserId, Long modifyDatetime, Boolean builtInFlg, String ownerRoleId, Boolean notReferFlg )
	{
		setFacilityId(facilityId);
		setFacilityName(facilityName);
		setFacilityType(facilityType);
		setDescription(description);
		setDisplaySortOrder(displaySortOrder);
		setIconImage(iconImage);
		setValid(valid);
		setCreateUserId(createUserId);
		setCreateDatetime(createDatetime);
		setModifyUserId(modifyUserId);
		setModifyDatetime(modifyDatetime);
		setBuiltInFlg(builtInFlg);
		setOwnerRoleId(ownerRoleId);
		setNotReferFlg(notReferFlg);
	}

	/**
	 * FacilityDataインスタンスのコピーを生成する時に利用します。
	 * @param otherData
	 */
	public FacilityInfo( FacilityInfo otherData )
	{
		if (otherData != null) {
			setFacilityId(otherData.getFacilityId());
			setFacilityName(otherData.getFacilityName());
			setFacilityType(otherData.getFacilityType());
			setDescription(otherData.getDescription());
			setDisplaySortOrder(otherData.getDisplaySortOrder());
			setIconImage(otherData.getIconImage());
			setValid(otherData.isValid());
			setCreateUserId(otherData.getCreateUserId());
			setCreateDatetime(otherData.getCreateDatetime());
			setModifyUserId(otherData.getModifyUserId());
			setModifyDatetime(otherData.getModifyDatetime());
			setBuiltInFlg(otherData.isBuiltInFlg());
			setOwnerRoleId(otherData.getOwnerRoleId());
			setNotReferFlg(otherData.isNotReferFlg());
		}
	}

	/**
	 * ファシリティIDのgetterです。
	 * @return String
	 */
	public String getFacilityId()
	{
		return this.facilityId;
	}

	/**
	 * ファシリティIDのsetterです。
	 * not nullかつuniqueです。
	 * 一度Hinemosに登録された場合、変更不可な値となります。
	 * @param facilityId
	 */
	public void setFacilityId( String facilityId )
	{
		this.facilityId = facilityId;
	}

	/**
	 * ファシリティ名のgetterです。
	 * @return String
	 */
	public String getFacilityName()
	{
		return this.facilityName;
	}

	/**
	 * ファシリティ名のsetterです。
	 * not nullです。
	 * @param facilityName
	 */
	public void setFacilityName( String facilityName )
	{
		this.facilityName = facilityName;
	}

	/**
	 * ファシリティ種別のgetterです。
	 * @return Integer
	 */
	public Integer getFacilityType()
	{
		return this.facilityType;
	}

	/**
	 * ファシリティ種別のsetterです。
	 * スコープの場合「0」、ノードの場合「1」となります。
	 * @param facilityType
	 */
	public void setFacilityType( Integer facilityType )
	{
		this.facilityType = facilityType;
	}

	/**
	 * 説明のgetterです。
	 * @return String
	 */
	public String getDescription()
	{
		return this.description;
	}

	/**
	 * 説明のsetterです。
	 * @param description
	 */
	public void setDescription( String description )
	{
		this.description = description;
	}

	/**
	 * 表示ソート順のgetterです。
	 * @return Integer
	 */
	public Integer getDisplaySortOrder()
	{
		return this.displaySortOrder;
	}

	/**
	 * 表示ソート順のsetterです。
	 * ノードの場合100、スコープの場合200を設定します。
	 * @param displaySortOrder
	 */
	public void setDisplaySortOrder( Integer displaySortOrder )
	{
		this.displaySortOrder = displaySortOrder;
	}

	/**
	 * 画面アイコンイメージのgetterです。
	 * @return String
	 */
	public String getIconImage()
	{
		return this.iconImage;
	}

	/**
	 * 画面アイコンイメージのsetterです。
	 * @param iconImage
	 */
	public void setIconImage( java.lang.String iconImage )
	{
		this.iconImage = iconImage;
	}


	/**
	 * 管理対象のgetterです。
	 * @return Boolean
	 */
	public Boolean isValid()
	{
		return this.valid;
	}

	/**
	 * 管理対象のsetterです。
	 * 有効の場合1、無効の場合0を設定します。
	 * @param valid
	 */
	public void setValid( Boolean valid )
	{
		this.valid = valid;
	}

	/**
	 * 新規作成ユーザのgetterです。
	 * @return String
	 */
	public String getCreateUserId()
	{
		return this.createUserId;
	}

	/**
	 * 新規作成ユーザのsetterです。
	 * @param createUserId
	 */
	public void setCreateUserId( String createUserId )
	{
		this.createUserId = createUserId;
	}

	/**
	 * 作成日時のgetterです。
	 * @return Date
	 */
	public Long getCreateDatetime()
	{
		return this.createDatetime;
	}

	/**
	 * 作成日時のsetterです。
	 * @param createDatetime
	 */
	public void setCreateDatetime( Long createDatetime )
	{
		this.createDatetime = createDatetime;
	}

	/**
	 * 最終変更ユーザのgetterです。
	 * @return String
	 */
	public String getModifyUserId()
	{
		return this.modifyUserId;
	}

	/**
	 * 最終変更ユーザのsetterです。
	 * @param modifyUserId
	 */
	public void setModifyUserId( String modifyUserId )
	{
		this.modifyUserId = modifyUserId;
	}

	/**
	 * 最終変更日時のgetterです。
	 * @return Date
	 */
	public Long getModifyDatetime()
	{
		return this.modifyDatetime;
	}

	/**
	 * 最終変更日時のsetterです。
	 * @param modifyDatetime
	 */
	public void setModifyDatetime( Long modifyDatetime )
	{
		this.modifyDatetime = modifyDatetime;
	}

	/**
	 * オーナーロールIDgetterです。
	 * @return String
	 */
	public String getOwnerRoleId() {
		return this.ownerRoleId;
	}

	/**
	 * オーナーロールIDのsetterです。
	 * @param ownerRoleId
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	/**
	 * 参照不可フラグを取得します。<BR>
	 *
	 * @return 参照不可フラグ
	 */
	public Boolean isNotReferFlg() {
		return this.notReferFlg;
	}

	/**
	 * 参照不可フラグを設定します。<BR>
	 *
	 * @param notReferFlg 参照不可フラグ（true：参照権限のないスコープ）
	 */
	public void setNotReferFlg(Boolean notReferFlg) {
		this.notReferFlg = notReferFlg;
	}

	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer("{");

		str.append("facilityId=" + getFacilityId() + " " +
				"facilityName=" + getFacilityName() + " " +
				"facilityType=" + getFacilityType() + " " +
				"description=" + getDescription() + " " +
				"displaySortOrder=" + getDisplaySortOrder() + " " +
				"iconImage=" + getIconImage() + " " +
				"valid=" + isValid() + " " +
				"ownerRoleId=" + getOwnerRoleId() + " " +
				"createUserId=" + getCreateUserId() + " " +
				"createDatetime=" + getCreateDatetime() + " " +
				"modifyUserId=" + getModifyUserId() + " " +
				"modifyDatetime=" + getModifyDatetime() + " " +
				"buildInFlg=" + isBuiltInFlg() + " " +
				"notReferFlg=" + isNotReferFlg());
		str.append('}');

		return(str.toString());
	}

	@Override
	public boolean equals( Object pOther )
	{
		if( pOther instanceof FacilityInfo )
		{
			FacilityInfo lTest = (FacilityInfo) pOther;
			boolean lEquals = true;

			if( this.facilityId == null )
			{
				lEquals = lEquals && ( lTest.facilityId == null );
			}
			else
			{
				lEquals = lEquals && this.facilityId.equals( lTest.facilityId );
			}
			if( this.facilityName == null )
			{
				lEquals = lEquals && ( lTest.facilityName == null );
			}
			else
			{
				lEquals = lEquals && this.facilityName.equals( lTest.facilityName );
			}
			if( this.facilityType == null )
			{
				lEquals = lEquals && ( lTest.facilityType == null );
			}
			else
			{
				lEquals = lEquals && this.facilityType.equals( lTest.facilityType );
			}
			if( this.description == null )
			{
				lEquals = lEquals && ( lTest.description == null );
			}
			else
			{
				lEquals = lEquals && this.description.equals( lTest.description );
			}
			if( this.displaySortOrder == null )
			{
				lEquals = lEquals && ( lTest.displaySortOrder == null );
			}
			else
			{
				lEquals = lEquals && this.displaySortOrder.equals( lTest.displaySortOrder );
			}
			if( this.iconImage == null)
			{
				lEquals = lEquals && (lTest.iconImage == null);
			}
			else
			{
				lEquals = lEquals && this.iconImage.equals( lTest.iconImage );
			}
			if( this.valid == null )
			{
				lEquals = lEquals && ( lTest.valid == null );
			}
			else
			{
				lEquals = lEquals && this.valid.equals( lTest.valid );
			}
			if( this.ownerRoleId == null )
			{
				lEquals = lEquals && ( lTest.ownerRoleId == null );
			}
			else
			{
				lEquals = lEquals && this.ownerRoleId.equals( lTest.ownerRoleId );
			}
			if( this.notReferFlg == null )
			{
				lEquals = lEquals && ( lTest.notReferFlg == null );
			}
			else
			{
				lEquals = lEquals && this.notReferFlg.equals( lTest.notReferFlg );
			}
			if( this.createUserId == null )
			{
				lEquals = lEquals && ( lTest.createUserId == null );
			}
			else
			{
				lEquals = lEquals && this.createUserId.equals( lTest.createUserId );
			}
			if( this.createDatetime == null )
			{
				lEquals = lEquals && ( lTest.createDatetime == null );
			}
			else
			{
				lEquals = lEquals && this.createDatetime.equals( lTest.createDatetime );
			}
			if( this.modifyUserId == null )
			{
				lEquals = lEquals && ( lTest.modifyUserId == null );
			}
			else
			{
				lEquals = lEquals && this.modifyUserId.equals( lTest.modifyUserId );
			}
			if( this.modifyDatetime == null )
			{
				lEquals = lEquals && ( lTest.modifyDatetime == null );
			}
			else
			{
				lEquals = lEquals && this.modifyDatetime.equals( lTest.modifyDatetime );
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

		result = 37*result + ((this.facilityId != null) ? this.facilityId.hashCode() : 0);

		result = 37*result + ((this.facilityName != null) ? this.facilityName.hashCode() : 0);

		result = 37*result + ((this.facilityType != null) ? this.facilityType.hashCode() : 0);

		result = 37*result + ((this.description != null) ? this.description.hashCode() : 0);

		result = 37*result + ((this.displaySortOrder != null) ? this.displaySortOrder.hashCode() : 0);

		result = 37*result + ((this.iconImage != null) ? this.iconImage.hashCode() : 0);

		result = 37*result + ((this.valid != null) ? this.valid.hashCode() : 0);

		result = 37*result + ((this.ownerRoleId != null) ? this.ownerRoleId.hashCode() : 0);

		result = 37*result + ((this.notReferFlg != null) ? this.notReferFlg.hashCode() : 0);

		result = 37*result + ((this.createUserId != null) ? this.createUserId.hashCode() : 0);

		result = 37*result + ((this.createDatetime != null) ? this.createDatetime.hashCode() : 0);

		result = 37*result + ((this.modifyUserId != null) ? this.modifyUserId.hashCode() : 0);

		result = 37*result + ((this.modifyDatetime != null) ? this.modifyDatetime.hashCode() : 0);

		return result;
	}

	public Boolean isBuiltInFlg() {
		return builtInFlg;
	}

	public void setBuiltInFlg(Boolean builtInFlg) {
		this.builtInFlg = builtInFlg;
	}

	@Override
	public FacilityInfo clone(){
		FacilityInfo cloneInfo = null;
		try {
			cloneInfo = (FacilityInfo) super.clone();
			cloneInfo.facilityId = this.facilityId;
			cloneInfo.facilityName = this.facilityName;
			cloneInfo.facilityType = this.facilityType;
			cloneInfo.description = this.description;
			cloneInfo.displaySortOrder = this.displaySortOrder;
			cloneInfo.iconImage = this.iconImage;
			cloneInfo.valid = this.valid;
			cloneInfo.createUserId = this.createUserId;
			cloneInfo.createDatetime = this.createDatetime;
			cloneInfo.modifyUserId = this.modifyUserId;
			cloneInfo.modifyDatetime = this.modifyDatetime;
			cloneInfo.builtInFlg = this.builtInFlg;
			cloneInfo.ownerRoleId = this.ownerRoleId;
			cloneInfo.notReferFlg = this.notReferFlg;
		} catch (CloneNotSupportedException e) {
			// do nothing
		}
		return cloneInfo;
	}
}
