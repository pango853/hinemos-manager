package com.clustercontrol.jmx.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.clustercontrol.commons.util.CryptUtil;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;



/**
 * The persistent class for the cc_monitor_http_info database table.
 * 
 */
@Entity
@Table(name="cc_monitor_jmx_info", schema="setting")
@Cacheable(true)
public class MonitorJmxInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private String authUser;
	private String authPassword;
	private Integer port;
	private MonitorInfoEntity monitorInfoEntity;
	private MonitorJmxMstEntity jmxTypeMstEntity;

	@Deprecated
	public MonitorJmxInfoEntity() {
	}

	public MonitorJmxInfoEntity(MonitorInfoEntity monitorInfoEntity) {
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


	@Column(name="port")
	public Integer getPort() {
		return this.port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	@OneToOne
	@JoinColumn(name="master_id", referencedColumnName="master_id", insertable=true, updatable=true)
	public MonitorJmxMstEntity getJmxTypeMstEntity() {
		return this.jmxTypeMstEntity;
	}

	public void setJmxTypeMstEntity(MonitorJmxMstEntity jmxTypeMstEntity) {
		this.jmxTypeMstEntity = jmxTypeMstEntity;
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
			monitorInfoEntity.setMonitorJmxInfoEntity(this);
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
			this.monitorInfoEntity.setMonitorJmxInfoEntity(null);
		}
	}

}