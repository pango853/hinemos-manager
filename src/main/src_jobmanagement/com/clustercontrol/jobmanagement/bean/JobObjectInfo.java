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
 * ジョブの待ち条件の判定対象に関する情報を保持するクラス<BR>
 * 
 * @version 2.0.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobObjectInfo implements Serializable, Comparable<JobObjectInfo> {

	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = -4050301670424654620L;

	/** 判定対象種別 */
	private Integer m_type = new Integer(0);

	/** ジョブID */
	private String m_jobId;

	/** ジョブ名 */
	private String m_jobName;

	/** 値 */
	private Integer m_value = new Integer(0);

	/** 時刻 */
	private Long m_time;

	/** セッション開始時の時間（分） */
	private Integer m_startMinute = new Integer(0);

	/**
	 * 待ち条件の判定対象となるジョブIDを返す。<BR>
	 * @return 待ち条件の判定対象となるジョブID
	 */
	public String getJobId() {
		return m_jobId;
	}

	/**
	 * 待ち条件の判定対象となるジョブIDを設定する。<BR>
	 * @param jobId 待ち条件となる判定対象となるジョブID
	 */
	public void setJobId(String jobId) {
		this.m_jobId = jobId;
	}

	/**
	 * 待ち条件の時刻を返す。<BR>
	 * @return 待ち条件の時刻
	 */
	public Long getTime() {
		return m_time;
	}

	/**
	 * 待ち条件の時刻を設定する。<BR>
	 * @param time 待ち条件の時刻
	 */
	public void setTime(Long time) {
		this.m_time = time;
	}

	/**
	 * 待ち条件の判定対象となるジョブの値（終了値 or 終了状態）を返す。<BR>
	 * @return 待ち条件の判定対象となるジョブの値（終了値 or 終了状態）
	 * @see com.clustercontrol.bean.EndStatusConstant
	 */
	public Integer getValue() {
		return m_value;
	}

	/**
	 * 待ち条件の判定対象となるジョブの値（終了値 or 終了状態）を設定する。<BR>
	 * @param value 待ち条件の判定対象となるジョブの値（終了値 or 終了状態）
	 * @see com.clustercontrol.bean.EndStatusConstant
	 */
	public void setValue(Integer value) {
		this.m_value = value;
	}

	/**
	 * 判定対象種別を返す。<BR>
	 * @return 判定対象種別
	 * @see com.clustercontrol.bean.JudgmentObjectConstant
	 */
	public Integer getType() {
		return m_type;
	}

	/**
	 * 判定対象種別を設定する。<BR>
	 * @param type 判定対象種別
	 * @see com.clustercontrol.bean.JudgmentObjectConstant
	 */
	public void setType(Integer type) {
		this.m_type = type;
	}

	/**
	 * ジョブ名を返す。<BR>
	 * @return ジョブ名
	 */
	public String getJobName() {
		return m_jobName;
	}

	/**
	 * ジョブ名を設定する。<BR>
	 * @param jobName ジョブ名
	 */
	public void setJobName(String jobName) {
		this.m_jobName = jobName;
	}

	/**
	 * セッション開始時の時間（分）を設定する。<BR>
	 * @param startMinute セッション開始時の時間（分）
	 */
	public void setStartMinute(Integer startMinute) {
		this.m_startMinute = startMinute;
	}
	/**
	 * セッション開始時の時間（分）を返す。<BR>
	 * @return セッション開始時の時間（分）
	 */
	public Integer getStartMinute() {
		return m_startMinute;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JobObjectInfo)) {
			return false;
		}
		JobObjectInfo o1 = this;
		JobObjectInfo o2 = (JobObjectInfo)o;

		boolean ret = false;
		ret = 	equalsSub(o1.getType(), o2.getType()) &&
				equalsSub(o1.getJobId(), o2.getJobId()) &&
				equalsSub(o1.getValue(), o2.getValue()) &&
				equalsSub(o1.getTime(), o2.getTime()) &&
				equalsSub(o1.getStartMinute(), o2.getStartMinute());
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
	public int compareTo(JobObjectInfo o) {
		return ("" + this.m_jobId + this.m_jobName + this.m_type + this.m_value + this.m_time + this.m_startMinute).compareTo
				("" + o.m_jobId + o.m_jobName + o.m_type + o.m_value + o.m_time + o.m_startMinute);
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
	public static void testEquals(){

		System.out.println("=== JobObjectInfo の単体テスト ===");

		System.out.println("*** 全部一致 ***");
		JobObjectInfo info1 = new JobObjectInfo();
		info1.setType(0);
		info1.setJobId("testJob");
		info1.setJobName("テストジョブ");
		info1.setValue(0);
		info1.setTime(0L);
		info1.setStartMinute(0);

		JobObjectInfo info2 = new JobObjectInfo();
		info2.setType(0);
		info2.setJobId("testJob");
		info2.setJobName("テストジョブ");
		info2.setValue(0);
		info2.setTime(0L);
		info2.setStartMinute(0);

		judge(true,info1.equals(info2));

		System.out.println("*** 「判定対象種別」のみ違う ***");
		info2 = new JobObjectInfo();
		info2.setType(1);
		info2.setJobId("testJob");
		info2.setJobName("テストジョブ");
		info2.setValue(0);
		info2.setTime(0L);
		info2.setStartMinute(0);

		judge(false,info1.equals(info2));

		System.out.println("*** 「ジョブID」のみ違う ***");
		info2 = new JobObjectInfo();
		info2.setType(0);
		info2.setJobId("testJob ");
		info2.setJobName("テストジョブ");
		info2.setValue(0);
		info2.setTime(0L);
		info2.setStartMinute(0);

		judge(false,info1.equals(info2));

		System.out.println("*** 「ジョブ名」のみ違う ***");
		info2 = new JobObjectInfo();
		info2.setType(0);
		info2.setJobId("testJob");
		info2.setJobName("テストジョブ ");
		info2.setValue(0);
		info2.setTime(0L);
		info2.setStartMinute(0);

		judge(false,info1.equals(info2));

		System.out.println("*** 「値」のみ違う ***");
		info2 = new JobObjectInfo();
		info2.setType(0);
		info2.setJobId("testJob");
		info2.setJobName("テストジョブ ");
		info2.setValue(1);
		info2.setTime(0L);
		info2.setStartMinute(0);

		judge(false,info1.equals(info2));

		System.out.println("*** 「時刻」のみ違う ***");
		info2 = new JobObjectInfo();
		info2.setType(0);
		info2.setJobId("testJob");
		info2.setJobName("テストジョブ ");
		info2.setValue(0);
		info2.setTime(1L);
		info2.setStartMinute(0);

		judge(false,info1.equals(info2));

		System.out.println("*** 「セッション開始時の時間（分）」のみ違う ***");
		info2 = new JobObjectInfo();
		info2.setType(0);
		info2.setJobId("testJob");
		info2.setJobName("テストジョブ ");
		info2.setValue(0);
		info2.setTime(0L);
		info2.setStartMinute(1);

		judge(false,info1.equals(info2));
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