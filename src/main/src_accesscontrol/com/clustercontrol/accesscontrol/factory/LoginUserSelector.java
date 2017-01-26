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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.codec.binary.Base64;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.accesscontrol.bean.SystemPrivilegeInfo;
import com.clustercontrol.accesscontrol.bean.UserInfo;
import com.clustercontrol.accesscontrol.model.UserEntity;
import com.clustercontrol.accesscontrol.util.QueryUtil;
import com.clustercontrol.accesscontrol.util.UserRoleCache;

/**
 * ユーザ情報を検索するファクトリクラス<BR>
 *
 * @version 1.0.0
 * @since 3.2.0
 */
public class LoginUserSelector {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(LoginUserSelector.class);


	/**
	 * 指定のユーザ情報を取得する。<BR>
	 * 
	 * @return ユーザ情報
	 * @throws HinemosUnknown
	 */
	public static UserInfo getUserInfo(String userId) throws HinemosUnknown {

		UserInfo info = null;

		if(userId != null && userId.compareTo("") != 0){
			try {
				// ユーザを取得
				UserEntity entity = QueryUtil.getUserPK(userId);
				info = getUserInfoBean(entity);
			} catch (UserNotFound e) {
				// 何もしない
			} catch (Exception e) {
				m_log.warn("getUserInfo() failure to get user. : userId = " + userId, e);
				throw new HinemosUnknown(e.getMessage() + " : failure to get user.", e);
			}
		}
		return info;
	}

	/**
	 * ユーザ名とパスワードからUserInfoを取得する。
	 * @param userid
	 * @param password
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public static void getUserInfoByPassword(String userId, String password, ArrayList<SystemPrivilegeInfo> systemPrivilegeList)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {

		UserEntity entity = null;
		boolean invalidUserFlag = true;

		try {
			entity = QueryUtil.getUserPK(userId);
		} catch (UserNotFound e) {
			// なにもしない
		} catch (Exception e) {
			m_log.warn("getUserInfoByPassword() " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		if (entity != null && entity.getPassword().equals(hash(password))) {
			invalidUserFlag = false;
		}
		if (invalidUserFlag) {
			String message = "user(" + userId + ")/password is invalid combination";
			m_log.info("getUserInfoByPassword() " + message);
			throw new InvalidUserPass(message);
		}

		// ユーザが保持するシステム権限情報を取得する
		for (SystemPrivilegeInfo systemPrivilegeInfo : systemPrivilegeList) {
			if (!UserRoleCache.isSystemPrivilege(userId, systemPrivilegeInfo)) {
				String message = "need-role " + list2String(systemPrivilegeList);
				m_log.info("getUserInfoByPassword() " + message);
				throw new InvalidRole(message);
			}
		}
	}

	private static String hash(String password) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			m_log.info("hash() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			return null;
		}
		return Base64.encodeBase64String(md.digest(password.getBytes()));
	}

	/**
	 * 処理速度を測定するためのサンプルスクリプト
	 * @param args
	 */
	public static void main (String args[]) {
		int maxN = 1000000;
		String str = null;
		System.out.println("start (" + maxN + " loops) : " + new Date());
		for (int i = 0; i < maxN; i ++ ) {
			str = hash("HINEMOS_AGENT");
		}
		System.out.println("hash : " + str);
		System.out.println("end   (" + maxN + " loops) : " + new Date());
	}

	/**
	 * リストを文字列に変換する関数。
	 * debug用。
	 * @param list
	 * @return
	 */
	private static String list2String (Collection<SystemPrivilegeInfo> list) {
		String ret = "";
		if (list == null) {
			return ret;
		}
		for (SystemPrivilegeInfo s : list) {
			ret += (s.getSystemFunction() + s.getSystemPrivilege());
		}
		return ret;
	}

	/**
	 * ユーザ一覧情報を取得する。<BR>
	 * 
	 * @return ユーザ情報のリスト
	 * @throws UserNotFound
	 * @throws HinemosUnknown
	 */
	public static ArrayList<UserInfo> getUserInfoList() {

		ArrayList<UserInfo> list = new ArrayList<UserInfo>();
		List<UserEntity> users = null;

		m_log.debug("getting all user...");

		// 全ユーザを取得
		users = QueryUtil.getAllShowUser();

		if(users != null && users.size() > 0){
			Iterator<UserEntity> itr = users.iterator();
			while(itr.hasNext()){
				UserEntity cal = itr.next();
				UserInfo info = getUserInfoBean(cal);
				list.add(info);
			}
		}

		m_log.debug("successful in getting all user.");
		return list;
	}

	/**
	 * ユーザIDに付与されたユーザ名を取得する。<BR>
	 * 
	 * @param userId ユーザID
	 * @return ユーザ名
	 * @throws HinemosUnknown
	 * @throws UserNotFound
	 */
	public static String getUserName(String userId) throws HinemosUnknown, UserNotFound {

		UserEntity user = null;

		if(userId != null && userId.compareTo("") != 0){
			// 該当するユーザを取得
			try {
				user = QueryUtil.getUserPK(userId);
			} catch (UserNotFound e) {
				throw e;
			} catch (Exception e) {
				m_log.warn("getUserName() failure to get a user's name. : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw new HinemosUnknown(e.getMessage(), e);
			}
		}
		return user.getUserName();
	}

	/**
	 * UserEntityからUserInfoBeanへ変換
	 */
	public static UserInfo getUserInfoBean(UserEntity entity) {
		UserInfo info = new UserInfo();
		info.setId(entity.getUserId());
		info.setName(entity.getUserName());
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
}
