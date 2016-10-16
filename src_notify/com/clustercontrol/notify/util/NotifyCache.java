package com.clustercontrol.notify.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.notify.bean.NotifyInfo;
import com.clustercontrol.notify.bean.NotifyInfoDetail;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.notify.model.NotifyCommandInfoEntity;
import com.clustercontrol.notify.model.NotifyEventInfoEntity;
import com.clustercontrol.notify.model.NotifyInfoEntity;
import com.clustercontrol.notify.model.NotifyJobInfoEntity;
import com.clustercontrol.notify.model.NotifyLogEscalateInfoEntity;
import com.clustercontrol.notify.model.NotifyMailInfoEntity;
import com.clustercontrol.notify.model.NotifyStatusInfoEntity;

public class NotifyCache {
	private static Log m_log = LogFactory.getLog( NotifyCache.class );

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(NotifyCache.class.getName());
		
		try {
			_lock.writeLock();
			
			HashMap<String, NotifyInfo> notifyInfoCache = getNotifyInfoCache();
			HashMap<String, NotifyInfoDetail> notifyDetailCache = getNotifyDetailCache();
			
			if (notifyInfoCache == null || notifyDetailCache == null)  {	// not null when clustered
				refresh();
			}
		} finally {
			_lock.writeUnlock();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, NotifyInfo> getNotifyInfoCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_NOTIFY_INFO);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_NOTIFY_INFO + " : " + cache);
		return cache == null ? null : (HashMap<String, NotifyInfo>)cache;
	}
	
	private static void storeNotifyInfoCache(HashMap<String, NotifyInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_NOTIFY_INFO + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_NOTIFY_INFO, newCache);
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, NotifyInfoDetail> getNotifyDetailCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_NOTIFY_DETAIL);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_NOTIFY_DETAIL + " : " + cache);
		return cache == null ? null : (HashMap<String, NotifyInfoDetail>)cache;
	}
	
	private static void storeNotifyDetailCache(HashMap<String, NotifyInfoDetail> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_NOTIFY_DETAIL + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_NOTIFY_DETAIL, newCache);
	}

	/**
	 * キャッシュをリフレッシュする。
	 * 通知の登録、変更、削除時に呼ぶ。
	 */
	public static void refresh() {
		try {
			_lock.writeLock();
			
			long start = System.currentTimeMillis();
			HashMap<String, NotifyInfo> notifyMap = new HashMap<String, NotifyInfo>();
			HashMap<String, NotifyInfoDetail> notifyDetailMap = new HashMap<String, NotifyInfoDetail>();
			
			List<NotifyInfoEntity> c = QueryUtil.getAllNotifyInfo_NONE();
			for (NotifyInfoEntity entity : c) {
				NotifyInfo info = new NotifyInfo();
				String notifyId = entity.getNotifyId();
				info.setNotifyId(notifyId);
				// info.setDescription(local.getDescription());
				info.setNotifyType(entity.getNotifyType());
				info.setInitialCount(entity.getInitialCount());
				info.setNotFirstNotify(entity.getNotFirstNotify());
				info.setRenotifyType(entity.getRenotifyType());
				info.setRenotifyPeriod(entity.getRenotifyPeriod());
				// info.setRegDate(local.getRegDate().getTime());
				// info.setUpdateDate(local.getUpdateDate().getTime());
				// info.setRegUser(local.getRegUser());
				// info.setUpdateUser(local.getUpdateUser());
				info.setValidFlg(entity.getValidFlg());
				info.setCalendarId(entity.getCalendarId());
				notifyMap.put(notifyId, info);

				m_log.debug("refresh() notifyInfo(notifyId=" + notifyId + ") cache is refreshed");

				NotifyInfoDetail notifyInfoDetail = new NotifyInfoDetail();
				notifyInfoDetail.setNotifyId(notifyId);
				setValidFlgtoDetail(entity, notifyInfoDetail);
				notifyDetailMap.put(notifyId, notifyInfoDetail);
			}
			
			m_log.info("refresh NotifyCache " + (System.currentTimeMillis() - start) + "ms. size=" + notifyDetailMap.size());
			
			storeNotifyInfoCache(notifyMap);
			storeNotifyDetailCache(notifyDetailMap);
		} catch (Exception e) {
			m_log.warn("refresh() create notifyCache failed : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.writeUnlock();
		}
	}


	/**
	 * NotifyInfoをキャッシュから取得する。
	 * NotifyInfoは性能向上のため、メンバ変数の一部が欠けているため注意すること。
	 * 
	 * @param notifyId
	 * @return
	 */
	public static NotifyInfo getNotifyInfo(String notifyId) {
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		HashMap<String, NotifyInfo> notifyMap = getNotifyInfoCache();
		NotifyInfo notifyInfo = notifyMap.get(notifyId);
		if (notifyInfo == null) {
			m_log.info("getNotifyInfo() notifyInfo (notifyId=" + notifyId + ") is null");
			m_log.debug("getNotifyInfo() notifyInfo map size=" + notifyMap.size());
		}
		return notifyInfo;
	}

	/**
	 * NotifyInfoDetailをキャッシュから取得する。
	 * 
	 * @param notifyId
	 * @return
	 */
	public static NotifyInfoDetail getNotifyInfoDetail(String notifyId) {
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		HashMap<String, NotifyInfoDetail> notifyDetailMap = getNotifyDetailCache();
		NotifyInfoDetail notifyInfoDetail = notifyDetailMap.get(notifyId);
		if (notifyInfoDetail == null) {
			m_log.info("getNotifyInfoDetail() notifyInfoDetail is null");
		}
		return notifyInfoDetail;
	}

	/**
	 * 重要度毎の有効無効フラグを設定します。
	 *
	 */
	private static void setValidFlgtoDetail(NotifyInfoEntity entity, NotifyInfoDetail detail) {
		switch (entity.getNotifyType()) {
		case NotifyTypeConstant.TYPE_EVENT:
			NotifyEventInfoEntity event = entity.getNotifyEventInfoEntity();
			detail.setInfoValidFlg(event.getInfoEventNormalFlg());
			detail.setWarnValidFlg(event.getWarnEventNormalFlg());
			detail.setCriticalValidFlg(event.getCriticalEventNormalFlg());
			detail.setUnknownValidFlg(event.getUnknownEventNormalFlg());
			break;

		case NotifyTypeConstant.TYPE_STATUS:
			NotifyStatusInfoEntity status = entity.getNotifyStatusInfoEntity();
			detail.setInfoValidFlg(status.getInfoStatusFlg());
			detail.setWarnValidFlg(status.getWarnStatusFlg());
			detail.setCriticalValidFlg(status.getCriticalStatusFlg());
			detail.setUnknownValidFlg(status.getUnknownStatusFlg());
			break;

		case NotifyTypeConstant.TYPE_MAIL:
			NotifyMailInfoEntity mail = entity.getNotifyMailInfoEntity();
			detail.setInfoValidFlg(mail.getInfoMailFlg());
			detail.setWarnValidFlg(mail.getWarnMailFlg());
			detail.setCriticalValidFlg(mail.getCriticalMailFlg());
			detail.setUnknownValidFlg(mail.getUnknownMailFlg());
			break;

		case NotifyTypeConstant.TYPE_JOB:
			NotifyJobInfoEntity job = entity.getNotifyJobInfoEntity();
			detail.setInfoValidFlg(job.getInfoJobRun());
			detail.setWarnValidFlg(job.getWarnJobRun());
			detail.setCriticalValidFlg(job.getCriticalJobRun());
			detail.setUnknownValidFlg(job.getUnknownJobRun());
			break;

		case NotifyTypeConstant.TYPE_LOG_ESCALATE:
			NotifyLogEscalateInfoEntity log = entity.getNotifyLogEscalateInfoEntity();
			detail.setInfoValidFlg(log.getInfoEscalateFlg());
			detail.setWarnValidFlg(log.getWarnEscalateFlg());
			detail.setCriticalValidFlg(log.getCriticalEscalateFlg());
			detail.setUnknownValidFlg(log.getUnknownEscalateFlg());
			break;

		case NotifyTypeConstant.TYPE_COMMAND:
			NotifyCommandInfoEntity command = entity.getNotifyCommandInfoEntity();
			detail.setInfoValidFlg(command.getInfoValidFlg());
			detail.setWarnValidFlg(command.getWarnValidFlg());
			detail.setCriticalValidFlg(command.getCriticalValidFlg());
			detail.setUnknownValidFlg(command.getUnknownValidFlg());
			break;

		default:
			m_log.info("setValidFlgtoDetail() notify type mismatch. " + entity.getNotifyId() + "  type = " + entity.getNotifyType());
		}
	}
}
