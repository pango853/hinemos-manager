package com.clustercontrol.infra.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.Table;

import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.infra.bean.CommandModuleInfo;
import com.clustercontrol.infra.bean.InfraManagementInfo;

@Entity
@Table(name="cc_infra_command_module_info", schema="setting")
@Inheritance
@DiscriminatorValue(CommandModuleInfoEntity.typeName)
@Cacheable(true)
public class CommandModuleInfoEntity extends InfraModuleInfoEntity<CommandModuleInfo> {
	public static final String typeName = "ExecModule";
	
	private int accessMethodType;
	private String execCommand;
	private String checkCommand;
	
	public CommandModuleInfoEntity() {
	}
	
	public CommandModuleInfoEntity(InfraManagementInfoEntity parent, String moduleId) {
		super(parent, moduleId);
	}
	
	@Column(name="access_method_type")
	public int getAccessMethodType() {
		return accessMethodType;
	}
	public void setAccessMethodType(int accessMethodType) {
		this.accessMethodType = accessMethodType;
	}

	@Column(name="exec_command")
	public String getExecCommand() {
		return execCommand;
	}
	public void setExecCommand(String execCommand) {
		this.execCommand = execCommand;
	}

	@Column(name="check_command")
	public String getCheckCommand() {
		return checkCommand;
	}
	public void setCheckCommand(String checkCommand) {
		this.checkCommand = checkCommand;
	}
	
	@Override
	public String getModuleTypeName() {
		return typeName;
	}

	@Override
	protected Class<CommandModuleInfo> getWebEntityClass() {
		return CommandModuleInfo.class;
	}

	@Override
	protected void overwriteWebEntity(InfraManagementInfo management, CommandModuleInfo module) {
		module.setExecCommand(getExecCommand());
		module.setCheckCommand(getCheckCommand());
		module.setPrecheckFlg(ValidConstant.typeToBoolean(getPrecheckFlg()));
		module.setAccessMethodType(getAccessMethodType());
	}
}
