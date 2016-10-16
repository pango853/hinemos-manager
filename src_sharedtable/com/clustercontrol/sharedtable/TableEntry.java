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
import java.util.Date;

/**
 * 時刻と値をセットで保持するクラス
 * 
 * @version 3.0.0
 * @since 3.0.0
 */
public class TableEntry implements Serializable, Comparable<TableEntry> {
	private static final long serialVersionUID = -5926453308072828591L;

	private final String key;
	private final long date;
	private final Serializable value;

	public TableEntry(String key, long date, Serializable value){
		this.key = key;
		this.date = date;
		this.value = value;
	}

	/**
	 * 時刻を取得
	 * @return 時刻
	 */
	public long getDate() {
		return date;
	}

	/**
	 * 値を取得
	 * @return 値のオブジェクト
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * キーを取得
	 * @return キー
	 */
	public String getKey() {
		return key;
	}

	/**
	 * 設定されている時刻と値の文字列表現を返します。
	 */
	@Override
	public String toString(){
		return new Date(this.date) + " : "  + key + " : " + this.value.toString();
	}

	/**
	 * このオブジェクトと指定されたオブジェクトの順序を比較します。
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(TableEntry another) {
		long anatherDate = another.getDate();

		if(date < anatherDate){
			return -1;
		} else if (date == anatherDate) {
			// 時刻が同じの場合は、キーの順番
			return key.compareTo(another.getKey());
		} else {
			return 1;
		}
	}
}
