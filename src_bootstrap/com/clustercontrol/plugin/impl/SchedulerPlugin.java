/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.plugin.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import com.clustercontrol.HinemosManagerMain;
import com.clustercontrol.HinemosManagerMain.StartupMode;
import com.clustercontrol.HinemosManagerMain.StartupTask;
import com.clustercontrol.commons.quartz.job.ReflectionInvokerJob;
import com.clustercontrol.commons.util.JpaPersistenceConfig;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.session.JobRunManagementBean;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.run.factory.ModifySchedule;
import com.clustercontrol.monitor.session.MonitorControllerBean;
import com.clustercontrol.performance.util.code.PerformanceRestartManager;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.plugin.util.DbAccountProperties;
import com.clustercontrol.repository.session.RepositoryRunManagementBean;

/**
 * 内部スケジューラを管理するプラグインサービス
 */
public class SchedulerPlugin implements HinemosPlugin {

	public static final Log log = LogFactory.getLog(SchedulerPlugin.class);

	// スケジュール情報の登録方法(CRON : cronと同様の書式を指定, SIMPLE : INTERVALのみを指定, NONE : トリガーを登録しない )
	public static enum TriggerType { CRON, SIMPLE, NONE };

	// スケジューラ情報の保持種別(RAM : オンメモリで管理、DBMS : DBで永続化管理)
	public static enum SchedulerType { RAM, DBMS };

	private static final Object _schedulerLock = new Object();
	private static final Map<SchedulerType, Scheduler> _scheduler = new ConcurrentHashMap<SchedulerType, Scheduler>(2);

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(WebServiceStartHTTPSPlugin.class.getName());
		return dependency;
	}

	@Override
	public void create() {
		try {
			synchronized (_schedulerLock) {

				Properties dbmsProp = new Properties();
				dbmsProp.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");
				dbmsProp.setProperty("org.quartz.dataSource.SchedulerDS.driver", HinemosPropertyUtil.getHinemosPropertyStr("quartz.dataSource.SchedulerDS.driver", "org.postgresql.Driver"));
				dbmsProp.setProperty("org.quartz.dataSource.SchedulerDS.maxConnections", Integer.toString(HinemosPropertyUtil.getHinemosPropertyNum("quartz.dataSource.SchedulerDS.maxConnections", 16)));
				dbmsProp.setProperty("org.quartz.dataSource.SchedulerDS.password", DbAccountProperties.getHinemosQuartzPass());
				dbmsProp.setProperty("org.quartz.dataSource.SchedulerDS.URL", JpaPersistenceConfig.getHinemosJdbcUrl());
				dbmsProp.setProperty("org.quartz.dataSource.SchedulerDS.user", DbAccountProperties.getHinemosQuartzUser());
				dbmsProp.setProperty("org.quartz.dataSource.SchedulerDS.validationQuery", HinemosPropertyUtil.getHinemosPropertyStr("quartz.dataSource.SchedulerDS.validationQuery", "SELECT 1 FOR UPDATE"));
				dbmsProp.setProperty("org.quartz.jobStore.class", HinemosPropertyUtil.getHinemosPropertyStr("quartz.jobStore.dbms.class", "org.quartz.impl.jdbcjobstore.JobStoreTX"));
				dbmsProp.setProperty("org.quartz.jobStore.dataSource", HinemosPropertyUtil.getHinemosPropertyStr("quartz.jobStore.dataSource", "SchedulerDS"));
				dbmsProp.setProperty("org.quartz.jobStore.driverDelegateClass", HinemosPropertyUtil.getHinemosPropertyStr("quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate"));
				dbmsProp.setProperty("org.quartz.jobStore.misfireThreshold", Integer.toString(HinemosPropertyUtil.getHinemosPropertyNum("quartz.dbms.jobStore.misfireThreshold", 3600000)));
				dbmsProp.setProperty("org.quartz.jobStore.tablePrefix", HinemosPropertyUtil.getHinemosPropertyStr("quartz.jobStore.tablePrefix", "setting.QRTZ_"));
				dbmsProp.setProperty("org.quartz.scheduler.instanceName", HinemosPropertyUtil.getHinemosPropertyStr("quartz.dbms.scheduler.instanceName", "DBMSScheduler"));
				dbmsProp.setProperty("org.quartz.threadPool.class", HinemosPropertyUtil.getHinemosPropertyStr("quartz.dbms.threadPool.class", "org.quartz.simpl.SimpleThreadPool"));
				dbmsProp.setProperty("org.quartz.threadPool.threadCount", Integer.toString(HinemosPropertyUtil.getHinemosPropertyNum("quartz.dbms.threadPool.threadCount", 8)));
				dbmsProp.setProperty("org.quartz.threadPool.threadPriority", Integer.toString(HinemosPropertyUtil.getHinemosPropertyNum("quartz.dbms.threadPool.threadPriority", 5)));

				Properties ramProp = new Properties();
				ramProp.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");
				ramProp.setProperty("org.quartz.jobStore.class", HinemosPropertyUtil.getHinemosPropertyStr("quartz.jobStore.ram.class", "com.clustercontrol.plugin.impl.HinemosRAMJobStore"));
				ramProp.setProperty("org.quartz.jobStore.misfireThreshold", Integer.toString(HinemosPropertyUtil.getHinemosPropertyNum("quartz.ram.jobStore.misfireThreshold", 3600000)));
				ramProp.setProperty("org.quartz.scheduler.instanceName", HinemosPropertyUtil.getHinemosPropertyStr("quartz.ram.scheduler.instanceName", "RAMScheduler"));
				ramProp.setProperty("org.quartz.threadPool.class", HinemosPropertyUtil.getHinemosPropertyStr("quartz.ram.threadPool.class", "org.quartz.simpl.SimpleThreadPool"));
				ramProp.setProperty("org.quartz.threadPool.threadCount", Integer.toString(HinemosPropertyUtil.getHinemosPropertyNum("quartz.ram.threadPool.threadCount", 32)));
				ramProp.setProperty("org.quartz.threadPool.threadPriority", Integer.toString(HinemosPropertyUtil.getHinemosPropertyNum("quartz.ram.threadPool.threadPriority", 5)));

				int delaySec = HinemosPropertyUtil.getHinemosPropertyNum("common.scheduler.startup.delay", 60);
				log.info("initializing SchedulerPlugin : properties (delaySec = " + delaySec + ")");

				StdSchedulerFactory ramSf = new StdSchedulerFactory();
				ramSf.initialize(ramProp);

				StdSchedulerFactory dbmsSf = new StdSchedulerFactory();
				dbmsSf.initialize(dbmsProp);

				_scheduler.put(SchedulerType.RAM, ramSf.getScheduler());
				_scheduler.put(SchedulerType.DBMS, dbmsSf.getScheduler());
			}
			
			if (HinemosManagerMain._startupMode != StartupMode.MAINTENANCE) {
				initTrigger();
			}
		} catch (SchedulerException e) {
			log.error("initialization failure : SchedulerPlugin", e);
		} catch (HinemosException e) {
			log.error("initialization failure : SchedulerPlugin", e);
		}
	}

	@Override
	public void destroy() {


	}

	@Override
	public void activate() {
		if (HinemosManagerMain._startupMode == StartupMode.MAINTENANCE) {
			log.info("skipped activation (startup mode is MAINTENANCE) : SchedulerPlugin");
			HinemosManagerMain.addStartupTask(new SchedulerStartupTask(this));
			return;
		}

		for (Entry<SchedulerType, Scheduler> entry : _scheduler.entrySet()) {
			try {
				log.debug("activate scheduler name=" + entry.getValue().getSchedulerName());
				int delaySec = HinemosPropertyUtil.getHinemosPropertyNum("common.scheduler.startup.delay", 60);
				entry.getValue().startDelayed(delaySec);
			} catch (SchedulerException e) {
				log.error("activation failure : SchedulerPlugin", e);
			}
		}

	}

	public class SchedulerStartupTask implements StartupTask {
		
		private final SchedulerPlugin _plugin;
		
		public SchedulerStartupTask(SchedulerPlugin plugin) {
			_plugin = plugin;
		}
		
		@Override
		public void init() {
			try {
				_plugin.initTrigger();
			} catch (HinemosException e) {
				log.error("initialization failure : SchedulerPlugin", e);
			}
			
			for (Entry<SchedulerType, Scheduler> entry : _scheduler.entrySet()) {
				try {
					log.info("activate scheduler name=" + entry.getValue().getSchedulerName());
					entry.getValue().start();
				} catch (SchedulerException e) {
					log.error("activation failure : SchedulerPlugin", e);
				}
			}
		}
		
	}
	
	@Override
	public void deactivate() {
		if (HinemosManagerMain._startupMode == StartupMode.NORMAL) {
			new MonitorControllerBean().persistMonitorStatusCache();
		}

		if (HinemosManagerMain._startupMode == StartupMode.MAINTENANCE) {
			log.info("skipped deactivation (startup mode is MAINTENANCE) : SchedulerPlugin");
			return;
		}

		for (Entry<SchedulerType, Scheduler> entry : _scheduler.entrySet()) {
			try {
				log.debug("shutdown scheduler name=" + entry.getValue().getSchedulerName());
				entry.getValue().shutdown(false);
			} catch (SchedulerException e) {
				log.error("shutdown failure : SchedulerPlugin", e);
			}
		}
	}

	/**
	 * <pre>
	 * 単に定期実行するだけのジョブをスケジューリングするためのメソッド。<br/>
	 * ユーザは実行周期のみを定義可能であり、cronのように具体的な実行タイミングを定義できない。<br/>
	 * </pre>
	 *
	 * @param type スケジューラ定義の保持型
	 * @param name ジョブの名前
	 * @param group ジョブのグループ名
	 * @param startTime 実行開始日時
	 * @param intervalSec 実行間隔[sec]
	 * @param rstOnRestart JVM再起動時に実行開始日時をリセットする場合はtrue(Misfire時間内に実行予定となっていたジョブを繰り返し実行せずに、現在時刻以降の実行予定から開始する）
	 * @param className ジョブが実装されたクラス名
	 * @param methodName ジョブが実装されたメソッド名
	 * @param argsType メソッドの引数型配列
	 * @param args メソッドの引数配列
	 * @throws HinemosUnknown
	 */
	public static void scheduleSimpleJob(SchedulerType type, String name, String group,
			Date startTime, int intervalSec, boolean rstOnRestart,
			String className, String methodName, Class<? extends Serializable>[] argsType, Serializable[] args) throws HinemosUnknown {

		log.debug("scheduleSimpleJob() name=" + name + ", group=" + group + ", startTime=" + startTime
				+ ", rstOnRestart=" + rstOnRestart + ", className=" + className + ", methodName=" + methodName);

		// ジョブ定義の作成
		JobDetail job = JobBuilder.newJob(ReflectionInvokerJob.class)
				.withIdentity(name, group)
				.storeDurably(true)		// ジョブ完了時に削除されない設定を反映
				.requestRecovery(false)	// ジョブ実行が失敗した際に再実行しない設定を反映(JVM起動中に再実行が繰り返される可能性を回避するため)
				.usingJobData(ReflectionInvokerJob.KEY_CLASS_NAME, className)	// ジョブから呼び出すクラス名を反映
				.usingJobData(ReflectionInvokerJob.KEY_METHOD_NAME, methodName)	// ジョブから呼び出すメソッドを反映
				.usingJobData(ReflectionInvokerJob.KEY_RESET_ON_RESTART, rstOnRestart)	// 再起動時にtriggerをリセット()するかどうかを反映
				.build();

		// [WARNING] job.getJobDataMap()ではなく、"trigger".getJobDataMap()に対して値を定義してはいけない。
		// Quartz (JBoss EAP 5.1 Bundle) Bugにより、java.lang.StackOverflowErrorの発生を引き起こす。

		// メソッドの引数を定義する（引数無は0-lengthの配列とする)
		if (args == null) {
			throw new NullPointerException("args must not be null. if not args, set 0-length list.");
		}
		if (argsType == null) {
			throw new NullPointerException("argsType must not be null. if not args, set 0-length list.");
		}
		if (args.length != argsType.length) {
			throw new IndexOutOfBoundsException("list's length is not same between args and argsType.");
		}
		job.getJobDataMap().put(ReflectionInvokerJob.KEY_ARGS_TYPE, argsType);
		job.getJobDataMap().put(ReflectionInvokerJob.KEY_ARGS, args);

		// ジョブ実行定義となるtriggerを作成
		SimpleScheduleBuilder scheduleBuilder = null;
		if (rstOnRestart) {
			log.debug("scheduleSimpleJob() name=" + name + ", misfireHandlingInstruction=NextWithRemainingCount");
			scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(intervalSec).repeatForever().withMisfireHandlingInstructionNextWithRemainingCount();
		} else {
			log.debug("scheduleSimpleJob() name=" + name + ", misfireHandlingInstruction=IgnoreMisfires");
			scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(intervalSec).repeatForever().withMisfireHandlingInstructionIgnoreMisfires();
		}

		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(name, group)
				.startAt(startTime)
				.withSchedule(scheduleBuilder)
				.build();

		// ジョブが既に存在する場合は削除
		log.debug("deleteJob() name=" + name + ", group=" + group);
		deleteJob(type, name, group);

		// ジョブスケジューラを作成
		try {
			synchronized (_schedulerLock) {
				log.debug("scheduleJob() name=" + name + ", group=" + group);
				_scheduler.get(type).scheduleJob(job, trigger);
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed scheduling job. (name = " + name + ", group = " + group + ")", e);
		}
	}

	public static void scheduleCronJob(SchedulerType type, String name, String group,
			Date startTime, String cronExpression, boolean rstOnRestart,
			String className, String methodName, Class<? extends Serializable>[] argsType, Serializable[] args) throws HinemosUnknown {

		log.debug("scheduleCronJob() name=" + name + ", group=" + group + ", startTime=" + startTime + ", cronExpression=" + cronExpression
				+ ", rstOnRestart=" + rstOnRestart + ", className=" + className + ", methodName=" + methodName);

		// ジョブ定義の作成
		JobDetail job = JobBuilder.newJob(ReflectionInvokerJob.class)
				.withIdentity(name, group)
				.storeDurably(true)		// ジョブ完了時に削除されない設定を反映
				.requestRecovery(false)	// ジョブ実行が失敗した際に再実行しない設定を反映(JVM起動中に再実行が繰り返される可能性を回避するため)
				.usingJobData(ReflectionInvokerJob.KEY_CLASS_NAME, className)	// ジョブから呼び出すクラス名を反映
				.usingJobData(ReflectionInvokerJob.KEY_METHOD_NAME, methodName)	// ジョブから呼び出すメソッドを反映
				.usingJobData(ReflectionInvokerJob.KEY_RESET_ON_RESTART, rstOnRestart)	// 再起動時にtriggerをリセット()するかどうかを反映
				.build();

		// [WARNING] job.getJobDataMap()ではなく、"trigger".getJobDataMap()に対して値を定義してはいけない。
		// Quartz (JBoss EAP 5.1 Bundle) Bugにより、java.lang.StackOverflowErrorの発生を引き起こす。

		// メソッドの引数を定義する（引数無は0-lengthの配列とする)
		if (args == null) {
			throw new NullPointerException("args must not be null. if not args, set 0-length list.");
		}
		if (argsType == null) {
			throw new NullPointerException("argsType must not be null. if not args, set 0-length list.");
		}
		if (args.length != argsType.length) {
			throw new IndexOutOfBoundsException("list's length is not same between args and argsType.");
		}
		job.getJobDataMap().put(ReflectionInvokerJob.KEY_ARGS_TYPE, argsType);
		job.getJobDataMap().put(ReflectionInvokerJob.KEY_ARGS, args);

		// ジョブ実行定義となるtriggerを作成
		CronScheduleBuilder schedulerBuilder = null;
		if (rstOnRestart) {
			log.debug("scheduleCronJob() name=" + name + ", misfireHandlingInstruction=DoNothing");
			schedulerBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing();
		} else {
			log.debug("scheduleCronJob() name=" + name + ", misfireHandlingInstruction=IgnoreMisfires");
			schedulerBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionIgnoreMisfires();
		}

		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(name, group)
				.startAt(startTime)
				.withSchedule(schedulerBuilder)
				.build();

		// ジョブが既に存在する場合は削除
		log.debug("deleteJob() name=" + name + ", group=" + group);
		deleteJob(type, name, group);


		// ジョブスケジューラを作成
		try {
			synchronized (_schedulerLock) {
				log.debug("scheduleJob() name=" + name + ", group=" + group);
				_scheduler.get(type).scheduleJob(job, trigger);
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed scheduling job. (name = " + name + ", group = " + group + ")", e);
		}
	}

	/**
	 * <pre>
	 * 実行中のジョブを停止する。<br/>
	 * </pre>
	 * @param type スケジューラ定義の保持型
	 * @param name ジョブの名前
	 * @param group ジョブのグループ名
	 * @throws HinemosException 予期せぬ内部エラー
	 */
	public static void pauseJob(SchedulerType type, String name, String group) throws HinemosUnknown {
		try {
			synchronized (_schedulerLock) {
				_scheduler.get(type).pauseJob(new JobKey(name, group));
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed pausing job. (name = " + name + ", group = " + group + ")", e);
		}
	}

	/**
	 * <pre>
	 * 既にジョブが登録されている場合、そのジョブを削除する。<br/>
	 * (APIの仕様上、未登録の場合はfalseが返されるだけで例外は生じない)</br>
	 * </pre>
	 * @param type スケジューラ定義の保持型
	 * @param name ジョブの名前
	 * @param group ジョブのグループ名
	 * @throws HinemosUnknown 予期せぬ内部エラー
	 */
	public static void deleteJob(SchedulerType type, String name, String group) throws HinemosUnknown {
		try {
			synchronized (_schedulerLock) {
				_scheduler.get(type).deleteJob(new JobKey(name, group));
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed removing job. (name = " + name + ", group = " + group + ")", e);
		}
	}

	/**
	 * <pre>
	 * cron型のジョブの実行タイミングを変更する。<br/>
	 * </pre>
	 * @param type
	 * @param name
	 * @param group
	 * @param cronExpression
	 * @throws HinemosUnknown
	 */
	public static void updateCronJob(SchedulerType type, String name, String group, String cronExpression) throws HinemosUnknown {
		log.debug("updateCronJob() name=" + name + ", group=" + group + ", cronExpression=" + cronExpression);

		try {
			synchronized (_schedulerLock) {
				Trigger trigger = TriggerBuilder.newTrigger()
						.withIdentity(name, group)
						.startAt(new Date())
						.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing())
						.build();

				_scheduler.get(type).rescheduleJob(new TriggerKey(name, group), trigger);
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed updating job. (name = " + name + ", group = " + group + ", cronExpression = " + cronExpression + ")", e);
		}
	}

	public static void updateSimpleJob(SchedulerType type, String name, String group, int intervalSec) throws HinemosUnknown {
		log.debug("updateSimpleJob() name=" + name + ", group=" + group);

		try {
			synchronized (_schedulerLock) {
				Trigger trigger = TriggerBuilder.newTrigger()
						.withIdentity(name, group)
						.startAt(new Date())
						.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(intervalSec).repeatForever())
						.build();

				_scheduler.get(type).rescheduleJob(new TriggerKey(name, group), trigger);
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed updating job. (name = " + name + ", group = " + group + ", intervalSec = " + intervalSec + ")", e);
		}
	}

	private void initTrigger() throws HinemosUnknown {
		// setup Job for Status Notification Management
		try {
			if (! _scheduler.get(SchedulerType.RAM).checkExists(new JobKey("MonitorController", "MON"))) {
				scheduleCronJob(SchedulerType.RAM, "MonitorController", "MON",
						new Date(), HinemosPropertyUtil.getHinemosPropertyStr(
								"scheduler.monitor.cron", "0 */5 * * * ? *"),
						true, MonitorControllerBean.class.getName(),
						"manageStatus", new Class[0], new Serializable[0]);
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed initializing job. (name = MonitorController, group = MON)", e);
		}

		// setup Job for Job Management
		try {
			if (! _scheduler.get(SchedulerType.RAM).checkExists(new JobKey("JobRunManagement", "JOB_MANAGEMENT"))) {
				scheduleCronJob(SchedulerType.RAM, "JobRunManagement",
						"JOB_MANAGEMENT", new Date(),
						HinemosPropertyUtil.getHinemosPropertyStr("scheduler.job.cron",
								"0 */1 * * * ? *"), true,
						JobRunManagementBean.class.getName(), "run",
						new Class[0], new Serializable[0]);
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed initializing job. (name = JobRunManagement, group = JOB_MANAGEMENT)", e);
		}

		// setup Job for Repository Run Management
		try {
			if (! _scheduler.get(SchedulerType.RAM).checkExists(new JobKey("RepositoryRunManagement", "REPOSITORY_MANAGEMENT"))) {
				scheduleCronJob(SchedulerType.RAM, "RepositoryRunManagement",
						"REPOSITORY_MANAGEMENT", new Date(),
						HinemosPropertyUtil.getHinemosPropertyStr("scheduler.repository.cron",
								"40 */1 * * * ? *"), true,
						RepositoryRunManagementBean.class.getName(), "run",
						new Class[0], new Serializable[0]);
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed initializing job. (name = RepositoryRunManagement, group = REPOSITORY_MANAGEMENT)", e);
		}

		// setup Job for Monitor Status Management
		try {
			if (! _scheduler.get(SchedulerType.RAM).checkExists(new JobKey("MonitorStatusManagement", "MONITOR_STATUS_MANAGEMENT"))) {
				scheduleCronJob(SchedulerType.RAM, "MonitorStatusManagement",
						"MONITOR_STATUS_MANAGEMENT", new Date(),
						HinemosPropertyUtil.getHinemosPropertyStr(
								"scheduler.monitor.status.cron",
								"50 3/10 * * * ? *"), true,
						MonitorControllerBean.class.getName(),
						"persistMonitorStatusCache", new Class[0],
						new Serializable[0]);
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed initializing job. (name = MonitorStatusManagement, group = MONITOR_STATUS_MANAGEMENT)", e);
		}

		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			// 性能管理機能の全ての収集を再開する
			new PerformanceRestartManager().restartAll();

			// 監視をリスケジューリングする。
			new ModifySchedule().updateScheduleAll();
			jtm.commit();
		} catch (Exception e) {
			jtm.rollback();
			log.warn("failed to start schedulers.", e);
		} finally {
			jtm.close();
		}
	}

	public static List<SchedulerInfo> getSchedulerList(SchedulerType type) throws HinemosUnknown {
		List<SchedulerInfo> list = new ArrayList<SchedulerInfo>();

		try {
			synchronized (_schedulerLock) {
				for (String group : _scheduler.get(type).getTriggerGroupNames()) {
					for (TriggerKey key : _scheduler.get(type).getTriggerKeys(GroupMatcher.triggerGroupEquals(group))) {
						Trigger trigger = _scheduler.get(type).getTrigger(key);

						list.add(new SchedulerInfo(key.getName(), key.getGroup(),
								trigger.getStartTime(), trigger.getPreviousFireTime(), trigger.getNextFireTime(),
								_scheduler.get(type).getTriggerState(key) == TriggerState.PAUSED ? true : false));
					}
				}
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed getting list of scheduled jobs.", e);
		}

		return Collections.unmodifiableList(list);
	}

	public static Date getNextFireTime(SchedulerType type, String name, String group) throws HinemosUnknown {
		Date nextFireTime = null;

		try {
			synchronized (_schedulerLock) {
				Trigger trigger = _scheduler.get(type).getTrigger(new TriggerKey(name, group));
				nextFireTime = trigger.getNextFireTime();
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed getting next fire time.", e);
		}

		log.debug("getNextFireTime() : " + nextFireTime);
		return nextFireTime;
	}

	public static String getScheduledList(SchedulerType type) throws HinemosUnknown {
		String str = "";
		String lineSeparator = System.getProperty("line.separator");

		try {
			synchronized (_schedulerLock) {
				for (String group : _scheduler.get(type).getTriggerGroupNames()) {
					for (TriggerKey key : _scheduler.get(type).getTriggerKeys(GroupMatcher.triggerGroupEquals(group))) {
						Trigger trigger = _scheduler.get(type).getTrigger(key);

						Trigger.TriggerState state = _scheduler.get(type).getTriggerState(key);
						String startFireTime = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", trigger.getStartTime());
						String prevFireTime = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", trigger.getPreviousFireTime());
						String nextFireTime = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", trigger.getNextFireTime());

						str = str + String.format("%s (in %s) :", key.getName(), key.getGroup()) + lineSeparator;
						str = str + String.format("   start fire time - %s", startFireTime) + lineSeparator;
						str = str + String.format("   last fire time  - %s", prevFireTime) + lineSeparator;
						str = str + String.format("   next fire time  - %s", nextFireTime) + lineSeparator;
						str = str + String.format("   current state   - %s", state) + lineSeparator;

						str = str + lineSeparator;
					}
				}

			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed getting list of scheduled jobs.", e);
		}

		log.debug("getScheduledList() : " + str);
		return str;
	}

	public static boolean isSchedulerRunning(SchedulerType type) throws HinemosUnknown {
		try {
			synchronized (_schedulerLock) {
				return _scheduler.get(type).isShutdown() ? false : true;
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed getting state of scheduler.", e);
		}
	}

}
