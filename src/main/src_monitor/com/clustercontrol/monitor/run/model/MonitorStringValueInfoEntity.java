package com.clustercontrol.monitor.run.model;

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
 * The persistent class for the cc_monitor_string_value_info database table.
 * 
 */
@Entity
@Table(name="cc_monitor_string_value_info", schema="setting")
@Cacheable(true)
public class MonitorStringValueInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private MonitorStringValueInfoEntityPK id;
	private Integer caseSensitivityFlg;
	private String description;
	private String message;
	private String pattern;
	private Integer priority;
	private Integer processType;
	private Integer validFlg;
	private MonitorInfoEntity monitorInfoEntity;

	@Deprecated
	public MonitorStringValueInfoEntity() {
	}

	public MonitorStringValueInfoEntity(MonitorStringValueInfoEntityPK pk,
			MonitorInfoEntity monitorInfoEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToMonitorInfoEntity(monitorInfoEntity);
	}

	public MonitorStringValueInfoEntity(MonitorInfoEntity monitorInfoEntity, Integer orderNo) {
		this(new MonitorStringValueInfoEntityPK(monitorInfoEntity.getMonitorId(), orderNo), monitorInfoEntity);
	}


	@EmbeddedId
	public MonitorStringValueInfoEntityPK getId() {
		return this.id;
	}

	public void setId(MonitorStringValueInfoEntityPK id) {
		this.id = id;
	}


	@Column(name="case_sensitivity_flg")
	public Integer getCaseSensitivityFlg() {
		return this.caseSensitivityFlg;
	}

	public void setCaseSensitivityFlg(Integer caseSensitivityFlg) {
		this.caseSensitivityFlg = caseSensitivityFlg;
	}


	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}


	public String getPattern() {
		return this.pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}


	public Integer getPriority() {
		return this.priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}


	@Column(name="process_type")
	public Integer getProcessType() {
		return this.processType;
	}

	public void setProcessType(Integer processType) {
		this.processType = processType;
	}


	@Column(name="valid_flg")
	public Integer getValidFlg() {
		return this.validFlg;
	}

	public void setValidFlg(Integer validFlg) {
		this.validFlg = validFlg;
	}


	//bi-directional many-to-one association to MonitorInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="monitor_id", insertable=false, updatable=false)
	public MonitorInfoEntity getMonitorInfoEntity() {
		return this.monitorInfoEntity;
	}

	@Deprecated
	public void setMonitorInfoEntity(MonitorInfoEntity monitorInfoEntity) {
		this.monitorInfoEntity = monitorInfoEntity;
	}

	/**
	 * MonitorInfoEntityオブジェクト参照設定<BR>
	 * 
	 * MonitorInfoEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToMonitorInfoEntity(MonitorInfoEntity monitorInfoEntity) {
		this.setMonitorInfoEntity(monitorInfoEntity);
		if (monitorInfoEntity != null) {
			List<MonitorStringValueInfoEntity> list = monitorInfoEntity.getMonitorStringValueInfoEntities();
			if (list == null) {
				list = new ArrayList<MonitorStringValueInfoEntity>();
			} else {
				for(MonitorStringValueInfoEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorInfoEntity.setMonitorStringValueInfoEntities(list);
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

		// MonitorInfoEntity
		if (this.monitorInfoEntity != null) {
			List<MonitorStringValueInfoEntity> list = this.monitorInfoEntity.getMonitorStringValueInfoEntities();
			if (list != null) {
				Iterator<MonitorStringValueInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					MonitorStringValueInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}