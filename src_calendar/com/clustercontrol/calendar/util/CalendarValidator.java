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


package com.clustercontrol.calendar.util;

import java.util.Calendar;
import java.util.List;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.model.JobFileCheckEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobScheduleEntity;
import com.clustercontrol.maintenance.model.MaintenanceInfoEntity;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.notify.model.NotifyInfoEntity;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.calendar.bean.CalendarDetailInfo;
import com.clustercontrol.calendar.bean.CalendarInfo;
import com.clustercontrol.calendar.bean.CalendarPatternInfo;
import com.clustercontrol.calendar.bean.YMD;
import com.clustercontrol.calendar.model.CalDetailInfoEntity;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.util.Messages;

/**
 * カレンダの入力チェッククラス
 * 
 * @since 4.0
 */
public class CalendarValidator {

	private static Log m_log = LogFactory.getLog(CalendarValidator.class);
	/**
	 * カレンダ情報(CalendarInfo)の基本設定の妥当性チェック
	 * 
	 * @param calendarInfo
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateCalendarInfo(CalendarInfo calendarInfo) throws InvalidSetting, InvalidRole {
		// calendarId
		CommonValidator.validateId(Messages.getString("calendar.id"), calendarInfo.getId(), 64);

		// calendarName
		CommonValidator.validateString(Messages.getString("calendar.name"), calendarInfo.getName(), true, 1, 256);

		// ownerRoleId
		CommonValidator.validateOwnerRoleId(calendarInfo.getOwnerRoleId(), true, calendarInfo.getId(), HinemosModuleConstant.PLATFORM_CALENDAR);
		
		// description
		CommonValidator.validateString(Messages.getString("description"), calendarInfo.getDescription(), false, 0, 256);

		if (calendarInfo.getValidTimeFrom() == null || calendarInfo.getValidTimeFrom() == 0) {
			m_log.warn("validateCalendarInfo() " + Messages.getString("start"));
			String[] args = { "(" + Messages.getString("start") + ")" };
			throw new InvalidSetting(Messages.getString("message.calendar.24", args));
		}
		if (calendarInfo.getValidTimeTo() == null || calendarInfo.getValidTimeTo() == 0) {
			m_log.warn("validateCalendarInfo() " + Messages.getString("end"));
			String[] args = { "(" + Messages.getString("end") + ")" };
			throw new InvalidSetting(Messages.getString("message.calendar.24", args));
		}
		if (calendarInfo.getValidTimeFrom() >= calendarInfo.getValidTimeTo()) {
			m_log.warn("validateCalendarInfo() " + Messages.getString("end"));
			String[] args = { Messages.getString("time") + "(" + Messages.getString("end") + ")",
					Messages.getString("time") + "(" + Messages.getString("start") + ")" };
			throw new InvalidSetting(Messages.getString("message.calendar.30", args));
		}
		//カレンダ詳細チェック
		for(CalendarDetailInfo detailInfo : calendarInfo.getCalendarDetailList()){
			validdateCalendarDetailInfo(detailInfo, calendarInfo.getOwnerRoleId());
		}

	}
	/**
	 * カレンダ詳細情報（CalendarDetailInfo）の基本設定の妥当性チェック
	 * @param detailInfo
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	private static void validdateCalendarDetailInfo(CalendarDetailInfo detailInfo, String ownerRoleId) throws InvalidSetting, InvalidRole {
		//説明
		CommonValidator.validateString(Messages.getString("description"), detailInfo.getDescription(), false, 0, 256);
		
		//年は必須項目のためチェック
		if (detailInfo.getYear() == null || detailInfo.getYear() < 0) {
			String[] args = { "(" + Messages.getString("year") + ")" };
			m_log.warn("ValidYear:" + args[0]);
			throw new InvalidSetting(Messages.getString("message.calendar.15", args));
		}
		//月は必須項目のためチェック（コンボボックス入力だが、一応）
		if (detailInfo.getMonth() == null || detailInfo.getMonth() < 0) {
			String[] args = { "(" + Messages.getString("month") + ")" };
			m_log.warn("ValidMonth:" + args.toString());
			throw new InvalidSetting(Messages.getString("message.calendar.15", args));
		}
		//日は必須項目のためチェック
		if (detailInfo.getDayType() == null || detailInfo.getDayType() < 0 || detailInfo.getDayType() > 3) {
			String[] args = { "(" + Messages.getString("calendar.detail.date.type") + ")" };
			m_log.warn("ValidDateType:" + args.toString());
			throw new InvalidSetting(Messages.getString("message.calendar.15", args));
		}
		//日タイプが「1」の場合、第x週、曜日が必須項目となるためチェック
		if(detailInfo.getDayType() == 1){
			if(detailInfo.getDayOfWeekInMonth() == null || detailInfo.getDayOfWeekInMonth() < 0){
				String[] args = { "(" + Messages.getString("calendar.detail.xth") + ")" };
				m_log.warn("ValidXth:" + args.toString());
				throw new InvalidSetting(Messages.getString("message.calendar.15", args));
			}
			if(detailInfo.getDayOfWeek() == null || detailInfo.getDayOfWeek() < 1 || detailInfo.getDayOfWeek() > 7){
				String[] args = { "(" + Messages.getString("weekday") + ")" };
				m_log.warn("ValidWeekDay:" + args.toString());
				throw new InvalidSetting(Messages.getString("message.calendar.15", args));
			}
		}
		//日タイプが「2」の場合、日が必須項目となるためチェック
		if(detailInfo.getDayType() == 2){
			if(detailInfo.getDate() == null || detailInfo.getDate() < 0){
				String[] args = { "(" + Messages.getString("monthday") + ")" };
				m_log.warn("ValidMonthDay:" + args.toString());
				throw new InvalidSetting(Messages.getString("message.calendar.15", args));
			}
		}
		//日タイプが「3」の場合、カレンダパターンが必須項目となるためチェック
		if(detailInfo.getDayType() == 3){
			if(detailInfo.getCalPatternId() == null){
				String[] args = { "(" + Messages.getString("calendar.pattern") + ")" };
				m_log.warn("ValidCalendarPattern:" + args.toString());
				throw new InvalidSetting(Messages.getString("message.calendar.15", args));
			}
			//IDと一致するカレンダパターン情報が存在しない場合
			try {
				CalendarPatternCache.getCalendarPatternInfo(detailInfo.getCalPatternId());
				QueryUtil.getCalPatternInfoPK_OR(detailInfo.getCalPatternId(), ownerRoleId);
			} catch (CalendarNotFound e) {
				String[] args = { "(" + detailInfo.getCalPatternId() + ")" };
				m_log.warn("ValidCalendarPattern:" + args.toString());
				throw new InvalidSetting(Messages.getString("message.calendar.16", args));
			} catch (InvalidRole e) {
				m_log.warn("ValidCalendarPattern: "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		if (detailInfo.getAfterday() != null) {
			if (-32768 > detailInfo.getAfterday() || detailInfo.getAfterday() > 32767) {
				String[] args = {Messages.getString("calendar.detail.before.after"),
						"-32768", "32767"};
				m_log.warn("ValidAfterDay:" + args.toString());
				throw new InvalidSetting(Messages.getString("message.calendar.52", args));
			}
		}
		//時間：開始時間、終了時間は必須項目のためチェック
		if (detailInfo.getTimeFrom() == null) {
			String[] args = { "(" + Messages.getString("start") + ")" };
			m_log.warn("ValidTimeFrom :" + args.toString());
			throw new InvalidSetting(Messages.getString("message.calendar.25", args));
		}
		if (detailInfo.getTimeTo() == null) {
			String[] args = { "(" + Messages.getString("end") + ")" };
			m_log.warn("ValidTimeTo :" + args.toString());
			throw new InvalidSetting(Messages.getString("message.calendar.25", args));
		}
		//終了時間が開始時間より過去に設定されてはならないため、チェック
		if (detailInfo.getTimeFrom() >= detailInfo.getTimeTo()) {
			String[] args = { Messages.getString("time") + "(" + Messages.getString("end") + ")",
					Messages.getString("time") + "(" + Messages.getString("start") + ")" };
			m_log.warn("ValidFromTo:" + args.toString());
			throw new InvalidSetting(Messages.getString("message.calendar.31", args));
		}
	}
	/**
	 * カレンダパターン情報（CalendarPatternInfo）の妥当性チェック
	 * @param CalendarPatternInfo
	 * @throws InvalidSetting
	 */
	public static void validateCalendarPatternInfo(CalendarPatternInfo info) throws InvalidSetting{

		// calendarPatternId
		CommonValidator.validateId(Messages.getString("calendar.pattern.id"), info.getId(), 64);

		// calendarPatternName
		CommonValidator.validateString(Messages.getString("calendar.pattern.name"), info.getName(), true, 1, 128);

		// ownerRoleId
		CommonValidator.validateOwnerRoleId(info.getOwnerRoleId(), true,
				info.getId(), HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN);

		//カレンダパターン詳細チェック
		for(YMD ymd : info.getYmd()){
			validateYMD(ymd);
		}
	}

	/**
	 * YMD
	 * YearMonthDayの妥当性チェック
	 * @param ymd
	 * @throws InvalidSetting
	 */
	public static void validateYMD(YMD ymd) throws InvalidSetting{
		Integer year = ymd.getYear();
		Integer month = ymd.getMonth();
		Integer day = ymd.getDay();

		if(year == null){
			String[] args = { "(" + Messages.getString("year") + ")" };
			m_log.warn("validateYMD year=" + year);
			throw new InvalidSetting(Messages.getString("message.calendar.41", args));
		}
		if(month == null || month <= 0){
			String[] args = { "(" + Messages.getString("month") + ")" };
			m_log.warn("validateYMD month=" + month);
			throw new InvalidSetting(Messages.getString("message.calendar.41", args));
		}
		if(day == null || day <= 0){
			String[] args = { "(" + Messages.getString("day") + ")" };
			m_log.warn("validateYMD day=" + day);
			throw new InvalidSetting(Messages.getString("message.calendar.41", args));
		}
		//存在する年月日かチェック
		boolean ret = false;
		Calendar cal = Calendar.getInstance();
		cal.setLenient( true );
		cal.set(year, month - 1, day);
		if (cal.get(Calendar.MONTH) != (month - 1) % 12) {
			// error
			m_log.warn("year=" + year + ",month=" + month + ",day=" + day + ",ret=" + ret);
			String[] args = { year + "/" + month + "/" + day };
			throw new InvalidSetting(Messages.getString("message.calendar.48",args));
		}
		m_log.debug("year=" + year + ",month=" + month + ",day=" + day + ",ret=" + ret);
	}

	/**
	 * 他の機能にて、カレンダが参照状態であるか調査する。
	 * 参照状態の場合、メッセージダイアログが出力される。
	 * @param calendarId
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public static void valideDeleteCalendar(String calendarId) throws InvalidSetting, HinemosUnknown{
		try{
			//ジョブ
			List<JobMstEntity> jobMstList =
					com.clustercontrol.jobmanagement.util.QueryUtil.getJobMstEntityFindByCalendarId(calendarId);
			if (jobMstList != null) {
				for(JobMstEntity jobMst : jobMstList){
					m_log.debug("valideDeleteCalendar() target JobMaster " + jobMst.getId().getJobId() + ", calendarId = " + calendarId);
					if(jobMst.getCalendarId() != null){
						String[] args = {jobMst.getId().getJobId(),calendarId};
						throw new InvalidSetting(Messages.getString("message.calendar.44", args));
					}
				}
			}
			//ジョブスケジュール
			List<JobScheduleEntity> jobScheduleList =
					com.clustercontrol.jobmanagement.util.QueryUtil.getJobScheduleEntityFindByCalendarId_NONE(calendarId);
			if (jobScheduleList != null) {
				for(JobScheduleEntity jobSchedule : jobScheduleList){
					m_log.debug("valideDeleteCalendar() target jobschedule " + jobSchedule.getScheduleId() + ", calendarId = " + calendarId);
					if(jobSchedule.getCalendarId() != null){
						String[] args = {jobSchedule.getScheduleId(),calendarId};
						throw new InvalidSetting(Messages.getString("message.calendar.42", args));
					}
				}
			}
			//ジョブファイルチェック
			List<JobFileCheckEntity> jobFileCheckList =
					com.clustercontrol.jobmanagement.util.QueryUtil.getJobFileCheckEntityFindByCalendarId_NONE(calendarId);
			if (jobFileCheckList != null) {
				for(JobFileCheckEntity jobFileCheck : jobFileCheckList){
					m_log.debug("valideDeleteCalendar() target jobFileCheck " + jobFileCheck.getScheduleId() + ", calendarId = " + calendarId);
					if(jobFileCheck.getCalendarId() != null){
						String[] args = {jobFileCheck.getScheduleId(),calendarId};
						throw new InvalidSetting(Messages.getString("message.calendar.43", args));
					}
				}
			}
			//監視設定
			List<MonitorInfoEntity> monitorList =
					com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoEntityFindByCalendarId_NONE(calendarId);
			if (monitorList != null) {
				for(MonitorInfoEntity monitorInfo : monitorList){
					m_log.debug("valideDeleteCalendar() target MonitorInfo " + monitorInfo.getMonitorId() + ", calendarId = " + calendarId);
					if(monitorInfo.getCalendarId() != null){
						String[] args = {monitorInfo.getMonitorId(),calendarId};
						throw new InvalidSetting(Messages.getString("message.calendar.45", args));
					}
				}
			}
			//メンテナンス
			List<MaintenanceInfoEntity> maintenanceInfoList =
					com.clustercontrol.maintenance.util.QueryUtil.getMaintenanceInfoFindByCalendarId_NONE(calendarId);
			if (maintenanceInfoList != null) {
				for(MaintenanceInfoEntity maintenanceInfo : maintenanceInfoList){
					m_log.debug("valideDeleteCalendar() target MaintenanceInfo " + maintenanceInfo.getMaintenanceId() + ", calendarId = " + calendarId);
					if(maintenanceInfo.getCalendarId() != null){
						String[] args = {maintenanceInfo.getMaintenanceId(),calendarId};
						throw new InvalidSetting(Messages.getString("message.calendar.46", args));
					}
				}
			}
			//通知
			List<NotifyInfoEntity> notifyInfoList =
					com.clustercontrol.notify.util.QueryUtil.getNotifyInfoFindByCalendarId_NONE(calendarId);
			if (notifyInfoList != null) {
				for(NotifyInfoEntity notifyInfo : notifyInfoList){
					m_log.debug("valideDeleteCalendar() target MaintenanceInfo " + notifyInfo.getNotifyId() + ", calendarId = " + calendarId);
					if(notifyInfo.getCalendarId() != null){
						String[] args = {notifyInfo.getNotifyId(), calendarId};
						throw new InvalidSetting(Messages.getString("message.calendar.73", args));
					}
				}
			}
			
		} catch (InvalidSetting e) {
			throw e;
		} catch (Exception e) {
			HinemosUnknown e1 = new HinemosUnknown(e.getMessage(), e);
			m_log.warn("valideDeleteCalendar() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e1;
		}
	}
	/**
	 * 削除対象のカレンダパターンがカレンダにて使用中の場合、
	 * DBコミット前に、メッセージダイアログを出力し中止する
	 * @param calPatternId
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public static void valideDeleteCalendarPattern(String calPatternId) throws InvalidSetting, HinemosUnknown{
		List<CalDetailInfoEntity> calDetailList = null;
		try{
			//カレンダパターンIDと一致するカレンダ詳細情報を取得する
			calDetailList = QueryUtil.getCalDetailByCalPatternId(calPatternId);
			/*
			 * カレンダパターンIDと一致したカレンダ詳細情報を取得した場合に、
			 * メッセージダイアログ出力する
			 * nullの場合は、何もせずにreturn
			 */
			if(calDetailList != null){
				for(CalDetailInfoEntity calDtail : calDetailList){
					m_log.warn("valideDeleteCalendarPattern() target CalendarDetailInfo " + calDtail.getId().getCalendarId() + ", calendarId = " + calPatternId);
					if(calDtail.getCalPatternId() != null){
						String[] args = {calDtail.getCalPatternId(),calPatternId};
						throw new InvalidSetting(Messages.getString("message.calendar.47", args));
					}
				}
			}
		} catch (InvalidSetting e) {
			throw e;
		} catch (Exception e) {
			HinemosUnknown e1 = new HinemosUnknown(e.getMessage(), e);
			m_log.warn("valideDeleteCalendar() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e1;
		}
	}
}
