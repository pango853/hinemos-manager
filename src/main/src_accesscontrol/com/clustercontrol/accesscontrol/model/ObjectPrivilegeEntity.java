package com.clustercontrol.accesscontrol.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;



/**
 * The persistent class for the cc_object_privilege database table.
 * 
 */
@Entity
@Table(name="cc_object_privilege", schema="setting")
@Cacheable(true)
public class ObjectPrivilegeEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private ObjectPrivilegeEntityPK id;
	private Timestamp createDatetime = new java.sql.Timestamp(0);
	private String createUserId = "";
	private Timestamp modifyDatetime = new java.sql.Timestamp(0);
	private String modifyUserId = "";

	public ObjectPrivilegeEntity() {
	}

	public ObjectPrivilegeEntity(ObjectPrivilegeEntityPK pk) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}

	public ObjectPrivilegeEntity(String objectType, String objectId, String userId, String objectPrivilege) {
		this(new ObjectPrivilegeEntityPK(objectType, objectId, userId, objectPrivilege));
	}

	@EmbeddedId
	public ObjectPrivilegeEntityPK getId() {
		return this.id;
	}

	public void setId(ObjectPrivilegeEntityPK id) {
		this.id = id;
	}


	@Column(name="create_datetime")
	public Timestamp getCreateDatetime() {
		return this.createDatetime;
	}

	public void setCreateDatetime(Timestamp createDatetime) {
		this.createDatetime = createDatetime;
	}


	@Column(name="create_user_id")
	public String getCreateUserId() {
		return this.createUserId;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}


	@Column(name="modify_datetime")
	public Timestamp getModifyDatetime() {
		return this.modifyDatetime;
	}

	public void setModifyDatetime(Timestamp modifyDatetime) {
		this.modifyDatetime = modifyDatetime;
	}


	@Column(name="modify_user_id")
	public String getModifyUserId() {
		return this.modifyUserId;
	}

	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
	}
}