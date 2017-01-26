package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_job_end_mst database table.
 * 
 */
@Embeddable
public class JobEndMstEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String jobunitId;
	private String jobId;
	private Integer endStatus;

	public JobEndMstEntityPK() {
	}

	public JobEndMstEntityPK(String jobunitId, String jobId,Integer endStatus) {
		this.setJobunitId(jobunitId);
		this.setJobId(jobId);
		this.setEndStatus(endStatus);
	}

	@Column(name="jobunit_id")
	public String getJobunitId() {
		return this.jobunitId;
	}
	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	@Column(name="job_id")
	public String getJobId() {
		return this.jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	@Column(name="end_status")
	public Integer getEndStatus() {
		return this.endStatus;
	}
	public void setEndStatus(Integer endStatus) {
		this.endStatus = endStatus;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof JobEndMstEntityPK)) {
			return false;
		}
		JobEndMstEntityPK castOther = (JobEndMstEntityPK)other;
		return
				this.jobunitId.equals(castOther.jobunitId)
				&& this.jobId.equals(castOther.jobId)
				&& this.endStatus.equals(castOther.endStatus);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.jobunitId.hashCode();
		hash = hash * prime + this.jobId.hashCode();
		hash = hash * prime + this.endStatus.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"jobunitId",
				"jobId",
				"endStatus"
		};
		String[] values = {
				this.jobunitId,
				this.jobId,
				this.endStatus.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}