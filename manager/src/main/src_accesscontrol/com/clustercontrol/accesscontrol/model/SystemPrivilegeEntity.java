package com.clustercontrol.accesscontrol.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_system_privilege database table.
 * 
 */
@Entity
@Table(name="cc_system_privilege", schema="setting")
@Cacheable(true)
public class SystemPrivilegeEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private SystemPrivilegeEntityPK id;
	private RoleEntity roleEntity;

	public SystemPrivilegeEntity() {
	}

	public SystemPrivilegeEntity(SystemPrivilegeEntityPK pk, RoleEntity roleEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToRoleEntity(roleEntity);
	}

	public SystemPrivilegeEntity(RoleEntity roleEntity, String systemFunction, String systemPrivilege) {
		this(new SystemPrivilegeEntityPK(roleEntity.getRoleId(), systemFunction, systemPrivilege), roleEntity);
	}

	@EmbeddedId
	public SystemPrivilegeEntityPK getId() {
		return this.id;
	}

	public void setId(SystemPrivilegeEntityPK id) {
		this.id = id;
	}

	//bi-directional one-to-one association to RoleEntity
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="role_id", insertable=false, updatable=false)
	public RoleEntity getRoleEntity() {
		return this.roleEntity;
	}

	@Deprecated
	public void setRoleEntity(RoleEntity roleEntity) {
		this.roleEntity = roleEntity;
	}



	/**
	 * RoleEntityオブジェクト参照設定<BR>
	 * 
	 * RoleEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToRoleEntity(RoleEntity roleEntity) {
		this.setRoleEntity(roleEntity);
		if (roleEntity != null) {
			List<SystemPrivilegeEntity> list = roleEntity.getSystemPrivilegeEntities();
			if (list == null) {
				list = new ArrayList<SystemPrivilegeEntity>();
			} else {
				for(SystemPrivilegeEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			roleEntity.setSystemPrivilegeEntities(list);
		}
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

		// RoleEntity
		if (this.roleEntity != null) {
			List<SystemPrivilegeEntity> list = this.roleEntity.getSystemPrivilegeEntities();
			if (list != null) {
				Iterator<SystemPrivilegeEntity> iter = list.iterator();
				while(iter.hasNext()) {
					SystemPrivilegeEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
}