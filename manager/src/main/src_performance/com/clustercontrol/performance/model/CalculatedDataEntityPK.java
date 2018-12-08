package com.clustercontrol.performance.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_calculated_data database table.
 * 
 */
@Embeddable
public class CalculatedDataEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String collectorid;
	private String itemCode;
	private String displayName;
	private java.util.Date dateTime;
	private String facilityid;

	public CalculatedDataEntityPK() {
	}

	public CalculatedDataEntityPK(String collectorid,
			String itemCode,
			String displayName,
			java.util.Date dateTime,
			String facilityid) {
		this.setCollectorid(collectorid);
		this.setItemCode(itemCode);
		this.setDisplayName(displayName);
		this.setDateTime(dateTime);
		this.setFacilityid(facilityid);
	}

	public String getCollectorid() {
		return this.collectorid;
	}
	public void setCollectorid(String collectorid) {
		this.collectorid = collectorid;
	}

	@Column(name="item_code")
	public String getItemCode() {
		return this.itemCode;
	}
	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	@Column(name="display_name")
	public String getDisplayName() {
		return this.displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="date_time")
	public java.util.Date getDateTime() {
		return this.dateTime;
	}
	public void setDateTime(java.util.Date dateTime) {
		this.dateTime = dateTime;
	}

	public String getFacilityid() {
		return this.facilityid;
	}
	public void setFacilityid(String facilityid) {
		this.facilityid = facilityid;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CalculatedDataEntityPK)) {
			return false;
		}
		CalculatedDataEntityPK castOther = (CalculatedDataEntityPK)other;
		return
				this.collectorid.equals(castOther.collectorid)
				&& this.itemCode.equals(castOther.itemCode)
				&& this.displayName.equals(castOther.displayName)
				&& this.dateTime.equals(castOther.dateTime)
				&& this.facilityid.equals(castOther.facilityid);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.collectorid.hashCode();
		hash = hash * prime + this.itemCode.hashCode();
		hash = hash * prime + this.displayName.hashCode();
		hash = hash * prime + this.dateTime.hashCode();
		hash = hash * prime + this.facilityid.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"collectorid",
				"itemCode",
				"displayName",
				"dateTime",
				"facilityid"
		};
		String[] values = {
				this.collectorid,
				this.itemCode,
				this.displayName,
				this.dateTime.toString(),
				this.facilityid
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}