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

package com.clustercontrol.repository.factory;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

import com.clustercontrol.bean.SnmpSecurityLevelConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.repository.NodeSearchTask;
import com.clustercontrol.repository.bean.NodeInfo;
import com.clustercontrol.repository.bean.NodeInfoDeviceSearch;
import com.clustercontrol.repository.bean.NodeInfoDeviceSearchFuture;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.FacilityIdCacheInitCallback;
import com.clustercontrol.repository.util.FacilityTreeCacheRefreshCallback;
import com.clustercontrol.repository.util.RepositoryChangedNotificationCallback;
import com.clustercontrol.repository.util.RepositoryUtil;
import com.clustercontrol.util.Messages;


/**
 * ノートサーチ機能の実行管理を行う Session Bean クラス<BR>
 *
 */
public class NodeSearcher {
	/** ログ出力のインスタンス<BR> */
	private static Log m_log = LogFactory.getLog( NodeSearcher.class );

	// 二重起動を防ぐためのセマフォ
	private static final Semaphore duplicateExec = new Semaphore(1);

	public static final String MaxSearchNodeKey = "repository.node.search.max.node"; 

	private final ExecutorService _executorService = Executors.newCachedThreadPool(
			new ThreadFactory() {
		private volatile int _count = 0;
		@Override
		public Thread newThread(Runnable r) {
			String threadName = "NodeSearchWorker-" + _count++;
			m_log.debug("new thread=" + threadName);
			return new Thread(r, threadName);
		}
	});

	public List<NodeInfoDeviceSearch> searchNode(String ownerRoleId,
			String ipAddrFrom, String ipAddrTo, int port, String community,
			String version, String facilityID, String securityLevel,
			String user, String authPass, String privPass, String authProtocol,
			String privProtocol) throws HinemosUnknown, FacilityDuplicate, InvalidSetting {

		long startTime = System.currentTimeMillis();
		// クライアントのタイムアウト(デフォルト60秒)よりも短くしておく。
		int maxMsec = HinemosPropertyUtil.getHinemosPropertyNum("repository.node.search.timeout", 50 * 1000);
		List<NodeInfoDeviceSearch> nodeList = new ArrayList<NodeInfoDeviceSearch>();

		if (duplicateExec.tryAcquire()) {
			try {
				List<String> ipAddressList = new ArrayList<String> ();
				String errMsg = Messages.getString("message.repository.37");

				//入力チェック
				if (ipAddrFrom == null || ipAddrFrom.equals("") || ipAddrTo == null || ipAddrTo.equals("")) {
					throw new HinemosUnknown(Messages.getString("message.repository.nodesearch.1"));
				} else if (version.equals("3")
						&& securityLevel.equals(SnmpSecurityLevelConstant.NOAUTH_NOPRIV) == false) {
					if(user == null || user.equals("")) {
						throw new HinemosUnknown(Messages.getString("message.repository.nodesearch.3"));
					} else if(authPass == null || authPass.equals("")) {
						throw new HinemosUnknown(Messages.getString("message.repository.nodesearch.4"));
					} else if (securityLevel.equals(SnmpSecurityLevelConstant.AUTH_PRIV)) {
						if(privPass == null || privPass.equals("")) {
							throw new HinemosUnknown(Messages.getString("message.repository.nodesearch.5"));
						}
					}
				}

				// IPアドレスチェック
				InetAddress addressFrom;
				InetAddress addressTo;
				try {
					addressFrom = InetAddress.getByName(ipAddrFrom);
					addressTo = InetAddress.getByName(ipAddrTo);

					if (addressFrom instanceof Inet4Address && addressTo instanceof Inet4Address){
						//IPv4の場合はさらにStringをチェック
						if (!ipAddrFrom.matches(".{1,3}?\\..{1,3}?\\..{1,3}?\\..{1,3}?")){
							m_log.info(errMsg);
							throw new HinemosUnknown(errMsg);
						}
						ipAddressList = RepositoryUtil.getIpList(ipAddrFrom, ipAddrTo, 4);
					} else if (addressFrom instanceof Inet6Address && addressTo instanceof Inet6Address){
						//IPv6の場合は特にStringチェックは無し
						ipAddressList = RepositoryUtil.getIpList(ipAddrFrom, ipAddrTo, 6);
					} else {
						m_log.info(errMsg);
						throw new HinemosUnknown(errMsg);
					}
				} catch (UnknownHostException e) {
					m_log.warn(errMsg);
					throw new HinemosUnknown(errMsg);
				}

				if (m_log.isDebugEnabled()) {
					String str = "";
					for (String ipAddress : ipAddressList) {
						if (str.length() != 0) {
							str += ", ";
						}
						str += ipAddress;
					}
					m_log.debug("ipAddress=" + str);
				}

				List<NodeInfoDeviceSearchFuture> futureList = new ArrayList<NodeInfoDeviceSearchFuture>();
				NodeInfo nodeInfo = new NodeInfo();
				try {
					//ノード一覧を取得
					//256ノードより多い場合はエラーとする。
					if (ipAddressList.size() > HinemosPropertyUtil.getHinemosPropertyNum(MaxSearchNodeKey, 256)) {
						m_log.info(Messages.getString("message.repository.nodesearch.7"));
						throw new HinemosUnknown(Messages.getString("message.repository.nodesearch.7"));
					}

					//ノードの数だけ多重起動
					//delayさせながらスレッドを立ち上げる。終わったスレッドは再利用される。(60秒間利用されないとスレッドは消える)
					List<Future<NodeInfoDeviceSearchFuture>> list = new ArrayList<Future<NodeInfoDeviceSearchFuture>>();
					for (String ipAddress : ipAddressList) {
						if (list.size() > 0) {
							Thread.sleep(HinemosPropertyUtil.getHinemosPropertyNum("repository.node.search.delay", 50));
						}
						NodeSearchTask task = new NodeSearchTask(ipAddress, port, community, version, facilityID, securityLevel,
								user, authPass, privPass, authProtocol, privProtocol);
						list.add(_executorService.submit(task));
					}

					for (Future<NodeInfoDeviceSearchFuture> future : list) {
						if (future != null) {
							try {
								// SNMPのタイムアウトは5秒にしているが念のため、getのときにタイムアウトを指定する。
								futureList.add(future.get(maxMsec, TimeUnit.MILLISECONDS));
							} catch (TimeoutException e) {
								m_log.warn("searchNode : " + e.getClass().getName() + ", " + e.getMessage());
							}
						}
					}
				} catch (InterruptedException e) {
					m_log.error("searchNode : " + e.getClass().getName() + ", " + e.getMessage());
					throw new HinemosUnknown(e.getMessage());
				} catch (ExecutionException e) {
					m_log.error("searchNode : " + e.getClass().getName() + ", " + e.getMessage());
					throw new HinemosUnknown(e.getMessage());
				}

				//ノード登録
				RepositoryControllerBean controller = new RepositoryControllerBean();

				boolean commitFlag = false;
				for(NodeInfoDeviceSearchFuture info : futureList) {
					try {
						if (info == null) {
							continue;
						}
						nodeInfo = info.getNodeInfo();
						nodeInfo.setOwnerRoleId(ownerRoleId);
						m_log.debug("nodeInfo " + nodeInfo);

						//タイムアウトチェック
						long msec = System.currentTimeMillis() - startTime;
						if (msec > maxMsec) {
							m_log.info(Messages.getString("message.process.8") + " msec=" + msec);
							throw new HinemosUnknown(Messages.getString("message.process.8"));
						}
						try {
							//性能改善のためリフレッシュは全ノード登録後に一度のみ行う
							controller.addNodeWithoutRefresh(nodeInfo);
						} catch (FacilityDuplicate | InvalidSetting | HinemosUnknown e) {
							info.setErrorNodeInfo(nodeInfo);
						} finally {
							nodeList.add(info);
						}
						commitFlag = true;
					} catch (Exception e) {
						m_log.error("searchNode : " + e.getClass().getName() + ", " + e.getMessage(), e);
						throw new HinemosUnknown(e.getMessage());
					}
				}
				if (commitFlag) {
					new FacilityIdCacheInitCallback().postCommit();

					//FacilityTreeCache更新のためにトランザクションが必要
					JpaTransactionManager jtm = new JpaTransactionManager();
					try {
						jtm.begin();
						new FacilityTreeCacheRefreshCallback().postCommit();
						jtm.commit();
					} catch (Exception e1) {
						jtm.rollback();
						throw e1;
					} finally {
						jtm.close();
					}

					new RepositoryChangedNotificationCallback().postCommit();
				}
			} catch(HinemosUnknown e) {
				throw e;
			} finally {
				m_log.info("node search : " +
						"ipAddrFrom=" + ipAddrFrom + ", ipAddrTo=" + ipAddrTo + 
						", time=" + (System.currentTimeMillis() - startTime) + "ms");
				duplicateExec.release();
			}
		} else {
			m_log.warn("runningCheck is busy !!");
		}

		return nodeList;
	}
}
