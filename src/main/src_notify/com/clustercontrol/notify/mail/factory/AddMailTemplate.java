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

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.MailTemplateDuplicate;
import com.clustercontrol.notify.mail.bean.MailTemplateInfo;
import com.clustercontrol.notify.mail.model.MailTemplateInfoEntity;

/**
 * メールテンプレート情報を作成するクラス<BR>
 *
 * @version 2.4.0
 * @since 2.4.0
 */
public class AddMailTemplate {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( AddMailTemplate.class );

	/**
	 * メールテンプレート情報を作成します。
	 * <p>
	 * <ol>
	 *  <li>メールテンプレート情報を作成します。</li>
	 * </ol>
	 * 
	 * @param info 作成対象のメールテンプレート情報
	 * @param name メールテンプレート情報を作成したユーザ名
	 * @return 作成に成功した場合、<code> true </code>
	 * @throws MailTemplateDuplicate
	 * 
	 * @see com.clustercontrol.notify.ejb.entity.MailTemplateInfoBean
	 */
	public boolean add(MailTemplateInfo data, String name) throws MailTemplateDuplicate {

		JpaTransactionManager jtm = new JpaTransactionManager();

		Timestamp timestamp = new Timestamp(new Date().getTime());

		//エンティティBeanを作る
		try {

			// カレンダ登録
			MailTemplateInfoEntity entity = new MailTemplateInfoEntity(data.getMailTemplateId());
			// 重複チェック
			jtm.checkEntityExists(MailTemplateInfoEntity.class, entity.getMailTemplateId());
			entity.setBody(data.getBody());
			entity.setDescription(data.getDescription());
			entity.setSubject(data.getSubject());
			entity.setOwnerRoleId(data.getOwnerRoleId());
			entity.setRegDate(timestamp);
			entity.setRegUser(name);
			entity.setUpdateDate(timestamp);
			entity.setUpdateUser(name);

		} catch (EntityExistsException e) {
			m_log.info("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new MailTemplateDuplicate(e.getMessage(),e);
		}

		return true;
	}

}
