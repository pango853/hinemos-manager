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
package com.clustercontrol.infra.util;

import org.apache.log4j.Logger;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.infra.bean.InfraFileInfo;
import com.clustercontrol.infra.bean.InfraManagementInfo;
import com.clustercontrol.infra.bean.InfraModuleInfo;
import com.clustercontrol.notify.bean.NotifyRelationInfo;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.util.Messages;

/**
 * 環境構築機能情報をバリデーションする。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraManagementValidator {
	private static Logger m_log = Logger.getLogger(InfraManagementValidator.class);

	
	/**
	 * 環境構築機能情報のvalidate
	 * 
	 * @param jobSchedule
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public static void validateInfraManagementInfo (InfraManagementInfo infraManagementInfo) throws InvalidSetting, InvalidRole {
		if(infraManagementInfo == null){
			InvalidSetting e = new InvalidSetting("InfraManagementInfo is not defined.");
			m_log.info("validate() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		//infraManagementInfo.validate();

			// managementId
			CommonValidator.validateId(Messages.getString("infra.management.id"), infraManagementInfo.getManagementId(), 64);
			
			// name
			CommonValidator.validateString(Messages.getString("infra.management.name"), infraManagementInfo.getName(), true, 1, 64);
			
			// description
			CommonValidator.validateString(Messages.getString("infra.management.description"), infraManagementInfo.getDescription(), false, 0, 256);
			
			// ownerRoleId
			CommonValidator.validateOwnerRoleId(infraManagementInfo.getOwnerRoleId(), true, infraManagementInfo.getManagementId(), HinemosModuleConstant.INFRA);

			// facilityId
			if(infraManagementInfo.getFacilityId() == null || "".equals(infraManagementInfo.getFacilityId())){
				InvalidSetting e = new InvalidSetting(Messages.getString("message.hinemos.3"));
				m_log.info("validate() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}else{
				try {
					FacilityTreeCache.validateFacilityId(infraManagementInfo.getFacilityId(), infraManagementInfo.getOwnerRoleId(), false);
				} catch (FacilityNotFound e) {
					throw new InvalidSetting(e.getMessage(), e);
				}
			}
			
			// scope
			CommonValidator.validateString(Messages.getString("infra.management.scope"), infraManagementInfo.getScope(), true, 1, 64);
			
			// validFlg : not implemented
			// notifyGroupID : not implemented			
			// startPriority : not implemented
			// normalPriorityRun : not implemented
			// abnormalPriorityRun : not implemented
			// normalPriorityCheck : not implemented
			// abnormalPriorityCheck : not implemented
			
			// notifyId
			if(infraManagementInfo.getNotifyRelationList() != null){
				for(NotifyRelationInfo notifyRelationInfo: infraManagementInfo.getNotifyRelationList()){
					CommonValidator.validateNotifyId(notifyRelationInfo.getNotifyId(), true, infraManagementInfo.getOwnerRoleId());
				}
			}
			
			for (InfraModuleInfo<?> infraModuleInfo: infraManagementInfo.getModuleList()) {
				infraModuleInfo.validate();
			}

	}
	
	public static void validateInfraFileInfo (InfraFileInfo infraFileInfo) throws InvalidSetting, InvalidRole {
		if(infraFileInfo == null){
			InvalidSetting e = new InvalidSetting("InfraFlieInfo is not defined.");
			m_log.info("validate() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		//protected byte[] fileContent;
		// fileId
		CommonValidator.validateId(Messages.getString("infra.file.id"), infraFileInfo.getFileId(), 256);
		
		//fileName
		CommonValidator.validateString(Messages.getString("infra.m.name"), infraFileInfo.getFileName(), true, 1, 256);
		
		//protected String ownerRoleId;
		CommonValidator.validateOwnerRoleId(infraFileInfo.getOwnerRoleId(), true, infraFileInfo.getFileId(), HinemosModuleConstant.INFRA);

		//uploadDatetime : not implemented
		//uploadUserId : not implemented
		
	}
}
