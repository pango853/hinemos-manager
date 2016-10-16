/*

Copyright (C) 2008 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.notify.mail.factory;

import java.sql.Timestamp;
import java.util.Date;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.notify.mail.bean.MailTemplateInfo;
import com.clustercontrol.notify.mail.model.MailTemplateInfoEntity;
import com.clustercontrol.notify.mail.util.QueryUtil;

/**
 * メールテンプレート情報を変更するクラス<BR>
 *
 * @version 2.4.0
 * @since 2.4.0
 */
public class ModifyMailTemplate {

	/**
	 * メールテンプレート情報を変更します。
	 * <p>
	 * <ol>
	 *  <li>メールテンプレートIDより、メールテンプレート情報を取得し、
	 *      メールテンプレート情報を変更します。</li>
	 * </ol>
	 * 
	 * @param info 変更対象のメールテンプレート情報
	 * @param name 変更したユーザ名
	 * @return 変更に成功した場合、<code> true </code>
	 * @throws MailTemplateNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.notify.ejb.entity.MailTemplateInfoBean
	 */
	public boolean modify(MailTemplateInfo data, String name) throws MailTemplateNotFound, InvalidRole {

		//メールテンプレート情報を取得
		MailTemplateInfoEntity mailTemplateInfo = QueryUtil.getMailTemplateInfoPK(data.getMailTemplateId(), ObjectPrivilegeMode.MODIFY);

		//メールテンプレート情報を更新
		mailTemplateInfo.setDescription(data.getDescription());
		mailTemplateInfo.setSubject(data.getSubject());
		mailTemplateInfo.setBody(data.getBody());
		mailTemplateInfo.setOwnerRoleId(data.getOwnerRoleId());
		mailTemplateInfo.setUpdateDate(new Timestamp(new Date().getTime()));
		mailTemplateInfo.setUpdateUser(name);

		return true;
	}

}
