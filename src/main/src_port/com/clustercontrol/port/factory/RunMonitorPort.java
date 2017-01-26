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

package com.clustercontrol.port.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType;
import com.clustercontrol.port.bean.PortRunCountConstant;
import com.clustercontrol.port.bean.PortRunIntervalConstant;
import com.clustercontrol.port.model.MonitorPortInfoEntity;
import com.clustercontrol.port.model.MonitorProtocolMstEntity;
import com.clustercontrol.port.protocol.ReachAddressProtocol;
import com.clustercontrol.port.util.QueryUtil;
import com.clustercontrol.repository.bean.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 * port監視クラス
 * 
 * @version 4.0.0
 * @since 2.4.0
 */
public class RunMonitorPort extends RunMonitorNumericValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorPort.class );

	private static final String MESSAGE_ID_INFO = "001"; // 通知

	private static final String MESSAGE_ID_WARNING = "002"; // 警告

	private static final String MESSAGE_ID_CRITICAL = "003"; // 危険

	private static final String MESSAGE_ID_UNKNOWN = "004"; // 不明

	/** port監視情報 */
	private MonitorPortInfoEntity m_port = null;

	/** ポート番号 */
	private int m_portNo;

	/** 試行回数 */
	private int m_runCount = PortRunCountConstant.TYPE_COUNT_01;

	/** 試行間隔（ミリ秒） */
	private int m_runInterval = PortRunIntervalConstant.TYPE_SEC_01;

	/** タイムアウト（ミリ秒） */
	private int m_portTimeout;

	/** サービスID */
	private String m_serviceId;

	/** サービスプロトコル情報 */
	private MonitorProtocolMstEntity m_protocol = null;

	/** メッセージ */
	private String m_message = null;

	/** オリジナルメッセージ */
	private String m_messageOrg = null;

	/** 応答時間（ミリ秒） */
	private long m_response = 0;

	// port実行
	private ReachAddressProtocol m_reachability = null;

	/**
	 * 
	 * コンストラクタ
	 * 
	 */
	public RunMonitorPort() {
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
		return new RunMonitorPort();
	}

	/**
	 * port数を取得
	 * 
	 * @param facilityId
	 *            ファシリティID
	 * @return 値取得に成功した場合、true
	 * @throws FacilityNotFound
	 */
	@Override
	public boolean collect(String facilityId) throws FacilityNotFound, HinemosUnknown {

		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}
		m_message = "";
		m_messageOrg = "";
		m_response = 0;

		if (m_reachability == null) {
			try {
				// m_serviceIdを基にDBよりクラス名を得る
				m_protocol = QueryUtil.getMonitorProtocolMstPK(m_serviceId);

				String protocolClassName = "";
				// クラス名の取得
				if (m_protocol.getClassName() != null)
					protocolClassName = m_protocol.getClassName();

				// そのクラスのインスタンスを生成する
				Class<?> cls = Class.forName(protocolClassName);
				m_reachability = (ReachAddressProtocol) cls.newInstance();
				m_reachability.setPortNo(m_portNo);
				m_reachability.setSentCount(m_runCount);
				m_reachability.setSentInterval(m_runInterval);
				m_reachability.setTimeout(m_portTimeout);
			} catch (MonitorNotFound e) {
			} catch (java.lang.ClassNotFoundException e) {
				m_log.info("collect() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
			} catch (java.lang.InstantiationException e) {
				m_log.info("collect() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
			} catch (IllegalAccessException e) {
				m_log.info("collect() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
			}
		}

		// ノードの属性取得
		NodeInfo info = new RepositoryControllerBean().getNode(facilityId);

		String ipNetworkNumber = info.getAvailableIpAddress();
		
		String nodeName = info.getNodeName();

		boolean result = m_reachability.isReachable(ipNetworkNumber, nodeName);
		m_message = m_reachability.getMessage();
		m_messageOrg = m_reachability.getMessageOrg();
		if (result) {
			m_response = m_reachability.getResponse();
			m_value = m_response;
		}

		return result;
	}

	/*
	 * (非 Javadoc) port監視情報を設定
	 * 
	 * @see com.clustercontrol.monitor.run.factory.OperationNumericValueInfo#setMonitorAdditionInfo()
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {

		// port監視情報を取得
		m_port = QueryUtil.getMonitorPortInfoPK(m_monitorId);

		// port監視情報を設定
		if (m_port.getPortNumber() != null)
			m_portNo = m_port.getPortNumber().intValue();
		if (m_port.getRunCount() != null)
			m_runCount = m_port.getRunCount().intValue();
		if (m_port.getRunInterval() != null)
			m_runInterval = m_port.getRunInterval().intValue();
		if (m_port.getTimeout() != null)
			m_portTimeout = m_port.getTimeout().intValue();
		if (m_port.getMonitorProtocolMstEntity() != null
				&& m_port.getMonitorProtocolMstEntity().getServiceId() != null)
			m_serviceId = m_port.getMonitorProtocolMstEntity().getServiceId();
	}

	/*
	 * (非 Javadoc) 判定結果を取得
	 * 
	 * @see com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType#getCheckResult(boolean)
	 */
	@Override
	public int getCheckResult(boolean ret) {
		return super.getCheckResult(ret);
	}

	/*
	 * (非 Javadoc) ノード用メッセージIDを取得
	 * 
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessageId(int)
	 */
	@Override
	public String getMessageId(int id) {

		if (id == PriorityConstant.TYPE_INFO) {
			return MESSAGE_ID_INFO;
		} else if (id == PriorityConstant.TYPE_WARNING) {
			return MESSAGE_ID_WARNING;
		} else if (id == PriorityConstant.TYPE_CRITICAL) {
			return MESSAGE_ID_CRITICAL;
		} else {
			return MESSAGE_ID_UNKNOWN;
		}
	}

	/*
	 * (非 Javadoc) ノード用メッセージを取得
	 * 
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessage(int)
	 */
	@Override
	public String getMessage(int id) {
		return m_message;
	}

	/*
	 * (非 Javadoc) ノード用オリジナルメッセージを取得
	 * 
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessageOrg(int)
	 */
	@Override
	public String getMessageOrg(int id) {
		return m_messageOrg;
	}

	/*
	 * (非 Javadoc) スコープ用メッセージIDを取得
	 * 
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#getMessageIdForScope(int)
	 */
	@Override
	protected String getMessageIdForScope(int priority) {

		if (priority == PriorityConstant.TYPE_INFO) {
			return MESSAGE_ID_INFO;
		} else if (priority == PriorityConstant.TYPE_WARNING) {
			return MESSAGE_ID_WARNING;
		} else if (priority == PriorityConstant.TYPE_CRITICAL) {
			return MESSAGE_ID_CRITICAL;
		} else {
			return MESSAGE_ID_UNKNOWN;
		}
	}

	/**
	 * トランザクションを開始し、引数で指定された監視情報の監視を実行します。
	 * 
	 * @param monitorTypeId
	 *            監視対象ID
	 * @param monitorId
	 *            監視項目ID
	 * @throws MonitorNotFound
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * @see #runMonitorInfo()
	 */
	@Override
	public void run(String monitorTypeId, String monitorId) throws MonitorNotFound, FacilityNotFound, InvalidRole, HinemosUnknown {

		super.run(monitorTypeId, monitorId);
	}
}