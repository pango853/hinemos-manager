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

package com.clustercontrol.util.apllog;


import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.bean.EventConfirmConstant;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.notify.util.NotifyUtil;
import com.clustercontrol.notify.util.SendSyslog;
import com.clustercontrol.util.CommandCreator;
import com.clustercontrol.util.CommandCreator.PlatformType;
import com.clustercontrol.util.CommandExecutor;
import com.clustercontrol.util.CommandExecutor.CommandResult;
import com.clustercontrol.util.StringBinder;

/**
 *
 * Hinemosの内部ログ（HinemosApp.log）の出力を行うクラス<BR>
 *
 * Hinemos内部で発生する事象をログやHinemosのイベントとして
 * 処理します。
 *
 */
public class AplLogger {

	private static final String RESOURCE_BUNDLE= "com.clustercontrol.util.apllog.apllog";
	private static ResourceBundle m_bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);

	private static final String INTERNAL_SCOPE="INTERNAL";
	private static final String INTERNAL_SCOPE_TEXT="Hinemos_Internal";

	private static final String PRIORITY_INFO = "info";
	private static final String PRIORITY_WARNING = "warning";
	private static final String PRIORITY_CRITICAL = "critical";
	private String m_pluginID;
	private String m_aplName;

	private static ConcurrentHashMap<Long, Boolean> m_isInternalMode = new ConcurrentHashMap<Long, Boolean>();
	private static Log FILE_LOGGER = LogFactory.getLog("HinemosInternal");
	private static Log log = LogFactory.getLog(AplLogger.class);

	/**
	 * コンストラクタ<BR>
	 *
	 * @param pluginID	プラグインID
	 * @param aplId		アプリケーションID
	 */
	public AplLogger(String pluginID, String aplId) {
		m_pluginID = pluginID;

		//アプリケーション名取得
		m_aplName = getString(pluginID + "." + aplId);
	}

	/**
	 * メインクラス
	 * @param args
	 */
	public static void main(String[] args) {
		AplLogger apllog = new AplLogger("REP","rep");
		apllog.put("SYS","001");
		apllog.put("USR","001");
		apllog.put("USR","002");
		apllog.put("USR","003");
	}

	/**
	 * ログを出力します。<BR>
	 *
	 * @param monitorID		監視項目ID
	 * @param msgID			メッセージID
	 * @since
	 */
	public void put(String monitorID, String msgID) {
		put(monitorID, msgID,null,null);
	}

	/**
	 *
	 * ログを出力します。<BR>
	 * @param monitorID		監視項目ID
	 * @param msgID			メッセージID
	 * @param msgArgs		メッセージ置換項目
	 * @since
	 */
	public void put(String monitorID, String msgID, Object[] msgArgs) {
		put(monitorID, msgID,msgArgs,null);
	}

	/**
	 *
	 * ログを出力します。<BR>
	 *
	 * @param monitorId		監視項目ID
	 * @param msgID			メッセージID
	 * @param msgArgs		メッセージ置換項目
	 * @param detailMsg		詳細メッセージ
	 * @since
	 */
	public void put(String monitorId, String msgID, Object[] msgArgs, String detailMsg) {
		put(monitorId, msgID, msgArgs, detailMsg, null);
	}

	/**
	 *
	 * ログを出力します。<BR>
	 *
	 * @param monitorId		監視項目ID
	 * @param msgID			メッセージID
	 * @param msgArgs		メッセージ置換項目
	 * @param detailMsg		詳細メッセージ
	 * @since
	 */
	public void put(String monitorId, String msgID, Object[] msgArgs, String detailMsg, Integer priority) {
		// 既にINTERNALモードに入っている場合はこれ以上出力しない
		Boolean isInternalMode = m_isInternalMode.get(Thread.currentThread().getId());
		if(isInternalMode != null && isInternalMode){
			log.debug("AplLogger has been already in INTERNAL mode. No INTERNAL logs are outputted. ");
			return;
		}
		try{
			// INTERNALモードに入る
			log.debug("AplLogger INTERNAL mode start");
			m_isInternalMode.put(Thread.currentThread().getId(), true);

			//現在日時取得
			Date nowDate = new Date();

			//監視項目IDとプラグインIDでイベントメッセージ（リソースファイル）から
			String keyBase = m_pluginID + "." + monitorId + "." + msgID;

			//重要度を取得
			String keyPriority = keyBase + "." + "priority";

			if (priority == null) {
				priority = getPriority(getString(keyPriority));
			}

			//メッセージを取得する。
			String keyMsg = keyBase + "." + "message";

			//メッセージ項目値が指定されている場合、メッセージの項目を置換
			String msg = null;
			if(msgArgs != null && msgArgs.length != 0){
				msg = getString(keyMsg,msgArgs);
			}else{
				msg = getString(keyMsg);
			}

			//    	・発生日時				:現在日時※メソッド呼び出し時に取得したもの
			//    	・出力日時				:現在日時※イベント出力直前に取得したもの

			//メッセージ情報作成
			OutputBasicInfo output = new OutputBasicInfo();
			output.setPluginId(m_pluginID);
			output.setMonitorId(monitorId);
			output.setFacilityId(INTERNAL_SCOPE);
			output.setScopeText(INTERNAL_SCOPE_TEXT);
			output.setApplication(m_aplName);
			output.setMessageId(msgID);
			output.setMessage(msg);
			output.setMessageOrg(detailMsg == null ? "":detailMsg);
			output.setPriority(priority);
			if (nowDate != null) {
				output.setGenerationDate(nowDate.getTime());
			}

			/////
			// 設定値取得(internal.event)
			////
			boolean isEvent = HinemosPropertyUtil.getHinemosPropertyBool("internal.event", true);
			int eventLevel = getPriority(HinemosPropertyUtil.getHinemosPropertyStr("internal.event.priority", PRIORITY_INFO));

			if(isEvent && isOutput(eventLevel, priority)){
				putEvent(output);
			}

			/////
			// 設定値取得(internal.file)
			////
			boolean isFile = HinemosPropertyUtil.getHinemosPropertyBool("internal.file", true);
			int fileLevel = getPriority(HinemosPropertyUtil.getHinemosPropertyStr("internal.file.priority", PRIORITY_INFO));

			if(isFile && isOutput(fileLevel, priority)){
				putFile(output);
			}

			/////
			// 設定値取得(internal.syslog)
			////
			boolean isSyslog = HinemosPropertyUtil.getHinemosPropertyBool("internal.syslog", false);
			int syslogLevel = getPriority(HinemosPropertyUtil.getHinemosPropertyStr("internal.syslog.priority", PRIORITY_INFO));
			if (isSyslog && isOutput(syslogLevel, priority)){
				putSyslog(output);
			}

			/////
			// 設定値取得(internal.mail)
			////
			boolean isMail = HinemosPropertyUtil.getHinemosPropertyBool("internal.mail", false);
			int mailLevel = getPriority(HinemosPropertyUtil.getHinemosPropertyStr(
					"internal.mail.priority", PRIORITY_INFO));
			if (isMail && isOutput(mailLevel, priority)) {
				putMail(output);
			}

			/////
			// 設定値取得(internal.command)
			////
			boolean isCommand = HinemosPropertyUtil.getHinemosPropertyBool("internal.command", true);
			int commandLevel = getPriority(HinemosPropertyUtil.getHinemosPropertyStr("internal.command.priority", PRIORITY_INFO));

			if (isCommand && isOutput(commandLevel, priority)) {
				putCommand(output);
			}
		}finally{
			// INTERNALモードから出る
			m_isInternalMode.put(Thread.currentThread().getId(), false);
			log.debug("AplLogger INTERNAL mode end");
		}
	}

	private void putFile(OutputBasicInfo notifyInfo) {
		/**	ログファイル出力用フォーマット「日付  プラグインID,アプリケーション,監視項目ID,メッセージID,ファシリティID,メッセージ,詳細メッセージ」 */
		MessageFormat logfmt = new MessageFormat("{0,date,yyyy/MM/dd HH:mm:ss}  {1},{2},{3},{4},{5},{6}");
		//メッセージを編集
		Object[] args ={notifyInfo.getGenerationDate(),m_pluginID, m_aplName,
				notifyInfo.getMonitorId(), notifyInfo.getMessageId(), notifyInfo.getPriority(),
				notifyInfo.getMessage(), notifyInfo.getMessageOrg()};
		String logmsg = logfmt.format(args);
		//ファイル出力
		log.debug("putFile() logmsg = " + logmsg);
		FILE_LOGGER.info(logmsg);
	}

	private void putSyslog(OutputBasicInfo notifyInfo) {
		/**	syslog出力用フォーマット
		 * 「日付  プラグインID,アプリケーション,監視項目ID,メッセージID,ファシリティID,メッセージ,詳細メッセージ」 */
		MessageFormat syslogfmt = new MessageFormat("hinemos: {0},{1},{2},{3},{4},{5}");

		// メッセージを編集
		String priorityStr = PriorityConstant.typeToString(notifyInfo.getPriority());
		Object[] args ={m_pluginID, m_aplName, notifyInfo.getMonitorId(), notifyInfo.getMessageId(),
				priorityStr, notifyInfo.getMessage(), notifyInfo.getMessageOrg()};
		String logmsg = syslogfmt.format(args);

		// 送信時刻をセット
		SimpleDateFormat sdf = new SimpleDateFormat(SendSyslog.HEADER_DATE_FORMAT, Locale.US);
		String timeStamp = sdf.format(new Date());

		/////
		// 設定値取得(internal.syslog)
		////
		String hosts = HinemosPropertyUtil.getHinemosPropertyStr("internal.syslog.host", "192.168.1.1,192.168.1.2");
		String[] syslogHostList = hosts.split(",");
		int syslogPort = HinemosPropertyUtil.getHinemosPropertyNum("internal.syslog.port", 514);
		String syslogFacility = HinemosPropertyUtil.getHinemosPropertyStr("internal.syslog.facility", "daemon");
		String syslogSeverity = HinemosPropertyUtil.getHinemosPropertyStr("internal.syslog.severity", "alert");

		if(syslogHostList == null){
			return;
		}
		for (String syslogHost : syslogHostList) {
			log.debug("putSyslog() syslogHost = " + syslogHost + ", syslogPort = " + syslogPort +
					", syslogFacility = " + syslogFacility + ", syslogSeverity = " + syslogSeverity +
					", logmsg = " + logmsg + ", timeStamp = " + timeStamp);

			try {
				new NotifyControllerBean().sendAfterConvertHostname(syslogHost, syslogPort, syslogFacility,
						syslogSeverity, INTERNAL_SCOPE, logmsg, timeStamp);
			} catch (InvalidRole e) {
				log.warn("fail putSyslog monitorId=" + notifyInfo.getMonitorId() + ", message=" + notifyInfo.getMessage());
			} catch (HinemosUnknown e) {
				log.warn("fail putSyslog monitorId=" + notifyInfo.getMonitorId() + ", message=" + notifyInfo.getMessage());
			}
		}
	}

	private boolean putEvent(OutputBasicInfo notifyInfo) {
		JpaTransactionManager jtm = null;
		
		try {
			// rollbackするとイベントが出力されなくなるため、rollback用のコールバックメソッドを登録する 
			jtm = new JpaTransactionManager(); 
			jtm.begin(); 
			
			jtm.addCallback(new AplLoggerPutEventAfterRollbackCallback(notifyInfo)); 
			
			jtm.commit(); 
			
			new NotifyControllerBean().insertEventLog(notifyInfo, EventConfirmConstant.TYPE_UNCONFIRMED);
			return true;
		} catch (HinemosUnknown e) {
			log.warn("fail putEvent monitorId=" + notifyInfo.getMonitorId() + ", message=" + notifyInfo.getMessage());
			return false;
		} catch (InvalidRole e) {
			log.warn("fail putEvent monitorId=" + notifyInfo.getMonitorId() + ", message=" + notifyInfo.getMessage());
			return false;
		} finally {
			jtm.close();
		}
	}

	private void putMail(OutputBasicInfo notifyInfo) {
		// メール通知（デフォルトテンプレート）
		try {
			String addr = HinemosPropertyUtil.getHinemosPropertyStr("internal.mail.address", "user1@host.domain,user2@host.domain");
			String[] mailAddress = addr.split(",");
			new NotifyControllerBean().sendMail(mailAddress, notifyInfo);
		} catch (Exception e) {
			log.warn("fail putMail monitorId=" + notifyInfo.getMonitorId() + ", message=" + notifyInfo.getMessage());
		}
	}

	private void putCommand(OutputBasicInfo notifyInfo) {
		// コマンド通知
		try {
			String commandUser = HinemosPropertyUtil.getHinemosPropertyStr("internal.command.user", "root");
			String commandLine = HinemosPropertyUtil.getHinemosPropertyStr(
					"internal.command.commandline",
					"echo #[GENERATION_DATE] #[MESSAGE] >> /tmp/test.txt");
			int commandTimeout = 0;
			try {
				commandTimeout = HinemosPropertyUtil.getHinemosPropertyNum("internal.command.timeout", 15000);
			} catch (Exception e) {}

			Map<String, String> param = NotifyUtil.createParameter(notifyInfo, null);
			StringBinder binder = new StringBinder(param);
			String command = binder.bindParam(commandLine);

			log.info("excuting command. (effectiveUser = " + commandUser + ", command = " + command + ", mode = " + PlatformType.UNIX + ", timeout = " + commandTimeout + ")");
			String[] cmd = CommandCreator.createCommand(commandUser, command, PlatformType.UNIX);
			CommandExecutor cmdExec = new CommandExecutor(cmd, commandTimeout);
			cmdExec.execute();
			CommandResult ret = cmdExec.getResult();

			if (ret != null) {
				log.info("executed command. (exitCode = " + ret.exitCode + ", stdout = " + ret.stdout + ", stderr = " + ret.stderr + ")");
			}

		} catch (Exception e) {
			log.warn("fail putCommand monitorId=" + notifyInfo.getMonitorId() + ", message=" + notifyInfo.getMessage());
			return;
		}
	}


	/**
	 *
	 * 文字列から、Priority区分を取得します。<BR>
	 *
	 * @param priority
	 * @since
	 */
	private int getPriority(String priority) {
		int ret = PriorityConstant.TYPE_UNKNOWN;
		if(priority.equals(PRIORITY_CRITICAL)){
			ret = PriorityConstant.TYPE_CRITICAL;
		}else if(priority.equals(PRIORITY_WARNING)){
			ret = PriorityConstant.TYPE_WARNING;
		}else if(priority.equals(PRIORITY_INFO)){
			ret = PriorityConstant.TYPE_INFO;
		}
		return ret;
	}

	/**
	 * 送信を行うかのPriority毎の判定を行う
	 */
	private static boolean isOutput(int level, int priority){
		if (priority == PriorityConstant.TYPE_CRITICAL) {
			if (level == PriorityConstant.TYPE_CRITICAL ||
					level == PriorityConstant.TYPE_UNKNOWN ||
					level == PriorityConstant.TYPE_WARNING ||
					level == PriorityConstant.TYPE_INFO) {
				return true;
			} else {
				return false;
			}
		}
		if (priority == PriorityConstant.TYPE_UNKNOWN) {
			if (level == PriorityConstant.TYPE_UNKNOWN ||
					level == PriorityConstant.TYPE_WARNING ||
					level == PriorityConstant.TYPE_INFO) {
				return true;
			} else {
				return false;
			}
		}
		if (priority == PriorityConstant.TYPE_WARNING) {
			if (level == PriorityConstant.TYPE_WARNING ||
					level == PriorityConstant.TYPE_INFO) {
				return true;
			} else {
				return false;
			}
		}
		if (priority == PriorityConstant.TYPE_INFO) {
			if (level == PriorityConstant.TYPE_INFO) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}


	/**
	 * メッセージを置換します<BR>
	 *
	 * @param key リソース名
	 * @param args メッセージの引数
	 * @return the メッセージ
	 */
	private static String getString(String key, Object[] args) {
		MessageFormat messageFormat = new MessageFormat(getString(key));
		return messageFormat.format(args);
	}

	/**
	 * メッセージを置換します<BR>
	 *
	 * @param key リソース名
	 * @return the メッセージ
	 */
	private static String getString(String key) {
		try {
			return m_bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}
}
