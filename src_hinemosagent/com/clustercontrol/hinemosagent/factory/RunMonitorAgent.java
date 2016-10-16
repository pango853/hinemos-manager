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

package com.clustercontrol.hinemosagent.factory;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.monitor.run.bean.MonitorJudgementInfo;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorTruthValueType;
import com.clustercontrol.util.Messages;

/**
 * Hinemos Agent監視を実行するクラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class RunMonitorAgent extends RunMonitorTruthValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorAgent.class );

	private static final String MESSAGE_ID_INFO = "001";
	private static final String MESSAGE_ID_WARNING = "002";
	private static final String MESSAGE_ID_CRITICAL = "003";
	private static final String MESSAGE_ID_UNKNOWN = "004";

	/** メッセージ */
	private String m_message = null;

	/**
	 * コンストラクタ
	 * 
	 */
	public RunMonitorAgent() {
		super();
	}

	/**
	 * マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッド
	 * 
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#runMonitorInfo()
	 * @see com.clustercontrol.monitor.run.util.CallableTask
	 */
	@Override
	protected RunMonitor createMonitorInstance() {
		return new RunMonitorAgent();
	}

	/**
	 * Hinemos エージェントをチェック
	 * 
	 * @param facilityId ファシリティID
	 * @return 値取得に成功した場合、true
	 */
	@Override
	public boolean collect(String facilityId) {
		boolean duplication = false;

		// 監視開始時刻を設定
		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}

		//値を初期化
		m_message = "";

		m_value = AgentConnectUtil.isValidAgent(facilityId);
		m_log.debug("checkAgent facilityId=" + facilityId + ", " + m_value);

		// TODO 同一のfacilityIdのノードチェック機構
		if(!duplication){
			if(m_value){
				//OK
				m_message = Messages.getString("message.agent.1");
			} else {
				//NG
				m_message = Messages.getString("message.agent.2");
			}
		} else {
			//同一ファシリティのAgentの重複
			String[] args = {facilityId};
			m_message = Messages.getString("message.agent.3", args);
		}

		return true;
	}

	/* (non-Javadoc)
	 * Hinemos Agent監視情報を設定
	 * @see com.clustercontrol.monitor.run.factory.OperationNumericValueInfo#setMonitorAdditionInfo()
	 */
	@Override
	protected void setCheckInfo() {
	}

	/* (non-Javadoc)
	 * ノード用メッセージIDを取得
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessageId(int)
	 */
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

	/* (non-Javadoc)
	 * ノード用メッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessage(int)
	 */
	@Override
	public String getMessage(int id) {
		return m_message;
	}

	/* (non-Javadoc)
	 * ノード用オリジナルメッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessageOrg(int)
	 */
	@Override
	public String getMessageOrg(int id) {
		return null;
	}
}
