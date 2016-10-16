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

package com.clustercontrol.maintenance.factory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.jobmanagement.factory.JobSessionImpl;
import com.clustercontrol.jobmanagement.util.JobMultiplicityCache;
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.notify.model.MonitorStatusEntity;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.notify.util.MonitorStatusCache;

/**
 * ジョブ履歴の削除処理
 *
 * @version 4.0.0
 * @since 3.1.0
 *
 */
public class MaintenanceJob extends MaintenanceObject{

	private static Log m_log = LogFactory.getLog( MaintenanceJob.class );

	/**
	 * 削除処理
	 */
	@Override
	protected int _delete(Timestamp keep, boolean status, String ownerRoleId) {
		m_log.debug("_delete() start : keep = " + keep.toString() + ", status = " + status);

		int ret = 0;	// target job_session_id count
		int ret1 = 0;	// target notify_relation_info count
		int ret2 = 0;	// target monitor_status count
		int ret3 = 0;	// target notify_history
		long time1 = 0; // job_session_idのカウント時間
		long time2 = 0; // MonitorStatusCacheの削除時間
		long time3 = 0; // NotifyControllerBean_Cacheの削除時間
		long time4 = 0; // ロックオブジェクトの削除時間
		long time5 = 0; // DBの削除時間

		///////////////////////////////////////////////
		// RUN SQL STATEMENT
		///////////////////////////////////////////////

		long start = System.currentTimeMillis();

		// 削除対象となるsession_idを格納するテンポラリテーブル作成(スキーマ定義引継ぎ)
		m_log.debug("_delete() : CREATE TEMPORARY TABLE AND INSERT JOB_SESSION_ID");
		m_log.debug("_delete() : sql = JobCompletedSessionsEntity.createTable");
		ret = QueryUtil.createJobCompletedSessionsTable();

		// 削除対象となるsession_idの検索と挿入
		//オーナーロールIDがADMINISTRATORSの場合
		if(RoleIdConstant.isAdministratorRole(ownerRoleId)) {
			if(status){
				ret = QueryUtil.insertJobCompletedSessionsJobSessionJob(keep);
			} else {
				ret = QueryUtil.insertJobCompletedSessionsJobSessionJobByStatus(keep);
			}
		}
		//オーナーロールが一般ロールの場合
		else {
			if(status){
				ret = QueryUtil.insertJobCompletedSessionsJobSessionJobByOwnerRoleId(keep, ownerRoleId);
			} else {
				ret = QueryUtil.insertJobCompletedSessionsJobSessionJobByStatusAndOwnerRoleId(keep, ownerRoleId);
			}
		}

		//ロックオブジェクトとキャッシュの削除
		ArrayList<String> sessionIdList = QueryUtil.selectCompletedSession();
		
		HashMap<String, String> sessionMap = new HashMap<String, String>();
		for (String sessionId : sessionIdList) { 
			sessionMap.put(sessionId,  sessionId);
		}
		
		time1 = System.currentTimeMillis() - start;
		
		start = System.currentTimeMillis();
		List<MonitorStatusEntity> monitorStatusList = MonitorStatusCache.getByPluginIdAndMonitorMap(HinemosModuleConstant.JOB, sessionMap);
		for(MonitorStatusEntity monitorStatus : monitorStatusList){
			MonitorStatusCache.remove(monitorStatus);
		}
		time2 = System.currentTimeMillis() - start;

		start = System.currentTimeMillis();
		// NotifyControllerBeanにあるキャッシュも削除する
		NotifyControllerBean.removeCache(HinemosModuleConstant.JOB, sessionMap);
		time3 = System.currentTimeMillis() - start;

		start = System.currentTimeMillis();
		ILockManager lm = LockManagerFactory.instance().create();
		for (String sessionId : sessionIdList) {
			lm.delete(JobSessionImpl.class.getName() + "-" + sessionId);
		}
		time4 = System.currentTimeMillis() - start;

		start = System.currentTimeMillis();
		m_log.info("_delete() : completed session list size = " + sessionIdList.size()); 
		
		// 削除:cc_job_sessionと関連テーブル(CASCADE)
		m_log.debug("_delete() : DELETE cc_job_session");
		m_log.debug("_delete() : sql = JobCompletedSessionsEntity.deleteByJobCompletedSessions");
		ret = QueryUtil.deleteJobSessionByCompletedSessions();
		m_log.debug("_delete() : DELETE cc_job_session COUNT = " + ret);


		// 削除:cc_notify_relation_info
		m_log.debug("_delete() : DELETE cc_notify_relation_info");
		m_log.debug("_delete() : sql = NotifyRelationInfoEntity.deleteByJobCompletedSessions");
		ret1 = QueryUtil.deleteNotifyRelationInfoByCompletedSessions();
		m_log.debug("_delete() : DELETE cc_notify_relation_info COUNT = " + ret1);


		// 削除:cc_monitor_status
		m_log.debug("_delete() : DELETE cc_monitor_status");
		m_log.debug("_delete() : sql = MonitorStatusEntity.deleteByJobCompletedSessions");
		ret2 = QueryUtil.deleteMonitorStatusByCompletedSessions();
		m_log.debug("_delete() : DELETE cc_monitor_status COUNT = " + ret2);

		// 削除:cc_notify_history
		m_log.debug("_delete() : DELETE cc_notify_history");
		m_log.debug("_delete() : sql = NotifyHistoryEntity.deleteByJobCompletedSessions");
		ret3 = QueryUtil.deleteNotifyHistoryByCompletedSessions();
		m_log.debug("_delete() : DELETE cc_notify_history COUNT = " + ret3);

		// 削除対象となるsession_idを格納するテンポラリテーブル削除
		m_log.debug("_delete() : DROP TEMPORARY TABLE");
		m_log.debug("_delete() : sql = JobCompletedSessionsEntity.dropTable");
		QueryUtil.dropJobCompletedSessionsTable();

		time5 = System.currentTimeMillis() - start;
		
		m_log.info("_delete() count : " + ret +", count time : " + time1 +"ms, delete time (MonitorStatus) : " + time2 
				+ "ms, delete time (NotifyController) : " + time3 + "ms, delete time (Lock) : " + time4 
				+ "ms, delete time (DB) " + time5 + "ms");

		// ジョブ多重度のリフレッシュ
		JobMultiplicityCache.refresh();

		return ret;
	}
}
