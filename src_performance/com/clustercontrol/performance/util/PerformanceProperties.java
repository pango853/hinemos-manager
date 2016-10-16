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

package com.clustercontrol.performance.util;

import com.clustercontrol.maintenance.util.HinemosPropertyUtil;

/**
 * リソース監視プロパティ情報を取得するクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class PerformanceProperties {
	private static PerformanceProperties m_instance = null;

	/**
	 * このオブジェクトを取得します。
	 *
	 *
	 * @version 4.0.0
	 * @since 4.0.0
	 *
	 * @return ConnectionManager コネクションマネージャ
	 */
	public static PerformanceProperties getProperties() {
		if (m_instance==null) {
			m_instance = new PerformanceProperties();
		}
		return m_instance;
	}

	/**
	 * @return m_startSecond を戻します。
	 */
	public int getStartSecond() {
		/** プロセス監視値取得開始時間（秒） */
		return HinemosPropertyUtil.getHinemosPropertyNum("monitor.resource.start.second", 30);
	}
}
