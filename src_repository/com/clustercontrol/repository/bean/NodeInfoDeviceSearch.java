/*

Copyright (C) since 2009 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.repository.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.util.Messages;

/**
 * このクラスはDeviceSearchの更新情報を持つクラスです。
 * DeviceSearchでノード情報を取得した場合に使用します。
 *
 * @since 5.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class NodeInfoDeviceSearch implements Serializable
{
	private static final long serialVersionUID = -6677435738596727809L;

	private NodeInfo nodeInfo = null;
	private NodeInfo newNodeInfo = null;
	private ArrayList<DeviceSearchMessageInfo> deviceSearchMessageInfo = new ArrayList<DeviceSearchMessageInfo>();
	private static String itemCpu = Messages.getString("cpu.list");
	private static String itemMem = Messages.getString("memory.list");
	private static String itemNic = Messages.getString("network.interface.list");
	private static String itemDisk = Messages.getString("disk.list");
	private static String itemFileSys = Messages.getString("file.system.list");
	private static String itemGenDev = Messages.getString("general.device.list");
	private NodeInfo errorNodeInfo = null;

	/**
	 * 登録時エラーのあったノード詳細情報のsetter
	 * @param nodeInfo
	 */
	public void setErrorNodeInfo(NodeInfo info) {
		this.errorNodeInfo = info;
	}

	/**
	 * 登録時エラーのあったノード詳細情報のgetter
	 * @return nodeInfo
	 */
	public NodeInfo getErrorNodeInfo() {
		return this.errorNodeInfo;
	}

	/**
	 * ノード詳細情報のsetter
	 * @param nodeInfo
	 */
	public void setNodeInfo(NodeInfo nodeInfo) {
		nodeInfo.setDefaultInfo();
		this.nodeInfo = nodeInfo;
	}

	/**
	 * ノード詳細情報のgetter
	 * @return nodeInfo
	 */
	public NodeInfo getNodeInfo() {
		return this.nodeInfo;
	}

	/**
	 * 取得後のノード詳細情報のgetter
	 * @return newNodeInfo
	 */
	public NodeInfo getNewNodeInfo() {
		return this.newNodeInfo;
	}

	/**
	 * メッセージのsetter
	 */
	public void setDeviceSearchMessageInfo(ArrayList<DeviceSearchMessageInfo> deviceSearchMessageInfo) {
		this.deviceSearchMessageInfo = deviceSearchMessageInfo;
	}

	/**
	 * メッセージのgetter
	 */
	public ArrayList<DeviceSearchMessageInfo> getDeviceSearchMessageInfo() {
		return this.deviceSearchMessageInfo;
	}

	private void setMessage(String item, String lastVal, String thisVal) {
		DeviceSearchMessageInfo msgInfo = new DeviceSearchMessageInfo();
		msgInfo.setItemName(item);
		msgInfo.setLastVal(lastVal);
		msgInfo.setThisVal(thisVal);
		deviceSearchMessageInfo.add(msgInfo);
	}

	/**
	 * ノード詳細情報が等しいかを返す
	 * 検出対象でない場合は前回情報に置き換え
	 * @param lastNode 前回のノード詳細情報
	 * @return 検出対象の場合、前回と同一か
	 */
	public boolean equalsNodeInfo(NodeInfo lastNode) {
		boolean equalsBasic = true;
		boolean equalsCpu = true;
		boolean equalsMem = true;
		boolean equalsNic = true;
		boolean equalsFs = true;
		boolean equalsDisk = true;
		boolean equalsDevice = true;

		//サーバ基本情報
		equalsBasic = equalsNodeBasicInfo(lastNode);

		//CPU情報
		if (HinemosPropertyUtil.getHinemosPropertyBool("repository.device.search.prop.device.cpu", true)) {
			Set<NodeCpuInfo> last = new HashSet<NodeCpuInfo>();
			if (lastNode.getNodeCpuInfo() != null) {
				last.addAll(lastNode.getNodeCpuInfo());
			}
			
			Set<NodeCpuInfo> current = new HashSet<NodeCpuInfo>();
			if (nodeInfo.getNodeCpuInfo() != null) {
				current.addAll(nodeInfo.getNodeCpuInfo());
				for (NodeCpuInfo cpu : last) {
					if (cpu != null && ! "cpu".equals(cpu.getDeviceType())) {
						// cpu以外のものはSearchされないため、非更新対象として扱う
						current.add(cpu);
					}
				}
			}
			
			if (last.size() == 0) {
				equalsCpu = current.size() == 0 ? true : false;
				if(equalsCpu == false) {
					//前回ノード情報:なし 今回ノード情報:あり
					setMessage(itemCpu, Messages.getString("nonexistent"), Messages.getString("existent"));
				}
			} else if (current.size() == 0) {
				equalsCpu = false;
				//前回ノード情報:あり 今回ノード情報:なし
				setMessage(itemCpu, Messages.getString("existent"), Messages.getString("nonexistent"));
			} else if (last.equals(current)) {
				equalsCpu = true;
			} else {
				equalsCpu = equalsWithMap(itemCpu, new ArrayList<NodeDeviceInfo>(last), new ArrayList<NodeDeviceInfo>(current));
			}
			
			nodeInfo.getNodeCpuInfo().clear();
			nodeInfo.getNodeCpuInfo().addAll(current);
		}

		//メモリ情報
		if (HinemosPropertyUtil.getHinemosPropertyBool("repository.device.search.prop.device.memory", true)) {
			Set<NodeMemoryInfo> last = new HashSet<NodeMemoryInfo>();
			if (lastNode.getNodeMemoryInfo() != null) {
				last.addAll(lastNode.getNodeMemoryInfo());
			}
			
			Set<NodeMemoryInfo> current = new HashSet<NodeMemoryInfo>();
			if (nodeInfo.getNodeMemoryInfo() != null) {
				current.addAll(nodeInfo.getNodeMemoryInfo());
				for (NodeMemoryInfo mem : last) {
					if (mem != null && ! "mem".equals(mem.getDeviceType())) {
						// mem以外のものはSearchされないため、非更新対象として扱う
						current.add(mem);
					}
				}
			}
			
			if (last.size() == 0) {
				equalsMem = current.size() == 0 ? true : false;
				if(!equalsMem) {
					//前回ノード情報:なし 今回ノード情報:あり
					setMessage(itemMem, Messages.getString("nonexistent"), Messages.getString("existent"));
				}
			} else if (current.size() == 0) {
				equalsMem = false;
				//前回ノード情報:あり 今回ノード情報:なし
				setMessage(itemMem, Messages.getString("existent"), Messages.getString("nonexistent"));
			} else if (last.equals(current)) {
				equalsMem = true;
			} else {
				equalsMem = equalsWithMap(itemMem, new ArrayList<NodeDeviceInfo>(last), new ArrayList<NodeDeviceInfo>(current));
			}
			
			nodeInfo.getNodeMemoryInfo().clear();
			nodeInfo.getNodeMemoryInfo().addAll(current);
		}

		//NIC情報
		if (HinemosPropertyUtil.getHinemosPropertyBool("repository.device.search.prop.device.nic", true)) {
			Set<NodeNetworkInterfaceInfo> last = new HashSet<NodeNetworkInterfaceInfo>();
			if (lastNode.getNodeNetworkInterfaceInfo() != null) {
				last.addAll(lastNode.getNodeNetworkInterfaceInfo());
			}
			
			Set<NodeNetworkInterfaceInfo> current = new HashSet<NodeNetworkInterfaceInfo>();
			if (nodeInfo.getNodeNetworkInterfaceInfo() != null) {
				current.addAll(nodeInfo.getNodeNetworkInterfaceInfo());
				for (NodeNetworkInterfaceInfo nic : last) {
					if (nic != null && ! "nic".equals(nic.getDeviceType())) {
						// nic以外のものはSearchされないため、非更新対象として扱う
						current.add(nic);
					}
				}
			}
			
			//NIC情報
			if (last.size() == 0) {
				equalsNic = current.size() == 0 ? true : false;
				if(! equalsNic) {
					//前回ノード情報:なし 今回ノード情報:あり
					setMessage(itemNic, Messages.getString("nonexistent"), Messages.getString("existent"));
				}
			} else if (current.size() == 0) {
				equalsNic = false;
				//前回ノード情報:あり 今回ノード情報:なし
				setMessage(itemNic, Messages.getString("existent"), Messages.getString("nonexistent"));
			} else if (last.equals(current)) {
				equalsNic = true;
			} else {
				equalsNic = equalsWithMap(itemNic, new ArrayList<NodeDeviceInfo>(last), new ArrayList<NodeDeviceInfo>(current));
			}
			
			nodeInfo.getNodeNetworkInterfaceInfo().clear();
			nodeInfo.getNodeNetworkInterfaceInfo().addAll(current);
		}

		//ディスク情報
		if (HinemosPropertyUtil.getHinemosPropertyBool("repository.device.search.prop.device.disk", true)) {
			Set<NodeDiskInfo> last = new HashSet<NodeDiskInfo>();
			if (lastNode.getNodeDiskInfo() != null) {
				last.addAll(lastNode.getNodeDiskInfo());
			}
			
			Set<NodeDiskInfo> current = new HashSet<NodeDiskInfo>();
			if (nodeInfo.getNodeDiskInfo() != null) {
				current.addAll(nodeInfo.getNodeDiskInfo());
				for (NodeDiskInfo disk : last) {
					if (disk != null && ! "disk".equals(disk.getDeviceType())) {
						// disk以外のものはSearchされないため、非更新対象として扱う
						current.add(disk);
					}
				}
			}
			
			if (last.size() == 0) {
				equalsDisk = current.size() == 0 ? true : false;
				if(! equalsDisk) {
					//前回ノード情報:なし 今回ノード情報:あり
					setMessage(itemDisk, Messages.getString("nonexistent"), Messages.getString("existent"));
				}
			} else if (current.size() == 0) {
				equalsDisk = false;
				//前回ノード情報:あり 今回ノード情報:なし
				setMessage(itemDisk, Messages.getString("existent"), Messages.getString("nonexistent"));
			} else if (last.equals(current)) {
				equalsDisk = true;
			} else {
				equalsDisk = equalsWithMap(itemDisk, new ArrayList<NodeDeviceInfo>(last), new ArrayList<NodeDeviceInfo>(current));
			}
			
			nodeInfo.getNodeDiskInfo().clear();
			nodeInfo.getNodeDiskInfo().addAll(current);
		}

		//ファイルシステム情報
		if (HinemosPropertyUtil.getHinemosPropertyBool("repository.device.search.prop.device.filesystem", true)) {
			Set<NodeFilesystemInfo> last = new HashSet<NodeFilesystemInfo>();
			if (lastNode.getNodeFilesystemInfo() != null) {
				last.addAll(lastNode.getNodeFilesystemInfo());
			}
			
			Set<NodeFilesystemInfo> current = new HashSet<NodeFilesystemInfo>();
			if (nodeInfo.getNodeFilesystemInfo() != null) {
				current.addAll(nodeInfo.getNodeFilesystemInfo());
				for (NodeFilesystemInfo fs : last) {
					if (fs != null && ! "filesystem".equals(fs.getDeviceType())) {
						// filesystem以外のものはSearchされないため、非更新対象として扱う
						current.add(fs);
					}
				}
			}
			
			if (last.size() == 0) {
				equalsFs = current.size() == 0 ? true : false;
				if(!equalsFs) {
					//前回ノード情報:なし 今回ノード情報:あり
					setMessage(itemFileSys, Messages.getString("nonexistent"), Messages.getString("existent"));
				}
			} else if (current.size() == 0) {
				equalsFs = false;
				//前回ノード情報:あり 今回ノード情報:なし
				setMessage(itemFileSys, Messages.getString("existent"), Messages.getString("nonexistent"));
			} else if (last.equals(current)) {
				equalsFs = true;
			} else {
				equalsFs = equalsWithMap(itemFileSys, new ArrayList<NodeDeviceInfo>(last), new ArrayList<NodeDeviceInfo>(current));
			}
			
			nodeInfo.getNodeFilesystemInfo().clear();
			nodeInfo.getNodeFilesystemInfo().addAll(current);
		}

		//汎用デバイス情報(自動検出されないため、trueにすると必ず削除されることを注意せよ）
		if (HinemosPropertyUtil.getHinemosPropertyBool("repository.device.search.prop.device.general", false)) {
			Set<NodeDeviceInfo> last = new HashSet<NodeDeviceInfo>();
			if (lastNode.getNodeDeviceInfo() != null) {
				last.addAll(lastNode.getNodeDeviceInfo());
			}
			
			Set<NodeDeviceInfo> current = new HashSet<NodeDeviceInfo>();
			if (nodeInfo.getNodeDeviceInfo() != null) {
				current.addAll(nodeInfo.getNodeDeviceInfo());
			}
			
			if (last.size() == 0) {
				equalsDevice = current.size() == 0 ? true : false;
				if(!equalsDevice) {
					//前回ノード情報:なし 今回ノード情報:あり
					setMessage(itemGenDev, Messages.getString("nonexistent"), Messages.getString("existent"));
				}
			} else if (current.size() == 0) {
				equalsDevice = false;
				//前回ノード情報:あり 今回ノード情報:なし
				setMessage(itemGenDev, Messages.getString("existent"), Messages.getString("nonexistent"));
			} else if (last.equals(current)) {
				equalsDevice = equalsDevice  && true;
			} else {
				equalsDevice = equalsWithMap(itemGenDev, new ArrayList<NodeDeviceInfo>(last), new ArrayList<NodeDeviceInfo>(current));
			}
		}

		boolean equals = equalsBasic && equalsCpu && equalsMem && equalsNic && equalsDisk
				&& equalsFs && equalsDevice;

		if (equals == false) {
			//前回情報をベースとする
			this.newNodeInfo = lastNode.clone();
			//取得した情報で更新
			if (equalsBasic == false) {
				// サーバ基本情報->ハードウェア
				if (HinemosPropertyUtil.getHinemosPropertyBool("repository.device.search.prop.basic.hardware", false)) {
					this.newNodeInfo.setPlatformFamily(this.nodeInfo.getPlatformFamily());
					this.newNodeInfo.setSubPlatformFamily(this.nodeInfo.getSubPlatformFamily());
					this.newNodeInfo.setHardwareType(this.nodeInfo.getHardwareType());
					this.newNodeInfo.setIconImage(this.nodeInfo.getIconImage());
				}
				// サーバ基本情報->ネットワーク
				if (HinemosPropertyUtil.getHinemosPropertyBool("repository.device.search.prop.basic.network", true)) {
					this.newNodeInfo.setIpAddressVersion(this.nodeInfo.getIpAddressVersion());
					this.newNodeInfo.setIpAddressV4(this.nodeInfo.getIpAddressV4());
					this.newNodeInfo.setIpAddressV6(this.nodeInfo.getIpAddressV6());
					this.newNodeInfo.setNodeHostnameInfo(this.nodeInfo.getNodeHostnameInfo());
				}
				// サーバ基本情報->OS
				if (HinemosPropertyUtil.getHinemosPropertyBool("repository.device.search.prop.basic.os", false)) {
					this.newNodeInfo.setNodeName(this.nodeInfo.getNodeName());
					this.newNodeInfo.setOsName(this.nodeInfo.getOsName());
					this.newNodeInfo.setOsRelease(this.nodeInfo.getOsRelease());
					this.newNodeInfo.setOsVersion(this.nodeInfo.getOsVersion());
					this.newNodeInfo.setCharacterSet(this.nodeInfo.getCharacterSet());
				}
				// サーバ基本情報->Hinemosエージェント
				if (HinemosPropertyUtil.getHinemosPropertyBool("repository.device.search.prop.basic.agent", false)) {
					this.newNodeInfo.setAgentAwakePort(this.nodeInfo.getAgentAwakePort());
				}
			}
			if (equalsCpu == false) {
				this.newNodeInfo.setNodeCpuInfo(this.nodeInfo.getNodeCpuInfo());
			}
			if (equalsMem == false) {
				this.newNodeInfo.setNodeMemoryInfo(this.nodeInfo.getNodeMemoryInfo());
			}
			if (equalsNic == false) {
				this.newNodeInfo.setNodeNetworkInterfaceInfo(this.nodeInfo.getNodeNetworkInterfaceInfo());
			}
			if (equalsDisk == false) {
				this.newNodeInfo.setNodeDiskInfo(this.nodeInfo.getNodeDiskInfo());
			}
			if (equalsFs == false) {
				this.newNodeInfo.setNodeFilesystemInfo(this.nodeInfo.getNodeFilesystemInfo());
			}
			if (equalsDevice == false) {
				this.newNodeInfo.setNodeDeviceInfo(this.nodeInfo.getNodeDeviceInfo());
			}
		}

		return equals;
	}

	/**
	 * 基本情報が検出対象の場合は前回情報と比較
	 * 検出対象でない場合は前回情報に置き換え
	 * @param lastNode
	 * @return 検出対象の場合、前回と同一か
	 */
	private boolean equalsNodeBasicInfo(NodeInfo lastNode) {
		boolean lEquals = true;

		if (HinemosPropertyUtil.getHinemosPropertyBool("repository.device.search.prop.basic.hardware", false)) {
			// HW
			if( nodeInfo.getPlatformFamily() == null ) {
				lEquals = lEquals && ( lastNode.getPlatformFamily() == null );
				if (lastNode.getPlatformFamily() != null) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("hardware") + "." + Messages.getString("platform.family.name"),
							Messages.getString("existent"),
							Messages.getString("nonexistent"));
				}
			} else {
				lEquals = lEquals && nodeInfo.getPlatformFamily().equals( lastNode.getPlatformFamily() );
				if (nodeInfo.getPlatformFamily().equals( lastNode.getPlatformFamily()) == false) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("hardware") + "." + Messages.getString("platform.family.name"),
							lastNode.getPlatformFamily(),
							nodeInfo.getPlatformFamily());
				}
			}

			if( nodeInfo.getSubPlatformFamily() == null ) {
				lEquals = lEquals && ( lastNode.getSubPlatformFamily() == null );
				if (lastNode.getSubPlatformFamily() != null) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("hardware") + "." + Messages.getString("sub.platform.family.name"),
							Messages.getString("existent"),
							Messages.getString("nonexistent"));
				}
			} else {
				lEquals = lEquals && nodeInfo.getSubPlatformFamily().equals( lastNode.getSubPlatformFamily() );
				if (nodeInfo.getSubPlatformFamily().equals( lastNode.getSubPlatformFamily()) == false) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("hardware") + "." + Messages.getString("sub.platform.family.name"),
							lastNode.getSubPlatformFamily(),
							nodeInfo.getSubPlatformFamily());
				}
			}

			if( nodeInfo.getHardwareType() == null ) {
				lEquals = lEquals && ( lastNode.getHardwareType() == null );
				if (lastNode.getHardwareType() != null) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("hardware") + "." + Messages.getString("hardware.type"),
							Messages.getString("existent"),
							Messages.getString("nonexistent"));
				}
			} else {
				lEquals = lEquals && nodeInfo.getHardwareType().equals( lastNode.getHardwareType() );
				if (nodeInfo.getHardwareType().equals( lastNode.getHardwareType()) == false) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("hardware") + "." + Messages.getString("hardware.type"),
							lastNode.getHardwareType(),
							nodeInfo.getHardwareType());
				}
			}

			if( nodeInfo.getIconImage() == null ) {
				lEquals = lEquals && ( lastNode.getIconImage() == null );
				if (lastNode.getIconImage() != null) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("hardware") + "." + Messages.getString("icon.image"),
							Messages.getString("existent"),
							Messages.getString("nonexistent"));
				}
			} else {
				lEquals = lEquals && nodeInfo.getIconImage().equals( lastNode.getIconImage() );
				if (nodeInfo.getIconImage().equals( lastNode.getIconImage()) == false) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("hardware") + "." + Messages.getString("icon.image"),
							lastNode.getIconImage(),
							nodeInfo.getIconImage());
				}
			}
		}

		// IP アドレス
		if (HinemosPropertyUtil.getHinemosPropertyBool("repository.device.search.prop.basic.network", true)) {
			if( nodeInfo.getIpAddressVersion() == null )
			{
				lEquals = lEquals && ( lastNode.getIpAddressVersion() == null );
				if (lastNode.getIpAddressVersion() != null) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("network") + "." + Messages.getString("ip.address.version"),
							Messages.getString("existent"),
							Messages.getString("nonexistent"));
				}
			} else {
				lEquals = lEquals && nodeInfo.getIpAddressVersion().equals( lastNode.getIpAddressVersion() );
				if (nodeInfo.getIpAddressVersion().equals( lastNode.getIpAddressVersion() ) == false) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("network") + "." + Messages.getString("ip.address.version"),
							lastNode.getIpAddressVersion().toString(),
							nodeInfo.getIpAddressVersion().toString());
				}
			}
			if( nodeInfo.getIpAddressV4() == null )
			{
				lEquals = lEquals && ( lastNode.getIpAddressV4() == null );
			} else {
				lEquals = lEquals && nodeInfo.getIpAddressV4().equals( lastNode.getIpAddressV4() );
				if (nodeInfo.getIpAddressV4().equals( lastNode.getIpAddressV4() ) == false) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("network") + "." + Messages.getString("ip.address.v4"),
							lastNode.getIpAddressV4(),
							nodeInfo.getIpAddressV4());
				}
			}
			if( nodeInfo.getIpAddressV6() == null )
			{
				lEquals = lEquals && ( lastNode.getIpAddressV6() == null );
			} else {
				lEquals = lEquals && nodeInfo.getIpAddressV6().equals( lastNode.getIpAddressV6() );
				if (nodeInfo.getIpAddressV6().equals( lastNode.getIpAddressV6() ) == false) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("network") + "." + Messages.getString("ip.address.v6"),
							lastNode.getIpAddressV6(),
							nodeInfo.getIpAddressV6());
				}
			}
			if( nodeInfo.getNodeHostnameInfo() == null || nodeInfo.getNodeHostnameInfo().isEmpty() )
			{
				lEquals = lEquals && ( lastNode.getNodeHostnameInfo() == null || lastNode.getNodeHostnameInfo().isEmpty());
			} else {
				lEquals = lEquals
						&& nodeInfo
								.getNodeHostnameInfo()
								.get(0)
								.getHostname()
								.equals(lastNode.getNodeHostnameInfo().get(0)
										.getHostname());
				if (nodeInfo
						.getNodeHostnameInfo()
						.get(0)
						.getHostname()
						.equals(lastNode.getNodeHostnameInfo().get(0)
								.getHostname()) == false) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("network") + "." + Messages.getString("host.name"),
							lastNode.getNodeHostnameInfo().get(0).getHostname(),
							nodeInfo.getNodeHostnameInfo().get(0).getHostname());
				}
			}
		}

		// OS
		if (HinemosPropertyUtil.getHinemosPropertyBool("repository.device.search.prop.basic.os", false)) {
			if( nodeInfo.getNodeName() == null )
			{
				lEquals = lEquals && ( lastNode.getNodeName() == null );
			} else {
				lEquals = lEquals && nodeInfo.getNodeName().equals( lastNode.getNodeName() );
				if (nodeInfo.getNodeName().equals( lastNode.getNodeName() ) == false) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("os") + "." + Messages.getString("node.name"),
							lastNode.getNodeName(),
							nodeInfo.getNodeName());
				}
			}
			if( nodeInfo.getOsName() == null )
			{
				lEquals = lEquals && ( lastNode.getOsName() == null );
				if (lastNode.getOsName() != null) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("os") + "." + Messages.getString("os.name"),
							Messages.getString("existent"),
							Messages.getString("nonexistent"));
				}
			} else {
				lEquals = lEquals && nodeInfo.getOsName().equals( lastNode.getOsName() );
				if (nodeInfo.getOsName().equals( lastNode.getOsName() ) == false ) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("os") + "." + Messages.getString("os.name"),
							lastNode.getOsName(),
							nodeInfo.getOsName());
				}
			}
			if( nodeInfo.getOsRelease() == null )
			{
				lEquals = lEquals && ( lastNode.getOsRelease() == null );
				if (lastNode.getOsRelease() != null) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("os") + "." + Messages.getString("os.release"),
							Messages.getString("existent"),
							Messages.getString("nonexistent"));
				}
			} else {
				lEquals = lEquals && nodeInfo.getOsRelease().equals( lastNode.getOsRelease() );
				if (nodeInfo.getOsRelease().equals( lastNode.getOsRelease() ) == false ) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("os") + "." + Messages.getString("os.release"),
							lastNode.getOsRelease(),
							nodeInfo.getOsRelease());
				}
			}
			if( nodeInfo.getOsVersion() == null )
			{
				lEquals = lEquals && ( lastNode.getOsVersion() == null );
				if (lastNode.getOsVersion() != null) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("os") + "." + Messages.getString("os.version"),
							Messages.getString("existent"),
							Messages.getString("nonexistent"));
				}
			} else {
				lEquals = lEquals && nodeInfo.getOsVersion().equals( lastNode.getOsVersion() );
				if (nodeInfo.getOsVersion().equals( lastNode.getOsVersion() ) == false ) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("os") + "." + Messages.getString("os.version"),
							lastNode.getOsVersion(),
							nodeInfo.getOsVersion());
				}
			}
			if( nodeInfo.getCharacterSet() == null )
			{
				lEquals = lEquals && ( lastNode.getCharacterSet() == null );
				if (lastNode.getCharacterSet() != null) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("os") + "." + Messages.getString("character.set"),
							Messages.getString("existent"),
							Messages.getString("nonexistent"));
				}
			} else {
				lEquals = lEquals && nodeInfo.getCharacterSet().equals( lastNode.getCharacterSet() );
				if (nodeInfo.getCharacterSet().equals( lastNode.getCharacterSet() ) == false ) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("os") + "." + Messages.getString("character.set"),
							lastNode.getCharacterSet(),
							nodeInfo.getCharacterSet());
				}
			}
		}

		// Hinemosエージェント
		if (HinemosPropertyUtil.getHinemosPropertyBool("repository.device.search.prop.basic.agent", false)) {
			if( nodeInfo.getAgentAwakePort() == null )
			{
				lEquals = lEquals && ( lastNode.getAgentAwakePort() == null );
				if (lastNode.getAgentAwakePort() != null) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("agent") + "." + Messages.getString("agent.awake.port"),
							Messages.getString("existent"),
							Messages.getString("nonexistent"));
				}
			} else {
				lEquals = lEquals && nodeInfo.getAgentAwakePort().equals( lastNode.getAgentAwakePort() );
				if (nodeInfo.getAgentAwakePort().equals( lastNode.getAgentAwakePort() ) == false ) {
					setMessage(Messages.getString("basic.information") + "." + Messages.getString("agent") + "." + Messages.getString("agent.awake.port"),
									lastNode.getAgentAwakePort().toString(),
									nodeInfo.getAgentAwakePort().toString());
				}
			}
		}

		return lEquals;
	}

	private boolean equalsNodeDeviceInfo(String itemNm,
			NodeDeviceInfo lastDI, NodeDeviceInfo thisDI) {
		boolean equalsDevice = true;

		if (!lastDI.getDeviceDescription().equals(thisDI.getDeviceDescription())) {
			equalsDevice = false;
			String item = itemNm + "." + Messages.getString("description");
			setMessage(item, lastDI.getDeviceDescription(), thisDI.getDeviceDescription());
		}
		if (!lastDI.getDeviceDisplayName().equals(thisDI.getDeviceDisplayName())) {
			equalsDevice = false;
			String item = itemNm + "." + Messages.getString("device.display.name");
			setMessage(item, lastDI.getDeviceDisplayName(), thisDI.getDeviceDisplayName());
		}
		if (!lastDI.getDeviceIndex().equals(thisDI.getDeviceIndex())) {
			equalsDevice = false;
			String item = itemNm + "." + Messages.getString("device.index");
			setMessage(
							item,
							lastDI.getDeviceIndex() == null ? "" : lastDI.getDeviceIndex().toString(),
							thisDI.getDeviceIndex() == null ? "" : thisDI.getDeviceIndex().toString());
		}
		if (!lastDI.getDeviceName().equals(thisDI.getDeviceName())) {
			equalsDevice = false;
			String item = itemNm + "." + Messages.getString("device.name");
			setMessage(item, lastDI.getDeviceName(), thisDI.getDeviceName());
		}
		if (!lastDI.getDeviceSize().equals(thisDI.getDeviceSize())) {
			equalsDevice = false;
			String item = itemNm + "." + Messages.getString("device.size");
			setMessage(
							item,
							lastDI.getDeviceSize() == null ? "" : lastDI.getDeviceSize().toString(),
							thisDI.getDeviceSize() == null ? "" : thisDI.getDeviceSize().toString());
		}
		if (!lastDI.getDeviceSizeUnit().equals(thisDI.getDeviceSizeUnit())) {
			equalsDevice = false;
			String item = itemNm + "." + Messages.getString("device.size.unit");
			setMessage(item, lastDI.getDeviceIndex().toString(), thisDI.getDeviceIndex().toString());
		}
		if (!lastDI.getDeviceType().equals(thisDI.getDeviceType())) {
			equalsDevice = false;
			String item = itemNm + "." + Messages.getString("device.type");
			setMessage(item, lastDI.getDeviceType(), thisDI.getDeviceType());
		}
		return equalsDevice;
	}

	private boolean equalsWithMap(String itemName, ArrayList<NodeDeviceInfo> lastList, ArrayList<NodeDeviceInfo> thisList) {
		boolean equalsInfo = true;

		HashMap<String, NodeDeviceInfo> lastMap = new HashMap<String, NodeDeviceInfo>();
		HashMap<String, NodeDeviceInfo> thisMap = new HashMap<String, NodeDeviceInfo>();

		//比較のためMapに詰め替える
		for (int i=0; lastList.size() > i; i++) {
			NodeDeviceInfo lastDI = lastList.get(i);
			String key = lastDI.getDeviceIndex().toString() + "."  + lastDI.getDeviceType() + "."  + lastDI.getDeviceName();
			lastMap.put(key, lastDI);
		}

		for (int i=0; thisList.size() > i; i++) {
			NodeDeviceInfo thisDI =  thisList.get(i);
			String key = thisDI.getDeviceIndex().toString() + "."  + thisDI.getDeviceType() + "."  + thisDI.getDeviceName();
			thisMap.put(key, thisDI);
		}

		//比較
		for (int i=0; thisList.size() > i; i++) {
			NodeDeviceInfo thisDI = (NodeDeviceInfo)thisList.get(i);
			String key = thisDI.getDeviceIndex().toString() + "."  + thisDI.getDeviceType() + "."  + thisDI.getDeviceName();
			if (lastMap.containsKey(key)) {
				equalsInfo = equalsInfo && equalsNodeDeviceInfo(itemName, lastMap.get(key), thisDI);
			} else {
				equalsInfo = false;
				setMessage(itemName + "." + Messages.getString("device.name") + "." + thisDI.getDeviceIndex(), Messages.getString("nonexistent"), thisDI.getDeviceName());
				setMessage(itemName + "." + Messages.getString("device.index")  + "." + thisDI.getDeviceIndex(), Messages.getString("nonexistent"), thisDI.getDeviceIndex().toString());
				setMessage(itemName + "." + Messages.getString("device.type")  + "." + thisDI.getDeviceIndex(), Messages.getString("nonexistent"), thisDI.getDeviceType());
			}
		}
		for (int i=0; lastList.size() > i; i++) {
			NodeDeviceInfo lastDI = (NodeDeviceInfo)lastList.get(i);
			String key = lastDI.getDeviceIndex().toString() + "."  + lastDI.getDeviceType() + "."  + lastDI.getDeviceName();
			if (thisMap.containsKey(key)) {
				equalsInfo = equalsInfo && equalsNodeDeviceInfo(itemName, lastDI, thisMap.get(key));
			} else {
				equalsInfo = false;
				setMessage(itemName + "." + Messages.getString("device.name") + "." + lastDI.getDeviceIndex(), lastDI.getDeviceName(), Messages.getString("nonexistent"));
				setMessage(itemName + "." + Messages.getString("device.index") + "." + lastDI.getDeviceIndex(), lastDI.getDeviceIndex().toString(), Messages.getString("nonexistent"));
				setMessage(itemName + "." + Messages.getString("device.type") + "." + lastDI.getDeviceIndex(), lastDI.getDeviceType(), Messages.getString("nonexistent"));
			}
		}

		return equalsInfo;

	}
}
