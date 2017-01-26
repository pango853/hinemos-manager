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
package com.clustercontrol.http.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * 
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class Variable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5597499421764256782L;

	private String name;
	private String value;
	private boolean matchingWithResponseFlg;

	public Variable() {
	}


	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}


	public boolean getMatchingWithResponseFlg() {
		return matchingWithResponseFlg;
	}

	public void setMatchingWithResponseFlg(boolean matchingWithResponseFlg) {
		this.matchingWithResponseFlg = matchingWithResponseFlg;
	}
}
