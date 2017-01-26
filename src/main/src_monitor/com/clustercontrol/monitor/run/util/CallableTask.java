package com.clustercontrol.monitor.run.util;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.ProcessConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorStringValueType;

/**
 * 
 * 各監視を実行するクラスです。
 * 
 * @version 4.0.0
 * @since 2.4.0
 */
public class CallableTask implements Callable<MonitorRunResultInfo>{

	private RunMonitor m_runMonitor;
	private String m_facilityId;

	// Logger
	private static Log m_log = LogFactory.getLog( CallableTask.class );

	/**
	 * コンストラクタ
	 * @param monitor
	 * @param facilityId
	 */
	public CallableTask(RunMonitor monitor, String facilityId) {
		m_runMonitor = monitor;
		m_facilityId = facilityId;
	}

	/**
	 * 各監視を実行します。
	 * 
	 * @see #setMonitorInfo(String, String)
	 */
	@Override
	public MonitorRunResultInfo call() throws Exception {

		JpaTransactionManager jtm = null;

		// 結果を格納
		MonitorRunResultInfo info = new MonitorRunResultInfo();
		info.setFacilityId(m_facilityId);

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			Boolean ret = false;
			// 各監視処理を実行し、実行の可否を格納
			ret = m_runMonitor.collect(m_facilityId);

			// 監視値より判定結果を取得
			// 値取得失敗だと-1が返ってくる。
			// (文字列監視の場合、 どれにもマッチしなかったら-2が返ってくる。)
			Integer checkResult = m_runMonitor.getCheckResult(ret);

			info.setMonitorFlg(ret);
			info.setCollectorResult(ret);
			info.setCheckResult(checkResult);
			info.setMessage(m_runMonitor.getMessage(checkResult));
			info.setMessageId(m_runMonitor.getMessageId(checkResult));
			info.setMessageOrg(m_runMonitor.getMessageOrg(checkResult));
			if (checkResult == -2) {
				info.setPriority(PriorityConstant.TYPE_NONE);
			} else {
				info.setPriority(m_runMonitor.getPriority(checkResult));
			}
			info.setNodeDate(m_runMonitor.getNodeDate());
			info.setValue(m_runMonitor.getValue());
			if (checkResult == -2) {
				info.setProcessType(ProcessConstant.TYPE_NO);
			} else {
				info.setProcessType(ProcessConstant.TYPE_YES);
			}

			// 文字列監視の場合
			if(m_runMonitor instanceof RunMonitorStringValueType){
				// 値取得に成功し、マッチング処理でマッチした場合のみパターンマッチ表現/処理タイプを設定する。
				if(ret && (checkResult > -1)){
					// 通知抑制のサブキーとするため、パターンマッチ表現を設定する。
					info.setPatternText(((RunMonitorStringValueType)m_runMonitor).getPatternText(checkResult));

					// 処理タイプを更新する
					info.setProcessType(((RunMonitorStringValueType)m_runMonitor).getProcessType(checkResult));
				}
			}

			// 通知グループIDをセットする
			// SNMP文字列監視のマルチスレッド化対応用の実装
			info.setNotifyGroupId(m_runMonitor.getNotifyGroupId());
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("call() : "
					+ "facilityId=" + m_facilityId + ", notifyGroupId=" + m_runMonitor.getNotifyGroupId()
					+ ", Exception=" + e.getClass().getSimpleName() + ", message=" + e.getMessage(), e);
			jtm.rollback();
			throw e;
		} finally {
			jtm.close();
		}
		return info;
	}
}
