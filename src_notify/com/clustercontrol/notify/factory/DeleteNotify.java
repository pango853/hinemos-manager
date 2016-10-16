/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.notify.factory;

import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.notify.model.NotifyHistoryEntity;
import com.clustercontrol.notify.model.NotifyInfoEntity;
import com.clustercontrol.notify.model.NotifyRelationInfoEntity;
import com.clustercontrol.notify.util.QueryUtil;

/**
 * 通知情報を削除するクラスです。
 *
 * @version 3.0.0
 * @since 1.0.0
 */
public class DeleteNotify {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( DeleteNotify.class );

	/**
	 * 通知情報を削除します。
	 * <p>
	 * <ol>
	 *  <li>通知IDより、通知情報を取得します。</li>
	 *  <li>通知情報に設定されている通知イベント情報を削除します。</li>
	 *  <li>通知情報を削除します。</li>
	 *  <li>キャッシュ更新用の通知情報を生成し、ログ出力キューへ送信します。
	 *      監視管理機能で、監視管理機能で保持している通知情報キャッシュから削除されます。</li>
	 * </ol>
	 * 
	 * @param notifyId 削除対象の通知ID
	 * @return 削除に成功した場合、<code> true </code>
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.notify.ejb.entity.NotifyInfoBean
	 * @see com.clustercontrol.notify.ejb.entity.NotifyEventInfoBean
	 * @see #deleteEvents(Collection)
	 */
	public boolean delete(String notifyId) throws NotifyNotFound, InvalidRole, HinemosUnknown {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		NotifyInfoEntity notify = null;
		try
		{
			// 通知設定を取得
			notify = QueryUtil.getNotifyInfoPK(notifyId, ObjectPrivilegeMode.MODIFY);

			// この通知設定の結果として通知された通知履歴を削除する
			List<NotifyHistoryEntity> list = QueryUtil.getNotifyHistoryByNotifyId(notifyId);
			for(NotifyHistoryEntity history : list){
				try {
					em.remove(history);
				} catch (Exception e) {
					m_log.warn("delete() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				}
			}

			// システム通知情報を削除
			List<NotifyRelationInfoEntity> relations = QueryUtil.getNotifyRelationInfoByNotifyId(notifyId);
			for(NotifyRelationInfoEntity relation : relations){
				try {
					em.remove(relation);
				} catch (Exception e) {
					m_log.warn("delete() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				}
			}

			// 通知情報を削除
			em.remove(notify);

		} catch (InvalidRole e) {
			throw e;
		} catch (NotifyNotFound e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("delete() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		return true;
	}
}
