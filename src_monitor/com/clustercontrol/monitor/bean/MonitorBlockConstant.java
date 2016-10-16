/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.bean;

import com.clustercontrol.util.Messages;

/**
 * 監視単位の定義を定数として格納するクラス<BR>
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
public class MonitorBlockConstant {
	/** ノード（種別） */
	public static final int TYPE_NODE = 0;

	/** スコープ（種別） */
	public static final int TYPE_SCOPE = 1;

	/** スコープとノード（種別） */
	public static final int TYPE_ALL = 2;


	/** ノード（文字列） */
	private static final String STRING_NODE = Messages.getString("node");

	/** スコープ（文字列） */
	private static final String STRING_SCOPE = Messages.getString("scope");

	/** スコープとノード（文字列） */
	private static final String STRING_ALL = Messages.getString("scope.node");

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type) {
		if (type == TYPE_NODE) {
			return STRING_NODE;
		} else if (type == TYPE_SCOPE) {
			return STRING_SCOPE;
		} else if (type == TYPE_ALL) {
			return STRING_ALL;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 * 
	 * @param string 文字列
	 * @return 種別
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_NODE)) {
			return TYPE_NODE;
		} else if (string.equals(STRING_SCOPE)) {
			return TYPE_SCOPE;
		} else if (string.equals(STRING_ALL)) {
			return TYPE_ALL;
		}
		return -1;
	}
}