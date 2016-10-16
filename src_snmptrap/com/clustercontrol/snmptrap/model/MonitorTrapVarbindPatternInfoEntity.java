package com.clustercontrol.snmptrap.model;

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
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;

/**
 * The persistent class for the cc_monitor_trap_varbind_pattern_info database table.
 * 
 */
@Entity
@Table(name="cc_monitor_trap_varbind_pattern_info", schema="setting")
@Cacheable(true)
public class MonitorTrapVarbindPatternInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	private MonitorTrapVarbindPatternInfoEntityPK id;
	private String description;
	private Integer processType;
	private String pattern;
	private Integer priority;
	private Integer caseSensitivityFlg;
	private Integer validFlg;
	private MonitorTrapValueInfoEntity monitorTrapValueInfoEntity;

	@Deprecated
	public MonitorTrapVarbindPatternInfoEntity() {
	}

	public MonitorTrapVarbindPatternInfoEntity(
			MonitorTrapValueInfoEntity trapValueInfoEntity,
			Integer orderNo
			) {
		this.setId(new MonitorTrapVarbindPatternInfoEntityPK(
				trapValueInfoEntity.getId().getMonitorId(),
				trapValueInfoEntity.getId().getMib(),
				trapValueInfoEntity.getId().getTrapOid(),
				trapValueInfoEntity.getId().getGenericId(),
				trapValueInfoEntity.getId().getSpecificId(),
				orderNo));
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		relateToMonitorTrapValueInfoEntity(trapValueInfoEntity);
	}

	@EmbeddedId
	public MonitorTrapVarbindPatternInfoEntityPK getId() {
		return id;
	}
	public void setId(MonitorTrapVarbindPatternInfoEntityPK id) {
		this.id = id;
	}

	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="process_type")
	public Integer getProcessType() {
		return processType;
	}
	public void setProcessType(Integer processType) {
		this.processType = processType;
	}

	@Column(name="pattern")
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	@Column(name="priority")
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	@Column(name="case_sensitivity_flg")
	public Integer getCaseSensitivityFlg() {
		return caseSensitivityFlg;
	}
	public void setCaseSensitivityFlg(Integer caseSensitivityFlg) {
		this.caseSensitivityFlg = caseSensitivityFlg;
	}

	@Column(name="valid_flg")
	public Integer getValidFlg() {
		return validFlg;
	}
	public void setValidFlg(Integer validFlg) {
		this.validFlg = validFlg;
	}

	@Override
	public String toString() {
		return "MonitorTrapVarbindPatternInfoEntity [id=" + id
				+ ", description=" + description + ", processType="
				+ processType + ", pattern=" + pattern + ", priority="
				+ priority + ", caseSensitivityFlg="
				+ caseSensitivityFlg + ", validFlg=" + validFlg + "]";
	}
	
	//bi-directional many-to-one association to MonitorTrapValueInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
	@JoinColumn(name="monitor_id", referencedColumnName="monitor_id", insertable=false, updatable=false),
	@JoinColumn(name="mib", referencedColumnName="mib", insertable=false, updatable=false),
	@JoinColumn(name="trap_oid", referencedColumnName="trap_oid", insertable=false, updatable=false),
	@JoinColumn(name="generic_id", referencedColumnName="generic_id", insertable=false, updatable=false),
	@JoinColumn(name="specific_id", referencedColumnName="specific_id", insertable=false, updatable=false)
	})
	public MonitorTrapValueInfoEntity getMonitorTrapValueInfoEntity() {
		return this.monitorTrapValueInfoEntity;
	}
	
	@Deprecated
	public void setMonitorTrapValueInfoEntity(MonitorTrapValueInfoEntity trapValueInfoEntity) {
		this.monitorTrapValueInfoEntity = trapValueInfoEntity;
	}

	/**
	 * MonitorTrapValueInfoEntityオブジェクト参照設定<BR>
	 * 
	 * MonitorTrapValueInfoEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToMonitorTrapValueInfoEntity(MonitorTrapValueInfoEntity trapValueInfoEntity) {
		this.setMonitorTrapValueInfoEntity(trapValueInfoEntity);
		if (trapValueInfoEntity != null) {
			List<MonitorTrapVarbindPatternInfoEntity> list = trapValueInfoEntity.getMonitorTrapVarbindPatternInfoEntities();
			if (list == null) {
				list = new ArrayList<MonitorTrapVarbindPatternInfoEntity>();
			} else {
				for(MonitorTrapVarbindPatternInfoEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			trapValueInfoEntity.setMonitorTrapVarbindPatternInfoEntities(list);
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
		if (this.monitorTrapValueInfoEntity != null) {
			List<MonitorTrapVarbindPatternInfoEntity> list = this.monitorTrapValueInfoEntity.getMonitorTrapVarbindPatternInfoEntities();
			if (list != null) {
				Iterator<MonitorTrapVarbindPatternInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					MonitorTrapVarbindPatternInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
}