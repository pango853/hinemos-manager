/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

 */

package com.clustercontrol.performance.monitor.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.monitor.run.factory.DeleteMonitor;

/**
 * リソース監視情報削除クラス
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class DeleteMonitorPerformance extends DeleteMonitor{

	private static Log m_log = LogFactory.getLog( DeleteMonitorPerformance.class );

	/**
	 * リソース監視情報を削除します。
	 * @see com.clustercontrol.monitor.run.factory.DeleteMonitor#deleteCheckInfo()
	 */
	@Override
	protected boolean deleteCheckInfo() {
		m_log.debug("deleteCheckInfo()");

		// ポーラを停止
		boolean result = true;
		ModifyPollingSchedule poller = new ModifyPollingSchedule();
		result = poller.deleteSchedule(
				m_monitor.getMonitorTypeId(),
				m_monitor.getMonitorId(),
				m_monitor.getFacilityId());

		return result;
	}
}
