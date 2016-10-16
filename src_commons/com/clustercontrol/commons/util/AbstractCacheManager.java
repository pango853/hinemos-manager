/*

Copyright (C) 2015 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.commons.util;

public abstract class AbstractCacheManager implements ICacheManager {
	
	public static final String KEY_ACCESS_ROLE_USER = "ACCESS_ROLE_USER";
	public static final String KEY_ACCESS_USER_ROLE = "ACCESS_USER_ROLE";
	public static final String KEY_ACCESS_ROLE_SYSTEMPRIVILEGE = "ACCESS_ROLE_SYSTEMPRIVILEGE";
	
	public static final String KEY_NOTIFY_INFO = "NOTIFY_INFO";
	public static final String KEY_NOTIFY_DETAIL = "NOTIFY_DETAIL";
	public static final String KEY_NOTIFY_MONITOR_STATUS = "NOTIFY_MONITOR_STATUS";
	public static final String KEY_NOTIFY_RELATION = "NOTIFY_RELATION";
	
	public static final String KEY_CALENDAR = "CALENDAR";
	public static final String KEY_CALENDAR_PATTERN = "CALENDAR_PATTERN";
	
	public static final String KEY_CUSTOM = "CUSTOM";
	
	public static final String KEY_AGENT = "AGENT";
	public static final String KEY_AGENT_TOPIC = "AGENT_TOPIC";
	public static final String KEY_AGENT_LIBMD5 = "AGENT_LIBMD5";
	
	public static final String KEY_JOB_INFO = "JOB_INFO";
	public static final String KEY_JOB_MST = "JOB_MST";
	public static final String KEY_JOB_KICK = "JOB_KICK";
	public static final String KEY_JOB_WAITING = "JOB_WAITING";
	public static final String KEY_JOB_RUNNING = "JOB_RUNNING";
	public static final String KEY_JOB_FORCE_CHECK = "JOB_FORCE_CHECK";
	
	public static final String KEY_LOGFILE = "LOGFILE";
	
	public static final String KEY_COMMON_PROPERTY = "COMMON_PROPERTY";
	public static final String KEY_COMMON_SETTING_UPDATE = "COMMON_SETTING_UPDATE";
	public static final String KEY_COMMON_SHAREDTABLE = "COMMON_SHAREDTABLE";
	public static final String KEY_COMMON_POLLERTABLE = "COMMON_POLLERTABLE";
	
	public static final String KEY_SYSTEMLOG = "SYSTEMLOG";
	
	public static final String KEY_PERFORMANCE_CATEGORY_COLLECT = "PERFORMANCE_CATEGORY_COLLECT";
	public static final String KEY_PERFORMANCE_ITEM_CODE = "PERFORMANCE_ITEM_CODE";
	public static final String KEY_PERFORMANCE_ITEM_CALC_METHOD = "PERFORMANCE_ITEM_CALC_METHOD";
	public static final String KEY_PERFORMANCE_POLLING_TARGET = "PERFORMANCE_POLLING_TARGET";
	public static final String KEY_PERFORMANCE_PREVIOUS_VALUE = "PERFORMANCE_PREVIOUS_VALUE";
	
	public static final String KEY_POLLING_SKIP_TIME = "POLLING_SKIP_TIME";
	
	public static final String KEY_PROCESS_POLLING = "PROCESS_POLLING";
	
	public static final String KEY_REPOSITORY_NODE = "REPOSITORY_NODE";
	public static final String KEY_REPOSITORY_FACILITY = "REPOSITORY_FACILITY";
	public static final String KEY_REPOSITORY_TREE_ROOT = "REPOSITORY_TREE_ROOT";
	public static final String KEY_REPOSITORY_TREE_ITEM = "REPOSITORY_TREE_ITEM";
	public static final String KEY_REPOSITORY_HOSTNAME_IPADDR_FACILITYID = "REPOSITORY_HOSTNAME_IPADDR_FACILITYID";
	public static final String KEY_REPOSITORY_NODENAME_FACILITYID = "REPOSITORY_NODENAME_FACILITYID";
	public static final String KEY_REPOSITORY_HOSTNAME_FACILITYID = "REPOSITORY_HOSTNAME_FACILITYID";
	public static final String KEY_REPOSITORY_IPADDR_FACILITYID = "REPOSITORY_IPADDR_FACILITYID";
	public static final String KEY_REPOSITORY_SCOPE_NODE_FACILITYID = "REPOSITORY_SCOPE_NODE_FACILITYID";
	
	public static final String KEY_WINEVENT = "WINEVENT";
	
}
