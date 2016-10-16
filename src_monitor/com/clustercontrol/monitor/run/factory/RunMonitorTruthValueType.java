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

import com.clustercontrol.monitor.run.bean.MonitorJudgementInfo;
import com.clustercontrol.monitor.run.bean.MonitorTruthValueInfo;
import com.clustercontrol.monitor.run.bean.TruthConstant;
import com.clustercontrol.monitor.run.model.MonitorTruthValueInfoEntity;

/**
 * 真偽値監視を実行する抽象クラス<BR>
 * <p>
 * 真偽値監視を行う各監視管理クラスで継承してください。
 *
 * @version 3.0.0
 * @since 2.0.0
 */
abstract public class RunMonitorTruthValueType extends RunMonitor{

	/** 監視取得値 */
	protected boolean m_value;

	/**
	 * コンストラクタ。
	 * 
	 */
	protected RunMonitorTruthValueType() {
		super();
	}

	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#collect(java.lang.String)
	 */
	@Override
	public abstract boolean collect(String facilityId);

	/**
	 * 判定結果を返します。
	 * <p>
	 * 監視取得値の真偽値定数を返します。
	 * 
	 * @see com.clustercontrol.monitor.run.bean.TruthConstant
	 */
	@Override
	public int getCheckResult(boolean ret) {

		int result = -1;
		//		int result = m_failurePriority;

		// 値取得の成功時
		if(ret){
			if(m_value){
				// 真
				result = TruthConstant.TYPE_TRUE;
			}
			else{
				// 偽
				result = TruthConstant.TYPE_FALSE;
			}
		}
		return result;
	}

	/**
	 * 判定情報を設定します。
	 * <p>
	 * <ol>
	 * <li>監視情報より判定情報を取得します。</li>
	 * <li>取得した真偽値監視の判定情報を、真偽値定数をキーに判定情報マップにセットします。</li>
	 * </ol>
	 * 
	 * @see com.clustercontrol.monitor.run.bean.TruthConstant
	 * @see com.clustercontrol.monitor.run.bean.MonitorTruthValueInfo
	 */
	@Override
	protected void setJudgementInfo() {

		// 真偽値監視判定値、ログ出力メッセージ情報を取得
		Collection<MonitorTruthValueInfoEntity> ct = m_monitor.getMonitorTruthValueInfoEntities();
		Iterator<MonitorTruthValueInfoEntity> itr = ct.iterator();

		m_judgementInfoList = new TreeMap<Integer, MonitorJudgementInfo>();
		MonitorTruthValueInfoEntity entity = null;
		while(itr.hasNext()){

			entity = itr.next();
			Integer truthValue = entity.getId().getTruthValue();

			MonitorTruthValueInfo info = new MonitorTruthValueInfo();
			info.setTruthValue(truthValue.intValue());
			info.setPriority(entity.getId().getPriority().intValue());
			info.setMessageId(entity.getMessageId());
			info.setMessage(entity.getMessage());

			m_judgementInfoList.put(truthValue, info);
		}
	}
}
