/*

Copyright (C) since 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.notify.util;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.jobmanagement.util.JobValidator;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.notify.bean.ExecFacilityConstant;
import com.clustercontrol.notify.bean.NotifyCommandInfo;
import com.clustercontrol.notify.bean.NotifyEventInfo;
import com.clustercontrol.notify.bean.NotifyInfo;
import com.clustercontrol.notify.bean.NotifyJobInfo;
import com.clustercontrol.notify.bean.NotifyLogEscalateInfo;
import com.clustercontrol.notify.bean.NotifyMailInfo;
import com.clustercontrol.notify.bean.NotifyStatusInfo;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.notify.mail.bean.MailTemplateInfo;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.util.Messages;

/**
 * 通知の入力チェッククラス
 * 
 * @since 4.0
 */
public class NotifyValidator {

	private static Log m_log = LogFactory.getLog(NotifyValidator.class);

	private static String getPriority(int i) {
		String[] priorities = new String[] { PriorityConstant.STRING_INFO,
				PriorityConstant.STRING_WARNING,
				PriorityConstant.STRING_CRITICAL,
				PriorityConstant.STRING_UNKNOWN };
		return priorities[i];
	}

	private static boolean isNullOrEmpty(String str) {
		return str == null || str.isEmpty();
	}

	private static void throwInvalidSetting(HinemosException e)
			throws InvalidSetting {
		m_log.info("validateNotifyInfo() : " + e.getClass().getSimpleName()
				+ ", " + e.getMessage());
		throw new InvalidSetting(e.getMessage(), e);
	}

	private static void throwInvalidSetting(String messageId)
			throws InvalidSetting {
		InvalidSetting e = new InvalidSetting(Messages.getString(messageId));
		m_log.info("validateNotifyInfo() : " + e.getClass().getSimpleName()
				+ ", " + e.getMessage());
		throw e;
	}

	private static void throwInvalidSetting(String messageId, String priority)
			throws InvalidSetting {
		String[] args = { "(" + priority + ")" };
		InvalidSetting e = new InvalidSetting(Messages.getString(messageId,
				args));
		m_log.info("validateNotifyInfo() : " + e.getClass().getSimpleName()
				+ ", " + e.getMessage());
		throw e;
	}

	private static boolean validateCommandInfo(NotifyCommandInfo info,
			NotifyInfo notifyInfo) throws InvalidSetting {
		ArrayList<Integer> validFlgIndexes = NotifyUtil.getValidFlgIndexes(info);
		if (validFlgIndexes.isEmpty()) {
			return false;
		}

		// 実効ユーザ
		String[] effectiveUsers = new String[] {
				info.getInfoEffectiveUser(),
				info.getWarnEffectiveUser(),
				info.getCriticalEffectiveUser(),
				info.getUnknownEffectiveUser()
		};
		// 実行コマンド
		String[] commands = new String[] { info.getInfoCommand(),
				info.getWarnCommand(), info.getCriticalCommand(),
				info.getUnknownCommand() };

		for (int i = 0; i < validFlgIndexes.size(); i++) {
			int validFlgIndex = validFlgIndexes.get(i);
			if (isNullOrEmpty(effectiveUsers[validFlgIndex])) {
				throwInvalidSetting("message.notify.31");
			}
			CommonValidator.validateString("effective.user", effectiveUsers[validFlgIndex],
					true, 1, 64);

			if (isNullOrEmpty(commands[validFlgIndex])) {
				throwInvalidSetting("message.notify.32");
			}
			CommonValidator.validateString("command", commands[validFlgIndex], true, 1, 1024);
		}

		return true;
	}

	private static boolean validateEventInfo(NotifyEventInfo info,
			NotifyInfo notifyInfo) {
		return !NotifyUtil.getValidFlgIndexes(info).isEmpty();
	}

	private static boolean validateJobInfo(NotifyJobInfo info,
			NotifyInfo notifyInfo) throws InvalidSetting, InvalidRole {
		ArrayList<Integer> validFlgIndexes = NotifyUtil.getValidFlgIndexes(info);
		if (validFlgIndexes.isEmpty()) {
			return false;
		}

		if (info.getJobExecFacilityFlg() == ExecFacilityConstant.TYPE_FIX) {
			// 固定スコープを選択している場合の確認
			if (info.getJobExecFacility() == null) {
				throwInvalidSetting("message.notify.40");
			}
			try {
				FacilityTreeCache.validateFacilityId(info.getJobExecFacility(),
						notifyInfo.getOwnerRoleId(), false);
			} catch (FacilityNotFound e) {
				throwInvalidSetting(e);
			}
		}

		String[] jobIds = new String[] {
				info.getInfoJobId(),
				info.getWarnJobId(),
				info.getCriticalJobId(),
				info.getUnknownJobId()
		};
		String[] jobunitIds = new String[] {
				info.getInfoJobunitId(),
				info.getWarnJobunitId(),
				info.getCriticalJobunitId(),
				info.getUnknownJobunitId()
		};
		for (int i = 0; i < validFlgIndexes.size(); i++) {
			int validFlgIndex = validFlgIndexes.get(i);
			if (isNullOrEmpty(jobIds[validFlgIndex])) {
				throwInvalidSetting("message.notify.20");
			}

			// ジョブIDが指定されている場合、参照可能かどうかを確認する
			JobValidator.validateJobId(jobunitIds[validFlgIndex],
					jobIds[validFlgIndex], notifyInfo.getOwnerRoleId());
		}

		return true;
	}

	private static boolean validateLogInfo(NotifyLogEscalateInfo info,
			NotifyInfo notifyInfo) throws InvalidSetting, InvalidRole {
		ArrayList<Integer> validFlgIndexes = NotifyUtil.getValidFlgIndexes(info);
		if (validFlgIndexes.isEmpty()) {
			return false;
		}

		if (info.getEscalateFacilityFlg() == ExecFacilityConstant.TYPE_FIX) {
			// 固定スコープを選択している場合の確認
			if (info.getEscalateFacility() == null) {
				throwInvalidSetting("message.notify.40");
			}

			try {
				FacilityTreeCache.validateFacilityId(
						info.getEscalateFacility(),
						notifyInfo.getOwnerRoleId(), false);
			} catch (FacilityNotFound e) {
				throwInvalidSetting(e);
			}
		}

		Integer[] syslogFacilities = new Integer[] {
				info.getInfoSyslogFacility(), info.getWarnSyslogFacility(),
				info.getCriticalSyslogFacility(),
				info.getUnknownSyslogFacility() };
		Integer[] syslogPriorities = new Integer[] {
				info.getInfoSyslogPriority(), info.getWarnSyslogPriority(),
				info.getCriticalSyslogPriority(),
				info.getUnknownSyslogPriority() };
		String[] escalateMessages = new String[] {
				info.getInfoEscalateMessage(), info.getWarnEscalateMessage(),
				info.getCriticalEscalateMessage(),
				info.getUnknownEscalateMessage() };

		for (int i = 0; i < validFlgIndexes.size(); i++) {
			int validFlgIndex = validFlgIndexes.get(i);
			if (syslogFacilities[validFlgIndex] == null) {
				throwInvalidSetting("message.notify.21", getPriority(validFlgIndex));
			}
			if (syslogPriorities[validFlgIndex] == null) {
				throwInvalidSetting("message.notify.22", getPriority(validFlgIndex));
			}
			if (isNullOrEmpty(escalateMessages[validFlgIndex])) {
				throwInvalidSetting("message.notify.23", getPriority(validFlgIndex));
			}
			CommonValidator.validateString(Messages.getString("message"),
					escalateMessages[validFlgIndex], true, 1, 1024);

		}

		if (info.getEscalatePort() == null) {
			throwInvalidSetting("message.notify.24");
		}
		CommonValidator.validateInt(Messages.getString("port.number"),
				info.getEscalatePort(), 1, DataRangeConstant.PORT_NUMBER_MAX);

		return true;
	}

	private static boolean validateMailInfo(NotifyMailInfo info,
			NotifyInfo notifyInfo) throws InvalidSetting, InvalidRole {
		ArrayList<Integer> validFlgIndexes = NotifyUtil.getValidFlgIndexes(info);
		if (validFlgIndexes.isEmpty()) {
			return false;
		}

		// メールテンプレートの参照権限
		if (info.getMailTemplateId() != null) {
			try {
				// 存在確認
				com.clustercontrol.notify.mail.util.QueryUtil
						.getMailTemplateInfoPK(info.getMailTemplateId());
				// 権限確認
				com.clustercontrol.notify.mail.util.QueryUtil
						.getMailTemplateInfoPK_OR(info.getMailTemplateId(),
								notifyInfo.getOwnerRoleId());
			} catch (MailTemplateNotFound e) {
				throw new InvalidSetting(e.getMessage(), e);
			} catch (InvalidRole e) {
				m_log.warn("validateNotifyInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		String[] mailAddresses = new String[] { info.getInfoMailAddress(),
				info.getWarnMailAddress(), info.getCriticalMailAddress(),
				info.getUnknownMailAddress() };
		for (int i = 0; i < validFlgIndexes.size(); i++) {
			int validFlgIndex = validFlgIndexes.get(i);

			if (isNullOrEmpty(mailAddresses[validFlgIndex])) {
				throwInvalidSetting("message.notify.18");
			}

			CommonValidator.validateString(
					Messages.getString("email.address.ssv"), mailAddresses[validFlgIndex], true,
					1, 1024);
		}

		return true;
	}

	/**
	 * メールテンプレート情報の妥当性チェック
	 * 
	 * @param mailTemplateInfo
	 * @throws InvalidSetting
	 */
	public static void validateMailTemplateInfo(
			MailTemplateInfo mailTemplateInfo) throws InvalidSetting {
		// mailTemplateId
		CommonValidator.validateId(Messages.getString("mail.template.id"),
				mailTemplateInfo.getMailTemplateId(), 64);

		CommonValidator.validateString(Messages.getString("description"),
				mailTemplateInfo.getDescription(), false, 0, 256);

		// ownerRoleId
		CommonValidator.validateOwnerRoleId(mailTemplateInfo.getOwnerRoleId(),
				true, mailTemplateInfo.getMailTemplateId(),
				HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE);

	}

	public static void validateNotifyInfo(NotifyInfo notifyInfo)
			throws InvalidSetting, InvalidRole {
		// notifyId
		CommonValidator.validateId(Messages.getString("message.notify.10"),
				notifyInfo.getNotifyId(), 64);

		// description
		CommonValidator.validateString(Messages.getString("description"),
				notifyInfo.getDescription(), false, 0, 256);

		// ownerRoleId
		CommonValidator
				.validateOwnerRoleId(notifyInfo.getOwnerRoleId(), true,
						notifyInfo.getNotifyId(),
						HinemosModuleConstant.PLATFORM_NOTIFY);

		// 再通知抑制期間
		if (notifyInfo.getRenotifyPeriod() != null) {
			CommonValidator.validateInt(
					Messages.getString("suppress.by.time.interval"),
					notifyInfo.getRenotifyPeriod(), 1,
					DataRangeConstant.SMALLINT_HIGH);
		}
		// 初回通知するまでのカウント
		int maxInitialCount = HinemosPropertyUtil.getHinemosPropertyNum(
				"notify.initial.count.max", 10);
		CommonValidator.validateInt(Messages.getString("notify.initial"),
				notifyInfo.getInitialCount(), 1, maxInitialCount - 1);

		// コマンド通知
		boolean result = true;
		switch (notifyInfo.getNotifyType()) {
		case NotifyTypeConstant.TYPE_COMMAND:
			NotifyCommandInfo command = notifyInfo.getNotifyCommandInfo();
			result = validateCommandInfo(command, notifyInfo);
			break;

		case NotifyTypeConstant.TYPE_EVENT:
			NotifyEventInfo event = notifyInfo.getNotifyEventInfo();
			result = validateEventInfo(event, notifyInfo);
			break;

		case NotifyTypeConstant.TYPE_JOB:
			NotifyJobInfo job = notifyInfo.getNotifyJobInfo();
			result = validateJobInfo(job, notifyInfo);
			break;

		case NotifyTypeConstant.TYPE_LOG_ESCALATE:
			NotifyLogEscalateInfo log = notifyInfo.getNotifyLogEscalateInfo();
			result = validateLogInfo(log, notifyInfo);
			break;

		case NotifyTypeConstant.TYPE_MAIL:
			NotifyMailInfo mail = notifyInfo.getNotifyMailInfo();
			result = validateMailInfo(mail, notifyInfo);
			break;

		case NotifyTypeConstant.TYPE_STATUS:
			NotifyStatusInfo status = notifyInfo.getNotifyStatusInfo();
			result = validateStatusInfo(status, notifyInfo);
			break;

		default:
			break;
		}
		if (!result) {
			throwInvalidSetting("message.notify.13");
		}
	}

	private static boolean validateStatusInfo(NotifyStatusInfo info,
			NotifyInfo notifyInfo) {
		return !NotifyUtil.getValidFlgIndexes(info).isEmpty();
	}
}
