/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.notify.factory;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.notify.bean.NotifyCommandInfo;
import com.clustercontrol.notify.bean.NotifyEventInfo;
import com.clustercontrol.notify.bean.NotifyInfo;
import com.clustercontrol.notify.bean.NotifyJobInfo;
import com.clustercontrol.notify.bean.NotifyLogEscalateInfo;
import com.clustercontrol.notify.bean.NotifyMailInfo;
import com.clustercontrol.notify.bean.NotifyStatusInfo;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.notify.mail.model.MailTemplateInfoEntity;
import com.clustercontrol.notify.model.NotifyCommandInfoEntity;
import com.clustercontrol.notify.model.NotifyCommandInfoEntityPK;
import com.clustercontrol.notify.model.NotifyEventInfoEntity;
import com.clustercontrol.notify.model.NotifyEventInfoEntityPK;
import com.clustercontrol.notify.model.NotifyHistoryEntity;
import com.clustercontrol.notify.model.NotifyInfoEntity;
import com.clustercontrol.notify.model.NotifyJobInfoEntity;
import com.clustercontrol.notify.model.NotifyJobInfoEntityPK;
import com.clustercontrol.notify.model.NotifyLogEscalateInfoEntity;
import com.clustercontrol.notify.model.NotifyLogEscalateInfoEntityPK;
import com.clustercontrol.notify.model.NotifyMailInfoEntity;
import com.clustercontrol.notify.model.NotifyMailInfoEntityPK;
import com.clustercontrol.notify.model.NotifyStatusInfoEntity;
import com.clustercontrol.notify.model.NotifyStatusInfoEntityPK;
import com.clustercontrol.notify.util.NotifyUtil;
import com.clustercontrol.notify.util.QueryUtil;

/**
 * 通知情報を変更するクラスです。
 *
 * @version 3.0.0
 * @since 1.0.0
 */
public class ModifyNotify {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( ModifyNotify.class );

	/**
	 * 通知情報を変更します。
	 * <p>
	 * <ol>
	 *  <li>通知IDより、通知情報を取得します。</li>
	 *  <li>通知情報を変更します。</li>
	 *  <li>通知情報に設定されている通知イベント情報を削除します。</li>
	 *  <li>通知イベント情報を作成し、通知情報に設定します。</li>
	 *  <li>キャッシュ更新用の通知情報を生成し、ログ出力キューへ送信します。
	 *      監視管理機能で、監視管理機能で保持している通知情報キャッシュが更新されます。</li>
	 * </ol>
	 *
	 * @param info 変更対象の通知情報
	 * @return 変更に成功した場合、<code> true </code>
	 * @throws NotifyDuplicate
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.ejb.entity.NotifyInfoBean
	 * @see com.clustercontrol.notify.ejb.entity.NotifyEventInfoBean
	 * @see com.clustercontrol.notify.factory.DeleteNotify#deleteEvents(Collection)
	 */
	public boolean modify(NotifyInfo info , String user) throws NotifyDuplicate, InvalidRole, HinemosUnknown {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();


		try
		{
			Timestamp now = new Timestamp(new Date().getTime());

			// 通知情報を取得
			NotifyInfoEntity notify = QueryUtil.getNotifyInfoPK(info.getNotifyId(), ObjectPrivilegeMode.MODIFY);
			String calendarId=info.getCalendarId();
			if("".equals(calendarId)){
				calendarId=null;
			}
			// 通知情報を更新
			notify.setDescription(info.getDescription());
			notify.setValidFlg(info.getValidFlg());
			notify.setInitialCount(info.getInitialCount());
			notify.setRenotifyType(info.getRenotifyType());
			notify.setNotFirstNotify(info.getNotFirstNotify());
			notify.setRenotifyPeriod(info.getRenotifyPeriod());
			notify.setUpdateDate(now);
			notify.setUpdateUser(user);
			notify.setValidFlg(info.getValidFlg());
			notify.setOwnerRoleId(info.getOwnerRoleId());
			notify.setCalendarId(calendarId);

			// 通知設定を無効に設定した場合は、関連する通知履歴を削除
			if(notify.getValidFlg().intValue() == ValidConstant.TYPE_INVALID){
				m_log.debug("remove NotifyHistory");
				List<NotifyHistoryEntity> list = QueryUtil.getNotifyHistoryByNotifyId(notify.getNotifyId());

				for(NotifyHistoryEntity history : list){
					m_log.debug("remove NotifyHistory : " + history);

					try {
						em.remove(history);
					} catch (Exception e) {
						m_log.warn("modify() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					}
				}
			}

			// 通知詳細情報を変更
			switch(info.getNotifyType()){
			case NotifyTypeConstant.TYPE_COMMAND:
				modifyNotifyCommand(info, notify);
				break;
			case NotifyTypeConstant.TYPE_EVENT:
				modifyNotifyEvent(info, notify);
				break;
			case NotifyTypeConstant.TYPE_JOB:
				modifyNotifyJob(info, notify);
				break;
			case NotifyTypeConstant.TYPE_LOG_ESCALATE:
				modifyNotifyLogEscalate(info, notify);
				break;
			case NotifyTypeConstant.TYPE_MAIL:
				modifyNotifyMail(info, notify);
				break;
			case NotifyTypeConstant.TYPE_STATUS:
				modifyNotifyStatus(info, notify);
				break;
			}

		} catch (NotifyNotFound e) {
			NotifyDuplicate e2 = new NotifyDuplicate(e.getMessage(), e);
			e2.setNotifyId(info.getNotifyId());
			throw e2;
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("modify() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return true;
	}

	private boolean modifyNotifyCommand(NotifyInfo info, NotifyInfoEntity notify) {
		NotifyCommandInfo command = info.getNotifyCommandInfo();
		if (command != null) {
			NotifyCommandInfoEntityPK entityPk = new NotifyCommandInfoEntityPK(command.getNotifyId());
			NotifyCommandInfoEntity entity = null;
			try {
				entity = QueryUtil.getNotifyCommandInfoPK(entityPk);
			} catch (NotifyNotFound e) {
				// 新規登録
				entity = new NotifyCommandInfoEntity(entityPk, notify);
			}
			NotifyUtil.copyProperties(command, entity);
		}
		return true;
	}

	private boolean modifyNotifyEvent(NotifyInfo info, NotifyInfoEntity notify) {
		NotifyEventInfo event = info.getNotifyEventInfo();
		if (event != null) {
			NotifyEventInfoEntityPK entityPk = new NotifyEventInfoEntityPK(info.getNotifyId());
			NotifyEventInfoEntity entity = null;
			try {
				entity = QueryUtil.getNotifyEventInfoPK(entityPk);
			} catch (NotifyNotFound e) {
				// 新規登録
				entity = new NotifyEventInfoEntity(entityPk, notify);
			}
			NotifyUtil.copyProperties(event, entity);
		}
		return true;
	}

	private boolean modifyNotifyJob(NotifyInfo info, NotifyInfoEntity notify) {
		NotifyJobInfo job = info.getNotifyJobInfo();
		if (job != null) {
			NotifyJobInfoEntityPK entityPk = new NotifyJobInfoEntityPK(info.getNotifyId());
			NotifyJobInfoEntity entity = null;
			try {
				entity = QueryUtil.getNotifyJobInfoPK(entityPk);
			} catch (NotifyNotFound e) {
				// 新規登録
				entity = new NotifyJobInfoEntity(entityPk, notify);
				entity.setId(entityPk);
			}

			NotifyUtil.copyProperties(job, entity);
		}
		return true;
	}

	private boolean modifyNotifyLogEscalate(NotifyInfo info, NotifyInfoEntity notify) {
		NotifyLogEscalateInfo log = info.getNotifyLogEscalateInfo();
		if (log != null) {
			NotifyLogEscalateInfoEntityPK entityPk = new NotifyLogEscalateInfoEntityPK(info.getNotifyId());
			NotifyLogEscalateInfoEntity entity = null;
			try {
				entity = QueryUtil.getNotifyLogEscalateInfoPK(entityPk);
			} catch (NotifyNotFound e) {
				// 新規登録
				entity = new NotifyLogEscalateInfoEntity(entityPk, notify);
			}

			NotifyUtil.copyProperties(log, entity);
		}
		return true;
	}

	private boolean modifyNotifyMail(NotifyInfo info, NotifyInfoEntity notify) {
		NotifyMailInfo mail = info.getNotifyMailInfo();
		if (mail != null) {
			MailTemplateInfoEntity mailTemplateInfoEntity = null;
			if (mail.getMailTemplateId() != null
					&& !"".equals(mail.getMailTemplateId())) {
				try {
					mailTemplateInfoEntity
					= com.clustercontrol.notify.mail.util.QueryUtil.getMailTemplateInfoPK(mail.getMailTemplateId());
				} catch (MailTemplateNotFound e) {
				} catch (InvalidRole e) {
				}
			}
			NotifyMailInfoEntityPK entityPk = new NotifyMailInfoEntityPK(info.getNotifyId());
			NotifyMailInfoEntity entity = null;
			try {
				entity = QueryUtil.getNotifyMailInfoPK(entityPk);
				entity.relateToMailTemplateInfoEntity(mailTemplateInfoEntity);
			} catch (NotifyNotFound e) {
				// 新規登録
				entity = new NotifyMailInfoEntity(entityPk
						, mailTemplateInfoEntity, notify);
			}
			NotifyUtil.copyProperties(mail, entity);
		}
		return true;
	}

	private boolean modifyNotifyStatus(NotifyInfo info, NotifyInfoEntity notify) {
		NotifyStatusInfo status = info.getNotifyStatusInfo();
		if (status != null) {
			NotifyStatusInfoEntityPK entityPk = new NotifyStatusInfoEntityPK(info.getNotifyId());
			NotifyStatusInfoEntity entity = null;
			try {
				entity = QueryUtil.getNotifyStatusInfoPK(entityPk);
			} catch (NotifyNotFound e) {
				// 新規登録
				entity = new NotifyStatusInfoEntity(entityPk, notify);
			}
			NotifyUtil.copyProperties(status, entity);
		}
		return true;
	}
}
