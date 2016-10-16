/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.UserIdConstant;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.SnmpResponseError;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.bean.EventConfirmConstant;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.repository.bean.DeviceSearchMessageInfo;
import com.clustercontrol.repository.bean.NodeInfo;
import com.clustercontrol.repository.bean.NodeInfoDeviceSearch;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.Messages;

/**
 * 自動デバイスサーチ処理の実装クラス
 */
public class DeviceSearchTask implements Callable<Boolean> {

	private static Log m_log = LogFactory.getLog(DeviceSearchTask.class);
	private static final String MSG_001 = "001";
	private static final String MSG_002 = "002";
	private String facilityId;

	/**
	 * コンストラクタ
	 * @param facilityId 処理対象のファシリティID
	 */
	public DeviceSearchTask(String facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * 自動デバイスサーチ処理の実行
	 */
	@Override
	public Boolean call() {
		m_log.debug("run() start");
		String messageId = MSG_001;
		boolean isOutPutLog = false;
		NodeInfoDeviceSearch nodeDeviceSearch = null;

		try {
			String user = HinemosPropertyUtil.getHinemosPropertyStr("repository.auto.device.user", UserIdConstant.HINEMOS);
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, user);
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR,
					new AccessControllerBean().isAdministrator());
			RepositoryControllerBean controller = new RepositoryControllerBean();

			//ノードの数だけSNMPでノード情報を再取得
			NodeInfo node = controller.getNode(facilityId);

			if (node.isAutoDeviceSearch() == false) {
				return isOutPutLog;
			}

			nodeDeviceSearch = controller.getNodePropertyBySNMP(
					node.getAvailableIpAddress(),
					node.getSnmpPort(),
					node.getSnmpCommunity(),
					node.getSnmpVersion(),
					facilityId,
					node.getSnmpSecurityLevel(),
					node.getSnmpUser(),
					node.getSnmpAuthPassword(),
					node.getSnmpPrivPassword(),
					node.getSnmpAuthProtocol(),
					node.getSnmpPrivProtocol()
					);

			if (nodeDeviceSearch.getDeviceSearchMessageInfo() != null
					&& nodeDeviceSearch.getDeviceSearchMessageInfo().size() > 0) {
				//変更ありの場合はDB更新
				controller.modifyNode(nodeDeviceSearch.getNewNodeInfo());
				//変更ありの場合はイベント登録
				isOutPutLog = true;
			}
		} catch (SnmpResponseError e) {
			//SNMPの応答エラー時にイベント登録有無を取得
			isOutPutLog = HinemosPropertyUtil.getHinemosPropertyBool("repository.auto.device.find.log", false);
		} catch (Exception e) {
			m_log.warn("run() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			messageId = MSG_002;
			//SNMPの応答エラー時にイベント登録有無を取得
			isOutPutLog = HinemosPropertyUtil.getHinemosPropertyBool("repository.auto.device.find.log", false);
		} finally {
			//イベントログの登録
			if (isOutPutLog) {
				String details = "";
				ArrayList<DeviceSearchMessageInfo> list = nodeDeviceSearch == null ? null : nodeDeviceSearch.getDeviceSearchMessageInfo();

				for (DeviceSearchMessageInfo msgInfo : list) {
					details = details.length() > 0 ? details + ", " : details;
					details = details + msgInfo.getItemName() + " "
							+ Messages.getString("lasttime") + ":" + msgInfo.getLastVal() + " "
							+ Messages.getString("thistime") + ":" + msgInfo.getThisVal();
				}
				putEvent(messageId, facilityId, details);
			}
		}
		return isOutPutLog;
	}

	private void putEvent(String msgID, String facilityID, String details) {
		//現在日時取得
		Date nowDate = new Date();
		String msg = null;
		int priority = 0;

		if (msgID == MSG_001) {
			msg = Messages.getString("message.repository.snmp.2") + " " + details;
			priority = PriorityConstant.TYPE_INFO;
		} else {
			msg = Messages.getString("message.repository.snmp.3");
			priority = PriorityConstant.TYPE_WARNING;
		}

		//メッセージ情報作成
		OutputBasicInfo output = new OutputBasicInfo();
		output.setPluginId(HinemosModuleConstant.REPOSITORY_DEVICE_SEARCH);
		output.setMonitorId(HinemosModuleConstant.SYSYTEM);
		output.setFacilityId(facilityID);
		output.setScopeText(facilityID);
		output.setApplication("");
		output.setMessageId(msgID);
		output.setMessage(msg);
		output.setMessageOrg(msg);
		output.setPriority(priority);
		output.setGenerationDate(nowDate.getTime());

		try {
			new NotifyControllerBean().insertEventLog(output, EventConfirmConstant.TYPE_UNCONFIRMED);
		} catch (Exception e ) {
			m_log.error(e.getMessage(), e);
		}
	}
}
