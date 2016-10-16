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
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.commons.scheduler.TriggerSchedulerException;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.monitor.run.bean.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.notify.bean.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * 監視情報を作成する抽象クラス<BR>
 * <p>
 * 監視種別（真偽値，数値，文字列）の各クラスで継承してください。
 *
 * @version 4.0.0
 * @since 2.0.0
 */
abstract public class AddMonitor {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( AddMonitor.class );

	/** 監視情報。 */
	protected MonitorInfo m_monitorInfo;

	/**
	 * トランザクションを開始し、引数で指定された監視情報を作成します。
	 * 
	 * @param info 監視情報
	 * @param user 新規作成ユーザ
	 * @return 作成に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws MonitorDuplicate
	 * @throws TriggerSchedulerException
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * 
	 * @see #addMonitorInfo(String)
	 */
	public boolean add(MonitorInfo info, String user) throws MonitorNotFound, MonitorDuplicate, TriggerSchedulerException, HinemosUnknown, InvalidRole {

		m_monitorInfo = info;

		boolean result = false;

		try{
			// 監視情報を登録
			result = addMonitorInfo(user);
		} catch (EntityExistsException e) {
			throw new MonitorDuplicate(e.getMessage(),e);
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
	 * 判定情報を作成し、監視情報に設定します。
	 * <p>
	 * 各監視種別（真偽値，数値，文字列）のサブクラスで実装します。
	 * 
	 * @return 作成に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 */
	protected abstract boolean addJudgementInfo() throws MonitorNotFound, InvalidRole;

	/**
	 * チェック条件情報を作成し、監視情報に設定します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 * 
	 * @return 作成に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	protected abstract boolean addCheckInfo() throws MonitorNotFound, HinemosUnknown, InvalidRole;

	/**
	 * スケジュール実行種別を返します。
	 */
	protected abstract TriggerType getTriggerType();

	/**
	 * スケジュール実行の遅延時間を返します。
	 */
	protected abstract int getDelayTime();

	/**
	 * 監視項目IDをベースにスケジュール実行の遅延時間を生成して返します。
	 */
	public static int getDelayTimeBasic(MonitorInfo monitorInfo){
		// 再起動時も常に同じタイミングでQuartzのTriggerが起動されるように収集種別と収集項目IDから、DelayTimeを生成する

		// 収集種別と収集項目IDを結合した文字列のhashを求める
		int hashCode = (monitorInfo.getMonitorId() + monitorInfo.getMonitorType()).hashCode();

		// hashをシードとして乱数を作成する。このとき乱数の範囲は、0～(monitorInfo-1)とする
		int offsetSecond = new Random(hashCode).nextInt(monitorInfo.getRunInterval());
		m_log.debug("MonitorID : " + monitorInfo.getMonitorId()
				+ ", MonitorType : " + monitorInfo.getMonitorType()
				+ ", offset : " + offsetSecond);

		return offsetSecond;
	}


	/**
	 * 監視情報を作成します。
	 * <p>
	 * <ol>
	 * <li>監視情報を、引数で指定されたユーザで作成します。</li>
	 * <li>判定情報を作成し、監視情報に設定します。各監視種別（真偽値，数値，文字列）のサブクラスで実装します（{@link #addJudgementInfo()}）。</li>
	 * <li>チェック条件情報を作成し、監視情報に設定します。各監視管理のサブクラスで実装します（{@link #addCheckInfo()}）。</li>
	 * <li>Quartzに、スケージュールと監視情報の有効/無効を登録します。</li>
	 * </ol>
	 * 
	 * @param user 新規作成ユーザ
	 * @return 作成に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws TriggerSchedulerException
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorInfoBean
	 * @see #addJudgementInfo()
	 * @see #addCheckInfo()
	 * @see com.clustercontrol.monitor.run.factory.ModifySchedule#addSchedule(MonitorInfo, String, Calendar)
	 */
	protected boolean addMonitorInfo(String user) throws MonitorNotFound, TriggerSchedulerException, EntityExistsException, HinemosUnknown, InvalidRole {
		Timestamp now = new Timestamp(new Date().getTime());
		JpaTransactionManager jtm = new JpaTransactionManager();

		try{
			// 監視情報を挿入
			// インスタンス生成
			MonitorInfoEntity entity = new MonitorInfoEntity(m_monitorInfo.getMonitorId());
			// 重複チェック
			jtm.checkEntityExists(MonitorInfoEntity.class, entity.getMonitorId());
			entity.setApplication(m_monitorInfo.getApplication());
			entity.setCollectorFlg(m_monitorInfo.getCollectorFlg());
			entity.setDelayTime(getDelayTime());
			entity.setDescription(m_monitorInfo.getDescription());
			entity.setFailurePriority(m_monitorInfo.getFailurePriority());
			entity.setItemName(m_monitorInfo.getItemName());
			entity.setMeasure(m_monitorInfo.getMeasure());
			entity.setMonitorFlg(m_monitorInfo.getMonitorFlg());
			entity.setMonitorType(m_monitorInfo.getMonitorType());
			entity.setMonitorTypeId(m_monitorInfo.getMonitorTypeId());
			entity.setNotifyGroupId(NotifyGroupIdGenerator.generate(m_monitorInfo));
			entity.setOwnerRoleId(m_monitorInfo.getOwnerRoleId());
			entity.setRegDate(now);
			entity.setRegUser(user);
			entity.setRunInterval(m_monitorInfo.getRunInterval());
			entity.setTriggerType(getTriggerType().name());
			entity.setUpdateDate(now);
			entity.setUpdateUser(user);
			entity.setCalendarId(m_monitorInfo.getCalendarId());
			entity.setFacilityId(m_monitorInfo.getFacilityId());

			//notifyGroupIdの更新
			String notifyGroupId = NotifyGroupIdGenerator.generate(m_monitorInfo);
			if (m_monitorInfo.getNotifyId() != null) {
				for (NotifyRelationInfo notifyRelationInfo : m_monitorInfo.getNotifyId()) {
					notifyRelationInfo.setNotifyGroupId(notifyGroupId);
				}
			}

			// 通知情報を投入
			new NotifyControllerBean().addNotifyRelation(m_monitorInfo.getNotifyId());

			// 判定情報を設定
			if(addJudgementInfo()){
				// チェック条件情報を設定
				if(addCheckInfo()){
					// Quartzに登録(runInterval = 0 -> スケジュール起動を行わない監視)
					if(m_monitorInfo.getRunInterval() > 0){
						ModifySchedule quartz = new ModifySchedule();
						quartz.updateSchedule(m_monitorInfo.getMonitorId());
					}
					return true;
				}
			}
			return false;

		} catch (EntityExistsException e) {
			m_log.info("addMonitorInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		} catch (MonitorNotFound e) {
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		}
	}
}
