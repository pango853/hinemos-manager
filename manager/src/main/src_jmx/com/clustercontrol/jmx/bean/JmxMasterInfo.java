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

package com.clustercontrol.jmx.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * JMX 監視設定情報のBeanクラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class JmxMasterInfo implements Serializable {

	private static final long serialVersionUID = 5802612741291625279L;

	/**  */
	private String id;

	/**  */
	private String objectName;

	/**  */
	private String attributeName;

	/**  */
	private String keys;

	/**  */
	private String name;

	/**  */
	private String measure;


	public JmxMasterInfo() {
	}

	public JmxMasterInfo(String id, String objectName, String attributeName, String keys, String name, String measure) {
		this.id = id;
		this.objectName = objectName;
		this.attributeName = attributeName;
		this.keys = keys;
		this.name = name;
		this.measure = measure;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}


	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}


	public String getKeys() {
		return keys;
	}

	public void setKeys(String keys) {
		this.keys = keys;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getMeasure() {
		return measure;
	}

	public void setMeasure(String measure) {
		this.measure = measure;
	}
}
