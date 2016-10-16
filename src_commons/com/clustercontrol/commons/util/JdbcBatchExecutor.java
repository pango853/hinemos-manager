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

package com.clustercontrol.commons.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JDBCドライバを用いて、高速にinsertまたはupdateの一括処理を行うクラス
 */
public class JdbcBatchExecutor {
	private static final Log log = LogFactory.getLog(JdbcBatchExecutor.class);

	/**
	 * クエリを実行する
	 * @param query insertまたはupdate
	 */
	public static void execute(JdbcBatchQuery query) {
		Connection conn = null;
		long start = System.currentTimeMillis();
		
		JpaTransactionManager tm = null;
		try {
			tm = new JpaTransactionManager();
			conn = tm.getEntityManager().unwrap(java.sql.Connection.class);
			conn.setAutoCommit(false);

			PreparedStatement pstmt = conn.prepareStatement(query.getSql());
			query.addBatch(pstmt);
			pstmt.executeBatch();
			
			if (! tm.isNestedEm()) {
				conn.commit();
			}
		} catch (Exception e) {
			log.warn(e);
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					log.warn(e1);
				}
			}
		} finally {
			if (tm != null) {
				tm.close();
			}
		}
		long time = System.currentTimeMillis() - start;
		String message = String.format("Execute [%s] batch: %dms %s", query.getClass().getSimpleName(), time, query.getSql());
		if (time > 3000) {
			log.warn(message);
		} else if (time > 1000) {
			log.info(message);
		} else {
			log.debug(message);
		}
	}
}
