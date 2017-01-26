package com.clustercontrol.monitor.run.model;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetEntity;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.custom.model.MonitorCustomInfoEntity;
import com.clustercontrol.http.model.MonitorHttpInfoEntity;
import com.clustercontrol.http.model.MonitorHttpScenarioInfoEntity;
import com.clustercontrol.jmx.model.MonitorJmxInfoEntity;
import com.clustercontrol.logfile.model.MonitorLogfileInfoEntity;
import com.clustercontrol.monitor.plugin.model.MonitorPluginInfoEntity;
import com.clustercontrol.performance.monitor.model.MonitorPerfInfoEntity;
import com.clustercontrol.ping.model.MonitorPingInfoEntity;
import com.clustercontrol.port.model.MonitorPortInfoEntity;
import com.clustercontrol.process.model.MonitorProcessInfoEntity;
import com.clustercontrol.snmp.model.MonitorSnmpInfoEntity;
import com.clustercontrol.snmptrap.model.MonitorTrapInfoEntity;
import com.clustercontrol.sql.model.MonitorSqlInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventInfoEntity;
import com.clustercontrol.winservice.model.MonitorWinserviceInfoEntity;


/**
 * The persistent class for the cc_monitor_info database table.
 * 
 */
@Entity
@Table(name="cc_monitor_info", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.MONITOR,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="monitor_id", insertable=false, updatable=false))
public class MonitorInfoEntity extends ObjectPrivilegeTargetEntity {
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private String application;
	private Integer collectorFlg;
	private Integer delayTime;
	private String description;
	private Integer failurePriority;
	private String itemName;
	private String measure;
	private Integer monitorFlg;
	private Integer monitorType;
	private String monitorTypeId;
	private String notifyGroupId;
	private Timestamp regDate;
	private String regUser;
	private Integer runInterval;
	private String triggerType;
	private Timestamp updateDate;
	private String updateUser;
	private MonitorCustomInfoEntity monitorCustomInfoEntity;
	private MonitorHttpInfoEntity monitorHttpInfoEntity;
	private String calendarId;
	private String facilityId;
	private List<MonitorNumericValueInfoEntity> monitorNumericValueInfoEntities;
	private MonitorPerfInfoEntity monitorPerfInfoEntity;
	private MonitorPingInfoEntity monitorPingInfoEntity;
	private MonitorPortInfoEntity monitorPortInfoEntity;
	private MonitorProcessInfoEntity monitorProcessInfoEntity;
	private MonitorSnmpInfoEntity monitorSnmpInfoEntity;
	private MonitorSqlInfoEntity monitorSqlInfoEntity;
	private MonitorTrapInfoEntity monitorTrapInfoEntity;
	private MonitorWinserviceInfoEntity monitorWinserviceInfoEntity;
	private MonitorWinEventInfoEntity monitorWinEventInfoEntity;
	private MonitorLogfileInfoEntity monitorLogfileInfoEntity;
	private MonitorPluginInfoEntity monitorPluginInfoEntity;
	private MonitorHttpScenarioInfoEntity monitorHttpScenarioInfoEntity;
	private MonitorJmxInfoEntity monitorJmxInfoEntity;
	private List<MonitorStringValueInfoEntity> monitorStringValueInfoEntities;
	private List<MonitorTruthValueInfoEntity> monitorTruthValueInfoEntities;

	@Deprecated
	public MonitorInfoEntity() {
		super();
	}

	public MonitorInfoEntity(String monitorId) {
		super();
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		this.setMonitorId(monitorId);
		em.persist(this);
		this.setObjectId(this.getMonitorId());
	}

	@Id
	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}


	public String getApplication() {
		return this.application;
	}

	public void setApplication(String application) {
		this.application = application;
	}


	@Column(name="collector_flg")
	public Integer getCollectorFlg() {
		return this.collectorFlg;
	}

	public void setCollectorFlg(Integer collectorFlg) {
		this.collectorFlg = collectorFlg;
	}


	@Column(name="delay_time")
	public Integer getDelayTime() {
		return this.delayTime;
	}

	public void setDelayTime(Integer delayTime) {
		this.delayTime = delayTime;
	}


	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Column(name="failure_priority")
	public Integer getFailurePriority() {
		return this.failurePriority;
	}

	public void setFailurePriority(Integer failurePriority) {
		this.failurePriority = failurePriority;
	}


	@Column(name="item_name")
	public String getItemName() {
		return this.itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}


	public String getMeasure() {
		return this.measure;
	}

	public void setMeasure(String measure) {
		this.measure = measure;
	}


	@Column(name="monitor_flg")
	public Integer getMonitorFlg() {
		return this.monitorFlg;
	}

	public void setMonitorFlg(Integer monitorFlg) {
		this.monitorFlg = monitorFlg;
	}


	@Column(name="monitor_type")
	public Integer getMonitorType() {
		return this.monitorType;
	}

	public void setMonitorType(Integer monitorType) {
		this.monitorType = monitorType;
	}


	@Column(name="monitor_type_id")
	public String getMonitorTypeId() {
		return this.monitorTypeId;
	}

	public void setMonitorTypeId(String monitorTypeId) {
		this.monitorTypeId = monitorTypeId;
	}


	@Column(name="notify_group_id")
	public String getNotifyGroupId() {
		return this.notifyGroupId;
	}

	public void setNotifyGroupId(String notifyGroupId) {
		this.notifyGroupId = notifyGroupId;
	}


	@Column(name="reg_date")
	public Timestamp getRegDate() {
		return this.regDate;
	}

	public void setRegDate(Timestamp regDate) {
		this.regDate = regDate;
	}


	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}


	@Column(name="run_interval")
	public Integer getRunInterval() {
		return this.runInterval;
	}

	public void setRunInterval(Integer runInterval) {
		this.runInterval = runInterval;
	}


	@Column(name="trigger_type")
	public String getTriggerType() {
		return this.triggerType;
	}

	public void setTriggerType(String triggerType) {
		this.triggerType = triggerType;
	}


	@Column(name="update_date")
	public Timestamp getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(Timestamp updateDate) {
		this.updateDate = updateDate;
	}


	@Column(name="update_user")
	public String getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}


	//bi-directional one-to-one association to MonitorCustomInfoEntity
	@OneToOne(mappedBy="monitorInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public MonitorCustomInfoEntity getMonitorCustomInfoEntity() {
		return this.monitorCustomInfoEntity;
	}

	public void setMonitorCustomInfoEntity(MonitorCustomInfoEntity monitorCustomInfoEntity) {
		this.monitorCustomInfoEntity = monitorCustomInfoEntity;
	}


	//bi-directional one-to-one association to MonitorHttpInfoEntity
	@OneToOne(mappedBy="monitorInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public MonitorHttpInfoEntity getMonitorHttpInfoEntity() {
		return this.monitorHttpInfoEntity;
	}

	public void setMonitorHttpInfoEntity(MonitorHttpInfoEntity monitorHttpInfoEntity) {
		this.monitorHttpInfoEntity = monitorHttpInfoEntity;
	}


	@Column(name="calendar_id")
	public String getCalendarId() {
		return this.calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}


	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}


	//bi-directional many-to-one association to MonitorNumericValueInfoEntity
	@OneToMany(mappedBy="monitorInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorNumericValueInfoEntity> getMonitorNumericValueInfoEntities() {
		return this.monitorNumericValueInfoEntities;
	}

	public void setMonitorNumericValueInfoEntities(List<MonitorNumericValueInfoEntity> monitorNumericValueInfoEntities) {
		this.monitorNumericValueInfoEntities = monitorNumericValueInfoEntities;
	}


	//bi-directional one-to-one association to MonitorPerfInfoEntity
	@OneToOne(mappedBy="monitorInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public MonitorPerfInfoEntity getMonitorPerfInfoEntity() {
		return this.monitorPerfInfoEntity;
	}

	public void setMonitorPerfInfoEntity(MonitorPerfInfoEntity monitorPerfInfoEntity) {
		this.monitorPerfInfoEntity = monitorPerfInfoEntity;
	}


	//bi-directional one-to-one association to MonitorPingInfoEntity
	@OneToOne(mappedBy="monitorInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public MonitorPingInfoEntity getMonitorPingInfoEntity() {
		return this.monitorPingInfoEntity;
	}

	public void setMonitorPingInfoEntity(MonitorPingInfoEntity monitorPingInfoEntity) {
		this.monitorPingInfoEntity = monitorPingInfoEntity;
	}


	//bi-directional one-to-one association to MonitorPortInfoEntity
	@OneToOne(mappedBy="monitorInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public MonitorPortInfoEntity getMonitorPortInfoEntity() {
		return this.monitorPortInfoEntity;
	}

	public void setMonitorPortInfoEntity(MonitorPortInfoEntity monitorPortInfoEntity) {
		this.monitorPortInfoEntity = monitorPortInfoEntity;
	}


	//bi-directional one-to-one association to MonitorProcessInfoEntity
	@OneToOne(mappedBy="monitorInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public MonitorProcessInfoEntity getMonitorProcessInfoEntity() {
		return this.monitorProcessInfoEntity;
	}

	public void setMonitorProcessInfoEntity(MonitorProcessInfoEntity monitorProcessInfoEntity) {
		this.monitorProcessInfoEntity = monitorProcessInfoEntity;
	}


	//bi-directional one-to-one association to MonitorSnmpInfoEntity
	@OneToOne(mappedBy="monitorInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public MonitorSnmpInfoEntity getMonitorSnmpInfoEntity() {
		return this.monitorSnmpInfoEntity;
	}

	public void setMonitorSnmpInfoEntity(MonitorSnmpInfoEntity monitorSnmpInfoEntity) {
		this.monitorSnmpInfoEntity = monitorSnmpInfoEntity;
	}


	//bi-directional one-to-one association to MonitorSqlInfoEntity
	@OneToOne(mappedBy="monitorInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public MonitorSqlInfoEntity getMonitorSqlInfoEntity() {
		return this.monitorSqlInfoEntity;
	}

	public void setMonitorSqlInfoEntity(MonitorSqlInfoEntity monitorSqlInfoEntity) {
		this.monitorSqlInfoEntity = monitorSqlInfoEntity;
	}


	//bi-directional one-to-one association to MonitorTrapInfoEntity
	@OneToOne(mappedBy="monitorInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public MonitorTrapInfoEntity getMonitorTrapInfoEntity() {
		return this.monitorTrapInfoEntity;
	}

	public void setMonitorTrapInfoEntity(MonitorTrapInfoEntity monitorTrapInfoEntity) {
		this.monitorTrapInfoEntity = monitorTrapInfoEntity;
	}


	//bi-directional one-to-one association to MonitorWinEventInfoEntity
	@OneToOne(mappedBy="monitorInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public MonitorWinEventInfoEntity getMonitorWinEventInfoEntity() {
		return this.monitorWinEventInfoEntity;
	}

	public void setMonitorWinEventInfoEntity(MonitorWinEventInfoEntity monitorWinEventInfoEntity) {
		this.monitorWinEventInfoEntity = monitorWinEventInfoEntity;
	}

	//bi-directional one-to-one association to MonitorWinserviceInfoEntity
	@OneToOne(mappedBy="monitorInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public MonitorWinserviceInfoEntity getMonitorWinserviceInfoEntity() {
		return this.monitorWinserviceInfoEntity;
	}

	public void setMonitorWinserviceInfoEntity(MonitorWinserviceInfoEntity monitorWinserviceInfoEntity) {
		this.monitorWinserviceInfoEntity = monitorWinserviceInfoEntity;
	}


	//bi-directional one-to-one association to MonitorLogfileInfoEntity
	@OneToOne(mappedBy="monitorInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public MonitorLogfileInfoEntity getMonitorLogfileInfoEntity() {
		return this.monitorLogfileInfoEntity;
	}

	public void setMonitorLogfileInfoEntity(MonitorLogfileInfoEntity monitorLogfileInfoEntity) {
		this.monitorLogfileInfoEntity = monitorLogfileInfoEntity;
	}


	//bi-directional one-to-one association to MonitorLogfileInfoEntity
	@OneToOne(mappedBy="monitorInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public MonitorHttpScenarioInfoEntity getMonitorHttpScenarioInfoEntity() {
		return monitorHttpScenarioInfoEntity;
	}

	public void setMonitorHttpScenarioInfoEntity(
			MonitorHttpScenarioInfoEntity monitorHttpScenarioInfoEntity) {
		this.monitorHttpScenarioInfoEntity = monitorHttpScenarioInfoEntity;
	}

	//bi-directional one-to-one association to MonitorLogfileInfoEntity
	@OneToOne(mappedBy="monitorInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public MonitorJmxInfoEntity getMonitorJmxInfoEntity() {
		return monitorJmxInfoEntity;
	}

	public void setMonitorJmxInfoEntity(
			MonitorJmxInfoEntity monitorJmxInfoEntity) {
		this.monitorJmxInfoEntity = monitorJmxInfoEntity;
	}


	//bi-directional one-to-one association to MonitorLogfileInfoEntity
	@OneToOne(mappedBy="monitorInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public MonitorPluginInfoEntity getMonitorPluginInfoEntity() {
		return this.monitorPluginInfoEntity;
	}

	public void setMonitorPluginInfoEntity(MonitorPluginInfoEntity monitorPluginInfoEntity) {
		this.monitorPluginInfoEntity = monitorPluginInfoEntity;
	}


	//bi-directional many-to-one association to MonitorStringValueInfoEntity
	@OneToMany(mappedBy="monitorInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorStringValueInfoEntity> getMonitorStringValueInfoEntities() {
		return this.monitorStringValueInfoEntities;
	}

	public void setMonitorStringValueInfoEntities(List<MonitorStringValueInfoEntity> monitorStringValueInfoEntities) {
		this.monitorStringValueInfoEntities = monitorStringValueInfoEntities;
	}


	//bi-directional many-to-one association to MonitorTruthValueInfoEntity
	@OneToMany(mappedBy="monitorInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorTruthValueInfoEntity> getMonitorTruthValueInfoEntities() {
		return this.monitorTruthValueInfoEntities;
	}

	public void setMonitorTruthValueInfoEntities(List<MonitorTruthValueInfoEntity> monitorTruthValueInfoEntities) {
		this.monitorTruthValueInfoEntities = monitorTruthValueInfoEntities;
	}

	/**
	 * MonitorNumericValueInfoEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteMonitorNumericValueInfoEntities(List<MonitorNumericValueInfoEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorNumericValueInfoEntity> list = this.getMonitorNumericValueInfoEntities();
		Iterator<MonitorNumericValueInfoEntity> iter = list.iterator();
		while(iter.hasNext()) {
			MonitorNumericValueInfoEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}

	/**
	 * MonitorStringValueInfoEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteMonitorStringValueInfoEntities(List<MonitorStringValueInfoEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorStringValueInfoEntity> list = this.getMonitorStringValueInfoEntities();
		Iterator<MonitorStringValueInfoEntity> iter = list.iterator();
		while(iter.hasNext()) {
			MonitorStringValueInfoEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}

	/**
	 * MonitorTruthValueInfoEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteMonitorTruthValueInfoEntities(List<MonitorTruthValueInfoEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorTruthValueInfoEntity> list = this.getMonitorTruthValueInfoEntities();
		Iterator<MonitorTruthValueInfoEntity> iter = list.iterator();
		while(iter.hasNext()) {
			MonitorTruthValueInfoEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}
}