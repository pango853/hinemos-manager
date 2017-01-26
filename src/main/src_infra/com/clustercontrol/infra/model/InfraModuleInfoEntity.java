package com.clustercontrol.infra.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.infra.bean.InfraManagementInfo;
import com.clustercontrol.infra.bean.InfraModuleInfo;

@Entity
@Table(name="cc_infra_module_info", schema="setting")
@Inheritance
@DiscriminatorColumn(name="module_type")
@Cacheable(true)
public abstract class InfraModuleInfoEntity<E extends InfraModuleInfo<?>> {
	private InfraModuleInfoEntityPK id;
	private String name;
	private int orderNo;
	private int validFlg;
	private int stopIfFailFlg;
	private int precheckFlg;

	private InfraManagementInfoEntity infraManagementInfoEntity;
	
	public InfraModuleInfoEntity() {
	}

	public InfraModuleInfoEntity(InfraManagementInfoEntity parent, String moduleId) {
		this.setId(new InfraModuleInfoEntityPK(parent.getManagementId(), moduleId));
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		setInfraManagementInfoEntity(parent);
		parent.getInfraModuleInfoEntities().add(this);
	}
	
	@EmbeddedId
	public InfraModuleInfoEntityPK getId() {
		return id;
	}
	public void setId(InfraModuleInfoEntityPK id) {
		this.id = id;
	}
	
	@Column(name="order_no")
	public int getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(int orderNo) {
		this.orderNo = orderNo;
	}

	@Column(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="management_id", insertable=false, updatable=false)
	public InfraManagementInfoEntity getInfraManagementInfoEntity() {
		return this.infraManagementInfoEntity;
	}
	public void setInfraManagementInfoEntity(InfraManagementInfoEntity infraManagementInfoEntity) {
		this.infraManagementInfoEntity = infraManagementInfoEntity;
	}
	
	@Column(name="valid_flg")
	public int getValidFlg() {
		return validFlg;
	}
	public void setValidFlg(int validFlg) {
		this.validFlg = validFlg;
	}

	@Column(name="stop_if_fail_flg")
	public int getStopIfFailFlg() {
		return stopIfFailFlg;
	}
	public void setStopIfFailFlg(int stopIfFailFlg) {
		this.stopIfFailFlg = stopIfFailFlg;
	}

	@Column(name="precheck_flg")
	public int getPrecheckFlg() {
		return precheckFlg;
	}
	public void setPrecheckFlg(int precheckFlg) {
		this.precheckFlg = precheckFlg;
	}
	
	public abstract String getModuleTypeName();
	
	protected abstract Class<E> getWebEntityClass();

	protected abstract void overwriteWebEntity(InfraManagementInfo management, E module);

	public void addAsWebEntity(InfraManagementInfo management) throws HinemosUnknown {
		management.getModuleList().add(createWebEntity(management));
	}

	public E createWebEntity(InfraManagementInfo management) throws HinemosUnknown {
		try {
			E module = (E)getWebEntityClass().newInstance();

			module.setModuleId(getId().getModuleId());
			module.setName(getName());
			module.setValidFlg(ValidConstant.typeToBoolean(getValidFlg()));
			module.setStopIfFailFlg(ValidConstant.typeToBoolean(getStopIfFailFlg()));
			overwriteWebEntity(management, module);
			
			return module;
		}
		catch (InstantiationException | IllegalAccessException e) {
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}
	
	public void removeSelf() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		setInfraManagementInfoEntity(null);
		em.remove(this);
	}
}
