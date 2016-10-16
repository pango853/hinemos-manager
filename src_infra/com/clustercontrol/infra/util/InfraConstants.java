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
package com.clustercontrol.infra.util;

import java.util.HashMap;
import java.util.Map;

import com.clustercontrol.bean.PriorityConstant;

/**
 * 環境構築関連の定数管理クラス
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraConstants {
	public static final String notifyGroupIdPrefix = "INFRA_";
	
	public static final String MESSAGE_ID_INFO = "001";
	public static final String MESSAGE_ID_WARNING = "002";
	public static final String MESSAGE_ID_CRITICAL = "003";
	public static final String MESSAGE_ID_UNKNOWN = "100";
	public static final String MESSAGE_ID_FAILURE = "200";

	public static final Map<Integer, String> messageIdMap= new HashMap<Integer, String>() {
		private static final long serialVersionUID = 1L;
		{
			put(PriorityConstant.TYPE_INFO, MESSAGE_ID_INFO);
			put(PriorityConstant.TYPE_WARNING, MESSAGE_ID_WARNING);
			put(PriorityConstant.TYPE_CRITICAL, MESSAGE_ID_CRITICAL);
			put(PriorityConstant.TYPE_UNKNOWN, MESSAGE_ID_UNKNOWN);
			put(PriorityConstant.TYPE_FAILURE, MESSAGE_ID_FAILURE);
		}
	};
	
	private InfraConstants() {
	}
}
