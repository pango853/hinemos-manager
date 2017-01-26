/*

Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.selfcheck.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * Java VMの利用可能なヒープ容量を確認する処理の実装クラス
 */
public class JVMHeapMonitor extends SelfCheckMonitorBase {

	private static Log m_log = LogFactory.getLog( JVMHeapMonitor.class );

	public final String monitorId = "SYS_JVM_HEAP";
	public final String subKey = "";
	public final String application = "SELFCHECK (Java VM Heap)";

	/**
	 * セルフチェック処理名
	 */
	@Override
	public String toString() {
		int jvmMinHeapThreshold = HinemosPropertyUtil.getHinemosPropertyNum("selfcheck.monitoring.jvm.freeheap.threshold", 32);
		return "monitoring jvm free heap (threshold = " + jvmMinHeapThreshold + " [mbyte] )";
	}

	/**
	 * 監視項目ID
	 */
	@Override
	public String getMonitorId() {
		return monitorId;
	}

	/**
	 * Java VMの利用可能なヒープ容量が最小値以上であるかを確認する処理
	 * @return 通知情報（アプリケーション名は未格納）
	 */
	@Override
	public void execute() {
		if(!HinemosPropertyUtil.getHinemosPropertyBool("selfcheck.monitoring.jvm.freeheap", true)) {
			m_log.debug("skip");
			return;
		}
		
		/** ローカル変数 */
		int freeHeapMByte = 0;
		boolean warn = true;

		/** メイン処理 */
		if (m_log.isDebugEnabled()) m_log.debug("monitoring java vm heap size.");

		// 利用可能なヒープ容量をMByte単位で取得する
		freeHeapMByte = (int)(Runtime.getRuntime().freeMemory() / 1024 / 1024);
		int jvmMinHeapThreshold = HinemosPropertyUtil.getHinemosPropertyNum("selfcheck.monitoring.jvm.freeheap.threshold", 32);

		if (jvmMinHeapThreshold >= 0 && freeHeapMByte >= jvmMinHeapThreshold) {
			m_log.debug("size of java vm's free heap is enough. (free heap's size = " + freeHeapMByte + " [MByte], threshold = " + jvmMinHeapThreshold + " [MByte])");
			warn = false;
		}

		if (warn) {
			m_log.info("size of java vm's free heap is low. (free heap's size = " + freeHeapMByte + " [MByte], threshold = " + jvmMinHeapThreshold + " [MByte])");
		}
		if (!isNotify(subKey, warn)) {
			return;
		}

		String[] msgAttr1 = { new Integer(freeHeapMByte).toString(),
				new Integer(jvmMinHeapThreshold).toString() };
		AplLogger aplLogger = new AplLogger(PLUGIN_ID, APL_ID);
		aplLogger.put(MESSAGE_ID, "003", msgAttr1,
				"free heap of jvm (" +
						freeHeapMByte +
						" [mbyte]) is not enough (threshold " +
						jvmMinHeapThreshold +
				" [mbyte]).");

		return;
	}

}
