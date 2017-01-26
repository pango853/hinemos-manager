package com.clustercontrol.monitor.plugin.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * 
 *
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class MonitorPluginStringInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String monitorId;
	private String key;
	private String value;


	public MonitorPluginStringInfo() {
		super();
	}

	public MonitorPluginStringInfo(MonitorPluginStringInfo otherData) {
		super();
		this.monitorId = otherData.getMonitorId();
		this.key = otherData.getKey();
		this.value = otherData.getValue();
	}


	public MonitorPluginStringInfo(String monitorId, String key, String value) {
		super();
		this.monitorId = monitorId;
		this.key = key;
		this.value = value;
	}


	public String getMonitorId() {
		return monitorId;
	}


	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}


	public String getKey() {
		return key;
	}


	public void setKey(String key) {
		this.key = key;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}

}
