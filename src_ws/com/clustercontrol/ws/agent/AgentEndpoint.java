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
package com.clustercontrol.ws.agent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.annotation.Resource;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.bean.SettingUpdateInfo;
import com.clustercontrol.custom.bean.CommandExecuteDTO;
import com.clustercontrol.custom.bean.CommandResultDTO;
import com.clustercontrol.custom.session.MonitorCustomControllerBean;
import com.clustercontrol.fault.CustomInvalid;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.JobSessionDuplicate;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.hinemosagent.bean.AgentOutputBasicInfo;
import com.clustercontrol.hinemosagent.bean.HinemosTopicInfo;
import com.clustercontrol.hinemosagent.bean.TopicInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.jobmanagement.bean.JobFileCheck;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.bean.RunResultInfo;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.jobmanagement.session.JobRunManagementBean;
import com.clustercontrol.logfile.bean.LogfileResultDTO;
import com.clustercontrol.logfile.session.MonitorLogfileControllerBean;
import com.clustercontrol.monitor.run.bean.MonitorInfo;
import com.clustercontrol.monitor.run.bean.MonitorStringValueInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.systemlog.service.MessageInfo;
import com.clustercontrol.util.apllog.AplLogger;
import com.clustercontrol.winevent.bean.WinEventResultDTO;
import com.clustercontrol.winevent.session.MonitorWinEventControllerBean;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * ジョブ操作用のWebAPIエンドポイント
 */
@MTOM
@javax.jws.WebService(targetNamespace = "http://agent.ws.clustercontrol.com")
public class AgentEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( AgentEndpoint.class );

	/**
	 * echo(WebサービスAPI疎通用)
	 *
	 * 権限必要なし（ユーザ名チェックのみ実施）
	 *
	 * @param str
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public String echo(String str) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return str + ", " + str;
	}

	/**
	 * [Agent Base] エージェントからwebサービスで利用。
	 * エージェントから取得要請のあるものをまとめて返す。
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param agentInfo
	 * @return
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	public HinemosTopicInfo getHinemosTopic (AgentInfo agentInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole
	{
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		HinemosTopicInfo hinemosTopicInfo = new HinemosTopicInfo();

		// TopicInfo のリストを設定
		ArrayList<String> facilityIdList = getFacilityId(agentInfo);
		for (String facilityId : facilityIdList) {
			AgentConnectUtil.checkAgentLibMd5(facilityId);
		}

		ArrayList<TopicInfo> topicInfoList = new ArrayList<TopicInfo>();
		for (String facilityId : facilityIdList) {
			ArrayList<TopicInfo> list = AgentConnectUtil.getTopic(facilityId);
			/*
			 * Agent.propertiesでfacilityIdが直接指定されていない場合、
			 * agentInfoにfacilityIdが含まれていないので、ここで詰める。
			 */
			agentInfo.setFacilityId(facilityId);
			AgentConnectUtil.putAgentMap(agentInfo);
			if (list != null && list.size() != 0) {
				topicInfoList.addAll(list);
			}
		}

		int awakePort = 0;
		for (String facilityId : facilityIdList) {
			try {
				int tmp = NodeProperty.getProperty(facilityId).getAgentAwakePort();
				if (awakePort != 0 && tmp != awakePort) {
					m_log.warn("getHinemosTopic() different awake port " + tmp); 
				}
				awakePort = tmp;
				hinemosTopicInfo.setAwakePort(awakePort);
			} catch (FacilityNotFound e) {
				m_log.info("getHinemosTopic() : FacilityNotFound " + facilityId); 
			}
		}
		hinemosTopicInfo.setTopicInfoList(topicInfoList);

		// SettingUpdateInfo を設定
		hinemosTopicInfo.setSettingUpdateInfo(SettingUpdateInfo.getInstance());

		return hinemosTopicInfo;
	}


	/**
	 * [Agent Base] エージェントからwebサービスで利用。
	 * エージェントがシャットダウンする際に利用。(shutdownhook に登録)
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param agentInfo
	 * @return
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	public void deleteAgent (AgentInfo agentInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		ArrayList<String> facilityIdList = getFacilityId(agentInfo);
		for (String facilityId : facilityIdList) {
			m_log.info("deleteAgent " + facilityId + " is shutdown");
			AgentConnectUtil.deleteAgent(facilityId, agentInfo);
		}
	}

	/**
	 * [Agent Base] Internalイベントに出力する際に利用する。
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param message
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public void sendMessage(AgentOutputBasicInfo message)
			throws HinemosUnknown, FacilityNotFound,
			InvalidUserPass, InvalidRole {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		ArrayList<String> facilityIdList = getFacilityId(message.getAgentInfo());
		OutputBasicInfo outputBasicInfo = message.getOutputBasicInfo();
		if (facilityIdList == null || facilityIdList.size() == 0) {
			m_log.info("sendMessage facilityId is null");
			return;
		}
		if (facilityIdList.size() == 0) {
			m_log.info("sendMessage facilityId.size() is 0");
			return;
		}
		AgentConnectUtil.sendMessageLocal(outputBasicInfo, facilityIdList);
	}

	/**
	 * [Agent Base] AgentInfoからFacilityIdのリストを取得する。
	 *
	 * @param agentInfo
	 * @return
	 * @throws HinemosUnknown
	 */
	private ArrayList<String> getFacilityId(AgentInfo agentInfo) throws HinemosUnknown {
		ArrayList<String> facilityIdList = new ArrayList<String>();

		if (agentInfo.getFacilityId() != null && !agentInfo.getFacilityId().equals("")) {
			/*
			 * agentInfoにfacilityIdが入っている場合。
			 */
			// 複数facilityId対応。
			// agentInfoの内容をカンマ(,)で分割する。
			StringTokenizer st = new StringTokenizer(agentInfo.getFacilityId(), ",");
			while (st.hasMoreTokens()) {
				String facilityId = st.nextToken();
				facilityIdList.add(facilityId);
				m_log.debug("add facilityId=" + facilityId);
			}

		} else {
			/*
			 * agentInfoにfacilityIdが入っていない場合。
			 * この場合は、ホスト名とIPアドレスからファシリティIDを決める。
			 */
			try {
				for (String ipAddress : agentInfo.getIpAddress()) {
					ArrayList<String> list
					= new RepositoryControllerBean().getFacilityIdList(agentInfo.getHostname(), ipAddress);
					if (list != null && list.size() != 0) {
						for (String facilityId : list) {
							m_log.debug("facilityId=" + facilityId + ", " + agentInfo.toString());
						}
						facilityIdList.addAll(list);
					}
				}
			} catch (Exception e) {
				m_log.warn(e,e);
				throw new HinemosUnknown("getFacilityId " + e.getMessage());
			}
		}
		return facilityIdList;
	}

	/**
	 * [Job] ジョブの結果を送信する。
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param info
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public boolean jobResult (RunResultInfo info)
			throws HinemosUnknown, JobInfoNotFound,
			InvalidUserPass, InvalidRole {
		m_log.info("jobResult : " +
				info.getSessionId() + ", " +
				info.getJobunitId() + ", " +
				info.getJobId() + ", " +
				info.getCommandType() + ", " +
				info.getCommand() + ", " +
				info.getStatus() + ", " +
				info.getFacilityId() + ", "
				);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return new JobRunManagementBean().endNode(info);
	}

	/**
	 * [Logfile] ログファイル監視の監視設定を取得
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param agentInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getMonitorLogfile (AgentInfo agentInfo) throws MonitorNotFound, HinemosUnknown, InvalidUserPass, InvalidRole
	{
		m_log.debug("getMonitorLogFile : " + agentInfo);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		ArrayList<String> facilityIdList = new ArrayList<String>();

		if (agentInfo.getFacilityId() != null && !agentInfo.getFacilityId().equals("")) {
			facilityIdList.add(agentInfo.getFacilityId());
		} else {
			try {
				RepositoryControllerBean bean = new RepositoryControllerBean();
				for (String ipAddress : agentInfo.getIpAddress()) {
					ArrayList<String> list = bean.getFacilityIdList(agentInfo.getHostname(), ipAddress);
					if (list != null && list.size() != 0) {
						for (String facilityId : list) {
							m_log.debug("facilityId=" + facilityId + ", " + agentInfo.toString());
						}
						facilityIdList.addAll(list);
					}
				}
			} catch (Exception e) {
				m_log.warn(e,e);
				return null;
			}
		}

		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		MonitorLogfileControllerBean bean = new MonitorLogfileControllerBean();
		for (String facilityId : facilityIdList) {
			list.addAll(bean.getLogfileListForFacilityId(facilityId, true));
		}
		return list;
	}

	/**
	 * [JobFileCheck] ファイルチェック(ジョブ)の設定を取得
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param agentInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws JobMasterNotFound
	 */
	public ArrayList<JobFileCheck> getFileCheckForAgent (AgentInfo agentInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole, JobMasterNotFound
	{
		m_log.debug("getFileCheckForAgent : " + agentInfo);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		ArrayList<String> facilityIdList = new ArrayList<String>();

		if (agentInfo.getFacilityId() != null && !agentInfo.getFacilityId().equals("")) {
			facilityIdList.add(agentInfo.getFacilityId());
		} else {
			try {
				RepositoryControllerBean bean = new RepositoryControllerBean();
				for (String ipAddress : agentInfo.getIpAddress()) {
					ArrayList<String> list = bean.getFacilityIdList(agentInfo.getHostname(), ipAddress);
					if (list != null && list.size() != 0) {
						for (String facilityId : list) {
							m_log.debug("facilityId=" + facilityId + ", " + agentInfo.toString());
						}
						facilityIdList.addAll(list);
					}
				}
			} catch (Exception e) {
				m_log.warn(e,e);
				return null;
			}
		}

		return new JobControllerBean().getJobFileCheck(facilityIdList);
	}


	/**
	 * [JobFileCheck] ファイルチェック(ジョブ)の結果
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param jobFileCheck
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws JobMasterNotFound
	 * @throws JobInfoNotFound
	 * @throws FacilityNotFound
	 */
	public String jobFileCheckResult (JobFileCheck jobFileCheck, AgentInfo agentInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole, JobMasterNotFound, FacilityNotFound, JobInfoNotFound, JobSessionDuplicate
	{
		String id = jobFileCheck.getId();
		String jobunitId = jobFileCheck.getJobunitId();
		String jobId = jobFileCheck.getJobId();
		String filename = jobFileCheck.getFileName();
		String directory = jobFileCheck.getDirectory();
		Integer eventType = jobFileCheck.getEventType();
		Integer modifyType = jobFileCheck.getModifyType();
		m_log.info("jobFileCheckResult : id=" + id + ", jobunitId=" + jobunitId + ", jobId=" + jobId
				+ ", filename=" + filename + ", directory=" + directory + ", eventType=" + eventType + ", modifyType=" + modifyType);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		JobTriggerInfo trigger = new JobTriggerInfo();
		trigger.setTrigger_type(JobTriggerTypeConstant.TYPE_FILECHECK);
		trigger.setTrigger_info(jobFileCheck.getName() + "(" + id + ") file=" + filename);
		trigger.setFilename(filename);
		trigger.setDirectory(directory);
		OutputBasicInfo output = null;
		String sessionId = null;
		for (String facilityId : getFacilityId(agentInfo)) {
			ArrayList<String> facilityList =
					FacilitySelector.getFacilityIdList(jobFileCheck.getFacilityId(), jobFileCheck.getOwnerRoleId(), 0, false, false);
			if (facilityList.contains(facilityId)) {
				output = new OutputBasicInfo();
				output.setFacilityId(facilityId);
				try {
					sessionId = new JobControllerBean().runJob(jobunitId, jobId, output, trigger);
				} catch (Exception e) {
					m_log.warn("jobFileCheckResult() : " + e.getMessage());
					AplLogger apllog = new AplLogger(HinemosModuleConstant.JOB, "job");
					String[] args = { jobId, trigger.getTrigger_info() };
					apllog.put("SYS", "017", args);
					throw new HinemosUnknown(e.getMessage(), e);
				}
			}
		}
		return sessionId;
	}

	/**
	 * [WinEvent] Windowsイベント監視の監視設定を取得
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param agentInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getMonitorWinEvent (AgentInfo agentInfo) throws MonitorNotFound, HinemosUnknown, InvalidUserPass, InvalidRole
	{
		m_log.debug("getMonitorWinEvent : " + agentInfo);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		ArrayList<String> facilityIdList = new ArrayList<String>();

		if (agentInfo.getFacilityId() != null && !agentInfo.getFacilityId().equals("")) {
			facilityIdList.add(agentInfo.getFacilityId());
		} else {
			try {
				RepositoryControllerBean bean = new RepositoryControllerBean();
				for (String ipAddress : agentInfo.getIpAddress()) {
					ArrayList<String> list = bean.getFacilityIdList(agentInfo.getHostname(), ipAddress);
					if (list != null && list.size() != 0) {
						for (String facilityId : list) {
							m_log.debug("facilityId=" + facilityId + ", " + agentInfo.toString());
						}
						facilityIdList.addAll(list);
					}
				}
			} catch (Exception e) {
				m_log.warn(e,e);
				return null;
			}
		}

		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		MonitorWinEventControllerBean bean = new MonitorWinEventControllerBean();
		for (String facilityId : facilityIdList) {
			list.addAll(bean.getWinEventList(facilityId));
		}
		return list;
	}

	/**
	 * [Logfile] ログファイル監視でマッチしたものに対して通知をマネージャに依頼する。
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	@Deprecated
	public void sendNotify(String message, MessageInfo logmsg,
			MonitorInfo monitorInfo, MonitorStringValueInfo rule, AgentInfo agentInfo) throws HinemosUnknown, InvalidRole, InvalidUserPass {

		m_log.debug("sendNotify : " + message);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		for (String logFacilityId : getFacilityId(agentInfo)) {
			String facilityId = monitorInfo.getFacilityId();
			ArrayList<String> facilityList = FacilitySelector.getFacilityIdList(facilityId, monitorInfo.getOwnerRoleId(), 0, false, false);
			String str = "";
			if (m_log.isDebugEnabled()) {
				for (String s : facilityList) {
					str += s + ", ";
				}
				m_log.debug("facilityId=" + logFacilityId + ", (" + str + ")");
			}
			if (facilityList.contains(logFacilityId)) {
				new MonitorLogfileControllerBean().run(logFacilityId, Arrays.asList(new LogfileResultDTO(message, logmsg, monitorInfo, rule)));
			} else {
				m_log.debug("sendNotify facilityId=" + logFacilityId + ", " + str);
			}
		}
	}
	

	/**
	 * [Command Monitor] コマンド実行情報を問い合わせられた場合にコールされるメソッドであり、問い合わせたエージェントが実行すべきコマンド実行情報を返す。
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param agentInfo メソッドをコールしたエージェント情報
	 * @return コマンド実行情報の一覧
	 * @throws CustomInvalid コマンド監視設定に不整合が見つかった場合にスローされる例外
	 * @throws HinemosUnknown 予期せぬ内部エラーによりスローされる例外
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<CommandExecuteDTO> getCommandExecuteDTO(AgentInfo agentInfo)
			throws CustomInvalid, HinemosUnknown,
			InvalidUserPass, InvalidRole
			{
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// Local Variables
		ArrayList<CommandExecuteDTO> dtos = null;
		ArrayList<String> facilityIds = null;

		// MAIN
		facilityIds = getFacilityId(agentInfo);
		dtos = new ArrayList<CommandExecuteDTO>();
		MonitorCustomControllerBean monitorCmdCtrl = new MonitorCustomControllerBean();
		for (String facilityId : facilityIds) {
			dtos.addAll(monitorCmdCtrl.getCommandExecuteDTO(facilityId));
		}

		return dtos;
			}

	/**
	 * [Custom Monitor] コマンド監視において、コマンドの実行結果をマネージャに通知する。
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param result コマンドの実行結果
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	@Deprecated
	public void putCommandResultDTO(CommandResultDTO result) throws InvalidUserPass, InvalidRole, HinemosUnknown
	{
		forwardCustomResult(Arrays.asList(result));
	}
	
	/**
	 * [Custom Monitor] コマンド監視において、コマンドの実行結果をまとめてマネージャに通知する。
	 * 1 HTTP Requestで多数のコマンド実行結果を送信できるため、リソース観点から効率的に処理できる。
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param resultList コマンドの実行結果のリスト
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	public void forwardCustomResult(List<CommandResultDTO> resultList)  throws InvalidUserPass, InvalidRole, HinemosUnknown
	{
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// MAIN
		try {
			new MonitorCustomControllerBean().evalCommandResult(resultList);
		} catch (MonitorNotFound e) {
			m_log.warn("monitor not found...", e);
		} catch (HinemosUnknown e) {
			m_log.warn("unexpected internal failure occurred...", e);
			throw e;
		} catch (CustomInvalid e) {
			m_log.warn("configuration of command monitor is not valid...", e);
		}
	}
	

	/**
	 * [Logfile] ログファイル監視でマッチしたものに対して通知をマネージャに依頼する。
	 * 1 HTTP Requestで多数の監視実行結果を送信できるため、リソース観点から効率的に処理できる。
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public void forwardLogfileResult(List<LogfileResultDTO> resultList, AgentInfo agentInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		for (String facilityId : getFacilityId(agentInfo)) {
			new MonitorLogfileControllerBean().run(facilityId, resultList);
		}
	}
	
	/**
	 * [WinEvent] Windowsイベント監視でマッチしたものに対して通知をマネージャに依頼する。
	 * 1 HTTP Requestで多数の監視実行結果を送信できるため、リソース観点から効率的に処理できる。
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public void forwardWinEventResult(List<WinEventResultDTO> resultList, AgentInfo agentInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		for (String facilityId : getFacilityId(agentInfo)) {
			new MonitorWinEventControllerBean().run(facilityId, resultList);
		}
	}

	/**
	 * [Update] 新モジュールを取得
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	@XmlMimeType("application/octet-stream")
	public DataHandler downloadModule(String filename) throws InvalidUserPass, InvalidRole, HinemosUnknown
	{
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		String filepath= "/opt/hinemos/lib/agent/" + filename;
		File file = new File(filepath);
		if(!file.exists()) {
			m_log.info("file not found : " + filepath);
			return null;
		}
		FileDataSource source = new FileDataSource(file);
		DataHandler dataHandler = new DataHandler(source);
		return dataHandler;
	}

	/**
	 * [Update] モジュール一覧を取得
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<String> getAgentLibMap() throws HinemosUnknown, InvalidRole, InvalidUserPass {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// TODO HashMapが利用できないのでArrayList<String>で実装。
		// あとで調査すること。
		HashMap<String, String> map = new RepositoryControllerBean().getAgentLibMap();
		ArrayList<String> ret = new ArrayList<String>();
		for (String str : map.keySet()) {
			ret.add(str);
			ret.add(map.get(str));
		}
		return ret;
	}

	/**
	 * [Update] ファイル名とMD5の組をマネージャに登録しておく。
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * TODO
	 * ArrayList<String>ではなく、HashMap<String, String>にしたい。
	 * 要調査。
	 *
	 * @param filenameMd5
	 * @param agentInto
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public void setAgentLibMd5(ArrayList<String> filenameMd5, AgentInfo agentInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		HashMap<String, String> map = new HashMap<String, String>();
		Iterator<String> itr = filenameMd5.iterator();
		while(itr.hasNext()) {
			map.put(itr.next(), itr.next());
		}

		ArrayList<String> facilityIdList = getFacilityId(agentInfo);
		for (String facilityId : facilityIdList) {
			m_log.debug("setAgentLibMd5() : facilityId=" + facilityId);
			AgentConnectUtil.setAngetLibMd5(facilityId, map);
		}
	}
}
