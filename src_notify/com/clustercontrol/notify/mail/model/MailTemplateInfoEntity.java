package com.clustercontrol.notify.mail.model;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetEntity;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.notify.model.NotifyMailInfoEntity;


/**
 * The persistent class for the cc_mail_template_info database table.
 * 
 */
@Entity
@Table(name="cc_mail_template_info", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="mail_template_id", insertable=false, updatable=false))
public class MailTemplateInfoEntity extends ObjectPrivilegeTargetEntity {
	private static final long serialVersionUID = 1L;
	private String mailTemplateId;
	private String body;
	private String description;
	private Timestamp regDate;
	private String regUser;
	private String subject;
	private Timestamp updateDate;
	private String updateUser;
	private List<NotifyMailInfoEntity> notifyMailInfoEntities;

	@Deprecated
	public MailTemplateInfoEntity() {
	}

	public MailTemplateInfoEntity(String mailTemplateId) {
		this.setMailTemplateId(mailTemplateId);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.setObjectId(this.getMailTemplateId());
	}

	@Id
	@Column(name="mail_template_id")
	public String getMailTemplateId() {
		return this.mailTemplateId;
	}

	public void setMailTemplateId(String mailTemplateId) {
		this.mailTemplateId = mailTemplateId;
	}


	public String getBody() {
		return this.body;
	}

	public void setBody(String body) {
		this.body = body;
	}


	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
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


	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
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


	//bi-directional many-to-one association to NotifyMailInfoEntity
	@OneToMany(mappedBy="mailTemplateInfoEntity", fetch=FetchType.LAZY)
	public List<NotifyMailInfoEntity> getNotifyMailInfoEntities() {
		return this.notifyMailInfoEntities;
	}

	public void setNotifyMailInfoEntities(List<NotifyMailInfoEntity> notifyMailInfoEntities) {
		this.notifyMailInfoEntities = notifyMailInfoEntities;
	}

}