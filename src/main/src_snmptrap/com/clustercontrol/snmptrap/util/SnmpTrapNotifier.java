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

package com.clustercontrol.snmptrap.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.snmptrap.bean.SnmpTrap;
import com.clustercontrol.snmptrap.bean.TrapId;

/**
 * snmptrap監視の通知実装のインタフェース(シングル版)
 */
public class SnmpTrapNotifier {
	private static final Log log = LogFactory.getLog(SnmpTrapNotifier.class);

	public static final String MESSAGE_ID_INFO = "001";
	public static final String MESSAGE_ID_WARNING = "002";
	public static final String MESSAGE_ID_CRITICAL = "003";
	public static final String MESSAGE_ID_UNKNOWN = "004";

	public static final int _messageMaxLength = 255;

	public static String replaceUnsafeCharacter(String str) {

		String charList = HinemosPropertyUtil.getHinemosPropertyStr("notify.replace.before", "0");
		char newChar = HinemosPropertyUtil.getHinemosPropertyStr("notify.replace.after", "?").charAt(0);

		for (String oldCharByte : charList.split(",")) {
			int oldCharInt = Integer.parseInt(oldCharByte);
			Character oldChar = (char)oldCharInt;
			if (str != null) {
				str = str.replace(oldChar, newChar);
			}
		}

		return str;
	}

	private OutputBasicInfo createOutputBasicInfo(SnmpTrap snmptrap,
			String monitorId, int priority, String facilityId,
			String facilityPath, String application, String msg, String msgOrig) {
		OutputBasicInfo info = new OutputBasicInfo();

		info.setMonitorId(monitorId);
		info.setPluginId(HinemosModuleConstant.MONITOR_SNMPTRAP);

		TrapId trapV1 = snmptrap.getTrapId().asTrapV1Id();

		info.setSubKey(trapV1.getEnterpriseId() + "_" + trapV1.getGenericId() + "_" + trapV1.getSpecificId());

		info.setPriority(priority);

		info.setApplication(application);
		info.setFacilityId(facilityId);
		info.setScopeText(facilityPath);
		info.setGenerationDate(snmptrap.getReceivedTime());

		msg = replaceUnsafeCharacter(msg);
		msgOrig = replaceUnsafeCharacter(msgOrig);

		if (priority == PriorityConstant.TYPE_INFO) {
			info.setMessageId(MESSAGE_ID_INFO);
		} else if (priority == PriorityConstant.TYPE_WARNING) {
			info.setMessageId(MESSAGE_ID_WARNING);
		} else if (priority == PriorityConstant.TYPE_CRITICAL) {
			info.setMessageId(MESSAGE_ID_CRITICAL);
		} else {
			info.setMessageId(MESSAGE_ID_UNKNOWN);
		}

		if (msg.length() > _messageMaxLength) {
			info.setMessage(msg.substring(0, _messageMaxLength));
		} else {
			info.setMessage(msg);
		}

		info.setMessageOrg(msgOrig);
		return info;
	}

	public void put(List<SnmpTrap> receivedTrapList,
			List<MonitorInfoEntity> monitorList,
			List<Integer> priorityList, 
			List<String> facilityIdList,
			List<String[]> msgsList) {
		
		Map<String, String> facilityPathMap = new HashMap<String, String>();
		Map<MonitorInfoEntity, ArrayList<OutputBasicInfo>> outputBasicInfoMap = new HashMap<MonitorInfoEntity, ArrayList<OutputBasicInfo>>();
		for (int i = 0; i < receivedTrapList.size(); i++) {
			SnmpTrap receivedTrap = receivedTrapList.get(i);
			MonitorInfoEntity monitor = monitorList.get(i);
			int priority = priorityList.get(i);
			String facilityId = facilityIdList.get(i);
			String[] msgs = msgsList.get(i);

			String facilityPath = facilityPathMap.get(facilityId);
			if (facilityPath == null) {
				facilityPath = getFacilityPath(facilityId, receivedTrap);
				if (facilityPath == null) {
					continue;
				}
				facilityPathMap.put(facilityId, facilityPath);
			}
			
			OutputBasicInfo basicInfo = createOutputBasicInfo(receivedTrap,
					monitor.getMonitorId(), priority, facilityId, facilityPath,
					monitor.getApplication(), msgs[0], msgs[1]);
			ArrayList<OutputBasicInfo> basicInfoList = outputBasicInfoMap.get(monitor);
			if (basicInfoList == null) {
				basicInfoList = new ArrayList<OutputBasicInfo>();
			}
			basicInfoList.add(basicInfo);
			outputBasicInfoMap.put(monitor, basicInfoList);
		}
		
		for (MonitorInfoEntity monitor : outputBasicInfoMap.keySet()) {
			new NotifyControllerBean().notify(outputBasicInfoMap.get(monitor), monitor.getNotifyGroupId());
		}
	}
	
	private String getFacilityPath(String facilityId, SnmpTrap receivedTrap) {
		if (FacilityTreeAttributeConstant.UNREGISTEREFD_SCOPE.equals(facilityId)) {
			return receivedTrap.getAgentAddr();
		}
		else {
			try {
				return new RepositoryControllerBean().getFacilityPath(facilityId, null);
			} catch (HinemosUnknown e) {
				log.error(e);
			}
			
			return null;
		}
	}
}