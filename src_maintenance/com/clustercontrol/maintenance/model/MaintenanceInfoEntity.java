package com.clustercontrol.maintenance.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetEntity;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_maintenance_info database table.
 * 
 */
@Entity
@Table(name="cc_maintenance_info", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.SYSYTEM_MAINTENANCE,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="maintenance_id", insertable=false, updatable=false))
public class MaintenanceInfoEntity extends ObjectPrivilegeTargetEntity {
	private static final long serialVersionUID = 1L;
	private String maintenanceId;
	private String application;
	private Integer dataRetentionPeriod;
	private Integer day;
	private String description;
	private Integer hour;
	private Integer minute;
	private Integer month;
	private String notifyGroupId;
	private Timestamp regDate;
	private String regUser;
	private Integer scheduleType;
	private Timestamp updateDate;
	private String updateUser;
	private Integer validFlg;
	private Integer week;
	private String calendarId;
	private MaintenanceTypeMstEntity maintenanceTypeMstEntity;

	@Deprecated
	public MaintenanceInfoEntity() {
	}

	public MaintenanceInfoEntity(String maintenanceId,
			MaintenanceTypeMstEntity maintenanceTypeMstEntity) {
		this.setMaintenanceId(maintenanceId);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToMaintenanceTypeMstEntity(maintenanceTypeMstEntity);
		this.setObjectId(this.getMaintenanceId());
	}


	@Id
	@Column(name="maintenance_id")
	public String getMaintenanceId() {
		return this.maintenanceId;
	}

	public void setMaintenanceId(String maintenanceId) {
		this.maintenanceId = maintenanceId;
	}


	public String getApplication() {
		return this.application;
	}

	public void setApplication(String application) {
		this.application = application;
	}


	@Column(name="data_retention_period")
	public Integer getDataRetentionPeriod() {
		return this.dataRetentionPeriod;
	}

	public void setDataRetentionPeriod(Integer dataRetentionPeriod) {
		this.dataRetentionPeriod = dataRetentionPeriod;
	}


	public Integer getDay() {
		return this.day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}


	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public Integer getHour() {
		return this.hour;
	}

	public void setHour(Integer hour) {
		this.hour = hour;
	}


	public Integer getMinute() {
		return this.minute;
	}

	public void setMinute(Integer minute) {
		this.minute = minute;
	}


	public Integer getMonth() {
		return this.month;
	}

	public void setMonth(Integer month) {
		this.month = month;
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


	@Column(name="schedule_type")
	public Integer getScheduleType() {
		return this.scheduleType;
	}

	public void setScheduleType(Integer scheduleType) {
		this.scheduleType = scheduleType;
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


	@Column(name="valid_flg")
	public Integer getValidFlg() {
		return this.validFlg;
	}

	public void setValidFlg(Integer validFlg) {
		this.validFlg = validFlg;
	}


	public Integer getWeek() {
		return this.week;
	}

	public void setWeek(Integer week) {
		this.week = week;
	}


	@Column(name="calendar_id")
	public String getCalendarId() {
		return this.calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}


	//bi-directional many-to-one association to MaintenanceTypeMstEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="type_id")
	public MaintenanceTypeMstEntity getMaintenanceTypeMstEntity() {
		return this.maintenanceTypeMstEntity;
	}

	@Deprecated
	public void setMaintenanceTypeMstEntity(MaintenanceTypeMstEntity maintenanceTypeMstEntity) {
		this.maintenanceTypeMstEntity = maintenanceTypeMstEntity;
	}

	/**
	 * MaintenanceTypeMstEntityオブジェクト参照設定<BR>
	 * 
	 * MaintenanceTypeMstEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToMaintenanceTypeMstEntity(MaintenanceTypeMstEntity maintenanceTypeMstEntity) {
		this.setMaintenanceTypeMstEntity(maintenanceTypeMstEntity);
		if (maintenanceTypeMstEntity != null) {
			List<MaintenanceInfoEntity> list = maintenanceTypeMstEntity.getMaintenanceInfoEntities();
			if (list == null) {
				list = new ArrayList<MaintenanceInfoEntity>();
			} else {
				for(MaintenanceInfoEntity entity : list){
					if (entity.getMaintenanceId().equals(this.maintenanceId)) {
						return;
					}
				}
			}
			list.add(this);
			maintenanceTypeMstEntity.setMaintenanceInfoEntities(list);
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

		// MaintenanceTypeMstEntity
		if (this.maintenanceTypeMstEntity != null) {
			List<MaintenanceInfoEntity> list = this.maintenanceTypeMstEntity.getMaintenanceInfoEntities();
			if (list != null) {
				Iterator<MaintenanceInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					MaintenanceInfoEntity entity = iter.next();
					if (entity.getMaintenanceId().equals(this.getMaintenanceId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}