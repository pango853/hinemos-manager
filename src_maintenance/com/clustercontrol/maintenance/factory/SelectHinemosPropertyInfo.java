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

package com.clustercontrol.maintenance.factory;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.HinemosPropertyNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.maintenance.bean.HinemosPropertyInfo;
import com.clustercontrol.maintenance.model.HinemosPropertyEntity;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.maintenance.util.QueryUtil;

/**
 *
 * 共通設定情報検索クラスです。
 *
 * @since	5.0.0
 * @version	5.0.0
 *
 */
public class SelectHinemosPropertyInfo {

	/**
	 * 共通設定情報リストを取得します。
	 * @return 共通設定情報リスト
	 * @throws HinemosPropertyNotFound
	 */
	public ArrayList<HinemosPropertyInfo> getHinemosPropertyInfoList()
			throws HinemosPropertyNotFound {

		ArrayList<HinemosPropertyInfo> list = new ArrayList<HinemosPropertyInfo>();
		HinemosPropertyInfo info = null;
		// 共通設定情報一覧を取得

		List<HinemosPropertyEntity> sList = QueryUtil.getAllHinemosPropertyOrderByKey();
		for (HinemosPropertyEntity entity : sList) {
			info = HinemosPropertyUtil.createHinemosPropertyInfo(entity);
			list.add(info);
		}

		return list;

	}

	/**
	 * 共通設定情報を取得します。
	 * @param key キー
	 * @return 共通設定情報
	 * @throws HinemosPropertyNotFound
	 * @throws InvalidRole
	 */
	public HinemosPropertyInfo getHinemosPropertyInfo(String key)
		throws HinemosUnknown, InvalidRole, HinemosPropertyNotFound {

		HinemosPropertyEntity entity = QueryUtil.getHinemosPropertyInfoPK(key);
		return HinemosPropertyUtil.createHinemosPropertyInfo(entity);
	}

	/**
	 * 共通設定情報を取得します。
	 * @param key キー
	 * @return 共通設定情報
	 */
	public HinemosPropertyInfo getHinemosPropertyInfo_None(String key) {
		return HinemosPropertyInfoCache.getProperty(key);
	}
}
