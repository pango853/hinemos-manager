/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.systemlog.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.ProcessConstant;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.calendar.factory.SelectCalendar;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.run.bean.MonitorInfo;
import com.clustercontrol.monitor.run.bean.MonitorStringValueInfo;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.systemlog.bean.SyslogMessage;
import com.clustercontrol.systemlog.util.SyslogHandler;

public class SystemLogMonitor implements SyslogHandler{

	private static final Log log = LogFactory.getLog(SystemLogMonitor.class);

	private ExecutorService _executor;
	private SystemLogNotifier _notifier = new SystemLogNotifier();

	private final int _threadSize;
	private final int _queueSize;

	private long receivedCount = 0;
	private long discardedCount = 0;
	private long notifiedCount = 0;

	public SystemLogMonitor(int threadSize, int queueSize) {
		_threadSize = threadSize;
		_queueSize = queueSize;
	}

	@Override
	public synchronized void syslogReceived(List<SyslogMessage> syslogList) {
		String _receiverId = HinemosPropertyUtil.getHinemosPropertyStr("monitor.systemlog.receiverid", System.getProperty("hinemos.manager.nodename"));
		countupReceived();
		_executor.execute(new SystemLogMonitorTask(_receiverId, syslogList));
	}
	
	public synchronized void syslogReceivedSync(List<SyslogMessage> syslogList) {
		String _receiverId = HinemosPropertyUtil.getHinemosPropertyStr("monitor.systemlog.receiverid", System.getProperty("hinemos.manager.nodename"));
		countupReceived();
		new SystemLogMonitorTask(_receiverId, syslogList).run();
	}

	private synchronized void countupReceived() {
		receivedCount = receivedCount >= Long.MAX_VALUE ? 0 : receivedCount + 1;
		int _statsInterval = HinemosPropertyUtil.getHinemosPropertyNum("monitor.systemlog.stats.interval", 1000);
		if (receivedCount % _statsInterval == 0) {
			log.info("The number of syslog (received) : " + receivedCount);
		}
	}

	private synchronized void countupDiscarded() {
		discardedCount = discardedCount >= Long.MAX_VALUE ? 0 : discardedCount + 1;
		int _statsInterval = HinemosPropertyUtil.getHinemosPropertyNum("monitor.systemlog.stats.interval", 1000);
		if (discardedCount % _statsInterval == 0) {
			log.info("The number of syslog (discarded) : " + discardedCount);
		}
	}

	private synchronized void countupNotified() {
		notifiedCount = notifiedCount >= Long.MAX_VALUE ? 0 : notifiedCount + 1;
		int _statsInterval = HinemosPropertyUtil.getHinemosPropertyNum("monitor.systemlog.stats.interval", 1000);
		if (notifiedCount % _statsInterval == 0) {
			log.info("The number of syslog (notified) : " + notifiedCount);
		}
	}

	public long getReceivedCount() {
		return receivedCount;
	}

	public long getDiscardedCount() {
		return discardedCount;
	}

	public long getNotifiedCount() {
		return notifiedCount;
	}

	public int getQueuedCount() {
		return ((ThreadPoolExecutor)_executor).getQueue().size();
	}

	@Override
	public synchronized void start() {
		_executor = new MonitoredThreadPoolExecutor(_threadSize, _threadSize,
				0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(_queueSize),
				new ThreadFactory() {
			private volatile int _count = 0;
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "SystemLogFilter-" + _count++);
			}
		}, new SystemLogRejectionHandler());
	}

	private class SystemLogRejectionHandler extends ThreadPoolExecutor.DiscardPolicy {

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
			if (r instanceof SystemLogMonitorTask) {
				countupDiscarded();
				log.warn("too many syslog. syslog discarded : " + r);
			}
		}
	}

	@Override
	public synchronized void shutdown() {
		_executor.shutdown();
		try {
			long _shutdownTimeoutMsec = HinemosPropertyUtil.getHinemosPropertyNum("monitor.systemlog.shutdown.timeout", 60000);

			if (! _executor.awaitTermination(_shutdownTimeoutMsec, TimeUnit.MILLISECONDS)) {
				List<Runnable> remained = _executor.shutdownNow();
				if (remained != null) {
					log.info("shutdown timeout. runnable remained. (size = " + remained.size() + ")");
				}
			}
		} catch (InterruptedException e) {
			_executor.shutdownNow();
		}
	}

	private class SystemLogMonitorTask implements Runnable {

		public final String receiverId;
		public final List<SyslogMessage> syslogList;

		public SystemLogMonitorTask(String receiverId, List<SyslogMessage> syslogList) {
			this.receiverId = receiverId;
			this.syslogList = syslogList;
		}

		public SystemLogMonitorTask(String receiverId, SyslogMessage syslog) {
			this.receiverId = receiverId;
			this.syslogList = new ArrayList<SyslogMessage>();
			this.syslogList.add(syslog);
		}

		@Override
		public void run() {
			JpaTransactionManager tm = null;

			if (log.isDebugEnabled()) {
				for (SyslogMessage syslog : syslogList) {
					log.debug("monitoring syslog : " + syslog);
				}
			}

			try {
				tm = new JpaTransactionManager();
				tm.begin();

				Collection<MonitorInfo> monitorList = null;
				try {
					monitorList = new MonitorSettingControllerBean().getSystemlogMonitorCache();
				} catch (MonitorNotFound e) {
					log.debug("monitor configuration (system log) not found.");
					return;
				} catch (InvalidRole e) {
					log.debug("monitor configuration (system log) not found.");
					return;
				}
				if (log.isDebugEnabled()) {
					log.debug("monitor configuration (system log) : count = " + monitorList.size());
				}
				
				if (monitorList.isEmpty()) {
					return;
				}

				// syslogヘッダのhostnameから該当ファシリティを取得
				Map<String, Set<String>> syslogHostnameFacilityIdSetMap = new HashMap<String, Set<String>>();
				for (SyslogMessage syslog : syslogList) {
					if (syslogHostnameFacilityIdSetMap.containsKey(syslog.hostname)) {
						continue;
					}
					
					Set<String> facilityIdSet = resolveFacilityId(syslog.hostname);
					syslogHostnameFacilityIdSetMap.put(syslog.hostname, facilityIdSet);
				}
				
				for (MonitorInfo monitor : monitorList) {
					if (log.isDebugEnabled()) {
						log.debug("filtering by configuration : " + monitor.getMonitorId());
					}

					// 管理対象フラグが無効であれば、次の設定の処理へスキップする
					if (monitor.getMonitorFlg() == ValidConstant.TYPE_INVALID) {
						continue;
					}
					
					// 関連の通知設定がなければ、スキップ
					if (monitor.getNotifyId().isEmpty()) {
						continue;
					}
					
					for (SyslogMessage syslog : syslogList) {
						List<SyslogMessage> syslogListBuffer = new ArrayList<SyslogMessage>();
						List<MonitorStringValueInfo> ruleListBuffer = new ArrayList<MonitorStringValueInfo>();
						List<String> facilityIdListBuffer = new ArrayList<String>();

						if (isNotInCalendar(monitor, syslog)) {
							continue;
						}
	
						Set<String> facilityIdSet = syslogHostnameFacilityIdSetMap.get(syslog.hostname);
						if (facilityIdSet == null) {
							log.warn("target facility not found: " + syslog.hostname);
							continue;
						}
						List<String> validFacilityIdList = getValidFacilityIdList(facilityIdSet, monitor);
	
						int orderNo = 0;
						for (MonitorStringValueInfo rule : monitor.getStringValueInfo()) {
							++orderNo;
							if (log.isDebugEnabled()) {
								log.debug(String.format("monitoring (monitorId = %s, orderNo = %d, patten = %s, enabled = %s, casesensitive = %s)",
										monitor.getMonitorId(), orderNo, rule.getPattern(), rule.isValidFlg(), rule.getCaseSensitivityFlg()));
							}
	
							if (! rule.isValidFlg()) {
								// 無効化されているルールはスキップする
								continue;
							}
	
							// パターンマッチを実施
							if (log.isDebugEnabled()) {
								log.debug(String.format("filtering syslog (regex = %s, syslog = %s", rule.getPattern(), syslog));
							}
	
							try {
								Pattern pattern = null;
								if (rule.getCaseSensitivityFlg()) {
									// 大文字・小文字を区別しない場合
									pattern = Pattern.compile(rule.getPattern(), Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
								} else {
									// 大文字・小文字を区別する場合
									pattern = Pattern.compile(rule.getPattern(), Pattern.DOTALL);
								}
	
								Matcher matcher = pattern.matcher(syslog.message);
								if (matcher.matches()) {
									if (rule.getProcessType() == ProcessConstant.TYPE_YES) {
										log.debug(String.format("matched (regex = %s, syslog = %s", rule.getPattern(), syslog));
										for (String facilityId : validFacilityIdList) {
											syslogListBuffer.add(syslog);
											ruleListBuffer.add(rule);
											facilityIdListBuffer.add(facilityId);
											countupNotified();
										}
									} else {
										log.debug(String.format("not matched (regex = %s, syslog = %s", rule.getPattern(), syslog));
									}
									break;
								}
							} catch (Exception e) {
								log.warn("filtering failure. (regex = " + rule.getPattern() + ") . " +
										e.getMessage(), e);
							}
						}

						_notifier.put(receiverId, syslogListBuffer, monitor, ruleListBuffer, facilityIdListBuffer);
					}
				}

				tm.commit();
			} catch (Exception e) {
				// HinemosException系はthrow元でlog出力するため、何もしない
				// HA構成のため、例外を握りつぶしてはいけない
				throw new RuntimeException("unexpected internal error. : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			} finally {
				if (tm != null) {
					tm.close();
				}
			}

		}

		private boolean isNotInCalendar(MonitorInfo monitor, SyslogMessage syslog) {
			boolean notInCalendar = false;
			// カレンダが割り当てられている場合
			if (monitor.getCalendarId() != null && monitor.getCalendarId().length() > 0) {
				try {
					boolean run = new SelectCalendar().isRun(monitor.getCalendarId(), syslog.date);
					notInCalendar = !run;
				} catch (CalendarNotFound e) {
					log.warn("calendar not found (calendarId = " + monitor.getCalendarId() + ")");
				} catch (InvalidRole e) {
					log.warn("calendar not found (calendarId = " + monitor.getCalendarId() + ") ,"
							+ e.getMessage());
				}

				// カレンダの有効期間外の場合
				if (notInCalendar) {
					if (log.isDebugEnabled()) {
						log.debug("skip monitoring because of calendar. (monitorId = " + monitor.getMonitorId()
								+ ", calendarId = " + monitor.getCalendarId() + ")");
					}
				}
			}
			
			return notInCalendar;
		}

		private List<String> getValidFacilityIdList(Set<String> facilityIdSet, MonitorInfo monitor) {
			List<String> validFacilityIdList = new ArrayList<String>();
			for (String facilityId : facilityIdSet) {
				if (log.isDebugEnabled()) {
					log.debug("filtering node. (monitorId = " + monitor.getMonitorId()
							+ ", facilityId = " + facilityId + ")");
				}

				if (FacilityTreeAttributeConstant.UNREGISTEREFD_SCOPE.equals(facilityId)) {
					if (! FacilityTreeAttributeConstant.UNREGISTEREFD_SCOPE.equals(monitor.getFacilityId())) {
						// 未登録ノードから送信されたsyslogだが、未登録ノードに対する設定でない場合はスキップする
						continue;
					}
				} else {
						if (! new RepositoryControllerBean().containsFaciliyId(monitor.getFacilityId(), facilityId, monitor.getOwnerRoleId())) {
						// syslogの送信元ノードが、設定のスコープ内に含まれない場合はスキップする
						continue;
					}
				}
				
				validFacilityIdList.add(facilityId);
			}
			return validFacilityIdList;
		}

		private Set<String> resolveFacilityId(String hostname){
			Set<String> facilityIdSet = null;

			if (log.isDebugEnabled()) {
				log.debug("resolving facilityId from hostname = " + hostname);
			}

			// ノード名による一致確認
			facilityIdSet = new RepositoryControllerBean().getNodeListByNodename(hostname);

			// ノード名で一致するノードがない場合
			if (facilityIdSet == null) {
				// IPアドレスによる一致確認
				try {
					facilityIdSet = new RepositoryControllerBean().getNodeListByIpAddress(InetAddress.getByName(hostname));
				} catch (UnknownHostException e) {
					if (log.isDebugEnabled()) {
						log.debug("unknow host " + hostname + ".", e);
					}
				}

				// ノード名でもIPアドレスでも一致するノードがない場合
				if (facilityIdSet == null) {
					// ホスト名による一致確認
					facilityIdSet = new RepositoryControllerBean().getNodeListByHostname(hostname);
				}
			}

			if (facilityIdSet == null) {
				// 指定のノード名、IPアドレスで登録されているノードがリポジトリに存在しないため、
				// 「"UNREGISTEREFD"（FacilityTreeAttributeConstant.UNREGISTEREFD_SCOPE）」だけを
				// セットに含めたものをマップに登録する。
				facilityIdSet = new HashSet<String>();
				facilityIdSet.add(FacilityTreeAttributeConstant.UNREGISTEREFD_SCOPE);
			}

			if (log.isDebugEnabled()) {
				log.debug("resolved facilityId " + facilityIdSet + "(from hostname = " + hostname + ")");
			}

			return facilityIdSet;
		}

		@Override
		public String toString() {
			return String.format("%s [receiverId = %s, syslogList = %s]",
					this.getClass().getSimpleName(), receiverId, syslogList);
		}

	}
}
