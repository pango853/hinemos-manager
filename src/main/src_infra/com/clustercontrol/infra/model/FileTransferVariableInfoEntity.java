package com.clustercontrol.infra.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;

@Entity
@Table(name="cc_infra_file_transfer_variable_info", schema="setting")
@Cacheable(true)
public class FileTransferVariableInfoEntity {
	private FileTransferVariableInfoEntityPK id;
	private String value;
	private FileTransferModuleInfoEntity fileTransferModuleInfoEntity;
	
	@Deprecated
	public FileTransferVariableInfoEntity() {
	}
	
	public FileTransferVariableInfoEntity(FileTransferModuleInfoEntity parent, String name) {
		this.setId(new FileTransferVariableInfoEntityPK(parent.getId().getManagementId(), parent.getId().getModuleId(), name));
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		setFileTransferModuleInfoEntity(parent);
		parent.getFileTransferVariableInfoEntities().add(this);
	}
	
	@EmbeddedId
	public FileTransferVariableInfoEntityPK getId() {
		return id;
	}
	public void setId(FileTransferVariableInfoEntityPK id) {
		this.id = id;
	}
	
	@Column(name="value")
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	//bi-directional many-to-one association to FileTransferModuleInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="management_id", referencedColumnName="management_id", insertable=false, updatable=false),
		@JoinColumn(name="module_id", referencedColumnName="module_id", insertable=false, updatable=false)
	})
	public FileTransferModuleInfoEntity getFileTransferModuleInfoEntity() {
		return this.fileTransferModuleInfoEntity;
	}
	
	@Deprecated
	public void setFileTransferModuleInfoEntity(FileTransferModuleInfoEntity fileTransferModuleInfoEntity) {
		this.fileTransferModuleInfoEntity = fileTransferModuleInfoEntity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileTransferVariableInfoEntity other = (FileTransferVariableInfoEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FileTransferVariableInfoEntity [id=" + id + ", value=" + value + "]";
	}

}
