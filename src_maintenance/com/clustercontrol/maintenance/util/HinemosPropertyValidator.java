/*

Copyright (C) since 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.maintenance.util;

import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.maintenance.HinemosPropertyTypeConstant;
import com.clustercontrol.maintenance.bean.HinemosPropertyInfo;
import com.clustercontrol.util.Messages;

/**
 * 共通設定情報の入力チェッククラス
 *
 * @version 5.0.0
 * @since 5.0
 */
public class HinemosPropertyValidator {

	/**
	 * 共通設定情報の妥当性チェック
	 *
	 * @param info
	 * @throws InvalidSetting
	 */
	public static void validateHinemosPropertyInfo(HinemosPropertyInfo info) throws InvalidSetting {

		// key
		//nullチェックあり
		CommonValidator.validateString(Messages.getString("hinemos.property.key"),
				info.getKey(), true, 1, 64);

		//value
		if (info.getValueType() == HinemosPropertyTypeConstant.TYPE_STRING
				&& info.getValueString() != null && info.getValueString().trim().length() > 0) {
			CommonValidator.validateString(Messages.getString("hinemos.property.value"),
					info.getValueString(), false, 0, 256);
		} else if (info.getValueType() == HinemosPropertyTypeConstant.TYPE_NUMERIC && info.getValueNumeric() != null) {
			CommonValidator.validateInt(
					Messages.getString("hinemos.property.value"),
					info.getValueNumeric(), Integer.MIN_VALUE,
					Integer.MAX_VALUE);
		}

		// description
		CommonValidator.validateString(Messages.getString("description"),info.getDescription(), false, 0, 256);
	}
}
