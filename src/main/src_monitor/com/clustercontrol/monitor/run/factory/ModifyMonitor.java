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

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.YesNoConstant;
import com.clustercontrol.commons.scheduler.TriggerSchedulerException;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.monitor.run.bean.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.notify.bean.NotifyRelationInfo;
import com.clustercontrol.notify.model.MonitorStatusEntity;
import com.clustercontrol.notify.model.NotifyHistoryEntity;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.notify.util.MonitorStatusCache;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * 監視情報を変更する抽象クラスです。
 * <p>
 * 監視種別（真偽値，数値，文字列）の各クラスで継承してください。
 *
 * @version 4.0.0
 * @since 2.0.0
 */
abstract public class ModifyMonitor {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( ModifyMonitor.class );

	/** 監視情報のエンティティ */
	protected MonitorInfoEntity m_monitor;

	/** 監視情報。 */
	protected MonitorInfo m_monitorInfo;

	/** ファシリティ変更フラグ。 */
	protected boolean m_isModifyFacilityId = false;

	/** 実行間隔変更フラグ。 */
	private boolean m_isModifyRunInterval = false;

	/** 有効への変更フラグ。 */
	private boolean m_isModifyEnableFlg = false;

	/**
	 * スケジュール実行種別を返します。
	 */
	protected abstract TriggerType getTriggerType();

	/**
	 * スケジュール実行の遅延時間を返します。
	 */
	protected abstract int getDelayTime();

	/**
	 * トランザクションを開始し、引数で指定された監視情報を変更します。
	 * 
	 * @param info 監視情報
	 * @param user 最終変更ユーザ
	 * @return 変更に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws TriggerSchedulerException
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * 
	 */
	public boolean modify(MonitorInfo info, String user) throws MonitorNotFound, TriggerSchedulerException, HinemosUnknown, InvalidRole {

		m_monitorInfo = info;

		boolean result = false;

		// 監視情報を変更
		result = modifyMonitorInfo(user);

		return result;
	}

	/**
	 * 判定情報を変更し、監視情報に設定します。
	 * <p>
	 * 各監視種別（真偽値，数値，文字列）のサブクラスで実装します。
	 * 
	 * @return 変更に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws EntityExistsException
	 * @throws InvalidRole
	 */
	protected abstract boolean modifyJudgementInfo() throws MonitorNotFound, EntityExistsException, InvalidRole;

	/**
	 * チェック条件情報を変更し、監視情報に設定します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 * 
	 * @return 変更に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	protected abstract boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole, HinemosUnknown;

	/**
	 * 監視情報を作成します。
	 * <p>
	 * <ol>
	 * <li>監視対象IDと監視項目IDより、監視情報を取得します。</li>
	 * <li>監視情報を、引数で指定されたユーザで変更します。</li>
	 * <li>判定情報を変更し、監視情報に設定します。各監視種別（真偽値，数値，文字列）のサブクラスで実装します（{@link #modifyJudgementInfo()}）。</li>
	 * <li>チェック条件情報を変更し、監視情報に設定します。各監視管理のサブクラスで実装します（{@link #modifyCheckInfo()}）。</li>
	 * <li>実行間隔 もしくは 有効/無効が変更されている場合は、Quartzの登録を変更します。</li>
	 * </ol>
	 * 
	 * @param user ユーザ
	 * @return 更新に成功した場合、true
	 * @throws MonitorNotFound
	 * @throws TriggerSchedulerException
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	protected boolean modifyMonitorInfo(String user) throws MonitorNotFound, TriggerSchedulerException, HinemosUnknown, InvalidRole {
		Timestamp now = new Timestamp(new Date().getTime());
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		try{
			// 監視情報を設定
			m_monitor = QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId(), ObjectPrivilegeMode.MODIFY);

			// ファシリティIDが変更されているか
			if(!m_monitorInfo.getFacilityId().equals(m_monitor.getFacilityId())){
				m_isModifyFacilityId = true;
			}
			// 監視/収集間隔が変更されているか
			if(m_monitorInfo.getRunInterval() != m_monitor.getRunInterval().intValue()){
				m_isModifyRunInterval = true;
			}
			// 監視/収集無効から有効(監視or収集のいずれか1つでも)に変更されているか
			if(m_monitor.getMonitorFlg().intValue() == YesNoConstant.TYPE_NO &&
					m_monitor.getCollectorFlg().intValue() == YesNoConstant.TYPE_NO &&
					(m_monitorInfo.getMonitorFlg() == YesNoConstant.TYPE_YES || m_monitorInfo.getCollectorFlg() == YesNoConstant.TYPE_YES)){
				m_isModifyEnableFlg = true;
			}
			m_log.debug("modifyMonitorInfo() m_isModifyFacilityId = " + m_isModifyFacilityId
					+ ", m_isModifyRunInterval = " + m_isModifyRunInterval
					+ ", m_isModifyEnableFlg = " + m_isModifyEnableFlg);

			m_monitor.setDescription(m_monitorInfo.getDescription());
			if(m_isModifyFacilityId)
				m_monitor.setFacilityId(m_monitorInfo.getFacilityId());
			if(m_isModifyRunInterval)
				m_monitor.setRunInterval(m_monitorInfo.getRunInterval());
			m_monitor.setDelayTime(getDelayTime());
			m_monitor.setTriggerType(getTriggerType().name());
			m_monitor.setCalendarId(m_monitorInfo.getCalendarId());
			m_monitor.setFailurePriority(m_monitorInfo.getFailurePriority());
			m_monitor.setApplication(m_monitorInfo.getApplication());

			String notifyGroupId = NotifyGroupIdGenerator.generate(m_monitorInfo);
			if (m_monitorInfo.getNotifyId() != null) {
				for (NotifyRelationInfo notifyRelationInfo : m_monitorInfo.getNotifyId()) {
					notifyRelationInfo.setNotifyGroupId(notifyGroupId);
				}
			}

			m_monitor.setNotifyGroupId(notifyGroupId);

			m_monitor.setMonitorFlg(m_monitorInfo.getMonitorFlg());
			m_monitor.setCollectorFlg(m_monitorInfo.getCollectorFlg());
			m_monitor.setItemName(m_monitorInfo.getItemName());
			m_monitor.setMeasure(m_monitorInfo.getMeasure());
			m_monitor.setOwnerRoleId(m_monitorInfo.getOwnerRoleId());
			m_monitor.setUpdateDate(now);
			m_monitor.setUpdateUser(user);

			new NotifyControllerBean().modifyNotifyRelation(m_monitorInfo.getNotifyId(), notifyGroupId);

			// 判定情報を設定
			if(modifyJudgementInfo()){

				// チェック条件情報を設定
				if(modifyCheckInfo()){

					// Quartzの登録情報を変更
					new ModifySchedule().updateSchedule(m_monitorInfo.getMonitorId());

					// この監視設定の監視結果状態を削除する
					List<MonitorStatusEntity> statusList
						= MonitorStatusCache.getByPluginIdAndMonitorId(m_monitor.getMonitorTypeId(), m_monitor.getMonitorId());
					for(MonitorStatusEntity status : statusList){
						MonitorStatusCache.remove(status);
					}

					// この監視設定の結果として通知された通知履歴を削除する
					List<NotifyHistoryEntity> historyList
					= com.clustercontrol.notify.util.QueryUtil.getNotifyHistoryByPluginIdAndMonitorId(m_monitor.getMonitorTypeId(), m_monitor.getMonitorId());
					for(NotifyHistoryEntity history : historyList){
						em.remove(history);
					}

					return true;
				}
			}
			return false;

		} catch (NotifyNotFound e) {
			throw new MonitorNotFound(e.getMessage(), e);
		} catch (MonitorNotFound e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		}
	}

}
