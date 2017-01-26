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
@Table(name="cc_monitor_http_scenario_pattern_info", schema="setting")
//@Cacheable(true)
public class MonitorHttpScenarioPatternInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	private MonitorHttpScenarioPatternInfoEntityPK id;
    private String pattern;
    private String description;
    private Integer processType;
    private Integer caseSensitivityFlg;
    private Integer validFlg;
    
    private MonitorHttpScenarioPageInfoEntity monitorHttpScenarioPageInfoEntity;


	@Deprecated
	public MonitorHttpScenarioPatternInfoEntity() {
	}

	public MonitorHttpScenarioPatternInfoEntity(MonitorHttpScenarioPageInfoEntity pageEntry, MonitorHttpScenarioPatternInfoEntityPK pk) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToMonitorHttpScenarioPageInfoEntity(pageEntry);
	}

	public MonitorHttpScenarioPatternInfoEntity(MonitorHttpScenarioPageInfoEntity pageEntry, Integer patternOrderNo) {
		this(pageEntry, new MonitorHttpScenarioPatternInfoEntityPK(pageEntry.getId().getMonitorId(), pageEntry.getId().getPageOrderNo(), patternOrderNo));
	}


	@EmbeddedId
	public MonitorHttpScenarioPatternInfoEntityPK getId() {
		return this.id;
	}

	public void setId(MonitorHttpScenarioPatternInfoEntityPK id) {
		this.id = id;
	}


	@Column(name="pattern")
	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}


	@Column(name="description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Column(name="process_type")
	public Integer getProcessType() {
		return processType;
	}

	public void setProcessType(Integer processType) {
		this.processType = processType;
	}


	@Column(name="case_sensitivity_flg")
	public Integer getCaseSensitivityFlg() {
		return caseSensitivityFlg;
	}

	public void setCaseSensitivityFlg(Integer caseSensitivityFlg) {
		this.caseSensitivityFlg = caseSensitivityFlg;
	}


	@Column(name="valid_flg")
	public Integer getValidFlg() {
		return validFlg;
	}

	public void setValidFlg(Integer validFlg) {
		this.validFlg = validFlg;
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
			List<MonitorHttpScenarioPatternInfoEntity> list = monitorHttpScenarioPageInfoEntity.getMonitorHttpScenarioPatternInfoEntities();
			if (list == null) {
				list = new ArrayList<MonitorHttpScenarioPatternInfoEntity>();
			} else {
				for (MonitorHttpScenarioPatternInfoEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorHttpScenarioPageInfoEntity.setMonitorHttpScenarioPatternInfoEntities(list);
		}
	}

}
