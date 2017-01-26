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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.infra.bean.ModuleNodeResult;
import com.clustercontrol.infra.model.InfraCheckResultEntity;
import com.clustercontrol.infra.util.QueryUtil;

/**
 * チェックの実行結果を更新する。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class UpdateInfraCheckResult {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( UpdateInfraCheckResult.class );

	/**
	 * @throws InfraManagementNotFound 
	 */
	public static void update(String managementId, String moduleId, List<ModuleNodeResult> resultList) {
		m_log.debug("update() : start");
		
		m_log.debug(String.format("update() : managementId = %s", managementId));
		
		// 環境構築情報へのアクセス権チェック
		try {
			new SelectInfraManagement().get(managementId, ObjectPrivilegeMode.READ);
		} catch (InfraManagementNotFound | InvalidRole | HinemosUnknown e) {
			m_log.warn("update " + e.getClass().getName() + ", " + e.getMessage());
		}
		
		List<InfraCheckResultEntity> entities = QueryUtil.getInfraCheckResultFindByModuleId(managementId, moduleId);

		List<ModuleNodeResult> newResultList = new ArrayList<>(resultList);
		List<InfraCheckResultEntity> oldResultList = new ArrayList<>(entities);
		
		m_log.info("newResult.size=" + newResultList.size() + ", oldResult.size=" + oldResultList.size());
		
		// update
		Iterator<InfraCheckResultEntity> oldItr = oldResultList.iterator();
		while (oldItr.hasNext()) {
			InfraCheckResultEntity oldResult = oldItr.next();
			Iterator<ModuleNodeResult> newItr = newResultList.iterator();
			while (newItr.hasNext()) {
				ModuleNodeResult newResult = newItr.next();
				if (
					oldResult.getId().getManagementId().equals(managementId) &&
					oldResult.getId().getModuleId().equals(moduleId) &&
					oldResult.getId().getNodeId().equals(newResult.getFacilityId())
					) {
					oldResult.setResult(newResult.getResult());
					
					newItr.remove();
					oldItr.remove();
					break;
				}
			}
		}
		
		m_log.info("newResult.size=" + newResultList.size() + ", oldResult.size=" + oldResultList.size());

		// insert
		for (ModuleNodeResult newResult: newResultList) {
			InfraCheckResultEntity resultEntity = new InfraCheckResultEntity(managementId, moduleId, newResult.getFacilityId());
			resultEntity.setResult(newResult.getResult());
		}
		
		// delete
		for (InfraCheckResultEntity oldResult: oldResultList) {
			oldResult.removeSelf();
		}

		m_log.debug("update() : end");
	}
}