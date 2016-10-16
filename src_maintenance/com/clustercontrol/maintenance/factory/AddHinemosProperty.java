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

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.maintenance.HinemosPropertyTypeConstant;
import com.clustercontrol.maintenance.bean.HinemosPropertyInfo;
import com.clustercontrol.maintenance.model.HinemosPropertyEntity;

/**
 * Hinemosプロパティを登録するためのクラスです。
 *
 * @version 5.0.0
 * @since 5.0.0
 *
 */
public class AddHinemosProperty {

	private static Log m_log = LogFactory.getLog(AddHinemosProperty.class);

	/**
	 * 共通設定情報を追加します。
	 *
	 * @param info 共通設定情報
	 * @param loginUser ログインユーザー名
	 * @return
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 */
	public boolean addHinemosProperty(HinemosPropertyInfo info, String loginUser)
			throws EntityExistsException, InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = new JpaTransactionManager();

		// Entityクラスのインスタンス生成
		try {
			// インスタンス生成
			HinemosPropertyEntity entity = new HinemosPropertyEntity(info.getKey());

			// 重複チェック
			entity.setKey(info.getKey());
			jtm.checkEntityExists(HinemosPropertyEntity.class, entity.getKey());

			//値種別に応じて値をセット
			if (info.getValueType() == HinemosPropertyTypeConstant.TYPE_STRING) {
				entity.setValueString(info.getValueString());
			} else if (info.getValueType() == HinemosPropertyTypeConstant.TYPE_NUMERIC) {
				entity.setValueNumeric(info.getValueNumeric());
			} else {
				entity.setValueBoolean(info.isValueBoolean());
			}

			entity.setValueType(info.getValueType());
			entity.setDescription(info.getDescription());
			entity.setOwnerRoleId(info.getOwnerRoleId());
			entity.setCreateUserId(loginUser);
			entity.setCreateDatetime(new Timestamp(new Date().getTime()));
			entity.setModifyUserId(loginUser);
			entity.setModifyDatetime(new Timestamp(new Date().getTime()));
		} catch (EntityExistsException e){
			m_log.info("addHinemosProperty() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e;
		}

		return true;
	}

}
