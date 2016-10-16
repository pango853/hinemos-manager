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

package com.clustercontrol.sql.util;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.sql.bean.SqlCheckInfo;
import com.clustercontrol.sql.model.MonitorSqlInfoEntity;

/**
 * SQL監視 判定情報管理クラス
 *
 * @version 4.0.0
 * @since 2.1.0
 */
public class ControlSqlInfo {

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
	public ControlSqlInfo(String monitorId, String monitorTypeId) {
		m_monitorId = monitorId;
		m_monitorTypeId = monitorTypeId;
	}

	/**
	 * SQL監視情報を取得
	 * 
	 * @return SQL監視情報
	 * @throws MonitorNotFound
	 * @version 2.1.0
	 * @since 2.1.0
	 */
	public SqlCheckInfo get() throws MonitorNotFound {

		// SQL監視情報を取得
		MonitorSqlInfoEntity entity = QueryUtil.getMonitorSqlInfoPK(m_monitorId);

		SqlCheckInfo sql = new SqlCheckInfo();
		sql.setMonitorTypeId(m_monitorTypeId);
		sql.setMonitorId(m_monitorId);
		sql.setConnectionUrl(entity.getConnectionUrl());
		sql.setUser(entity.getConnectionUser());
		sql.setPassword(entity.getConnectionPassword());
		sql.setQuery(entity.getQuery());
		sql.setJdbcDriver(entity.getJdbcDriver());

		return sql;
	}

	/**
	 * SQL監視情報を追加
	 * 
	 * @param sql SQL監視情報
	 * @return 成功した場合、true
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @version 2.1.0
	 * @since 2.1.0
	 */
	public boolean add(SqlCheckInfo sql) throws MonitorNotFound, InvalidRole {

		MonitorInfoEntity monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorId);

		// SQL監視情報を追加
		MonitorSqlInfoEntity entity = new MonitorSqlInfoEntity(monitorEntity);
		entity.setConnectionPassword(sql.getPassword());
		entity.setConnectionUrl(sql.getConnectionUrl());
		entity.setConnectionUser(sql.getUser());
		entity.setJdbcDriver(sql.getJdbcDriver());
		entity.setQuery(sql.getQuery());

		return true;
	}

	/**
	 * SQL監視情報を変更
	 * 
	 * @param sql SQL監視情報
	 * @return 成功した場合、true
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @version 2.1.0
	 * @since 2.1.0
	 */
	public boolean modify(SqlCheckInfo sql) throws MonitorNotFound, InvalidRole {

		MonitorInfoEntity monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorId);

		// SQL監視情報を取得
		MonitorSqlInfoEntity entity = QueryUtil.getMonitorSqlInfoPK(m_monitorId);

		// SQL監視情報を設定
		entity.setConnectionUrl(sql.getConnectionUrl());
		entity.setConnectionUser(sql.getUser());
		entity.setConnectionPassword(sql.getPassword());
		entity.setQuery(sql.getQuery());
		entity.setJdbcDriver(sql.getJdbcDriver());
		monitorEntity.setMonitorSqlInfoEntity(entity);
		return true;
	}
}
