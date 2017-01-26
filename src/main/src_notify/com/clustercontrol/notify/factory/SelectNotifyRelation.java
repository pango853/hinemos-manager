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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.clustercontrol.notify.bean.NotifyRelationInfo;
import com.clustercontrol.notify.model.NotifyRelationInfoEntity;
import com.clustercontrol.notify.util.QueryUtil;

/**
 * システム通知情報を検索するクラスです。
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class SelectNotifyRelation {

	/**
	 * システム通知情報を返します。
	 * 
	 * @param notifyId 取得対象の通知ID
	 * @return システム通知情報
	 */
	public ArrayList<NotifyRelationInfo> getNotifyRelation(String notifyGroupId) {

		ArrayList<NotifyRelationInfo> ret = new ArrayList<NotifyRelationInfo> ();

		List<NotifyRelationInfoEntity> notifies = QueryUtil.getNotifyRelationInfoByNotifyGroupId(notifyGroupId);

		Iterator<NotifyRelationInfoEntity> it = notifies.iterator();

		while(it.hasNext()){

			NotifyRelationInfoEntity entity =it.next();

			NotifyRelationInfo detail = new NotifyRelationInfo(
					entity.getId().getNotifyGroupId(),
					entity.getId().getNotifyId(),
					entity.getNotifyType());
			ret.add(detail);
		}

		return ret;

	}

	/**
	 * 引数で指定した通知IDを利用している通知グループIDを取得する。
	 * 
	 * @param notifyId
	 * @return 通知グループIDのリスト
	 */
	public ArrayList<String> getNotifyGroupIdBaseOnNotifyId(String notifyId) {

		ArrayList<String> ret = new ArrayList<String>();

		List<NotifyRelationInfoEntity> relations = QueryUtil.getNotifyRelationInfoByNotifyId(notifyId);

		Iterator<NotifyRelationInfoEntity> itr = relations.iterator();

		while(itr.hasNext()){

			NotifyRelationInfoEntity relation = itr.next();

			ret.add(relation.getId().getNotifyGroupId());

		}

		return ret;
	}

}
