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

/**
 * ジョブのファイル転送に関する情報を保持するクラス<BR>
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobFileInfo implements Serializable {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = 6448926354392693297L;

	/** スコープ処理方法 */
	private Integer m_processingMethod = new Integer(0);

	/** 転送ファシリティID */
	private String m_srcFacilityID;

	/** 受信ファシリティID */
	private String m_destFacilityID;

	/** 転送スコープ */
	private String m_srcScope;

	/** 受信スコープ */
	private String m_destScope;

	/** ファイル */
	private String m_srcFile;

	/** 転送作業ディレクトリ */
	private String m_srcWorkDir = "";

	/** 受信ディレクトリ */
	private String m_destDirectory;

	/** 受信作業ディレクトリ */
	private String m_destWorkDir = "";

	/** ファイル圧縮 */
	private Integer m_compressionFlg = new Integer(0);

	/** ファイルチェック */
	private Integer m_checkFlg = new Integer(0);

	/** ユーザ種別 */
	private Integer m_specifyUser = new Integer(0);

	/** 実効ユーザ */
	private String m_user;

	/** リトライ回数 */
	private Integer m_messageRetry = new Integer(0);
	
	/** コマンド実行失敗時終了フラグ */
	private Integer m_messageRetryEndFlg = new Integer(0);

	/** コマンド実行失敗時終了値 */
	private Integer m_messageRetryEndValue = new Integer(0);

	/** 正常終了するまでリトライフラグ */
	private Integer m_commandRetryFlg = new Integer(0);

	/** 正常終了するまでリトライ回数 */
	private Integer m_commandRetry;

	/**
	 * ファイル圧縮をするかしないかを返す。<BR>
	 * @return ファイル圧縮のするかしないか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public Integer getCompressionFlg() {
		return m_compressionFlg;
	}

	/**
	 * ファイル圧縮をするかしないかを設定する。<BR>
	 * @param compressionFlg ファイル圧縮をするかしないか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public void setCompressionFlg(Integer compressionFlg) {
		this.m_compressionFlg = compressionFlg;
	}

	/**
	 * ファイルチェックをするかしないかを返す。<BR>
	 * @return ファイルチェックをするかしないか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public Integer getCheckFlg() {
		return m_checkFlg;
	}

	/**
	 * ファイルチェックをするかしないかを設定する。<BR>
	 * @param checkFlg ファイルチェックをするかしないか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public void setCheckFlg(Integer checkFlg) {
		this.m_checkFlg = checkFlg;
	}

	/**
	 * 転送元のスコープを返す。<BR>
	 * @return 転送元のスコープ
	 */
	public String getSrcScope() {
		return m_srcScope;
	}

	/**
	 * 転送元のスコープを設定する。<BR>
	 * @param srcScope 転送元のスコープ
	 */
	public void setSrcScope(String srcScope) {
		this.m_srcScope = srcScope;
	}

	/**
	 * 受信スコープを返す。<BR>
	 * @return 受信スコープ
	 */
	public String getDestScope() {
		return m_destScope;
	}

	/**
	 * 受信スコープを設定する。<BR>
	 * @param destScope 受信スコープ
	 */
	public void setDestScope(String destScope) {
		this.m_destScope = destScope;
	}

	/**
	 * 転送元のファシリティIDを返す。<BR>
	 * @return 転送元のファシリティID
	 */
	public String getSrcFacilityID() {
		return m_srcFacilityID;
	}

	/**
	 * 転送元のファシリティIDを設定する。<BR>
	 * @param srcFacilityID 転送元のファシリティID
	 */
	public void setSrcFacilityID(String srcFacilityID) {
		this.m_srcFacilityID = srcFacilityID;
	}

	/**
	 * 受信ファシリティIDを返す。<BR>
	 * @return 受信ファシリティID
	 */
	public String getDestFacilityID() {
		return m_destFacilityID;
	}

	/**
	 * 受信ファシリティIDを設定する。<BR>
	 * @param destFacilityID 受信ファシリティID
	 */
	public void setDestFacilityID(String destFacilityID) {
		this.m_destFacilityID = destFacilityID;
	}

	/**
	 * 転送するファイルのパスを返す。<BR>
	 * @return 転送するファイルのパス
	 */
	public String getSrcFile() {
		return m_srcFile;
	}

	/**
	 * 転送するファイルのパスを設定する。<BR>
	 * @param srcFile ファイル
	 */
	public void setSrcFile(String srcFile) {
		this.m_srcFile = srcFile;
	}

	/**
	 * 転送作業ディレクトリを返す。<BR>
	 * @return 転送作業ディレクトリ
	 */
	public String getSrcWorkDir() {
		return m_srcWorkDir;
	}

	/**
	 * 転送作業ディレクトリを設定する。<BR>
	 * @param srcWorkDir 転送作業ディレクトリ
	 */
	public void setSrcWorkDir(String srcWorkDir) {
		this.m_srcWorkDir = srcWorkDir;
	}

	/**
	 * 受信するディレクトリのパスを返す。<BR>
	 * @return 受信ディレクトリ
	 */
	public String getDestDirectory() {
		return m_destDirectory;
	}

	/**
	 * 受信するディレクトリのパスを設定する。<BR>
	 * @param destDirectory 受信ディレクトリ
	 */
	public void setDestDirectory(String destDirectory) {
		this.m_destDirectory = destDirectory;
	}

	/**
	 * 受信作業ディレクトリを返す
	 * @return 受信作業ディレクトリ
	 */
	public String getDestWorkDir() {
		return m_destWorkDir;
	}

	/**
	 * 受信作業ディレクトリを設定する
	 * @param destWorkDir 受信作業ディレクトリ
	 */
	public void setDestWorkDir(String destWorkDir) {
		this.m_destWorkDir = destWorkDir;
	}

	/**
	 * スコープの処理方法を返す。<BR>
	 * @return スコープ処理方法
	 * @see com.clustercontrol.bean.ProcessingMethodConstant
	 */
	public Integer getProcessingMethod() {
		return m_processingMethod;
	}

	/**
	 * スコープの処理方法を設定する。<BR>
	 * @param processingMethod スコープ処理方法
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
	 * @param userType ユーザ種別
	 */
	public void setSpecifyUser(Integer specifyUser) {
		this.m_specifyUser = specifyUser;;
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
	 * コマンド実行失敗時終了フラグを返す。<BR>
	 * @return コマンド実行失敗時終了フラグ
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public Integer getMessageRetryEndFlg() {
		return m_messageRetryEndFlg;
	}

	/**
	 * コマンド実行失敗時終了フラグを設定する。<BR>
	 * @param errorEndFlg コマンド実行失敗時終了フラグ
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
	 * @param errorEndValue コマンド実行失敗時終了値
	 */
	public void setMessageRetryEndValue(Integer messageRetryEndValue) {
		this.m_messageRetryEndValue = messageRetryEndValue;
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
	 * @param errorMessageRetry 正常終了するまでリトライ回数
	 */
	public void setCommandRetry(Integer commandRetry) {
		this.m_commandRetry = commandRetry;
	}


	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JobFileInfo)) {
			return false;
		}
		JobFileInfo o1 = this;
		JobFileInfo o2 = (JobFileInfo)o;

		boolean ret = false;
		ret = 	equalsSub(o1.getCheckFlg(), o2.getCheckFlg()) &&
				equalsSub(o1.getCompressionFlg(), o2.getCompressionFlg()) &&
				equalsSub(o1.getDestDirectory(), o2.getDestDirectory()) &&
				equalsSub(o1.getDestFacilityID(), o2.getDestFacilityID()) &&
				equalsSub(o1.getDestScope(), o2.getDestScope()) &&
				equalsSub(o1.getDestWorkDir(), o2.getDestWorkDir()) &&
				equalsSub(o1.getMessageRetry(), o2.getMessageRetry()) &&
				equalsSub(o1.getMessageRetryEndFlg(), o2.getMessageRetryEndFlg()) &&
				equalsSub(o1.getMessageRetryEndValue(), o2.getMessageRetryEndValue()) &&
				equalsSub(o1.getCommandRetryFlg(), o2.getCommandRetryFlg()) &&
				equalsSub(o1.getCommandRetry(), o2.getCommandRetry()) &&
				equalsSub(o1.getProcessingMethod(), o2.getProcessingMethod()) &&
				equalsSub(o1.getSpecifyUser(), o2.getSpecifyUser()) &&
				equalsSub(o1.getSrcFacilityID(), o2.getSrcFacilityID()) &&
				equalsSub(o1.getSrcFile(), o2.getSrcFile()) &&
				equalsSub(o1.getSrcScope(), o2.getSrcScope()) &&
				equalsSub(o1.getSrcWorkDir(), o2.getSrcWorkDir()) &&
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
		System.out.println("*** ALL *** \n⇒ true");
		JobFileInfo info1 = createSampleInfo();
		JobFileInfo info2 = createSampleInfo();
		System.out.println("ressult : " + info1.equals(info2));

		String[] str = {
				"ファイルチェック",
				"ファイル圧縮",
				"受信ディレクトリ",
				"受信ファシリティID",
				"受信スコープ",
				"受信作業ディレクトリ",
				"リトライ回数",
				"スコープ処理方法",
				"ユーザ種別",
				"転送ファシリティID",
				"ファイル",
				"転送スコープ",
				"転送作業ディレクトリ",
				"実効ユーザ"
		};
		/**
		 * カウントアップするごとに
		 * パラメータ1つ変えて単体テスト実行する
		 */
		for (int i = 0; i < 14; i++) {
			info2 = createSampleInfo();
			switch (i) {
			case 0 :
				info2.setCheckFlg(1);
				break;
			case 1 :
				info2.setCompressionFlg(1);
				break;
			case 2 :
				info2.setDestDirectory("/optopt/");
				break;
			case 3 :
				info2.setDestFacilityID("facility_Id");
				break;
			case 4 :
				info2.setDestScope("stope");
				break;
			case 5 :
				info2.setDestWorkDir("/opt/hii/");
				break;
			case 6 :
				info2.setMessageRetry(1);
				break;
			case 7:
				info2.setProcessingMethod(1);
				break;
			case 8 :
				info2.setSpecifyUser(1);
				break;
			case 9 :
				info2.setSrcFacilityID("srcFacility_Id");
				break;
			case 10 :
				info2.setSrcFile("test.txt.test");
				break;
			case 11 :
				info2.setSrcScope("nodeeen");
				break;
			case 12 :
				info2.setSrcWorkDir("/root/test/");
				break;
			case 13 :
				info2.setUser("admin");
				break;
			}
			System.out.println("*** 「" + str[i] + "」 のみ違う*** \n⇒ false");
			System.out.println("ressult : " + info1.equals(info2));
		}
	}
	/**
	 * 単体テスト用
	 * メンバ変数が比較できればいいので、値は適当
	 * @return
	 */
	public static JobFileInfo createSampleInfo() {
		JobFileInfo info = new JobFileInfo();
		info.setCheckFlg(0);
		info.setCompressionFlg(0);
		info.setDestDirectory("/opt/");
		info.setDestFacilityID("facilityId");
		info.setDestScope("scope");
		info.setDestWorkDir("/opt/hinemos/");
		info.setMessageRetry(0);
		info.setProcessingMethod(0);
		info.setSpecifyUser(0);
		info.setSrcFacilityID("srcFacilityId");
		info.setSrcFile("test.txt");
		info.setSrcScope("node");
		info.setSrcWorkDir("/root/");
		info.setUser("root");
		return info;
	}
}