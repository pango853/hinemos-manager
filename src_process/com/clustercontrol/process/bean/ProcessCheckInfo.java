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

package com.clustercontrol.process.bean;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.monitor.run.bean.MonitorCheckInfo;

/**
 * プロセス監視設定情報のBeanクラス<BR>
 * 
 * @version 4.1.0
 * @since 2.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class ProcessCheckInfo extends MonitorCheckInfo
{
	private static final long serialVersionUID = -5276197256254070640L;

	/** 監視コマンド文字列  */
	private String command="";

	/** パラメータ文字列  */
	private String param="";

	/** 大文字・小文字を区別するか否かのフラグ
	 * 
	 * @see com.clustercontrol.bean.ValidConstant
	 */
	private boolean m_caseSensitivityFlg = ValidConstant.BOOLEAN_INVALID;



	public ProcessCheckInfo()
	{
	}
	/**
	 * 監視コマンド文字列を取得します。
	 * @return
	 */
	public String getCommand()
	{
		return this.command;
	}
	/**
	 * 監視コマンド文字列を設定します。
	 * @param command
	 */
	public void setCommand( String command )
	{
		this.command = command;
	}
	/**
	 * パラメータ文字列を取得します。
	 * @return
	 */
	public String getParam()
	{
		return this.param;
	}
	/**
	 * パラメータ文字列を設定します。
	 * @param param
	 */
	public void setParam( String param )
	{
		this.param = param;
	}

	/**
	 * 大文字・小文字を区別するか否かのフラグを返します。
	 * 
	 * @return 大文字・小文字を区別するか否かのフラグ
	 */
	public boolean getCaseSensitivityFlg() {
		return m_caseSensitivityFlg;
	}

	/**
	 * 大文字・小文字を区別するか否かのフラグを設定します。
	 * 
	 * @param caseSensitivityFlg 大文字・小文字を区別するか否か
	 */
	public void setCaseSensitivityFlg(boolean caseSensitivityFlg) {
		this.m_caseSensitivityFlg = caseSensitivityFlg;
	}

}
