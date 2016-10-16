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
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.bean.RoleInfo;
import com.clustercontrol.accesscontrol.bean.RoleTypeConstant;
import com.clustercontrol.accesscontrol.bean.SystemPrivilegeInfo;
import com.clustercontrol.accesscontrol.bean.UserTypeConstant;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeEntity;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeEntityPK;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetEntity;
import com.clustercontrol.accesscontrol.model.RoleEntity;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeEntity;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeEntityPK;
import com.clustercontrol.accesscontrol.model.UserEntity;
import com.clustercontrol.accesscontrol.util.ObjectPrivilegeUtil;
import com.clustercontrol.accesscontrol.util.QueryUtil;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.calendar.model.CalInfoEntity;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.PrivilegeDuplicate;
import com.clustercontrol.fault.PrivilegeNotFound;
import com.clustercontrol.fault.RoleDuplicate;
import com.clustercontrol.fault.RoleNotFound;
import com.clustercontrol.fault.UnEditableRole;
import com.clustercontrol.fault.UsedObjectPrivilege;
import com.clustercontrol.fault.UsedRole;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.jobmanagement.model.JobFileCheckEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobScheduleEntity;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.notify.model.NotifyInfoEntity;

/**
 * ロール情報を更新するファクトリクラス<BR>
 *
 * @version 1.0.0
 * @since 3.2.0
 */
public class RoleModifier {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(RoleModifier.class);

	/**
	 * ロールを追加する。<BR>
	 * 
	 * @param info 追加するロール情報
	 * @param modifyUserId 作業ユーザID
	 * @return
	 * @throws RoleDuplicate
	 * @throws HinemosUnknown
	 */
	public static void addRoleInfo(RoleInfo info, String modifyUserId) throws RoleDuplicate, HinemosUnknown {

		JpaTransactionManager jtm = new JpaTransactionManager();

		if(info != null && modifyUserId != null && modifyUserId.compareTo("") != 0){
			String roleId = null;
			try {
				// ロールID
				roleId = info.getId();

				// 現在日時を取得
				Timestamp now = new Timestamp(new Date().getTime());

				// 重複チェック
				jtm.checkEntityExists(RoleEntity.class, roleId);
				// インスタンスの作成
				RoleEntity entity = new RoleEntity(roleId);
				entity.setRoleName(info.getName());
				entity.setDescription(info.getDescription());
				entity.setCreateUserId(modifyUserId);
				entity.setCreateDatetime(now);
				entity.setModifyUserId(modifyUserId);
				entity.setModifyDatetime(now);
				entity.setRoleType(RoleTypeConstant.USER_ROLE);

			} catch (EntityExistsException e) {
				m_log.info("addRoleInfo() failure to add a role. a role'id is duplicated. (roleId = " + roleId + ")");
				throw new RoleDuplicate(e.getMessage(), e);
			} catch (Exception e) {
				m_log.warn("addRoleInfo() failure to add a role. (roleId = " + roleId + ")", e);
				throw new HinemosUnknown("failure to add a role. (roleId = " + roleId + ")", e);
			}
		}
	}

	/**
	 * ロールを削除する。<BR>
	 * 
	 * @param roleId 削除対象のロールID
	 * @param modifyUserId 作業ユーザID
	 * @throws RoleNotFound
	 * @throws UnEditableRole
	 * @throws UsedRole
	 * @throws HinemosUnknown
	 */
	public static void deleteRoleInfo(String roleId, String modifyUserId) throws RoleNotFound, UnEditableRole, UsedRole, HinemosUnknown {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		if(roleId != null && roleId.compareTo("") != 0 && modifyUserId != null && modifyUserId.compareTo("") != 0){
			try {
				// 該当するロールを検索して取得
				RoleEntity role = QueryUtil.getRolePK(roleId);
				// システムロール、内部モジュールロールは削除不可
				if (role != null && !role.getRoleType().equals(RoleTypeConstant.USER_ROLE)) {
					throw new UnEditableRole();
				}
				if (role.getUserEntities() != null && role.getUserEntities().size() > 0) {
					throw new UsedRole();
				}

				// ロールを削除する
				role.unchain();	// 削除前処理
				em.remove(role);

			} catch (RoleNotFound e) {
				throw e;
			} catch (UnEditableRole e) {
				throw e;
			} catch (UsedRole e) {
				throw e;
			} catch (Exception e) {
				m_log.warn("deleteRoleInfo() failure to delete a role. (roleId = " + roleId + ")", e);
				throw new HinemosUnknown(e.getMessage(), e);
			}

			m_log.info("successful in deleting a role. (roleId = " + roleId + ")");
		}
	}

	/**
	 * ロールを変更する。<BR>
	 * 
	 * @param info 変更するロール情報
	 * @param modifyUserId 作業ユーザID
	 * @throws RoleNotFound
	 * @throws UnEditableRole
	 * @throws HinemosUnknown
	 */
	public static void modifyRoleInfo(RoleInfo info, String modifyUserId) throws RoleNotFound, UnEditableRole, HinemosUnknown {

		if(info != null && modifyUserId != null && modifyUserId.compareTo("") != 0){
			String roleId = null;
			try {
				// ロールID
				roleId = info.getId();

				// 現在日時を取得
				Timestamp now = new Timestamp(new Date().getTime());

				// ユーザインスタンスの取得
				RoleEntity entity = QueryUtil.getRolePK(roleId);
				// システムロール、内部モジュールロールは変更不可
				if (entity != null && !entity.getRoleType().equals(RoleTypeConstant.USER_ROLE)) {
					throw new UnEditableRole();
				}

				entity.setRoleName(info.getName());
				entity.setDescription(info.getDescription());
				entity.setModifyUserId(modifyUserId);
				entity.setModifyDatetime(now);
				entity.setRoleType(RoleTypeConstant.USER_ROLE);

			} catch (RoleNotFound e) {
				throw e;
			} catch (UnEditableRole e) {
				throw e;
			} catch (Exception e) {
				m_log.warn("modifyRoleInfo() failure to modify a role. (roleId = " + roleId + ")", e);
				throw new HinemosUnknown(e.getMessage(), e);
			}
			m_log.info("successful in modifing a role. (roleId = " + roleId + ")");
		}
	}

	/**
	 * ロールにユーザを割り当てる。<BR>
	 * 
	 * @param roleId ロールID
	 * @param userIds ユーザID配列
	 * @throws UserNotFound
	 * @throws RoleNotFound
	 * @throws UnEditableRole
	 * @throws HinemosUnknown
	 */
	public static void assignUserToRole(String roleId, String[] userIds) throws UserNotFound, RoleNotFound, UnEditableRole, HinemosUnknown {

		/** ローカル変数 */
		RoleEntity roleEntity = null;

		/** メイン処理 */
		try {
			// ロール情報の取得
			roleEntity = QueryUtil.getRolePK(roleId);

			//システムロールである場合、システムユーザ（hinemos）が含まれてない場合は変更不可
			if (roleEntity != null && (roleEntity.getRoleType().equals(RoleTypeConstant.SYSTEM_ROLE))) {
				boolean existsFlg = false;
				for (String userId : userIds) {
					UserEntity userEntity = QueryUtil.getUserPK(userId);
					if(userEntity.getUserType().equals(UserTypeConstant.SYSTEM_USER)) {
						existsFlg = true;
						break;
					}
				}
				//システムユーザが存在しない場合
				if (!existsFlg) {
					throw new UnEditableRole();
				}
			}

			// ロールの修正
			// ロールに所属するユーザのリスト
			List<String> notDelPkList = Arrays.asList(userIds);
			List<String> delPkList = new ArrayList<String>();
			List<String> addPkList = new ArrayList<String>();
			List<UserEntity> userEntities = roleEntity.getUserEntities();
			for (String userId : userIds) {
				boolean existsFlg = false;
				if (roleEntity.getUserEntities() != null) {
					for (UserEntity entity : roleEntity.getUserEntities()) {
						if (entity.getUserId().equals(userId)) {
							existsFlg = true;
							break;
						}
					}
				}
				if (!existsFlg) {
					// ロールに登録されていない場合に登録
					addPkList.add(userId);
					UserEntity userEntity = QueryUtil.getUserPK(userId);
					userEntities.add(userEntity);
				}
			}
			// 対象以外削除
			Iterator<UserEntity> userIter = userEntities.iterator();
			while(userIter.hasNext()) {
				UserEntity entity = userIter.next();
				if (notDelPkList == null
						|| notDelPkList.size() == 0
						|| !notDelPkList.contains(entity.getUserId())) {
					delPkList.add(entity.getUserId());
					userIter.remove();
				}
			}
			roleEntity.setUserEntities(userEntities);


			// ユーザの修正
			// ユーザへのロールの追加
			for (String userId : addPkList) {
				UserEntity entity = QueryUtil.getUserPK(userId);
				boolean existsFlg = false;
				for (RoleEntity userRoleEntity : entity.getRoleEntities()) {
					if (userRoleEntity.getRoleId().equals(roleId)) {
						existsFlg = true;
						break;
					}
				}
				if (!existsFlg) {
					// ユーザに登録されていない場合に登録
					List<RoleEntity> list = entity.getRoleEntities();
					list.add(roleEntity);
					entity.setRoleEntities(list);
				}
			}
			// ユーザからのロールの削除
			if (delPkList != null && delPkList.size() > 0) {
				for (String userId : delPkList) {
					UserEntity entity = QueryUtil.getUserPK(userId);
					List<RoleEntity> list = entity.getRoleEntities();
					Iterator<RoleEntity> roleIter = list.iterator();
					while(roleIter.hasNext()) {
						RoleEntity userRoleEntity = roleIter.next();
						if (roleId.equals(userRoleEntity.getRoleId())) {
							roleIter.remove();
						}
					}
					entity.setRoleEntities(list);
				}
			}
		} catch (UserNotFound e) {
			throw e;
		} catch (RoleNotFound e) {
			throw e;
		} catch (UnEditableRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("modifyRoleInfo() failure to assign. (roleId = " + roleId + ")", e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * ロールにシステム権限を割り当てる。<BR>
	 * 
	 * @param roleId ロールID
	 * @param systemPrivileges システム権限配列
	 * @throws RoleNotFound
	 * @throws UnEditableRole
	 * @throws HinemosUnknown
	 */
	public static void replaceSystemPrivilegeToRole(String roleId, List<SystemPrivilegeInfo> systemPrivileges) throws RoleNotFound, UnEditableRole, HinemosUnknown {

		/** ローカル変数 */
		RoleEntity roleEntity = null;
		/** メイン処理 */
		try {
			// ロール情報の取得
			roleEntity = QueryUtil.getRolePK(roleId);

			// ADMINISTRATORSロールはシステム権限変更不可
			if (roleEntity != null && roleEntity.getRoleId().equals(RoleIdConstant.ADMINISTRATORS)) {
				throw new UnEditableRole();
			}

			List<SystemPrivilegeEntityPK> systemPrivilegeEntityPkList = new ArrayList<SystemPrivilegeEntityPK>();

			if (systemPrivileges != null && systemPrivileges.size() > 0) {
				for (SystemPrivilegeInfo systemPrivilegeInfo : systemPrivileges) {
					SystemPrivilegeEntityPK entityPk = new SystemPrivilegeEntityPK(
							roleId,
							systemPrivilegeInfo.getSystemFunction(),
							systemPrivilegeInfo.getSystemPrivilege());
					try {
						QueryUtil.getSystemPrivilegePK(entityPk);
					} catch (RoleNotFound e) {
						// 新規登録
						new SystemPrivilegeEntity(entityPk,
								roleEntity);
					}
					systemPrivilegeEntityPkList.add(entityPk);
				}
			}
			// 不要なシステム権限を削除
			roleEntity.deleteSystemPrivilegeEntities(systemPrivilegeEntityPkList);
		} catch (RoleNotFound e) {
			throw e;
		} catch (UnEditableRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("modifyRoleInfo() failure to assign. (roleId = " + roleId + ")", e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * オブジェクト種別、オブジェクトIDに紐づくオブジェクト権限情報を差し替える。<BR>
	 * 
	 * @param objectType オブジェクト種別
	 * @param objectId オブジェクトID
	 * @param list 差し替えるオブジェクト情報
	 * @param modifyUserId 作業ユーザID
	 * @throws PrivilegeDuplicate
	 * @throws HinemosUnknown
	 */
	public static void replaceObjectPrivilegeInfo(String objectType, String objectId, List<ObjectPrivilegeInfo> list, String modifyUserId)
			throws PrivilegeDuplicate, UsedObjectPrivilege, HinemosUnknown {

		JpaTransactionManager jtm = new JpaTransactionManager();
		HinemosEntityManager em = jtm.getEntityManager();

		try {
			// 現在日時を取得
			Timestamp now = new Timestamp(new Date().getTime());

			// オブジェクト種別、オブジェクトIDに該当する既存のオブジェクト権限を取得し、削除
			List<ObjectPrivilegeEntityPK> deleteList = new ArrayList<ObjectPrivilegeEntityPK>();
			List<ObjectPrivilegeEntity> oldList = QueryUtil.getAllObjectPrivilegeByFilter(
					objectType,
					objectId,
					null,
					null);
			if (oldList != null && oldList.size() > 0) {
				for (ObjectPrivilegeEntity oldEntity : oldList) {
					deleteList.add(oldEntity.getId());
				}
			}

			// 更新対象オブジェクトのオーナーロールIDを取得しておく
			ObjectPrivilegeTargetEntity objectPrivilegeTargetEntity
			= (ObjectPrivilegeTargetEntity)ObjectPrivilegeUtil.getObjectPrivilegeObject(objectType, objectId, ObjectPrivilegeMode.READ);
			if (objectPrivilegeTargetEntity == null) {
				m_log.warn("unknown object : objectType=" + objectType + ", objectId=" + objectId);
			}
			String ownerRoleId = objectPrivilegeTargetEntity.getOwnerRoleId();
			if(list != null
					&& list.size() > 0
					&& modifyUserId != null
					&& modifyUserId.compareTo("") != 0){

				// 登録・更新処理
				for (ObjectPrivilegeInfo info : list) {
					// オーナーロールIDが指定されている場合はスルーする。
					if (ownerRoleId.equals(info.getRoleId())) {
						continue;
					}
					// インスタンスの作成
					ObjectPrivilegeEntityPK entityPk = new ObjectPrivilegeEntityPK(
							objectType,
							objectId,
							info.getRoleId(),
							info.getObjectPrivilege());
					ObjectPrivilegeEntity entity = null;
					try {
						entity = QueryUtil.getObjectPrivilegePK(entityPk);
						// 更新する場合、削除対象から除く。
						deleteList.remove(entityPk);
					} catch (PrivilegeNotFound e) {
						// 新規作成
						entity = new ObjectPrivilegeEntity(entityPk);
					}
					entity.setCreateUserId(modifyUserId);
					entity.setCreateDatetime(now);
					entity.setModifyUserId(modifyUserId);
					entity.setModifyDatetime(now);
				}
			}

			// 削除処理
			if (deleteList != null && deleteList.size() > 0) {
				List<? extends ObjectPrivilegeTargetEntity> referList = null;
				String referObjectType = null;
				for (ObjectPrivilegeEntityPK deletePk : deleteList) {
					// READの場合、使用されているオブジェクト権限の場合はエラーとする
					if (deletePk.getObjectPrivilege().equals(ObjectPrivilegeMode.READ.name())) {
						if (HinemosModuleConstant.PLATFORM_REPOSITORY.equals(objectType)) {
							/*
							 *  リポジトリ関連（スコープ、ノード）の場合
							 *  (参照権限の継承されている場合、ノードが指定されている場合はチェック不可)
							 */
							// 監視設定
							referList = em.createNamedQuery("MonitorInfoEntity.findByFacilityIdOwnerRoleId", MonitorInfoEntity.class)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.MONITOR;
								break;
							}

							// ジョブ
							referList = em.createNamedQuery("JobMstEntity.findByFacilityIdOwnerRoleId", JobMstEntity.class)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.JOB;
								break;
							}

							// ファイルチェック
							referList = em.createNamedQuery("JobFileCheckEntity.findByFacilityIdOwnerRoleId", JobFileCheckEntity.class)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.JOB_FILE_CHECK;
								break;
							}

							// 通知（ログエスカレーション）
							referList = em.createNamedQuery("NotifyInfoEntity.findByEscalateFacilityIdOwnerRoleId", NotifyInfoEntity.class)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.PLATFORM_NOTIFY;
								break;
							}

							// 通知（ジョブ）
							referList = em.createNamedQuery("NotifyInfoEntity.findByExecFacilityIdOwnerRoleId", NotifyInfoEntity.class)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.PLATFORM_NOTIFY;
								break;
							}
						} else if (HinemosModuleConstant.JOB.equals(objectType)) {
							/*
							 * ジョブの場合
							 */
							// ファイルチェック
							referList = em.createNamedQuery("JobFileCheckEntity.findByJobUnitIdOwnerRoleId", JobFileCheckEntity.class)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.JOB_FILE_CHECK;
								break;
							}

							// スケジュール
							referList = em.createNamedQuery("JobScheduleEntity.findByJobUnitIdOwnerRoleId", JobScheduleEntity.class)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.JOB_SCHEDULE;
								break;
							}

							// 通知（ジョブ）
							referList = em.createNamedQuery("NotifyInfoEntity.findByJobUnitIdOwnerRoleId", NotifyInfoEntity.class)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.PLATFORM_NOTIFY;
								break;
							}

						} else if (HinemosModuleConstant.PLATFORM_CALENDAR.equals(objectType)) {
							/*
							 *  カレンダの場合
							 */
							// 監視設定
							referList = em.createNamedQuery("MonitorInfoEntity.findByCalendarIdOwnerRoleId", MonitorInfoEntity.class)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.MONITOR;
								break;
							}

							// ジョブ
							referList = em.createNamedQuery("JobMstEntity.findByCalendarIdOwnerRoleId", JobMstEntity.class)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.JOB;
								break;
							}

							// ファイルチェック
							referList = em.createNamedQuery("JobFileCheckEntity.findByCalendarIdOwnerRoleId", JobFileCheckEntity.class)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.JOB_FILE_CHECK;
								break;
							}

							// スケジュール
							referList = em.createNamedQuery("JobScheduleEntity.findByCalendarIdOwnerRoleId", JobScheduleEntity.class)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.JOB_SCHEDULE;
								break;
							}


						} else if (HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN.equals(objectType)) {
							/*
							 * カレンダパターンの場合
							 */
							// カレンダ
							referList = em.createNamedQuery("CalInfoEntity.findByCalendarPatternIdOwnerRoleId", CalInfoEntity.class)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.PLATFORM_CALENDAR;
								break;
							}

						} else if (HinemosModuleConstant.PLATFORM_NOTIFY.equals(objectType)) {
							/*
							 *  通知の場合
							 */
							// 監視設定
							referList = em.createNamedQuery("MonitorInfoEntity.findByNotifyIdOwnerRoleId", MonitorInfoEntity.class)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.MONITOR;
								break;
							}

							// ジョブ
							referList = em.createNamedQuery("JobMstEntity.findByNotifyIdOwnerRoleId", JobMstEntity.class)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.JOB;
								break;
							}

						} else if (HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE.equals(objectType)) {
							/*
							 *  メールテンプレート
							 */
							// メール通知
							referList = em.createNamedQuery("NotifyInfoEntity.findByMailTemplateIdOwnerRoleId", NotifyInfoEntity.class)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							referObjectType = HinemosModuleConstant.PLATFORM_NOTIFY;
						}
					}
					ObjectPrivilegeEntity deleteEntity = null;
					try {
						deleteEntity = QueryUtil.getObjectPrivilegePK(deletePk);
						em.remove(deleteEntity);
					} catch (PrivilegeNotFound e) {
						// データが存在しない場合は特に処理しない。
						m_log.debug("ObjectPrivilegeEntity entity is not found.");
					}
				}
				if (referList != null && referList.size() > 0) {
					UsedObjectPrivilege e = new UsedObjectPrivilege(referObjectType, referList.get(0).getObjectId());
					m_log.warn("replaceObjectPrivilegeInfo() : "
							+ "objectType = " + e.getObjectType()
							+ ", objectId = " + e.getObjectId()
							+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw e;
				}
			}

		} catch (UsedObjectPrivilege e) {
			m_log.debug("replaceObjectPrivilegeInfo() failure to add a entity. " + e.getMessage());
			throw e;
		} catch (EntityExistsException e) {
			m_log.debug("replaceObjectPrivilegeInfo() failure to add a entity. " + e.getMessage());
			throw new PrivilegeDuplicate(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("replaceObjectPrivilegeInfo() failure to add a entity. " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		m_log.info("successful in modifing a entity.");
	}

}
