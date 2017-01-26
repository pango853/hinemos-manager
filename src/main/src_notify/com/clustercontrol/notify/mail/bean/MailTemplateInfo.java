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

package com.clustercontrol.notify.mail.bean;

import java.io.Serializable;

/**
 * メンテナンス情報のデータクラスです。
 * 
 * @version 4.0.0
 * @since 2.2.0
 *
 */
public class MailTemplateInfo implements Serializable{

	private static final long serialVersionUID = 2140680242123326612L;
	private String mailTemplateId;
	private String description;
	private String subject;
	private String body;
	private String ownerRoleId;
	private Long regDate;
	private Long updateDate;
	private String regUser;
	private String updateUser;

	public MailTemplateInfo() {
		super();
	}

	public MailTemplateInfo(
			String mailTemplateId,
			String description,
			String subject,
			String body,
			String ownerRoleId,
			Long regDate,
			Long updateDate,
			String regUser,
			String updateUser) {
		this.setMailTemplateId(mailTemplateId);
		this.setDescription(description);
		this.setSubject(subject);
		this.setBody(body);
		this.setOwnerRoleId(ownerRoleId);
		this.setRegDate(regDate);
		this.setUpdateDate(updateDate);
		this.setRegUser(regUser);
		this.setUpdateUser(updateUser);
	}
	public String getMailTemplateId() {
		return mailTemplateId;
	}

	public void setMailTemplateId(String mailTemplateId) {
		this.mailTemplateId = mailTemplateId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public Long getRegDate() {
		return regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	public Long getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
}
