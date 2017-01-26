package com.clustercontrol.monitor.run.model;

import java.io.Serializable;
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
 * The persistent class for the cc_monitor_numeric_value_info database table.
 * 
 */
@Entity
@Table(name="cc_monitor_numeric_value_info", schema="setting")
@Cacheable(true)
public class MonitorNumericValueInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private MonitorNumericValueInfoEntityPK id;
	private String message;
	private String messageId;
	private double thresholdLowerLimit;
	private double thresholdUpperLimit;
	private MonitorInfoEntity monitorInfoEntity;

	@Deprecated
	public MonitorNumericValueInfoEntity() {
	}

	public MonitorNumericValueInfoEntity(MonitorNumericValueInfoEntityPK pk,
			MonitorInfoEntity monitorInfoEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToMonitorInfoEntity(monitorInfoEntity);
	}

	public MonitorNumericValueInfoEntity(MonitorInfoEntity monitorInfoEntity, Integer priority) {
		this(new MonitorNumericValueInfoEntityPK(monitorInfoEntity.getMonitorId(), priority), monitorInfoEntity);
	}


	@EmbeddedId
	public MonitorNumericValueInfoEntityPK getId() {
		return this.id;
	}

	public void setId(MonitorNumericValueInfoEntityPK id) {
		this.id = id;
	}


	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}


	@Column(name="message_id")
	public String getMessageId() {
		return this.messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}


	@Column(name="threshold_lower_limit")
	public double getThresholdLowerLimit() {
		return this.thresholdLowerLimit;
	}

	public void setThresholdLowerLimit(double thresholdLowerLimit) {
		this.thresholdLowerLimit = thresholdLowerLimit;
	}


	@Column(name="threshold_upper_limit")
	public double getThresholdUpperLimit() {
		return this.thresholdUpperLimit;
	}

	public void setThresholdUpperLimit(double thresholdUpperLimit) {
		this.thresholdUpperLimit = thresholdUpperLimit;
	}


	//bi-directional many-to-one association to MonitorInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="monitor_id", insertable=false, updatable=false)
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
			List<MonitorNumericValueInfoEntity> list = monitorInfoEntity.getMonitorNumericValueInfoEntities();
			if (list == null) {
				list = new ArrayList<MonitorNumericValueInfoEntity>();
			} else {
				for(MonitorNumericValueInfoEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorInfoEntity.setMonitorNumericValueInfoEntities(list);
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
			List<MonitorNumericValueInfoEntity> list = this.monitorInfoEntity.getMonitorNumericValueInfoEntities();
			if (list != null) {
				Iterator<MonitorNumericValueInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					MonitorNumericValueInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}