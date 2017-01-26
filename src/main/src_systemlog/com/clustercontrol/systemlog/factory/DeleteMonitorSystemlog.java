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

package com.clustercontrol.systemlog.factory;

import com.clustercontrol.monitor.run.factory.DeleteMonitor;

/**
 * システムログ監視情報をマネージャから削除するクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class DeleteMonitorSystemlog extends DeleteMonitor{

	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.DeleteMonitor#deleteCheckInfo()
	 */
	@Override
	protected boolean deleteCheckInfo() {

		// システムログ監視のチェック条件はないため、常にtrue
		return true;
	}
}
