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

package com.clustercontrol.jmx.util;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jmx.bean.JmxCheckInfo;
import com.clustercontrol.jmx.model.MonitorJmxInfoEntity;
import com.clustercontrol.jmx.model.MonitorJmxMstEntity;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;

/**
 * JMX 監視 判定情報を管理するクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class ControlJmxInfo {

	/** 監視対象ID */
	private String m_monitorTypeId;

	/** 監視ID */
	private String m_monitorId;

	/**
	 * コンストラクタ
	 * 
	 * @param monitorId 監視項目ID
	 * @param monitorTypeId 監視対象ID
	 */
	public ControlJmxInfo(String monitorId, String monitorTypeId) {
		m_monitorId = monitorId;
		m_monitorTypeId = monitorTypeId;
	}

	/**
	 * JMX 監視情報を取得します。<BR>
	 * 
	 * @return JMX 監視情報
	 * @throws MonitorNotFound
	 */
	public JmxCheckInfo get() throws MonitorNotFound {

		// JMX 監視情報を取得
		MonitorJmxInfoEntity entity = QueryUtil.getMonitorJmxInfoPK(m_monitorId);

		JmxCheckInfo jmx = new JmxCheckInfo();
		jmx.setMonitorTypeId(m_monitorTypeId);
		jmx.setMonitorId(m_monitorId);
		jmx.setAuthUser(entity.getAuthUser());
		jmx.setAuthPassword(entity.getAuthPassword());
		jmx.setPort(entity.getPort());
		jmx.setMasterId(entity.getJmxTypeMstEntity().getId());

		return jmx;
	}

	/**
	 * JMX 監視情報を追加します。<BR>
	 * 
	 * @param jmx JMX 監視情報
	 * @return 成功した場合、true
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 */
	public boolean add(JmxCheckInfo jmx) throws MonitorNotFound, InvalidRole {

		MonitorInfoEntity monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorId);

		// JMX 監視情報を追加
		MonitorJmxInfoEntity entity = new MonitorJmxInfoEntity(monitorEntity);
		entity.setAuthUser(jmx.getAuthUser());
		entity.setAuthPassword(jmx.getAuthPassword());
		entity.setPort(jmx.getPort());
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		entity.setJmxTypeMstEntity(em.find(MonitorJmxMstEntity.class, jmx.getMasterId(), ObjectPrivilegeMode.READ));

		return true;
	}

	/**
	 * JMX 監視情報を変更します。<BR>
	 * 
	 * @param jmx JMX 監視情報
	 * @return 成功した場合、true
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 */
	public boolean modify(JmxCheckInfo jmx) throws MonitorNotFound, InvalidRole {

		MonitorInfoEntity monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorId);

		// JMX 監視情報を取得
		MonitorJmxInfoEntity entity = QueryUtil.getMonitorJmxInfoPK(m_monitorId);

		// JMX 監視情報を設定
		entity.setAuthUser(jmx.getAuthUser());
		entity.setAuthPassword(jmx.getAuthPassword());
		entity.setPort(jmx.getPort());
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		entity.setJmxTypeMstEntity(em.find(MonitorJmxMstEntity.class, jmx.getMasterId(), ObjectPrivilegeMode.READ));
		monitorEntity.setMonitorJmxInfoEntity(entity);

		return true;
	}
}
