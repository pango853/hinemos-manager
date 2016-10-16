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

package com.clustercontrol.winservice.factory;

import intel.management.wsman.WsmanException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.bean.MonitorJudgementInfo;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorTruthValueType;
import com.clustercontrol.repository.bean.NodeInfo;
import com.clustercontrol.winservice.model.MonitorWinserviceInfoEntity;
import com.clustercontrol.winservice.util.RequestWinRM;
import com.clustercontrol.winservice.util.QueryUtil;

/**
 * Windowsサービス監視クラス
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
public class RunMonitorWinService extends RunMonitorTruthValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorWinService.class );

	private static final String MESSAGE_ID_INFO = "001";
	private static final String MESSAGE_ID_WARNING = "002";
	private static final String MESSAGE_ID_CRITICAL = "003";
	private static final String MESSAGE_ID_UNKNOWN = "004";

	/** Windowsサービス監視情報 */
	private MonitorWinserviceInfoEntity m_winService = null;

	/** Windowsサービス名 */
	private String m_serviceName;

	private RequestWinRM m_request ;

	/** メッセージ */
	private String m_message = null;

	/** オリジナルメッセージ */
	private String m_messageOrg = null;

	/**
	 * コンストラクタ
	 * 
	 */
	public RunMonitorWinService() {
		super();
	}

	/**
	 * 
	 */
	@Override
	public boolean collect(String facilityId){

		m_value = false;

		try{
			// ノードの属性取得
			NodeInfo info = nodeInfo.get(facilityId);
			if(info == null){
				m_log.info("collect() targe NodeInfo is Null facilityId = " + facilityId);
				return false;
			}

			String ipAddress = info.getAvailableIpAddress();

			String user = info.getWinrmUser();
			String userPassword = info.getWinrmUserPassword();
			int port = info.getWinrmPort();
			String protocol = info.getWinrmProtocol();
			int timeout = info.getWinrmTimeout();
			int retries = info.getWinrmRetries();

			m_request = new RequestWinRM(m_serviceName);
			m_value = m_request.polling(
					ipAddress,
					user,
					userPassword,
					port,
					protocol,
					timeout, retries);

			m_message = m_request.getMessage();
			m_messageOrg = m_request.getMessageOrg();
			m_nodeDate = m_request.getDate();

			return true;
		} catch (WsmanException | HinemosUnknown e) {
			// 不明
			m_message = "unknown error . facilityId = " + m_facilityId;
			m_messageOrg = "unknown error . facilityId = " + m_facilityId + ". " + e.getMessage();
			m_nodeDate = System.currentTimeMillis();
			m_log.warn("collect() facilityId = " + facilityId + ", " +
					e.getMessage() + ", class=" + e.getClass().getName());
			return false;
		} catch (Exception e) {
			// 不明
			m_message = "unknown error . facilityId = " + m_facilityId;
			m_messageOrg = "unknown error . facilityId = " + m_facilityId + ". " + e.getMessage();
			m_nodeDate = System.currentTimeMillis();

			m_log.warn("collect() facilityId = " + facilityId, e);

			return false;
		}
	}


	/**
	 * Windowsサービス監視情報を設定
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {
		// Windowsサービス監視情報を取得
		m_winService = QueryUtil.getMonitorWinserviceInfoPK(m_monitorId);

		// Windowsサービス監視情報を設定
		if(m_winService.getServiceName() != null){
			m_serviceName = m_winService.getServiceName();
		}
	}

	@Override
	protected RunMonitor createMonitorInstance() {
		return new RunMonitorWinService();
	}

	@Override
	public String getMessageOrg(int key) {
		return m_messageOrg;
	}

	@Override
	public String getMessage(int key) {
		return m_message;
	}

	@Override
	public String getMessageId(int id) {

		MonitorJudgementInfo info = m_judgementInfoList.get(id);
		if(info != null){
			int priority = info.getPriority();
			if(priority == PriorityConstant.TYPE_INFO){
				return MESSAGE_ID_INFO;
			}
			else if(priority == PriorityConstant.TYPE_WARNING){
				return MESSAGE_ID_WARNING;
			}
			else if(priority == PriorityConstant.TYPE_CRITICAL){
				return MESSAGE_ID_CRITICAL;
			}
		}
		return MESSAGE_ID_UNKNOWN;
	}
}
