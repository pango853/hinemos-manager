package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 * The persistent class for the cc_job_end_info database table.
 * 
 */
@Entity
@Table(name="cc_job_end_info", schema="log")
public class JobEndInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobEndInfoEntityPK id;
	private Integer endValue		=	null;
	private Integer endValueFrom	=	null;
	private Integer endValueTo		=	null;
	private JobInfoEntity jobInfoEntity;

	@Deprecated
	public JobEndInfoEntity() {
	}

	public JobEndInfoEntity(JobEndInfoEntityPK pk,
			JobInfoEntity jobInfoEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToJobInfoEntity(jobInfoEntity);
	}

	public JobEndInfoEntity(
			JobInfoEntity jobInfoEntity,
			Integer endStatus) {
		this(new JobEndInfoEntityPK(
				jobInfoEntity.getId().getSessionId(),
				jobInfoEntity.getId().getJobunitId(),
				jobInfoEntity.getId().getJobId(),
				endStatus), jobInfoEntity);
	}


	@EmbeddedId
	public JobEndInfoEntityPK getId() {
		return this.id;
	}

	public void setId(JobEndInfoEntityPK id) {
		this.id = id;
	}


	@Column(name="end_value")
	public Integer getEndValue() {
		return this.endValue;
	}

	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}


	@Column(name="end_value_from")
	public Integer getEndValueFrom() {
		return this.endValueFrom;
	}

	public void setEndValueFrom(Integer endValueFrom) {
		this.endValueFrom = endValueFrom;
	}


	@Column(name="end_value_to")
	public Integer getEndValueTo() {
		return this.endValueTo;
	}

	public void setEndValueTo(Integer endValueTo) {
		this.endValueTo = endValueTo;
	}


	//bi-directional many-to-one association to JobInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="job_id", referencedColumnName="job_id", insertable=false, updatable=false),
		@JoinColumn(name="jobunit_id", referencedColumnName="jobunit_id", insertable=false, updatable=false),
		@JoinColumn(name="session_id", referencedColumnName="session_id", insertable=false, updatable=false)
	})
	public JobInfoEntity getJobInfoEntity() {
		return this.jobInfoEntity;
	}

	@Deprecated
	public void setJobInfoEntity(JobInfoEntity jobInfoEntity) {
		this.jobInfoEntity = jobInfoEntity;
	}

	/**
	 * JobInfoEntityオブジェクト参照設定<BR>
	 * 
	 * JobInfoEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToJobInfoEntity(JobInfoEntity jobInfoEntity) {
		this.setJobInfoEntity(jobInfoEntity);
		if (jobInfoEntity != null) {
			List<JobEndInfoEntity> list = jobInfoEntity.getJobEndInfoEntities();
			if (list == null) {
				list = new ArrayList<JobEndInfoEntity>();
			} else {
				for(JobEndInfoEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobInfoEntity.setJobEndInfoEntities(list);
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

		// JobInfoEntity
		if (this.jobInfoEntity != null) {
			List<JobEndInfoEntity> list = this.jobInfoEntity.getJobEndInfoEntities();
			if (list != null) {
				Iterator<JobEndInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobEndInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}