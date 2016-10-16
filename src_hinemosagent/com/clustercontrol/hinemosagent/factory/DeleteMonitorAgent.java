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

package com.clustercontrol.hinemosagent.factory;

import com.clustercontrol.monitor.run.factory.DeleteMonitor;

/**
 * Hinemos Agent監視情報削除クラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class DeleteMonitorAgent extends DeleteMonitor{

	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.DeleteMonitor#deleteCheckInfo()
	 */
	@Override
	protected boolean deleteCheckInfo() {

		// Agent監視情報を取得
		// Agent監視は監視の有無を設定するのみ。CheckInfoは存在しない。

		return true;
	}
}
