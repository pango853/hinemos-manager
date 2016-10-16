package com.clustercontrol.snmp.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_monitor_snmp_value database table.
 * 
 */
@Embeddable
public class MonitorSnmpValueEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private String facilityId;

	public MonitorSnmpValueEntityPK() {
	}

	public MonitorSnmpValueEntityPK(String monitorId, String facilityId) {
		this.setMonitorId(monitorId);
		this.setFacilityId(facilityId);
	}

	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MonitorSnmpValueEntityPK)) {
			return false;
		}
		MonitorSnmpValueEntityPK castOther = (MonitorSnmpValueEntityPK)other;
		return
				this.monitorId.equals(castOther.monitorId)
				&& this.facilityId.equals(castOther.facilityId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.monitorId.hashCode();
		hash = hash * prime + this.facilityId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"monitorId",
				"facilityId"
		};
		String[] values = {
				this.monitorId,
				this.facilityId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}