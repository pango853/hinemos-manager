package com.clustercontrol.poller.session;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.poller.PollerManager;
import com.clustercontrol.poller.PollingController;
import com.clustercontrol.util.apllog.AplLogger;

/**
 *　スケジューラ(Quartz)からキックされる情報収集処理の実行クラス
 * 
 */
public class PollingJobBean {

	private static Log m_log = LogFactory.getLog( PollingJobBean.class );

	public static final String METHOD_NAME = "run";

	// Quartzに起動されたスレッドの情報を格納するクラス
	private class PollingThreadInfo {
		public PollingThreadInfo(String pollerGroup, String pollerName) {
			this.pollerGroup = pollerGroup;
			this.pollerName = pollerName;
		}
		private final long startTime = System.currentTimeMillis();
		public long getStartTime() {
			return startTime;
		}
		private final String pollerGroup;
		public String getPollerGroup() {
			return pollerGroup;
		}
		private final String pollerName;
		public String getPollerName() {
			return pollerName;
		}
		private volatile boolean isLogged = false;
		public boolean getIsLogged() {
			return isLogged;
		}
		public void setIsLogged() {
			isLogged = true;
		}
	}
	// quartzより起動されて現在アクティブなスレッドに関する情報をスレッドIDをキーに保持するマップ
	private static final ConcurrentHashMap<Long, PollingThreadInfo> activePollingThreadInfoMap = new ConcurrentHashMap<Long, PollingThreadInfo>();

	/**
	 * Quartzからのコールバックメソッド<BR>
	 * 
	 * トランザクション開始はユーザが制御する。
	 * また、追加実装により、トランザクションの入れ子が予期せず生じることを避けるため、Neverを採用する。
	 * 
	 * @since 4.0.0
	 */
	public void run(String jndiName, String pollerGroup, String pollerName) throws HinemosUnknown {
		// デバッグログ出力
		m_log.debug("execute start : ");

		JpaTransactionManager jtm = null;

		try {
			// トランザクションがすでに開始されている場合は処理終了
			jtm = new JpaTransactionManager();
			jtm.begin(true);

			// 指定のポーラを取得する
			PollerManager manager = PollerManager.getInstnace();

			PollingController poller = manager.getPoller(pollerGroup, pollerName);

			// 現在走行中のスレッド情報を走査して、長時間（ここでは60*60*1000(ms)=1h）
			// 経過していて、 なおかつ今回初めて長いと判定されたスレッド情報を探し出す。
			final int timeout_min = 60;
			for (final PollingThreadInfo threadInfo : activePollingThreadInfoMap.values()) {
				if (System.currentTimeMillis() - (timeout_min*60*1000) > threadInfo.getStartTime() &&
						threadInfo.getIsLogged() == false) {

					// 今回リークした可能性があるスレッドについて情報を取りだし、ログ出力済みフラグを立てる
					final String longPollerGroup = threadInfo.getPollerGroup();
					final String longPollerName = threadInfo.getPollerName();
					threadInfo.setIsLogged();

					// 全スレッドに関する統計情報を取得する
					final int currentThreadCount = activePollingThreadInfoMap.size();
					int longThreadCount = 0;
					for (final PollingThreadInfo tmpInfo : activePollingThreadInfoMap.values()) {
						if (tmpInfo.getIsLogged()) {
							longThreadCount++;
						}
					}

					// 今回新規にリークした可能性があるスレッドの情報と、全スレッドの統計情報をログ出力する
					AplLogger apllog = new AplLogger("COMMON", "common");
					String[] args = {
							Integer.toString(timeout_min),
							longPollerGroup,
							longPollerName,
							Integer.toString(longThreadCount),
							Integer.toString(currentThreadCount)
					};
					apllog.put("SYS", "001", args);
				}
			}

			// ポーリング開始前に自身のスレッド情報をマップに登録
			activePollingThreadInfoMap.put(
					Thread.currentThread().getId(),
					new PollingThreadInfo(pollerGroup, pollerName)
					);

			// ポーリングを実行
			poller.run();
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("addMaintenance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
		} finally {
			// 自身のスレッド情報を登録解除
			activePollingThreadInfoMap.remove(Thread.currentThread().getId());

			jtm.close();
		}

		// デバッグログ出力
		m_log.debug("execute end   : " + jndiName);
	}

}
