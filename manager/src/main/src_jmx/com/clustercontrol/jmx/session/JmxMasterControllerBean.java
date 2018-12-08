/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 */

package com.clustercontrol.jmx.session;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jmx.bean.JmxMasterInfo;
import com.clustercontrol.jmx.model.MonitorJmxMstEntity;
import com.clustercontrol.util.Messages;

/**
 * JMX 監視項目マスタ情報を制御するSession Bean <BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class JmxMasterControllerBean {

	private static Log m_log = LogFactory.getLog( JmxMasterControllerBean.class );


	/**
	 * JMX 監視項目マスタを登録します。
	 * 
	 * @param datas JMX 監視項目マスタ
	 * @return 登録に成功した場合、true
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 */
	public boolean addJmxMasterList(List<JmxMasterInfo> datas) throws HinemosUnknown, InvalidSetting {
		JpaTransactionManager jtm = null;

		boolean ret = false;

		for (JmxMasterInfo m: datas) {
			validateJmxMasterInfo(m);
		}

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// インスタンス生成
			for (JmxMasterInfo data: datas) {
				MonitorJmxMstEntity entity = new MonitorJmxMstEntity(data.getId(), data.getObjectName(), data.getAttributeName(), data.getKeys(), data.getName(), data.getMeasure());
				// 重複チェック
				jtm.checkEntityExists(MonitorJmxMstEntity.class, entity.getId());
			}

			jtm.commit();

			ret = true;
		} catch (EntityExistsException e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("addJmxMasterList() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}

		return ret;
	}

	/**
	 * JMX 監視項目マスタを削除します。
	 * 
	 * @return 削除に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean deleteJmxMasterList(List<String> ids) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		boolean ret = false;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorJmxMstEntity> entities = em.createNamedQuery("MonitorJmxMstEntity.findList", MonitorJmxMstEntity.class).setParameter("ids", ids).getResultList();
			for (MonitorJmxMstEntity entity : entities) {
				// 削除処理
				em.remove(entity);
			}

			jtm.commit();

			ret = true;
		} catch (Exception e) {
			m_log.warn("deleteJmxMasterList() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return ret;

	}

	/**
	 * JMX 監視項目マスタを全て削除します。
	 * 
	 * @return 削除に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean deleteJmxMasterAll() throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		boolean ret = false;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorJmxMstEntity> entities = em.createNamedQuery("MonitorJmxMstEntity.findAll", MonitorJmxMstEntity.class).getResultList();
			for (MonitorJmxMstEntity entity : entities) {
				// 削除処理
				em.remove(entity);
			}

			jtm.commit();

			ret = true;
		} catch (Exception e) {
			m_log.warn("deleteJmxMasterAll() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return ret;

	}

	/**
	 * JMX 監視項目マスタを取得します。
	 * 
	 * @return JMX 監視項目マスタ
	 * @throws HinemosUnknown
	 */
	public List<JmxMasterInfo> getJmxMasterList() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<JmxMasterInfo> ret = new ArrayList<>();

		try {
			jtm = new JpaTransactionManager();
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorJmxMstEntity> entities = em.createNamedQuery("MonitorJmxMstEntity.findAll", MonitorJmxMstEntity.class).getResultList();

			for (MonitorJmxMstEntity entity: entities) {
				ret.add(new JmxMasterInfo(entity.getId(), entity.getObjectName(), entity.getAttributeName(), entity.getKeys(), entity.getName(), entity.getMeasure()));
			}
		} catch (Exception e) {
			m_log.warn("getJmxMasterList() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return ret;
	}

	private void validateJmxMasterInfo(JmxMasterInfo info) throws InvalidSetting {
		CommonValidator.validateString(Messages.getString("monitor.jmx.master.id"), info.getId(), true, 1, 64);
		CommonValidator.validateString(Messages.getString("monitor.jmx.master.objectname"), info.getObjectName(), true, 1, 512);
		CommonValidator.validateString(Messages.getString("monitor.jmx.master.attributename"), info.getAttributeName(), true, 1, 256);
		CommonValidator.validateString(Messages.getString("monitor.jmx.master.keys"), info.getKeys(), false, 0, 512);
		CommonValidator.validateString(Messages.getString("monitor.jmx.master.name"), info.getName(), true, 1, 256);
		CommonValidator.validateString(Messages.getString("monitor.jmx.master.measure"), info.getMeasure(), true, 1, 64);
	}
}
