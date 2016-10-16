package com.clustercontrol.snmptrap.model;

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
 * The persistent class for the cc_monitor_trap_info database table.
 * 
 */
@Entity
@Table(name="cc_monitor_trap_info", schema="setting")
@Cacheable(true)
public class MonitorTrapInfoEntity implements Serializable {


	private static final long serialVersionUID = 1L;

	private String monitorId;
	private Integer charsetConvert;
	private String charsetName;
	private Integer communityCheck;
	private String communityName;
	private Integer notifyofReceivingUnspecifiedFlg;
	private Integer priorityUnspecified;
	private MonitorInfoEntity monitorInfoEntity;

	private List<MonitorTrapValueInfoEntity> monitorTrapValueInfoEntities;

	@Deprecated
	public MonitorTrapInfoEntity() {
	}

	public MonitorTrapInfoEntity(MonitorInfoEntity monitorInfoEntity) {
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


	@Column(name="charset_convert")
	public Integer getCharsetConvert() {
		return this.charsetConvert;
	}

	public void setCharsetConvert(Integer charsetConvert) {
		this.charsetConvert = charsetConvert;
	}


	@Column(name="charset_name")
	public String getCharsetName() {
		return this.charsetName;
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}


	@Column(name="community_check")
	public Integer getCommunityCheck() {
		return this.communityCheck;
	}

	public void setCommunityCheck(Integer communityCheck) {
		this.communityCheck = communityCheck;
	}


	@Column(name="community_name")
	public String getCommunityName() {
		return this.communityName;
	}

	public void setCommunityName(String communityName) {
		this.communityName = communityName;
	}


	@Column(name="notifyof_receiving_unspecified_flg")
	public Integer getNotifyofReceivingUnspecifiedFlg() {
		return notifyofReceivingUnspecifiedFlg;
	}
	public void setNotifyofReceivingUnspecifiedFlg(
			Integer notifyofReceivingUnspecifiedFlg) {
		this.notifyofReceivingUnspecifiedFlg = notifyofReceivingUnspecifiedFlg;
	}

	@Column(name="priority_unspecified")
	public Integer getPriorityUnspecified() {
		return priorityUnspecified;
	}
	public void setPriorityUnspecified(Integer priorityUnspecified) {
		this.priorityUnspecified = priorityUnspecified;
	}

	@OneToMany(mappedBy="monitorTrapInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorTrapValueInfoEntity> getMonitorTrapValueInfoEntities() {
		return this.monitorTrapValueInfoEntities;
	}

	public void setMonitorTrapValueInfoEntities(List<MonitorTrapValueInfoEntity> monitorTrapValueInfoEntities) {
		this.monitorTrapValueInfoEntities = monitorTrapValueInfoEntities;
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
			monitorInfoEntity.setMonitorTrapInfoEntity(this);
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
			this.monitorInfoEntity.setMonitorTrapInfoEntity(null);
		}
	}

	public void deleteMonitorTrapValueInfoEntities(List<MonitorTrapValueInfoEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorTrapValueInfoEntity> list = this.getMonitorTrapValueInfoEntities();
		Iterator<MonitorTrapValueInfoEntity> iter = list.iterator();
		while(iter.hasNext()) {
			MonitorTrapValueInfoEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}
}