package com.clustercontrol.accesscontrol.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_cfg_user database table.
 * 
 */
@Entity
@Table(name="cc_cfg_user", schema="setting")
@Cacheable(true)
public class UserEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String userId;
	private Timestamp createDatetime = new java.sql.Timestamp(0);
	private String createUserId = "";
	private String description = "";
	private Timestamp modifyDatetime = new java.sql.Timestamp(0);
	private String modifyUserId = "";
	private String password = "";
	private String userName = "";
	private String userType = "";
	private List<RoleEntity> roleEntities;

	@Deprecated
	public UserEntity() {
	}

	public UserEntity(String userId) {
		this.setUserId(userId);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}

	@Id
	@Column(name="user_id")
	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
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


	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	@Column(name="user_name")
	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}


	@Column(name="user_type")
	public String getUserType() {
		return this.userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	//bi-directional many-to-many association to RoleEntity
	@ManyToMany(mappedBy="userEntities")
	public List<RoleEntity> getRoleEntities() {
		return this.roleEntities;
	}

	public void setRoleEntities(List<RoleEntity> roleEntities) {
		this.roleEntities = roleEntities;
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

		// RoleEntities
		if (this.roleEntities != null && this.roleEntities.size() > 0) {
			for(RoleEntity roleEntity : this.roleEntities) {
				List<UserEntity> list = roleEntity.getUserEntities();
				if (list != null) {
					Iterator<UserEntity> iter = list.iterator();
					while(iter.hasNext()) {
						UserEntity entity = iter.next();
						if (entity.getUserId().equals(this.getUserId())){
							iter.remove();
							break;
						}
					}
				}
			}
		}
	}

}