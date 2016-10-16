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

package com.clustercontrol.accesscontrol.util;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.accesscontrol.bean.UserInfo;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.util.Messages;

/**
 * ユーザ管理の入力チェッククラス
 * 
 * @since 4.0
 */
public class UserValidator {

	/**
	 * ユーザ情報(UserInfo)の基本設定の妥当性チェック
	 * @param userInfo
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public static void validateUserInfo(UserInfo userInfo) throws InvalidSetting {

		// userId
		CommonValidator.validateId(Messages.getString("user.id"), userInfo.getId(), 64);

		// userName
		CommonValidator.validateString(Messages.getString("user.name"), userInfo.getName(), true, 1, 128);

		// description
		CommonValidator.validateString(Messages.getString("description"), userInfo.getDescription(), false, 0, 256);

	}


}
