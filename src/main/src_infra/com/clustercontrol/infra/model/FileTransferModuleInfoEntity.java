package com.clustercontrol.infra.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.infra.bean.FileTransferModuleInfo;
import com.clustercontrol.infra.bean.FileTransferVariableInfo;
import com.clustercontrol.infra.bean.InfraManagementInfo;

@Entity
@Table(name="cc_infra_file_transfer_module_info", schema="setting")
@Inheritance
@DiscriminatorValue(FileTransferModuleInfoEntity.typeName)
@Cacheable(true)
public class FileTransferModuleInfoEntity extends InfraModuleInfoEntity<FileTransferModuleInfo> {
	public static final String typeName = "FileTransferModule";

	private String destPath;
	private int sendMethodType;
	private String destOwner;
	private String destAttribute;
	private int backupIfExistFlg;
	private InfraFileEntity infraFileEntity;
	
	private List<FileTransferVariableInfoEntity> fileTransferVariableInfoEntities;
	
	public FileTransferModuleInfoEntity() {
	}

	public FileTransferModuleInfoEntity(InfraManagementInfoEntity parent, String moduleId) {
		super(parent, moduleId);
	}
	
	@Column(name="dest_path")
	public String getDestPath() {
		return destPath;
	}
	public void setDestPath(String destPath) {
		this.destPath = destPath;
	}
	
	@Column(name="send_method_type")
	public int getSendMethodType() {
		return sendMethodType;
	}
	public void setSendMethodType(int sendMethodType) {
		this.sendMethodType = sendMethodType;
	}

	@Column(name="dest_owner")
	public String getDestOwner() {
		return destOwner;
	}
	public void setDestOwner(String destOwner) {
		this.destOwner = destOwner;
	}
	
	@Column(name="dest_attribute")
	public String getDestAttribute() {
		return destAttribute;
	}
	public void setDestAttribute(String destAttribute) {
		this.destAttribute = destAttribute;
	}
	
	@OneToMany(mappedBy="fileTransferModuleInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<FileTransferVariableInfoEntity> getFileTransferVariableInfoEntities() {
		return this.fileTransferVariableInfoEntities;
	}
	public void setFileTransferVariableInfoEntities(List<FileTransferVariableInfoEntity> fileTransferVariableInfoEntities) {
		this.fileTransferVariableInfoEntities = fileTransferVariableInfoEntities;
	}

	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="file_id")
	public InfraFileEntity getInfraFileEntity() {
		return this.infraFileEntity;
	}
	public void setInfraFileEntity(InfraFileEntity infraFileEntity) {
		this.infraFileEntity = infraFileEntity;
	}
	
	@Column(name="backup_if_exist_flg")
	public int getBackupIfExistFlg() {
		return backupIfExistFlg;
	}
	public void setBackupIfExistFlg(int backupIfExistFlg) {
		this.backupIfExistFlg = backupIfExistFlg;
	}

	@Override
	public String getModuleTypeName() {
		return typeName;
	}

	@Override
	protected Class<FileTransferModuleInfo> getWebEntityClass() {
		return FileTransferModuleInfo.class;
	}

	@Override
	protected void overwriteWebEntity(InfraManagementInfo management, FileTransferModuleInfo module) {
		module.setDestOwner(getDestOwner());
		module.setDestAttribute(getDestAttribute());
		module.setDestPath(getDestPath());
		module.setSendMethodType(getSendMethodType());
		module.setPrecheckFlg(ValidConstant.typeToBoolean(getPrecheckFlg()));
		module.setBackupIfExistFlg(ValidConstant.typeToBoolean(getBackupIfExistFlg()));
		module.setFileId(getInfraFileEntity().getFileId());

		List<FileTransferVariableInfo> webVariableList = new ArrayList<FileTransferVariableInfo>(module.getFileTransferVariableList());
		List<FileTransferVariableInfoEntity> dbVariableList = new ArrayList<FileTransferVariableInfoEntity>(getFileTransferVariableInfoEntities());
		
		Iterator<FileTransferVariableInfo> webVariableIter = webVariableList.iterator();
		while (webVariableIter.hasNext()) {
			FileTransferVariableInfo webVariable = webVariableIter.next();
			
			Iterator<FileTransferVariableInfoEntity> dbVariableIter = dbVariableList.iterator();
			while (dbVariableIter.hasNext()) {
				FileTransferVariableInfoEntity dbVariable = dbVariableIter.next();
				if (webVariable.getName().equals(dbVariable.getId().getName())) {
					webVariable.setValue(dbVariable.getValue());
					
					webVariableIter.remove();
					dbVariableIter.remove();
					break;
				}
			}
		}
		
		for (FileTransferVariableInfoEntity dbVariable: dbVariableList) {
			module.getFileTransferVariableList().add(new FileTransferVariableInfo(dbVariable.getId().getName(), dbVariable.getValue()));
		}
		
		for (FileTransferVariableInfo webVariable: webVariableList) {
			module.getFileTransferVariableList().remove(webVariable);
		}
	}

	@Override
	public void removeSelf() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		Iterator<FileTransferVariableInfoEntity> iter = getFileTransferVariableInfoEntities().iterator();
		while (iter.hasNext()) {
			FileTransferVariableInfoEntity v = iter.next();
			iter.remove();
			em.remove(v);
		}
		super.removeSelf();
	}
}