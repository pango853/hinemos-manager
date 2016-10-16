package com.clustercontrol.notify.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_notify_command_info database table.
 * 
 */
@Entity
@Table(name="cc_notify_command_info", schema="setting")
@Cacheable(true)
public class NotifyCommandInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private NotifyCommandInfoEntityPK id;
	private Long commandTimeout;
	private Integer setEnvironment;

	private Integer infoValidFlg;
	private Integer warnValidFlg;
	private Integer criticalValidFlg;
	private Integer unknownValidFlg;

	private String infoCommand;
	private String warnCommand;
	private String criticalCommand;
	private String unknownCommand;

	private String infoEffectiveUser;
	private String warnEffectiveUser;
	private String criticalEffectiveUser;
	private String unknownEffectiveUser;

	private NotifyInfoEntity notifyInfoEntity;

	@Deprecated
	public NotifyCommandInfoEntity() {
	}

	public NotifyCommandInfoEntity(NotifyCommandInfoEntityPK pk,
			NotifyInfoEntity notifyInfoEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToNotifyInfoEntity(notifyInfoEntity);
	}

	public NotifyCommandInfoEntity(String notifyId,
			NotifyInfoEntity notifyInfoEntity) {
		this(new NotifyCommandInfoEntityPK(notifyId), notifyInfoEntity);
	}

	@Column(name="info_valid_flg")
	public Integer getInfoValidFlg() {
		return infoValidFlg;
	}

	public void setInfoValidFlg(Integer infoValidFlg) {
		this.infoValidFlg = infoValidFlg;
	}

	@Column(name="warn_valid_flg")
	public Integer getWarnValidFlg() {
		return warnValidFlg;
	}

	public void setWarnValidFlg(Integer warnValidFlg) {
		this.warnValidFlg = warnValidFlg;
	}

	@Column(name="critical_valid_flg")
	public Integer getCriticalValidFlg() {
		return criticalValidFlg;
	}

	public void setCriticalValidFlg(Integer criticalValidFlg) {
		this.criticalValidFlg = criticalValidFlg;
	}

	@Column(name="unknown_valid_flg")
	public Integer getUnknownValidFlg() {
		return unknownValidFlg;
	}

	public void setUnknownValidFlg(Integer unknownValidFlg) {
		this.unknownValidFlg = unknownValidFlg;
	}

	@Column(name="info_command")
	public String getInfoCommand() {
		return infoCommand;
	}

	public void setInfoCommand(String infoCommand) {
		this.infoCommand = infoCommand;
	}

	@Column(name="warn_command")
	public String getWarnCommand() {
		return warnCommand;
	}

	public void setWarnCommand(String warnCommand) {
		this.warnCommand = warnCommand;
	}

	@Column(name="critical_command")
	public String getCriticalCommand() {
		return criticalCommand;
	}

	public void setCriticalCommand(String criticalCommand) {
		this.criticalCommand = criticalCommand;
	}

	@Column(name="unknown_command")
	public String getUnknownCommand() {
		return unknownCommand;
	}

	public void setUnknownCommand(String unknownCommand) {
		this.unknownCommand = unknownCommand;
	}


	@Column(name="info_effective_user")
	public String getInfoEffectiveUser() {
		return infoEffectiveUser;
	}

	public void setInfoEffectiveUser(String infoEffectiveUser) {
		this.infoEffectiveUser = infoEffectiveUser;
	}

	@Column(name="warn_effective_user")
	public String getWarnEffectiveUser() {
		return warnEffectiveUser;
	}

	public void setWarnEffectiveUser(String warnEffectiveUser) {
		this.warnEffectiveUser = warnEffectiveUser;
	}

	@Column(name="critical_effective_user")
	public String getCriticalEffectiveUser() {
		return criticalEffectiveUser;
	}

	public void setCriticalEffectiveUser(String criticalEffectiveUser) {
		this.criticalEffectiveUser = criticalEffectiveUser;
	}

	@Column(name="unknown_effective_user")
	public String getUnknownEffectiveUser() {
		return unknownEffectiveUser;
	}

	public void setUnknownEffectiveUser(String unknownEffectiveUser) {
		this.unknownEffectiveUser = unknownEffectiveUser;
	}


	@EmbeddedId
	public NotifyCommandInfoEntityPK getId() {
		return this.id;
	}

	public void setId(NotifyCommandInfoEntityPK id) {
		this.id = id;
	}


	@Column(name="command_timeout")
	public Long getCommandTimeout() {
		return this.commandTimeout;
	}

	public void setCommandTimeout(Long commandTimeout) {
		this.commandTimeout = commandTimeout;
	}


	@Column(name="set_environment")
	public Integer getSetEnvironment() {
		return this.setEnvironment;
	}

	public void setSetEnvironment(Integer setEnvironment) {
		this.setEnvironment = setEnvironment;
	}

	//bi-directional one-to-one association to NotifyInfoEntity
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="notify_id", insertable=false, updatable=false)
	public NotifyInfoEntity getNotifyInfoEntity() {
		return this.notifyInfoEntity;
	}

	@Deprecated
	public void setNotifyInfoEntity(NotifyInfoEntity notifyInfoEntity) {
		this.notifyInfoEntity = notifyInfoEntity;
	}

	/**
	 * NotifyInfoEntityオブジェクト参照設定<BR>
	 * 
	 * NotifyInfoEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToNotifyInfoEntity(NotifyInfoEntity notifyInfoEntity) {
		this.setNotifyInfoEntity(notifyInfoEntity);
		if (notifyInfoEntity != null) {
			notifyInfoEntity.setNotifyCommandInfoEntity(this);
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
		// NotifyInfoEntity
		if (this.notifyInfoEntity != null) {
			this.notifyInfoEntity.setNotifyCommandInfoEntity(null);
		}
	}
}