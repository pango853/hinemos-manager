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

package com.clustercontrol.sharedtable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * キーとオブジェクトのマッピングを保持するクラス
 * 
 * @version 3.0.0
 * @since 2.0.0
 */
public class DataTable implements Cloneable, Serializable {

	// データテーブルが作成された時刻（データテーブルホルダーではこれを基にデータの有効・無効を決定する）
	private final long createTime = System.currentTimeMillis();

	// キーと値オブジェクトのマッピングを保持するマップ
	private final ConcurrentHashMap<String, TableEntry> m_hm =
			new ConcurrentHashMap<String,TableEntry>();

	private volatile long lastModify;

	/**
	 * 新しい空のテーブルを作成します。
	 */
	public DataTable(){
	}

	/**
	 * 指定された値と指定されたキーをこのテーブルに関連付けます。
	 * 
	 * @param key 指定される値が関連付けられるキー
	 * @param date 指定される値に関連付けられる時刻
	 * @param value 指定されるキーに関連付けられる値
	 */
	public void putValue(String key, long date, Serializable value) {
		m_hm.put(key, new TableEntry(key, date, value));
		lastModify = System.currentTimeMillis();
	}

	/**
	 * エントリをこのテーブルに関連付けます。
	 * 
	 * @param entry 登録するエントリ
	 */
	public void putValue(TableEntry entry) {
		m_hm.put(entry.getKey(), entry);
		lastModify = System.currentTimeMillis();
	}

	/**
	 * テーブル内のエントリをこのテーブルに関連付けます。
	 * 
	 * @param table 追加登録するテーブル
	 */
	public void putAll(DataTable table) {
		m_hm.putAll(table.m_hm);
		lastModify = System.currentTimeMillis();
	}

	/**
	 * 指定されたキーにマップされている値を返します。
	 * 
	 * @param key 関連付けられた値が返されるキー
	 * @return 指定されたキーにマッピングしている値オブジェクト。
	 */
	public TableEntry getValue(String key){
		TableEntry entry = m_hm.get(key);
		return entry;
	}

	/**
	 * キー（文字列）にマッピングされている値のうち、
	 * 指定された接頭辞で始まるキーにマッピングされている値のセットを返します。
	 * 
	 * @param prefix 接頭辞
	 * @return 指定された接頭辞で始まるキーで取得できる値のセット
	 * 値を保持しているが指定の接頭辞で始まるキーのものが存在しない場合は空のセットを返す
	 * 値をまったく保持していない場合は、nullを返す
	 */
	public Set<TableEntry> getValueSetStartWith(String prefix) {
		if (m_hm.size() == 0) {
			return null;
		}
		final Set<TableEntry> set = new HashSet<TableEntry>();
		// キーとされているフルのOIDを取得しそのフルOIDの文字列の先頭部分が、
		// 引数指定のOIDである場合は、その値を戻りのセットに格納する
		for (final java.util.Map.Entry<String,TableEntry> entry : m_hm.entrySet()) {
			if (entry.getKey().startsWith(prefix + ".")) {
				set.add(entry.getValue());
			}
		}
		return set;
	}

	/**
	 * キーのセットを返します。
	 * @return キーのセット
	 */
	public Set<String> keySet(){
		return this.m_hm.keySet();
	}

	/**
	 * 全てのマッピングをマップから削除します。
	 */
	public void clear(){
		m_hm.clear();
		lastModify = System.currentTimeMillis();
	}

	/**
	 * 最終更新日時を返します。
	 * @return 最終更新日時
	 */
	public long getLastModify() {
		return lastModify;
	}

	/**
	 * このデータテーブルが作成された時刻を返します
	 * @return 作成時刻
	 */
	protected long getCreateTime() {
		return createTime;
	}
}
