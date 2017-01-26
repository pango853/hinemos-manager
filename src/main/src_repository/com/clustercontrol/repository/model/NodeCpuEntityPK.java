package com.clustercontrol.repository.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_cfg_node_cpu database table.
 * 
 */
@Embeddable
public class NodeCpuEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String facilityId;
	private Integer deviceIndex;
	private String deviceType;
	private String deviceName;

	public NodeCpuEntityPK() {
	}

	public NodeCpuEntityPK(String facilityId,
			Integer deviceIndex,
			String deviceType,
			String deviceName) {
		this.setFacilityId(facilityId);
		this.setDeviceIndex(deviceIndex);
		this.setDeviceType(deviceType);
		this.setDeviceName(deviceName);
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="device_index")
	public Integer getDeviceIndex() {
		return this.deviceIndex;
	}
	public void setDeviceIndex(Integer deviceIndex) {
		this.deviceIndex = deviceIndex;
	}

	@Column(name="device_type")
	public String getDeviceType() {
		return this.deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	@Column(name="device_name")
	public String getDeviceName() {
		return this.deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof NodeCpuEntityPK)) {
			return false;
		}
		NodeCpuEntityPK castOther = (NodeCpuEntityPK)other;
		return
				this.facilityId.equals(castOther.facilityId)
				&& this.deviceIndex.equals(castOther.deviceIndex)
				&& this.deviceType.equals(castOther.deviceType)
				&& this.deviceName.equals(castOther.deviceName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.facilityId.hashCode();
		hash = hash * prime + this.deviceIndex.hashCode();
		hash = hash * prime + this.deviceType.hashCode();
		hash = hash * prime + this.deviceName.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"facilityId",
				"deviceIndex",
				"deviceType",
				"deviceName"
		};
		String[] values = {
				this.facilityId,
				this.deviceIndex.toString(),
				this.deviceType,
				this.deviceName
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}