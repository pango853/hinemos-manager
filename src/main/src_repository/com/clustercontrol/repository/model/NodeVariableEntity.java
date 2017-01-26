package com.clustercontrol.repository.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_cfg_node_variable database table.
 * 
 */
@Entity
@Table(name="cc_cfg_node_variable", schema="setting")
@Cacheable(true)
public class NodeVariableEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private NodeVariableEntityPK id;
	private String nodeVariableValue			= "";
	private NodeEntity nodeEntity;

	@Deprecated
	public NodeVariableEntity() {
	}

	public NodeVariableEntity(NodeVariableEntityPK pk,
			NodeEntity nodeEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToNodeEntity(nodeEntity);
	}

	public NodeVariableEntity(NodeEntity nodeEntity, String nodeVariableName) {
		this(new NodeVariableEntityPK(nodeEntity.getFacilityId(), nodeVariableName), nodeEntity);
	}


	@EmbeddedId
	public NodeVariableEntityPK getId() {
		return this.id;
	}

	public void setId(NodeVariableEntityPK id) {
		this.id = id;
	}


	@Column(name="node_variable_value")
	public String getNodeVariableValue() {
		return this.nodeVariableValue;
	}

	public void setNodeVariableValue(String nodeVariableValue) {
		this.nodeVariableValue = nodeVariableValue;
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
			List<NodeVariableEntity> list = nodeEntity.getNodeVariableEntities();
			if (list == null) {
				list = new ArrayList<NodeVariableEntity>();
			} else {
				for(NodeVariableEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			nodeEntity.setNodeVariableEntities(list);
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
			List<NodeVariableEntity> list = this.nodeEntity.getNodeVariableEntities();
			if (list != null) {
				Iterator<NodeVariableEntity> iter = list.iterator();
				while(iter.hasNext()) {
					NodeVariableEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}