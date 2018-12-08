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

package com.clustercontrol.maintenance.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.maintenance.bean.MaintenanceTypeMst;
import com.clustercontrol.maintenance.model.MaintenanceTypeMstEntity;
import com.clustercontrol.maintenance.util.QueryUtil;

/**
 * 
 * メンテナンス種別に関する情報を検索するためのクラスです。
 * 
 * @since	2.2.0
 * @version	2.2.0
 *
 */
public class SelectMaintenanceTypeMst {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectMaintenanceTypeMst.class );

	/**
	 * MaintenanceTypeMstEntityからMaintenanceTypeMstBeanへ変換
	 */
	private MaintenanceTypeMst getMaintenanceTypeBean(MaintenanceTypeMstEntity entity) {

		MaintenanceTypeMst type  = new MaintenanceTypeMst();
		type.setType_id(entity.getTypeId());
		type.setName_id(entity.getNameId());
		type.setOrder_no(entity.getOrderNo());
		return type;
	}

	/**
	 * メンテナンス種別一覧を取得する
	 * @return
	 */
	public ArrayList<MaintenanceTypeMst> getMaintenanceTypeList() {
		m_log.debug("getMaintenanceTypeList() : start");
		ArrayList<MaintenanceTypeMst> list = new ArrayList<MaintenanceTypeMst>();

		// メンテナンス種別マスタ一覧を取得
		List<MaintenanceTypeMstEntity> ct = QueryUtil.getAllMaintenanceTypeMst();

		for(MaintenanceTypeMstEntity entity : ct){
			list.add(getMaintenanceTypeBean(entity));
			// for debug
			if(m_log.isDebugEnabled()){
				m_log.debug("getMaintenanceTypeList() : " +
						"type_id = " + entity.getTypeId() +
						", name_id = " + entity.getNameId() +
						", order_no = " + entity.getOrderNo());
			}
		}

		return list;
	}
}
