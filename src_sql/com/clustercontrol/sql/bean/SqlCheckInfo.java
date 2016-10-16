/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.sql.bean;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.bean.MonitorCheckInfo;

/**
 * SQL監視設定情報のBean(DTO)クラス<BR>
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class SqlCheckInfo extends MonitorCheckInfo {
	private static final long serialVersionUID = 2973168313412207682L;

	/** 接続先URL **/
	private java.lang.String connectionUrl;

	/** ユーザ **/
	private java.lang.String user;

	/** パスワード **/
	private java.lang.String password;

	/** クエリ **/
	private java.lang.String query;

	/** JDBCドライバ **/
	private String jdbcDriver;

	public SqlCheckInfo(){
	}

	/**
	 * 接続先URLを取得します。
	 * @return 接続先URL
	 */
	public java.lang.String getConnectionUrl() {
		return connectionUrl;
	}
	/**
	 * 接続先URLを設定します。
	 * @param connection_url
	 */
	public void setConnectionUrl(java.lang.String connection_url) {
		this.connectionUrl = connection_url;
	}
	/**
	 * 接続用JDBCドライバ名を取得します。<BR>
	 * @return　JDBCドライバ名
	 */
	public java.lang.String getJdbcDriver() {
		return jdbcDriver;
	}
	/**
	 * 接続用JDBCドライバ名を設定します。<BR>
	 * @param jdbcDriver
	 */
	public void setJdbcDriver(java.lang.String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
	}
	/**
	 * DBMSに接続する際のパスワードを取得します。<BR>
	 * @return　DBMSに接続する際のパスワード
	 */
	public java.lang.String getPassword() {
		return password;
	}
	/**
	 *  DBMSに接続する際のパスワードを設定します。<BR>
	 * @param password
	 */
	public void setPassword(java.lang.String password) {
		this.password = password;
	}
	/**
	 * 監視の検索式を取得します。<BR>
	 * @return
	 */
	public java.lang.String getQuery() {
		return query;
	}
	/**
	 * 監視の検索式を設定します。<BR>
	 * @param query
	 */
	public void setQuery(java.lang.String query) {
		this.query = query;
	}
	/**
	 * DBMSに接続するユーザ名を取得します。<BR>
	 * @return DBMSに接続するユーザ名
	 */
	public java.lang.String getUser() {
		return user;
	}
	/**
	 * DBMSに接続するユーザ名を設定します。<BR>
	 * @param user
	 */
	public void setUser(java.lang.String user) {
		this.user = user;
	}

}
