/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.http.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;

@Entity
@Table(name="cc_monitor_http_scenario_page_info", schema="setting")
@Cacheable(true)
public class MonitorHttpScenarioPageInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	private MonitorHttpScenarioPageInfoEntityPK id;
	private String url;
	private String description;
	private String statusCode;
	private String post;
	private Integer priority;
	private String message;
	
	private MonitorHttpScenarioInfoEntity monitorHttpScenarioInfoEntity;

	private List<MonitorHttpScenarioPatternInfoEntity> monitorHttpScenarioPatternInfoEntities;

	private List<MonitorHttpScenarioVariableInfoEntity> monitorHttpScenarioVariableInfoEntities;

	@Deprecated
	public MonitorHttpScenarioPageInfoEntity() {
	}

	public MonitorHttpScenarioPageInfoEntity(MonitorHttpScenarioInfoEntity monitorInfoEntity, MonitorHttpScenarioPageInfoEntityPK pk) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToMonitorHttpScenarioInfoEntity(monitorInfoEntity);
	}

	public MonitorHttpScenarioPageInfoEntity(MonitorHttpScenarioInfoEntity monitorInfoEntity, Integer pageOrderNo) {
		this(monitorInfoEntity, new MonitorHttpScenarioPageInfoEntityPK(monitorInfoEntity.getMonitorId(), pageOrderNo));
	}

	@EmbeddedId
	public MonitorHttpScenarioPageInfoEntityPK getId() {
		return this.id;
	}

	public void setId(MonitorHttpScenarioPageInfoEntityPK id) {
		this.id = id;
	}


	@Column(name="url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}


	@Column(name="description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Column(name="status_code")
	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}


	@Column(name="post")
	public String getPost() {
		return post;
	}

	public void setPost(String post) {
		this.post = post;
	}


	@Column(name="priority")
	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}


	@Column(name="message")
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	//bi-directional many-to-one association to MonitorHttpScenarioInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="monitor_id", insertable=false, updatable=false)
	public MonitorHttpScenarioInfoEntity getMonitorHttpScenarioInfoEntity() {
		return this.monitorHttpScenarioInfoEntity;
	}
	
	@Deprecated
	public void setMonitorHttpScenarioInfoEntity(MonitorHttpScenarioInfoEntity monitorHttpScenarioInfoEntity) {
		this.monitorHttpScenarioInfoEntity = monitorHttpScenarioInfoEntity;
	}


	public void relateToMonitorHttpScenarioInfoEntity(MonitorHttpScenarioInfoEntity monitorInfoEntity) {
		this.setMonitorHttpScenarioInfoEntity(monitorInfoEntity);
		if (monitorInfoEntity != null) {
			List<MonitorHttpScenarioPageInfoEntity> list = monitorInfoEntity.getMonitorHttpScenarioPageInfoEntities();
			if (list == null) {
				list = new ArrayList<MonitorHttpScenarioPageInfoEntity>();
			} else {
				for (MonitorHttpScenarioPageInfoEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorInfoEntity.setMonitorHttpScenarioPageInfoEntities(list);
		}
	}

	@OneToMany(mappedBy="monitorHttpScenarioPageInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public List<MonitorHttpScenarioPatternInfoEntity> getMonitorHttpScenarioPatternInfoEntities() {
		return this.monitorHttpScenarioPatternInfoEntities;
	}

	public void setMonitorHttpScenarioPatternInfoEntities(List<MonitorHttpScenarioPatternInfoEntity> monitorHttpScenarioPatternInfoEntities) {
		this.monitorHttpScenarioPatternInfoEntities = monitorHttpScenarioPatternInfoEntities;
	}


	@OneToMany(mappedBy="monitorHttpScenarioPageInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public List<MonitorHttpScenarioVariableInfoEntity> getMonitorHttpScenarioVariableInfoEntities() {
		return this.monitorHttpScenarioVariableInfoEntities;
	}

	public void setMonitorHttpScenarioVariableInfoEntities(List<MonitorHttpScenarioVariableInfoEntity> monitorHttpScenarioVariableInfoEntities) {
		this.monitorHttpScenarioVariableInfoEntities = monitorHttpScenarioVariableInfoEntities;
	}
}
