package com.clustercontrol.monitor.plugin.factory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.plugin.util.PluginCheckInfoUtil;
import com.clustercontrol.monitor.run.factory.AddMonitor;
import com.clustercontrol.monitor.run.factory.ModifyMonitorStringValueType;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

public class ModifyMonitorPluginString extends ModifyMonitorStringValueType {

	@Override
	protected TriggerType getTriggerType() {
		return TriggerType.SIMPLE;
	}

	@Override
	protected int getDelayTime() {
		return AddMonitor.getDelayTimeBasic(m_monitorInfo);
	}

	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole,
			HinemosUnknown {
		return (new PluginCheckInfoUtil()).modifyCheckInfo(m_monitorInfo.getPluginCheckInfo());
	}

}
