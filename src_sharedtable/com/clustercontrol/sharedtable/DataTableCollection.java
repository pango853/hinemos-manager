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
package com.clustercontrol.sharedtable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.poller.NotInitializedException;

/**
 * 複数のデータホルダを管理するクラスです。
 * データホルダは、収集間隔ごとに保持されます。
 */
public class DataTableCollection implements Serializable {
	private static Log m_log = LogFactory.getLog( DataTableCollection.class );
	
	private final String tableGroup;
	private final String tableName;
	
	/**
	 * 管理するデータホルダのページサイズ
	 */
	private final int m_pageSize;

	/**
	 * 参照されなくなったデータテーブルの保持期間
	 */
	private long m_keepAlive = 60000L;  // 60秒間

	/**
	 * 収集間隔ごとにDataTableHolderを保持する
	 * 
	 * Key   : 収集間隔
	 * Value : 収集間隔ごとにDataTableを複数まとめて管理するクラス
	 */
	private HashMap<Integer, DataTableHolder> m_tableHolderMap;
	
	
	
	private static MetaData getCache(String tableGroup, String tableName, String collectionName) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		CacheKey key = new CacheKey(tableGroup, tableName);
		Map<String, MetaData> map = (ConcurrentHashMap<String, MetaData>)cm.get(key);

		Serializable cache = null;
		if( null != map ){
			cache = map.get( collectionName );
		}
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + key + " : " + cache);
		return cache == null ? null : (MetaData)cache;
	}
	
	private static final Object mapLock = new Object();
	private static void storeCache(String tableGroup, String tableName, String collectionName, MetaData newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		CacheKey key = new CacheKey(tableGroup, tableName);

		synchronized(mapLock){
			ConcurrentHashMap<String, MetaData> map = (ConcurrentHashMap<String, MetaData>)cm.get(key);
			if(null == map){
				map = new ConcurrentHashMap<>();
			}
			map.put(collectionName, newCache);
	
			if (m_log.isDebugEnabled()) m_log.debug("store cache [" + key + ", " + collectionName + "] : " + newCache);
			cm.store(key, map);
		}
	}
	
	private static MetaData removeCache(String tableGroup, String tableName, String collectionName) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		CacheKey key = new CacheKey(tableGroup, tableName);

		Serializable cache = null;
		synchronized(mapLock){
			ConcurrentHashMap<String, MetaData> map = (ConcurrentHashMap<String, MetaData>)cm.get(key);
			if(null == map){
				return null;
			}
	
			cache = map.remove( collectionName );
			if(map.size() == 0){
				cm.remove(key);
			}else{
				cm.store(key, map);
			}
		}

		if (m_log.isDebugEnabled()) m_log.debug("get cache [" + key + ", " + collectionName + "] : " + cache);
		return cache == null ? null : (MetaData)cache;
	}

	private static Map<String, MetaData> getCacheMap(String tableGroup, String tableName){
		CacheKey key = new CacheKey(tableGroup, tableName);

		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable map = cm.get(key);
		return map == null ? null : (Map<String, MetaData>)map;
	}

	private static class CacheKey implements Serializable, Comparable<CacheKey> {
		
		public final String key = AbstractCacheManager.KEY_COMMON_SHAREDTABLE;
		
		public final String tableGroup;
		public final String tableName;
		
		public CacheKey(String tableGroup, String tableName) {
			this.tableGroup = tableGroup;
			this.tableName = tableName;
		}
		
		@Override
		public int hashCode() {
			int h = 1;
			h = h * 31 + tableGroup == null ? 0 : tableGroup.hashCode();
			h = h * 31 + tableName == null ? 0 : tableName.hashCode();
			return h;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			
			if (obj instanceof CacheKey) {
				CacheKey cast = (CacheKey)obj;
				if (tableGroup != null && tableName != null) {
					return tableGroup.equals(cast.tableGroup) && tableName.equals(cast.tableName);
				}
			}
			
			return false;
		}
		
		@Override
		public String toString() {
			return String.format("%s [tableGroup = %s, tableName = %s]",
					CacheKey.class.getName(), tableGroup, tableName);
		}

		@Override
		public int compareTo( CacheKey o ){
			int ret = 0;
			ret = tableGroup.compareTo(o.tableGroup);
			if (ret != 0) {
				return ret;
			}
			ret = tableName.compareTo(o.tableName);
			if (ret != 0) {
				return ret;
			}
			return 0;
		}
	}

	private static class MetaData implements Serializable {
		public final int interval;
		public final long lastReference;
		
		public MetaData(int interval, long lastReference) {
			this.interval = interval;
			this.lastReference = lastReference;
		}
		
		@Override
		public int hashCode() {
			int h = 1;
			h = h * 31 + interval;
			h = h * 31 + (int)(lastReference & 0xFFFFFFFF);
			return h;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			
			if (obj instanceof MetaData) {
				MetaData cast = (MetaData)obj;
				return interval == cast.interval && lastReference == cast.lastReference;
			}
			
			return false;
		}
		
		@Override
		public String toString() {
			return String.format("%s [interval = %d, lastReference = %d]", 
					MetaData.class.getName(), interval, lastReference);
		}
	}
	
	
	/**
	 * コンストラクタ
	 * @param pageSize ページサイズ
	 * @param keepAlive 参照されなくなったデータテーブルの保持期間
	 */
	protected DataTableCollection(String tableGroup, String tableName, int pageSize, long keepAlive){
		this.tableGroup = tableGroup;
		this.tableName = tableName;
		m_pageSize = pageSize;
		m_keepAlive = keepAlive;
		m_tableHolderMap = new HashMap<Integer, DataTableHolder>();
	}

	/**
	 * 指定の収集名で指定の収集期間で参照、更新されるテーブルホルダを作成する
	 * @param collectorName 収集名
	 * @param interval 収集間隔
	 */
	protected void creatDataTableHolder(String collectorName, int interval){
		m_log.debug("Create DataTableHolder : " + collectorName);
		
		storeCache(tableGroup, tableName, collectorName, new MetaData(interval, System.currentTimeMillis()));
		
		// 指定の収集期間のテーブルホルダが存在しない場合は作成する
		if(m_tableHolderMap.get(interval) == null){
			DataTableHolder holder = new DataTableHolder(m_pageSize);
			m_tableHolderMap.put(interval, holder);
		}
	}

	/**
	 * 指定の収集名の設定を削除する。
	 * 収集名を削除することで、参照されなくなるテーブルホルダも同時に削除する
	 * @param collectorName 収集名
	 */
	protected void removeCollectorName(String collectorName){
		
		CacheKey key = new CacheKey(tableGroup, tableName);
		MetaData cache = getCache(tableGroup, tableName, collectorName);
		
		if (cache == null) {
			return;
		}
		
		// 現在登録されている全ての収集間隔を走査し、
		// 今回収集名に関連する収集間隔を削除することで、
		// 参照されなくなるテーブルホルダが存在するか否かを調べる
		boolean deleteInterval = true;
		Set<String> collectorNames = getCollectorNames();
		for (String collectorName2 : collectorNames) {
			if (! collectorName.equals(collectorName2)) {
				MetaData cache2 = getCache(tableGroup, tableName, collectorName2);
				if (cache.interval == cache2.interval) {
					deleteInterval = false;
					break;
				}
			}
		}
		
		// 指定の収集名の情報を削除する
		removeCache(tableGroup, tableName, collectorName);
		
		// テーブルホルダを削除する
		if (deleteInterval){
			m_log.debug("Remove DataTableHolder : " + cache.interval);
			m_tableHolderMap.remove(cache.interval);
		}
	}

	/**
	 * 指定の収集名で登録されているデータホルダが存在するか確認する。
	 * @param collectorName 収集名
	 * @return 存在する場合はtrue
	 */
	protected boolean containsCollectorName(String collectorName){
		MetaData cache = getCache(tableGroup, tableName, collectorName);
		return cache == null ? false : m_tableHolderMap.containsKey(cache.interval);
	}

	protected Set<Integer> getIntervals(){
		return m_tableHolderMap.keySet();
	}

	protected int getInterval(String collectorName){
		MetaData cache = getCache(tableGroup, tableName, collectorName);
		return cache == null ? -1 : cache.interval;
	}

	protected long getLastReference(String collectorName){
		MetaData cache = getCache(tableGroup, tableName, collectorName);
		return cache == null ? -1 : cache.lastReference;
	}

	protected int getPageSize() {
		return m_pageSize;
	}

	protected DataTableHolder getDataTableHolder(String collectorName){
		MetaData cache = getCache(tableGroup, tableName, collectorName);
		
		if (cache == null) {
			return null;
		}
		
		storeCache(tableGroup, tableName, collectorName, new MetaData(cache.interval, System.currentTimeMillis()));
		return cache == null ? null : m_tableHolderMap.get(cache.interval);
	}

	/**
	 * 設定されている収集名のセットを取得します
	 * @return 設定されている収集名のセット
	 */
	protected Set<String> getCollectorNames(){
		Map<String, MetaData> map = getCacheMap( tableGroup, tableName );
		return (null == map) ? new HashSet<String>(): map.keySet();
	}

	/**
	 * 収集間隔ごとに管理されているDataTableに新たなテーブルを挿入する。
	 * checkKeyは、テーブルホルダで管理されているページのキーと同じキーを指定する必要があります。
	 * キーが異なる場合は、テーブルホルダの全てのページがクリアされ、
	 * 今回与えられたデータテーブルのみが挿入された状態となります。
	 * 
	 * @param interval 収集間隔
	 * @param table 挿入するテーブル
	 * @param checkKey テーブルホルダにデータを格納する際のキー
	 * @throws NotInitializedException
	 */
	protected void insertDataTable(int interval, DataTable table, String checkKey)
			throws NotInitializedException{
		DataTableHolder holder = m_tableHolderMap.get(interval);

		if(holder == null){
			// エラー処理
			throw new NotInitializedException("DataTableHolder is not found.");
		}

		holder.insertDataTable(table, checkKey);
	}

	/**
	 * 収集名ごとに管理されているテーブルホルダのうち
	 * 最終参照時刻から生存期間以上経過しているものを削除する
	 * 
	 * @param now 基準時刻
	 * @return 管理している全てのテーブルが削除された場合にはfalseを返す
	 */
	protected boolean checkAlive(long now){
		m_log.debug("DataTableCollection check : " + now);

		// 削除対象の収集名を保持するリスト
		ArrayList<String> removeList = new ArrayList<String>();

		Set<String> collectorNames = getCollectorNames();
		Iterator<String> itr = collectorNames.iterator();

		while(itr.hasNext()){
			// 収集名
			String collcetorName = itr.next();
			
			MetaData cache = getCache(tableGroup, tableName, collcetorName);

			// 現在チェックしている収集名の収集間隔と最終参照時刻を取得する
			int interval = cache.interval;
			long lastRef = cache.lastReference;

			// 収集間隔2回分 ＋ 保持時間 を超えたものは削除対処
			long lastMoment = (lastRef + (interval * 1000L) * 2 + m_keepAlive);
			if(m_log.isDebugEnabled()){
				m_log.debug(collcetorName + " : " + "lastMoment=" + new Date(lastMoment));
			}

			if(now  >= lastMoment){
				m_log.debug("remove target = " + collcetorName);
				removeList.add(collcetorName);
			}
		}

		// 削除対象がある場合
		if(removeList.size() > 0){
			// 削除する
			itr = removeList.iterator();
			while(itr.hasNext()){
				String collcetorName = itr.next();

				MetaData cache = getCache(tableGroup, tableName, collcetorName);
				
				m_log.info("remove CollectorName : " + collcetorName + ", "
						+ cache.interval);
				
				removeCache(tableGroup, tableName, collcetorName);
			}

			// 今後も参照される可能性のある参照間隔を保持する
			HashSet<Integer> activeIntrevalSet = new HashSet<Integer>();

			collectorNames = getCollectorNames();
			itr = collectorNames.iterator();

			while(itr.hasNext()){
				String collcetorName = itr.next();
				MetaData cache = getCache(tableGroup, tableName, collcetorName);

				activeIntrevalSet.add(cache.interval);
			}

			// 削除対象の収集間隔を求める
			// 現在登録されている収集間隔を全てセット
			Set<Integer> removeIntrevalSet = new HashSet<Integer>(m_tableHolderMap.keySet());

			// 今後も参照されるものは除く
			removeIntrevalSet.removeAll(activeIntrevalSet);

			// 削除する
			Iterator<Integer> intItr = removeIntrevalSet.iterator();
			while(intItr.hasNext()){
				int interval = intItr.next();

				m_log.debug("remove Interval : " + interval);

				m_tableHolderMap.remove(interval);
			}
		}
		return (m_tableHolderMap.size() == 0);
	}
}
