package com.clustercontrol.infra.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.infra.bean.InfraCheckResult;

@Entity
@Table(name="cc_infra_check_result_info", schema="setting")
@Cacheable(true)
public class InfraCheckResultEntity {
	private InfraCheckResultEntityPK id;
	private int result;

	@Deprecated
	public InfraCheckResultEntity() {
	}

	public InfraCheckResultEntity(String managementId, String moduleId, String facilityId) {
		this.setId(new InfraCheckResultEntityPK(managementId, moduleId, facilityId));
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}
	
	@EmbeddedId
	public InfraCheckResultEntityPK getId() {
		return id;
	}
	public void setId(InfraCheckResultEntityPK id) {
		this.id = id;
	}

	@Column(name="result")
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	
	public InfraCheckResult createWebEntity() throws InvalidRole, HinemosUnknown {
		InfraCheckResult result = new InfraCheckResult();
		result.setManagementId(getId().getManagementId());
		result.setModuleId(getId().getModuleId());
		result.setNodeId(getId().getNodeId());
		result.setResult(getResult());
		return result;
	}
	
	public void removeSelf() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.remove(this);
	}
}
