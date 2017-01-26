package com.clustercontrol.calendar.model;

import java.sql.Time;
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
import javax.persistence.Table;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetEntity;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_cal_info database table.
 * 
 */
@Entity
@Table(name="cc_cal_info", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.PLATFORM_CALENDAR,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="calendar_id", insertable=false, updatable=false))
public class CalInfoEntity extends ObjectPrivilegeTargetEntity {
	private static final long serialVersionUID = 1L;
	private String calendarId;
	private String calendarName;
	private String description;
	private Timestamp regDate;
	private String regUser;
	private Time startTime;
	private Timestamp updateDate;
	private String updateUser;
	private Timestamp validTimeFrom;
	private Timestamp validTimeTo;
	private List<CalDetailInfoEntity> calDetailInfoEntities;

	@Deprecated
	public CalInfoEntity() {
	}

	public CalInfoEntity(String calendarId) {
		this.setCalendarId(calendarId);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.setObjectId(this.getCalendarId());
	}


	@Id
	@Column(name="calendar_id")
	public String getCalendarId() {
		return this.calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	@Column(name="calendar_name")
	public String getCalendarName() {
		return this.calendarName;
	}

	public void setCalendarName(String calendarName) {
		this.calendarName = calendarName;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
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


	@Column(name="start_time")
	public Time getStartTime() {
		return this.startTime;
	}

	public void setStartTime(Time startTime) {
		this.startTime = startTime;
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

	@Column(name="valid_time_from")
	public Timestamp getValidTimeFrom() {
		return this.validTimeFrom;
	}

	public void setValidTimeFrom(Timestamp validTimeFrom) {
		this.validTimeFrom = validTimeFrom;
	}

	@Column(name="valid_time_to")
	public Timestamp getValidTimeTo() {
		return this.validTimeTo;
	}

	public void setValidTimeTo(Timestamp validTimeTo) {
		this.validTimeTo = validTimeTo;
	}

	/**
	 * CalDetailInfoEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteCalDetailInfoEntities(List<CalDetailInfoEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalDetailInfoEntity> list = this.getCalDetailInfoEntities();
		Iterator<CalDetailInfoEntity> iter = list.iterator();
		while(iter.hasNext()) {
			CalDetailInfoEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}

	//bi-directional many-to-one association to CalDetailInfoEntity
	@OneToMany(mappedBy="calInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<CalDetailInfoEntity> getCalDetailInfoEntities() {
		return this.calDetailInfoEntities;
	}

	public void setCalDetailInfoEntities(List<CalDetailInfoEntity> calDetailInfoEntities) {
		this.calDetailInfoEntities = calDetailInfoEntities;
	}

}