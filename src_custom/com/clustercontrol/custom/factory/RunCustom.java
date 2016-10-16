/*


This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.custom.factory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.CustomInvalid;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.custom.bean.CommandResultDTO;
import com.clustercontrol.monitor.run.bean.MonitorInfo;
import com.clustercontrol.monitor.run.bean.MonitorNumericValueInfo;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.performance.bean.CollectedDataErrorTypeConstant;
import com.clustercontrol.performance.bean.Sample;
import com.clustercontrol.performance.util.PerformanceDataUtil;
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 * コマンド監視の監視処理を実装したクラス<br/>
 * 
 * @since 4.0
 */
public class RunCustom {

	private static Log m_log = LogFactory.getLog( RunCustom.class );

	private final CommandResultDTO result;
	private HashMap<Integer, MonitorNumericValueInfo> thresholds = new HashMap<Integer, MonitorNumericValueInfo>();

	/**
	 * コンストラクタ<br/>
	 * @param result 各エージェントから送信されるコマンドの実行結果情報
	 * @throws HinemosUnknown 予期せぬ内部エラーが発生した場合
	 * @throws MonitorNotFound コマンドの実行結果に対応する監視設定が存在しない場合
	 */
	public RunCustom(CommandResultDTO result) throws HinemosUnknown, MonitorNotFound {
		this.result = result;

		// Local Variables
		MonitorInfo monitor = null;

		// MAIN
		try {
			if (m_log.isDebugEnabled()) {
				m_log.debug("received command result : " + result);
			}
			String monitorId = result.getMonitorId();
			monitor = new MonitorSettingControllerBean().getMonitor(monitorId, HinemosModuleConstant.MONITOR_CUSTOM);
			ArrayList<MonitorNumericValueInfo> list = monitor.getNumericValueInfo();
			for (MonitorNumericValueInfo threshold : list) {
				MonitorNumericValueInfo valueInfo = new MonitorNumericValueInfo();
				valueInfo.setPriority(threshold.getPriority());
				valueInfo.setThresholdLowerLimit(threshold.getThresholdLowerLimit());
				valueInfo.setThresholdUpperLimit(threshold.getThresholdUpperLimit());
				valueInfo.setMessageId(threshold.getMessageId());
				valueInfo.setMessage(threshold.getMessage());
				thresholds.put(valueInfo.getPriority(), valueInfo);
			}
		} catch (MonitorNotFound e) {
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			HinemosUnknown e1 = new HinemosUnknown("unexpected internal failure occurred. [" + result + "]", e);
			m_log.warn("RunCustom() unexpected internal failure occurred. [" + result + "] : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e1;
		}
	}

	/**
	 * 閾値判定を行い、監視結果を通知する。<br/>
	 * @throws HinemosUnknown 予期せぬ内部エラーが発生した場合
	 * @throws MonitorNotFound 該当する監視設定が存在しない場合
	 * @throws CustomInvalid 監視設定に不整合が存在する場合
	 */
	public void monitor() throws HinemosUnknown, MonitorNotFound, CustomInvalid {
		// Local Variables
		MonitorInfo monitor = null;

		int priority = PriorityConstant.TYPE_UNKNOWN;
		String facilityPath = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String executeDate  = "";
		String exitDate = "";
		String collectDate = "";
		String msg = "";
		String msgOrig = "";

		// MAIN
		try {
			monitor = new MonitorSettingControllerBean().getMonitor(result.getMonitorId(), HinemosModuleConstant.MONITOR_CUSTOM);

			facilityPath = new RepositoryControllerBean().getFacilityPath(result.getFacilityId(), null);
			executeDate = dateFormat.format(result.getExecuteDate());
			exitDate = dateFormat.format(result.getExitDate());
			collectDate = dateFormat.format(result.getCollectDate());

			if (result.getTimeout() || result.getStdout() == null || "".equals(result.getStdout()) || result.getResults() == null) {
				if (m_log.isDebugEnabled()) {
					m_log.debug("command monitoring : timeout or no stdout [" + result + "]");
				}

				// if command execution failed (timeout or no stdout)
				if (monitor.getMonitorFlg() == ValidConstant.TYPE_VALID) {
					msg = "FAILURE : command execution failed (timeout, no stdout or not unexecutable command)...";
					msgOrig = "FAILURE : command execution failed (timeout, no stdout or unexecutable command)...\n\n"
							+ "COMMAND : " + result.getCommand() + "\n"
							+ "COLLECTION DATE : " + collectDate + "\n"
							+ "executed at " + executeDate + "\n"
							+ "exited (or timeout) at " + exitDate + "\n"
							+ "EXIT CODE : " + (result.getExitCode() != null ? result.getExitCode() : "timeout") + "\n\n"
							+ "[STDOUT]\n" + result.getStdout() + "\n"
							+ "[STDERR]\n" + result.getStderr() + "\n";

					notify(PriorityConstant.TYPE_UNKNOWN, monitor, result.getFacilityId(), facilityPath, null, msg, msgOrig);
				}
			} else {
				// if command stdout was returned
				for (String key : result.getResults().keySet()) {
					if (m_log.isDebugEnabled()) {
						m_log.debug("command monitoring : judgement values [" + result + ", key = " + key + "]");
					}

					if (monitor.getMonitorFlg() == ValidConstant.TYPE_VALID) {	// monitor each value
						priority = judgePriority(result.getResults().get(key));

						msg = "VALUE : " + key + " = " + result.getResults().get(key);
						msgOrig = "VALUE : " + key + " = " + result.getResults().get(key) + "\n\n"
								+ "COMMAND : " + result.getCommand() + "\n"
								+ "COLLECTION DATE : " + collectDate + "\n"
								+ "executed at " + executeDate + "\n"
								+ "exited (or timeout) at " + exitDate + "\n"
								+ "EXIT CODE : " + (result.getExitCode() != null ? result.getExitCode() : "timeout") + "\n\n"
								+ "[STDOUT]\n" + result.getStdout() + "\n\n"
								+ "[STDERR]\n" + result.getStderr() + "\n";

						notify(priority, monitor, result.getFacilityId(), facilityPath, key, msg, msgOrig);
					}

					if (monitor.getCollectorFlg() == ValidConstant.TYPE_VALID) {	// collector each value
						Sample sample = new Sample(
								monitor.getMonitorId(),
								result.getCollectDate()==null?null:new Date(result.getCollectDate()));
						sample.set(result.getFacilityId(), monitor.getMonitorId(), key, result.getResults().get(key), CollectedDataErrorTypeConstant.NOT_ERROR);
						PerformanceDataUtil.put(sample);
					}
				}
				if (monitor.getMonitorFlg() == ValidConstant.TYPE_VALID) {	// notify invalid lines of stdout
					for (Integer lineNum : result.getInvalidLines().keySet()) {
						if (m_log.isDebugEnabled()) {
							m_log.debug("command monitoring : notify invalid result [" + result + ", lineNum = " + lineNum + "]");
						}
						msg = "FAILURE : invalid line found (not 2 column or duplicate) - (line " + lineNum + ") " + result.getInvalidLines().get(lineNum);
						msgOrig = "FAILURE : invalid line found (not 2 column or duplicate) - (line " + lineNum + ") " + result.getInvalidLines().get(lineNum) + "\n\n"
								+ "COMMAND : " + result.getCommand() + "\n"
								+ "COLLECTION DATE : " + collectDate + "\n"
								+ "executed at " + executeDate + "\n"
								+ "exited (or timeout) at " + exitDate + "\n"
								+ "EXIT CODE : " + (result.getExitCode() != null ? result.getExitCode() : "timeout") + "\n\n"
								+ "[STDOUT]\n" + result.getStdout() + "\n\n"
								+ "[STDERR]\n" + result.getStderr() + "\n";

						notify(PriorityConstant.TYPE_UNKNOWN, monitor, result.getFacilityId(), facilityPath, lineNum.toString(), msg, msgOrig);
					}
				}
			}
		} catch (MonitorNotFound e) {
			m_log.warn("unexpected internal failure occurred. [" + result + "]");
			throw e;
		} catch (CustomInvalid e) {
			m_log.warn("unexpected internal failure occurred. [" + result + "]");
			throw e;
		} catch (HinemosUnknown e) {
			m_log.warn("unexpected internal failure occurred. [" + result + "]");
			throw e;
		} catch (Exception e) {
			m_log.warn("unexpected internal failure occurred. [" + result + "]", e);
			throw new HinemosUnknown("unexpected internal failure occurred. [" + result + "]", e);
		}
	}

	/**
	 * 監視値を閾値判定して重要度を返す。<br/>
	 * @param value 監視値(Double.NaNも許容する)
	 * @return 重要度(PriorityConstant.INFOなど)
	 * @throws CustomInvalid 監視設定に不整合が存在する場合
	 */
	private int judgePriority(Double value) throws CustomInvalid {
		// Local Variables
		int priority = PriorityConstant.TYPE_UNKNOWN;

		// MAIN
		if (Double.isNaN(value)) {
			// if user defined not a number
			priority = PriorityConstant.TYPE_UNKNOWN;
		} else {
			// if numeric value is defined
			if (thresholds.containsKey(PriorityConstant.TYPE_INFO) && thresholds.containsKey(PriorityConstant.TYPE_WARNING)) {
				if (value >= thresholds.get(PriorityConstant.TYPE_INFO).getThresholdLowerLimit()
						&& value < thresholds.get(PriorityConstant.TYPE_INFO).getThresholdUpperLimit()) {
					return PriorityConstant.TYPE_INFO;
				} else if (value >= thresholds.get(PriorityConstant.TYPE_WARNING).getThresholdLowerLimit()
						&& value < thresholds.get(PriorityConstant.TYPE_WARNING).getThresholdUpperLimit()) {
					return PriorityConstant.TYPE_WARNING;
				} else {
					priority = PriorityConstant.TYPE_CRITICAL;
				}
			} else {
				// if threshold is not defined
				CustomInvalid e = new CustomInvalid("configuration of command monitor is not valid. [" + result + "]");
				m_log.info("judgePriority() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		return priority;
	}

	/**
	 * 重要度に対応するメッセージIDを返す。<br/>
	 * 
	 * @param priority 重要度(PriorityConstant.INFOなど)
	 * @return メッセージID
	 * @throws CustomInvalid 監視設定に不整合が存在する場合
	 */
	private String getMessageId(int priority) throws CustomInvalid {
		// Local Variables
		String messageId = "";

		// MAIN
		if (thresholds.containsKey(priority)) {
			messageId = thresholds.get(priority).getMessageId();
		} else {
			CustomInvalid e = new CustomInvalid("configuration of custom is not valid. [" + result + "]");
			m_log.info("getMessageId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return messageId;
	}

	/**
	 * 通知機能に対して、コマンド監視の結果を通知する。<br/>
	 * 
	 * @param priority 監視結果の重要度(PriorityConstant.INFOなど)
	 * @param monitor コマンド監視に対応するMonitorInfo
	 * @param facilityId 監視結果に対応するファシリティID
	 * @param facilityPath ファシリティIDに対応するパス文字列
	 * @param msg 監視結果に埋め込むメッセージ
	 * @param msgOrig 監視結果に埋め込むオリジナルメッセージ
	 * @throws HinemosUnknown 予期せぬ内部エラーが発生した場合
	 * @throws CustomInvalid 監視設定に不整合が存在する場合
	 */
	protected void notify(int priority, MonitorInfo monitor, String facilityId, String facilityPath, String deviceName, String msg, String msgOrig) throws HinemosUnknown, CustomInvalid {
		// Local Variable
		OutputBasicInfo notifyInfo = null;

		// MAIN
		notifyInfo = new OutputBasicInfo();
		notifyInfo.setMonitorId(monitor.getMonitorId());
		notifyInfo.setPluginId(HinemosModuleConstant.MONITOR_CUSTOM);
		// デバイス名単位に通知抑制されるよう、抑制用サブキーを設定する。
		notifyInfo.setSubKey(deviceName == null ? "" : deviceName);
		notifyInfo.setPriority(priority);
		notifyInfo.setApplication(monitor.getApplication());
		notifyInfo.setFacilityId(facilityId);
		notifyInfo.setScopeText(facilityPath);
		notifyInfo.setGenerationDate(result.getCollectDate());
		notifyInfo.setMessageId(getMessageId(priority));
		notifyInfo.setMessage(msg);
		notifyInfo.setMessageOrg(msgOrig);

		try {
			// 通知処理
			new NotifyControllerBean().notify(notifyInfo, NotifyGroupIdGenerator.generate(monitor));
		} catch (Exception e) {
			m_log.warn("notify() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown("unexpected internal failure occurred. [" + result + "]", e);
		}
	}

}
