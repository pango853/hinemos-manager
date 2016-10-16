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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.bean.UserInfo;
import com.clustercontrol.accesscontrol.bean.UserTypeConstant;
import com.clustercontrol.accesscontrol.model.RoleEntity;
import com.clustercontrol.accesscontrol.model.UserEntity;
import com.clustercontrol.accesscontrol.util.QueryUtil;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.UnEditableUser;
import com.clustercontrol.fault.UsedUser;
import com.clustercontrol.fault.UserDuplicate;
import com.clustercontrol.fault.UserNotFound;

/**
 * ユーザ情報を更新するファクトリクラス<BR>
 *
 * @version 1.0.0
 * @since 3.2.0
 */
public class LoginUserModifier {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(LoginUserModifier.class);

	/**
	 * ログインユーザを追加する。<BR>
	 * 
	 * @param info 追加するユーザ情報
	 * @param modifyUserId 作業ユーザID
	 * @return
	 * @throws UserDuplicate
	 * @throws HinemosUnknown
	 */
	public static void addUserInfo(UserInfo info, String modifyUserId) throws UserDuplicate, HinemosUnknown {

		JpaTransactionManager jtm = new JpaTransactionManager();
		if(info != null && modifyUserId != null && modifyUserId.compareTo("") != 0){
			String userId = null;
			try {
				// ユーザID
				userId = info.getId();

				// 現在日時を取得
				Timestamp now = new Timestamp(new Date().getTime());

				// ユーザインスタンスの作成
				UserEntity entity = new UserEntity(userId);
				// 重複チェック
				jtm.checkEntityExists(UserEntity.class, entity.getUserId());
				entity.setUserName(info.getName());
				entity.setUserType(UserTypeConstant.LOGIN_USER);	// ユーザ種別の格納
				entity.setDescription(info.getDescription());
				entity.setCreateUserId(modifyUserId);
				entity.setCreateDatetime(now);
				entity.setModifyUserId(modifyUserId);
				entity.setModifyDatetime(now);

				// ALL_USERSロールを設定
				RoleEntity roleEntity = QueryUtil.getRolePK(RoleIdConstant.ALL_USERS);
				roleEntity.getUserEntities().add(entity);
				entity.setRoleEntities(new ArrayList<RoleEntity>());
				entity.getRoleEntities().add(roleEntity);

			} catch (EntityExistsException e) {
				m_log.info("addUserInfo() failure to add a user. a user'id is duplicated. (userId = " + userId + ")");
				throw new UserDuplicate(e.getMessage(), e);
			} catch (Exception e) {
				m_log.warn("addUserInfo() failure to add a user. (userId = " + userId + ")", e);
				throw new HinemosUnknown("failure to add a user. (userId = " + userId + ")", e);
			}
		}
	}

	/**
	 * ログインユーザを削除する。<BR>
	 * 
	 * @param userId 削除対象のユーザID
	 * @param modifyUserId 作業ユーザID
	 * @throws UserNotFound
	 * @throws UnEditableUser
	 * @throws UsedUser
	 * @throws HinemosUnknown
	 */
	public static void deleteUserInfo(String userId, String modifyUserId) throws UserNotFound, UsedUser, UnEditableUser, HinemosUnknown {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		if(userId != null && userId.compareTo("") != 0 && modifyUserId != null && modifyUserId.compareTo("") != 0){
			try {
				// 作業ユーザと削除対象のユーザが一致している場合、削除不可とする
				if (userId.compareTo(modifyUserId) == 0) {
					throw new UsedUser("a user will be deleted is equal to current login user.");
				}

				// 該当するユーザを検索して取得
				UserEntity user = QueryUtil.getUserPK(userId);
				// システムユーザ、内部モジュールユーザは削除不可
				if (user != null && !user.getUserType().equals(UserTypeConstant.LOGIN_USER)) {
					throw new UnEditableUser();
				}
				// ユーザを削除する（DELETE CASCADEによりユーザ権限も削除される）
				user.unchain();		// 削除前処理
				em.remove(user);

			} catch (UserNotFound e) {
				throw e;
			} catch (UnEditableUser e) {
				throw e;
			} catch (UsedUser e) {
				m_log.info("deleteUserInfo() failure to delete a user. (userId = " + userId + ") : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			} catch (Exception e) {
				m_log.warn("deleteUserInfo() failure to delete a user. (userId = " + userId + ")", e);
				throw new HinemosUnknown(e.getMessage(), e);
			}

			m_log.info("successful in deleting a user. (userId = " + userId + ")");
		}
	}

	/**
	 * ログインユーザを変更する。<BR>
	 * 
	 * @param info 変更するユーザ情報
	 * @param modifyUserId 作業ユーザID
	 * @throws UserNotFound
	 * @throws UnEditableUser
	 * @throws HinemosUnknown
	 */
	public static void modifyUserInfo(UserInfo info, String modifyUserId) throws UserNotFound, UnEditableUser, HinemosUnknown {

		if(info != null && modifyUserId != null && modifyUserId.compareTo("") != 0){
			String userId = null;
			try {
				// ユーザID
				userId = info.getId();

				// 現在日時を取得
				Timestamp now = new Timestamp(new Date().getTime());

				// ユーザインスタンスの取得
				UserEntity entity = QueryUtil.getUserPK(userId);

				// システムユーザ、内部モジュールユーザは変更不可
				if (entity != null && !entity.getUserType().equals(UserTypeConstant.LOGIN_USER)) {
					throw new UnEditableUser();
				}

				entity.setUserName(info.getName());
				entity.setDescription(info.getDescription());
				entity.setModifyUserId(modifyUserId);
				entity.setModifyDatetime(now);

			} catch (UserNotFound e) {
				throw e;
			} catch (UnEditableUser e) {
				throw e;
			} catch (Exception e) {
				m_log.warn("modifyUserInfo() failure to modify a user. (userId = " + userId + ")", e);
				throw new HinemosUnknown(e.getMessage(), e);
			}
			m_log.info("successful in modifing a user. (userId = " + userId + ")");
		}
	}


	/**
	 * ログインユーザに設定されたパスワードを変更する。<BR>
	 * 
	 * @param userId ユーザID
	 * @param password 新しいパスワード文字列
	 * @throws UserNotFound
	 * @throws HinemosUnknown
	 */
	public static void modifyUserPassword(String userId, String password) throws UserNotFound, HinemosUnknown {

		if(userId != null && userId.compareTo("") != 0 && password != null && password.compareTo("") != 0){
			// 該当するユーザを検索して取得
			UserEntity user;
			try {
				user = QueryUtil.getUserPK(userId);
				// パスワードを反映する
				user.setPassword(password);

			} catch (UserNotFound e) {
				throw e;
			} catch (Exception e) {
				m_log.warn("modifyUserPassword() failure to modify user's password. (userId = " + userId + ")", e);
				throw new HinemosUnknown(e.getMessage(), e);
			}

			m_log.info("successful in modifing a user's password. (userId = " + userId + ")");
		}
	}

}
