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
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlType;

/**
 * Hinemosのロール情報を格納するクラス。<BR>
 * DTOクラスとしてマネージャ、クライアント間の通信で利用する。
 *
 */
@XmlType(namespace = "http://access.ws.clustercontrol.com")
public class RoleInfo implements Serializable {
	private static final long serialVersionUID = 402750470824251665L;

	private String id = null;
	private String name = null;
	private String description = null;
	private String createUserId = null;
	private Long createDate = new Long(0);
	private String modifyUserId = null;
	private Long modifyDate = new Long(0);
	private String roleType = null;
	private ArrayList<String> userList = new ArrayList<String>();
	private ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();

	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
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
	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}
	public String getRoleType() {
		return roleType;
	}
	public void setUserList(ArrayList<String> userList) {
		this.userList = userList;
	}
	public void addUser(String user){
		this.userList.add(user);
	}
	public ArrayList<String> getUserList() {
		return userList;
	}
	public void setSystemPrivilegeList(ArrayList<SystemPrivilegeInfo> systemPrivilegeList) {
		this.systemPrivilegeList = systemPrivilegeList;
	}
	public void addSystemPrivilege(SystemPrivilegeInfo systemPrivilege){
		this.systemPrivilegeList.add(systemPrivilege);
	}
	public ArrayList<SystemPrivilegeInfo> getSystemPrivilegeList() {
		return systemPrivilegeList;
	}
}