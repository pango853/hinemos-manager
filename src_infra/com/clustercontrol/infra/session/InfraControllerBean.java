/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.session;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.PluginConstant;
import com.clustercontrol.commons.session.CheckFacility;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraFileBeingUsed;
import com.clustercontrol.fault.InfraFileNotFound;
import com.clustercontrol.fault.InfraFileTooLarge;
import com.clustercontrol.fault.InfraManagementDuplicate;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InfraModuleNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.SessionNotFound;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.infra.bean.AccessInfo;
import com.clustercontrol.infra.bean.InfraCheckResult;
import com.clustercontrol.infra.bean.InfraFileInfo;
import com.clustercontrol.infra.bean.InfraManagementInfo;
import com.clustercontrol.infra.bean.ModuleResult;
import com.clustercontrol.infra.factory.AddInfraFile;
import com.clustercontrol.infra.factory.AddInfraManagement;
import com.clustercontrol.infra.factory.AsyncModuleWorker;
import com.clustercontrol.infra.factory.DeleteInfraFile;
import com.clustercontrol.infra.factory.DeleteInfraManagement;
import com.clustercontrol.infra.factory.DownloadInfraFile;
import com.clustercontrol.infra.factory.ModifyInfraFile;
import com.clustercontrol.infra.factory.ModifyInfraManagement;
import com.clustercontrol.infra.factory.SelectInfraCheckResult;
import com.clustercontrol.infra.factory.SelectInfraManagement;
import com.clustercontrol.infra.model.InfraFileEntity;
import com.clustercontrol.infra.model.InfraManagementInfoEntity;
import com.clustercontrol.infra.util.InfraManagementValidator;
import com.clustercontrol.infra.util.QueryUtil;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.notify.bean.NotifyCheckIdResultInfo;
import com.clustercontrol.notify.factory.SelectNotifyRelation;

/**
 * 環境構築機能の管理を行う Session Bean です。<BR>
 * クライアントからの Entity Bean へのアクセスは、Session Bean を介して行います。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraControllerBean implements CheckFacility {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( InfraControllerBean.class );
	private static final Object LOCK = new Object();

	/**
	 * 環境構築機能を作成します。
	 * @throws InfraManagementDuplicate 
	 *
	 */
	public boolean addInfraManagement(InfraManagementInfo info) throws NotifyDuplicate, InvalidSetting, InvalidRole, HinemosUnknown, InfraManagementDuplicate {
		JpaTransactionManager jtm = null;

		// 通知情報を登録
		boolean flag;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			//入力チェック
			InfraManagementValidator.validateInfraManagementInfo(info);

			AddInfraManagement proc = new AddInfraManagement();
			flag = proc.add(info, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.commit();
		} catch (EntityExistsException e) {
			jtm.rollback();
			throw new InfraManagementDuplicate(e.getMessage(), e);
		} catch (NotifyDuplicate e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("addInfraManagement() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		return  flag;
	}

	/**
	 * 環境構築機能を変更します。
	 * @throws InfraManagementDuplicate 
	 *
	 */
	public boolean modifyInfraManagement(InfraManagementInfo info) throws InfraManagementNotFound, NotifyDuplicate, NotifyNotFound, InvalidRole, HinemosUnknown, InvalidSetting, InfraManagementDuplicate {
		JpaTransactionManager jtm = null;

		// 通知情報を更新
		boolean flag;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			//入力チェック
			InfraManagementValidator.validateInfraManagementInfo(info);

			ModifyInfraManagement proc = new ModifyInfraManagement();
			flag = proc.modify(info, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.commit();
		} catch (EntityExistsException e) {
			jtm.rollback();
			throw new InfraManagementDuplicate(e.getMessage(), e);
		} catch (NotifyDuplicate e) {
			jtm.rollback();
			throw e;
		} catch (NotifyNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (InfraManagementNotFound e){
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("modifyInfraManagement() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return flag;
	}

	/**
	 */
	public boolean deleteInfraManagement(String[] infraManagementIds) throws InfraManagementNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 通知情報を削除
		DeleteInfraManagement proc = new DeleteInfraManagement();
		boolean flag = true;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for (String infraManagementId: infraManagementIds) {
				flag = flag && proc.delete(infraManagementId);
			}

			jtm.commit();
		} catch(HinemosUnknown e){
			jtm.rollback();
			throw e;
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (InfraManagementNotFound e){
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("deleteInfraManagement() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return flag;
	}

	/**
	 */
	public InfraManagementInfo getInfraManagement(String infraManagementId) throws InvalidRole, HinemosUnknown, InfraManagementNotFound {
		JpaTransactionManager jtm = null;

		// 通知情報を取得
		SelectInfraManagement proc = new SelectInfraManagement();
		InfraManagementInfo info = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			info = proc.get(infraManagementId, ObjectPrivilegeMode.READ);
			jtm.commit();
		} catch (HinemosUnknown e){
			jtm.rollback();
			throw e;
		} catch (InvalidRole e){
			jtm.rollback();
			throw e;
		} catch (InfraManagementNotFound e){
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getInfraManagement() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		return info;
	}

	/**
	 */
	public List<InfraManagementInfo> getInfraManagementList() throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 通知一覧を取得
		SelectInfraManagement proc = new SelectInfraManagement();
		List<InfraManagementInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = proc.getList();
			jtm.commit();
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getInfraManagementList() : "
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
	 */
	public List<InfraManagementInfo> getInfraManagementListByOwnerRole(String ownerRoleId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 通知一覧を取得
		SelectInfraManagement proc = new SelectInfraManagement();
		List<InfraManagementInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = proc.getListByOwnerRole(ownerRoleId);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getInfraManagementListByOwnerRole() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		return list;
	}

	/**

	 */
	public void checkNotifyId(String[] notifyIds) throws InvalidRole, HinemosUnknown {
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
			// ファシリティIDが使用されている設定を取得する。
			Collection<InfraManagementInfoEntity> ct = QueryUtil.getInfraManagementInfoFindByFacilityId_NONE(facilityId);

			if(ct != null && ct.size() > 0) {
				UsedFacility e = new UsedFacility(PluginConstant.TYPE_INFRA);
				m_log.info("isUseFacilityId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			jtm.commit();
		} catch (UsedFacility e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("isUseFacilityId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
		} finally {
			jtm.close();
		}
	}

	public String createSession(String infraManagementId, List<String> moduleIdList, List<AccessInfo> accessList)
			throws InfraManagementNotFound, InfraModuleNotFound, InvalidRole, HinemosUnknown, FacilityNotFound, InvalidSetting {
		return AsyncModuleWorker.createSession(infraManagementId, moduleIdList, accessList);
	}

	public boolean deleteSession(String sessionId) throws InfraManagementNotFound, InvalidRole, HinemosUnknown {
		return AsyncModuleWorker.deleteSession(sessionId);
	}
	
	/**
	 * 
	 * @param sessionId
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InfraManagementNotFound
	 * @throws InfraModuleNotFound
	 * @throws InvalidUserPass
	 * @throws SessionNotFound
	 */
	public ModuleResult runInfraModule(String sessionId, String account)
			throws HinemosUnknown, InvalidRole, InfraManagementNotFound, InfraModuleNotFound, InvalidUserPass, SessionNotFound {
		JpaTransactionManager jtm = null;
		ModuleResult ret = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			ret = AsyncModuleWorker.runInfraModule(sessionId, account);
			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		}
		return ret;
	}

	/**
	 * 
	 * @param sessionId
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InfraManagementNotFound
	 * @throws InfraModuleNotFound
	 * @throws InvalidUserPass
	 * @throws SessionNotFound
	 */
	public ModuleResult checkInfraModule(String sessionId, boolean verbose)
			throws HinemosUnknown, InvalidRole, InfraManagementNotFound, InfraModuleNotFound, InvalidUserPass, SessionNotFound {
		JpaTransactionManager jtm = null;
		ModuleResult ret = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			ret = AsyncModuleWorker.checkInfraModule(sessionId, verbose);
			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		}
		return ret;
	}
	

	public List<InfraCheckResult> getCheckResultList(String managementId) throws HinemosUnknown, InvalidRole {
		JpaTransactionManager jtm = null;

		SelectInfraCheckResult select = new SelectInfraCheckResult();
		List<InfraCheckResult> resultList = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			resultList = select.getListByManagementId(managementId);
			jtm.commit();
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
			m_log.warn("getCheckResultList() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}

		return resultList;
	}

	public void addInfraFile(InfraFileInfo fileInfo, DataHandler fileContent) throws InvalidRole, HinemosUnknown, InfraFileTooLarge, InfraManagementDuplicate {
		synchronized (LOCK) {
			JpaTransactionManager jtm = null;
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();
				
				//入力チェック
				InfraManagementValidator.validateInfraFileInfo(fileInfo);
				
				if (fileContent == null) {
					InvalidSetting e = new InvalidSetting("fileContent is not defined.");
					m_log.info("addInfraFile() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
	
				String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
				new AddInfraFile().add(fileInfo, fileContent, userId);
	
				jtm.commit();
			} catch (EntityExistsException e) {
				jtm.rollback();
				throw new InfraManagementDuplicate(e.getMessage(), e);
			} catch (ObjectPrivilege_InvalidRole e) {
				jtm.rollback();
				throw new InvalidRole(e.getMessage(), e);
			} catch (InfraFileTooLarge e) {
				jtm.rollback();
				throw e;
			} catch (Exception e){
				m_log.warn("addInfraFile() : " + e.getClass().getSimpleName() +
						", " + e.getMessage(), e);
				jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} finally {
				if (jtm != null) {
					jtm.close();
				}
			}
		}
	}

	public List<InfraFileInfo> getInfraFileList() {
		JpaTransactionManager jtm = null;

		List<InfraFileInfo> list = new ArrayList<InfraFileInfo>();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for (InfraFileEntity entity : QueryUtil.getAllInfraFile()) {
				list.add(entity.createWebEntity());
			}
			jtm.commit();
		} finally {
			jtm.close();
		}

		return list;
	}

	public void deleteInfraFileList(List<String> fileIdList) throws InvalidRole, HinemosUnknown, InfraFileNotFound, InfraFileBeingUsed {
		JpaTransactionManager jtm = null;
		DeleteInfraFile proc = new DeleteInfraFile();
		
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			for (String fileId : fileIdList) {
				proc.delete(fileId);
			}
			
			jtm.commit();
		} finally {
			jtm.close();
		}
	}

	public DataHandler downloadInfraFile(String fileId, String fileName) throws InvalidSetting, InfraFileNotFound, HinemosUnknown, IOException {
		synchronized (LOCK) {
			DataHandler dh = null;
			JpaTransactionManager jtm = null;
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();
				
				//入力チェック
				if (fileId == null) {
					InvalidSetting e = new InvalidSetting("fileId is not defined.");
					m_log.info("downloadInfraFile() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				
				dh = new DownloadInfraFile().download(fileId, fileName);
	
				jtm.commit();
			} finally {
				if (jtm != null) {
					jtm.close();
				}
			}
			
			return dh;
		}
	}
	
	public void deleteDownloadedInfraFile(String fileName) {
		String exportDirectory = HinemosPropertyUtil.getHinemosPropertyStr(
				"infra.export.dir", "/opt/hinemos/var/export/");
		File file = new File(exportDirectory + "/" + fileName);
		file.delete();
	}

	public void modifyInfraFile(InfraFileInfo fileInfo, DataHandler fileContent) throws InvalidRole, HinemosUnknown, InfraFileTooLarge {
		synchronized (LOCK) {
			JpaTransactionManager jtm = null;
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();
				
				//入力チェック
				InfraManagementValidator.validateInfraFileInfo(fileInfo);
				
				String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
				new ModifyInfraFile().modify(fileInfo, fileContent, userId);
	
				jtm.commit();
			} catch (ObjectPrivilege_InvalidRole e) {
				jtm.rollback();
				throw new InvalidRole(e.getMessage(), e);
			} catch (InfraFileTooLarge e) {
				jtm.rollback();
				throw e;
			} catch (Exception e){
				m_log.warn("modifyInfraFile() : " + e.getClass().getSimpleName() +
						", " + e.getMessage(), e);
				jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} finally {
				if (jtm != null) {
					jtm.close();
				}
			}
		}
	}

	public DataHandler downloadTransferFile(String fileName) {
		m_log.info("downloadTransferFile fileName="+fileName);
		String infraDirectory = HinemosPropertyUtil.getHinemosPropertyStr("infra.transfer.dir", "/opt/hinemos/var/infra") + File.separator + "send" + File.separator;
		FileDataSource fileData = new FileDataSource(infraDirectory + fileName);
		return new DataHandler(fileData);
	}
}
