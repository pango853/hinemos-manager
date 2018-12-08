/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.repository.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.SnmpProtocolConstant;
import com.clustercontrol.bean.SnmpSecurityLevelConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityInfo;
import com.clustercontrol.repository.bean.NodeCpuInfo;
import com.clustercontrol.repository.bean.NodeDeviceInfo;
import com.clustercontrol.repository.bean.NodeDiskInfo;
import com.clustercontrol.repository.bean.NodeFilesystemInfo;
import com.clustercontrol.repository.bean.NodeHostnameInfo;
import com.clustercontrol.repository.bean.NodeInfo;
import com.clustercontrol.repository.bean.NodeMemoryInfo;
import com.clustercontrol.repository.bean.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.bean.NodeVariableInfo;
import com.clustercontrol.repository.bean.ScopeInfo;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.repository.model.NodeCpuEntityPK;
import com.clustercontrol.repository.model.NodeDeviceEntityPK;
import com.clustercontrol.repository.model.NodeDiskEntityPK;
import com.clustercontrol.repository.model.NodeFilesystemEntityPK;
import com.clustercontrol.repository.model.NodeHostnameEntityPK;
import com.clustercontrol.repository.model.NodeMemoryEntityPK;
import com.clustercontrol.repository.model.NodeNetworkInterfaceEntityPK;
import com.clustercontrol.repository.model.NodeVariableEntityPK;
import com.clustercontrol.util.Messages;

/**
 * リポジトリ管理の入力チェッククラス
 *
 *
 */
public class RepositoryValidator {

	private static Log m_log = LogFactory.getLog(RepositoryValidator.class);

	public static void validateIpv4(String ipv4) throws InvalidSetting {
		
		if (!ipv4.matches("\\d{1,3}?\\.\\d{1,3}?\\.\\d{1,3}?\\.\\d{1,3}?")){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.repository.24") + "(" + ipv4 + ")");
			m_log.info("validateNodeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		String[] ipv4Array = ipv4.split("\\.");
		if (ipv4Array.length != 4) {
			InvalidSetting e = new InvalidSetting(Messages.getString("message.repository.24") + "(" + ipv4 + ")");
			m_log.info("validateNodeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		for (int i = 0; i < 4; i ++) {
			int j = Integer.parseInt(ipv4Array[i]);
			if (j < 0 || 255 < j) {
				InvalidSetting e = new InvalidSetting(Messages.getString("message.repository.24") + "(" + ipv4 + ")");
				m_log.info("validateNodeInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		
	}
	
	public static void validateNodeInfo(NodeInfo nodeInfo) throws InvalidSetting{

		validateFacilityInfo(nodeInfo);

		// hardware
		CommonValidator.validateString(Messages.getString("hardware.type"), nodeInfo.getHardwareType(), false, 0, 128);
		// platformFamily
		CommonValidator.validateString(Messages.getString("platform.family.name"), nodeInfo.getPlatformFamily(), true, 1, 128);
		// subPlatformFamily
		CommonValidator.validateString(Messages.getString("sub.platform.family.name"), nodeInfo.getSubPlatformFamily(), true, 0, 128);

		try {
			QueryUtil.getCollectorPlatformMstPK(nodeInfo.getPlatformFamily());
		} catch (FacilityNotFound e) {
			throw new InvalidSetting("platform " + nodeInfo.getPlatformFamily() + " does not exist!");
		}

		// facilityType
		if(nodeInfo.getFacilityType() != FacilityConstant.TYPE_NODE){
			InvalidSetting e = new InvalidSetting("Node FacilityType is  " + FacilityConstant.TYPE_NODE);
			m_log.info("validateNodeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		//IPアドレスの入力チェック
		if(nodeInfo.getIpAddressVersion() == null){
			InvalidSetting e = new InvalidSetting("IpAddressVersion is null.");
			m_log.info("validateNodeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		int ipaddressVersion = nodeInfo.getIpAddressVersion().intValue();
		if(ipaddressVersion == 4){
			//versionが空か4の場合には、
			if(nodeInfo.getIpAddressV4() == null || "".equals(nodeInfo.getIpAddressV4())){
				InvalidSetting e = new InvalidSetting(Messages.getString("message.repository.24"));
				m_log.info("validateNodeInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			// ipv4形式チェック
			try{
				InetAddress address = InetAddress.getByName(nodeInfo.getIpAddressV4());
				if (address instanceof Inet4Address){
					//IPv4の場合はさらにStringをチェック
					validateIpv4(nodeInfo.getIpAddressV4());
				} else{
					InvalidSetting e = new InvalidSetting(Messages.getString("message.repository.24") + "(" + nodeInfo.getIpAddressV4() + ")");
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}catch (UnknownHostException e) {
				InvalidSetting e1 = new InvalidSetting(Messages.getString("message.repository.24") + "(" + nodeInfo.getIpAddressV4() + ")");
				m_log.info("validateNodeInfo() : "
						+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
				throw e1;
			}
		}
		else if(ipaddressVersion == 6){
			//	versionが6の場合には、
			if(nodeInfo.getIpAddressV6() == null || "".equals(nodeInfo.getIpAddressV6())){
				InvalidSetting e = new InvalidSetting(Messages.getString("message.repository.25"));
				m_log.info("validateNodeInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			// ipv6形式チェック
			try{
				InetAddress address = InetAddress.getByName(nodeInfo.getIpAddressV6());
				if (address instanceof Inet6Address){
				} else{
					InvalidSetting e = new InvalidSetting(Messages.getString("message.repository.25") + "(" + nodeInfo.getIpAddressV6() + ")");
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} catch (UnknownHostException e) {
				InvalidSetting e1 = new InvalidSetting(Messages.getString("message.repository.25") + "(" + nodeInfo.getIpAddressV6() + ")");
				m_log.info("validateNodeInfo() : "
						+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
				throw e1;
			}
		}
		else{
			InvalidSetting e = new InvalidSetting("IpAddressVersion is not 4 / 6.");
			m_log.info("validateNodeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		//ノード名の入力チェック
		CommonValidator.validateString(Messages.getString("node.name"), nodeInfo.getNodeName(), true, 1, 128);

		// クラウド管理->クラウドサービス
		CommonValidator.validateString(Messages.getString("cloud.service"), nodeInfo.getCloudService(), false, 0, 64);
		// クラウド管理->クラウドアカウントリソース
		CommonValidator.validateString(Messages.getString("cloud.scope"), nodeInfo.getCloudScope(), false, 0, 64);
		// クラウド管理->クラウドリソースタイプ
		CommonValidator.validateString(Messages.getString("cloud.resource.type"), nodeInfo.getCloudResourceType(), false, 0, 64);
		// クラウド管理->クラウドリソースID
		CommonValidator.validateString(Messages.getString("cloud.resource.id"), nodeInfo.getCloudResourceId(), false, 0, 64);
		// クラウド管理->クラウドリソース名
		CommonValidator.validateString(Messages.getString("cloud.resource.name"), nodeInfo.getCloudResourceName(), false, 0, 64);
		// クラウド管理->クラウドロケーション
		CommonValidator.validateString(Messages.getString("cloud.location"), nodeInfo.getCloudLocation(), false, 0, 64);

		// OS名
		CommonValidator.validateString(Messages.getString("os.name"), nodeInfo.getOsName(), false, 0, 256);
		// OSリリース
		CommonValidator.validateString(Messages.getString("os.release"), nodeInfo.getOsRelease(), false, 0, 256);
		// OSバージョン
		CommonValidator.validateString(Messages.getString("os.version"), nodeInfo.getOsVersion(), false, 0, 256);
		// 文字セット
		CommonValidator.validateString(Messages.getString("character.set"), nodeInfo.getCharacterSet(), false, 0, 16);

		//デバイスの入力チェック
		if(nodeInfo.getNodeCpuInfo() != null){
			String DeviceTypeName = Messages.getString("cpu");
			List<NodeCpuEntityPK> pkList = new ArrayList<NodeCpuEntityPK>();
			for(NodeCpuInfo info : nodeInfo.getNodeCpuInfo()){
				CommonValidator.validateString(DeviceTypeName + "[" + Messages.getString("device.name") + "]", info.getDeviceName(), true, 1, 128);
				CommonValidator.validateString(DeviceTypeName + "[" + Messages.getString("device.type") + "]", info.getDeviceType(), true, 1, 32);
				CommonValidator.validateString(DeviceTypeName + "[" + Messages.getString("device.display.name") + "]", info.getDeviceDisplayName(), true, 1, 256);
				CommonValidator.validateDouble(DeviceTypeName + "[" + Messages.getString("device.index") + "]", info.getDeviceIndex(), 0, Integer.MAX_VALUE);

				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeCpuEntityPK entityPk = new NodeCpuEntityPK(
						nodeInfo.getFacilityId(),
						info.getDeviceIndex(),
						info.getDeviceType(),
						info.getDeviceName());
				if (pkList.contains(entityPk)) {
					Object[] args = { Messages.getString("cpu"), info.getDeviceName()};
					InvalidSetting e = new InvalidSetting(Messages.getString("message.repository.32", args));
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				pkList.add(entityPk);
			}
		}
		if(nodeInfo.getNodeMemoryInfo() != null){
			String DeviceTypeName = Messages.getString("memory");
			List<NodeMemoryEntityPK> pkList = new ArrayList<NodeMemoryEntityPK>();
			for(NodeMemoryInfo info : nodeInfo.getNodeMemoryInfo()){
				CommonValidator.validateString(DeviceTypeName + "[" + Messages.getString("device.name") + "]", info.getDeviceName(), true, 1, 128);
				CommonValidator.validateString(DeviceTypeName + "[" + Messages.getString("device.type") + "]", info.getDeviceType(), true, 1, 32);
				CommonValidator.validateString(DeviceTypeName + "[" + Messages.getString("device.display.name") + "]", info.getDeviceDisplayName(), true, 1, 256);
				CommonValidator.validateDouble(DeviceTypeName + "[" + Messages.getString("device.index") + "]", info.getDeviceIndex(), 0, Integer.MAX_VALUE);

				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeMemoryEntityPK entityPk = new NodeMemoryEntityPK(
						nodeInfo.getFacilityId(),
						info.getDeviceIndex(),
						info.getDeviceType(),
						info.getDeviceName());
				if (pkList.contains(entityPk)) {
					Object[] args = { Messages.getString("memory"), info.getDeviceName()};
					InvalidSetting e = new InvalidSetting(Messages.getString("message.repository.32", args));
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				pkList.add(entityPk);
			}
		}
		if(nodeInfo.getNodeDiskInfo() != null){
			String DeviceTypeName = Messages.getString("disk");
			List<NodeDiskEntityPK> pkList = new ArrayList<NodeDiskEntityPK>();
			for(NodeDiskInfo info : nodeInfo.getNodeDiskInfo()){
				CommonValidator.validateString(DeviceTypeName + "[" + Messages.getString("device.name") + "]", info.getDeviceName(), true, 1, 128);
				CommonValidator.validateString(DeviceTypeName + "[" + Messages.getString("device.type") + "]", info.getDeviceType(), true, 1, 32);
				CommonValidator.validateString(DeviceTypeName + "[" + Messages.getString("device.display.name") + "]", info.getDeviceDisplayName(), true, 1, 256);
				CommonValidator.validateDouble(DeviceTypeName + "[" + Messages.getString("device.index") + "]", info.getDeviceIndex(), 0, Integer.MAX_VALUE);

				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeDiskEntityPK entityPk = new NodeDiskEntityPK(
						nodeInfo.getFacilityId(),
						info.getDeviceIndex(),
						info.getDeviceType(),
						info.getDeviceName());
				if (pkList.contains(entityPk)) {
					Object[] args = { Messages.getString("disk"), info.getDeviceName()};
					InvalidSetting e = new InvalidSetting(Messages.getString("message.repository.32", args));
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				pkList.add(entityPk);
			}
		}
		if(nodeInfo.getNodeNetworkInterfaceInfo() != null){
			String DeviceTypeName = Messages.getString("network.interface");
			List<NodeNetworkInterfaceEntityPK> pkList = new ArrayList<NodeNetworkInterfaceEntityPK>();
			for(NodeNetworkInterfaceInfo info : nodeInfo.getNodeNetworkInterfaceInfo()){
				CommonValidator.validateString(DeviceTypeName + "[" + Messages.getString("device.name") + "]", info.getDeviceName(), true, 1, 128);
				CommonValidator.validateString(DeviceTypeName + "[" + Messages.getString("device.type") + "]", info.getDeviceType(), true, 1, 32);
				CommonValidator.validateString(DeviceTypeName + "[" + Messages.getString("device.display.name") + "]", info.getDeviceDisplayName(), true, 1, 256);
				CommonValidator.validateDouble(DeviceTypeName + "[" + Messages.getString("device.index") + "]", info.getDeviceIndex(), 0, Integer.MAX_VALUE);

				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeNetworkInterfaceEntityPK entityPk = new NodeNetworkInterfaceEntityPK(
						nodeInfo.getFacilityId(),
						info.getDeviceIndex(),
						info.getDeviceType(),
						info.getDeviceName());
				if (pkList.contains(entityPk)) {
					Object[] args = { Messages.getString("network.interface"), info.getDeviceName()};
					InvalidSetting e = new InvalidSetting(Messages.getString("message.repository.32", args));
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				pkList.add(entityPk);
			}
		}
		if(nodeInfo.getNodeFilesystemInfo() != null){
			String DeviceTypeName = Messages.getString("file.system");
			List<NodeFilesystemEntityPK> pkList = new ArrayList<NodeFilesystemEntityPK>();
			for(NodeFilesystemInfo info : nodeInfo.getNodeFilesystemInfo()){
				CommonValidator.validateString(DeviceTypeName + "[" + Messages.getString("device.name") + "]", info.getDeviceName(), true, 1, 128);
				CommonValidator.validateString(DeviceTypeName + "[" + Messages.getString("device.type") + "]", info.getDeviceType(), true, 1, 32);
				CommonValidator.validateString(DeviceTypeName + "[" + Messages.getString("device.display.name") + "]", info.getDeviceDisplayName(), true, 1, 256);
				CommonValidator.validateDouble(DeviceTypeName + "[" + Messages.getString("device.index") + "]", info.getDeviceIndex(), 0, Integer.MAX_VALUE);

				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeFilesystemEntityPK entityPk = new NodeFilesystemEntityPK(
						nodeInfo.getFacilityId(),
						info.getDeviceIndex(),
						info.getDeviceType(),
						info.getDeviceName());
				if (pkList.contains(entityPk)) {
					Object[] args = { Messages.getString("file.system"), info.getDeviceName()};
					InvalidSetting e = new InvalidSetting(Messages.getString("message.repository.32", args));
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				pkList.add(entityPk);
			}
		}
		if(nodeInfo.getNodeDeviceInfo() != null){
			String DeviceTypeName = Messages.getString("general.device");
			List<NodeDeviceEntityPK> pkList = new ArrayList<NodeDeviceEntityPK>();
			for(NodeDeviceInfo info : nodeInfo.getNodeDeviceInfo()){
				CommonValidator.validateString(DeviceTypeName + "[" + Messages.getString("device.name") + "]", info.getDeviceName(), true, 1, 128);
				CommonValidator.validateString(DeviceTypeName + "[" + Messages.getString("device.type") + "]", info.getDeviceType(), true, 1, 32);
				CommonValidator.validateString(DeviceTypeName + "[" + Messages.getString("device.display.name") + "]", info.getDeviceDisplayName(), true, 1, 256);
				CommonValidator.validateDouble(DeviceTypeName + "[" + Messages.getString("device.index") + "]", info.getDeviceIndex(), 0, Integer.MAX_VALUE);

				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeDeviceEntityPK entityPk = new NodeDeviceEntityPK(
						nodeInfo.getFacilityId(),
						info.getDeviceIndex(),
						info.getDeviceType(),
						info.getDeviceName());
				if (pkList.contains(entityPk)) {
					Object[] args = { Messages.getString("general.device"), info.getDeviceName()};
					InvalidSetting e = new InvalidSetting(Messages.getString("message.repository.32", args));
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				pkList.add(entityPk);
			}
		}
		if(nodeInfo.getNodeHostnameInfo() != null){
			List<NodeHostnameEntityPK> pkList = new ArrayList<NodeHostnameEntityPK>();
			for(NodeHostnameInfo info : nodeInfo.getNodeHostnameInfo()){
				CommonValidator.validateString(Messages.getString("host.name"), info.getHostname(), false, 0, 128);

				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeHostnameEntityPK entityPk = new NodeHostnameEntityPK(nodeInfo.getFacilityId(), info.getHostname());
				if (pkList.contains(entityPk)) {
					Object[] args = { Messages.getString("host.name"), info.getHostname()};
					InvalidSetting e = new InvalidSetting(Messages.getString("message.repository.32", args));
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				pkList.add(entityPk);
			}
		}
		if(nodeInfo.getNodeVariableInfo() != null){
			List<NodeVariableEntityPK> pkList = new ArrayList<NodeVariableEntityPK>();
			for(NodeVariableInfo variable : nodeInfo.getNodeVariableInfo()){
				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeVariableEntityPK entityPk = new NodeVariableEntityPK(nodeInfo.getFacilityId(), variable.getNodeVariableName());
				if (pkList.contains(entityPk)) {
					Object[] args = { Messages.getString("node.variable"), variable.getNodeVariableName()};
					InvalidSetting e = new InvalidSetting(Messages.getString("message.repository.32", args));
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				pkList.add(entityPk);
			}
		}

		//サービスのチェック(SNMP)
		CommonValidator.validateString(Messages.getString("community.name"), nodeInfo.getSnmpCommunity(), false, 0, 64);
		if(nodeInfo.getSnmpVersion() == null
				|| (!"".equals(nodeInfo.getSnmpVersion())
						&& !SnmpVersionConstant.STRING_V1.equals(nodeInfo.getSnmpVersion())
						&& !SnmpVersionConstant.STRING_V2.equals(nodeInfo.getSnmpVersion())
						&& !SnmpVersionConstant.STRING_V3.equals(nodeInfo.getSnmpVersion()))){
			InvalidSetting e = new InvalidSetting("SNMP Version is " + SnmpVersionConstant.STRING_V1 + " or " + SnmpVersionConstant.STRING_V2);
			m_log.info("validateNodeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		if(SnmpVersionConstant.STRING_V3.equals(nodeInfo.getSnmpVersion()) &&
				(SnmpSecurityLevelConstant.AUTH_NOPRIV.equals(nodeInfo.getSnmpSecurityLevel())
					|| SnmpSecurityLevelConstant.AUTH_PRIV.equals(nodeInfo.getSnmpSecurityLevel()))) {
			CommonValidator.validateString(Messages.getString("snmp.auth.password"), nodeInfo.getSnmpAuthPassword(), true, 8, 64);
			String auth = nodeInfo.getSnmpAuthProtocol();
			if (auth == null || !SnmpProtocolConstant.getAuthProtocol().contains(auth)) {
				Object[] args = { Messages.getString("snmp.auth.protocol") };
				InvalidSetting e = new InvalidSetting(Messages.getString("message.common.1", args));
				m_log.info("validateNodeInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} else {
			CommonValidator.validateString(Messages.getString("snmp.auth.password"), nodeInfo.getSnmpAuthPassword(), false, 0, 64);
		}
		if(SnmpVersionConstant.STRING_V3.equals(nodeInfo.getSnmpVersion()) &&
				(SnmpSecurityLevelConstant.AUTH_PRIV.equals(nodeInfo.getSnmpSecurityLevel()))) {
			CommonValidator.validateString(Messages.getString("snmp.priv.password"), nodeInfo.getSnmpPrivPassword(), true, 8, 64);
			String priv = nodeInfo.getSnmpPrivProtocol();
			if (priv == null || !SnmpProtocolConstant.getPrivProtocol().contains(priv)) {
				Object[] args = { Messages.getString("snmp.priv.protocol") };
				InvalidSetting e = new InvalidSetting(Messages.getString("message.common.1", args));
				m_log.info("validateNodeInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} else {
			CommonValidator.validateString(Messages.getString("snmp.priv.password"), nodeInfo.getSnmpPrivPassword(), false, 0, 64);
		}

		CommonValidator.validateString(Messages.getString("snmp.user"),
				nodeInfo.getSnmpUser(), false, 0, 64);
		CommonValidator.validateInt(Messages.getString("snmp.retries"),
				nodeInfo.getSnmpRetryCount(), 1, 10);
		CommonValidator.validateInt(Messages.getString("snmp.timeout"),
				nodeInfo.getSnmpTimeout(), 1, Integer.MAX_VALUE);

		//サービスのチェック(WBEM)
		if(nodeInfo.getWbemProtocol() == null
				|| (!"".equals(nodeInfo.getWbemProtocol())
						&& !"http".equals(nodeInfo.getWbemProtocol())
						&& !"https".equals(nodeInfo.getWbemProtocol()))){
			InvalidSetting e = new InvalidSetting("WBEM Protocol is http or https");
			m_log.info("validateNodeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(Messages.getString("wbem.user"),
				nodeInfo.getWbemUser(), false, 0, 64);
		CommonValidator.validateString(Messages.getString("wbem.user.password"),
				nodeInfo.getWbemUserPassword(), false, 0, 64);
		CommonValidator.validateInt(Messages.getString("wbem.retries"),
				nodeInfo.getWbemRetryCount(), 1, 10);
		CommonValidator.validateInt(Messages.getString("wbem.timeout"),
				nodeInfo.getWbemTimeout(), 1, Integer.MAX_VALUE);

		//サービスのチェック(IPMI)
		CommonValidator.validateString(Messages.getString("ipmi.protocol"), nodeInfo.getIpmiProtocol(), false, 0, 32);
		CommonValidator.validateString(Messages.getString("ipmi.level"), nodeInfo.getIpmiLevel(), false, 0, 32);
		CommonValidator.validateString(Messages.getString("ipmi.user"),
				nodeInfo.getIpmiUser(), false, 0, 64);
		CommonValidator.validateString(Messages.getString("ipmi.user.password"),
				nodeInfo.getIpmiUserPassword(), false, 0, 64);
		CommonValidator.validateInt(Messages.getString("ipmi.retries"),
				nodeInfo.getIpmiRetries(), 1, 10);
		CommonValidator.validateInt(Messages.getString("ipmi.timeout"),
				nodeInfo.getIpmiTimeout(), 1, Integer.MAX_VALUE);

		//サービスのチェック(WinRM)
		if(nodeInfo.getWinrmProtocol() == null
				|| (!"".equals(nodeInfo.getWinrmProtocol())
						&& !"http".equals(nodeInfo.getWinrmProtocol())
						&& !"https".equals(nodeInfo.getWinrmProtocol()))){
			InvalidSetting e = new InvalidSetting("WinRM Protocol is http or https");
			m_log.info("validateNodeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(Messages.getString("winrm.user"),
				nodeInfo.getWinrmUser(), false, 0, 64);
		CommonValidator.validateString(Messages.getString("winrm.user.password"),
				nodeInfo.getWinrmUserPassword(), false, 0, 64);
		CommonValidator.validateInt(Messages.getString("winrm.retries"),
				nodeInfo.getWinrmRetries(), 1, 10);
		CommonValidator.validateInt(Messages.getString("winrm.timeout"),
				nodeInfo.getWinrmTimeout(), 1, Integer.MAX_VALUE);

		// administrator
		CommonValidator.validateString(Messages.getString("administrator"), nodeInfo.getAdministrator(), false, 0, 256);

	}

	/**
	 * スコープ情報の妥当性チェック
	 *
	 * @param scopeInfo
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateScopeInfo(String parentFacilityId, ScopeInfo scopeInfo, boolean parentCheck)
			throws InvalidSetting, InvalidRole {

		validateFacilityInfo(scopeInfo);

		// parentFacilityId
		if(parentCheck){
			if(parentFacilityId != null && parentFacilityId.compareTo("") != 0){
				try{
					QueryUtil.getFacilityPK(parentFacilityId, ObjectPrivilegeMode.MODIFY);
				} catch (FacilityNotFound e) {
					InvalidSetting e1 = new InvalidSetting("Scope does not exist! facilityId = " + parentFacilityId);
					m_log.info("validateScopeInfo() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				} catch (InvalidRole e) {
					throw e;
				}
			}
		}

		// facilityType
		if(scopeInfo.getFacilityType() != FacilityConstant.TYPE_SCOPE){
			InvalidSetting e = new InvalidSetting("Scope FacilityType is  " + scopeInfo.getFacilityType());
			m_log.info("validateScopeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	public static void validateFacilityInfo (FacilityInfo facilityInfo) throws InvalidSetting {
		// facilityId
		CommonValidator.validateId(Messages.getString("facility.id"), facilityInfo.getFacilityId(), 512);

		// facilityName
		CommonValidator.validateString(Messages.getString("facility.name"), facilityInfo.getFacilityName(), true, 1, 128);

		// description
		CommonValidator.validateString(Messages.getString("description"), facilityInfo.getDescription(), false, 0, 256);

		// ownerRoleId
		CommonValidator.validateOwnerRoleId(facilityInfo.getOwnerRoleId(), true,
				facilityInfo.getFacilityId(), HinemosModuleConstant.PLATFORM_REPOSITORY);
	}

	/**
	 * ノード割り当て時のチェック
	 *
	 * @param parentFacilityId
	 * @param facilityIds
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateaAssignNodeScope(String parentFacilityId, String[] facilityIds)
			throws InvalidSetting, InvalidRole {
		// parentFacilityId
		try{
			QueryUtil.getFacilityPK(parentFacilityId, ObjectPrivilegeMode.MODIFY);
		} catch (FacilityNotFound e) {
			InvalidSetting e1 = new InvalidSetting("Scope does not exist! facilityId = " + parentFacilityId);
			m_log.info("validateaAssignNodeScope() : "
					+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
			throw e1;
		} catch (InvalidRole e) {
			throw e;
		}

		// facilityIds
		if(facilityIds == null || facilityIds.length == 0){
			InvalidSetting e = new InvalidSetting(Messages.getString("message.repository.2"));
			m_log.info("validateaAssignNodeScope() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		for (int i = 0; i < facilityIds.length; i++) {
			try{
				NodeProperty.getProperty(facilityIds[i]);
			} catch (FacilityNotFound e) {
				InvalidSetting e1 = new InvalidSetting("Node does not exist! facilityId = " + facilityIds[i]);
				m_log.info("validateaAssignNodeScope() : "
						+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
				throw e1;
			}
		}
	}

}
