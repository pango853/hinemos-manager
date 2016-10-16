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

package com.clustercontrol.repository.session;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.repository.DeviceSearchTask;


/**
 * リポジトリ管理機能の実行管理を行う Session Bean クラス<BR>
 *
 */
public class RepositoryRunManagementBean {
	/** ログ出力のインスタンス<BR> */
	private static Log m_log = LogFactory.getLog( RepositoryRunManagementBean.class );

	// 二重起動を防ぐためのセマフォ
	private static final Semaphore duplicateExec = new Semaphore(1);

	// 実行回数カウンタ
	private static int execCount = 0;

	private static final ExecutorService _executorService = Executors.newFixedThreadPool(
			HinemosPropertyUtil.getHinemosPropertyNum("repository.device.search.threadpool.size", 5),
			new ThreadFactory() {
				private volatile int _count = 0;

				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r, "DeviceSearchWorker-" + _count++);
				}
			}
	);

	/**
	 * Quartzからのコールバックメソッド<BR>
	 * <P>
	 * Quartzから定周期で呼び出されます。<BR>
	 * <BR>
	 * 実行状態が実行中のセッションをチェックし、自動デバイスサーチ処理を開始する。<BR>
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 *
	 * @see com.clustercontrol.jobmanagement.factory.OperationJob#runJob()
	 */
	public void run() {
		if (duplicateExec.tryAcquire()) {
			try {
				int interval = HinemosPropertyUtil.getHinemosPropertyNum("repository.device.search.interval", 5);
				if (interval <= 0) {
					return;
				}
				int amari = execCount % interval;
				if (amari == 0) {
					long start = System.currentTimeMillis();
					RepositoryControllerBean controller = new RepositoryControllerBean();
					//ノード一覧を取得
					try {
						List<String> facilityIdList = controller.getNodeFacilityIdList();
						List<Future<Boolean>> futureList = new ArrayList<Future<Boolean>>();
						//ノードの数だけ多重起動
						for (String facilityId : facilityIdList) {
							futureList.add(_executorService.submit(new DeviceSearchTask(facilityId)));
						}
						
						//終了を待つ
						for (Future<Boolean> future : futureList) {
							try {
								future.get(60 * 1000, TimeUnit.MILLISECONDS);
							} catch (TimeoutException e) {
								// SNMPがタイムアウトするため、ここには到達しないはずだが。。。
								m_log.warn("run() : " + e.getClass().getName(), e);
							} catch (InterruptedException e) {
								m_log.warn("run() : " + e.getClass().getName(), e);
							} catch (ExecutionException e) {
								m_log.warn("run() : " + e.getClass().getName(), e);
							}
						}

					} catch (HinemosUnknown e) {
						m_log.warn("run() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					}
					m_log.info("auto device search : time=" + (System.currentTimeMillis() - start) + "ms");
				}
				m_log.debug("execCount=" + execCount + " amari=" + amari + " interval=" + interval);
				execCount = execCount>=interval ? 1 : ++execCount;
			} finally {
				duplicateExec.release();
			}
		} else {
			m_log.warn("runningCheck is busy !!");
		}
	}
}
