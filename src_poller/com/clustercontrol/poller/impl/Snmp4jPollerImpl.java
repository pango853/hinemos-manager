/*

 Copyright (C) 2008 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 */

package com.clustercontrol.poller.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.util.DefaultPDUFactory;

import com.clustercontrol.bean.SnmpProtocolConstant;
import com.clustercontrol.bean.SnmpSecurityLevelConstant;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.nodemap.util.SearchConnectionProperties;
import com.clustercontrol.poller.SnmpPoller;
import com.clustercontrol.poller.bean.PollerProtocolConstant;
import com.clustercontrol.repository.util.SearchDeviceProperties;
import com.clustercontrol.sharedtable.DataTable;

/**
 * snmp4jライブラリを用いて実装したsnmpポーリングクラス
 * 
 * v3のユーザ作成について、下記のページを参照してください。
 * https://access.redhat.com/documentation/ja-JP/Red_Hat_Enterprise_Linux/6/html/Deployment_Guide/sect-System_Monitoring_Tools-Net-SNMP-Configuring.html
 */
public class Snmp4jPollerImpl implements SnmpPoller {

	private final static Log log = LogFactory.getLog(Snmp4jPollerImpl.class);

	private static final String PROP_DELETE_LABEL = "monitor.resource.delete.label";
	private final static String PROP_NON_REPEATERS = "monitor.poller.snmp.bulk.nonrepeaters";
	private final static String PROP_MAX_REPETITIONS = "monitor.poller.snmp.bulk.maxrepetitions";
	private final static String PROP_NOT_V3_SNMP_POOL_SIZE = "monitor.poller.snmp.not.v3.snmp.pool.size";
	public final static String LABEL_REPLACE_KEY = "monitor.resource.label.replace";
	public final static String LABEL_REPLACE_DEFAULT = " Label:\\S*  Serial Number .*";

	private final List<String> processOidList;

	private final Integer maxRepetitions = HinemosPropertyUtil.getHinemosPropertyNum(PROP_MAX_REPETITIONS, 10);
	private final Integer nonRepeaters  = HinemosPropertyUtil.getHinemosPropertyNum(PROP_NON_REPEATERS, 0);
	private final boolean deleteLabel = HinemosPropertyUtil.getHinemosPropertyBool(PROP_DELETE_LABEL, true);
	
	private final int notV3SnmpPoolSize = HinemosPropertyUtil.getHinemosPropertyNum(PROP_NOT_V3_SNMP_POOL_SIZE, 32);
	
	private List<Snmp> notV3SnmpPool = new ArrayList<Snmp>(notV3SnmpPoolSize);
	private int notV3SnmpPoolIndex = 0;
	
	private static Snmp4jPollerImpl instance = new Snmp4jPollerImpl();
	
	private Snmp4jPollerImpl() {
		try {
			for (int i = 0; i < notV3SnmpPoolSize; i++) {
				notV3SnmpPool.add(createNotV3Snmp());
			}
		} catch (IOException e) {
			log.warn("IOException message=" + e.getMessage());
		}
		
		String oidName = ".1.3.6.1.2.1.25.4.2.1.2";
		String oidParam = ".1.3.6.1.2.1.25.4.2.1.5";
		String oidPath = ".1.3.6.1.2.1.25.4.2.1.4";
		processOidList = new ArrayList<String>();
		processOidList.add(oidName);
		processOidList.add(oidParam);
		processOidList.add(oidPath);
	}
	
	public static Snmp4jPollerImpl getInstance() {
		return instance;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.clustercontrol.poller.impl.SnmpPoller#polling(java.lang.String, int, int, java.lang.String, int, int, java.util.List, boolean)
	 */
	@Override
	public DataTable polling(
			String ipAddress,
			int port,
			int version,
			String community,
			int retries,
			int timeout,
			List<String> oidList,
			String securityLevel,
			String user,
			String authPassword,
			String privPassword,
			String authProtocol,
			String privProtocol) {

		if (log.isDebugEnabled()) {
			String[][] nameValues = {
					{"ipAddress", ipAddress},
					{"port", String.valueOf(port)},
					{"version", String.valueOf(version)},
					{"community", community},
					{"retries", String.valueOf(retries)},
					{"timeout", String.valueOf(timeout)},
					{"securityLevel", securityLevel},
					{"user", user},
					{"authPassword", authPassword},
					{"privPassword", privPassword},
					{"authProtocol", authProtocol},
					{"privProtocol", privProtocol}
			};
			StringBuffer buffer = new StringBuffer();
			for (String[] nameValue : nameValues) {
				buffer.append(String.format("%s=%s, ", nameValue[0], nameValue[1]));
			}

			for (int i = 0; i < oidList.size(); i++) {
				buffer.append(String.format("oidList[%d]=%s, ", i, oidList.get(i)));
			}
			log.debug(buffer.toString());
		}

		//retriesは本当が試行回数だが、hinemosで実行回数になっているため、それに1を減らす。
		if (--retries < 0) {
			retries =0;
		}
		
		DataTable dataTable = new DataTable();

		Snmp snmp = null;
		try {
			if (version == SnmpConstants.version3) {
				snmp = createV3Snmp(securityLevel, user, authPassword,
						privPassword, authProtocol, privProtocol);
			} else {
				snmp = getNotV3SnmpFromPool();
			}

			oidList = formalizeOidList(oidList);
			DefaultPDUFactory factory = createPduFactory(oidList, version);
			
			Target target = createTarget(ipAddress, port, version,
					community, retries, timeout, securityLevel, user);

			MultipleOidsUtils utils = new MultipleOidsUtils(snmp, factory);
			
			int maxRetry = HinemosPropertyUtil.getHinemosPropertyNum("monitor.poller.snmp.max.retry", 3);
			boolean errorFlag = true;
			for (int i = 0; i < maxRetry; i++) {
				Collection<VariableBinding> vbs = utils.query(target, createColumnOidList(oidList).toArray(new OID[0])); 
				DataTable dataTableNotChecked = createDataTable(vbs);
				
				// 最後まで到達
				if (isDataTableValid(oidList, dataTableNotChecked)) {
					dataTable = dataTableNotChecked;
					errorFlag = false;
					break;
				}
			}
			if (errorFlag) {
				log.warn("reach max retry(" + maxRetry + ")");
			}
		} catch (IOException e) {
			log.warn("polling : IOException message=" + e.getMessage());
		} finally {
			if (version == SnmpConstants.version3 && snmp != null) {
				try {
					snmp.close();
				} catch (IOException e) {
					log.warn(e);
				}
			}
		}

		return dataTable;
	}

	public synchronized Snmp getNotV3SnmpFromPool() {
		if (notV3SnmpPoolIndex >= notV3SnmpPoolSize) {
			notV3SnmpPoolIndex = 0;
		}
		
		return notV3SnmpPool.get(notV3SnmpPoolIndex++);
	}

	private static int convertSecurityLevelToInt(String securityLevel) {
		if (SnmpSecurityLevelConstant.AUTH_NOPRIV.equals(securityLevel)) {
			return SecurityLevel.AUTH_NOPRIV;
		} else if (SnmpSecurityLevelConstant.AUTH_PRIV.equals(securityLevel)) {
			return SecurityLevel.AUTH_PRIV;
		}

		return SecurityLevel.NOAUTH_NOPRIV;
	}

	private List<OID> createColumnOidList(List<String> oidList) {
		List<OID> columnOIDList = new ArrayList<OID>();
		for (String oid : oidList) {
			columnOIDList.add(new OID(oid));
		}
		return columnOIDList;
	}

	private DataTable createDataTable(Collection<VariableBinding> vbs) {
		DataTable dataTable = new DataTable();
		
		long time = System.currentTimeMillis();
		for (VariableBinding binding : vbs) {
			if (binding == null) {
				continue;
			}
			String oidString = "." + binding.getOid().toDottedString();
			dataTable.putValue(
					getEntryKey(oidString),
					time,
					getVariableValue(oidString,
							binding.getVariable()));
		}
		
		return dataTable;
	}

	private static Target createNotV3Target(String ipAddress, int port, int version,
			String community, int retries, int timeout) {
		CommunityTarget target = new CommunityTarget();
		target.setAddress(new UdpAddress(String.format("%s/%d", ipAddress, port)));
		target.setCommunity(new OctetString(community));
		target.setTimeout(timeout);
		target.setRetries(retries);
		target.setVersion(version);
		return target;
	}

	private DefaultPDUFactory createPduFactory(List<String> oidList, int version) {
		DefaultPDUFactory factory = new DefaultPDUFactory();
		
		// SNMPv1以外のプロセス監視の場合はBULK
		if (isProcessOidList(oidList) && version != SnmpConstants.version1) {
			factory.setPduType(PDU.GETBULK);
			factory.setMaxRepetitions(maxRepetitions);
			factory.setNonRepeaters(nonRepeaters);
		//　それ以外の場合はv4.1踏襲のGETNEXT
		}else{
			factory.setPduType(PDU.GETNEXT);
		}
		return factory;
	}

	private Snmp createNotV3Snmp() throws IOException {
		Snmp snmp = new Snmp(new UdpTransportMappingImpl());
		snmp.listen();
		return snmp;
	}

	public Snmp createV3Snmp(String securityLevel, String user, String authPassword, 
			String privPassword, String authProtocol, String privProtocol) throws IOException {
		Snmp snmp = new Snmp(new UdpTransportMappingImpl());

		OctetString securityName = new OctetString(user);
		USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
		SecurityModels.getInstance().addSecurityModel(usm);

		OID authProtocolOid = AuthMD5.ID;
		if (SnmpProtocolConstant.SHA.equals(authProtocol)) {
			authProtocolOid = AuthSHA.ID;
		}
		OID privProtocolOid = PrivDES.ID;
		if (SnmpProtocolConstant.AES.equals(privProtocol)) {
			privProtocolOid = PrivAES128.ID;
		}

		UsmUser usmUser;
		if (convertSecurityLevelToInt(securityLevel) == SecurityLevel.NOAUTH_NOPRIV) {
			usmUser = new UsmUser(securityName, null, null, null, null);
		} else if (convertSecurityLevelToInt(securityLevel) == SecurityLevel.AUTH_NOPRIV) {
			usmUser = new UsmUser(securityName, authProtocolOid, new OctetString(authPassword),
					null, null);
		} else {
			usmUser = new UsmUser(securityName, authProtocolOid, new OctetString(authPassword),
					privProtocolOid, new OctetString(privPassword));
		}
		snmp.getUSM().addUser(securityName, usmUser);

		snmp.listen();
		return snmp;
	}

	public static Target createTarget(String ipAddress, int port,
			int version, String community, int retries, int timeout,
			String securityLevel, String user) {
		Target target = null;
		if (version == SnmpConstants.version3) {
			target = createV3Target(ipAddress, port, version, community, retries, timeout, securityLevel, user);
		} else {
			target = createNotV3Target(ipAddress, port, version, community, retries,
					timeout);
		}
		return target;
	}

	private static Target createV3Target(String ipAddress, int port, int version, String community, int retries, int timeout,
			String securityLevel, String user) {
		UserTarget target = new UserTarget();
		target.setAddress(new UdpAddress(String.format("%s/%d", ipAddress, port)));
		target.setTimeout(timeout);
		target.setRetries(retries);
		target.setVersion(version);
		target.setSecurityLevel(convertSecurityLevelToInt(securityLevel));
		target.setSecurityName(new OctetString(user));
		return target;
	}

	private List<String> formalizeOidList(List<String> oidList) {
		List<String> newOidList = new ArrayList<String>(oidList.size());
		for (String oid : oidList) {
			//snmp4jのgetbulkが、末尾が0であるOIDを対応していないため、
			//.XX.YY.0ようなOIDを.XX.YYに変換
			if (oid.endsWith(".0")) {
				oid = oid.substring(0, oid.length() - 2);
			}
			newOidList.add(oid);
		}

		return newOidList;
	}

	/**
	 * DataTableに格納するためのEntryKeyを返すメソッド
	 *
	 * @param oidString OID
	 */
	private String getEntryKey(String oidString){
		return PollerProtocolConstant.PROTOCOL_SNMP + "." + oidString;
	}

	private Serializable getVariableValue(String oidString, Variable variable) {
		switch (variable.getSyntax()) {
		case SMIConstants.SYNTAX_OCTET_STRING:
			OctetString octStr = (OctetString) variable;
			String value = "";
			byte[] bytes = octStr.getValue();
			if ((oidString.startsWith(SearchDeviceProperties.getOidNicMacAddress()) ||
					oidString.startsWith(SearchConnectionProperties.DEFAULT_OID_ARP) ||
					oidString.startsWith(SearchConnectionProperties.DEFAULT_OID_FDB)) && bytes.length == 6)  {

				// 6 byteのbinaryのOctetString
				// 00:0A:1F:5F:30 という値として扱う
				for (byte b : bytes) {
					String part = String.format("%02x", b).toUpperCase();
					if (value.equals("")) {
						value += part;
					} else {
						value += ":" + part;
					}
				}
			} else {
				// WindowsのNIC名には0x00が含まれることがあるので除外する
				int length = bytes.length;
				for (int i = 0; i < bytes.length; i++) {
					if (bytes[i] == 0x00) {
						length = i;
						break;
					}
				}
				value = new String(bytes, 0, length);
			}

			log.debug("SnmpPollerImpl deleteLabel=" + deleteLabel);

			// Windowsのファイルシステム名からラベルを削除する。
			// C:\ Label:ABC  Serial Number 80f3e65c
			// ↓
			// C:\
			if (deleteLabel && oidString.startsWith(SearchDeviceProperties.getOidFilesystemName())) {
				value = value.replaceAll(HinemosPropertyUtil.getHinemosPropertyStr(LABEL_REPLACE_KEY, LABEL_REPLACE_DEFAULT), "");
			}
			return value;

		case SMIConstants.SYNTAX_OBJECT_IDENTIFIER:
			return "."  + variable.toString();

		default:
			return variable.toLong();
		}
	}

	private boolean isDataTableValid(List<String> oidList, DataTable dataTable) {
		//プロセス監視に関して、すべてoidの結果の数が同じであることをチェックする
		if (isProcessOidList(oidList)) {
			return isDataTableValidForProcess(oidList, dataTable);
		}

		return true;
	}

	private boolean isDataTableValidForProcess(List<String> oidList,
			DataTable dataTable) {
		
		int lastCount = -1;
		ArrayList<ArrayList<String>> pidListList = new ArrayList();
		
		// プロセス監視の3つのOIDについて、取得したデータ長及び、PIDがそろっているかを確認する
		for (String oid : oidList) {
			ArrayList<String> pidList = new ArrayList<String>();
			
			oid = getEntryKey(oid);
			int count = 0;
			for (String dataTableOid : dataTable.keySet()) {
				if (dataTableOid.startsWith(oid)) {
					count++;
					
					// PID取得&設定
					String pid = dataTableOid.substring(dataTableOid.lastIndexOf("."));
					pidList.add(pid);
				}
			}

			//Listをソート
			Collections.sort(pidList);
			pidListList.add(pidList);

			//最初のOIDの個数をセット
			if (lastCount == -1) {
				lastCount = count;
				continue;
			}

			//各OIDの個数をチェック
			if (lastCount != count) {
				return false;
			}
		}
		//全PID Listがそろっているかをチェックする
		ArrayList<String>[] pidListArray = new ArrayList[3];
		for(int i = 0; oidList.size() > i; i++){
			pidListArray[i] = pidListList.get(i);
		}
		
		for(int i = 0; lastCount > i; i++){
			String pid = null;
			for(int j = 0; pidListArray.length > j; j++){
				//最初のPIDをセット
				if(pid == null){
					pid = pidListArray[j].get(i);
					continue;
				}
				
				if(!pid.equals(pidListArray[j].get(i))){
					log.warn("isDataTableValidForProcess don't match. pid = " + pid + " is not exit.");
					log.warn("isDataTableValidForProcess current list = " + pidListArray[j].toString());
					return false;
				}
				else{
					if(log.isDebugEnabled()){
						log.debug("isDataTableValidForProcess match. pid = " + pid + " is exit.");
					}
				}
			}
		}
		
		if(log.isDebugEnabled()){
			log.debug("isDataTableValidForProcess success.");
		}
		return true;
	}

	private boolean isProcessOidList(List<String> oidList) {
		return oidList.size() == processOidList.size() && oidList.containsAll(processOidList);
	}
}