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

package com.clustercontrol.maintenance.factory;

import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.maintenance.util.QueryUtil;

/**
 * イベント履歴の削除処理
 *
 * @version 4.0.0
 * @since 3.1.0
 *
 */
public class MaintenanceEvent extends MaintenanceObject{

	private static Log m_log = LogFactory.getLog( MaintenanceEvent.class );

	/**
	 * 削除処理
	 */
	@Override
	protected int _delete(Timestamp keep, boolean status, String ownerRoleId) {
		m_log.debug("_delete() start : keep = " + keep.toString() + ", status = " + status);

		int ret = -1;

		//オーナーロールIDがADMINISTRATORSの場合
		if(RoleIdConstant.isAdministratorRole(ownerRoleId)) {
			//SQL文の実行
			if(status){
				// for HA (縮退判定時間を延ばすため)、シングルには影響なし(0) : タイムアウト値設定
				ret = QueryUtil.deleteEventLogByGenerationDate(keep, HinemosPropertyUtil.getHinemosPropertyNum(_QUERY_TIMEOUT_KEY, 0));
			} else {
				// for HA (縮退判定時間を延ばすため)、シングルには影響なし(0) : タイムアウト値設定
				//status=falseの場合は確認済みイベントのみを削除する
				ret = QueryUtil.deleteEventLogByGenerationDateConfigFlg(keep, HinemosPropertyUtil.getHinemosPropertyNum(_QUERY_TIMEOUT_KEY, 0));
			}
		}
		//オーナーロールが一般ロールの場合
		else {
			//SQL文の実行
			if(status){
				// for HA (縮退判定時間を延ばすため)、シングルには影響なし(0) : タイムアウト値設定
				ret = QueryUtil.deleteEventLogByGenerationDateAndOwnerRoleId(keep, HinemosPropertyUtil.getHinemosPropertyNum(_QUERY_TIMEOUT_KEY, 0), ownerRoleId);
			} else {
				// for HA (縮退判定時間を延ばすため)、シングルには影響なし(0) : タイムアウト値設定
				//status=falseの場合は確認済みイベントのみを削除する
				ret = QueryUtil.deleteEventLogByGenerationDateConfigFlgAndOwnerRoleId(keep, HinemosPropertyUtil.getHinemosPropertyNum(_QUERY_TIMEOUT_KEY, 0), ownerRoleId);
			}
		}

		//終了
		m_log.debug("_delete() count : " + ret);
		return ret;
	}

}
