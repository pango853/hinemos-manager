package com.clustercontrol.repository.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_cfg_facility_relation database table.
 * 
 */
@Entity
@Table(name="cc_cfg_facility_relation", schema="setting")
@Cacheable(true)
public class FacilityRelationEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private FacilityRelationEntityPK id;

	@Deprecated
	public FacilityRelationEntity() {
	}

	public FacilityRelationEntity(FacilityRelationEntityPK pk) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}

	public FacilityRelationEntity(String parentFacilityId, String childFacilityId) {
		this(new FacilityRelationEntityPK(parentFacilityId, childFacilityId));
	}

	@EmbeddedId
	public FacilityRelationEntityPK getId() {
		return this.id;
	}

	public void setId(FacilityRelationEntityPK id) {
		this.id = id;
	}

}