package com.clustercontrol.monitor.plugin.factory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.plugin.util.PluginCheckInfoUtil;
import com.clustercontrol.monitor.run.factory.AddMonitor;
import com.clustercontrol.monitor.run.factory.AddMonitorTruthValueType;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

public class AddMonitorPluginTruth extends AddMonitorTruthValueType {

	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, HinemosUnknown,
			InvalidRole {
		return (new PluginCheckInfoUtil()).addCheckInfo(m_monitorInfo.getPluginCheckInfo());
	}

	@Override
	protected TriggerType getTriggerType() {
		return TriggerType.SIMPLE;
	}

	@Override
	protected int getDelayTime() {
		return AddMonitor.getDelayTimeBasic(m_monitorInfo);
	}

}
