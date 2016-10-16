package com.clustercontrol.calendar.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;

/**
 * The persistent class for the cc_cal_detail_info database table.
 * 
 */
@Entity
@Table(name="cc_cal_detail_info", schema="setting")
@Cacheable(true)
public class CalDetailInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private CalDetailInfoEntityPK id;
	private String description;
	private Integer yearNo;
	private Integer monthNo;
	private Integer dayType;
	private Integer weekNo;
	private Integer weekXth;
	private Integer dayNo;
	private String calPatternId;
	private Integer afterDay;
	private Timestamp startTime;
	private Timestamp endTime;
	private Integer executeFlg;
	private CalInfoEntity calInfoEntity;

	@Deprecated
	public CalDetailInfoEntity() {
	}

	public CalDetailInfoEntity(CalDetailInfoEntityPK pk,
			CalInfoEntity calInfoEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToCalInfoEntity(calInfoEntity);
	}

	public CalDetailInfoEntity(CalInfoEntity calInfoEntity, Integer orderNo) {
		this(new CalDetailInfoEntityPK(calInfoEntity.getCalendarId(), orderNo), calInfoEntity);
	}

	@EmbeddedId
	public CalDetailInfoEntityPK getId() {
		return this.id;
	}

	public void setId(CalDetailInfoEntityPK id) {
		this.id = id;
	}

	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="year_no")
	public Integer getYearNo() {
		return yearNo;
	}
	public void setYearNo(Integer yearNo) {
		this.yearNo = yearNo;
	}

	@Column(name="month_no")
	public Integer getMonthNo() {
		return monthNo;
	}
	public void setMonthNo(Integer monthNo) {
		this.monthNo = monthNo;
	}

	@Column(name="day_type")
	public Integer getDayType() {
		return dayType;
	}
	public void setDayType(Integer dayType) {
		this.dayType = dayType;
	}

	@Column(name="week_no")
	public Integer getWeekNo() {
		return weekNo;
	}
	public void setWeekNo(Integer weekNo) {
		this.weekNo = weekNo;
	}

	@Column(name="week_xth")
	public Integer getWeekXth() {
		return weekXth;
	}
	public void setWeekXth(Integer weekXth) {
		this.weekXth = weekXth;
	}

	@Column(name="day_no")
	public Integer getDayNo() {
		return dayNo;
	}
	public void setDayNo(Integer dayNo) {
		this.dayNo = dayNo;
	}

	@Column(name="calendar_pattern_id")
	public String getCalPatternId() {
		return calPatternId;
	}
	public void setCalPatternId(String calPatternId) {
		this.calPatternId = calPatternId;
	}

	@Column(name="after_day")
	public Integer getAfterDay() {
		return afterDay;
	}
	public void setAfterDay(Integer afterDay) {
		this.afterDay = afterDay;
	}

	@Column(name="start_time")
	public Timestamp getStartTime() {
		return startTime;
	}
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	@Column(name="end_time")
	public Timestamp getEndTime() {
		return endTime;
	}
	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	@Column(name="execute_flg")
	public Integer getExecuteFlg() {
		return this.executeFlg;
	}
	public void setExecuteFlg(Integer executeFlg) {
		this.executeFlg = executeFlg;
	}

	//bi-directional many-to-one association to CalInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="calendar_id", insertable=false, updatable=false)
	public CalInfoEntity getCalInfoEntity() {
		return this.calInfoEntity;
	}

	@Deprecated
	public void setCalInfoEntity(CalInfoEntity calInfoEntity) {
		this.calInfoEntity = calInfoEntity;
	}

	/**
	 * CalInfoEntityオブジェクト参照設定<BR>
	 * 
	 * CalInfoEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToCalInfoEntity(CalInfoEntity calInfoEntity) {
		this.setCalInfoEntity(calInfoEntity);
		if (calInfoEntity != null) {
			List<CalDetailInfoEntity> list = calInfoEntity.getCalDetailInfoEntities();
			if (list == null) {
				list = new ArrayList<CalDetailInfoEntity>();
			} else {
				for(CalDetailInfoEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			calInfoEntity.setCalDetailInfoEntities(list);
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

		// CalInfoEntity
		if (this.calInfoEntity != null) {
			List<CalDetailInfoEntity> list = this.calInfoEntity.getCalDetailInfoEntities();
			if (list != null) {
				Iterator<CalDetailInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					CalDetailInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
}