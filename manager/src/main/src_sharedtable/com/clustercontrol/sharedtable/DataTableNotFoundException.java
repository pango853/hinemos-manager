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

/**
 * 指定のデータテーブルが見つからない場合の例外クラス
 * 
 * @version 3.0.0
 * @since 3.0.0
 */
public class DataTableNotFoundException extends Exception {
	private static final long serialVersionUID = -3612128760360182329L;

	private String m_tableGroup;
	private String m_tableName;

	public DataTableNotFoundException(String tableGroup, String tableName){
		m_tableGroup = tableGroup;
		m_tableName = tableName;
	}

	@Override
	public String getMessage() {
		return "TableName:" + m_tableName + "(" + m_tableGroup +") is not found.";
	}
}
