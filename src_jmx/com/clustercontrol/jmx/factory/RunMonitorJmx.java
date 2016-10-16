/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jmx.factory;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jmx.model.MonitorJmxInfoEntity;
import com.clustercontrol.jmx.util.QueryUtil;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType;
import com.clustercontrol.repository.bean.NodeInfo;
import com.clustercontrol.util.Messages;

/**
 * JMX 監視 数値監視を実行するクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class RunMonitorJmx extends RunMonitorNumericValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorJmx.class );

	private static final String NaN = "NaN";

	private static final String MESSAGE_ID_INFO = "001";
	private static final String MESSAGE_ID_WARNING = "002";
	private static final String MESSAGE_ID_CRITICAL = "003";
	private static final String MESSAGE_ID_UNKNOWN = "004";

	/** JMX 監視情報 */
	private MonitorJmxInfoEntity jmx = null;

	/**例外 */
	private Exception exception;

	/**
	 * コンストラクタ
	 * 
	 */
	public RunMonitorJmx() {
		super();
	}

	/**
	 * マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッド
	 * 
	 */
	@Override
	protected RunMonitor createMonitorInstance() {
		return new RunMonitorJmx();
	}


	/**
	 * JMX 経由で値を取得
	 * 
	 * @param facilityId ファシリティID
	 * @return 値取得に成功した場合、true
	 */
	@Override
	public boolean collect(String facilityId) {
		boolean result = false;

		if (m_now != null){
			m_nodeDate = m_now.getTime();
		}
		m_value = 0;
		exception = null;

		NodeInfo node = nodeInfo.get(facilityId);
		JMXServiceURL url = null;
		try {
			String rmiFormat = HinemosPropertyUtil.getHinemosPropertyStr("monitor.jmx.rmi.format", "service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi");
			String urlStr = String.format(rmiFormat, node.getAvailableIpAddress(), jmx.getPort());
			m_log.debug("facilityId=" + facilityId + ", url=" + urlStr);
			url = new JMXServiceURL(urlStr);
		}catch (Exception e) {
			m_log.warn("fail to initialize JMXServiceURL : " + e.getMessage() + " (" + e.getClass().getName() + ")", e);
			exception = e;
			return result;
		}

		JMXConnector jmxc = null;
		try {
			Map<String, Object> env = new HashMap<>();

			if (jmx.getAuthUser() != null)
				env.put(JMXConnector.CREDENTIALS, new String[]{jmx.getAuthUser(), jmx.getAuthPassword()});

			System.setProperty("sun.rmi.transport.tcp.responseTimeout",
					Integer.toString(HinemosPropertyUtil.getHinemosPropertyNum("system.sun.rmi.transport.tcp.responseTimeout", 10 * 1000)));
			jmxc = JMXConnectorFactory.connect(url, env);
			MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

			Object value = mbsc.getAttribute(new ObjectName(jmx.getJmxTypeMstEntity().getObjectName()), jmx.getJmxTypeMstEntity().getAttributeName());
			m_value = Double.parseDouble(searchTargetValue(value, Arrays.asList(KeyParser.parseKeys(jmx.getJmxTypeMstEntity().getKeys()))).toString());

			m_log.debug(jmx.getJmxTypeMstEntity().getName() + " : " + m_value + " " + jmx.getJmxTypeMstEntity().getMeasure());

			result = true;
		}catch (Exception e) {
			m_log.warn("fail to access JMXService : " + e.getMessage() + " (" + e.getClass().getName() + ")");
			exception = e;
		}finally{
			try {
				if (jmxc != null) {
					jmxc.close();
				}
			}catch(IOException e){
				m_log.info("fail to close JMXService : " + e.getMessage() + " (" + e.getClass().getName() + ")");
				exception = e;
			}
		}

		return result;
	}

	private SimpleType<?>[] validTypes = {
		SimpleType.BYTE,
		SimpleType.SHORT,
		SimpleType.INTEGER,
		SimpleType.LONG,
		SimpleType.FLOAT,
		SimpleType.DOUBLE
	};

	private Object searchTargetValue(Object value, List<Object> keys) throws Exception {
		if (value instanceof CompositeData) {
			if (keys.isEmpty())
				throw new Exception("not found value according to keys.");
			return searchTargetValue(((CompositeData)value).get(keys.get(0).toString()), keys.subList(1, keys.size()));
		}
		else if (value instanceof TabularData) {
			if (keys.isEmpty())
				throw new Exception("not found value according to keys.");
			return searchTargetValue(((TabularData)value).get((Object[])keys.get(0)), keys.subList(1, keys.size()));
		}
		else {
			for (SimpleType<?> t: validTypes) {
				if (t.isValue(value))
					return value;
			}
			throw new Exception("value type id invalid. " + value.getClass());
		}
	}

	@Override
	protected void setCheckInfo() throws MonitorNotFound {
		if (jmx == null)
			// JMX 監視情報を取得
			jmx = QueryUtil.getMonitorJmxInfoPK(m_monitorId);
	}

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

	/**
	 * メッセージを取得します。
	 */
	@Override
	public String getMessage(int result) {
		String message;
		if(exception == null){
			if(Double.isNaN(m_value)){
				message = NaN;
			}
			else {
				message = jmx.getJmxTypeMstEntity().getName() + " : " + NumberFormat.getNumberInstance().format(m_value);
			}
		}
		else {
			message = jmx.getJmxTypeMstEntity().getName() + " : " + Messages.getString("message.jmx.cannot.get.value");
		}
		return message;
	}

	/**
	 * オリジナルメッセージを取得します。
	 */
	@Override
	public String getMessageOrg(int result) {
		String message;
		if(exception == null){
			if(Double.isNaN(m_value)){
				message = NaN;
			}
			else {
				message = jmx.getJmxTypeMstEntity().getName() + " : " + NumberFormat.getNumberInstance().format(m_value);
			}
		}
		else {
			message = jmx.getJmxTypeMstEntity().getName() + " : " + exception.getMessage();
		}
		return message;
	}
}