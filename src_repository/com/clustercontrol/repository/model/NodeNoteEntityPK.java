package com.clustercontrol.repository.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_cfg_node_note database table.
 * 
 */
@Embeddable
public class NodeNoteEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String facilityId;
	private Integer noteId;

	public NodeNoteEntityPK() {
	}

	public NodeNoteEntityPK(String facilityId, Integer noteId) {
		this.setFacilityId(facilityId);
		this.setNoteId(noteId);
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="note_id")
	public Integer getNoteId() {
		return this.noteId;
	}
	public void setNoteId(Integer noteId) {
		this.noteId = noteId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof NodeNoteEntityPK)) {
			return false;
		}
		NodeNoteEntityPK castOther = (NodeNoteEntityPK)other;
		return
				this.facilityId.equals(castOther.facilityId)
				&& this.noteId.equals(castOther.noteId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.facilityId.hashCode();
		hash = hash * prime + this.noteId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"facilityId",
				"noteId"
		};
		String[] values = {
				this.facilityId,
				this.noteId.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}