package com.clustercontrol.notify.model;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.notify.mail.model.MailTemplateInfoEntity;



/**
 * The persistent class for the cc_notify_mail_info database table.
 * 
 */
@Entity
@Table(name="cc_notify_mail_info", schema="setting")
@Cacheable(true)
public class NotifyMailInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private NotifyMailInfoEntityPK id;

	private String infoMailAddress;
	private String warnMailAddress;
	private String criticalMailAddress;
	private String unknownMailAddress;

	private Integer infoMailFlg;
	private Integer warnMailFlg;
	private Integer criticalMailFlg;
	private Integer unknownMailFlg;

	private MailTemplateInfoEntity mailTemplateInfoEntity;
	private NotifyInfoEntity notifyInfoEntity;

	@Deprecated
	public NotifyMailInfoEntity() {
	}

	public NotifyMailInfoEntity(NotifyMailInfoEntityPK pk,
			MailTemplateInfoEntity mailTemplateInfoEntity, NotifyInfoEntity notifyInfoEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToMailTemplateInfoEntity(mailTemplateInfoEntity);
		this.relateToNotifyInfoEntity(notifyInfoEntity);
	}

	public NotifyMailInfoEntity(String notifyId,
			MailTemplateInfoEntity mailTemplateInfoEntity,
			NotifyInfoEntity notifyInfoEntity) {
		this(new NotifyMailInfoEntityPK(notifyId), mailTemplateInfoEntity,
				notifyInfoEntity);
	}


	@EmbeddedId
	public NotifyMailInfoEntityPK getId() {
		return this.id;
	}

	public void setId(NotifyMailInfoEntityPK id) {
		this.id = id;
	}


	@Column(name="info_mail_address")
	public String getInfoMailAddress() {
		return this.infoMailAddress;
	}

	public void setInfoMailAddress(String infoMailAddress) {
		this.infoMailAddress = infoMailAddress;
	}


	@Column(name="warn_mail_address")
	public String getWarnMailAddress() {
		return this.warnMailAddress;
	}

	public void setWarnMailAddress(String warnMailAddress) {
		this.warnMailAddress = warnMailAddress;
	}


	@Column(name="critical_mail_address")
	public String getCriticalMailAddress() {
		return this.criticalMailAddress;
	}

	public void setCriticalMailAddress(String criticalMailAddress) {
		this.criticalMailAddress = criticalMailAddress;
	}


	@Column(name="unknown_mail_address")
	public String getUnknownMailAddress() {
		return this.unknownMailAddress;
	}

	public void setUnknownMailAddress(String unknownMailAddress) {
		this.unknownMailAddress = unknownMailAddress;
	}


	@Column(name="info_mail_flg")
	public Integer getInfoMailFlg() {
		return this.infoMailFlg;
	}

	public void setInfoMailFlg(Integer infoMailFlg) {
		this.infoMailFlg = infoMailFlg;
	}


	@Column(name="warn_mail_flg")
	public Integer getWarnMailFlg() {
		return this.warnMailFlg;
	}

	public void setWarnMailFlg(Integer warnMailFlg) {
		this.warnMailFlg = warnMailFlg;
	}


	@Column(name="critical_mail_flg")
	public Integer getCriticalMailFlg() {
		return this.criticalMailFlg;
	}

	public void setCriticalMailFlg(Integer criticalMailFlg) {
		this.criticalMailFlg = criticalMailFlg;
	}


	@Column(name="unknown_mail_flg")
	public Integer getUnknownMailFlg() {
		return this.unknownMailFlg;
	}

	public void setUnknownMailFlg(Integer unknownMailFlg) {
		this.unknownMailFlg = unknownMailFlg;
	}



	//bi-directional many-to-one association to MailTemplateInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="mail_template_id")
	public MailTemplateInfoEntity getMailTemplateInfoEntity() {
		return this.mailTemplateInfoEntity;
	}

	@Deprecated
	public void setMailTemplateInfoEntity(MailTemplateInfoEntity mailTemplateInfoEntity) {
		this.mailTemplateInfoEntity = mailTemplateInfoEntity;
	}

	/**
	 * MailTemplateInfoEntityオブジェクト参照設定<BR>
	 * 
	 * MailTemplateInfoEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToMailTemplateInfoEntity(MailTemplateInfoEntity mailTemplateInfoEntity) {
		this.setMailTemplateInfoEntity(mailTemplateInfoEntity);
		if (mailTemplateInfoEntity != null) {
			List<NotifyMailInfoEntity> list = mailTemplateInfoEntity.getNotifyMailInfoEntities();
			if (list == null) {
				list = new ArrayList<NotifyMailInfoEntity>();
			} else {
				for(NotifyMailInfoEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			mailTemplateInfoEntity.setNotifyMailInfoEntities(list);
		}
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
			notifyInfoEntity.setNotifyMailInfoEntity(this);
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
			this.notifyInfoEntity.setNotifyMailInfoEntity(null);
		}

		// MailTemplateInfoEntity
		if (this.mailTemplateInfoEntity != null) {
			List<NotifyMailInfoEntity> list = this.mailTemplateInfoEntity.getNotifyMailInfoEntities();
			if (list != null) {
				Iterator<NotifyMailInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					NotifyMailInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}