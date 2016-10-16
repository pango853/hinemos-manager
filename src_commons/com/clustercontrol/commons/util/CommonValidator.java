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

package com.clustercontrol.commons.util;

import java.math.BigDecimal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.bean.PatternConstant;
import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.commons.bean.Schedule;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.RoleNotFound;
import com.clustercontrol.util.Messages;

/**
 * Hinemos の入力チェックで使用する共通メソッド
 * @since 4.0
 */
public class CommonValidator {

	private static Log m_log = LogFactory.getLog( CommonValidator.class );

	/**
	 * 指定されたIDがHinemosのID規則にマッチするかを確認する。
	 * [a-z,A-Z,0-9,-,_,.,@]のみ許可する (Hinemos5.0で「.」と「@」を追加)
	 * 
	 * @param id
	 * @throws InvalidSetting
	 */
	public static void validateId(String name, String id, int maxSize) throws InvalidSetting{

		// null check
		if(id == null || "".equals(id)){
			Object[] args = { name };
			InvalidSetting e = new InvalidSetting(Messages.getString("message.common.5", args));
			m_log.info("validateId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// string check
		validateString(name, id, false, 1, maxSize);

		/** メイン処理 */
		if(!id.matches(PatternConstant.HINEMOS_ID_PATTERN)){
			Object[] args = { id, name };
			InvalidSetting e = new InvalidSetting(Messages.getString("message.common.6", args));
			m_log.info("validateId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * 文字列の長さチェック
	 * 
	 * @param name
	 * @param str
	 * @param nullcheck
	 * @param minSize
	 * @param maxSize
	 * @throws InvalidSetting
	 */
	public static void validateString(String name, String str, boolean nullcheck, int minSize, int maxSize) throws InvalidSetting{
		if(str == null){
			if(nullcheck){
				Object[] args = { name };
				InvalidSetting e = new InvalidSetting(Messages.getString("message.common.1", args));
				m_log.info("validateString() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		else{
			int size = str.length();
			if(size < minSize){
				if(size == 0){
					Object[] args = { name };
					InvalidSetting e = new InvalidSetting(Messages.getString("message.common.1", args));
					m_log.info("validateString() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}else{
					Object[] args = { name, Integer.toString(minSize) };
					InvalidSetting e = new InvalidSetting(Messages.getString("message.common.3", args));
					m_log.info("validateString() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}

			if(size > maxSize){
				Object[] args = { name, Integer.toString(maxSize) };
				InvalidSetting e = new InvalidSetting(Messages.getString("message.common.2", args));
				m_log.info("validateString() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
	}

	/**
	 * 数値の上限下限チェック
	 * @throws InvalidSetting
	 */
	public static void validateDouble(String name, double i, double minSize, double maxSize) throws InvalidSetting {
		if (i < minSize || maxSize < i) {
			Object[] args = {name,
					((new BigDecimal(minSize)).toBigInteger()).toString(),
					((new BigDecimal(maxSize)).toBigInteger()).toString()};
			InvalidSetting e = new InvalidSetting(Messages.getString("message.common.4", args));
			m_log.info("validateDouble() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * 数値の上限下限チェック
	 * @throws InvalidSetting
	 */
	public static void validateInt(String name, int i, int minSize, int maxSize) throws InvalidSetting {
		if (i < minSize || maxSize < i) {
			Object[] args = {name, Integer.toString(minSize), Integer.toString(maxSize)};
			InvalidSetting e = new InvalidSetting(Messages.getString("message.common.4", args));
			m_log.info("validateInt() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * calendarIdがnullまたは、対象のカレンダ設定が存在するかを確認する
	 * 
	 * @param calendarId
	 * @param nullcheck
	 * @param ownerRoleId
	 * 
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateCalenderId(String calendarId, boolean nullcheck, String ownerRoleId) throws InvalidSetting, InvalidRole {

		if(calendarId == null || "".equals(calendarId)){
			if(nullcheck){
				InvalidSetting e = new InvalidSetting(Messages.getString("message.common.7"));
				m_log.info("validateCalenderId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return;
		}
		else{
			try {
				//対象のカレンダ設定を取得できない場合にExceptionエラーを発生させる。
				com.clustercontrol.calendar.util.QueryUtil.getCalInfoPK_OR(calendarId, ownerRoleId);
			} catch (CalendarNotFound e) {
				Object[] args = {calendarId};
				m_log.warn("validateCalenderId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw new InvalidSetting(Messages.getString("message.common.8", args));
			} catch (InvalidRole e) {
				m_log.warn("validateCalenderId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		return;
	}

	/**
	 * notifyIdがnullまたは、対象の通知設定が存在するかを確認する
	 * 
	 * @param notifyId
	 * @param nullcheck
	 * @param ownerRoleId
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateNotifyId(String notifyId, boolean nullcheck, String ownerRoleId) throws InvalidSetting, InvalidRole {

		if(notifyId == null){
			if(nullcheck){
				InvalidSetting e = new InvalidSetting(Messages.getString("message.common.9"));
				m_log.info("validateNotifyId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return;
		}
		else{
			try {
				com.clustercontrol.notify.util.QueryUtil.getNotifyInfoPK_OR(notifyId, ownerRoleId);
			} catch (NotifyNotFound e) {
				Object[] args = {notifyId};
				throw new InvalidSetting(Messages.getString("message.common.10", args));
			}
		}
		return;
	}

	/**
	 * schedule型のチェック
	 */
	public static void validateScheduleHour(Schedule schedule) throws InvalidSetting {
		validateSchedule(schedule);
		// 分だけでなく、時も必須。
		if (schedule.getHour() == null ||
				schedule.getHour() < 0 || 24 <= schedule.getHour()) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.job.28"));
			m_log.info("validateScheduleHour() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * schedule型のチェック
	 */
	public static void validateSchedule(Schedule schedule) throws InvalidSetting {
		boolean emptyFlag = true;
		if (schedule.getType() == ScheduleConstant.TYPE_DAY) {
			if (schedule.getMonth() != null) {
				emptyFlag = false;
				if (schedule.getMonth() < 0 || 12 < schedule.getMonth()) {
					InvalidSetting e = new InvalidSetting(Messages.getString("message.job.26"));
					m_log.info("validateSchedule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			if (schedule.getDay() != null) {
				emptyFlag = false;
				if (schedule.getDay() < 0 || 31 < schedule.getDay()) {
					InvalidSetting e = new InvalidSetting(Messages.getString("message.job.27"));
					m_log.info("validateSchedule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} else if (!emptyFlag){
				// 月を入力した場合は日も必須。
				InvalidSetting e = new InvalidSetting(Messages.getString("message.job.27"));
				m_log.info("validateSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} else if (schedule.getType() == ScheduleConstant.TYPE_WEEK) {
			if (schedule.getWeek() == null ||
					schedule.getWeek() < 0 || 7 < schedule.getWeek()) {
				InvalidSetting e = new InvalidSetting(Messages.getString("message.job.37"));
				m_log.info("validateSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} else {
			InvalidSetting e = new InvalidSetting("unknown schedule type");
			m_log.info("validateSchedule() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		if (schedule.getHour() != null) {
			emptyFlag = false;
			if (schedule.getHour() < 0 || 24 < schedule.getHour()) {
				InvalidSetting e = new InvalidSetting(Messages.getString("message.job.28"));
				m_log.info("validateSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} else if (!emptyFlag){
			// 日を入力した場合は時間も必須。
			InvalidSetting e = new InvalidSetting(Messages.getString("message.job.28"));
			m_log.info("validateSchedule() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		if (schedule.getMinute() != null) {
			emptyFlag = false;
			if (schedule.getMinute() < 0 || 60 < schedule.getMinute()) {
				InvalidSetting e = new InvalidSetting(Messages.getString("message.job.29"));
				m_log.info("validateSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} else {
			// 分は必須。
			InvalidSetting e = new InvalidSetting(Messages.getString("message.job.29"));
			m_log.info("validateSchedule() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * ownerRoleIdがnullまたは、対象のロールが存在するかを確認する
	 * さらに、変更時には、オーナーロールIDが変更されていないかを確認する
	 * 
	 * @param ownerRoleId
	 * @param nullcheck
	 * @param objectType
	 * @throws InvalidSetting
	 */
	public static void validateOwnerRoleId(String ownerRoleId, boolean nullcheck, Object pk, String objectType) throws InvalidSetting {

		if(ownerRoleId == null){
			if(nullcheck){
				InvalidSetting e = new InvalidSetting(Messages.getString("message.accesscontrol.54"));
				m_log.info("validateOwnerRoleId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return;
		}
		else{
			try {
				// 存在確認
				com.clustercontrol.accesscontrol.util.QueryUtil.getRolePK(ownerRoleId);

				// 変更時にオーナーロールIDが変わっていないか確認する
				RoleValidator.validateModifyOwnerRole(pk, objectType, ownerRoleId);
			} catch (RoleNotFound e) {
				throw new InvalidSetting(e.getMessage(), e);
			}
		}
		return;
	}


	/**
	 * for debug
	 * @param args
	 */
	public static void main(String[] args) {

		String id;

		// OKのパターン(半角英数字、-(半角ハイフン)、_(半角アンダーバー))
		id = "Linux-_1";
		try{
			validateId("name", id , 64);
			System.out.println("id = " + id + " is OK");
		} catch (Exception e) {
			System.out.println("???");
			e.printStackTrace();
		}

		// NGのパターン
		id = "/?/";
		try{
			validateId("name", id , 64);
		} catch (Exception e) {
			System.out.println("id = " + id + " is NG");
		}

	}


}
