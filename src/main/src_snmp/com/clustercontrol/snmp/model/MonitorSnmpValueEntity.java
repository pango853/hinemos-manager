package com.clustercontrol.snmp.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_monitor_snmp_value database table.
 * 
 */
@Entity
@Table(name="cc_monitor_snmp_value", schema="setting")
@Cacheable(true)
public class MonitorSnmpValueEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private MonitorSnmpValueEntityPK id;
	private Timestamp getDate;
	private double value;
	private MonitorSnmpInfoEntity monitorSnmpInfoEntity;

	@Deprecated
	public MonitorSnmpValueEntity() {
	}

	public MonitorSnmpValueEntity(MonitorSnmpValueEntityPK pk,
			MonitorSnmpInfoEntity monitorSnmpInfoEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToMonitorSnmpInfoEntity(monitorSnmpInfoEntity);
	}

	public MonitorSnmpValueEntity(String monitorId, String facilityId,
			MonitorSnmpInfoEntity monitorSnmpInfoEntity) {
		this(new MonitorSnmpValueEntityPK(monitorId, facilityId), monitorSnmpInfoEntity);
	}


	@EmbeddedId
	public MonitorSnmpValueEntityPK getId() {
		return this.id;
	}

	public void setId(MonitorSnmpValueEntityPK id) {
		this.id = id;
	}


	@Column(name="get_date")
	public Timestamp getGetDate() {
		return this.getDate;
	}

	public void setGetDate(Timestamp getDate) {
		this.getDate = getDate;
	}


	public double getValue() {
		return this.value;
	}

	public void setValue(double value) {
		this.value = value;
	}


	//bi-directional many-to-one association to MonitorSnmpInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="monitor_id", insertable=false, updatable=false)
	public MonitorSnmpInfoEntity getMonitorSnmpInfoEntity() {
		return this.monitorSnmpInfoEntity;
	}

	@Deprecated
	public void setMonitorSnmpInfoEntity(MonitorSnmpInfoEntity monitorSnmpInfoEntity) {
		this.monitorSnmpInfoEntity = monitorSnmpInfoEntity;
	}

	/**
	 * MonitorSnmpInfoEntityオブジェクト参照設定<BR>
	 * 
	 * MonitorSnmpInfoEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToMonitorSnmpInfoEntity(MonitorSnmpInfoEntity monitorSnmpInfoEntity) {
		this.setMonitorSnmpInfoEntity(monitorSnmpInfoEntity);
		if (monitorSnmpInfoEntity != null) {
			List<MonitorSnmpValueEntity> list = monitorSnmpInfoEntity.getMonitorSnmpValueEntities();
			if (list == null) {
				list = new ArrayList<MonitorSnmpValueEntity>();
			} else {
				for(MonitorSnmpValueEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorSnmpInfoEntity.setMonitorSnmpValueEntities(list);
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

		// MonitorSnmpInfoEntity
		if (this.monitorSnmpInfoEntity != null) {
			List<MonitorSnmpValueEntity> list = this.monitorSnmpInfoEntity.getMonitorSnmpValueEntities();
			if (list != null) {
				Iterator<MonitorSnmpValueEntity> iter = list.iterator();
				while(iter.hasNext()) {
					MonitorSnmpValueEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}