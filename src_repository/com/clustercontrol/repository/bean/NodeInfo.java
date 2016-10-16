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

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.bean.SnmpProtocolConstant;
import com.clustercontrol.bean.SnmpSecurityLevelConstant;

/**
 * このクラスはノード詳細のクラスです。
 * ノードの詳細を保持するために使用されます。
 *
 * TODO
 * HinemosClientのNodePropertyUtil.javaのsetDefaultNodeと値をあわせておくこと！
 *
 * @since 0.8
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class NodeInfo extends FacilityInfo implements Serializable
{
	private static final long serialVersionUID = -4120880233299545923L;

	private Boolean autoDeviceSearch = Boolean.TRUE;

	// HW
	private java.lang.String platformFamily = "";
	private java.lang.String subPlatformFamily = "";
	private java.lang.String hardwareType = "";

	// IPアドレス
	private java.lang.Integer ipAddressVersion = null;
	private java.lang.String ipAddressV4 = "";
	private java.lang.String ipAddressV6 = "";
	private ArrayList<NodeHostnameInfo> nodeHostnameInfo;

	// OS
	private java.lang.String nodeName = "";
	private java.lang.String osName = "";
	private java.lang.String osRelease = "";
	private java.lang.String osVersion = "";
	private java.lang.String characterSet = "";

	// Hinemosエージェント
	private java.lang.Integer agentAwakePort = null;

	// JOB
	private java.lang.Integer jobPriority = null;
	private java.lang.Integer jobMultiplicity = null;

	// SNMP
	private java.lang.String snmpUser = "";
	private java.lang.String snmpAuthPassword = "";
	private java.lang.String snmpPrivPassword = "";
	private java.lang.Integer snmpPort = null;
	private java.lang.String snmpCommunity = "";
	private java.lang.String snmpVersion = "";
	private java.lang.String snmpSecurityLevel = SnmpSecurityLevelConstant.NOAUTH_NOPRIV;
	private java.lang.String snmpAuthProtocol = SnmpProtocolConstant.MD5;
	private java.lang.String snmpPrivProtocol = SnmpProtocolConstant.DES;
	private java.lang.Integer snmpTimeout = null;
	private java.lang.Integer snmpRetryCount = null;

	// WBEM
	private java.lang.String wbemUser = "";
	private java.lang.String wbemUserPassword = "";
	private java.lang.Integer wbemPort = null;
	private java.lang.String wbemProtocol = "";
	private java.lang.Integer wbemTimeout = null;
	private java.lang.Integer wbemRetryCount = null;

	// IPMI
	private java.lang.String ipmiIpAddress = "";
	private java.lang.Integer ipmiPort = null;
	private java.lang.String ipmiUser = "";
	private java.lang.String ipmiUserPassword = "";
	private java.lang.Integer ipmiTimeout = null;
	private java.lang.Integer ipmiRetries = null;
	private java.lang.String ipmiProtocol = "";
	private java.lang.String ipmiLevel = "";

	// WinRM
	private java.lang.String winrmUser = "";
	private java.lang.String winrmUserPassword = "";
	private java.lang.String winrmVersion = "";
	private java.lang.Integer winrmPort = null;
	private java.lang.String winrmProtocol = "";
	private java.lang.Integer winrmTimeout = null;
	private java.lang.Integer winrmRetries = null;

	// SSH
	private java.lang.String sshUser = "";
	private java.lang.String sshUserPassword = "";
	private java.lang.String sshPrivateKeyFilepath = "";
	private java.lang.String sshPrivateKeyPassphrase = "";
	private java.lang.Integer sshPort = null;
	private java.lang.Integer sshTimeout = null;

	// デバイス
	private ArrayList<NodeDeviceInfo> nodeDeviceInfo;
	private ArrayList<NodeCpuInfo> nodeCpuInfo;
	private ArrayList<NodeMemoryInfo> nodeMemoryInfo;
	private ArrayList<NodeDiskInfo> nodeDiskInfo;
	private ArrayList<NodeNetworkInterfaceInfo> nodeNetworkInterfaceInfo;
	private ArrayList<NodeFilesystemInfo> nodeFilesystemInfo;

	// クラウド・仮想化
	private java.lang.String cloudService = "";
	private java.lang.String cloudScope = "";
	private java.lang.String cloudResourceType = "";
	private java.lang.String cloudResourceName = "";
	private java.lang.String cloudResourceId = "";
	private java.lang.String cloudLocation = "";

	// ノード変数
	private ArrayList<NodeVariableInfo> nodeVariableInfo;

	// 保守
	private java.lang.String administrator = "";
	private java.lang.String contact = "";

	// 備考
	private ArrayList<NodeNoteInfo> nodeNoteInfo;

	/**
	 * デフォルトコンストラクタ
	 */
	public NodeInfo() {}

	/**
	 * コンストラクタです。
	 * ファシリティIDとファシリティ名は引数で与えます。
	 * ファシリティ種別は1、表示ソート順は100に設定されます。
	 * @param facilityId
	 * @param facilityName
	 */
	public NodeInfo(String facilityId, String facilityName)
	{
		super();

		setFacilityId(facilityId);
		setFacilityName(facilityName);

		/*
		 * デフォルト値
		 */
		// 0 - scope, 1 - node
		setFacilityType(FacilityConstant.TYPE_NODE);

		// 100 - node, 200 - scope
		// 10000 - display_sort_order, 11000 - REGISTERED, 12000 - UNREGISTERED
		setDisplaySortOrder(100);

		/*
		setDescription(description);
		setCreateUserId(createUserId);
		setCreateDatetime(createDatetime);
		setModifyUserId(modifyUserId);
		setModifyDatetime(modifyDatetime);
		 */
	}

	/**
	 * プラットフォームのgetterです。
	 * @return String
	 */
	public java.lang.String getPlatformFamily()
	{
		return this.platformFamily;
	}

	/**
	 * プラットフォームのsetterです。
	 * @param platformFamily
	 */
	public void setPlatformFamily( java.lang.String platformFamily )
	{
		this.platformFamily = platformFamily;
	}

	/**
	 * サブプラットフォームのgetterです。
	 * @return String
	 */
	public java.lang.String getSubPlatformFamily() {
		return subPlatformFamily;
	}

	/**
	 * サブプラットフォームのsetterです。
	 * @param subPlatformFamily
	 */
	public void setSubPlatformFamily(java.lang.String subPlatformFamily) {
		this.subPlatformFamily = subPlatformFamily;
	}

	/**
	 * H/Wのgetterです。
	 * @return String
	 */
	public java.lang.String getHardwareType()
	{
		return this.hardwareType;
	}

	/**
	 * H/Wのsetterです。
	 * @param hardwareType
	 */
	public void setHardwareType( java.lang.String hardwareType )
	{
		this.hardwareType = hardwareType;
	}

	/**
	 * SNMP->接続ユーザ名のgetterです。
	 * @return String
	 */
	public java.lang.String getSnmpUser()
	{
		return this.snmpUser;
	}

	/**
	 * SNMP->接続ユーザ名のsetterです。
	 * @param snmpUser
	 */
	public void setSnmpUser( java.lang.String snmpUser )
	{
		this.snmpUser = snmpUser;
	}

	/**
	 * SNMP->接続ユーザ認証パスワードのgetterです。
	 * @return String
	 */
	public java.lang.String getSnmpAuthPassword()
	{
		return this.snmpAuthPassword;
	}

	/**
	 * SNMP->接続ユーザ認証パスワードのsetterです。
	 * @param snmpUserPassword
	 */
	public void setSnmpAuthPassword( java.lang.String snmpUserPassword )
	{
		this.snmpAuthPassword = snmpUserPassword;
	}

	/**
	 * SNMP->接続ユーザ暗号化パスワードのgetterです。
	 * @return String
	 */
	public java.lang.String getSnmpPrivPassword()
	{
		return this.snmpPrivPassword;
	}

	/**
	 * SNMP->接続ユーザ暗号化パスワードのsetterです。
	 * @param snmpPrivPassword
	 */
	public void setSnmpPrivPassword( java.lang.String snmpPrivPassword )
	{
		this.snmpPrivPassword = snmpPrivPassword;
	}

	/**
	 * SNMP->ポート番号のgetterです。
	 * @return Integer
	 */
	public java.lang.Integer getSnmpPort()
	{
		return this.snmpPort;
	}

	/**
	 * SNMP->ポート番号のsetterです。
	 * @param snmpPort
	 */
	public void setSnmpPort( java.lang.Integer snmpPort )
	{
		this.snmpPort = snmpPort;
	}

	/**
	 * SNMP->コミュニティ名のgetterです。
	 * @return String
	 */
	public java.lang.String getSnmpCommunity()
	{
		return this.snmpCommunity;
	}

	/**
	 * SNMP->コミュニティ名のsetterです。
	 * @param snmpCommunity
	 */
	public void setSnmpCommunity( java.lang.String snmpCommunity )
	{
		this.snmpCommunity = snmpCommunity;
	}

	/**
	 * SNMP->バージョンのgetterです。
	 * @return String
	 */
	public java.lang.String getSnmpVersion()
	{
		return this.snmpVersion;
	}

	/**
	 * SNMP->バージョンのsetterです。
	 * @param snmpVersion
	 */
	public void setSnmpVersion( java.lang.String snmpVersion )
	{
		this.snmpVersion = snmpVersion;
	}

	/**
	 * SNMP->セキュリティレベルのgetterです。
	 * @return String
	 */
	public java.lang.String getSnmpSecurityLevel()
	{
		return this.snmpSecurityLevel;
	}

	/**
	 * SNMP->セキュリティレベルのsetterです。
	 * @param snmpSecurityLevel
	 */
	public void setSnmpSecurityLevel( java.lang.String snmpSecurityLevel )
	{
		this.snmpSecurityLevel = snmpSecurityLevel;
	}

	/**
	 * SNMP->認証プロトコルのgetterです。
	 * @return String
	 */
	public java.lang.String getSnmpAuthProtocol()
	{
		return this.snmpAuthProtocol;
	}

	/**
	 * SNMP->認証プロトコルのsetterです。
	 * @param snmpAuthProtocol
	 */
	public void setSnmpAuthProtocol( java.lang.String snmpAuthProtocol )
	{
		this.snmpAuthProtocol = snmpAuthProtocol;
	}

	/**
	 * SNMP->暗号化プロトコルのgetterです。
	 * @return String
	 */
	public java.lang.String getSnmpPrivProtocol()
	{
		return this.snmpPrivProtocol;
	}

	/**
	 * SNMP->暗号化プロトコルのsetterです。
	 * @param snmpPrivProtocol
	 */
	public void setSnmpPrivProtocol( java.lang.String snmpPrivProtocol )
	{
		this.snmpPrivProtocol = snmpPrivProtocol;
	}

	/**
	 * SNMP->SNMP タイムアウトのgetterです。
	 * @return Integer
	 */
	public java.lang.Integer getSnmpTimeout()
	{
		return this.snmpTimeout;
	}

	/**
	 * SNMP->SNMP タイムアウトのsetterです。
	 * @param snmpTimeout
	 */
	public void setSnmpTimeout( java.lang.Integer snmpTimeout )
	{
		this.snmpTimeout = snmpTimeout;
	}

	/**
	 * SNMP->SNMP リトライ回数のgetterです。
	 * @return Integer
	 */
	public java.lang.Integer getSnmpRetryCount()
	{
		return this.snmpRetryCount;
	}

	/**
	 * SNMP->SNMP リトライ回数のsetterです。
	 * @param snmpRetryCount
	 */
	public void setSnmpRetryCount( java.lang.Integer snmpRetryCount )
	{
		this.snmpRetryCount = snmpRetryCount;
	}

	/**
	 * WBEM->接続ユーザ名のgetterです。
	 * @return String
	 */
	public java.lang.String getWbemUser()
	{
		return this.wbemUser;
	}

	/**
	 * WBEM->接続ユーザ名のsetterです。
	 * @param wbemUser
	 */
	public void setWbemUser( java.lang.String wbemUser )
	{
		this.wbemUser = wbemUser;
	}

	/**
	 * WBEM->接続ユーザパスワードのgetterです。
	 * @return String
	 */
	public java.lang.String getWbemUserPassword()
	{
		return this.wbemUserPassword;
	}

	/**
	 * WBEM->接続ユーザパスワードのsetterです。
	 * @param wbemUserPassword
	 */
	public void setWbemUserPassword( java.lang.String wbemUserPassword )
	{
		this.wbemUserPassword = wbemUserPassword;
	}

	/**
	 * WBEM->WBEM ポート番号のgetterです。
	 * @return Integer
	 */
	public java.lang.Integer getWbemPort()
	{
		return this.wbemPort;
	}

	/**
	 * WBEM->WBEM ポート番号のsetterです。
	 * @param wbemPort
	 */
	public void setWbemPort( java.lang.Integer wbemPort )
	{
		this.wbemPort = wbemPort;
	}

	/**
	 * WBEM->WBEM プロトコルのgetterです。
	 * @return String
	 */
	public java.lang.String getWbemProtocol()
	{
		return this.wbemProtocol;
	}

	/**
	 * WBEM->WBEM プロトコルのsetterです。
	 * @param wbemProtocol
	 */
	public void setWbemProtocol( java.lang.String wbemProtocol )
	{
		this.wbemProtocol = wbemProtocol;
	}

	/**
	 * WBEM->WBEM タイムアウトのgetterです。
	 * @return Integer
	 */
	public java.lang.Integer getWbemTimeout()
	{
		return this.wbemTimeout;
	}

	/**
	 * WBEM->WBEM タイムアウトのsetterです。
	 * @param wbemTimeout
	 */
	public void setWbemTimeout( java.lang.Integer wbemTimeout )
	{
		this.wbemTimeout = wbemTimeout;
	}

	/**
	 * WBEM->WBEM リトライ回数のgetterです。
	 * @return Integer
	 */
	public java.lang.Integer getWbemRetryCount()
	{
		return this.wbemRetryCount;
	}

	/**
	 * WBEM->WBEM リトライ回数のsetterです。
	 * @param wbemRetryCount
	 */
	public void setWbemRetryCount( java.lang.Integer wbemRetryCount )
	{
		this.wbemRetryCount = wbemRetryCount;
	}

	/**
	 * WBEMで値取得時に必要となるNameSpaceです。
	 * 現在は固定値を返します。
	 * @return 固定値（root/cimv2）を返す。
	 */
	public String getWbemNameSpace(){
		return "root/cimv2";
	}

	/**
	 * IPアドレスのバージョンのgetterです。
	 * @return Integer
	 */
	public java.lang.Integer getIpAddressVersion()
	{
		return this.ipAddressVersion;
	}

	/**
	 * IPアドレスのバージョンのsetterです。
	 * @param ipAddressVersion
	 */
	public void setIpAddressVersion( java.lang.Integer ipAddressVersion )
	{
		this.ipAddressVersion = ipAddressVersion;
	}

	/**
	 * IPv4のアドレスのgetterです。
	 * @return String
	 */
	public java.lang.String getIpAddressV4()
	{
		return this.ipAddressV4;
	}

	/**
	 * IPv4のアドレスのsetterです。
	 * @param ipAddressV4
	 */
	public void setIpAddressV4( java.lang.String ipAddressV4 )
	{
		this.ipAddressV4 = ipAddressV4;
	}

	/**
	 * IPv6のアドレスのgetterです。
	 * @return String
	 */
	public java.lang.String getIpAddressV6()
	{
		return this.ipAddressV6;
	}

	/**
	 * IPv6のアドレスのsetterです。
	 * @param ipAddressV6
	 */
	public void setIpAddressV6( java.lang.String ipAddressV6 )
	{
		this.ipAddressV6 = ipAddressV6;
	}

	/**
	 * ノード名のgetterです。
	 * @return String
	 */
	public java.lang.String getNodeName()
	{
		return this.nodeName;
	}

	/**
	 * ノード名のsetterです。
	 * @param nodeName
	 */
	public void setNodeName( java.lang.String nodeName )
	{
		this.nodeName = nodeName;
	}

	/**
	 * OS名のgetterです。
	 * @return String
	 */
	public java.lang.String getOsName()
	{
		return this.osName;
	}

	/**
	 * OS名のsetterです。
	 * @param osName
	 */
	public void setOsName( java.lang.String osName )
	{
		this.osName = osName;
	}

	/**
	 * OSリリースのgetterです。
	 * @return String
	 */
	public java.lang.String getOsRelease()
	{
		return this.osRelease;
	}

	/**
	 * OSリリースのsetterです。
	 * @param osRelease
	 */
	public void setOsRelease( java.lang.String osRelease )
	{
		this.osRelease = osRelease;
	}

	/**
	 * OSバージョンのgetterです。
	 * @return String
	 */
	public java.lang.String getOsVersion()
	{
		return this.osVersion;
	}

	/**
	 * OSバージョンのgetterです。
	 * @param osVersion
	 */
	public void setOsVersion( java.lang.String osVersion )
	{
		this.osVersion = osVersion;
	}

	/**
	 * 文字セットのgetterです。
	 * @return String
	 */
	public java.lang.String getCharacterSet()
	{
		return this.characterSet;
	}

	/**
	 * 文字セットのsetterです。
	 * @param characterSet
	 */
	public void setCharacterSet( java.lang.String characterSet )
	{
		this.characterSet = characterSet;
	}


	/**
	 * Hinemosエージェントのawakeポートのgetter
	 * @return String
	 */
	public java.lang.Integer getAgentAwakePort() {
		return this.agentAwakePort;
	}

	/**
	 * Hinemosエージェントのawakeポートのsetter
	 * @param agentAwakePort
	 */
	public void setAgentAwakePort(java.lang.Integer agentAwakePort) {
		this.agentAwakePort = agentAwakePort;
	}


	/**
	 * ジョブ優先度のgetterです。
	 * @return Integer
	 */
	public java.lang.Integer getJobPriority()
	{
		return this.jobPriority;
	}

	/**
	 * ジョブ優先度のsetterです。
	 * @param jobPriority
	 */
	public void setJobPriority( java.lang.Integer jobPriority )
	{
		this.jobPriority = jobPriority;
	}


	/**
	 * ジョブ多重度のgetterです。
	 * @return Integer
	 */
	public java.lang.Integer getJobMultiplicity()
	{
		return this.jobMultiplicity;
	}

	/**
	 * ジョブ多重度のsetterです。
	 * @param jobMultiplicity
	 */
	public void setJobMultiplicity( java.lang.Integer jobMultiplicity )
	{
		this.jobMultiplicity = jobMultiplicity;
	}


	/**
	 * IPMI接続アドレスのgetter
	 * @return ipmiIpAddress
	 */
	public java.lang.String getIpmiIpAddress() {
		return this.ipmiIpAddress;
	}

	/**
	 * IPMI接続アドレスのsetter
	 * @param ipmiIpAddress
	 */
	public void setIpmiIpAddress(java.lang.String ipmiIpAddress) {
		this.ipmiIpAddress = ipmiIpAddress;
	}

	/**
	 * IPMI接続ポートのgetter
	 * @return ipmiPort
	 */
	public java.lang.Integer getIpmiPort() {
		return this.ipmiPort;
	}

	/**
	 * IPMI接続ポートのsetter
	 * @param ipmiPort
	 */
	public void setIpmiPort(java.lang.Integer ipmiPort) {
		this.ipmiPort = ipmiPort;
	}

	/**
	 * IPMI接続ユーザのgetter
	 * @return ipmiUser
	 */
	public java.lang.String getIpmiUser() {
		return this.ipmiUser;
	}

	/**
	 * IPMI接続ユーザのsetter
	 * @param ipmiUser
	 */
	public void setIpmiUser(java.lang.String ipmiUser) {
		this.ipmiUser = ipmiUser;
	}

	/**
	 * IPMI接続ユーザパスワードのgetter
	 * @return ipmiUserPassword
	 */
	public java.lang.String getIpmiUserPassword() {
		return this.ipmiUserPassword;
	}

	/**
	 * IPMI接続ユーザパスワードのsetter
	 * @param ipmiUserPassword
	 */
	public void setIpmiUserPassword(java.lang.String ipmiUserPassword) {
		this.ipmiUserPassword = ipmiUserPassword;
	}

	/**
	 * IPMI接続タイムアウトのgetter
	 * @return ipmiTimeout
	 */
	public java.lang.Integer getIpmiTimeout() {
		return this.ipmiTimeout;
	}

	/**
	 * IPMI接続タイムアウトのsetter
	 * @param ipmiTimeout
	 */
	public void setIpmiTimeout(java.lang.Integer ipmiTimeout) {
		this.ipmiTimeout = ipmiTimeout;
	}

	/**
	 * IPMI接続リトライ回数のgetter
	 * @return ipmiRetries
	 */
	public java.lang.Integer getIpmiRetries() {
		return this.ipmiRetries;
	}

	/**
	 * IPMI接続リトライ回数のsetter
	 * @param ipmiRetries
	 */
	public void setIpmiRetries(java.lang.Integer ipmiRetries) {
		this.ipmiRetries = ipmiRetries;
	}

	/**
	 * IPMI接続プロトコルのgetter
	 * @return ipmiProtocol
	 */
	public java.lang.String getIpmiProtocol() {
		return this.ipmiProtocol;
	}

	/**
	 * IPMI接続プロトコルのsetter
	 * @param ipmiProtocol
	 */
	public void setIpmiProtocol(java.lang.String ipmiProtocol) {
		this.ipmiProtocol = ipmiProtocol;
	}

	/**
	 * IPMI接続レベルのgetter
	 * @return ipmiLevel
	 */
	public java.lang.String getIpmiLevel() {
		return this.ipmiLevel;
	}

	/**
	 * IPMI接続レベルのsetter
	 * @param ipmiLevel
	 */
	public void setIpmiLevel(java.lang.String ipmiLevel) {
		this.ipmiLevel = ipmiLevel;
	}

	/**
	 * WinRM接続ユーザのgetter
	 * @return winrmUser
	 */
	public java.lang.String getWinrmUser() {
		return this.winrmUser;
	}

	/**
	 * WinRM接続ユーザのsetter
	 * @param winrmUser
	 */
	public void setWinrmUser(java.lang.String winrmUser) {
		this.winrmUser = winrmUser;
	}

	/**
	 * WinRM接続ユーザパスワードのgetter
	 * @return winrmUserPassword
	 */
	public java.lang.String getWinrmUserPassword() {
		return this.winrmUserPassword;
	}

	/**
	 * WinRM接続ユーザパスワードのsetter
	 * @param winrmUserPassword
	 */
	public void setWinrmUserPassword(java.lang.String winrmUserPassword) {
		this.winrmUserPassword = winrmUserPassword;
	}

	/**
	 * WinRMバージョンのgetter
	 * @return winrmVersion
	 */
	public java.lang.String getWinrmVersion() {
		return this.winrmVersion;
	}

	/**
	 * WinRMバージョンのsetter
	 * @param winrmVersion
	 */
	public void setWinrmVersion(java.lang.String winrmVersion) {
		this.winrmVersion = winrmVersion;
	}

	/**
	 * WinRM接続ポート番号のgetter
	 * @return winrmPort
	 */
	public java.lang.Integer getWinrmPort() {
		return this.winrmPort;
	}

	/**
	 * WinRM接続ポート番号のsetter
	 * @param winrmPort
	 */
	public void setWinrmPort(java.lang.Integer winrmPort) {
		this.winrmPort = winrmPort;
	}

	/**
	 * WinRM接続プロトコルのgetter
	 * @return winrmProtocol
	 */
	public java.lang.String getWinrmProtocol() {
		return this.winrmProtocol;
	}

	/**
	 * WinRM接続プロトコルのsetter
	 * @param winrmProtocol
	 */
	public void setWinrmProtocol(java.lang.String winrmProtocol) {
		this.winrmProtocol = winrmProtocol;
	}

	/**
	 * WinRM接続タイムアウトのgetter
	 * @return winrmTimeout
	 */
	public java.lang.Integer getWinrmTimeout() {
		return this.winrmTimeout;
	}

	/**
	 * WinRM接続タイムアウトのsetter
	 * @param winrmTimeout
	 */
	public void setWinrmTimeout(java.lang.Integer winrmTimeout) {
		this.winrmTimeout = winrmTimeout;
	}

	/**
	 * WinRM接続リトライ回数のgetter
	 * @return winrmRetries
	 */
	public java.lang.Integer getWinrmRetries() {
		return this.winrmRetries;
	}

	/**
	 * WinRM接続リトライ回数のsetter
	 * @param winrmRetries
	 */
	public void setWinrmRetries(java.lang.Integer winrmRetries) {
		this.winrmRetries = winrmRetries;
	}

	/**
	 * クラウド管理->クラウドサービスのgetterです。
	 * @return String
	 */
	public java.lang.String getCloudService() {
		return cloudService;
	}

	/**
	 * クラウド管理->クラウドサービスのsetterです。
	 * @param cloudService
	 */
	public void setCloudService(java.lang.String cloudService) {
		this.cloudService = cloudService;
	}

	/**
	 * クラウド管理->クラウドスコープのgetterです。
	 * @return String
	 */
	public java.lang.String getCloudScope() {
		return cloudScope;
	}

	/**
	 * クラウド管理->クラウドスコープのsetterです。
	 * @param cloudScope
	 */
	public void setCloudScope(java.lang.String cloudScope) {
		this.cloudScope = cloudScope;
	}

	/**
	 * クラウド管理->クラウドリソースタイプのgetterです。
	 * @return String
	 */
	public java.lang.String getCloudResourceType() {
		return cloudResourceType;
	}

	/**
	 * クラウド管理->クラウドリソースタイプのsetterです。
	 * @param cloudResourceType
	 */
	public void setCloudResourceType(java.lang.String cloudResourceType) {
		this.cloudResourceType = cloudResourceType;
	}

	/**
	 * クラウド管理->クラウドリソースIdのgetterです。
	 * @return String
	 */
	public java.lang.String getCloudResourceId() {
		return cloudResourceId;
	}
	
	/**
	 * クラウド管理->クラウドリソースIdのsetterです。
	 * @param cloudResourceId
	 */
	public void setCloudResourceId(java.lang.String cloudResourceId) {
		this.cloudResourceId = cloudResourceId;
	}

	/**
	 * クラウド管理-> クラウドリソース名のsetterです。
	 * @param cloudResourceName
	 */
	public void setCloudResourceName(java.lang.String cloudResourceName) {
		this.cloudResourceName = cloudResourceName;
	}
	
	/**
	 * クラウド管理->クラウドリソース名のgetterです。
	 * @return
	 */
	public java.lang.String getCloudResourceName() {
		return cloudResourceName;
	}
	
	/**
	 * クラウド管理->ロケーションのgetterです。
	 * @return String
	 */
	public java.lang.String getCloudLocation() {
		return cloudLocation;
	}

	/**
	 * クラウド管理->ロケーションのsetterです。
	 * @param cloudLocation
	 */
	public void setCloudLocation(java.lang.String cloudLocation) {
		this.cloudLocation = cloudLocation;
	}

	/**
	 * ノード変数のgetter
	 * @return
	 */
	public ArrayList<NodeVariableInfo> getNodeVariableInfo() {
		return nodeVariableInfo;
	}

	/**
	 * ノード変数のsetter
	 * @param nodeVariableInfo
	 */
	public void setNodeVariableInfo(ArrayList<NodeVariableInfo> nodeVariableInfo) {
		this.nodeVariableInfo = nodeVariableInfo;
	}

	/**
	 * 管理者のgetterです。
	 * @return String
	 */
	public java.lang.String getAdministrator()
	{
		return this.administrator;
	}

	/**
	 * 管理者のsetterです。
	 * @param administrator
	 */
	public void setAdministrator( java.lang.String administrator )
	{
		this.administrator = administrator;
	}

	/**
	 * 連絡先のgetterです。
	 * @return String
	 */
	public java.lang.String getContact()
	{
		return this.contact;
	}

	/**
	 * 連絡先のsetterです。
	 * @param contact
	 */
	public void setContact( java.lang.String contact )
	{
		this.contact = contact;
	}

	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer("");

		str.append(super.toString());

		str.append('{');

		str.append(
				"autoDeviceSearch=" + isAutoDeviceSearch() + " " +
				"platformFamily=" + getPlatformFamily() + " " +
				"subPlatformFamily=" + getSubPlatformFamily() + " " +
				"hardwareType=" + getHardwareType() + " " +
				"iconImage=" + getIconImage() + " " +

				"ipAddressVersion=" + getIpAddressVersion() + " " +
				"ipAddressV4=" + getIpAddressV4() + " " +
				"ipAddressV6=" + getIpAddressV6() + " " +

				"nodeName=" + getNodeName() + " " +
				"osName=" + getOsName() + " " +
				"osRelease=" + getOsRelease() + " " +
				"osVersion=" + getOsVersion() + " " +
				"characterSet=" + getCharacterSet() + " " +

				"agentAwakePort=" + getAgentAwakePort() + " " +

				"snmpUser=" + getSnmpUser() + " " +
				"snmpAuthPassword=" + getSnmpAuthPassword() + " " +
				"snmpPrivPassword=" + getSnmpPrivPassword() + " " +
				"snmpPort=" + getSnmpPort() + " " +
				"snmpCommunity=" + getSnmpCommunity() + " " +
				"snmpVersion=" + getSnmpVersion() + " " +
				"snmpSecurityLevel=" + getSnmpSecurityLevel() + " " +
				"snmpAuthProtocol=" + getSnmpAuthProtocol() + " " +
				"snmpPrivProtocol=" + getSnmpPrivProtocol() + " " +
				"snmpTimeout=" + getSnmpTimeout() + " " +
				"snmpRetryCount=" + getSnmpRetryCount() + " " +

				"wbemUser=" + getWbemUser() + " " +
				"wbemUserPassword=" + getWbemUserPassword() + " " +
				"wbemPort=" + getWbemPort() + " " +
				"wbemProtocol=" + getWbemProtocol() + " " +
				"wbemTimeout=" + getWbemTimeout() + " " +
				"wbemRetryCount=" + getWbemRetryCount() + " " +

				"ipmiIpAddress=" + getIpmiIpAddress() + " " +
				"ipmiPort=" + getIpmiPort() + " " +
				"ipmiUser=" + getIpmiUser() + " " +
				"ipmiUserPassword=" + getIpmiUserPassword() + " " +
				"ipmiTimeout=" + getIpmiTimeout() + " " +
				"ipmiRetries=" + getIpmiRetries() + " " +
				"ipmiProtocol=" + getIpmiProtocol() + " " +
				"ipmiLevel=" + getIpmiLevel() + " " +

				"cloudService=" + getCloudService() + " " +
				"cloudScope=" + getCloudScope() + " " +
				"cloudResourceType=" + getCloudResourceType() + " " +
				"cloudResourceId=" + getCloudResourceId() + " " +
				"cloudLocation=" + getCloudLocation() + " " +

				"administrator=" + getAdministrator() + " " +
				"contact=" + getContact()
				);
		str.append('}');

		return(str.toString());
	}

	@Override
	public boolean equals( Object pOther )
	{
		if( pOther instanceof NodeInfo )
		{
			NodeInfo lTest = (NodeInfo) pOther;
			boolean lEquals = true;

			lEquals = lEquals && super.equals(pOther);

			if( this.autoDeviceSearch == null )
			{
				lEquals = lEquals && ( lTest.autoDeviceSearch == null );
			}
			else
			{
				lEquals = lEquals && this.autoDeviceSearch.equals( lTest.autoDeviceSearch );
			}
			// HW
			if( this.platformFamily == null )
			{
				lEquals = lEquals && ( lTest.platformFamily == null );
			}
			else
			{
				lEquals = lEquals && this.platformFamily.equals( lTest.platformFamily );
			}
			if( this.subPlatformFamily == null )
			{
				lEquals = lEquals && ( lTest.subPlatformFamily == null );
			}
			else
			{
				lEquals = lEquals && this.subPlatformFamily.equals( lTest.subPlatformFamily );
			}
			if( this.hardwareType == null )
			{
				lEquals = lEquals && ( lTest.hardwareType == null );
			}
			else
			{
				lEquals = lEquals && this.hardwareType.equals( lTest.hardwareType );
			}

			// IP アドレス
			if( this.ipAddressVersion == null )
			{
				lEquals = lEquals && ( lTest.ipAddressVersion == null );
			}
			else
			{
				lEquals = lEquals && this.ipAddressVersion.equals( lTest.ipAddressVersion );
			}
			if( this.ipAddressV4 == null )
			{
				lEquals = lEquals && ( lTest.ipAddressV4 == null );
			}
			else
			{
				lEquals = lEquals && this.ipAddressV4.equals( lTest.ipAddressV4 );
			}
			if( this.ipAddressV6 == null )
			{
				lEquals = lEquals && ( lTest.ipAddressV6 == null );
			}
			else
			{
				lEquals = lEquals && this.ipAddressV6.equals( lTest.ipAddressV6 );
			}
			if( this.nodeName == null )
			{
				lEquals = lEquals && ( lTest.nodeName == null );
			}
			else
			{
				lEquals = lEquals && this.nodeName.equals( lTest.nodeName );
			}

			// OS
			if( this.osName == null )
			{
				lEquals = lEquals && ( lTest.osName == null );
			}
			else
			{
				lEquals = lEquals && this.osName.equals( lTest.osName );
			}
			if( this.osRelease == null )
			{
				lEquals = lEquals && ( lTest.osRelease == null );
			}
			else
			{
				lEquals = lEquals && this.osRelease.equals( lTest.osRelease );
			}
			if( this.osVersion == null )
			{
				lEquals = lEquals && ( lTest.osVersion == null );
			}
			else
			{
				lEquals = lEquals && this.osVersion.equals( lTest.osVersion );
			}
			if( this.characterSet == null )
			{
				lEquals = lEquals && ( lTest.characterSet == null );
			}
			else
			{
				lEquals = lEquals && this.characterSet.equals( lTest.characterSet );
			}

			// Hinemosエージェント
			if( this.agentAwakePort == null )
			{
				lEquals = lEquals && ( lTest.agentAwakePort == null );
			}
			else
			{
				lEquals = lEquals && this.agentAwakePort.equals( lTest.agentAwakePort );
			}

			// SNMP
			if( this.snmpUser == null )
			{
				lEquals = lEquals && ( lTest.snmpUser == null );
			}
			else
			{
				lEquals = lEquals && this.snmpUser.equals( lTest.snmpUser );
			}
			if( this.snmpAuthPassword == null )
			{
				lEquals = lEquals && ( lTest.snmpAuthPassword == null );
			}
			else
			{
				lEquals = lEquals && this.snmpAuthPassword.equals( lTest.snmpAuthPassword );
			}
			if( this.snmpPrivPassword == null )
			{
				lEquals = lEquals && ( lTest.snmpPrivPassword == null );
			}
			else
			{
				lEquals = lEquals && this.snmpPrivPassword.equals( lTest.snmpPrivPassword );
			}
			if( this.snmpPort == null )
			{
				lEquals = lEquals && ( lTest.snmpPort == null );
			}
			else
			{
				lEquals = lEquals && this.snmpPort.equals( lTest.snmpPort );
			}
			if( this.snmpCommunity == null )
			{
				lEquals = lEquals && ( lTest.snmpCommunity == null );
			}
			else
			{
				lEquals = lEquals && this.snmpCommunity.equals( lTest.snmpCommunity );
			}
			if( this.snmpVersion == null )
			{
				lEquals = lEquals && ( lTest.snmpVersion == null );
			}
			else
			{
				lEquals = lEquals && this.snmpVersion.equals( lTest.snmpVersion );
			}
			if( this.snmpSecurityLevel == null )
			{
				lEquals = lEquals && ( lTest.snmpSecurityLevel == null );
			}
			else
			{
				lEquals = lEquals && this.snmpSecurityLevel.equals( lTest.snmpSecurityLevel );
			}
			if( this.snmpAuthProtocol == null )
			{
				lEquals = lEquals && ( lTest.snmpAuthProtocol == null );
			}
			else
			{
				lEquals = lEquals && this.snmpAuthProtocol.equals( lTest.snmpAuthProtocol );
			}
			if( this.snmpPrivProtocol == null )
			{
				lEquals = lEquals && ( lTest.snmpPrivProtocol == null );
			}
			else
			{
				lEquals = lEquals && this.snmpPrivProtocol.equals( lTest.snmpPrivProtocol );
			}
			if( this.snmpTimeout == null )
			{
				lEquals = lEquals && ( lTest.snmpTimeout == null );
			}
			else
			{
				lEquals = lEquals && this.snmpTimeout.equals( lTest.snmpTimeout );
			}
			if( this.snmpRetryCount == null )
			{
				lEquals = lEquals && ( lTest.snmpRetryCount == null );
			}
			else
			{
				lEquals = lEquals && this.snmpRetryCount.equals( lTest.snmpRetryCount );
			}

			// WBEM
			if( this.wbemUser == null )
			{
				lEquals = lEquals && ( lTest.wbemUser == null );
			}
			else
			{
				lEquals = lEquals && this.wbemUser.equals( lTest.wbemUser );
			}
			if( this.wbemUserPassword == null )
			{
				lEquals = lEquals && ( lTest.wbemUserPassword == null );
			}
			else
			{
				lEquals = lEquals && this.wbemUserPassword.equals( lTest.wbemUserPassword );
			}
			if( this.wbemPort == null )
			{
				lEquals = lEquals && ( lTest.wbemPort == null );
			}
			else
			{
				lEquals = lEquals && this.wbemPort.equals( lTest.wbemPort );
			}
			if( this.wbemProtocol == null )
			{
				lEquals = lEquals && ( lTest.wbemProtocol == null );
			}
			else
			{
				lEquals = lEquals && this.wbemProtocol.equals( lTest.wbemProtocol );
			}
			if( this.wbemTimeout == null )
			{
				lEquals = lEquals && ( lTest.wbemTimeout == null );
			}
			else
			{
				lEquals = lEquals && this.wbemTimeout.equals( lTest.wbemTimeout );
			}
			if( this.wbemRetryCount == null )
			{
				lEquals = lEquals && ( lTest.wbemRetryCount == null );
			}
			else
			{
				lEquals = lEquals && this.wbemRetryCount.equals( lTest.wbemRetryCount );
			}

			// IPMI
			if( this.ipmiIpAddress == null )
			{
				lEquals = lEquals && ( lTest.ipmiIpAddress == null );
			}
			else
			{
				lEquals = lEquals && this.ipmiIpAddress.equals( lTest.ipmiIpAddress );
			}
			if( this.ipmiPort == null )
			{
				lEquals = lEquals && ( lTest.ipmiPort == null );
			}
			else
			{
				lEquals = lEquals && this.ipmiPort.equals( lTest.ipmiPort );
			}
			if( this.ipmiUser == null )
			{
				lEquals = lEquals && ( lTest.ipmiUser == null );
			}
			else
			{
				lEquals = lEquals && this.ipmiUser.equals( lTest.ipmiUser );
			}
			if( this.ipmiUserPassword == null )
			{
				lEquals = lEquals && ( lTest.ipmiUserPassword == null );
			}
			else
			{
				lEquals = lEquals && this.ipmiUserPassword.equals( lTest.ipmiUserPassword );
			}
			if( this.ipmiTimeout == null )
			{
				lEquals = lEquals && ( lTest.ipmiTimeout == null );
			}
			else
			{
				lEquals = lEquals && this.ipmiTimeout.equals( lTest.ipmiTimeout );
			}
			if( this.ipmiRetries == null )
			{
				lEquals = lEquals && ( lTest.ipmiRetries == null );
			}
			else
			{
				lEquals = lEquals && this.ipmiRetries.equals( lTest.ipmiRetries );
			}
			if( this.ipmiProtocol == null )
			{
				lEquals = lEquals && ( lTest.ipmiProtocol == null );
			}
			else
			{
				lEquals = lEquals && this.ipmiProtocol.equals( lTest.ipmiProtocol );
			}
			if( this.ipmiLevel == null )
			{
				lEquals = lEquals && ( lTest.ipmiLevel == null );
			}
			else
			{
				lEquals = lEquals && this.ipmiLevel.equals( lTest.ipmiLevel );
			}

			// クラウド・仮想化管理
			if( this.cloudService == null )
			{
				lEquals = lEquals && ( lTest.cloudService == null );
			}
			else
			{
				lEquals = lEquals && this.cloudService.equals( lTest.cloudService );
			}
			if( this.cloudScope == null )
			{
				lEquals = lEquals && ( lTest.cloudScope == null );
			}
			else
			{
				lEquals = lEquals && this.cloudScope.equals( lTest.cloudScope );
			}
			if( this.cloudResourceType == null )
			{
				lEquals = lEquals && ( lTest.cloudResourceType == null );
			}
			else
			{
				lEquals = lEquals && this.cloudResourceType.equals( lTest.cloudResourceType );
			}
			if (this.cloudResourceName == null) 
			{
				lEquals = lEquals && ( lTest.cloudResourceName == null );
			} else {
				lEquals = lEquals && this.cloudResourceName.equals( lTest.cloudResourceName );
			}
			if( this.cloudResourceId == null )
			{
				lEquals = lEquals && ( lTest.cloudResourceId == null );
			}
			else
			{
				lEquals = lEquals && this.cloudResourceId.equals( lTest.cloudResourceId );
			}
			if( this.cloudLocation == null )
			{
				lEquals = lEquals && ( lTest.cloudLocation == null );
			}
			else
			{
				lEquals = lEquals && this.cloudLocation.equals( lTest.cloudLocation );
			}

			// 保守
			if( this.administrator == null )
			{
				lEquals = lEquals && ( lTest.administrator == null );
			}
			else
			{
				lEquals = lEquals && this.administrator.equals( lTest.administrator );
			}
			if( this.contact == null )
			{
				lEquals = lEquals && ( lTest.contact == null );
			}
			else
			{
				lEquals = lEquals && this.contact.equals( lTest.contact );
			}

			return lEquals;
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		int result = 17;

		result = 37*result + super.hashCode();

		result = 37*result + ((this.autoDeviceSearch != null) ? this.autoDeviceSearch.hashCode() : 0);
		result = 37*result + ((this.platformFamily != null) ? this.platformFamily.hashCode() : 0);
		result = 37*result + ((this.subPlatformFamily != null) ? this.subPlatformFamily.hashCode() : 0);
		result = 37*result + ((this.hardwareType != null) ? this.hardwareType.hashCode() : 0);

		result = 37*result + ((this.snmpUser != null) ? this.snmpUser.hashCode() : 0);
		result = 37*result + ((this.snmpAuthPassword != null) ? this.snmpAuthPassword.hashCode() : 0);
		result = 37*result + ((this.snmpPrivPassword != null) ? this.snmpPrivPassword.hashCode() : 0);
		result = 37*result + ((this.snmpPort != null) ? this.snmpPort.hashCode() : 0);
		result = 37*result + ((this.snmpCommunity != null) ? this.snmpCommunity.hashCode() : 0);
		result = 37*result + ((this.snmpVersion != null) ? this.snmpVersion.hashCode() : 0);
		result = 37*result + ((this.snmpSecurityLevel != null) ? this.snmpSecurityLevel.hashCode() : 0);
		result = 37*result + ((this.snmpAuthProtocol != null) ? this.snmpAuthProtocol.hashCode() : 0);
		result = 37*result + ((this.snmpPrivProtocol != null) ? this.snmpPrivProtocol.hashCode() : 0);
		result = 37*result + ((this.snmpTimeout != null) ? this.snmpTimeout.hashCode() : 0);
		result = 37*result + ((this.snmpRetryCount != null) ? this.snmpRetryCount.hashCode() : 0);

		result = 37*result + ((this.wbemUser != null) ? this.wbemUser.hashCode() : 0);
		result = 37*result + ((this.wbemUserPassword != null) ? this.wbemUserPassword.hashCode() : 0);
		result = 37*result + ((this.wbemPort != null) ? this.wbemPort.hashCode() : 0);
		result = 37*result + ((this.wbemProtocol != null) ? this.wbemProtocol.hashCode() : 0);
		result = 37*result + ((this.wbemTimeout != null) ? this.wbemTimeout.hashCode() : 0);
		result = 37*result + ((this.wbemRetryCount != null) ? this.wbemRetryCount.hashCode() : 0);

		result = 37*result + ((this.ipAddressVersion != null) ? this.ipAddressVersion.hashCode() : 0);
		result = 37*result + ((this.ipAddressV4 != null) ? this.ipAddressV4.hashCode() : 0);
		result = 37*result + ((this.ipAddressV6 != null) ? this.ipAddressV6.hashCode() : 0);
		result = 37*result + ((this.nodeName != null) ? this.nodeName.hashCode() : 0);

		result = 37*result + ((this.osName != null) ? this.osName.hashCode() : 0);
		result = 37*result + ((this.osRelease != null) ? this.osRelease.hashCode() : 0);
		result = 37*result + ((this.osVersion != null) ? this.osVersion.hashCode() : 0);
		result = 37*result + ((this.characterSet != null) ? this.characterSet.hashCode() : 0);

		result = 37*result + ((this.cloudService != null) ? this.cloudService.hashCode() : 0);
		result = 37*result + ((this.cloudScope != null) ? this.cloudScope.hashCode() : 0);
		result = 37*result + ((this.cloudResourceType != null) ? this.cloudResourceType.hashCode() : 0);
		result = 37*result + ((this.cloudResourceName != null) ? this.cloudResourceName.hashCode() : 0);
		result = 37*result + ((this.cloudResourceId != null) ? this.cloudResourceId.hashCode() : 0);
		result = 37*result + ((this.cloudLocation != null) ? this.cloudLocation.hashCode() : 0);

		result = 37*result + ((this.administrator != null) ? this.administrator.hashCode() : 0);
		result = 37*result + ((this.contact != null) ? this.contact.hashCode() : 0);

		result = 37*result + ((this.agentAwakePort != null) ? this.agentAwakePort.hashCode() : 0);

		return result;
	}

	/**
	 * デバイスのgetter
	 * @return ArrayList<NodeDeviceInfo>
	 */
	public ArrayList<NodeDeviceInfo> getNodeDeviceInfo() {
		return nodeDeviceInfo;
	}

	/**
	 * デバイスのsetter
	 * @param nodeDevice
	 */
	public void setNodeDeviceInfo(ArrayList<NodeDeviceInfo> nodeDeviceInfo) {
		this.nodeDeviceInfo = nodeDeviceInfo;
	}

	/**
	 * CPUのgetter
	 * @return nodeCpuInfo
	 */
	public ArrayList<NodeCpuInfo> getNodeCpuInfo() {
		return this.nodeCpuInfo;
	}

	/**
	 * CPUのsetter
	 * @param nodeCpuInfo
	 */
	public void setNodeCpuInfo(ArrayList<NodeCpuInfo> nodeCpuInfo) {
		this.nodeCpuInfo = nodeCpuInfo;
	}

	/**
	 * メモリのgetter
	 * @return nodeMemoryInfo
	 */
	public ArrayList<NodeMemoryInfo> getNodeMemoryInfo() {
		return this.nodeMemoryInfo;
	}

	/**
	 * メモリのsetter
	 * @param nodeMemoryInfo
	 */
	public void setNodeMemoryInfo(ArrayList<NodeMemoryInfo> nodeMemoryInfo) {
		this.nodeMemoryInfo = nodeMemoryInfo;
	}

	/**
	 * ディスクのgetter
	 * @return nodeDiskInfo
	 */
	public ArrayList<NodeDiskInfo> getNodeDiskInfo() {
		return this.nodeDiskInfo;
	}

	/**
	 * ディスクのsetter
	 * @param nodeDiskInfo
	 */
	public void setNodeDiskInfo(ArrayList<NodeDiskInfo> nodeDiskInfo) {
		this.nodeDiskInfo = nodeDiskInfo;
	}

	/**
	 * NICのgetter
	 * @return nodeNetworkInterfaceInfo
	 */
	public ArrayList<NodeNetworkInterfaceInfo> getNodeNetworkInterfaceInfo() {
		return this.nodeNetworkInterfaceInfo;
	}

	/**
	 * NICのsetter
	 * @param nodeNetworkInterfaceInfo
	 */
	public void setNodeNetworkInterfaceInfo(
			ArrayList<NodeNetworkInterfaceInfo> nodeNetworkInterfaceInfo) {
		this.nodeNetworkInterfaceInfo = nodeNetworkInterfaceInfo;
	}

	/**
	 * ファイルシステムのgetter
	 * @return ArrayList<NodeFilesystemInfo>
	 */
	public ArrayList<NodeFilesystemInfo> getNodeFilesystemInfo() {
		return nodeFilesystemInfo;
	}

	/**
	 * ファイルシステムのsetter
	 * @param nodeFilesystemInfo
	 */
	public void setNodeFilesystemInfo(
			ArrayList<NodeFilesystemInfo> nodeFilesystemInfo) {
		this.nodeFilesystemInfo = nodeFilesystemInfo;
	}

	/**
	 * ノード名のgetter
	 * @return ArrayList<NodeHostnameInfo>
	 */
	public ArrayList<NodeHostnameInfo> getNodeHostnameInfo() {
		return nodeHostnameInfo;
	}

	/**
	 * ノード名のsetter
	 * @param nodeHostnameInfo
	 */
	public void setNodeHostnameInfo(ArrayList<NodeHostnameInfo> nodeHostnameInfo) {
		this.nodeHostnameInfo = nodeHostnameInfo;
	}

	/**
	 * 備考のgetter
	 * @return ArrayList<NodeNoteInfo>
	 */
	public ArrayList<NodeNoteInfo> getNodeNoteInfo() {
		return nodeNoteInfo;
	}

	/**
	 * 備考のsetter
	 * @param nodeNoteInfo
	 */
	public void setNodeNoteInfo(ArrayList<NodeNoteInfo> nodeNoteInfo) {
		this.nodeNoteInfo = nodeNoteInfo;
	}

	/**
	 * 利用可能なIPアドレスを返します。
	 *
	 * @return 利用可能なIPアドレス
	 * @throws UnknownHostException
	 */
	public String getAvailableIpAddress() {
		// 「IPアドレスのバージョン」により指定されたIPアドレスを設定する。
		Integer ipVersion = getIpAddressVersion();
		String ipAddress = null;
		if(ipVersion != null && ipVersion.intValue() == 6){
			ipAddress = getIpAddressV6();
		} else {
			ipAddress = getIpAddressV4();
		}
		return ipAddress;
	}

	/**
	 * メンバ変数にnullが含まれていた場合は、デフォルト値を設定する。
	 */
	public void setDefaultInfo() {
		// HW
		if (platformFamily == null) platformFamily = "";
		if (subPlatformFamily == null) subPlatformFamily = "";
		if (hardwareType == null) hardwareType = "";

		// IPアドレス
		if (ipAddressVersion == null) ipAddressVersion = null;
		if (ipAddressV4 == null) ipAddressV4 = "";
		if (ipAddressV6 == null) ipAddressV6 = "";

		// OS
		if (nodeName == null) nodeName = "";
		if (osName == null) osName = "";
		if (osRelease == null) osRelease = "";
		if (osVersion == null) osVersion = "";
		if (characterSet == null) characterSet = "";

		// Hinemosエージェント
		if (agentAwakePort == null) agentAwakePort = 24005;

		// JOB
		if (jobPriority == null) jobPriority = 16;
		if (jobMultiplicity == null) jobMultiplicity = 0;

		// SNMP
		if (snmpUser == null) snmpUser = "";
		if (snmpAuthPassword == null) snmpAuthPassword = "";
		if (snmpPrivPassword == null) snmpPrivPassword = "";
		if (snmpPort == null) snmpPort = 161;
		if (snmpCommunity == null) snmpCommunity = "";
		if (snmpVersion == null) snmpVersion = "";
		if (snmpSecurityLevel == null) snmpSecurityLevel = "";
		if (snmpAuthProtocol == null) snmpAuthProtocol = "";
		if (snmpPrivProtocol == null) snmpPrivProtocol = "";
		if (snmpTimeout == null) snmpTimeout = 5000;
		if (snmpRetryCount == null) snmpRetryCount = 3;

		// WBEM
		if (wbemUser == null) wbemUser = "";
		if (wbemUserPassword == null) wbemUserPassword = "";
		if (wbemPort == null) wbemPort = 5988;
		if (wbemProtocol == null) wbemProtocol = "";
		if (wbemTimeout == null) wbemTimeout = 5000;
		if (wbemRetryCount == null) wbemRetryCount = 3;

		// IPMI
		if (ipmiIpAddress == null) ipmiIpAddress = "";
		if (ipmiPort == null) ipmiPort = 0;
		if (ipmiUser == null) ipmiUser = "";
		if (ipmiUserPassword == null) ipmiUserPassword = "";
		if (ipmiTimeout == null) ipmiTimeout = 5000;
		if (ipmiRetries == null) ipmiRetries = 3;
		if (ipmiProtocol == null) ipmiProtocol = "";
		if (ipmiLevel == null) ipmiLevel = "";

		// WinRM
		if (winrmUser == null) winrmUser = "";
		if (winrmUserPassword == null) winrmUserPassword = "";
		if (winrmVersion == null) winrmVersion = "";
		if (winrmPort == null) winrmPort = 5985;
		if (winrmProtocol == null) winrmProtocol = "";
		if (winrmTimeout == null) winrmTimeout = 5000;
		if (winrmRetries == null) winrmRetries = 3;

		if (sshUser == null) sshUser = "root";
		if (sshUserPassword == null) sshUserPassword = "";
		if (sshPrivateKeyFilepath == null) sshPrivateKeyFilepath = "";
		if (sshPrivateKeyPassphrase == null) sshPrivateKeyPassphrase = "";
		if (sshPort == null) sshPort = 22;
		if (sshTimeout == null) sshTimeout = 50000;

		// デバイス

		// クラウド・サーバ仮想化
		if (cloudService == null) cloudService = "";
		if (cloudScope == null) cloudScope = "";
		if (cloudResourceType == null) cloudResourceType = "";
		if (cloudResourceName == null) cloudResourceName = "";
		if (cloudResourceId == null) cloudResourceId = "";
		if (cloudLocation == null) cloudLocation = "";

		// ノード変数

		// 保守
		if (administrator == null) administrator = "";
		if (contact == null) contact = "";
	}

	@Override
	public NodeInfo clone() {
		NodeInfo cloneInfo = new NodeInfo();
		cloneInfo = (NodeInfo) super.clone();

		cloneInfo.autoDeviceSearch = this.autoDeviceSearch;
		// HW
		cloneInfo.platformFamily = this.platformFamily;
		cloneInfo.subPlatformFamily = this.subPlatformFamily;
		cloneInfo.hardwareType = this.hardwareType;

		// IPアドレス
		cloneInfo.ipAddressVersion = this.ipAddressVersion;
		cloneInfo.ipAddressV4 = this.ipAddressV4;
		cloneInfo.ipAddressV6 = this.ipAddressV6;

		//参照型は一旦別のオブジェクトへコピー（親クラスをcloneした時点で子クラスの
		//参照型メンバ変数への参照がセットされてしまうため）
		ArrayList<NodeHostnameInfo> tmpHostnameList = new ArrayList<NodeHostnameInfo>();
		for (NodeHostnameInfo thisInfo : this.nodeHostnameInfo) {
			tmpHostnameList.add(thisInfo.clone());
		}
		cloneInfo.nodeHostnameInfo = tmpHostnameList;

		// OS
		cloneInfo.nodeName = this.nodeName;
		cloneInfo.osName = this.osName;
		cloneInfo.osRelease = this.osRelease;
		cloneInfo.osVersion = this.osVersion;
		cloneInfo.characterSet = this.characterSet;

		// Hinemosエージェント
		cloneInfo.agentAwakePort = this.agentAwakePort;

		// JOB
		cloneInfo.jobPriority = this.jobPriority;
		cloneInfo.jobMultiplicity = this.jobMultiplicity;

		// SNMP
		cloneInfo.snmpUser = this.snmpUser;
		cloneInfo.snmpAuthPassword = this.snmpAuthPassword;
		cloneInfo.snmpPrivPassword = this.snmpPrivPassword;
		cloneInfo.snmpPort = this.snmpPort;
		cloneInfo.snmpCommunity = this.snmpCommunity;
		cloneInfo.snmpVersion = this.snmpVersion;
		cloneInfo.snmpSecurityLevel = this.snmpSecurityLevel;
		cloneInfo.snmpAuthProtocol = this.snmpAuthProtocol;
		cloneInfo.snmpPrivProtocol = this.snmpPrivProtocol;
		cloneInfo.snmpTimeout = this.snmpTimeout;
		cloneInfo.snmpRetryCount = this.snmpRetryCount;

		// WBEM
		cloneInfo.wbemUser = this.wbemUser;
		cloneInfo.wbemUserPassword = this.wbemUserPassword;
		cloneInfo.wbemPort = this.wbemPort;
		cloneInfo.wbemProtocol = this.wbemProtocol;
		cloneInfo.wbemTimeout = this.wbemTimeout;
		cloneInfo.wbemRetryCount = this.wbemRetryCount;

		// IPMI
		cloneInfo.ipmiIpAddress = this.ipmiIpAddress;
		cloneInfo.ipmiPort = this.ipmiPort;
		cloneInfo.ipmiUser = this.ipmiUser;
		cloneInfo.ipmiUserPassword = this.ipmiUserPassword;
		cloneInfo.ipmiTimeout = this.ipmiTimeout;
		cloneInfo.ipmiRetries = this.ipmiRetries;
		cloneInfo.ipmiProtocol = this.ipmiProtocol;
		cloneInfo.ipmiLevel = this.ipmiLevel;

		// WinRM
		cloneInfo.winrmUser = this.winrmUser;
		cloneInfo.winrmUserPassword = this.winrmUserPassword;
		cloneInfo.winrmVersion = this.winrmVersion;
		cloneInfo.winrmPort = this.winrmPort;
		cloneInfo.winrmProtocol = this.winrmProtocol;
		cloneInfo.winrmTimeout = this.winrmTimeout;
		cloneInfo.winrmRetries = this.winrmRetries;

		// デバイス
		//参照型は一旦別のオブジェクトへコピー
		ArrayList<NodeDeviceInfo> tmpDeviceList = new ArrayList<NodeDeviceInfo>();
		for (NodeDeviceInfo thisInfo : this.nodeDeviceInfo) {
			tmpDeviceList.add(thisInfo.clone());
		}
		cloneInfo.nodeDeviceInfo = tmpDeviceList;

		ArrayList<NodeCpuInfo> tmpCpuList = new ArrayList<NodeCpuInfo>();
		for (NodeCpuInfo thisInfo : this.nodeCpuInfo) {
			tmpCpuList.add(thisInfo.clone());
		}
		cloneInfo.nodeCpuInfo = tmpCpuList;

		ArrayList<NodeMemoryInfo> tmpMemList = new ArrayList<NodeMemoryInfo>();
		for (NodeMemoryInfo thisInfo : this.nodeMemoryInfo) {
			tmpMemList.add(thisInfo.clone());
		}
		cloneInfo.nodeMemoryInfo = tmpMemList;

		ArrayList<NodeDiskInfo> tmpDiskList = new ArrayList<NodeDiskInfo>();
		for (NodeDiskInfo thisInfo : this.nodeDiskInfo) {
			tmpDiskList.add(thisInfo.clone());
		}
		cloneInfo.nodeDiskInfo = tmpDiskList;

		ArrayList<NodeNetworkInterfaceInfo> tmpNwIfList = new ArrayList<NodeNetworkInterfaceInfo>();
		for (NodeNetworkInterfaceInfo thisInfo : this.nodeNetworkInterfaceInfo) {
			tmpNwIfList.add(thisInfo.clone());
		}
		cloneInfo.nodeNetworkInterfaceInfo = tmpNwIfList;

		ArrayList<NodeFilesystemInfo> tmpFSList = new ArrayList<NodeFilesystemInfo>();
		for (NodeFilesystemInfo thisInfo : this.nodeFilesystemInfo) {
			tmpFSList.add(thisInfo.clone());
		}
		cloneInfo.nodeFilesystemInfo = tmpFSList;

		// クラウド・仮想化
		cloneInfo.cloudService = this.cloudService;
		cloneInfo.cloudScope = this.cloudScope;
		cloneInfo.cloudResourceType = this.cloudResourceType;
		cloneInfo.cloudResourceName = this.cloudResourceName;
		cloneInfo.cloudResourceId = this.cloudResourceId;
		cloneInfo.cloudLocation = this.cloudLocation;

		// ノード変数
		ArrayList<NodeVariableInfo> tmpVlList = new ArrayList<NodeVariableInfo>();
		for (NodeVariableInfo thisInfo : this.nodeVariableInfo) {
			tmpVlList.add(thisInfo.clone());
		}
		cloneInfo.nodeVariableInfo = tmpVlList;

		// 保守
		cloneInfo.administrator = this.administrator;
		cloneInfo.contact = this.contact;

		// 備考
		ArrayList<NodeNoteInfo> tmpNoteList = new ArrayList<NodeNoteInfo>();
		for (NodeNoteInfo thisInfo : this.nodeNoteInfo) {
			tmpNoteList.add(thisInfo.clone());
		}
		cloneInfo.nodeNoteInfo = tmpNoteList;

		return cloneInfo;

	}

	public java.lang.String getSshUser() {
		return sshUser;
	}

	public void setSshUser(java.lang.String sshUser) {
		this.sshUser = sshUser;
	}

	public java.lang.String getSshUserPassword() {
		return sshUserPassword;
	}

	public void setSshUserPassword(java.lang.String sshUserPassword) {
		this.sshUserPassword = sshUserPassword;
	}
	
	public java.lang.String getSshPrivateKeyFilepath() {
		return this.sshPrivateKeyFilepath;
	}
	
	public void setSshPrivateKeyFilepath(java.lang.String sshPrivateKeyFilepath) {
		this.sshPrivateKeyFilepath = sshPrivateKeyFilepath;
	}
	
	public java.lang.String getSshPrivateKeyPassphrase() {
		return this.sshPrivateKeyPassphrase;
	}
	
	public void setSshPrivateKeyPassphrase(java.lang.String sshPrivateKeyPassphrase) {
		this.sshPrivateKeyPassphrase = sshPrivateKeyPassphrase;
	}
	
	public java.lang.Integer getSshPort() {
		return this.sshPort;
	}
	
	public void setSshPort(java.lang.Integer sshPort) {
		this.sshPort = sshPort;
	}
	
	public java.lang.Integer getSshTimeout() {
		return this.sshTimeout;
	}
	
	public void setSshTimeout(java.lang.Integer sshTimeout) {
		this.sshTimeout = sshTimeout;
	}

	/**
	 * 自動デバイスサーチのgetterです。
	 * @return Boolean
	 */
	public Boolean isAutoDeviceSearch()
	{
		return this.autoDeviceSearch;
	}

	/**
	 * 自動デバイスサーチのsetterです。
	 * 有効の場合1、無効の場合0を設定します。
	 * @param autoDeviceSearch
	 */
	public void setAutoDeviceSearch( Boolean autoDeviceSearch )
	{
		this.autoDeviceSearch = autoDeviceSearch;
	}

}
