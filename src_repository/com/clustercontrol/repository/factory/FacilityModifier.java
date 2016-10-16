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

package com.clustercontrol.repository.factory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.Ipv6Util;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.ObjectValidator;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityInfo;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.bean.NodeCpuInfo;
import com.clustercontrol.repository.bean.NodeDeviceInfo;
import com.clustercontrol.repository.bean.NodeDiskInfo;
import com.clustercontrol.repository.bean.NodeFilesystemInfo;
import com.clustercontrol.repository.bean.NodeHostnameInfo;
import com.clustercontrol.repository.bean.NodeInfo;
import com.clustercontrol.repository.bean.NodeMemoryInfo;
import com.clustercontrol.repository.bean.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.bean.NodeNoteInfo;
import com.clustercontrol.repository.bean.NodeVariableInfo;
import com.clustercontrol.repository.bean.ScopeInfo;
import com.clustercontrol.repository.entity.CollectorPlatformMstData;
import com.clustercontrol.repository.entity.CollectorSubPlatformMstData;
import com.clustercontrol.repository.model.CollectorPlatformMstEntity;
import com.clustercontrol.repository.model.CollectorSubPlatformMstEntity;
import com.clustercontrol.repository.model.FacilityEntity;
import com.clustercontrol.repository.model.FacilityRelationEntity;
import com.clustercontrol.repository.model.NodeCpuEntity;
import com.clustercontrol.repository.model.NodeCpuEntityPK;
import com.clustercontrol.repository.model.NodeDeviceEntity;
import com.clustercontrol.repository.model.NodeDeviceEntityPK;
import com.clustercontrol.repository.model.NodeDiskEntity;
import com.clustercontrol.repository.model.NodeDiskEntityPK;
import com.clustercontrol.repository.model.NodeEntity;
import com.clustercontrol.repository.model.NodeFilesystemEntity;
import com.clustercontrol.repository.model.NodeFilesystemEntityPK;
import com.clustercontrol.repository.model.NodeHostnameEntity;
import com.clustercontrol.repository.model.NodeHostnameEntityPK;
import com.clustercontrol.repository.model.NodeMemoryEntity;
import com.clustercontrol.repository.model.NodeMemoryEntityPK;
import com.clustercontrol.repository.model.NodeNetworkInterfaceEntity;
import com.clustercontrol.repository.model.NodeNetworkInterfaceEntityPK;
import com.clustercontrol.repository.model.NodeNoteEntity;
import com.clustercontrol.repository.model.NodeNoteEntityPK;
import com.clustercontrol.repository.model.NodeVariableEntity;
import com.clustercontrol.repository.model.NodeVariableEntityPK;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.repository.util.FacilityUtil;
import com.clustercontrol.repository.util.QueryUtil;

/**
 * ファシリティの更新処理を実装したクラス<BR>
 */
public class FacilityModifier {

	private static Log m_log = LogFactory.getLog(FacilityModifier.class);

	/** プラットフォーム定義情報を登録する。<BR>
	 *
	 * @param CollectorPlatformMstData プラットフォーム定義情報
	 * @throws CollectorPlatformMstData
	 * @throws EntityExistsException
	 */
	public static void addCollectorPratformMst(CollectorPlatformMstData data) throws EntityExistsException {
		JpaTransactionManager jtm = new JpaTransactionManager();

		try {
			// インスタンス生成
			CollectorPlatformMstEntity entity = new CollectorPlatformMstEntity(data.getPlatformId());
			// 重複チェック
			jtm.checkEntityExists(CollectorPlatformMstEntity.class, entity.getPlatformId());
			entity.setOrderNo(data.getOrderNo().intValue());
			entity.setPlatformName(data.getPlatformName());
		} catch (EntityExistsException e) {
			m_log.info("addCollectorPratformMst() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * 指定されたプラットフォームIDのプラットフォーム定義情報を削除する。<BR>
	 *
	 * @param platformId プラットフォームID
	 * @throws FacilityNotFound
	 */
	public static void deleteCollectorPratformMst(String platformId) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		// 該当するプラットフォーム定義情報を取得
		CollectorPlatformMstEntity entity = QueryUtil.getCollectorPlatformMstPK(platformId);
		// 削除
		em.remove(entity);
	}

	/** サブプラットフォーム定義情報を登録する。<BR>
	 *
	 * @param addCollectorSubPratformMst サブプラットフォーム定義情報
	 * @throws addCollectorSubPratformMst
	 * @throws EntityExistsException
	 */
	public static void addCollectorSubPratformMst(CollectorSubPlatformMstData data) throws EntityExistsException {
		JpaTransactionManager jtm = new JpaTransactionManager();

		try {
			// インスタンス生成
			CollectorSubPlatformMstEntity entity = new CollectorSubPlatformMstEntity(data.getSubPlatformId());
			// 重複チェック
			jtm.checkEntityExists(CollectorSubPlatformMstEntity.class, entity.getSubPlatformId());
			entity.setSubPlatformName(data.getSubPlatformName());
			entity.setType(data.getType());
			entity.setOrderNo(data.getOrderNo().intValue());
		} catch (EntityExistsException e) {
			m_log.info("addCollectorSubPratformMst() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * 指定されたサブプラットフォームIDのサブプラットフォーム定義情報を削除する。<BR>
	 *
	 * @param subPlatformId サブプラットフォームID
	 * @throws FacilityNotFound
	 */
	public static void deleteCollectorSubPratformMst(String subPlatformId) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		// 該当するプラットフォーム定義情報を取得
		CollectorSubPlatformMstEntity entity = QueryUtil.getCollectorSubPlatformMstPK(subPlatformId);
		// 削除
		em.remove(entity);
	}

	/** スコープを追加する。<BR>
	 *
	 * @param parentFacilityId スコープを配置する親スコープのファシリティID（空文字の場合はルートスコープとなる）
	 * @param property 追加するスコープ情報
	 * @param modifyUserId 作業ユーザID
	 * @param displaySortOrder 表示ソート順位
	 * @param topicSendFlg 更新を周知する場合はtrue, 周知しない場合はfalse
	 * @throws FacilityNotFound
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 */
	public static void addScope(String parentFacilityId, ScopeInfo property, String modifyUserId, int displaySortOrder, boolean topicSendFlg)
			throws FacilityNotFound, EntityExistsException, HinemosUnknown {

		JpaTransactionManager jtm = new JpaTransactionManager();

		/** ローカル変数 */
		FacilityEntity parentFacility = null;
		FacilityEntity facility = null;
		String facilityId = null;

		/** メイン処理 */
		m_log.debug("adding a scope...");

		try {
			// 入力値（ファシリティID）の格納
			facilityId = property.getFacilityId();

			// 親ファシリティがスコープかどうかを確認する
			if (! ObjectValidator.isEmptyString(parentFacilityId)) {
				parentFacility = QueryUtil.getFacilityPK_NONE(parentFacilityId);
				if (!FacilityUtil.isScope(parentFacility)) {
					HinemosUnknown e = new HinemosUnknown("a parent's facility is not a scope. (parentFacilityId = "
							+ parentFacilityId + ", facilityId = " + facilityId + ")");
					m_log.info("addScope() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}

			// ファシリティインスタンスの生成
			facility = new FacilityEntity(facilityId);
			// 重複チェック
			jtm.checkEntityExists(FacilityEntity.class, facility.getFacilityId());
			facility.setDisplaySortOrder(displaySortOrder);
			facility.setFacilityType(FacilityConstant.TYPE_SCOPE);
			facility.setValid(ValidConstant.TYPE_VALID);
			setFacilityEntityProperties(facility, property, modifyUserId, false);

			// ファシリティ関連インスタンスの生成
			if (! ObjectValidator.isEmptyString(parentFacilityId)) {
				assignFacilityToScope(parentFacilityId, facilityId);
			}
		} catch (FacilityNotFound e) {
			throw e;
		} catch (EntityExistsException e) {
			m_log.info("addScope() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		}

		m_log.info("addScope() successful in adding a scope . (parentFacilityId = " + parentFacilityId + ", facilityId = " + facilityId + ")");
	}

	/**
	 * オーナーロールスコープを変更する。<BR>
	 * 
	 * @param property 変更後のスコープ情報
	 * @param modifyUserId 作業ユーザID
	 * @param topicSendFlg 更新を周知する場合はtrue, 周知しない場合はfalse
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 */
	public static void modifyOwnerRoleScope(ScopeInfo property, String modifyUserId, boolean topicSendFlg)
			throws FacilityNotFound, InvalidRole {

		/** ローカル変数 */
		FacilityEntity facility = null;
		String facilityId = null;

		/** メイン処理 */
		m_log.debug("modifing an owner role scope...");

		// 入力値（ファシリティID）の格納
		facilityId = property.getFacilityId();

		// ファシリティインスタンスを取得（オブジェクト権限のチェックなし）
		facility = QueryUtil.getFacilityPK_NONE(facilityId);

		// ファシリティがスコープかどうかを確認する
		if (!FacilityUtil.isScope(facility)) {
			FacilityNotFound e = new FacilityNotFound("this facility is not a scope. (facilityId = " + facilityId + ")");
			m_log.info("modifyOwnerRoleScope() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		// 変更後の値を格納する
		setFacilityEntityProperties(facility, property, modifyUserId, false);
		m_log.info("modifyOwnerRoleScope() successful in modifing a scope. (facilityId = " + facilityId + ")");
	}

	/**
	 * スコープを変更する。<BR>
	 *
	 * @param property 変更後のスコープ情報
	 * @param modifyUserId 作業ユーザID
	 * @param topicSendFlg 更新を周知する場合はtrue, 周知しない場合はfalse
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 */
	public static void modifyScope(ScopeInfo property, String modifyUserId, boolean topicSendFlg) throws FacilityNotFound, InvalidRole {

		/** ローカル変数 */
		FacilityEntity facility = null;
		String facilityId = null;

		/** メイン処理 */
		m_log.debug("modifing a scope...");

		// 入力値（ファシリティID）の格納
		facilityId = property.getFacilityId();

		// ファシリティインスタンスを取得
		facility = QueryUtil.getFacilityPK(facilityId, ObjectPrivilegeMode.MODIFY);
		
		// ファシリティがスコープかどうかを確認する
		if (!FacilityUtil.isScope(facility)) {
			FacilityNotFound e = new FacilityNotFound("this facility is not a scope. (facilityId = " + facilityId + ")");
			m_log.info("modifyScope() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// 変更後の値を格納する
		setFacilityEntityProperties(facility, property, modifyUserId, false);

		m_log.info("modifyScope() successful in modifing a scope. (facilityId = " + facilityId + ")");
	}

	/**
	 * オーナーロールスコープを削除する。<BR>
	 *
	 * @param facilityId 削除するスコープのファシリティID
	 * @param modifyUserId 作業ユーザID
	 * @param topicSendFlg 更新を周知する場合はtrue, 周知しない場合はfalse
	 * @throws UsedFacility
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public static void deleteOwnerRoleScope(String facilityId, String modifyUserId, boolean topicSendFlg) throws UsedFacility, FacilityNotFound, InvalidRole, HinemosUnknown {

		/** メイン処理 */
		m_log.debug("deleting a owner role scope with sub scopes...");


		// 該当するファシリティインスタンスを取得（オブジェクト権限チェックなし）
		FacilityEntity facility = QueryUtil.getFacilityPK_NONE(facilityId);

		// 関連インスタンス、ファシリティインスタンスを削除する
		deleteScopeRecursive(facility);

		m_log.info("deleteScope() successful in deleting a owner role scope with sub scopes. (facilityId = " + facilityId + ")");
	}

	/**
	 * スコープを削除する。<BR>
	 *
	 * @param facilityId 削除するスコープのファシリティID
	 * @param modifyUserId 作業ユーザID
	 * @param topicSendFlg 更新を周知する場合はtrue, 周知しない場合はfalse
	 * @throws UsedFacility
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public static void deleteScope(String facilityId, String modifyUserId, boolean topicSendFlg) throws UsedFacility, FacilityNotFound, InvalidRole, HinemosUnknown {

		/** メイン処理 */
		m_log.debug("deleting a scope with sub scopes...");

		// 該当するファシリティインスタンスを取得
		FacilityEntity facility = QueryUtil.getFacilityPK(facilityId, ObjectPrivilegeMode.MODIFY);

		// 関連インスタンス、ファシリティインスタンスを削除する
		deleteScopeRecursive(facility);

		m_log.info("deleteScope() successful in deleting a scope with sub scopes. (facilityId = " + facilityId + ")");
	}

	/**
	 * サブスコープを含めて、スコープを削除する。<BR>
	 *
	 * @param scope 削除するスコープインスタンス
	 * @throws UsedFacility
	 * @throws HinemosUnknown
	 */
	private static void deleteScopeRecursive(FacilityEntity scope) throws UsedFacility, HinemosUnknown, FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		/** ローカル変数 */
		String facilityId = null;

		/** メイン処理 */
		facilityId = scope.getFacilityId();

		// スコープでない場合はエラーとする
		if (!FacilityUtil.isScope(scope)) {
			HinemosUnknown e = new HinemosUnknown("this facility is not a scope. (facilityId = " + facilityId + ")");
			m_log.info("deleteScopeRecursive() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// 直下にスコープを存在する場合、そのスコープおよびサブスコープを削除する
		List<FacilityEntity> childEntities = QueryUtil.getChildFacilityEntity(scope.getFacilityId());
		if (childEntities != null && childEntities.size() > 0) {
			Iterator<FacilityEntity> iter = childEntities.iterator();
			while(iter.hasNext()) {
				FacilityEntity childEntity = iter.next();
				if (FacilityUtil.isScope(childEntity)) {
					childEntity.tranSetUncheckFlg(true);
					// リレーションを削除する
					FacilityRelationEntity facilityRelationEntity
					= QueryUtil.getFacilityRelationPk(scope.getFacilityId(), childEntity.getFacilityId());
					em.remove(facilityRelationEntity);
					deleteScopeRecursive(childEntity);
				}
			}
		}
		em.remove(scope);

		m_log.info("deleteScopeRecursive() successful in deleting a scope. (facilityId = " + facilityId + ")");
	}

	/**
	 * ノードを追加する。<BR>
	 *
	 * @param nodeInfo 追加するノード情報
	 * @param modifyUserId 作業ユーザID
	 * @param displaySortOrder 表示ソート順位
	 * @throws EntityExistsException
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	public static void addNode(NodeInfo nodeInfo, String modifyUserId, int displaySortOrder)
			throws EntityExistsException, FacilityNotFound, HinemosUnknown {
		m_log.debug("adding a node...");

		JpaTransactionManager jtm = new JpaTransactionManager();
		String facilityId = nodeInfo.getFacilityId();
		Boolean valid = nodeInfo.isValid();

		try {
			// インスタンス生成
			FacilityEntity facilityEntity = new FacilityEntity(facilityId);
			// 重複チェック
			jtm.checkEntityExists(FacilityEntity.class, facilityEntity.getFacilityId());
			facilityEntity.setFacilityType(FacilityConstant.TYPE_NODE);
			facilityEntity.setDisplaySortOrder(displaySortOrder);
			facilityEntity.setValid(FacilityUtil.getValid(valid));
			setFacilityEntityProperties(facilityEntity, nodeInfo, modifyUserId, false);

			// ノードインスタンスの生成
			NodeEntity nodeEntity = new NodeEntity(facilityEntity);
			// 重複チェック
			jtm.checkEntityExists(NodeEntity.class, nodeEntity.getFacilityId());
			setNodeEntityProperties(nodeEntity, nodeInfo, false);

			// ファシリティ関連インスタンスの生成
			long startTime = System.currentTimeMillis();
			assignFacilityToScope(FacilityTreeAttributeConstant.REGISTEREFD_SCOPE, facilityId);
			m_log.info(String.format("assignFacilityToScope FacilityTreeAttributeConstant.REGISTEREFD_SCOPE: %dms", System.currentTimeMillis() - startTime));

			// オーナー別スコープへの登録
			startTime = System.currentTimeMillis();
			assignFacilityToScope(facilityEntity.getOwnerRoleId(), facilityId);
			m_log.info(String.format("assignFacilityToScope facilityEntity.getOwnerRoleId(): %dms", System.currentTimeMillis() - startTime));

			// OS別スコープへの登録
			startTime = System.currentTimeMillis();
			String platformFamily = nodeEntity.getPlatformFamily();
			assignFacilityToScope(platformFamily, facilityId);
			m_log.info(String.format("assignFacilityToScope %s: %dms", platformFamily, System.currentTimeMillis() - startTime));
		} catch (FacilityNotFound e) {
			throw e;
		} catch (EntityExistsException e) {
			m_log.info("addNode() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		}

		m_log.info("addNode() successful in adding a node. (facilityId = " + facilityId + ")");
	}

	/**
	 * ノードを変更する。<BR>
	 *
	 * @param nodeInfo 変更後のノード情報
	 * @param modifyUserId 作業ユーザID
	 * @param topicSendFlg 更新を周知する場合はtrue, 周知しない場合はfalse
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public static void modifyNode(NodeInfo nodeInfo, String modifyUserId, boolean topicSendFlg)
			throws FacilityNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("modifing a node...");

		String facilityId = nodeInfo.getFacilityId();
		Boolean valid = nodeInfo.isValid();
		FacilityEntity facilityEntity = QueryUtil.getFacilityPK(facilityId, ObjectPrivilegeMode.MODIFY);
		if (valid == null) {
			HinemosUnknown e = new HinemosUnknown("node's valid is invalid . (valid = null)");
			m_log.info("modifyNode() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		} else {
			facilityEntity.setValid(FacilityUtil.getValid(valid));
		}

		// ファシリティがノードかどうかを確認する
		if (!FacilityUtil.isNode(facilityEntity)) {
			FacilityNotFound e = new FacilityNotFound("this facility is not a node. (facilityId = " + facilityId + ")");
			m_log.info("modifyNode() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		setFacilityEntityProperties(facilityEntity, nodeInfo, modifyUserId, false);

		// ノードインスタンスの取得
		NodeEntity nodeEntity = facilityEntity.getNodeEntity();

		// 変更情報の反映
		String oldPlatformFamily = nodeEntity.getPlatformFamily();
		setNodeEntityProperties(nodeEntity, nodeInfo, false);

		//OS別スコープの更新
		String platformFamily = nodeEntity.getPlatformFamily();
		if (!oldPlatformFamily.equals(platformFamily)) {//OSスコープが変わる場合
			//旧OSスコープから削除
			String[] facilityIds = {facilityId};
			releaseNodeFromScope(oldPlatformFamily, facilityIds, modifyUserId, topicSendFlg);

			//新OSスコープに割り当て
			assignFacilityToScope(platformFamily, facilityId);
		}

		m_log.info("modifyNode() successful in modifing a node. (facilityId = " + facilityId + ")");
	}

	/**
	 * ノードを削除する。<BR>
	 *
	 * @param facilityId 削除するノードのファシリティID
	 * @param modifyUserId 作業ユーザID
	 * @param topicSendFlg 更新を周知する場合はtrue, 周知しない場合はfalse
	 * @throws InvalidRole
	 * @throws FacilityNotFound
	 */
	public static void deleteNode(String facilityId, String modifyUserId, boolean topicSendFlg) throws FacilityNotFound, InvalidRole {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		m_log.debug("deleting a node...");

		// 該当するファシリティインスタンスを取得
		FacilityEntity facility = QueryUtil.getFacilityPK(facilityId, ObjectPrivilegeMode.MODIFY);

		// ノードでない場合はエラーとする
		if (!FacilityUtil.isNode(facility)) {
			FacilityNotFound e = new FacilityNotFound("this facility is not a node. (facilityId = " + facilityId + ")");
			m_log.info("deleteNode() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		em.remove(facility);

		m_log.info("deleteNode() successful in deleting a node. (facilityId = " + facilityId + ")");
	}

	/**
	 * スコープにノードを割り当てる。<BR>
	 *
	 * @param scopeFacilityId スコープのファシリティID
	 * @param facilityId ノードのファシリティID
	 * @throws FacilityNotFound
	 */
	private static void assignFacilityToScope(String scopeFacilityId, String facilityId) throws FacilityNotFound, HinemosUnknown {
		String[] facilityIds = { facilityId };
		assignFacilitiesToScope(scopeFacilityId, facilityIds);
	}

	/**
	 * スコープにノードを割り当てる。<BR>
	 *
	 * @param scopeFacilityId スコープのファシリティID
	 * @param facilityIds ノードのファシリティID配列
	 * @throws FacilityNotFound
	 */
	public static void assignFacilitiesToScope(String scopeFacilityId, String[] facilityIds) throws FacilityNotFound {
		m_log.debug("assigning facilities to a scope...");

		try {
			// スコープのファシリティインスタンスの取得
			FacilityEntity scope = QueryUtil.getFacilityPK_NONE(scopeFacilityId);
			scope.tranSetUncheckFlg(true);

			if (!FacilityUtil.isScope(scope)) {
				FacilityNotFound e = new FacilityNotFound("parent's facility is not a scope. (facilityId = " + scopeFacilityId + ")");
				m_log.info("assignFacilitiesToScope() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			for (String facilityId : facilityIds) {
				if (doesFacilityRelationEntityExist(scopeFacilityId, facilityId)) {
					m_log.info("assignFacilitiesToScope() skipped assinging a facility to a scope. (parentFacilityId = " + scopeFacilityId + ", facilityId = " + facilityId + ")");
				} else {
					new FacilityRelationEntity(scopeFacilityId, facilityId);
					m_log.info("assignFacilitiesToScope() successful in assinging a facility to a scope. (parentFacilityId = " + scopeFacilityId + ", facilityId = " + facilityId + ")");
				}
			}
		} catch (FacilityNotFound e) {
			throw e;
		}

		m_log.info("assignFacilitiesToScope() successful in assigning facilities to a scope.");
	}

	private static boolean doesFacilityRelationEntityExist(
			String parentFacilityId, String childFacilityId) {
		return FacilityTreeCache.getChildFacilityIdSet(parentFacilityId).contains(childFacilityId);
	}

	/**
	 * スコープからノードの割り当てを解除する。<BR>
	 *
	 * @param parentFacilityId スコープのファシリティID
	 * @param facilityIds ノードのファシリティID
	 * @param modifyUserId 作業ユーザID
	 * @param topicSendFlg 更新を周知する場合はtrue, 周知しない場合はfalse
	 *
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 */
	public static void releaseNodeFromScope(String parentFacilityId, String[] facilityIds, String modifyUserId, boolean topicSendFlg)
			throws FacilityNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		/** ローカル変数 */
		FacilityEntity facility = null;
		String facilityId = null;

		/** メイン処理 */
		m_log.debug("releasing nodes from a scope...");

		// 該当するファシリティインスタンスを取得
		facility = QueryUtil.getFacilityPK(parentFacilityId);

		if (!FacilityUtil.isScope(facility)) {
			FacilityNotFound e = new FacilityNotFound("parent's facility is not a scope. (parentFacilityId = " + parentFacilityId + ")");
			m_log.info("releaseNodeFromScope() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		for (int i = 0; i < facilityIds.length; i++) {
			facilityId = facilityIds[i];
			FacilityRelationEntity relation
			= QueryUtil.getFacilityRelationPk(parentFacilityId, facilityId);
			em.remove(relation);
			m_log.info("releaseNodeFromScope() successful in releaseing a node. (parentFacilityId = " + parentFacilityId + ", facilityId = " + facilityId + ")");
		}

		m_log.info("releaseNodeFromScope() successful in releasing nodes from a scope.");
	}

	/**
	 * facilityInfoインスタンスに格納された値をファシリティインスタンスに格納する。<BR>
	 *
	 * @param facilityEntity 格納先となるファシリティインスタンス
	 * @param facilityInfo 格納する情報
	 * @param modifyUserId 作業ユーザID
	 * @param skipIfEmptyFlg trueにすると、Propertyインスタンスの各格納値がnullあるいは空文字の場合に格納しない
	 * @throws FacilityNotFound
	 */
	private static void setFacilityEntityProperties(FacilityEntity facilityEntity, FacilityInfo facilityInfo, String modifyUserId, boolean skipIfEmptyFlg) throws FacilityNotFound {
		/** ローカル変数 */
		String facilityName = null;
		String description = null;
		Timestamp now = null;

		/** メイン処理 */
		// 現在日時を取得
		now = new Timestamp(new Date().getTime());

		if (FacilityUtil.isScope(facilityEntity)) {
			// 入力値（ファシリティ名）の格納
			facilityName = facilityInfo.getFacilityName();

			// 入力値（説明）の格納
			description = facilityInfo.getDescription();

		} else if (FacilityUtil.isNode(facilityEntity)) {
			// 入力値（ファシリティ名）の格納
			facilityName = facilityInfo.getFacilityName();

			// 入力値（説明）の格納
			description = facilityInfo.getDescription();
		} else {
			FacilityNotFound e = new FacilityNotFound("this facility's type is invalid. (facilityType = " + facilityEntity.getFacilityType() + ")");
			m_log.info("setFacilityEntityProperties() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// ファシリティインスタンスへの入力値の格納
		if ( ! (skipIfEmptyFlg && ObjectValidator.isEmptyString(facilityName)) ) {
			facilityEntity.setFacilityName(facilityName);
		}
		if ( ! (skipIfEmptyFlg && ObjectValidator.isEmptyString(description)) ) {
			facilityEntity.setDescription(description);
		}
		// アイコン名の格納
		facilityEntity.setIconImage(facilityInfo.getIconImage());
		// 入力値（オーナーロールID）の格納
		facilityEntity.setOwnerRoleId(facilityInfo.getOwnerRoleId());
		if (ObjectValidator.isEmptyString(facilityEntity.getCreateUserId())) {
			facilityEntity.setCreateUserId(modifyUserId);
			facilityEntity.setCreateDatetime(now);
		}
		facilityEntity.setModifyUserId(modifyUserId);
		facilityEntity.setModifyDatetime(now);
	}

	/**
	 * nodeInfoインスタンス上のノード情報をノードインスタンスに格納する。<BR>
	 *
	 * @param nodeEntity 格納先となるノードインスタンス
	 * @param nodeInfo ノード情報
	 * @param skipIfEmptyFlg trueにすると、Propertyインスタンスの各格納値がnullあるいは空文字の場合に格納しない
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 */
	private static void setNodeEntityProperties(NodeEntity nodeEntity, NodeInfo nodeInfo, boolean skipIfEmptyFlg) throws EntityExistsException, HinemosUnknown {
		m_log.debug("setNodeEntityProperties() : facilityId = " + nodeInfo.getFacilityId());

		/** ローカル変数 */
		// オートデバイスサーチ
		Integer autoDeviceSearch = ValidConstant.TYPE_VALID;

		// HW
		String platformFamily = null;
		String subPlatformFamily = null;
		String hardwareType = null;

		// IPアドレス
		Integer ipAddressVersion = null;
		String ipAddressV4 = null;
		String ipAddressV6 = null;

		// OS
		String nodeName = null;
		String osName = null;
		String osRelease = null;
		String osVersion = null;
		String characterSet = null;

		// Hinemosエージェント
		Integer agentAwakePort = null;

		// JOB
		Integer jobPriority = null;
		Integer jobMultiplicity = null;

		// SNMP
		String snmpUser = null;
		String snmpAuthPassword = null;
		String snmpPrivPassword = null;
		Integer snmpPort = null;
		String snmpCommunity = null;
		String snmpVersion = null;
		String snmpSecurityLevel = null;
		String snmpAuthProtocol = null;
		String snmpPrivProtocol = null;
		Integer snmpTimeout = null;
		Integer snmpRetryCount = null;

		// WBEM
		String wbemUser = null;
		String wbemUserPassword = null;
		Integer wbemPort = null;
		String wbemProtocol = null;
		Integer wbemTimeout = null;
		Integer wbemRetryCount = null;

		// IPMI
		String ipmiIpAddress = null;
		Integer ipmiPort = null;
		String ipmiUser = null;
		String ipmiUserPassword = null;
		Integer ipmiTimeout = null;
		Integer ipmiRetries = null;
		String ipmiProtocol = null;
		String ipmiLevel = null;

		// WinRM
		String winrmUser = null;
		String winrmUserPassword = null;
		String winrmVersion = null;
		Integer winrmPort = null;
		String winrmProtocol = null;
		Integer winrmTimeout = null;
		Integer winrmRetries = null;

		// SSH
		String sshUser = "";
		String sshUserPassword = "";
		String sshPrivateKeyFilepath = "";
		String sshPrivateKeyPassphrase = "";
		Integer sshPort = null;
		Integer sshTimeout = null;
		
		// デバイス(主キー項目)
		Integer deviceIndex = null;
		String deviceType = null;
		String deviceName = null;

		// クラウド管理
		String cloudService = null;
		String cloudScope = null;
		String cloudResourceType = null;
		String cloudResourceName = null;
		String cloudResourceId = null;
		String cloudLocation = null;

		// 保守
		String administrator = null;
		String contact = null;

		/** メイン処理 */
		if (nodeInfo.isAutoDeviceSearch() != null && nodeInfo.isAutoDeviceSearch() == false) {
			autoDeviceSearch = ValidConstant.TYPE_INVALID;
		}
		nodeEntity.setAutoDeviceSearch(autoDeviceSearch);

		// HW
		platformFamily = nodeInfo.getPlatformFamily();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(platformFamily))) {
			nodeEntity.setPlatformFamily(platformFamily);
		}
		subPlatformFamily = nodeInfo.getSubPlatformFamily();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(subPlatformFamily))) {
			nodeEntity.setSubPlatformFamily(subPlatformFamily);
		}
		hardwareType = nodeInfo.getHardwareType();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(hardwareType))) {
			nodeEntity.setHardwareType(hardwareType);
		}


		// IPアドレス関連
		ipAddressVersion = nodeInfo.getIpAddressVersion();
		if (! (skipIfEmptyFlg && ipAddressVersion == -1)) {
			nodeEntity.setIpAddressVersion(ipAddressVersion);
		}
		ipAddressV4 = nodeInfo.getIpAddressV4();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(ipAddressV4))) {
			nodeEntity.setIpAddressV4(ipAddressV4);
		}
		ipAddressV6 = nodeInfo.getIpAddressV6();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(ipAddressV6))) {
			nodeEntity.setIpAddressV6(Ipv6Util.expand(ipAddressV6));
		}


		// ホスト名(複数項目)
		if (! skipIfEmptyFlg) {
			List<NodeHostnameEntityPK> nodeHostnameEntityPkList = new ArrayList<NodeHostnameEntityPK>();

			if (nodeInfo.getNodeHostnameInfo() != null) {
				for (NodeHostnameInfo hostname : nodeInfo.getNodeHostnameInfo()) {
					NodeHostnameEntityPK entityPk = new NodeHostnameEntityPK(nodeEntity.getFacilityId(), hostname.getHostname());
					try {
						QueryUtil.getNodeHostnamePK(entityPk);
					} catch (FacilityNotFound e) {
						// 新規登録
						new NodeHostnameEntity(nodeEntity, hostname.getHostname());
					}
					nodeHostnameEntityPkList.add(entityPk);
				}
			}
			// 不要なNodeHostnameEntityを削除
			nodeEntity.deleteNodeHostnameEntities(nodeHostnameEntityPkList);
		}

		// OS関連
		nodeName = nodeInfo.getNodeName();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(nodeName))) {
			nodeEntity.setNodeName(nodeName);
		}
		osName = nodeInfo.getOsName();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(osName))) {
			nodeEntity.setOsName(osName);
		}
		osRelease = nodeInfo.getOsRelease();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(osRelease))) {
			nodeEntity.setOsRelease(osRelease);
		}
		osVersion = nodeInfo.getOsVersion();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(osVersion))) {
			nodeEntity.setOsVersion(osVersion);
		}
		characterSet = nodeInfo.getCharacterSet();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(characterSet))) {
			nodeEntity.setCharacterSet(characterSet);
		}

		// Hinemosエージェント
		agentAwakePort = nodeInfo.getAgentAwakePort();
		if (! (skipIfEmptyFlg && agentAwakePort == -1)) {
			nodeEntity.setAgentAwakePort(agentAwakePort);
		}

		// JOB
		jobPriority = nodeInfo.getJobPriority();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(jobPriority))) {
			nodeEntity.setJobPriority(jobPriority);
		}
		jobMultiplicity = nodeInfo.getJobMultiplicity();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(jobMultiplicity))) {
			nodeEntity.setJobMultiplicity(jobMultiplicity);
		}

		// SNMP関連
		snmpUser = nodeInfo.getSnmpUser();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(snmpUser))) {
			nodeEntity.setSnmpUser(snmpUser);
		}
		snmpAuthPassword = nodeInfo.getSnmpAuthPassword();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(snmpAuthPassword))) {
			nodeEntity.setSnmpAuthPassword(snmpAuthPassword);
		}
		snmpPrivPassword = nodeInfo.getSnmpPrivPassword();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(snmpPrivPassword))) {
			nodeEntity.setSnmpPrivPassword(snmpPrivPassword);
		}
		snmpPort = nodeInfo.getSnmpPort();
		if (! (skipIfEmptyFlg && snmpPort == -1)) {
			nodeEntity.setSnmpPort(snmpPort);
		}
		snmpCommunity = nodeInfo.getSnmpCommunity();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(snmpCommunity))) {
			nodeEntity.setSnmpCommunity(snmpCommunity);
		}
		snmpVersion = nodeInfo.getSnmpVersion();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(snmpVersion))) {
			nodeEntity.setSnmpVersion(snmpVersion);
		}
		snmpSecurityLevel = nodeInfo.getSnmpSecurityLevel();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(snmpSecurityLevel))) {
			nodeEntity.setSnmpSecurityLevel(snmpSecurityLevel);
		}
		snmpAuthProtocol = nodeInfo.getSnmpAuthProtocol();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(snmpAuthProtocol))) {
			nodeEntity.setSnmpAuthProtocol(snmpAuthProtocol);
		}
		snmpPrivProtocol = nodeInfo.getSnmpPrivProtocol();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(snmpPrivProtocol))) {
			nodeEntity.setSnmpPrivProtocol(snmpPrivProtocol);
		}
		snmpTimeout = nodeInfo.getSnmpTimeout();
		if (! (skipIfEmptyFlg && snmpTimeout == -1)) {
			nodeEntity.setSnmpTimeout(snmpTimeout);
		}
		snmpRetryCount = nodeInfo.getSnmpRetryCount();
		if (! (skipIfEmptyFlg && snmpRetryCount == -1)) {
			nodeEntity.setSnmpRetryCount(snmpRetryCount);
		}

		// WBEM関連
		wbemUser = nodeInfo.getWbemUser();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(wbemUser))) {
			nodeEntity.setWbemUser(wbemUser);
		}
		wbemUserPassword = nodeInfo.getWbemUserPassword();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(wbemUserPassword))) {
			nodeEntity.setWbemUserPassword(wbemUserPassword);
		}
		wbemPort = nodeInfo.getWbemPort();
		if (! (skipIfEmptyFlg && wbemPort == -1)) {
			nodeEntity.setWbemPort(wbemPort);
		}
		wbemProtocol = nodeInfo.getWbemProtocol();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(wbemProtocol))) {
			nodeEntity.setWbemProtocol(wbemProtocol);
		}
		wbemTimeout = nodeInfo.getWbemTimeout();
		if (! (skipIfEmptyFlg && wbemTimeout == -1)) {
			nodeEntity.setWbemTimeout(wbemTimeout);
		}
		wbemRetryCount = nodeInfo.getWbemRetryCount();
		if (! (skipIfEmptyFlg && wbemRetryCount == -1)) {
			nodeEntity.setWbemRetryCount(wbemRetryCount);
		}

		// IPMI関連
		ipmiIpAddress = nodeInfo.getIpmiIpAddress();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(ipmiIpAddress))) {
			nodeEntity.setIpmiIpAddress(ipmiIpAddress);
		}
		ipmiPort = nodeInfo.getIpmiPort();
		if (! (skipIfEmptyFlg && ipmiPort == -1)) {
			nodeEntity.setIpmiPort(ipmiPort);
		}
		ipmiUser = nodeInfo.getIpmiUser();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(ipmiUser))) {
			nodeEntity.setIpmiUser(ipmiUser);
		}
		ipmiUserPassword = nodeInfo.getIpmiUserPassword();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(ipmiUserPassword))) {
			nodeEntity.setIpmiUserPassword(ipmiUserPassword);
		}
		ipmiTimeout = nodeInfo.getIpmiTimeout();
		if (! (skipIfEmptyFlg && ipmiTimeout == -1)) {
			nodeEntity.setIpmiTimeout(ipmiTimeout);
		}
		ipmiRetries = nodeInfo.getIpmiRetries();
		if (! (skipIfEmptyFlg && ipmiRetries == -1)) {
			nodeEntity.setIpmiRetryCount(ipmiRetries);
		}
		ipmiProtocol = nodeInfo.getIpmiProtocol();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(ipmiProtocol))) {
			nodeEntity.setIpmiProtocol(ipmiProtocol);
		}
		ipmiLevel = nodeInfo.getIpmiLevel();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(ipmiLevel))) {
			nodeEntity.setIpmiLevel(ipmiLevel);
		}

		// WinRM関連
		winrmUser = nodeInfo.getWinrmUser();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(winrmUser))) {
			nodeEntity.setWinrmUser(winrmUser);
		}
		winrmUserPassword = nodeInfo.getWinrmUserPassword();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(winrmUserPassword))) {
			nodeEntity.setWinrmUserPassword(winrmUserPassword);
		}
		winrmVersion = nodeInfo.getWinrmVersion();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(winrmVersion))) {
			nodeEntity.setWinrmVersion(winrmVersion);
		}
		winrmPort = nodeInfo.getWinrmPort();
		if (! (skipIfEmptyFlg && winrmPort == -1)) {
			nodeEntity.setWinrmPort(winrmPort);
		}
		winrmProtocol = nodeInfo.getWinrmProtocol();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(winrmProtocol))) {
			nodeEntity.setWinrmProtocol(winrmProtocol);
		}
		winrmTimeout = nodeInfo.getWinrmTimeout();
		if (! (skipIfEmptyFlg && winrmTimeout == -1)) {
			nodeEntity.setWinrmTimeout(winrmTimeout);
		}
		winrmRetries = nodeInfo.getWinrmRetries();
		if (! (skipIfEmptyFlg && winrmRetries == -1)) {
			nodeEntity.setWinrmRetryCount(winrmRetries);
		}
		
		// SSH関連
		sshUser = nodeInfo.getSshUser();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(sshUser))) {
			nodeEntity.setSshUser(sshUser);
		}
		sshUserPassword = nodeInfo.getSshUserPassword();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(sshUserPassword))) {
			nodeEntity.setSshUserPassword(sshUserPassword);
		}
		sshPrivateKeyFilepath = nodeInfo.getSshPrivateKeyFilepath();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(sshPrivateKeyFilepath))) {
			nodeEntity.setSshPrivateKeyFilepath(sshPrivateKeyFilepath);
		}
		sshPrivateKeyPassphrase = nodeInfo.getSshPrivateKeyPassphrase();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(sshPrivateKeyPassphrase))) {
			nodeEntity.setSshPrivateKeyPassphrase(sshPrivateKeyPassphrase);
		}
		sshPort = nodeInfo.getSshPort();
		if (! (skipIfEmptyFlg && sshPort == -1)) {
			nodeEntity.setSshPort(sshPort);
		}
		sshTimeout = nodeInfo.getSshTimeout();
		if (! (skipIfEmptyFlg && sshTimeout == -1)) {
			nodeEntity.setSshTimeout(sshTimeout);
		}
		
		// デバイス関連
		// 汎用デバイス関連
		if (! skipIfEmptyFlg) {
			List<NodeDeviceEntityPK> nodeDeviceEntityPkList = new ArrayList<NodeDeviceEntityPK>();
			if (nodeInfo.getNodeDeviceInfo() != null) {
				for (NodeDeviceInfo deviceProperty : nodeInfo.getNodeDeviceInfo()) {
					if(deviceProperty != null){
						// 入力チェック
						if (deviceProperty.getDeviceIndex() != -1
								&& ! ObjectValidator.isEmptyString(deviceProperty.getDeviceType())
								&& ! ObjectValidator.isEmptyString(deviceProperty.getDeviceName())) {
							NodeDeviceEntityPK entityPk
							= new NodeDeviceEntityPK(nodeEntity.getFacilityId(),
									deviceProperty.getDeviceIndex(),
									deviceProperty.getDeviceType(),
									deviceProperty.getDeviceName());
							NodeDeviceEntity entity = null;
							try {
								entity = QueryUtil.getNodeDeviceEntityPK(entityPk);
							} catch (FacilityNotFound e) {
								// 新規登録
								entity = new NodeDeviceEntity(entityPk, nodeEntity);
							}
							entity.setDeviceDisplayName(deviceProperty.getDeviceDisplayName());
							entity.setDeviceSize(deviceProperty.getDeviceSize());
							entity.setDeviceSizeUnit(deviceProperty.getDeviceSizeUnit());
							entity.setDeviceDescription(deviceProperty.getDeviceDescription());
							nodeDeviceEntityPkList.add(entityPk);
						} else {
							HinemosUnknown e = new HinemosUnknown("both type and index of device are required. " +
									"(facilityId = " + nodeEntity.getFacilityId() + ", deviceType = " + deviceType + ", deviceIndex = " + deviceIndex + ", deviceName = " + deviceName + ")");
							m_log.info("setNode() : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage());
							throw e;
						}
					}
				}
			}
			// 不要なNodeDeviceEntityを削除
			nodeEntity.deleteNodeDeviceEntities(nodeDeviceEntityPkList);
		}

		// CPUデバイス関連
		if (! skipIfEmptyFlg) {
			List<NodeCpuEntityPK> nodeCpuEntityPkList = new ArrayList<NodeCpuEntityPK>();

			if (nodeInfo.getNodeCpuInfo() != null) {
				for (NodeCpuInfo cpuProperty : nodeInfo.getNodeCpuInfo()) {
					if(cpuProperty != null){
						// 入力チェック
						if (cpuProperty.getDeviceIndex() != -1
								&& ! ObjectValidator.isEmptyString(cpuProperty.getDeviceType())
								&& ! ObjectValidator.isEmptyString(cpuProperty.getDeviceName())) {
							NodeCpuEntityPK entityPk
							= new NodeCpuEntityPK(nodeEntity.getFacilityId(),
									cpuProperty.getDeviceIndex(),
									cpuProperty.getDeviceType(),
									cpuProperty.getDeviceName());
							NodeCpuEntity entity = null;
							try {
								entity = QueryUtil.getNodeCpuEntityPK(entityPk);
							} catch (FacilityNotFound e) {
								// 新規登録
								entity = new NodeCpuEntity(entityPk, nodeEntity);
							}
							entity.setDeviceDisplayName(cpuProperty.getDeviceDisplayName());
							entity.setDeviceSize(cpuProperty.getDeviceSize());
							entity.setDeviceSizeUnit(cpuProperty.getDeviceSizeUnit());
							entity.setDeviceDescription(cpuProperty.getDeviceDescription());
							nodeCpuEntityPkList.add(entityPk);
						} else {
							HinemosUnknown e = new HinemosUnknown("both type and index of cpu are required. " +
									"(facilityId = " + nodeEntity.getFacilityId() + ", deviceType = " + deviceType + ", deviceIndex = " + deviceIndex + ", deviceName = " + deviceName + ")");
							m_log.info("setNode() : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage());
							throw e;
						}
					}
				}
			}
			// 不要なNodeCpuEntityを削除
			nodeEntity.deleteNodeCpuEntities(nodeCpuEntityPkList);
		}

		// MEMデバイス関連
		if (!skipIfEmptyFlg) {
			List<NodeMemoryEntityPK> nodeMemoryEntityPkList = new ArrayList<NodeMemoryEntityPK>();

			if (nodeInfo.getNodeMemoryInfo() != null) {
				for (NodeMemoryInfo memoryProperty : nodeInfo.getNodeMemoryInfo()) {
					if(memoryProperty != null){
						// 入力チェック
						if (memoryProperty.getDeviceIndex() != -1
								&& ! ObjectValidator.isEmptyString(memoryProperty.getDeviceType())
								&& ! ObjectValidator.isEmptyString(memoryProperty.getDeviceName())) {
							NodeMemoryEntityPK entityPk
							= new NodeMemoryEntityPK(nodeEntity.getFacilityId(),
									memoryProperty.getDeviceIndex(),
									memoryProperty.getDeviceType(),
									memoryProperty.getDeviceName());
							NodeMemoryEntity entity = null;
							try {
								entity = QueryUtil.getNodeMemoryEntityPK(entityPk);
							} catch (FacilityNotFound e) {
								// 新規登録
								entity = new NodeMemoryEntity(entityPk, nodeEntity);
							}
							entity.setDeviceDisplayName(memoryProperty.getDeviceDisplayName());
							entity.setDeviceSize(memoryProperty.getDeviceSize());
							entity.setDeviceSizeUnit(memoryProperty.getDeviceSizeUnit());
							entity.setDeviceDescription(memoryProperty.getDeviceDescription());
							nodeMemoryEntityPkList.add(entityPk);
						} else {
							HinemosUnknown e = new HinemosUnknown("both type and index of memory are required. " +
									"(facilityId = " + nodeEntity.getFacilityId() + ", deviceType = " + deviceType + ", deviceIndex = " + deviceIndex + ", deviceName = " + deviceName + ")");
							m_log.info("setNode() : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage());
							throw e;
						}
					}
				}
			}
			// 不要なNodeMemoryEntityを削除
			nodeEntity.deleteNodeMemoryEntities(nodeMemoryEntityPkList);
		}

		// NICデバイス関連
		if (! skipIfEmptyFlg) {
			List<NodeNetworkInterfaceEntityPK> nodeNetworkInterfaceEntityPkList = new ArrayList<NodeNetworkInterfaceEntityPK>();

			if (nodeInfo.getNodeNetworkInterfaceInfo() != null) {
				for (NodeNetworkInterfaceInfo nicProperty : nodeInfo.getNodeNetworkInterfaceInfo()) {
					if(nicProperty != null){
						// 入力チェック
						if (nicProperty.getDeviceIndex() != -1
								&& ! ObjectValidator.isEmptyString(nicProperty.getDeviceType())
								&& ! ObjectValidator.isEmptyString(nicProperty.getDeviceName())) {
							NodeNetworkInterfaceEntityPK entityPk
							= new NodeNetworkInterfaceEntityPK(nodeEntity.getFacilityId(),
									nicProperty.getDeviceIndex(),
									nicProperty.getDeviceType(),
									nicProperty.getDeviceName());
							NodeNetworkInterfaceEntity entity = null;
							try {
								entity = QueryUtil.getNodeNetworkInterfaceEntityPK(entityPk);
							} catch (FacilityNotFound e) {
								// 新規登録
								entity = new NodeNetworkInterfaceEntity(entityPk, nodeEntity);
							}
							entity.setDeviceDisplayName(nicProperty.getDeviceDisplayName());
							entity.setDeviceSize(nicProperty.getDeviceSize());
							entity.setDeviceSizeUnit(nicProperty.getDeviceSizeUnit());
							entity.setDeviceDescription(nicProperty.getDeviceDescription());
							entity.setDeviceNicIpAddress(nicProperty.getNicIpAddress());
							entity.setDeviceNicMacAddress(nicProperty.getNicMacAddress());
							nodeNetworkInterfaceEntityPkList.add(entityPk);
						} else {
							HinemosUnknown e = new HinemosUnknown("both type and index of nic are required. " +
									"(facilityId = " + nodeEntity.getFacilityId() + ", deviceType = " + deviceType + ", deviceIndex = " + deviceIndex + ", deviceName = " + deviceName + ")");
							m_log.info("setNode() : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage());
							throw e;
						}
					}
				}
			}
			// 不要なNodeNetworkInterfaceEntityを削除
			nodeEntity.deleteNodeNetworkInterfaceEntities(nodeNetworkInterfaceEntityPkList);
		}

		// DISKデバイス関連
		if (! skipIfEmptyFlg) {
			List<NodeDiskEntityPK> nodeDiskEntityPkList = new ArrayList<NodeDiskEntityPK>();

			if (nodeInfo.getNodeDiskInfo() != null) {
				for (NodeDiskInfo diskProperty : nodeInfo.getNodeDiskInfo()) {
					if(diskProperty != null){
						// 入力チェック
						if (diskProperty.getDeviceIndex() != -1
								&& ! ObjectValidator.isEmptyString(diskProperty.getDeviceType())
								&& ! ObjectValidator.isEmptyString(diskProperty.getDeviceName())) {
							NodeDiskEntityPK entityPk
							= new NodeDiskEntityPK(nodeEntity.getFacilityId(),
									diskProperty.getDeviceIndex(),
									diskProperty.getDeviceType(),
									diskProperty.getDeviceName());
							NodeDiskEntity entity = null;
							try {
								entity = QueryUtil.getNodeDiskEntityPK(entityPk);
							} catch (FacilityNotFound e) {
								// 新規登録
								entity = new NodeDiskEntity(entityPk, nodeEntity);
							}
							entity.setDeviceDisplayName(diskProperty.getDeviceDisplayName());
							entity.setDeviceSize(diskProperty.getDeviceSize());
							entity.setDeviceSizeUnit(diskProperty.getDeviceSizeUnit());
							entity.setDeviceDescription(diskProperty.getDeviceDescription());
							entity.setDeviceDiskRpm(diskProperty.getDiskRpm());
							nodeDiskEntityPkList.add(entityPk);
						} else {
							HinemosUnknown e = new HinemosUnknown("both type and index of disk are required. " +
									"(facilityId = " + nodeEntity.getFacilityId() + ", deviceType = " + deviceType + ", deviceIndex = " + deviceIndex + ", deviceName = " + deviceName + ")");
							m_log.info("setNode() : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage());
							throw e;
						}
					}
				}
			}
			// 不要なNodeDiskEntityを削除
			nodeEntity.deleteNodeDiskEntities(nodeDiskEntityPkList);
		}

		// ファイルシステム関連
		if (! skipIfEmptyFlg) {
			List<NodeFilesystemEntityPK> nodeFilesystemEntityPkList = new ArrayList<NodeFilesystemEntityPK>();

			if (nodeInfo.getNodeFilesystemInfo() != null) {
				for (NodeFilesystemInfo filesystemProperty : nodeInfo.getNodeFilesystemInfo()) {
					if(filesystemProperty != null){
						// 入力チェック
						if (filesystemProperty.getDeviceIndex() != -1
								&& ! ObjectValidator.isEmptyString(filesystemProperty.getDeviceType())
								&& ! ObjectValidator.isEmptyString(filesystemProperty.getDeviceName())) {
							NodeFilesystemEntityPK entityPk
							= new NodeFilesystemEntityPK(nodeEntity.getFacilityId(),
									filesystemProperty.getDeviceIndex(),
									filesystemProperty.getDeviceType(),
									filesystemProperty.getDeviceName());
							NodeFilesystemEntity entity = null;
							try {
								entity = QueryUtil.getFilesystemDiskEntityPK(entityPk);
							} catch (FacilityNotFound e) {
								// 新規登録
								entity = new NodeFilesystemEntity(entityPk, nodeEntity);
							}
							entity.setDeviceDisplayName(filesystemProperty.getDeviceDisplayName());
							entity.setDeviceSize(filesystemProperty.getDeviceSize());
							entity.setDeviceSizeUnit(filesystemProperty.getDeviceSizeUnit());
							entity.setDeviceDescription(filesystemProperty.getDeviceDescription());
							entity.setDeviceFilesystemType(filesystemProperty.getFilesystemType());
							nodeFilesystemEntityPkList.add(entityPk);
						} else {
							HinemosUnknown e = new HinemosUnknown("both type and index of filesystem are required. " +
									"(facilityId = " + nodeEntity.getFacilityId() + ", deviceType = " + deviceType + ", deviceIndex = " + deviceIndex + ", deviceName = " + deviceName + ")");
							m_log.info("setNode() : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage());
							throw e;
						}
					}
				}
			}
			// 不要なNodeFilesystemEntityを削除
			nodeEntity.deleteNodeFilesystemEntities(nodeFilesystemEntityPkList);
		}

		// クラウド・仮想化管理関連
		cloudService = nodeInfo.getCloudService();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(cloudService))) {
			nodeEntity.setCloudService(cloudService);
		}
		cloudScope = nodeInfo.getCloudScope();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(cloudScope))) {
			nodeEntity.setCloudScope(cloudScope);
		}
		cloudResourceType = nodeInfo.getCloudResourceType();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(cloudResourceType))) {
			nodeEntity.setCloudResourceType(cloudResourceType);
		}
		cloudResourceId = nodeInfo.getCloudResourceId();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(cloudResourceId))) {
			nodeEntity.setCloudResourceId(cloudResourceId);
		}
		cloudResourceName = nodeInfo.getCloudResourceName();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(cloudResourceName))) {
			nodeEntity.setCloudResourceName(cloudResourceName);
		}
		cloudResourceId = nodeInfo.getCloudResourceId();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(cloudResourceId))) {
			nodeEntity.setCloudResourceId(cloudResourceId);
		}
		cloudLocation = nodeInfo.getCloudLocation();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(cloudLocation))) {
			nodeEntity.setCloudLocation(cloudLocation);
		}


		// ノード変数
		if (! skipIfEmptyFlg) {
			List<NodeVariableEntityPK> nodeVariableEntityPkList = new ArrayList<NodeVariableEntityPK>();

			if (nodeInfo.getNodeVariableInfo() != null) {
				for (NodeVariableInfo variable : nodeInfo.getNodeVariableInfo()) {
					if (variable != null) {
						NodeVariableEntityPK entityPk = new NodeVariableEntityPK(nodeEntity.getFacilityId(), variable.getNodeVariableName());
						NodeVariableEntity entity = null;
						try {
							entity = QueryUtil.getNodeVariableEntityPK(entityPk);
						} catch (FacilityNotFound e) {
							// 新規登録
							entity = new NodeVariableEntity(nodeEntity, variable.getNodeVariableName());
						}
						entity.setNodeVariableValue(variable.getNodeVariableValue());
						nodeVariableEntityPkList.add(entityPk);
					}
				}
			}
			// 不要なNodeVariableEntityを削除
			nodeEntity.deleteNodeVariableEntities(nodeVariableEntityPkList);
		}

		// 管理情報関連
		administrator = nodeInfo.getAdministrator();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(administrator))) {
			nodeEntity.setAdministrator(administrator);
		}
		contact = nodeInfo.getContact();
		if (! (skipIfEmptyFlg && ObjectValidator.isEmptyString(contact))) {
			nodeEntity.setContact(contact);
		}

		// 備考
		if (! skipIfEmptyFlg) {
			List<NodeNoteEntityPK> nodeNoteEntityPkList = new ArrayList<NodeNoteEntityPK>();

			if (nodeInfo.getNodeNoteInfo() != null) {
				for (NodeNoteInfo note : nodeInfo.getNodeNoteInfo()) {
					if(note != null){
						NodeNoteEntity entity = null;
						NodeNoteEntityPK entityPk = new NodeNoteEntityPK(nodeEntity.getFacilityId(), note.getNoteId());
						try {
							entity = QueryUtil.getNodeNoteEntityPK(entityPk);
						} catch (FacilityNotFound e) {
							// 新規登録
							entity = new NodeNoteEntity(entityPk, nodeEntity);
						}
						entity.setNote(note.getNote());
						nodeNoteEntityPkList.add(entityPk);
					}
				}
			}
			// 不要なNodeNoteEntityを削除
			nodeEntity.deleteNodeNoteEntities(nodeNoteEntityPkList);
		}
	}
}
