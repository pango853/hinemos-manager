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
import com.clustercontrol.notify.bean.NotifyRelationInfo;
import com.clustercontrol.notify.model.NotifyRelationInfoEntity;

/**
 * システム通知情報を作成するクラスです。
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class AddNotifyRelation {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( AddNotifyRelation.class );

	/**
	 * システム通知情報を作成します。
	 * <p>
	 * <ol>
	 *  <li>通知情報を作成します。</li>
	 *  <li>通知イベント情報を作成し、通知情報に設定します。</li>
	 * </ol>
	 * 
	 * @param info 作成対象の通知情報
	 * @return 作成に成功した場合、<code> true </code>
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.notify.ejb.entity.SystemNotifyInfoBean
	 * @see com.clustercontrol.notify.ejb.entity.SystemNotifyEventInfoBean
	 */
	public boolean add(Collection<NotifyRelationInfo> info) throws HinemosUnknown {
		NotifyRelationInfo relation = null;

		try
		{
			if(info != null){
				// システム通知イベント情報を挿入
				Iterator<NotifyRelationInfo> it= info.iterator();

				while(it.hasNext()){

					relation = it.next();

					if(relation != null){
						NotifyRelationInfoEntity entity = new NotifyRelationInfoEntity(relation.getNotifyGroupId(), relation.getNotifyId());
						entity.setNotifyType(relation.getNotifyType());
					}
				}
			}

		} catch (Exception e) {
			m_log.warn("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return true;
	}
}
