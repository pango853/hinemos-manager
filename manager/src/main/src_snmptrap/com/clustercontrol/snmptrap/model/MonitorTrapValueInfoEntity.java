package com.clustercontrol.snmptrap.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;

/**
 * The persistent class for the cc_monitor_trap_value_info database table.
 * 
 */
@Entity
@Table(name="cc_monitor_trap_value_info", schema="setting")
@Cacheable(true)
public class MonitorTrapValueInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	private MonitorTrapValueInfoEntityPK id;
	private String uei;
	private Integer version;
	private String logmsg;
	private String description;
	private Integer procVarBindType;
	private Integer priorityAnyVarBind;
	private String formatVarBinds;
	private Integer validFlg;
	private MonitorTrapInfoEntity monitorTrapInfoEntity;

	private List<MonitorTrapVarbindPatternInfoEntity> monitorTrapVarbindPatternInfoEntities;

	@Deprecated
	public MonitorTrapValueInfoEntity() {
	}

	public MonitorTrapValueInfoEntity(
			MonitorTrapInfoEntity trapInfoEntity,
			String mib,
			String trapOid,
			Integer genericId,
			Integer specificId
			) {
		this.setId(new MonitorTrapValueInfoEntityPK(trapInfoEntity.getMonitorId(), mib, trapOid, genericId, specificId));
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToMonitorTrapInfoEntity(trapInfoEntity);
	}

	@EmbeddedId
	public MonitorTrapValueInfoEntityPK getId() {
		return id;
	}
	public void setId(MonitorTrapValueInfoEntityPK id) {
		this.id = id;
	}

	@Column(name="uei")
	public String getUei() {
		return uei;
	}
	public void setUei(String uei) {
		this.uei = uei;
	}

	@Column(name="version")
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}

	@Column(name="logmsg")
	public String getLogmsg() {
		return logmsg;
	}
	public void setLogmsg(String logmsg) {
		this.logmsg = logmsg;
	}

	@Column(name="descr")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="proc_varbind_type")
	public Integer getProcessingVarbindType() {
		return procVarBindType;
	}
	public void setProcessingVarbindType(Integer procVarbindType) {
		this.procVarBindType = procVarbindType;
	}

	@Column(name="priority_any_varbind")
	public Integer getPriorityAnyVarbind() {
		return priorityAnyVarBind;
	}
	public void setPriorityAnyVarbind(Integer priorityAnyVarbind) {
		this.priorityAnyVarBind = priorityAnyVarbind;
	}

	@Column(name="format_varbinds")
	public String getFormatVarBinds() {
		return formatVarBinds;
	}
	public void setFormatVarBinds(String formatVarBinds) {
		this.formatVarBinds = formatVarBinds;
	}

	@Column(name="valid_flg")
	public Integer getValidFlg() {
		return validFlg;
	}
	public void setValidFlg(Integer validFlg) {
		this.validFlg = validFlg;
	}

	@OneToMany(mappedBy="monitorTrapValueInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorTrapVarbindPatternInfoEntity> getMonitorTrapVarbindPatternInfoEntities() {
		return this.monitorTrapVarbindPatternInfoEntities;
	}

	public void setMonitorTrapVarbindPatternInfoEntities(List<MonitorTrapVarbindPatternInfoEntity> monitorTrapVarbindPatternInfoEntities) {
		this.monitorTrapVarbindPatternInfoEntities = monitorTrapVarbindPatternInfoEntities;
	}

	@Override
	public String toString() {
		return "MonitorTrapValueInfoEntity [id=" + id + ", uei=" + uei
				+ ", version=" + version + ", logmsg=" + logmsg
				+ ", description=" + description + ", procVarBindType="
				+ procVarBindType + ", priorityAnyVarBind="
				+ priorityAnyVarBind + ", formatVarBind=" + formatVarBinds
				+ ", validFlg=" + validFlg
				+ ", monitorTrapVarbindPatternInfoEntities="
				+ monitorTrapVarbindPatternInfoEntities + "]";
	}
	
	//bi-directional many-to-one association to MonitorTrapInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="monitor_id", insertable=false, updatable=false)
	public MonitorTrapInfoEntity getMonitorTrapInfoEntity() {
		return this.monitorTrapInfoEntity;
	}

	@Deprecated
	public void setMonitorTrapInfoEntity(MonitorTrapInfoEntity monitorTrapInfoEntity) {
		this.monitorTrapInfoEntity = monitorTrapInfoEntity;
	}
	
	/**
	 * MonitorTrapInfoEntityオブジェクト参照設定<BR>
	 * 
	 * MonitorTrapInfoEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToMonitorTrapInfoEntity(MonitorTrapInfoEntity monitorTrapInfoEntity) {
		this.setMonitorTrapInfoEntity(monitorTrapInfoEntity);
		if (monitorTrapInfoEntity != null) {
			List<MonitorTrapValueInfoEntity> list = monitorTrapInfoEntity.getMonitorTrapValueInfoEntities();
			if (list == null) {
				list = new ArrayList<MonitorTrapValueInfoEntity>();
			} else {
				for(MonitorTrapValueInfoEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorTrapInfoEntity.setMonitorTrapValueInfoEntities(list);
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

		// MonitorTrapInfoEntity
		if (this.monitorTrapInfoEntity != null) {
			List<MonitorTrapValueInfoEntity> list = this.monitorTrapInfoEntity.getMonitorTrapValueInfoEntities();
			if (list != null) {
				Iterator<MonitorTrapValueInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					MonitorTrapValueInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
	
	public void deleteMonitorTrapVarbindPatternInfoEntities(List<MonitorTrapVarbindPatternInfoEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorTrapVarbindPatternInfoEntity> list = this.getMonitorTrapVarbindPatternInfoEntities();
		Iterator<MonitorTrapVarbindPatternInfoEntity> iter = list.iterator();
		while(iter.hasNext()) {
			MonitorTrapVarbindPatternInfoEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}
}
