package com.clustercontrol.accesscontrol.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
/**
 * The persistent class for the cc_cfg_role database table.
 * 
 */
@Entity
@Table(name="cc_cfg_role", schema="setting")
@Cacheable(true)
public class RoleEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String roleId;
	private Timestamp createDatetime = new java.sql.Timestamp(0);
	private String createUserId = "";
	private String description = "";
	private Timestamp modifyDatetime = new java.sql.Timestamp(0);
	private String modifyUserId = "";
	private String roleName = "";
	private String roleType = "";
	private List<UserEntity> userEntities;
	private List<SystemPrivilegeEntity> systemPrivilegeEntities;

	@Deprecated
	public RoleEntity() {
	}

	public RoleEntity(String roleId) {
		this.setRoleId(roleId);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}

	@Id
	@Column(name="role_id")
	public String getRoleId() {
		return this.roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
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


	@Column(name="role_name")
	public String getRoleName() {
		return this.roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}


	@Column(name="role_type")
	public String getRoleType() {
		return this.roleType;
	}

	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}


	//bi-directional many-to-many association to UserEntity
	@ManyToMany
	@OrderBy("userId ASC")
	@JoinTable(
			name="cc_cfg_user_role_relation"
			,schema="setting"
			, joinColumns={
					@JoinColumn(name="role_id", referencedColumnName="role_id")
			}
			, inverseJoinColumns={
					@JoinColumn(name="user_id", referencedColumnName="user_id")
			}
			)
	public List<UserEntity> getUserEntities() {
		return this.userEntities;
	}

	public void setUserEntities(List<UserEntity> userEntities) {
		this.userEntities = userEntities;
	}

	//bi-directional many-to-one association to SystemPrivilegeEntity
	@OneToMany(mappedBy="roleEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<SystemPrivilegeEntity> getSystemPrivilegeEntities() {
		return this.systemPrivilegeEntities;
	}

	public void setSystemPrivilegeEntities(List<SystemPrivilegeEntity> systemPrivilegeEntities) {
		this.systemPrivilegeEntities = systemPrivilegeEntities;
	}

	/**
	 * 削除前処理<BR>
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void unchain() {

		// UserEntities
		if (this.userEntities != null && this.userEntities.size() > 0) {
			for(UserEntity userEntity : this.userEntities) {
				List<RoleEntity> list = userEntity.getRoleEntities();
				if (list != null) {
					Iterator<RoleEntity> iter = list.iterator();
					while(iter.hasNext()) {
						RoleEntity entity = iter.next();
						if (entity.getRoleId().equals(this.getRoleId())){
							iter.remove();
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * SystemPrivilegeEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteSystemPrivilegeEntities(List<SystemPrivilegeEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<SystemPrivilegeEntity> list = this.getSystemPrivilegeEntities();
		Iterator<SystemPrivilegeEntity> iter = list.iterator();
		while(iter.hasNext()) {
			SystemPrivilegeEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}

}