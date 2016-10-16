package com.clustercontrol.maintenance.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;



/**
 * The persistent class for the cc_maintenance_type_mst database table.
 * 
 */
@Entity
@Table(name="cc_maintenance_type_mst", schema="setting")
@Cacheable(true)
public class MaintenanceTypeMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String typeId;
	private String nameId;
	private Integer orderNo;
	private List<MaintenanceInfoEntity> maintenanceInfoEntities;

	@Deprecated
	public MaintenanceTypeMstEntity() {
	}

	public MaintenanceTypeMstEntity(String typeId) {
		this.setTypeId(typeId);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}


	@Id
	@Column(name="type_id")
	public String getTypeId() {
		return this.typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}


	@Column(name="name_id")
	public String getNameId() {
		return this.nameId;
	}

	public void setNameId(String nameId) {
		this.nameId = nameId;
	}


	@Column(name="order_no")
	public Integer getOrderNo() {
		return this.orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}


	//bi-directional many-to-one association to MaintenanceInfoEntity
	@OneToMany(mappedBy="maintenanceTypeMstEntity", fetch=FetchType.LAZY)
	public List<MaintenanceInfoEntity> getMaintenanceInfoEntities() {
		return this.maintenanceInfoEntities;
	}

	public void setMaintenanceInfoEntities(List<MaintenanceInfoEntity> maintenanceInfoEntities) {
		this.maintenanceInfoEntities = maintenanceInfoEntities;
	}

}