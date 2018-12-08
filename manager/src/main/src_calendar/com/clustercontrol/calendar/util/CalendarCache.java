package com.clustercontrol.calendar.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.calendar.bean.CalendarDetailInfo;
import com.clustercontrol.calendar.bean.CalendarInfo;
import com.clustercontrol.calendar.model.CalDetailInfoEntity;
import com.clustercontrol.calendar.model.CalInfoEntity;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.CalendarNotFound;

public class CalendarCache {

	private static Log m_log = LogFactory.getLog( CalendarCache.class );
	
	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(CalendarCache.class.getName());
		
		try {
			_lock.writeLock();
			
			Map<String, CalendarInfo> cache = getCache();
			if (cache == null) {	// not null when clustered
				init();
			}
		} finally {
			_lock.writeUnlock();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static ConcurrentHashMap<String, CalendarInfo> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_CALENDAR);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_CALENDAR + " : " + cache);
		return cache == null ? null : (ConcurrentHashMap<String, CalendarInfo>)cache;
	}
	
	private static void storeCache(ConcurrentHashMap<String, CalendarInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_CALENDAR + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_CALENDAR, newCache);
	}
	
	public static void init() {
		try {
			_lock.writeLock();
			
			storeCache(new ConcurrentHashMap<String, CalendarInfo>());
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * キャッシュをリフレッシュする。
	 * カレンダの登録、変更、削除時に呼ぶ。
	 */
	public static void remove(String id) {
		m_log.info("remove() calendar cache is removed");

		try {
			_lock.writeLock();
			
			ConcurrentHashMap<String, CalendarInfo> cache = getCache();
			cache.remove(id);
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * id一致するCalendarInfoを返す。
	 * 一致しなければ、キャッシュに追加する
	 * @param id
	 * @return
	 * @throws CalendarNotFound
	 */
	public static CalendarInfo getCalendarInfo(String id) throws CalendarNotFound {
		if (id == null) {
			return null;
		}
		
		{
			// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
			// (ConcurrentHashMapの特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
			Map<String, CalendarInfo> cache = getCache();
			CalendarInfo calendar = cache.get(id);
			if (calendar != null) {
				return calendar;
			}
		}
		
		// getCache後からここまでの間に他スレッドによりキャッシュが格納される可能性があり、多重の無駄なキャッシュ格納処理の場合がある。
		// ただし、キャッシュが破損するわけでないため、本方式にて段階的なキャッシングの仕組みを採用する。
		try {
			_lock.writeLock();
			
			ConcurrentHashMap<String, CalendarInfo> cache = getCache();
			CalendarInfo calendar = getCalendarInfoDB(id);
			cache.put(id, calendar);
			storeCache(cache);
			
			return calendar;
		} finally {
			_lock.writeUnlock();
		}
	}
	/**
	 * IDと一致するカレンダ情報一覧をDBより取得します。
	 * @param id
	 * @return
	 * @throws CalendarNotFound
	 */
	private static CalendarInfo getCalendarInfoDB(String id) throws CalendarNotFound{
		//カレンダ取得
		CalInfoEntity entity = null;

		entity = QueryUtil.getCalInfoPK_NONE(id);

		//カレンダ情報のDTOを生成
		CalendarInfo ret = new CalendarInfo();
		//id
		ret.setId(entity.getCalendarId());
		//名前
		ret.setName(entity.getCalendarName());
		//有効期間(From)
		if (entity.getValidTimeFrom() != null) {
			ret.setValidTimeFrom(entity.getValidTimeFrom().getTime());
		}
		//有効期間(To)
		if (entity.getValidTimeTo() != null) {
			ret.setValidTimeTo(entity.getValidTimeTo().getTime());
		}
		//説明
		ret.setDescription(entity.getDescription());
		//登録者
		ret.setRegUser(entity.getRegUser());
		//登録日時
		if (entity.getRegDate() != null) {
			ret.setRegDate(entity.getRegDate().getTime());
		}
		//更新者
		ret.setUpdateUser(entity.getUpdateUser());
		//更新日時
		if (entity.getUpdateDate() != null) {
			ret.setUpdateDate(entity.getUpdateDate().getTime());
		}
		//カレンダ詳細情報
		ArrayList<CalendarDetailInfo> detailList = getCalDetailList(id);
		ret.getCalendarDetailList().addAll(detailList);

		return ret;
	}
	/**
	 * IDと一致するカレンダ詳細情報一覧をDBより取得します。
	 * @param id
	 * @return カレンダ詳細情報のリスト
	 */
	private static ArrayList<CalendarDetailInfo> getCalDetailList(String id) {
		ArrayList<CalendarDetailInfo> list = new ArrayList<CalendarDetailInfo>();

		//カレンダIDの曜日別情報を取得
		List<CalDetailInfoEntity> ct = QueryUtil.getCalDetailByCalendarId(id);

		for (CalDetailInfoEntity cal : ct) {
			CalendarDetailInfo info = new CalendarDetailInfo();
			//説明
			if(cal.getDescription() != null){
				info.setDescription(cal.getDescription());
			}
			//年
			info.setYear(cal.getYearNo());
			//月
			info.setMonth(cal.getMonthNo());
			//曜日選択
			info.setDayType(cal.getDayType());
			//曜日
			if(cal.getWeekNo() != null){
				info.setDayOfWeek(cal.getWeekNo());
			}
			//第x週
			if(cal.getWeekXth() != null){
				info.setDayOfWeekInMonth(cal.getWeekXth());
			}
			//日
			if(cal.getDayNo() != null){
				info.setDate(cal.getDayNo());
			}
			//カレンダパターン
			if(cal.getCalPatternId() != null){
				info.setCalPatternId(cal.getCalPatternId());
			}
			//上記の日程からx日後
			info.setAfterday(cal.getAfterDay());
			//開始時間
			if(cal.getStartTime() != null){
				info.setTimeFrom(cal.getStartTime().getTime());
			}
			//終了時間
			if(cal.getEndTime() != null){
				info.setTimeTo(cal.getEndTime().getTime());
			}
			//稼動・非稼動
			info.setOperateFlg(ValidConstant.typeToBoolean(cal.getExecuteFlg()));

			list.add(info);
		}

		return list;
	}

}
