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
import javax.persistence.Transient;

import com.clustercontrol.commons.util.CryptUtil;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;



/**
 * The persistent class for the cc_monitor_http_scenario_info database table.
 * 
 */
@Entity
@Table(name="cc_monitor_http_scenario_info", schema="setting")
@Cacheable(true)
public class MonitorHttpScenarioInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	private String monitorId;
	private String authType;
	private String authUser;
	private String authPassword;
	private Integer proxyFlg;
	private String proxyUrl;
	private Integer proxyPort;
	private String proxyUser;
	private String proxyPassword;
	private Integer monitoringPerPageFlg;
	private String userAgent;
	private Integer connectTimeout;
	private Integer requestTimeout;

	private List<MonitorHttpScenarioPageInfoEntity> monitorHttpScenarioPageInfoEntities;

	private MonitorInfoEntity monitorInfoEntity;

	@Deprecated
	public MonitorHttpScenarioInfoEntity() {
	}

	public MonitorHttpScenarioInfoEntity(MonitorInfoEntity monitorInfoEntity) {
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


	@Column(name="auth_type")
	public String getAuthType() {
		return this.authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}


	@Column(name="auth_user")
	public String getAuthUser() {
		return this.authUser;
	}

	public void setAuthUser(String authUser) {
		this.authUser = authUser;
	}

	@Transient
	public String getAuthPassword() {
		return CryptUtil.decrypt(getAuthPasswordCrypt());
	}

	public void setAuthPassword(String authPassword) {
		setAuthPasswordCrypt(CryptUtil.encrypt(authPassword));
	}

	@Column(name="auth_password")
	public String getAuthPasswordCrypt() {
		return this.authPassword;
	}

	public void setAuthPasswordCrypt(String authPassword) {
		this.authPassword = authPassword;
	}

	@Column(name="proxy_flg")
	public Integer getProxyFlg() {
		return this.proxyFlg;
	}

	public void setProxyFlg(Integer proxyFlg) {
		this.proxyFlg = proxyFlg;
	}

	@Column(name="proxy_url")
	public String getProxyUrl() {
		return this.proxyUrl;
	}

	public void setProxyUrl(String proxyUrl) {
		this.proxyUrl = proxyUrl;
	}


	@Column(name="proxy_port")
	public Integer getProxyPort() {
		return this.proxyPort;
	}

	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}


	@Column(name="proxy_user")
	public String getProxyUser() {
		return this.proxyUser;
	}

	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	@Transient
	public String getProxyPassword() {
		return CryptUtil.decrypt(getProxyPasswordCrypt());
	}

	public void setProxyPassword(String proxyPassword) {
		setProxyPasswordCrypt(CryptUtil.encrypt(proxyPassword));
	}

	@Column(name="proxy_password")
	public String getProxyPasswordCrypt() {
		return this.proxyPassword;
	}

	public void setProxyPasswordCrypt(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}


	@Column(name="monitoring_per_page_flg")
	public Integer getMonitoringPerPageFlg() {
		return this.monitoringPerPageFlg;
	}

	public void setMonitoringPerPageFlg(Integer monitoringPerPageFlg) {
		this.monitoringPerPageFlg = monitoringPerPageFlg;
	}


	@Column(name="user_agent")
	public String getUserAgent() {
		return this.userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}


	@Column(name="connect_timeout")
	public Integer getConnectTimeout() {
		return this.connectTimeout;
	}

	public void setConnectTimeout(Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
	}


	@OneToMany(mappedBy="monitorHttpScenarioInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public List<MonitorHttpScenarioPageInfoEntity> getMonitorHttpScenarioPageInfoEntities() {
		return this.monitorHttpScenarioPageInfoEntities;
	}

	public void setMonitorHttpScenarioPageInfoEntities(List<MonitorHttpScenarioPageInfoEntity> monitorHttpScenarioPageInfoEntities) {
		this.monitorHttpScenarioPageInfoEntities = monitorHttpScenarioPageInfoEntities;
	}


	//bi-directional one-to-one association to MonitorInfoEntity
	@OneToOne(fetch=FetchType.LAZY, optional=false)
	@PrimaryKeyJoinColumn
	public MonitorInfoEntity getMonitorInfoEntity() {
		return this.monitorInfoEntity;
	}

	@Deprecated
	public void setMonitorInfoEntity(MonitorInfoEntity monitorInfoEntity) {
		this.monitorInfoEntity = monitorInfoEntity;
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
			monitorInfoEntity.setMonitorHttpScenarioInfoEntity(this);
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
			this.monitorInfoEntity.setMonitorHttpInfoEntity(null);
		}
	}

	@Column(name="request_timeout")
	public Integer getRequestTimeout() {
		return requestTimeout;
	}

	public void setRequestTimeout(Integer requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

}