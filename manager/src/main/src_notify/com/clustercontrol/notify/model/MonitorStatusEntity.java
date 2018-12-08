package com.clustercontrol.notify.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;



/**
 * The persistent class for the cc_monitor_status database table.
 * 
 */
@Entity
@Table(name="cc_monitor_status", schema="setting")
@Cacheable(true)
public class MonitorStatusEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private MonitorStatusEntityPK id;
	private Long counter;
	private Timestamp lastUpdate;
	private Integer priority;

	@Deprecated
	public MonitorStatusEntity() {
	}

	public MonitorStatusEntity(MonitorStatusEntityPK pk) {
		this.setId(pk);
	}

	public MonitorStatusEntity(String facilityId, String pluginId, String monitorId, String subKey) {
		this(new MonitorStatusEntityPK(facilityId, pluginId, monitorId, subKey));
	}


	@EmbeddedId
	public MonitorStatusEntityPK getId() {
		return this.id;
	}

	public void setId(MonitorStatusEntityPK id) {
		this.id = id;
	}


	public Long getCounter() {
		return this.counter;
	}

	public void setCounter(Long counter) {
		this.counter = counter;
	}


	@Column(name="last_update")
	public Timestamp getLastUpdate() {
		return this.lastUpdate;
	}

	public void setLastUpdate(Timestamp lastUpdate) {
		this.lastUpdate = lastUpdate;
	}


	public Integer getPriority() {
		return this.priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public String toString() {
		return String.format("%s [id = %s, counter = %d, lastUpdate = %s, priority = %d]", 
				MonitorStatusEntity.class.getSimpleName(), id, counter, lastUpdate, priority);
	};
	
	public MonitorStatusEntity clone() {
		MonitorStatusEntity entity = new MonitorStatusEntity(id.getFacilityId(), id.getPluginId(), id.getMonitorId(), id.getSubKey());
		entity.counter = this.counter;
		entity.lastUpdate = this.lastUpdate;
		entity.priority = this.priority;
		return entity;
	}
}