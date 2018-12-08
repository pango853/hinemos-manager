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
import javax.persistence.EntityExistsException;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraFileTooLarge;
import com.clustercontrol.infra.bean.InfraFileInfo;
import com.clustercontrol.infra.model.InfraFileEntity;
import com.clustercontrol.infra.util.InfraJdbcExecutor;

/**
 * 環境構築ファイルを新規作成
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class AddInfraFile {
	public void add(InfraFileInfo fileInfo, DataHandler fileContent,
			String userId) throws IOException, InfraFileTooLarge, EntityExistsException, HinemosUnknown {
		JpaTransactionManager jtm = new JpaTransactionManager();
		
		Timestamp now = new Timestamp(new Date().getTime());
		InfraFileEntity entity = new InfraFileEntity(fileInfo.getFileId(), fileInfo.getFileName());
		jtm.checkEntityExists(InfraFileEntity.class, entity.getFileId());
		entity.setCreateDatetime(now);
		entity.setCreateUserId(userId);
		entity.setModifyDatetime(now);
		entity.setModifyUserId(userId);
		entity.setOwnerRoleId(fileInfo.getOwnerRoleId());
		jtm.flush();
		InfraJdbcExecutor.insertFileContent(fileInfo.getFileId(), fileContent);
	}
}