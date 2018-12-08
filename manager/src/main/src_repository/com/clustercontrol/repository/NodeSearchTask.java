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

package com.clustercontrol.repository;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.UserIdConstant;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.SnmpResponseError;
import com.clustercontrol.repository.bean.NodeInfoDeviceSearch;
import com.clustercontrol.repository.bean.NodeInfoDeviceSearchFuture;
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 * ノードサーチ処理の実装クラス
 */
public class NodeSearchTask implements Callable<NodeInfoDeviceSearchFuture> {

	private static Log m_log = LogFactory.getLog(NodeSearchTask.class);
	private String ipAddress;
	private int port;
	private String community;
	private String version;
	private String facilityID;
	private String securityLevel;
	private String user;
	private String authPass;
	private String privPass;
	private String authProtocol;
	private String privProtocol;

	/**
	 * コンストラクタ
	 * @param ipAddressTo IPアドレス
	 * @param port ポート
	 * @param community コミュニティ
	 * @param version バージョン
	 * @param facilityID ファシリティID
	 * @param securityLevel セキュリティレベル
	 * @param user ユーザー名
	 * @param authPass 認証パスワード
	 * @param privPass 暗号化パスワード
	 * @param authProtocol 認証プロトコル
	 * @param privProtocol 暗号化プロトコル
	 */
	public NodeSearchTask(String ipAddress, int port, String community,
			String version, String facilityID, String securityLevel,
			String user, String authPass, String privPass, String authProtocol,
			String privProtocol) {

		this.ipAddress = ipAddress;
		this.port = port;
		this.community = community;
		this.version = version;
		this.facilityID = facilityID;
		this.securityLevel = securityLevel;
		this.user = user;
		this.authPass = authPass;
		this.privPass = privPass;
		this.authProtocol = authProtocol;
		this.privProtocol = privProtocol;
	}

	/**
	 * ノードサーチ処理の実行
	 */
	@Override
	public NodeInfoDeviceSearchFuture call() {
		m_log.debug("call() start");
		NodeInfoDeviceSearch info = null;

		HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, UserIdConstant.HINEMOS);
		RepositoryControllerBean controller = new RepositoryControllerBean();
		//リストの分だけSNMPでノード検索
		try {
			m_log.debug("getNodePropertyBySNMP ipAddress=" + ipAddress);
			InetAddress address = InetAddress.getByName(ipAddress);
			List<String> facilityList = controller.getFacilityIdByIpAddress(address);

			if(facilityList != null && facilityList.isEmpty() == false) {
				//ノード一覧に既にIPアドレスが存在する場合は終了
				m_log.debug("ipAddress " + address + " is already registered.");
				return null;
			}

			info = controller.getNodePropertyBySNMP(ipAddress,port, community,
					version, facilityID, securityLevel, user,authPass,
					privPass, authProtocol, privProtocol);
		} catch (HinemosUnknown e) {
			return null;
		} catch(SnmpResponseError e) {
			//SNMP応答がないノードはスキップ
			return null;
		} catch (UnknownHostException e) {
			return null;
		}

		NodeInfoDeviceSearchFuture future = new NodeInfoDeviceSearchFuture();
		future.setNodeInfo(info.getNodeInfo());
		return future;
	}
}
