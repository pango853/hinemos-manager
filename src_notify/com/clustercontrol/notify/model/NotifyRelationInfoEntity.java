package com.clustercontrol.notify.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_notify_relation_info database table.
 * 
 */
@Entity
@Table(name="cc_notify_relation_info", schema="setting")
@Cacheable(true)
public class NotifyRelationInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private NotifyRelationInfoEntityPK id;
	private Integer notifyType;

	@Deprecated
	public NotifyRelationInfoEntity() {
	}

	public NotifyRelationInfoEntity(NotifyRelationInfoEntityPK pk) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}

	public NotifyRelationInfoEntity(String notifyGroupId, String notifyId) {
		this(new NotifyRelationInfoEntityPK(notifyGroupId, notifyId));
	}


	@EmbeddedId
	public NotifyRelationInfoEntityPK getId() {
		return this.id;
	}

	public void setId(NotifyRelationInfoEntityPK id) {
		this.id = id;
	}

	@Column(name="notify_type")
	public Integer getNotifyType() {
		return this.notifyType;
	}

	public void setNotifyType(Integer notifyType) {
		this.notifyType = notifyType;
	}

}