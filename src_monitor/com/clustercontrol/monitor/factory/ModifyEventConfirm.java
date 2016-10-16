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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.fault.EventLogNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.monitor.bean.EventBatchConfirmInfo;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.util.QueryUtil;
import com.clustercontrol.repository.bean.FacilityTargetConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;


/**
 * イベント情報の確認を更新するクラス<BR>
 *
 * @version 3.0.0
 * @since 1.0.0
 */
public class ModifyEventConfirm {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(ModifyEventConfirm.class);

	/**
	 * 引数で指定されたイベント情報一覧の確認を更新します。<BR>
	 * 確認ユーザとして、操作を実施したユーザを設定します。<BR>
	 * 取得したイベント情報の確認フラグを更新します。確認フラグが済の場合は、確認済み日時も更新します。
	 * 
	 * @param list 更新対象のイベント情報一覧（EventLogDataが格納されたArrayList）
	 * @param confirmType 確認フラグ（未／済）（更新値）
	 * @param confirmUser 確認ユーザ
	 * 
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.monitor.bean.EventTabelDefine
	 * @see com.clustercontrol.bean.ConfirmConstant
	 * @see #modifyConfirm(String, String, String, Date, Date, int, String)
	 */
	public void modifyConfirm(ArrayList<EventDataInfo> list, int confirmType, String confirmUser
			) throws MonitorNotFound, InvalidRole  {

		if (list != null && list.size()>0) {

			// 確認済み日時
			Long confirmDate = null;
			if(confirmType == ConfirmConstant.TYPE_CONFIRMED){
				confirmDate = new Date().getTime();
			}

			for(EventDataInfo event : list) {

				if (event != null) {

					this.modifyConfirm(
							event.getMonitorId(),
							event.getMonitorDetailId(),
							event.getPluginId(),
							event.getFacilityId(),
							event.getPriority(),
							event.getGenerationDate(),
							event.getOutputDate(),
							confirmDate,
							confirmType,
							confirmUser);

				}
			}
		}
	}

	/**
	 * 引数で指定されたイベント情報の確認を更新します。<BR>
	 * 確認ユーザとして、操作を実施したユーザを設定します。<BR>
	 * 取得したイベント情報の確認フラグを更新します。確認フラグが済の場合は、確認済み日時も更新します。
	 * 
	 * @param monitorId 更新対象の監視項目ID
	 * @param pluginId 更新対象のプラグインID
	 * @param facilityId 更新対象のファシリティID
	 * @param priority 更新対象の重要度
	 * @param generateDate 更新対象の出力日時
	 * @param outputDate 更新対象の受信日時
	 * @param confirmDate 更新対象の確認済み日時
	 * @param confirmType 確認フラグ（未／済）（更新値）
	 * @param confirmUser 確認ユーザ
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.bean.ConfirmConstant
	 * @see com.clustercontrol.monitor.ejb.entity.EventLogBean#ejbFindByPrimaryKey(EventLogPK)
	 */
	public void modifyConfirm(
			String monitorId,
			String monitorDetailId,
			String pluginId,
			String facilityId,
			int priority,
			Long generateDate,
			Long outputDate,
			Long confirmDate,
			int confirmType,
			String confirmUser
			) throws  MonitorNotFound, InvalidRole  {

		// イベントログ情報を取得
		EventLogEntity event = null;
		try {
			event = QueryUtil.getEventLogPK(monitorId, monitorDetailId, pluginId, new Timestamp(outputDate), facilityId, ObjectPrivilegeMode.MODIFY);
		} catch (EventLogNotFound e) {
			throw new MonitorNotFound(e.getMessage(), e);
		} catch (InvalidRole e) {
			throw e;
		}

		// 確認有無を変更
		event.setConfirmFlg(new Integer(confirmType));

		if(confirmType == ConfirmConstant.TYPE_CONFIRMED){
			if(confirmDate == null){
				confirmDate = new Date().getTime();
			}
			event.setConfirmDate(new Timestamp(confirmDate));
		}

		// 確認を実施したユーザを設定
		event.setConfirmUser(confirmUser);
	}

	/**
	 * 引数で指定された条件に一致するイベント情報の確認を一括更新します。
	 * <p>
	 * <ol>
	 * <li>引数で指定されたプロパティに格納された更新条件を、プロパティユーティリティ（{@link com.clustercontrol.util.PropertyUtil}）を使用して取得します。</li>
	 * <li>引数で指定されたファシリティ配下のファシリティと更新条件を基に、イベント情報を取得します。</li>
	 * <li>確認ユーザとして、操作を実施したユーザを設定します。</li>
	 * <li>取得したイベント情報の確認フラグを更新します。確認フラグが済の場合は、確認済み日時も更新します。</li>
	 * <li>イベント情報Entity Beanのキャッシュをフラッシュします。</li>
	 * 
	 * @param confirmType 確認フラグ（未／済）（更新値）
	 * @param facilityId 更新対象の親ファシリティID
	 * @param property 更新条件
	 * @param confirmUser 確認ユーザ
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.bean.ConfirmConstant
	 * @see com.clustercontrol.util.PropertyUtil#getPropertyValue(com.clustercontrol.bean.Property, java.lang.String)
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#getFacilityIdList(String, int)
	 * @see com.clustercontrol.monitor.ejb.entity.EventLogBean#ejbHomeBatchConfirm(String[], Integer, Timestamp, Timestamp, Timestamp, Timestamp, String, String, Integer, Integer)
	 */
	public void modifyBatchConfirm(int confirmType, String facilityId, EventBatchConfirmInfo info, String confirmUser) throws HinemosUnknown {

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
		String comment = null;
		String commentUser = null;

		//重要度取得
		if(info.getPriorityList() != null && info.getPriorityList().length>0){
			priorityList = info.getPriorityList();
		}

		//更新日時（自）取得
		if(info.getOutputFromDate() instanceof Long){
			outputFromDate = new Timestamp(info.getOutputFromDate());
			outputFromDate.setNanos(0);
		}

		//更新日時（至）取得
		if(info.getOutputToDate() instanceof Long){
			outputToDate = new Timestamp(info.getOutputToDate());
			outputToDate.setNanos(999999999);
		}

		//出力日時（自）取得
		if(info.getGenerationFromDate() instanceof Long){
			generationFromDate = new Timestamp(info.getGenerationFromDate());
			generationFromDate.setNanos(0);
		}

		//出力日時（至）取得
		if(info.getGenerationToDate() instanceof Long){
			generationToDate = new Timestamp(info.getGenerationToDate());
			generationToDate.setNanos(999999999);
		}

		//監視項目ID取得
		if(!"".equals(info.getMonitorId())){
			monitorId = info.getMonitorId();
		}

		//監視詳細取得
		if(!"".equals(info.getMonitorDetailId())){
			monitorDetailId = info.getMonitorDetailId();
		}

		//対象ファシリティ種別取得
		if(!"".equals(info.getFacilityType())){
			facilityType = info.getFacilityType();
		}

		//アプリケーション取得
		if(!"".equals(info.getApplication())){
			application = info.getApplication();
		}

		//メッセージ取得
		if(!"".equals(info.getMessage())){
			message = info.getMessage();
		}
		//コメント
		if(!"".equals(info.getComment())){
			comment = info.getComment();
		}
		//コメントユーザ
		if(!"".equals(info.getCommentUser())){
			commentUser = info.getCommentUser();
		}

		// 対象ファシリティのファシリティIDを取得
		// ファシリティが指定されない（最上位）場合は、ファシリティIDを指定せずに検索を行う
		String[] facilityIds = null;
		if(facilityId != null && !"".equals(facilityId)){

			int level = RepositoryControllerBean.ALL;
			if(FacilityTargetConstant.STRING_BENEATH.equals(facilityType)){
				level = RepositoryControllerBean.ONE_LEVEL;
			}

			ArrayList<String> facilityIdList = new RepositoryControllerBean().getFacilityIdList(facilityId, level);

			// スコープの場合
			if(facilityIdList != null && facilityIdList.size() > 0){
				facilityIds = new String[facilityIdList.size()];
				facilityIdList.toArray(facilityIds);
			}
			// ノードの場合
			else{
				facilityIds = new String[1];
				facilityIds[0] = facilityId;
			}
		}


		// アップデートする設定フラグ
		Integer confirmFlg = new Integer(confirmType);
		int rtn = QueryUtil.updateEventLogFlgByFilter(facilityIds,
				priorityList,
				outputFromDate,
				outputToDate,
				generationFromDate,
				generationToDate,
				monitorId,
				monitorDetailId,
				application,
				message,
				comment,
				commentUser,
				confirmFlg,
				confirmType,
				confirmUser);
		m_log.debug("The result of updateEventLogFlgByFilter is: " + rtn);
	}

}

