/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.plugin.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
import com.clustercontrol.sharedtable.SharedTable;
import com.clustercontrol.sharedtable.session.CheckJobBean;

/**
 * 各種機能で共有される情報を保持するSharedTableを管理するプラグイン.
 */
public class SharedTablePlugin implements HinemosPlugin {

	public static final Log log = LogFactory.getLog(SharedTablePlugin.class);

	private static SharedTable _sharedTable = SharedTable.getInstance();

	/** 古い情報のGCなどを行うメンテナンス処理の定義 */
	public static final String _checkJobName = "CHECK_JOB";
	public static final String _checkJobGroup = "SHAREDTABLE";
	public static final String _checkJobCronExpression = "34 */10 * * * ? *"; // チェックジョブの起動条件


	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(Log4jReloadPlugin.class.getName());
		return dependency;
	}

	@Override
	public void create() {
		/** 情報の保持期間(GC対象とするかどうかの判断基準) */
		long _keepAliveSec = HinemosPropertyUtil.getHinemosPropertyNum(
				"common.plugin.sharedtable.keepalive", 60000).longValue();
		_sharedTable.setKeepAlive(_keepAliveSec);

	}

	@Override
	public void activate() {
		schedulerCheckJob();
	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// do nothing
	}

	public static SharedTable getSharedTable() {
		return _sharedTable;
	}

	private void schedulerCheckJob() {
		try {
			// SimpleTrigger でジョブをスケジューリング登録
			// 監視も収集も無効の場合、登録後にポーズするようにスケジュール
			SchedulerPlugin.scheduleCronJob(SchedulerType.RAM, _checkJobName, _checkJobGroup, new Date(),
					_checkJobCronExpression, true, CheckJobBean.class.getName(), CheckJobBean.METHOD_NAME,
					new Class[0], new Serializable[0]);

		} catch (HinemosUnknown e) {
			log.warn(e.getMessage(), e);
		}
	}

}
