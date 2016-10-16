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
package com.clustercontrol.snmptrap.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Embeddable;


/**
 * The primary key class for the cc_monitor_trap_value_info database table.
 * 
 */
@Embeddable
public class MonitorTrapValueInfoEntityPK implements Serializable {
	private static final long serialVersionUID = 1L;

	private String monitorId;
	private String mib;
	private String trapOid;
	private Integer genericId;
	private Integer specificId;


	public MonitorTrapValueInfoEntityPK() {
	}

	public MonitorTrapValueInfoEntityPK(String monitorId, String mib, String trapOid, Integer genericId, Integer specificId) {
		this.setMonitorId(monitorId);
		this.setMib(mib);
		this.setTrapOid(trapOid);
		this.setGenericId(genericId);
		this.setSpecificId(specificId);
	}

	@Column(name="monitor_id")
	public String getMonitorId() {
		return monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	@Column(name="mib")
	public String getMib() {
		return mib;
	}
	public void setMib(String mib) {
		this.mib = mib;
	}

	@Column(name="trap_oid")
	public String getTrapOid() {
		return trapOid;
	}
	public void setTrapOid(String trapOid) {
		this.trapOid = trapOid;
	}

	@Column(name="generic_id")
	public Integer getGenericId() {
		return genericId;
	}
	public void setGenericId(Integer genericId) {
		this.genericId = genericId;
	}

	@Column(name="specific_id")
	public Integer getSpecificId() {
		return specificId;
	}
	public void setSpecificId(Integer specificId) {
		this.specificId = specificId;
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MonitorTrapValueInfoEntityPK)) {
			return false;
		}
		MonitorTrapValueInfoEntityPK castOther = (MonitorTrapValueInfoEntityPK)other;
		return
				this.monitorId.equals(castOther.monitorId)
				&& this.mib.equals(castOther.mib)
				&& this.trapOid.equals(castOther.trapOid)
				&& this.genericId.equals(castOther.genericId)
				&& this.specificId.equals(castOther.specificId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.monitorId.hashCode();
		hash = hash * prime + this.mib.hashCode();
		hash = hash * prime + this.trapOid.hashCode();
		hash = hash * prime + this.genericId.hashCode();
		hash = hash * prime + this.specificId.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"monitorId",
				"mib",
				"trapOid",
				"genericId",
				"specificId"
		};
		Object[] values = {
				this.monitorId,
				this.mib,
				this.trapOid,
				this.genericId,
				this.specificId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}
