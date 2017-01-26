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

package com.clustercontrol.http.factory;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.http.model.MonitorHttpInfoEntity;
import com.clustercontrol.http.util.GetHttpResponse;
import com.clustercontrol.http.util.QueryUtil;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorStringValueType;
import com.clustercontrol.repository.util.RepositoryUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.StringBinder;

/**
 * HTTP監視 文字列監視を実行するクラス<BR>
 *
 * @version 5.0.0
 * @since 2.1.0
 */
public class RunMonitorHttpString extends RunMonitorStringValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorHttpString.class );

	private static final String MESSAGE_ID_UNKNOWN = "100";

	/** HTTP監視情報 */
	private MonitorHttpInfoEntity m_http = null;

	/** URL */
	private String m_requestUrl = null;

	/** タイムアウト */
	private int m_httpTimeout;

	/** 不明メッセージ */
	private String m_unKnownMessage = null;

	/** オリジナルメッセージ */
	private String m_messageOrg = null;

	/**
	 * コンストラクタ
	 * 
	 */
	public RunMonitorHttpString() {
		super();
	}

	/**
	 * マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッド
	 * 
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#runMonitorInfo()
	 * @see com.clustercontrol.monitor.run.util.CallableTask
	 */
	@Override
	public RunMonitor createMonitorInstance() {
		return new RunMonitorHttpString();
	}

	/**
	 * HTTP数を取得
	 * 
	 * @param facilityId ファシリティID
	 * @return 値取得に成功した場合、true
	 */
	@Override
	public boolean collect(String facilityId) {

		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}
		m_value = null;
		m_unKnownMessage = "";
		m_messageOrg = "";

		String url = m_requestUrl;
		// 変数を置換したURLの生成
		if (nodeInfo != null && nodeInfo.containsKey(facilityId)) {
			Map<String, String> nodeParameter = RepositoryUtil.createNodeParameter(nodeInfo.get(facilityId));
			StringBinder strbinder = new StringBinder(nodeParameter);
			url = strbinder.bindParam(m_requestUrl);
			if (m_log.isTraceEnabled()) m_log.trace("http request. (nodeInfo = " + nodeInfo + ", facilityId = " + facilityId + ", url = " + url + ")");
		}

		boolean result = false;
		try (GetHttpResponse m_request = GetHttpResponse.custom()
				.setConnectTimeout(m_httpTimeout)
				.setRequestTimeout(m_httpTimeout)
				.setNeedAuthSSLCert(! HinemosPropertyUtil.getHinemosPropertyBool("monitor.http.ssl.trustall", true))
				.build()) {
			result = m_request.execute(url);
			if(result &&
					(m_request.getErrorMessage() == null || m_request.getErrorMessage().equals(""))){
				m_value = m_request.getResponseBody();
				if (m_value == null) { // 404などの場合はbodyがnullになってしまう。
					m_value = "";
				}

				StringBuffer response = new StringBuffer();
				response.append(Messages.getString("request.url") + " : " + url);
				response.append("\n" + Messages.getString("status.code") + " : " + m_request.getStatusCode());
				if(m_request.getHeaderString() != null && !"".equals(m_request.getHeaderString().trim())){
					response.append("\n" + Messages.getString("header") + " :\n" + m_request.getHeaderString().trim());
				}
				if(m_request.getResponseBody() != null && !"".equals(m_request.getResponseBody().trim())){
					response.append("\n" + Messages.getString("response.body") + " :\n" + m_request.getResponseBody().trim());
				}
				m_messageOrg = response.toString();
			}
			else{
				m_unKnownMessage = Messages.getString("message.http.3");

				StringBuffer response = new StringBuffer();
				response.append(m_request.getErrorMessage());
				response.append("\n");
				response.append("\n" + Messages.getString("request.url") + " : " + url);
				response.append("\n" + Messages.getString("status.code") + " :\n" + m_request.getStatusCode());
				if(m_request.getHeaderString() != null && !"".equals(m_request.getHeaderString().trim())){
					response.append("\n" + Messages.getString("header") + " :\n" + m_request.getHeaderString().trim());
				}
				m_messageOrg = response.toString();
			}
		}
		catch (IOException e) {
			m_log.warn("fail to close HttpClient : " + e.getMessage(), e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * HTTP監視情報を設定
	 * @see com.clustercontrol.monitor.run.factory.OperationNumericValueInfo#setMonitorAdditionInfo()
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {
		if(m_http == null){
			// HTTP監視情報を取得
			m_http = QueryUtil.getMonitorHttpInfoPK(m_monitorId);
			// HTTP監視情報を設定
			m_requestUrl = m_http.getRequestUrl().trim();
			m_httpTimeout = m_http.getTimeout().intValue();
		}
	}

	/* (非 Javadoc)
	 * ノード用メッセージIDを取得
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#getMessageId(int)
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
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#getMessage(int)
	 */
	@Override
	public String getMessage(int id) {

		String message = super.getMessage(id);
		if(message == null || "".equals(message)){
			return m_unKnownMessage;
		}
		return message;
	}

	/* (非 Javadoc)
	 * ノード用オリジナルメッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#getMessageOrg(int)
	 */
	@Override
	public String getMessageOrg(int id) {
		return m_messageOrg;
	}
}