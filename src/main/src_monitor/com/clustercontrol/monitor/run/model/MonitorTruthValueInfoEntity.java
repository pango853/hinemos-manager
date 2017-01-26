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
 * The persistent class for the cc_monitor_truth_value_info database table.
 * 
 */
@Entity
@Table(name="cc_monitor_truth_value_info", schema="setting")
@Cacheable(true)
public class MonitorTruthValueInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private MonitorTruthValueInfoEntityPK id;
	private String message;
	private String messageId;
	private MonitorInfoEntity monitorInfoEntity;

	@Deprecated
	public MonitorTruthValueInfoEntity() {
	}

	public MonitorTruthValueInfoEntity(MonitorTruthValueInfoEntityPK pk,
			MonitorInfoEntity monitorInfoEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToMonitorInfoEntity(monitorInfoEntity);
	}

	public MonitorTruthValueInfoEntity(MonitorInfoEntity monitorInfoEntity, Integer priority, Integer truthValue) {
		this(new MonitorTruthValueInfoEntityPK(monitorInfoEntity.getMonitorId(), priority, truthValue), monitorInfoEntity);
	}


	@EmbeddedId
	public MonitorTruthValueInfoEntityPK getId() {
		return this.id;
	}

	public void setId(MonitorTruthValueInfoEntityPK id) {
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


	//bi-directional many-to-one association to MonitorInfoEntity
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
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
			List<MonitorTruthValueInfoEntity> list = monitorInfoEntity.getMonitorTruthValueInfoEntities();
			if (list == null) {
				list = new ArrayList<MonitorTruthValueInfoEntity>();
			} else {
				for(MonitorTruthValueInfoEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorInfoEntity.setMonitorTruthValueInfoEntities(list);
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
			List<MonitorTruthValueInfoEntity> list = this.monitorInfoEntity.getMonitorTruthValueInfoEntities();
			if (list != null) {
				Iterator<MonitorTruthValueInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					MonitorTruthValueInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}