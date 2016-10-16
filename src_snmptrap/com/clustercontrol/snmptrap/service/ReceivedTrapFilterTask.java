/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.snmptrap.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.ProcessConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.snmptrap.bean.MonitorTrapConstant;
import com.clustercontrol.snmptrap.bean.SnmpTrap;
import com.clustercontrol.snmptrap.bean.SnmpVarBind;
import com.clustercontrol.snmptrap.bean.TrapId;
import com.clustercontrol.snmptrap.model.MonitorTrapInfoEntity;
import com.clustercontrol.snmptrap.model.MonitorTrapValueInfoEntity;
import com.clustercontrol.snmptrap.model.MonitorTrapVarbindPatternInfoEntity;
import com.clustercontrol.snmptrap.util.SnmpTrapConstants;
import com.clustercontrol.snmptrap.util.SnmpTrapNotifier;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * フィルタリング処理を実装したクラス
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class ReceivedTrapFilterTask implements Runnable {

	/*
	 * 受信したSNMPTRAPにマッチするSNMPTRAP監視設定を順番に処理するためのクラス
	 * 
	 * クエリの結果、受信したSNMPTRAPにマッチする(MonitorInfoEntity, MonitorTrapValueInfoEntity)の配列のリストが得られるので、
	 * MonitorInfoEntityごとに順番に処理する
	 * 
	 * 例：SNMPTRAP監視設定A,Bがあり、受信したSNMPTRAPが、監視設定Aのトラップ定義A-1とトラップ定義A-2、監視設定Bのトラップ定義B-1にマッチする場合
	 * resultsには、以下のように2要素の配列から成るリストが設定される
	 * [[監視設定A, トラップ定義A-1],
	 *  [監視設定A, トラップ定義A-2],
	 *  [監視設定B, トラップ定義B-1]]
	 *  
	 *  この場合next()を実行すると、監視設定A, 監視設定Bと順に得られる。
	 *  next()で監視設定Aが得られた後、次にnext()を実行するまでgetValueInfoList()を実行すると、リスト[トラップ定義A-1, トラップ定義A-2]が得られ、
	 *  next()で監視設定Bが得られた後、次にnext()を実行するまでgetValueInfoList()を実行すると、リスト[トラップ定義B-1]が得られる。
	 */
	private static class QueryResultIterator<E> implements Iterator<E> {
		
		/* 受信したSNMPTRAPにマッチする(MonitorInfoEntity, MonitorTrapValueInfoEntity)の配列のリスト
		 * 配列の0番目にはMonitorInfoEntityが、1番目にはMonitorTrapValueInfoEntityが入る
		 */
		private List<Object[]> results;
		private static int MONITOR_INFO = 0;
		private static int MONITOR_TRAP_VALUE_INFO = 1;
		
		// 処理しているMonitorInfoEntityを示すための、リストresultsのインデックス
		private int current;
		// currentに対応するMonitorInfoEntity
		private MonitorInfoEntity currentMonitorInfo;
		// リストresultsのインデックス(next()で次に得られるものを示す）
		private Integer nextIndex = null;
		
		public QueryResultIterator(List<Object[]> results) {
			this.results = results;
			this.current = 0;
			this.currentMonitorInfo = results.isEmpty() ? null: (MonitorInfoEntity)results.get(current)[MONITOR_INFO];
			this.nextIndex = currentMonitorInfo == null ? -1: current;
		}

		@Override
		public boolean hasNext() {
			if (nextIndex == null) {
				for (int i = current; i < results.size(); ++i) {
					MonitorInfoEntity o = (MonitorInfoEntity)results.get(i)[MONITOR_INFO];
					if (o != null && o != currentMonitorInfo) {
						nextIndex = i;
						current = i;
						currentMonitorInfo = o;
						break;
					}
				}
				if (nextIndex == null) {
					nextIndex = -1;
				}
			}
			return nextIndex != -1;
		}
		
		// MonitorInfoEntityに対応するMonitorTrapValueInfoEntity(受信したトラップにマッチするもの)のリストを返す
		private List<MonitorTrapValueInfoEntity> getValueInfoList() {
			List<MonitorTrapValueInfoEntity> list = new ArrayList<MonitorTrapValueInfoEntity>();
			for (int i = current; i < results.size(); ++i) {
				MonitorInfoEntity o = (MonitorInfoEntity)results.get(i)[MONITOR_INFO];
				if (o != null && o == currentMonitorInfo) {
					// MonitorInfoEntityに対応するMonitorTrapValueInfoEntityが複数ある場合はすべてリストにつめる
					MonitorTrapValueInfoEntity entity = (MonitorTrapValueInfoEntity)results.get(i)[MONITOR_TRAP_VALUE_INFO];
					if (entity != null) {
						list.add(entity);
					}
				} else {
					break;
				}
			}
			
			return list;
		}

		// MonitorInfoEntityを順番に返す
		@SuppressWarnings("unchecked")
		@Override
		public E next() {
			if (hasNext()) {
				Object result = results.get(nextIndex)[MONITOR_INFO];
				nextIndex = null;
				return (E)result;
			}
			return null;
		}

		@Override
		public void remove() {
			if ((MonitorInfoEntity)results.get(current)[MONITOR_INFO] == currentMonitorInfo) {
				results.remove(current);
				nextIndex = null;
			}
		}
	}

	private Logger logger = Logger.getLogger(this.getClass());

	private List<SnmpTrap> receivedTrapList;
	private SnmpTrapNotifier notifier;
	private TrapProcCounter counter;
	private Charset defaultCharset;

	public ReceivedTrapFilterTask(List<SnmpTrap> receivedTrapList, SnmpTrapNotifier notifier, TrapProcCounter counter, Charset defaultCharset) {
		this.receivedTrapList = receivedTrapList;
		this.notifier = notifier;
		this.counter = counter;
		this.defaultCharset = defaultCharset;
	}

	public ReceivedTrapFilterTask(SnmpTrap receivedTrap,
			SnmpTrapNotifier notifier, TrapProcCounter counter, Charset defaultCharset) {

		this(Arrays.asList(receivedTrap), notifier, counter, defaultCharset);
	}

	@Override
	public void run() {
		JpaTransactionManager tm = null;
		boolean warn = true;

		try {
			tm = new JpaTransactionManager();
			tm.begin();

			HinemosEntityManager em = tm.getEntityManager();

			List<MonitorInfoEntity> monitorList = QueryUtil.getMonitorInfoByMonitorTypeId(HinemosModuleConstant.MONITOR_SNMPTRAP);
			if (monitorList == null) {
				if (logger.isDebugEnabled()) {
					for (SnmpTrap receivedTrap : receivedTrapList) {
						logger.debug("snmptrap monitor not found. skip filtering. [" + receivedTrap + "]");
					}
				}
				return;
			}

			// 以下の処理は、未指定トラップの検出のために追加。
			// 上記クエリは、MonitorTrapValueInfoEntity に焦点を置いている。トラップ情報にマッチしないと、MonitorInfoEntity が残らない。
			// トラップ情報にマッチしなかった監視情報を抽出するために、以下の処理を行う。
			List<MonitorInfoEntity> unspecifiedFlgMonitorInfoEntityList = em
					.createNamedQuery(
							"MonitorInfoEntity.findByNotifyofReceivingUnspecifiedFlg",
							MonitorInfoEntity.class).getResultList();

			List<SnmpTrap> receivedTrapBuffer = new ArrayList<SnmpTrap>();
			List<MonitorInfoEntity> monitorBuffer = new ArrayList<MonitorInfoEntity>();
			List<Integer> priorityBuffer = new ArrayList<Integer>();
			List<String> facilityIdBuffer = new ArrayList<String>();
			List<String[]> msgsBuffer = new ArrayList<String[]>();
			Map<String, List<String>> matchedFacilityIdListMap = new HashMap<String, List<String>>();

			RepositoryControllerBean repositoryCtrl = new RepositoryControllerBean();
			for (SnmpTrap receivedTrap : receivedTrapList) {
				String ipAddr =receivedTrap.getAgentAddr();
				if (!matchedFacilityIdListMap.containsKey(ipAddr)) {
					List<String> matchedFacilityIdList = repositoryCtrl.getFacilityIdByIpAddress(InetAddress.getByName(ipAddr));
					matchedFacilityIdListMap.put(ipAddr, matchedFacilityIdList);
				}
			}
			
			for (SnmpTrap receivedTrap : receivedTrapList) {
				List<Object[]> results = getTargetMonitorTrapList(
						receivedTrap, em, unspecifiedFlgMonitorInfoEntityList);

				QueryResultIterator<MonitorInfoEntity> monitorIter = new QueryResultIterator<MonitorInfoEntity>(results);
				while (monitorIter.hasNext()) {
					MonitorInfoEntity currentMonitor = monitorIter.next();
	
					// カレンダの有効期間外の場合、スキップする
					if (isInDisabledCalendar(currentMonitor, receivedTrap)) {
						continue;
					}

					MonitorTrapInfoEntity trapInfo = currentMonitor.getMonitorTrapInfoEntity();
					// コミュニティのチェック
					if (trapInfo.getCommunityCheck() != MonitorTrapConstant.COMMUNITY_CHECK_OFF) {
						if (!trapInfo.getCommunityName().equals(receivedTrap.getCommunity())) {
							if (logger.isDebugEnabled()) {
								logger.debug("community " + trapInfo.getCommunityName() + " is not matched. [" + receivedTrap + "]");
							}
							continue;
						}
					}
					
					String ipAddr = receivedTrap.getAgentAddr();
					List<String> notifyFacilityIdList = getNotifyFacilityIdList(
							currentMonitor, receivedTrap,
							matchedFacilityIdListMap.get(ipAddr));
					if (notifyFacilityIdList.isEmpty()) {
						if (logger.isDebugEnabled()) {
							logger.debug("notification facilities not found [" + receivedTrap + "]");
						}
						continue;
					}
	
					// varbindの文字列変換
					Charset charset = defaultCharset;
					if (trapInfo.getCharsetConvert() == MonitorTrapConstant.CHARSET_CONVERT_ON) {
						if (trapInfo.getCharsetName() != null) {
							if (Charset.isSupported(trapInfo.getCharsetName())) {
								charset = Charset.forName(trapInfo.getCharsetName());
							} else {
								logger.warn("not supported charset : " + trapInfo.getCharsetName());
							}
						}
					}
	
					List<SnmpVarBind> varBinds = receivedTrap.getVarBinds();
					String[] varBindStrs = new String[varBinds.size()];
					for (int i = 0; i < varBinds.size(); i++) {
						switch (varBinds.get(i).getType()) {
						case OctetString :
						case Opaque :
							varBindStrs[i] = new String(varBinds.get(i).getObject(), charset);
							break;
						default :
							varBindStrs[i] = new String(varBinds.get(i).getObject());
						}
					}

					MonitorTrapValueInfoEntity matchedTrapValueInfo = null;
					MonitorTrapVarbindPatternInfoEntity matchedPattern = null;
					String matchedString = null;
					
					List<MonitorTrapValueInfoEntity> valueList = monitorIter.getValueInfoList();
					Iterator<MonitorTrapValueInfoEntity> valueIterator = valueList.iterator();
					
					if (!valueIterator.hasNext() && ValidConstant.typeToBoolean(trapInfo.getNotifyofReceivingUnspecifiedFlg())) {
						// マッチするTRAPの設定が存在せず、存在しない場合に通知する場合
						String[] msgs = createReceivingUnspecifiedMessages(trapInfo, varBindStrs, receivedTrap);
						for (String facilityId : notifyFacilityIdList) {
							receivedTrapBuffer.add(receivedTrap);
							monitorBuffer.add(currentMonitor);
							priorityBuffer.add(trapInfo.getPriorityUnspecified());
							facilityIdBuffer.add(facilityId);
							msgsBuffer.add(msgs);
							counter.countupNotified();
						}
						continue;
					}

					while (valueIterator.hasNext()) {
						MonitorTrapValueInfoEntity currentValueInfo = valueIterator.next();
							

						if (currentValueInfo.getProcessingVarbindType() == MonitorTrapConstant.PROC_VARBIND_ANY) {
							// varbindで判定をおこなわない場合
							matchedTrapValueInfo = currentValueInfo;
							if (!SnmpTrapConstants.genericTrapV2Set.contains(currentValueInfo.getId().getTrapOid())) {
								// GENERIC TRAPのOIDより、個別のOIDを優先する
								break;
							}
						} else {
							// varbindで判定をおこなう場合
							
							String varBindStr = getBindedString(currentValueInfo.getFormatVarBinds(), varBindStrs);
							List<MonitorTrapVarbindPatternInfoEntity> patterns = new ArrayList<>(currentValueInfo.getMonitorTrapVarbindPatternInfoEntities());
							Collections.sort(patterns, new Comparator<MonitorTrapVarbindPatternInfoEntity>() {
								@Override
								public int compare(MonitorTrapVarbindPatternInfoEntity o1, MonitorTrapVarbindPatternInfoEntity o2) {
									return o1.getId().getOrderNo().compareTo(o2.getId().getOrderNo());
								}
							});
								
							for (MonitorTrapVarbindPatternInfoEntity currentPattern: patterns) {
								if (!ValidConstant.typeToBoolean(trapInfo.getNotifyofReceivingUnspecifiedFlg()) && !ValidConstant.typeToBoolean(currentPattern.getValidFlg()))
									continue;
	
								
								Pattern pattern = null;
								if (ValidConstant.typeToBoolean(currentPattern.getCaseSensitivityFlg())) {
									// 大文字・小文字を区別しない場合
									pattern = Pattern.compile(currentPattern.getPattern(), Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
								} else {
									// 大文字・小文字を区別する場合
									pattern = Pattern.compile(currentPattern.getPattern(), Pattern.DOTALL);
								}
	
								// パターンマッチ表現でマッチング
								Matcher matcher = pattern.matcher(varBindStr);
								if (matcher.matches()) {
									matchedTrapValueInfo = currentValueInfo;
									matchedPattern = currentPattern;
									matchedString = varBindStr;
									break;
								}
							}
								
							if (matchedTrapValueInfo != null && !SnmpTrapConstants.genericTrapV2Set.contains(currentValueInfo.getId().getTrapOid())) {
								// GENERIC TRAPのOIDより、個別のOIDを優先する
								break;
							}
						}
					} // while valueIterator
					
					if (matchedTrapValueInfo == null) {
						continue;
					}

					if (matchedTrapValueInfo.getProcessingVarbindType() == MonitorTrapConstant.PROC_VARBIND_ANY) {
						// 変数にかかわらず常に通知する
						String[] msgs = createMessages(trapInfo, matchedTrapValueInfo, varBindStrs, receivedTrap);
						
						for (String facilityId : notifyFacilityIdList) {
							receivedTrapBuffer.add(receivedTrap);
							monitorBuffer.add(currentMonitor);
							priorityBuffer.add(matchedTrapValueInfo.getPriorityAnyVarbind());
							facilityIdBuffer.add(facilityId);
							msgsBuffer.add(msgs);
							counter.countupNotified();
						}
					} else {
						// 変数で判定する
						if (matchedPattern.getProcessType() == ProcessConstant.TYPE_YES) {
							// 処理する
							String[] msgs = createPattenMatchedOrgMessage(
									trapInfo, matchedTrapValueInfo,
									matchedPattern, matchedString,
									varBindStrs, receivedTrap);
							
							for (String facilityId : notifyFacilityIdList) {
								receivedTrapBuffer.add(receivedTrap);
								monitorBuffer.add(currentMonitor);
								priorityBuffer.add(matchedPattern.getPriority());
								facilityIdBuffer.add(facilityId);
								msgsBuffer.add(msgs);
								counter.countupNotified();
							}
						} else {
							//処理しない
						}
					}
				}//while monitorIter
				
			}//for
			
			notifier.put(receivedTrapBuffer, monitorBuffer, priorityBuffer, facilityIdBuffer, msgsBuffer);
			
			tm.commit();
			warn = false;
		} catch (Exception e) {
			logger.warn("unexpected internal error. : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			// HA構成のため、例外を握りつぶしてはいけない
			throw new RuntimeException("unexpected internal error. : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			if (tm != null) {
				tm.close();
			}

			if (warn) {
				for (SnmpTrap receivedTrap : receivedTrapList) {
					TrapId trapV1 = receivedTrap.getTrapId().asTrapV1Id();
					
					// Internal Event
					AplLogger apllog = new AplLogger("TRAP", "trap");
					String[] args = {
							trapV1.getEnterpriseId(),
							String.valueOf(trapV1.getGenericId()),
							String.valueOf(trapV1.getSpecificId())};
					apllog.put("SYS", "009", args);
				}
			}
		}
	}

	private boolean isInDisabledCalendar(MonitorInfoEntity monitorInfoEntity,
			SnmpTrap trap) {
		
		String calendarId = monitorInfoEntity.getCalendarId();
		boolean inDisabledCalendar = false;
		if (calendarId != null && !"".equals(calendarId)) {
			try {
				inDisabledCalendar = !new CalendarControllerBean().isRun(calendarId, trap.getReceivedTime());
				if (inDisabledCalendar) {
					if (logger.isDebugEnabled()) {
						logger.debug("calendar " + calendarId + " is not enabled term. [" + trap + "]");
					}
				}
			} catch (Exception e) {
				// カレンダが未定義の場合は、スキップせずに継続する（予期せぬロストの回避）
				logger.info("calendar " + calendarId
						+ " is not found, skip calendar check. [" + trap + "]");
			}
		}
		
		return inDisabledCalendar;
	}

	private List<Object[]> getTargetMonitorTrapList (
			SnmpTrap receivedTrap, HinemosEntityManager em,
			List<MonitorInfoEntity> unspecifiedFlgMonitorInfoEntityList) {
		TrapId trapV1;
		List<TrapId> trapV2List;
		if (receivedTrap.getTrapId().getVersion() == SnmpVersionConstant.TYPE_V1) {
			trapV1 = (TrapId)receivedTrap.getTrapId();
			trapV2List = trapV1.asTrapV2Id();
		} else {
			TrapId trap2 = (TrapId)receivedTrap.getTrapId();
			trapV1 = trap2.asTrapV1Id();
			trapV2List = Arrays.asList(trap2);
		}

		Query query = em.createNamedQuery("MonitorTrapValueInfoEntity.findByReceivedTrap");
		query.setParameter("enterpriseId", trapV1.getEnterpriseId());
		query.setParameter("genericId", trapV1.getGenericId());
		query.setParameter("specificId", trapV1.getSpecificId());
		List<String> snmpTrapOids = new ArrayList<>();
		for (TrapId trapV2: trapV2List) {
			snmpTrapOids.add(trapV2.getSnmpTrapOid());
		}
		query.setParameter("v2TrapOids", snmpTrapOids);
		query.setParameter("enterpriseSpecific", SnmpTrapConstants.SNMP_GENERIC_enterpriseSpecific);
		query.setParameter("genericTrapOid", SnmpTrapConstants.genericTrapV1Map.get(trapV1.getGenericId()));

		long startTime = System.currentTimeMillis();
		@SuppressWarnings("unchecked")
		List<Object[]> results = (List<Object[]>)query.getResultList();
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("finished searching traps. elapsed time = %d, matched count = %d", System.currentTimeMillis() -startTime, results.size()));
		}

		Set<String> monitorIds = new HashSet<>();
		for (Object[] m: results) {
			monitorIds.add(((MonitorInfoEntity)m[0]).getMonitorId());
		}
		for (MonitorInfoEntity m: unspecifiedFlgMonitorInfoEntityList) {
			if (!monitorIds.contains(m.getMonitorId()))
				results.add(new Object[]{m, null,null});
		}
		return results;
	}

	private List<String> getNotifyFacilityIdList(MonitorInfoEntity monitor, SnmpTrap receivedTrap, List<String>matchedFacilityIdList) throws HinemosUnknown, UnknownHostException {
		RepositoryControllerBean repositoryCtrl = new RepositoryControllerBean();

		if (matchedFacilityIdList == null || matchedFacilityIdList.isEmpty()) {
			matchedFacilityIdList = Arrays.asList(FacilityTreeAttributeConstant.UNREGISTEREFD_SCOPE);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("matched facilities : " + matchedFacilityIdList + " [" + receivedTrap + "]");
		}

		// 監視対象のファシリティID一覧を取得する
		List<String> targetFacilityIdList = Collections.emptyList();
		if (FacilityTreeAttributeConstant.UNREGISTEREFD_SCOPE.equals(monitor.getFacilityId())) {
			targetFacilityIdList = Arrays.asList(FacilityTreeAttributeConstant.UNREGISTEREFD_SCOPE);
		}	else {
			targetFacilityIdList = repositoryCtrl.getExecTargetFacilityIdList(monitor.getFacilityId(), monitor.getOwnerRoleId());
		}
		if (logger.isDebugEnabled()) {
			logger.debug("target facilities : " + targetFacilityIdList + " [" + receivedTrap + "]");
		}

		// 通知対象のファシリティID一覧を絞り込む
		List<String> notifyFacilityIdList = new ArrayList<String>(matchedFacilityIdList);
		notifyFacilityIdList.retainAll(targetFacilityIdList);

		return notifyFacilityIdList;
	}

	private String[] createMessages(MonitorTrapInfoEntity checkInfo, MonitorTrapValueInfoEntity valueInfo, String[] varBindStrs, SnmpTrap receivedTrap) {
		StringBuilder orgMessage = new StringBuilder();

		if (receivedTrap.getTrapId().getVersion() == SnmpVersionConstant.TYPE_V1) {
			TrapId trapV1 = (TrapId)receivedTrap.getTrapId();
			orgMessage.append("OID=").append(trapV1.getEnterpriseId()).append("\nTrapName=").append(valueInfo.getUei()).append('\n');
		}
		else {
			TrapId trapV2 = (TrapId)receivedTrap.getTrapId();
			orgMessage.append("OID=").append(trapV2.getSnmpTrapOid()).append("\nTrapName=").append(valueInfo.getUei()).append('\n');
		}

		StringBuilder detail = new StringBuilder();
		if (HinemosPropertyUtil.getHinemosPropertyBool("monitor.snmptrap.org.message.community", true))
			detail.append(Messages.getString("CommunityName")).append("=").append(receivedTrap.getCommunity()).append(" \n");

		if (HinemosPropertyUtil.getHinemosPropertyBool("monitor.snmptrap.org.message.varbind", true)) {
			boolean first = true;
			for (String value: varBindStrs) {
				if (first) {
					first = false;
					detail.append("VarBind=").append(value);
				}
				else
					detail.append(", ").append(value);
			}
			detail.append(" \n");
		}
		detail.append(Messages.getString("Description")).append("=").append(valueInfo.getDescription());
		orgMessage.append(getBindedString(detail.toString(), varBindStrs));

		return new String[]{getBindedString(valueInfo.getLogmsg(), varBindStrs), orgMessage.toString()};
	}

	private String[] createReceivingUnspecifiedMessages(MonitorTrapInfoEntity checkInfo, String[] varBindStrs, SnmpTrap receivedTrap) {
		StringBuilder orgMessage = new StringBuilder();
		if (HinemosPropertyUtil.getHinemosPropertyBool("monitor.snmptrap.org.message.community", true))
			orgMessage.append(Messages.getString("CommunityName")).append("=").append(receivedTrap.getCommunity()).append(" \n");

		if (HinemosPropertyUtil.getHinemosPropertyBool("monitor.snmptrap.org.message.varbind", true)) {
			boolean first = true;
			for (String value: varBindStrs) {
				if (first) {
					first = false;
					orgMessage.append("VarBind=").append(value);
				}
				else
					orgMessage.append(", ").append(value);
			}
			orgMessage.append(" \n");
		}

		if (receivedTrap.getTrapId().getVersion() == SnmpVersionConstant.TYPE_V1) {
			TrapId trapV1 = (TrapId)receivedTrap.getTrapId();
			orgMessage.append("version : ").append(SnmpVersionConstant.typeToString(trapV1.getVersion()))
				.append(", oid : ").append(trapV1.getEnterpriseId())
				.append(", generic_id : ").append(trapV1.getGenericId())
				.append(", specificId : ").append(trapV1.getSpecificId());
		}
		else {
			TrapId trapV2 = (TrapId)receivedTrap.getTrapId();
			orgMessage.append("version : ").append(SnmpVersionConstant.typeToString(trapV2.getVersion()))
				.append(", oid : ").append(trapV2.getSnmpTrapOid());
		}

		String msg = orgMessage.toString();
		return new String[]{msg, msg};
	}

	private String[] createPattenMatchedOrgMessage(
			MonitorTrapInfoEntity checkInfo,
			MonitorTrapValueInfoEntity valueInfo,
			MonitorTrapVarbindPatternInfoEntity pattern, String matchedString,
			String[] varBindStrs, SnmpTrap receivedTrap) {
		
		String[] msgs = createMessages(checkInfo, valueInfo, varBindStrs, receivedTrap);
		StringBuilder orgMessage = new StringBuilder().append(msgs[1])
				.append('\n').append("Pattern=").append(pattern.getPattern())
				.append('\n').append("Matched string=").append(matchedString);
		return new String[]{msgs[0], orgMessage.toString()};
	}

	private String getBindedString(String str, String[] varbinds) {
		if (str == null) {
			return "";
		}
		if (varbinds == null) {
			return str;
		}

		for (int i = 0; i < varbinds.length; i++) {
			if (logger.isDebugEnabled()) {
				logger.debug("binding : " + str + "  " + "%parm[#" + (i + 1) + "]% to " + varbinds[i] + "]");
			}
			str = str.replace("%parm[#" + (i + 1) + "]%", varbinds[i]);
			if (logger.isDebugEnabled()) {
				logger.debug("binded : " + str);
			}
		}

		return str;
	}
}