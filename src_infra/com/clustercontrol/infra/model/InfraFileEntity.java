package com.clustercontrol.infra.model;

import java.sql.Timestamp;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetEntity;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.infra.bean.InfraFileInfo;


@Entity
@Table(name="cc_infra_file", schema="setting")
@HinemosObjectPrivilege(objectType=HinemosModuleConstant.INFRA_FILE, isModifyCheck=true)
@AttributeOverride(name="objectId", column=@Column(name="file_id", insertable=false, updatable=false))
@Cacheable(true)
public class InfraFileEntity extends ObjectPrivilegeTargetEntity {
	private static final long serialVersionUID = 1L;
	
	private String fileId;
	private String fileName;
	private String createUserId;
	private Timestamp createDatetime;
	private String modifyUserId;
	private Timestamp modifyDatetime;
	private InfraFileContentEntity infraFileContentEntity;
	
	public InfraFileEntity() {
	}
	
	public InfraFileEntity(String fileId, String fileName) {
		this.fileId = fileId;
		this.fileName = fileName;
		
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}

	@Id
	@Column(name="file_id")
	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	@Column(name="file_name")
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Column(name="create_user_id")
	public String getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}

	@Column(name="create_datetime")
	public Timestamp getCreateDatetime() {
		return createDatetime;
	}

	public void setCreateDatetime(Timestamp createDatetime) {
		this.createDatetime = createDatetime;
	}

	@Column(name="modify_user_id")
	public String getModifyUserId() {
		return modifyUserId;
	}

	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
	}

	@Column(name="modify_datetime")
	public Timestamp getModifyDatetime() {
		return modifyDatetime;
	}

	public void setModifyDatetime(Timestamp modifyDatetime) {
		this.modifyDatetime = modifyDatetime;
	}
	
	@OneToOne(fetch=FetchType.LAZY)
	@PrimaryKeyJoinColumn(name="file_id")
	public InfraFileContentEntity getInfraFileContentEntity() {
		return this.infraFileContentEntity;
	}
	public void setInfraFileContentEntity(InfraFileContentEntity infraFileContentEntity) {
		this.infraFileContentEntity = infraFileContentEntity;
	}

	public InfraFileInfo createWebEntity() {
		InfraFileInfo info = new InfraFileInfo();
		info.setFileId(fileId);
		info.setFileName(fileName);
		info.setOwnerRoleId(getOwnerRoleId());
		info.setCreateDatetime(createDatetime.getTime());
		info.setCreateUserId(createUserId);
		info.setModifyDatetime(modifyDatetime.getTime());
		info.setModifyUserId(modifyUserId);
		
		return info;
	}
}