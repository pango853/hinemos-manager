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

package com.clustercontrol.monitor.run.factory;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.monitor.run.bean.MonitorJudgementInfo;
import com.clustercontrol.monitor.run.bean.MonitorNumericValueInfo;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfoEntity;

/**
 * 数値監視を実行する抽象クラス<BR>
 * <p>
 * 数値監視を行う各監視管理クラスで継承してください。
 *
 * @version 3.0.0
 * @since 2.0.0
 */
abstract public class RunMonitorNumericValueType extends RunMonitor{

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( RunMonitorNumericValueType.class );

	/**
	 * コンストラクタ。
	 * 
	 */
	protected RunMonitorNumericValueType() {
		super();
	}

	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#collect(java.lang.String)
	 */
	@Override
	public abstract boolean collect(String facilityId) throws FacilityNotFound, HinemosUnknown;

	/**
	 * 判定結果を返します。
	 * <p>
	 * 判定情報マップにセットしてある各重要度の上下限値から、監視取得値がどの重要度の範囲に該当するか判定し、
	 * 重要度を返します。
	 * 
	 * @see com.clustercontrol.bean.PriorityConstant
	 * @see com.clustercontrol.monitor.run.bean.MonitorJudgementInfo
	 */
	@Override
	public int getCheckResult(boolean ret) {

		int result = m_failurePriority;

		MonitorNumericValueInfo info = null;

		// 値取得の成功時
		if(ret){

			// 通知の範囲をチェック
			info = (MonitorNumericValueInfo)m_judgementInfoList.get(Integer.valueOf(PriorityConstant.TYPE_INFO));
			if(m_value >= info.getThresholdLowerLimit() && m_value < info.getThresholdUpperLimit()){
				result = PriorityConstant.TYPE_INFO;
			}
			else{
				// 警告の範囲チェック
				info = (MonitorNumericValueInfo)m_judgementInfoList.get(Integer.valueOf(PriorityConstant.TYPE_WARNING));
				if(m_value >= info.getThresholdLowerLimit() && m_value < info.getThresholdUpperLimit()){
					result = PriorityConstant.TYPE_WARNING;
				}
				else{
					// 危険（通知・警告以外）
					result = PriorityConstant.TYPE_CRITICAL;
				}
			}
		}
		if(m_log.isDebugEnabled()){
			m_log.debug("getCheckResult() : ret = " + ret + ", m_value = " + m_value + ", result = " + result);
		}
		return result;
	}

	/**
	 * 判定情報を設定します。
	 * <p>
	 * <ol>
	 * <li>監視情報より判定情報を取得します。</li>
	 * <li>取得した数値監視の判定情報を、重要度をキーに判定情報マップにセットします。</li>
	 * </ol>
	 * 
	 * @see com.clustercontrol.bean.PriorityConstant
	 * @see com.clustercontrol.monitor.run.bean.MonitorJudgementInfo
	 */
	@Override
	protected void setJudgementInfo() {

		// 数値監視判定値、ログ出力メッセージ情報を取得
		Collection<MonitorNumericValueInfoEntity> ct = m_monitor.getMonitorNumericValueInfoEntities();
		Iterator<MonitorNumericValueInfoEntity> itr = ct.iterator();

		m_judgementInfoList = new TreeMap<Integer, MonitorJudgementInfo>();
		MonitorNumericValueInfoEntity entity = null;
		while(itr.hasNext()){

			entity = itr.next();
			Integer priority = entity.getId().getPriority();

			MonitorNumericValueInfo info = new MonitorNumericValueInfo();
			info.setPriority(priority.intValue());
			info.setThresholdLowerLimit(entity.getThresholdLowerLimit());
			info.setThresholdUpperLimit(entity.getThresholdUpperLimit());
			info.setMessageId(entity.getMessageId());
			info.setMessage(entity.getMessage());

			m_judgementInfoList.put(priority, info);
		}
	}

	/**
	 * 重要度を返します。
	 * <p>
	 * 重要度が判定情報キーとなっているため、そのまま返します。
	 * キーが通知，警告，危険以外の場合は、値取得の失敗時の重要度を返します。
	 */
	@Override
	public int getPriority(int key) {

		// 重要度が判定情報キーとなっているため、そのままリターン
		if(key == PriorityConstant.TYPE_INFO ||
				key == PriorityConstant.TYPE_WARNING ||
				key == PriorityConstant.TYPE_CRITICAL){
			return key;
		}
		else{
			return m_failurePriority;
		}
	}
}
