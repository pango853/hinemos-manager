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
 * The persistent class for the cc_status_info database table.
 * 
 */
@Entity
@Table(name="cc_status_info", schema="log")
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.MONITOR)
@AttributeOverride(name="objectId",
column=@Column(name="monitor_id", insertable=false, updatable=false))
public class StatusInfoEntity extends ObjectPrivilegeTargetEntity {
	private static final long serialVersionUID = 1L;
	private StatusInfoEntityPK id;
	private String application;
	private Timestamp expirationDate;
	private Integer expirationFlg;
	private Timestamp generationDate;
	private String message;
	private String messageId;
	private Timestamp outputDate;
	private Integer priority;

	@Deprecated
	public StatusInfoEntity() {
	}

	public StatusInfoEntity(StatusInfoEntityPK pk) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.setObjectId(this.getId().getMonitorId());

		this.setOwnerRoleId(NotifyUtil.getOwnerRoleId(pk.getPluginId(), pk.getMonitorId(),
				pk.getMonitorDetailId(), pk.getFacilityId(), false));
	}

	public StatusInfoEntity(String facilityId,
			String monitorId,
			String monitorDetailId,
			String pluginId) {
		this(new StatusInfoEntityPK(facilityId, monitorId, monitorDetailId, pluginId));
	}

	public StatusInfoEntity(StatusInfoEntityPK pk, String ownerRoleId) {
		this.setId(pk);
		this.setObjectId(this.getId().getMonitorId());
		this.setOwnerRoleId(ownerRoleId);
	}


	@EmbeddedId
	public StatusInfoEntityPK getId() {
		return this.id;
	}

	public void setId(StatusInfoEntityPK id) {
		this.id = id;
	}


	public String getApplication() {
		return this.application;
	}

	public void setApplication(String application) {
		this.application = application;
	}


	@Column(name="expiration_date")
	public Timestamp getExpirationDate() {
		return this.expirationDate;
	}

	public void setExpirationDate(Timestamp expirationDate) {
		this.expirationDate = expirationDate;
	}


	@Column(name="expiration_flg")
	public Integer getExpirationFlg() {
		return this.expirationFlg;
	}

	public void setExpirationFlg(Integer expirationFlg) {
		this.expirationFlg = expirationFlg;
	}


	@Column(name="generation_date")
	public Timestamp getGenerationDate() {
		return this.generationDate;
	}

	public void setGenerationDate(Timestamp generationDate) {
		this.generationDate = generationDate;
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


	@Column(name="output_date")
	public Timestamp getOutputDate() {
		return this.outputDate;
	}

	public void setOutputDate(Timestamp outputDate) {
		this.outputDate = outputDate;
	}


	public Integer getPriority() {
		return this.priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

}