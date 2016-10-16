/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.performance.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.clustercontrol.commons.util.JdbcBatchQuery;
import com.clustercontrol.performance.model.CalculatedDataEntity;
import com.clustercontrol.performance.model.CalculatedDataEntityPK;

/**
 *  CalculatedDataEntityにマッピングするテーブルにデータを登録するクラス
 */
public class CalculatedDataEntityJdbcBatchInsert extends JdbcBatchQuery {
	private static final String SQL = "insert into log.cc_calculated_data "
			+ "(collectorid, item_code, display_name, date_time, facilityid, value) "
			+ "values (?, ?, ?, ?, ?, ?)";;

	private List<CalculatedDataEntity> entities = null;

	public CalculatedDataEntityJdbcBatchInsert(List<CalculatedDataEntity> entities) {
		this.entities = entities;
	}

	@Override
	public String getSql() {
		return SQL;
	}

	@Override
	public void addBatch(PreparedStatement pstmt) throws SQLException {
		for (CalculatedDataEntity entity : entities) {
			CalculatedDataEntityPK pk = entity.getId();
			Object[] params = new Object[] {
				pk.getCollectorid(),
				pk.getItemCode(),
				pk.getDisplayName(),
				pk.getDateTime(),
				pk.getFacilityid(),
				entity.getValue()
			};
			setParameters(pstmt, params);
			pstmt.addBatch();
		}
	}
}
