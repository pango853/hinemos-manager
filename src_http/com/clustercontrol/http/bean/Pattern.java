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
public class Pattern implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8592793299763089891L;

	private String pattern;
	private String description;
	private Integer processType;
	private boolean caseSensitivityFlg;
	private boolean validFlg;


	public Pattern() {
	}


	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}


	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public Integer getProcessType() {
		return processType;
	}

	public void setProcessType(Integer processType) {
		this.processType = processType;
	}


	public boolean getCaseSensitivityFlg() {
		return caseSensitivityFlg;
	}

	public void setCaseSensitivityFlg(boolean caseSensitivityFlg) {
		this.caseSensitivityFlg = caseSensitivityFlg;
	}


	public boolean getValidFlg() {
		return validFlg;
	}

	public void setValidFlg(boolean validFlg) {
		this.validFlg = validFlg;
	}
}
