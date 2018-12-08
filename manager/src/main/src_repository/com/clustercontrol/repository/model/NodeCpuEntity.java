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
 * The persistent class for the cc_cfg_node_cpu database table.
 * 
 */
@Entity
@Table(name="cc_cfg_node_cpu", schema="setting")
@Cacheable(true)
public class NodeCpuEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private NodeCpuEntityPK id;
	private String deviceDescription			= "";
	private String deviceDisplayName			= "";
	private Integer deviceSize					= 0;
	private String deviceSizeUnit				= "";
	private NodeEntity nodeEntity;

	@Deprecated
	public NodeCpuEntity() {
	}

	public NodeCpuEntity(NodeCpuEntityPK pk,
			NodeEntity nodeEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToNodeEntity(nodeEntity);
	}

	public NodeCpuEntity(NodeEntity nodeEntity,
			Integer deviceIndex,
			String deviceType,
			String deviceName) {
		this(new NodeCpuEntityPK(
				nodeEntity.getFacilityId(),
				deviceIndex,
				deviceType,
				deviceName), nodeEntity);
	}

	@EmbeddedId
	public NodeCpuEntityPK getId() {
		return this.id;
	}

	public void setId(NodeCpuEntityPK id) {
		this.id = id;
	}


	@Column(name="device_description")
	public String getDeviceDescription() {
		return this.deviceDescription;
	}

	public void setDeviceDescription(String deviceDescription) {
		this.deviceDescription = deviceDescription;
	}


	@Column(name="device_display_name")
	public String getDeviceDisplayName() {
		return this.deviceDisplayName;
	}

	public void setDeviceDisplayName(String deviceDisplayName) {
		this.deviceDisplayName = deviceDisplayName;
	}


	@Column(name="device_size")
	public Integer getDeviceSize() {
		return this.deviceSize;
	}

	public void setDeviceSize(Integer deviceSize) {
		this.deviceSize = deviceSize;
	}


	@Column(name="device_size_unit")
	public String getDeviceSizeUnit() {
		return this.deviceSizeUnit;
	}

	public void setDeviceSizeUnit(String deviceSizeUnit) {
		this.deviceSizeUnit = deviceSizeUnit;
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
			List<NodeCpuEntity> list = nodeEntity.getNodeCpuEntities();
			if (list == null) {
				list = new ArrayList<NodeCpuEntity>();
			} else {
				for(NodeCpuEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			nodeEntity.setNodeCpuEntities(list);
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
			List<NodeCpuEntity> list = this.nodeEntity.getNodeCpuEntities();
			if (list != null) {
				Iterator<NodeCpuEntity> iter = list.iterator();
				while(iter.hasNext()) {
					NodeCpuEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}