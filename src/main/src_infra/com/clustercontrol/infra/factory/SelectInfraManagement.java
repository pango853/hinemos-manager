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

package com.clustercontrol.infra.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.infra.bean.InfraManagementInfo;
import com.clustercontrol.infra.model.InfraManagementInfoEntity;
import com.clustercontrol.infra.util.QueryUtil;

/**
 * 環境構築情報を取得する。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class SelectInfraManagement {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectInfraManagement.class );

	/**
	 * 環境構築情報を取得します。
	 * <p>
	 */
	public InfraManagementInfo get(String infraManagementId, ObjectPrivilegeMode mode) throws InfraManagementNotFound, InvalidRole, HinemosUnknown {
		
		InfraManagementInfoEntity entity = QueryUtil.getInfraManagementInfoPK(infraManagementId, mode);
		
		InfraManagementInfo info = entity.createWebEntity();
		
		return info;
	}
	
	public List<InfraManagementInfo> getList() throws InvalidRole, HinemosUnknown {
		
		List<InfraManagementInfoEntity> list = QueryUtil.getAllInfraManagementInfo();
		
		List<InfraManagementInfo> webEntities = new ArrayList<>();
		for (InfraManagementInfoEntity entity: list) {
			webEntities.add(entity.createWebEntity());
		}
		
		return webEntities;
	}

	public List<InfraManagementInfo> getListByOwnerRole(String ownerRoleId) throws InvalidRole, HinemosUnknown {
		m_log.debug("getList() : start");

		List<InfraManagementInfoEntity> list = QueryUtil.getAllInfraManagementInfoOrderByInfraManagementId_OR(ownerRoleId);
		List<InfraManagementInfo> webEntities = new ArrayList<>();
		for (InfraManagementInfoEntity entity: list) {
			webEntities.add(entity.createWebEntity());
		}
		
		return webEntities;
	}
}
