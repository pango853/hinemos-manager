package com.clustercontrol.repository.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_cfg_node_variable database table.
 * 
 */
@Embeddable
public class NodeVariableEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String facilityId;
	private String nodeVariableName;

	public NodeVariableEntityPK() {
	}

	public NodeVariableEntityPK(String facilityId, String nodeVariableName) {
		this.setFacilityId(facilityId);
		this.setNodeVariableName(nodeVariableName);
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="node_variable_name")
	public String getNodeVariableName() {
		return this.nodeVariableName;
	}
	public void setNodeVariableName(String nodeVariableName) {
		this.nodeVariableName = nodeVariableName;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof NodeVariableEntityPK)) {
			return false;
		}
		NodeVariableEntityPK castOther = (NodeVariableEntityPK)other;
		return
				this.facilityId.equals(castOther.facilityId)
				&& this.nodeVariableName.equals(castOther.nodeVariableName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.facilityId.hashCode();
		hash = hash * prime + this.nodeVariableName.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"facilityId",
				"nodeVariableName"
		};
		String[] values = {
				this.facilityId,
				this.nodeVariableName
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}