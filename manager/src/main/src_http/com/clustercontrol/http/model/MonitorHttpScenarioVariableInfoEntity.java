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

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;

@Entity
@Table(name="cc_monitor_http_scenario_variable_info", schema="setting")
//@Cacheable(true)
public class MonitorHttpScenarioVariableInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	private MonitorHttpScenarioVariableInfoEntityPK id;
	private String value;
	private Integer matchingWithResponseFlg;
	
	private MonitorHttpScenarioPageInfoEntity monitorHttpScenarioPageInfoEntity;

	@Deprecated
	public MonitorHttpScenarioVariableInfoEntity() {
	}

	public MonitorHttpScenarioVariableInfoEntity(MonitorHttpScenarioPageInfoEntity pageEntity, MonitorHttpScenarioVariableInfoEntityPK pk) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToMonitorHttpScenarioPageInfoEntity(pageEntity);
	}

	public MonitorHttpScenarioVariableInfoEntity(MonitorHttpScenarioPageInfoEntity pageEntity, String name) {
		this(pageEntity, new MonitorHttpScenarioVariableInfoEntityPK(pageEntity.getId().getMonitorId(), pageEntity.getId().getPageOrderNo(), name));
	}

	@EmbeddedId
	public MonitorHttpScenarioVariableInfoEntityPK getId() {
		return this.id;
	}

	public void setId(MonitorHttpScenarioVariableInfoEntityPK id) {
		this.id = id;
	}


	@Column(name="value")
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}


	@Column(name="matching_with_response_flg")
	public Integer getMatchingWithResponseFlg() {
		return matchingWithResponseFlg;
	}

	public void setMatchingWithResponseFlg(Integer matchingWithResponseFlg) {
		this.matchingWithResponseFlg = matchingWithResponseFlg;
	}
	
	//bi-directional many-to-one association to MonitorHttpScenarioPageInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="monitor_id", referencedColumnName="monitor_id", insertable=false, updatable=false),
		@JoinColumn(name="page_order_no", referencedColumnName="page_order_no", insertable=false, updatable=false)
	})
	public MonitorHttpScenarioPageInfoEntity getMonitorHttpScenarioPageInfoEntity() {
		return this.monitorHttpScenarioPageInfoEntity;
	}
	
	@Deprecated
	public void setMonitorHttpScenarioPageInfoEntity(MonitorHttpScenarioPageInfoEntity monitorHttpScenarioPageInfoEntity) {
		this.monitorHttpScenarioPageInfoEntity = monitorHttpScenarioPageInfoEntity;
	}
	
	public void relateToMonitorHttpScenarioPageInfoEntity(MonitorHttpScenarioPageInfoEntity monitorHttpScenarioPageInfoEntity) {
		this.setMonitorHttpScenarioPageInfoEntity(monitorHttpScenarioPageInfoEntity);
		if (monitorHttpScenarioPageInfoEntity != null) {
			List<MonitorHttpScenarioVariableInfoEntity> list = monitorHttpScenarioPageInfoEntity.getMonitorHttpScenarioVariableInfoEntities();
			if (list == null) {
				list = new ArrayList<MonitorHttpScenarioVariableInfoEntity>();
			} else {
				for (MonitorHttpScenarioVariableInfoEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorHttpScenarioPageInfoEntity.setMonitorHttpScenarioVariableInfoEntities(list);
		}
	}
}
