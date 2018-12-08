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

package com.clustercontrol.notify.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PluginConstant;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.session.CheckFacility;
import com.clustercontrol.commons.util.AsyncTaskPersistentConfig;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.notify.bean.NotifyCheckIdResultInfo;
import com.clustercontrol.notify.bean.NotifyInfo;
import com.clustercontrol.notify.bean.NotifyRelationInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.entity.MonitorStatusPK;
import com.clustercontrol.notify.factory.AddNotify;
import com.clustercontrol.notify.factory.AddNotifyRelation;
import com.clustercontrol.notify.factory.DeleteNotify;
import com.clustercontrol.notify.factory.DeleteNotifyRelation;
import com.clustercontrol.notify.factory.ModifyNotify;
import com.clustercontrol.notify.factory.ModifyNotifyRelation;
import com.clustercontrol.notify.factory.NotifyDispatcher;
import com.clustercontrol.notify.factory.SelectNotify;
import com.clustercontrol.notify.factory.SelectNotifyRelation;
import com.clustercontrol.notify.model.NotifyJobInfoEntity;
import com.clustercontrol.notify.model.NotifyLogEscalateInfoEntity;
import com.clustercontrol.notify.util.NotifyCache;
import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.notify.util.NotifyValidator;
import com.clustercontrol.notify.util.OutputEvent;
import com.clustercontrol.notify.util.OutputStatus;
import com.clustercontrol.notify.util.QueryUtil;
import com.clustercontrol.notify.util.SendMail;
import com.clustercontrol.notify.util.SendSyslog;
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 * 通知機能の管理を行う Session Bean です。<BR>
 * クライアントからの Entity Bean へのアクセスは、Session Bean を介して行います。
 */
public class NotifyControllerBean implements CheckFacility {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( NotifyControllerBean.class );
	
	private static SendMail m_reportingSendMail = null;

	/**
	 * 通知情報を作成します。
	 *
	 * @param info 作成対象の通知情報
	 * @return 作成に成功した場合、<code> true </code>
	 * @throws NotifyDuplicate
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.factory.AddNotify#add(NotifyInfo)
	 */
	public boolean addNotify(NotifyInfo info) throws NotifyDuplicate, InvalidSetting, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 通知情報を登録
		boolean flag;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			//入力チェック
			NotifyValidator.validateNotifyInfo(info);

			AddNotify notify = new AddNotify();
			flag = notify.add(info,(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.commit();

			// コミット後にキャッシュクリア
			NotifyCache.refresh();
			NotifyRelationCache.refresh();

		} catch (NotifyDuplicate e) {
			jtm.rollback();
			throw e;
		} catch (InvalidSetting e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("addNotify() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		return  flag;
	}

	/**
	 * 通知情報を変更します。
	 *
	 * @param info 変更対象の通知情報
	 * @return 変更に成功した場合、<code> true </code>
	 * @throws NotifyDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws InvalidSetting
	 *
	 * @see com.clustercontrol.notify.factory.ModifyNotify#modify(NotifyInfo)
	 */
	public boolean modifyNotify(NotifyInfo info) throws NotifyDuplicate, InvalidRole, HinemosUnknown, InvalidSetting {
		JpaTransactionManager jtm = null;

		// 通知情報を更新
		boolean flag;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			//入力チェック
			NotifyValidator.validateNotifyInfo(info);

			ModifyNotify notify = new ModifyNotify();
			flag = notify.modify(info,(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.commit();

			// コミット後にキャッシュクリア
			NotifyCache.refresh();
			NotifyRelationCache.refresh();

		} catch (NotifyDuplicate e) {
			jtm.rollback();
			throw e;
		} catch (InvalidSetting e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("modifyNotify() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return flag;
	}

	/**
	 * 通知情報を削除します。
	 *
	 * @param notifyIds 削除対象の通知IDリスト
	 * @return 削除に成功した場合、<code> true </code>
	 * @throws NotifyDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 *
	 * @see com.clustercontrol.notify.factory.DeleteNotify#delete(String)
	 */
	public boolean deleteNotify(String[] notifyIds) throws NotifyNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 通知情報を削除
		DeleteNotify notify = new DeleteNotify();
		boolean flag = true;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for (String notifyId : notifyIds) {
				flag = flag && notify.delete(notifyId);
			}

			jtm.commit();

			// コミット後にキャッシュクリア
			NotifyCache.refresh();
			NotifyRelationCache.refresh();

		} catch(NotifyNotFound e){
			jtm.rollback();
			throw e;
		} catch(HinemosUnknown e){
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("deleteNotify() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return flag;
	}

	/**
	 * 引数で指定された通知情報を返します。
	 *
	 * @param notifyId 取得対象の通知ID
	 * @return 通知情報
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.factory.SelectNotify#getNotify(String)
	 */
	public NotifyInfo getNotify(String notifyId) throws NotifyNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 通知情報を取得
		SelectNotify notify = new SelectNotify();
		NotifyInfo info = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			info = notify.getNotify(notifyId);
			jtm.commit();
		} catch (NotifyNotFound e){
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e){
			jtm.rollback();
			throw e;
		} catch (InvalidRole e){
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getNotify() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		return info;
	}

	/**
	 * 通知情報一覧を返します。
	 *
	 * @return 通知情報一覧（Objectの2次元配列）
	 * @throws NotifyNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 *
	 * @see com.clustercontrol.notify.factory.SelectNotify#getNotifyList()
	 */
	public ArrayList<NotifyInfo> getNotifyList() throws NotifyNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 通知一覧を取得
		SelectNotify notify = new SelectNotify();
		ArrayList<NotifyInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = notify.getNotifyList();
			jtm.commit();
		}catch (NotifyNotFound e){
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getNotifyList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		return list;
	}

	/**
	 * オーナーロールIDを指定して通知情報一覧を返します。
	 *
	 * @param ownerRoleId オーナーロールID
	 * @return 通知情報一覧（Objectの2次元配列）
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 */
	public ArrayList<NotifyInfo> getNotifyListByOwnerRole(String ownerRoleId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 通知一覧を取得
		SelectNotify notify = new SelectNotify();
		ArrayList<NotifyInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = notify.getNotifyListByOwnerRole(ownerRoleId);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getNotifyListByOwnerRole() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		return list;
	}


	/**
	 * 通知グループに対応する通知を取得します。
	 *
	 * @param notifyGroupId  通知グループID
	 * @return 通知
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 */
	public  ArrayList<NotifyRelationInfo> getNotifyRelation(String notifyGroupId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		SelectNotifyRelation notify = new SelectNotifyRelation();
		ArrayList<NotifyRelationInfo> info =  new ArrayList<NotifyRelationInfo>();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			info = notify.getNotifyRelation(notifyGroupId);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getNotifyRelation(notifyGroupId) : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return info;
	}


	/**
	 * 通知グループを変更します。
	 *
	 * @param info 通知のセット
	 * @param notifyGroupId 通知グループID
	 * @return 変更に成功した場合、<code> true </code>
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 */
	public boolean modifyNotifyRelation(Collection<NotifyRelationInfo> info, String notifyGroupId)
			throws NotifyNotFound, InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = null;

		// システム通知情報を更新
		ModifyNotifyRelation notify = new ModifyNotifyRelation();
		boolean flag;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			flag = notify.modify(info, notifyGroupId);
			jtm.commit();
		} catch (NotifyNotFound e){
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (HinemosUnknown e){
			jtm.rollback();
			throw e;
		} catch (Exception e){
			m_log.warn("modifyNotifyRelation() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return flag;
	}

	/**
	 * 通知グループを削除します。
	 *
	 * @param notifyGroupId 通知グループID
	 * @return 削除に成功した場合、<code> true </code>
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 */
	public boolean deleteNotifyRelation(String notifyGroupId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// システム通知情報を削除
		DeleteNotifyRelation notify = new DeleteNotifyRelation();
		boolean flag;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			flag = notify.delete(notifyGroupId);

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (HinemosUnknown e){
			jtm.rollback();
			throw e;
		} catch (Exception e){
			m_log.warn("deleteNotifyRelation() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return flag;
	}

	/**
	 * 通知グループを作成します。
	 *
	 * @param info 通知グループ
	 * @return 作成に成功した場合、<code> true </code>
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean addNotifyRelation(Collection<NotifyRelationInfo> info) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// システム通知情報を登録
		if(info != null){
			AddNotifyRelation notify = new AddNotifyRelation();
			boolean flag;
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();

				flag = notify.add(info);

				jtm.commit();
			} catch (ObjectPrivilege_InvalidRole e) {
				jtm.rollback();
				throw new InvalidRole(e.getMessage(), e);
			} catch (HinemosUnknown e){
				jtm.rollback();
				throw e;
			} catch (Exception e){
				m_log.warn("addNotifyRelation() : " + e.getClass().getSimpleName() +
						", " + e.getMessage(), e);
				jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} finally {
				jtm.close();
			}
			return flag;
		}else{
			return true;
		}
	}

	/**
	 *　引数で指定した通知IDを利用している通知グループIDを取得する。
	 *
	 * @param notifyIds
	 * @return　通知グループIDのリスト
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<NotifyCheckIdResultInfo> checkNotifyId(String[] notifyIds) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		ArrayList<NotifyCheckIdResultInfo> ret = new ArrayList<NotifyCheckIdResultInfo>();
		SelectNotifyRelation notify = new SelectNotifyRelation();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for (int i = 0; i < notifyIds.length; i++) {
				String notifyId = notifyIds[i];
				NotifyCheckIdResultInfo result = new NotifyCheckIdResultInfo();
				result.setNotifyId(notifyId);
				result.setNotifyGroupIdList(notify.getNotifyGroupIdBaseOnNotifyId(notifyId));
				ret.add(result);
			}
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("checkNotifyId() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return ret;
	}

	/**
	 *　指定した通知IDを有効化/無効化する。
	 *
	 * @param notifyId
	 * @throws HinemosUnknown
	 * @throws NotifyNotFound
	 * @throws NotifyDuplicate
	 * @throws InvalidRole
	 */
	public void setNotifyStatus(String notifyId, boolean validFlag) throws HinemosUnknown, NotifyNotFound, NotifyDuplicate, InvalidRole {
		JpaTransactionManager jtm = null;

		// null check
		if(notifyId == null || "".equals(notifyId)){
			HinemosUnknown e = new HinemosUnknown("target notifyId is null or empty.");
			m_log.info("setNotifyStatus() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			NotifyInfo info = getNotify(notifyId);
			if (validFlag) {
				// 通知設定の有効化
				if(info.getValidFlg().intValue() != ValidConstant.TYPE_VALID){
					info.setValidFlg(ValidConstant.TYPE_VALID);
					modifyNotify(info);
				}
			} else {
				// 通知設定の無効化
				if(info.getValidFlg().intValue() != ValidConstant.TYPE_INVALID){
					info.setValidFlg(ValidConstant.TYPE_INVALID);
					modifyNotify(info);
				}
			}

			jtm.commit();

			// コミット後にキャッシュクリア
			NotifyCache.refresh();
			NotifyRelationCache.refresh();

		} catch (NotifyNotFound e) {
			jtm.rollback();
			throw e;
		} catch (NotifyDuplicate e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidSetting e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("setNotifyStatus() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	/**
	 * 指定したファシリティIDが利用されているか確認する
	 *
	 *
	 */
	@Override
	public void isUseFacilityId(String facilityId) throws UsedFacility {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 指定のファシリティIDが存在するか確認
			new RepositoryControllerBean().getFacilityEntityByPK(facilityId);

			List<NotifyJobInfoEntity> notifyJobInfoEntityList = QueryUtil.getNotifyJobInfoByJobExecFacilityId(facilityId);
			if (notifyJobInfoEntityList != null
					&& notifyJobInfoEntityList.size() > 0) {
				UsedFacility e = new UsedFacility(PluginConstant.TYPE_NOTIFY);
				m_log.info("isUseFacilityId() : " + e.getClass().getSimpleName() +
						", " + e.getMessage());
				throw e;
			}
			List<NotifyLogEscalateInfoEntity> notifyLogEscalateInfoEntityList
			= QueryUtil.getNotifyLogEscalateInfoByEscalateFacilityId(facilityId);
			if (notifyLogEscalateInfoEntityList != null
					&& notifyLogEscalateInfoEntityList.size() > 0) {
				UsedFacility e = new UsedFacility(PluginConstant.TYPE_NOTIFY);
				m_log.info("isUseFacilityId() : " + e.getClass().getSimpleName() +
						", " + e.getMessage());
				throw e;
			}
			jtm.commit();
		} catch (UsedFacility e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
		} catch (FacilityNotFound e) {
			jtm.rollback();
		} catch (InvalidRole e) {
			jtm.rollback();
		} catch (Exception e) {
			m_log.warn("isUseFacilityId() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			jtm.rollback();
		} finally {
			jtm.close();
		}
	}

	/**
	 * イベント通知
	 *
	 * トランザクションは引き継がれたものを使用
	 *
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public void insertEventLog(OutputBasicInfo output, int confirmState) throws InvalidRole, HinemosUnknown{
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			new OutputEvent().insertEventLog(output, confirmState);

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("insertEventLog() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}
	
	/**
	 * ステータス通知
	 * 
	 * トランザクションは引き継がれたものを使用
	 * 
	 * @param output
	 * @throws HinemosUnknown
	 */
	public void updateStatusLog(OutputBasicInfo output) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			new OutputStatus().updateStatus(output);

			jtm.commit();
		} catch (Exception e) {
			m_log.warn("insertEventLog() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * Syslog通知
	 *
	 * トランザクションは引き継がれたものを使用
	 *
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void sendAfterConvertHostname(String ipAddress, int port, String facility, String severity, String facilityId, String message, String timeStamp) throws InvalidRole, HinemosUnknown{
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			new SendSyslog().sendAfterConvertHostname(ipAddress, port, facility, severity, facilityId, message, timeStamp);

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("sendAfterConvertHostname() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	/**
	 * メール通知
	 *
	 * トランザクションは引き継がれたものを使用
	 *
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void sendMail(String[] address, OutputBasicInfo outputBasicInfo) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SendMail sendMail;
			
			// レポーティングオプション用添付ファイル対応 
			if(outputBasicInfo.getPluginId().equals(HinemosModuleConstant.REPORTING) 
					&& outputBasicInfo.getSubKey() != null) {
				
				// 最初の1回だけ実施
				if(m_reportingSendMail == null) {
					String sendMailClass = "com.clustercontrol.notify.util.ReportingSendMail";
					try {
						Class<? extends SendMail> clazz = (Class<? extends SendMail>) Class.forName(sendMailClass);
						m_reportingSendMail = clazz.newInstance();
						m_log.info("load " + sendMailClass + ".");
					} catch (Exception e) {
						throw new HinemosUnknown(e.getMessage(), e);
					}
				}
				
				m_log.debug("m_reportingSendMail.notify");
				sendMail = m_reportingSendMail;
				
			} else {
				m_log.debug("sendMail.notify");
				sendMail = new SendMail();
			}
			
			String mailSubject = sendMail.getSubject(outputBasicInfo, null);
			String mailBody = sendMail.getContent(outputBasicInfo, null);
			sendMail.sendMail(address, mailSubject, mailBody);

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("sendMail() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	// 排他制御用のキーセット
	private static ConcurrentHashMap<MonitorStatusPK, MonitorStatusPK> m_keyMap = new ConcurrentHashMap<MonitorStatusPK, MonitorStatusPK>();

	private static MonitorStatusPK getLock(MonitorStatusPK pk) {
		MonitorStatusPK lockObject = null;
		synchronized (m_keyMap) {
			lockObject = m_keyMap.get(pk);
			if(lockObject == null){
				lockObject = pk;
				// このキャッシュを削除する機構は実装していない。
				// 問題にならない容量であり、削除する機構を実装すると複雑なので。
				m_keyMap.put(pk, lockObject);
			}
		}
		return lockObject;
	}
	
	// ジョブは実行ごとにキャッシュができるためそれを消すための機構
	public static void removeCache(String pluginId, Map<String, String> monitorMap) {
		synchronized (m_keyMap) {
			Set<MonitorStatusPK> keySet = m_keyMap.keySet();
			for (MonitorStatusPK key : keySet) {
				if (key.getPluginId().equals(pluginId) && monitorMap.containsKey(key.getMonitorId())) {
					m_keyMap.remove(key);
				}
			}
		}
		return;
	}

	/**
	 * 外部から直接通知処理を実行します。
	 *
	 * @param pluginId プラグインID
	 * @param monitorId 監視項目ID
	 * @param facilityId ファシリティID
	 * @param subKey 抑制用のサブキー（任意の文字列）
	 * @param generationDate 出力日時（エポック秒）
	 * @param priority 重要度
	 * @param application アプリケーション
	 * @param messageId メッセージID
	 * @param message メッセージ
	 * @param messageOrg オリジナルメッセージ
	 * @param notifyIdList 通知IDのリスト
	 * @param srcId 送信元を特定するためのID
	 * @throws FacilityNotFound
	 * @throws NotifyNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public void notify(
			String pluginId,
			String monitorId,
			String facilityId,
			String subKey,
			long generationDate,
			int priority,
			String application,
			String messageId,
			String message,
			String messageOrg,
			ArrayList<String> notifyIdList,
			String srcId) throws FacilityNotFound, HinemosUnknown, NotifyNotFound, InvalidRole {
		m_log.info("notify() "
				+ "pluginId = " + pluginId
				+ ", monitorId = " + monitorId
				+ ", facilityId = " + facilityId
				+ ", subKey = " + subKey
				+ ", generationDate = " + generationDate
				+ ", priority = " + priority
				+ ", application = " + application
				+ ", messageId = " + messageId
				+ ", message = " + message
				+ ", messageOrg = " + messageOrg
				+ ", srcId = " + srcId
				);

		JpaTransactionManager jtm = null;

		// パラメータのnullチェック
		if(pluginId == null){
			m_log.info("notify() Invalid argument. pluginId is null.");
			return;
		} else if(monitorId == null){
			m_log.info("notify() Invalid argument. monitorId is null.");
			return;
		} else if(facilityId == null){
			m_log.info("notify() Invalid argument. facilityId is null.");
			return;
		} else if(application == null){
			m_log.info("notify() Invalid argument. application is null.");
			return;
		} else if(messageId == null){
			m_log.info("notify() Invalid argument. messageId is null.");
			return;
		} else if(message == null){
			m_log.info("notify() Invalid argument. message is null.");
			return;
		} else if(messageOrg == null){
			m_log.info("notify() Invalid argument. messageOrg is null.");
			return;
		} else if(notifyIdList == null){
			m_log.info("notify() Invalid argument. notifyIdList is null.");
			return;
		}

		if(subKey == null){
			// エラーとして扱わず空文字を設定する。
			subKey = "";
		}

		// 通知キューに入れる処理(抑制チェック処理)はpkで排他とする。
		MonitorStatusPK pk = new MonitorStatusPK(facilityId, pluginId, monitorId, subKey);
		MonitorStatusPK lockObject = getLock(pk);
		synchronized (lockObject) {
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();

				// 指定のファシリティIDが存在するか確認
				new RepositoryControllerBean().getFacilityEntityByPK(facilityId);

				// 指定の通知設定が存在するか確認
				ArrayList<String> confirmedNotifyIdList = new ArrayList<String>();
				for(String notifyId : notifyIdList){
					QueryUtil.getNotifyInfoPK(notifyId);
					confirmedNotifyIdList.add(notifyId);
				}

				OutputBasicInfo output = new OutputBasicInfo();
				output.setApplication(application);
				output.setFacilityId(facilityId);
				try {
					String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
					output.setScopeText(facilityPath);
				} catch (ObjectPrivilege_InvalidRole e) {
					throw new InvalidRole(e.getMessage(), e);
				} catch (HinemosUnknown e) {
					// ファシリティIDをファシリティパスとする
					output.setScopeText(facilityId);
				} catch (Exception e) {
					m_log.warn("notify() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					// ファシリティIDをファシリティパスとする
					output.setScopeText(facilityId);
				}
				output.setGenerationDate(generationDate);
				output.setMessage(message);
				output.setMessageId(messageId);
				output.setMessageOrg(messageOrg);
				output.setMonitorId(monitorId);
				output.setMultiId(srcId);
				output.setPluginId(pluginId);
				output.setPriority(priority);
				output.setSubKey(subKey);

				notify(output, confirmedNotifyIdList);

				jtm.commit();
			} catch (NotifyNotFound e) {
				jtm.rollback();
				throw e;
			} catch (InvalidRole e) {
				jtm.rollback();
				throw e;
			} catch (Exception e) {
				m_log.warn("notify() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} finally {
				jtm.close();
			}
		}
	}

	/**
	 * 外部から直接通知処理を実行します。
	 *
	 * @param notifyInfo 通知情報
	 * @param notifyGroupId
	 * @throws HinemosUnknown 
	 */
	public void notify(OutputBasicInfo notifyInfo, String notifyGroupId) throws HinemosUnknown {

		JpaTransactionManager jtm = null;

		// 通知キューに入れる処理(抑制チェック処理)はpkで排他とする。
		MonitorStatusPK pk = new MonitorStatusPK(notifyInfo.getFacilityId(),
				notifyInfo.getPluginId(), notifyInfo.getMonitorId(), notifyInfo.getSubKey());
		MonitorStatusPK lockObject = getLock(pk);
		synchronized (lockObject) {
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();

				// 監視設定から通知IDのリストを取得する
				List<String> notifyIdList = NotifyRelationCache.getNotifyIdList(notifyGroupId);
				m_log.trace("notifyIdList.size=" + notifyIdList.size() + ", notifyGroupId=" + notifyGroupId);
				// 該当の通知IDのリストがない場合は終了する
				if(notifyIdList == null || notifyIdList.size() <= 0){
					return;
				}

				// 通知処理を行う
				notify(notifyInfo, notifyIdList);

				jtm.commit();
			} catch (Exception e) {
				m_log.warn("notify() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				jtm.rollback();
				
				throw new HinemosUnknown("notify() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			} finally {
				jtm.close();
			}
		}
	}

	protected void notify(OutputBasicInfo notifyInfo, List<String> notifyIdList) throws HinemosUnknown {
		// 機能毎に設定されているJMSの永続化モードを取得
		boolean persist = AsyncTaskPersistentConfig.isPersisted(notifyInfo.getPluginId());

		// 通知キューへの登録処理を実行
		NotifyDispatcher.notifyAction(notifyInfo, notifyIdList, persist);
	}

	/**
	 * 一括通知
	 * 前提条件：notifyInfoListの通知情報は同じ監視項目に属する。
	 * 
	 * @param notifyInfoList 通知情報配列
	 * @param notifyGroupId 通知グループID
	 */
	public void notify(ArrayList<OutputBasicInfo> notifyInfoList,
			String notifyGroupId) {

		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 監視設定から通知IDのリストを取得する
			List<String> notifyIdList = NotifyRelationCache.getNotifyIdList(notifyGroupId);

			// 該当の通知IDのリストがない場合は終了する
			if(notifyIdList == null || notifyIdList.size() <= 0){
				return;
			}

			// 通知処理を行う
			notify(notifyInfoList, notifyIdList);

			jtm.commit();
		} catch (Exception e) {
			m_log.warn("notify() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new RuntimeException("notify() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	private void notify(ArrayList<OutputBasicInfo> outputBasicInfoList,
			List<String> notifyIdList) throws HinemosUnknown {
		if (outputBasicInfoList.isEmpty()) {
			return;
		}

		// 機能毎に設定されているJMSの永続化モードを取得
		boolean persist = AsyncTaskPersistentConfig.isPersisted(outputBasicInfoList.get(0).getPluginId());

		// 通知キューへの登録処理を実行
		NotifyDispatcher.notifyAction(outputBasicInfoList, notifyIdList, persist);
	}
}
