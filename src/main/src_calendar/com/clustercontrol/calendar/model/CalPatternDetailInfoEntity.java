package com.clustercontrol.calendar.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;

/**
 * The persistent class for the cc_cal_pattern_detail_info database table.
 * 
 */
@Entity
@Table(name="cc_cal_pattern_detail_info", schema="setting")
@Cacheable(true)
public class CalPatternDetailInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private CalPatternDetailInfoEntityPK id;
	private CalPatternInfoEntity calPatternInfoEntity;

	@Deprecated
	public CalPatternDetailInfoEntity() {
	}

	public CalPatternDetailInfoEntity(CalPatternDetailInfoEntityPK pk,
			CalPatternInfoEntity calPatternInfoEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToCalPatternInfoEntity(calPatternInfoEntity);
	}

	public CalPatternDetailInfoEntity(String calPatternId,
			Integer yearNo, Integer monthNo, Integer dayNo,
			CalPatternInfoEntity calPatternInfoEntity) {
		this(new CalPatternDetailInfoEntityPK(calPatternId, yearNo, monthNo, dayNo), calPatternInfoEntity);
	}

	@EmbeddedId
	public CalPatternDetailInfoEntityPK getId() {
		return this.id;
	}

	public void setId(CalPatternDetailInfoEntityPK id) {
		this.id = id;
	}

	//bi-directional many-to-one association to CalPatternInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="calendar_pattern_id", insertable=false, updatable=false)
	public CalPatternInfoEntity getCalPatternInfoEntity() {
		return this.calPatternInfoEntity;
	}

	@Deprecated
	public void setCalPatternInfoEntity(CalPatternInfoEntity calPatternInfoEntity) {
		this.calPatternInfoEntity = calPatternInfoEntity;
	}

	/**
	 * CalPatternInfoEntityオブジェクト参照設定<BR>
	 * 
	 * CalPatternInfoEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToCalPatternInfoEntity(CalPatternInfoEntity calPatternInfoEntity) {
		this.setCalPatternInfoEntity(calPatternInfoEntity);
		if (calPatternInfoEntity != null) {
			List<CalPatternDetailInfoEntity> list = calPatternInfoEntity.getCalPatternDetailInfoEntities();
			if (list == null) {
				list = new ArrayList<CalPatternDetailInfoEntity>();
			} else {
				for(CalPatternDetailInfoEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			calPatternInfoEntity.setCalPatternDetailInfoEntities(list);
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

		// CalPatternInfoEntity
		if (this.calPatternInfoEntity != null) {
			List<CalPatternDetailInfoEntity> list = this.calPatternInfoEntity.getCalPatternDetailInfoEntities();
			if (list != null) {
				Iterator<CalPatternDetailInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					CalPatternDetailInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
}