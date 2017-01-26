/*

Copyright (C) 2008 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.snmptrap.factory;

import com.clustercontrol.monitor.run.factory.DeleteMonitor;

/**
 * SNMPTRAP監視情報を削除するクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class DeleteMonitorTrap extends DeleteMonitor {

	@Override
	protected boolean deleteCheckInfo() {
		return true;
	}
}
