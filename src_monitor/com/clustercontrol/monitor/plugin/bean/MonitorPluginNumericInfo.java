package com.clustercontrol.monitor.plugin.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * 
 *
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class MonitorPluginNumericInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String monitorId;
	private String key;
	private Double value;


	public MonitorPluginNumericInfo() {
		super();
	}

	public MonitorPluginNumericInfo(MonitorPluginNumericInfo otherData) {
		super();
		this.monitorId = otherData.getMonitorId();
		this.key = otherData.getKey();
		this.value = otherData.getValue();
	}


	public MonitorPluginNumericInfo(String monitorId, String key, Double value) {
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


	public Double getValue() {
		return value;
	}


	public void setValue(Double value) {
		this.value = value;
	}

}
