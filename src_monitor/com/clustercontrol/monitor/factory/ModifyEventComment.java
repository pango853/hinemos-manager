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


package com.clustercontrol.monitor.factory;

import java.sql.Timestamp;
import java.util.Date;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.fault.EventLogNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.util.QueryUtil;

public class ModifyEventComment {

	/**
	 * 引数で指定されたイベント情報のコメントを更新します。
	 * 
	 * @param monitorId
	 * @param pluginId
	 * @param facilityId
	 * @param outputDate
	 * @param comment
	 * @param commentDate
	 * @param commentUser
	 * @throws EventLogNotFound
	 * @throws InvalidRole
	 */

	public void modifyComment(String monitorId, String monitorDetailId, String pluginId,
			String facilityId,
			Long outputDate, String comment, Long commentDate,
			String commentUser) throws EventLogNotFound, InvalidRole {

		// イベントログ情報を取得
		EventLogEntity event = null;
		try {
			event = QueryUtil.getEventLogPK(monitorId, monitorDetailId, pluginId, new Timestamp(outputDate), facilityId, ObjectPrivilegeMode.MODIFY);
		} catch (EventLogNotFound e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		}
		commentDate = new Date().getTime();
		event.setCommentDate(new Timestamp (commentDate));

		event.setCommentUser(commentUser);
		event.setComment(comment);
	}
}
