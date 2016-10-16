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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.notify.bean.NotifyCommandInfo;
import com.clustercontrol.notify.bean.NotifyEventInfo;
import com.clustercontrol.notify.bean.NotifyInfo;
import com.clustercontrol.notify.bean.NotifyJobInfo;
import com.clustercontrol.notify.bean.NotifyLogEscalateInfo;
import com.clustercontrol.notify.bean.NotifyMailInfo;
import com.clustercontrol.notify.bean.NotifyStatusInfo;
import com.clustercontrol.notify.mail.model.MailTemplateInfoEntity;
import com.clustercontrol.notify.model.NotifyCommandInfoEntity;
import com.clustercontrol.notify.model.NotifyEventInfoEntity;
import com.clustercontrol.notify.model.NotifyInfoEntity;
import com.clustercontrol.notify.model.NotifyJobInfoEntity;
import com.clustercontrol.notify.model.NotifyLogEscalateInfoEntity;
import com.clustercontrol.notify.model.NotifyMailInfoEntity;
import com.clustercontrol.notify.model.NotifyStatusInfoEntity;
import com.clustercontrol.notify.util.QueryUtil;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * 通知情報を検索するクラスです。
 *
 * @version 3.0.0
 * @since 1.0.0
 */
public class SelectNotify {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectNotify.class );

	/**
	 * 通知情報を返します。
	 *
	 * @param notifyId 取得対象の通知ID
	 * @return 通知情報
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.ejb.entity.NotifyInfoBean
	 * @see com.clustercontrol.notify.ejb.entity.NotifyEventInfoBean
	 */
	public NotifyInfo getNotify(String notifyId) throws NotifyNotFound, InvalidRole, HinemosUnknown {
		// 通知情報を取得
		NotifyInfo info = null;

		try {
			NotifyInfoEntity notify = QueryUtil.getNotifyInfoPK(notifyId);
			info = getNotifyInfo(notify);
		} catch (NotifyNotFound e) {
			AplLogger apllog = new AplLogger("NOTIFY", "notify");
			String[] args = { notifyId };
			apllog.put("SYS", "004", args);
			throw e;
		} catch (InvalidRole e) {
			AplLogger apllog = new AplLogger("NOTIFY", "notify");
			String[] args = { notifyId };
			apllog.put("SYS", "004", args);
			throw e;
		} catch (HinemosUnknown e) {
			AplLogger apllog = new AplLogger("NOTIFY", "notify");
			String[] args = { notifyId };
			apllog.put("SYS", "004", args);
			throw e;
		}

		return info;
	}

	private NotifyInfo getNotifyInfo(NotifyInfoEntity notify) throws HinemosUnknown {

		NotifyInfo bean = null;
		String notifyId = notify.getNotifyId();

		NotifyCommandInfo notifyCommand = getNotifyCommand(notifyId,notify);
		NotifyEventInfo notifyEvent = getNotifyEvent(notifyId,notify);
		NotifyJobInfo notifyJob = getNotifyJob(notifyId,notify);
		NotifyLogEscalateInfo notifyLogEscalate = getNotifyLogEscalate(notifyId,notify);
		NotifyMailInfo notifyMail = getNotifyMail(notifyId,notify);
		NotifyStatusInfo notifyStatus = getNotifyStatus(notifyId,notify);
		Long regDate = notify.getRegDate() == null ? null:notify.getRegDate().getTime();
		Long updateDate = notify.getUpdateDate() == null ? null:notify.getUpdateDate().getTime();

		bean = new NotifyInfo(
				notify.getNotifyId(),
				notify.getDescription(),
				notify.getNotifyType(),
				notify.getInitialCount(),
				notify.getNotFirstNotify(),
				notify.getRenotifyType(),
				notify.getRenotifyPeriod(),
				regDate,
				updateDate,
				notify.getRegUser(),
				notify.getUpdateUser(),
				notify.getValidFlg(),
				notify.getCalendarId(),
				notify.getOwnerRoleId(),
				notifyCommand,
				notifyEvent,
				notifyJob,
				notifyLogEscalate,
				notifyMail,
				notifyStatus
				);
		return bean;
	}

	/**
	 * コマンド通知詳細情報を取得します。
	 *
	 * @param notifyId
	 * @param notify
	 * @return
	 */
	private NotifyCommandInfo getNotifyCommand(String notifyId, NotifyInfoEntity notify){
		NotifyCommandInfoEntity entity = notify.getNotifyCommandInfoEntity();
		if (entity == null) {
			return null;
		}

		NotifyCommandInfo info = new NotifyCommandInfo();
		info.setNotifyId(notifyId);

		info.setInfoValidFlg(entity.getInfoValidFlg());
		info.setWarnValidFlg(entity.getWarnValidFlg());
		info.setCriticalValidFlg(entity.getCriticalValidFlg());
		info.setUnknownValidFlg(entity.getUnknownValidFlg());

		info.setInfoCommand(entity.getInfoCommand());
		info.setWarnCommand(entity.getWarnCommand());
		info.setCriticalCommand(entity.getCriticalCommand());
		info.setUnknownCommand(entity.getUnknownCommand());

		info.setInfoEffectiveUser(entity.getInfoEffectiveUser());
		info.setWarnEffectiveUser(entity.getWarnEffectiveUser());
		info.setCriticalEffectiveUser(entity.getCriticalEffectiveUser());
		info.setUnknownEffectiveUser(entity.getUnknownEffectiveUser());

		info.setTimeout(entity.getCommandTimeout());
		info.setSetEnvironment(entity.getSetEnvironment());

		return info;
	}

	/**
	 * イベント通知詳細情報を取得します。
	 *
	 * @param notifyId
	 * @param notify
	 * @return
	 */
	private NotifyEventInfo getNotifyEvent(String notifyId, NotifyInfoEntity notify){
		NotifyEventInfoEntity entity = notify.getNotifyEventInfoEntity();
		if (entity == null) {
			return null;
		}

		NotifyEventInfo info = new NotifyEventInfo();
		info.setNotifyId(notifyId);

		info.setInfoValidFlg(entity.getInfoEventNormalFlg());
		info.setWarnValidFlg(entity.getWarnEventNormalFlg());
		info.setCriticalValidFlg(entity.getCriticalEventNormalFlg());
		info.setUnknownValidFlg(entity.getUnknownEventNormalFlg());

		info.setInfoEventNormalState(entity.getInfoEventNormalState());
		info.setWarnEventNormalState(entity.getWarnEventNormalState());
		info.setCriticalEventNormalState(entity.getCriticalEventNormalState());
		info.setUnknownEventNormalState(entity.getUnknownEventNormalState());

		return info;
	}

	/**
	 * ジョブ通知詳細情報を取得します。
	 *
	 * @param notifyId
	 * @param notify
	 * @return
	 * @throws HinemosUnknown
	 */
	private NotifyJobInfo getNotifyJob(String notifyId, NotifyInfoEntity notify) throws HinemosUnknown {
		NotifyJobInfoEntity entity = notify.getNotifyJobInfoEntity();
		if (entity == null) {
			return null;
		}

		String facilityPath = new RepositoryControllerBean().getFacilityPath(entity.getJobExecFacilityId(), null);

		NotifyJobInfo info = new NotifyJobInfo();
		info.setNotifyId(notifyId);

		info.setInfoValidFlg(entity.getInfoJobRun());
		info.setWarnValidFlg(entity.getWarnJobRun());
		info.setCriticalValidFlg(entity.getCriticalJobRun());
		info.setUnknownValidFlg(entity.getUnknownJobRun());

		info.setInfoJobunitId(entity.getInfoJobunitId());
		info.setWarnJobunitId(entity.getWarnJobunitId());
		info.setCriticalJobunitId(entity.getCriticalJobunitId());
		info.setUnknownJobunitId(entity.getUnknownJobunitId());

		info.setInfoJobId(entity.getInfoJobId());
		info.setWarnJobId(entity.getWarnJobId());
		info.setCriticalJobId(entity.getCriticalJobId());
		info.setUnknownJobId(entity.getUnknownJobId());

		info.setInfoJobFailurePriority(entity.getInfoJobFailurePriority());
		info.setWarnJobFailurePriority(entity.getWarnJobFailurePriority());
		info.setCriticalJobFailurePriority(entity.getCriticalJobFailurePriority());
		info.setUnknownJobFailurePriority(entity.getUnknownJobFailurePriority());

		info.setJobExecFacilityFlg(entity.getJobExecFacilityFlg());
		info.setJobExecFacility(entity.getJobExecFacilityId());
		info.setJobExecScope(facilityPath);

		return info;

	}

	/**
	 * ログエスカレーション通知詳細情報を取得します。
	 *
	 * @param notifyId
	 * @param notify
	 * @return
	 * @throws HinemosUnknown
	 */
	private NotifyLogEscalateInfo getNotifyLogEscalate(String notifyId, NotifyInfoEntity notify) throws HinemosUnknown {
		NotifyLogEscalateInfoEntity entity = notify.getNotifyLogEscalateInfoEntity();
		if (entity == null) {
			return null;
		}

		NotifyLogEscalateInfo info = new NotifyLogEscalateInfo();
		info.setNotifyId(notifyId);

		info.setInfoValidFlg(entity.getInfoEscalateFlg());
		info.setWarnValidFlg(entity.getWarnEscalateFlg());
		info.setCriticalValidFlg(entity.getCriticalEscalateFlg());
		info.setUnknownValidFlg(entity.getUnknownEscalateFlg());

		info.setInfoEscalateMessage(entity.getInfoEscalateMessage());
		info.setWarnEscalateMessage(entity.getWarnEscalateMessage());
		info.setCriticalEscalateMessage(entity.getCriticalEscalateMessage());
		info.setUnknownEscalateMessage(entity.getUnknownEscalateMessage());

		info.setInfoSyslogFacility(entity.getInfoSyslogFacility());
		info.setWarnSyslogFacility(entity.getWarnSyslogFacility());
		info.setCriticalSyslogFacility(entity.getCriticalSyslogFacility());
		info.setUnknownSyslogFacility(entity.getUnknownSyslogFacility());

		info.setInfoSyslogPriority(entity.getInfoSyslogPriority());
		info.setWarnSyslogPriority(entity.getWarnSyslogPriority());
		info.setCriticalSyslogPriority(entity.getCriticalSyslogPriority());
		info.setUnknownSyslogPriority(entity.getUnknownSyslogPriority());

		info.setEscalateFacilityFlg(entity.getEscalateFacilityFlg());
		info.setEscalatePort(entity.getEscalatePort());
		info.setEscalateFacility(entity.getEscalateFacilityId());
		
		//スコープの取得
		String facilityPath = new RepositoryControllerBean().getFacilityPath(entity.getEscalateFacilityId(), null);
		info.setEscalateScope(facilityPath);

		return info;
	}

	/**
	 * メール通知詳細情報を取得します。
	 *
	 * @param notifyId
	 * @param notify
	 * @return
	 */
	private NotifyMailInfo getNotifyMail(String notifyId, NotifyInfoEntity notify){
		NotifyMailInfoEntity entity = notify.getNotifyMailInfoEntity();
		if (entity == null) {
			return null;
		}

		NotifyMailInfo info = new NotifyMailInfo();
		info.setNotifyId(notifyId);
		MailTemplateInfoEntity mailTemplateInfoEntity = entity.getMailTemplateInfoEntity();
		if (mailTemplateInfoEntity != null) {
			info.setMailTemplateId(mailTemplateInfoEntity.getMailTemplateId());
		}

		info.setInfoValidFlg(entity.getInfoMailFlg());
		info.setWarnValidFlg(entity.getWarnMailFlg());
		info.setCriticalValidFlg(entity.getCriticalMailFlg());
		info.setUnknownValidFlg(entity.getUnknownMailFlg());

		info.setInfoMailAddress(entity.getInfoMailAddress());
		info.setWarnMailAddress(entity.getWarnMailAddress());
		info.setCriticalMailAddress(entity.getCriticalMailAddress());
		info.setUnknownMailAddress(entity.getUnknownMailAddress());

		return info;
	}

	/**
	 * ステータス通知詳細情報を取得します。
	 *
	 * @param notifyId
	 * @param notify
	 * @return
	 */
	private NotifyStatusInfo getNotifyStatus(String notifyId, NotifyInfoEntity notify){
		NotifyStatusInfoEntity entity = notify.getNotifyStatusInfoEntity();
		if (entity == null) {
			return null;
		}

		NotifyStatusInfo info = new NotifyStatusInfo();
		info.setNotifyId(notifyId);

		info.setInfoValidFlg(entity.getInfoStatusFlg());
		info.setWarnValidFlg(entity.getWarnStatusFlg());
		info.setCriticalValidFlg(entity.getCriticalStatusFlg());
		info.setUnknownValidFlg(entity.getUnknownStatusFlg());

		info.setStatusInvalidFlg(entity.getStatusInvalidFlg());
		info.setStatusUpdatePriority(entity.getStatusUpdatePriority());
		info.setStatusValidPeriod(entity.getStatusValidPeriod());

		return info;
	}

	/**
	 * 通知情報一覧を返します(障害検知用通知を除く)。
	 * <p>
	 * <ol>
	 * <li>通知IDの昇順に並んだ全ての通知情報を取得します。</li>
	 * <li>１通知情報をテーブルのカラム順（{@link com.clustercontrol.notify.bean.NotifyTableDefine}）に、リスト（{@link ArrayList}）にセットします。</li>
	 * <li>この１通知情報を保持するリストを、通知情報一覧を保持するリスト（{@link ArrayList}）に格納し返します。<BR>
	 *  <dl>
	 *  <dt>通知情報一覧（Objectの2次元配列）</dt>
	 *  <dd>{ 通知情報1 {カラム1の値, カラム2の値, … }, 通知情報2{カラム1の値, カラム2の値, …}, … }</dd>
	 *  </dl>
	 * </li>
	 * </ol>
	 *
	 * @return 通知情報一覧（Objectの2次元配列）
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.bean.NotifyTableDefine
	 * @see #collectionToArray(Collection)
	 */
	public ArrayList<NotifyInfo> getNotifyList() throws NotifyNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("getNotifyList() : start");
		ArrayList<NotifyInfo> list = new ArrayList<NotifyInfo>();

		try {
			// 通知情報一覧を取得
			List<NotifyInfoEntity> ct = QueryUtil.getAllNotifyInfoOrderByNotifyId();
			for(NotifyInfoEntity notify : ct){
				list.add(getNotify(notify.getNotifyId()));
			}
		} catch (NotifyNotFound e) {
			AplLogger apllog = new AplLogger("NOTIFY", "notify");
			apllog.put("SYS", "006");
			throw e;
		} catch (InvalidRole e) {
			AplLogger apllog = new AplLogger("NOTIFY", "notify");
			apllog.put("SYS", "006");
			throw e;
		} catch (HinemosUnknown e) {
			AplLogger apllog = new AplLogger("NOTIFY", "notify");
			apllog.put("SYS", "006");
			throw e;
		}

		return list;
	}

	/**
	 * オーナーロールIDを条件として通知情報一覧を返します(障害検知用通知を除く)。
	 *
	 * @param ownerRoleId
	 * @return 通知情報一覧（Objectの2次元配列）
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.bean.NotifyTableDefine
	 * @see #collectionToArray(Collection)
	 */
	public ArrayList<NotifyInfo> getNotifyListByOwnerRole(String ownerRoleId) throws HinemosUnknown {
		m_log.debug("getNotifyListByOwnerRole() : start");
		ArrayList<NotifyInfo> list = new ArrayList<NotifyInfo>();

		try {
			// 通知情報一覧を取得
			List<NotifyInfoEntity> ct = QueryUtil.getAllNotifyInfoOrderByNotifyId_OR(ownerRoleId);
			for(NotifyInfoEntity notify : ct){
				list.add(getNotifyInfo(notify));
			}
		} catch (HinemosUnknown e) {
			AplLogger apllog = new AplLogger("NOTIFY", "notify");
			apllog.put("SYS", "006");
			throw e;
		}

		return list;
	}

}
