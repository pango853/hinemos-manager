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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.repository.bean.NodeCpuInfo;
import com.clustercontrol.repository.bean.NodeDeviceInfo;
import com.clustercontrol.repository.bean.NodeDiskInfo;
import com.clustercontrol.repository.bean.NodeFilesystemInfo;
import com.clustercontrol.repository.bean.NodeHostnameInfo;
import com.clustercontrol.repository.bean.NodeInfo;
import com.clustercontrol.repository.bean.NodeMemoryInfo;
import com.clustercontrol.repository.bean.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.bean.NodeNoteInfo;
import com.clustercontrol.repository.bean.NodeVariableInfo;
import com.clustercontrol.repository.model.FacilityEntity;
import com.clustercontrol.repository.model.NodeCpuEntity;
import com.clustercontrol.repository.model.NodeDeviceEntity;
import com.clustercontrol.repository.model.NodeDiskEntity;
import com.clustercontrol.repository.model.NodeEntity;
import com.clustercontrol.repository.model.NodeFilesystemEntity;
import com.clustercontrol.repository.model.NodeHostnameEntity;
import com.clustercontrol.repository.model.NodeMemoryEntity;
import com.clustercontrol.repository.model.NodeNetworkInterfaceEntity;
import com.clustercontrol.repository.model.NodeNoteEntity;
import com.clustercontrol.repository.model.NodeVariableEntity;
import com.clustercontrol.repository.util.FacilityUtil;
import com.clustercontrol.repository.util.QueryUtil;

/**
 * ノード用プロパティを作成するクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class NodeProperty {

	private static Log m_log = LogFactory.getLog(NodeProperty.class);

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(NodeProperty.class.getName());
		
		try {
			_lock.writeLock();
			
			Map<String, NodeInfo> cache = getCache();
			if (cache == null) {	// not null when clustered
				init();
			}
		} finally {
			_lock.writeUnlock();
		}
	}
	
	/** ----- 初期値キャッシュ ----- */
	
	@SuppressWarnings("unchecked")
	private static ConcurrentHashMap<String, NodeInfo> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_REPOSITORY_NODE);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_REPOSITORY_NODE + " : " + cache);
		return cache == null ? null : (ConcurrentHashMap<String, NodeInfo>)cache;
	}
	
	private static void storeCache(ConcurrentHashMap<String, NodeInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_REPOSITORY_NODE + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_REPOSITORY_NODE, newCache);
	}

	public static void removeNode (String facilityId) {
		m_log.info("remove NodeCache : " + facilityId);
		
		try {
			_lock.writeLock();
			
			ConcurrentHashMap<String, NodeInfo> cache = getCache();
			cache.remove(facilityId);
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	public static void init() {
		try {
			_lock.writeLock();
			
			ConcurrentHashMap<String, NodeInfo> cache = new ConcurrentHashMap<String, NodeInfo>();
			
			List<FacilityEntity> facilityList = QueryUtil.getAllNode_NONE();
			for (FacilityEntity facilityEntity : facilityList) {
				try {
					cache.put(facilityEntity.getFacilityId(), createNodeInfo(facilityEntity));
				} catch (FacilityNotFound e) {
					m_log.warn(e);
				}
			}
			
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	public static void clearCache () {
		m_log.info("clear NodeCache");
		
		try {
			_lock.writeLock();
			
			ConcurrentHashMap<String, NodeInfo> cache = getCache();
			cache.clear();
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * 与えられたファシリティIDに基づき、該当するノード情報を返す。<BR>
	 *
	 * @param facilityId ファシリティID
	 * @param mode ノード情報扱い種別（参照、追加、変更）
	 * @return ノード情報
	 * @throws FacilityNotFound
	 */
	public static NodeInfo getProperty(String facilityId) throws FacilityNotFound {
		m_log.debug("getProperty() : facilityId = " + facilityId);

		if (facilityId == null || facilityId.compareTo("") == 0) {
			return new NodeInfo();
		}

		{
			// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
			// (ConcurrentHashMapの特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
			ConcurrentHashMap<String, NodeInfo> cache = getCache();
			
			NodeInfo nodeInfo = cache.get(facilityId);
			if (nodeInfo != null) {
				if (!facilityId.equals(nodeInfo.getFacilityId())) {
					// 試験中に怪しい挙動があったので、一応ログを仕込んでおく。
					m_log.error("cache is broken." + facilityId + "," + nodeInfo.getFacilityId());
				}
				return nodeInfo;
			}
		}
		
		try {
			_lock.writeLock();
			
			ConcurrentHashMap<String, NodeInfo> cache = getCache();
			
			FacilityEntity facilityEntity = QueryUtil.getFacilityPK_NONE(facilityId);
			NodeInfo nodeInfo = createNodeInfo(facilityEntity);
			cache.put(facilityId, nodeInfo);
			storeCache(cache);
			
			return nodeInfo;
		} finally {
			_lock.writeUnlock();
		}
	}

	private static NodeInfo createNodeInfo(FacilityEntity facilityEntity) throws FacilityNotFound {
		String facilityId = facilityEntity.getFacilityId();
		NodeEntity node;
		// ファシリティがノードかどうかを確認する
		if (!FacilityUtil.isNode(facilityEntity)) {
			FacilityNotFound e = new FacilityNotFound("this facility is not a node. (facilityId = " + facilityId + ")");
			m_log.info("createNodeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		node = facilityEntity.getNodeEntity();

		NodeInfo nodeInfo = new NodeInfo();

		// ----- ファシリティ関連 -----
		// ファシリティID
		nodeInfo.setFacilityId(facilityEntity.getFacilityId());
		// ファシリティ名
		nodeInfo.setFacilityName(facilityEntity.getFacilityName());
		// 説明
		nodeInfo.setDescription(facilityEntity.getDescription());
		// 有効/無効
		nodeInfo.setValid(FacilityUtil.isValid(facilityEntity));
		// アイコン名
		nodeInfo.setIconImage(facilityEntity.getIconImage());
		// オーナーロールID
		nodeInfo.setOwnerRoleId(facilityEntity.getOwnerRoleId());
		// 登録ユーザID
		nodeInfo.setCreateUserId(facilityEntity.getCreateUserId());
		// 登録日時
		if (facilityEntity.getCreateDatetime() != null
				&& facilityEntity.getCreateDatetime().getTime() != 0) {
			nodeInfo.setCreateDatetime(facilityEntity.getCreateDatetime().getTime());
		}
		// 最終更新ユーザID
		nodeInfo.setModifyUserId(facilityEntity.getModifyUserId());
		// 最終更新日時
		if (facilityEntity.getModifyDatetime() != null
				&& facilityEntity.getModifyDatetime().getTime() != 0) {
			nodeInfo.setModifyDatetime(facilityEntity.getModifyDatetime().getTime());
		}

		// ----- ノード基本情報 -----
		// 自動デバイスサーチ
		boolean isAutoDeviceSearch = false;
		if (node.getAutoDeviceSearch() != null && node.getAutoDeviceSearch() == ValidConstant.TYPE_VALID) {
			isAutoDeviceSearch =  true;
		}
		nodeInfo.setAutoDeviceSearch(isAutoDeviceSearch);

		// ----- HW -----
		// プラットフォーム
		nodeInfo.setPlatformFamily(node.getPlatformFamily());
		// サブプラットフォーム
		nodeInfo.setSubPlatformFamily(node.getSubPlatformFamily());
		// H/Wタイプ
		nodeInfo.setHardwareType(node.getHardwareType());
		// 画面アイコンイメージ
		nodeInfo.setIconImage(facilityEntity.getIconImage());

		// ----- IPアドレス関連 -----
		// IPバージョン
		if (node.getIpAddressVersion() != -1) {
			nodeInfo.setIpAddressVersion(node.getIpAddressVersion());
		}
		// IPアドレスV4
		nodeInfo.setIpAddressV4(node.getIpAddressV4());
		// IPアドレスV6
		nodeInfo.setIpAddressV6(node.getIpAddressV6());
		// ホスト名
		ArrayList<NodeHostnameInfo> hostnameList = new ArrayList<NodeHostnameInfo>();
		if (node.getNodeHostnameEntities() != null) {
			for (NodeHostnameEntity nodeHostname : node.getNodeHostnameEntities()) {
				// ホスト名
				hostnameList.add(new NodeHostnameInfo(nodeHostname.getId().getHostname()));
			}
		}
		nodeInfo.setNodeHostnameInfo(hostnameList);

		// ----- OS関連 -----
		// ノード名
		nodeInfo.setNodeName(node.getNodeName());
		// OS名
		nodeInfo.setOsName(node.getOsName());
		// OSリリース
		nodeInfo.setOsRelease(node.getOsRelease());
		// OSバージョン
		nodeInfo.setOsVersion(node.getOsVersion());
		// 文字セット
		nodeInfo.setCharacterSet(node.getCharacterSet());

		// ----- Hinemosエージェント関連 -----
		// 即時反映用ポート番号
		if (node.getAgentAwakePort() != -1) {
			nodeInfo.setAgentAwakePort(node.getAgentAwakePort());
		}

		// ----- ジョブ -----
		// ジョブ優先度
		if (node.getJobPriority() != -1) {
			nodeInfo.setJobPriority(node.getJobPriority());
		}
		// ジョブ多重度
		if (node.getJobMultiplicity() != -1) {
			nodeInfo.setJobMultiplicity(node.getJobMultiplicity());
		}


		// ----- サービス -----

		// ----- SNMP関連 -----
		// SNMP接続ユーザ
		nodeInfo.setSnmpUser(node.getSnmpUser());
		// SNMP接続認証パスワード
		nodeInfo.setSnmpAuthPassword(node.getSnmpAuthPassword());
		// SNMP接続暗号化パスワード
		nodeInfo.setSnmpPrivPassword(node.getSnmpPrivPassword());
		// SNMPポート番号
		if (node.getSnmpPort() != -1) {
			nodeInfo.setSnmpPort(node.getSnmpPort());
		}
		// SNMPコミュニティ名
		nodeInfo.setSnmpCommunity(node.getSnmpCommunity());
		// SNMPバージョン
		nodeInfo.setSnmpVersion(node.getSnmpVersion());
		// SNMPセキュリティレベル
		nodeInfo.setSnmpSecurityLevel(node.getSnmpSecurityLevel());
		// SNMP認証プロトコル
		nodeInfo.setSnmpAuthProtocol(node.getSnmpAuthProtocol());
		// SNMP暗号化プロトコル
		nodeInfo.setSnmpPrivProtocol(node.getSnmpPrivProtocol());
		// SNMPタイムアウト
		if (node.getSnmpTimeout() != -1) {
			nodeInfo.setSnmpTimeout(node.getSnmpTimeout());
		}
		// SNMPリトライ回数
		if (node.getSnmpRetryCount() != -1) {
			nodeInfo.setSnmpRetryCount(node.getSnmpRetryCount());
		}

		// ----- WBEM関連 -----
		// WBEM接続ポート番号
		if (node.getWbemPort() != -1) {
			nodeInfo.setWbemPort(node.getWbemPort());
		}
		// WBEM接続ユーザ
		nodeInfo.setWbemUser(node.getWbemUser());
		// WBEM接続ユーザパスワード
		nodeInfo.setWbemUserPassword(node.getWbemUserPassword());
		// WBEM接続プロトコル
		nodeInfo.setWbemProtocol(node.getWbemProtocol());
		// WBEM接続タイムアウト
		if (node.getWbemTimeout() != -1) {
			nodeInfo.setWbemTimeout(node.getWbemTimeout());
		}
		// WBEM接続リトライ回数
		if (node.getWbemRetryCount() != -1) {
			nodeInfo.setWbemRetryCount(node.getWbemRetryCount());
		}

		// ----- IPMI関連 -----
		// IPMI接続アドレス
		nodeInfo.setIpmiIpAddress(node.getIpmiIpAddress());
		// IPMIポート番号
		if (node.getIpmiPort() != -1) {
			nodeInfo.setIpmiPort(node.getIpmiPort());
		}
		// IPMI接続ユーザ
		nodeInfo.setIpmiUser(node.getIpmiUser());
		// IPMI接続ユーザパスワード
		nodeInfo.setIpmiUserPassword(node.getIpmiUserPassword());
		// IPMI接続タイムアウト
		if (node.getIpmiTimeout() != -1) {
			nodeInfo.setIpmiTimeout(node.getIpmiTimeout());
		}
		// IPMI接続リトライ回数
		if (node.getIpmiRetryCount() != -1) {
			nodeInfo.setIpmiRetries(node.getIpmiRetryCount());
		}
		// IPMI接続プロトコル
		nodeInfo.setIpmiProtocol(node.getIpmiProtocol());
		// IPMI特権レベル
		nodeInfo.setIpmiLevel(node.getIpmiLevel());

		// ----- WinRM関連 -----
		// WinRM接続ユーザ
		nodeInfo.setWinrmUser(node.getWinrmUser());
		// WinRM接続ユーザパスワード
		nodeInfo.setWinrmUserPassword(node.getWinrmUserPassword());
		// WinRMバージョン
		nodeInfo.setWinrmVersion(node.getWinrmVersion());
		// WinRM接続ポート番号
		if (node.getWinrmPort() != -1) {
			nodeInfo.setWinrmPort(node.getWinrmPort());
		}
		// WinRM接続プロトコル
		nodeInfo.setWinrmProtocol(node.getWinrmProtocol());
		// WinRM接続タイムアウト
		if (node.getWinrmTimeout() != -1) {
			nodeInfo.setWinrmTimeout(node.getWinrmTimeout());
		}
		// WinRM接続リトライ回数
		if (node.getWinrmRetryCount() != -1) {
			nodeInfo.setWinrmRetries(node.getWinrmRetryCount());
		}
		
		// ----- SSH関連 -----
		// SSH接続ユーザ
		nodeInfo.setSshUser(node.getSshUser());
		// SSH接続ユーザパスワード
		nodeInfo.setSshUserPassword(node.getSshUserPassword());
		// SSH秘密鍵ファイル名
		nodeInfo.setSshPrivateKeyFilepath(node.getSshPrivateKeyFilepath());
		// SSH秘密鍵パスフレーズ
		nodeInfo.setSshPrivateKeyPassphrase(node.getSshPrivateKeyPassphrase());
		// SSHポート番号
		if (node.getSshPort() != -1) {
			nodeInfo.setSshPort(node.getSshPort());
		}
		// SSHタイムアウト
		if (node.getSshTimeout() != -1) {
			nodeInfo.setSshTimeout(node.getSshTimeout());
		}

		// ----- デバイス関連 -----

		// ----- 汎用デバイス情報 -----
		ArrayList<NodeDeviceInfo> deviceList = new ArrayList<NodeDeviceInfo>();
		if (node.getNodeDeviceEntities() != null) {
			for (NodeDeviceEntity nodeDevice : node.getNodeDeviceEntities()) {
				NodeDeviceInfo device = new NodeDeviceInfo();

				// デバイス種別
				device.setDeviceType(nodeDevice.getId().getDeviceType());
				// デバイス表示名
				device.setDeviceDisplayName(nodeDevice.getDeviceDisplayName());
				// デバイスINDEX
				device.setDeviceIndex(nodeDevice.getId().getDeviceIndex());
				// デバイス名
				device.setDeviceName(nodeDevice.getId().getDeviceName());
				// デバイスサイズ
				device.setDeviceSize(nodeDevice.getDeviceSize());
				// デバイスサイズ単位
				device.setDeviceSizeUnit(nodeDevice.getDeviceSizeUnit());
				// 説明
				device.setDeviceDescription(nodeDevice.getDeviceDescription());
				deviceList.add(device);
			}
		}
		nodeInfo.setNodeDeviceInfo(deviceList);

		// ----- CPUデバイス情報 -----
		ArrayList<NodeCpuInfo> cpuList = new ArrayList<NodeCpuInfo>();
		if (node.getNodeCpuEntities() != null) {
			for(NodeCpuEntity nodeCpu : node.getNodeCpuEntities()){
				NodeCpuInfo cpu = new NodeCpuInfo();

				// デバイス種別
				cpu.setDeviceType(nodeCpu.getId().getDeviceType());
				// デバイス表示名
				cpu.setDeviceDisplayName(nodeCpu.getDeviceDisplayName());
				// デバイスINDEX
				cpu.setDeviceIndex(nodeCpu.getId().getDeviceIndex());
				// デバイス名
				cpu.setDeviceName(nodeCpu.getId().getDeviceName());
				// デバイスサイズ
				cpu.setDeviceSize(nodeCpu.getDeviceSize());
				// デバイスサイズ単位
				cpu.setDeviceSizeUnit(nodeCpu.getDeviceSizeUnit());
				// 説明
				cpu.setDeviceDescription(nodeCpu.getDeviceDescription());
				cpuList.add(cpu);
			}
		}
		nodeInfo.setNodeCpuInfo(cpuList);

		// ----- MEMデバイス情報 -----
		ArrayList<NodeMemoryInfo> memList = new ArrayList<NodeMemoryInfo>();
		if (node.getNodeMemoryEntities() != null) {
			for(NodeMemoryEntity nodeMem : node.getNodeMemoryEntities()){
				NodeMemoryInfo mem = new NodeMemoryInfo();

				// デバイス種別
				mem.setDeviceType(nodeMem.getId().getDeviceType());
				// デバイス表示名
				mem.setDeviceDisplayName(nodeMem.getDeviceDisplayName());
				// デバイスINDEX
				mem.setDeviceIndex(nodeMem.getId().getDeviceIndex());
				// デバイス名
				mem.setDeviceName(nodeMem.getId().getDeviceName());
				// デバイスサイズ
				mem.setDeviceSize(nodeMem.getDeviceSize());
				// デバイスサイズ単位
				mem.setDeviceSizeUnit(nodeMem.getDeviceSizeUnit());
				// 説明
				mem.setDeviceDescription(nodeMem.getDeviceDescription());
				memList.add(mem);
			}
		}
		nodeInfo.setNodeMemoryInfo(memList);

		// ----- NICデバイス情報 -----
		ArrayList<NodeNetworkInterfaceInfo> nicList = new ArrayList<NodeNetworkInterfaceInfo>();
		if (node.getNodeNetworkInterfaceEntities() != null) {
			for(NodeNetworkInterfaceEntity nodeNic : node.getNodeNetworkInterfaceEntities()){
				NodeNetworkInterfaceInfo nic = new NodeNetworkInterfaceInfo();

				// デバイス種別
				nic.setDeviceType(nodeNic.getId().getDeviceType());
				// デバイス表示名
				nic.setDeviceDisplayName(nodeNic.getDeviceDisplayName());
				// デバイスINDEX
				nic.setDeviceIndex(nodeNic.getId().getDeviceIndex());
				// デバイス名
				nic.setDeviceName(nodeNic.getId().getDeviceName());
				// デバイスサイズ
				nic.setDeviceSize(nodeNic.getDeviceSize());
				// デバイスサイズ単位
				nic.setDeviceSizeUnit(nodeNic.getDeviceSizeUnit());
				// 説明
				nic.setDeviceDescription(nodeNic.getDeviceDescription());
				// NIC ip
				nic.setNicIpAddress(nodeNic.getDeviceNicIpAddress());
				// NIC MAC
				nic.setNicMacAddress(nodeNic.getDeviceNicMacAddress());
				nicList.add(nic);
			}
		}
		nodeInfo.setNodeNetworkInterfaceInfo(nicList);

		// ----- DISKデバイス情報 -----
		ArrayList<NodeDiskInfo> diskList = new ArrayList<NodeDiskInfo>();
		if (node.getNodeDiskEntities() != null) {
			for(NodeDiskEntity nodeDisk : node.getNodeDiskEntities()){
				NodeDiskInfo disk = new NodeDiskInfo();

				// デバイス種別
				disk.setDeviceType(nodeDisk.getId().getDeviceType());
				// デバイス表示名
				disk.setDeviceDisplayName(nodeDisk.getDeviceDisplayName());
				// デバイスINDEX
				disk.setDeviceIndex(nodeDisk.getId().getDeviceIndex());
				// デバイス名
				disk.setDeviceName(nodeDisk.getId().getDeviceName());
				// デバイスサイズ
				disk.setDeviceSize(nodeDisk.getDeviceSize());
				// デバイスサイズ単位
				disk.setDeviceSizeUnit(nodeDisk.getDeviceSizeUnit());
				// 説明
				disk.setDeviceDescription(nodeDisk.getDeviceDescription());
				// 回転数
				disk.setDiskRpm(nodeDisk.getDeviceDiskRpm());
				diskList.add(disk);
			}
		}
		nodeInfo.setNodeDiskInfo(diskList);

		// ---- ファイルシステム情報 -----
		ArrayList<NodeFilesystemInfo> filesystemList = new ArrayList<NodeFilesystemInfo>();
		if (node.getNodeFilesystemEntities() != null) {
			for (NodeFilesystemEntity nodeFilesystem : node.getNodeFilesystemEntities()) {
				NodeFilesystemInfo filesystem = new NodeFilesystemInfo();

				// デバイス種別
				filesystem.setDeviceType(nodeFilesystem.getId().getDeviceType());
				// デバイス表示名
				filesystem.setDeviceDisplayName(nodeFilesystem.getDeviceDisplayName());
				// デバイスINDEX
				filesystem.setDeviceIndex(nodeFilesystem.getId().getDeviceIndex());
				// デバイス名
				filesystem.setDeviceName(nodeFilesystem.getId().getDeviceName());
				// デバイスサイズ
				filesystem.setDeviceSize(nodeFilesystem.getDeviceSize());
				// デバイスサイズ単位
				filesystem.setDeviceSizeUnit(nodeFilesystem.getDeviceSizeUnit());
				// 説明
				filesystem.setDeviceDescription(nodeFilesystem.getDeviceDescription());
				// ファイルシステム種別
				filesystem.setFilesystemType(nodeFilesystem.getDeviceFilesystemType());
				filesystemList.add(filesystem);
			}
		}
		nodeInfo.setNodeFilesystemInfo(filesystemList);

		// ----- クラウド・仮想化関連 -----
		// クラウドサービス
		nodeInfo.setCloudService(node.getCloudService());
		// クラウドスコープ
		nodeInfo.setCloudScope(node.getCloudScope());
		// クラウドリソースタイプ
		nodeInfo.setCloudResourceType(node.getCloudResourceType());
		// クラウドリソースID
		nodeInfo.setCloudResourceId(node.getCloudResourceId());
		// クラウドリソース名
		nodeInfo.setCloudResourceName(node.getCloudResourceName());
		// クラウドロケーション
		nodeInfo.setCloudLocation(node.getCloudLocation());

		// ----- 	ノード変数 -----
		ArrayList<NodeVariableInfo> nodeVariableList = new ArrayList<NodeVariableInfo>();
		if (node.getNodeVariableEntities() != null) {
			for (NodeVariableEntity nodeVariable : node.getNodeVariableEntities()) {
				NodeVariableInfo variable = new NodeVariableInfo();

				// ノード変数名
				variable.setNodeVariableName(nodeVariable.getId().getNodeVariableName());
				// ノード変数値
				variable.setNodeVariableValue(nodeVariable.getNodeVariableValue());
				nodeVariableList.add(variable);
			}
		}
		nodeInfo.setNodeVariableInfo(nodeVariableList);

		// ----- 管理情報関連 -----
		// 連絡先
		nodeInfo.setContact(node.getContact());
		// 管理者
		nodeInfo.setAdministrator(node.getAdministrator());


		// ----- 備考 -----
		ArrayList<NodeNoteInfo> noteList = new ArrayList<NodeNoteInfo>();
		if (node.getNodeNoteEntities() != null) {
			int cnt = 0;
			for (NodeNoteEntity nodeNote : node.getNodeNoteEntities()) {
				// 備考
				noteList.add(new NodeNoteInfo(cnt, nodeNote.getNote()));
				cnt++;
			}
		}
		nodeInfo.setNodeNoteInfo(noteList);

		return nodeInfo;
	}
}
