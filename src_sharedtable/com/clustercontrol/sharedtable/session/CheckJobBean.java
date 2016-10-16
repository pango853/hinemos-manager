/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

 */


package com.clustercontrol.sharedtable.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.plugin.impl.SharedTablePlugin;
import com.clustercontrol.sharedtable.SharedTable;

/**
 *　スケジューラ(Quartz)からキックされるアーカイブ情報削除処理の実行クラス
 * 
 */
public class CheckJobBean {

	private static Log log = LogFactory.getLog( CheckJobBean.class );

	public static final String METHOD_NAME = "run";

	/**
	 * Quartzからのコールバックメソッド<BR>
	 * 
	 * Database commit後に実施すべきユーザ実装が存在するため、トランザクション開始はユーザが制御する。
	 * また、追加実装により、トランザクションの入れ子が予期せず生じることを避けるため、Neverを採用する。
	 * 
	 * @since 4.0.0
	 */
	public void run() {
		// デバッグログ出力
		log.debug("Check Job Execute : start.");

		// 共有テーブルを取得する
		SharedTable sharedTable = SharedTablePlugin.getSharedTable();
		sharedTable.checkUnnecessaryTable();

		// デバッグログ出力
		log.debug("Check Job Execute : end.");
	}

}
