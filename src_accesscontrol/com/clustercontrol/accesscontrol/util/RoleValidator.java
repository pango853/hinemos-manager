/*

Copyright (C) since 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.accesscontrol.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PluginConstant;
import com.clustercontrol.calendar.model.CalInfoEntity;
import com.clustercontrol.calendar.model.CalPatternInfoEntity;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.UsedOwnerRole;
import com.clustercontrol.infra.model.InfraManagementInfoEntity;
import com.clustercontrol.jobmanagement.model.JobFileCheckEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobScheduleEntity;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.notify.mail.model.MailTemplateInfoEntity;
import com.clustercontrol.notify.model.NotifyInfoEntity;
import com.clustercontrol.repository.model.FacilityEntity;
import com.clustercontrol.util.Messages;

/**
 * ロール管理の入力チェッククラス
 * 
 * @since 4.0
 */
public class RoleValidator {

	private static Log m_log = LogFactory.getLog( RoleValidator.class );

	/**
	 * ロール情報(RoleInfo)の基本設定の妥当性チェック
	 * @param roleInfo
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public static void validateRoleInfo(RoleInfo roleInfo) throws InvalidSetting {

		// roleId
		CommonValidator.validateId(Messages.getString("role.id"), roleInfo.getId(), 64);

		// roleName
		CommonValidator.validateString(Messages.getString("role.name"), roleInfo.getName(), true, 1, 128);

		// description
		CommonValidator.validateString(Messages.getString("description"), roleInfo.getDescription(), false, 0, 256);

	}


	/**
	 * 他の機能にて、オーナーロールとして使用されているか調査する。
	 * 参照状態の場合、メッセージダイアログが出力される。
	 * @param roleId
	 * @throw UsedRole
	 * @throw HinemosUnknown
	 */
	public static void validateDeleteRole(String roleId) throws UsedOwnerRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// リポジトリ（ノード、スコープ）
			List<FacilityEntity> infoCollectionFacility
			= com.clustercontrol.repository.util.QueryUtil.getFacilityByOwnerRoleId_NONE(roleId);
			if (infoCollectionFacility != null && infoCollectionFacility.size() > 0) {
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.STRING_REPOSITORY);
				throw new UsedOwnerRole(PluginConstant.TYPE_REPOSITORY);
			}

			// 監視設定
			List<MonitorInfoEntity> infoCollectionMonitor
			=  com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoByOwnerRoleId_NONE(roleId);
			if (infoCollectionMonitor != null && infoCollectionMonitor.size() > 0) {
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.STRING_MONITOR);
				throw new UsedOwnerRole(PluginConstant.TYPE_MONITOR);
			}

			// ジョブ
			List<JobMstEntity> infoCollectionJob
			= com.clustercontrol.jobmanagement.util.QueryUtil.getJobMstEntityFindByOwnerRoleId_NONE(roleId);
			if (infoCollectionJob != null && infoCollectionJob.size() > 0) {
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.STRING_JOBMANAGEMENT);
				throw new UsedOwnerRole(PluginConstant.TYPE_JOBMANAGEMENT);
			}

			// ジョブファイルチェック
			List<JobFileCheckEntity> infoCollectionJobFile
			= com.clustercontrol.jobmanagement.util.QueryUtil.getJobFileCheckEntityFindByOwnerRoleId_NONE(roleId);
			if (infoCollectionJobFile != null && infoCollectionJobFile.size() > 0) {
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.STRING_JOBMANAGEMENT);
				throw new UsedOwnerRole(PluginConstant.TYPE_JOBMANAGEMENT);
			}

			// ジョブスケジュール
			List<JobScheduleEntity> infoCollectionJobSche
			= com.clustercontrol.jobmanagement.util.QueryUtil.getJobScheduleEntityFindByOwnerRoleId_NONE(roleId);
			if (infoCollectionJobSche != null && infoCollectionJobSche.size() > 0) {
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.STRING_JOBMANAGEMENT);
				throw new UsedOwnerRole(PluginConstant.TYPE_JOBMANAGEMENT);
			}

			// カレンダ
			List<CalInfoEntity> infoCollectionCalInfo
			= com.clustercontrol.calendar.util.QueryUtil.getCalInfoFindByOwnerRoleId_NONE(roleId);
			if (infoCollectionCalInfo != null && infoCollectionCalInfo.size() > 0) {
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.STRING_CALENDAR);
				throw new UsedOwnerRole(PluginConstant.TYPE_CALENDAR);
			}

			// カレンダパターン
			List<CalPatternInfoEntity> infoCollectionCalPattern
			= com.clustercontrol.calendar.util.QueryUtil.getCalPatternInfoFindByOwnerRoleId_NONE(roleId);
			if (infoCollectionCalPattern != null && infoCollectionCalPattern.size() > 0) {
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.STRING_CALENDAR);
				throw new UsedOwnerRole(PluginConstant.TYPE_CALENDAR);
			}

			// 通知
			List<NotifyInfoEntity> infoCollectionNotifyInfo
			= com.clustercontrol.notify.util.QueryUtil.getNotifyInfoFindByOwnerRoleId_NONE(roleId);
			if (infoCollectionNotifyInfo != null && infoCollectionNotifyInfo.size() > 0) {
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.STRING_NOTIFY);
				throw new UsedOwnerRole(PluginConstant.TYPE_NOTIFY);
			}

			// メールテンプレート
			List<MailTemplateInfoEntity> infoCollectionMailTemp
			= com.clustercontrol.notify.mail.util.QueryUtil.getMailTemplateInfoFindByOwnerRoleId_NONE(roleId);
			if (infoCollectionMailTemp != null && infoCollectionMailTemp.size() > 0) {
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.STRING_NOTIFY);
				throw new UsedOwnerRole(PluginConstant.TYPE_NOTIFY);
			}

			// 環境構築
			List<InfraManagementInfoEntity> infoCollectionInfra
			= com.clustercontrol.infra.util.QueryUtil.getInfraManagementInfoFindByOwnerRoleId_NONE(roleId);
			if (infoCollectionInfra != null && infoCollectionInfra.size() > 0) {
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.STRING_INFRA);
				throw new UsedOwnerRole(PluginConstant.TYPE_INFRA);
			}
			
			
			jtm.commit();
		} catch (UsedOwnerRole e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("validateDeleteRole() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
		} finally {
			jtm.close();
		}
	}

	/**
	 * 変更前後でオーナーロールが変更されていないかチェックする
	 * 
	 * @param pk
	 * @param objectType
	 * @param ownerRoleId
	 * @throw InvalidSetting
	 */
	public static void validateModifyOwnerRole(Object pk, String objectType, String ownerRoleId) throws InvalidSetting{

		JpaTransactionManager jtm = null;
		InvalidSetting ise = new InvalidSetting(Messages.getString("message.accesscontrol.55"));

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// リポジトリ(ノード、スコープ)
			if (objectType.equals(HinemosModuleConstant.PLATFORM_REPOSITORY)) {

				m_log.debug("validateModifyOwnerRole() : FacilityEntity check.");
				FacilityEntity info = null;

				try {
					info = com.clustercontrol.repository.util.QueryUtil.getFacilityPK((String)pk);
				}catch (FacilityNotFound e) {
					// 設定が見つからない場合は新規登録と判断し、何もしない;
				}

				// 既存の設定のオーナーロールから変更されている場合
				if(info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}
			// 監視
			else if (objectType.equals(HinemosModuleConstant.MONITOR)) {

				m_log.debug("validateModifyOwnerRole() : MonitorInfoEntity check.");
				MonitorInfoEntity info = null;

				try {
					info = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK((String)pk);
				} catch (MonitorNotFound e) {
					// 設定が見つからない場合は新規登録と判断し、何もしない
				}

				// 既存の設定のオーナーロールから変更されている場合
				if(info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}
			// ジョブユニット
			else if (objectType.equals(HinemosModuleConstant.JOB)) {

				m_log.debug("validateModifyOwnerRole() : JobMstEntity check.");
				JobMstEntity info = null;

				try {
					info = com.clustercontrol.jobmanagement.util.QueryUtil.getJobMstPK_NONE((JobMstEntityPK)pk);
				} catch (JobMasterNotFound e) {
					// 設定が見つからない場合は新規登録と判断し、何もしない
				}

				// 既存の設定のオーナーロールから変更されている場合
				if(info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}

			// ジョブファイルチェック
			else if (objectType.equals(HinemosModuleConstant.JOB_FILE_CHECK)) {

				m_log.debug("validateModifyOwnerRole() : JobFileCheckEntity check.");
				HinemosEntityManager em = jtm.getEntityManager();
				JobFileCheckEntity info = em.find(JobFileCheckEntity.class, pk, ObjectPrivilegeMode.READ);
				if (info == null) {
					// 設定が見つからない場合は新規登録と判断し、何もしない
				}

				// 既存の設定のオーナーロールから変更されている場合
				if(info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}

			// ジョブスケジュール
			else if (objectType.equals(HinemosModuleConstant.JOB_SCHEDULE)) {

				m_log.debug("validateModifyOwnerRole() : JobScheduleEntity check.");
				HinemosEntityManager em = jtm.getEntityManager();
				JobScheduleEntity info = em.find(JobScheduleEntity.class, pk, ObjectPrivilegeMode.READ);
				if (info == null) {
					// 設定が見つからない場合は新規登録と判断し、何もしない
				}

				// 既存の設定のオーナーロールから変更されている場合
				if(info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}

			// カレンダ
			else if (objectType.equals(HinemosModuleConstant.PLATFORM_CALENDAR)) {

				m_log.debug("validateModifyOwnerRole() : CalInfoEntity check.");
				CalInfoEntity info = null;

				try {
					info = com.clustercontrol.calendar.util.QueryUtil.getCalInfoPK((String)pk);
				} catch (CalendarNotFound e) {
					// 設定が見つからない場合は新規登録と判断し、何もしない
				}

				// 既存の設定のオーナーロールから変更されている場合
				if(info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}

			// カレンダパターン
			else if (objectType.equals(HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN)) {

				m_log.debug("validateModifyOwnerRole() : CalPatternInfoEntity check.");
				CalPatternInfoEntity info = null;

				try {
					info = com.clustercontrol.calendar.util.QueryUtil.getCalPatternInfoPK((String)pk);
				} catch (CalendarNotFound e) {
					// 設定が見つからない場合は新規登録と判断し、何もしない
				}

				// 既存の設定のオーナーロールから変更されている場合
				if(info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}

			// 通知
			else if (objectType.equals(HinemosModuleConstant.PLATFORM_NOTIFY)) {

				m_log.debug("validateModifyOwnerRole() : NotifyInfoEntity check.");
				NotifyInfoEntity info = null;

				try {
					info = com.clustercontrol.notify.util.QueryUtil.getNotifyInfoPK((String)pk);
				} catch (NotifyNotFound e) {
					// 設定が見つからない場合は新規登録と判断し、何もしない
				}

				// 既存の設定のオーナーロールから変更されている場合
				if(info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}

			// メールテンプレート
			else if (objectType.equals(HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE)) {

				m_log.debug("validateModifyOwnerRole() : MailTemplateInfoEntity check.");
				MailTemplateInfoEntity info = null;

				try {
					info = com.clustercontrol.notify.mail.util.QueryUtil.getMailTemplateInfoPK((String)pk);
				} catch (MailTemplateNotFound e) {
					// 設定が見つからない場合は新規登録と判断し、何もしない
				}

				// 既存の設定のオーナーロールから変更されている場合
				if(info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}

			jtm.commit();
		} catch (InvalidSetting e) {
			m_log.warn("validateModifyOwnerRole() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("validateModifyOwnerRole() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
		} finally {
			jtm.close();
		}
	}


}
