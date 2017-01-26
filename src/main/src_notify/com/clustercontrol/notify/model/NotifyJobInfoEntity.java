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
 * The persistent class for the cc_notify_job_info database table.
 * 
 */
@Entity
@Table(name="cc_notify_job_info", schema="setting")
@Cacheable(true)
public class NotifyJobInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private NotifyJobInfoEntityPK id;
	private Integer jobExecFacilityFlg;

	private Integer infoJobRun;
	private Integer warnJobRun;
	private Integer criticalJobRun;
	private Integer unknownJobRun;

	private String infoJobunitId;
	private String warnJobunitId;
	private String criticalJobunitId;
	private String unknownJobunitId;

	private String infoJobId;
	private String warnJobId;
	private String criticalJobId;
	private String unknownJobId;

	private Integer infoJobFailurePriority;
	private Integer warnJobFailurePriority;
	private Integer criticalJobFailurePriority;
	private Integer unknownJobFailurePriority;

	private String jobExecFacilityId;
	private NotifyInfoEntity notifyInfoEntity;

	@Deprecated
	public NotifyJobInfoEntity() {
	}

	public NotifyJobInfoEntity(NotifyJobInfoEntityPK pk,
			NotifyInfoEntity notifyInfoEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToNotifyInfoEntity(notifyInfoEntity);
	}

	public NotifyJobInfoEntity(String notifyId,
			NotifyInfoEntity notifyInfoEntity) {
		this(new NotifyJobInfoEntityPK(notifyId), notifyInfoEntity);
	}

	@EmbeddedId
	public NotifyJobInfoEntityPK getId() {
		return this.id;
	}

	public void setId(NotifyJobInfoEntityPK id) {
		this.id = id;
	}

	@Column(name="job_exec_facility_flg")
	public Integer getJobExecFacilityFlg() {
		return this.jobExecFacilityFlg;
	}

	public void setJobExecFacilityFlg(Integer jobExecFacilityFlg) {
		this.jobExecFacilityFlg = jobExecFacilityFlg;
	}


	@Column(name="info_job_run")
	public Integer getInfoJobRun() {
		return this.infoJobRun;
	}

	public void setInfoJobRun(Integer infoJobRun) {
		this.infoJobRun = infoJobRun;
	}

	@Column(name="warn_job_run")
	public Integer getWarnJobRun() {
		return this.warnJobRun;
	}

	public void setWarnJobRun(Integer warnJobRun) {
		this.warnJobRun = warnJobRun;
	}

	@Column(name="critical_job_run")
	public Integer getCriticalJobRun() {
		return this.criticalJobRun;
	}

	public void setCriticalJobRun(Integer criticalJobRun) {
		this.criticalJobRun = criticalJobRun;
	}

	@Column(name="unknown_job_run")
	public Integer getUnknownJobRun() {
		return this.unknownJobRun;
	}

	public void setUnknownJobRun(Integer unknownJobRun) {
		this.unknownJobRun = unknownJobRun;
	}

	@Column(name="info_jobunit_id")
	public String getInfoJobunitId() {
		return this.infoJobunitId;
	}

	public void setInfoJobunitId(String infoJobunitId) {
		this.infoJobunitId = infoJobunitId;
	}

	@Column(name="warn_jobunit_id")
	public String getWarnJobunitId() {
		return this.warnJobunitId;
	}

	public void setWarnJobunitId(String warnJobunitId) {
		this.warnJobunitId = warnJobunitId;
	}

	@Column(name="critical_jobunit_id")
	public String getCriticalJobunitId() {
		return this.criticalJobunitId;
	}

	public void setCriticalJobunitId(String criticalJobunitId) {
		this.criticalJobunitId = criticalJobunitId;
	}

	@Column(name="unknown_jobunit_id")
	public String getUnknownJobunitId() {
		return this.unknownJobunitId;
	}

	public void setUnknownJobunitId(String unknownJobunitId) {
		this.unknownJobunitId = unknownJobunitId;
	}

	@Column(name="info_job_id")
	public String getInfoJobId() {
		return this.infoJobId;
	}

	public void setInfoJobId(String infoJobId) {
		this.infoJobId = infoJobId;
	}

	@Column(name="warn_job_id")
	public String getWarnJobId() {
		return this.warnJobId;
	}

	public void setWarnJobId(String warnJobId) {
		this.warnJobId = warnJobId;
	}

	@Column(name="critical_job_id")
	public String getCriticalJobId() {
		return this.criticalJobId;
	}

	public void setCriticalJobId(String criticalJobId) {
		this.criticalJobId = criticalJobId;
	}

	@Column(name="unknown_job_id")
	public String getUnknownJobId() {
		return this.unknownJobId;
	}

	public void setUnknownJobId(String unknownJobId) {
		this.unknownJobId = unknownJobId;
	}


	@Column(name="info_job_failure_priority")
	public Integer getInfoJobFailurePriority() {
		return this.infoJobFailurePriority;
	}

	public void setInfoJobFailurePriority(Integer infoJobFailurePriority) {
		this.infoJobFailurePriority = infoJobFailurePriority;
	}

	@Column(name="warn_job_failure_priority")
	public Integer getWarnJobFailurePriority() {
		return this.warnJobFailurePriority;
	}

	public void setWarnJobFailurePriority(Integer warnJobFailurePriority) {
		this.warnJobFailurePriority = warnJobFailurePriority;
	}

	@Column(name="critical_job_failure_priority")
	public Integer getCriticalJobFailurePriority() {
		return this.criticalJobFailurePriority;
	}

	public void setCriticalJobFailurePriority(Integer criticalJobFailurePriority) {
		this.criticalJobFailurePriority = criticalJobFailurePriority;
	}

	@Column(name="unknown_job_failure_priority")
	public Integer getUnknownJobFailurePriority() {
		return this.unknownJobFailurePriority;
	}

	public void setUnknownJobFailurePriority(Integer unknownJobFailurePriority) {
		this.unknownJobFailurePriority = unknownJobFailurePriority;
	}


	@Column(name="job_exec_facility")
	public String getJobExecFacilityId() {
		return this.jobExecFacilityId;
	}

	public void setJobExecFacilityId(String jobExecFacilityId) {
		this.jobExecFacilityId = jobExecFacilityId;
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
			notifyInfoEntity.setNotifyJobInfoEntity(this);
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
			this.notifyInfoEntity.setNotifyJobInfoEntity(null);
		}
	}
}