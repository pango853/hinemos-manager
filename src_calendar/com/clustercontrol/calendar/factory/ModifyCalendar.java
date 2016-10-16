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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.calendar.bean.CalendarDetailInfo;
import com.clustercontrol.calendar.bean.CalendarInfo;
import com.clustercontrol.calendar.bean.CalendarPatternInfo;
import com.clustercontrol.calendar.bean.YMD;
import com.clustercontrol.calendar.model.CalDetailInfoEntity;
import com.clustercontrol.calendar.model.CalDetailInfoEntityPK;
import com.clustercontrol.calendar.model.CalInfoEntity;
import com.clustercontrol.calendar.model.CalPatternDetailInfoEntity;
import com.clustercontrol.calendar.model.CalPatternDetailInfoEntityPK;
import com.clustercontrol.calendar.model.CalPatternInfoEntity;
import com.clustercontrol.calendar.util.QueryUtil;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CalendarDuplicate;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;

/**
 * カレンダ更新を行うファクトリークラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class ModifyCalendar {
	private static Log m_log = LogFactory.getLog(ModifyCalendar.class);

	/**
	 * カレンダ追加
	 * 
	 * @param info
	 * @param userName
	 * @return
	 * @throws HinemosUnknown
	 * @throws CalendarDuplicate
	 * @throws CalendarNotFound
	 */
	public void addCalendar(CalendarInfo info, String userName)
			throws HinemosUnknown, CalendarDuplicate, CalendarNotFound {

		JpaTransactionManager jtm = new JpaTransactionManager();
		//カレンダを作成
		String id = null;
		try {
			//現在日時を取得
			Timestamp now = new Timestamp(new Date().getTime());
			//ID取得
			id = info.getId();
			//名前を取得
			String name = info.getName();
			//説明を取得
			String description = info.getDescription();
			//オーナーロールIDを取得
			String ownerRoleId = info.getOwnerRoleId();
			//有効期間(From)を取得
			Timestamp validTimeFrom = null;
			if (info.getValidTimeFrom() != null) {
				validTimeFrom = new Timestamp(info.getValidTimeFrom());
			}
			//有効期間(To)を取得
			Timestamp validTimeTo = null;
			if (info.getValidTimeTo() != null) {
				validTimeTo = new Timestamp(info.getValidTimeTo());
			}
			// インスタンス生成
			CalInfoEntity entity = new CalInfoEntity(id);
			// 重複チェック
			jtm.checkEntityExists(CalInfoEntity.class, id);
			entity.setCalendarName(name);
			entity.setDescription(description);
			entity.setOwnerRoleId(ownerRoleId);
			entity.setRegDate(now);
			entity.setRegUser(userName);
			entity.setStartTime(null);
			entity.setUpdateDate(now);
			entity.setUpdateUser(userName);
			entity.setValidTimeFrom(validTimeFrom);
			entity.setValidTimeTo(validTimeTo);
			// カレンダ詳細情報登録
			if (info.getCalendarDetailList() != null) {
				CalDetailInfoEntity calDetailInfoEntity = null;
				for (int i = 0 ; i < info.getCalendarDetailList().size();  i++ ) {
					try {
						calDetailInfoEntity = QueryUtil.getCalDetailInfoPK(id, i + 1);
					} catch (CalendarNotFound e) {
						calDetailInfoEntity = new CalDetailInfoEntity(entity, i + 1);
					}
					copyProperties(calDetailInfoEntity, info.getCalendarDetailList().get(i));
				}
			}
		} catch (EntityExistsException e) {
			m_log.info("addCalendar() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new CalendarDuplicate(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addCalendar() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * カレンダ詳細情報を追加します。<BR>
	 * 
	 * @param calDetailInfoEntity
	 * @param CalendarDetailInfo
	 * @return
	 */
	public void copyProperties(CalDetailInfoEntity calDetailInfoEntity, CalendarDetailInfo info) {

		//説明
		calDetailInfoEntity.setDescription(info.getDescription());
		//年
		calDetailInfoEntity.setYearNo(info.getYear());
		//月
		calDetailInfoEntity.setMonthNo(info.getMonth());
		//曜日
		calDetailInfoEntity.setDayType(info.getDayType());
		calDetailInfoEntity.setWeekNo(info.getDayOfWeek());
		calDetailInfoEntity.setWeekXth(info.getDayOfWeekInMonth());
		calDetailInfoEntity.setDayNo(info.getDate());
		calDetailInfoEntity.setCalPatternId(info.getCalPatternId());
		calDetailInfoEntity.setAfterDay(info.getAfterday());
		//時間
		Long startTime = info.getTimeFrom();
		if(startTime != null){
			calDetailInfoEntity.setStartTime(new Timestamp(startTime));
		}
		Long endTime = info.getTimeTo();
		if(endTime != null){
			calDetailInfoEntity.setEndTime(new Timestamp(endTime));
		}
		//稼動・非稼動
		calDetailInfoEntity.setExecuteFlg(Integer.valueOf(ValidConstant.booleanToType(info.isOperateFlg())));
	}

	/**
	 * カレンダ[カレンダパターン]情報追加
	 * 
	 * @param info
	 * @param userName
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws CalendarDuplicate
	 * @throws CalendarNotFound
	 */
	public void addCalendarPattern(CalendarPatternInfo info, String userName)
			throws HinemosUnknown, InvalidRole, CalendarDuplicate, CalendarNotFound {

		JpaTransactionManager jtm = new JpaTransactionManager();
		//カレンダパターンを作成
		String id = null;
		try {
			//現在日時を取得
			Timestamp now = new Timestamp(new Date().getTime());
			//ID取得
			id = info.getId();
			//名前を取得
			String name = info.getName();
			//オーナーロールIDを取得
			String ownerRoleId = info.getOwnerRoleId();
			// 重複チェック
			jtm.checkEntityExists(CalPatternInfoEntity.class, id);
			// インスタンス生成
			CalPatternInfoEntity entity = new CalPatternInfoEntity(id);
			entity.setCalPatternName(name);
			entity.setOwnerRoleId(ownerRoleId);
			entity.setRegDate(now);
			entity.setRegUser(userName);
			entity.setUpdateDate(now);
			entity.setUpdateUser(userName);

			if(info.getYmd() != null){
				int num = 1;
				for(YMD ymd : info.getYmd()){
					m_log.trace("No." + num + " : YMD= " + ymd.yyyyMMdd());
					addCalendarPatternDetail(id, ymd);
					num++;
				}
			}
		} catch (InvalidRole e) {
			throw e;
		} catch (EntityExistsException e) {
			m_log.info("addCalendarPattern() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new CalendarDuplicate(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addCalendarPattern() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * カレンダ[カレンダパターン]詳細情報追加
	 * @param id
	 * @param ymd
	 * @throws InvalidRole
	 * @throws CalendarDuplicate
	 * @throws CalendarNotFound
	 * @throws HinemosUnknown
	 */
	private void addCalendarPatternDetail(String id, YMD ymd)
			throws InvalidRole, CalendarDuplicate, CalendarNotFound, HinemosUnknown {

		JpaTransactionManager jtm = new JpaTransactionManager();

		try {
			CalPatternInfoEntity calPatternEntity = QueryUtil.getCalPatternInfoPK(id);

			//年を取得
			Integer year = ymd.getYear();

			//月を取得
			Integer month = ymd.getMonth();

			//日を取得
			Integer day = ymd.getDay();

			//カレンダパターン詳細情報を作成
			// 主キー作成
			CalPatternDetailInfoEntityPK entityPk = new CalPatternDetailInfoEntityPK(id,year,month,day);
			// インスタンス生成
			new CalPatternDetailInfoEntity(entityPk, calPatternEntity);
			// 重複チェック
			jtm.checkEntityExists(CalPatternDetailInfoEntity.class, entityPk);
		} catch (InvalidRole e) {
			throw e;
		} catch (CalendarNotFound e) {
			throw e;
		} catch (EntityExistsException e) {
			m_log.info("addCalPatternDetailInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new CalendarDuplicate(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addCalPatternDetailInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}


	/**
	 * カレンダ情報を変更します。<BR>
	 * @param info
	 * @param userName
	 * @throws CalendarNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public void modifyCalendar(CalendarInfo info, String userName)
			throws CalendarNotFound, HinemosUnknown, InvalidRole {
		String id = null;
		try {
			//現在日時を取得
			Timestamp now = new Timestamp(new Date().getTime());
			//ID取得
			id = info.getId();
			//名前を取得
			String name = info.getName();
			//説明を取得
			String description = info.getDescription();
			//オーナーロールを取得
			String ownerRoleId = info.getOwnerRoleId();
			//有効期間(From)を取得
			Timestamp validTimeFrom = null;
			if (info.getValidTimeFrom() != null) {
				validTimeFrom = new Timestamp(info.getValidTimeFrom());
			}
			//有効期間(To)を取得
			Timestamp validTimeTo = null;
			if (info.getValidTimeTo() != null) {
				validTimeTo = new Timestamp(info.getValidTimeTo());
			}
			//カレンダを作成
			CalInfoEntity entity = QueryUtil.getCalInfoPK(id, ObjectPrivilegeMode.MODIFY);
			entity.setCalendarName(name);
			entity.setDescription(description);
			/* 作成時刻、作成ユーザは変更時に更新されるはずがないので、再度登録しない
				entity.setRegDate(now);
				entity.setRegUser(userName);
			 */
			entity.setOwnerRoleId(ownerRoleId);
			entity.setStartTime(null);
			entity.setUpdateDate(now);
			entity.setUpdateUser(userName);
			entity.setValidTimeFrom(validTimeFrom);
			entity.setValidTimeTo(validTimeTo);

			// カレンダ詳細情報登録
			if (info.getCalendarDetailList() != null) {
				CalDetailInfoEntity calDetailInfoEntity = null;
				List<CalDetailInfoEntityPK> calDetailInfoEntityPkList
				= new ArrayList<CalDetailInfoEntityPK>();
				for (int i = 0 ; i < info.getCalendarDetailList().size();  i++ ) {
					try {
						calDetailInfoEntity = QueryUtil.getCalDetailInfoPK(id, i + 1);
					} catch (CalendarNotFound e) {
						calDetailInfoEntity = new CalDetailInfoEntity(entity, i + 1);
					}
					calDetailInfoEntityPkList.add(new CalDetailInfoEntityPK(id, i + 1));
					copyProperties(calDetailInfoEntity, info.getCalendarDetailList().get(i));
				}
				// 不要なCalDetailInfoEntityを削除
				entity.deleteCalDetailInfoEntities(calDetailInfoEntityPkList);
			}
		} catch (CalendarNotFound e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("modifyCalendar() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}
	/**
	 * カレンダ[カレンダパターン]情報を変更します。<BR>
	 * @param info
	 * @param userName
	 * @throws HinemosUnknown
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public void modifyCalendarPattern(CalendarPatternInfo info, String userName)
			throws HinemosUnknown, CalendarNotFound, InvalidRole {
		String id = null;
		try {
			//現在日時を取得
			Timestamp now = new Timestamp(new Date().getTime());
			//ID取得
			id = info.getId();
			//名前を取得
			String name = info.getName();
			//オーナーロールIDを取得
			String ownerRoleId = info.getOwnerRoleId();
			//カレンダを作成
			CalPatternInfoEntity entity = QueryUtil.getCalPatternInfoPK(id, ObjectPrivilegeMode.MODIFY);
			entity.setCalPatternId(id);
			entity.setCalPatternName(name);
			entity.setOwnerRoleId(ownerRoleId);
			/* 作成時刻、作成ユーザは変更時に更新されるはずがないので、再度登録しない
				entity.setRegDate(now);
				entity.setRegUser(userName);
			 */
			entity.setUpdateDate(now);
			entity.setUpdateUser(userName);

			// カレンダパターン詳細情報登録
			//TODO: 実装を見直す
			if (info.getYmd() != null) {
				List<CalPatternDetailInfoEntityPK> calPatternDetailInfoEntityPkList
				= new ArrayList<CalPatternDetailInfoEntityPK>();
				for (YMD ymd : info.getYmd()) {
					try {
						QueryUtil.getCalPatternDetailInfoPK(id, ymd.getYear(), ymd.getMonth(), ymd.getDay());
					} catch (CalendarNotFound e) {
						new CalPatternDetailInfoEntity(id, ymd.getYear(), ymd.getMonth(), ymd.getDay(), entity);
					}
					calPatternDetailInfoEntityPkList.add(new CalPatternDetailInfoEntityPK(id, ymd.getYear(), ymd.getMonth(), ymd.getDay()));
				}
				// 不要なCalDetailInfoEntityを削除
				entity.deleteCalPatternDetailInfoEntities(calPatternDetailInfoEntityPkList);
			}
		} catch (CalendarNotFound e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("modifyCalendarPattern() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("modifyCalendarPattern() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * カレンダ（基本）情報を削除します。<BR>
	 * 
	 * @param id
	 * @return
	 * @throws HinemosUnknown
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public void deleteCalendar(String id) throws HinemosUnknown, CalendarNotFound, InvalidRole {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		try {
			//カレンダ情報を検索し取得
			CalInfoEntity cal = QueryUtil.getCalInfoPK(id, ObjectPrivilegeMode.MODIFY);

			//カレンダ情報を削除
			em.remove(cal);

		} catch (CalendarNotFound e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteCalendar() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * カレンダ[カレンダパターン]情報を削除します。<BR>
	 * 
	 * @param id
	 * @return
	 * @throws HinemosUnknown
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public void deleteCalendarPattern(String id) throws HinemosUnknown, CalendarNotFound, InvalidRole {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		m_log.info("deleteCalendarPattern : deleted " + id);
		try {
			//カレンダ[カレンダパターン]情報を検索し取得
			CalPatternInfoEntity calPa = QueryUtil.getCalPatternInfoPK(id, ObjectPrivilegeMode.MODIFY);
			//カレンダ[カレンダパターン]情報を削除
			em.remove(calPa);
		} catch (CalendarNotFound e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteCalendarPattern() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}
}
