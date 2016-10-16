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

package com.clustercontrol.notify.mail.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.notify.mail.model.MailTemplateInfoEntity;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static MailTemplateInfoEntity getMailTemplateInfoPK(String mailTemplateId) throws MailTemplateNotFound, InvalidRole {
		return getMailTemplateInfoPK(mailTemplateId, ObjectPrivilegeMode.READ);
	}

	public static MailTemplateInfoEntity getMailTemplateInfoPK(String mailTemplateId, ObjectPrivilegeMode mode) throws MailTemplateNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MailTemplateInfoEntity entity = null;
		try {
			entity = em.find(MailTemplateInfoEntity.class, mailTemplateId, mode);
			if (entity == null) {
				MailTemplateNotFound e = new MailTemplateNotFound("MailTemplateInfoEntity.findByPrimaryKey"
						+ ", mailTemplateId = " + mailTemplateId);
				m_log.info("getMailTemplateInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getMailTemplateInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static MailTemplateInfoEntity getMailTemplateInfoPK_OR(String mailTemplateId, String ownerRoleId) throws MailTemplateNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MailTemplateInfoEntity entity = null;
		try {
			entity = em.find_OR(MailTemplateInfoEntity.class, mailTemplateId, ownerRoleId);
			if (entity == null) {
				MailTemplateNotFound e = new MailTemplateNotFound("MailTemplateInfoEntity.findByPrimaryKey"
						+ ", mailTemplateId = " + mailTemplateId);
				m_log.info("getMailTemplateInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getMailTemplateInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static List<MailTemplateInfoEntity> getAllMailTemplateInfo() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MailTemplateInfoEntity> list
		= em.createNamedQuery("MailTemplateInfoEntity.findAll", MailTemplateInfoEntity.class)
		.getResultList();
		return list;
	}

	public static List<MailTemplateInfoEntity> getAllMailTemplateInfoOrderByMailTemplateId() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MailTemplateInfoEntity> list
		= em.createNamedQuery("MailTemplateInfoEntity.findAllOrderByMailTemplateId", MailTemplateInfoEntity.class)
		.getResultList();
		return list;
	}

	public static List<MailTemplateInfoEntity> getAllMailTemplateInfoOrderByMailTemplateId_OR(String ownerRoleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MailTemplateInfoEntity> list
		= em.createNamedQuery_OR("MailTemplateInfoEntity.findAllOrderByMailTemplateId", MailTemplateInfoEntity.class, ownerRoleId)
		.getResultList();
		return list;
	}

	public static List<MailTemplateInfoEntity> getMailTemplateInfoFindByOwnerRoleId_NONE(String roleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MailTemplateInfoEntity> list
		= em.createNamedQuery("MailTemplateInfoEntity.findByOwnerRoleId", MailTemplateInfoEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("ownerRoleId", roleId)
		.getResultList();
		return list;
	}
}
