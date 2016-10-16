package com.clustercontrol.jmx.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;



/**
 * The persistent class for the cc_jmx_type_mst database table.
 * 
 */
@Entity
@Table(name="cc_monitor_jmx_mst", schema="setting")
@Cacheable(true)
public class MonitorJmxMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id;
	private String objectName;
	private String attributeName;
	private String keys;
	private String name;
	private String measure;

	@Deprecated
	public MonitorJmxMstEntity() {
	}

	public MonitorJmxMstEntity(String id, String objectName, String attributeName, String keys, String name, String measure) {
		this.setId(id);
		this.setObjectName(objectName);
		this.setAttributeName(attributeName);
		this.setKeys(keys);
		this.setName(name);
		this.setMeasure(measure);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}

	@Id
	@Column(name="master_id")
	public String getId() {
		return this.id;
	}
	public void setId(String id) {
		this.id = id;
	}

	@Column(name="object_name")
	public String getObjectName() {
		return this.objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	@Column(name="attribute_name")
	public String getAttributeName() {
		return this.attributeName;
	}
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	@Column(name="keys")
	public String getKeys() {
		return this.keys;
	}
	public void setKeys(String keys) {
		this.keys = keys;
	}

	@Column(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Column(name="measure")
	public String getMeasure() {
		return measure;
	}
	public void setMeasure(String measure) {
		this.measure = measure;
	}
}