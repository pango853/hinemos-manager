package com.clustercontrol.notify.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_notify_event_info database table.
 * 
 */
@Entity
@Table(name="cc_notify_event_info", schema="setting")
@Cacheable(true)
public class NotifyEventInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private NotifyEventInfoEntityPK id;

	private Integer infoEventNormalFlg;
	private Integer warnEventNormalFlg;
	private Integer criticalEventNormalFlg;
	private Integer unknownEventNormalFlg;

	private Integer infoEventNormalState;
	private Integer warnEventNormalState;
	private Integer criticalEventNormalState;
	private Integer unknownEventNormalState;

	private NotifyInfoEntity notifyInfoEntity;

	@Deprecated
	public NotifyEventInfoEntity() {
	}

	public NotifyEventInfoEntity(NotifyEventInfoEntityPK pk,
			NotifyInfoEntity notifyInfoEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToNotifyInfoEntity(notifyInfoEntity);
	}

	public NotifyEventInfoEntity(String notifyId,
			NotifyInfoEntity notifyInfoEntity) {
		this(new NotifyEventInfoEntityPK(notifyId), notifyInfoEntity);
	}


	@EmbeddedId
	public NotifyEventInfoEntityPK getId() {
		return this.id;
	}

	public void setId(NotifyEventInfoEntityPK id) {
		this.id = id;
	}


	@Column(name="info_event_normal_flg")
	public Integer getInfoEventNormalFlg() {
		return this.infoEventNormalFlg;
	}

	public void setInfoEventNormalFlg(Integer infoEventNormalFlg) {
		this.infoEventNormalFlg = infoEventNormalFlg;
	}

	@Column(name="warn_event_normal_flg")
	public Integer getWarnEventNormalFlg() {
		return this.warnEventNormalFlg;
	}

	public void setWarnEventNormalFlg(Integer warnEventNormalFlg) {
		this.warnEventNormalFlg = warnEventNormalFlg;
	}

	@Column(name="critical_event_normal_flg")
	public Integer getCriticalEventNormalFlg() {
		return this.criticalEventNormalFlg;
	}

	public void setCriticalEventNormalFlg(Integer criticalEventNormalFlg) {
		this.criticalEventNormalFlg = criticalEventNormalFlg;
	}

	@Column(name="unknown_event_normal_flg")
	public Integer getUnknownEventNormalFlg() {
		return this.unknownEventNormalFlg;
	}

	public void setUnknownEventNormalFlg(Integer unknownEventNormalFlg) {
		this.unknownEventNormalFlg = unknownEventNormalFlg;
	}


	@Column(name="info_event_normal_state")
	public Integer getInfoEventNormalState() {
		return this.infoEventNormalState;
	}

	public void setInfoEventNormalState(Integer infoEventNormalState) {
		this.infoEventNormalState = infoEventNormalState;
	}

	@Column(name="warn_event_normal_state")
	public Integer getWarnEventNormalState() {
		return this.warnEventNormalState;
	}

	public void setWarnEventNormalState(Integer warnEventNormalState) {
		this.warnEventNormalState = warnEventNormalState;
	}

	@Column(name="critical_event_normal_state")
	public Integer getCriticalEventNormalState() {
		return this.criticalEventNormalState;
	}

	public void setCriticalEventNormalState(Integer criticalEventNormalState) {
		this.criticalEventNormalState = criticalEventNormalState;
	}

	@Column(name="unknown_event_normal_state")
	public Integer getUnknownEventNormalState() {
		return this.unknownEventNormalState;
	}

	public void setUnknownEventNormalState(Integer unknownEventNormalState) {
		this.unknownEventNormalState = unknownEventNormalState;
	}


	//bi-directional one-to-one association to NotifyInfoEntity
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="notify_id", insertable=false, updatable=false)
	public NotifyInfoEntity getNotifyInfoEntity() {
		return this.notifyInfoEntity;
	}

	@Deprecated
	public void setNotifyInfoEntity(NotifyInfoEntity notifyInfoEntity) {
		this.notifyInfoEntity = notifyInfoEntity;
	}

	/**
	 * NotifyInfoEntityオブジェクト参照設定<BR>
	 * 
	 * NotifyInfoEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToNotifyInfoEntity(NotifyInfoEntity notifyInfoEntity) {
		this.setNotifyInfoEntity(notifyInfoEntity);
		if (notifyInfoEntity != null) {
			notifyInfoEntity.setNotifyEventInfoEntity(this);
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
		// NotifyInfoEntity
		if (this.notifyInfoEntity != null) {
			this.notifyInfoEntity.setNotifyEventInfoEntity(null);
		}
	}
}