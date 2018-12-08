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

import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.scheduler.QuartzUtil;
import com.clustercontrol.commons.util.EmptyJpaTransactionCallback;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.maintenance.bean.QuartzConstant;
import com.clustercontrol.maintenance.bean.MaintenanceInfo;
import com.clustercontrol.maintenance.session.MaintenanceControllerBean;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;

/**
 * スケジュール情報を操作するクラスです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class ModifySchedule {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( ModifySchedule.class );

	/**
	 * スケジュール情報を基にQuartzにジョブを登録します。<BR>
	 * Quartzからは、{@link com.clustercontrol.jobmanagement.session.JobControllerBean#scheduleRunJob(String, String)} が呼び出されます。
	 * 
	 * @param info スケジュール情報
	 * @param user ユーザID
	 * @throws ParseException
	 * @throws SchedulerException
	 * @throws RemoteException
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.QuartzConstant
	 * @see com.clustercontrol.jobmanagement.util.QuartzUtil#getQuartzManager()
	 */
	public void addSchedule(final MaintenanceInfo info, String user) throws HinemosUnknown {
		m_log.debug("addSchedule() : id=" + info.getMaintenanceId());
		
		JpaTransactionManager jtm = new JpaTransactionManager();
		jtm.addCallback(new EmptyJpaTransactionCallback() {
			@Override
			public void postCommit() {
				//JobDetailに呼び出すメソッドの引数を設定
				//ジョブIDを設定
				Serializable[] jdArgs = new Serializable[2];
				Class<? extends Serializable>[] jdArgsType = new Class[2];
				jdArgs[0] = info.getMaintenanceId();
				jdArgsType[0] = String.class;
				//カレンダIDを設定
				jdArgs[1] = info.getCalendarId();
				jdArgsType[1] = String.class;
				
				try {
					SchedulerPlugin.scheduleCronJob(SchedulerType.DBMS, info.getMaintenanceId(), QuartzConstant.GROUP_NAME, new Date(System.currentTimeMillis() + 15 * 1000),
							QuartzUtil.getCronString(info.getSchedule()), true, MaintenanceControllerBean.class.getName(), QuartzConstant.METHOD_NAME, jdArgsType, jdArgs);
					if (! ValidConstant.typeToBoolean(info.getValidFlg())) {
						SchedulerPlugin.pauseJob(SchedulerType.DBMS, info.getMaintenanceId(), QuartzConstant.GROUP_NAME);
					}
				} catch (HinemosUnknown e) {
					m_log.error(e);
				}
			}
		});
	}

	/**
	 * スケジュール情報を基にQuartzに登録したジョブを削除します。
	 * 
	 * @param scheduleId スケジュールID
	 * @throws SchedulerException
	 * @throws RemoteException
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.QuartzConstant
	 * @see com.clustercontrol.jobmanagement.util.QuartzUtil#getQuartzManager()
	 * @see com.clustercontrol.quartzmanager.ejb.session.QuartzManager#deleteSchedule(java.lang.String, java.lang.String)
	 */
	public void deleteSchedule(final String scheduleId) throws HinemosUnknown {
		m_log.debug("deleteSchedule() : id=" + scheduleId);

		JpaTransactionManager jtm = new JpaTransactionManager();
		jtm.addCallback(new EmptyJpaTransactionCallback() {
			@Override
			public void postCommit() {
				try {
					SchedulerPlugin.deleteJob(SchedulerType.DBMS, scheduleId, QuartzConstant.GROUP_NAME);
				} catch (HinemosUnknown e) {
					m_log.error(e);
				}
			}
		});
	}
}
