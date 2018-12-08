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

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import javax.activation.DataHandler;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraFileNotFound;
import com.clustercontrol.fault.InfraFileTooLarge;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.infra.bean.InfraFileInfo;
import com.clustercontrol.infra.model.InfraFileEntity;
import com.clustercontrol.infra.util.InfraJdbcExecutor;

/**
 * 環境構築ファイルを変更
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class ModifyInfraFile {
	private static Logger m_log = Logger.getLogger( DeleteInfraFile.class );
	
	public void modify(InfraFileInfo fileInfo, DataHandler fileContent,
			String userId) throws IOException, InfraFileTooLarge, InfraFileNotFound, InvalidRole, HinemosUnknown {
		
		// ファイルを取得
		InfraFileEntity entity = null;
		try {
			HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
			String fileId = fileInfo.getFileId();
			entity = em.find(InfraFileEntity.class, fileId, ObjectPrivilegeMode.MODIFY);
			if (entity == null) {
				InfraFileNotFound e = new InfraFileNotFound("InfraFileEntity.findByPrimaryKey, fileId = " + fileId);
				m_log.info("delete() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			entity.setModifyUserId(userId);
			entity.setModifyDatetime(new Timestamp(new Date().getTime()));
			entity.setFileName(fileInfo.getFileName());
			
			if (fileContent != null) {
				em.remove(entity.getInfraFileContentEntity());
				new JpaTransactionManager().flush();
				InfraJdbcExecutor.insertFileContent(fileInfo.getFileId(), fileContent);
			}

		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("delete() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
	}		
}