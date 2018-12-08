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

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ジョブのコマンドに関する情報を保持するクラス
 *
 * @version 2.0.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobCommandInfo implements Serializable {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = 333607610499761260L;

	private static Log m_log = LogFactory.getLog( JobCommandInfo.class );

	/** ファシリティID */
	private String m_facilityID;

	/** スコープ */
	private String m_scope;

	/** スコープ処理 */
	private Integer m_processingMethod = new Integer(0);

	/** 起動コマンド */
	private String m_startCommand;

	/** コマンド停止方式 */
	private Integer m_stopType;

	/** 停止コマンド */
	private String m_stopCommand;

	/** ユーザ種別 */
	private Integer m_specifyUser = new Integer(0);

	/** 実効ユーザ */
	private String m_user;

	/** リトライ回数 */
	private Integer m_messageRetry;

	/** コマンド実行失敗時終了フラグ */
	private Integer m_messageRetryEndFlg = new Integer(0);

	/** コマンド実行失敗時終了値 */
	private Integer m_messageRetryEndValue = new Integer(0);

	/** 正常終了するまでリトライフラグ */
	private Integer m_commandRetryFlg = new Integer(0);

	/** 正常終了するまでリトライ回数 */
	private Integer m_commandRetry;


	/**
	 * コマンド実行失敗時終了フラグを返す。<BR>
	 * @return コマンド実行失敗時終了フラグ
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public Integer getMessageRetryEndFlg() {
		return m_messageRetryEndFlg;
	}

	/**
	 * コマンド実行失敗時終了フラグを設定する。<BR>
	 * @param messageRetryEndFlg コマンド実行失敗時終了フラグ
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public void setMessageRetryEndFlg(Integer messageRetryEndFlg) {
		this.m_messageRetryEndFlg = messageRetryEndFlg;
	}

	/**
	 * コマンド実行失敗時終了値を返す。<BR>
	 * @return コマンド実行失敗時終了値
	 */
	public Integer getMessageRetryEndValue() {
		return m_messageRetryEndValue;
	}

	/**
	 * コマンド実行失敗時終了値を設定する。<BR>
	 * @param messageRetryEndValue コマンド実行失敗時終了値
	 */
	public void setMessageRetryEndValue(Integer messageRetryEndValue) {
		this.m_messageRetryEndValue = messageRetryEndValue;
	}

	/**
	 * スコープを返す。<BR>
	 * @return スコープ
	 */
	public String getScope() {
		return m_scope;
	}

	/**
	 * スコープを設定する。<BR>
	 * @param scope スコープ
	 */
	public void setScope(String scope) {
		this.m_scope = scope;
	}

	/**
	 * 起動コマンドを返す。<BR>
	 * @return 起動コマンド
	 */
	public String getStartCommand() {
		return m_startCommand;
	}

	/**
	 * 起動コマンドを設定する。<BR>
	 * @param startCommand 起動コマンド
	 */
	public void setStartCommand(String startCommand) {
		this.m_startCommand = startCommand;
	}

	/**
	 * ファシリティIDを返す。<BR>
	 * @return ファシリティID
	 */
	public String getFacilityID() {
		return m_facilityID;
	}

	/**
	 * ファシリティIDを設定する。<BR>
	 * @param facilityID ファシリティID
	 */
	public void setFacilityID(String facilityID) {
		this.m_facilityID = facilityID;
	}

	/**
	 * 停止種別を返す。<BR>
	 * @return 停止種別
	 */
	public Integer getStopType() {
		return m_stopType;
	}

	/**
	 * 停止種別を設定する。<BR>
	 * @param stopType 停止種別
	 */
	public void setStopType(Integer stopType) {
		this.m_stopType = stopType;
	}

	/**
	 * 停止コマンドを返す。<BR>
	 * @return 停止コマンド
	 */
	public String getStopCommand() {
		return m_stopCommand;
	}

	/**
	 * 停止コマンドを設定する。<BR>
	 * @param stopCommand 停止コマンド
	 */
	public void setStopCommand(String stopCommand) {
		this.m_stopCommand = stopCommand;
	}

	/**
	 * スコープ処理を返す。<BR>
	 * @return スコープ処理
	 * @see com.clustercontrol.bean.ProcessingMethodConstant
	 */
	public Integer getProcessingMethod() {
		return m_processingMethod;
	}

	/**
	 * スコープ処理を設定する。<BR>
	 * @param processingMethod スコープ処理
	 * @see com.clustercontrol.bean.ProcessingMethodConstant
	 */
	public void setProcessingMethod(Integer processingMethod) {
		this.m_processingMethod = processingMethod;
	}

	/**
	 * ユーザ種別を返す。<BR>
	 * @return ユーザ種別
	 */
	public Integer getSpecifyUser() {
		return m_specifyUser;
	}

	/**
	 * ユーザ種別を設定する。<BR>
	 * @param specifyUser ユーザ種別
	 */
	public void setSpecifyUser(Integer specifyUser) {
		this.m_specifyUser = specifyUser;
	}

	/**
	 * 実効ユーザを返す。<BR>
	 * @return 実効ユーザ
	 */
	public String getUser() {
		return m_user;
	}

	/**
	 * 実効ユーザを設定する。<BR>
	 * @param user 実効ユーザ
	 */
	public void setUser(String user) {
		this.m_user = user;
	}


	/**
	 * リトライ回数を返す。<BR>
	 * @return リトライ回数
	 */
	public Integer getMessageRetry() {
		return m_messageRetry;
	}

	/**
	 * リトライ回数を設定する。<BR>
	 * @param messageRetry リトライ回数
	 */
	public void setMessageRetry(Integer messageRetry) {
		this.m_messageRetry = messageRetry;
	}


	/**
	 * 正常終了するまでリトライフラグを返す。<BR>
	 * @return 正常終了するまでリトライフラグ
	 */
	public Integer getCommandRetryFlg() {
		return m_commandRetryFlg;
	}

	/**
	 * 正常終了するまでリトライフラグを設定する。<BR>
	 * @param errorRetryFlg 正常終了するまでリトライフラグ
	 */
	public void setCommandRetryFlg(Integer commandRetryFlg) {
		this.m_commandRetryFlg = commandRetryFlg;
	}


	/**
	 * 正常終了するまでリトライ回数を返す。<BR>
	 * @return 正常終了するまでリトライ回数
	 */
	public Integer getCommandRetry() {
		return m_commandRetry;
	}

	/**
	 * 正常終了するまでリトライ回数を設定する。<BR>
	 * @param commandRetry 正常終了するまでリトライ回数
	 */
	public void setCommandRetry(Integer commandRetry) {
		this.m_commandRetry = commandRetry;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JobCommandInfo)) {
			return false;
		}
		JobCommandInfo o1 = this;
		JobCommandInfo o2 = (JobCommandInfo)o;

		boolean ret = false;
		ret = 	equalsSub(o1.getMessageRetryEndFlg(), o2.getMessageRetryEndFlg()) &&
				equalsSub(o1.getMessageRetryEndValue(), o2.getMessageRetryEndValue()) &&
				equalsSub(o1.getFacilityID(), o2.getFacilityID()) &&
				equalsSub(o1.getMessageRetry(), o2.getMessageRetry()) &&
				equalsSub(o1.getCommandRetryFlg(), o2.getCommandRetryFlg()) &&
				equalsSub(o1.getCommandRetry(), o2.getCommandRetry()) &&
				equalsSub(o1.getProcessingMethod(), o2.getProcessingMethod()) &&
				equalsSub(o1.getSpecifyUser(), o2.getSpecifyUser()) &&
				equalsSub(o1.getStartCommand(), o2.getStartCommand()) &&
				equalsSub(o1.getStopCommand(), o2.getStopCommand()) &&
				equalsSub(o1.getStopType(), o2.getStopType()) &&
				equalsSub(o1.getUser(), o2.getUser());
		return ret;
	}

	private boolean equalsSub(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return true;
		}
		if (o1 != null && o2 == null) {
			return false;
		}
		if (o1 == null && o2 != null) {
			return false;
		}
		boolean ret = o1.equals(o2);
		if (!ret) {
			if (m_log.isTraceEnabled()) {
				m_log.trace("equalsSub : " + o1 + "!=" + o2);
			}
		}
		return ret;
	}
	/**
	 * 単体テスト用
	 * @param args
	 */
	public static void main (String args[]) {
		testEquals();
	}
	/**
	 * 単体テスト
	 */
	public static void testEquals() {
		System.out.println("*** ALL ***");
		JobCommandInfo info1 = createSampleInfo();
		JobCommandInfo info2 = createSampleInfo();
		judge(true,info1.equals(info2));

		String[] str = {
				"コマンド実行失敗時終了フラグ",
				"コマンド実行失敗時終了値",
				"ファシリティID",
				"リトライ回数",
				"スコープ処理",
				"スコープ",
				"ユーザ種別",
				"起動コマンド",
				"停止コマンド",
				"コマンド停止方式",
				"実効ユーザ"
		};
		/**
		 * カウントアップするごとに
		 * パラメータ1つ変えて単体テスト実行する
		 */
		for (int i = 0; i < 11; i++) {
			info2 = createSampleInfo();
			switch (i) {
			case 0 :
				info2.setMessageRetryEndFlg(1);
				break;
			case 1 :
				info2.setMessageRetryEndValue(1);
				break;
			case 2 :
				info2.setFacilityID("facility_ID");
				break;
			case 3 :
				info2.setMessageRetry(1);
				break;
			case 4 :
				info2.setProcessingMethod(1);
				break;
			case 5 :
				info2.setScope("Stope");
				break;
			case 6 :
				info2.setSpecifyUser(1);
				break;
			case 7 :
				info2.setStartCommand("echo");
				break;
			case 8 :
				info2.setStopCommand("echo 1");
				break;
			case 9 :
				info2.setStopType(1);
				break;
			case 10 :
				info2.setUser("admin");
				break;
			}
			System.out.println("*** 「" + str[i] + "」 のみ違う***");
			judge(false,info1.equals(info2));
		}
	}
	/**
	 * 単体テスト用
	 * メンバ変数が比較できればいいので、値は適当
	 * @return
	 */
	public static JobCommandInfo createSampleInfo() {
		JobCommandInfo info = new JobCommandInfo();
		info.setMessageRetryEndFlg(0);
		info.setMessageRetryEndValue(0);
		info.setFacilityID("facilityID");
		info.setMessageRetry(0);
		info.setProcessingMethod(0);
		info.setScope("Scope");
		info.setSpecifyUser(0);
		info.setStartCommand("ls");
		info.setStopCommand("ls -l");
		info.setStopType(0);
		info.setUser("root");
		return info;
	}

	/**
	 * 単体テストの結果が正しいものか判断する
	 * @param judge
	 * @param result
	 */
	private static void judge(boolean judge, boolean result){

		System.out.println("expect : " + judge);
		System.out.print("result : " + result);
		String ret = "NG";
		if (judge == result) {
			ret = "OK";
		}
		System.out.println("    is ...  " + ret);
	}
}