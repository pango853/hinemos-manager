/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.mbean;

import com.clustercontrol.fault.HinemosUnknown;

public interface ManagerMXBean {

	/**
	 * エージェントの管理情報を文字列で返す。<br/>
	 * @return エージェントの管理情報文字列
	 */
	public String getValidAgentStr();

	/**
	 * Pollerの管理情報を文字列で返す。<br/>
	 * @return Pollerの管理情報文字列
	 */
	public String getPollerInfoStr();

	/**
	 * SharedTableの管理情報を文字列で返す。<br/>
	 * @return SharedTableの管理情報文字列
	 */
	public String getSharedTableInfoStr();

	/**
	 * 管理しているスケジューラ情報を文字列で返す。
	 * @return スケジューラ情報文字列
	 * @throws HinemosUnknown
	 */
	public String getSchedulerInfoStr() throws HinemosUnknown;

	/**
	 * セルフチェック機能が最後に動作した日時文字列を返す。<br/>
	 * @return セルフチェック機能が最後に動作した日時文字列を
	 */
	public String getSelfCheckLastFireTimeStr();

	/**
	 * syslogの統計情報を返す。<br/>
	 * @return syslogの統計情報
	 */
	public String getSyslogStatistics();

	/**
	 * snmptrapの統計情報を返す。<br/>
	 * @return snmptrapの統計情報
	 */
	public String getSnmpTrapStatistics();

	/**
	 * 非同期タスクの蓄積数を返す。<br/>
	 * @return 非同期タスクの蓄積数
	 * @throws HinemosUnknown
	 */
	public String getAsyncWorkerStatistics() throws HinemosUnknown;

	/**
	 * 通知抑制の履歴情報（最終重要度および通知日時）をリセットする
	 * @return
	 * @throws HinemosUnknown
	 */
	public String resetNotificationLogger() throws HinemosUnknown;


	/**
	 * ジョブ多重度のキューの状態を出力する。
	 * @return
	 */
	public String getJobQueueStr();
	
	/**
	 * 実行中のジョブセッションレコード数を取得
	 * @return 実行中のジョブセッションレコード数
	 */
	public long getJobRunSessionCount();
	
	/**
	 * snmptrapの処理待ち数を取得
	 * @return snmptrapの処理待ち数
	 */
	public int getSnmpTrapQueueSize();
	
	/**
	 * syslogの処理待ち数を取得
	 * @return syslogの処理待ち数
	 */
	public int getSyslogQueueSize();
	
	/**
	 * WSのQueueサイズを取得
	 * @return WSのQueueサイズ
	 */
	public int getWebServiceQueueSize();
	
	/**
	 * テーブルの物理サイズ（Byte）を取得
	 * @param tableName テーブル名
	 *  
	 * @return テーブルの物理サイズ（Byte）
	 */
	public long getTablePhysicalSize(String tableName);
	
	/**
	 * テーブルのレコード数を取得
	 * @param tableName テーブル名
	 * 
	 * @return テーブルのレコード数
	 */
	public long getTableRecordCount(String tableName);

	/**
	 * JPAのキャッシュを全て出力する。
	 * 現状、hinemos_manager.logに出力されてしまいます。
	 */
	public void printJpaCacheAll();

	/**
	 * リポジトリのキャッシュ情報を、hinemos_manager.logに出力する
	 */
	public void printFacilityTreeCacheAll() ;
	/**
	 * リポジトリのキャッシュ情報をリフレッシュする
	 */
	public void refreshFacilityTreeCache();
}