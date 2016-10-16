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

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.scheduler.TriggerSchedulerException;
import com.clustercontrol.commons.util.EmptyJpaTransactionCallback;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.bean.QuartzConstant;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.session.MonitorRunManagementBean;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;
import com.clustercontrol.process.factory.ModifyPollingSchedule;
import com.clustercontrol.process.util.ProcessProperties;

/**
 * スケジュールを登録するクラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class ModifySchedule {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( ModifySchedule.class );

	/**
	 * DBに登録されている全ての監視項目の設定をスケジューラに登録します。
	 * @throws TriggerSchedulerException
	 * 
	 * @since 4.0.0
	 */
	public void updateScheduleAll() throws HinemosUnknown{
		m_log.debug("updateScheduleAll()");

		JpaTransactionManager jtm = null;
		Throwable exception = null;
		Collection<MonitorInfoEntity> entityList = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			entityList = QueryUtil.getAllMonitorInfo();
			jtm.commit();
		} catch (Exception e) {
			String msg = "updateScheduleAll() " + e.getClass().getSimpleName() + ", " + e.getMessage();
			m_log.warn(msg, e);
			jtm.rollback();
			exception = e;
		} finally {
			jtm.close();
		}

		for (MonitorInfoEntity entity : entityList) {
			try {
				// 監視または収集フラグが有効な設定のみスケジュール登録
				if(ValidConstant.typeToBoolean(entity.getMonitorFlg())
						|| ValidConstant.typeToBoolean(entity.getCollectorFlg()) ){
					updateSchedule(entity, true);
				}
			} catch (Exception e) {
				m_log.info("updateScheduleAll() scheduleJob : monitorId = " + entity.getMonitorId()
						+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage());
				// 次の設定を処理するため、throwはしない。
				exception = e;
			}
		}

		if(exception != null){
			throw new HinemosUnknown("An error occurred while scheduling the trigger.", exception);
		}
	}

	/**
	 * スケジューラに監視情報のジョブを登録します。
	 * <p>
	 * <ol>
	 * <li>監視対象IDと監視項目IDを指定し、監視を実行するメソッドのジョブを作成します。</li>
	 * <li>呼びだすメソッドの引数として、下記項目をセットします。</li>
	 * <dl>
	 *  <dt>引数</dt>
	 *  <dd>監視項目ID</dd>
	 * </dl>
	 * <li>スケジューラにジョブとトリガを登録します。</li>
	 * </ol>
	 * 
	 * @param monitorId 監視項目ID
	 * @throws TriggerSchedulerException
	 * @since 2.0.0
	 */
	protected void updateSchedule(String monitorId) throws HinemosUnknown {
		m_log.debug("updateSchedule() : monitorId=" + monitorId);

		MonitorInfoEntity entity = null;
		try {
			entity = QueryUtil.getMonitorInfoPK_NONE(monitorId);
		} catch (MonitorNotFound e) {
			String msg = "updateSchedule() found no scheduleJob : monitorId = " + monitorId;
			TriggerSchedulerException e1 = new TriggerSchedulerException(msg);
			throw new HinemosUnknown(msg, e1);
		} catch (Exception e) {
			String msg = "updateSchedule() scheduleJob : monitorId = " + monitorId + e.getClass().getSimpleName() + ", " + e.getMessage();
			m_log.warn(msg, e);
			throw new HinemosUnknown(msg, e);
		}
		
		final MonitorInfoEntity entityCopy = entity;
		JpaTransactionManager jtm = new JpaTransactionManager();
		jtm.addCallback(new EmptyJpaTransactionCallback() {
			@Override
			public void postCommit() {
				try {
					updateSchedule(entityCopy, false);
				} catch (HinemosUnknown e) {
					m_log.error(e);
				}
			}
		});
	}

	private void updateSchedule(MonitorInfoEntity entity, boolean isInitManager) throws HinemosUnknown {
		String monitorId = entity.getMonitorId();
		String monitorTypeId = entity.getMonitorTypeId();

		//JobDetailに呼び出すメソッドの引数を設定
		// 監視対象IDを設定
		Serializable[] jdArgs = new Serializable[3];
		Class<? extends Serializable>[] jdArgsType = new Class[3];
		jdArgs[0] = monitorTypeId;
		jdArgsType[0] = String.class;
		// 監視項目IDを設定
		jdArgs[1] = monitorId;
		jdArgsType[1] = String.class;
		// 監視判定タイプを設定
		jdArgs[2] = Integer.valueOf(entity.getMonitorType());
		jdArgsType[2] = Integer.class;



		TriggerType type = null;
		try {
			type = TriggerType.valueOf(entity.getTriggerType());
		} catch (IllegalArgumentException e) {
			m_log.info("updateSchedule() Invalid TRIGGER_TYPE. monitorTypeId = " + monitorTypeId + ", + monitorId = " + monitorId);
			return;
		}

		switch (type) {
		case SIMPLE :
			int interval = entity.getRunInterval();

			m_log.debug("Schedule SimpleTrigger. monitorId = " + monitorId);

			// SimpleTrigger でジョブをスケジューリング登録
			// 監視も収集も無効の場合、登録後にポーズするようにスケジュール
			SchedulerPlugin.scheduleSimpleJob(SchedulerType.RAM, monitorId, monitorTypeId, new Date(calcSimpleTriggerStartTime(interval, entity.getDelayTime())),
					interval, true, MonitorRunManagementBean.class.getName(), QuartzConstant.METHOD_NAME, jdArgsType, jdArgs);

			if (! ValidConstant.typeToBoolean(entity.getMonitorFlg()) && ! ValidConstant.typeToBoolean(entity.getCollectorFlg())) {
				SchedulerPlugin.pauseJob(SchedulerType.RAM, monitorId, monitorTypeId);
			}
			break;
		case CRON :
			m_log.debug("Schedule CronTrigger. monitorId = " + monitorId + ", monitorTypeId = " + monitorTypeId);

			// プロセス監視の場合は、ポーリングスレッドを立ち上げ、初回の閾値判定処理を遅らせるためのディレイを設定する
			long delay = 0;
			if (HinemosModuleConstant.MONITOR_PROCESS.equals(monitorTypeId)) {
				delay = ProcessProperties.getProperties().getStartSecond();
				if (isInitManager == true) {
					// マネージャ起動直後に限り、ポーラーを起動する
					new ModifyPollingSchedule().addSchedule(monitorTypeId, monitorId, entity.getFacilityId(), entity.getRunInterval());
					// ここで登録するポーリング処理と、その後下のほうで登録する閾値判定処理は、上記のdelay分発動タイミングがずれるはずだが、
					// マネージャ起動直後に限っては、Quartzが有効に機能するまでに時間がかかり、Quartzが有効化後に一気に両者が動作し、
					// 結果として不明となる可能性がある。そのため、マネージャ起動直後のみ閾値処理の開始時刻にさらに猶予をもうける。
					delay += ProcessProperties.getProperties().getCollectInitDelaySecond();
				}
				m_log.debug("process delay " + delay);
			}

			// CronTrigger でジョブをスケジューリング登録
			// 監視も収集も無効の場合、登録後にポーズするようにスケジュール
			SchedulerPlugin.scheduleCronJob(SchedulerType.RAM, monitorId, monitorTypeId,
					new Date(System.currentTimeMillis() + (15 + delay) * 1000),
					getCronString(entity.getRunInterval(), entity.getDelayTime()), true, MonitorRunManagementBean.class.getName(), QuartzConstant.METHOD_NAME, jdArgsType, jdArgs);

			if (! ValidConstant.typeToBoolean(entity.getMonitorFlg()) && ! ValidConstant.typeToBoolean(entity.getCollectorFlg())) {
				SchedulerPlugin.pauseJob(SchedulerType.RAM, monitorId, monitorTypeId);
			}
			break;
		case NONE :
			// スケジュール登録しない
			break;
		}
	}


	/**
	 * 引数で指定された監視情報をQuartzから削除します。
	 * 
	 * @param monitorTypeId 監視対象ID
	 * @param monitorId 監視項目ID
	 * @throws TriggerSchedulerException
	 * 
	 * @see com.clustercontrol.monitor.run.bean.QuartzConstant
	 * @see com.clustercontrol.commons.util.QuartzUtil#getQuartzManager()
	 * @see com.clustercontrol.quartzmanager.ejb.session.QuartzManager#deleteSchedule(java.lang.String, java.lang.String)
	 */
	protected void deleteSchedule(final String monitorTypeId, final String monitorId) throws HinemosUnknown {
		m_log.debug("deleteSchedule() : type =" + monitorTypeId + ", id=" + monitorId);
		
		JpaTransactionManager jtm = new JpaTransactionManager();
		jtm.addCallback(new EmptyJpaTransactionCallback() {
			@Override
			public void postCommit() {
				try {
					SchedulerPlugin.deleteJob(SchedulerType.RAM, monitorId, monitorTypeId);
				} catch (HinemosUnknown e) {
					m_log.error(e);
				}
			}
		});
	}

	/**
	 * Cron形式のスケージュール定義を返します。
	 * 
	 * @param schedule スケジュールカレンダ
	 * @return Cron形式スケジュール
	 */
	private String getCronString(int interval, int delayTime){
		String cronString = null;
		if(interval > 0 && interval < 3600){
			int minute = interval / 60;

			// Quartzのcron形式（例： 30 */1 * * * ? *）
			cronString = delayTime + " */" + minute + " * * * ? *";
		} else if(interval > 0 && interval >= 3600){
			int hour = interval / 3600;

			// Quartzのcron形式（例： 30 0 */1 * * ? *）
			cronString = delayTime + " 0 */" + hour + " * * ? *";
		}

		m_log.debug("getCronString() interval = " + interval + ", delayTime = " + delayTime + ", cronString = " + cronString);
		return cronString;
	}

	/**
	 * スケジュール開始時刻を求めます。
	 */
	private long calcSimpleTriggerStartTime(int interval, int delayTime){
		// 再起動時も常に同じタイミングでQuartzのTriggerが起動されるようにstartTimeを設定する
		long now = System.currentTimeMillis() + 1000l; // pauseFlag が trueの場合、起動させないように実行開始時刻を少し遅らせる。
		long intervalMilliSecond = interval * 1000;

		// 1) 現在時刻の直前で、監視間隔の倍数となる時刻を求める
		//   例）22:32:05 に 5分間間隔の設定を追加する場合は、22:35:00
		long roundout = (now / intervalMilliSecond + 1) * intervalMilliSecond;

		// 2) 1)の時刻にDelayTimeを秒数として足したものをstartTimeとして設定する
		long startTime = roundout + delayTime * 1000;

		// 3) もう一つ前の実行タイミングが（現在時刻+5秒）より後の場合は、そちらをstartTimeとする
		if((System.currentTimeMillis() + 5 * 1000l) < (startTime - intervalMilliSecond)){
			m_log.debug("reset time before : " + new Date(startTime));
			startTime = startTime - intervalMilliSecond;
			m_log.debug("reset time after : " + new Date(startTime));
		}

		return startTime;
	}
}
