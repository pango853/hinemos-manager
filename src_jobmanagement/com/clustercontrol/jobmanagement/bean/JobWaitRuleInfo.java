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
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.bean.YesNoConstant;

/**
 * ジョブの待ち条件に関する情報を保持するクラス<BR>
 * 
 * @version 2.1.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobWaitRuleInfo implements Serializable {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = -6362706494732152461L;

	/** ログ出力のインスタンス<BR> */
	private static Log m_log = LogFactory.getLog( JobWaitRuleInfo.class );

	/** 保留 */
	private Integer suspend = YesNoConstant.TYPE_NO;

	/** スキップ */
	private Integer skip = YesNoConstant.TYPE_NO;

	/** スキップ時終了状態 */
	private Integer skipEndStatus = new Integer(0);

	/** スキップ時終了値 */
	private Integer skipEndValue = EndStatusConstant.INITIAL_VALUE_NORMAL;

	/** 判定対象の条件関係 */
	private Integer condition = ConditionTypeConstant.TYPE_AND;

	/** ジョブ判定対象情報 */
	private ArrayList<JobObjectInfo> object;

	/** 条件を満たさなければ終了する */
	private Integer endCondition = YesNoConstant.TYPE_NO;

	/** 条件を満たさない時の終了状態 */
	private Integer endStatus = new Integer(0);

	/** 条件を満たさない時の終了値 */
	private Integer endValue = EndStatusConstant.INITIAL_VALUE_NORMAL;

	/** カレンダ */
	private Integer calendar = YesNoConstant.TYPE_NO;

	/** カレンダID */
	private String calendarId;

	/** カレンダにより未実行時の終了状態 */
	private Integer calendarEndStatus = EndStatusConstant.INITIAL_VALUE_NORMAL;

	/** カレンダにより未実行時の終了値 */
	private Integer calendarEndValue = EndStatusConstant.INITIAL_VALUE_NORMAL;

	/** 開始遅延 */
	private Integer start_delay = YesNoConstant.TYPE_NO;

	/** 開始遅延セッション開始後の時間 */
	private Integer start_delay_session = YesNoConstant.TYPE_NO;

	/** 開始遅延セッション開始後の時間の値 */
	private Integer start_delay_session_value = 1;

	/** 開始遅延時刻 */
	private Integer start_delay_time = YesNoConstant.TYPE_NO;

	/** 開始遅延時刻の値 */
	private Long start_delay_time_value = new Long(0);

	/** 開始遅延判定対象の条件関係 */
	private Integer start_delay_condition_type = ConditionTypeConstant.TYPE_AND;

	/** 開始遅延通知 */
	private Integer start_delay_notify = YesNoConstant.TYPE_NO;

	/** 開始遅延通知重要度 */
	private Integer start_delay_notify_priority = new Integer(0);

	/** 開始遅延操作 */
	private Integer start_delay_operation = YesNoConstant.TYPE_NO;

	/** 開始遅延操作種別 */
	private Integer start_delay_operation_type = new Integer(0);

	/** 開始遅延操作終了状態 */
	private Integer start_delay_operation_end_status = EndStatusConstant.INITIAL_VALUE_ABNORMAL;

	/** 開始遅延操作終了値 */
	private Integer start_delay_operation_end_value = EndStatusConstant.INITIAL_VALUE_NORMAL;

	/** 終了遅延 */
	private Integer end_delay = YesNoConstant.TYPE_NO;

	/** 終了遅延セッション開始後の時間 */
	private Integer end_delay_session = YesNoConstant.TYPE_NO;

	/** 終了遅延セッション開始後の時間の値 */
	private Integer end_delay_session_value = 1;

	/** 終了遅延ジョブ開始後の時間 */
	private Integer end_delay_job = YesNoConstant.TYPE_NO;

	/** 終了遅延ジョブ開始後の時間の値 */
	private Integer end_delay_job_value = 1;

	/** 終了遅延時刻 */
	private Integer end_delay_time = YesNoConstant.TYPE_NO;

	/** 終了遅延時刻の値 */
	private Long end_delay_time_value;

	/** 終了遅延判定対象の条件関係 */
	private Integer end_delay_condition_type = ConditionTypeConstant.TYPE_AND;

	/** 終了遅延通知 */
	private Integer end_delay_notify = YesNoConstant.TYPE_NO;

	/** 終了遅延通知重要度 */
	private Integer end_delay_notify_priority = new Integer(0);

	/** 終了遅延操作 */
	private Integer end_delay_operation = YesNoConstant.TYPE_NO;

	/** 終了遅延操作種別 */
	private Integer end_delay_operation_type = new Integer(0);

	/** 終了遅延操作終了状態 */
	private Integer end_delay_operation_end_status = EndStatusConstant.INITIAL_VALUE_ABNORMAL;

	/** 終了遅延操作終了値 */
	private Integer end_delay_operation_end_value = EndStatusConstant.INITIAL_VALUE_NORMAL;

	/** 多重度 */
	private Integer multiplicity_notify = new Integer(1);
	private Integer multiplicity_notify_priority = PriorityConstant.TYPE_WARNING;
	private Integer multiplicity_operation = StatusConstant.TYPE_WAIT;
	private Integer multiplicity_end_value = new Integer(-1);

	/**
	 * ジョブのスキップをするかしないかを返す。<BR>
	 * @return スキップをするかしないか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public Integer getSkip() {
		return skip;
	}

	/**
	 * ジョブのスキップをするかしないかを設定する。<BR>
	 * @param skip スキップするかしないか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public void setSkip(Integer skip) {
		this.skip = skip;
	}

	/**
	 * スキップ時の終了状態を返す。<BR>
	 * @return スキップ時終了状態
	 * @see com.clustercontrol.bean.EndStatusConstant
	 */
	public Integer getSkipEndStatus() {
		return skipEndStatus;
	}

	/**
	 * スキップ時の終了状態を設定する。<BR>
	 * @param endStatus スキップ時終了状態
	 * @see com.clustercontrol.bean.EndStatusConstant
	 */
	public void setSkipEndStatus(Integer endStatus) {
		this.skipEndStatus = endStatus;
	}

	/**
	 * スキップ時の終了値を返す。<BR>
	 * @return スキップ時終了値
	 */
	public Integer getSkipEndValue() {
		return skipEndValue;
	}

	/**
	 * スキップ時の終了値を設定する。<BR>
	 * @param endValue スキップ時終了値
	 */
	public void setSkipEndValue(Integer endValue) {
		this.skipEndValue = endValue;
	}

	/**
	 * 判定対象の条件関係を返す。<BR>
	 * @return 判定対象の条件関係
	 * @see com.clustercontrol.jobmanagement.bean.ConditionTypeConstant
	 */
	public Integer getCondition() {
		return condition;
	}

	/**
	 * 判定対象の条件関係を返す。<BR>
	 * @param condition 判定対象の条件関係
	 * @see com.clustercontrol.jobmanagement.bean.ConditionTypeConstant
	 */
	public void setCondition(Integer condition) {
		this.condition = condition;
	}

	/**
	 * 待ち条件を満たさない時の終了状態を返す。<BR>
	 * @return 待ち条件を満たさない時の終了状態
	 * @see com.clustercontrol.bean.EndStatusConstant
	 */
	public Integer getEndStatus() {
		return endStatus;
	}

	/**
	 * 待ち条件を満たさない時の終了状態を設定する。<BR>
	 * @param endStatus 待ち条件を満たさない時の終了状態
	 * @see com.clustercontrol.bean.EndStatusConstant
	 */
	public void setEndStatus(Integer endStatus) {
		this.endStatus = endStatus;
	}

	/**
	 * 待ち条件を満たさない時の終了値を返す。<BR>
	 * @return 待ち条件を満たさない時の終了値
	 */
	public Integer getEndValue() {
		return endValue;
	}

	/**
	 * 待ち条件を満たさない時の終了値を設定する。<BR>
	 * @param endValue 待ち条件を満たさない時の終了値
	 */
	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}

	/**
	 * ジョブ判定対象情報を返す。<BR>
	 * @return ジョブ判定対象情報のリスト
	 * @see com.clustercontrol.jobmanagement.bean.JobObjectInfo
	 */
	public ArrayList<JobObjectInfo> getObject() {
		return object;
	}

	/**
	 * ジョブ判定対象情報を設定する。<BR>
	 * @param object ジョブ判定対象情報のリスト
	 * @see com.clustercontrol.jobmanagement.bean.JobObjectInfo
	 */
	public void setObject(ArrayList<JobObjectInfo> object) {
		this.object = object;
	}

	/**
	 * 待ち条件を満たさなければ終了するかどうかを返す。<BR>
	 * 
	 * @return 待ち条件を満たさなければ終了するかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public Integer getEndCondition() {
		return endCondition;
	}

	/**
	 * まち条件を満たさなければ終了するかどうかを設定する。<BR>
	 * @param endCondition 待ち条件を満たさなければ終了するかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public void setEndCondition(Integer endCondition) {
		this.endCondition = endCondition;
	}

	/**
	 * 保留するかどうかを返す。<BR>
	 * @return 保留するかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public Integer getSuspend() {
		return suspend;
	}

	/**
	 * 保留するかどうかを設定する。<BR>
	 * @param suspend 保留するかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public void setSuspend(Integer suspend) {
		this.suspend = suspend;
	}

	/**
	 * カレンダを返す。<BR>
	 * @return カレンダ
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public Integer getCalendar() {
		return calendar;
	}

	/**
	 * カレンダを設定する。<BR>
	 * @param calendar カレンダ
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public void setCalendar(Integer calendar) {
		this.calendar = calendar;
	}

	/**
	 * カレンダIDを返す。<BR>
	 * @return カレンダID
	 */
	public String getCalendarId() {
		return calendarId;
	}

	/**
	 * カレンダIDを設定する。<BR>
	 * @param calendarId カレンダID
	 */
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	/**
	 * カレンダにより未実行となった場合の終了状態を返す。<BR>
	 * @return カレンダにより未実行となった場合の終了状態
	 */
	public Integer getCalendarEndStatus() {
		return calendarEndStatus;
	}

	/**
	 * カレンダにより未実行となった場合の終了状態を設定する。<BR>
	 * @param calendarEndValue カレンダにより未実行となった場合の終了状態
	 */
	public void setCalendarEndStatus(Integer calendarEndStatus) {
		this.calendarEndStatus = calendarEndStatus;
	}

	/**
	 * カレンダにより未実行となった場合の終了値を返す。<BR>
	 * @return カレンダにより未実行となった場合の終了値
	 */
	public Integer getCalendarEndValue() {
		return calendarEndValue;
	}

	/**
	 * カレンダにより未実行となった場合の終了値を設定する。<BR>
	 * @param calendarEndValue カレンダにより未実行となった場合の終了値
	 */
	public void setCalendarEndValue(Integer calendarEndValue) {
		this.calendarEndValue = calendarEndValue;
	}

	/**
	 * 終了遅延を監視するかどうかを返す。<BR>
	 * @return 終了遅延を監視するかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public Integer getEnd_delay() {
		return end_delay;
	}

	/**
	 * 終了遅延を監視するかどうかを設定する。<BR>
	 * @param end_delay 終了遅延を監視するかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public void setEnd_delay(Integer end_delay) {
		this.end_delay = end_delay;
	}

	/**
	 * 終了遅延判定対象の条件関係を返す。<BR>
	 * @return 終了遅延判定対象の条件関係
	 * @see com.clustercontrol.jobmanagement.bean.ConditionTypeConstant
	 */
	public Integer getEnd_delay_condition_type() {
		return end_delay_condition_type;
	}

	/**
	 * 終了遅延判定対象の条件関係を設定する。<BR>
	 * @param end_delay_condition_type 終了遅延判定対象の条件関係
	 * @see com.clustercontrol.jobmanagement.bean.ConditionTypeConstant
	 */
	public void setEnd_delay_condition_type(Integer end_delay_condition_type) {
		this.end_delay_condition_type = end_delay_condition_type;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * ジョブ開始後の時間で判定するかどうかを返す。<BR>
	 * @return ジョブ開始後の時間で終了遅延監視を行うかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public Integer getEnd_delay_job() {
		return end_delay_job;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * ジョブ開始後の時間で判定するかどうかを設定する。<BR>
	 * @param end_delay_job ジョブ開始後の時間で終了遅延監視を行うかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public void setEnd_delay_job(Integer end_delay_job) {
		this.end_delay_job = end_delay_job;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * ジョブ開始後の時間で判定する場合、<BR>
	 * 開始後何分で監視するのかの値を返す。<BR>
	 * @return 終了遅延ジョブ開始後の時間の値
	 */
	public Integer getEnd_delay_job_value() {
		return end_delay_job_value;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * ジョブ開始後の時間で判定する場合、<BR>
	 * 開始後何分で監視するかの値を設定する。<BR>
	 * @param end_delay_job_value 終了遅延ジョブ開始後の時間の値
	 */
	public void setEnd_delay_job_value(Integer end_delay_job_value) {
		this.end_delay_job_value = end_delay_job_value;
	}

	/**
	 * 終了遅延を通知するかどうかを返す。<BR>
	 * @return 終了遅延を通知するかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public Integer getEnd_delay_notify() {
		return end_delay_notify;
	}

	/**
	 * 終了遅延を通知するかどうかを設定する。<BR>
	 * @param end_delay_notify 終了遅延を通知するかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public void setEnd_delay_notify(Integer end_delay_notify) {
		this.end_delay_notify = end_delay_notify;
	}

	/**
	 * 終了遅延を通知する場合の重要度を返す。<BR>
	 * @return 終了遅延を通知する場合の重要度
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public Integer getEnd_delay_notify_priority() {
		return end_delay_notify_priority;
	}

	/**
	 * 終了遅延を通知する場合の重要度を設定する。<BR>
	 * @param end_delay_notify_priority 終了遅延を通知する場合の重要度
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public void setEnd_delay_notify_priority(Integer end_delay_notify_priority) {
		this.end_delay_notify_priority = end_delay_notify_priority;
	}

	/**
	 * 終了遅延時に操作するかどうかを返す。<BR>
	 * @return 終了遅延時に操作するかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public Integer getEnd_delay_operation() {
		return end_delay_operation;
	}

	/**
	 * 終了遅延時に操作するかどうかを設定する。<BR>
	 * @param end_delay_operation 終了遅延時に操作するかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public void setEnd_delay_operation(Integer end_delay_operation) {
		this.end_delay_operation = end_delay_operation;
	}

	/**
	 * 終了遅延時に操作する場合の終了状態を返す。<BR>
	 * @return 終了遅延時に操作する場合の終了状態
	 */
	public Integer getEnd_delay_operation_end_status() {
		return end_delay_operation_end_status;
	}

	/**
	 * 終了遅延時に操作する場合の終了状態を設定する。<BR>
	 * @param end_delay_operation_end_value 終了遅延時に操作する場合の終了状態
	 */
	public void setEnd_delay_operation_end_status(Integer end_delay_operation_end_status) {
		this.end_delay_operation_end_status = end_delay_operation_end_status;
	}

	/**
	 * 終了遅延時に操作する場合の終了値を返す。<BR>
	 * @return 終了遅延時に操作する場合の終了値
	 */
	public Integer getEnd_delay_operation_end_value() {
		return end_delay_operation_end_value;
	}

	/**
	 * 終了遅延時に操作する場合の終了値を設定する。<BR>
	 * @param end_delay_operation_end_value 終了遅延時に操作する場合の終了値
	 */
	public void setEnd_delay_operation_end_value(Integer end_delay_operation_end_value) {
		this.end_delay_operation_end_value = end_delay_operation_end_value;
	}

	/**
	 * 終了遅延時に操作する場合の操作種別を返す。<BR>
	 * @return 終了遅延時の操作種別
	 * @see com.clustercontrol.jobmanagement.bean.OperationConstant
	 */
	public Integer getEnd_delay_operation_type() {
		return end_delay_operation_type;
	}

	/**
	 * 終了遅延時に操作する場合の操作種別を設定する。<BR>
	 * @param end_delay_operation_type 終了遅延時の操作種別
	 * @see com.clustercontrol.jobmanagement.bean.OperationConstant
	 */
	public void setEnd_delay_operation_type(Integer end_delay_operation_type) {
		this.end_delay_operation_type = end_delay_operation_type;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * セッション開始後の時間で判定するかどうかを返す。<BR>
	 * @return セッション開始後の時間で終了遅延監視を行うかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public Integer getEnd_delay_session() {
		return end_delay_session;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * セッション開始後の時間で判定するかどうかを設定する。<BR>
	 * @param end_delay_session セッション開始後の時間で終了遅延監視を行うかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public void setEnd_delay_session(Integer end_delay_session) {
		this.end_delay_session = end_delay_session;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * セッション開始後の時間で判定する場合、<BR>
	 * 開始後何分で監視するかの値を返す。<BR>
	 * @return 終了遅延セッション開始後の時間の値
	 */
	public Integer getEnd_delay_session_value() {
		return end_delay_session_value;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * セッション開始後の時間で判定する場合、<BR>
	 * 開始後何分で監視するかの値を設定する。<BR>
	 * @param end_delay_session_value 終了遅延セッション開始後の時間の値
	 */
	public void setEnd_delay_session_value(Integer end_delay_session_value) {
		this.end_delay_session_value = end_delay_session_value;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * 時刻で判定するかどうかを返す。<BR>
	 * @return 終了遅延時刻
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public Integer getEnd_delay_time() {
		return end_delay_time;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * 時刻で判定するかどうかを設定する。<BR>
	 * @param end_delay_time 終了遅延時刻
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public void setEnd_delay_time(Integer end_delay_time) {
		this.end_delay_time = end_delay_time;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * 時刻で判定する場合の時刻を返す。<BR>
	 * @return 終了遅延時刻の値
	 */
	public Long getEnd_delay_time_value() {
		return end_delay_time_value;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * 時刻の判定する場合の値を設定する。<BR>
	 * @param end_delay_time_value 終了遅延時刻の値
	 */
	public void setEnd_delay_time_value(Long end_delay_time_value) {
		this.end_delay_time_value = end_delay_time_value;
	}

	/**
	 * 開始遅延を監視するかどうかを返す。<BR>
	 * @return 開始遅延を監視するかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public Integer getStart_delay() {
		return start_delay;
	}

	/**
	 * 開始遅延を監視するかどうかを設定する。<BR>
	 * @param start_delay 開始遅延を監視するかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public void setStart_delay(Integer start_delay) {
		this.start_delay = start_delay;
	}

	/**
	 * 開始遅延判定対象の条件関係を返す。<BR>
	 * @return 開始遅延判定対象の条件関係
	 * @see com.clustercontrol.jobmanagement.bean.ConditionTypeConstant
	 */
	public Integer getStart_delay_condition_type() {
		return start_delay_condition_type;
	}

	/**
	 * 開始遅延判定対象の条件関係を設定する。<BR>
	 * @param start_delay_condition_type 開始遅延判定対象の条件関係
	 * @see com.clustercontrol.jobmanagement.bean.ConditionTypeConstant
	 */
	public void setStart_delay_condition_type(Integer start_delay_condition_type) {
		this.start_delay_condition_type = start_delay_condition_type;
	}

	/**
	 * 開始遅延を通知するかどうかを返す。<BR>
	 * @return 開始遅延を通知するかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public Integer getStart_delay_notify() {
		return start_delay_notify;
	}

	/**
	 * 開始遅延を通知するかどうかを設定する。<BR>
	 * @param start_delay_notify 開始遅延通知
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public void setStart_delay_notify(Integer start_delay_notify) {
		this.start_delay_notify = start_delay_notify;
	}

	/**
	 * 開始遅延を通知する場合の重要度を返す。<BR>
	 * @return 開始遅延を通知する場合の重要度
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public Integer getStart_delay_notify_priority() {
		return start_delay_notify_priority;
	}

	/**
	 * 開始遅延を通知する場合の重要度を設定する。<BR>
	 * @param start_delay_notify_priority 開始遅延を通知する場合の重要度
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public void setStart_delay_notify_priority(Integer start_delay_notify_priority) {
		this.start_delay_notify_priority = start_delay_notify_priority;
	}

	/**
	 * 開始遅延時に操作するかどうかを返す。<BR>
	 * @return 開始遅延時に操作するかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public Integer getStart_delay_operation() {
		return start_delay_operation;
	}

	/**
	 * 開始遅延時に操作するかどうかを設定する。<BR>
	 * @param start_delay_operation 開始遅延時に操作するかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public void setStart_delay_operation(Integer start_delay_operation) {
		this.start_delay_operation = start_delay_operation;
	}

	/**
	 * 開始遅延時に操作する場合の終了状態を返す。<BR>
	 * @return 開始遅延時に操作する場合の終了状態
	 */
	public Integer getStart_delay_operation_end_status() {
		return start_delay_operation_end_status;
	}

	/**
	 * 開始遅延時に操作する場合の終了状態を設定する。<BR>
	 * @param start_delay_operation_end_value 開始遅延時に操作する場合の終了状態
	 */
	public void setStart_delay_operation_end_status(
			Integer start_delay_operation_end_status) {
		this.start_delay_operation_end_status = start_delay_operation_end_status;
	}

	/**
	 * 開始遅延時に操作する場合の終了値を返す。<BR>
	 * @return 開始遅延時に操作する場合の終了値
	 */
	public Integer getStart_delay_operation_end_value() {
		return start_delay_operation_end_value;
	}

	/**
	 * 開始遅延時に操作する場合の終了値を設定する。<BR>
	 * @param start_delay_operation_end_value 開始遅延時に操作する場合の終了値
	 */
	public void setStart_delay_operation_end_value(
			Integer start_delay_operation_end_value) {
		this.start_delay_operation_end_value = start_delay_operation_end_value;
	}

	/**
	 * 開始遅延時に操作する場合の操作種別を返す。<BR>
	 * @return 開始遅延時の操作種別
	 * @see com.clustercontrol.jobmanagement.bean.OperationConstant
	 */
	public Integer getStart_delay_operation_type() {
		return start_delay_operation_type;
	}

	/**
	 * 開始遅延時に操作する場合の操作種別を返す。<BR>
	 * @param start_delay_operation_type 開始遅延時の操作種別
	 * @see com.clustercontrol.jobmanagement.bean.OperationConstant
	 */
	public void setStart_delay_operation_type(Integer start_delay_operation_type) {
		this.start_delay_operation_type = start_delay_operation_type;
	}

	/**
	 * 開始遅延の判定条件のうち、<BR>
	 * セッション開始後の時間で判定するかどうかを返す。<BR>
	 * @return 開始遅延セッション開始後の時間で判定するかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public Integer getStart_delay_session() {
		return start_delay_session;
	}

	/**
	 * 開始遅延の判定条件のうち、<BR>
	 * セッション開始後の時間で判定するかどうかを設定する。<BR>
	 * @param start_delay_session 開始遅延セッション開始後の時間で判定するどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public void setStart_delay_session(Integer start_delay_session) {
		this.start_delay_session = start_delay_session;
	}

	/**
	 * 開始遅延の判定条件のうち、<BR>
	 * セッション開始後の時間で判定する場合、<BR>
	 * 開始後何分で監視するかの値を返す。<BR>
	 * @return 開始遅延セッション開始後の時間の値
	 */
	public Integer getStart_delay_session_value() {
		return start_delay_session_value;
	}

	/**
	 * 開始遅延の判定条件のうち、<BR>
	 * セッション開始後の時間で判定する場合、<BR>
	 * 開始後何分で監視するかの値を設定する。<BR>
	 * @param start_delay_session_value 開始遅延セッション開始後の時間の値
	 */
	public void setStart_delay_session_value(Integer start_delay_session_value) {
		this.start_delay_session_value = start_delay_session_value;
	}

	/**
	 * 開始遅延の判定条件のうち、<BR>
	 * 時刻で判定するかどうかを返す。<BR>
	 * @return 開始遅延時刻で判定するかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public Integer getStart_delay_time() {
		return start_delay_time;
	}

	/**
	 * 開始遅延の判定条件のうち、<BR>
	 * 時刻で判定するかどうかを設定する。<BR>
	 * @param start_delay_time 開始遅延時刻で判定するかどうか
	 * @see com.clustercontrol.bean.YesNoConstant
	 */
	public void setStart_delay_time(Integer start_delay_time) {
		this.start_delay_time = start_delay_time;
	}

	/**
	 * 開始遅延の判定条件のうち、<BR>
	 * 時刻で判定する場合の時刻を返す。<BR>
	 * @return 開始遅延時刻の値
	 */
	public Long getStart_delay_time_value() {
		return start_delay_time_value;
	}

	/**
	 * 開始遅延の判定条件のうち、<BR>
	 * 時刻で判定する場合の時刻を設定する。<BR>
	 * @param start_delay_time_value 開始遅延時刻の値
	 */
	public void setStart_delay_time_value(Long start_delay_time_value) {
		this.start_delay_time_value = start_delay_time_value;
	}

	public Integer getMultiplicityNotify() {
		return this.multiplicity_notify;
	}

	public void setMultiplicityNotify(Integer multiplicity_notify) {
		this.multiplicity_notify = multiplicity_notify;
	}

	public Integer getMultiplicityNotifyPriority() {
		return this.multiplicity_notify_priority;
	}

	public void setMultiplicityNotifyPriority(Integer multiplicity_notify_priority) {
		this.multiplicity_notify_priority = multiplicity_notify_priority;
	}

	public Integer getMultiplicityOperation() {
		return this.multiplicity_operation;
	}

	public void setMultiplicityOperation(Integer multiplicity_operation) {
		this.multiplicity_operation = multiplicity_operation;
	}

	public Integer getMultiplicityEndValue() {
		return this.multiplicity_end_value;
	}

	public void setMultiplicityEndValue(Integer multiplicity_end_value) {
		this.multiplicity_end_value = multiplicity_end_value;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JobWaitRuleInfo)) {
			return false;
		}
		JobWaitRuleInfo o1 = this;
		JobWaitRuleInfo o2 = (JobWaitRuleInfo)o;

		boolean ret = false;
		ret = 	equalsSub(o1.getSuspend(), o2.getSuspend()) &&
				equalsSub(o1.getSkip(), o2.getSkip()) &&
				equalsSub(o1.getSkipEndStatus(), o2.getSkipEndStatus()) &&
				equalsSub(o1.getSkipEndValue(), o2.getSkipEndValue()) &&
				equalsSub(o1.getCondition(), o2.getCondition()) &&
				equalsArray(o1.getObject(), o2.getObject()) &&
				equalsSub(o1.getEndCondition(), o2.getEndCondition()) &&
				equalsSub(o1.getEndStatus(), o2.getEndStatus()) &&
				equalsSub(o1.getEndValue(), o2.getEndValue()) &&

				equalsSub(o1.getCalendar(), o2.getCalendar()) &&
				equalsSub(o1.getCalendarId(), o2.getCalendarId()) &&
				equalsSub(o1.getCalendarEndStatus(), o2.getCalendarEndStatus()) &&
				equalsSub(o1.getCalendarEndValue(), o2.getCalendarEndValue()) &&

				equalsSub(o1.getStart_delay(), o2.getStart_delay()) &&
				equalsSub(o1.getStart_delay_session(), o2.getStart_delay_session()) &&
				equalsSub(o1.getStart_delay_session_value(), o2.getStart_delay_session_value()) &&
				equalsSub(o1.getStart_delay_time(), o2.getStart_delay_time()) &&
				equalsSub(o1.getStart_delay_time_value(), o2.getStart_delay_time_value()) &&
				equalsSub(o1.getStart_delay_condition_type(), o2.getStart_delay_condition_type()) &&
				equalsSub(o1.getStart_delay_notify(), o2.getStart_delay_notify()) &&
				equalsSub(o1.getStart_delay_notify_priority(), o2.getStart_delay_notify_priority()) &&
				equalsSub(o1.getStart_delay_operation(), o2.getStart_delay_operation()) &&
				equalsSub(o1.getStart_delay_operation_type(), o2.getStart_delay_operation_type()) &&
				equalsSub(o1.getStart_delay_operation_end_status(), o2.getStart_delay_operation_end_status()) &&
				equalsSub(o1.getStart_delay_operation_end_value(), o2.getStart_delay_operation_end_value()) &&

				equalsSub(o1.getEnd_delay(), o2.getEnd_delay()) &&
				equalsSub(o1.getEnd_delay_session(), o2.getEnd_delay_session()) &&
				equalsSub(o1.getEnd_delay_session_value(), o2.getEnd_delay_session_value()) &&
				equalsSub(o1.getEnd_delay_job(), o2.getEnd_delay_job()) &&
				equalsSub(o1.getEnd_delay_job_value(), o2.getEnd_delay_job_value()) &&
				equalsSub(o1.getEnd_delay_time(), o2.getEnd_delay_time()) &&
				equalsSub(o1.getEnd_delay_time_value(), o2.getEnd_delay_time_value()) &&
				equalsSub(o1.getEnd_delay_condition_type(), o2.getEnd_delay_condition_type()) &&
				equalsSub(o1.getEnd_delay_notify(), o2.getEnd_delay_notify()) &&
				equalsSub(o1.getEnd_delay_notify_priority(), o2.getEnd_delay_notify_priority()) &&
				equalsSub(o1.getEnd_delay_operation(), o2.getEnd_delay_operation()) &&
				equalsSub(o1.getEnd_delay_operation_type(), o2.getEnd_delay_operation_type()) &&
				equalsSub(o1.getEnd_delay_operation_end_status(), o2.getEnd_delay_operation_end_status()) &&
				equalsSub(o1.getEnd_delay_operation_end_value(), o2.getEnd_delay_operation_end_value()) &&

				equalsSub(o1.getMultiplicityNotify(), o2.getMultiplicityNotify()) &&
				equalsSub(o1.getMultiplicityNotifyPriority(), o2.getMultiplicityNotifyPriority()) &&
				equalsSub(o1.getMultiplicityOperation(), o2.getMultiplicityOperation()) &&
				equalsSub(o1.getMultiplicityEndValue(), o2.getMultiplicityEndValue());
		m_log.debug("waitRule ret = " + ret);
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

	private boolean equalsArray(ArrayList<JobObjectInfo> list1, ArrayList<JobObjectInfo> list2) {
		if ((list1 == null || list1.size() == 0) && (list2 == null || list2.size() == 0)) {
			return true;
		}
		if (list1 != null && list2 == null) {
			return false;
		}
		if (list1 == null && list2 != null) {
			return false;
		}
		if (list1.size() != list2.size()) {
			return false;
		}
		Collections.sort(list1);
		Collections.sort(list2);
		for (int i = 0; i < list1.size(); i++) {
			if (!list1.get(i).equals(list2.get(i))) {
				if (m_log.isTraceEnabled()) {
					m_log.trace("equalsArray : " + list1.get(i) + "!=" + list2.get(i));
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * 単体テスト用
	 * @param args
	 */
	public static void main (String args[]) {
		testEquals();
	}
	public static void testEquals(){
		System.out.println("=== JobWaitRuleInfo の単体テスト ===");

		System.out.println("*** all agreement ***");
		JobWaitRuleInfo info1 = createSampleInfo();
		JobWaitRuleInfo info2 = createSampleInfo();
		judge(true, info1.equals(info2));

		String[] str = {
				"保留",
				"スキップ",
				"スキップ時終了状態",
				"スキップ時終了値",
				"判定対象の条件関係",
				"ジョブの判定対象情報",
				"条件を満たさなければ終了する",
				"条件を満たさない時の終了状態",
				"条件を満たさない時の終了値",
				"カレンダ",
				"カレンダID",
				"カレンダによる未実行時の終了状態",
				"カレンダによる未実行時の終了値",
				"開始遅延",
				"開始遅延セッション開始後の時間",
				"開始遅延セッション開始後の時間の値",
				"開始遅延時刻",
				"開始遅延時刻の値",
				"開始遅延判定対象の条件関係",
				"開始遅延通知",
				"開始遅延通知の重要度",
				"開始遅延操作",
				"開始遅延操作種別",
				"開始遅延操作終了状態",
				"開始遅延操作終了値",
				"終了遅延",
				"終了遅延セッション開始後の時間",
				"終了遅延セッション開始後の時間の値",
				"終了遅延ジョブの開始後の時間",
				"終了遅延ジョブの開始後の時間の値",
				"終了遅延時刻",
				"終了遅延時刻の値",
				"終了遅延判定対象の条件関係",
				"終了遅延通知",
				"終了遅延通知重要度",
				"終了遅延操作",
				"終了遅延操作種別",
				"終了遅延操作終了状態",
				"終了遅延操作終了値",
				"多重度の通知",
				"多重度の重要度",
				"多重度の状態種別",
				"多重度の終了値"
		};

		for (int i = 0; i < 43 ; i++) {
			System.out.println("*** Only " + str[i] + " is different ***");
			info2 = createSampleInfo2(i);
			judge(false, info1.equals(info2));
		}
	}

	/**
	 * ジョブの待ち条件に関する情報のサンプルデータを作成する
	 * 単体テスト用
	 * @return
	 */
	public static JobWaitRuleInfo createSampleInfo() {
		JobWaitRuleInfo info1 = new JobWaitRuleInfo();
		info1.setSuspend(0);

		info1.setSkip(0);
		info1.setSkipEndStatus(0);
		info1.setSkipEndValue(0);

		info1.setCondition(0);
		ArrayList<JobObjectInfo> objList = new ArrayList<JobObjectInfo>();
		{
			JobObjectInfo objInfo = new JobObjectInfo();
			objInfo.setType(0);
			objInfo.setJobId("jobId");
			objInfo.setJobName("jobName");
			objInfo.setValue(0);
			objInfo.setTime(0L);
			objList.add(objInfo);
		}
		info1.setObject(objList);
		info1.setEndCondition(0);
		info1.setEndStatus(0);
		info1.setEndValue(0);
		info1.setCalendar(0);
		info1.setCalendarId("calendarId");
		info1.setCalendarEndStatus(0);
		info1.setCalendarEndValue(0);

		info1.setStart_delay(0);
		info1.setStart_delay_session(0);
		info1.setStart_delay_session_value(0);

		info1.setStart_delay_time(0);
		info1.setStart_delay_time_value(0L);

		info1.setStart_delay_condition_type(0);

		info1.setStart_delay_notify(0);
		info1.setStart_delay_notify_priority(0);

		info1.setStart_delay_operation(0);
		info1.setStart_delay_operation_type(0);
		info1.setStart_delay_operation_end_status(0);
		info1.setStart_delay_operation_end_value(0);

		info1.setEnd_delay(0);

		info1.setEnd_delay_session(0);
		info1.setEnd_delay_session_value(0);

		info1.setEnd_delay_job(0);
		info1.setEnd_delay_job_value(0);

		info1.setEnd_delay_time(0);
		info1.setEnd_delay_time_value(0L);

		info1.setEnd_delay_condition_type(0);

		info1.setEnd_delay_notify(0);
		info1.setEnd_delay_notify_priority(0);

		info1.setEnd_delay_operation(0);
		info1.setEnd_delay_operation_type(0);
		info1.setEnd_delay_operation_end_status(0);
		info1.setEnd_delay_operation_end_value(0);

		info1.setMultiplicityNotify(0);
		info1.setMultiplicityNotifyPriority(0);
		info1.setMultiplicityOperation(0);
		info1.setMultiplicityEndValue(0);

		return info1;
	}
	/**
	 * createSampleInfo()にて作成されたデータのパラメータを１つ変更する
	 * @param i
	 * @return
	 */
	public static JobWaitRuleInfo createSampleInfo2(int i){
		JobWaitRuleInfo info2 = createSampleInfo();
		switch (i) {
		case 0 :
			info2.setSuspend(1);
			break;
		case 1 :
			info2.setSkip(1);
			break;
		case 2 :
			info2.setSkipEndStatus(1);
			break;
		case 3 :
			info2.setSkipEndValue(1);
			break;
		case 4 :
			info2.setCondition(1);
			break;
		case 5 :
			ArrayList<JobObjectInfo> objList = new ArrayList<JobObjectInfo>();
			{
				JobObjectInfo objInfo = new JobObjectInfo();
				objInfo.setType(1);
				objInfo.setJobId("jobId");
				objInfo.setJobName("jobName");
				objInfo.setValue(0);
				objInfo.setTime(0L);
				objList.add(objInfo);
			}
			info2.setObject(objList);
			break;
		case 6 :
			info2.setEndCondition(1);
			break;
		case 7 :
			info2.setEndStatus(1);
			break;
		case 8 :
			info2.setEndValue(1);
			break;
		case 9 :
			info2.setCalendar(1);
			break;
		case 10 :
			info2.setCalendarId("calendar_Id");
			break;
		case 11 :
			info2.setCalendarEndStatus(1);
			break;
		case 12 :
			info2.setCalendarEndValue(1);
			break;
		case 13 :
			info2.setStart_delay(1);
			break;
		case 14 :
			info2.setStart_delay_session(1);
			break;
		case 15 :
			info2.setStart_delay_session_value(1);
			break;
		case 16 :
			info2.setStart_delay_time(1);
			break;
		case 17 :
			info2.setStart_delay_time_value(1L);
			break;
		case 18 :
			info2.setStart_delay_condition_type(1);
			break;
		case 19 :
			info2.setStart_delay_notify(1);
			break;
		case 20 :
			info2.setStart_delay_notify_priority(1);
			break;
		case 21 :
			info2.setStart_delay_operation(1);
			break;
		case 22 :
			info2.setStart_delay_operation_type(1);
			break;
		case 23 :
			info2.setStart_delay_operation_end_status(1);
			break;
		case 24 :
			info2.setStart_delay_operation_end_value(1);
			break;
		case 25 :
			info2.setEnd_delay(1);
			break;
		case 26 :
			info2.setEnd_delay_session(1);
			break;
		case 27 :
			info2.setEnd_delay_session_value(1);
			break;
		case 28 :
			info2.setEnd_delay_job(1);
			break;
		case 29 :
			info2.setEnd_delay_job_value(1);
			break;
		case 30 :
			info2.setEnd_delay_time(1);
			break;
		case 31 :
			info2.setEnd_delay_time_value(1L);
			break;
		case 32 :
			info2.setEnd_delay_condition_type(1);
			break;
		case 33 :
			info2.setEnd_delay_notify(1);
			break;
		case 34 :
			info2.setEnd_delay_notify_priority(1);
			break;
		case 35 :
			info2.setEnd_delay_operation(1);
			break;
		case 36 :
			info2.setEnd_delay_operation_type(1);
			break;
		case 37 :
			info2.setEnd_delay_operation_end_status(1);
			break;
		case 38 :
			info2.setEnd_delay_operation_end_value(1);
			break;
		case 39 :
			info2.setMultiplicityNotify(1);
			break;
		case 40 :
			info2.setMultiplicityNotifyPriority(1);
			break;
		case 41 :
			info2.setMultiplicityOperation(1);
			break;
		case 42 :
			info2.setMultiplicityEndValue(1);
			break;
		}
		return info2;
	}

	public static ArrayList<JobWaitRuleInfo> createSampleList() {

		ArrayList<JobWaitRuleInfo> retList = new ArrayList<JobWaitRuleInfo>();
		/**
		 * JobWaitRuleInfo内のパラメータは43種類のため、
		 * その回数繰り返す
		 * カウントアップするごとに、パラメータの値を変える。
		 * 常に、いずれか１つのパラメータがcreateSampleInfo()にて作成されたデータと違う
		 */
		for (int i = 0; i < 43 ; i++) {
			JobWaitRuleInfo info2 = createSampleInfo2(i);
			retList.add(info2);
		}
		return retList;
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
