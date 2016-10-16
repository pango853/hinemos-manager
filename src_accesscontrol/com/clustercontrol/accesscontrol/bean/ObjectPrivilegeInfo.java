/*

Copyright (C) since 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.accesscontrol.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * Hinemosのオブジェクト権限情報を格納するクラス。<BR>
 * DTOクラスとしてマネージャ、クライアント間の通信で利用する。
 *
 */
@XmlType(namespace = "http://access.ws.clustercontrol.com")
public class ObjectPrivilegeInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private String objectType;
	private String objectId;
	private String roleId;
	private String objectPrivilege;
	private String createUserId = null;
	private Long createDate = new Long(0);
	private String modifyUserId = null;
	private Long modifyDate = new Long(0);

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	public String getRoleId() {
		return roleId;
	}
	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	public String getObjectPrivilege() {
		return objectPrivilege;
	}
	public void setObjectPrivilege(String objectPrivilege) {
		this.objectPrivilege = objectPrivilege;
	}
	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}
	public String getCreateUserId() {
		return createUserId;
	}
	public void setCreateDate(Long createDate) {
		this.createDate = createDate;
	}
	public Long getCreateDate() {
		return createDate;
	}
	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
	}
	public String getModifyUserId() {
		return modifyUserId;
	}
	public void setModifyDate(Long modifyDate) {
		this.modifyDate = modifyDate;
	}
	public Long getModifyDate() {
		return modifyDate;
	}
}