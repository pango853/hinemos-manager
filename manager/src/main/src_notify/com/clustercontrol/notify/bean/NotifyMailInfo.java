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
 * 通知メール情報を保持するクラスです。
 * 
 * @version 3.0.0
 * @since 3.0.0
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
public class NotifyMailInfo  extends NotifyInfoDetail
{
	private static final long serialVersionUID = -5089086735470199399L;


	/**
	 * メールテンプレートID。
	 * @see com.clustercontrol.bean.ValidConstant
	 */
	private String mailTemplateId;

	/** メールアドレス（セミコロン区切り）。 */
	private String infoMailAddress;
	private String warnMailAddress;
	private String criticalMailAddress;
	private String unknownMailAddress;


	/**
	 * コンストラクタ。
	 */
	public NotifyMailInfo() {
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
	public NotifyMailInfo(
			String notifyId,

			Integer infoValidFlg,
			Integer warnValidFlg,
			Integer criticalValidFlg,
			Integer unknownValidFlg,

			String infoMailAddress,
			String warnMailAddress,
			String criticalMailAddress,
			String unknownMailAddress,

			String mailTemplateId) {

		super(notifyId, infoValidFlg, warnValidFlg, criticalValidFlg, unknownValidFlg);

		setInfoMailAddress(infoMailAddress);
		setWarnMailAddress(warnMailAddress);
		setCriticalMailAddress(criticalMailAddress);
		setUnknownMailAddress(unknownMailAddress);

		setMailTemplateId(mailTemplateId);
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param otherData コピー元の通知情報
	 */
	public NotifyMailInfo( NotifyMailInfo otherData ) {
		super(otherData);
		setInfoMailAddress(otherData.getInfoMailAddress());
		setWarnMailAddress(otherData.getWarnMailAddress());
		setCriticalMailAddress(otherData.getCriticalMailAddress());
		setUnknownMailAddress(otherData.getUnknownMailAddress());

		setMailTemplateId(otherData.getMailTemplateId());
	}


	/**
	 * メールテンプレートIDを返します。
	 * 
	 * @return メールテンプレートID
	 * 
	 */
	public String getMailTemplateId() {
		return this.mailTemplateId;
	}

	/**
	 * メールテンプレートIDを設定します。
	 * 
	 * @param mailTamplateId メールテンプレートID
	 * 
	 */
	public void setMailTemplateId(String  mailTemplateId) {
		this.mailTemplateId = mailTemplateId;
	}

	public String getInfoMailAddress() {
		return infoMailAddress;
	}

	public void setInfoMailAddress(String infoMailAddress) {
		this.infoMailAddress = infoMailAddress;
	}

	public String getWarnMailAddress() {
		return warnMailAddress;
	}

	public void setWarnMailAddress(String warnMailAddress) {
		this.warnMailAddress = warnMailAddress;
	}

	public String getCriticalMailAddress() {
		return criticalMailAddress;
	}

	public void setCriticalMailAddress(String criticalMailAddress) {
		this.criticalMailAddress = criticalMailAddress;
	}

	public String getUnknownMailAddress() {
		return unknownMailAddress;
	}

	public void setUnknownMailAddress(String unknownMailAddress) {
		this.unknownMailAddress = unknownMailAddress;
	}
}
