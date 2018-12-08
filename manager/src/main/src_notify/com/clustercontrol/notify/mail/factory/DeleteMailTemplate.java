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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.notify.mail.model.MailTemplateInfoEntity;
import com.clustercontrol.notify.mail.util.QueryUtil;
import com.clustercontrol.notify.model.NotifyInfoEntity;
import com.clustercontrol.notify.model.NotifyMailInfoEntity;

/**
 * メールテンプレート情報を削除するクラス<BR>
 *
 * @version 2.4.0
 * @since 2.4.0
 */
public class DeleteMailTemplate {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( DeleteMailTemplate.class );

	/**
	 * メールテンプレート情報を削除します。<BR>
	 * <p>
	 * <ol>
	 *  <li>メールテンプレートIDより、メールテンプレート情報を取得し、
	 *      メールテンプレート情報を削除します。</li>
	 * </ol>
	 * 
	 * @param mailTemplateId 削除対象のメールテンプレートID
	 * @return 削除に成功した場合、<code> true </code>
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean delete(String mailTemplateId) throws InvalidRole, HinemosUnknown {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		// メールテンプレート情報を検索し取得
		MailTemplateInfoEntity entity = null;
		try {
			entity = QueryUtil.getMailTemplateInfoPK(mailTemplateId, ObjectPrivilegeMode.MODIFY);
		} catch (MailTemplateNotFound e) {
			throw new HinemosUnknown(e.getMessage(), e);
		}

		// 利用されているメールテンプレートか否かチェックする
		List<NotifyInfoEntity> notifyList = com.clustercontrol.notify.util.QueryUtil.getAllNotifyInfo_NONE();
		for (NotifyInfoEntity notify : notifyList) {
			NotifyMailInfoEntity mail = notify.getNotifyMailInfoEntity();
			if (mail == null || mail.getMailTemplateInfoEntity() == null) {
				continue;
			}
			if (entity.getMailTemplateId().equals(
					mail.getMailTemplateInfoEntity().getMailTemplateId())) {
				String message = "used by " + notify.getNotifyId();
				m_log.info(message);
				throw new HinemosUnknown(message);
			}
		}

		//メールテンプレート情報を削除
		em.remove(entity);

		return true;
	}

}
