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

package com.clustercontrol.performance.util;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JdbcBatchExecutor;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.performance.bean.CollectedDataErrorTypeConstant;
import com.clustercontrol.performance.bean.PerfData;
import com.clustercontrol.performance.bean.Sample;
import com.clustercontrol.performance.model.CalculatedDataEntity;

/**
 * 性能情報を登録するユーティティクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class PerformanceDataUtil {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( PerformanceDataUtil.class );

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(PerformanceDataUtil.class.getName());
		
		init();
	}
	
	public static void init() {
		try {
			_lock.writeLock();
			
			HashMap<PerfKey, PerfValue> cache = getCache();
			if (cache == null) {	// not null when clustered
				storeCache(new HashMap<PerfKey, PerfValue>());
			}
		} finally {
			_lock.writeUnlock();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<PerfKey, PerfValue> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_PERFORMANCE_PREVIOUS_VALUE);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_PERFORMANCE_PREVIOUS_VALUE + " : " + cache);
		return cache == null ? null : (HashMap<PerfKey, PerfValue>)cache;
	}
	
	private static void storeCache(HashMap<PerfKey, PerfValue> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_PERFORMANCE_PREVIOUS_VALUE + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_PERFORMANCE_PREVIOUS_VALUE, newCache);
	}

	/**
	 * 性能情報を登録するために Queue に put する
	 *
	 * @param sample 性能情報
	 */
	public static void put(Sample sample) {
		m_log.debug("put() start");

		//  for debug
		if(m_log.isDebugEnabled()){
			m_log.debug("put() collectorId = " + sample.getCollectorId() + ", dateTime = " + sample.getDateTime());
			ArrayList<PerfData> list = sample.getPerfDataList();
			for (PerfData data : list){
				m_log.info("put() list facilityId = " + data.getFacilityId() + ", value = " + data.getValue());
			}
		}

		int useOld = HinemosPropertyUtil.getHinemosPropertyNum("performance.use.old", 660);
		ArrayList<PerfData> list = sample.getPerfDataList();
		List<CalculatedDataEntity> entities = new ArrayList<CalculatedDataEntity>();
		for (PerfData data : list){
			m_log.debug("persist itemCode = " + data.getItemCode());
			// インスタンス生成
			CalculatedDataEntity entity = new CalculatedDataEntity(
					sample.getCollectorId(),
					data.getItemCode(),
					data.getDisplayName(),
					sample.getDateTime() == null ? null : new Timestamp(sample.getDateTime().getTime()),
							data.getFacilityId());

			PerfKey perfKey = new PerfKey(
					sample.getCollectorId(),
					data.getItemCode(),
					data.getDisplayName(),
					data.getFacilityId());

			// 性能値
			if(data.getErrorType() == CollectedDataErrorTypeConstant.NOT_ENOUGH_COLLECT_COUNT
					|| data.getErrorType() == CollectedDataErrorTypeConstant.POLLING_TIMEOUT
					|| data.getErrorType() == CollectedDataErrorTypeConstant.FACILITY_NOT_FOUND
					|| data.getErrorType() == CollectedDataErrorTypeConstant.UNKNOWN
					|| Double.isNaN(data.getValue())){
				/*
				 * 性能値が算出不能だった場合は前の値を利用する。
				 * 660秒以上前のデータは利用しない。
				 */
				try {
					_lock.readLock();
					
					HashMap<PerfKey, PerfValue> cache = getCache();
					PerfValue perfValue = cache.get(perfKey);
					if (sample.getDateTime() != null &&
							perfValue != null &&
							perfValue.getTime() != null &&
							sample.getDateTime().getTime() - perfValue.getTime().getTime()
							< useOld * 1000) {
						m_log.info("insert() : use old perf-data " + perfKey);
						entity.setValue(perfValue.getValue());
					}
				} finally {
					_lock.readUnlock();
				}
			} else {
				// 性能値が算出可能だった場合はそのまま設定する
				entity.setValue(data.getValue());

				try {
					_lock.writeLock();
					
					HashMap<PerfKey, PerfValue> cache = getCache();
					cache.put(perfKey, new PerfValue(sample.getDateTime(), data.getValue()));
					storeCache(cache);
				} finally {
					_lock.writeUnlock();
				}
			}

			entities.add(entity);
		}
		JdbcBatchExecutor.execute(new CalculatedDataEntityJdbcBatchInsert(entities));

		if(m_log.isDebugEnabled()){
			m_log.debug("insert() end : collectorId = " + sample.getCollectorId() + ", dateTime = " + sample.getDateTime());
		}
		m_log.debug("put() end");
	}
}

class PerfKey implements Serializable {
	private String collectorId = null;
	private String itemCode = null;
	private String displayName = null;
	private String facilityId = null;
	public PerfKey (String collectorId, String itemCode, String displayName, String facilityId) {
		this.collectorId = collectorId;
		this.itemCode = itemCode;
		this.displayName = displayName;
		this.facilityId = facilityId;
	}

	@Override
	public boolean equals(Object other) {
		PerfKey otherKey = (PerfKey) other;
		if (!collectorId.equals(otherKey.collectorId)){
			return false;
		}
		if (!itemCode.equals(otherKey.itemCode)){
			return false;
		}
		if (!displayName.equals(otherKey.displayName)){
			return false;
		}
		if (!facilityId.equals(otherKey.facilityId)){
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return collectorId + "," + itemCode + "," + displayName + "," + facilityId;
	}

	@Override
	public int hashCode() {
		int result = collectorId.hashCode();
		result = 37 * result + itemCode.hashCode();
		result = 37 * result + displayName.hashCode();
		result = 37 * result + facilityId.hashCode();
		return result;
	}
}
class PerfValue implements Serializable {
	private Date time = null;
	private double value = 0;
	public PerfValue (Date time, double value) {
		this.time = time;
		this.value = value;
	}
	public Date getTime () {
		return time;
	}
	public double getValue() {
		return value;
	}
}
