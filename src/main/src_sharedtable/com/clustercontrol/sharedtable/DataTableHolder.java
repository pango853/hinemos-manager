/*

Copyright (C) 2008 NTT DATA Corporation

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
import java.util.ArrayList;
import java.util.List;

/**
 * 複数のデータテーブルを管理するクラス
 * ページサイズで指定された数のデータテーブルをリストで保持する
 * 最後に挿入されたデータテーブルがリストの先頭（ページ番号 0）となる
 */
public class DataTableHolder implements Serializable {

	private ArrayList<DataTable> m_list;

	private volatile String m_checkKey = null;

	private final int m_pageSize;

	// 挿入回数を保持
	private long _insertCount;

	/**
	 * 指定のページサイズでインスタンスを生成します。
	 * @param pageSize ページサイズ
	 */
	protected DataTableHolder(int pageSize){
		m_pageSize = pageSize;
		m_list = new ArrayList<DataTable>(pageSize);
		//		m_list = new CopyOnWriteArrayList<DataTable>();

		// 空のテーブルを設置する
		for(int i=0; i < m_pageSize; i++){
			m_list.add(new DataTable());
		}

		// 挿入回数を初期化
		_insertCount = 0;
	}

	/**
	 * データテーブルを挿入します。
	 * 挿入されたデータテーブルがリストの先頭（ページ番号 0）となります。
	 * 但し、同一のキーで既に登録されているデータテーブルの中に、挿入対象のデータテーブルより
	 * 新しい（後に作成された）データテーブルがある場合、挿入は実施されません。
	 * 
	 * checkKeyは、テーブルホルダで管理されているページのキーと同じキーを指定する必要があります。
	 * 異なるキーで挿入された場合は、テーブルホルダの全てのページがクリアされ、
	 * 今回与えられたデータテーブルのみが先頭（ページ番号 0）に存在する状態になります。
	 * 
	 * @param table データテーブル
	 * @param checkKey テーブルホルダにデータを格納する際のキー
	 */
	protected void insertDataTable(DataTable table, String checkKey){
		// 挿入するテーブルが挿入済みのテーブルより新しいか否かをチェックする（古ければ何もせず抜ける）
		DataTable lastTable = m_list.get(0);
		if (lastTable != null && lastTable.getCreateTime() > table.getCreateTime()) {
			return;
		}

		if(m_checkKey == null){
			// 新規にキーを設定
			m_checkKey = checkKey;
		}

		// 保持しているcheckKeyと異なるキーの場合
		if(!m_checkKey.equals(checkKey)){
			// ページを全てクリアする
			m_list.clear();

			// 空のテーブルを設置する
			for(int i=0; i < m_pageSize-1; i++){
				m_list.add(new DataTable());
			}
			_insertCount = 0;

			// 新規にキーを設定
			m_checkKey = checkKey;
		}

		if(m_list.size() == m_pageSize){
			m_list.remove(m_pageSize-1);
		}
		m_list.add(0, table);

		// 挿入回数をカウントアップ
		_insertCount++;
	}

	/**
	 * 指定のページのデータテーブルを返します。
	 * @param page ページ番号
	 * @return データテーブル
	 */
	protected DataTable get(int page) {
		if (m_list.size() <= page) {
			return null;
		}

		return m_list.get(page);
	}

	/**
	 * 先頭から指定ページ分のデータテーブルを返します。
	 * @param page ページ数
	 * @return データテーブル
	 */
	protected List<DataTable> getLast(int pageSize) {
		if (m_list.size() < pageSize) {
			return null;
		}

		ArrayList<DataTable> list = new ArrayList<DataTable>(m_pageSize);
		list.addAll(m_list.subList(0, pageSize));

		return list;
	}

	/**
	 * ページ数を返します。
	 * @return ページ数
	 */
	protected int getPageSize() {
		return m_pageSize;
	}

	/**
	 * DataTableの挿入回数を返します。
	 * @return DataTableの挿入回数
	 */
	protected long getInsertCount(){
		return _insertCount;
	}
}
