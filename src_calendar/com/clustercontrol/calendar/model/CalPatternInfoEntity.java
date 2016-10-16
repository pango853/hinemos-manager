package com.clustercontrol.calendar.model;


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
 * The persistent class for the cc_cal_pattern_info database table.
 * 
 */
@Entity
@Table(name="cc_cal_pattern_info", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="calendar_pattern_id", insertable=false, updatable=false))
public class CalPatternInfoEntity extends ObjectPrivilegeTargetEntity {
	private static final long serialVersionUID = 1L;
	private String calendarPatternId;
	private String calendarPatternName;
	private Timestamp regDate;
	private String regUser;
	private Timestamp updateDate;
	private String updateUser;
	private List<CalPatternDetailInfoEntity> calPatternDetailInfoEntities;

	@Deprecated
	public CalPatternInfoEntity() {
	}

	public CalPatternInfoEntity(String calPatternId) {
		this.setCalPatternId(calPatternId);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.setObjectId(this.getCalPatternId());
	}

	@Id
	@Column(name="calendar_pattern_id")
	public String getCalPatternId() {
		return this.calendarPatternId;
	}

	public void setCalPatternId(String calPatternId) {
		this.calendarPatternId = calPatternId;
	}

	@Column(name="calendar_pattern_name")
	public String getCalPatternName() {
		return this.calendarPatternName;
	}

	public void setCalPatternName(String calPatternName) {
		this.calendarPatternName = calPatternName;
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

	//bi-directional many-to-one association to CalPatternDetailInfoEntity
	@OneToMany(mappedBy="calPatternInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<CalPatternDetailInfoEntity> getCalPatternDetailInfoEntities() {
		return this.calPatternDetailInfoEntities;
	}

	public void setCalPatternDetailInfoEntities(List<CalPatternDetailInfoEntity> calPatternDetailInfoEntities) {
		this.calPatternDetailInfoEntities = calPatternDetailInfoEntities;
	}


	/**
	 * CalDetailInfoEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteCalPatternDetailInfoEntities(List<CalPatternDetailInfoEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalPatternDetailInfoEntity> list = this.getCalPatternDetailInfoEntities();
		Iterator<CalPatternDetailInfoEntity> iter = list.iterator();
		while(iter.hasNext()) {
			CalPatternDetailInfoEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}

}