/*

 Copyright (C) 2008 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.notify.bean;

import javax.xml.bind.annotation.XmlType;

/**
 * 通知ログエスカレート情報を保持するクラスです。
 * 
 * @version 3.0.0
 * @since 3.0.0
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
public class NotifyLogEscalateInfo  extends NotifyInfoDetail
{
	private static final long serialVersionUID = -5089086735470199399L;


	/**転送メッセージ*/
	private String  infoEscalateMessage;
	private String  warnEscalateMessage;
	private String  criticalEscalateMessage;
	private String  unknownEscalateMessage;

	/**シスログ重要度*/
	private Integer infoSyslogPriority;
	private Integer warnSyslogPriority;
	private Integer criticalSyslogPriority;
	private Integer unknownSyslogPriority;

	/**シスログファシリティ*/
	private Integer infoSyslogFacility;
	private Integer warnSyslogFacility;
	private Integer criticalSyslogFacility;
	private Integer unknownSyslogFacility;

	/**転送先ファシリティフラグ*/
	private Integer esaclateFacilityFlg;

	/**転送先ファシリティID*/
	private String  escalateFacility;

	/**転送先スコープテキスト*/
	private String	escalateScope;

	/**転送先ポート*/
	private Integer escalatePort;

	/**
	 * コンストラクタ。
	 */
	public NotifyLogEscalateInfo(){
	}


	/**
	 * コンストラクタ。
	 * 
	 * @param notifyId 通知ID
	 * @param priority 重要度
	 * @param mailTemplateId メールテンプレートID
	 * @param validFlg イベント通知フラグ
	 * @param mailAddress メールアドレス（セミコロン区切り）
	 * 
	 * @see com.clustercontrol.bean.PriorityConstant
	 * @see com.clustercontrol.bean.ValidConstant
	 * @see com.clustercontrol.bean.EventConfirmConstant
	 */
	public NotifyLogEscalateInfo(
			String notifyId,

			Integer infoValidFlg,
			Integer warnValidFlg,
			Integer criticalValidFlg,
			Integer unknownValidFlg,

			String infoEscalateMessage,
			String warnEscalateMessage,
			String criticalEscalateMessage,
			String unknownEscalateMessage,

			Integer infoSyslogPriority,
			Integer warnSyslogPriority,
			Integer criticalSyslogPriority,
			Integer unknownSyslogPriority,

			Integer infoSyslogFacility,
			Integer warnSyslogFacility,
			Integer criticalSyslogFacility,
			Integer unknownSyslogFacility,

			Integer escalateFacilityFlg,
			String escalateFacility,
			String escalateScope,
			Integer escalatePort) {

		super(notifyId, infoValidFlg, warnValidFlg, criticalValidFlg, unknownValidFlg);

		setInfoEscalateMessage(infoEscalateMessage);
		setWarnEscalateMessage(warnEscalateMessage);
		setCriticalEscalateMessage(criticalEscalateMessage);
		setUnknownEscalateMessage(unknownEscalateMessage);

		setInfoSyslogPriority(infoSyslogPriority);
		setWarnSyslogPriority(warnSyslogPriority);
		setCriticalSyslogPriority(criticalSyslogPriority);
		setUnknownSyslogPriority(unknownSyslogPriority);

		setInfoSyslogFacility(infoSyslogFacility);
		setWarnSyslogFacility(warnSyslogFacility);
		setCriticalSyslogFacility(criticalSyslogFacility);
		setUnknownSyslogFacility(unknownSyslogFacility);

		setEscalateFacilityFlg(escalateFacilityFlg);
		setEscalateFacility(escalateFacility);
		setEscalateScope(escalateScope);
		setEscalatePort(escalatePort);
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param otherData コピー元の通知情報
	 */
	public NotifyLogEscalateInfo( NotifyLogEscalateInfo otherData ) {
		super(otherData);

		setInfoEscalateMessage(otherData.getInfoEscalateMessage());
		setWarnEscalateMessage(otherData.getWarnEscalateMessage());
		setCriticalEscalateMessage(otherData.getCriticalEscalateMessage());
		setUnknownEscalateMessage(otherData.getUnknownEscalateMessage());

		setInfoSyslogPriority(otherData.getInfoSyslogPriority());
		setWarnSyslogPriority(otherData.getWarnSyslogPriority());
		setCriticalSyslogPriority(otherData.getCriticalSyslogPriority());
		setUnknownSyslogPriority(otherData.getUnknownSyslogPriority());

		setInfoSyslogFacility(otherData.getInfoSyslogFacility());
		setWarnSyslogFacility(otherData.getWarnSyslogFacility());
		setCriticalSyslogFacility(otherData.getCriticalSyslogFacility());
		setUnknownSyslogFacility(otherData.getUnknownSyslogFacility());

		setEscalateFacilityFlg(otherData.getEscalateFacilityFlg());
		setEscalateFacility(otherData.getEscalateFacility());
		setEscalateScope(otherData.getEscalateScope());
		setEscalatePort(otherData.getEscalatePort());
	}

	/**
	 * 転送先ファシリティフラグを返します。
	 * 
	 * @return 転送先ファシリティフラグ
	 */
	public Integer getEscalateFacilityFlg() {
		return esaclateFacilityFlg;
	}

	/**
	 * 転送先ファシリティフラグを設定します。
	 * 
	 * @param esaclateFacilityFlg
	 */
	public void setEscalateFacilityFlg(Integer esaclateFacilityFlg) {
		this.esaclateFacilityFlg = esaclateFacilityFlg;
	}

	/**
	 * 転送先ファシリティを返します。
	 * 
	 * @return　転送先ファシリティ
	 */
	public String getEscalateFacility() {

		return escalateFacility;
	}

	/**
	 * 転送先ファシリティを設定します。
	 * 
	 * @param escalateFacility
	 */
	public void setEscalateFacility(String escalateFacility) {
		this.escalateFacility = escalateFacility;
	}

	/**
	 * 転送先スコープを返します。
	 * 
	 * @return　転送先スコープ
	 */
	public String getEscalateScope() {

		return escalateScope;
	}

	/**
	 * 転送先スコープを設定します。
	 * 
	 * @param escalateScope
	 */
	public void setEscalateScope(String escalateScope) {
		this.escalateScope = escalateScope;
	}

	/**
	 * 転送先ポートを返します。
	 * 
	 * @return 転送先ポート
	 */
	public Integer getEscalatePort() {
		return escalatePort;
	}

	/**
	 * 転送先ポートを設定します。
	 * 
	 * @param escalatePort
	 */
	public void setEscalatePort(Integer escalatePort) {
		this.escalatePort = escalatePort;
	}


	public String getInfoEscalateMessage() {
		return infoEscalateMessage;
	}


	public void setInfoEscalateMessage(String infoEscalateMessage) {
		this.infoEscalateMessage = infoEscalateMessage;
	}


	public String getWarnEscalateMessage() {
		return warnEscalateMessage;
	}


	public void setWarnEscalateMessage(String warnEscalateMessage) {
		this.warnEscalateMessage = warnEscalateMessage;
	}


	public String getCriticalEscalateMessage() {
		return criticalEscalateMessage;
	}


	public void setCriticalEscalateMessage(String criticalEscalateMessage) {
		this.criticalEscalateMessage = criticalEscalateMessage;
	}


	public String getUnknownEscalateMessage() {
		return unknownEscalateMessage;
	}


	public void setUnknownEscalateMessage(String unknownEscalateMessage) {
		this.unknownEscalateMessage = unknownEscalateMessage;
	}


	public Integer getInfoSyslogPriority() {
		return infoSyslogPriority;
	}


	public void setInfoSyslogPriority(Integer infoSyslogPriority) {
		this.infoSyslogPriority = infoSyslogPriority;
	}


	public Integer getWarnSyslogPriority() {
		return warnSyslogPriority;
	}


	public void setWarnSyslogPriority(Integer warnSyslogPriority) {
		this.warnSyslogPriority = warnSyslogPriority;
	}


	public Integer getCriticalSyslogPriority() {
		return criticalSyslogPriority;
	}


	public void setCriticalSyslogPriority(Integer criticalSyslogPriority) {
		this.criticalSyslogPriority = criticalSyslogPriority;
	}


	public Integer getUnknownSyslogPriority() {
		return unknownSyslogPriority;
	}


	public void setUnknownSyslogPriority(Integer unknownSyslogPriority) {
		this.unknownSyslogPriority = unknownSyslogPriority;
	}


	public Integer getInfoSyslogFacility() {
		return infoSyslogFacility;
	}


	public void setInfoSyslogFacility(Integer infoSyslogFacility) {
		this.infoSyslogFacility = infoSyslogFacility;
	}


	public Integer getWarnSyslogFacility() {
		return warnSyslogFacility;
	}


	public void setWarnSyslogFacility(Integer warnSyslogFacility) {
		this.warnSyslogFacility = warnSyslogFacility;
	}


	public Integer getCriticalSyslogFacility() {
		return criticalSyslogFacility;
	}


	public void setCriticalSyslogFacility(Integer criticalSyslogFacility) {
		this.criticalSyslogFacility = criticalSyslogFacility;
	}


	public Integer getUnknownSyslogFacility() {
		return unknownSyslogFacility;
	}


	public void setUnknownSyslogFacility(Integer unknownSyslogFacility) {
		this.unknownSyslogFacility = unknownSyslogFacility;
	}



}
