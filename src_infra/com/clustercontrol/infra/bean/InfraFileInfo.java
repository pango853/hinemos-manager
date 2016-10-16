/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.bean;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.util.Messages;

@XmlType(namespace = "http://infra.ws.clustercontrol.com")
public class InfraFileInfo {
	private String fileId;
	private String fileName;
	private String ownerRoleId;
	private String createUserId;
	private long createDatetime;
	private String modifyUserId;
	private long modifyDatetime;
	
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getCreateUserId() {
		return createUserId;
	}
	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}
	public long getCreateDatetime() {
		return createDatetime;
	}
	public void setCreateDatetime(long createDatetime) {
		this.createDatetime = createDatetime;
	}
	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
	
	public String getModifyUserId() {
		return modifyUserId;
	}
	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
	}
	public long getModifyDatetime() {
		return modifyDatetime;
	}
	public void setModifyDatetime(long modifyDatetime) {
		this.modifyDatetime = modifyDatetime;
	}
	public void validate() throws InvalidSetting, InvalidRole {
		CommonValidator.validateId(Messages.getString("infra.filemanager.id"), fileId, 256);
		CommonValidator.validateString(Messages.getString("infra.filemanager.name"), fileName, true, 1, 256);
		CommonValidator.validateOwnerRoleId(ownerRoleId, true, fileId, HinemosModuleConstant.INFRA);
	}
	
	@Override
	public String toString() {
		return String.format("InfraFileInfo [fileId=%s, fileName=%s, ownerRoleId=%s, createUserId=%s, createDatetime=%s, modifyUserId=%s, modifyDatetime=%s]", 
				fileId, fileName, ownerRoleId, createUserId, createDatetime, modifyUserId, modifyDatetime);
	}
}
