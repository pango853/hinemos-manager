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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.sharedtable.DataTableNotFoundException;

public class PollerManager {

	private static Log m_log = LogFactory.getLog( PollerManager.class );

	private static PollerManager _instance = new PollerManager();

	public static final ILock _lock;

	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(PollerManager.class.getName());
	}
	
	/**
	 * 管理しているポーラを一意に識別するための情報を保持するクラス
	 * HashMapのキーとして利用できるように equalsメソッドと hashCodeメソッドを実装
	 */
	private static final class CacheKey implements Serializable, Comparable<CacheKey> {
		
		public final String key = AbstractCacheManager.KEY_COMMON_POLLERTABLE;
		
		public final String pollerGroup;
		public final String pollerName;
		
		public CacheKey(String pollerGroup, String pollerName) {
			this.pollerGroup = pollerGroup;
			this.pollerName = pollerName;
		}
		
		@Override
		public int hashCode() {
			int h = 1;
			h = h * 31 + pollerGroup == null ? 0 : pollerGroup.hashCode();
			h = h * 31 + pollerName == null ? 0 : pollerName.hashCode();
			return h;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			
			if (obj instanceof CacheKey) {
				CacheKey cast = (CacheKey)obj;
				if (pollerGroup != null && pollerName != null) {
					return pollerGroup.equals(cast.pollerGroup) && pollerName.equals(cast.pollerName);
				}
			}
			
			return false;
		}
		
		@Override
		public String toString() {
			return String.format("%s [pollerGroup = %s, pollerName = %s]", CacheKey.class.getName(), pollerGroup, pollerName);
		}

		@Override
		public int compareTo( CacheKey o ){
			int ret;
			ret = pollerGroup.compareTo( o.pollerGroup );
			if( 0 != ret ){
				return ret;
			}
			ret = pollerName.compareTo( o.pollerName );
			if( 0 != ret ){
				return ret;
			}

			return 0;
		}
	}

	/**
	 * Key   : ポーラ識別情報
	 * Value : ポーラ
	 */
	public static PollingController getCache(String pollerGroup, String pollerName) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		CacheKey key = new CacheKey(pollerGroup, pollerName);
		Serializable cache = cm.get(key);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + key + " : " + cache);
		return cache == null ? null : (PollingController)cache;
	}
	
	public static void storeCache(String pollerGroup, String pollerName, PollingController newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		CacheKey key = new CacheKey(pollerGroup, pollerName);
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + key + " : " + newCache);
		cm.store(key, newCache);
	}
	
	private static Set<CacheKey> cacheKeys() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Set<CacheKey> cacheKeys = cm.getKeySet(CacheKey.class);
		return Collections.unmodifiableSet(cacheKeys);
	}

	private PollerManager() {

	}

	public static PollerManager getInstnace() {
		return _instance;
	}

	public PollingController createPoller(
			String group,
			String facilityId,
			boolean indexCheckFlg,
			String tableGroup,
			String tableName)
					throws NotInitializedException, DataTableNotFoundException {
		if(m_log.isDebugEnabled()){
			m_log.debug(
					"create poller    " +
							"PollerGroup : " + group + ", " +
							"FacilityId : " + facilityId + ", " +
							"IndexCheckFlg : " + indexCheckFlg + ", " +
							"TableGroup : " + tableGroup + ", " +
							"TableName : " + tableName
					);
		}

		try {
			_lock.writeLock();
			
			// 収集情報、情報書き込み対象テーブル情報を格納したポーラを生成する
			// この時点ではインスタンスを生成するだけでポーリングはスケジューリングしないため、
			// ポーリングは実行されない
			PollingController poller = new PollingController(
					group,
					facilityId,
					facilityId,
					indexCheckFlg,
					tableGroup,
					tableName);
			
			// マップに登録する
			storeCache(group, facilityId, poller);
			
			return poller;
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * 
	 * @param pollerGroup
	 * @param pollerName
	 */
	public PollingController getPoller(String pollerGroup, String pollerName){
		
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		PollingController cache = getCache(pollerGroup, pollerName);
		return cache;
	}


	/**
	 * 管理しているポーラの情報を文字列で返します。
	 * @return 管理しているテーブルの情報
	 */
	public String getPollerDebugInfo(){
		try {
			_lock.readLock();
			
			String debugStr = "";
			
			Set<CacheKey> keySet = cacheKeys();
	
			for (CacheKey key : keySet) {
				PollingController cache = getCache(key.pollerGroup, key.pollerName);
				
				int minInterval = cache.getPollingConfig().getMinPollingInterval();
				debugStr = debugStr + key.pollerGroup + ", " + key.pollerName + ", " + minInterval+ System.getProperty("line.separator");
				debugStr = debugStr + cache.getPollingConfig().getDebugInfo();
			}
	
			return debugStr;
		} finally {
			_lock.readUnlock();
		}
	}

}
