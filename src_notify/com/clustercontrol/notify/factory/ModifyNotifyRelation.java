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
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.notify.bean.NotifyRelationInfo;
import com.clustercontrol.notify.model.NotifyRelationInfoEntity;

/**
 * システム通知情報を変更するクラスです。
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class ModifyNotifyRelation {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( ModifyNotifyRelation.class );

	/**
	 * システム通知情報を変更します。
	 * <p>
	 * <ol>
	 *  <li>通知IDより、システム通知情報を取得します。</li>
	 *  <li>システム通知情報を変更します。</li>
	 *  <li>システム通知情報に設定されているシステム通知イベント情報を削除します。</li>
	 *  <li>システム通知イベント情報を作成し、通知情報に設定します。</li>
	 * </ol>
	 * 
	 * @param info 変更対象のシステム通知情報
	 * @return 変更に成功した場合、<code> true </code>
	 * @throws HinemosUnknown
	 * @throws NotifyNotFound
	 * 
	 * @see com.clustercontrol.notify.ejb.entity.SystemNotifyInfoBean
	 * @see com.clustercontrol.notify.ejb.entity.SystemNotifyEventInfoBean
	 * @see com.clustercontrol.notify.factory.DeleteSystemNotify#deleteEvents(Collection)
	 */
	public boolean modify(Collection<NotifyRelationInfo> info, String notifyGroupId) throws HinemosUnknown, NotifyNotFound {
		NotifyRelationInfo relation = null;
		m_log.debug("ModifyNotifyRelation.modify() notifyGroupId = " + notifyGroupId);
		try
		{
			/**
			 * 通知グループと通知IDは更新のたびに内容が変わる可能性があるので、
			 * findByPKでデータを読み出し更新するのでは、消え残る可能性がある。
			 * そこで、通知グループに紐つくものをすべて削除し、再度投入する。
			 **/
			// システム通知イベント情報を削除
			if(notifyGroupId != null && !notifyGroupId.equals("")){
				DeleteNotifyRelation delete = new DeleteNotifyRelation();
				delete.delete(notifyGroupId);
			}
			if(info != null){
				Iterator<NotifyRelationInfo> it= info.iterator();

				while(it.hasNext()){

					relation = it.next();

					if(relation != null){
						// 通知情報を検索
						m_log.debug("NotifyRelation ADD before : notifyGroupId = " + relation.getNotifyGroupId() + ", notifyId = " + relation.getNotifyId());
						NotifyRelationInfoEntity entity = new NotifyRelationInfoEntity(relation.getNotifyGroupId(), relation.getNotifyId());
						entity.setNotifyType(relation.getNotifyType());
						m_log.debug("NotifyRelation ADD : notifyGroupId = " + entity.getId().getNotifyGroupId() + ", notifyId = " + entity.getId().getNotifyId());
					}
				}
			}
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("modify() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return true;
	}

}
