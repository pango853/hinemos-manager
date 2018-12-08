package com.clustercontrol.repository.model;

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
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.repository.bean.FacilityConstant;


/**
 * The persistent class for the cc_cfg_facility database table.
 *
 */
@Entity
@Table(name="cc_cfg_facility", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.PLATFORM_REPOSITORY,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="facility_id", insertable=false, updatable=false))
public class FacilityEntity extends ObjectPrivilegeTargetEntity {
	private static final long serialVersionUID = 1L;
	private String facilityId;
	private Timestamp createDatetime	= new Timestamp(0);
	private String createUserId			= "";
	private String description			= "";
	private Integer displaySortOrder	= 0;
	private String facilityName			= "";
	private Integer facilityType		= FacilityConstant.TYPE_SCOPE;
	private Timestamp modifyDatetime	= new Timestamp(0);
	private String modifyUserId			= "";
	private Integer valid				= ValidConstant.TYPE_VALID;
	private String iconImage				= "";
	private NodeEntity nodeEntity;

	@Deprecated
	public FacilityEntity() {
	}

	public FacilityEntity(String facilityId) {
		this.setFacilityId(facilityId);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.setObjectId(this.getFacilityId());
	}


	@Id
	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
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


	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Column(name="display_sort_order")
	public Integer getDisplaySortOrder() {
		return this.displaySortOrder;
	}

	public void setDisplaySortOrder(Integer displaySortOrder) {
		this.displaySortOrder = displaySortOrder;
	}


	@Column(name="facility_name")
	public String getFacilityName() {
		return this.facilityName;
	}

	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}


	@Column(name="facility_type")
	public Integer getFacilityType() {
		return this.facilityType;
	}

	public void setFacilityType(Integer facilityType) {
		this.facilityType = facilityType;
	}

	@Column(name="icon_image")
	public String getIconImage() {
		return this.iconImage;
	}

	public void setIconImage(String iconImage) {
		this.iconImage = iconImage;
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


	public Integer getValid() {
		return this.valid;
	}

	public void setValid(Integer valid) {
		this.valid = valid;
	}

	//bi-directional one-to-one association to NodeEntity
	@OneToOne(mappedBy="facilityEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public NodeEntity getNodeEntity() {
		return this.nodeEntity;
	}

	public void setNodeEntity(NodeEntity nodeEntity) {
		this.nodeEntity = nodeEntity;
	}
}