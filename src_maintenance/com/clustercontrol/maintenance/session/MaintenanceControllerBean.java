/*

Copyright (C) 2007 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.maintenance.session;

import java.util.ArrayList;
import java.util.Date;
import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MaintenanceDuplicate;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.maintenance.bean.MaintenanceInfo;
import com.clustercontrol.maintenance.bean.MaintenanceTypeMst;
import com.clustercontrol.maintenance.factory.AddMaintenance;
import com.clustercontrol.maintenance.factory.DeleteMaintenance;
import com.clustercontrol.maintenance.factory.MaintenanceEvent;
import com.clustercontrol.maintenance.factory.MaintenanceJob;
import com.clustercontrol.maintenance.factory.MaintenancePerf;
import com.clustercontrol.maintenance.factory.ModifyMaintenance;
import com.clustercontrol.maintenance.factory.ModifySchedule;
import com.clustercontrol.maintenance.factory.OperationMaintenance;
import com.clustercontrol.maintenance.factory.SelectMaintenanceInfo;
import com.clustercontrol.maintenance.factory.SelectMaintenanceTypeMst;
import com.clustercontrol.maintenance.util.MaintenanceValidator;
import com.clustercontrol.notify.util.NotifyRelationCache;

/**
 * 
 * メンテナンス機能を管理する Session Bean です。<BR>
 * 
 */
public class MaintenanceControllerBean {

	private static Log m_log = LogFactory.getLog( MaintenanceControllerBean.class );

	/**
	 * メンテナンス情報を追加します。
	 * 
	 * @throws HinemosUnknown
	 * @throws MaintenanceDuplicate
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public void addMaintenance(MaintenanceInfo maintenanceInfo)
			throws HinemosUnknown, MaintenanceDuplicate, InvalidSetting, InvalidRole {
		m_log.debug("addMaintenance");

		JpaTransactionManager jtm = null;
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		// メイン処理
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 入力チェック
			MaintenanceValidator.validateMaintenanceInfo(maintenanceInfo);

			// メンテナンス情報を登録
			AddMaintenance add = new AddMaintenance();
			add.addMaintenance(maintenanceInfo, loginUser);

			// Quartzへ登録
			ModifySchedule modify = new ModifySchedule();
			modify.addSchedule(maintenanceInfo, loginUser);

			jtm.commit();

			// コミット後にキャッシュクリア
			NotifyRelationCache.refresh();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (EntityExistsException e) {
			jtm.rollback();
			throw new MaintenanceDuplicate(e.getMessage(), e);
		} catch (InvalidSetting e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addMaintenance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	/**
	 * メンテナンス情報を変更します。
	 * 
	 * @throws HinemosUnknown
	 * @throws NotifyNotFound
	 * @throws MaintenanceNotFound
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public void modifyMaintenance(MaintenanceInfo maintenanceInfo) throws HinemosUnknown, NotifyNotFound, MaintenanceNotFound, InvalidSetting, InvalidRole {
		m_log.debug("modifyMaintenance");

		JpaTransactionManager jtm = null;
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		// メイン処理
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 入力チェック
			MaintenanceValidator.validateMaintenanceInfo(maintenanceInfo);

			// メンテナンス情報を登録
			ModifyMaintenance modify = new ModifyMaintenance();
			modify.modifyMaintenance(maintenanceInfo, loginUser);

			// Quartzへ登録
			ModifySchedule modifySchedule = new ModifySchedule();
			modifySchedule.addSchedule(maintenanceInfo, loginUser);

			jtm.commit();

			// コミット後にキャッシュクリア
			NotifyRelationCache.refresh();
		} catch (InvalidSetting e) {
			jtm.rollback();
			throw e;
		} catch (MaintenanceNotFound e) {
			jtm.rollback();
			throw e;
		} catch (NotifyNotFound e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("modifyMaintenance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	/**
	 * メンテナンス情報を削除します。
	 * @throws HinemosUnknown
	 * @throws MaintenanceNotFound
	 * @throws InvalidRole
	 * 
	 */
	public void deleteMaintenance(String maintenanceId) throws HinemosUnknown, MaintenanceNotFound, InvalidRole {
		m_log.debug("deleteMaintenance");

		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// メンテナンス情報を削除
			DeleteMaintenance delete = new DeleteMaintenance();
			delete.deleteMaintenance(maintenanceId);

			ModifySchedule modify = new ModifySchedule();
			modify.deleteSchedule(maintenanceId);

			jtm.commit();

			// コミット後にキャッシュクリア
			NotifyRelationCache.refresh();

		} catch (MaintenanceNotFound e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteMaintenance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}


	/**
	 * メンテナンス情報を取得します。
	 *
	 * @return
	 * @throws MaintenanceNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 */
	public MaintenanceInfo getMaintenanceInfo(String maintenanceId) throws MaintenanceNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("getMaintenanceInfo()");

		JpaTransactionManager jtm = null;
		SelectMaintenanceInfo select = new SelectMaintenanceInfo();
		MaintenanceInfo info;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			info = select.getMaintenanceInfo(maintenanceId);
			jtm.commit();
		} catch (MaintenanceNotFound e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getMaintenanceInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return info;
	}

	/**
	 * メンテナンス情報の一覧を取得します。<BR>
	 * 
	 * @return メンテナンス情報の一覧を保持するArrayList
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 */
	public ArrayList<MaintenanceInfo> getMaintenanceList() throws InvalidRole, HinemosUnknown {
		m_log.debug("getMaintenanceList()");

		JpaTransactionManager jtm = null;
		SelectMaintenanceInfo select = new SelectMaintenanceInfo();
		ArrayList<MaintenanceInfo> list;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = select.getMaintenanceList();
			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getMaintenanceList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		return list;

	}

	/**
	 * メンテナンス種別の一覧を取得します。<BR>
	 * 下記のようにして生成されるArrayListを、要素として持つArrayListが一覧として返されます。
	 * 
	 * <p>
	 * MaintenanceTypeMstEntity mst = (MaintenanceTypeMstEntity)itr.next();
	 * ArrayList info = new ArrayList();
	 * info.add(mst.getType_id());
	 * info.add(mst.getName_id());
	 * info.add(mst.getOrder_no());
	 * ist.add(info);
	 * </p>
	 * 
	 * @return メンテナンス種別の一覧を保持するArrayList
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<MaintenanceTypeMst> getMaintenanceTypeList() throws InvalidRole, HinemosUnknown {
		m_log.debug("getMaintenanceTypeList()");

		JpaTransactionManager jtm = null;
		SelectMaintenanceTypeMst select = new SelectMaintenanceTypeMst();
		ArrayList<MaintenanceTypeMst> list;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = select.getMaintenanceTypeList();
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getMaintenanceTypeList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return list;
	}

	/**
	 * 
	 * メンテナンスの有効、無効を変更するメソッドです。
	 * @throws HinemosUnknown
	 * @throws MaintenanceNotFound
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 */
	public void setMaintenanceStatus(String maintenanceId, boolean validFlag) throws NotifyNotFound, MaintenanceNotFound, InvalidRole, HinemosUnknown{
		m_log.debug("setMaintenanceStatus() : maintenanceId=" + maintenanceId + ", validFlag=" + validFlag);
		// null check
		if(maintenanceId == null || "".equals(maintenanceId)){
			HinemosUnknown e = new HinemosUnknown("target maintenanceId is null or empty.");
			m_log.info("setMaintenanceStatus() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		MaintenanceInfo info = this.getMaintenanceInfo(maintenanceId);
		info.setValidFlg(ValidConstant.booleanToType(validFlag));

		try{
			this.modifyMaintenance(info);
		} catch (InvalidSetting  e) {
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * イベントログを削除します。
	 * 
	 * @param dataRetentionPeriod 保存期間(日)
	 * @param status 削除対象のステータス(true=全イベント、false=確認済みイベント)
	 * @param ownerRoleId オーナーロールID
	 * @return 削除件数
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * 時間を要する処理のため、NotSupportedを採用してJTAの管理下から除外する
	 */
	public int deleteEventLog(int dataRetentionPeriod, boolean status, String ownerRoleId) throws InvalidRole, HinemosUnknown {
		m_log.debug("deleteEventLog() : dataRetentionPeriod = " + dataRetentionPeriod + ", status = " + status + ", ownerRoleId = " + ownerRoleId);

		JpaTransactionManager jtm = null;
		MaintenanceEvent event = new MaintenanceEvent();
		int ret = 0;
		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = event.delete(dataRetentionPeriod, status, ownerRoleId);

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch(Exception e){
			m_log.warn("deleteEventLog() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		return ret;
	}


	/**
	 * ジョブ実行履歴を削除します。
	 * 
	 * @param dataRetentionPeriod 保存期間(日)
	 * @param status 削除対象のステータス(true=全履歴、false=実行状態が「終了」または「変更済み」の履歴)
	 * @param ownerRoleId オーナーロールID
	 * @return 削除件数
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * 時間を要する処理のため、NotSupportedを採用してJTAの管理下から除外する
	 */
	public int deleteJobHistory(int dataRetentionPeriod, boolean status, String ownerRoleId) throws InvalidRole, HinemosUnknown {
		m_log.debug("deleteJobHistory() : dataRetentionPeriod = " + dataRetentionPeriod + ", status = " + status + ", ownerRoleId = " + ownerRoleId);

		JpaTransactionManager jtm = null;
		MaintenanceJob job = new MaintenanceJob();
		int ret = 0;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = job.delete(dataRetentionPeriod, status, ownerRoleId);

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch(Exception e){
			m_log.warn("deleteJobHistory() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		return ret;
	}

	/**
	 * 性能実績を削除します。
	 * 
	 * @param dataRetentionPeriod 保存期間(日)
	 * @param status 削除対象のステータス(true=全履歴、false=実行状態が「終了」または「変更済み」の履歴)
	 * @param ownerRoleId オーナーロールID
	 * @return 削除件数
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * 時間を要する処理のため、NotSupportedを採用してJTAの管理下から除外する
	 * 
	 */
	public int deletePerfData(int dataRetentionPeriod, boolean status, String ownerRoleId) throws InvalidRole, HinemosUnknown {
		m_log.debug("deletePerfData() : dataRetentionPeriod = " + dataRetentionPeriod + ", status = " + status + ", ownerRoleId = " + ownerRoleId);

		JpaTransactionManager jtm = null;
		MaintenancePerf perf = new MaintenancePerf();
		int ret = 0;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = perf.delete(dataRetentionPeriod, status, ownerRoleId);

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch(Exception e){
			m_log.warn("deletePerfData() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		return ret;
	}


	/**
	 * 
	 * メンテナンス機能をスケジュール実行します。<BR>
	 * Quartzからスケジュール実行時に呼び出されます。
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * トランザクション開始はユーザが制御する。
	 * また、追加実装により、トランザクションの入れ子が予期せず生じることを避けるため、Neverを採用する。
	 */
	public void scheduleRunMaintenance(String maintenanceId, String calendarId) throws InvalidRole, HinemosUnknown {
		m_log.debug("scheduleRunMaintenance() : maintenanceId=" + maintenanceId + ", calendarId=" + calendarId);

		JpaTransactionManager jtm = null;
		try {
			// トランザクションがすでに開始されている場合は処理終了
			jtm = new JpaTransactionManager();
			jtm.begin(true);

			//カレンダをチェック
			boolean check = false;
			if(calendarId != null && calendarId.length() > 0){
				//カレンダによる実行可/不可のチェック
				if(new CalendarControllerBean().isRun(calendarId, new Date().getTime()).booleanValue()){
					check = true;
				}
			}
			else{
				check = true;
			}

			if(!check)
				return;

			//メンテナンス実行
			runMaintenance(maintenanceId);

			jtm.commit();
		} catch (CalendarNotFound e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch(Exception e){
			m_log.warn("scheduleRunMaintenance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	/**
	 * 
	 * メンテナンス機能を実行するメソッドです。
	 * 
	 * @param maintenanceId
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void runMaintenance(String maintenanceId) throws InvalidRole, HinemosUnknown {
		m_log.debug("runMaintenance() : maintenanceId=" + maintenanceId);

		JpaTransactionManager jtm = null;
		OperationMaintenance operation = new OperationMaintenance();

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			operation.runMaintenance(maintenanceId);

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch(Exception e){
			m_log.warn("runMaintenance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}
}
