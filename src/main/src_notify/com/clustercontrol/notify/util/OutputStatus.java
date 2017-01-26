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

package com.clustercontrol.notify.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JdbcBatchExecutor;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.monitor.bean.StatusExpirationConstant;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.model.NotifyStatusInfoEntity;
import com.clustercontrol.notify.monitor.model.StatusInfoEntity;
import com.clustercontrol.notify.monitor.model.StatusInfoEntityPK;

/**
 * ステータス情報を更新するクラス<BR>
 *
 * @version 3.2.0
 * @since 3.0.0
 */
public class OutputStatus implements DependDbNotifier {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(OutputStatus.class);

	// 無期限収集の場合に設定される終了予定時刻（9999.12.31 23:59:59.000 に相当）
	private final long EXPIRATION_DATE_MAX = 253402268399000l;

	public void updateStatus(OutputBasicInfo status) {
		NotifyRequestMessage msg = new NotifyRequestMessage();
		msg.setOutputInfo(status);
		msg.setOutputDate(new Date());
		msg.setNotifyId(null);
		notify(msg);
	}
	
	@Override
	public void notify(NotifyRequestMessage message) {
		updateStatus(message.getOutputInfo(), message.getNotifyId());
	}

	public void notify(List<NotifyRequestMessage> msgList) throws NotifyNotFound {
		if (msgList.isEmpty()) {
			return;
		}
		NotifyRequestMessage firstMsg = msgList.get(0);
		List<StatusInfoEntity> oldEntityList = QueryUtil.getStatusInfoByPluginIdAndMonitorId(
						firstMsg.getOutputInfo().getPluginId(),
						firstMsg.getOutputInfo()	.getMonitorId());
		Map<StatusInfoEntityPK, StatusInfoEntity> oldEntityMap = new HashMap<StatusInfoEntityPK, StatusInfoEntity>();
		for (StatusInfoEntity oldEntity : oldEntityList) {
			oldEntityMap.put(oldEntity.getId(), oldEntity);
		}

		//一括通知で通知するメッセージは同一ロールなので、1個目のロールIDを利用する。
		String ownerRoleId = NotifyUtil.getOwnerRoleId(firstMsg, false);
		
		NotifyStatusInfoEntity notifyStatus = null;
		if (firstMsg.getNotifyId() != null) {
			notifyStatus = getNotifyStatusInfo(firstMsg);
		}
		
		List<StatusInfoEntity> insertEntityList = new ArrayList<StatusInfoEntity>();
		for (NotifyRequestMessage msg : msgList) {
			outputStatusInfo(
					msg.getOutputInfo(),
					notifyStatus,
					ownerRoleId,
					oldEntityMap,
					insertEntityList);
		}

		if (insertEntityList.size() > 0) {
			JdbcBatchExecutor.execute(new StatusInfoEntityJdbcBatchInsert(insertEntityList));
		}
	}


	private NotifyStatusInfoEntity getNotifyStatusInfo(NotifyRequestMessage msg) throws NotifyNotFound {
		return QueryUtil.getNotifyStatusInfoPK(msg.getNotifyId());
	}

	/**
	 * ステータス情報を更新します。
	 * 更新対象のステータスが存在しない場合は新規に生成します。
	 * 
	 * @param outputInfo 通知情報
	 * @param notifyId 通知ID
	 */
	private void updateStatus(OutputBasicInfo outputInfo, String notifyId) {
		NotifyStatusInfoEntity notifyStatusInfo = null;
		
		if (notifyId != null) {
			try {
				notifyStatusInfo = QueryUtil.getNotifyStatusInfoPK(notifyId);
			} catch (NotifyNotFound e) {
				m_log.debug("notify(notifyId=" + notifyId + ") not found.", e);
			}
		}

		outputStatusInfo(outputInfo, notifyStatusInfo, null, null, new ArrayList<StatusInfoEntity>());
	}

	private void outputStatusInfo(OutputBasicInfo outputInfo,
			NotifyStatusInfoEntity notifyStatusInfo,
			String roleId,
			Map<StatusInfoEntityPK, StatusInfoEntity> oldEntityMap,
			List<StatusInfoEntity> insertEntityList) {
		long outputDateTime = System.currentTimeMillis();

		// 有効期限制御フラグを設定（わかりずらい仕様のため要注意）
		// cc_status_info の expirationFlg は、有効期限制御を行うか否かの2値ではなく、
		// 行う場合はどの重要度に置き換えるのかの情報も合わせて管理する。
		Integer expirationFlg = null;
		if (notifyStatusInfo == null || notifyStatusInfo.getStatusInvalidFlg() == StatusExpirationConstant.TYPE_DELETE) {
			expirationFlg = new Integer(StatusExpirationConstant.TYPE_DELETE);
		}
		else if (notifyStatusInfo.getStatusInvalidFlg() == StatusExpirationConstant.TYPE_UPDATE) {
			// 有効期間経過後に、更新されていない旨のメッセージに置き換える場合は、
			// 置換え後の重要度を設定する
			expirationFlg = new Integer(notifyStatusInfo.getStatusUpdatePriority());
		}

		// 有効期限日時を設定
		long expirationDateTime = EXPIRATION_DATE_MAX;
		if (notifyStatusInfo != null && expirationFlg != null && notifyStatusInfo.getStatusValidPeriod() > 0) {
			// StatusValidPeriod は分単位であるため、ミリ秒単位に変換する。
			expirationDateTime = outputDateTime + notifyStatusInfo.getStatusValidPeriod() * 60 * 1000l;
		}

		outputStatusInfo(outputInfo, expirationFlg, expirationDateTime, outputDateTime, oldEntityMap, roleId, insertEntityList);
	}

	/**
	 * ステータス情報を出力します。<BR>
	 * 同じステータス情報が存在しない場合は、ステータス情報を作成します。
	 * 同じステータス情報がすでに存在する場合は、ステータス情報を更新します。
	 * 
	 * @param outputInfo 通知情報
	 * @param expirationFlg 有効期限制御フラグ
	 * @param expirationDateTime 有効期限日時
	 * @param outputDateTime 受信日時
	 * @param oldEntityMap
	 * @param roleId
	 * @param insertEntityList
	 */
	private void outputStatusInfo(OutputBasicInfo outputInfo, int expirationFlg, long expirationDateTime, long outputDateTime,
			Map<StatusInfoEntityPK, StatusInfoEntity> oldEntityMap, String roleId, List<StatusInfoEntity> insertEntityList) {
		StatusInfoEntity outputStatus = null;
		
		// エンティティ情報の検索
		StatusInfoEntityPK outputStatusPk
		= new StatusInfoEntityPK(outputInfo.getFacilityId(),
				outputInfo.getMonitorId(),
				outputInfo.getSubKey(),
				outputInfo.getPluginId());
		if (oldEntityMap == null) {
			try {
				outputStatus = com.clustercontrol.notify.monitor.util.QueryUtil.getStatusInfoPK(outputStatusPk);
			} catch (MonitorNotFound e) {
				m_log.debug("monitor not found.", e);
			} catch (InvalidRole e) {
				m_log.debug("invalid role.", e);
			}
		} else {
			outputStatus = oldEntityMap.get(outputStatusPk);
		}
		if (outputStatus == null) {
			// 検索条件に合致するエンティティが存在しないため新規に生成
			// インスタンス生成
			if (roleId == null) {
				outputStatus = new StatusInfoEntity(outputStatusPk);
			} else {
				outputStatus = new StatusInfoEntity(outputStatusPk, roleId);
			}

			// 重複チェック
			outputStatus.setApplication(outputInfo.getApplication());
			outputStatus.setExpirationDate(new Timestamp(expirationDateTime));
			outputStatus.setExpirationFlg(expirationFlg);
			outputStatus.setGenerationDate(new Timestamp(outputInfo.getGenerationDate()));
			String message = outputInfo.getMessage();
			if (message == null) {
				message = "";
			}
			if (message.length() > 255) {
				outputStatus.setMessage(message.substring(0, 255));
			} else {
				outputStatus.setMessage(message);
			}
			outputStatus.setMessageId(outputInfo.getMessageId());
			outputStatus.setOutputDate(new Timestamp(outputDateTime));
			outputStatus.setPriority(new Integer(outputInfo.getPriority()));
			insertEntityList.add(outputStatus);
		} else {
			// ステータス情報の更新
			outputStatus.setApplication(outputInfo.getApplication());
			outputStatus.setMessageId(outputInfo.getMessageId());
			outputStatus.setMessage(outputInfo.getMessage());

			// 重要度が変更されていた場合、出力日時を更新する
			if (outputStatus.getPriority().intValue() != outputInfo.getPriority()) {
				outputStatus.setGenerationDate(new java.sql.Timestamp(outputInfo.getGenerationDate()));
			}

			outputStatus.setPriority(new Integer(outputInfo.getPriority()));
			outputStatus.setOutputDate(new Timestamp(outputDateTime));
			outputStatus.setExpirationFlg(expirationFlg);
			outputStatus.setExpirationDate(new Timestamp(expirationDateTime));
		}
	}

	/**
	 * 通知失敗時の内部エラー通知を定義します
	 */
	@Override
	public void internalErrorNotify(String notifyId, String msgID, String detailMsg) {
		//FIXME
		// 何もしない
	}
}
