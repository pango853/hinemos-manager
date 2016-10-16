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

package com.clustercontrol.jobmanagement.util;

import java.text.DateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.jobmanagement.model.JobParamInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.bean.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.RepositoryUtil;

/**
 * ジョブ変数ユーティリティクラス<BR>
 *
 * @version 3.0.0
 * @since 2.1.0
 */
public class ParameterUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( ParameterUtil.class );


	public static final int TYPE_JOB = 1;
	public static final int TYPE_MONITOR = 2;
	public static final int TYPE_NODE = 3;


	/**
	 * ログ出力情報からパラメータIDに対応する値を取得します。
	 * ジョブセッション作成時に、ジョブ変数に対応する値を取得するために使用します。
	 *
	 * @param paramId パラメータID
	 * @param info ログ出力情報
	 * @return 値
	 */
	public static String getParameterValue(String paramId, OutputBasicInfo info) {
		String value = null;

		if(info == null) {
			return value;
		}

		if(paramId.equals(SystemParameterConstant.FACILITY_ID)){
			//ファシリティID
			value = info.getFacilityId();
		} else if(paramId.equals(SystemParameterConstant.PLUGIN_ID)){
			//プラグインID
			value = info.getPluginId();
		} else if(paramId.equals(SystemParameterConstant.MONITOR_ID)){
			//監視項目ID
			value = info.getMonitorId();
		} else if(paramId.equals(SystemParameterConstant.MESSAGE_ID)){
			//メッセージID
			value = info.getMessageId();
		} else if(paramId.equals(SystemParameterConstant.APPLICATION)){
			//アプリケーション
			value = info.getApplication();
		} else if(paramId.equals(SystemParameterConstant.PRIORITY)){
			//重要度
			value = String.valueOf(info.getPriority());
		} else if(paramId.equals(SystemParameterConstant.MESSAGE)){
			//メッセージ
			value = info.getMessage();
		} else if(paramId.equals(SystemParameterConstant.ORG_MESSAGE)){
			//オリジナルメッセージ
			value = info.getMessageOrg();
		}

		return value;
	}

	/**
	 * ジョブセッションからジョブ変数情報の一覧を取得します。
	 *
	 * @param sessionId ジョブセッションID
	 * @return 連想配列（ジョブ変数名, 値）
	 * @throws JobInfoNotFound
	 * @throws HinemosUnknown
	 */
	private static HashMap<String, String> getSessionParameters(String sessionId) throws JobInfoNotFound, HinemosUnknown {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		/** Local Variables */
		HashMap<String, String> parameters = new HashMap<String, String>();
		Collection<JobParamInfoEntity> collection = null;
		Iterator<JobParamInfoEntity> itr = null;
		JobParamInfoEntity param = null;

		/** Main */
		m_log.debug("getting parameters of job session... (session_id = " + sessionId + ")" );
		try {
			collection = em.createNamedQuery("JobParamInfoEntity.findBySessionId", JobParamInfoEntity.class)
					.setParameter("sessionId", sessionId)
					.getResultList();
			if (collection == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobParamInfoEntity.findBySessionId"
						+ ", sessionId = " + sessionId);
				m_log.info("getSessionParameters() : " + je.getClass().getSimpleName() + ", " + je.getMessage());
				throw je;
			}
			if(collection != null && collection.size() > 0){
				itr = collection.iterator();
				while(itr.hasNext()){
					param = itr.next();
					if (! parameters.containsKey(param.getId().getParamId())) {
						m_log.debug("added parameter. (session_id = " + sessionId + ", param_id = " + param.getId().getParamId() + ", value = " + param.getValue() + ")");
						parameters.put(param.getId().getParamId(), param.getValue());
					} else {
						m_log.debug("duplicated, skipped paramter. (session_id = " + sessionId + ", param_id = " + param.getId().getParamId() + ", value = " + param.getValue() + ")");
					}
				}
			}
		} catch (JobInfoNotFound e) {
			m_log.info("getSessionParameters() failed to get parameters of job session... (session_id = " + sessionId + ") : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			e.setSessionId(sessionId);
			throw e;
		} catch (Exception e) {
			m_log.warn("getSessionParameters() failed to get parameters of job session... (session_id = " + sessionId + ")", e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		m_log.debug("getSessionParameters() successful in get parameters of job session... (session_id = " + sessionId + ")" );
		return parameters;
	}

	/**
	 *
	 * ジョブセッション情報からパラメータIDに対応する値を取得します。
	 * ジョブセッション作成時に、ジョブ変数に対応する値を取得するために使用します。
	 *
	 * @param sessionId 情報を取得するジョブセッションを特定するためのセッションID
	 * @param paramId 取得する情報を識別するID
	 * @return
	 * @throws JobInfoNotFound
	 */
	public static String getJobParameterValue(String sessionId, String paramId) throws JobInfoNotFound{

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		String ret = null;

		if(paramId.equals(SystemParameterConstant.START_DATE)){

			//セッション開始日時
			JobSessionEntity session = em.find(JobSessionEntity.class, sessionId, ObjectPrivilegeMode.READ);
			if (session == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobSessionEntity.findByPrimaryKey"
						+ ", sessionId = " + sessionId);
				m_log.info("getJobParameterValue() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				je.setSessionId(sessionId);
				throw je;
			}
			ret = DateFormat.getDateTimeInstance().format(session.getScheduleDate());

		} else if (paramId.equals(SystemParameterConstant.SESSION_ID)){
			ret = sessionId;

		} else if (paramId.equals(SystemParameterConstant.TRIGGER_TYPE)){
			JobSessionEntity session = em.find(JobSessionEntity.class, sessionId, ObjectPrivilegeMode.READ);
			if (session == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobSessionEntity.findByPrimaryKey"
						+ ", sessionId = " + sessionId);
				m_log.info("getJobParameterValue() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				je.setSessionId(sessionId);
				throw je;
			}
			ret = JobTriggerTypeConstant.typeToString(session.getTriggerType());

		} else if (paramId.equals(SystemParameterConstant.TRIGGER_INFO)){
			JobSessionEntity session = em.find(JobSessionEntity.class, sessionId, ObjectPrivilegeMode.READ);
			if (session == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobSessionEntity.findByPrimaryKey"
						+ ", sessionId = " + sessionId);
				m_log.info("getJobParameterValue() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				je.setSessionId(sessionId);
				throw je;
			}
			ret = session.getTriggerInfo();
		}

		return ret;
	}


	/**
	 * ジョブセッションからパラメータIDに対応する値を取得します。
	 * ジョブセッション作成後（ジョブ変数を含む）に、変数に格納されている値を取り出すために使用します。
	 *
	 * @param sessionId セッションID
	 * @param paramId パラメータID
	 * @return 値
	 * @throws JobInfoNotFound
	 */
	public static String getSessionParameterValue(String sessionId, String paramId) throws JobInfoNotFound {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		String value = null;

		Collection<JobParamInfoEntity> collection = null;
		collection = em.createNamedQuery("JobParamInfoEntity.findBySessionIdAndParamId" , JobParamInfoEntity.class)
				.setParameter("sessionId", sessionId)
				.setParameter("paramId", paramId)
				.getResultList();

		if(collection != null) {
			for (JobParamInfoEntity param : collection) {
				value = param.getValue();
				break;
			}
		}

		return value;
	}

	/**
	 * 引数で指定された文字列からパラメータIDを取得し、<BR>
	 * セッションからパラメータIDに対応する値を取得します。<BR>
	 * 引数で指定された文字列のパラメータID部分を取得した値で置き換えます。
	 *
	 * @param sessionId セッションID
	 * @param source 置き換え対象文字列
	 * @return 置き換え後の文字列
	 * @throws JobInfoNotFound
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound 
	 */
	public static String replaceSessionParameterValue(String sessionId, String facilityId, String source) throws JobInfoNotFound, HinemosUnknown, FacilityNotFound {
		// Local Variables
		HashMap<String, String> parameters = new HashMap<String, String>();
		String commandOrig = source;
		String commandConv = source;

		// Main
		m_log.debug("generating command string... (session_id = " + sessionId + ", facility_id = " + facilityId + ", command_orig = " + commandOrig + ")");

		if (commandOrig == null) {
			m_log.info("registed command is invalid. (session_id = " + sessionId + ", facility_id = " + facilityId + ", command_orig = " + commandOrig + ")");
		}
		
		// ノードプロパティを取得
		NodeInfo nodeInfo = new RepositoryControllerBean().getNode(facilityId);
		Map<String, String> nodeMap = RepositoryUtil.createNodeParameter(nodeInfo);

		parameters = getSessionParameters(sessionId);

		for (String parameter : parameters.keySet()) {
			if (parameters.get(parameter) != null) {
				// システム変数(ジョブ)とユーザ変数
				m_log.debug("replace parameter. (session_id = " + sessionId + ", facility_id = " + facilityId + ", command_orig = " + commandOrig + ", parameter = " + parameter + ", value = " + parameters.get(parameter) + ")");
				commandConv = commandConv.replace(SystemParameterConstant.getParamText(parameter), parameters.get(parameter));
			} else {
				if (nodeMap.get(parameter) != null) {
					// システム変数(ノード)
					m_log.debug("replace parameter. (session_id = " + sessionId + ", facility_id = " + facilityId + ", command_orig = " + commandOrig + ", parameter = " + parameter + ", value = " + nodeMap.get(parameter) + ")");
					commandConv = commandConv.replace(SystemParameterConstant.getParamText(parameter), nodeMap.get(parameter));
				} else {
					// どれにも該当しない
					m_log.debug("skip replace parameter (undef). (session_id = " + sessionId + ", facility_id = " + facilityId + ", command_orig = " + commandOrig + ", parameter = " + parameter + ")");
				}
			}
		}

		m_log.debug("successful in generating command string... (session_id = " + sessionId + ", facility_id = " + facilityId + ", command_orig = " + commandOrig + ", command_conv = " + commandConv + ")");
		return commandConv;
	}


	/**
	 * 変数#[RETURN:jobId:facilityId]を置換する。
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @param source
	 * @return
	 */
	public static String replaceReturnCodeParameter(String sessionId, String jobunitId, String source) {
		String regex = "#\\[RETURN:([^:]*):([^:]*)\\]";
		Pattern pattern = Pattern.compile(regex);
		String ret = source;
		for (int i = 0; i < 100; i ++) { //無限ループにならないように、上限を定める。
			Matcher matcher = pattern.matcher(ret);
			if (matcher.find()) {
				String rJobId = matcher.group(1);
				String rFacilityId = matcher.group(2);
				try {
					JobSessionNodeEntity node = QueryUtil.getJobSessionNodePK(sessionId, jobunitId, rJobId, rFacilityId);
					Integer endValue = node.getEndValue();
					if (endValue != null) {
						ret = ret.replaceFirst(regex, endValue.toString());
					} else {
						// 先行ジョブが終了していない。
						ret = ret.replaceFirst(regex, "null");
					}
				} catch (JobInfoNotFound e) {
					m_log.warn("replaceReturnCodeParameter : jobId=" + rJobId +
							", facilityId=" + rFacilityId);
					// ジョブ、ファシリティIDが存在しない。
					ret = ret.replaceFirst(regex, "null");
				}
				/*
				 * for test
				 * 単体試験(このクラスをmainで実行)する際に利用
				 */
				// ret = ret.replaceFirst(regex, "12345");
			} else {
				break;
			}
		}
		return ret;
	}

	/**
	 *
	 *
	 *
	 * @param paramId ジョブ変数の種類を示す文字列
	 * @return
	 */
	public static int checkFunctionType(String paramId) {
		int ret = 0;

		// ジョブ関連
		if (paramId.equals(SystemParameterConstant.START_DATE)
				|| paramId.equals(SystemParameterConstant.SESSION_ID)
				|| paramId.equals(SystemParameterConstant.TRIGGER_TYPE)
				|| paramId.equals(SystemParameterConstant.TRIGGER_INFO)
				|| paramId.equals(SystemParameterConstant.FILENAME)
			|| paramId.equals(SystemParameterConstant.DIRECTORY)){

			ret = TYPE_JOB;

			// 監視関連
		} else if (paramId.equals(SystemParameterConstant.FACILITY_ID)
				|| 	paramId.equals(SystemParameterConstant.PLUGIN_ID)
				|| paramId.equals(SystemParameterConstant.MONITOR_ID)
				|| paramId.equals(SystemParameterConstant.MESSAGE_ID)
				|| paramId.equals(SystemParameterConstant.APPLICATION)
				|| paramId.equals(SystemParameterConstant.PRIORITY)
				|| paramId.equals(SystemParameterConstant.MESSAGE)
				|| paramId.equals(SystemParameterConstant.ORG_MESSAGE)){

			ret = TYPE_MONITOR;
			// ノード関連
		} else if (paramId.equals(SystemParameterConstant.FACILITY_NAME)
				|| paramId.equals(SystemParameterConstant.IP_ADDRESS)
				|| paramId.equals(SystemParameterConstant.IP_ADDRESS_VERSION)
				|| paramId.equals(SystemParameterConstant.IP_ADDRESS_V4)
				|| paramId.equals(SystemParameterConstant.IP_ADDRESS_V6)
				|| paramId.equals(SystemParameterConstant.NODE_NAME)
				|| paramId.equals(SystemParameterConstant.OS_NAME)
				|| paramId.equals(SystemParameterConstant.OS_RELEASE)
				|| paramId.equals(SystemParameterConstant.OS_VERSION)
				|| paramId.equals(SystemParameterConstant.CHARSET)
				|| paramId.equals(SystemParameterConstant.AGENT_AWAKE_PORT)
				|| paramId.equals(SystemParameterConstant.JOB_PRIORITY)
				|| paramId.equals(SystemParameterConstant.JOB_MULTIPLICITY)
				|| paramId.equals(SystemParameterConstant.SNMP_PORT)
				|| paramId.equals(SystemParameterConstant.SNMP_COMMUNITY)
				|| paramId.equals(SystemParameterConstant.SNMP_VERSION)
				|| paramId.equals(SystemParameterConstant.SNMP_TIMEOUT)
				|| paramId.equals(SystemParameterConstant.SNMP_TRIES)
				|| paramId.equals(SystemParameterConstant.WBEM_PORT)
				|| paramId.equals(SystemParameterConstant.WBEM_PROTOCOL)
				|| paramId.equals(SystemParameterConstant.WBEM_TIMEOUT)
				|| paramId.equals(SystemParameterConstant.WBEM_TRIES)
				|| paramId.equals(SystemParameterConstant.WBEM_USER)
				|| paramId.equals(SystemParameterConstant.WBEM_PASSWORD)
				|| paramId.equals(SystemParameterConstant.WINRM_USER)
				|| paramId.equals(SystemParameterConstant.WINRM_PASSWORD)
				|| paramId.equals(SystemParameterConstant.WINRM_VERSION)
				|| paramId.equals(SystemParameterConstant.WINRM_PORT)
				|| paramId.equals(SystemParameterConstant.WINRM_PROTOCOL)
				|| paramId.equals(SystemParameterConstant.WINRM_TIMEOUT)
				|| paramId.equals(SystemParameterConstant.WINRM_TRIES)
				|| paramId.equals(SystemParameterConstant.IPMI_IP_ADDRESS)
				|| paramId.equals(SystemParameterConstant.IPMI_PORT)
				|| paramId.equals(SystemParameterConstant.IPMI_TIMEOUT)
				|| paramId.equals(SystemParameterConstant.IPMI_TRIES)
				|| paramId.equals(SystemParameterConstant.IPMI_PROTOCOL)
				|| paramId.equals(SystemParameterConstant.IPMI_LEVEL)
				|| paramId.equals(SystemParameterConstant.IPMI_USER)
				|| paramId.equals(SystemParameterConstant.IPMI_PASSWORD)
				|| paramId.equals(SystemParameterConstant.SSH_USER)
				|| paramId.equals(SystemParameterConstant.SSH_USER_PASSWORD)
				|| paramId.equals(SystemParameterConstant.SSH_PRIVATE_KEY_FILENAME)
				|| paramId.equals(SystemParameterConstant.SSH_PRIVATE_KEY_PASSPHRASE)
				|| paramId.equals(SystemParameterConstant.SSH_PORT)
				|| paramId.equals(SystemParameterConstant.SSH_TIMEOUT)
				|| paramId.equals(SystemParameterConstant.CLOUD_SERVICE)
				|| paramId.equals(SystemParameterConstant.CLOUD_SCOPE)
				|| paramId.equals(SystemParameterConstant.CLOUD_RESOURCE_TYPE)
				|| paramId.equals(SystemParameterConstant.CLOUD_RESOURCE_ID)
				|| paramId.equals(SystemParameterConstant.CLOUD_RESOURCE_NAME)
				|| paramId.equals(SystemParameterConstant.CLOUD_LOCATION)) {

			ret = TYPE_NODE;
	}
		return ret;
	}

	public static void main(String args[]) {
		String source = "";
		// source = "ls #[RETURN:jobId1:facilityId1]a";
		source = "ls #[RETURN:jobId1:facilityId1]a -l#[RETURN:jobId2:facilityId2]a";
		System.out.println("source=" + source);
		System.out.println("replace=" + replaceReturnCodeParameter(null, null, source));
	}
}
