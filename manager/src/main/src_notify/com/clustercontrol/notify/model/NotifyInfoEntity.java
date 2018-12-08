package com.clustercontrol.notify.model;

import java.sql.Timestamp;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetEntity;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_notify_info database table.
 *
 */
@Entity
@Table(name="cc_notify_info", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.PLATFORM_NOTIFY,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="notify_id", insertable=false, updatable=false))
public class NotifyInfoEntity extends ObjectPrivilegeTargetEntity {
	private static final long serialVersionUID = 1L;
	private String notifyId;
	private String description;
	private Integer initialCount;
	private Integer notFirstNotify;
	private Integer notifyType;
	private Timestamp regDate;
	private String regUser;
	private Integer renotifyPeriod;
	private Integer renotifyType;
	private Timestamp updateDate;
	private String updateUser;
	private Integer validFlg;
	private String calendarId;
	private NotifyCommandInfoEntity notifyCommandInfoEntity;
	private NotifyEventInfoEntity notifyEventInfoEntity;
	private NotifyJobInfoEntity notifyJobInfoEntity;
	private NotifyLogEscalateInfoEntity notifyLogEscalateInfoEntity;
	private NotifyMailInfoEntity notifyMailInfoEntity;
	private NotifyStatusInfoEntity notifyStatusInfoEntity;

	@Deprecated
	public NotifyInfoEntity() {
	}

	public NotifyInfoEntity(String notifyId) {
		this.setNotifyId(notifyId);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.setObjectId(this.getNotifyId());
	}


	@Id
	@Column(name="notify_id")
	public String getNotifyId() {
		return this.notifyId;
	}

	public void setNotifyId(String notifyId) {
		this.notifyId = notifyId;
	}


	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Column(name="initial_count")
	public Integer getInitialCount() {
		return this.initialCount;
	}

	public void setInitialCount(Integer initialCount) {
		this.initialCount = initialCount;
	}


	@Column(name="not_first_notify")
	public Integer getNotFirstNotify() {
		return this.notFirstNotify;
	}

	public void setNotFirstNotify(Integer notFirstNotify) {
		this.notFirstNotify = notFirstNotify;
	}


	@Column(name="notify_type")
	public Integer getNotifyType() {
		return this.notifyType;
	}

	public void setNotifyType(Integer notifyType) {
		this.notifyType = notifyType;
	}


	@Column(name="reg_date")
	public Timestamp getRegDate() {
		return this.regDate;
	}

	public void setRegDate(Timestamp regDate) {
		this.regDate = regDate;
	}


	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}


	@Column(name="renotify_period")
	public Integer getRenotifyPeriod() {
		return this.renotifyPeriod;
	}

	public void setRenotifyPeriod(Integer renotifyPeriod) {
		this.renotifyPeriod = renotifyPeriod;
	}


	@Column(name="renotify_type")
	public Integer getRenotifyType() {
		return this.renotifyType;
	}

	public void setRenotifyType(Integer renotifyType) {
		this.renotifyType = renotifyType;
	}


	@Column(name="update_date")
	public Timestamp getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(Timestamp updateDate) {
		this.updateDate = updateDate;
	}


	@Column(name="update_user")
	public String getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}


	@Column(name="valid_flg")
	public Integer getValidFlg() {
		return this.validFlg;
	}

	public void setValidFlg(Integer validFlg) {
		this.validFlg = validFlg;
	}


	@Column(name="calendar_id")
	public String getCalendarId() {
		return this.calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}


	//bi-directional one-to-one association to NotifyCommandInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyCommandInfoEntity getNotifyCommandInfoEntity() {
		return this.notifyCommandInfoEntity;
	}

	public void setNotifyCommandInfoEntity(NotifyCommandInfoEntity notifyCommandInfoEntity) {
		this.notifyCommandInfoEntity = notifyCommandInfoEntity;
	}


	//bi-directional one-to-one association to NotifyEventInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyEventInfoEntity getNotifyEventInfoEntity() {
		return this.notifyEventInfoEntity;
	}

	public void setNotifyEventInfoEntity(NotifyEventInfoEntity notifyEventInfoEntity) {
		this.notifyEventInfoEntity = notifyEventInfoEntity;
	}


	//bi-directional one-to-one association to NotifyJobInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyJobInfoEntity getNotifyJobInfoEntity() {
		return this.notifyJobInfoEntity;
	}

	public void setNotifyJobInfoEntity(NotifyJobInfoEntity notifyJobInfoEntity) {
		this.notifyJobInfoEntity = notifyJobInfoEntity;
	}


	//bi-directional one-to-one association to NotifyLogEscalateInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyLogEscalateInfoEntity getNotifyLogEscalateInfoEntity() {
		return this.notifyLogEscalateInfoEntity;
	}

	public void setNotifyLogEscalateInfoEntity(NotifyLogEscalateInfoEntity notifyLogEscalateInfoEntity) {
		this.notifyLogEscalateInfoEntity = notifyLogEscalateInfoEntity;
	}

	//bi-directional one-to-one association to NotifyMailInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyMailInfoEntity getNotifyMailInfoEntity() {
		return this.notifyMailInfoEntity;
	}

	public void setNotifyMailInfoEntity(NotifyMailInfoEntity notifyMailInfoEntity) {
		this.notifyMailInfoEntity = notifyMailInfoEntity;
	}


	//bi-directional one-to-one association to NotifyStatusInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyStatusInfoEntity getNotifyStatusInfoEntity() {
		return this.notifyStatusInfoEntity;
	}

	public void setNotifyStatusInfoEntity(NotifyStatusInfoEntity notifyStatusInfoEntity) {
		this.notifyStatusInfoEntity = notifyStatusInfoEntity;
	}
}