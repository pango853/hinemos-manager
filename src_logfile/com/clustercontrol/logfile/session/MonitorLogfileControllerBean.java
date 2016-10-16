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

package com.clustercontrol.logfile.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.calendar.bean.CalendarInfo;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.logfile.bean.LogfileResultDTO;
import com.clustercontrol.logfile.factory.RunMonitorLogfileString;
import com.clustercontrol.logfile.factory.SelectMonitorLogfile;
import com.clustercontrol.monitor.run.bean.MonitorInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 * ログファイル監視の管理を行う Session Bean <BR>
 * クライアントからの Entity Bean へのアクセスは、Session Bean を介して行います。
 * 
 */
public class MonitorLogfileControllerBean {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( MonitorLogfileControllerBean.class );

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(MonitorLogfileControllerBean.class.getName());
		
		try {
			_lock.writeLock();
			
			ArrayList<MonitorInfo> cache = getCache();
			if (cache == null) {	// not null when clustered
				refreshCache();
			}
		} finally {
			_lock.writeUnlock();
			m_log.info("Static Initialization [Thread : " + Thread.currentThread() + ", User : " + (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID) + "]");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static ArrayList<MonitorInfo> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_LOGFILE);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_LOGFILE + " : " + cache);
		return cache == null ? null : (ArrayList<MonitorInfo>)cache;
	}
	
	private static void storeCache(ArrayList<MonitorInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_LOGFILE + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_LOGFILE, newCache);
	}
	
	/**
	 * ログファイル監視一覧リストを返します。
	 * 
	 * 
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getLogfileList() throws MonitorNotFound, InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;
		SelectMonitorLogfile logfile = new SelectMonitorLogfile();
		ArrayList<MonitorInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = logfile.getMonitorListObjectPrivilegeModeNONE(HinemosModuleConstant.MONITOR_LOGFILE);
			jtm.commit();
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (MonitorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getLogfileList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return list;
	}

	public static void refreshCache () {
		m_log.info("refreshCache()");
		
		long startTime = System.currentTimeMillis();
		try {
			_lock.writeLock();
			
			ArrayList<MonitorInfo> logfileCache = new MonitorLogfileControllerBean().getLogfileList();
			storeCache(logfileCache);
			
			m_log.info("refresh logfileCache " + (System.currentTimeMillis() - startTime) +
					"ms. size=" + logfileCache.size());
		} catch (Exception e) {
			m_log.warn("failed refreshing cache.", e);
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * 
	 * <注意！> このメソッドはAgentユーザ以外で呼び出さないこと！
	 * <注意！> キャッシュの都合上、Agentユーザ以外から呼び出すと、正常に動作しません。
	 * 
	 * facilityIDごとのログファイル監視一覧リストを返します。
	 * withCalendarをtrueにするとMonitorInfoのcalendarDTOに値が入ります。
	 * 
	 * 
	 * @return Objectの2次元配列
	 * @throws HinemosUnknown
	 * @throws MonitorNotFound
	 * 
	 */
	public ArrayList<MonitorInfo> getLogfileListForFacilityId (String facilityId, boolean withCalendar)
			throws MonitorNotFound, HinemosUnknown {
		ArrayList<MonitorInfo> ret = new ArrayList<MonitorInfo>();
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
			// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
			ArrayList<MonitorInfo> monitorList = getCache();
			
			for (MonitorInfo monitorInfo : monitorList) {
				String scope = monitorInfo.getFacilityId();
				ArrayList<String> facilityIdList
				= new RepositoryControllerBean().getExecTargetFacilityIdList(scope, monitorInfo.getOwnerRoleId());
				if (facilityIdList != null && facilityIdList.contains(facilityId)) {
					if (withCalendar) {
						String calendarId = monitorInfo.getCalendarId();
						try {
							CalendarInfo calendar = new CalendarControllerBean().getCalendarFull(calendarId);
							monitorInfo.setCalendar(calendar);
						} catch (Exception e) {
							m_log.warn("getLogfileList() : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
							throw new HinemosUnknown(e.getMessage(), e);
						}
					}
					ret.add(monitorInfo);
				}
			}
			
			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getLogfileListForFacilityId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		return ret;
	}
	
	/**
	 * ログファイル監視結果を通知する.
	 * 
	 * @param results ログファイル監視結果のリスト
	 * @throws HinemosUnknown 
	 */
	public void run(String facilityId, List<LogfileResultDTO> results) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			if (results != null) {
				for (LogfileResultDTO result : results) {
					new RunMonitorLogfileString().run(facilityId, result);
				}
			}
			
			jtm.commit();
		} catch (HinemosUnknown e) {
			m_log.warn("failed storeing result.", e);
			jtm.rollback();
			
			throw e;
		} finally {
			jtm.close();
		}
	}
}
