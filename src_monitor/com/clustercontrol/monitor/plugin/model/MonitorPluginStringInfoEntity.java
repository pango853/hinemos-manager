package com.clustercontrol.monitor.plugin.model;

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
 * The persistent class for the cc_monitor_plugin_string_info database table.
 * 
 */
@Entity
@Table(name="cc_monitor_plugin_string_info", schema="setting")
@Cacheable(true)
public class MonitorPluginStringInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private MonitorPluginStringInfoEntityPK id;
	private String value;
	private MonitorPluginInfoEntity monitorPluginInfoEntity;

	@Deprecated
	public MonitorPluginStringInfoEntity() {
	}

	public MonitorPluginStringInfoEntity(MonitorPluginStringInfoEntityPK id,
			MonitorPluginInfoEntity monitorPluginInfoEntity) {
		this.setId(id);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToMonitorPluginInfoEntity(monitorPluginInfoEntity);
	}

	public MonitorPluginStringInfoEntity(String monitorId, String key, MonitorPluginInfoEntity monitorPluginInfoEntity) {
		this(new MonitorPluginStringInfoEntityPK(monitorId, key), monitorPluginInfoEntity);
	}

	@EmbeddedId
	public MonitorPluginStringInfoEntityPK getId() {
		return id;
	}

	public void setId(MonitorPluginStringInfoEntityPK id) {
		this.id = id;
	}

	@Column(name="value")
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	//bi-directional many-to-one association to CalInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="monitor_id", insertable=false, updatable=false)
	public MonitorPluginInfoEntity getMonitorPluginInfoEntity() {
		return monitorPluginInfoEntity;
	}

	@Deprecated
	public void setMonitorPluginInfoEntity(
			MonitorPluginInfoEntity monitorPluginInfoEntity) {
		this.monitorPluginInfoEntity = monitorPluginInfoEntity;
	}

	/**
	 * MonitorPluginInfoEntityオブジェクト参照設定<BR>
	 * 
	 * MonitorPluginInfoEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToMonitorPluginInfoEntity(MonitorPluginInfoEntity monitorPluginInfoEntity) {
		this.setMonitorPluginInfoEntity(monitorPluginInfoEntity);
		if (monitorPluginInfoEntity != null) {
			List<MonitorPluginStringInfoEntity> list = monitorPluginInfoEntity.getMonitorPluginStringInfoEntities();
			if (list == null) {
				list = new ArrayList<MonitorPluginStringInfoEntity>();
			} else {
				for(MonitorPluginStringInfoEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorPluginInfoEntity.setMonitorPluginStringInfoEntities(list);
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

		// MonitorPluginInfoEntity
		if (this.monitorPluginInfoEntity != null) {
			List<MonitorPluginStringInfoEntity> list = this.monitorPluginInfoEntity.getMonitorPluginStringInfoEntities();
			if (list != null) {
				Iterator<MonitorPluginStringInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					MonitorPluginStringInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}
