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

package com.clustercontrol.snmp.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorStringValueType;
import com.clustercontrol.repository.bean.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.snmp.model.MonitorSnmpInfoEntity;
import com.clustercontrol.snmp.util.QueryUtil;
import com.clustercontrol.snmp.util.RequestSnmp4j;
import com.clustercontrol.util.Messages;

/**
 * SNMP監視 文字列監視を実行するファクトリークラス<BR>
 *
 * @version 4.0.0
 * @since 2.1.0
 */
public class RunMonitorSnmpString extends RunMonitorStringValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorSnmpString.class );

	private static final String MESSAGE_ID_INFO = "001";
	private static final String MESSAGE_ID_WARNING = "002";
	private static final String MESSAGE_ID_CRITICAL = "003";
	private static final String MESSAGE_ID_UNKNOWN = "004";

	/** SNMP監視情報 */
	private MonitorSnmpInfoEntity m_snmp = null;

	/** OID */
	private String m_snmpOid = null;

	/** オリジナルメッセージ */
	private String m_messageOrg = null;

	/** メッセージ */
	private String m_message = "";

	/**
	 * コンストラクタ
	 * 
	 */
	public RunMonitorSnmpString() {
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
		return new RunMonitorSnmpString();
	}

	/**
	 * OID値を取得
	 * 
	 * @param facilityId ファシリティID
	 * @return 値取得に成功した場合、true
	 * @throws HinemosUnknown
	 */
	@Override
	public boolean collect(String facilityId) throws HinemosUnknown {

		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}
		m_value = null;

		// メッセージを設定
		m_message = "";
		m_messageOrg = Messages.getString("oid") + " : " + m_snmpOid;

		NodeInfo info = null;
		try {
			// ノードの属性取得
			info = new RepositoryControllerBean().getNode(facilityId);
		}
		catch(FacilityNotFound e){
			m_message = Messages.getString("message.snmp.6");
			m_messageOrg = m_messageOrg + " (" + e.getMessage() + ")";
			return false;
		}

		// SNMP値取得
		RequestSnmp4j m_request = new RequestSnmp4j();

		m_log.debug("version=" + info.getSnmpVersion());
		boolean result = false;
		try {
			result = m_request.polling(
					info.getAvailableIpAddress(),
					info.getSnmpCommunity(),
					info.getSnmpPort(),
					m_snmpOid,
					SnmpVersionConstant.stringToSnmpType(info.getSnmpVersion()),
					info.getSnmpTimeout(),
					info.getSnmpRetryCount(),
					info.getSnmpSecurityLevel(),
					info.getSnmpUser(),
					info.getSnmpAuthPassword(),
					info.getSnmpPrivPassword(),
					info.getSnmpAuthProtocol(),
					info.getSnmpPrivProtocol()
					);
		} catch (Exception e) {
			m_message = Messages.getString("message.snmp.6");
			m_messageOrg = m_message + ", " + e.getMessage() + " (" + e.getClass().getName() + ")";
			m_log.warn(m_messageOrg, e);
			return false;
		}

		if(result){

			m_value = m_request.getValue();
			m_nodeDate = m_request.getDate();

			m_messageOrg = m_messageOrg + ", " + Messages.getString("select.value") + " : " + m_value;
		}
		else{
			m_message = m_request.getMessage();
		}
		return result;
	}

	/* (非 Javadoc)
	 * SNMP監視情報を設定
	 * @see com.clustercontrol.monitor.run.factory.OperationNumericValueInfo#setMonitorAdditionInfo()
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {

		// SNMP監視情報を取得
		m_snmp = QueryUtil.getMonitorSnmpInfoPK(m_monitorId);

		// SNMP監視情報を設定
		m_snmpOid = m_snmp.getSnmpOid().trim();
	}

	/* (非 Javadoc)
	 * ノード用メッセージIDを取得
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessageId()
	 */
	@Override
	public String getMessageId(int id) {

		String messageId = super.getMessageId(id);
		if(messageId == null || "".equals(messageId)){
			return MESSAGE_ID_UNKNOWN;
		}
		return messageId;
	}

	/* (非 Javadoc)
	 * ノード用メッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessage(int)
	 */
	@Override
	public String getMessage(int id) {

		String message = super.getMessage(id);
		if(message == null || "".equals(message)){
			return m_message;
		}
		return message;
	}

	/* (非 Javadoc)
	 * ノード用オリジナルメッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessageOrg(int)
	 */
	@Override
	public String getMessageOrg(int id) {
		return m_messageOrg;
	}

	/* (非 Javadoc)
	 * スコープ用メッセージIDを取得
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#getMessageIdForScope(int)
	 */
	@Override
	protected String getMessageIdForScope(int priority) {

		if(priority == PriorityConstant.TYPE_INFO){
			return MESSAGE_ID_INFO;
		}
		else if(priority == PriorityConstant.TYPE_WARNING){
			return MESSAGE_ID_WARNING;
		}
		else if(priority == PriorityConstant.TYPE_CRITICAL){
			return MESSAGE_ID_CRITICAL;
		}
		else{
			return MESSAGE_ID_UNKNOWN;
		}
	}
}
