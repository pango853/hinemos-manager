/*

Copyright (C) 2008 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

 */

package com.clustercontrol.poller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 各収集プロトコルのポーリングのための情報を保持するクラス
 */
public class PollingControllerConfig implements Serializable {
	private static Log m_log = LogFactory.getLog( PollingControllerConfig.class );

	/**
	 * ポーリングした収集対象値群の結果のインデックスが一致しているか否かをチェックするか否かを指定するフラグ
	 */
	private boolean m_indexCheckFlg;

	/**
	 * Key   : 収集名
	 * Value : 収集間隔と,収集プロトコルとポーリング対象のマップ,の設定
	 */
	private ConcurrentHashMap<String ,IntervalAndValues> m_targetValueMap;

	/**
	 * m_targetValueMapが更新されるたびに更新の必要がある
	 * 
	 * Key   : ポーリング対象名(プロトコルを含む)
	 * Value : ポーリング対象名毎のポーリング実行間隔設定
	 */
	private ConcurrentHashMap<PollingTargetAndCollectMethod, IntervalAndNextTime> m_scheduleMap;

	/**
	 * 収集間隔毎にデータテーブルへの次回書き込み時刻を保持する
	 * m_targetValueMapが更新されるたびに更新の必要がある
	 * 
	 * Key   : 収集間隔（秒単位）
	 * Value : 次回書き込み時刻
	 */
	private ConcurrentHashMap<Integer, Long> m_refreshTableTimes;

	/**
	 * 最小の収集間隔（秒単位）
	 */
	private int m_minPollingInterval = -1;

	/**
	 * 指定の各収集プロトコルのパラメータ設定でインスタンスを生成する
	 * 
	 * @param config 各収集プロトコルのパラメータ設定
	 */
	public PollingControllerConfig(boolean indexCheckFlg){
		m_targetValueMap = new ConcurrentHashMap<String ,IntervalAndValues>();
		m_scheduleMap = new ConcurrentHashMap<PollingTargetAndCollectMethod, IntervalAndNextTime>();
		m_refreshTableTimes = new ConcurrentHashMap<Integer, Long>();
		m_indexCheckFlg = indexCheckFlg;
	}

	/**
	 * 収集対象のポーリング対象を登録する
	 * @param collectorName 収集名
	 * @param interval 収集間隔
	 * @param pollingMap 収集プロトコルとポーリング対象のマップ
	 */
	public void putPollingTargets(String collectorName, int interval, HashMap<String, List<String>> pollingMap){
		m_log.debug("putPollingTargets() collectorName = " + collectorName + ", interval = " + interval);

		try {
			PollerManager._lock.writeLock();
			
			// 収集間隔と,収集プロトコルと取得対象のマップ,の設定を収集名をキーにしてマップに登録する
			m_targetValueMap.put(collectorName, new IntervalAndValues(interval, pollingMap));
		} finally {
			PollerManager._lock.writeUnlock();
		}

		// 収集対象値単位の収集間隔を更新する
		rescheduling();
	}

	/**
	 * 指定の収集名で登録されているポーリング対象を収集対象から除く
	 * @param collectorName 収集名
	 * @return 最小収集間隔が更新された場合はtrueを返す
	 */
	public boolean removePollingTargets(String collectorName){
		m_log.debug("removePollingTargets() collectorName = " + collectorName);

		try {
			PollerManager._lock.writeLock();
			
			// 指定の収集名の設定を削除する
			m_targetValueMap.remove(collectorName);
		} finally {
			PollerManager._lock.writeUnlock();
		}

		// 収集間隔を更新する
		return rescheduling();
	}

	/**
	 * 収集名で登録されている全てのポーリング対象を収集対象から除く
	 * @param collectorName 収集名
	 * @return 最小収集間隔が更新された場合はtrueを返す
	 */
	public boolean removePollingAllTargets(){
		m_log.debug("removePollingAllTargets()");

		try {
			PollerManager._lock.writeLock();
			
			// 指定の収集名の設定を削除する
			m_targetValueMap.clear();
		} finally {
			PollerManager._lock.writeUnlock();
		}

		// 収集間隔を更新する
		return rescheduling();
	}


	/**
	 * ポーリング対象単位の収集間隔を更新し、
	 * 最小の収集間隔（秒単位）を算出します（m_minPollingIntervalに設定）。
	 * 新規に追加された収集間隔がある場合、収集間隔毎のデータテーブルへの次回書き込み時刻を設定します。
	 * @return 最小収集間隔が更新された場合はtrueを返す
	 */
	private boolean rescheduling(){
		try {
			PollerManager._lock.writeLock();
			
			// 最小収集間隔が更新されたことを示すフラグ
			boolean modifyFlg = false;

			// 収集対象値単位の収集間隔を更新します
			m_scheduleMap.clear();

			// 一旦最小収集間隔をリセットする
			m_minPollingInterval = Integer.MAX_VALUE;

			// 収集名ごとに登録されている全ての収集対象値に対して処理するように
			// キーである収集名のセットでループをまわす
			for(String collectorName : m_targetValueMap.keySet()){
				m_log.debug("collectorName : " + collectorName);

				IntervalAndValues setting = m_targetValueMap.get(collectorName);

				// 収集ＩＤ毎の収集プロトコルと取得対象のマップを取得する
				HashMap<String, List<String>> pollingMap = setting.getPoliingMap();

				// 収集ＩＤ毎の収集間隔を取得する
				int interval = setting.getPollingInterval();

				// 最小収集間隔を設定する
				if(m_minPollingInterval > interval){
					m_log.debug("Set Min Polling Intreval : " + interval);
					m_minPollingInterval = interval;
					modifyFlg = true;
				}

				// ポーリング対象をキーにしてポーリング対象毎の収集間隔を設定する

				Iterator<String> keys = pollingMap.keySet().iterator();
				while(keys.hasNext()){
					String protocol = keys.next();

					m_log.debug("protocol : " + protocol);

					Iterator<String> pollingTargets = pollingMap.get(protocol).iterator();

					while(pollingTargets.hasNext()){

						// pollingTargetの空チェック
						String pollingTarget = pollingTargets.next();

						if(pollingTarget != null && !pollingTarget.equals("")) {
							PollingTargetAndCollectMethod target =
									new PollingTargetAndCollectMethod(pollingTarget, protocol);

							// 既に設定されている収集間隔がある場合は、その値と比較し、
							// 小さい場合に、更新する。
							if(m_scheduleMap.get(target) != null){
								if(m_scheduleMap.get(target).getPollingInterval() > interval){
									m_scheduleMap.get(target).setPollingInterval(interval);
								}
							} else {
								m_scheduleMap.put(target, new IntervalAndNextTime(interval));
								m_log.debug("m_scheduleMap put PollingTarget : " + target.getPollingTarget() + ", Protocol : " + target.getProtocol());
							}
						}
					}
				}

				// 新規に追加された収集間隔がある場合は、
				// 収集間隔毎にテーブルへの次回書き込み時刻を設定する
				if(m_refreshTableTimes.get(interval) == null){
					m_refreshTableTimes.put(interval, 0L);
				}
			}

			// 最小収集間隔が設定されていない場合は、-lとする
			if(m_minPollingInterval == Integer.MAX_VALUE){
				m_minPollingInterval = -1;
				modifyFlg = true;
			}

			return modifyFlg;
		} finally {
			PollerManager._lock.writeUnlock();
		}
	}


	public HashMap<String, List<String>> getCurrentTargetMap(long currentTime){
		HashMap<String, List<String>> currentTarget = new HashMap<String, List<String>>();

		m_log.debug("getCurrentTargetMap start");

		// 各ポーリング対象をキーにして収集対象値毎の次回収集時刻を取得する
		Iterator<PollingTargetAndCollectMethod> itr = m_scheduleMap.keySet().iterator();

		m_log.debug("m_scheduleMap size : " + m_scheduleMap.keySet().size());

		while(itr.hasNext()){
			PollingTargetAndCollectMethod target = itr.next();

			if(m_scheduleMap.get(target) != null){

				long nextPollingTime = m_scheduleMap.get(target).getNextPollingTime();
				int interval = m_scheduleMap.get(target).getPollingInterval();

				if(nextPollingTime <= currentTime){

					String pollingTarget = target.getPollingTarget();
					String protocol = target.getProtocol();

					List<String> targetList = currentTarget.get(protocol);

					if(targetList == null) {
						targetList = new ArrayList<String>();
						targetList.add(pollingTarget);

						currentTarget.put(protocol, targetList);
					}
					else{
						targetList.add(pollingTarget);
					}

					// 次回ポーリング時刻を設定する
					long intervalMillis = interval * 1000L;

					// 処理遅延が発生した場合を考慮し、現時刻よりも先の時刻を設定するように算出
					long count = (currentTime - nextPollingTime) / intervalMillis;

					nextPollingTime = nextPollingTime + intervalMillis * (count + 1);

					m_scheduleMap.get(target).setNextPollingTime(nextPollingTime);
				}
			}
		}

		return currentTarget;
	}

	/**
	 * 与えられた時刻で更新すべき収集間隔のリストを返します。
	 * 
	 * 例）
	 *   収集間隔、1分、5分で収集されている場合
	 *   前回収集時刻が、10:00:00 の場合で、currentTimeとして、10:01:08が与えられると、
	 *   収集間隔 1分 は、この時刻で更新すべき
	 *   収集間隔 5分 は、10:05:00までは更新すべきでない
	 *   よって、
	 *   返る収集間隔は、1分（=60）となります。
	 * 
	 * @param currentTime 現在時刻（チェック対象時刻）
	 * @return 収集間隔のリスト
	 */
	public List<Integer> getCurrentRefreshIntervals(long currentTime){
		m_log.debug("current time : " + currentTime);

		List<Integer> currentTarget = new ArrayList<Integer>();

		// 収集間隔をキーにして次回書き込み時刻を取得する
		Iterator<Integer> itr = m_refreshTableTimes.keySet().iterator();
		HashMap<Integer, Long> tmpRefreshTableTimes = new HashMap<Integer, Long>();
		while(itr.hasNext()){
			int interval = itr.next();

			long nextTime = m_refreshTableTimes.get(interval);
			if(nextTime <= currentTime){
				currentTarget.add(interval);

				// 次回書き込み時刻を設定する
				long intervalMillis = interval * 1000L;

				// 処理遅延が発生した場合を考慮し、現時刻よりも先の時刻を設定するように算出
				long count = (currentTime - nextTime) / intervalMillis;

				nextTime = nextTime + intervalMillis * (count + 1);

				m_log.debug("put nexttime : " + interval + ", " + nextTime);

				tmpRefreshTableTimes.put(interval, nextTime);
			}
		}
		m_refreshTableTimes.putAll(tmpRefreshTableTimes);

		return currentTarget;
	}

	public Set<String> getCollectorNames(){
		return m_targetValueMap.keySet();
	}

	/**
	 * 設定されている収集名のうち収集間隔が最小のものの値を返します。
	 * @return 最小収集間隔
	 */
	public int getMinPollingInterval() {
		return m_minPollingInterval;
	}

	/**
	 * 指定の収集名の収集が設定されていることを確認する
	 * @param collectorName 収集名
	 * @return 設定されている場合はtrue
	 */
	public boolean containsCollectorName(String collectorName){
		try {
			PollerManager._lock.readLock();
			return m_targetValueMap.containsKey(collectorName);
		} finally {
			PollerManager._lock.readUnlock();
		}
	}

	public boolean isIndexCheckFlg() {
		return m_indexCheckFlg;
	}

	/**
	 * 管理している収集名ごとの情報を文字列のリストで出力します。
	 * @return 管理している収集名ごとの収集期間、最終収集時刻情報
	 */
	public String getDebugInfo(){
		String debugStr = "";

		Iterator<String> itr = m_targetValueMap.keySet().iterator();

		while(itr.hasNext()){
			String collectorName = itr.next();
			HashMap<String, List<String>> pollingMap = m_targetValueMap.get(collectorName).getPoliingMap();
			int interval = m_targetValueMap.get(collectorName).getPollingInterval();

			String str = "\t" + collectorName + "  interval : " + interval + "\n";

			Iterator<String> keys = pollingMap.keySet().iterator();
			while(keys.hasNext()){
				String collectMethod = keys.next();
				Iterator<String> pollingTargets = pollingMap.get(collectMethod).iterator();

				while(pollingTargets.hasNext()) {
					String pollingTarget = pollingTargets.next();
					IntervalAndNextTime ian = m_scheduleMap.get(new PollingTargetAndCollectMethod(pollingTarget, collectMethod));
					str = str
							+ "\t" + collectMethod
							+ "\t" + pollingTarget
							+ "\t" + ian.getPollingInterval()
							+ "\t" + new Date(ian.getNextPollingTime()) + "\n";
				}

			}

			debugStr = debugStr + str;
		}

		return debugStr;
	}

	private class IntervalAndValues implements Serializable {
		private int m_pollingInterval;
		// key:key:収集プロトコル、val:ポーリング対象
		private HashMap<String, List<String>> m_pollingMap;

		public IntervalAndValues(int pollingInterval, HashMap<String, List<String>> pollingMap){
			m_pollingInterval = pollingInterval;
			m_pollingMap = pollingMap;
		}

		public int getPollingInterval(){
			return m_pollingInterval;
		}

		public HashMap<String, List<String>> getPoliingMap(){
			return m_pollingMap;
		}
	}

	private class IntervalAndNextTime implements Serializable {
		private int m_pollingInterval;
		private long m_nextPollingTime;

		public IntervalAndNextTime(int pollingInterval){
			m_pollingInterval = pollingInterval;
			m_nextPollingTime = 0L;
		}

		public int getPollingInterval(){
			return m_pollingInterval;
		}

		public void setPollingInterval(int interval){
			m_pollingInterval = interval;
		}

		public long getNextPollingTime(){
			return m_nextPollingTime;
		}

		public void setNextPollingTime(long nextPollingTime){
			m_nextPollingTime = nextPollingTime;
		}
	}

	private class PollingTargetAndCollectMethod implements Serializable {
		private String m_protocol;
		private String m_pollingTarget;

		public PollingTargetAndCollectMethod(String pollingTarget, String protocol){
			m_pollingTarget = pollingTarget;
			m_protocol = protocol;
		}

		public String getProtocol(){
			return m_protocol;
		}

		public String getPollingTarget(){
			return m_pollingTarget;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof PollingTargetAndCollectMethod) {
				PollingTargetAndCollectMethod info = (PollingTargetAndCollectMethod)other;

				if (this.m_pollingTarget == null && this.m_protocol == null){
					if (info.m_pollingTarget == null && info.m_protocol == null){
						return true;
					}
				} else if (this.m_pollingTarget == null && this.m_protocol != null){
					if (info.m_pollingTarget == null && this.m_protocol.equals(info.m_protocol)){
						return true;
					}
				} else if (this.m_pollingTarget != null && this.m_protocol == null){
					if (this.m_pollingTarget.equals(info.m_pollingTarget) && info.m_protocol == null){
						return true;
					}
				} else {
					if (this.m_pollingTarget.equals(info.m_pollingTarget)){
						return this.m_protocol.equals(info.m_protocol);
					}
				}
				return false;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			int result = 17;

			result = 37 * result + ((this.m_pollingTarget != null) ? this.m_pollingTarget.hashCode() : 0);

			result = 37 * result + ((this.m_protocol != null) ? this.m_protocol.hashCode() : 0);

			return result;
		}
	}
}
