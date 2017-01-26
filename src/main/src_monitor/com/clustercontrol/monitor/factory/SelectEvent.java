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

package com.clustercontrol.monitor.factory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.EventLogNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.monitor.bean.EventFilterInfo;
import com.clustercontrol.monitor.bean.ViewListInfo;
import com.clustercontrol.monitor.session.MonitorControllerBean;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.util.QueryUtil;
import com.clustercontrol.repository.bean.FacilityTargetConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.Messages;

/**
 * イベント情報を検索するクラス<BR>
 *
 * @version 3.0.0
 * @since 1.0.0
 */
public class SelectEvent {

	/**
	 * 表示イベント数（デフォルト値）。<BR>
	 * 監視[イベント]ビューに表示するイベント表示数を格納します。
	 */
	private final static int MAX_DISPLAY_NUMBER = 500;

	/**
	 * イベント情報を取得します。<BR>
	 *
	 * @param monitorId
	 * @param pluginId
	 * @param facilityId
	 * @param outputDate
	 * @return イベント情報
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 *
	 */
	public static EventDataInfo getEventInfo(
			String monitorId,
			String monitorDetailId,
			String pluginId,
			String facilityId,
			Long outputDate) throws MonitorNotFound, InvalidRole {

		EventDataInfo info = null;

		// イベントログ情報を取得
		EventLogEntity event = null;
		try {
			event = QueryUtil.getEventLogPK(monitorId,
					monitorDetailId,
					pluginId,
					new Timestamp(outputDate),
					facilityId);
		} catch (EventLogNotFound e) {
			throw new MonitorNotFound(e.getMessage(), e);
		} catch (InvalidRole e) {
			throw e;
		}
		info = new EventDataInfo();
		info.setPriority(event.getPriority());
		if(event.getId().getOutputDate() != null){
			info.setOutputDate(event.getId().getOutputDate().getTime());
		}
		if(event.getGenerationDate() != null){
			info.setGenerationDate(event.getGenerationDate().getTime());
		}
		info.setPluginId(event.getId().getPluginId());
		info.setMonitorId(event.getId().getMonitorId());
		info.setMonitorDetailId(event.getId().getMonitorDetailId());
		info.setFacilityId(event.getId().getFacilityId());
		info.setScopeText(event.getScopeText());
		info.setApplication(event.getApplication());
		info.setMessageId(event.getMessageId());
		info.setMessage(event.getMessage());
		info.setMessageOrg(event.getMessageOrg());
		info.setConfirmed(event.getConfirmFlg());
		if(event.getConfirmDate() != null){
			info.setConfirmDate(event.getConfirmDate().getTime());
		}
		info.setConfirmUser(event.getConfirmUser());
		info.setDuplicationCount(event.getDuplicationCount().intValue());
		info.setComment(event.getComment());
		if(event.getCommentDate() != null) {
			info.setCommentDate(event.getCommentDate().getTime());
		}
		info.setCommentUser(event.getCommentUser());
		info.setOwnerRoleId(event.getOwnerRoleId());
		return info;
	}

	/**
	 * 引数で指定された条件に一致するイベント一覧情報を返します。<BR>
	 * 表示イベント数を越えた場合は、表示イベント数分のイベント情報一覧を返します。
	 * 各イベント情報は、EventLogDataインスタンスとして保持されます。<BR>
	 * 戻り値のViewListInfoは、クライアントにて表示用の形式に変換されます。
	 *
	 * @param facilityId 取得対象の親ファシリティID
	 * @param property 検索条件
	 * @param messages 表示イベント数
	 * @return ビュー一覧情報
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.monitor.bean.EventDataInfo
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#getFacilityIdList(String, int)
	 * @see com.clustercontrol.monitor.ejb.entity.EventLogBean#ejbFindEvent(String[], Integer, Timestamp, Timestamp, Timestamp, Timestamp, String, String, Integer, boolean, Integer)
	 * @see com.clustercontrol.monitor.ejb.entity.EventLogBean#ejbHomeCountEvent(String[], Integer, Timestamp, Timestamp, Timestamp, Timestamp, String, String, Integer)
	 * @see com.clustercontrol.monitor.bean.EventTabelDefine
	 */
	public ViewListInfo getEventList(String facilityId, EventFilterInfo filter, int messages)
			throws HinemosUnknown {

		ViewListInfo ret = null;

		Integer[] priorityList = null;
		Timestamp outputFromDate = null;
		Timestamp outputToDate = null;
		Timestamp generationFromDate = null;
		Timestamp generationToDate = null;
		String monitorId = null;
		String monitorDetailId = null;
		String facilityType = null;
		String application = null;
		String message = null;
		Integer confirmFlg = new Integer(ConfirmConstant.TYPE_UNCONFIRMED);
		String confirmUser = null;
		String comment = null;
		String commentUser = null;
		String ownerRoleId = null;

		String[] facilityIds = null;

		Collection<EventLogEntity> ct = null;

		Integer limit = new Integer(0);

		if(filter != null){
			//重要度取得
			if (filter.getPriorityList() != null && filter.getPriorityList().length>0) {
				priorityList = filter.getPriorityList();
			}

			//更新日時（自）取得
			if (filter.getOutputDateFrom() instanceof Long) {
				outputFromDate = new Timestamp(filter.getOutputDateFrom());
				outputFromDate.setNanos(0);
			}

			//更新日時（至）取得
			if (filter.getOutputDateTo() instanceof Long) {
				outputToDate = new Timestamp(filter.getOutputDateTo());
				outputToDate.setNanos(999999999);
			}

			//出力日時（自）取得
			if (filter.getGenerationDateFrom() instanceof Long) {
				generationFromDate = new Timestamp(filter.getGenerationDateFrom());
				generationFromDate.setNanos(0);
			}

			//出力日時（至）取得
			if (filter.getGenerationDateTo() instanceof Long) {
				generationToDate = new Timestamp(filter.getGenerationDateTo());
				generationToDate.setNanos(999999999);
			}

			//監視項目ID取得
			if (!"".equals(filter.getMonitorId())) {
				monitorId = filter.getMonitorId();
			}

			//監視詳細取得
			if (!"".equals(filter.getMonitorDetailId())) {
				monitorDetailId = filter.getMonitorDetailId();
			}

			//対象ファシリティ種別取得
			if (!"".equals(filter.getFacilityType())) {
				facilityType = filter.getFacilityType();
			}

			//アプリケーション取得
			if (!"".equals(filter.getApplication())) {
				application = filter.getApplication();
			}

			//メッセージ取得
			if (!"".equals(filter.getMessage())) {
				message = filter.getMessage();
			}

			// 確認有無取得
			confirmFlg = filter.getConfirmFlgType();
			if (confirmFlg != null && confirmFlg == ConfirmConstant.TYPE_ALL){
				confirmFlg = null;
			}

			// 確認ユーザ取得
			if (!"".equals(filter.getConfirmedUser())) {
				confirmUser = filter.getConfirmedUser();
			}

			//コメント取得
			if (!"".equals(filter.getComment())){
				comment = filter.getComment();
			}

			//コメントユーザ取得
			if (!"".equals(filter.getCommentUser())){
				commentUser = filter.getCommentUser();
			}

			//オーナーロールID取得
			if (!"".equals(filter.getOwnerRoleId())){
				ownerRoleId = filter.getOwnerRoleId();
			}
		}

		// 対象ファシリティのファシリティIDを取得
		int level = RepositoryControllerBean.ALL;
		if (FacilityTargetConstant.STRING_BENEATH.equals(facilityType)) {
			level = RepositoryControllerBean.ONE_LEVEL;
		}

		ArrayList<String> facilityIdList
		= new RepositoryControllerBean().getFacilityIdList(facilityId, level);

		if (facilityIdList != null && facilityIdList.size() > 0) {
			// スコープの場合
			facilityIds = new String[facilityIdList.size()];
			facilityIdList.toArray(facilityIds);
		}
		else {
			// ノードの場合
			facilityIds = new String[1];
			facilityIds[0] = facilityId;
		}

		if(messages <= 0){
			messages = MAX_DISPLAY_NUMBER;
		}
		limit = new Integer(messages + 1);

		// イベントログ情報一覧を取得
		ct = QueryUtil.getEventLogByFilter(
				facilityIds,
				priorityList,
				outputFromDate,
				outputToDate,
				generationFromDate,
				generationToDate,
				monitorId,
				monitorDetailId,
				application,
				message,
				confirmFlg,
				confirmUser,
				comment,
				commentUser,
				ownerRoleId,
				false,
				limit);
		// 2次元配列に変換
		ret = this.collectionToEventList(ct, messages);

		return ret;
	}

	/**
	 * 重要度が最高で受信日時が最新のイベント情報を返します。
	 * <p>
	 * <ol>
	 * <li>引数で指定されたファシリティ配下のファシリティを、指定されたファシリティのターゲットで取得します。</li>
	 * <li>取得したファシリティに属する重要度が最高 かつ 受信日時が最新の未確認のイベント情報を取得し返します。</li>
	 * </ol>
	 *
	 * @param facilityId 取得対象の親ファシリティID
	 * @param level 取得対象のファシリティのターゲット（配下全て／直下のみ）
	 * @param orderFlg ソートの有無
	 * @return イベントのローカルコンポーネントインターフェース
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#ALL
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#ONE_LEVEL
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#getFacilityIdList(String, int)
	 * @see com.clustercontrol.monitor.ejb.entity.EventLogBean#ejbFindHighPriorityEvent(String[], Integer, Timestamp, Timestamp, Timestamp, Timestamp, String, String, Integer)
	 */
	protected EventLogEntity getHighPriorityEvent(String facilityId, int level, boolean orderFlg)
			throws HinemosUnknown {

		EventLogEntity event = null;

		String[] facilityIds = null;
		if (level == MonitorControllerBean.ONLY) {
			if (facilityId != null && !"".equals(facilityId)) {
				facilityIds = new String[1];
				facilityIds[0] = facilityId;
			} else {
				return null;
			}
		} else {
			// 直下 または 配下すべてのファシリティIDを取得
			ArrayList<String> facilityIdList
			= new RepositoryControllerBean().getFacilityIdList(facilityId,
					level);

			if (facilityIdList != null && facilityIdList.size() > 0) {
				// スコープの場合
				if(facilityId != null){
					facilityIdList.add(facilityId);
				}
				facilityIds = new String[facilityIdList.size()];
				facilityIdList.toArray(facilityIds);
			} else {
				if(facilityId != null){
					// ノードの場合
					facilityIds = new String[1];
					facilityIds[0] = facilityId;
				}
				else{
					// リポジトリが1件も登録されていない場合
					return null;
				}
			}
		}

		// 重要度のリストを取得する
		int[] priorityList = PriorityConstant.PRIORITY_LIST;
		for(int i=0; i<priorityList.length; i++){
			// イベントログ情報一覧から重要度が危険のもので、最近出力されたイベントを取得する。
			List<EventLogEntity> ct = QueryUtil.getEventLogByHighPriorityFilter(
					facilityIds,
					priorityList[i],
					null,
					null,
					null,
					null,
					null,
					null,
					new Integer(ConfirmConstant.TYPE_UNCONFIRMED),
					null,
					orderFlg);

			// 重要度の高いもの順にループされるため、取得できた場合は、それを返す。
			Iterator<EventLogEntity> itr = ct.iterator();
			// イテレータで参照するが、0件か１件しかない。
			if (itr.hasNext()) {
				event = itr.next();
				return event;
			}
		}

		return event;
	}

	public void deleteEventFile(String filename) {
		String exportDirectory = HinemosPropertyUtil.getHinemosPropertyStr(
				"performance.export.dir", "/opt/hinemos/var/export/");
		File file = new File(exportDirectory + "/" + filename);
		file.delete();
	}

	/**
	 * 引数で指定された条件に一致する帳票出力用のイベント情報一覧を返します。
	 * <p>
	 * <ol>
	 * <li>引数で指定されたプロパティに格納された検索条件を、プロパティユーティリティ（{@link com.clustercontrol.util.PropertyUtil}）を使用して取得します。</li>
	 * <li>引数で指定されたファシリティ配下のファシリティと検索条件を基に、イベント情報を取得します。</li>
	 * <li>１イベント情報を帳票出力用イベント情報（{@link com.clustercontrol.monitor.bean.ReportEventInfo}）にセットします。</li>
	 * <li>この帳票出力用イベント情報を、イベント情報一覧を保持するリスト（{@link ArrayList}）にセットし返します。<BR>
	 * </ol>
	 *
	 * @param facilityId 取得対象の親ファシリティID
	 * @param property 検索条件
	 * @return 帳票出力用イベント情報一覧（{@link com.clustercontrol.monitor.bean.ReportEventInfo}のリスト）
	 * @throws HinemosUnknown
	 * @throws IOException
	 *
	 * @since 2.1.0
	 *
	 * @see com.clustercontrol.util.PropertyUtil#getPropertyValue(com.clustercontrol.bean.Property, java.lang.String)
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#getFacilityIdList(String, int)
	 * @see com.clustercontrol.monitor.ejb.entity.EventLogBean#ejbFindEvent(String[], Integer, Timestamp, Timestamp, Timestamp, Timestamp, String, String, Integer, boolean, Integer)
	 * @see com.clustercontrol.monitor.bean.ReportEventInfo
	 */
	public DataHandler getEventFile(String facilityId, EventFilterInfo filter, String filename, String username)
			throws HinemosUnknown, IOException {

		Integer[] priorityList = null;
		Timestamp outputFromDate = null;
		Timestamp outputToDate = null;
		Timestamp generationFromDate = null;
		Timestamp generationToDate = null;
		String monitorId = null;
		String monitorDetailId = null;
		String facilityType = null;
		String application = null;
		String message = null;
		Integer confirmFlg = null;
		String confirmUser = null;
		String comment = null;
		String commentUser = null;
		String ownerRoleId = null;

		String exportDirectory = HinemosPropertyUtil.getHinemosPropertyStr(
				"performance.export.dir", "/opt/hinemos/var/export/");
		String filepath = exportDirectory + "/" + filename;
		File file = new File(filepath);
		boolean UTF8_BOM = HinemosPropertyUtil.getHinemosPropertyBool("monitor.common.report.event.bom", true);
		if (UTF8_BOM) {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write( 0xef );
			fos.write( 0xbb );
			fos.write( 0xbf );
			fos.close();
		}
		FileWriter filewriter = new FileWriter(file, true);

		try {
			//重要度取得
			if (filter.getPriorityList() != null && filter.getPriorityList().length>0) {
				priorityList = filter.getPriorityList();
			}

			//更新日時（自）取得
			if (filter.getOutputDateFrom() instanceof Long) {
				outputFromDate = new Timestamp(filter.getOutputDateFrom());
				outputFromDate.setNanos(0);
			}

			//更新日時（至）取得
			if (filter.getOutputDateTo() instanceof Long) {
				outputToDate = new Timestamp(filter.getOutputDateTo());
				outputToDate.setNanos(999999999);
			}

			//出力日時（自）取得
			if (filter.getGenerationDateFrom() instanceof Long) {
				generationFromDate = new Timestamp(filter.getGenerationDateFrom());
				generationFromDate.setNanos(0);
			}

			//出力日時（至）取得
			if (filter.getGenerationDateTo() instanceof Long) {
				generationToDate = new Timestamp(filter.getGenerationDateTo());
				generationToDate.setNanos(999999999);
			}

			//監視項目ID取得
			if (!"".equals(filter.getMonitorId())) {
				monitorId = filter.getMonitorId();
			}

			//監視詳細取得
			if (!"".equals(filter.getMonitorDetailId())) {
				monitorDetailId = filter.getMonitorDetailId();
			}

			//対象ファシリティ種別取得
			if (!"".equals(filter.getFacilityType())) {
				facilityType = filter.getFacilityType();
			}

			//アプリケーション取得
			if (!"".equals(filter.getApplication())) {
				application = filter.getApplication();
			}

			//メッセージ取得
			if (!"".equals(filter.getMessage())) {
				message = filter.getMessage();
			}

			// 確認有無取得
			int confirmFlgType = filter.getConfirmFlgType();
			if (confirmFlgType != -1) {
				confirmFlg = new Integer(confirmFlgType);
			}

			// 確認ユーザ
			if (!"".equals(filter.getConfirmedUser())) {
				confirmUser = filter.getConfirmedUser();
			}

			// コメント
			if (!"".equals(filter.getComment())){
				comment = filter.getComment();
			}

			// コメントユーザ
			if (!"".equals(filter.getCommentUser())){
				commentUser = filter.getCommentUser();
			}

			// オーナーロールID
			if (!"".equals(filter.getOwnerRoleId())){
				ownerRoleId = filter.getOwnerRoleId();
			}

			// ヘッダを追記
			/*
			イベント情報,,,,,,,,,,,,,
			出力日時,2012/02/08 17:20:10,,,,,,,,,,,,
			出力ユーザ,Hinemos Administrator,,,,,,,,,,,,
			重要度,受信日時,出力日時,ファシリティID,アプリケーション,オーナーロールID,確認,確認ユーザ,メッセージ,,,,,,
			,,,,,未,,,,,,,,
			 */
			String SEPARATOR = HinemosPropertyUtil.getHinemosPropertyStr("monitor.common.report.event.separator", ",");
			String DATE_FORMAT = HinemosPropertyUtil.getHinemosPropertyStr("monitor.common.report.event.format",  "yyyy/MM/dd HH:mm:ss");

			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			filewriter.write(Messages.getString("report.title.monitor.event") + "\n");
			filewriter.write(Messages.getString("report.output.date") + SEPARATOR +
					sdf.format(new Date()) + "\n");
			filewriter.write(Messages.getString("report.output.user") + SEPARATOR + username + "\n");
			filewriter.write(
					Messages.getString("def.result") + SEPARATOR +
					Messages.getString("receive.time") + SEPARATOR +
					Messages.getString("report.output.date") + SEPARATOR +
					Messages.getString("facility.id") + SEPARATOR +
					Messages.getString("application") + SEPARATOR +
					Messages.getString("confirmed") + SEPARATOR +
					Messages.getString("confirm.user") + SEPARATOR +
					Messages.getString("message") + SEPARATOR +
					Messages.getString("comment") + SEPARATOR +
					Messages.getString("comment.user") +SEPARATOR +
					Messages.getString("owner.role.id") +
					"\n");
			
			// 重要度リストの文字列化
			String priorityMsg = "";
			if(priorityList != null) {
				for(int i = 0; i<priorityList.length; i++) {
					priorityMsg = priorityMsg + PriorityConstant.typeToString(priorityList[i]) + " ";
				}
			}
			filewriter.write(
					(priorityList == null ? "" : priorityMsg) + SEPARATOR +
					(outputFromDate == null ? "" : sdf.format(new Date(outputFromDate.getTime()))) + " - " +
					(outputToDate == null ? "" : sdf.format(new Date(outputToDate.getTime()))) + SEPARATOR +
					(generationFromDate == null ? "" : sdf.format(new Date(generationFromDate.getTime()))) + " - " +
					(generationToDate == null ? "" : sdf.format(new Date(generationToDate.getTime()))) + SEPARATOR +
					(facilityType == null ? "" : facilityType) + SEPARATOR +
					(application == null ? "" : application) + SEPARATOR +
					(confirmFlg == null ? "" : ConfirmConstant.typeToString(confirmFlg)) + SEPARATOR +
					(confirmUser == null ? "" : confirmUser) + SEPARATOR +
					(message == null ? "" : message) + SEPARATOR +
					(comment == null ? "" : comment) + SEPARATOR +
					(commentUser == null ? "" : commentUser) + SEPARATOR +
					(ownerRoleId == null ? "" : ownerRoleId) +
					"\n");
			filewriter.write(
					Messages.getString("number") + SEPARATOR +
					Messages.getString("def.result") + SEPARATOR +
					Messages.getString("receive.time") + SEPARATOR +
					Messages.getString("report.output.date") + SEPARATOR +
					Messages.getString("facility.id") + SEPARATOR +
					Messages.getString("scope") + SEPARATOR +
					Messages.getString("monitor.id") + SEPARATOR +
					Messages.getString("monitor.detail.id") + SEPARATOR +
					Messages.getString("message.id") + SEPARATOR +
					Messages.getString("plugin.id") + SEPARATOR +
					Messages.getString("application") + SEPARATOR +
					Messages.getString("owner.role.id") + SEPARATOR +
					Messages.getString("confirmed") + SEPARATOR +
					Messages.getString("confirm.time") + SEPARATOR +
					Messages.getString("confirm.user") + SEPARATOR +
					Messages.getString("comment") + SEPARATOR +
					Messages.getString("comment.date") + SEPARATOR +
					Messages.getString("comment.user") + SEPARATOR +
					Messages.getString("message") + SEPARATOR +
					Messages.getString("message.org") + "\n");

			// 対象ファシリティのファシリティIDを取得
			String[] facilityIds = null;

			int level = RepositoryControllerBean.ALL;
			if (FacilityTargetConstant.STRING_BENEATH.equals(facilityType)) {
				level = RepositoryControllerBean.ONE_LEVEL;
			}

			ArrayList<String> facilityIdList
			= new RepositoryControllerBean().getFacilityIdList(facilityId, level);

			if (facilityIdList != null && facilityIdList.size() > 0) {
				// スコープの場合
				facilityIds = new String[facilityIdList.size()];
				facilityIdList.toArray(facilityIds);
			}
			else {
				// ノードの場合
				facilityIds = new String[1];
				facilityIds[0] = facilityId;
			}

			// イベントログ情報一覧を取得
			List<EventLogEntity> ct = QueryUtil.getEventLogByFilter(
					facilityIds,
					priorityList,
					outputFromDate,
					outputToDate,
					generationFromDate,
					generationToDate,
					monitorId,
					monitorDetailId,
					application,
					message,
					confirmFlg,
					confirmUser,
					comment,
					commentUser,
					ownerRoleId,
					true,
					HinemosPropertyUtil.getHinemosPropertyNum("monitor.common.report.event.count", 2000));
			// 帳票出力用に変換
			collectionToFile(ct, filewriter);

		} finally {
			filewriter.close();
		}

		// リストをファイルに書き出し。
		FileDataSource source = new FileDataSource(file);
		DataHandler handler = new DataHandler(source);

		return handler;
	}

	/**
	 * DBより取得したイベント情報をイベント一覧情報に格納します。
	 * <p>
	 * <ol>
	 * <li>１イベント情報をEventLogDataのインスタンスとし、イベント情報一覧を保持するリスト（{@link ArrayList}）に格納します。<BR>
	 * <li>イベント情報一覧を、引数で指定されたビュー一覧情報（{@link com.clustercontrol.monitor.bean.ViewListInfo}）にセットします。</li>
	 * </ol>
	 * <p>
	 * また、イベント数を重要度毎にカウントし、
	 * 表示イベント数よりもイベント数が少ない場合は、重要度別イベント数を引数で指定されたビュー一覧情報にセットします。
	 *
	 * @param ct イベント情報取得結果
	 * @param eventList ビュー一覧情報
	 * @param messages イベント最大表示件数
	 *
	 * @see com.clustercontrol.monitor.ejb.entity.EventLogData
	 * @see com.clustercontrol.monitor.bean.EventTabelDefine
	 */
	private ViewListInfo collectionToEventList(Collection<EventLogEntity> ct, int messages) {
		int critical = 0;
		int warning = 0;
		int info = 0;
		int unknown = 0;

		ViewListInfo viewListInfo = new ViewListInfo();
		ArrayList<EventDataInfo> list = new ArrayList<EventDataInfo>();

		for (EventLogEntity event : ct) {

			EventDataInfo eventInfo = new EventDataInfo();
			eventInfo.setPriority(event.getPriority());
			if (event.getId().getOutputDate() != null) {
				eventInfo.setOutputDate(event.getId().getOutputDate().getTime());
			}
			if (event.getGenerationDate() != null) {
				eventInfo.setGenerationDate(event.getGenerationDate().getTime());
			}
			eventInfo.setPluginId(event.getId().getPluginId());
			eventInfo.setMonitorId(event.getId().getMonitorId());
			eventInfo.setMonitorDetailId(event.getId().getMonitorDetailId());
			eventInfo.setFacilityId(event.getId().getFacilityId());
			eventInfo.setScopeText(event.getScopeText());
			eventInfo.setApplication(event.getApplication());
			eventInfo.setMessageId(event.getMessageId());
			eventInfo.setMessage(event.getMessage());
			eventInfo.setConfirmed(event.getConfirmFlg());
			eventInfo.setConfirmUser(event.getConfirmUser());
			eventInfo.setComment(event.getComment());
			if (event.getCommentDate() != null ) {
				eventInfo.setCommentDate(event.getCommentDate().getTime());
			}
			eventInfo.setCommentUser(event.getCommentUser());
			eventInfo.setOwnerRoleId(event.getOwnerRoleId());

			list.add(eventInfo);

			//最大表示件数以下の場合
			if(event.getPriority().intValue() == PriorityConstant.TYPE_CRITICAL)
				critical++;
			else if(event.getPriority().intValue() == PriorityConstant.TYPE_WARNING)
				warning++;
			else if(event.getPriority().intValue() == PriorityConstant.TYPE_INFO)
				info++;
			else if(event.getPriority().intValue() == PriorityConstant.TYPE_UNKNOWN)
				unknown++;

			//取得したイベントを最大表示件数まで格納したら終了
			if(list.size() >= messages)
				break;
		}

		//イベント数を設定
		viewListInfo.setCritical(critical);
		viewListInfo.setWarning(warning);
		viewListInfo.setInfo(info);
		viewListInfo.setUnKnown(unknown);
		viewListInfo.setTotal(ct.size());

		viewListInfo.setEventList(list);

		return viewListInfo;
	}

	/**
	 * DBより取得したイベント情報を帳票出力用イベント情報一覧に格納します。
	 *
	 * @param ct イベント情報取得結果
	 * @return 帳票出力用イベント情報一覧
	 *
	 * @version 2.1.0
	 * @throws IOException
	 * @since 2.1.0
	 */
	private void collectionToFile(Collection<EventLogEntity> ct, FileWriter filewriter) throws IOException {

		int n = 0;
		String SEPARATOR = HinemosPropertyUtil.getHinemosPropertyStr("monitor.common.report.event.separator", ",");
		for (EventLogEntity event : ct) {
			n ++;
			filewriter.write(
					getDoubleQuote(String.valueOf(n)) + SEPARATOR +
					getDoubleQuote(PriorityConstant.typeToString(event.getPriority())) + SEPARATOR +
					getDoubleQuote(d2s(event.getId().getOutputDate())) + SEPARATOR +
					getDoubleQuote(t2s(event.getGenerationDate())) + SEPARATOR +
					getDoubleQuote(event.getId().getFacilityId()) + SEPARATOR +
					getDoubleQuote(event.getScopeText()) + SEPARATOR +
					getDoubleQuote(event.getId().getMonitorId()) + SEPARATOR +
					getDoubleQuote(event.getId().getMonitorDetailId()) + SEPARATOR +
					getDoubleQuote(event.getMessageId()) + SEPARATOR +
					getDoubleQuote(event.getId().getPluginId()) + SEPARATOR +
					getDoubleQuote(event.getApplication()) + SEPARATOR +
					getDoubleQuote(event.getOwnerRoleId()) + SEPARATOR +
					getDoubleQuote(ConfirmConstant.typeToString(event.getConfirmFlg())) + SEPARATOR +
					getDoubleQuote(t2s(event.getConfirmDate())) + SEPARATOR +
					getDoubleQuote(event.getConfirmUser()) + SEPARATOR +
					getDoubleQuote(event.getComment()) + SEPARATOR +
					getDoubleQuote(t2s(event.getCommentDate())) + SEPARATOR +
					getDoubleQuote(event.getCommentUser()) + SEPARATOR +
					getDoubleQuote(event.getMessage()) + SEPARATOR +
					getDoubleQuote(event.getMessageOrg()) +
					"\n");
		}
	}

	/**
	 * Dateを整形する。
	 */
	private String d2s(Date d) {
		if (d == null) {
			return "";
		}
		String DATE_FORMAT = HinemosPropertyUtil.getHinemosPropertyStr("monitor.common.report.event.format",  "yyyy/MM/dd HH:mm:ss");
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		return sdf.format(d);
	}

	/**
	 * Timestampを整形する。
	 * @param t
	 * @return
	 */
	private String t2s(Timestamp t) {
		if (t == null) {
			return "";
		}
		String DATE_FORMAT = HinemosPropertyUtil.getHinemosPropertyStr("monitor.common.report.event.format",  "yyyy/MM/dd HH:mm:ss");
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		return sdf.format(new Date(t.getTime()));
	}

	/**
	 * メッセージやオリジナルメッセージに改行が含まれている場合や、
	 * 「"」が含まれている場合はMS Excelで読もうとするとおかしくなる。
	 * 改行等が含まれる可能性のある箇所は下記を利用すること。
	 */
	private String getDoubleQuote(String in) {
		if (in == null) {
			return "";
		}
		return "\"" + in.replace("\"", "\"\"") + "\"";
	}
}
