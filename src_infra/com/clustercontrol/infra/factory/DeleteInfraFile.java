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

package com.clustercontrol.infra.factory;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraFileBeingUsed;
import com.clustercontrol.fault.InfraFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.infra.model.InfraFileEntity;
import com.clustercontrol.infra.util.QueryUtil;

/**
 * 環境構築ファイルを削除する。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class DeleteInfraFile {
	/** ログ出力のインスタンス。 */
	private static Logger m_log = Logger.getLogger( DeleteInfraFile.class );

	/**
	 * ファイルを削除します。
	 * @throws InfraFileNotFound 
	 * @throws InfraFileBeingUsed 
	 */
	public void delete(String fileId) throws InvalidRole, HinemosUnknown, InfraFileNotFound, InfraFileBeingUsed {
		m_log.debug(String.format("delete() : fileId = %s", fileId));

		// ファイルを取得
		InfraFileEntity entity = null;
		try {
			HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
			entity = em.find(InfraFileEntity.class, fileId, ObjectPrivilegeMode.MODIFY);
			if (entity == null) {
				InfraFileNotFound e = new InfraFileNotFound("InfraFileEntity.findByPrimaryKey, fileId = " + fileId);
				m_log.info("delete() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			if (QueryUtil.isInfraFileReferredByFileTransferModuleInfoEntity(fileId)) {
				InfraFileBeingUsed e = new InfraFileBeingUsed("InfraFile is used by FileTransferModuleInfoEntity, fileId = " + fileId);
				m_log.info("delete() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			em.remove(entity);
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("delete() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
	}
}
