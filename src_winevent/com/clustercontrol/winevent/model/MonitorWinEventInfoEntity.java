/*

 Copyright (C) 2013 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.winevent.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;


/**
 * The persistent class for the cc_monitor_winevent_info database table.
 * 
 */
@Entity
@Table(name="cc_monitor_winevent_info", schema="setting")
@Cacheable(true)
public class MonitorWinEventInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private boolean levelCritical;
	private boolean levelWarning;
	private boolean levelVerbose;
	private boolean levelError;
	private boolean levelInformational;
	private List<MonitorWinEventLogInfoEntity> monitorWinEventLogInfoEntities;
	private List<MonitorWinEventSourceInfoEntity> monitorWinEventSourceInfoEntities;
	private List<MonitorWinEventIdInfoEntity> monitorWinEventIdInfoEntities;
	private List<MonitorWinEventCategoryInfoEntity> monitorWinEventCategoryInfoEntities;
	private List<MonitorWinEventKeywordInfoEntity> monitorWinEventKeywordInfoEntities;
	private MonitorInfoEntity monitorInfoEntity;

	@Deprecated
	public MonitorWinEventInfoEntity() {
	}

	public MonitorWinEventInfoEntity(MonitorInfoEntity monitorInfoEntity) {
		this.setMonitorId(monitorInfoEntity.getMonitorId());
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToMonitorInfoEntity(monitorInfoEntity);
	}

	@Id
	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}


	@Column(name="level_critical")
	public boolean isLevelCritical() {
		return levelCritical;
	}


	public void setLevelCritical(boolean levelCritical) {
		this.levelCritical = levelCritical;
	}


	@Column(name="level_warning")
	public boolean isLevelWarning() {
		return levelWarning;
	}


	public void setLevelWarning(boolean levelWarning) {
		this.levelWarning = levelWarning;
	}


	@Column(name="level_verbose")
	public boolean isLevelVerbose() {
		return levelVerbose;
	}


	public void setLevelVerbose(boolean levelVerbose) {
		this.levelVerbose = levelVerbose;
	}


	@Column(name="level_error")
	public boolean isLevelError() {
		return levelError;
	}


	public void setLevelError(boolean levelError) {
		this.levelError = levelError;
	}


	@Column(name="level_informational")
	public boolean isLevelInformational() {
		return levelInformational;
	}


	public void setLevelInformational(boolean levelInformational) {
		this.levelInformational = levelInformational;
	}


	//bi-directional one-to-one association to MonitorInfoEntity
	@OneToOne(fetch=FetchType.LAZY, optional=false)
	@PrimaryKeyJoinColumn(name="monitor_id")
	public MonitorInfoEntity getMonitorInfoEntity() {
		return this.monitorInfoEntity;
	}

	@Deprecated
	public void setMonitorInfoEntity(MonitorInfoEntity monitorInfoEntity) {
		this.monitorInfoEntity = monitorInfoEntity;
	}

	//bi-directional many-to-one association to MonitorWinEventLogInfoEntity
	@OneToMany(mappedBy="monitorWinEventInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorWinEventLogInfoEntity> getMonitorWinEventLogInfoEntities() {
		return this.monitorWinEventLogInfoEntities;
	}

	public void setMonitorWinEventLogInfoEntities(List<MonitorWinEventLogInfoEntity> monitorWinEventLogInfoEntities) {
		this.monitorWinEventLogInfoEntities = monitorWinEventLogInfoEntities;
	}

	//bi-directional many-to-one association to MonitorWinEventSourceInfoEntity
	@OneToMany(mappedBy="monitorWinEventInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorWinEventSourceInfoEntity> getMonitorWinEventSourceInfoEntities() {
		return this.monitorWinEventSourceInfoEntities;
	}

	public void setMonitorWinEventSourceInfoEntities(List<MonitorWinEventSourceInfoEntity> monitorWinEventSourceInfoEntities) {
		this.monitorWinEventSourceInfoEntities = monitorWinEventSourceInfoEntities;
	}

	//bi-directional many-to-one association to MonitorWinEventIdInfoEntity
	@OneToMany(mappedBy="monitorWinEventInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorWinEventIdInfoEntity> getMonitorWinEventIdInfoEntities() {
		return this.monitorWinEventIdInfoEntities;
	}

	public void setMonitorWinEventIdInfoEntities(List<MonitorWinEventIdInfoEntity> monitorWinEventIdInfoEntities) {
		this.monitorWinEventIdInfoEntities = monitorWinEventIdInfoEntities;
	}

	//bi-directional many-to-one association to MonitorWinEventCategoryInfoEntity
	@OneToMany(mappedBy="monitorWinEventInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorWinEventCategoryInfoEntity> getMonitorWinEventCategoryInfoEntities() {
		return this.monitorWinEventCategoryInfoEntities;
	}

	public void setMonitorWinEventCategoryInfoEntities(List<MonitorWinEventCategoryInfoEntity> monitorWinEventCategoryInfoEntities) {
		this.monitorWinEventCategoryInfoEntities = monitorWinEventCategoryInfoEntities;
	}

	//bi-directional many-to-one association to MonitorWinEventKeywordInfoEntity
	@OneToMany(mappedBy="monitorWinEventInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorWinEventKeywordInfoEntity> getMonitorWinEventKeywordInfoEntities() {
		return this.monitorWinEventKeywordInfoEntities;
	}

	public void setMonitorWinEventKeywordInfoEntities(List<MonitorWinEventKeywordInfoEntity> monitorWinEventKeywordInfoEntities) {
		this.monitorWinEventKeywordInfoEntities = monitorWinEventKeywordInfoEntities;
	}

	/**
	 * MonitorInfoEntityオブジェクト参照設定<BR>
	 * 
	 * MonitorInfoEntity設定時はSetterに代わりこちらを使用すること。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void relateToMonitorInfoEntity(MonitorInfoEntity monitorInfoEntity) {
		this.setMonitorInfoEntity(monitorInfoEntity);
		if (monitorInfoEntity != null) {
			monitorInfoEntity.setMonitorWinEventInfoEntity(this);
		}
	}

	/**
	 * 削除前処理<BR>
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void unchain() {

		// MonitorInfoEntity
		if (this.monitorInfoEntity != null) {
			this.monitorInfoEntity.setMonitorWinEventInfoEntity(null);
		}
	}

}