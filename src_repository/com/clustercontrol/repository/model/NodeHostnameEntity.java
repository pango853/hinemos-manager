package com.clustercontrol.repository.model;

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
 * The persistent class for the cc_cfg_node_hostname database table.
 * 
 */
@Entity
@Table(name="cc_cfg_node_hostname", schema="setting")
@Cacheable(true)
public class NodeHostnameEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private NodeHostnameEntityPK id;
	private NodeEntity nodeEntity;

	@Deprecated
	public NodeHostnameEntity() {
	}

	public NodeHostnameEntity(NodeHostnameEntityPK pk,
			NodeEntity nodeEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToNodeEntity(nodeEntity);
	}

	public NodeHostnameEntity(NodeEntity nodeEntity, String hostname) {
		this(new NodeHostnameEntityPK(nodeEntity.getFacilityId(), hostname), nodeEntity);
	}


	@EmbeddedId
	public NodeHostnameEntityPK getId() {
		return this.id;
	}

	public void setId(NodeHostnameEntityPK id) {
		this.id = id;
	}


	//bi-directional many-to-one association to NodeEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="facility_id", insertable=false, updatable=false)
	public NodeEntity getNodeEntity() {
		return this.nodeEntity;
	}

	@Deprecated
	public void setNodeEntity(NodeEntity nodeEntity) {
		this.nodeEntity = nodeEntity;
	}

	/**
	 * NodeEntityオブジェクト参照設定<BR>
	 * 
	 * NodeEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToNodeEntity(NodeEntity nodeEntity) {
		this.setNodeEntity(nodeEntity);
		if (nodeEntity != null) {
			List<NodeHostnameEntity> list = nodeEntity.getNodeHostnameEntities();
			if (list == null) {
				list = new ArrayList<NodeHostnameEntity>();
			} else {
				for(NodeHostnameEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			nodeEntity.setNodeHostnameEntities(list);
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

		// NodeEntity
		if (this.nodeEntity != null) {
			List<NodeHostnameEntity> list = this.nodeEntity.getNodeHostnameEntities();
			if (list != null) {
				Iterator<NodeHostnameEntity> iter = list.iterator();
				while(iter.hasNext()) {
					NodeHostnameEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}