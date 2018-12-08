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

package com.clustercontrol.infra.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.infra.bean.AccessMethodConstant;
import com.clustercontrol.infra.model.CommandModuleInfoEntity;
import com.clustercontrol.infra.model.InfraFileEntity;
import com.clustercontrol.infra.model.InfraManagementInfoEntity;
import com.clustercontrol.infra.util.JschUtil;
import com.clustercontrol.infra.util.QueryUtil;
import com.clustercontrol.infra.util.WinRMUtil;
import com.clustercontrol.repository.bean.NodeInfo;
import com.clustercontrol.repository.util.RepositoryUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.StringBinder;

@XmlType(namespace = "http://infra.ws.clustercontrol.com")
public class CommandModuleInfo extends InfraModuleInfo<CommandModuleInfoEntity> {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( CommandModuleInfo.class );
	
	public static final int MESSAGE_SIZE = 1024;

	private int accessMethodType = -1;
	private String execCommand;
	private String checkCommand;
	
	public int getAccessMethodType() {
		return accessMethodType;
	}
	public void setAccessMethodType(int accessMethodType) {
		this.accessMethodType = accessMethodType;
	}

	public String getExecCommand() {
		return execCommand;
	}
	public void setExecCommand(String execCommand) {
		this.execCommand = execCommand;
	}

	public String getCheckCommand() {
		return checkCommand;
	}
	public void setCheckCommand(String checkCommand) {
		this.checkCommand = checkCommand;
	}

	@Override
	public ModuleNodeResult run(InfraManagementInfo management, NodeInfo node, AccessInfo access, String sessionId, String account) throws HinemosUnknown, InvalidUserPass {
		m_log.debug(String.format(String.format("%s %s, manegementId = %s, moduleId = %s", "start", "run", management.getManagementId(), getModuleId())));

		ModuleNodeResult result =  execCommand(node, getExecCommand(), access);
		
		m_log.debug(String.format(String.format("%s %s, manegementId = %s, moduleId = %s", "end", "run", management.getManagementId(), getModuleId())));
		
		return result;
	}

	@Override
	public ModuleNodeResult check(InfraManagementInfo management, NodeInfo node, AccessInfo access, String sessionId, boolean check) throws HinemosUnknown, InvalidUserPass {
		return execCommand(node, getCheckCommand(), access);
	}
	
	private ModuleNodeResult execCommand(NodeInfo node, String command, AccessInfo access) {
		String facilityId = node.getFacilityId();
		String address = node.getAvailableIpAddress();
		
		List<InfraFileEntity> fileList = QueryUtil.getAllInfraFile();

		// コマンド文字列の置換
		String bindCommand;
		try {
			HashMap<String, String> map = new HashMap<String, String>();
			
			// ノード変数
			Map<String, String> variable = RepositoryUtil.createNodeParameter(node);
			map.putAll(variable);
			
			// ファイルID			
			for (InfraFileEntity file : fileList) {
				String key = "FILE:" + file.getFileId();
				String value = file.getFileName();
				map.put(key, value);
				m_log.debug("execCommand()  >>> param.put = : " + key  + "  value = " +  value);
			}
			StringBinder binder = new StringBinder(map);
			bindCommand = binder.bindParam(command);
		} catch (Exception e) {
			m_log.warn("execCommand() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			bindCommand = command;
		}
		
		
		ModuleNodeResult ret = null;
		switch (getAccessMethodType()) {
		case AccessMethodConstant.TYPE_SSH:
			ret = JschUtil.execCommand(access.getSshUser(), access.getSshPassword(), address, access.getSshPort(),
					access.getSshTimeout(), bindCommand, MESSAGE_SIZE, access.getSshPrivateKeyFilepath(), access.getSshPrivateKeyPassphrase());
			break;
		case AccessMethodConstant.TYPE_WINRM:
			ret = WinRMUtil.execCommand(access.getWinRmUser(), access.getWinRmPassword(), address, access.getWinRmPort(),
					node.getWinrmProtocol(), bindCommand, MESSAGE_SIZE);
			break;
		default:
			String msg = String.format("AccessMethodType is invalid. value = %d", getAccessMethodType());
			m_log.warn("execCommand : " + msg);
			ret = new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, msg);
		}
		ret.setFacilityId(facilityId);
		return ret;
	}

	@Override
	protected void validateSub() throws InvalidSetting, InvalidRole {
		// execCommand
		CommonValidator.validateString(Messages.getString("infra.module.exec.command"), getExecCommand(), false, 0, 1024);
		
		// checkCommand
		CommonValidator.validateString(Messages.getString("infra.module.check.command"), getCheckCommand(), false, 0, 1024);
		
		// accessMethodType
		boolean match = false;
		for (int type: AccessMethodConstant.getTypeList()) {
			if (type == getAccessMethodType()) {
				match = true;
				break;
			}
		}
		if (!match) {
			InvalidSetting e = new InvalidSetting("AccessMethodType must be SSH(0) / WinRM(1).");
			m_log.info("validateSub() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	@Override
	protected Class<CommandModuleInfoEntity> getEntityClass() {
		return CommandModuleInfoEntity.class;
	}
	@Override
	protected void overwriteCounterEntity(InfraManagementInfoEntity management, CommandModuleInfoEntity module, HinemosEntityManager em) {
		module.setAccessMethodType(getAccessMethodType());
		module.setExecCommand(getExecCommand());
		module.setCheckCommand(getCheckCommand());
	}
	@Override
	public boolean canPrecheck(InfraManagementInfo management, NodeInfo node, AccessInfo access, String sessionId) throws HinemosUnknown, InvalidUserPass {
		return this.isPrecheckFlg();
	}

	@Override
	public String getModuleTypeName() {
		return CommandModuleInfoEntity.typeName;
	}
	
	@Override
	public void beforeRun(String sessionId) {
		//Do Nothing
	}
	
	@Override
	public void afterRun(String sessionId) {
		//Do Nothing
	}
}