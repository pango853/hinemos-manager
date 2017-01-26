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
 * The persistent class for the cc_notify_status_info database table.
 * 
 */
@Entity
@Table(name="cc_notify_status_info", schema="setting")
@Cacheable(true)
public class NotifyStatusInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private NotifyStatusInfoEntityPK id;

	private Integer infoStatusFlg;
	private Integer warnStatusFlg;
	private Integer criticalStatusFlg;
	private Integer unknownStatusFlg;

	private Integer statusInvalidFlg;
	private Integer statusUpdatePriority;
	private Integer statusValidPeriod;
	private NotifyInfoEntity notifyInfoEntity;

	@Deprecated
	public NotifyStatusInfoEntity() {
	}

	public NotifyStatusInfoEntity(NotifyStatusInfoEntityPK pk,
			NotifyInfoEntity notifyInfoEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToNotifyInfoEntity(notifyInfoEntity);
	}

	public NotifyStatusInfoEntity(String notifyId,
			NotifyInfoEntity notifyInfoEntity) {
		this(new NotifyStatusInfoEntityPK(notifyId), notifyInfoEntity);
	}


	@EmbeddedId
	public NotifyStatusInfoEntityPK getId() {
		return this.id;
	}

	public void setId(NotifyStatusInfoEntityPK id) {
		this.id = id;
	}


	@Column(name="info_status_flg")
	public Integer getInfoStatusFlg() {
		return this.infoStatusFlg;
	}

	public void setInfoStatusFlg(Integer infoStatusFlg) {
		this.infoStatusFlg = infoStatusFlg;
	}


	@Column(name="warn_status_flg")
	public Integer getWarnStatusFlg() {
		return this.warnStatusFlg;
	}

	public void setWarnStatusFlg(Integer warnStatusFlg) {
		this.warnStatusFlg = warnStatusFlg;
	}


	@Column(name="critical_status_flg")
	public Integer getCriticalStatusFlg() {
		return this.criticalStatusFlg;
	}

	public void setCriticalStatusFlg(Integer criticalStatusFlg) {
		this.criticalStatusFlg = criticalStatusFlg;
	}


	@Column(name="unknown_status_flg")
	public Integer getUnknownStatusFlg() {
		return this.unknownStatusFlg;
	}

	public void setUnknownStatusFlg(Integer unknownStatusFlg) {
		this.unknownStatusFlg = unknownStatusFlg;
	}


	@Column(name="status_invalid_flg")
	public Integer getStatusInvalidFlg() {
		return this.statusInvalidFlg;
	}

	public void setStatusInvalidFlg(Integer statusInvalidFlg) {
		this.statusInvalidFlg = statusInvalidFlg;
	}


	@Column(name="status_update_priority")
	public Integer getStatusUpdatePriority() {
		return this.statusUpdatePriority;
	}

	public void setStatusUpdatePriority(Integer statusUpdatePriority) {
		this.statusUpdatePriority = statusUpdatePriority;
	}


	@Column(name="status_valid_period")
	public Integer getStatusValidPeriod() {
		return this.statusValidPeriod;
	}

	public void setStatusValidPeriod(Integer statusValidPeriod) {
		this.statusValidPeriod = statusValidPeriod;
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
			notifyInfoEntity.setNotifyStatusInfoEntity(this);
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
			this.notifyInfoEntity.setNotifyStatusInfoEntity(null);
		}
	}
}