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

import java.util.Date;

/**
 * スケジュール定義情報を格納するクラス<br/>
 */
public class SchedulerInfo {

	// スケジューラ名およびグループ名
	public final String name;
	public final String group;

	// スケジューラの開始日時、前回実行日時、次回予定日時
	public final Date startTime;
	public final Date previousFireTime;
	public final Date nextFireTime;

	// pauseされた状態かどうかを示すフラグ
	public final boolean isPaused;

	public SchedulerInfo(String name, String group, Date startTime, Date previousFireTime, Date nextFireTime, boolean isPaused) {
		this.name = name;
		this.group = group;
		this.startTime = startTime;
		this.previousFireTime = previousFireTime;
		this.nextFireTime = nextFireTime;
		this.isPaused = isPaused;
	}
}
