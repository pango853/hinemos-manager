package com.clustercontrol.commons.util;

import java.util.HashMap;


public class HinemosSessionContext {
	// ログインユーザID
	public final static String LOGIN_USER_ID = "loginUserId";
	// ADMINISTRATORSロール所属
	public final static String IS_ADMINISTRATOR = "isAdministrator";
	// オブジェクト権限チェック対象（更新対象、削除対象Entity情報）
	public final static String OBJECT_PRIVILEGE_TARGET_LIST = "objectPrivilegeTargetList";
	private static ThreadLocal<Object> instance  = new ThreadLocal<Object>() {
		@Override
		protected Object initialValue()
		{
			return null;
		}
	};

	HashMap<String, Object> properties;

	public static HinemosSessionContext instance() {
		HinemosSessionContext ctx = (HinemosSessionContext)instance.get();
		if (ctx != null) {
			return ctx;
		}

		instance.set(new HinemosSessionContext());
		return (HinemosSessionContext)instance.get();
	}

	public Object getProperty(String key) {
		Object value = null;
		if (properties != null
				&& key != null
				&& key.length() > 0) {
			value = properties.get(key);
		}
		return value;
	}

	public void setProperty(String key, Object value) {
		if (properties == null) {
			properties = new HashMap<String, Object> ();
		}
		if (key != null && key.length() > 0) {
			properties.put(key, value);
		}
		return;
	}
}

