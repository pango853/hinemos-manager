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

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.performance.model.CalculatedDataEntity;

public class QueryUtil {

	private static int GRAPH_MAX_PLOT = 10000;

	public static List<CalculatedDataEntity> getCalculatedDataByCollectoridFacilityidDateTime(
			String collectorid,
			String facilityid,
			Timestamp dateTime,
			int fetchSize) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalculatedDataEntity> list
		= em.createNamedQuery("CalculatedDataEntity.findByCollectoridFacilityidDateTime", CalculatedDataEntity.class)
		.setHint("eclipselink.jdbc.fetch-size", fetchSize)
		.setParameter("collectorid", collectorid)
		.setParameter("facilityid", facilityid)
		.setParameter("dateTime", dateTime)
		.getResultList();
		return list;
	}

	public static List<CalculatedDataEntity> getLatestDateByCollectorid(String collectorid, int rowCount) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalculatedDataEntity> list
		= em.createNamedQuery("CalculatedDataEntity.getLatestDateByCollectorid", CalculatedDataEntity.class)
		.setParameter("collectorid", collectorid)
		.setMaxResults(rowCount)
		.getResultList();
		return list;
	}

	public static List<CalculatedDataEntity> getOldestDateByCollectorid(String collectorid, int rowCount) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalculatedDataEntity> list
		= em.createNamedQuery("CalculatedDataEntity.getOldestDateByCollectorid", CalculatedDataEntity.class)
		.setParameter("collectorid", collectorid)
		.setMaxResults(rowCount)
		.getResultList();
		return list;
	}

	public static List<CalculatedDataEntity> getCalculatedDataByFilter(
			String collectorid,
			String itemCode,
			String displayName,
			String facilityid,
			Date startDate,
			Date stopDate) {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		// SQL作成
		StringBuffer sbJpql = new StringBuffer();
		sbJpql.append("SELECT a FROM CalculatedDataEntity a WHERE true = true");
		sbJpql.append(" AND a.id.collectorid = :collectorid");
		sbJpql.append(" AND a.id.itemCode = :itemCode");
		sbJpql.append(" AND a.id.displayName = :displayName");
		sbJpql.append(" AND a.id.facilityid = :facilityid");
		// 開始日時設定
		if(startDate != null) {
			sbJpql.append(" AND a.id.dateTime >= :dateTimeFrom");
		}
		// 終了日時設定
		if (stopDate != null){
			sbJpql.append(" AND a.id.dateTime <= :dateTimeTo");
		}
		sbJpql.append(" ORDER BY a.id.dateTime");

		TypedQuery<CalculatedDataEntity> typedQuery = em.createQuery(sbJpql.toString(), CalculatedDataEntity.class);

		typedQuery = typedQuery.setParameter("collectorid", collectorid);
		typedQuery = typedQuery.setParameter("itemCode", itemCode);
		typedQuery = typedQuery.setParameter("displayName", displayName);
		typedQuery = typedQuery.setParameter("facilityid", facilityid);
		// 開始日時設定
		if(startDate != null) {
			typedQuery = typedQuery.setParameter("dateTimeFrom", new Timestamp(startDate.getTime()));
		}
		// 終了日時設定
		if (stopDate != null){
			typedQuery = typedQuery.setParameter("dateTimeTo", new Timestamp(stopDate.getTime()));
		}
		typedQuery = typedQuery.setMaxResults(GRAPH_MAX_PLOT);

		return typedQuery.getResultList();
	}
}
