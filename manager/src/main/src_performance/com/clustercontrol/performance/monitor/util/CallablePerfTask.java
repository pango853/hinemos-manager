package com.clustercontrol.performance.monitor.util;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.performance.monitor.factory.RunMonitorPerformance;

/**
 * リソース監視を実行するクラスです
 * 
 *
 */
public class CallablePerfTask implements Callable<ArrayList<MonitorRunResultInfo>> {

	private RunMonitorPerformance m_runMonitor;
	private String m_facilityId;

	/**
	 * 
	 * @param monitor
	 * @param facilityId
	 */
	public CallablePerfTask(RunMonitorPerformance monitor, String facilityId) {
		m_runMonitor = monitor;
		m_facilityId = facilityId;
	}
	@Override
	public ArrayList<MonitorRunResultInfo> call() throws Exception {

		JpaTransactionManager jtm = null;
		// 結果を格納
		ArrayList<MonitorRunResultInfo> infoList = new ArrayList<MonitorRunResultInfo>();

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 各監視処理を実行し、実行の可否を格納
			infoList = m_runMonitor.collectList(m_facilityId);

			// コミット
			jtm.commit();
		} catch (Exception e) {
			jtm.rollback();
			throw e;
		} finally {
			// 一時停止していたトランザクションを再開
			jtm.close();
		}

		return infoList;
	}

}
