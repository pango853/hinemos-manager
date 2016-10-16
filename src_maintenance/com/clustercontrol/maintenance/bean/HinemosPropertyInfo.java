/*

Copyright (C) 2007 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.maintenance.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * Hinemosプロパティのデータクラスです。
 *
 * @version 5.0.0
 * @since 5.0.0
 *
 */
@XmlType(namespace = "http://maintenance.ws.clustercontrol.com")
public class HinemosPropertyInfo implements Serializable{
	private static final long serialVersionUID = 4890593575884133961L;
	private String key;
	private String valueString;
	private Integer valueNumeric;
	private Boolean valueBoolean;
	private Integer valueType;
	private String description;
	private String ownerRoleId;
	private String createUserId;
	private Long createDatetime;
	private String modifyUserId;
	private Long modifyDatetime;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValueString() {
		return valueString;
	}

	public void setValueString(String valueString) {
		this.valueString = valueString;
	}

	public Integer getValueNumeric() {
		return valueNumeric;
	}

	public void setValueNumeric(Integer valueNumeric) {
		this.valueNumeric = valueNumeric;
	}

	public Boolean isValueBoolean() {
		return valueBoolean;
	}

	public void setValueBoolean(Boolean valueBoolean) {
		this.valueBoolean = valueBoolean;
	}

	public Integer getValueType() {
		return valueType;
	}

	public void setValueType(Integer valueType) {
		this.valueType = valueType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public String getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}

	public Long getCreateDatetime() {
		return createDatetime;
	}

	public void setCreateDatetime(Long createDatetime) {
		this.createDatetime = createDatetime;
	}

	public String getModifyUserId() {
		return modifyUserId;
	}

	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
	}

	public Long getModifyDatetime() {
		return modifyDatetime;
	}

	public void setModifyDatetime(Long modifyDatetime) {
		this.modifyDatetime = modifyDatetime;
	}

}
