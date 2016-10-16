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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Date;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.snmp4j.smi.SMIConstants;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType;
import com.clustercontrol.repository.bean.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.snmp.bean.ConvertValueConstant;
import com.clustercontrol.snmp.model.MonitorSnmpInfoEntity;
import com.clustercontrol.snmp.model.MonitorSnmpValueEntity;
import com.clustercontrol.snmp.model.MonitorSnmpValueEntityPK;
import com.clustercontrol.snmp.util.QueryUtil;
import com.clustercontrol.snmp.util.RequestSnmp4j;
import com.clustercontrol.snmp.util.SnmpProperties;
import com.clustercontrol.util.Messages;

/**
 * SNMP監視 数値監視を実行するファクトリークラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class RunMonitorSnmp extends RunMonitorNumericValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorSnmp.class );

	private static final String MESSAGE_ID_INFO = "001";
	private static final String MESSAGE_ID_WARNING = "002";
	private static final String MESSAGE_ID_CRITICAL = "003";
	private static final String MESSAGE_ID_UNKNOWN = "004";

	/** SNMP監視情報 */
	private MonitorSnmpInfoEntity m_snmp = null;

	/** OID */
	private String m_snmpOid = null;

	/** 取得値の加工 */
	private int m_convertFlg = 0;

	/** オリジナルメッセージ */
	private String m_messageOrg = null;

	/** メッセージ */
	private String m_message = "";

	/**
	 * コンストラクタ
	 * 
	 */
	public RunMonitorSnmp() {
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
		return new RunMonitorSnmp();
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

		JpaTransactionManager jtm = new JpaTransactionManager();

		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}
		m_value = 0;

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

			// 今回の取得値
			double value = -1;
			try {
				if (m_request.getValue() == null) {
					m_log.warn("collect() : m_request.getValue() is null");
					return false;
				}
				value = Double.parseDouble(m_request.getValue());
			} catch (Exception e) {
				m_log.warn("collect() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				String[] args = { m_request.getValue() };
				m_message = Messages.getString("message.snmp.8", args);
				return false;
			}
			long date = m_request.getDate();

			// 加工しない場合
			if(m_convertFlg == ConvertValueConstant.TYPE_NO){
				m_value = value;
				m_nodeDate = date;
			}
			// 差分をとる場合
			else if(m_convertFlg == ConvertValueConstant.TYPE_DELTA){

				// 前回値を取得
				MonitorSnmpValueEntity valueEntity = null;
				MonitorSnmpValueEntityPK valueEntityPk = null;
				double prevValue = 0;
				long prevDate = 0;

				valueEntityPk = new MonitorSnmpValueEntityPK(m_monitorId, facilityId);
				try {
					valueEntity = QueryUtil.getMonitorSnmpValuePK(valueEntityPk);
				} catch (MonitorNotFound e) {
				}
				if (valueEntity == null) {
					// 初回だった場合
					try {
						// インスタンス生成
						valueEntity = new MonitorSnmpValueEntity(valueEntityPk, null);
						// 重複チェック
						jtm.checkEntityExists(MonitorSnmpValueEntity.class, valueEntity.getId());
					} catch (EntityExistsException e) {
						m_log.info("run() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
						m_message = Messages.getString("message.snmp.7");
						m_messageOrg = m_messageOrg + " (" + e.getMessage() + ")";
						return false;
					}

				}
				// 前回の取得値
				prevValue = valueEntity.getValue();
				if (valueEntity.getGetDate() != null) {
					prevDate = valueEntity.getGetDate().getTime();
				}
				
				if (prevValue > value) {
					if (m_request.getType() == SMIConstants.SYNTAX_COUNTER32) {
						value += ((double)Integer.MAX_VALUE + 1) * 2;
					} else if (m_request.getType() == SMIConstants.SYNTAX_COUNTER64) {
						value += ((double)Long.MAX_VALUE + 1) * 2;
					}
				}

				// SNMP前回値情報を今回の取得値に更新
				valueEntity.setValue(Double.valueOf(value));
				valueEntity.setGetDate(new Timestamp(date));

				// 前回値取得時刻がSNMP取得許容時間よりも前だった場合、値取得失敗
				int tolerance = (m_runInterval + SnmpProperties.getProperties().getValidSecond()) * 1000;

				if(prevDate > date - tolerance){
					m_value = value - prevValue;
					m_nodeDate = m_request.getDate();
				}
				else{
					String[] args = { DateFormat.getDateTimeInstance().format(new Date(prevDate))};
					m_message = Messages.getString("message.snmp.9",args);
					return false;
				}
			}
			m_message = Messages.getString("select.value") + " : " + m_value;
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
		m_convertFlg = m_snmp.getConvertFlg().intValue();
	}

	/* (非 Javadoc)
	 * ノード用メッセージIDを取得
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessageId(int)
	 */
	@Override
	public String getMessageId(int id) {

		if(id == PriorityConstant.TYPE_INFO){
			return MESSAGE_ID_INFO;
		}
		else if(id == PriorityConstant.TYPE_WARNING){
			return MESSAGE_ID_WARNING;
		}
		else if(id == PriorityConstant.TYPE_CRITICAL){
			return MESSAGE_ID_CRITICAL;
		}
		else{
			return MESSAGE_ID_UNKNOWN;
		}
	}

	/* (非 Javadoc)
	 * ノード用メッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessage(int)
	 */
	@Override
	public String getMessage(int id) {
		return m_message;
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
