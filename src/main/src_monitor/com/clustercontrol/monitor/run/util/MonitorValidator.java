package com.clustercontrol.monitor.run.util;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.ProcessConstant;
import com.clustercontrol.bean.RunIntervalConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.bean.YesNoConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.custom.bean.CustomCheckInfo;
import com.clustercontrol.custom.bean.CustomConstant;
import com.clustercontrol.fault.CollectorNotFound;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.http.bean.HttpCheckInfo;
import com.clustercontrol.http.bean.HttpScenarioCheckInfo;
import com.clustercontrol.http.bean.Page;
import com.clustercontrol.http.bean.Variable;
import com.clustercontrol.http.util.GetHttpResponse;
import com.clustercontrol.jmx.bean.JmxCheckInfo;
import com.clustercontrol.logfile.bean.LogfileCheckInfo;
import com.clustercontrol.monitor.run.bean.MonitorInfo;
import com.clustercontrol.monitor.run.bean.MonitorStringValueInfo;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.notify.bean.NotifyRelationInfo;
import com.clustercontrol.performance.monitor.bean.PerfCheckInfo;
import com.clustercontrol.ping.bean.PingCheckInfo;
import com.clustercontrol.port.bean.PortCheckInfo;
import com.clustercontrol.process.bean.ProcessCheckInfo;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.snmp.bean.SnmpCheckInfo;
import com.clustercontrol.snmptrap.bean.MonitorTrapConstant;
import com.clustercontrol.snmptrap.bean.TrapCheckInfo;
import com.clustercontrol.snmptrap.bean.TrapValueInfo;
import com.clustercontrol.snmptrap.bean.VarBindPattern;
import com.clustercontrol.snmptrap.model.MonitorTrapValueInfoEntityPK;
import com.clustercontrol.sql.bean.SqlCheckInfo;
import com.clustercontrol.util.Messages;
import com.clustercontrol.winevent.bean.WinEventCheckInfo;
import com.clustercontrol.winservice.bean.WinServiceCheckInfo;

public class MonitorValidator {

	private static Log m_log = LogFactory.getLog( MonitorValidator.class );

	/**
	 * 監視設定(MonitorInfo)の妥当性チェック
	 * @param monitorInfo
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateMonitorInfo(MonitorInfo monitorInfo) throws InvalidSetting, InvalidRole {

		// 監視共通
		validateMonitorCommonSettings(monitorInfo);

		// 監視種別のチェック
		int monitorType = monitorInfo.getMonitorType();
		if(monitorType == MonitorTypeConstant.TYPE_TRUTH){
			validateMonitorTruthSettings(monitorInfo);
		}else if(monitorType == MonitorTypeConstant.TYPE_NUMERIC){
			validateMonitorNumericSettings(monitorInfo);
		}else if(monitorType == MonitorTypeConstant.TYPE_STRING){
			validateMonitorStringSettings(monitorInfo);
		}else if(monitorType == MonitorTypeConstant.TYPE_TRAP){
			// validateSnmptrap() で纏めて validate。
		}else if(monitorType == MonitorTypeConstant.TYPE_SCENARIO){
			// validateHttpScenario() で纏めて validate。
		}else if(monitorType == MonitorTypeConstant.TYPE_SCENARIO){
			// validateHttpScenario() で纏めて validate。
		}else{
			InvalidSetting e = new InvalidSetting("validateMonitorInfo() Invalid Monitor Type. monitorType = "
					+ monitorInfo.getMonitorType());
			m_log.info("validateMonitorInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// 監視種別ID(プラグインID)のチェック
		String monitorTypeId = monitorInfo.getMonitorTypeId();
		if(HinemosModuleConstant.MONITOR_AGENT.equals(monitorTypeId)){
			validateHinemosAgent(monitorInfo);
		}else if (HinemosModuleConstant.MONITOR_HTTP_N.equals(monitorTypeId)) {
			validateHttpNumeric(monitorInfo);
		}else if (HinemosModuleConstant.MONITOR_HTTP_S.equals(monitorTypeId)) {
			validateHttp(monitorInfo);
		}else if (HinemosModuleConstant.MONITOR_HTTP_SCENARIO.equals(monitorTypeId)) {
			validateHttpScenario(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_PERFORMANCE.equals(monitorTypeId)){
			validatePerformance(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_PING.equals(monitorTypeId)){
			validatePing(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_PORT.equals(monitorTypeId)){
			validatePort(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_PROCESS.equals(monitorTypeId)){
			validateProcess(monitorInfo);
		}else if (HinemosModuleConstant.MONITOR_SNMP_N.equals(monitorTypeId)){
			validateSnmpNumeric(monitorInfo);
		}else if (HinemosModuleConstant.MONITOR_SNMP_S.equals(monitorTypeId)) {
			validateSnmp(monitorInfo);
		}else if (HinemosModuleConstant.MONITOR_SQL_N.equals(monitorTypeId)) {
			validateSqlNumeric(monitorInfo);
		}else if (HinemosModuleConstant.MONITOR_SQL_S.equals(monitorTypeId)) {
			validateSql(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorTypeId)){
			validateSystemlog(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorTypeId)){
			validateLogfile(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_CUSTOM.equals(monitorTypeId)){
			validateCustom(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_SNMPTRAP.equals(monitorTypeId)){
			validateSnmptrap(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_WINSERVICE.equals(monitorTypeId)){
			validateWinService(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_WINEVENT.equals(monitorTypeId)){
			validateWinEvent(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_JMX.equals(monitorTypeId)){
			validateJMX(monitorInfo);
		}else {

			// クラウド管理オプションの専用肝機能追加のため、本例外処理をコメントアプトする

			/*
			InvalidSetting e = new InvalidSetting("Invalid monitorTypeId. monitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validateMonitorInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
			*/
		}

	}


	/**
	 * 監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	private static void validateMonitorCommonSettings(MonitorInfo monitorInfo) throws InvalidSetting, InvalidRole {

		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateMonitorCommonSettings() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateMonitorCommonSettings() monitorId = " + monitorInfo.getMonitorId());

		//
		// 共通項目を対象とする。ただし、監視間隔については各監視機能で実装する(トラップ系の監視があるため)
		// 数値監視、文字列監視、真偽値監視は各継承したクラスで実装する
		//

		// monitorId
		if (monitorInfo.getMonitorId() == null || monitorInfo.getMonitorId().length() == 0) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.1"));
			m_log.info("validateMonitorCommonSettings() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateId(Messages.getString("monitor.id"), monitorInfo.getMonitorId(), 64);

		// monitorTypeId
		// monitorType

		// クラウド管理オプションの専用肝機能追加のため、本例外処理をコメントアプトする
		/*
		boolean flag = true;
		for (ArrayList<Object> a : MonitorTypeMstConstant.getListAll()) {
			if (a.get(0).equals(monitorInfo.getMonitorTypeId()) &&
					a.get(1).equals(monitorInfo.getMonitorType())) {
				flag = false;
				break;
			}
		}
		if (flag) {
			InvalidSetting e = new InvalidSetting("Invalid MonitorType. monitorTyeId(pluginId) = " + monitorInfo.getMonitorTypeId()
					+ ", monitorType = " + monitorInfo.getMonitorType());
			m_log.info("validateMonitorCommonSettings() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		*/

		// description : not implemented
		CommonValidator.validateString(Messages.getString("description"),
				monitorInfo.getDescription(), false, 0, 256);

		// ownerRoleId
		CommonValidator.validateOwnerRoleId(monitorInfo.getOwnerRoleId(), true,
				monitorInfo.getMonitorId(), HinemosModuleConstant.MONITOR);

		// facilityId
		if(monitorInfo.getFacilityId() == null || "".equals(monitorInfo.getFacilityId())){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.hinemos.3"));
			m_log.info("validateMonitorCommonSettings() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}else{
			try {
				FacilityTreeCache.validateFacilityId(monitorInfo.getFacilityId(), monitorInfo.getOwnerRoleId(), false);
			} catch (FacilityNotFound e) {
				throw new InvalidSetting(e.getMessage(), e);
			}
		}

		// runInterval : not implemented
		if(monitorInfo.getRunInterval() != RunIntervalConstant.TYPE_MIN_01
				&& monitorInfo.getRunInterval() != RunIntervalConstant.TYPE_MIN_05
				&& monitorInfo.getRunInterval() != RunIntervalConstant.TYPE_MIN_10
				&& monitorInfo.getRunInterval() != RunIntervalConstant.TYPE_MIN_30
				&& monitorInfo.getRunInterval() != RunIntervalConstant.TYPE_MIN_60){

			// if polling type monitoring
			if(!HinemosModuleConstant.MONITOR_SNMPTRAP.equals(monitorInfo.getMonitorTypeId()) &&
					!HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorInfo.getMonitorTypeId()) &&
					!HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorInfo.getMonitorTypeId()) &&
					!HinemosModuleConstant.MONITOR_WINEVENT.equals(monitorInfo.getMonitorTypeId())){
				InvalidSetting e = new InvalidSetting("RunInterval is not 1 min / 5 min / 10 min / 30 min / 60 min.");
				m_log.info("validateMonitorCommonSettings() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		// delayTime : not implemented
		// triggerType : not implemented

		// calendarId
		CommonValidator.validateCalenderId(monitorInfo.getCalendarId(), false, monitorInfo.getOwnerRoleId());

		// failurePriority : not implemented

		// application
		if(monitorInfo.getMonitorFlg() == YesNoConstant.TYPE_YES){
			CommonValidator.validateString(Messages.getString("application"),
					monitorInfo.getApplication(), true, 1, 64);
		}

		// notifyGroupId : not implemented

		// notifyId
		if(monitorInfo.getNotifyId() != null){
			for(NotifyRelationInfo notifyInfo : monitorInfo.getNotifyId()){
				CommonValidator.validateNotifyId(notifyInfo.getNotifyId(), true, monitorInfo.getOwnerRoleId());
			}
		}

		// monitorFlg : not implemented
		// collectorFlg
		if(monitorInfo.getCollectorFlg() == YesNoConstant.TYPE_YES){
			if(monitorInfo.getMonitorType() != MonitorTypeConstant.TYPE_NUMERIC && monitorInfo.getMonitorType() != MonitorTypeConstant.TYPE_SCENARIO){
				InvalidSetting e = new InvalidSetting("CollectorFlg is true. but this monitorType is not numeric.");
				m_log.info("validateMonitorCommonSettings() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

	}

	/**
	 * 文字列用監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateMonitorStringSettings(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateMonitorStringSettings() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateMonitorStringSettings() monitorId = " + monitorInfo.getMonitorId());

		ArrayList<MonitorStringValueInfo> stringValueInfoList = monitorInfo.getStringValueInfo();
		if(stringValueInfoList == null || stringValueInfoList.size() == 0){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.27"));
			m_log.info("validateMonitorStringSettings() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}else{
			int orderNo = 0;
			for(MonitorStringValueInfo info : stringValueInfoList){

				// monitorId : not implemented

				// orderNo
				++orderNo;
				// description : not implemented
				String description = info.getDescription();
				if (description != null) {
					CommonValidator.validateString(Messages.getString("description"),
							description, true, 0, 256);
				}

				// processType : not implemented

				// pattern
				if(info.getPattern() == null ){
					InvalidSetting e = new InvalidSetting("Pattern is not defined. monitorId = "
							+ monitorInfo.getMonitorId() + ", orderNo = " + orderNo);
					m_log.info("validateMonitorStringSettings() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				} else if ("".equals(info.getPattern()) && info.getProcessType() == ProcessConstant.TYPE_YES){
					InvalidSetting e = new InvalidSetting("Pattern is empty string. monitorId = "
							+ monitorInfo.getMonitorId() + ", orderNo = " + orderNo);
					m_log.info("validateMonitorStringSettings() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				} else {
					CommonValidator.validateString(Messages.getString("pattern.matching.expression"),
							info.getPattern(), true, 1, 1024);
				}
				try{
					Pattern.compile(info.getPattern());
				}
				catch(PatternSyntaxException e){
					InvalidSetting e1 = new InvalidSetting("Pattern is not regular expression. monitorId = "
							+ monitorInfo.getMonitorId() + ", orderNo = " + orderNo, e);
					m_log.info("validateMonitorStringSettings() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				}
				// priority : not implemented

				// message
				if(info.getProcessType() == ProcessConstant.TYPE_YES){
					CommonValidator.validateString(Messages.getString("message"), info.getMessage(), true, 1, 256);
				} else {
					CommonValidator.validateString(Messages.getString("message"), info.getMessage(), false, 0, 256);
				}

				// caseSensitivityFlg : not implemented

				// validFlg : not implemented

			}
		}
	}

	/**
	 * 数値用監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateMonitorNumericSettings(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateMonitorNumericSettings() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		boolean nullCheck = false;
		if(monitorInfo.getCollectorFlg() == YesNoConstant.TYPE_YES){
			nullCheck = true;
		}
		
		// itemName
		CommonValidator.validateString(Messages.getString("collection.display.name"), monitorInfo.getItemName(), nullCheck, 1, 256);

		// measure
		CommonValidator.validateString(Messages.getString("collection.unit"), monitorInfo.getMeasure(), nullCheck, 1, 64);
	}

	private static void validateNumeric(MonitorInfo monitorInfo, int timeout)
			throws InvalidSetting {

		if (monitorInfo.getMonitorFlg() == ValidConstant.TYPE_INVALID) {
			return;
		}

		Double infoLower = (monitorInfo.getNumericValueInfo().get(0)).getThresholdLowerLimit();
		Double infoUpper = (monitorInfo.getNumericValueInfo().get(0)).getThresholdUpperLimit();
		Double warnLower = (monitorInfo.getNumericValueInfo().get(1)).getThresholdLowerLimit();
		Double warnUpper = (monitorInfo.getNumericValueInfo().get(1)).getThresholdUpperLimit();
		int runInterval = monitorInfo.getRunInterval();

		if (infoLower == null || infoUpper == null) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.7"));
			m_log.info("validateNumeric() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		if (warnLower == null || warnUpper == null) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.8"));
			m_log.info("validateNumeric() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateDouble(Messages.getString("info"), infoLower, DataRangeConstant.DOUBLE_LOW, DataRangeConstant.DOUBLE_HIGH);
		CommonValidator.validateDouble(Messages.getString("info"), infoUpper, DataRangeConstant.DOUBLE_LOW, DataRangeConstant.DOUBLE_HIGH);
		CommonValidator.validateDouble(Messages.getString("warning"), warnLower, DataRangeConstant.DOUBLE_LOW, DataRangeConstant.DOUBLE_HIGH);
		CommonValidator.validateDouble(Messages.getString("warning"), warnUpper, DataRangeConstant.DOUBLE_LOW, DataRangeConstant.DOUBLE_HIGH);

		// ping監視のみ通常のinfo/warnの閾値ではない
		if(!HinemosModuleConstant.MONITOR_PING.equals(monitorInfo.getMonitorTypeId())){
			if (infoLower > infoUpper) {
				InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.7"));
				m_log.info("validateNumeric() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			if (warnLower > warnUpper) {
				InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.8"));
				m_log.info("validateNumeric() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		if (timeout < 0) {
			return;
		}
		// 間隔よりタイムアウトが大きい場合
		if (runInterval*1000 < timeout) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.43"));
			m_log.info("validateNumeric() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		if(HinemosModuleConstant.MONITOR_PING.equals(monitorInfo.getMonitorTypeId())){
			if (timeout < infoLower) {
				InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.50"));
				m_log.info("validateNumeric() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			if (timeout < warnLower) {
				InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.51"));
				m_log.info("validateNumeric() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} else {
			// タイムアウトより通知の上限が大きい場合
			if (timeout < infoUpper) {
				InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.50"));
				m_log.info("validateNumeric() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// タイムアウトより警告の上限が大きい場合
			if (timeout < warnUpper) {
				InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.51"));
				m_log.info("validateNumeric() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
	}

	/**
	 * 真偽値用監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateMonitorTruthSettings(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateMonitorTruthSettings() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateMonitorTruthSettings() monitorId = " + monitorInfo.getMonitorId());
		m_log.debug("validateMonitorTruthSettings() is not implemented. ");
	}

	/**
	 * Hinemosエージェント監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateHinemosAgent(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateHinemosAgent() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateHinemosAgent() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo : not impletemted( check info is not exists)
		m_log.debug("validateHinemosAgent() is not needed. ");

		// monitorType
		if(!HinemosModuleConstant.MONITOR_AGENT.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Agent Monitor Setting. But MonitorTypeId = "
					+ monitorInfo.getMonitorTypeId());
			m_log.info("validateHinemosAgent() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * HTTP監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateHttpNumeric(MonitorInfo monitorInfo) throws InvalidSetting {
		validateHttp(monitorInfo);

		HttpCheckInfo checkInfo = monitorInfo.getHttpCheckInfo();

		// input validate
		validateNumeric(monitorInfo, checkInfo.getTimeout());

	}

	/**
	 * HTTP監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateHttp(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateHttp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateHttp() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		HttpCheckInfo checkInfo = monitorInfo.getHttpCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("HTTP Monitor Setting is not defined. monitorId = "
					+ monitorInfo.getMonitorId());
			m_log.info("validateHttp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if (!HinemosModuleConstant.MONITOR_HTTP_N.equals(monitorInfo.getMonitorTypeId())
				&& !HinemosModuleConstant.MONITOR_HTTP_S.equals(monitorInfo.getMonitorTypeId())) {
			InvalidSetting e = new InvalidSetting("This is HTTP Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validateHttp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// requestUrl
		if(checkInfo.getRequestUrl() == null || "".equals(checkInfo.getRequestUrl())){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.http.1"));
			m_log.info("validateHttp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}else{
			String url = checkInfo.getRequestUrl();
			// format check
			if (url.length() > 0 && (!url.startsWith("http://") && !url.startsWith("https://"))) {
				InvalidSetting e = new InvalidSetting(Messages.getString("message.http.5"));
				m_log.info("validateHttp() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			else if((url.startsWith("http://") && url.length() == 7) || (url.startsWith("https://") && url.length() == 8)){
				InvalidSetting e = new InvalidSetting(Messages.getString("message.http.5"));
				m_log.info("validateHttp() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		CommonValidator.validateString(Messages.getString("request.url"),
				checkInfo.getRequestUrl(), true, 8, 2083);

		// urlReplace : not implemented

		// timeout : not implemented
		if(checkInfo.getTimeout() == null) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.custom.msg.timeout.undef"));
			m_log.info("validateHttp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(Messages.getString("time.out"),
				checkInfo.getTimeout(), 1, 60 * 60 * 1000);

		// proxySet : not implemented

		// proxyHost : not implemented

		// proxyPort : not implemented
	}




	private static void validateHttpScenario(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateHttpScenario() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateHttp() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		HttpScenarioCheckInfo checkInfo = monitorInfo.getHttpScenarioCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("HTTP Scenario Monitor Setting is not defined. monitorId = "
					+ monitorInfo.getMonitorId());
			m_log.info("validateHttpScenario() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if (!HinemosModuleConstant.MONITOR_HTTP_SCENARIO.equals(monitorInfo.getMonitorTypeId())) {
			InvalidSetting e = new InvalidSetting("This is HTTP Scenario Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validateHttpScenario() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if (checkInfo.getAuthType() != null) {
			boolean match = false;
			for (GetHttpResponse.AuthType at: GetHttpResponse.AuthType.values()) {
				if (at.name().equals(checkInfo.getAuthType())) {
					match = true;
					break;
				}
			}
			if (!match) {
				InvalidSetting e = new InvalidSetting("This is HTTP Scenario Monitor Setting. But AuthType = " + checkInfo.getAuthType());
				m_log.info("validateHttpScenario() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		if (checkInfo.getAuthUser() != null) {
			CommonValidator.validateString(Messages.getString("monitor.http.scenario.authuser"), checkInfo.getAuthUser(), false, 0, 64);
		}
		if (checkInfo.getAuthPassword() != null) {
			CommonValidator.validateString(Messages.getString("monitor.http.scenario.authpassword"), checkInfo.getAuthPassword(), false, 0, 64);
		}
		if (checkInfo.getProxyFlg()) {
			if ("".equals(checkInfo.getProxyUrl())){
				InvalidSetting e = new InvalidSetting(Messages.getString("message.http.1"));
				m_log.info("validateHttpScenario() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			else {
				String url = checkInfo.getProxyUrl();
				// format check
				if (url.length() > 0 && (!url.startsWith("http://") && !url.startsWith("https://"))) {
					InvalidSetting e = new InvalidSetting(Messages.getString("monitor.http.scenario.proxyurl"));
					m_log.info("validateHttpScenario() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				else if((url.startsWith("http://") && url.length() == 7) || (url.startsWith("https://") && url.length() == 8)){
					InvalidSetting e = new InvalidSetting(Messages.getString("monitor.http.scenario.proxyurl"));
					m_log.info("validateHttpScenario() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			CommonValidator.validateString(Messages.getString("monitor.http.scenario.proxyurl"), checkInfo.getProxyUrl(), false, 0, 1024);
			if (checkInfo.getProxyPort() != null) {
				CommonValidator.validateInt(Messages.getString("monitor.http.scenario.proxyport"), checkInfo.getProxyPort(), 0, 65535);
			} else {
				InvalidSetting e = new InvalidSetting(Messages.getString("monitor.http.scenario.proxyport"));
				m_log.info("validateHttpScenario() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		if (checkInfo.getProxyUser() != null) {
			CommonValidator.validateString(Messages.getString("monitor.http.scenario.proxyuser"), checkInfo.getProxyUser(), false, 0, 64);
		}
		if (checkInfo.getProxyPassword() != null) {
			CommonValidator.validateString(Messages.getString("monitor.http.scenario.proxypassword"), checkInfo.getProxyPassword(), false, 0, 64);
		}
		
		// checkInfo.getMonitoringPerPageFlg();
		if (checkInfo.getUserAgent() != null) {
			CommonValidator.validateString(Messages.getString("monitor.http.scenario.userAgent"), checkInfo.getUserAgent(), false, 0, 1024);
		}
		// checkInfo.getCancelProxyCacheFlg();
		if (checkInfo.getConnectTimeout() == null) {
			InvalidSetting e = new InvalidSetting(Messages.getString("monitor.http.scenario.connecttimeout"));
			m_log.info("validateHttpScenario() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		if (checkInfo.getConnectTimeout() != null) {
			CommonValidator.validateInt(Messages.getString("monitor.http.scenario.connecttimeout"), checkInfo.getConnectTimeout(), 0, 60 * 60 * 1000);
		}
		if (checkInfo.getRequestTimeout() != null) {
			CommonValidator.validateInt(Messages.getString("monitor.http.scenario.requesttimeout"), checkInfo.getRequestTimeout(), 0, 60 * 60 * 1000);
		}

		if (checkInfo.getPages().isEmpty()) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.http.scenario.must.specify.more.than.one.pattern"));
			m_log.info("validateHttpScenario() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		else {
			for (Page p: checkInfo.getPages()) {
				if (p.getUrl() == null && "".equals(p.getUrl())){
					InvalidSetting e = new InvalidSetting(Messages.getString("monitor.http.scenario.page.url"));
					m_log.info("validateHttpScenario() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				else {
					String url = p.getUrl();
					// format check
					if (url.length() > 0 && (!url.startsWith("http://") && !url.startsWith("https://"))) {
						InvalidSetting e = new InvalidSetting(Messages.getString("monitor.http.scenario.page.url"));
						m_log.info("validateHttpScenario() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
					else if((url.startsWith("http://") && url.length() == 7) || (url.startsWith("https://") && url.length() == 8)){
						InvalidSetting e = new InvalidSetting(Messages.getString("monitor.http.scenario.page.url"));
						m_log.info("validateHttpScenario() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
				}
				CommonValidator.validateString(Messages.getString("monitor.http.scenario.page.url"), p.getUrl(), false, 0, 1024);
				CommonValidator.validateString(Messages.getString("monitor.http.scenario.page.description"), p.getDescription(), false, 0, 1024);
				if (p.getStatusCode() != null) {
					if (!Pattern.matches("^(\\s*\\d+,)*\\s*\\d+\\s*$", p.getStatusCode())) {
						InvalidSetting e = new InvalidSetting(Messages.getString("monitor.http.scenario.page.statuscode"));
						m_log.info("validateHttpScenario() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
				}
				CommonValidator.validateString(Messages.getString("monitor.http.scenario.pape.post"), p.getPost(), false, 0, 1024);

				for (com.clustercontrol.http.bean.Pattern pt: p.getPatterns()) {
					CommonValidator.validateString(Messages.getString("monitor.http.scenario.pattern.pattern"), pt.getPattern(), false, 0, 1024);
					CommonValidator.validateString(Messages.getString("monitor.http.scenario.pattern.description"), pt.getDescription(), false, 0, 256);
					// processType : not implemented pt.getProcessType();
					// pt.getCaseSensitivityFlg();
					// pt.getValidFlg();
				}

				for (Variable v: p.getVariables()) {
					CommonValidator.validateString(Messages.getString("monitor.http.scenario.variable.name"), v.getName(), true, 0, 1024);
					CommonValidator.validateString(Messages.getString("monitor.http.scenario.variable.value"), v.getValue(), true, 0, 1024);
					// v.getMatchingWithResponseFlg();
				}
			}
		}
	}





	/**
	 * ログファイル監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateLogfile(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateLogfile() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateLogfile() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		LogfileCheckInfo checkInfo = monitorInfo.getLogfileCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Logfile Monitor Setting is not defined. monitorId = " + monitorInfo.getMonitorId());
			m_log.info("validateLogfile() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Logfile Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validateLogfile() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		//Directory
		if(checkInfo.getDirectory() == null || "".equals(checkInfo.getDirectory())){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.logfile.2"));
			m_log.info("validateLogfile() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		//FileName
		if(checkInfo.getFileName() == null || "".equals(checkInfo.getFileName())){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.logfile.3"));
			m_log.info("validateLogfile() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		//FileEncoding
		if(checkInfo.getFileEncoding() == null || "".equals(checkInfo.getFileEncoding())){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.logfile.4"));
			m_log.info("validateLogfile() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		//FileReturnCode
		if(checkInfo.getFileReturnCode() == null || "".equals(checkInfo.getFileReturnCode())){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.logfile.5"));
			m_log.info("validateLogfile() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		CommonValidator.validateString(Messages.getString("directory"), checkInfo.getDirectory(), true, 1, 1024);
		CommonValidator.validateString(Messages.getString("file.name"), checkInfo.getFileName(), true, 1, 1024);
		CommonValidator.validateString(Messages.getString("file.encoding"), checkInfo.getFileEncoding(), true, 1, 32);
		CommonValidator.validateString(Messages.getString("file.returncode"), checkInfo.getFileEncoding(), true, 1, 16);
	}

	/**
	 * リソース監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validatePerformance(MonitorInfo monitorInfo) throws InvalidSetting {

		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validatePerformance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validatePerformance() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		PerfCheckInfo checkInfo = monitorInfo.getPerfCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Performance Monitor Setting is not defined. monitorId = " + monitorInfo.getMonitorId());
			m_log.info("validatePerformance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_PERFORMANCE.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Performance Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validatePerformance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// itemCode
		if(checkInfo.getItemCode() == null || "".equals(checkInfo.getItemCode())){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.57"));
			m_log.info("validatePerformance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}else{
			try {
				com.clustercontrol.performance.monitor.util.QueryUtil.getCollectorItemCodeMstPK(checkInfo.getItemCode());
			} catch (CollectorNotFound e) {
				throw new InvalidSetting(e.getMessage(), e);
			}
		}

		// deviceDisplayName : not implemented
		if(checkInfo.getDeviceDisplayName() == null){
			InvalidSetting e = new InvalidSetting("Target Display Name is not defined.");
			m_log.info("validatePerformance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		// breakdownFlg : not implemented

		validateNumeric(monitorInfo, -1);
	}

	/**
	 * ping監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validatePing(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validatePing() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validatePing() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		PingCheckInfo checkInfo = monitorInfo.getPingCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Ping Monitor Setting is not defined. monitorId = " + monitorInfo.getMonitorId());
			m_log.info("validatePing() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_PING.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Ping Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validatePing() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// runCount : implement
		if(checkInfo.getRunCount() == null) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.custom.msg.runcount.undef"));
			m_log.info("validatePing() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(Messages.getString("run.count"),
				checkInfo.getRunCount(), 1, 9);

		// runInterval : implement
		if(checkInfo.getRunInterval() == null) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.custom.msg.runinterval.undef"));
			m_log.info("validatePing() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(Messages.getString("run.interval"),
				checkInfo.getRunInterval(), 0, 5  * 1000);

		// timeout
		if(checkInfo.getTimeout() == null) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.custom.msg.timeout.undef"));
			m_log.info("validatePing() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(Messages.getString("time.out"),
				checkInfo.getTimeout(), 1, 60 * 60 * 1000);

		// input validate
		int runInterval = monitorInfo.getRunInterval();
		int runCount = checkInfo.getRunCount();
		int interval = checkInfo.getRunInterval();
		int timeout = checkInfo.getTimeout();

		// 間隔よりチェック設定の「回数×タイムアウト＋間隔」が大きい場合
		double total = runCount * ((double)timeout / 1000) + ((double)interval / 1000);
		if (runInterval <= (int)total) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.52"));
			m_log.info("validatePing() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		validateNumeric(monitorInfo, timeout);

		// パケット紛失(%)は0-100の間
		CommonValidator.validateDouble(Messages.getString("ping.reach"),
				monitorInfo.getNumericValueInfo().get(0).getThresholdUpperLimit(),
				0f,100f);
		CommonValidator.validateDouble(Messages.getString("ping.reach"),
				monitorInfo.getNumericValueInfo().get(1).getThresholdUpperLimit(),
				0f,100f);
	}

	/**
	 * ポート監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validatePort(MonitorInfo monitorInfo) throws InvalidSetting {

		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validatePort() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validatePort() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		PortCheckInfo checkInfo = monitorInfo.getPortCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Port Monitor Setting is not defined. monitorId = " + monitorInfo.getMonitorId());
			m_log.info("validatePort() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_PORT.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Port Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validatePort() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// portNo : not implemented
		if(checkInfo.getPortNo() == null) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.port.8"));
			m_log.info("validatePort() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(Messages.getString("port.number"), checkInfo.getPortNo(), 1, 65535);

		// runCount : implement
		if(checkInfo.getRunCount() == null) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.port.1"));
			m_log.info("validatePing() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(Messages.getString("run.count"),
				checkInfo.getRunCount(), 1, 9);

		// runInterval : not implemented
		if(checkInfo.getRunInterval() == null) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.port.2"));
			m_log.info("validatePort() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(Messages.getString("run.interval"),
				checkInfo.getRunInterval(), 0, 5  * 1000);

		// timeout
		if(checkInfo.getTimeout() == null) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.custom.msg.timeout.undef"));
			m_log.info("validatePort() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(Messages.getString("time.out"),
				checkInfo.getTimeout(), 1, 60 * 60 * 1000);

		// serviceId : not implemented
		if(checkInfo.getServiceId() == null || "".equals(checkInfo.getServiceId())){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.port.7"));
			m_log.info("validatePort() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}else{
			try {
				com.clustercontrol.port.util.QueryUtil.getMonitorProtocolMstPK(checkInfo.getServiceId());
			} catch (MonitorNotFound e) {
				throw new InvalidSetting(e.getMessage(), e);
			}
		}

		// input validate
		int runInterval = monitorInfo.getRunInterval();
		int runCount = checkInfo.getRunCount();
		int interval = checkInfo.getRunInterval();
		int timeout = checkInfo.getTimeout();

		// 間隔よりチェック設定の「回数×タイムアウト＋間隔」が大きい場合
		double total = runCount * ((double)timeout / 1000) + ((double)interval / 1000);
		if (runInterval <= (int)total) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.52"));
			m_log.info("validatePort() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		validateNumeric(monitorInfo, timeout);
	}

	/**
	 * プロセス監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateProcess(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateProcess() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateProcess() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		ProcessCheckInfo checkInfo = monitorInfo.getProcessCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Process Monitor Setting is not defined. monitorId = " + monitorInfo.getMonitorId());
			m_log.info("validateProcess() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_PROCESS.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Process Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validateProcess() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// command
		if(checkInfo.getCommand() == null || "".equals(checkInfo.getCommand())){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.process.1"));
			m_log.info("validateProcess() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(Messages.getString("command"),
				checkInfo.getCommand(), true, 1, 256);
		try{
			Pattern.compile(checkInfo.getCommand());
		}
		catch(PatternSyntaxException e){
			InvalidSetting e1 = new InvalidSetting(Messages.getString("message.process.2"), e);
			m_log.info("validateProcess() : "
					+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
			throw e1;
		}

		// param
		if (checkInfo.getParam() != null) {
			CommonValidator.validateString(Messages.getString("param"),
					checkInfo.getParam(), false, 0, 256);
			try {
				Pattern.compile(checkInfo.getParam());
			} catch(PatternSyntaxException e) {
				InvalidSetting e1 = new InvalidSetting(Messages.getString("message.process.3"));
				m_log.info("validateProcess() : "
						+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
				throw e1;
			}
		}

		validateNumeric(monitorInfo, -1);
	}

	/**
	 * SNMPトラップ用監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateSnmptrap(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateSnmptrap() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateSnmptrap() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		TrapCheckInfo checkInfo = monitorInfo.getTrapCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("SNMP Trap Monitor Setting is not defined. monitorId = " + monitorInfo.getMonitorId());
			m_log.info("validateSnmptrap() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_SNMPTRAP.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is SNMP Trap Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validateSnmptrap() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		CommonValidator.validateString(Messages.getString("community.name"),
				checkInfo.getCommunityName(), false, 0, 64);

		// communityName
		if(checkInfo.getCommunityCheck() == MonitorTrapConstant.COMMUNITY_CHECK_ON){
			if(checkInfo.getCommunityName() == null || "".equals(checkInfo.getCommunityName())){
				InvalidSetting e = new InvalidSetting(Messages.getString("message.snmptrap.1"));
				m_log.info("validateSnmptrap() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		CommonValidator.validateString(Messages.getString("charset.snmptrap.code"), checkInfo.getCharsetName(),
				false, 1, 64);

		// charsetName
		if(checkInfo.getCharsetConvert() == MonitorTrapConstant.CHARSET_CONVERT_ON){
			if(checkInfo.getCharsetName() == null || "".equals(checkInfo.getCharsetName())){
				InvalidSetting e = new InvalidSetting(Messages.getString("message.snmptrap.4"));
				m_log.info("validateSnmptrap() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		// PriorityUnspecified : not implemented

		ArrayList<MonitorTrapValueInfoEntityPK> pkList = new ArrayList<MonitorTrapValueInfoEntityPK>();
		for (TrapValueInfo v: checkInfo.getTrapValueInfos()) {
			CommonValidator.validateString(Messages.getString("monitor.snmptrap.value.mib"), v.getMib(), true, 1, 1024);
			CommonValidator.validateString(Messages.getString("trap.name"), v.getUei(), true, 1, 256);
			CommonValidator.validateString(Messages.getString("trap.oid"), v.getTrapOid(), true, 1, 1024);
			
			//.と[0-9]以外はNG
			char c = 'a';
			for (int i = 0; i < v.getTrapOid().length(); i++) {
				c = v.getTrapOid().charAt(i);
				if (c != '.' && !('0' <= c && c <= '9')) {
					InvalidSetting e = new InvalidSetting(Messages.getString("message.snmptrap.trapoid.invalid", new String[]{v.getTrapOid()}));
					m_log.info("validateSnmptrap() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			
			// GenericId
			// SpecificId
			// トラップ定義の重複チェック
			MonitorTrapValueInfoEntityPK entityPk = new MonitorTrapValueInfoEntityPK(
					checkInfo.getMonitorId(), 
					v.getMib(),
					v.getTrapOid(), 
					v.getGenericId(),
					v.getSpecificId());
			if (pkList.contains(entityPk)) {
				String arg;
				if (v.getVersion() == SnmpVersionConstant.TYPE_V1) {
					arg = new String("MIB="+v.getMib()+",OID="+v.getTrapOid()+",generic_id="+v.getGenericId()+",specific_id="+v.getSpecificId());
				} else {
					arg = new String("MIB="+v.getMib()+",OID="+v.getTrapOid());
				}
				InvalidSetting e = new InvalidSetting(Messages.getString("message.snmptrap.trapoid.overlaps", new String[]{arg}));
				m_log.info("validateSnmptrap() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			pkList.add(entityPk);


			// Logmsg : not implemented
			// Description : not implemented

			switch (v.getProcessingVarbindType()) {
			case MonitorTrapConstant.PROC_VARBIND_SPECIFIED:
				if (v.getVarBindPatterns().isEmpty()) {
					InvalidSetting e = new InvalidSetting(Messages.getString("message.snmptrap.value.must.specify.more.than.one.pattern"));
					m_log.info("validateSnmptrap() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				break;
			case MonitorTrapConstant.PROC_VARBIND_ANY:
				break;
			}
			CommonValidator.validateString(Messages.getString("monitor.snmptrap.value.varbindpattern"), v.getFormatVarBinds(), false, 0, 128);

			for (VarBindPattern p: v.getVarBindPatterns()) {
				CommonValidator.validateString(Messages.getString("pattern.matching.expression"), p.getPattern(), false, 0, 1024);
				CommonValidator.validateString(Messages.getString("monitor.snmptrap.value.pattern.description"), p.getDescription(), false, 0, 256);
				// processType : not implemented
				// caseSensitivityFlg : not implemented
				// validFlg : not implemented
				// priority : not implemented
			}
		}
	}


	/**
	 * SNMP監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateSnmpNumeric(MonitorInfo monitorInfo) throws InvalidSetting {
		validateSnmp(monitorInfo);

		validateNumeric(monitorInfo, -1);
	}

	/**
	 * SNMP監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateSnmp(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateSnmp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateSnmp() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		SnmpCheckInfo checkInfo = monitorInfo.getSnmpCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("SNMP Monitor Setting is not defined. monitorId = " + monitorInfo.getMonitorId());
			m_log.info("validateSnmp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if (!HinemosModuleConstant.MONITOR_SNMP_N.equals(monitorInfo.getMonitorTypeId())
				&& !HinemosModuleConstant.MONITOR_SNMP_S.equals(monitorInfo.getMonitorTypeId())) {
			InvalidSetting e = new InvalidSetting("This is SNMP Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validateSnmp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// communityName : not implemented

		// convertFlg
		if(checkInfo.getConvertFlg() == null) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.snmp.3"));
			m_log.info("validateSnmp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// snmpOid
		String oid = checkInfo.getSnmpOid();
		if(oid == null || "".equals(oid)){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.snmp.2"));
			m_log.info("validateSnmp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(Messages.getString("oid"), oid, true, 1, 1024);
		//.と[0-9]以外はNG
		char c = 'a';
		for (int i = 0; i < oid.length(); i++) {
			c = oid.charAt(i);
			if (c != '.' && !('0' <= c && c <= '9')) {
				InvalidSetting e = new InvalidSetting(Messages.getString("message.snmp.2"));
				m_log.info("validateSnmp() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		// snmpPort : not implemented

		// snmpVersion : not implemented
	}

	/**
	 * SQL監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateSqlNumeric(MonitorInfo monitorInfo) throws InvalidSetting {
		validateSql(monitorInfo);

		validateNumeric(monitorInfo, -1);
	}

	/**
	 * SQL監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateSql(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateSql() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateSql() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		SqlCheckInfo checkInfo = monitorInfo.getSqlCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("SQL Monitor Setting is not defined. monitorId = "
					+ monitorInfo.getMonitorId());
			m_log.info("validateSql() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if (!HinemosModuleConstant.MONITOR_SQL_N.equals(monitorInfo.getMonitorTypeId())
				&& !HinemosModuleConstant.MONITOR_SQL_S.equals(monitorInfo.getMonitorTypeId())) {
			InvalidSetting e = new InvalidSetting("This is SQL Monitor Setting. But MonitorTypeId = "
					+ monitorInfo.getMonitorTypeId());
			m_log.info("validateSql() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// connectionUrl
		String url = checkInfo.getConnectionUrl();
		if(url == null || "".equals(url) || url.length() < 6 ||
				!url.startsWith("jdbc:")){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.sql.8"));
			m_log.info("validateSql() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(Messages.getString("connection.url"),
				checkInfo.getConnectionUrl(), true, 1, 256);

		// user
		if(checkInfo.getUser() == null || "".equals(checkInfo.getUser())){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.sql.2"));
			m_log.info("validateSql() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(Messages.getString("user.id"),
				checkInfo.getUser(), true, 1, 64);

		// password
		if(checkInfo.getPassword() == null || "".equals(checkInfo.getPassword())){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.sql.3"));
			m_log.info("validateSql() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(Messages.getString("password"),
				checkInfo.getPassword(), true, 1, 64);

		// query
		if(checkInfo.getQuery() == null || checkInfo.getQuery().length() < 7){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.sql.5"));
			m_log.info("validateSql() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}else {
			String work = checkInfo.getQuery().substring(0, 6);
			if(!work.equalsIgnoreCase("SELECT")){
				InvalidSetting e = new InvalidSetting(Messages.getString("message.sql.5"));
				m_log.info("validateSql() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		CommonValidator.validateString(Messages.getString("sql.string"),
				checkInfo.getQuery(), true, 1, 1024);

		// jdbcDriver
		if(checkInfo.getJdbcDriver() == null || "".equals(checkInfo.getJdbcDriver())){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.sql.1"));
			m_log.info("validateSql() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * システムログ監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateSystemlog(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateSystemlog() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateSystemlog() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo : not impletemted( check info is not exists)
		m_log.debug("validateSystemlog() is not needed. ");

		// monitorType
		if(!HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Systemlog Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validateSystemlog() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * コマンド監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	private static void validateCustom(MonitorInfo monitorInfo) throws InvalidSetting, InvalidRole {

		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateCustom() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateCustom() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		CustomCheckInfo checkInfo = monitorInfo.getCustomCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Custom Monitor Setting is not defined. monitorId = " + monitorInfo.getMonitorId());
			m_log.info("validateCustom() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_CUSTOM.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Custom Monitor Setting. But MonitorTypeId = "
					+ monitorInfo.getMonitorTypeId());
			m_log.info("validateCustom() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// execType
		if(checkInfo.getCommandExecType() == null &&
				(checkInfo.getCommandExecType().equals(CustomConstant.CommandExecType.SELECTED)
						|| checkInfo.getCommandExecType().equals(CustomConstant.CommandExecType.INDIVIDUAL))){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.custom.msg.type.undef"));
			m_log.info("validateCustom() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// selectedFacilityId
		if(checkInfo.getCommandExecType().equals(CustomConstant.CommandExecType.SELECTED)){
			if(checkInfo.getSelectedFacilityId() == null || "".equals(checkInfo.getSelectedFacilityId())){
				InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.custom.msg.node.undef"));
				m_log.info("validateCustom() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			try {
				FacilityTreeCache.validateFacilityId(checkInfo.getSelectedFacilityId(), monitorInfo.getOwnerRoleId(), false);
			} catch (FacilityNotFound e) {
				throw new InvalidSetting(Messages.getString("message.monitor.custom.msg.node.undef"));
			}
		}
		// effectiveUser
		if ((checkInfo.getSpecifyUser() == YesNoConstant.TYPE_YES || checkInfo.getSpecifyUser() == YesNoConstant.TYPE_NO) &&
				(checkInfo.getSpecifyUser() == YesNoConstant.TYPE_YES &&(checkInfo.getEffectiveUser() == null || "".equals(checkInfo.getEffectiveUser())))) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.custom.msg.effectiveuser.undef"));
			m_log.info("validateCustom() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		if (checkInfo.getSpecifyUser() == YesNoConstant.TYPE_YES) {
			CommonValidator.validateString(Messages.getString("effective.user"), checkInfo.getEffectiveUser(),
					true, 1, 64);
		}

		// command
		if(checkInfo.getCommand() == null || "".equals(checkInfo.getCommand())){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.custom.msg.command.undef"));
			m_log.info("validateCustom() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(Messages.getString("command"), checkInfo.getCommand(),
				true, 1, 1024);

		// timeout
		if(checkInfo.getTimeout() == null) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.custom.msg.timeout.undef"));
			m_log.info("validateCustom() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(Messages.getString("time.out"),
				checkInfo.getTimeout(), 1, 60 * 60 * 1000);
		int timeout = checkInfo.getTimeout();
		if (monitorInfo.getRunInterval() * 1000 < timeout) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.monitor.custom.msg.timeout.toolarge"));
			m_log.info("validateCustom() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		validateNumeric(monitorInfo, -1);
	}

	/**
	 * Windowsサービス監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateWinService(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateWinService() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateWinService() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		WinServiceCheckInfo checkInfo = monitorInfo.getWinServiceCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Windows Service Monitor Setting is not defined. monitorId = "
					+ monitorInfo.getMonitorId());
			m_log.info("validateWinService() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_WINSERVICE.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Windows Service Monitor Setting. But MonitorTypeId = "
					+ monitorInfo.getMonitorTypeId());
			m_log.info("validateWinService() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// serviceName
		if(checkInfo.getServiceName() == null || "".equals(checkInfo.getServiceName())){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.winservice.1"));
			m_log.info("validateWinService() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(Messages.getString("winservice.name"),
				checkInfo.getServiceName(), true, 1, 1024);
	}

	/**
	 * Windowsイベント監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateWinEvent(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateWinService() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateWinEvent() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		WinEventCheckInfo checkInfo = monitorInfo.getWinEventCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Windows Event Monitor Setting is not defined. monitorId = "
					+ monitorInfo.getMonitorId());
			m_log.info("validateWinEvent() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_WINEVENT.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Windows Event Monitor Setting. But MonitorTypeId = "
					+ monitorInfo.getMonitorTypeId());
			m_log.info("validateWinEvent() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// level
		if(!(checkInfo.isLevelCritical() || checkInfo.isLevelError() || checkInfo.isLevelInformational() || checkInfo.isLevelVerbose() || checkInfo.isLevelWarning()) ){
			InvalidSetting e = new InvalidSetting("Windows Event Monitor Setting must have one or more enabled levels. ");
			m_log.info("validateWinEvent() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// logName
		if(checkInfo.getLogName() == null || checkInfo.getLogName().size() == 0){
			InvalidSetting e = new InvalidSetting("Windows Event Monitor Setting must have one or more log names. ");
			m_log.info("validateWinEvent() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		////
		// Length Check
		////

		//logName : character varying(256)（winevent.log）
		for(String logName : checkInfo.getLogName()){
			CommonValidator.validateString(Messages.getString("winevent.log") + ":" + logName,logName, true, 1, 256);
		}

		//source : character varying(256)（winevent.source）
		for(String source : checkInfo.getSource()){
			CommonValidator.validateString(Messages.getString("winevent.source") + ":" + source,source, true, 1, 256);
		}

		//eventId : smallint(winevent.id)
		for(Integer eventId : checkInfo.getEventId()){
			CommonValidator.validateInt(Messages.getString("winevent.id") + ":" + eventId, eventId, 0, 32767);
		}

		//category : smallint（winevent.category）
		for(Integer category : checkInfo.getCategory()){
			CommonValidator.validateInt(Messages.getString("winevent.category") + ":" + category, category, 0, 32767);
		}

		//keywaord : bigint(winevent.keywords)
		for(Long keyword : checkInfo.getKeywords()){
			CommonValidator.validateDouble(Messages.getString("winevent.keywords") + ":" + keyword, keyword, 0, Long.MAX_VALUE);
		}

	}

	/**
	 * Windowsサービス監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateJMX(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateJMX() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateJMX() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		JmxCheckInfo checkInfo = monitorInfo.getJmxCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("JMX Monitor Setting is not defined. monitorId = "
					+ monitorInfo.getMonitorId());
			m_log.info("validateJMX() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_JMX.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is JMX Monitor Setting. But MonitorTypeId = "
					+ monitorInfo.getMonitorTypeId());
			m_log.info("validateJMX() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		if (checkInfo.getAuthPassword() != null) {
			CommonValidator.validateString(Messages.getString("monitor.jmx.authpassword"), checkInfo.getAuthPassword(), false, 0, 64);
		}

		if (checkInfo.getPort() == null) {
			InvalidSetting e = new InvalidSetting("JMX Monitor Setting must hava a destination port. monitorId = " + monitorInfo.getMonitorId());
			m_log.info("validateJMX() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(Messages.getString("monitor.jmx.port"), checkInfo.getPort(), 0, 65535);
	}
}
