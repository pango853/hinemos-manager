package com.clustercontrol.hinemosagent.bean;

import com.clustercontrol.notify.bean.OutputBasicInfo;


public class AgentOutputBasicInfo {
	OutputBasicInfo outputBasicInfo = null;
	AgentInfo agentInfo = null;
	public OutputBasicInfo getOutputBasicInfo() {
		return outputBasicInfo;
	}
	public void setOutputBasicInfo(OutputBasicInfo outputBasicInfo) {
		this.outputBasicInfo = outputBasicInfo;
	}
	public AgentInfo getAgentInfo() {
		return agentInfo;
	}
	public void setAgentInfo(AgentInfo agentInfo) {
		this.agentInfo = agentInfo;
	}
}
