package com.clustercontrol.jobmanagement.model;

import java.sql.Timestamp;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetEntity;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;



/**
 * The persistent class for the cc_job_schedule database table.
 * 
 */
@Entity
@Table(name="cc_job_schedule", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.JOB_SCHEDULE,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="schedule_id", insertable=false, updatable=false))
public class JobScheduleEntity extends ObjectPrivilegeTargetEntity {
	private static final long serialVersionUID = 1L;
	private String scheduleId;
	private String scheduleName		= null;
	private String jobId			= null;
	private String jobunitId		= null;
	private String calendarId		= null;
	private Integer scheduleType	= null;
	private Integer hour			= null;
	private Integer minute			= null;
	private Integer week			= null;
	private Integer fromXMinutes = null;
	private Integer everyXMinutes = null;
	private Integer validFlg		= null;
	private Timestamp regDate		= null;
	private String regUser			= null;
	private Timestamp updateDate	= null;
	private String updateUser		= null;

	@Deprecated
	public JobScheduleEntity() {
	}

	public JobScheduleEntity(String scheduleId) {
		this.setScheduleId(scheduleId);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.setObjectId(this.getScheduleId());
	}

	@Id
	@Column(name="schedule_id")
	public String getScheduleId() {
		return this.scheduleId;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	public Integer getHour() {
		return this.hour;
	}

	public void setHour(Integer hour) {
		this.hour = hour;
	}

	@Column(name="job_id")
	public String getJobId() {
		return this.jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	@Column(name="jobunit_id")
	public String getJobunitId() {
		return this.jobunitId;
	}

	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	public Integer getMinute() {
		return this.minute;
	}

	public void setMinute(Integer minute) {
		this.minute = minute;
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


	@Column(name="schedule_name")
	public String getScheduleName() {
		return this.scheduleName;
	}

	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
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

	@Column(name="from_x_minutes")
	public Integer getFromXMinutes() {
		return this.fromXMinutes;
	}

	public void setFromXMinutes(Integer fromXMinutes) {
		this.fromXMinutes = fromXMinutes;
	}

	@Column(name="every_x_minutes")
	public Integer getEveryXMinutes() {
		return this.everyXMinutes;
	}

	public void setEveryXMinutes(Integer everyXMinutes) {
		this.everyXMinutes = everyXMinutes;
	}
}