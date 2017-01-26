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

package com.clustercontrol.poller;

/**
 * ポーリング間隔を定義するクラス
 * 
 * @version 3.0.0
 * @since 2.0.0
 */
public class PollingInterval {
	private static final int INT_1SEC  = 1;
	private static final int INT_5SEC  = 5;
	private static final int INT_10SEC = 10;
	private static final int INT_15SEC = 15;
	private static final int INT_30SEC = 30;
	private static final int INT_1MIN  = 60;
	private static final int INT_5MIN  = 300;
	private static final int INT_10MIN = 600;
	private static final int INT_15MIN = 900;
	private static final int INT_30MIN = 1800;
	private static final int INT_60MIN = 3600;

	private static final String[] cronExpressionSet = {
		"*/1 * * * * ? *",
		"*/5 * * * * ? *",
		"*/10 * * * * ? *",
		"*/15 * * * * ? *",
		"*/30 * * * * ? *",
		"0 */1 * * * ? *",
		"0 */5 * * * ? *",
		"0 */10 * * * ? *",
		"0 */15 * * * ? *",
		"0 */30 * * * ? *",
		"0 0 */1 * * ? *"
	};

	// 収集設定可能なインターバル
	private static final int[] intervalSet
	= { INT_1SEC, INT_5SEC, INT_10SEC, INT_15SEC, INT_30SEC, INT_1MIN, INT_5MIN, INT_10MIN, INT_15MIN, INT_30MIN, INT_60MIN };

	public static String parseCronExpression(int interval){
		for(int i=0; i < intervalSet.length; i++){
			if(intervalSet[i] == interval){
				return cronExpressionSet[i];
			}
		}

		// 認められていない収集間隔である
		return null;
	}
}
