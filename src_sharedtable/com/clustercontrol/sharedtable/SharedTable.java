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
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.poller.NotInitializedException;

public class SharedTable {
	private static Log m_log = LogFactory.getLog( SharedTable.class );

	private static SharedTable m_instance = new SharedTable();

	// 参照されなくなったデータテーブルの保持期間
	private long m_keepAlive = 60000L;  // 60秒間
	
	/**
	 * データグループ名とデータテーブル名をキーに
	 * データテーブルの集合を管理するマップ
	 * 
	 * Key   : テーブルグループ名とテーブル名を保持するクラス
	 * Value : データテーブルの集合
	 */
	private static final class CacheKey implements Serializable, Comparable<CacheKey> {
		
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
			return String.format("%s [tableGroup = %s, tableName = %s]", CacheKey.class.getName(), tableGroup, tableName);
		}

		@Override
		public int compareTo(CacheKey o){
			int ret;
			ret = this.tableGroup.compareTo( o.tableGroup );
			if( ret != 0 ){
				return ret;
			}
			ret = this.tableName.compareTo( o.tableName );
			if( ret != 0 ){
				return ret;
			}
			return 0;
		}
	}

	@SuppressWarnings("unchecked")
	private static DataTableCollection getCache(String tableGroup, String tableName) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		CacheKey key = new CacheKey(tableGroup, tableName);
		Serializable cache = cm.get(key);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + key + " : " + cache);
		return cache == null ? null : (DataTableCollection)cache;
	}
	
	private static void storeCache(String tableGroup, String tableName, DataTableCollection newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		CacheKey key = new CacheKey(tableGroup, tableName);
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + key + " : " + newCache);
		cm.store(key, newCache);
	}
	
	@SuppressWarnings("unchecked")
	private static DataTableCollection removeCache(String tableGroup, String tableName) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		CacheKey key = new CacheKey(tableGroup, tableName);
		Object cache = cm.remove(key);
		if (m_log.isDebugEnabled()) m_log.debug("remove cache " + key + " : " + cache);
		return cache == null ? null : (DataTableCollection)cache;
	}
	
	private static Set<CacheKey> cacheKeys() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Set<CacheKey> cacheKeys = cm.getKeySet(CacheKey.class);
		return Collections.unmodifiableSet(cacheKeys);
	}

	private SharedTable(){

	}

	/**
	 * インスタンスを返します。
	 * シングルトンであるため返されるインスタンスは常に同じものとなります。
	 * @return SharedTableクラスのインスタンス
	 */
	public static SharedTable getInstance(){
		return m_instance;
	}

	/**
	 * 参照されなくなったデータテーブルの保持期間を設定します。
	 * @param keepAlive 参照されなくなったデータテーブルの保持期間
	 */
	public void setKeepAlive(long keepAlive) {
		m_keepAlive = keepAlive;
	}

	/**
	 * 参照されなくなったデータテーブルの保持期間を返します。
	 * @return 参照されなくなったデータテーブルの保持期間
	 */
	public long getKeepAlive() {
		return m_keepAlive;
	}

	/**
	 * データテーブルを生成します。（既に存在する場合は何もしません）
	 * @param tableGroup テーブルグループ名
	 * @param tableName テーブル名
	 * @param pageSize ページ数
	 */
	public void createDataTable(String tableGroup, String tableName, int pageSize){
		m_log.debug("creat table : " + tableName + "(" + tableGroup + ")");
		
		// ページサイズが1以下の場合はエラーとする
		if (pageSize < 1) {
			throw new IllegalArgumentException();
		}
		
		ILockManager lockManager = LockManagerFactory.instance().create();
		ILock lock = lockManager.create(String.format("%s [%s, %s]", SharedTable.class.getName(), tableGroup, tableName));
		
		try {
			lock.writeLock();
			
			DataTableCollection cache = getCache(tableGroup, tableName);
			
			// 既に存在するかを調べる
			// 指定のものと同じページサイズのものが既にある場合は、新規には生成しない
			if (cache != null && cache.getPageSize() == pageSize) {
				return;
			}
			
			cache = new DataTableCollection(tableGroup, tableName, pageSize, m_keepAlive);
			storeCache(tableGroup, tableName, cache);
		} finally {
			lock.writeUnlock();
		}
	}

	/**
	 * データテーブルを削除します。
	 * @param tableGroup テーブルグループ名
	 * @param tableName テーブル名
	 */
	public void removeDataTable(String tableGroup, String tableName){
		m_log.debug("remove table : " + tableName + "(" + tableGroup + ")");
		
		ILockManager lockManager = LockManagerFactory.instance().create();
		ILock lock = lockManager.create(String.format("%s [%s, %s]", SharedTable.class.getName(), tableGroup, tableName));
		
		try {
			lock.writeLock();
			
			DataTableCollection cache = getCache(tableGroup, tableName);
			
			// データテーブルの集合を管理するクラスをマップから削除
			removeCache(tableGroup, tableName);
		} finally {
			lock.writeUnlock();
		}
	}

	/**
	 * 指定の収集名でデータテーブルにアクセス可能なように設定する
	 * @param tableGroup テーブルグループ
	 * @param tableName テーブル名
	 * @param collectorName 収集名
	 * @param interval 収集間隔
	 * @throws DataTableNotFoundException
	 */
	public void registerCollector(String tableGroup, String tableName, String collectorName, int interval) throws DataTableNotFoundException{
		
		ILockManager lockManager = LockManagerFactory.instance().create();
		ILock lock = lockManager.create(String.format("%s [%s, %s]", SharedTable.class.getName(), tableGroup, tableName));
		
		try {
			lock.writeLock();
			
			DataTableCollection cache = getCache(tableGroup, tableName);
			if (cache == null) {
				DataTableNotFoundException e = new DataTableNotFoundException(tableGroup, tableName);
				m_log.info("registerCollector() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			cache.creatDataTableHolder(collectorName, interval);
			storeCache(tableGroup, tableName, cache);
		} finally {
			lock.writeUnlock();
		}
	}

	/**
	 * 指定の収集名でデータテーブルにアクセス不能なように設定する
	 * @param tableGroup テーブルグループ
	 * @param tableName テーブル名
	 * @param collectorName 収集名
	 * @throws DataTableNotFoundException
	 */
	public void unregisterCollector(String tableGroup, String tableName, String collectorName)
			throws DataTableNotFoundException{
		
		ILockManager lockManager = LockManagerFactory.instance().create();
		ILock lock = lockManager.create(String.format("%s [%s, %s]", SharedTable.class.getName(), tableGroup, tableName));
		
		try {
			lock.writeLock();
			
			DataTableCollection cache = getCache(tableGroup, tableName);
			if (cache == null) {
				DataTableNotFoundException e = new DataTableNotFoundException(tableGroup, tableName);
				m_log.info("unregisterCollector() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			cache.removeCollectorName(collectorName);
			storeCache(tableGroup, tableName, cache);
		} finally {
			lock.writeUnlock();
		}
	}

	/**
	 * データテーブルを挿入します。
	 * checkKeyは、テーブルホルダで管理されているページのキーと同じキーを指定する必要があります。
	 * キーが異なる場合は、テーブルホルダの全てのページがクリアされ、
	 * 今回与えられたデータテーブルのみが挿入された状態となります。
	 * @param tableGroup データグループ名
	 * @param tableName データテーブル名
	 * @param interval 収集間隔
	 * @param dataTable データテーブル
	 * @param checkKey テーブルホルダにデータを格納する際のキー
	 * @throws DataTableNotFoundException
	 */
	public void insertDataTable(String tableGroup, String tableName, int interval, DataTable table, String checkKey)
			throws DataTableNotFoundException{
		ILockManager lockManager = LockManagerFactory.instance().create();
		ILock lock = lockManager.create(String.format("%s [%s, %s]", SharedTable.class.getName(), tableGroup, tableName));
		
		try {
			lock.writeLock();
			
			DataTableCollection cache = getCache(tableGroup, tableName);
			if (cache == null) {
				// エラー処理
				throw new DataTableNotFoundException(tableGroup, tableName);
			}
			
			try {
				// m_log.debug("insert DataTable : " + tableGroup + ", " + tableName);
				cache.insertDataTable(interval, table, checkKey);
			} catch (NotInitializedException e) {
				// エラー処理
				throw new DataTableNotFoundException(tableGroup, tableName);
			}
			
			storeCache(tableGroup, tableName, cache);
		} finally {
			lock.writeUnlock();
		}
	}

	/**
	 * 指定のデータ格納テーブルが存在するか確認します。
	 * @param tableGroup テーブルグループ
	 * @param tableName テーブル名
	 * @return 存在する場合はtrue
	 */
	public boolean containsDataTable(String tableGroup, String tableName){
		ILockManager lockManager = LockManagerFactory.instance().create();
		ILock lock = lockManager.create(String.format("%s [%s, %s]", SharedTable.class.getName(), tableGroup, tableName));
		
		try {
			lock.readLock();
		
			DataTableCollection cache = getCache(tableGroup, tableName);
			return cache != null;
		} finally {
			lock.readUnlock();
		}
	}

	/**
	 * 指定のデータ格納テーブルが存在するか確認します。
	 * @param tableGroup データグループ名
	 * @param tableName データテーブル名
	 * @return 存在する場合はtrue
	 */
	public boolean containsTable(
			String tableGroup,
			String tableName,
			int pageSize){
		
		ILockManager lockManager = LockManagerFactory.instance().create();
		ILock lock = lockManager.create(String.format("%s [%s, %s]", SharedTable.class.getName(), tableGroup, tableName));
		
		try {
			lock.readLock();
			DataTableCollection cache = getCache(tableGroup, tableName);
			if (cache == null) {
				return false;
			}
			
			return cache.getPageSize() == pageSize;
		} finally {
			lock.readUnlock();
		}
	}

	/**
	 * 指定の収集名用のデータ格納テーブルが存在するか確認します。
	 * @param tableGroup データグループ名
	 * @param tableName データテーブル名
	 * @param collectorName 収集名
	 * @return 存在する場合はtrue
	 * @throws DataTableNotFoundException
	 */
	public boolean containsCollectorName(
			String tableGroup,
			String tableName,
			String collectorName) throws DataTableNotFoundException{
		
		ILockManager lockManager = LockManagerFactory.instance().create();
		ILock lock = lockManager.create(String.format("%s [%s, %s]", SharedTable.class.getName(), tableGroup, tableName));
		
		try {
			lock.readLock();
			
			DataTableCollection cache = getCache(tableGroup, tableName);
			if (cache == null) {
				DataTableNotFoundException e = new DataTableNotFoundException(tableGroup, tableName);
				m_log.info("containsCollectorName() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			return cache.containsCollectorName(collectorName);
		} finally {
			lock.readUnlock();
		}
	}

	/**
	 * データテーブルは、10秒間隔での監視用、30秒間隔での監視用...
	 * と監視間隔ごとに複数のテーブルを保持する
	 * 
	 * 指定のデータテーブルが保持しているテーブルホルダのうち
	 * 存在する収集間隔のセットを返す。
	 * 
	 * @param tableGroup データグループ名
	 * @param tableName データテーブル名
	 * @return 収集間隔のセット
	 * @throws DataTableNotFoundException
	 */
	public Set<Integer> getIntervals(String tableGroup,	String tableName)
			throws DataTableNotFoundException{
		
		ILockManager lockManager = LockManagerFactory.instance().create();
		ILock lock = lockManager.create(String.format("%s [%s, %s]", SharedTable.class.getName(), tableGroup, tableName));
		
		try {
			lock.readLock();
			
			DataTableCollection cache = getCache(tableGroup, tableName);
			if (cache == null) {
				throw new DataTableNotFoundException(tableGroup, tableName);
			}
			
			return cache.getIntervals();
		} finally {
			lock.readUnlock();
		}
	}

	/**
	 * 設定されている収集名のセットを取得します
	 * @return 設定されている収集名のセット
	 * @throws DataTableNotFoundException
	 */
	public Set<String> getCollectorNames(String tableGroup,	String tableName)
			throws DataTableNotFoundException{
		
		ILockManager lockManager = LockManagerFactory.instance().create();
		ILock lock = lockManager.create(String.format("%s [%s, %s]", SharedTable.class.getName(), tableGroup, tableName));
		
		try {
			lock.readLock();
			
			DataTableCollection cache = getCache(tableGroup, tableName);
			if (cache == null) {
				throw new DataTableNotFoundException(tableGroup, tableName);
			}
			
			return cache.getCollectorNames();
		} finally {
			lock.readUnlock();
		}
	}

	/**
	 * データテーブルを取得します。
	 * 
	 * @param tableGroup データグループ名
	 * @param tableName データテーブル名
	 * @param collectorName 収集名
	 * @param pageIndex ページ番号
	 * @return データテーブル
	 * @throws DataTableNotFoundException
	 * @throws NotInitializedException
	 */
	public DataTable getDataTable(
			String tableGroup,
			String tableName,
			String collectorName,
			int pageIndex)
					throws DataTableNotFoundException, NotInitializedException{
		DataTableCollection cache = null;
		
		ILockManager lockManager = LockManagerFactory.instance().create();
		ILock lock = lockManager.create(String.format("%s [%s, %s]", SharedTable.class.getName(), tableGroup, tableName));
		
		try {
			lock.readLock();
			
			cache = getCache(tableGroup, tableName);
			if (cache == null) {
				DataTableNotFoundException e = new DataTableNotFoundException(tableGroup, tableName);
				m_log.info("getDataTable() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			DataTableHolder tableHolder = cache.getDataTableHolder(collectorName);
	
			if (tableHolder == null) {
				NotInitializedException e = new NotInitializedException(
						collectorName + " is not registered in " + tableName + "(" + tableGroup + ")");
				m_log.info("getDataTable() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
	
			return tableHolder.get(pageIndex);
		} finally {
			lock.readUnlock();
		}
	}


	/**
	 * データテーブルを取得します。
	 * 
	 * @param tableGroup データグループ名
	 * @param tableName データテーブル名
	 * @param collectorName 収集名
	 * @param pageSize ページ数
	 * @return データテーブル
	 * @throws DataTableNotFoundException
	 * @throws NotInitializedException
	 */
	public List<DataTable> getLastDataTables(
			String tableGroup,
			String tableName,
			String collectorName,
			int pageSize)
					throws DataTableNotFoundException, NotInitializedException{
		DataTableCollection cache = null;
		
		ILockManager lockManager = LockManagerFactory.instance().create();
		ILock lock = lockManager.create(String.format("%s [%s, %s]", SharedTable.class.getName(), tableGroup, tableName));
		
		try {
			lock.readLock();

			cache = getCache(tableGroup, tableName);
			if (cache == null) {
				throw new DataTableNotFoundException(tableGroup, tableName);
			}
			
			DataTableHolder tableHolder = cache.getDataTableHolder(collectorName);
			
			if (tableHolder == null) {
				throw new DataTableNotFoundException(tableGroup, tableName);
			}
			
			long insertCount = tableHolder.getInsertCount();
	
			if (m_log.isDebugEnabled()) {
				m_log.debug(collectorName + ", " + tableName + "(" + tableGroup + ")  "
						+ "request page size=" + pageSize + " insert page="+ insertCount);
			}
	
			// 指定のページ数分まだDataTableが挿入されていない場合
			if (insertCount < pageSize) {
				throw new NotInitializedException(
						" Not enough page. " + collectorName + ", " + tableName + "(" + tableGroup + ")");
			}
	
			return tableHolder.getLast(pageSize);
		} finally {
			lock.readUnlock();
		}
	}

	/**
	 * 管理しえているデータテーブル全てをチェックし、
	 * 一定期間（収集間隔 ＋ 生存期間（m_keepAlive））以上経過しているものは削除する
	 */
	public void checkUnnecessaryTable() {
		
		long now = System.currentTimeMillis();
		
		Set<CacheKey> keySet = cacheKeys();
		for (CacheKey key : keySet) {
			
			ILockManager lockManager = LockManagerFactory.instance().create();
			ILock lock = lockManager.create(String.format("%s [%s, %s]", SharedTable.class.getName(), key.tableGroup, key.tableName));
			
			try {
				lock.writeLock();
				
				DataTableCollection cache = getCache(key.tableGroup, key.tableName);
				
				if (cache != null) {
					// チェックを実行
					cache.checkAlive(now);
					storeCache(key.tableGroup, key.tableName, cache);
				}
			} finally {
				lock.writeUnlock();
			}
		}
	}

	/**
	 * 管理しているテーブルの情報を文字列で返します。
	 * @return 管理しているテーブルの情報
	 */
	public String getTableListDebugInfo(){
		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");

		String debugStr = "";

		Set<CacheKey> keys = cacheKeys();
		
		// 各テーブルに関しての情報を下記フォーマットで出力
		// テーブルグループ    テーブル名    ページサイズ    (収集間隔, 収集間隔, 収集間隔...)
		//     収集名    収集間隔    最終参照日時
		//     収集名    収集間隔    最終参照日時
		//     収集名    収集間隔    最終参照日時
		//  
		for (CacheKey key : keys) {
			ILockManager lockManager = LockManagerFactory.instance().create();
			ILock lock = lockManager.create(String.format("%s [%s, %s]", SharedTable.class.getName(), key.tableGroup, key.tableName));
			
			try {
				lock.readLock();
				
				DataTableCollection cache = getCache(key.tableGroup, key.tableName);
				
				// 取得できなかった場合は、次のテーブルに処理を進める
				if (cache == null) {
					break;
				}
				
				Set<String> collectorNames = new HashSet<String>();
				
				try {
					collectorNames = getCollectorNames(key.tableGroup, key.tableName);
				} catch (DataTableNotFoundException e) { }
	
				String txt = key.tableGroup + ", " + key.tableName + ", page size = " + cache.getPageSize();
	
				// 保持しているテーブルの収集間隔を出力
				txt = txt + ", interval = (";
				TreeSet<Integer> intervals = new TreeSet<Integer>(cache.getIntervals());
				Iterator<Integer> itrInter = intervals.iterator();
				while(itrInter.hasNext()){
					int interval = itrInter.next();
					txt = txt + interval;
					if(itrInter.hasNext()){
						txt = txt + ", ";
					}
				}
				txt = txt + ")\n";
	
				// 各収集名に関しての情報を下記フォーマットで出力
				//     収集名    収集間隔    最終参照日時
				String collectorNameList = "";
				for(String collectorName : collectorNames){
					DataTableHolder holder = cache.getDataTableHolder(collectorName);
					
					collectorNameList = collectorNameList + "\t"
							+ collectorName + "\t"
							+ cache.getInterval(collectorName) + "\t" + " last ref \t"
							+ formatter.format(new Date(cache.getLastReference(collectorName)))
							+ "\t insert:" + holder.getInsertCount()
							+ "\n";
	
					// 各収集間隔ごとに管理されているDataTableHolderの各ページの更新時刻を出力
					String holderInfoTxt = "";
					if(holder != null){
						// 各ページの最終更新時刻を取得します
						for(int i=0; i<holder.getPageSize(); i++){
							DataTable table = holder.get(i);
							if(table != null){
								holderInfoTxt = holderInfoTxt + "\t\t"
										+ "page:" + i
										+ "\t" + " create \t" + formatter.format(new Date(table.getCreateTime()))
										+ "\t" + " last modify \t" + formatter.format(new Date(table.getLastModify()))
										+ "\n";
							}
						}
					}
	
					collectorNameList = collectorNameList + holderInfoTxt;
				}
	
				txt = txt + collectorNameList + "\n";
	
				debugStr = debugStr + txt;
			} finally {
				lock.readUnlock();
			}
		}

		return debugStr;
	}
	
}

