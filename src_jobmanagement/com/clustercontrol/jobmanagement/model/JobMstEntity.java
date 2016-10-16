package com.clustercontrol.jobmanagement.model;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetEntity;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.jobmanagement.bean.JobConstant;



/**
 * The persistent class for the cc_job_mst database table.
 *
 */
@Entity
@Table(name="cc_job_mst", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.JOB,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="jobunit_id", insertable=false, updatable=false))
public class JobMstEntity extends ObjectPrivilegeTargetEntity {
	private static final long serialVersionUID = 1L;
	private JobMstEntityPK id;
	private String description;
	private String jobName;
	private Integer jobType;
	private Timestamp regDate;
	private String regUser;
	private Timestamp updateDate;
	private String updateUser;
	// cc_job_command_mst
	private String argument;
	private String argumentJobId;
	private Integer specifyUser;
	private String effectiveUser;
	private Integer messageRetryEndFlg;
	private Integer messageRetryEndValue;
	private Integer commandRetryFlg;
	private String facilityId;
	private Integer processMode;
	private String startCommand;
	private Integer stopType;
	private String stopCommand;
	// cc_job_start_mst
	private Integer calendar;
	private Integer calendarEndStatus;
	private Integer calendarEndValue;
	private Integer conditionType;
	private Integer endDelay;
	private Integer endDelayConditionType;
	private Integer endDelayJob;
	private Integer endDelayJobValue;
	private Integer endDelayNotify;
	private Integer endDelayNotifyPriority;
	private Integer endDelayOperation;
	private Integer endDelayOperationEndStatus;
	private Integer endDelayOperationEndValue;
	private Integer endDelayOperationType;
	private Integer endDelaySession;
	private Integer endDelaySessionValue;
	private Integer endDelayTime;
	private Timestamp endDelayTimeValue;
	private Integer multiplicity_notify;
	private Integer multiplicity_notify_priority;
	private Integer multiplicity_operation;
	private Integer multiplicity_end_value;
	private Integer messageRetry;
	private Integer commandRetry;
	private Integer skip;
	private Integer skipEndStatus;
	private Integer skipEndValue;
	private Integer startDelay;
	private Integer startDelayConditionType;
	private Integer startDelayNotify;
	private Integer startDelayNotifyPriority;
	private Integer startDelayOperation;
	private Integer startDelayOperationEndStatus;
	private Integer startDelayOperationEndValue;
	private Integer startDelayOperationType;
	private Integer startDelaySession;
	private Integer startDelaySessionValue;
	private Integer startDelayTime;
	private Timestamp startDelayTimeValue;
	private Integer suspend;
	private Integer unmatchEndFlg;
	private Integer unmatchEndStatus;
	private Integer unmatchEndValue;
	private String calendarId;
	// cc_job_file_mst
	private Integer checkFlg;
	private Integer compressionFlg;
	private String destDirectory;
	private String destWorkDir;
	private String srcFile;
	private String srcWorkDir;
	private String srcFacilityId;
	private String destFacilityId;
	// cc_job_start_time_mst
	private Timestamp startTime;
	private Integer startMinute;

	//ジョブ通知関連
	private String notifyGroupId;
	private Integer beginPriority;
	private Integer normalPriority;
	private Integer warnPriority;
	private Integer abnormalPriority;

	private List<JobEndMstEntity> jobEndMstEntities;
	private List<JobParamMstEntity> jobParamMstEntities;
	private List<JobStartJobMstEntity> jobStartJobMstEntities;
	private String parentJobunitId;
	private String parentJobId;

	private String referJobUnitId;
	private String referJobId;

	@Deprecated
	public JobMstEntity() {
	}

	public JobMstEntity(JobMstEntityPK pk, Integer jobType) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.jobType = jobType;
		this.setObjectId(this.getId().getJobunitId());

		// JOBUNIT以外は、JOBUNITのowner_role_idを設定する
		if (this.getJobType() != null && this.getJobType() != JobConstant.TYPE_JOBUNIT) {
			JobMstEntity jobMstEntity
			= em.find(JobMstEntity.class,
					new JobMstEntityPK(this.getId().getJobunitId(), this.getId().getJobunitId()), ObjectPrivilegeMode.NONE);
			if (jobMstEntity != null && jobMstEntity.getOwnerRoleId() != null) {
				this.setOwnerRoleId(jobMstEntity.getOwnerRoleId());
			} else {
				this.setOwnerRoleId(RoleIdConstant.INTERNAL);
			}
		}
	}

	public JobMstEntity(String jobunitId, String jobId, Integer jobType) {
		this(new JobMstEntityPK(jobunitId, jobId), jobType);
	}


	@EmbeddedId
	public JobMstEntityPK getId() {
		return this.id;
	}

	public void setId(JobMstEntityPK id) {
		this.id = id;
	}


	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Column(name="job_name")
	public String getJobName() {
		return this.jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}


	@Column(name="job_type")
	public Integer getJobType() {
		return this.jobType;
	}

	public void setJobType(Integer jobType) {
		this.jobType = jobType;
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


	// cc_job_command_mst
	public String getArgument() {
		return this.argument;
	}

	public void setArgument(String argument) {
		this.argument = argument;
	}


	@Column(name="argument_job_id")
	public String getArgumentJobId() {
		return this.argumentJobId;
	}

	public void setArgumentJobId(String argumentJobId) {
		this.argumentJobId = argumentJobId;
	}


	@Column(name="specify_user")
	public Integer getSpecifyUser() {
		return this.specifyUser;
	}

	public void setSpecifyUser(Integer specifyUser) {
		this.specifyUser = specifyUser;
	}


	@Column(name="effective_user")
	public String getEffectiveUser() {
		return this.effectiveUser;
	}

	public void setEffectiveUser(String effectiveUser) {
		this.effectiveUser = effectiveUser;
	}


	@Column(name="message_retry_end_flg")
	public Integer getMessageRetryEndFlg() {
		return this.messageRetryEndFlg;
	}

	public void setMessageRetryEndFlg(Integer messageRetryEndFlg) {
		this.messageRetryEndFlg = messageRetryEndFlg;
	}


	@Column(name="message_retry_end_value")
	public Integer getMessageRetryEndValue() {
		return this.messageRetryEndValue;
	}

	public void setMessageRetryEndValue(Integer messageRetryEndValue) {
		this.messageRetryEndValue = messageRetryEndValue;
	}


	@Column(name="command_retry_flg")
	public Integer getCommandRetryFlg() {
		return this.commandRetryFlg;
	}

	public void setCommandRetryFlg(Integer commandRetryFlg) {
		this.commandRetryFlg = commandRetryFlg;
	}


	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}


	@Column(name="process_mode")
	public Integer getProcessMode() {
		return this.processMode;
	}

	public void setProcessMode(Integer processMode) {
		this.processMode = processMode;
	}


	@Column(name="start_command")
	public String getStartCommand() {
		return this.startCommand;
	}

	public void setStartCommand(String startCommand) {
		this.startCommand = startCommand;
	}

	@Column(name="stop_type")
	public Integer getStopType() {
		return this.stopType;
	}

	public void setStopType(Integer stopType) {
		this.stopType = stopType;
	}

	@Column(name="stop_command")
	public String getStopCommand() {
		return this.stopCommand;
	}

	public void setStopCommand(String stopCommand) {
		this.stopCommand = stopCommand;
	}

	// cc_job_start_mst


	public Integer getCalendar() {
		return this.calendar;
	}

	public void setCalendar(Integer calendar) {
		this.calendar = calendar;
	}


	@Column(name="calendar_end_status")
	public Integer getCalendarEndStatus() {
		return this.calendarEndStatus;
	}

	public void setCalendarEndStatus(Integer calendarEndStatus) {
		this.calendarEndStatus = calendarEndStatus;
	}


	@Column(name="calendar_end_value")
	public Integer getCalendarEndValue() {
		return this.calendarEndValue;
	}

	public void setCalendarEndValue(Integer calendarEndValue) {
		this.calendarEndValue = calendarEndValue;
	}


	@Column(name="condition_type")
	public Integer getConditionType() {
		return this.conditionType;
	}

	public void setConditionType(Integer conditionType) {
		this.conditionType = conditionType;
	}


	@Column(name="end_delay")
	public Integer getEndDelay() {
		return this.endDelay;
	}

	public void setEndDelay(Integer endDelay) {
		this.endDelay = endDelay;
	}


	@Column(name="end_delay_condition_type")
	public Integer getEndDelayConditionType() {
		return this.endDelayConditionType;
	}

	public void setEndDelayConditionType(Integer endDelayConditionType) {
		this.endDelayConditionType = endDelayConditionType;
	}


	@Column(name="end_delay_job")
	public Integer getEndDelayJob() {
		return this.endDelayJob;
	}

	public void setEndDelayJob(Integer endDelayJob) {
		this.endDelayJob = endDelayJob;
	}


	@Column(name="end_delay_job_value")
	public Integer getEndDelayJobValue() {
		return this.endDelayJobValue;
	}

	public void setEndDelayJobValue(Integer endDelayJobValue) {
		this.endDelayJobValue = endDelayJobValue;
	}


	@Column(name="end_delay_notify")
	public Integer getEndDelayNotify() {
		return this.endDelayNotify;
	}

	public void setEndDelayNotify(Integer endDelayNotify) {
		this.endDelayNotify = endDelayNotify;
	}


	@Column(name="end_delay_notify_priority")
	public Integer getEndDelayNotifyPriority() {
		return this.endDelayNotifyPriority;
	}

	public void setEndDelayNotifyPriority(Integer endDelayNotifyPriority) {
		this.endDelayNotifyPriority = endDelayNotifyPriority;
	}


	@Column(name="end_delay_operation")
	public Integer getEndDelayOperation() {
		return this.endDelayOperation;
	}

	public void setEndDelayOperation(Integer endDelayOperation) {
		this.endDelayOperation = endDelayOperation;
	}


	@Column(name="end_delay_operation_end_status")
	public Integer getEndDelayOperationEndStatus() {
		return this.endDelayOperationEndStatus;
	}

	public void setEndDelayOperationEndStatus(Integer endDelayOperationEndStatus) {
		this.endDelayOperationEndStatus = endDelayOperationEndStatus;
	}


	@Column(name="end_delay_operation_end_value")
	public Integer getEndDelayOperationEndValue() {
		return this.endDelayOperationEndValue;
	}

	public void setEndDelayOperationEndValue(Integer endDelayOperationEndValue) {
		this.endDelayOperationEndValue = endDelayOperationEndValue;
	}


	@Column(name="end_delay_operation_type")
	public Integer getEndDelayOperationType() {
		return this.endDelayOperationType;
	}

	public void setEndDelayOperationType(Integer endDelayOperationType) {
		this.endDelayOperationType = endDelayOperationType;
	}


	@Column(name="end_delay_session")
	public Integer getEndDelaySession() {
		return this.endDelaySession;
	}

	public void setEndDelaySession(Integer endDelaySession) {
		this.endDelaySession = endDelaySession;
	}


	@Column(name="end_delay_session_value")
	public Integer getEndDelaySessionValue() {
		return this.endDelaySessionValue;
	}

	public void setEndDelaySessionValue(Integer endDelaySessionValue) {
		this.endDelaySessionValue = endDelaySessionValue;
	}


	@Column(name="end_delay_time")
	public Integer getEndDelayTime() {
		return this.endDelayTime;
	}

	public void setEndDelayTime(Integer endDelayTime) {
		this.endDelayTime = endDelayTime;
	}


	@Column(name="end_delay_time_value")
	public Timestamp getEndDelayTimeValue() {
		return this.endDelayTimeValue;
	}

	public void setEndDelayTimeValue(Timestamp endDelayTimeValue) {
		this.endDelayTimeValue = endDelayTimeValue;
	}

	@Column(name="message_retry")
	public Integer getMessageRetry() {
		return this.messageRetry;
	}

	public void setMessageRetry(Integer messageRetry) {
		this.messageRetry = messageRetry;
	}

	@Column(name="command_retry")
	public Integer getCommandRetry() {
		return this.commandRetry;
	}

	public void setCommandRetry(Integer commandRetry) {
		this.commandRetry = commandRetry;
	}

	@Column(name="multiplicity_notify")
	public Integer getMultiplicityNotify() {
		return this.multiplicity_notify;
	}

	public void setMultiplicityNotify(Integer multiplicity_notify) {
		this.multiplicity_notify = multiplicity_notify;
	}

	@Column(name="multiplicity_notify_priority")
	public Integer getMultiplicityNotifyPriority() {
		return this.multiplicity_notify_priority;
	}

	public void setMultiplicityNotifyPriority(Integer multiplicity_notify_priority) {
		this.multiplicity_notify_priority = multiplicity_notify_priority;
	}

	@Column(name="multiplicity_operation")
	public Integer getMultiplicityOperation() {
		return this.multiplicity_operation;
	}

	public void setMultiplicityOperation(Integer multiplicity_operation) {
		this.multiplicity_operation = multiplicity_operation;
	}

	@Column(name="multiplicity_end_value")
	public Integer getMultiplicityEndValue() {
		return this.multiplicity_end_value;
	}

	public void setMultiplicityEndValue(Integer multiplicity_end_value) {
		this.multiplicity_end_value = multiplicity_end_value;
	}

	public Integer getSkip() {
		return this.skip;
	}

	public void setSkip(Integer skip) {
		this.skip = skip;
	}


	@Column(name="skip_end_status")
	public Integer getSkipEndStatus() {
		return this.skipEndStatus;
	}

	public void setSkipEndStatus(Integer skipEndStatus) {
		this.skipEndStatus = skipEndStatus;
	}


	@Column(name="skip_end_value")
	public Integer getSkipEndValue() {
		return this.skipEndValue;
	}

	public void setSkipEndValue(Integer skipEndValue) {
		this.skipEndValue = skipEndValue;
	}


	@Column(name="start_delay")
	public Integer getStartDelay() {
		return this.startDelay;
	}

	public void setStartDelay(Integer startDelay) {
		this.startDelay = startDelay;
	}


	@Column(name="start_delay_condition_type")
	public Integer getStartDelayConditionType() {
		return this.startDelayConditionType;
	}

	public void setStartDelayConditionType(Integer startDelayConditionType) {
		this.startDelayConditionType = startDelayConditionType;
	}


	@Column(name="start_delay_notify")
	public Integer getStartDelayNotify() {
		return this.startDelayNotify;
	}

	public void setStartDelayNotify(Integer startDelayNotify) {
		this.startDelayNotify = startDelayNotify;
	}


	@Column(name="start_delay_notify_priority")
	public Integer getStartDelayNotifyPriority() {
		return this.startDelayNotifyPriority;
	}

	public void setStartDelayNotifyPriority(Integer startDelayNotifyPriority) {
		this.startDelayNotifyPriority = startDelayNotifyPriority;
	}


	@Column(name="start_delay_operation")
	public Integer getStartDelayOperation() {
		return this.startDelayOperation;
	}

	public void setStartDelayOperation(Integer startDelayOperation) {
		this.startDelayOperation = startDelayOperation;
	}


	@Column(name="start_delay_operation_end_status")
	public Integer getStartDelayOperationEndStatus() {
		return this.startDelayOperationEndStatus;
	}

	public void setStartDelayOperationEndStatus(Integer startDelayOperationEndStatus) {
		this.startDelayOperationEndStatus = startDelayOperationEndStatus;
	}


	@Column(name="start_delay_operation_end_value")
	public Integer getStartDelayOperationEndValue() {
		return this.startDelayOperationEndValue;
	}

	public void setStartDelayOperationEndValue(Integer startDelayOperationEndValue) {
		this.startDelayOperationEndValue = startDelayOperationEndValue;
	}


	@Column(name="start_delay_operation_type")
	public Integer getStartDelayOperationType() {
		return this.startDelayOperationType;
	}

	public void setStartDelayOperationType(Integer startDelayOperationType) {
		this.startDelayOperationType = startDelayOperationType;
	}


	@Column(name="start_delay_session")
	public Integer getStartDelaySession() {
		return this.startDelaySession;
	}

	public void setStartDelaySession(Integer startDelaySession) {
		this.startDelaySession = startDelaySession;
	}


	@Column(name="start_delay_session_value")
	public Integer getStartDelaySessionValue() {
		return this.startDelaySessionValue;
	}

	public void setStartDelaySessionValue(Integer startDelaySessionValue) {
		this.startDelaySessionValue = startDelaySessionValue;
	}


	@Column(name="start_delay_time")
	public Integer getStartDelayTime() {
		return this.startDelayTime;
	}

	public void setStartDelayTime(Integer startDelayTime) {
		this.startDelayTime = startDelayTime;
	}


	@Column(name="start_delay_time_value")
	public Timestamp getStartDelayTimeValue() {
		return this.startDelayTimeValue;
	}

	public void setStartDelayTimeValue(Timestamp startDelayTimeValue) {
		this.startDelayTimeValue = startDelayTimeValue;
	}


	public Integer getSuspend() {
		return this.suspend;
	}

	public void setSuspend(Integer suspend) {
		this.suspend = suspend;
	}


	@Column(name="unmatch_end_flg")
	public Integer getUnmatchEndFlg() {
		return this.unmatchEndFlg;
	}

	public void setUnmatchEndFlg(Integer unmatchEndFlg) {
		this.unmatchEndFlg = unmatchEndFlg;
	}


	@Column(name="unmatch_end_status")
	public Integer getUnmatchEndStatus() {
		return this.unmatchEndStatus;
	}

	public void setUnmatchEndStatus(Integer unmatchEndStatus) {
		this.unmatchEndStatus = unmatchEndStatus;
	}


	@Column(name="unmatch_end_value")
	public Integer getUnmatchEndValue() {
		return this.unmatchEndValue;
	}

	public void setUnmatchEndValue(Integer unmatchEndValue) {
		this.unmatchEndValue = unmatchEndValue;
	}


	@Column(name="calendar_id")
	public String getCalendarId() {
		return this.calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}


	// cc_job_file_mst
	@Column(name="check_flg")
	public Integer getCheckFlg() {
		return this.checkFlg;
	}

	public void setCheckFlg(Integer checkFlg) {
		this.checkFlg = checkFlg;
	}


	@Column(name="compression_flg")
	public Integer getCompressionFlg() {
		return this.compressionFlg;
	}

	public void setCompressionFlg(Integer compressionFlg) {
		this.compressionFlg = compressionFlg;
	}


	@Column(name="dest_directory")
	public String getDestDirectory() {
		return this.destDirectory;
	}

	public void setDestDirectory(String destDirectory) {
		this.destDirectory = destDirectory;
	}


	@Column(name="dest_work_dir")
	public String getDestWorkDir() {
		return this.destWorkDir;
	}

	public void setDestWorkDir(String destWorkDir) {
		this.destWorkDir = destWorkDir;
	}


	@Column(name="src_file")
	public String getSrcFile() {
		return this.srcFile;
	}

	public void setSrcFile(String srcFile) {
		this.srcFile = srcFile;
	}


	@Column(name="src_work_dir")
	public String getSrcWorkDir() {
		return this.srcWorkDir;
	}

	public void setSrcWorkDir(String srcWorkDir) {
		this.srcWorkDir = srcWorkDir;
	}


	@Column(name="src_facility_id")
	public String getSrcFacilityId() {
		return this.srcFacilityId;
	}

	public void setSrcFacilityId(String srcFacilityId) {
		this.srcFacilityId = srcFacilityId;
	}


	@Column(name="dest_facility_id")
	public String getDestFacilityId() {
		return this.destFacilityId;
	}

	public void setDestFacilityId(String destFacilityId) {
		this.destFacilityId = destFacilityId;
	}


	// cc_job_start_time_mst
	@Column(name="start_time")
	public Timestamp getStartTime() {
		return this.startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	@Column(name="start_minute")
	public Integer getStartMinute() {
		return this.startMinute;
	}

	public void setStartMinute(Integer startMinute) {
		this.startMinute = startMinute;
	}

	@Column(name="parent_jobunit_id")
	public String getParentJobunitId() {
		return parentJobunitId;
	}

	public void setParentJobunitId(String parentJobunitId) {
		this.parentJobunitId = parentJobunitId;
	}

	@Column(name="parent_job_id")
	public String getParentJobId() {
		return parentJobId;
	}

	public void setParentJobId(String parentJobId) {
		this.parentJobId = parentJobId;
	}

	@Column(name="refer_jobunit_id")
	public String getReferJobUnitId() {
		return referJobUnitId;
	}

	public void setReferJobUnitId(String referJobUnitId) {
		this.referJobUnitId = referJobUnitId;
	}

	@Column(name="refer_job_id")
	public String getReferJobId() {
		return referJobId;
	}

	public void setReferJobId(String referJobId) {
		this.referJobId = referJobId;
	}

	@Column(name="notify_group_id")
	public String getNotifyGroupId() {
		return notifyGroupId;
	}

	public void setNotifyGroupId(String notifyGroupId) {
		this.notifyGroupId = notifyGroupId;
	}

	@Column(name="begin_priority")
	public Integer getBeginPriority() {
		return beginPriority;
	}

	public void setBeginPriority(Integer beginPriority) {
		this.beginPriority = beginPriority;
	}

	@Column(name="normal_priority")
	public Integer getNormalPriority() {
		return normalPriority;
	}

	public void setNormalPriority(Integer normalPriority) {
		this.normalPriority = normalPriority;
	}

	@Column(name="warn_priority")
	public Integer getWarnPriority() {
		return warnPriority;
	}

	public void setWarnPriority(Integer warnPriority) {
		this.warnPriority = warnPriority;
	}

	@Column(name="abnormal_priority")
	public Integer getAbnormalPriority() {
		return abnormalPriority;
	}

	public void setAbnormalPriority(Integer abnormalPriority) {
		this.abnormalPriority = abnormalPriority;
	}

	//bi-directional many-to-one association to JobEndMstEntity
	@OneToMany(mappedBy="jobMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobEndMstEntity> getJobEndMstEntities() {
		return this.jobEndMstEntities;
	}

	public void setJobEndMstEntities(List<JobEndMstEntity> jobEndMstEntities) {
		this.jobEndMstEntities = jobEndMstEntities;
	}

	//bi-directional many-to-one association to JobParamMstEntity
	@OneToMany(mappedBy="jobMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobParamMstEntity> getJobParamMstEntities() {
		return this.jobParamMstEntities;
	}

	public void setJobParamMstEntities(List<JobParamMstEntity> jobParamMstEntities) {
		this.jobParamMstEntities = jobParamMstEntities;
	}


	//bi-directional many-to-one association to JobStartJobMstEntity
	@OneToMany(mappedBy="jobMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobStartJobMstEntity> getJobStartJobMstEntities() {
		return this.jobStartJobMstEntities;
	}

	public void setJobStartJobMstEntities(List<JobStartJobMstEntity> jobStartJobMstEntities) {
		this.jobStartJobMstEntities = jobStartJobMstEntities;
	}
}