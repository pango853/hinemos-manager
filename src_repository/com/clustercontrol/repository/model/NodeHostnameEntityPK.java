package com.clustercontrol.repository.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_cfg_node_hostname database table.
 * 
 */
@Embeddable
public class NodeHostnameEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String facilityId;
	private String hostname;

	public NodeHostnameEntityPK() {
	}

	public NodeHostnameEntityPK(String facilityId, String hostname) {
		this.setFacilityId(facilityId);
		this.setHostname(hostname);
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getHostname() {
		return this.hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof NodeHostnameEntityPK)) {
			return false;
		}
		NodeHostnameEntityPK castOther = (NodeHostnameEntityPK)other;
		return
				this.facilityId.equals(castOther.facilityId)
				&& this.hostname.equals(castOther.hostname);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.facilityId.hashCode();
		hash = hash * prime + this.hostname.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"facilityId",
				"hostname"
		};
		String[] values = {
				this.facilityId,
				this.hostname
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}