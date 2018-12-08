/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.util;

import java.util.List;

import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.infra.model.FileTransferModuleInfoEntity;
import com.clustercontrol.infra.model.InfraCheckResultEntity;
import com.clustercontrol.infra.model.InfraFileEntity;
import com.clustercontrol.infra.model.InfraManagementInfoEntity;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static InfraManagementInfoEntity getInfraManagementInfoPK(String managementId) throws InfraManagementNotFound, InvalidRole {
		return getInfraManagementInfoPK(managementId, ObjectPrivilegeMode.READ);
	}

	public static InfraManagementInfoEntity getInfraManagementInfoPK(String managementId, ObjectPrivilegeMode mode) throws InfraManagementNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		InfraManagementInfoEntity entity = null;
		try {
			entity = em.find(InfraManagementInfoEntity.class, managementId, mode);
			if (entity == null) {
				InfraManagementNotFound e = new InfraManagementNotFound("InfraManagementInfoEntity.findByPrimaryKey"
						+ ", managementId = " + managementId);
				m_log.info("getInfraManagementInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getInfraManagementInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static List<InfraManagementInfoEntity> getAllInfraManagementInfo() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<InfraManagementInfoEntity> list
		= em.createNamedQuery("InfraManagementInfoEntity.findAll", InfraManagementInfoEntity.class)
		.getResultList();
		return list;
	}

	public static List<InfraManagementInfoEntity> getAllInfraManagementInfoOrderByInfraManagementId_OR(String ownerRoleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<InfraManagementInfoEntity> list
		= em.createNamedQuery_OR("InfraManagementInfoEntity.findAllOrderByInfraManagementId", InfraManagementInfoEntity.class, ownerRoleId)
		.getResultList();
		return list;
	}

	public static List<InfraManagementInfoEntity> getInfraManagementInfoFindByOwnerRoleId_NONE(String roleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<InfraManagementInfoEntity> list
		= em.createNamedQuery("InfraManagementInfoEntity.findByOwnerRoleId", InfraManagementInfoEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("ownerRoleId", roleId)
		.getResultList();
		return list;
	}

	public static List<InfraFileEntity> getAllInfraFile() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<InfraFileEntity> list
			= em.createNamedQuery("InfraFileEntity.findAll", InfraFileEntity.class).getResultList();
		return list;
	}

	public static List<InfraManagementInfoEntity> getInfraManagementInfoFindByFacilityId_NONE (String facilityId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<InfraManagementInfoEntity> list = null;

		// ファシリティIDが使用されている設定を取得する。
		list = em.createNamedQuery("InfraManagementInfoEntity.findByFacilityId", InfraManagementInfoEntity.class, ObjectPrivilegeMode.NONE)
			.setParameter("facilityId", facilityId)
			.getResultList();
		
		return list;
	}
	
	public static List<InfraCheckResultEntity> getInfraCheckResultFindByManagementId(String managementId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		TypedQuery<InfraCheckResultEntity> query = em.createNamedQuery("InfraCheckResultEntity.findByManagementId", InfraCheckResultEntity.class);
		query.setParameter("managementId", managementId);
		List<InfraCheckResultEntity> list = query.getResultList();
		return list;
	}
	
	public static List<InfraCheckResultEntity> getInfraCheckResultFindByModuleId(String managementId, String moduleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		TypedQuery<InfraCheckResultEntity> query = em.createNamedQuery("InfraCheckResultEntity.findByModuleId", InfraCheckResultEntity.class);
		query.setParameter("managementId", managementId);
		query.setParameter("moduleId", moduleId);
		List<InfraCheckResultEntity> list = query.getResultList();
		return list;
	}

	public static boolean isInfraFileReferredByFileTransferModuleInfoEntity(String fileId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		TypedQuery<FileTransferModuleInfoEntity> query = em.createNamedQuery("FileTransferModuleInfoEntity.findByFileId", FileTransferModuleInfoEntity.class);
		query.setParameter("fileId", fileId);
		query.setMaxResults(1);
		return !query.getResultList().isEmpty();
	}
}
