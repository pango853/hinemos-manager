package com.clustercontrol.maintenance.model;

import java.sql.Timestamp;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_hinemos_property database table.
 * 
 */
@Entity
@Table(name="cc_hinemos_property", schema="setting")
@Cacheable(true)
@AttributeOverride(name="objectId",
		column=@Column(name="key", insertable=false, updatable=false))
public class HinemosPropertyEntity {
	private static final long serialVersionUID = 1L;

	private String key;
	private Timestamp createDatetime;
	private String createUserId;
	private String description;
	private Timestamp modifyDatetime;
	private String modifyUserId;
	private String ownerRoleId;
	private Boolean valueBoolean;
	private Integer valueNumeric;
	private String valueString;
	private Integer valueType;

	@Deprecated
	public HinemosPropertyEntity() {
	}

	public HinemosPropertyEntity(String key) {
		this.key = key;
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}

	@Id
	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Column(name="create_datetime")
	public Timestamp getCreateDatetime() {
		return this.createDatetime;
	}

	public void setCreateDatetime(Timestamp createDatetime) {
		this.createDatetime = createDatetime;
	}

	@Column(name="create_user_id")
	public String getCreateUserId() {
		return this.createUserId;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="modify_datetime")
	public Timestamp getModifyDatetime() {
		return this.modifyDatetime;
	}

	public void setModifyDatetime(Timestamp modifyDatetime) {
		this.modifyDatetime = modifyDatetime;
	}

	@Column(name="modify_user_id")
	public String getModifyUserId() {
		return this.modifyUserId;
	}

	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
	}

	@Column(name="owner_role_id")
	public String getOwnerRoleId() {
		return this.ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	@Column(name="value_boolean")
	public Boolean getValueBoolean() {
		return this.valueBoolean;
	}

	public void setValueBoolean(Boolean valueBoolean) {
		this.valueBoolean = valueBoolean;
	}

	@Column(name="value_numeric")
	public Integer getValueNumeric() {
		return this.valueNumeric;
	}

	public void setValueNumeric(Integer valueNumeric) {
		this.valueNumeric = valueNumeric;
	}

	@Column(name="value_string")
	public String getValueString() {
		return this.valueString;
	}

	public void setValueString(String valueString) {
		this.valueString = valueString;
	}

	@Column(name="value_type")
	public Integer getValueType() {
		return this.valueType;
	}

	public void setValueType(Integer valueType) {
		this.valueType = valueType;
	}

}