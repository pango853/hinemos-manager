package com.clustercontrol.accesscontrol.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * オブジェクト権限チェックを行うクラスのMappedSuperClass
 * 
 */
@MappedSuperclass
@EntityListeners(value={EntityListener.class})
public class ObjectPrivilegeTargetEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String objectId;
	private String ownerRoleId;
	private boolean uncheckFlg = false;

	public ObjectPrivilegeTargetEntity() {
	}

	public String getObjectId() {
		return this.objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	@Column(name="owner_role_id")
	public String getOwnerRoleId() {
		return this.ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public boolean tranGetUncheckFlg() {
		return uncheckFlg;
	}

	public void tranSetUncheckFlg(boolean uncheckFlg) {
		this.uncheckFlg = uncheckFlg;
	}
}