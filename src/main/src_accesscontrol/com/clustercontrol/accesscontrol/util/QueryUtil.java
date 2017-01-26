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

package com.clustercontrol.accesscontrol.util;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleTypeConstant;
import com.clustercontrol.accesscontrol.bean.SystemPrivilegeInfo;
import com.clustercontrol.accesscontrol.bean.UserTypeConstant;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeEntity;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeEntityPK;
import com.clustercontrol.accesscontrol.model.RoleEntity;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeEntity;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeEntityPK;
import com.clustercontrol.accesscontrol.model.UserEntity;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.PrivilegeNotFound;
import com.clustercontrol.fault.RoleNotFound;
import com.clustercontrol.fault.UserNotFound;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static UserEntity getUserPK(String userId) throws UserNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		UserEntity entity = em.find(UserEntity.class, userId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			UserNotFound e = new UserNotFound("UserEntity.findByPrimaryKey, "
					+ "userId = " + userId);
			m_log.info("getUserPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<UserEntity> getAllShowUser() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<UserEntity> list
		= em.createNamedQuery("UserEntity.findAllLoginSystemUser", UserEntity.class)
		.setParameter("userType_login", UserTypeConstant.LOGIN_USER)
		.setParameter("userType_system", UserTypeConstant.SYSTEM_USER)
		.getResultList();
		return list;
	}

	/**
	 * オブジェクト権限チェックなし
	 * @return
	 */
	public static List<UserEntity> getAllUser_NONE() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<UserEntity> list
			= em.createNamedQuery("UserEntity.findAllUser", UserEntity.class, ObjectPrivilegeMode.NONE)
				.getResultList();
		return list;
	}

	public static RoleEntity getRolePK(String roleId) throws RoleNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		RoleEntity entity = em.find(RoleEntity.class, roleId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			RoleNotFound e = new RoleNotFound("RoleEntity.findByPrimaryKey, "
					+ "roleId = " + roleId);
			m_log.info("getRolePK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static SystemPrivilegeEntity getSystemPrivilegePK(SystemPrivilegeEntityPK entityPk) throws RoleNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		SystemPrivilegeEntity entity = em.find(SystemPrivilegeEntity.class, entityPk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			RoleNotFound e = new RoleNotFound("SystemPrivilegeEntity.findByPrimaryKey, "
					+ entityPk.toString());
			m_log.info("getSystemPrivilegePK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<RoleEntity> getAllShowRole() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<RoleEntity> list
		= em.createNamedQuery("RoleEntity.findAllLoginSystemRole", RoleEntity.class)
		.setParameter("roleType_user", RoleTypeConstant.USER_ROLE)
		.setParameter("roleType_system", RoleTypeConstant.SYSTEM_ROLE)
		.getResultList();
		return list;
	}

	/**
	 * オブジェクト権限チェックなし
	 * @return
	 */
	public static List<RoleEntity> getAllRole_NONE() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<RoleEntity> list
			= em.createNamedQuery("RoleEntity.findAllRole", RoleEntity.class, ObjectPrivilegeMode.NONE)
				.getResultList();
		return list;
	}

	public static List<SystemPrivilegeEntity> getAllSystemPrivilege() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<SystemPrivilegeEntity> list
			= em.createNamedQuery("SystemPrivilegeEntity.findAll", SystemPrivilegeEntity.class).getResultList();
		return list;
	}

	public static ArrayList<SystemPrivilegeInfo> getSystemPrivilegeByUserId(String userId) {
		ArrayList<SystemPrivilegeInfo> rtnList = new ArrayList<SystemPrivilegeInfo>();
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<Object[]> list = em.createNamedQuery("SystemPrivilegeEntity.findByUserId")
				.setParameter("userId", userId).getResultList();
		if (list != null && list.size() > 0) {
			for (Object[] obj : list) {
				SystemPrivilegeInfo info = new SystemPrivilegeInfo();
				info.setSystemFunction((String)obj[0]);
				info.setSystemPrivilege((String)obj[1]);
				rtnList.add(info);
			}
		}
		return rtnList;
	}

	public static ObjectPrivilegeEntity getObjectPrivilegePK(String objectType, String objectId, String roleId, String objectPrivilege)
			throws PrivilegeNotFound {
		return getObjectPrivilegePK(new ObjectPrivilegeEntityPK(objectType, objectId, roleId, objectPrivilege));
	}

	public static ObjectPrivilegeEntity getObjectPrivilegePK(ObjectPrivilegeEntityPK pk)
			throws PrivilegeNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		ObjectPrivilegeEntity entity = em.find(ObjectPrivilegeEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			PrivilegeNotFound e = new PrivilegeNotFound("ObjectPrivilegeEntity.findByPrimaryKey, "
					+ pk.toString());
			m_log.info("getObjectPrivilegePK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<ObjectPrivilegeEntity> getAllObjectPrivilege() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<ObjectPrivilegeEntity> list
		= em.createNamedQuery("ObjectPrivilegeEntity.findAll", ObjectPrivilegeEntity.class).getResultList();
		return list;
	}

	public static List<ObjectPrivilegeEntity> getAllObjectPrivilegeByFilter(
			String objectType,
			String objectId,
			String roleId,
			String objectPrivilege) {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		StringBuffer sbJpql = new StringBuffer();
		sbJpql.append("SELECT a FROM ObjectPrivilegeEntity a WHERE true = true");
		if(objectType != null && !"".equals(objectType)) {
			sbJpql.append(" AND a.id.objectType = :objectType");
		}
		if(objectId != null && !"".equals(objectId)) {
			sbJpql.append(" AND a.id.objectId = :objectId");
		}
		if(roleId != null && !"".equals(roleId)) {
			sbJpql.append(" AND a.id.roleId = :roleId");
		}
		if(objectPrivilege != null && !"".equals(objectPrivilege)) {
			sbJpql.append(" AND a.id.objectPrivilege = :objectPrivilege");
		}
		TypedQuery<ObjectPrivilegeEntity> typedQuery = em.createQuery(sbJpql.toString(), ObjectPrivilegeEntity.class);
		if(objectType != null && !"".equals(objectType)) {
			typedQuery = typedQuery.setParameter("objectType", objectType);
		}
		if(objectId != null && !"".equals(objectId)) {
			typedQuery = typedQuery.setParameter("objectId", objectId);
		}
		if(roleId != null && !"".equals(roleId)) {
			typedQuery = typedQuery.setParameter("roleId", roleId);
		}
		if(objectPrivilege != null && !"".equals(objectPrivilege)) {
			typedQuery = typedQuery.setParameter("objectPrivilege", objectPrivilege);
		}
		return typedQuery.getResultList();
	}

}
