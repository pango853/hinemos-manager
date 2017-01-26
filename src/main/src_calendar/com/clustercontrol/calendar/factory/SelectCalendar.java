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

package com.clustercontrol.calendar.factory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.calendar.bean.CalendarDetailInfo;
import com.clustercontrol.calendar.bean.CalendarInfo;
import com.clustercontrol.calendar.bean.CalendarPatternInfo;
import com.clustercontrol.calendar.model.CalDetailInfoEntity;
import com.clustercontrol.calendar.model.CalInfoEntity;
import com.clustercontrol.calendar.model.CalPatternInfoEntity;
import com.clustercontrol.calendar.util.CalendarCache;
import com.clustercontrol.calendar.util.CalendarPatternCache;
import com.clustercontrol.calendar.util.CalendarUtil;
import com.clustercontrol.calendar.util.QueryUtil;


/**
 * カレンダを検索するファクトリークラス<BR>
 *
 * @version 4.1.0
 * @since 2.0.0
 */
public class SelectCalendar {

	private static Log m_log = LogFactory.getLog( SelectCalendar.class );

	private static final long TIMEZONE = TimeZone.getDefault().getRawOffset();
	private static final long HOUR24 = 24 * 60 * 60 * 1000;

	/**
	 * カレンダ情報をキャッシュより取得します。
	 * 
	 * @param id
	 * @return カレンダ情報
	 * @throws CalendarNotFound
	 */
	public CalendarInfo getCalendarFromCache(String id) throws CalendarNotFound {
		CalendarInfo ret = null;
		if(id != null && !"".equals(id)){
			ret = CalendarCache.getCalendarInfo(id);
		}

		return ret;
	}

	/**
	 * カレンダ情報を取得します。
	 * 
	 * @param id
	 * @return カレンダ情報
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public CalendarInfo getCalendar(String id) throws CalendarNotFound, InvalidRole {
		CalendarInfo ret = null;
		if(id != null && !"".equals(id)){
			//カレンダ取得
			CalInfoEntity entity = QueryUtil.getCalInfoPK(id);

			//カレンダ情報のDTOを生成
			ret = getCalendarInfoBean(entity);
			//カレンダ詳細情報を追加
			ArrayList<CalendarDetailInfo> detailList = getCalDetailList(entity.getCalendarId());
			ret.getCalendarDetailList().clear();
			ret.getCalendarDetailList().addAll(detailList);

		}

		return ret;
	}

	/**
	 * カレンダ詳細情報一覧を取得します。
	 * @param id
	 * @return カレンダ詳細情報のリスト
	 */
	public ArrayList<CalendarDetailInfo> getCalDetailList(String id) {
		ArrayList<CalendarDetailInfo> list = new ArrayList<CalendarDetailInfo>();

		//カレンダIDの曜日別情報を取得
		List<CalDetailInfoEntity> ct = QueryUtil.getCalDetailByCalendarId(id);

		Iterator<CalDetailInfoEntity> itr = ct.iterator();
		while(itr.hasNext()){
			CalDetailInfoEntity cal = itr.next();
			CalendarDetailInfo info = new CalendarDetailInfo();
			//説明
			if(cal.getDescription() != null){
				info.setDescription(cal.getDescription());
			}
			//年
			info.setYear(cal.getYearNo());
			//月
			info.setMonth(cal.getMonthNo());
			//曜日選択
			info.setDayType(cal.getDayType());
			//曜日
			if(cal.getWeekNo() != null){
				info.setDayOfWeek(cal.getWeekNo());
			}
			//第x週
			if(cal.getWeekXth() != null){
				info.setDayOfWeekInMonth(cal.getWeekXth());
			}
			//日
			if(cal.getDayNo() != null){
				info.setDate(cal.getDayNo());
			}
			//カレンダパターン
			if(cal.getCalPatternId() != null){
				info.setCalPatternId(cal.getCalPatternId());
			}
			//上記の日程からx日後
			info.setAfterday(cal.getAfterDay());
			//開始時間
			if(cal.getStartTime() != null){
				info.setTimeFrom(cal.getStartTime().getTime());
			}
			//終了時間
			if(cal.getEndTime() != null){
				info.setTimeTo(cal.getEndTime().getTime());
			}
			//稼動・非稼動
			info.setOperateFlg(ValidConstant.typeToBoolean(cal.getExecuteFlg()));

			list.add(info);
		}

		return list;
	}

	/**
	 * 
	 * @param calendarId
	 * @return
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public CalendarInfo getCalendarFull(String calendarId) throws CalendarNotFound, InvalidRole {
		CalendarInfo info = CalendarCache.getCalendarInfo(calendarId);
		if (info == null) {
			return null;
		}
		for (CalendarDetailInfo detail : info.getCalendarDetailList()) {
			String calPatternId = detail.getCalPatternId();
			if (calPatternId == null || calPatternId.length() == 0) {
				continue;
			}
			//キャッシュより取得する
			CalendarPatternInfo calPatternInfo = CalendarPatternCache.getCalendarPatternInfo(calPatternId);
			m_log.debug("getCalendarFull() : calPatternInfo=" + calPatternInfo);
			detail.setCalPatternInfo(calPatternInfo);
		}
		return info;
	}

	/**
	 * カレンダ情報一覧を取得します。
	 * 
	 * @return カレンダ情報のリスト
	 */
	public ArrayList<CalendarInfo> getAllCalendarList(String ownerRoleId) {

		List<CalInfoEntity> ct = null;
		if (ownerRoleId == null || ownerRoleId.isEmpty()) {
			//全カレンダを取得
			ct = QueryUtil.getAllCalInfo();
		} else {
			// オーナーロールIDを条件として全カレンダ取得
			ct = QueryUtil.getAllCalInfo_OR(ownerRoleId);
		}
		ArrayList<CalendarInfo> list = new ArrayList<CalendarInfo>();
		Iterator<CalInfoEntity> itr = ct.iterator();
		while(itr.hasNext()){
			CalInfoEntity entity = itr.next();
			list.add(getCalendarInfoBean(entity));
		}
		return list;
	}

	/**
	 * カレンダID一覧を取得します。<BR>
	 * 
	 * @return カレンダID一覧
	 */
	public ArrayList<String> getCalendarIdList() {
		ArrayList<String> list = new ArrayList<String>();

		//全カレンダを取得
		List<CalInfoEntity> ct = QueryUtil.getAllCalInfo();
		for (CalInfoEntity cal : ct) {
			list.add(cal.getCalendarId());
		}
		return list;
	}

	/**
	 * 指定されたカレンダIDをもとに
	 * 月カレンダビューに表示する情報を取得します
	 * @param id
	 * @param year
	 * @param month
	 * @return
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public ArrayList<Integer> getCalendarMonth(String id, Integer year, Integer month) throws CalendarNotFound, InvalidRole {
		return getCalendarMonth(getCalendarFull(id), year, month);
	}

	/**
	 * 月カレンダビューに表示する情報を取得します
	 * @param info
	 * @param year
	 * @param month
	 * @return
	 */
	public ArrayList<Integer> getCalendarMonth(CalendarInfo info, Integer year, Integer month) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		ArrayList<CalendarDetailInfo>list24 = new ArrayList<CalendarDetailInfo>();
		for (CalendarDetailInfo d : info.getCalendarDetailList()) {
			list24.addAll(CalendarUtil.getDetail24(d));
		}

		long validFrom = info.getValidTimeFrom();
		long validTo = info.getValidTimeTo();

		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, 1);
		int lastDate = cal.getActualMaximum(Calendar.DATE) + 1;
		m_log.debug("maxDate=" + year + "/" + month + "/" + lastDate);
		for (int i = 1; i < lastDate; i ++) {
			Calendar startCalendar = Calendar.getInstance();
			startCalendar.clear();
			startCalendar.set(year, month - 1, i, 0, 0, 0);
			long dayStartTime = startCalendar.getTimeInMillis();
			Calendar endCalendar = Calendar.getInstance();
			endCalendar.clear();
			endCalendar.set(year, month - 1, i + 1, 0, 0, 0);
			long dayEndTime = endCalendar.getTimeInMillis();
			m_log.debug("i=" + i + " ==== start=" + new Date(dayStartTime) + ",end=" + new Date(dayEndTime));
			
			
			// 1日の時間内に非有効期間がある場合に立てるフラグ
			// （このフラグがtrueの場合、最後の判定の際に強制的に○から△に変更する）
			boolean isContainInvalidPeriod = false;
			// 有効期限を加味したその日の期間を計算する
			long dayValidStart;
			if (dayStartTime < validFrom) {
				dayValidStart = validFrom;
				isContainInvalidPeriod = true;
			} else {
				dayValidStart = dayStartTime;
			}
			long dayValidEnd;
			if (validTo < dayEndTime) {
				dayValidEnd = validTo;
				isContainInvalidPeriod = true;
			} else {
				dayValidEnd = dayEndTime;
			}
			// 日の最後・有効期間の最後は有効期間に含まれないため、デクリメントする
			dayValidEnd--;
			
			// その日が有効期限内に入っていない場合、無条件に×とする
			if (dayValidStart > dayValidEnd) {
				ret.add(2);
				continue;
			}
			
			/**
			 * 境界時刻をリストアップする。境界時刻とは以下のとおり
			 * ・その日の最初と最後（但しカレンダの有効期限が短ければカレンダの有効期限範囲）
			 * ・各CalendarDetailInfoのFromとTo（但し上記の有効期限内のものに限る）
			 */
			Set<Long> borderTimeSet = new HashSet<Long>();
			borderTimeSet.add(dayValidStart);
			borderTimeSet.add(dayValidEnd);
			// detail
			for (CalendarDetailInfo detail : list24) {
				long detailStart = dayStartTime + detail.getTimeFrom() + TIMEZONE;
				if (dayValidStart < detailStart && detailStart < dayValidEnd) {
					borderTimeSet.add(detailStart);
				}
				
				long detailEnd = dayStartTime + detail.getTimeTo() + TIMEZONE;
				if (dayValidStart < detailEnd && detailEnd < dayValidEnd) {
					borderTimeSet.add(detailEnd);
				}
			}
			
			/**
			 * 全境界時刻について、
			 * ・○[0]・・・すべてが有効（つまり全境界時刻がOK）
			 * ・×[2]・・・すべてが無効（つまり全境界時刻がNG）
			 * ・△[1]・・・一部がOKで一部がNG
			 * をチェックする
			 */
			boolean isAllNG = true; // OKを見つけた時点でfalseに遷移
			boolean isAllOK = true; // NGを見つけた時点でfalseに遷移
			for (Long borderTime : borderTimeSet) {
				// この境界時刻が動作時刻か、非動作時刻かを検証する
				
				boolean existValidCalendar = false;
				// カレンダ詳細設定から、この境界時刻時点で稼動か否かを調査する
				for (CalendarDetailInfo detail : list24) {
					if (CalendarUtil.isRunByDetailDateTime(detail, new Date(borderTime))) {
						// カレンダ詳細が有効期間内の場合は、このカレンダ詳細設定で有効・無効がわかるため、
						// この時刻に関しては以降のカレンダ詳細の調査はスキップする
						if (detail.isOperateFlg() == true) {
							isAllNG = false;
						} else {
							isAllOK = false;
						}
						// 有効なカレンダ詳細設定が見つかった場合、この境界時刻に関しては
						// これ以上のカレンダ詳細のチェックは行わない
						existValidCalendar = true;
						break;
					}
				}
				// この境界時刻に有効なカレンダ詳細設定が１つもない場合、その時刻はNGになる
				if (existValidCalendar == false) {
					isAllOK = false;
				}
				// 全OK・全NGではなくなったら△に確定なので、残りの処理は行わない
				if (isAllNG == false && isAllOK == false) {
					break;
				}
			}
			
			if (isAllNG == true) {
				// ×：全部NG
				ret.add(2);
			} else {
				if (isAllOK == true) {
					if (isContainInvalidPeriod) {
						// △：一部OK・一部NG （有効期間内は全てOKだが、カレンダそのものの非有効範囲が被るため）
						ret.add(1);
					} else {
						// ○：全てOK
						ret.add(0);
					}
				} else {
					// △：一部OK・一部NG
					ret.add(1);
				}
			}
		}
		return ret;
	}
	/**
	 * カレンダ詳細定義 - 年、月、日が現在の時間が等しいか調べる
	 * 時間、分、秒は見ない。
	 * CalendarWeekViewで利用する。
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */

	public ArrayList<CalendarDetailInfo> getCalendarWeek(String id, Integer year, Integer month, Integer day) throws CalendarNotFound, InvalidRole {

		CalendarInfo info = getCalendarFull(id);
		return getCalendarWeek(info, year, month, day);
	}

	public ArrayList<CalendarDetailInfo> getCalendarWeek(CalendarInfo info, Integer year, Integer month, Integer day) throws CalendarNotFound {
		long validFrom = info.getValidTimeFrom();
		long validTo = info.getValidTimeTo();
		ArrayList<CalendarDetailInfo> ret = new ArrayList<CalendarDetailInfo>();
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.clear();
		startCalendar.set(year, month - 1, day, 0, 0, 0);
		long startTime = startCalendar.getTimeInMillis();
		Calendar endCalendar = Calendar.getInstance();
		endCalendar.clear();
		endCalendar.set(year, month - 1, day + 1, 0, 0, 0);
		long endTime = endCalendar.getTimeInMillis();

		if (startTime <=  validFrom && endTime <= validFrom) {
			return ret;
		}
		if (validTo <= startTime && validTo <= endTime) {
			return ret;
		}
		if (startTime < validFrom && validFrom < endTime) {
			CalendarDetailInfo detail = new CalendarDetailInfo();
			detail.setTimeFrom(0 - TIMEZONE);
			detail.setTimeTo(validFrom - startTime - TIMEZONE);
			detail.setOperateFlg(false);
			ret.add(detail);
		}
		if (startTime < validTo && validTo < endTime) {
			CalendarDetailInfo detail = new CalendarDetailInfo();
			detail.setTimeFrom(validTo - startTime - TIMEZONE);
			detail.setTimeTo(HOUR24 - TIMEZONE);
			detail.setOperateFlg(false);
			ret.add(detail);
		}

		for (CalendarDetailInfo detail : info.getCalendarDetailList()) {
			for (CalendarDetailInfo detail24 : CalendarUtil.getDetail24(detail)) {
				if (CalendarUtil.isRunByDetailDate(detail24, startCalendar.getTime())) {
					ret.add(detail24);
				}
			}
		}
		if (m_log.isDebugEnabled()) {
			for (CalendarDetailInfo detail : ret) {
				m_log.debug("detail=" + detail);
			}
		}
		return ret;
	}

	/**
	 * カレンダ[カレンダパターン]情報を取得します。
	 * 
	 * @param id
	 * @return カレンダ[カレンダパターン]情報
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public CalendarPatternInfo getCalendarPattern(String id) throws CalendarNotFound, InvalidRole {
		CalendarPatternInfo ret = null;
		if(id != null && !"".equals(id)){
			ret = CalendarPatternCache.getCalendarPatternInfo(id);
		}
		return ret;
	}

	/**
	 * カレンダ[カレンダパターン]情報一覧を取得します。
	 * 
	 * @param ownerRoleId オーナーロールID
	 * @return カレンダ[カレンダパターン]情報のリスト
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public ArrayList<CalendarPatternInfo> getCalendarPatternList(String ownerRoleId) throws CalendarNotFound, InvalidRole {
		ArrayList<CalendarPatternInfo> list = new ArrayList<CalendarPatternInfo>();
		//全カレンダを取得
		ArrayList<String> patternIdList = getCalendarPatternIdList(ownerRoleId);
		for (String id : patternIdList) {
			CalendarPatternInfo info = new CalendarPatternInfo();
			info = CalendarPatternCache.getCalendarPatternInfo(id);
			list.add(info);
		}
		/*
		 * カレンダパターンIDで昇順ソート
		 */
		Collections.sort(list);
		return list;
	}

	/**
	 * カレンダパターンID一覧を取得します。<BR>
	 * 
	 * @param ownerRoleId オーナーロールID
	 * @return カレンダパターンのID一覧
	 */
	public ArrayList<String> getCalendarPatternIdList(String ownerRoleId) {
		ArrayList<String> list = new ArrayList<String>();
		//全カレンダパターンを取得
		List<CalPatternInfoEntity> entityList = QueryUtil.getAllCalPatternInfo();
		if (ownerRoleId == null || ownerRoleId.isEmpty()) {
			entityList = QueryUtil.getAllCalPatternInfo();
		} else {
			entityList = QueryUtil.getAllCalPatternInfo_OR(ownerRoleId);
		}
		for (CalPatternInfoEntity entity : entityList) {
			list.add(entity.getCalPatternId());
		}
		//ソート処理
		Collections.sort(list);
		return list;
	}

	/**
	 * 実行可能かをチェックします。<BR>
	 * 
	 * 指定カレンダにて、指定した日時が実行可能かチェックし、Bool値を返します。
	 * 
	 * @param id
	 * @param checkTimestamp
	 * @return 指定した日時が実行可能か
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public Boolean isRun(String id, Long checkTimestamp) throws CalendarNotFound, InvalidRole {
		CalendarInfo info = null;
		Date date = new Date(checkTimestamp);
		if (id == null) {
			return true;
		}
		info = getCalendarFull(id);

		return CalendarUtil.isRun(info, date);
	}

	/**
	 * テスト用
	 * @param args
	 */
	public static void main(String args[]) {
		monthTest();
	}
	/**
	 * 月カレンダビュー表示テスト
	 */
	public static void monthTest() {
		CalendarInfo info = new CalendarInfo();
		info.setValidTimeFrom(0l);
		info.setValidTimeTo(Long.MAX_VALUE);

		ArrayList<CalendarDetailInfo> detailList = new ArrayList<CalendarDetailInfo>();
		CalendarDetailInfo detail = null;


		detail = new CalendarDetailInfo();
		detail.setYear(2012);
		detail.setMonth(0); // 全ての月は0
		detail.setDayType(0);//毎日を選択
		detail.setDayType(1);//曜日を選択
		detail.setDayOfWeekInMonth(0);//第ｘ週、0は毎週
		detail.setDayOfWeek(1);//曜日、1は日曜日
		//		detail.setTimeFrom(0*3600*1000l - TIMEZONE);
		detail.setTimeFrom(1*3600*1000l - TIMEZONE);
		detail.setTimeTo(23*3600*1000l - TIMEZONE);
		//		detail.setTimeTo(24*3600*1000l - TIMEZONE);
		//		detail.setTimeTo(25*3600*1000l - TIMEZONE);
		detail.setOperateFlg(true);
		detailList.add(detail);

		info.setCalendarDetailList(detailList);

		SelectCalendar selectCalendar = new SelectCalendar();
		ArrayList<Integer> list = selectCalendar.getCalendarMonth(info, 2012, 2);
		int j = 0;
		String str = "";
		for (Integer i : list) {
			if (j % 7 == 0) {
				str += "\n";
			}
			str += i + " ";
			j++;
		}
		m_log.trace("getCalendarMonthInfo=" + str);
	}

	/**
	 * CalInfoEntityからCalendarInfoへ変換
	 */
	private CalendarInfo getCalendarInfoBean(CalInfoEntity entity) {

		//カレンダ情報のDTOを生成
		CalendarInfo info = new CalendarInfo();

		//id
		info.setId(entity.getCalendarId());
		//名前
		info.setName(entity.getCalendarName());
		//有効期間(From)
		if (entity.getValidTimeFrom() != null) {
			info.setValidTimeFrom(entity.getValidTimeFrom().getTime());
		}
		//有効期間(To)
		if (entity.getValidTimeTo() != null) {
			info.setValidTimeTo(entity.getValidTimeTo().getTime());
		}
		//説明
		info.setDescription(entity.getDescription());
		//オーナーロールID
		info.setOwnerRoleId(entity.getOwnerRoleId());
		//登録者
		info.setRegUser(entity.getRegUser());
		//登録日時
		if (entity.getRegDate() != null) {
			info.setRegDate(entity.getRegDate().getTime());
		}
		//更新者
		info.setUpdateUser(entity.getUpdateUser());
		//更新日時
		if (entity.getUpdateDate() != null) {
			info.setUpdateDate(entity.getUpdateDate().getTime());
		}
		return info;
	}
}
