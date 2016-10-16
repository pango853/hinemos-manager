/*
Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.
 */
package com.clustercontrol.ws.jobmanagement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobInvalid;
import com.clustercontrol.fault.JobKickDuplicate;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.JobSessionDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.OtherUserGetLock;
import com.clustercontrol.fault.UpdateTimeNotLatest;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.jobmanagement.bean.JobFileCheck;
import com.clustercontrol.jobmanagement.bean.JobForwardFile;
import com.clustercontrol.jobmanagement.bean.JobHistoryFilter;
import com.clustercontrol.jobmanagement.bean.JobHistoryList;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.bean.JobKick;
import com.clustercontrol.jobmanagement.bean.JobNodeDetail;
import com.clustercontrol.jobmanagement.bean.JobOperationInfo;
import com.clustercontrol.jobmanagement.bean.JobPlan;
import com.clustercontrol.jobmanagement.bean.JobPlanFilter;
import com.clustercontrol.jobmanagement.bean.JobSchedule;
import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * ジョブ操作用のWebAPIエンドポイント
 */
@javax.jws.WebService(targetNamespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( JobEndpoint.class );
	private static Log m_opelog = LogFactory.getLog("HinemosOperation");

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
	 * ジョブツリー情報を取得する。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param ownerRoleId
	 * @param treeOnly
	 * @throws UserNotFound
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public JobTreeItem getJobTree(String ownerRoleId, boolean treeOnly) throws NotifyNotFound, HinemosUnknown, JobMasterNotFound, UserNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("getJobTree : treeOnly=" + treeOnly);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", TreeOnly=");
		msg.append(treeOnly);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobTree, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getJobTree(ownerRoleId, treeOnly, Locale.getDefault());
	}

	/**
	 * ジョブ情報の詳細を取得する。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param jobInfo ジョブ情報(ツリー情報のみ)
	 * @return ジョブ情報(Full)
	 * @throws JobMasterNotFound
	 * @throws UserNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public JobInfo getJobFull(JobInfo jobInfo) throws JobMasterNotFound, UserNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobFull : jobInfo=" + jobInfo);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		if(jobInfo != null){
			StringBuffer msg = new StringBuffer();
			msg.append(", JobID=");
			msg.append(jobInfo.getId());
			m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobFull, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
		}

		return new JobControllerBean().getJobFull(jobInfo);
	}

	/**
	 * ジョブ情報の詳細を取得する。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param jobInfo ジョブ情報(ツリー情報のみ)
	 * @return ジョブ情報(Full)
	 * @throws UserNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public List<JobInfo> getJobFullList(List<JobInfo> jobList) throws UserNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		
		String idStr = "";
		for (JobInfo info : jobList) {
			if (idStr.length() > 0) {
				idStr += ", ";
			}
			idStr += info.getId();
		}
		
		m_log.debug("getJobFullList : id=" + idStr);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobID=");
		msg.append(idStr);
			m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobFull, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
		
		return new JobControllerBean().getJobFullList(jobList);
	}
	
	/**
	 * ジョブユニット情報を登録する。<BR>
	 *
	 * JobManagementAdd権限とJobManagementWrite権限が必要
	 *
	 * @param item ジョブユニット情報{@link com.clustercontrol.jobmanagement.bean.JobTreeItem}の階層オブジェクト
	 * @throws JobInvalid
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJob#registerJob(JobTreeItem, String)
	 */
	public Long registerJobunit(JobTreeItem item) throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting {
		long start = System.currentTimeMillis();
		String id = null;
		if (item != null && item.getData() != null) {
			id = item.getData().getId();
		}
		m_log.debug("registerJobunit : Id=" + id + ", item="+ item);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.ADD));
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);


		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if (item != null && item.getData() != null) {
			msg.append(", JobunitID=");
			msg.append(item.getData().getId());
		}
		
		Long lastUpdateTime = null;
		
		try {
			lastUpdateTime = new JobControllerBean().registerJobunit(item);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Set Jobunit Failed, Method=registerJobunit, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Set Jobunit, Method=registerJobunit, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		m_log.debug(String.format("registerJobunit: %d ms", System.currentTimeMillis() - start));
		
		return lastUpdateTime;
	}

	/**
	 * ジョブユニット単位でジョブツリー情報を削除する。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @param jobunitId 削除対象ジョブユニットのジョブID
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 */
	public void deleteJobunit(String jobunitId) throws HinemosUnknown, JobMasterNotFound, InvalidUserPass, InvalidRole, InvalidSetting, JobInvalid {
		m_log.debug("deleteJobunit : Id=" + jobunitId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobunitID=");
		msg.append(jobunitId);

		try {
			new JobControllerBean().deleteJobunit(jobunitId);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Delete Jobunit Failed, Method=deleteJobunit, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Delete Jobunit, Method=deleteJobunit, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}


	/**
	 * ジョブ操作開始用プロパティを返します。<BR>
	 *
	 * JobManagementExecute権限が必要
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param facilityId ファシリティID
	 * @param locale ロケール情報
	 * @return ジョブ操作開始用プロパティ
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.jobmanagement.factory.JobOperationProperty#getStartProperty(String, String, String, String, Locale)
	 */
	public ArrayList<String> getAvailableStartOperation(String sessionId, String jobunitId, String jobId, String facilityId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getAvailableStartOperation : sessionId=" + sessionId +
				", jobunitId=" + jobunitId + ", jobId=" + jobId + ", facilityId=" + facilityId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.EXEC));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SessionID=");
		msg.append(sessionId);
		msg.append(", JobunitID=");
		msg.append(jobunitId);
		msg.append(", JobID=");
		msg.append(jobId);
		msg.append(", FacilityID=");
		msg.append(facilityId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getAvailableStartOperation, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getAvailableStartOperation(sessionId, jobunitId, jobId, facilityId, Locale.getDefault());
	}

	/**
	 * ジョブ操作停止用プロパティを返します。<BR>
	 *
	 * JobManagementExecute権限が必要
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param facilityId ファシリティID
	 * @param locale ロケール情報
	 * @return ジョブ操作停止用プロパティ
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.jobmanagement.factory.JobOperationProperty#getStopProperty(String, String, String, String, Locale)
	 */
	public ArrayList<String> getAvailableStopOperation(String sessionId, String jobunitId,  String jobId, String facilityId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getAvailableStopOperation : sessionId=" + sessionId +
				", jobunitId=" + jobunitId + ", jobId=" + jobId + ", facilityId=" + facilityId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.EXEC));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SessionID=");
		msg.append(sessionId);
		msg.append(", JobunitID=");
		msg.append(jobunitId);
		msg.append(", JobID=");
		msg.append(jobId);
		msg.append(", FacilityID=");
		msg.append(facilityId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getAvailableStopOperation, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getAvailableStopOperation(sessionId, jobunitId, jobId, facilityId, Locale.getDefault());
	}

	/**
	 * ジョブを実行します。<BR>
	 *
	 * JobManagementExecute権限が必要
	 *
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param info ログ出力情報
	 * @param triggerInfo 実行契機情報
	 * @throws JobMasterNotFound
	 * @throws JobInfoNotFound
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * @see com.clustercontrol.jobmanagement.ejb.session.JobControllerBean#createJobInfo(String, String, NotifyRequestMessage, JobTriggerInfo}
	 * @see com.clustercontrol.jobmanagement.session.JobRunManagementBean#runJob(String, String)
	 */
	public String runJob(String jobunitId, String jobId, OutputBasicInfo info, JobTriggerInfo triggerInfo)
			throws  FacilityNotFound, HinemosUnknown, JobInfoNotFound, JobMasterNotFound, InvalidUserPass, InvalidRole, JobSessionDuplicate, InvalidSetting
	{
		m_log.debug("runJob : jobunitId=" + jobunitId + ", jobId=" + jobId + ", info=" + info +
				", triggerInfo=" + triggerInfo);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.EXEC));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// テスト実行時のシステム権限チェック
		if (triggerInfo.getJobWaitTime() || triggerInfo.getJobWaitMinute() || triggerInfo.getJobCommand()) {
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
			HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		}

		String ret = null;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobID=");
		msg.append(jobId);
		msg.append(", Trigger=");
		msg.append(triggerInfo==null?null:triggerTypeToString(triggerInfo.getTrigger_type()));

		try {
			ret = new JobControllerBean().runJob(jobunitId, jobId, info, triggerInfo);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Run Job Failed, Method=runJob, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Run Job, Method=runJob, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * ジョブ操作を行います。<BR>
	 *
	 * JobManagementExecute権限が必要
	 *
	 * @param property ジョブ操作用プロパティ
	 * @throws JobInfoNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @see com.clustercontrol.jobmanagement.session.JobRunManagementBean#operationJob(Property)
	 */
	public void operationJob(JobOperationInfo property) throws HinemosUnknown, JobInfoNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("operationJob : nodeOperationInfo=" + property);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.EXEC));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SessionID=");
		if(property != null){
			msg.append(property.getSessionId());
			msg.append(", JobID=");
			msg.append(property.getJobId());
			msg.append(", FacilityID=");
			msg.append(property.getFacilityId()==null?"(not set)":property.getFacilityId());
			msg.append(", Operation=");
			msg.append(getOperationString(property.getControl()));
			msg.append(", EndStatus=");
			msg.append(property.getEndStatus()==null?"(not set)":property.getEndStatus());
			msg.append(", EndValue=");
			msg.append(property.getEndValue()==null?"(not set)":property.getEndValue());
		}

		try {
			new JobControllerBean().operationJob(property);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Operate Job Failed, Method=operationJob, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Operate Job, Method=operationJob, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}


	/**
	 * ジョブ履歴一覧情報を返します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param property 履歴フィルタ用プロパティ
	 * @param histories 表示履歴数
	 * @return ジョブ履歴一覧情報（Objectの2次元配列）
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getHistoryList(Property, int)
	 */
	public JobHistoryList getJobHistoryList(JobHistoryFilter property, int histories) throws JobInfoNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getHistoryList : jobHistoryFilter=" + property + ", histories=" + histories);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(property != null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			msg.append(", StartFromDate=");
			msg.append(property.getStartFromDate()==null?null:sdf.format(new Date(property.getStartFromDate())));
			msg.append(", StartToDate=");
			msg.append(property.getStartToDate()==null?null:sdf.format(new Date(property.getStartToDate())));
			msg.append(", EndFromDate=");
			msg.append(property.getEndFromDate()==null?null:sdf.format(new Date(property.getEndFromDate())));
			msg.append(", EndToDate=");
			msg.append(property.getEndToDate()==null?null:sdf.format(new Date(property.getEndToDate())));
			msg.append(", JobID=");
			msg.append(property.getJobId());
			msg.append(", Status=");
			msg.append(property.getStatus());
			msg.append(", TriggerType=");
			msg.append(property.getTriggerType());
			msg.append(", TriggerInfo=");
			msg.append(property.getTriggerInfo());
			msg.append(", OwnerRoleId=");
			msg.append(property.getOwnerRoleId());
		}
		msg.append(", Count=");
		msg.append(histories);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobHistoryList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getJobHistoryList(property, histories);
	}

	/**
	 * ジョブ詳細一覧情報を返します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param sessionId セッションID
	 * @return ジョブ詳細一覧情報（Objectの2次元配列）
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getDetailList(String)
	 */
	public JobTreeItem getJobDetailList(String sessionId) throws JobInfoNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SessionID=");
		msg.append(sessionId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobDetailList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getJobDetailList(sessionId);
	}

	/**
	 * ノード詳細一覧情報を返します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param locale ロケール情報
	 * @return ノード詳細一覧情報（Objectの2次元配列）
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws JobInfoNotFound
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getNodeDetailList(String, String, String, Locale)
	 */
	public ArrayList<JobNodeDetail> getNodeDetailList(String sessionId, String jobunitId, String jobId) throws InvalidUserPass, InvalidRole, HinemosUnknown, JobInfoNotFound {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SessionID=");
		msg.append(sessionId);
		msg.append(", JobunitID=");
		msg.append(jobunitId);
		msg.append(", JobID=");
		msg.append(jobId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getNodeDetailList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getNodeDetailList(sessionId, jobunitId, jobId, Locale.getDefault());
	}

	/**
	 * ファイル転送一覧情報を返します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @return ファイル転送一覧情報（Objectの2次元配列）
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getForwardFileList(String, String)
	 */
	public ArrayList<JobForwardFile> getForwardFileList(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SessionID=");
		msg.append(sessionId);
		msg.append(", JobunitID=");
		msg.append(jobunitId);
		msg.append(", JobID=");
		msg.append(jobId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getForwardFileList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getForwardFileList(sessionId, jobunitId, jobId);
	}

	/**
	 * スケジュール情報を登録します。<BR>
	 *
	 * JobManagementAdd権限が必要
	 *
	 * @param info スケジュール情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 *
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#addSchedule(JobSchedule, String)
	 */
	public void addSchedule(JobSchedule info) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobKickDuplicate {
		m_log.debug("addSchedule : jobSchedule=" + info);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", ScheduleID=");
			msg.append(info.getId());
		}

		try {
			new JobControllerBean().addSchedule(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Add Schedule Failed, Method=addSchedule, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Add Schedule, Method=addSchedule, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	/**
	 * ジョブ[実行契機]ファイルチェック情報を登録します。<BR>
	 *
	 * JobManagementAdd権限が必要
	 *
	 * @param info ジョブ[実行契機]ファイルチェック情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 *
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#addSchedule(JobFileCheck, String)
	 */
	public void addFileCheck(JobFileCheck info) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobKickDuplicate {
		m_log.debug("addFileCheck : jobFileCheck=" + info);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", ScheduleID=");
			msg.append(info.getId());
		}

		try {
			new JobControllerBean().addFileCheck(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Add FileCheck Failed, Method=addFileCheck, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Add FileCheck, Method=addFileCheck, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}


	/**
	 * スケジュール情報を変更します。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @param info スケジュール情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#modifySchedule(JobSchedule, String)
	 */
	public void modifySchedule(JobSchedule info) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobInfoNotFound {
		m_log.debug("modifySchedule : jobSchedule=" + info);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", ScheduleID=");
			msg.append(info.getId());
		}

		try {
			new JobControllerBean().modifySchedule(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Change Schedule Failed, Method=modifySchedule, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Change Schedule, Method=modifySchedule, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	/**
	 * ジョブ[実行契機]ファイルチェック情報を変更します。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @param info ジョブ[実行契機]ファイルチェック情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#modifySchedule(JobSchedule, String)
	 */
	public void modifyFileCheck(JobFileCheck info) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobInfoNotFound {
		m_log.debug("modifyFileCheck : jobSchedule=" + info);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", ScheduleID=");
			msg.append(info.getId());
		}

		try {
			new JobControllerBean().modifyFileCheck(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Change FileCheck Failed, Method=modifyFileCheck, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Change FileCheck, Method=modifyFileCheck, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	/**
	 * ジョブ[実行契機]スケジュール情報を削除します。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @param scheduleIdList スケジュールIDリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#deleteSchedule(String)
	 */
	public void deleteSchedule(List<String> scheduleIdList) throws HinemosUnknown, InvalidUserPass, InvalidRole, JobInfoNotFound {
		m_log.debug("deleteSchedule : scheduleId=" + scheduleIdList);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ScheduleIDList=");
		msg.append(scheduleIdList);

		try {
			new JobControllerBean().deleteSchedule(scheduleIdList);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Delete Schedule Failed, Method=deleteSchedule, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Delete Schedule, Method=deleteSchedule, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * ジョブ[実行契機]ファイルチェック情報を削除します。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @param scheduleIdList スケジュールIDリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#deleteSchedule(String)
	 */
	public void deleteFileCheck(List<String> scheduleIdList) throws HinemosUnknown, InvalidUserPass, InvalidRole, JobInfoNotFound {
		m_log.debug("deleteFileCheck : scheduleId=" + scheduleIdList);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ScheduleID=");
		msg.append(scheduleIdList);

		try {
			new JobControllerBean().deleteFileCheck(scheduleIdList);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Delete FileCheck Failed, Method=deleteFileCheck, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Delete FileCheck, Method=deleteFileCheck, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	/**
	 * 実行契機IDと一致するジョブスケジュールを返します。<BR>
	 * @param jobKickId
	 * @return
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public JobSchedule getJobSchedule(String jobKickId) throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobSchedule :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobSchedule, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JobControllerBean().getJobSchedule(jobKickId);
	}
	/**
	 * 実行契機IDと一致するジョブファイルチェックを返します。<BR>
	 * @param jobKickId
	 * @return
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public JobFileCheck getJobFileCheck(String jobKickId) throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobFileCheck :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobFileCheck, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JobControllerBean().getJobFileCheck(jobKickId);
	}

	/**ジョブ[実行契機]スケジュール情報の有効/無効を変更します。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @param scheduleId スケジュールID
	 * @param validFlag 有効/無効
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	public void setJobKickStatus(String scheduleId, boolean validFlag) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobMasterNotFound, JobInfoNotFound {
		m_log.debug("setJobKickStatus : scheduleId=" + scheduleId + ", validFlag=" + validFlag);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ScheduleID=");
		msg.append(scheduleId);
		msg.append(", ValidFlag=");
		msg.append(validFlag);

		try {
			new JobControllerBean().setJobKickStatus(scheduleId, validFlag);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Change Valid Failed, Method=setJobKickStatus, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Change Valid, Method=setJobKickStatus, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

	}
	/**
	 * ジョブ[実行契機]スケジュール&ファイルチェック一覧情報を返します。<BR>
	 * スケジュール一覧情報を返します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @return スケジュール一覧情報（Objectの2次元配列）
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @see com.clustercontrol.jobmanagement.factory.SelectJobKick#getJobKickList()
	 */
	public ArrayList<JobKick> getJobKickList() throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getScheduleList :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getScheduleList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JobControllerBean().getJobKickList();
	}

	/**
	 * セッションジョブ情報を返します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @return ジョブツリー情報{@link com.clustercontrol.jobmanagement.bean.JobTreeItem}
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws JobInfoNotFound
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getSessionJobInfo(String, String, String)
	 */
	public JobTreeItem getSessionJobInfo(String sessionId, String jobunitId, String jobId) throws InvalidUserPass, InvalidRole, HinemosUnknown, JobInfoNotFound {
		m_log.debug("getSessionJobInfo : sessionId=" + sessionId + ", jobunitId=" + jobunitId +
				", jobId=" + jobId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SessionID=");
		msg.append(sessionId);
		msg.append(", JobunitID=");
		msg.append(jobunitId);
		msg.append(", JobID=");
		msg.append(jobId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getSessionJobInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getSessionJobInfo(sessionId, jobunitId, jobId);
	}

	/**
	 * ジョブ[スケジュール予定]の一覧を返します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @return ジョブ[スケジュール予定]の一覧情報
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @see com.clustercontrol.jobmanagement.factory.SelectJobKick#getPlanList()
	 */
	public ArrayList<JobPlan> getPlanList(JobPlanFilter property, int plans) throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getPlanList :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(property != null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			msg.append(", fromDate=");
			msg.append(property.getFromDate()==null?null:sdf.format(new Date(property.getFromDate())));
			msg.append(", toDate=");
			msg.append(property.getToDate()==null?null:sdf.format(new Date(property.getToDate())));
			msg.append(", jobKickID=");
			msg.append(property.getJobKickId()==null?null:property.getJobKickId());
		}
		msg.append(", Count=");
		msg.append(plans);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobPlanList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getPlanList(property, plans);
	}

	/**
	 * 実行契機の文字列取得
	 *
	 * @param type 実行契機
	 * @return 実行契機文字列
	 */
	private String triggerTypeToString(Integer type){
		if (type != null) {
			if (type == JobTriggerTypeConstant.TYPE_SCHEDULE) {
				return "Schedule";
			} else if (type == JobTriggerTypeConstant.TYPE_MANUAL) {
				return "Manual";
			} else if (type == JobTriggerTypeConstant.TYPE_MONITOR) {
				return "Monitor";
			}
		}
		return "";
	}

	/**
	 * ログ出力用の操作名取得
	 *
	 * @param op 操作名(画面表示)
	 * @return ログ出力用操作名
	 */
	private static String getOperationString(Integer op){
		if (op ==null) {
			return "[Unknown Operation]";
		}

		switch (op.intValue()) {
		case OperationConstant.TYPE_START_AT_ONCE://1
			return "Start[Start]";

		case OperationConstant.TYPE_START_SUSPEND://3
			return "Start[Cancel Suspend]";

		case OperationConstant.TYPE_START_SKIP://5
			return "Start[Cancel Skip]";

		case OperationConstant.TYPE_START_WAIT://7
			return "Start[Cancel Pause]";

		case OperationConstant.TYPE_STOP_AT_ONCE://0
			return "Stop[Command]";

		case OperationConstant.TYPE_STOP_SUSPEND://2
			return "Stop[Suspend]";

		case OperationConstant.TYPE_STOP_SKIP://4
			return "Stop[Skip]";

		case OperationConstant.TYPE_STOP_WAIT://6
			return "Stop[Pause]";

		case OperationConstant.TYPE_STOP_MAINTENANCE://8
			return "Stop[Change End Value]";

		case OperationConstant.TYPE_STOP_SET_END_VALUE://10
			return "Stop[Set End Value]";

		case OperationConstant.TYPE_STOP_FORCE://11
			return "Stop[Force]";

		default:
			return "[Unknown Operation]";
		}
	}

	/**
	 * 指定したジョブユニットの最終更新日時を返す
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param jobunitId ジョブユニットID
	 * @return Long 最終更新日時
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public List<Long> getUpdateTimeList(List<String> jobunitIdList) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobunitUpdateTime :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		String jobunitIdStr = "";
		for (String str : jobunitIdList) {
			if (jobunitIdStr.length() > 0) {
				jobunitIdStr += ", ";
			}
			jobunitIdStr += str;
		}
		
		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobunitUpdateTime, jobunitID="
				+ jobunitIdStr + ", User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JobControllerBean().getUpdateTime(jobunitIdList);

	}

	/**
	 * 編集ロックを取得する
	 *
	 * JobManagementAdd権限とJobManagementRead権限とJobManagementWrite権限が必要
	 *
	 * @param jobunitId ジョブユニットID
	 * @param updateTime 最終更新日時
	 * @param forceFlag 強制的に編集ロックを取得するか
	 *
	 * @return セッション
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws UpdateTimeNotLatest
	 * @throws OtherUserGetLock
	 * @throws JobInvalid
	 * @throws JobMasterNotFound
	 */
	public Integer getEditLock(String jobunitId, Long updateTime, boolean forceFlag) throws HinemosUnknown, InvalidUserPass, InvalidRole, OtherUserGetLock, UpdateTimeNotLatest, JobMasterNotFound, JobInvalid {
		m_log.debug("getEditLock :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.ADD));
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobunitID=");
		msg.append(jobunitId);
		msg.append(", UpdateTime=");
		msg.append(updateTime);
		msg.append(", ForceFlag=");
		msg.append(forceFlag);

		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get EditLock, Method=getEditLock, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		String userIpAddr = HttpAuthenticator.getUserAccountString(wsctx);
		// ユーザ名@[IPアドレス]の形式から、ユーザ名とIPアドレスを抜き出す
		String user = userIpAddr.substring(0, userIpAddr.indexOf("@"));
		String ipAddr = userIpAddr.substring(userIpAddr.indexOf("[") + 1, userIpAddr.indexOf("]"));

		return new JobControllerBean().getEditLock(jobunitId, updateTime, forceFlag, user, ipAddr);
	}

	/**
	 * 編集ロックの正当性をチェックする
	 *
	 * JobManagementAdd権限とJobManagementRead権限とJobManagementWrite権限が必要
	 *
	 * @param jobunitId ジョブユニットID
	 * @param editSession セッション
	 *
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws UpdateTimeNotLatest
	 * @throws OtherUserGetLock
	 * @throws JobInvalid
	 * @throws JobMasterNotFound
	 */
	public void checkEditLock(String jobunitId, Integer editSession) throws HinemosUnknown, InvalidUserPass, InvalidRole, OtherUserGetLock, UpdateTimeNotLatest, JobMasterNotFound, JobInvalid {
		m_log.debug("checkEditLock :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.ADD));
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobunitID=");
		msg.append(jobunitId);
		msg.append(", EditSession=");
		msg.append(editSession);

		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Check EditLock, Method=checkEditLock, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		new JobControllerBean().checkEditLock(jobunitId, editSession);
	}

	/**
	 * 編集ロックを開放する。
	 *
	 * @param editSession セッション
	 *
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public void releaseEditLock(Integer editSession) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("releaseEditLock : editSession="+editSession);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.ADD));
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", EditSession=");
		msg.append(editSession);

		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Release EditLock, Method=releaseEditLock, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		String userIpAddr = HttpAuthenticator.getUserAccountString(wsctx);
		// ユーザ名@[IPアドレス]の形式から、ユーザ名とIPアドレスを抜き出す
		String user = userIpAddr.substring(0, userIpAddr.indexOf("@"));
		String ipAddr = userIpAddr.substring(userIpAddr.indexOf("[") + 1, userIpAddr.indexOf("]"));
		
		new JobControllerBean().releaseEditLock(editSession, user, ipAddr);
	}
}
