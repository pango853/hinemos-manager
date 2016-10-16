package com.clustercontrol.infra.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetEntity;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.infra.bean.InfraManagementInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.repository.session.RepositoryControllerBean;


@Entity
@Table(name="cc_infra_management_info", schema="setting")
@HinemosObjectPrivilege(objectType=HinemosModuleConstant.INFRA, isModifyCheck=true)
@AttributeOverride(name="objectId", column=@Column(name="management_id", insertable=false, updatable=false))
@Cacheable(true)
public class InfraManagementInfoEntity extends ObjectPrivilegeTargetEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String managementId;
	private String name;
	private String description;
	private String facilityId;
	private String notifyGroupId;
	private int validFlg;
	
	private int startPriority;
	private int normalPriorityRun;
	private int abnormalPriorityRun;
	private int normalPriorityCheck;
	private int abnormalPriorityCheck;
	
	private List<InfraModuleInfoEntity<?>> infraModuleInfoEntities;

	private Timestamp regDate;
	private String regUser;
	private Timestamp updateDate;
	private String updateUser;
	
	public InfraManagementInfoEntity(String managementId, String name, String facilityId) {
		this.setManagementId(managementId);
		this.setName(name);
		this.setFacilityId(facilityId);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}

	public InfraManagementInfoEntity() {
	}
	
	@Id
	@Column(name="management_id")
	public String getManagementId() {
		return managementId;
	}
	public void setManagementId(String managementId) {
		this.managementId = managementId;
		this.setObjectId(this.managementId);
	}
	
	@Column(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Column(name="facility_id")
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	
	@Column(name="notify_group_id")
	public String getNotifyGroupId() {
		return notifyGroupId;
	}
	public void setNotifyGroupId(String notifyGroupId) {
		this.notifyGroupId = notifyGroupId;
	}
	
	@Column(name="valid_flg")
	public int getValidFlg() {
		return validFlg;
	}
	public void setValidFlg(int validFlg) {
		this.validFlg = validFlg;
	}
	
	@Column(name="start_priority")
	public int getStartPriority() {
		return startPriority;
	}
	public void setStartPriority(int startPriority) {
		this.startPriority = startPriority;
	}
	
	@Column(name="normal_priority_run")
	public int getNormalPriorityRun() {
		return normalPriorityRun;
	}
	public void setNormalPriorityRun(int normalPriorityRun) {
		this.normalPriorityRun = normalPriorityRun;
	}
	
	@Column(name="abnormal_priority_run")
	public int getAbnormalPriorityRun() {
		return abnormalPriorityRun;
	}
	public void setAbnormalPriorityRun(int abnormalPriorityRun) {
		this.abnormalPriorityRun = abnormalPriorityRun;
	}
	
	@Column(name="normal_priority_check")
	public int getNormalPriorityCheck() {
		return normalPriorityCheck;
	}
	public void setNormalPriorityCheck(int normalPriorityCheck) {
		this.normalPriorityCheck = normalPriorityCheck;
	}
	
	@Column(name="abnormal_priority_check")
	public int getAbnormalPriorityCheck() {
		return abnormalPriorityCheck;
	}
	public void setAbnormalPriorityCheck(int abnormalPriorityCheck) {
		this.abnormalPriorityCheck = abnormalPriorityCheck;
	}
	
	@OneToMany(mappedBy="infraManagementInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<InfraModuleInfoEntity<?>> getInfraModuleInfoEntities() {
		return this.infraModuleInfoEntities;
	}
	public void setInfraModuleInfoEntities(List<InfraModuleInfoEntity<?>> infraModuleInfoEntities) {
		this.infraModuleInfoEntities = infraModuleInfoEntities;
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
	
	public void removeSelf() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		for (InfraModuleInfoEntity<?> module: getInfraModuleInfoEntities()) {
			module.removeSelf();
		}
		getInfraModuleInfoEntities().clear();
		em.remove(this);
	}
	
	public InfraManagementInfo createWebEntity() throws InvalidRole, HinemosUnknown {
		InfraManagementInfo webEntity = new InfraManagementInfo();
		webEntity.setManagementId(getManagementId());
		webEntity.setName(getName());
		webEntity.setDescription(getDescription());
		webEntity.setFacilityId(getFacilityId());
		webEntity.setScope(new RepositoryControllerBean().getFacilityPath(getFacilityId(), null));
		webEntity.setOwnerRoleId(getOwnerRoleId());
		webEntity.setStartPriority(getStartPriority());
		webEntity.setNormalPriorityCheck(getNormalPriorityCheck());
		webEntity.setAbnormalPriorityCheck(getAbnormalPriorityCheck());
		webEntity.setNormalPriorityRun(getNormalPriorityRun());
		webEntity.setAbnormalPriorityRun(getAbnormalPriorityRun());
		webEntity.setRegDate(getRegDate().getTime());
		webEntity.setRegUser(getRegUser());
		webEntity.setUpdateDate(getUpdateDate().getTime());
		webEntity.setUpdateUser(getUpdateUser());
		webEntity.setValidFlg(ValidConstant.typeToBoolean(getValidFlg()));
		webEntity.setNotifyGroupID(getNotifyGroupId());
		
		List<InfraModuleInfoEntity<?>> modules = new ArrayList<>(getInfraModuleInfoEntities());
		Collections.sort(modules, new Comparator<InfraModuleInfoEntity<?>>() {
			@Override
			public int compare(InfraModuleInfoEntity<?> o1, InfraModuleInfoEntity<?> o2) {
				return Integer.valueOf(o1.getOrderNo()).compareTo(o2.getOrderNo());
			}
		});
		for (InfraModuleInfoEntity<?> module: modules) {
			module.addAsWebEntity(webEntity);
		}
		
		webEntity.setNotifyRelationList(new NotifyControllerBean().getNotifyRelation(getNotifyGroupId()));
		
		return webEntity;
		
	}
}
