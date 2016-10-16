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

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosPropertyDuplicate;
import com.clustercontrol.fault.HinemosPropertyNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MaintenanceDuplicate;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.maintenance.bean.HinemosPropertyInfo;
import com.clustercontrol.maintenance.factory.AddHinemosProperty;
import com.clustercontrol.maintenance.factory.HinemosPropertyInfoCache;
import com.clustercontrol.maintenance.factory.DeleteHinemosProperty;
import com.clustercontrol.maintenance.factory.ModifyHinemosProperty;
import com.clustercontrol.maintenance.factory.SelectHinemosPropertyInfo;
import com.clustercontrol.maintenance.util.HinemosPropertyValidator;
import com.clustercontrol.notify.util.NotifyRelationCache;

/**
 * 
 * 共通設定機能を管理する Session Bean です。<BR>
 * 
 */
public class HinemosPropertyControllerBean {

	private static Log m_log = LogFactory.getLog( HinemosPropertyControllerBean.class );

	/**
	 * 共通設定情報を追加します。
	 * 
	 * @throws HinemosUnknown
	 * @throws MaintenanceDuplicate
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public void addHinemosProperty(HinemosPropertyInfo info)
			throws HinemosUnknown, HinemosPropertyDuplicate, InvalidSetting, InvalidRole {
		m_log.debug("addMaintenance");

		JpaTransactionManager jtm = null;
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		// メイン処理
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 入力チェック
			HinemosPropertyValidator.validateHinemosPropertyInfo(info);

			// 共通設定情報を登録
			AddHinemosProperty add = new AddHinemosProperty();
			add.addHinemosProperty(info, loginUser);

			jtm.commit();
			HinemosPropertyInfoCache.refresh();

		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (EntityExistsException e) {
			jtm.rollback();
			throw new HinemosPropertyDuplicate(e.getMessage(), e);
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
			m_log.warn("addHinemosProperty() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	/**
	 * 共通設定情報を変更します。
	 * 
	 * @throws HinemosUnknown
	 * @throws NotifyNotFound
	 * @throws MaintenanceNotFound
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public void modifyHinemosProperty(HinemosPropertyInfo info) throws HinemosUnknown, HinemosPropertyNotFound, InvalidSetting, InvalidRole {
		m_log.debug("modifyHinemosProperty");

		JpaTransactionManager jtm = null;
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		// メイン処理
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 入力チェック
			HinemosPropertyValidator.validateHinemosPropertyInfo(info);

			// 共通設定情報を登録
			ModifyHinemosProperty modify = new ModifyHinemosProperty();
			modify.modifyHinemosProperty(info, loginUser);

			jtm.commit();

			HinemosPropertyInfoCache.refresh();

		} catch (InvalidSetting e) {
			jtm.rollback();
			throw e;
		} catch (HinemosPropertyNotFound e) {
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
			m_log.warn("modifyHinemosProperty() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	/**
	 * 共通設定情報を削除します。
	 * 
	 * @param key キー
	 * @throws HinemosUnknown
	 * @throws MaintenanceNotFound
	 * @throws InvalidRole
	 * 
	 */
	public void deleteHinemosProperty(String key) throws HinemosUnknown, HinemosPropertyNotFound, InvalidRole {
		m_log.debug("deleteHinemosProperty");

		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 共通設定情報を削除
			DeleteHinemosProperty delete = new DeleteHinemosProperty();
			delete.deleteHinemosProperty(key);

			jtm.commit();

			HinemosPropertyInfoCache.refresh();

			// コミット後にキャッシュクリア
			NotifyRelationCache.refresh();

		} catch (HinemosPropertyNotFound e) {
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
			m_log.warn("deleteHinemosProperty() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}


	/**
	 * 共通設定情報を取得します。
	 *
	 * @param key キー
	 * @return 共通設定情報
	 * @throws HinemosPropertyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 */
	public HinemosPropertyInfo getHinemosPropertyInfo(String key) throws HinemosPropertyNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("getHinemosPropertyInfo()");

		JpaTransactionManager jtm = null;
		SelectHinemosPropertyInfo select = new SelectHinemosPropertyInfo();
		HinemosPropertyInfo info;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			info = select.getHinemosPropertyInfo(key);
			jtm.commit();
		} catch (HinemosPropertyNotFound e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getHinemosPropertyInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return info;
	}

	/**
	 * 共通設定情報を取得します。
	 *
	 * @param key キー
	 * @return 共通設定情報
	 * 
	 */
	public HinemosPropertyInfo getHinemosPropertyInfo_None(String key) {
		m_log.debug("getHinemosPropertyInfo()");

		SelectHinemosPropertyInfo select = new SelectHinemosPropertyInfo();
		return select.getHinemosPropertyInfo_None(key);
	}
	/**
	 * 共通設定情報の一覧を取得します。<BR>
	 * 
	 * @return 共通設定情報の一覧を保持するArrayList
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 */
	public ArrayList<HinemosPropertyInfo> getHinemosPropertyList() throws InvalidRole, HinemosUnknown {
		m_log.debug("getHinemosPropertyList()");

		JpaTransactionManager jtm = null;
		SelectHinemosPropertyInfo select = new SelectHinemosPropertyInfo();
		ArrayList<HinemosPropertyInfo> list;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = select.getHinemosPropertyInfoList();
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getHinemosPropertyList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		return list;

	}
}
