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
import java.util.Date;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.notify.bean.NotifyCommandInfo;
import com.clustercontrol.notify.bean.NotifyEventInfo;
import com.clustercontrol.notify.bean.NotifyInfo;
import com.clustercontrol.notify.bean.NotifyJobInfo;
import com.clustercontrol.notify.bean.NotifyLogEscalateInfo;
import com.clustercontrol.notify.bean.NotifyMailInfo;
import com.clustercontrol.notify.bean.NotifyStatusInfo;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.notify.mail.model.MailTemplateInfoEntity;
import com.clustercontrol.notify.mail.util.QueryUtil;
import com.clustercontrol.notify.model.NotifyCommandInfoEntity;
import com.clustercontrol.notify.model.NotifyEventInfoEntity;
import com.clustercontrol.notify.model.NotifyInfoEntity;
import com.clustercontrol.notify.model.NotifyJobInfoEntity;
import com.clustercontrol.notify.model.NotifyLogEscalateInfoEntity;
import com.clustercontrol.notify.model.NotifyMailInfoEntity;
import com.clustercontrol.notify.model.NotifyStatusInfoEntity;
import com.clustercontrol.notify.util.NotifyUtil;

/**
 * 通知情報を作成するクラスです。
 *
 * @version 3.0.0
 * @since 1.0.0
 */
public class AddNotify {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( AddNotify.class );

	/**
	 * 通知情報を作成します。
	 * <p>
	 * <ol>
	 *  <li>通知情報を作成します。</li>
	 *  <li>通知イベント情報を作成し、通知情報に設定します。</li>
	 *  <li>キャッシュ更新用の通知情報を生成し、ログ出力キューへ送信します。
	 *      監視管理機能で、監視管理機能で保持している通知情報キャッシュに追加されます。</li>
	 * </ol>
	 * 
	 * @param info 作成対象の通知情報
	 * @return 作成に成功した場合、<code> true </code>
	 * @throws HinemosUnknown
	 * @throws NotifyDuplicate
	 * 
	 * @see com.clustercontrol.notify.ejb.entity.NotifyInfoBean
	 * @see com.clustercontrol.notify.ejb.entity.NotifyEventInfoBean
	 */
	public boolean add(NotifyInfo info, String user) throws HinemosUnknown, NotifyDuplicate {
		m_log.debug("add " + "NotifyID = " + info.getNotifyId());

		JpaTransactionManager jtm = new JpaTransactionManager();

		try {
			Timestamp now = new Timestamp(new Date().getTime());

			// 通知情報を挿入
			// インスタンス生成
			NotifyInfoEntity notify = new NotifyInfoEntity(info.getNotifyId());
			String calendarId=info.getCalendarId();
			if("".equals(calendarId)){
				calendarId=null;
			}
			// 重複チェック
			jtm.checkEntityExists(NotifyInfoEntity.class, notify.getNotifyId());
			notify.setDescription(info.getDescription());
			notify.setInitialCount(info.getInitialCount());
			notify.setNotifyType(info.getNotifyType());
			notify.setRenotifyPeriod(info.getRenotifyPeriod());
			notify.setRenotifyType(info.getRenotifyType());
			notify.setNotFirstNotify(info.getNotFirstNotify());
			notify.setValidFlg(info.getValidFlg());
			notify.setOwnerRoleId(info.getOwnerRoleId());
			notify.setCalendarId(calendarId);
			notify.setRegDate(now);
			notify.setRegUser(user);
			notify.setUpdateDate(now);
			notify.setUpdateUser(user);

			switch(info.getNotifyType()){
			case NotifyTypeConstant.TYPE_COMMAND:
				addNotifyCommand(info, notify);
				break;
			case NotifyTypeConstant.TYPE_EVENT:
				addNotifyEvent(info, notify);
				break;
			case NotifyTypeConstant.TYPE_JOB:
				addNotifyJob(info, notify);
				break;
			case NotifyTypeConstant.TYPE_LOG_ESCALATE:
				addNotifyLogEscalate(info, notify);
				break;
			case NotifyTypeConstant.TYPE_MAIL:
				addNotifyMail(info, notify);
				break;
			case NotifyTypeConstant.TYPE_STATUS:
				addNotifyStatus(info, notify);
				break;
			}

		} catch (EntityExistsException e) {
			m_log.info("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new NotifyDuplicate(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return true;
	}

	/**
	 * コマンド通知情報詳細を変更します。
	 * @param info　変更情報
	 * @param notify 変更対象Bean
	 * @return
	 */
	private boolean addNotifyCommand(NotifyInfo info, NotifyInfoEntity notify) {
		NotifyCommandInfo command = info.getNotifyCommandInfo();
		if (command == null) {
			return true;
		}

		NotifyCommandInfoEntity entity = new NotifyCommandInfoEntity(
				command.getNotifyId(), notify);
		NotifyUtil.copyProperties(command, entity);

		return true;
	}

	/**
	 * イベント通知情報詳細を変更します。
	 * @param info　変更情報
	 * @param notify 変更対象Bean
	 * @return
	 */
	private boolean addNotifyEvent(NotifyInfo info, NotifyInfoEntity notify) {
		// 通知イベント情報を挿入
		NotifyEventInfo event = info.getNotifyEventInfo();
		if (event != null) {
			NotifyEventInfoEntity entity = new NotifyEventInfoEntity(info.getNotifyId(),
					notify);
			NotifyUtil.copyProperties(event, entity);
		}
		return true;
	}

	/**
	 * ジョブ通知情報詳細を変更します。
	 * @param info　変更情報
	 * @param notify 変更対象Bean
	 * @return
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound
	 */
	private boolean addNotifyJob(NotifyInfo info, NotifyInfoEntity notify) throws HinemosUnknown, FacilityNotFound {
		NotifyJobInfo job = info.getNotifyJobInfo();
		if (job != null) {
			NotifyJobInfoEntity entity = new NotifyJobInfoEntity(info.getNotifyId(),
					notify);
			NotifyUtil.copyProperties(job, entity);
		}
		return true;
	}

	/**
	 * ログエスカレーション通知情報詳細を変更します。
	 * @param info　変更情報
	 * @param notify 変更対象Bean
	 * @return
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound
	 */
	private boolean addNotifyLogEscalate(NotifyInfo info, NotifyInfoEntity notify) throws HinemosUnknown, FacilityNotFound {
		NotifyLogEscalateInfo log = info.getNotifyLogEscalateInfo();
		if (log != null) {
			NotifyLogEscalateInfoEntity entity = new NotifyLogEscalateInfoEntity(info.getNotifyId(),
					notify);
			NotifyUtil.copyProperties(log, entity);
		}
		return true;
	}

	/**
	 * メール通知情報詳細を変更します。
	 * @param info　変更情報
	 * @param notify 変更対象Bean
	 * @return
	 */
	private boolean addNotifyMail(NotifyInfo info, NotifyInfoEntity notify) {
		NotifyMailInfo mail = info.getNotifyMailInfo();
		if (mail != null) {
			MailTemplateInfoEntity mailTemplateInfoEntity = null;
			if (mail.getMailTemplateId() != null
					&& !"".equals(mail.getMailTemplateId())) {
				try {
					mailTemplateInfoEntity
					= QueryUtil.getMailTemplateInfoPK(mail.getMailTemplateId());
				} catch (MailTemplateNotFound e) {
				} catch (InvalidRole e) {
				}
			}
			NotifyMailInfoEntity entity = new NotifyMailInfoEntity(info.getNotifyId(),
					mailTemplateInfoEntity, notify);
			NotifyUtil.copyProperties(mail, entity);
		}
		return true;
	}

	/**
	 * ステータス通知情報詳細を変更します。
	 * @param info　変更情報
	 * @param notify 変更対象Bean
	 * @return
	 */
	private boolean addNotifyStatus(NotifyInfo info, NotifyInfoEntity notify) {
		NotifyStatusInfo status = info.getNotifyStatusInfo();
		if (status != null) {
			NotifyStatusInfoEntity entity = new NotifyStatusInfoEntity(info.getNotifyId(),
					notify);
			NotifyUtil.copyProperties(status, entity);
		}
		return true;
	}
}
