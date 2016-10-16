package com.clustercontrol.performance.model;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * The persistent class for the cc_calculated_data database table.
 * 
 */
@Entity
@Table(name="cc_calculated_data", schema="log")
public class CalculatedDataEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private CalculatedDataEntityPK id;
	private double value;

	public CalculatedDataEntity() {
	}

	public CalculatedDataEntity(CalculatedDataEntityPK pk) {
		this.setId(pk);
	}

	public CalculatedDataEntity(String collectorid,
			String itemCode,
			String displayName,
			java.util.Date dateTime,
			String facilityid) {
		this(new CalculatedDataEntityPK(collectorid,
				itemCode,
				displayName,
				dateTime,
				facilityid));
	}


	@EmbeddedId
	public CalculatedDataEntityPK getId() {
		return this.id;
	}

	public void setId(CalculatedDataEntityPK id) {
		this.id = id;
	}


	public double getValue() {
		return this.value;
	}

	public void setValue(double value) {
		this.value = value;
	}

}