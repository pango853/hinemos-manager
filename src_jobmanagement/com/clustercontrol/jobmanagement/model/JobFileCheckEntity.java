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
 * The persistent class for the cc_job_file_check database table.
 * 
 */
@Entity
@Table(name="cc_job_file_check", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.JOB_FILE_CHECK,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="schedule_id", insertable=false, updatable=false))
public class JobFileCheckEntity extends ObjectPrivilegeTargetEntity {
	private static final long serialVersionUID = 1L;
	private String scheduleId;
	private String jobId			= null;
	private String jobunitId		= null;
	private Timestamp regDate		= null;
	private String regUser			= null;
	private String scheduleName		= null;
	private Timestamp updateDate	= null;
	private String updateUser		= null;
	private Integer validFlg		= null;
	private String calendarId		= null;
	private String facilityId = null;
	private String directory = null;
	private String fileName = null;
	private Integer eventType = null;
	private Integer modifyType = null;

	@Deprecated
	public JobFileCheckEntity() {
	}

	public JobFileCheckEntity(String scheduleId) {
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

	@Column(name="facility_id")
	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="directory")
	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	@Column(name="file_name")
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Column(name="event_type")
	public Integer getEventType() {
		return eventType;
	}

	public void setEventType(Integer eventType) {
		this.eventType = eventType;
	}

	@Column(name="modify_type")
	public Integer getModifyType() {
		return modifyType;
	}

	public void setModifyType(Integer modifyType) {
		this.modifyType = modifyType;
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

	@Column(name="calendar_id")
	public String getCalendarId() {
		return this.calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}
}