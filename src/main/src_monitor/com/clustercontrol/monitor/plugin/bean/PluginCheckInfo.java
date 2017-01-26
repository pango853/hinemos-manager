package com.clustercontrol.monitor.plugin.bean;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.bean.MonitorCheckInfo;

/**
 * 
 *
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class PluginCheckInfo extends MonitorCheckInfo {

	private static final long serialVersionUID = 1L;

	private ArrayList<MonitorPluginStringInfo> monitorPluginStringInfoList;
	private ArrayList<MonitorPluginNumericInfo> monitorPluginNumericInfoList;

	public PluginCheckInfo() {
		super();
	}

	public ArrayList<MonitorPluginNumericInfo> getMonitorPluginNumericInfoList() {
		return monitorPluginNumericInfoList;
	}

	public void setMonitorPluginNumericInfoList(
			ArrayList<MonitorPluginNumericInfo> monitorPluginNumericInfoList) {
		this.monitorPluginNumericInfoList = monitorPluginNumericInfoList;
	}

	public ArrayList<MonitorPluginStringInfo> getMonitorPluginStringInfoList() {
		return monitorPluginStringInfoList;
	}

	public void setMonitorPluginStringInfoList(
			ArrayList<MonitorPluginStringInfo> monitorPluginStringInfoList) {
		this.monitorPluginStringInfoList = monitorPluginStringInfoList;
	}

}
