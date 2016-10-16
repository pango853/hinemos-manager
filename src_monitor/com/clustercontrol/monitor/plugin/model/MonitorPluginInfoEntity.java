package com.clustercontrol.monitor.plugin.model;

import java.io.Serializable;
import java.util.Iterator;
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
 * The persistent class for the cc_monitor_plugin_info database table.
 * 
 */
@Entity
@Table(name="cc_monitor_plugin_info", schema="setting")
@Cacheable(true)
public class MonitorPluginInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private List<MonitorPluginNumericInfoEntity> monitorPluginNumericInfoEntities;
	private List<MonitorPluginStringInfoEntity> monitorPluginStringInfoEntities;
	private MonitorInfoEntity monitorInfoEntity;

	@Deprecated
	public MonitorPluginInfoEntity() {
	}

	public MonitorPluginInfoEntity(MonitorInfoEntity monitorInfoEntity) {
		this.setMonitorId(monitorInfoEntity.getMonitorId());
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToMonitorInfoEntity(monitorInfoEntity);
	}

	@Id
	@Column(name="monitor_id")
	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	//bi-directional many-to-one association to MonitorPluginNumericInfoEntity
	@OneToMany(mappedBy="monitorPluginInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public List<MonitorPluginNumericInfoEntity> getMonitorPluginNumericInfoEntities() {
		return this.monitorPluginNumericInfoEntities;
	}

	public void setMonitorPluginNumericInfoEntities(List<MonitorPluginNumericInfoEntity> monitorPluginNumericInfoEntities) {
		this.monitorPluginNumericInfoEntities = monitorPluginNumericInfoEntities;
	}

	//bi-directional many-to-one association to MonitorPluginNumericInfoEntity
	@OneToMany(mappedBy="monitorPluginInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public List<MonitorPluginStringInfoEntity> getMonitorPluginStringInfoEntities() {
		return this.monitorPluginStringInfoEntities;
	}

	public void setMonitorPluginStringInfoEntities(List<MonitorPluginStringInfoEntity> monitorPluginStringInfoEntities) {
		this.monitorPluginStringInfoEntities = monitorPluginStringInfoEntities;
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
	 * MonitorPluginNumericInfoEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteMonitorPluginNumericInfoEntities(List<MonitorPluginNumericInfoEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorPluginNumericInfoEntity> list = this.getMonitorPluginNumericInfoEntities();
		Iterator<MonitorPluginNumericInfoEntity> iter = list.iterator();
		while(iter.hasNext()) {
			MonitorPluginNumericInfoEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}

	/**
	 * MonitorPluginStringInfoEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteMonitorPluginStringInfoEntities(List<MonitorPluginStringInfoEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorPluginStringInfoEntity> list = this.getMonitorPluginStringInfoEntities();
		Iterator<MonitorPluginStringInfoEntity> iter = list.iterator();
		while(iter.hasNext()) {
			MonitorPluginStringInfoEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
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
			monitorInfoEntity.setMonitorPluginInfoEntity(this);
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
			this.monitorInfoEntity.setMonitorPluginInfoEntity(null);
		}
	}

}
