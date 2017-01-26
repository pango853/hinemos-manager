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

package com.clustercontrol.http.util;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.http.bean.HttpCheckInfo;
import com.clustercontrol.http.model.MonitorHttpInfoEntity;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;

/**
 * HTTP監視 判定情報を管理するクラス<BR>
 *
 * @version 5.0.0
 * @since 2.1.0
 */
public class ControlHttpInfo {

	/** 監視対象ID */
	private String m_monitorTypeId;

	/** 監視ID */
	private String m_monitorId;

	/**
	 * コンストラクタ
	 * 
	 * @param monitorId 監視項目ID
	 * @param monitorTypeId 監視対象ID
	 * @version 2.1.0
	 * @since 2.1.0
	 */
	public ControlHttpInfo(String monitorId, String monitorTypeId) {
		m_monitorId = monitorId;
		m_monitorTypeId = monitorTypeId;
	}

	/**
	 * HTTP監視情報を取得します。<BR>
	 * 
	 * @return HTTP監視情報
	 * @throws MonitorNotFound
	 * @version 2.1.0
	 * @since 2.1.0
	 */
	public HttpCheckInfo get() throws MonitorNotFound {

		// HTTP監視情報を取得
		MonitorHttpInfoEntity entity = QueryUtil.getMonitorHttpInfoPK(m_monitorId);

		HttpCheckInfo http = new HttpCheckInfo();
		http.setMonitorTypeId(m_monitorTypeId);
		http.setMonitorId(m_monitorId);
		http.setRequestUrl(entity.getRequestUrl());
		http.setTimeout(entity.getTimeout().intValue());
		http.setUrlReplace(entity.getUrlReplace().intValue());
		http.setProxySet(entity.getProxySet().intValue());
		http.setProxyHost(entity.getProxyHost());
		http.setProxyPort(entity.getProxyPort().intValue());

		return http;
	}

	/**
	 * HTTP監視情報を追加します。<BR>
	 * 
	 * @param http HTTP監視情報
	 * @return 成功した場合、true
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @version 2.1.0
	 * @since 2.1.0
	 */
	public boolean add(HttpCheckInfo http) throws MonitorNotFound, InvalidRole {

		MonitorInfoEntity monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorId);

		// HTTP監視情報を追加
		MonitorHttpInfoEntity entity = new MonitorHttpInfoEntity(monitorEntity);
		entity.setProxyHost(http.getProxyHost());
		entity.setProxyPort(Integer.valueOf(http.getProxyPort()));
		entity.setProxySet(Integer.valueOf(http.getProxySet()));
		entity.setRequestUrl(http.getRequestUrl());
		entity.setTimeout(Integer.valueOf(http.getTimeout()));
		entity.setUrlReplace(Integer.valueOf(http.getUrlReplace()));

		return true;
	}

	/**
	 * HTTP監視情報を変更します。<BR>
	 * 
	 * @param http HTTP監視情報
	 * @return 成功した場合、true
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @version 2.1.0
	 * @since 2.1.0
	 */
	public boolean modify(HttpCheckInfo http) throws MonitorNotFound, InvalidRole {

		MonitorInfoEntity monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorId);

		// HTTP監視情報を取得
		MonitorHttpInfoEntity entity = QueryUtil.getMonitorHttpInfoPK(m_monitorId);

		// HTTP監視情報を設定
		entity.setRequestUrl(http.getRequestUrl());
		entity.setUrlReplace(Integer.valueOf(http.getUrlReplace()));
		entity.setTimeout(Integer.valueOf(http.getTimeout()));
		entity.setProxySet(Integer.valueOf(http.getProxySet()));
		entity.setProxyHost(http.getProxyHost());
		entity.setProxyPort(Integer.valueOf(http.getProxyPort()));
		monitorEntity.setMonitorHttpInfoEntity(entity);

		return true;
	}
}
