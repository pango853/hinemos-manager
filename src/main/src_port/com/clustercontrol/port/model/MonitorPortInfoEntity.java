package com.clustercontrol.port.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;


/**
 * The persistent class for the cc_monitor_port_info database table.
 * 
 */
@Entity
@Table(name="cc_monitor_port_info", schema="setting")
@Cacheable(true)
public class MonitorPortInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private Integer portNumber;
	private Integer runCount;
	private Integer runInterval;
	private Integer timeout;
	private MonitorInfoEntity monitorInfoEntity;
	private MonitorProtocolMstEntity monitorProtocolMstEntity;

	@Deprecated
	public MonitorPortInfoEntity() {
	}

	public MonitorPortInfoEntity(MonitorInfoEntity monitorInfoEntity,
			MonitorProtocolMstEntity monitorProtocolMstEntity) {
		this.setMonitorId(monitorInfoEntity.getMonitorId());
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToMonitorInfoEntity(monitorInfoEntity);
		this.relateToMonitorProtocolMstEntity(monitorProtocolMstEntity);
	}


	@Id
	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}


	@Column(name="port_number")
	public Integer getPortNumber() {
		return this.portNumber;
	}

	public void setPortNumber(Integer portNumber) {
		this.portNumber = portNumber;
	}


	@Column(name="run_count")
	public Integer getRunCount() {
		return this.runCount;
	}

	public void setRunCount(Integer runCount) {
		this.runCount = runCount;
	}


	@Column(name="run_interval")
	public Integer getRunInterval() {
		return this.runInterval;
	}

	public void setRunInterval(Integer runInterval) {
		this.runInterval = runInterval;
	}


	public Integer getTimeout() {
		return this.timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
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
			monitorInfoEntity.setMonitorPortInfoEntity(this);
		}
	}


	//bi-directional many-to-one association to MonitorProtocolMstEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="service_id")
	public MonitorProtocolMstEntity getMonitorProtocolMstEntity() {
		return this.monitorProtocolMstEntity;
	}

	@Deprecated
	public void setMonitorProtocolMstEntity(MonitorProtocolMstEntity monitorProtocolMstEntity) {
		this.monitorProtocolMstEntity = monitorProtocolMstEntity;
	}

	/**
	 * MonitorProtocolMstEntityオブジェクト参照設定<BR>
	 * 
	 * MonitorProtocolMstEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToMonitorProtocolMstEntity(MonitorProtocolMstEntity monitorProtocolMstEntity) {
		this.setMonitorProtocolMstEntity(monitorProtocolMstEntity);
		if (monitorProtocolMstEntity != null) {
			List<MonitorPortInfoEntity> list = monitorProtocolMstEntity.getMonitorPortInfoEntities();
			if (list == null) {
				list = new ArrayList<MonitorPortInfoEntity>();
			} else {
				for(MonitorPortInfoEntity entity : list){
					if (entity.getMonitorId().equals(this.monitorId)) {
						return;
					}
				}
			}
			list.add(this);
			monitorProtocolMstEntity.setMonitorPortInfoEntities(list);
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
			this.monitorInfoEntity.setMonitorPortInfoEntity(null);
		}

		// MonitorProtocolMstEntity
		if (this.monitorProtocolMstEntity != null) {
			List<MonitorPortInfoEntity> list = this.monitorProtocolMstEntity.getMonitorPortInfoEntities();
			if (list != null) {
				Iterator<MonitorPortInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					MonitorPortInfoEntity entity = iter.next();
					if (entity.getMonitorId().equals(this.getMonitorId())){
						iter.remove();
						break;
					}
				}
			}
		}

	}

}