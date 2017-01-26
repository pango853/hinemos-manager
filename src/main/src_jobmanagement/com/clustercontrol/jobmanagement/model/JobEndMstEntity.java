package com.clustercontrol.jobmanagement.model;

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
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_job_end_mst database table.
 * 
 */
@Entity
@Table(name="cc_job_end_mst", schema="setting")
@Cacheable(true)
public class JobEndMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobEndMstEntityPK id;
	private Integer endValue;
	private Integer endValueFrom;
	private Integer endValueTo;
	private JobMstEntity jobMstEntity;

	@Deprecated
	public JobEndMstEntity() {
	}

	public JobEndMstEntity(JobEndMstEntityPK pk,
			JobMstEntity jobMstEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToJobMstEntity(jobMstEntity);
	}

	public JobEndMstEntity(JobMstEntity jobMstEntity, Integer endStatus) {
		this(new JobEndMstEntityPK(
				jobMstEntity.getId().getJobunitId(),
				jobMstEntity.getId().getJobId(),
				endStatus), jobMstEntity);
	}


	@EmbeddedId
	public JobEndMstEntityPK getId() {
		return this.id;
	}

	public void setId(JobEndMstEntityPK id) {
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


	//bi-directional many-to-one association to JobMstEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="job_id", referencedColumnName="job_id", insertable=false, updatable=false),
		@JoinColumn(name="jobunit_id", referencedColumnName="jobunit_id", insertable=false, updatable=false)
	})
	public JobMstEntity getJobMstEntity() {
		return this.jobMstEntity;
	}

	@Deprecated
	public void setJobMstEntity(JobMstEntity jobMstEntity) {
		this.jobMstEntity = jobMstEntity;
	}

	/**
	 * JobMstEntityオブジェクト参照設定<BR>
	 * 
	 * JobMstEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToJobMstEntity(JobMstEntity jobMstEntity) {
		this.setJobMstEntity(jobMstEntity);
		if (jobMstEntity != null) {
			List<JobEndMstEntity> list = jobMstEntity.getJobEndMstEntities();
			if (list == null) {
				list = new ArrayList<JobEndMstEntity>();
			} else {
				for(JobEndMstEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobMstEntity.setJobEndMstEntities(list);
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

		// JobMstEntity
		if (this.jobMstEntity != null) {
			List<JobEndMstEntity> list = this.jobMstEntity.getJobEndMstEntities();
			if (list != null) {
				Iterator<JobEndMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobEndMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}