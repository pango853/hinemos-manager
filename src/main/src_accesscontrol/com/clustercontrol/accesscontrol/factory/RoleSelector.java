/*

Copyright (C) since 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.accesscontrol.factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.PrivilegeNotFound;
import com.clustercontrol.fault.RoleNotFound;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.util.Messages;
import com.clustercontrol.accesscontrol.bean.ObjectPrivilegeFilterInfo;
import com.clustercontrol.accesscontrol.bean.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.bean.RoleInfo;
import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.accesscontrol.bean.RoleTreeItem;
import com.clustercontrol.accesscontrol.bean.SystemPrivilegeInfo;
import com.clustercontrol.accesscontrol.bean.UserInfo;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeEntity;
import com.clustercontrol.accesscontrol.model.RoleEntity;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeEntity;
import com.clustercontrol.accesscontrol.model.UserEntity;
import com.clustercontrol.accesscontrol.util.QueryUtil;

/**
 * ロール情報を検索するファクトリクラス<BR>
 *
 * @version 1.0.0
 * @since 3.2.0
 */
public class RoleSelector {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(RoleSelector.class);

	/**
	 * ロール一覧情報を取得する。<BR>
	 * 
	 * @return ロール情報のリスト
	 * @throws UserNotFound
	 * @throws HinemosUnknown
	 */
	public static ArrayList<RoleInfo> getRoleInfoList() {

		ArrayList<RoleInfo> list = new ArrayList<RoleInfo>();
		List<RoleEntity> roles = null;

		m_log.debug("getting all role...");

		// 全ユーザを取得
		roles = QueryUtil.getAllShowRole();

		if(roles != null && roles.size() > 0){
			Iterator<RoleEntity> itr = roles.iterator();
			while(itr.hasNext()){
				RoleEntity role = itr.next();
				RoleInfo info = getRoleInfoBean(role);
				list.add(info);
			}
		}

		m_log.debug("successful in getting all role.");
		return list;
	}

	/**
	 * RoleEntityからRoleInfoBeanへ変換
	 */
	private static RoleInfo getRoleInfoBean(RoleEntity entity) {
		RoleInfo info = new RoleInfo();
		info.setId(entity.getRoleId());
		info.setName(entity.getRoleName());
		info.setRoleType(entity.getRoleType());
		info.setDescription(entity.getDescription());
		info.setCreateUserId(entity.getCreateUserId());
		if (entity.getCreateDatetime() != null) {
			info.setCreateDate(entity.getCreateDatetime().getTime());
		}
		info.setModifyUserId(entity.getModifyUserId());
		if (entity.getModifyDatetime() != null) {
			info.setModifyDate(entity.getModifyDatetime().getTime());
		}
		return info;
	}


	/**
	 * 指定のロール情報を取得する。<BR>
	 * 
	 * @return ユーザ情報
	 * @throws HinemosUnknown
	 */
	public static RoleInfo getRoleInfo(String roleId) throws HinemosUnknown {

		RoleInfo info = null;

		if(roleId != null && roleId.compareTo("") != 0){
			try {
				// ユーザを取得
				RoleEntity entity = QueryUtil.getRolePK(roleId);
				info = getRoleInfoBean(entity);
				// ロールに所属するユーザIDのリスト
				for(UserEntity userEntity : entity.getUserEntities()){
					info.addUser(userEntity.getUserId());
				}
				// ロールに所属するシステム権限のリスト
				for(SystemPrivilegeEntity systemPrivilegeEntity : entity.getSystemPrivilegeEntities()){
					info.addSystemPrivilege(getSystemPrivilegeInfoBean(systemPrivilegeEntity));
				}
			} catch (RoleNotFound e) {
				// 何もしない
			} catch (Exception e) {
				m_log.warn("getRoleInfo() failure to get user. : userId = " + roleId, e);
				throw new HinemosUnknown(e.getMessage() + " : failure to get role.", e);
			}
		}
		return info;
	}

	/**
	 * ロールツリー情報を取得します。
	 * <P>
	 * <ol>
	 * <li>ロールツリー情報のルート(最上位)のインスタンスを作成します。</li>
	 * <li>ロール情報を取得し、</li>
	 * <li>取得したロール情報の数、ユーザ情報を取得し、ロールツリー情報を作成します。</li>
	 * </ol>
	 * 
	 * @param locale ロケール情報
	 * @param userId ログインユーザのユーザID
	 * @return ロールツリー情報{@link com.clustercontrol.accesscontrol.bean.RoleTreeItem}の階層オブジェクト
	 * @throws UserNotFound
	 * 
	 */
	public RoleTreeItem getRoleTree(Locale locale, String userId) throws UserNotFound {

		// 最上位インスタンスを作成
		RoleTreeItem top = new RoleTreeItem();

		// ルートインスタンスを作成
		RoleInfo rootRoleItem = new RoleInfo();
		rootRoleItem.setId(RoleSettingTreeConstant.ROOT_ID);
		rootRoleItem.setName(Messages.getString("role", locale));
		RoleTreeItem root = new RoleTreeItem(top, rootRoleItem);

		// ロール一覧を取得
		List<RoleEntity> list = QueryUtil.getAllShowRole();
		if (list != null) {
			m_log.debug("roleEntities size = " + list.size());
		}

		for (RoleEntity roleEntity : list) {
			if (roleEntity.getUserEntities() != null) {
				m_log.debug("userEntities size = " + roleEntity.getUserEntities().size());
			}

			// ロール情報を作成
			RoleInfo roleInfo = RoleSelector.getRoleInfoBean(roleEntity);

			// ロール情報のRoleTreeItemを作成
			RoleTreeItem role = new RoleTreeItem(root, roleInfo);

			for (UserEntity userEntity : roleEntity.getUserEntities()) {
				// ユーザ情報を作成
				UserInfo userInfo = LoginUserSelector.getUserInfoBean(userEntity);
				// ユーザ情報のRoleTreeItemを作成
				new RoleTreeItem(role, userInfo);
			}
		}
		removeParent(top);
		return top;
	}


	/**
	 * ユーザが所属するロールID情報を取得する。<BR>
	 * 
	 * @param ユーザID（指定されていない場合はADMINISTRATORS）
	 * @return ユーザID一覧
	 * @throws HinemosUnknown
	 */
	public static ArrayList<String> getOwnerRoleIdList(String userId) throws HinemosUnknown {

		ArrayList<String> list = new ArrayList<String>();
		List<RoleEntity> roleEntities = null;
		if (userId == null || userId.isEmpty()) {
			// 全ユーザを取得
			roleEntities = QueryUtil.getAllShowRole();
		} else {
			try {
				// ユーザを取得
				UserEntity entity = QueryUtil.getUserPK(userId);
				if (entity != null){
					roleEntities = entity.getRoleEntities();
				}
			} catch (UserNotFound e) {
				// 何もしない
			} catch (Exception e) {
				m_log.warn("getUserRoleInfo() failure to get user. : userId = " + userId, e);
				throw new HinemosUnknown(e.getMessage() + " : failure to get user.", e);
			}
		}
		if(roleEntities != null && roleEntities.size() > 0){
			Iterator<RoleEntity> itr = roleEntities.iterator();
			while(itr.hasNext()){
				RoleEntity roleEntity = itr.next();
				if (!roleEntity.getRoleId().equals(RoleIdConstant.INTERNAL)) {
					list.add(roleEntity.getRoleId());
				}
			}
		}
		return list;
	}

	/**
	 * webサービスでは双方向の参照を保持することができないので、
	 * 親方向への参照を消す。
	 * クライアント側で参照を付与する。
	 * @param roleSettinsgTreeItem
	 */
	private void removeParent(RoleTreeItem roleSettinsgTreeItem) {
		roleSettinsgTreeItem.setParent(null);
		for (RoleTreeItem child : roleSettinsgTreeItem.getChildren()) {
			removeParent(child);
		}
	}

	/**
	 * システム権限一覧情報を取得する。<BR>
	 * 
	 * @return システム権限情報のリスト
	 * @throws HinemosUnknown
	 */
	public static ArrayList<SystemPrivilegeInfo> getSystemPrivilegeInfoList() {

		ArrayList<SystemPrivilegeInfo> list = new ArrayList<SystemPrivilegeInfo>();
		List<SystemPrivilegeEntity> systemPrivileges = null;

		m_log.debug("getting all system privilege...");

		// 全ユーザを取得
		systemPrivileges = QueryUtil.getAllSystemPrivilege();

		if(systemPrivileges != null && systemPrivileges.size() > 0){
			Iterator<SystemPrivilegeEntity> itr = systemPrivileges.iterator();
			while(itr.hasNext()){
				SystemPrivilegeEntity systemPrivilege = itr.next();
				SystemPrivilegeInfo info = getSystemPrivilegeInfoBean(systemPrivilege);
				list.add(info);
			}
		}

		m_log.debug("successful in getting all system privilege.");
		return list;
	}

	/**
	 * 指定されたユーザIDを条件としてシステム権限一覧情報を取得する。<BR>
	 * 
	 * @param userId ユーザID
	 * @return システム権限情報のリスト
	 * @throws HinemosUnknown
	 */
	public static ArrayList<SystemPrivilegeInfo> getSystemPrivilegeInfoListByUserId(String userId) {

		ArrayList<SystemPrivilegeInfo> list = new ArrayList<SystemPrivilegeInfo>();

		// 全ユーザを取得
		list = QueryUtil.getSystemPrivilegeByUserId(userId);
		return list;
	}

	/**
	 * SystemPrivilegeEntityからSystemPrivilegeInfoへ変換
	 */
	private static SystemPrivilegeInfo getSystemPrivilegeInfoBean(SystemPrivilegeEntity entity) {
		SystemPrivilegeInfo info = new SystemPrivilegeInfo();
		info.setSystemFunction(entity.getId().getSystemFunction());
		info.setSystemPrivilege(entity.getId().getSystemPrivilege());
		return info;
	}

	/**
	 * 指定のオブジェクト権限情報を取得する。<BR>
	 * 
	 * @param objectType
	 * @param objectId
	 * @param roleId
	 * @param objectPrivilege
	 * @return オブジェクト権限情報
	 * @throws HinemosUnknown
	 */
	public static ObjectPrivilegeInfo getObjectPrivilegeInfo(
			String objectType,
			String objectId,
			String roleId,
			String objectPrivilege) throws HinemosUnknown {

		ObjectPrivilegeInfo info = null;

		if(objectType != null && objectType.compareTo("") != 0
				&& objectId != null && objectId.compareTo("") != 0
				&& roleId != null && roleId.compareTo("") != 0
				&& objectPrivilege != null && objectPrivilege.compareTo("") != 0){
			try {
				// オブジェクト権限を取得
				ObjectPrivilegeEntity entity = QueryUtil.getObjectPrivilegePK(objectType, objectId, roleId, objectPrivilege);
				info = getObjectPrivilegeInfoBean(entity);
			} catch (PrivilegeNotFound e) {
				// 何もしない
			} catch (Exception e) {
				m_log.warn("getRoleInfo() failure to get user. : userId = " + roleId, e);
				throw new HinemosUnknown(e.getMessage() + " : failure to get role.", e);
			}
		}
		return info;
	}

	/**
	 * オブジェクト権限一覧情報を取得する。<BR>
	 * 
	 * @return オブジェクト権限情報のリスト
	 * @throws HinemosUnknown
	 */
	public static ArrayList<ObjectPrivilegeInfo> getObjectPrivilegeInfoList(ObjectPrivilegeFilterInfo filter) {

		ArrayList<ObjectPrivilegeInfo> list = new ArrayList<ObjectPrivilegeInfo>();
		List<ObjectPrivilegeEntity> objectPrivileges = null;

		if (filter == null) {
			// 全ユーザを取得
			objectPrivileges = QueryUtil.getAllObjectPrivilege();
		} else {
			// 全ユーザを取得
			objectPrivileges = QueryUtil.getAllObjectPrivilegeByFilter(
					filter.getObjectType(),
					filter.getObjectId(),
					filter.getRoleId(),
					filter.getObjectPrivilege());
		}

		if(objectPrivileges != null && objectPrivileges.size() > 0){
			Iterator<ObjectPrivilegeEntity> itr = objectPrivileges.iterator();
			while(itr.hasNext()){
				ObjectPrivilegeEntity objectPrivilege = itr.next();
				ObjectPrivilegeInfo info = getObjectPrivilegeInfoBean(objectPrivilege);
				list.add(info);
			}
		}
		return list;
	}

	/**
	 * ObjectPrivilegeEntityからObjectPrivilegeInfoへ変換
	 */
	private static ObjectPrivilegeInfo getObjectPrivilegeInfoBean(ObjectPrivilegeEntity entity) {
		ObjectPrivilegeInfo info = new ObjectPrivilegeInfo();
		info.setObjectType(entity.getId().getObjectType());
		info.setObjectId(entity.getId().getObjectId());
		info.setRoleId(entity.getId().getRoleId());
		info.setObjectPrivilege(entity.getId().getObjectPrivilege());
		info.setCreateUserId(entity.getCreateUserId());
		if (entity.getCreateDatetime() != null) {
			info.setCreateDate(entity.getCreateDatetime().getTime());
		}
		info.setModifyUserId(entity.getModifyUserId());
		if (entity.getModifyDatetime() != null) {
			info.setModifyDate(entity.getModifyDatetime().getTime());
		}
		return info;
	}
}
