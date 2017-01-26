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

package com.clustercontrol.notify.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.infra.model.InfraManagementInfoEntity;
import com.clustercontrol.jobmanagement.factory.CreateJobSession;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.maintenance.model.MaintenanceInfoEntity;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.run.bean.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.notify.bean.NotifyCommandInfo;
import com.clustercontrol.notify.bean.NotifyEventInfo;
import com.clustercontrol.notify.bean.NotifyInfoDetail;
import com.clustercontrol.notify.bean.NotifyJobInfo;
import com.clustercontrol.notify.bean.NotifyLogEscalateInfo;
import com.clustercontrol.notify.bean.NotifyMailInfo;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.NotifyStatusInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.model.NotifyCommandInfoEntity;
import com.clustercontrol.notify.model.NotifyEventInfoEntity;
import com.clustercontrol.notify.model.NotifyInfoEntity;
import com.clustercontrol.notify.model.NotifyJobInfoEntity;
import com.clustercontrol.notify.model.NotifyLogEscalateInfoEntity;
import com.clustercontrol.notify.model.NotifyMailInfoEntity;
import com.clustercontrol.notify.model.NotifyStatusInfoEntity;
import com.clustercontrol.notify.monitor.util.OwnerDeterminDispatcher;
import com.clustercontrol.repository.bean.NodeInfo;
import com.clustercontrol.repository.model.FacilityEntity;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.FacilityUtil;
import com.clustercontrol.repository.util.RepositoryUtil;

/**
 * 通知に関するUtilityクラス<br/>
 *
 */
public class NotifyUtil {

	private static Log log = LogFactory.getLog(NotifyUtil.class);

	/** 監視キー */
	private static final String _KEY_PRIORITY = "PRIORITY";
	private static final String _KEY_PRIORITY_NUM = "PRIORITY_NUM";
	private static final String _KEY_PRIORITY_JP = "PRIORITY_JP";
	private static final String _KEY_PRIORITY_EN = "PRIORITY_EN";

	private static final String _KEY_PLUGIN_ID = "PLUGIN_ID";
	private static final String _KEY_MONITOR_ID = "MONITOR_ID";
	private static final String _KEY_MONITOR_DETAIL_ID = "MONITOR_DETAIL_ID";
	private static final String _KEY_MONITOR_DESCRIPTION = "MONITOR_DESCRIPTION";
	private static final String _KEY_FACILITY_ID = "FACILITY_ID";
	private static final String _KEY_SCOPE = "SCOPE";

	private static final String _KEY_FACILITY_NAME = "FACILITY_NAME";

	private static final String _KEY_GENERATION_DATE = "GENERATION_DATE";
	private static final String _KEY_APPLICATION = "APPLICATION";
	private static final String _KEY_MESSAGE_ID = "MESSAGE_ID";
	private static final String _KEY_MESSAGE = "MESSAGE";
	private static final String _KEY_ORG_MESSAGE = "ORG_MESSAGE";

	private static final String _KEY_MONITOR_OWNER_ROLE_ID = "MONITOR_OWNER_ROLE_ID";
	private static final String _KEY_CALENDAR_ID = "CALENDAR_ID";

	private static final String _KEY_JOB_MESSAGE = "JOB_MESSAGE:";

	/** 通知キー */
	private static final String _KEY_NOTIFY_ID = "NOTIFY_ID";
	private static final String _KEY_NOTIFY_DESCRIPTION = "NOTIFY_DESCRIPTION";

	/** 日時フォーマットデフォルト。 */
	private static String SUBJECT_DATE_FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss" ;

	/**
	 * 通知情報をハッシュとして返す。
	 * @param outputInfo 通知情報
	 * @return 通知情報のハッシュ
	 */
	public static Map<String, String> createParameter(OutputBasicInfo outputInfo) {
		return createParameter(outputInfo, null);
	}

	/**
	 * 通知情報をハッシュとして返す。
	 * @param outputInfo 通知情報
	 * @param notifyInfo 通知設定情報
	 * @return 通知情報のハッシュ
	 */
	public static Map<String, String> createParameter(OutputBasicInfo outputInfo, NotifyInfoEntity notifyInfo) {
		Map<String, String> param = null;
		SimpleDateFormat sdf = null;
		param = new HashMap<String, String>();

		if (outputInfo != null) {
			/** 日時フォーマット。 */
			String subjectDateFormat = HinemosPropertyUtil.getHinemosPropertyStr(
					"notify.date.format", SUBJECT_DATE_FORMAT_DEFAULT);
			if(log.isDebugEnabled()){
				log.debug("TextReplacer.static SUBJECT_DATE_FORMAT = " + subjectDateFormat);
			}

			sdf = new SimpleDateFormat(subjectDateFormat);

			param.put(_KEY_PRIORITY_NUM, String.valueOf(outputInfo.getPriority()));
			if (PriorityConstant.typeToString(outputInfo.getPriority()) != null) {
				param.put(_KEY_PRIORITY, PriorityConstant.typeToString(outputInfo.getPriority()));
			} else {
				param.put(_KEY_PRIORITY, null);
			}
			if (PriorityConstant.typeToStringJP(outputInfo.getPriority()) != null) {
				param.put(_KEY_PRIORITY_JP, PriorityConstant.typeToStringJP(outputInfo.getPriority()));
			} else {
				param.put(_KEY_PRIORITY_JP, null);
			}
			if (PriorityConstant.typeToStringEN(outputInfo.getPriority()) != null) {
				param.put(_KEY_PRIORITY_EN, PriorityConstant.typeToStringEN(outputInfo.getPriority()));
			} else {
				param.put(_KEY_PRIORITY_EN, null);
			}

			String pluginId = outputInfo.getPluginId();
			param.put(_KEY_PLUGIN_ID, pluginId);
			String monitorId = outputInfo.getMonitorId();
			param.put(_KEY_MONITOR_ID, outputInfo.getMonitorId());
			if (monitorId != null && pluginId != null && pluginId.startsWith("MON_")) {
				MonitorSettingControllerBean controller = new MonitorSettingControllerBean();
				try {
					MonitorInfo monitorInfo = controller.getMonitor(monitorId, pluginId);
					param.put(_KEY_MONITOR_DESCRIPTION, monitorInfo.getDescription());
					param.put(_KEY_CALENDAR_ID, monitorInfo.getCalendarId());
					param.put(_KEY_MONITOR_OWNER_ROLE_ID, monitorInfo.getOwnerRoleId());
				} catch (MonitorNotFound e) {
					log.debug("createParameter() : monitor not found. " + e.getMessage());
				} catch (HinemosUnknown e) {
					log.debug("createParameter() : HinemosUnknown. " + e.getMessage());
				} catch (InvalidRole e) {
					log.debug("createParameter() : InvalidRole. " + e.getMessage());
				}
			} else {
				param.put(_KEY_MONITOR_DESCRIPTION, "");
				param.put(_KEY_CALENDAR_ID, "");
				param.put(_KEY_MONITOR_OWNER_ROLE_ID, "");
			}
			param.put(_KEY_MONITOR_DETAIL_ID, outputInfo.getSubKey());

			param.put(_KEY_FACILITY_ID, outputInfo.getFacilityId());
			param.put(_KEY_SCOPE, outputInfo.getScopeText());

			if (outputInfo.getGenerationDate() != null) {
				param.put(_KEY_GENERATION_DATE, sdf.format(outputInfo.getGenerationDate()));
			} else {
				param.put(_KEY_GENERATION_DATE, null);
			}
			param.put(_KEY_APPLICATION, outputInfo.getApplication());
			param.put(_KEY_MESSAGE_ID, outputInfo.getMessageId());
			param.put(_KEY_MESSAGE, outputInfo.getMessage());
			param.put(_KEY_ORG_MESSAGE, outputInfo.getMessageOrg());

			List<String> jobFacilityIdList = outputInfo.getJobFacilityId();
			List<String> jobMessageList = outputInfo.getJobMessage();

			if (jobFacilityIdList != null) {
				for (int i = 0; i < jobFacilityIdList.size(); ++i) {
					String key = _KEY_JOB_MESSAGE + jobFacilityIdList.get(i);
					String value = jobMessageList.get(i);
					param.put(key, value);
					log.debug("NotifyUtil.createParameter  >>> param.put = : " + key  + "  value = " +  value);
				}

			}

			if (outputInfo.getFacilityId() != null) {
				try {
					RepositoryControllerBean repositoryCtrl = new RepositoryControllerBean();
					FacilityEntity facility
					= repositoryCtrl.getFacilityEntityByPK(outputInfo.getFacilityId());
					param.put(_KEY_FACILITY_NAME, facility.getFacilityName());
					if (FacilityUtil.isNode(facility)) {
						NodeInfo nodeInfo = repositoryCtrl.getNode(outputInfo.getFacilityId());
						Map<String, String> variable = RepositoryUtil.createNodeParameter(nodeInfo);
						param.putAll(variable);
					}
				} catch (FacilityNotFound e) {
					log.debug("createParameter() : facility not found. " + e.getMessage());
				} catch (InvalidRole e) {
					log.debug("createParameter() : InvalidRole. " + e.getMessage());
				} catch (HinemosUnknown e) {
					log.debug("createParameter() : HinemosUnknown. " + e.getMessage());
				} catch (Exception e) {
					log.warn("facility not found. (" + outputInfo.getFacilityId() + ") : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				}
			}
		}

		if (notifyInfo != null) {
			param.put(_KEY_NOTIFY_ID, String.valueOf(notifyInfo.getNotifyId()));
			param.put(_KEY_NOTIFY_DESCRIPTION, notifyInfo.getDescription());
		}

		if(log.isTraceEnabled()){
			for(String key : param.keySet()){
				log.trace("createParameter() : param[" + key + "]=" + param.get(key));
			}
		}

		return param;
	}

	public static void copyProperties(NotifyCommandInfo command,
			NotifyCommandInfoEntity entity) {
		entity.setInfoValidFlg(command.getInfoValidFlg());
		entity.setWarnValidFlg(command.getWarnValidFlg());
		entity.setCriticalValidFlg(command.getCriticalValidFlg());
		entity.setUnknownValidFlg(command.getUnknownValidFlg());

		entity.setInfoCommand(command.getInfoCommand());
		entity.setWarnCommand(command.getWarnCommand());
		entity.setCriticalCommand(command.getCriticalCommand());
		entity.setUnknownCommand(command.getUnknownCommand());

		entity.setInfoEffectiveUser(command.getInfoEffectiveUser());
		entity.setWarnEffectiveUser(command.getWarnEffectiveUser());
		entity.setCriticalEffectiveUser(command.getCriticalEffectiveUser());
		entity.setUnknownEffectiveUser(command.getUnknownEffectiveUser());

		entity.setCommandTimeout(command.getTimeout());
		entity.setSetEnvironment(command.getSetEnvironment());
	}

	public static void copyProperties(NotifyEventInfo event,
			NotifyEventInfoEntity entity) {
		entity.setInfoEventNormalFlg(event.getInfoValidFlg());
		entity.setWarnEventNormalFlg(event.getWarnValidFlg());
		entity.setCriticalEventNormalFlg(event.getCriticalValidFlg());
		entity.setUnknownEventNormalFlg(event.getUnknownValidFlg());

		entity.setInfoEventNormalState(event.getInfoEventNormalState());
		entity.setWarnEventNormalState(event.getWarnEventNormalState());
		entity.setCriticalEventNormalState(event.getCriticalEventNormalState());
		entity.setUnknownEventNormalState(event.getUnknownEventNormalState());
	}

	public static void copyProperties(NotifyJobInfo job,
			NotifyJobInfoEntity entity) {
		entity.setInfoJobRun(job.getInfoValidFlg());
		entity.setWarnJobRun(job.getWarnValidFlg());
		entity.setCriticalJobRun(job.getCriticalValidFlg());
		entity.setUnknownJobRun(job.getUnknownValidFlg());

		entity.setInfoJobunitId(job.getInfoJobunitId());
		entity.setWarnJobunitId(job.getWarnJobunitId());
		entity.setCriticalJobunitId(job.getCriticalJobunitId());
		entity.setUnknownJobunitId(job.getUnknownJobunitId());

		entity.setInfoJobId(job.getInfoJobId());
		entity.setWarnJobId(job.getWarnJobId());
		entity.setCriticalJobId(job.getCriticalJobId());
		entity.setUnknownJobId(job.getUnknownJobId());

		entity.setInfoJobFailurePriority(job.getInfoJobFailurePriority());
		entity.setWarnJobFailurePriority(job.getWarnJobFailurePriority());
		entity.setCriticalJobFailurePriority(job.getCriticalJobFailurePriority());
		entity.setUnknownJobFailurePriority(job.getUnknownJobFailurePriority());

		entity.setJobExecFacilityFlg(job.getJobExecFacilityFlg());
		entity.setJobExecFacilityId(job.getJobExecFacility());
	}

	public static void copyProperties(NotifyLogEscalateInfo log,
			NotifyLogEscalateInfoEntity entity) {
		entity.setInfoEscalateFlg(log.getInfoValidFlg());
		entity.setWarnEscalateFlg(log.getWarnValidFlg());
		entity.setCriticalEscalateFlg(log.getCriticalValidFlg());
		entity.setUnknownEscalateFlg(log.getUnknownValidFlg());

		entity.setInfoEscalateMessage(log.getInfoEscalateMessage());
		entity.setWarnEscalateMessage(log.getWarnEscalateMessage());
		entity.setCriticalEscalateMessage(log.getCriticalEscalateMessage());
		entity.setUnknownEscalateMessage(log.getUnknownEscalateMessage());

		entity.setInfoSyslogFacility(log.getInfoSyslogFacility());
		entity.setWarnSyslogFacility(log.getWarnSyslogFacility());
		entity.setCriticalSyslogFacility(log.getCriticalSyslogFacility());
		entity.setUnknownSyslogFacility(log.getUnknownSyslogFacility());

		entity.setInfoSyslogPriority(log.getInfoSyslogPriority());
		entity.setWarnSyslogPriority(log.getWarnSyslogPriority());
		entity.setCriticalSyslogPriority(log.getCriticalSyslogPriority());
		entity.setUnknownSyslogPriority(log.getUnknownSyslogPriority());

		entity.setEscalateFacilityFlg(log.getEscalateFacilityFlg());
		entity.setEscalatePort(log.getEscalatePort());
		entity.setEscalateFacilityId(log.getEscalateFacility());
	}

	public static void copyProperties(NotifyMailInfo mail,
			NotifyMailInfoEntity entity) {
		entity.setInfoMailFlg(mail.getInfoValidFlg());
		entity.setWarnMailFlg(mail.getWarnValidFlg());
		entity.setCriticalMailFlg(mail.getCriticalValidFlg());
		entity.setUnknownMailFlg(mail.getUnknownValidFlg());

		entity.setInfoMailAddress(mail.getInfoMailAddress());
		entity.setWarnMailAddress(mail.getWarnMailAddress());
		entity.setCriticalMailAddress(mail.getCriticalMailAddress());
		entity.setUnknownMailAddress(mail.getUnknownMailAddress());
	}

	public static void copyProperties(NotifyStatusInfo status,
			NotifyStatusInfoEntity entity) {
		entity.setInfoStatusFlg(status.getInfoValidFlg());
		entity.setWarnStatusFlg(status.getWarnValidFlg());
		entity.setCriticalStatusFlg(status.getCriticalValidFlg());
		entity.setUnknownStatusFlg(status.getUnknownValidFlg());

		entity.setStatusInvalidFlg(status.getStatusInvalidFlg());
		entity.setStatusUpdatePriority(status.getStatusUpdatePriority());
		entity.setStatusValidPeriod(status.getStatusValidPeriod());
	}

	public static ArrayList<Integer> getValidFlgIndexes(NotifyInfoDetail info) {
		Integer[] validFlgs = new Integer[] {
				info.getInfoValidFlg(),
				info.getWarnValidFlg(),
				info.getCriticalValidFlg(),
				info.getUnknownValidFlg() };

		ArrayList<Integer> validFlgIndexes = new ArrayList<Integer>();
		for (int i = 0; i < validFlgs.length; i++) {
			if (validFlgs[i] == ValidConstant.TYPE_VALID) {
				validFlgIndexes.add(i);
			}
		}
		return validFlgIndexes;
	}

	public static String getOwnerRoleId(NotifyRequestMessage msg, boolean isEvent) {
		OutputBasicInfo info = msg.getOutputInfo();
		if (info == null) {
			return null;
		}
		return getOwnerRoleId(info.getPluginId(), info.getMonitorId(), info.getSubKey(), info.getFacilityId(), isEvent);
	}
	
	public static String getOwnerRoleId(String pluginId, String monitorId, String monitorDetailId, String facilityId, boolean isEvent) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		
		// 通知元が監視の場合
		if(pluginId.matches(HinemosModuleConstant.MONITOR+".*")){
			// オブジェクト権限チェックのため、cc_monitor_infoのowner_role_idを設定する
			MonitorInfoEntity monitorInfoEntity
			= em.find(MonitorInfoEntity.class, monitorId, ObjectPrivilegeMode.NONE);
			if (monitorInfoEntity != null && monitorInfoEntity.getOwnerRoleId() != null) {
				return monitorInfoEntity.getOwnerRoleId();
			} else {
				return RoleIdConstant.INTERNAL;
			}
		}
		// 通知元がジョブの場合
		else if(pluginId.matches(HinemosModuleConstant.JOB+".*")) {
			// オブジェクト権限チェックのため、cc_job_session_jobのowner_role_idを設定する
			JobSessionEntity jobSessionEntity
			= em.find(JobSessionEntity.class, monitorId, ObjectPrivilegeMode.NONE);
			JobSessionJobEntity jobSessionJobEntity = null;
			if (jobSessionEntity == null) {
				log.warn("EventLogEntity(Job) is null : " + monitorId);
			} else {
				List<JobSessionJobEntity> jobSessionJobEntityList = jobSessionEntity.getJobSessionJobEntities();
				Iterator<JobSessionJobEntity> it = jobSessionJobEntityList.iterator();
				while(it.hasNext()) {
					jobSessionJobEntity = it.next();
					// ジョブユニット「ROOT」でない場合はwhileを抜ける
					// ジョブユニットが「ROOT」であるものは、オーナーロールIDが「ALL_USERS」であるため
					if(!jobSessionJobEntity.getId().getJobunitId().matches(CreateJobSession.TOP_JOBUNIT_ID)) {
						break;
					}
				}
			}

			if (jobSessionJobEntity != null && jobSessionJobEntity.getOwnerRoleId() != null) {
				return jobSessionJobEntity.getOwnerRoleId();
			} else {
				return RoleIdConstant.INTERNAL;
			}
		}
		// 通知元がメンテナンスの場合
		else if(pluginId.matches(HinemosModuleConstant.SYSYTEM_MAINTENANCE)){
			// オブジェクト権限チェックのため、cc_maintenance_infoのowner_role_idを設定する
			MaintenanceInfoEntity maintenanceInfoEntity
			= em.find(MaintenanceInfoEntity.class, monitorId, ObjectPrivilegeMode.NONE);
			if (maintenanceInfoEntity != null && maintenanceInfoEntity.getOwnerRoleId() != null) {
				return maintenanceInfoEntity.getOwnerRoleId();
			} else {
				return RoleIdConstant.INTERNAL;
			}
		}
		// 通知元が自動デバイスサーチの場合
		else if(pluginId.matches(HinemosModuleConstant.REPOSITORY_DEVICE_SEARCH)){
			return RoleIdConstant.ADMINISTRATORS;
		}
		// 通知元が環境構築の場合
		else if(pluginId.matches(HinemosModuleConstant.INFRA)){
			// オブジェクト権限チェックのため、cc_maintenance_infoのowner_role_idを設定する
			InfraManagementInfoEntity infraEntity
			= em.find(InfraManagementInfoEntity.class, monitorId, ObjectPrivilegeMode.NONE);
			
			if (infraEntity != null && infraEntity.getOwnerRoleId() != null) {
				return infraEntity.getOwnerRoleId();
			} else {
				return RoleIdConstant.INTERNAL;
			}
		}
		// 通知元が上記以外のプラグインIDの場合
		// ここでオプション等の任意イベント（設定に紐付かないタイプのもの）についてオーナロールを決定することが可能。
		// プラグインIDをキーにして、ObjectSharingServiceにIEventOwnerDeterminerの実装クラスを登録し、
		// そこでオーナロールを決定する。
		// 事前に当該プラグインIDに対応するIEventOwnerDeterminerの実装クラスが登録されていない場合には、
		// オーナはINTERNALとなる。
		// 但し、このルートではリフレクションを使うため、多くのイベントが発生するオプションなどでこの機構を使うと性能的に
		// 問題になる可能性が高いため、そういう場合にはここに分岐を作って直接処理をするべき。
		else {
			return OwnerDeterminDispatcher.getOptionalOwner(monitorId, monitorDetailId, pluginId, facilityId, isEvent);
		}
	}
}