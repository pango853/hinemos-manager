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

package com.clustercontrol.notify.bean;


import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 通知グループ情報を保持するクラス
 * 
 * 
 * 通知グループ情報とは、ジョブ、監視、メンテナンスなどで複数設定される
 * 通知情報をまとめる関連テーブルであり、notifyGroupInfoは、ジョブ、
 * 各種監視、メンテナンスにまたがって、一意に定まるIDとする。
 * notifyGroupIdは、com.clustercontrol.bean.HinemosModuleConstantで
 * 定義される識別子を必ず先頭に持つ（識別子により名前空間を分割する）
 *
 * @version 3.0.0
 * @since 2.0.0
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
public class NotifyRelationInfo implements java.io.Serializable, Comparable<NotifyRelationInfo> {
	private static final long serialVersionUID = 8167006206400666323L;

	/** ログ出力のインスタンス<BR> */
	private static Log m_log = LogFactory.getLog( NotifyRelationInfo.class );

	/*通知グループID*/
	private String notifyGroupId;
	/*通知ID*/
	private String notifyId;
	/*通知タイプ*/
	private Integer notifyType;


	/**
	 * コンストラクター
	 * 
	 */
	public NotifyRelationInfo() {

	}

	/**
	 * コンストラクター
	 * 
	 * @param notifyGroupId
	 * @param notifyId
	 * @param notifyType
	 */
	public NotifyRelationInfo(String notifyGroupId, String notifyId,
			Integer notifyType) {

		this.notifyGroupId = notifyGroupId;
		this.notifyId = notifyId;
		this.notifyType = notifyType;
	}

	/**
	 * 通知グループIDを返します。
	 * 
	 * @return
	 */
	public String getNotifyGroupId() {
		return notifyGroupId;
	}
	/**
	 * 通知グループIDを設定します。
	 * 
	 * @param notifyGroupId
	 */
	public void setNotifyGroupId(String notifyGroupId) {
		this.notifyGroupId = notifyGroupId;
	}
	/**
	 * 通知IDを返します。
	 * 
	 * @return
	 */
	public String getNotifyId() {
		return notifyId;
	}
	/**
	 * 通知IDを設定します。
	 * 
	 * @param notifyId
	 */
	public void setNotifyId(String notifyId) {
		this.notifyId = notifyId;
	}
	/**
	 * 通知タイプを返します。
	 * 
	 * @return 通知タイプ
	 * 
	 * @see com.clustercontrol.bean.NotifyTypeConstant
	 */
	public Integer getNotifyType() {
		return notifyType;
	}
	/**
	 * 通知タイプを設定します。
	 * @param notifyType
	 * 
	 * @see com.clustercontrol.bean.NotifyTypeConstant
	 */
	public void setNotifyType(Integer notifyType) {
		this.notifyType = notifyType;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof NotifyRelationInfo)) {
			return false;
		}
		NotifyRelationInfo o1 = this;
		NotifyRelationInfo o2 = (NotifyRelationInfo)o;

		boolean ret = false;
		ret = 	equalsSub(o1.getNotifyGroupId(), o2.getNotifyGroupId()) &&
				equalsSub(o1.getNotifyId(), o2.getNotifyId()) &&
				equalsSub(o1.getNotifyType(), o2.getNotifyType());

		if (!ret) {
			boolean notifyGroupId = equalsSub(o1.getNotifyGroupId(), o2.getNotifyGroupId());
			boolean notifyId = equalsSub(o1.getNotifyId(), o2.getNotifyId());
			boolean notifyType = equalsSub(o1.getNotifyType(), o2.getNotifyType());
			m_log.debug("notifyGroupId = " + notifyGroupId);
			m_log.debug("notifyId = " + notifyId);
			m_log.debug("notifyType = " + notifyType);
		}

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

	@Override
	public int compareTo(NotifyRelationInfo o) {
		return (this.notifyGroupId + this.notifyId + this.notifyType).compareTo(
				o.notifyGroupId + o.notifyId + o.notifyType);
	}

	/**
	 * 単体テスト用
	 * @param args
	 */
	public static void main (String args[]) {
		testEquals();
	}
	public static void testEquals() {

		System.out.println("=== NotifyRelationInfo の単体テスト ===");

		System.out.println("*** 全部一致 ***");
		NotifyRelationInfo info1 = new NotifyRelationInfo();
		info1.setNotifyGroupId("notifyGroup");
		info1.setNotifyId("notify");
		info1.setNotifyType(0);
		NotifyRelationInfo info2 = new NotifyRelationInfo();
		info2.setNotifyGroupId("notifyGroup");
		info2.setNotifyId("notify");
		info2.setNotifyType(0);

		judge(true, info1.equals(info2));

		System.out.println("*** 通知グループIDのみ違う ***");
		info2 = new NotifyRelationInfo();
		info2.setNotifyGroupId("notifyGroup_1");
		info2.setNotifyId("notify");
		info2.setNotifyType(0);

		judge(false, info1.equals(info2));

		System.out.println("*** 通知IDのみ違う ***");
		info2 = new NotifyRelationInfo();
		info2.setNotifyGroupId("notifyGroup");
		info2.setNotifyId("notify_1");
		info2.setNotifyType(0);

		judge(false, info1.equals(info2));

		System.out.println("*** 通知タイプのみ違う ***");
		info2 = new NotifyRelationInfo();
		info2.setNotifyGroupId("notifyGroup");
		info2.setNotifyId("notify");
		info2.setNotifyType(1);

		judge(false, info1.equals(info2));

		System.out.println("*** 通知フラグのみ違う ***");
		info2 = new NotifyRelationInfo();
		info2.setNotifyGroupId("notifyGroup");
		info2.setNotifyId("notify");
		info2.setNotifyType(0);

		judge(false, info1.equals(info2));
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
