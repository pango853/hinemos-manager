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
 * The persistent class for the cc_notify_log_escalate_info database table.
 * 
 */
@Entity
@Table(name="cc_notify_log_escalate_info", schema="setting")
@Cacheable(true)
public class NotifyLogEscalateInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private NotifyLogEscalateInfoEntityPK id;
	private Integer escalateFacilityFlg;
	private Integer escalatePort;

	private Integer infoEscalateFlg;
	private Integer warnEscalateFlg;
	private Integer criticalEscalateFlg;
	private Integer unknownEscalateFlg;

	private String infoEscalateMessage;
	private String warnEscalateMessage;
	private String criticalEscalateMessage;
	private String unknownEscalateMessage;

	private Integer infoSyslogPriority;
	private Integer warnSyslogPriority;
	private Integer criticalSyslogPriority;
	private Integer unknownSyslogPriority;

	private Integer infoSyslogFacility;
	private Integer warnSyslogFacility;
	private Integer criticalSyslogFacility;
	private Integer unknownSyslogFacility;

	private String escalateFacilityId;
	private NotifyInfoEntity notifyInfoEntity;

	@Deprecated
	public NotifyLogEscalateInfoEntity() {
	}

	public NotifyLogEscalateInfoEntity(NotifyLogEscalateInfoEntityPK pk,
			NotifyInfoEntity notifyInfoEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToNotifyInfoEntity(notifyInfoEntity);
	}

	public NotifyLogEscalateInfoEntity(String notifyId,
			NotifyInfoEntity notifyInfoEntity) {
		this(new NotifyLogEscalateInfoEntityPK(notifyId), notifyInfoEntity);
	}


	@EmbeddedId
	public NotifyLogEscalateInfoEntityPK getId() {
		return this.id;
	}

	public void setId(NotifyLogEscalateInfoEntityPK id) {
		this.id = id;
	}


	@Column(name="escalate_facility_flg")
	public Integer getEscalateFacilityFlg() {
		return this.escalateFacilityFlg;
	}

	public void setEscalateFacilityFlg(Integer escalateFacilityFlg) {
		this.escalateFacilityFlg = escalateFacilityFlg;
	}


	@Column(name="info_escalate_flg")
	public Integer getInfoEscalateFlg() {
		return this.infoEscalateFlg;
	}

	public void setInfoEscalateFlg(Integer infoEscalateFlg) {
		this.infoEscalateFlg = infoEscalateFlg;
	}


	@Column(name="warn_escalate_flg")
	public Integer getWarnEscalateFlg() {
		return this.warnEscalateFlg;
	}

	public void setWarnEscalateFlg(Integer warnEscalateFlg) {
		this.warnEscalateFlg = warnEscalateFlg;
	}


	@Column(name="critical_escalate_flg")
	public Integer getCriticalEscalateFlg() {
		return this.criticalEscalateFlg;
	}

	public void setCriticalEscalateFlg(Integer criticalEscalateFlg) {
		this.criticalEscalateFlg = criticalEscalateFlg;
	}


	@Column(name="unknown_escalate_flg")
	public Integer getUnknownEscalateFlg() {
		return this.unknownEscalateFlg;
	}

	public void setUnknownEscalateFlg(Integer unknownEscalateFlg) {
		this.unknownEscalateFlg = unknownEscalateFlg;
	}


	@Column(name="info_escalate_message")
	public String getInfoEscalateMessage() {
		return this.infoEscalateMessage;
	}

	public void setInfoEscalateMessage(String infoEscalateMessage) {
		this.infoEscalateMessage = infoEscalateMessage;
	}


	@Column(name="warn_escalate_message")
	public String getWarnEscalateMessage() {
		return this.warnEscalateMessage;
	}

	public void setWarnEscalateMessage(String warnEscalateMessage) {
		this.warnEscalateMessage = warnEscalateMessage;
	}


	@Column(name="critical_escalate_message")
	public String getCriticalEscalateMessage() {
		return this.criticalEscalateMessage;
	}

	public void setCriticalEscalateMessage(String criticalEscalateMessage) {
		this.criticalEscalateMessage = criticalEscalateMessage;
	}


	@Column(name="unknown_escalate_message")
	public String getUnknownEscalateMessage() {
		return this.unknownEscalateMessage;
	}

	public void setUnknownEscalateMessage(String unknownEscalateMessage) {
		this.unknownEscalateMessage = unknownEscalateMessage;
	}



	@Column(name="info_syslog_facility")
	public Integer getInfoSyslogFacility() {
		return this.infoSyslogFacility;
	}

	public void setInfoSyslogFacility(Integer infoSyslogFacility) {
		this.infoSyslogFacility = infoSyslogFacility;
	}


	@Column(name="warn_syslog_facility")
	public Integer getWarnSyslogFacility() {
		return this.warnSyslogFacility;
	}

	public void setWarnSyslogFacility(Integer warnSyslogFacility) {
		this.warnSyslogFacility = warnSyslogFacility;
	}


	@Column(name="critical_syslog_facility")
	public Integer getCriticalSyslogFacility() {
		return this.criticalSyslogFacility;
	}

	public void setCriticalSyslogFacility(Integer criticalSyslogFacility) {
		this.criticalSyslogFacility = criticalSyslogFacility;
	}


	@Column(name="unknown_syslog_facility")
	public Integer getUnknownSyslogFacility() {
		return this.unknownSyslogFacility;
	}

	public void setUnknownSyslogFacility(Integer unknownSyslogFacility) {
		this.unknownSyslogFacility = unknownSyslogFacility;
	}


	@Column(name="info_syslog_priority")
	public Integer getInfoSyslogPriority() {
		return this.infoSyslogPriority;
	}

	public void setInfoSyslogPriority(Integer infoSyslogPriority) {
		this.infoSyslogPriority = infoSyslogPriority;
	}


	@Column(name="warn_syslog_priority")
	public Integer getWarnSyslogPriority() {
		return this.warnSyslogPriority;
	}

	public void setWarnSyslogPriority(Integer warnSyslogPriority) {
		this.warnSyslogPriority = warnSyslogPriority;
	}


	@Column(name="critical_syslog_priority")
	public Integer getCriticalSyslogPriority() {
		return this.criticalSyslogPriority;
	}

	public void setCriticalSyslogPriority(Integer criticalSyslogPriority) {
		this.criticalSyslogPriority = criticalSyslogPriority;
	}


	@Column(name="unknown_syslog_priority")
	public Integer getUnknownSyslogPriority() {
		return this.unknownSyslogPriority;
	}

	public void setUnknownSyslogPriority(Integer unknownSyslogPriority) {
		this.unknownSyslogPriority = unknownSyslogPriority;
	}


	@Column(name="escalate_port")
	public Integer getEscalatePort() {
		return this.escalatePort;
	}

	public void setEscalatePort(Integer escalatePort) {
		this.escalatePort = escalatePort;
	}


	@Column(name="escalate_facility")
	public String getEscalateFacilityId() {
		return this.escalateFacilityId;
	}

	public void setEscalateFacilityId(String escalateFacilityId) {
		this.escalateFacilityId = escalateFacilityId;
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
			notifyInfoEntity.setNotifyLogEscalateInfoEntity(this);
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
			this.notifyInfoEntity.setNotifyLogEscalateInfoEntity(null);
		}
	}

}