package com.clustercontrol.maintenance.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.maintenance.HinemosPropertyTypeConstant;
import com.clustercontrol.maintenance.bean.HinemosPropertyInfo;
import com.clustercontrol.maintenance.model.HinemosPropertyEntity;
import com.clustercontrol.maintenance.session.HinemosPropertyControllerBean;

public class HinemosPropertyUtil {

	// 定数定義
	private static Log m_log = LogFactory.getLog (HinemosPropertyUtil.class);

	/**
	 * 共通設定情報より値（文字列）を取得します。
	 * @param key
	 * @param defaultValue
	 * @return 値（文字列）
	 */
	public static String getHinemosPropertyStr (String key, String defaultValue) {
		HinemosPropertyInfo info = new HinemosPropertyControllerBean().getHinemosPropertyInfo_None(key);
		if (info == null) {
			return defaultValue;
		}

		if (info.getValueType() == HinemosPropertyTypeConstant.TYPE_STRING
				&& info.getValueString() != null && !"".equals(info.getValueString())) {
			return info.getValueString();
		}
		return defaultValue;
	}

	/**
	 * 共通設定情報より値（数値）を取得します。
	 * @param key
	 * @param defaultValue
	 * @return 値（数値）
	 */
	public static Integer getHinemosPropertyNum (String key, Integer defaultValue) {
		Integer ret = defaultValue;
		HinemosPropertyInfo info = new HinemosPropertyControllerBean().getHinemosPropertyInfo_None(key);
		if (info == null) {
			return defaultValue;
		}

		if (info.getValueType() == HinemosPropertyTypeConstant.TYPE_NUMERIC
				&& info.getValueNumeric() != null) {
			m_log.trace(key + "=" + ret);
			return info.getValueNumeric();
		}
		return defaultValue;
	}

	/**
	 * 共通設定情報より値（論理値）を取得します。
	 * @param key
	 * @param defaultValue
	 * @return 値（論理値）
	 */
	public static Boolean getHinemosPropertyBool(String key, Boolean defaultValue) {
		HinemosPropertyInfo info = new HinemosPropertyControllerBean().getHinemosPropertyInfo_None(key);
		if (info == null) {
			return defaultValue;
		}

		if (info.getValueType() == HinemosPropertyTypeConstant.TYPE_TRUTH
				&& info.isValueBoolean() != null) {
			return info.isValueBoolean();
		}
		return defaultValue;
	}

	/**
	 * HinemosPropertyEntityの情報に基づいて、HinemosPropertyInfoを生成する
	 *
	 * @return HinemosPropertyInfoオブジェクト
	 */
	public static HinemosPropertyInfo createHinemosPropertyInfo (
			HinemosPropertyEntity entity) {
		HinemosPropertyInfo info = new HinemosPropertyInfo();
		info.setKey(entity.getKey());
		info.setValueString(entity.getValueString());
		if (entity.getValueNumeric() != null) {
			info.setValueNumeric(entity.getValueNumeric());
		} else {
			info.setValueNumeric(null);
		}

		info.setValueBoolean(entity.getValueBoolean());
		info.setValueType(entity.getValueType());
		info.setDescription(entity.getDescription());
		info.setOwnerRoleId(entity.getOwnerRoleId());
		if (entity.getCreateDatetime() == null) {
			info.setCreateDatetime(null);
		} else {
			info.setCreateDatetime(entity.getCreateDatetime().getTime());
		}
		info.setCreateUserId(entity.getCreateUserId());
		if (entity.getModifyDatetime() == null) {
			info.setModifyDatetime(null);
		} else {
			info.setModifyDatetime(entity.getModifyDatetime().getTime());
		}
		info.setModifyUserId(entity.getModifyUserId());

		return info;
	}
}
