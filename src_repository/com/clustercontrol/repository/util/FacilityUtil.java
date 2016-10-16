/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.repository.util;

import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityInfo;
import com.clustercontrol.repository.model.FacilityEntity;

/**
 * リポジトリに関するUtilityクラス<br/>
 *
 *
 */
public class FacilityUtil {

	/**
	 * FacilityEntityのNode判定
	 */
	public static boolean isNode(FacilityEntity entity) {
		if (entity.getFacilityType() != null && entity.getFacilityType() == FacilityConstant.TYPE_NODE) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * FacilityInfoのNode判定
	 */
	public static boolean isNode_FacilityInfo(FacilityInfo info) {
		if (info.getFacilityType() != null && info.getFacilityType() == FacilityConstant.TYPE_NODE) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * FacilityEntityのScope判定
	 */
	public static boolean isScope(FacilityEntity entity) {
		if (entity.getFacilityType() != null && entity.getFacilityType() == FacilityConstant.TYPE_SCOPE) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * FacilityInfoのScope判定
	 */
	public static boolean isScope_FacilityInfo(FacilityInfo info) {
		if (info.getFacilityType() != null && info.getFacilityType() == FacilityConstant.TYPE_SCOPE) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * FacilityEntityのValid判定
	 */
	public static boolean isValid(FacilityEntity entity) {
		if (entity.getValid() != null && entity.getValid() == ValidConstant.TYPE_VALID) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * FacilityInfoのValid判定
	 */
	public static boolean isValid_FacilityInfo(FacilityInfo info) {
		if (info.isValid() != null && info.isValid() == ValidConstant.BOOLEAN_VALID) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * FacilityEntityのValidのint値取得
	 */
	public static int getValid(Boolean boolValid) {
		int rtn;
		if (boolValid != null && boolValid) {
			rtn = ValidConstant.TYPE_VALID;
		} else {
			rtn = ValidConstant.TYPE_INVALID;
		}
		return rtn;
	}

	/**
	 * FacilityEntityのAutoDeviceSearchのint値取得
	 */
	public static int getAutoDeviceSearch(Boolean boolAutoDeviceSearch) {
		int rtn;
		if (boolAutoDeviceSearch != null && boolAutoDeviceSearch) {
			rtn = ValidConstant.TYPE_VALID;
		} else {
			rtn = ValidConstant.TYPE_INVALID;
		}
		return rtn;
	}

}
