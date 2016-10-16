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

package com.clustercontrol.logfile.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.logfile.bean.LogfileCheckInfo;
import com.clustercontrol.logfile.model.MonitorLogfileInfoEntity;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;

/**
 * ログファイル監視 判定情報を管理するクラス<BR>
 *
 * @version 4.1.0
 * @since 4.0.0
 */
public class ControlLogfileInfo {
	private static Log m_log = LogFactory.getLog( ControlLogfileInfo.class );

	/** 監視対象ID */
	private String m_monitorTypeId;

	/** 監視ID */
	private String m_monitorId;

	/**
	 * コンストラクタ
	 * 
	 * @param monitorId 監視項目ID
	 * @param monitorTypeId 監視対象ID
	 * @version 4.0.0
	 * @since 4.0.0
	 */
	public ControlLogfileInfo(String monitorId, String monitorTypeId) {
		m_monitorId = monitorId;
		m_monitorTypeId = monitorTypeId;
	}

	/**
	 * ログファイル監視情報を取得します。<BR>
	 * 
	 * @return ログファイル監視情報
	 * @throws MonitorNotFound
	 * @version 4.1.0
	 * @since 4.0.0
	 */
	public LogfileCheckInfo get() throws MonitorNotFound {

		// ログファイル監視情報を取得
		MonitorLogfileInfoEntity entity = QueryUtil.getMonitorLogfileInfoPK(m_monitorId);

		LogfileCheckInfo logfile = new LogfileCheckInfo();
		logfile.setMonitorTypeId(m_monitorTypeId);
		logfile.setMonitorId(m_monitorId);
		logfile.setDirectory(entity.getDirectory());
		logfile.setFileName(entity.getFileName());
		logfile.setFileEncoding(entity.getFileEncoding());
		logfile.setFileReturnCode(entity.getFileReturnCode());
		m_log.trace("get() : logfile.getDirectory = " + logfile.getDirectory());
		m_log.trace("get() : logfile.getFileName = " + logfile.getFileName());
		m_log.trace("get() : logfile.getFileEncoding = " + logfile.getFileEncoding());
		m_log.trace("get() : logfile.getFileReturnCode = " + logfile.getFileReturnCode());
		return logfile;
	}

	/**
	 * ログファイル監視情報を追加します。<BR>
	 * 
	 * @param logfile ログファイル監視情報
	 * @return 成功した場合、true
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @version 4.1.0
	 * @since 4.0.0
	 */
	public boolean add(LogfileCheckInfo logfile) throws MonitorNotFound, InvalidRole {

		MonitorInfoEntity monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorId);

		// ログファイル監視情報を追加
		m_log.info("logfile " + logfile);

		MonitorLogfileInfoEntity entity = new MonitorLogfileInfoEntity(monitorEntity);
		entity.setDirectory(logfile.getDirectory());
		entity.setFileName(logfile.getFileName());
		entity.setFileEncoding(logfile.getFileEncoding());
		entity.setFileReturnCode(logfile.getFileReturnCode());
		m_log.trace("add() : entity.getDirectory = " + entity.getDirectory());
		m_log.trace("add() : entity.getFileName = " + entity.getFileName());
		m_log.trace("add() : entity.getFileEncoding = " + entity.getFileEncoding());
		m_log.trace("add() : entity.getFileReturnCode = " + entity.getFileReturnCode());

		return true;
	}

	/**
	 * ログファイル監視情報を変更します。<BR>
	 * 
	 * @param logfile ログファイル監視情報
	 * @return 成功した場合、true
	 * @throws MonitorNotFound
	 * @version 4.1.0
	 * @since 4.0.0
	 */
	public boolean modify(LogfileCheckInfo logfile) throws MonitorNotFound {

		// ログファイル監視情報を取得
		MonitorLogfileInfoEntity entity = QueryUtil.getMonitorLogfileInfoPK(m_monitorId);

		// ログファイル監視情報を設定
		entity.setDirectory(logfile.getDirectory());
		entity.setFileName(logfile.getFileName());
		entity.setFileEncoding(logfile.getFileEncoding());
		entity.setFileReturnCode(logfile.getFileReturnCode());
		m_log.trace("modify() : entity.getDirectory = " + entity.getDirectory());
		m_log.trace("modify() : entity.getFileName = " + entity.getFileName());
		m_log.trace("modify() : entity.getFileEncoding = " + entity.getFileEncoding());
		m_log.trace("modify() : entity.getFileReturnCode = " + entity.getFileReturnCode());
		return true;
	}
}
