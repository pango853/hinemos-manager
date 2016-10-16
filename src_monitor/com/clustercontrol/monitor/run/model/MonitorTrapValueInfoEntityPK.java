package com.clustercontrol.monitor.run.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_monitor_trap_value_info database table.
 * 
 */
@Embeddable
public class MonitorTrapValueInfoEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private String mib;
	private String trapOid;
	private Integer genericId;
	private Integer specificId;
	private String uei;

	public MonitorTrapValueInfoEntityPK() {
	}

	public MonitorTrapValueInfoEntityPK(
			String monitorId,
			String mib,
			String trapOid,
			Integer genericId,
			Integer specificId,
			String uei) {
		this.setMonitorId(monitorId);
		this.setMib(mib);
		this.setTrapOid(trapOid);
		this.setGenericId(genericId);
		this.setSpecificId(specificId);
		this.setUei(uei);
	}

	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getMib() {
		return this.mib;
	}
	public void setMib(String mib) {
		this.mib = mib;
	}

	@Column(name="trap_oid")
	public String getTrapOid() {
		return this.trapOid;
	}
	public void setTrapOid(String trapOid) {
		this.trapOid = trapOid;
	}

	@Column(name="generic_id")
	public Integer getGenericId() {
		return this.genericId;
	}
	public void setGenericId(Integer genericId) {
		this.genericId = genericId;
	}

	@Column(name="specific_id")
	public Integer getSpecificId() {
		return this.specificId;
	}
	public void setSpecificId(Integer specificId) {
		this.specificId = specificId;
	}

	public String getUei() {
		return this.uei;
	}
	public void setUei(String uei) {
		this.uei = uei;
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
				&& this.specificId.equals(castOther.specificId)
				&& this.uei.equals(castOther.uei);
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
		hash = hash * prime + this.uei.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"monitorId",
				"mib",
				"trapOid",
				"genericId",
				"specificId",
				"uei"
		};
		String[] values = {
				this.monitorId,
				this.mib,
				this.trapOid,
				this.genericId.toString(),
				this.specificId.toString(),
				this.uei
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}