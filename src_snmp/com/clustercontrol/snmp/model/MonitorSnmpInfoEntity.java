package com.clustercontrol.snmp.model;

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
 * The persistent class for the cc_monitor_snmp_info database table.
 * 
 */
@Entity
@Table(name="cc_monitor_snmp_info", schema="setting")
@Cacheable(true)
public class MonitorSnmpInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private Integer convertFlg;
	private String snmpOid;
	private List<MonitorSnmpValueEntity> monitorSnmpValueEntities;
	private MonitorInfoEntity monitorInfoEntity;

	@Deprecated
	public MonitorSnmpInfoEntity() {
	}

	public MonitorSnmpInfoEntity(MonitorInfoEntity monitorInfoEntity) {
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


	@Column(name="convert_flg")
	public Integer getConvertFlg() {
		return this.convertFlg;
	}

	public void setConvertFlg(Integer convertFlg) {
		this.convertFlg = convertFlg;
	}


	@Column(name="snmp_oid")
	public String getSnmpOid() {
		return this.snmpOid;
	}

	public void setSnmpOid(String snmpOid) {
		this.snmpOid = snmpOid;
	}


	//bi-directional many-to-one association to MonitorNumericValueInfoEntity
	@OneToMany(mappedBy="monitorSnmpInfoEntity", cascade=CascadeType.REMOVE)
	public List<MonitorSnmpValueEntity> getMonitorSnmpValueEntities() {
		return this.monitorSnmpValueEntities;
	}

	public void setMonitorSnmpValueEntities(List<MonitorSnmpValueEntity> monitorSnmpValueEntities) {
		this.monitorSnmpValueEntities = monitorSnmpValueEntities;
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
			monitorInfoEntity.setMonitorSnmpInfoEntity(this);
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
			this.monitorInfoEntity.setMonitorSnmpInfoEntity(null);
		}
	}

}