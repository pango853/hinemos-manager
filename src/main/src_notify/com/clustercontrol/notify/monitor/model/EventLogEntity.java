package com.clustercontrol.notify.monitor.model;

import java.sql.Timestamp;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetEntity;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.notify.util.NotifyUtil;


/**
 * The persistent class for the cc_event_log database table.
 *
 */
@Entity
@Table(name="cc_event_log", schema="log")
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.MONITOR)
@AttributeOverride(name="objectId",
column=@Column(name="monitor_id", insertable=false, updatable=false))
public class EventLogEntity extends ObjectPrivilegeTargetEntity {
	private static final long serialVersionUID = 1L;
	private EventLogEntityPK id;
	private String application;
	private String comment;
	private Timestamp commentDate;
	private String commentUser;
	private Timestamp confirmDate;
	private Integer confirmFlg;
	private String confirmUser;
	private Long duplicationCount;
	private Timestamp generationDate;
	private Integer inhibitedFlg;
	private String message;
	private String messageId;
	private String messageOrg;
	private Integer priority;
	private String scopeText;

	@Deprecated
	public EventLogEntity() {
	}

	public EventLogEntity(EventLogEntityPK pk) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.setObjectId(this.getId().getMonitorId());

		this.setOwnerRoleId(NotifyUtil.getOwnerRoleId(pk.getPluginId(), pk.getMonitorId(),
				pk.getMonitorDetailId(), pk.getFacilityId(), true));
	}

	public EventLogEntity(String monitorId,
			String monitorDetailId,
			String pluginId,
			java.util.Date outputDate,
			String facilityId) {
		this(new EventLogEntityPK(monitorId,
				monitorDetailId,
				pluginId,
				outputDate,
				facilityId));
	}

	public EventLogEntity(String monitorId, String monitorDetailId,
			String pluginId, java.util.Date outputDate, String facilityId,
			String ownerRoleId) {

		this.setId(new EventLogEntityPK(monitorId, monitorDetailId, pluginId,
				outputDate, facilityId));
		this.setObjectId(this.getId().getMonitorId());
		this.setOwnerRoleId(ownerRoleId);
	}

	@EmbeddedId
	public EventLogEntityPK getId() {
		return this.id;
	}

	public void setId(EventLogEntityPK id) {
		this.id = id;
	}


	public String getApplication() {
		return this.application;
	}

	public void setApplication(String application) {
		this.application = application;
	}


	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}


	@Column(name="comment_date")
	public Timestamp getCommentDate() {
		return this.commentDate;
	}

	public void setCommentDate(Timestamp commentDate) {
		this.commentDate = commentDate;
	}


	@Column(name="comment_user")
	public String getCommentUser() {
		return this.commentUser;
	}

	public void setCommentUser(String commentUser) {
		this.commentUser = commentUser;
	}


	@Column(name="confirm_date")
	public Timestamp getConfirmDate() {
		return this.confirmDate;
	}

	public void setConfirmDate(Timestamp confirmDate) {
		this.confirmDate = confirmDate;
	}


	@Column(name="confirm_flg")
	public Integer getConfirmFlg() {
		return this.confirmFlg;
	}

	public void setConfirmFlg(Integer confirmFlg) {
		this.confirmFlg = confirmFlg;
	}


	@Column(name="confirm_user")
	public String getConfirmUser() {
		return this.confirmUser;
	}

	public void setConfirmUser(String confirmUser) {
		this.confirmUser = confirmUser;
	}


	@Column(name="duplication_count")
	public Long getDuplicationCount() {
		return this.duplicationCount;
	}

	public void setDuplicationCount(Long duplicationCount) {
		this.duplicationCount = duplicationCount;
	}


	@Column(name="generation_date")
	public Timestamp getGenerationDate() {
		return this.generationDate;
	}

	public void setGenerationDate(Timestamp generationDate) {
		this.generationDate = generationDate;
	}


	@Column(name="inhibited_flg")
	public Integer getInhibitedFlg() {
		return this.inhibitedFlg;
	}

	public void setInhibitedFlg(Integer inhibitedFlg) {
		this.inhibitedFlg = inhibitedFlg;
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


	@Column(name="message_org")
	public String getMessageOrg() {
		return this.messageOrg;
	}

	public void setMessageOrg(String messageOrg) {
		this.messageOrg = messageOrg;
	}


	public Integer getPriority() {
		return this.priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}


	@Column(name="scope_text")
	public String getScopeText() {
		return this.scopeText;
	}

	public void setScopeText(String scopeText) {
		this.scopeText = scopeText;
	}

}