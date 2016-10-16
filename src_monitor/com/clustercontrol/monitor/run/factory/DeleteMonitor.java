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

package com.clustercontrol.monitor.run.factory;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.scheduler.TriggerSchedulerException;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.notify.model.MonitorStatusEntity;
import com.clustercontrol.notify.model.NotifyHistoryEntity;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.notify.util.MonitorStatusCache;

/**
 * 監視情報を削除する抽象クラス<BR>
 * <p>
 * 監視種別（真偽値，数値，文字列）の各クラスで継承してください。
 *
 * @version 4.0.0
 * @since 2.0.0
 */
abstract public class DeleteMonitor {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( DeleteMonitor.class );

	/** 監視情報のエンティティ */
	protected MonitorInfoEntity m_monitor;

	/** 監視対象ID */
	private String m_monitorTypeId;

	/** 監視項目ID */
	private String m_monitorId;

	/** 監視設定削除時に性能データも共に削除するか否かのフラグ*/
	private static boolean deleteCascadeFlg = true;

	/**
	 * トランザクションを開始し、監視情報を削除します。
	 *
	 * @param monitorTypeId 監視対象ID
	 * @param monitorId 監視項目ID
	 * @return 削除に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws TriggerSchedulerException
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 *
	 * @see #deleteMonitorInfo()
	 */
	public boolean delete(String monitorTypeId, String monitorId) throws MonitorNotFound, TriggerSchedulerException, HinemosUnknown, InvalidRole {

		m_monitorTypeId = monitorTypeId;
		m_monitorId = monitorId;

		boolean result = false;

		try
		{
			// 監視情報を削除
			result = deleteMonitorInfo();
		} catch (MonitorNotFound e) {
			throw e;
		} catch (TriggerSchedulerException e) {
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		}
		return result;
	}

	/**
	 * スケジューラから削除する
	 * @throws TriggerSchedulerException
	 */
	protected void deleteSchedule() throws HinemosUnknown {
		// Quartzに登録(runInterval = 0 -> スケジュール起動を行わない監視)
		if(m_monitor.getRunInterval() > 0){
			new ModifySchedule().deleteSchedule(m_monitorTypeId, m_monitorId);
		}
	}

	/**
	 * チェック条件情報を削除します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 *
	 * @return 削除に成功した場合、</code> true </code>
	 */
	protected boolean deleteCheckInfo() {
		return true;
	}

	/**
	 * 監視情報を削除します。
	 * <p>
	 * <ol>
	 * <li>監視対象IDと監視項目IDより、監視情報を取得します。</li>
	 * <li>チェック条件情報を削除します。各監視管理のサブクラスで実装します（{@link #deleteCheckInfo()}）。</li>
	 * <li>判定情報を削除します。各監視種別（真偽値，数値，文字列）のサブクラスで実装します（{@link #deleteJudgementInfo()}）。</li>
	 * <li>監視情報を削除します。</li>
	 * <li>Quartzから監視情報を削除します。</li>
	 * </ol>
	 *
	 * @return 削除に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws TriggerSchedulerException
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 *
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorInfoBean
	 * @see #deleteCheckInfo()
	 * @see com.clustercontrol.monitor.run.factory.ModifySchedule#deleteSchedule(String, String)
	 */
	private boolean deleteMonitorInfo() throws MonitorNotFound, TriggerSchedulerException, HinemosUnknown, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		try
		{
			// 監視情報を取得
			m_monitor = QueryUtil.getMonitorInfoPK(m_monitorId, ObjectPrivilegeMode.MODIFY);

			// 監視グループ情報を削除
			new NotifyControllerBean().deleteNotifyRelation(m_monitor.getNotifyGroupId());

			String deleteCascadeFlgStr = HinemosPropertyUtil.getHinemosPropertyStr(
					"monitor.common.delete.cascade.perfdata", "on");
			if("off".equals(deleteCascadeFlgStr)){
				deleteCascadeFlg = false;
			} else{
				deleteCascadeFlg = true;
			}

			m_log.info("deleteCascadeFlg = " + deleteCascadeFlg);

			// この監視設定において収集された情報を削除
			if(deleteCascadeFlg){
				m_log.info("Delete Performance Data. monitorId = " + m_monitorId);
				// この監視設定に関連する収集情報が存在しない場合は何もしない
				QueryUtil.deleteCalculatedDataByCollectorid(m_monitorId);
			}else{
				m_log.info("Not Delete Performance Data. monitorId = " + m_monitorId);
			}

			// チェック条件情報を削除
			if(deleteCheckInfo()){
				// Quartzから削除
				deleteSchedule();

				// 監視情報を削除
				em.remove(m_monitor);

				// この監視設定の監視結果状態を削除する
				List<MonitorStatusEntity> statusList =
						MonitorStatusCache.getByPluginIdAndMonitorId(m_monitorTypeId, m_monitorId);

				for(MonitorStatusEntity status : statusList){
					MonitorStatusCache.remove(status);
				}

				// この監視設定の結果として通知された通知履歴を削除する
				List<NotifyHistoryEntity> historyList =
						com.clustercontrol.notify.util.QueryUtil.getNotifyHistoryByPluginIdAndMonitorId(m_monitorTypeId, m_monitorId);
				for(NotifyHistoryEntity history : historyList){
					em.remove(history);
				}

				return true;
			}
		} catch (MonitorNotFound e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		}
		return false;
	}
}
