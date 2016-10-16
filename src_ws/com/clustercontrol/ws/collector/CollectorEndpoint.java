/*
Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.
 */
package com.clustercontrol.ws.collector;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.annotation.Resource;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.performance.bean.CollectedDataSet;
import com.clustercontrol.performance.bean.CollectorItemInfo;
import com.clustercontrol.performance.bean.PerformanceDataSettings;
import com.clustercontrol.performance.bean.PerformanceFilterInfo;
import com.clustercontrol.performance.bean.PerformanceListInfo;
import com.clustercontrol.performance.session.CollectorControllerBean;
import com.clustercontrol.ws.util.HashMapInfo;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * 性能管理用のWebAPIエンドポイント
 */
@MTOM
@javax.jws.WebService(targetNamespace = "http://collector.ws.clustercontrol.com")
public class CollectorEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( CollectorEndpoint.class );
	private static Log m_opelog = LogFactory.getLog("HinemosOperation");

	/**
	 * echo(WebサービスAPI疎通用)
	 *
	 * 権限必要なし（ユーザ名チェックのみ実施）
	 *
	 * @param str
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public String echo(String str) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return str + ", " + str;
	}

	/**
	 * 性能[一覧]に表示するためのリストを取得します。
	 * 以下の条件のうちいずれかを満たす監視設定を取得します。
	 *
	 * ・収集が有効になっていること
	 * ・収集データが1件以上存在すること
	 *
	 * PerformanceRead権限が必要
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<PerformanceListInfo> getPerformanceList() throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getPerformanceList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.PERFORMANCE, SystemPrivilegeMode.READ));
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Get, Method=getPerformanceList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new CollectorControllerBean().getPerformanceList();
	}

	/**
	 * 性能[一覧]に表示するためのリストを取得します。
	 * 以下の条件のうちいずれかを満たす監視設定を取得します。
	 *
	 * ・収集が有効になっていること
	 * ・収集データが1件以上存在すること
	 * ・フィルタ条件に合致すること
	 *
	 * PerformanceRead権限が必要
	 *
	 * @param condition
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public ArrayList<PerformanceListInfo> getPerformanceListByCondition(PerformanceFilterInfo condition) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getPerformanceList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.PERFORMANCE, SystemPrivilegeMode.READ));
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		if(condition != null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			StringBuffer msg = new StringBuffer();
			msg.append(", MonitorID=");
			msg.append(condition.getMonitorId());
			msg.append(", MonitorTypeID=");
			msg.append(condition.getMonitorTypeId());
			msg.append(", Description=");
			msg.append(condition.getDescription());
			msg.append(", OldestFromDate=");
			msg.append(condition.getOldestFromDate()==null?null:sdf.format(new Date(condition.getOldestFromDate())));
			msg.append(", OldestToDate=");
			msg.append(condition.getOldestToDate()==null?null:sdf.format(new Date(condition.getOldestToDate())));
			msg.append(", LatestFromDate=");
			msg.append(condition.getLatestFromDate()==null?null:sdf.format(new Date(condition.getLatestFromDate())));
			msg.append(", LatestToDate=");
			msg.append(condition.getLatestToDate()==null?null:sdf.format(new Date(condition.getLatestToDate())));
			m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Get, Method=getPerformanceListByCondition, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
		}

		return new CollectorControllerBean().getPerformanceList(condition);
	}

	/**
	 * 性能実績収集に必要な基本情報を取得します。
	 *
	 * PerformanceRead権限が必要
	 *
	 * @param monitorId 監視項目ID
	 * @return グラフ描画に必要な基本情報
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public PerformanceDataSettings getPerformanceGraphInfo(String monitorId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getPerformanceGraphInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.PERFORMANCE, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", MonitorID=");
		msg.append(monitorId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Get, Method=getPerformanceGraphInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new CollectorControllerBean().getPerformanceGraphInfo(monitorId);
	}

	/**
	 * 実績収集で収集されたデータを取得します。
	 *
	 * PerformanceRead権限が必要
	 *
	 * @param facilityIdList　ファシリティIDのリスト
	 * @param itemInfoList　収集項目のリスト
	 * @param startDate　取得したい始点の時刻
	 * @param endDate　　取得した終点の時刻
	 * @return　収集データのデータセット
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 */
	public CollectedDataSet getRecordCollectedDataFromIdList(
			ArrayList<String> facilityIdList,
			ArrayList<CollectorItemInfo> itemInfoList,
			Long startDate,
			Long endDate) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getRecordCollectedDataFromIdList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.PERFORMANCE, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		return new CollectorControllerBean().getRecordCollectedData(
				facilityIdList,
				itemInfoList,
				startDate==null?null:new Date(startDate),
						endDate==null?null:new Date(endDate));
	}

	/**
	 * 収集項目コードの一覧を取得します
	 *
	 * PerformanceRead権限が必要
	 *
	 * @return 収集項目IDをキーとしCollectorItemTreeItemが格納されているHashMap
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public HashMapInfo getItemCodeMap() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getItemCodeMap");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.PERFORMANCE, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		HashMapInfo info = new HashMapInfo();
		info.getMap2().putAll(new CollectorControllerBean().getItemCodeMap());
		return info;
	}

	/**
	 * 指定のファシリティで収集可能な項目のリストを返します
	 * デバイス別の収集項目があり、ノード毎に登録されているデバイス情報が異なるため、
	 * 取得可能な収集項目はファシリティ毎に異なる。
	 *
	 * PerformanceRead権限が必要
	 *
	 * @param facilityId ファシリティID
	 * @return 指定のファシリティで収集可能な項目のリスト
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	public List<CollectorItemInfo> getAvailableCollectorItemList(String facilityId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getAvailableCollectorItemList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.PERFORMANCE, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		return new CollectorControllerBean().getAvailableCollectorItemList(facilityId);
	}

	/**
	 * 指定した監視ID、ファシリティIDに対する性能実績のDLデータを作成する。
	 * 指定したファシリティIDがスコープの場合は、配下の全てのノードに対して1ファイルずつCSVファイルを作成する。
	 * 本メソッドが終了した時点で、Hinemosマネージャ上にファイルが作成され、そのファイルパスのリストを返却する
	 *
	 * PerformanceRead権限が必要
	 *
	 * @param monitorId
	 * @param facilityId
	 * @param header
	 * @param archive
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public List<String> createPerfFile(String monitorId, String facilityId, boolean header, boolean archive) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("createPerfFile()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.PERFORMANCE, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		List<String> ret = null;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", MonitorID=");
		msg.append(monitorId);
		msg.append(", FacilityID=");
		msg.append(facilityId);
		msg.append(", Header=");
		msg.append(header);
		msg.append(", Archive=");
		msg.append(archive);

		try {
			ret = new CollectorControllerBean().createPerfFile(monitorId, facilityId, header, archive);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Download Failed, Method=createPerfFile, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Download, Method=createPerfFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * 性能データのファイルをDLする
	 *
	 * PerformanceRead権限が必要
	 *
	 * @param filepath
	 * @return
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	@XmlMimeType("application/octet-stream")
	public DataHandler downloadPerfFile(String fileName) throws InvalidUserPass, InvalidRole, HinemosUnknown
	{
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.PERFORMANCE, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FileName=");
		msg.append(fileName);

		String exportDirectory = HinemosPropertyUtil.getHinemosPropertyStr(
				"performance.export.dir", "/opt/hinemos/var/export/");
		File file = new File(exportDirectory + fileName);
		if(!file.exists()) {
			m_log.info("file is not found : " + exportDirectory + fileName);
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Download Failed, Method=downloadPerfFile, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			return null;
		}
		m_log.info("file is found : " + exportDirectory + fileName);
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Download, Method=downloadPerfFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		FileDataSource source = new FileDataSource(file);
		DataHandler dataHandler = new DataHandler(source);
		return dataHandler;
	}

	/**
	 * 性能データのファイルを削除する
	 *
	 * PerformanceREAD権限が必要
	 *
	 * @param filepathList
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void deletePerfFile(ArrayList<String> fileNameList) throws HinemosUnknown, InvalidUserPass, InvalidRole
	{
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.PERFORMANCE, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if (fileNameList != null) {
			msg.append(", FileName=");
			msg.append(Arrays.toString(fileNameList.toArray()));
		}

		try {
			new CollectorControllerBean().deletePerfFile(fileNameList);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Download Failed, Method=deletePerfFile, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Download, Method=deletePerfFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
}
