/*

 Copyright (C) 2009 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.notify.util;

import com.clustercontrol.notify.bean.NotifyRequestMessage;

/**
 * 通知を実行するユーティリティメソッドのインタフェースを規定します。
 */
public interface Notifier {
	/**
	 * 通知を実行します
	 */
	public void notify(NotifyRequestMessage message) throws Exception;

	/**
	 * 通知失敗時の内部エラー通知を定義します
	 */
	public void internalErrorNotify(String notifyId, String msgID, String detailMsg) throws Exception;
}
