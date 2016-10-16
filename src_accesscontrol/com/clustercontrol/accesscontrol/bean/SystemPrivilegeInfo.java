/*

Copyright (C) since 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.accesscontrol.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;

/**
 * Hinemosのシステム権限情報を格納するクラス。<BR>
 * DTOクラスとしてマネージャ、クライアント間の通信で利用する。
 *
 */
@XmlType(namespace = "http://access.ws.clustercontrol.com")
public class SystemPrivilegeInfo implements Serializable {
	private static final long serialVersionUID = 402750470824251665L;

	private String systemFunction = null;
	private String systemPrivilege = null;

	public SystemPrivilegeInfo() {
	}

	public SystemPrivilegeInfo(String systemFunction, SystemPrivilegeMode systemPrivilege) {
		this.systemFunction = systemFunction;
		this.systemPrivilege = systemPrivilege.name();
	}

	public void setSystemFunction(String systemFunction) {
		this.systemFunction = systemFunction;
	}
	public String getSystemFunction() {
		return systemFunction;
	}
	public void setSystemPrivilege(String systemPrivilege) {
		this.systemPrivilege = systemPrivilege;
	}
	public String getSystemPrivilege() {
		return systemPrivilege;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SystemPrivilegeInfo)) {
			return false;
		}
		SystemPrivilegeInfo castOther = (SystemPrivilegeInfo)other;
		return
				this.systemFunction.equals(castOther.systemFunction)
				&& this.systemPrivilege.equals(castOther.systemPrivilege);
	}
}