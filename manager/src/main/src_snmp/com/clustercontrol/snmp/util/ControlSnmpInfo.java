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

package com.clustercontrol.snmp.util;

import java.util.ArrayList;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.snmp.bean.SnmpCheckInfo;
import com.clustercontrol.snmp.model.MonitorSnmpInfoEntity;
import com.clustercontrol.snmp.model.MonitorSnmpValueEntity;

/**
 * SNMP監視 判定情報を管理するクラス<BR>
 *
 * @version 4.0.0
 * @since 2.1.0
 */
public class ControlSnmpInfo {

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
	public ControlSnmpInfo(String id, String typeId) {
		m_monitorId = id;
		m_monitorTypeId = typeId;
	}

	/**
	 * SNMP監視情報を取得
	 * 
	 * @return SNMP監視情報
	 * @throws MonitorNotFound
	 * @version 2.1.0
	 * @since 2.1.0
	 */
	public SnmpCheckInfo get() throws MonitorNotFound {

		// SNMP監視情報を取得
		MonitorSnmpInfoEntity entity = QueryUtil.getMonitorSnmpInfoPK(m_monitorId);

		SnmpCheckInfo snmp = new SnmpCheckInfo();
		snmp.setMonitorTypeId(m_monitorTypeId);
		snmp.setMonitorId(m_monitorId);
		snmp.setSnmpOid(entity.getSnmpOid());
		snmp.setConvertFlg(entity.getConvertFlg());

		return snmp;
	}

	/**
	 * SNMP監視情報を追加
	 * 
	 * @param snmp SNMP監視情報
	 * @return 成功した場合、true
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @version 2.1.0
	 * @since 2.1.0
	 */
	public boolean add(SnmpCheckInfo snmp) throws MonitorNotFound, InvalidRole {

		MonitorInfoEntity monitorEntity = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorId);

		// SNMP監視情報を追加
		MonitorSnmpInfoEntity entity = new MonitorSnmpInfoEntity(monitorEntity);
		entity.setConvertFlg(snmp.getConvertFlg());
		entity.setSnmpOid(snmp.getSnmpOid());

		return true;
	}

	/**
	 * SNMP監視情報を変更
	 * 
	 * @param snmp SNMP監視情報
	 * @param deleteValueFlg 前回値情報削除フラグ
	 * @return 成功した場合、true
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @version 2.1.0
	 * @since 2.1.0
	 */
	public boolean modify(SnmpCheckInfo snmp, boolean deleteValueFlg) throws MonitorNotFound, InvalidRole {

		MonitorInfoEntity monitorEntity = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorId);

		// SNMP監視情報を取得
		MonitorSnmpInfoEntity entity = QueryUtil.getMonitorSnmpInfoPK(m_monitorId);

		// 更新前のOIDを取得
		String prevSnmpOid = entity.getSnmpOid();

		entity.setSnmpOid(snmp.getSnmpOid());
		entity.setConvertFlg(snmp.getConvertFlg());
		// SNMP前回値情報を削除
		if(deleteValueFlg || !prevSnmpOid.equals(snmp.getSnmpOid())){
			entity.setMonitorSnmpValueEntities(new ArrayList<MonitorSnmpValueEntity>());
		}
		monitorEntity.setMonitorSnmpInfoEntity(entity);

		return true;
	}

}
