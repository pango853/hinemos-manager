package com.clustercontrol.performance.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://collector.ws.clustercontrol.com")
public class CollectorItemParentInfo implements Serializable {

	private static final long serialVersionUID = 6095022110402752294L;


	private String collectorId;
	private String parentItemCode;
	private String itemCode;
	private String displayName;
	public String getCollectorId() {
		return collectorId;
	}
	public void setCollectorId(String collectorId) {
		this.collectorId = collectorId;
	}
	public String getParentItemCode() {
		return parentItemCode;
	}
	public void setParentItemCode(String parentItemCode) {
		this.parentItemCode = parentItemCode;
	}
	public String getItemCode() {
		return itemCode;
	}
	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

}
