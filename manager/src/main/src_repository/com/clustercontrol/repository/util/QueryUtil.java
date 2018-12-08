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

package com.clustercontrol.repository.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.Ipv6Util;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.model.CollectorPlatformMstEntity;
import com.clustercontrol.repository.model.CollectorSubPlatformMstEntity;
import com.clustercontrol.repository.model.FacilityEntity;
import com.clustercontrol.repository.model.FacilityRelationEntity;
import com.clustercontrol.repository.model.FacilityRelationEntityPK;
import com.clustercontrol.repository.model.NodeCpuEntity;
import com.clustercontrol.repository.model.NodeCpuEntityPK;
import com.clustercontrol.repository.model.NodeDeviceEntity;
import com.clustercontrol.repository.model.NodeDeviceEntityPK;
import com.clustercontrol.repository.model.NodeDiskEntity;
import com.clustercontrol.repository.model.NodeDiskEntityPK;
import com.clustercontrol.repository.model.NodeEntity;
import com.clustercontrol.repository.model.NodeFilesystemEntity;
import com.clustercontrol.repository.model.NodeFilesystemEntityPK;
import com.clustercontrol.repository.model.NodeHostnameEntity;
import com.clustercontrol.repository.model.NodeHostnameEntityPK;
import com.clustercontrol.repository.model.NodeMemoryEntity;
import com.clustercontrol.repository.model.NodeMemoryEntityPK;
import com.clustercontrol.repository.model.NodeNetworkInterfaceEntity;
import com.clustercontrol.repository.model.NodeNetworkInterfaceEntityPK;
import com.clustercontrol.repository.model.NodeNoteEntity;
import com.clustercontrol.repository.model.NodeNoteEntityPK;
import com.clustercontrol.repository.model.NodeVariableEntity;
import com.clustercontrol.repository.model.NodeVariableEntityPK;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static NodeEntity getNodePK(String facilityId) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeEntity entity = em.find(NodeEntity.class, facilityId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeEntity.findByPrimaryKey"
					+ ", facilityId = " + facilityId);
			m_log.info("getNodePK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static FacilityEntity getFacilityPK(String facilityId) throws FacilityNotFound, InvalidRole {
		return getFacilityPK(facilityId, ObjectPrivilegeMode.READ);
	}

	public static FacilityEntity getFacilityPK(String facilityId, ObjectPrivilegeMode mode) throws FacilityNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		FacilityEntity entity = null;
		try {
			entity = em.find(FacilityEntity.class, facilityId, mode);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("FacilityEntity.findByPrimaryKey"
						+ ", facilityId = " + facilityId);
				m_log.info("getFacilityPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getFacilityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static FacilityEntity getFacilityPK_OR(String facilityId, String ownerRoleId) throws FacilityNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		FacilityEntity entity = null;
		try {
			entity = em.find_OR(FacilityEntity.class, facilityId, ownerRoleId);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("FacilityEntity.findByPrimaryKey"
						+ ", facilityId = " + facilityId);
				m_log.info("getFacilityPK_OR() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getFacilityPK_OR() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static FacilityEntity getFacilityPK_NONE(String facilityId) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		FacilityEntity entity = null;
		try {
			entity = em.find(FacilityEntity.class, facilityId, ObjectPrivilegeMode.NONE);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("FacilityEntity.findByPrimaryKey"
						+ ", facilityId = " + facilityId);
				m_log.info("getFacilityPK_NONE() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			// NONE（オブジェクト権限チェックなし）のため、ここは通らない。
		}

		return entity;
	}

	@Deprecated
	public static FacilityEntity getFacilityPK_WRITE(String facilityId) throws FacilityNotFound, InvalidRole {
		return getFacilityPK(facilityId, ObjectPrivilegeMode.MODIFY);
	}

	public static List<FacilityEntity> getFacilityByOwnerRoleId_NONE(String roleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<FacilityEntity> list
		= em.createNamedQuery("FacilityEntity.findByOwnerRoleId", FacilityEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("ownerRoleId", roleId)
		.getResultList();
		return list;
	}

	public static List<FacilityEntity> getAllFacility() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<FacilityEntity> list
		= em.createNamedQuery("FacilityEntity.findAll", FacilityEntity.class)
		.getResultList();
		return list;
	}

	public static List<FacilityEntity> getAllFacility_NONE() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<FacilityEntity> list
		= em.createNamedQuery("FacilityEntity.findAll", FacilityEntity.class, ObjectPrivilegeMode.NONE)
		.getResultList();
		return list;
	}

	public static List<FacilityEntity> getRootScopeFacility_NONE() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<FacilityEntity> list
		= em.createNamedQuery("FacilityEntity.findRootByFacilityType", FacilityEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("facilityType", FacilityConstant.TYPE_SCOPE)
		.getResultList();
		return list;
	}

	public static FacilityRelationEntity getFacilityRelationPk(String parentFacilityId, String childFacilityId) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		FacilityRelationEntityPK pk = new FacilityRelationEntityPK(parentFacilityId, childFacilityId);
		FacilityRelationEntity entity = em.find(FacilityRelationEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("FacilityRelationEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getFacilityRelationPk() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<FacilityEntity> getParentFacilityEntity(String childFacilityId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<FacilityEntity> list = new ArrayList<FacilityEntity>();
		List<FacilityRelationEntity> FacilityRelationEntities
		= em.createNamedQuery("FacilityRelationEntity.findParent", FacilityRelationEntity.class)
		.setParameter("childFacilityId", childFacilityId)
		.getResultList();
		for (FacilityRelationEntity facilityRelationEntity : FacilityRelationEntities) {
			try {
				FacilityEntity parentFacilityEntity
					= QueryUtil.getFacilityPK_NONE(facilityRelationEntity.getId().getParentFacilityId());
				list.add(parentFacilityEntity);
			} catch (FacilityNotFound e) {
				// 通らない。
			}
		}
		return list;
	}

	public static List<FacilityEntity> getChildFacilityEntity(String parentFacilityId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<FacilityEntity> list = new ArrayList<FacilityEntity>();
		List<FacilityRelationEntity> FacilityRelationEntities
		= em.createNamedQuery("FacilityRelationEntity.findChild", FacilityRelationEntity.class)
		.setParameter("parentFacilityId", parentFacilityId)
		.getResultList();
		for (FacilityRelationEntity facilityRelationEntity : FacilityRelationEntities) {
			try {
				FacilityEntity childFacilityEntity
					= QueryUtil.getFacilityPK_NONE(facilityRelationEntity.getId().getChildFacilityId());
				list.add(childFacilityEntity);
			} catch (FacilityNotFound e) {
				// 通らない。
			}
		}
		return list;
	}

	public static List<FacilityRelationEntity> getAllFacilityRelations_NONE() {
		HinemosEntityManager em = new JpaTransactionManager()
				.getEntityManager();
		List<FacilityRelationEntity> list = em.createNamedQuery(
				"FacilityRelationEntity.findAll", FacilityRelationEntity.class,
				ObjectPrivilegeMode.NONE).getResultList();
		return list;
	}

	public static List<FacilityEntity> getAllNode() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<FacilityEntity> list
		= em.createNamedQuery("FacilityEntity.findByFacilityType", FacilityEntity.class)
		.setParameter("facilityType", FacilityConstant.TYPE_NODE)
		.getResultList();
		return list;
	}

	public static List<FacilityEntity> getAllNode_NONE() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<FacilityEntity> list
		= em.createNamedQuery("FacilityEntity.findByFacilityType", FacilityEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("facilityType", FacilityConstant.TYPE_NODE)
		.getResultList();
		return list;
	}

	public static List<NodeHostnameEntity> getAllNodeHostname() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NodeHostnameEntity> list
		= em.createNamedQuery("NodeHostnameEntity.findAll", NodeHostnameEntity.class)
		.getResultList();
		return list;
	}

	public static List<FacilityEntity> getNodeByIpv4(String ipAddressV4) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<FacilityEntity> list
		= em.createNamedQuery("FacilityEntity.findByIpAddressV4", FacilityEntity.class)
		.setParameter("ipAddressV4", ipAddressV4)
		.getResultList();
		return list;
	}

	public static List<FacilityEntity> getNodeByIpv6(String ipAddressV6) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<FacilityEntity> list
		= em.createNamedQuery("FacilityEntity.findByIpAddressV6", FacilityEntity.class)
		.setParameter("ipAddressV6", Ipv6Util.expand(ipAddressV6))
		.getResultList();
		return list;
	}

	public static List<FacilityEntity> getNodeByNodename(String nodeName) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<FacilityEntity> list
		= em.createNamedQuery("FacilityEntity.findByNodename", FacilityEntity.class)
		.setParameter("nodeName", nodeName.toLowerCase())
		.getResultList();
		return list;
	}

	public static NodeHostnameEntity getNodeHostnamePK(NodeHostnameEntityPK pk) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeHostnameEntity entity = em.find(NodeHostnameEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeHostnameEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNodeHostnamePK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NodeDeviceEntity getNodeDeviceEntityPK(NodeDeviceEntityPK pk) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeDeviceEntity entity = em.find(NodeDeviceEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeDeviceEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNodeDeviceEntityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NodeCpuEntity getNodeCpuEntityPK(NodeCpuEntityPK pk) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeCpuEntity entity = em.find(NodeCpuEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeCpuEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNodeCpuEntityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NodeMemoryEntity getNodeMemoryEntityPK(NodeMemoryEntityPK pk) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeMemoryEntity entity = em.find(NodeMemoryEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeMemoryEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNodeMemoryEntityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NodeNetworkInterfaceEntity getNodeNetworkInterfaceEntityPK(NodeNetworkInterfaceEntityPK pk) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeNetworkInterfaceEntity entity = em.find(NodeNetworkInterfaceEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeNetworkInterfaceEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNodeNetworkInterfaceEntityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NodeDiskEntity getNodeDiskEntityPK(NodeDiskEntityPK pk) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeDiskEntity entity = em.find(NodeDiskEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeDiskEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNodeDiskEntityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NodeFilesystemEntity getFilesystemDiskEntityPK(NodeFilesystemEntityPK pk) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeFilesystemEntity entity = em.find(NodeFilesystemEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeFilesystemEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNodeFilesystemEntityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NodeVariableEntity getNodeVariableEntityPK(NodeVariableEntityPK pk) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeVariableEntity entity = em.find(NodeVariableEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeVariableEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNodeVariableEntityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NodeNoteEntity getNodeNoteEntityPK(NodeNoteEntityPK pk) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeNoteEntity entity = em.find(NodeNoteEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeNoteEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNodeNoteEntityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static CollectorPlatformMstEntity getCollectorPlatformMstPK(String platformId) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CollectorPlatformMstEntity entity = em.find(CollectorPlatformMstEntity.class, platformId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("CollectorPlatformMstEntity.findByPrimaryKey"
					+ ", platformId = " + platformId);
			m_log.info("getCollectorPlatformMstPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static CollectorSubPlatformMstEntity getCollectorSubPlatformMstPK(String subPlatformId) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CollectorSubPlatformMstEntity entity = em.find(CollectorSubPlatformMstEntity.class, subPlatformId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("CollectorSubPlatformMstEntity.findByPrimaryKey"
					+ ", subPlatformId = " + subPlatformId);
			m_log.info("getCollectorSubPlatformMstPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<CollectorPlatformMstEntity> getAllCollectorPlatformMst() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CollectorPlatformMstEntity> list
		= em.createNamedQuery("CollectorPlatformMstEntity.findAll", CollectorPlatformMstEntity.class)
		.getResultList();
		return list;
	}

	public static List<CollectorSubPlatformMstEntity> getAllCollectorSubPlatformMstEntity() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CollectorSubPlatformMstEntity> list
		= em.createNamedQuery("CollectorSubPlatformMstEntity.findAll", CollectorSubPlatformMstEntity.class)
		.getResultList();
		return list;
	}
}
