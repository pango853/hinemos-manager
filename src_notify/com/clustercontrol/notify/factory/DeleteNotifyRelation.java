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

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.notify.model.NotifyRelationInfoEntity;
import com.clustercontrol.notify.util.QueryUtil;

/**
 * システム通知情報を削除するクラスです。
 *
 * @version 3.0.0
 * @since 3.0.0
 */
public class DeleteNotifyRelation {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( DeleteNotifyRelation.class );

	/**
	 * 通知グループIDを基に関連情報を削除します。
	 * <p>
	 * <ol>
	 *  <li>通知グループIDを基に関連情報を削除します。</li>
	 * </ol>
	 * 
	 * @param notifyGroupId 削除対象の通知グループID
	 * @return 削除に成功した場合、<code> true </code>
	 * @throws HinemosUnknown
	 */
	public boolean delete(String notifyGroupId) throws HinemosUnknown {
		JpaTransactionManager jtm = new JpaTransactionManager();
		HinemosEntityManager em = jtm.getEntityManager();

		try
		{
			List<NotifyRelationInfoEntity> notifies = QueryUtil.getNotifyRelationInfoByNotifyGroupId(notifyGroupId);

			Iterator<NotifyRelationInfoEntity> it = notifies.iterator();

			while(it.hasNext()){
				NotifyRelationInfoEntity detail = it.next();
				em.remove(detail);
			}
			// JPAではDML処理順序が保障されないため、フラッシュ実行
			jtm.flush();
		} catch (Exception e) {
			m_log.warn("delete() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return true;
	}
}
