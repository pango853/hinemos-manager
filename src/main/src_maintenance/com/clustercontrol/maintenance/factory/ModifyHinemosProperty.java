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

import java.sql.Timestamp;
import java.util.Date;

import com.clustercontrol.fault.HinemosPropertyNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.maintenance.bean.HinemosPropertyInfo;
import com.clustercontrol.maintenance.model.HinemosPropertyEntity;
import com.clustercontrol.maintenance.util.QueryUtil;

/**
 * メンテナンス情報を変更するためのクラスです。
 *
 * @version 4.0.0
 * @since 2.2.0
 *
 */
public class ModifyHinemosProperty {

	/**
	 * 共通設定情報を変更します。
	 *
	 * @param info 共通設定情報
	 * @param loginUser ログインユーザー名
	 * @return
	 * @throws HinemosPropertyNotFound
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean modifyHinemosProperty(HinemosPropertyInfo info, String loginUser)
			throws HinemosPropertyNotFound, NotifyNotFound, InvalidRole, HinemosUnknown {

		//共通設定情報を取得
		HinemosPropertyEntity entity = QueryUtil.getHinemosPropertyInfoPK(info.getKey());

		//共通設定情報を更新
		entity.setDescription(info.getDescription());
		entity.setKey(info.getKey());
		entity.setValueString(info.getValueString());
		entity.setValueNumeric(info.getValueNumeric());
		entity.setValueBoolean(info.isValueBoolean());
		entity.setDescription(info.getDescription());
		entity.setModifyUserId(loginUser);
		entity.setModifyDatetime(new Timestamp(new Date().getTime()));

		return true;
	}

}
