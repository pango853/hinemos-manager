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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.ProcessConstant;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.bean.MonitorJudgementInfo;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;
import com.clustercontrol.monitor.run.util.CallableTask;
import com.clustercontrol.monitor.run.util.ParallelExecution;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.performance.bean.CollectedDataErrorTypeConstant;
import com.clustercontrol.performance.bean.Sample;
import com.clustercontrol.performance.util.PerformanceDataUtil;
import com.clustercontrol.repository.bean.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * 監視を実行する抽象クラス<BR>
 * <p>
 * 監視種別（真偽値，数値，文字列）の各クラスで継承してください。
 *
 * @version 4.0.0
 * @since 2.0.0
 */
abstract public class RunMonitor {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( RunMonitor.class );

	/** 通知のメッセージ。 */
	private static final String MESSAGE_INFO = Messages.getString("message.info");
	/** 警告のメッセージ。 */
	private static final String MESSAGE_WARNING = Messages.getString("message.warning");
	/** 危険のメッセージ。 */
	private static final String MESSAGE_CRITICAL = Messages.getString("message.critical");
	/** 不明のメッセージ。 */
	private static final String MESSAGE_UNKNOWN = Messages.getString("message.unknown");

	/** 監視情報のローカルコンポーネント。 */
	protected MonitorInfoEntity m_monitor;

	/** 監視対象ID。 */
	protected String m_monitorTypeId;

	/** 監視項目ID。 */
	protected String m_monitorId;

	/** 通知ID */
	private String m_notifyGroupId;

	/** 監視対象ファシリティID。 */
	protected String m_facilityId;
	
	/** 監視対象のノードのID（スコープ配下の個別のノード、本プロパティはプロセス監視でのみ使用される）*/
	protected String m_nodeId;

	/**
	 * 値取得の失敗時の重要度。
	 * 初期値は値取得失敗を示す値(-1)として、
	 * setMonitorInfoがコールされた際に、値取得失敗時の重要度がセットされる。
	 *
	 * @see #setMonitorInfo(String, String)
	 */
	protected int m_failurePriority = PriorityConstant.TYPE_FAILURE;

	/** 収集間隔 */
	protected int m_runInterval;

	/** 監視開始時刻。 */
	protected Date m_now;

	/** スコープ 監視結果取得時刻。 */
	protected long m_scopeDate;

	/** ノード 監視結果取得時刻。 */
	protected long m_nodeDate;

	/** 監視単位。 */
	protected int m_monitorBlock;

	/** ノードフラグ。 */
	protected boolean m_isNode;

	/** ノード情報一覧 */
	protected volatile Map<String, NodeInfo> nodeInfo;

	/** 重要度別ファシリティ名マップ。 */
	protected HashMap<Integer, ArrayList<String>> m_priorityMap;

	private static final String DISPLAY_NAME_STRING = "";

	/** カレンダの期間内か否かのフラグ */
	protected boolean m_isInCalendarTerm;

	/** 次の監視タイミングがカレンダの期間内か否かのフラグ */
	protected boolean m_isInNextCalendarTerm;

	/**
	 * 判定情報マップ。
	 * <p>
	 * <dl>
	 *  <dt>キー</dt>
	 *  <dd>真偽値監視：真偽値定数（{@link com.clustercontrol.monitor.run.bean.TruthConstant}）</dd>
	 *  <dd>数値監視：重要度定数（{@link com.clustercontrol.bean.PriorityConstant}）</dd>
	 *  <dd>文字列監視：順序（{@link com.clustercontrol.monitor.run.ejb.entity.MonitorStringValueInfoBean#getOrderNo()}）</dd>
	 * </dl>
	 */
	protected TreeMap<Integer, MonitorJudgementInfo> m_judgementInfoList;

	/** 監視取得値 */
	protected double m_value;

	/**
	 * トランザクションを開始し、引数で指定された監視情報の監視を実行します。
	 *
	 * @param monitorTypeId 監視対象ID
	 * @param monitorId 監視項目ID
	 * @throws FacilityNotFound
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see #runMonitorInfo()
	 */
	public void run(String monitorTypeId, String monitorId) throws FacilityNotFound, MonitorNotFound, InvalidRole, HinemosUnknown {

		this.initialize(monitorTypeId);

		m_monitorTypeId = monitorTypeId;
		m_monitorId = monitorId;

		boolean result = false;

		try
		{
			// 監視実行
			result = runMonitorInfo();
		} finally {
			if(!result){
				AplLogger apllog = new AplLogger("MON", "mon");
				String[] args = {m_monitorTypeId,m_monitorId};
				apllog.put("SYS", "012", args);
			}
			// 終了処理
			this.terminate();
		}
	}

	/**
	 * 監視を実行します。（並列処理）
	 * <p>
	 * <ol>
	 * <li>監視情報を取得し、保持します（{@link #setMonitorInfo(String, String)}）。</li>
	 * <li>判定情報を取得し、判定情報マップに保持します（{@link #setJudgementInfo()}）。</li>
	 * <li>チェック条件情報を取得し、保持します（{@link #setCheckInfo()}）。</li>
	 * <li>ファシリティ毎に並列に監視を実行し、値を収集します。 （{@link #collect(String)}）。</li>
	 * <li>監視結果から、判定結果を取得します。 （{@link #getCheckResult(boolean)}）。</li>
	 * <li>監視結果から、重要度を取得します（{@link #getPriority(int)}）。</li>
	 * <li>監視結果を通知します（{@link #notify(boolean, String, int, Date)}）。</li>
	 * </ol>
	 *
	 * @return 実行に成功した場合、</code> true </code>
	 * @throws FacilityNotFound
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 *
	 * @see #setMonitorInfo(String, String)
	 * @see #setJudgementInfo()
	 * @see #setCheckInfo()
	 * @see #collect(String)
	 * @see #getCheckResult(boolean)
	 * @see #getPriority(int)
	 * @see #notify(boolean, String, int, Date)
	 */
	protected boolean runMonitorInfo() throws FacilityNotFound, MonitorNotFound, InvalidRole, EntityExistsException, HinemosUnknown {

		m_now = new Date();

		m_priorityMap = new HashMap<Integer, ArrayList<String>>();
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_INFO),		new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_WARNING),	new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_CRITICAL),	new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_UNKNOWN),	new ArrayList<String>());

		try
		{
			// 監視基本情報を設定
			boolean run = this.setMonitorInfo(m_monitorTypeId, m_monitorId);
			if(!run){
				// 処理終了
				return true;
			}

			// 判定情報を設定
			setJudgementInfo();

			// チェック条件情報を設定
			setCheckInfo();

			// ファシリティIDの配下全ての一覧を取得
			// 有効/無効フラグがtrueとなっているファシリティIDを取得する
			ArrayList<String> facilityList = new RepositoryControllerBean().getExecTargetFacilityIdList(m_facilityId, m_monitor.getOwnerRoleId());
			if (facilityList.size() == 0) {
				return true;
			}

			m_isNode = new RepositoryControllerBean().isNode(m_facilityId);

			// 監視対象となっているノードの変数を取得
			nodeInfo = new HashMap<String, NodeInfo>();
			for (String facilityId : facilityList) {
				try {
					synchronized (this) {
						nodeInfo.put(facilityId, new RepositoryControllerBean().getNode(facilityId));
					}
				} catch (FacilityNotFound e) {
					// 何もしない
				}
			}

			m_log.debug("monitor start : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

			String facilityId = null;

			/**
			 * 監視の実行
			 */
			// ファシリティIDの数だけ、各監視処理を実行する
			Iterator<String> itr = facilityList.iterator();
			
			ExecutorCompletionService<MonitorRunResultInfo> ecs = new ExecutorCompletionService<MonitorRunResultInfo>(ParallelExecution.instance().getExecutorService());
			int taskCount = 0;
			
			while(itr.hasNext()){
				facilityId = itr.next();
				if(facilityId != null && !"".equals(facilityId)){

					// マルチスレッド実行用に、RunMonitorのインスタンスを新規作成する
					// インスタンスを新規作成するのは、共通クラス部分に監視結果を保持するため
					RunMonitor runMonitor = this.createMonitorInstance();

					// 監視実行に必要な情報を再度セットする
					runMonitor.m_monitorTypeId = this.m_monitorTypeId;
					runMonitor.m_monitorId = this.m_monitorId;
					runMonitor.m_now = this.m_now;
					runMonitor.setMonitorInfo(m_monitorTypeId, m_monitorId);
					runMonitor.m_priorityMap = this.m_priorityMap;
					runMonitor.setJudgementInfo();
					runMonitor.nodeInfo = this.nodeInfo;
					runMonitor.setCheckInfo();
					runMonitor.m_nodeId = facilityId;
					
					ecs.submit(new CallableTask(runMonitor, facilityId));
					taskCount++;
					
					if (m_log.isDebugEnabled()) {
						m_log.debug("starting monitor result : monitorId = " + m_monitorId + ", facilityId = " + facilityId);
					}
				}
				else {
					facilityList.remove(facilityId);
				}
			}
			/**
			 * 監視結果の集計
			 */
			MonitorRunResultInfo result = new MonitorRunResultInfo();	// 監視結果を格納

			m_log.debug("total start : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

			// 収集値の入れ物を作成
			Sample sample = null;
			if(m_monitor.getCollectorFlg() == ValidConstant.TYPE_VALID){
				sample = new Sample(m_monitorId, new Date());
			}
			
			for (int i = 0; i < taskCount; i++) {
				Future<MonitorRunResultInfo> future = ecs.take();
				result = future.get();	// 監視結果を取得
				
				facilityId = result.getFacilityId();
				m_nodeDate = result.getNodeDate();
				
				if (m_log.isDebugEnabled()) {
					m_log.debug("finished monitor : monitorId = " + m_monitorId + ", facilityId = " + facilityId);
				}
				
				// 処理する場合
				if(result.getProcessType() == ProcessConstant.TYPE_YES){

					// 監視結果を通知
					notify(true, facilityId, result.getCheckResult(), new Date(m_nodeDate), result);

					// 個々の収集値の登録
					if(sample != null){
						if(result.isCollectorResult()){
							sample.set(facilityId, m_monitorId, DISPLAY_NAME_STRING, result.getValue(), CollectedDataErrorTypeConstant.NOT_ERROR);
						}else{
							sample.set(facilityId, m_monitorId, DISPLAY_NAME_STRING, result.getValue(), CollectedDataErrorTypeConstant.UNKNOWN);
						}
					}
				}
			}

			// 収集値をまとめて登録
			if(sample != null){
				PerformanceDataUtil.put(sample);
			}

			m_log.debug("monitor end : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

			return true;

		} catch (FacilityNotFound e) {
			throw e;
		} catch (InterruptedException e) {
			m_log.info("runMonitorInfo() monitorTypeId = " + m_monitorTypeId + ", monitorId  = " + m_monitorId + " : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			return false;
		} catch (ExecutionException e) {
			m_log.info("runMonitorInfo() monitorTypeId = " + m_monitorTypeId + ", monitorId  = " + m_monitorId + " : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			return false;
		}
	}

	/**
	 * 判定情報を設定します。
	 * <p>
	 * 各監視種別（真偽値，数値，文字列）のサブクラスで実装します。
	 * 監視情報より判定情報を取得し、判定情報マップに保持します。
	 */
	protected abstract void setJudgementInfo();

	/**
	 * チェック条件情報を設定します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 * 監視情報よりチェック条件情報を取得し、保持します。
	 *
	 * @throws MonitorNotFound
	 */
	protected abstract void setCheckInfo() throws MonitorNotFound ;

	/**
	 * 監視対象に対する監視を実行し、値を収集します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 * 引数で指定されたファシリティIDの監視を実行して値を収集し、
	 * 各監視種別（真偽値，数値，文字列）のサブクラスの監視取得値にセットします。
	 *
	 * @param facilityId 監視対象のファシリティID
	 * @return 値取得に成功した場合、</code> true </code>
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	public abstract boolean collect(String facilityId) throws FacilityNotFound, HinemosUnknown;

	/**
	 * マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッドです。
	 * 監視を実装するクラスでは、RunMonitorクラス（すべての監視実装クラスの親クラス）のインスタンスを返すために、
	 * このメソッドを実装してください。
	 * このメソッドで生成されたインスタンスは、監視実行スレッドごとの監視結果を保持するために利用されます。
	 *
	 * すべての監視はマルチスレッドで動作しており、監視設定単位でRunMonitorクラスのインスタンスを共有しています。
	 * 監視結果（収集値）は、数値監視、文字列監視、真偽値監視のレベルで共有される変数に格納されるため、
	 * 同一監視設定で複数ノードに対して監視を実行する場合、監視結果に不整合が生じる可能性があります。
	 *
	 * したがって、本メソッドによって新たにインスタンスを生成し、マルチスレッドを実現するCallableTaskに渡す必要があります。
	 *
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#runMonitorInfo()
	 * @see com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType
	 * @see com.clustercontrol.monitor.run.factory.RunMonitorStringValueType
	 * @see com.clustercontrol.monitor.run.factory.RunMonitorTruthValueType
	 * @see com.clustercontrol.monitor.run.util.CallableTask
	 *
	 * @return
	 * @throws HinemosUnknown
	 */
	protected abstract RunMonitor createMonitorInstance() throws HinemosUnknown;

	/**
	 * 判定結果を返します。
	 * <p>
	 * 各監視種別（真偽値，数値，文字列）のサブクラスで実装します。
	 * {@link #collect(String)}メソッドで監視を実行した後に呼ばれます。
	 * 監視取得値と判定情報から、判定結果を返します。
	 * <p>
	 * <dl>
	 *  <dt>判定結果の値</dt>
	 *  <dd>真偽値監視：真偽値定数（{@link com.clustercontrol.monitor.run.bean.TruthConstant}）</dd>
	 *  <dd>数値監視：重要度定数（{@link com.clustercontrol.bean.PriorityConstant}）</dd>
	 *  <dd>文字列監視：順序（{@link com.clustercontrol.monitor.run.ejb.entity.MonitorStringValueInfoBean#getOrderNo()}）</dd>
	 * </dl>
	 *
	 * @param ret 監視の実行が成功した場合、</code> true </code>
	 * @return 判定結果
	 */
	public abstract int getCheckResult(boolean ret);

	/**
	 * 重要度を返します。
	 * <p>
	 * 引数で指定された判定結果のキーに対応する重要度を、判定情報マップから取得します。
	 *
	 * @param key 各監視種別（真偽値，数値，文字列）の判定結果のキー
	 * @return 重要度
	 * @since 2.0.0
	 */
	public int getPriority(int key) {

		MonitorJudgementInfo info = m_judgementInfoList.get(key);
		if(info != null){
			return info.getPriority();
		}
		else{
			return m_failurePriority;
		}
	}

	/**
	 * 通知グループIDを返します。
	 *
	 * @return 通知グループID
	 * @since 2.1.0
	 */
	public String getNotifyGroupId(){
		return m_notifyGroupId;
	}

	/**
	 * ノード用メッセージIDを返します。
	 * <p>
	 * 引数で指定された判定結果のキーに対応するメッセージIDを、判定情報マップから取得します。
	 *
	 * @param key 各監視種別（真偽値，数値，文字列）の判定結果のキー
	 * @return メッセージID
	 * @since 2.0.0
	 */
	public String getMessageId(int key) {

		MonitorJudgementInfo info = m_judgementInfoList.get(Integer.valueOf(key));
		if(info != null){
			if(info.getMessageId() != null){
				return info.getMessageId();
			}
		}
		return "";
	}

	/**
	 * ノード用メッセージを返します。
	 * <p>
	 * 引数で指定された判定結果のキーに対応するメッセージを、判定情報マップから取得します。
	 *
	 * @param key 各監視種別（真偽値，数値，文字列）の判定結果のキー
	 * @return メッセージ
	 * @since 2.0.0
	 */
	public String getMessage(int key) {

		MonitorJudgementInfo info = m_judgementInfoList.get(Integer.valueOf(key));
		if(info != null){
			if(info.getMessage() != null){
				return info.getMessage();
			}
		}
		return "";
	}

	/**
	 * ノード用オリジナルメッセージを返します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 *
	 * @param key 各監視種別（真偽値，数値，文字列）の判定結果のキー
	 * @return オリジナルメッセージ
	 * @since 2.0.0
	 */
	public abstract String getMessageOrg(int key);

	/**
	 * スコープ用メッセージIDを返します。
	 * <p>
	 * 引数で指定された重要度に対応するメッセージIDを、判定情報マップから取得します。
	 *
	 * @param priority 重要度
	 * @return メッセージ
	 * @since 2.1.0
	 */
	protected String getMessageIdForScope(int priority){

		Set<Integer> set = m_judgementInfoList.keySet();
		for (Iterator<Integer> iter = set.iterator(); iter.hasNext();) {
			Integer key = iter.next();
			MonitorJudgementInfo info = m_judgementInfoList.get(key);
			if(priority == info.getPriority()){
				if(info.getMessageId() != null){
					return info.getMessageId();
				}
			}
		}
		return "";
	}

	/**
	 * スコープ用メッセージを返します。
	 * <p>
	 * 引数で指定された重要度に対応するメッセージを返します。
	 *
	 * @param priority 重要度
	 * @return メッセージ
	 */
	protected String getMessageForScope(int priority){

		if(priority == PriorityConstant.TYPE_INFO){
			return MESSAGE_INFO;
		}
		else if(priority == PriorityConstant.TYPE_WARNING){
			return MESSAGE_WARNING;
		}
		else if(priority == PriorityConstant.TYPE_CRITICAL){
			return MESSAGE_CRITICAL;
		}
		else{
			return MESSAGE_UNKNOWN;
		}
	}

	/**
	 * スコープ用オリジナルメッセージを返します。
	 * <p>
	 * 重要度別の件数，ファシリティ名を表示する文字列を作成し、返します。
	 *
	 * <dl>
	 *  <dt>通知:X件, 警告:X件, 危険:X件, 不明:X件</dt>
	 *  <dt>通知:</dt>
	 * 	 <dd>NODE1</dd>
	 *   <dd>NODE2</dd>
	 *  <dt>警告:</dt>
	 *   <dd>NODE3</dd>
	 *  <dt>危険:</dt>
	 * 	 <dd>NODE4</dd>
	 *  <dt>不明:</dt>
	 *   <dd>NODE5</dd>
	 * </dl>
	 *
	 * @param priority 重要度
	 * @return オリジナルメッセージ
	 */
	protected String getMessageOrgForScope(int priority){

		ArrayList<String> info = m_priorityMap.get(Integer.valueOf(PriorityConstant.TYPE_INFO));
		ArrayList<String> warning = m_priorityMap.get(Integer.valueOf(PriorityConstant.TYPE_WARNING));
		ArrayList<String> critical = m_priorityMap.get(Integer.valueOf(PriorityConstant.TYPE_CRITICAL));
		ArrayList<String> unknown = m_priorityMap.get(Integer.valueOf(PriorityConstant.TYPE_UNKNOWN));

		// 重要度別の件数
		StringBuffer summary = new StringBuffer();
		summary.append(Messages.getString("info") + ":" + info.size() + Messages.getString("record") + ", ");
		summary.append(Messages.getString("warning") + ":" + warning.size() + Messages.getString("record") + ", ");
		summary.append(Messages.getString("critical") + ":" + critical.size() + Messages.getString("record") + ", ");
		summary.append(Messages.getString("unknown") + ":" + unknown.size() + Messages.getString("record"));

		// 重要度別のファシリティ名
		StringBuffer detail = new StringBuffer();
		detail.append(getItemListString("\n" + Messages.getString("info"), info));
		detail.append(getItemListString("\n" + Messages.getString("warning"), warning));
		detail.append(getItemListString("\n" + Messages.getString("critical"), critical));
		detail.append(getItemListString("\n" + Messages.getString("unknown"), unknown));

		return summary.toString() + detail.toString();
	}

	/**
	 * ノードの監視結果取得時刻を返します。
	 *
	 * @return ノードの監視結果取得時刻
	 * @since 3.0.0
	 */
	public long getNodeDate() {
		return m_nodeDate;
	}


	/**
	 * 監視値を返します。
	 *
	 * @return 監視値
	 * @since 4.0.0
	 */
	public double getValue() {
		return m_value;
	}

	/**
	 * 監視情報を設定します。
	 * <p>
	 * 引数で指定された監視対象と監視項目の監視情報を取得し、保持します。<BR>
	 * 通知IDが指定されていない場合は、処理を終了します。<BR>
	 * カレンダIDが指定されていた場合は、稼動日か否かチェックします（{@link com.clustercontrol.calendar.session.CalendarControllerBean#isRun(java.lang.String, java.util.Date)}）。非稼動日の場合は、処理を終了します。
	 *
	 * @param monitorTypeId 監視対象ID
	 * @param monitorId 監視項目ID
	 * @return 監視を実行する場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 *
	 * @since 2.0.0
	 *
	 * @see com.clustercontrol.calendar.session.CalendarControllerBean#isRun(java.lang.String, java.util.Date)
	 */
	protected boolean setMonitorInfo(String monitorTypeId, String monitorId) throws MonitorNotFound, HinemosUnknown {
		m_isInCalendarTerm = true;
		m_isInNextCalendarTerm = false;

		// 監視基本情報を取得
		m_monitor = QueryUtil.getMonitorInfoPK_NONE(monitorId);

		// 通知ID
		m_notifyGroupId = m_monitor.getNotifyGroupId();
		if(m_notifyGroupId == null || "".equals(m_notifyGroupId)){
			if(m_monitor.getMonitorType().intValue() != MonitorTypeConstant.TYPE_STRING){
				// 通知しない場合は、処理終了
				return false;
			}
		}

		// 監視対象ファシリティID
		m_facilityId = m_monitor.getFacilityId();
		// 値取得失敗時の重要度
		m_failurePriority = m_monitor.getFailurePriority().intValue();
		// 収集間隔
		m_runInterval = m_monitor.getRunInterval().intValue();

		// カレンダID
		String calendarId = m_monitor.getCalendarId();
		if(calendarId != null && !"".equals(calendarId)){
			// 稼働日か否かチェック
			try {
				CalendarControllerBean calendar = new CalendarControllerBean();
				if(!calendar.isRun(calendarId, m_now==null?null:m_now.getTime()).booleanValue()){
					// 非稼働日の場合は、処理終了
					m_log.debug("setMonitorInfo() monitorTypeId = " + m_monitorTypeId + ", monitorId = " + m_monitorId  + ", calenderId = " + calendarId
							+ ". The monitor is not executed because of non-operating day.");
					m_isInCalendarTerm = false;

					// 次回の監視契機が稼働日か
					if(calendar.isRun(calendarId, m_now==null?null:(m_now.getTime() + (m_runInterval*1000))).booleanValue()){
						m_log.debug("setMonitorInfo() : monitorTypeId:" + m_monitorTypeId + ",monitorId:" + m_monitorId  + ",calenderId:" + calendarId + ". Next term is operating day.");
						m_isInNextCalendarTerm = true;
					}

					return false;
				}
			} catch (InvalidRole e) {
				// 指定されたカレンダIDの参照権限がない場合は、処理終了
				// 手動でない限りここは通らない。
				// （ADMINISTRATORSロールのユーザ、もしくはユーザ未指定の場合はオブジェクト権限をしないため）
				return false;
			} catch (CalendarNotFound e) {
				// 指定されたカレンダIDがすでに存在しない場合は、処理終了
				return false;
			}
		}

		return true;
	}

	/**
	 * 監視管理に通知します。
	 * <p>
	 * 通知に必要となる情報をログ出力情報にセットし、キューへメッセージを送信します。
	 * <ol>
	 * <li>通知IDを取得し、<code>null</code>の場合は、処理を終了します。</li>
	 * <li>各情報を取得し、ログ出力情報（{@link com.clustercontrol.monitor.message.LogOutputNotifyInfo }）にセットします。
	 * 監視単位がノードの場合とスコープの場合では、通知する情報が異なります。
	 * </li>
	 * <li>ログ出力情報を、ログ出力キューへ送信します。</li>
	 * </ol>
	 *
	 * @param isNode ノードの場合、</code> true </code>
	 * @param facilityId 通知対象のファシリティID
	 * @param result 判定結果
	 * @param generationDate ログ出力日時（監視を実行した日時）
	 * @throws HinemosUnknown
	 * @since 2.0.0
	 *
	 * @see com.clustercontrol.monitor.message.LogOutputNotifyInfo
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#getFacilityPath(java.lang.String, java.lang.String)
	 * @see #getNotifyRelationInfos()
	 * @see #getPriority(int)
	 * @see #getMessageId(int)
	 * @see #getMessage(int)
	 * @see #getMessageOrg(int)
	 * @see #getJobRunInfo(int)
	 * @see #getMessageForScope(int)
	 * @see #getMessageIdForScope(int)
	 * @see #getMessageOrgForScope(int)
	 * @see #getJobRunInfoForScope(int)
	 */
	protected void notify(
			boolean isNode,
			String facilityId,
			int result,
			Date generationDate) throws HinemosUnknown {
		// for debug
		if (m_log.isDebugEnabled()) {
			m_log.debug("notify() isNode = " + isNode + ", facilityId = " + facilityId
					+ ", result = " + result + ", generationDate = " + generationDate.toString());
		}

		// 監視無効の場合、通知しない
		if(m_monitor.getMonitorFlg() == ValidConstant.TYPE_INVALID){
			m_log.debug("notify() isNode = " + isNode + ", facilityId = " + facilityId
					+ ", result = " + result + ", generationDate = " + generationDate.toString()
					+ ", monitorFlg is false");
			return;
		}

		// 通知IDが指定されていない場合、通知しない
		String notifyGroupId = getNotifyGroupId();
		if(notifyGroupId == null || "".equals(notifyGroupId)){
			return;
		}

		// 通知情報を設定
		OutputBasicInfo notifyInfo = new OutputBasicInfo();
		notifyInfo.setPluginId(m_monitorTypeId);
		notifyInfo.setMonitorId(m_monitorId);
		notifyInfo.setApplication(m_monitor.getApplication());

		String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
		notifyInfo.setFacilityId(facilityId);
		notifyInfo.setScopeText(facilityPath);

		int priority = -1;
		String messageId = "";
		String message = "";
		String messageOrg = "";

		if(isNode){
			// ノードの場合
			priority = getPriority(result);
			messageId = getMessageId(result);
			message = getMessage(result);
			messageOrg = getMessageOrg(result);
		}
		else{
			// スコープの場合
			priority = result;
			messageId = getMessageIdForScope(result);
			message = getMessageForScope(result);
			messageOrg = getMessageOrgForScope(result);
		}
		notifyInfo.setPriority(priority);
		notifyInfo.setMessageId(messageId);
		notifyInfo.setMessage(message);
		notifyInfo.setMessageOrg(messageOrg);
		if (generationDate != null) {
			notifyInfo.setGenerationDate(generationDate.getTime());
		}
		// for debug
		if (m_log.isDebugEnabled()) {
			m_log.debug("notify() priority = " + priority
					+ " , messageId = " + messageId
					+ " , message = " + message
					+ " , messageOrg = " + messageOrg
					+ ", generationDate = " + generationDate);
		}

		// ログ出力情報を送信
		if (m_log.isDebugEnabled()) {
			m_log.debug("sending message"
					+ " : priority=" + notifyInfo.getPriority()
					+ " generationDate=" + notifyInfo.getGenerationDate() + " pluginId=" + notifyInfo.getPluginId()
					+ " monitorId=" + notifyInfo.getMonitorId() + " facilityId=" + notifyInfo.getFacilityId()
					+ " subKey=" + notifyInfo.getSubKey()
					+ ")");
		}

		new NotifyControllerBean().notify(notifyInfo, notifyGroupId);
	}

	/**
	 * 監視管理に通知します。(並列処理時)
	 * <p>
	 * 通知に必要となる情報をログ出力情報にセットし、キューへメッセージを送信します。
	 * <ol>
	 * <li>通知IDを取得し、<code>null</code>の場合は、処理を終了します。</li>
	 * <li>各情報を取得し、ログ出力情報（{@link com.clustercontrol.monitor.message.LogOutputNotifyInfo }）にセットします。
	 * 監視単位がノードの場合とスコープの場合では、通知する情報が異なります。
	 * </li>
	 * <li>ログ出力情報を、ログ出力キューへ送信します。</li>
	 * </ol>
	 *
	 * @param isNode ノードの場合、</code> true </code>
	 * @param facilityId 通知対象のファシリティID
	 * @param result 判定結果
	 * @param generationDate ログ出力日時（監視を実行した日時）
	 * @param resultList 並列実行時の情報
	 * @throws HinemosUnknown
	 * @since 2.0.0
	 *
	 * @see com.clustercontrol.monitor.message.LogOutputNotifyInfo
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#getFacilityPath(java.lang.String, java.lang.String)
	 * @see #getNotifyRelationInfos()
	 * @see #getPriority(int)
	 * @see #getMessageId(int)
	 * @see #getMessage(int)
	 * @see #getMessageOrg(int)
	 * @see #getJobRunInfo(int)
	 * @see #getMessageForScope(int)
	 * @see #getMessageIdForScope(int)
	 * @see #getMessageOrgForScope(int)
	 * @see #getJobRunInfoForScope(int)
	 */
	protected void notify(
			boolean isNode,
			String facilityId,
			int result,
			Date generationDate,
			MonitorRunResultInfo resultInfo) throws HinemosUnknown {

		// for debug
		if (m_log.isDebugEnabled()) {
			m_log.debug("notify() isNode = " + isNode + ", facilityId = " + facilityId
					+ ", result = " + result + ", generationDate = " + generationDate.toString()
					+ ", resultInfo = " + resultInfo.getMessage());
		}

		Integer monitorFlg = 0;

		monitorFlg = m_monitor.getMonitorFlg();

		// 監視無効の場合、通知しない
		if(monitorFlg == ValidConstant.TYPE_INVALID){
			m_log.debug("notify() isNode = " + isNode + ", facilityId = " + facilityId
					+ ", result = " + result + ", generationDate = " + generationDate.toString()
					+ ", resultInfo = " + resultInfo.getMessage() + ", monitorFlg is false");
			return;
		}

		// 通知IDが指定されていない場合、通知しない
		String notifyGroupId = resultInfo.getNotifyGroupId();
		if(notifyGroupId == null || "".equals(notifyGroupId)){
			return;
		}

		// 通知情報を設定
		OutputBasicInfo notifyInfo = new OutputBasicInfo();
		notifyInfo.setPluginId(m_monitorTypeId);
		notifyInfo.setMonitorId(m_monitorId);
		notifyInfo.setApplication(m_monitor.getApplication());

		String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
		notifyInfo.setFacilityId(facilityId);
		notifyInfo.setScopeText(facilityPath);

		int priority = -1;
		String messageId = "";
		String message = "";
		String messageOrg = "";

		if(isNode){
			// ノードの場合
			priority = resultInfo.getPriority();
			messageId = resultInfo.getMessageId();
			message = resultInfo.getMessage();
			messageOrg = resultInfo.getMessageOrg();
		}
		else{
			// スコープの場合
			priority = result;
			messageId = getMessageIdForScope(result);
			message = getMessageForScope(result);
			messageOrg = getMessageOrgForScope(result);
		}
		notifyInfo.setPriority(priority);
		notifyInfo.setMessageId(messageId);
		// 通知抑制用のサブキーを設定。
		if(resultInfo.getDisplayName() != null && !"".equals(resultInfo.getDisplayName())){
			// 監視結果にデバイス名を含むものは、デバイス名をサブキーとして設定。
			notifyInfo.setSubKey(resultInfo.getDisplayName());
		} else if(resultInfo.getPatternText() != null){
			// 監視結果にパターンマッチ文字列を含むものは、デバイス名をサブキーとして設定。
			notifyInfo.setSubKey(resultInfo.getPatternText());
		}
		notifyInfo.setMessage(message);
		notifyInfo.setMessageOrg(messageOrg);
		if (generationDate != null) {
			notifyInfo.setGenerationDate(generationDate.getTime());
		}
		// for debug
		if (m_log.isDebugEnabled()) {
			m_log.debug("notify() priority = " + priority
					+ " , messageId = " + messageId
					+ " , message = " + message
					+ " , messageOrg = " + messageOrg
					+ ", generationDate = " + generationDate);
		}

		// ログ出力情報を送信
		if (m_log.isDebugEnabled()) {
			m_log.debug("sending message"
					+ " : priority=" + notifyInfo.getPriority()
					+ " generationDate=" + notifyInfo.getGenerationDate() + " pluginId=" + notifyInfo.getPluginId()
					+ " monitorId=" + notifyInfo.getMonitorId() + " facilityId=" + notifyInfo.getFacilityId()
					+ " subKey=" + notifyInfo.getSubKey()
					+ ")");
		}
		new NotifyControllerBean().notify(notifyInfo, notifyGroupId);
	}

	/**
	 * 重要度別ファシリティ名を設定します。
	 * <p>
	 * <ol>
	 * <li>引数で指定されたファシリティIDのファシリティ名を取得します。</li>
	 * <li>重要度別のファシリティマップの引数で指定された重要度に、ファシリティ名を追加します。</li>
	 *
	 * @param priority 重要度
	 * @param facilityId ファシリティ名
	 * @throws HinemosUnknown
	 */
	protected void setPriorityMap(Integer priority, String facilityId) throws HinemosUnknown {

		ArrayList<String> list = m_priorityMap.get(priority);
		if(list != null){

			// ファシリティ名を取得
			String facilityName = new RepositoryControllerBean().getFacilityPath(facilityId, null);

			list.add(facilityName);
			m_priorityMap.put(priority, list);
		}
	}

	/**
	 * 項目一覧の文字列を返します。
	 *
	 * <dl>
	 *  <dt>項目名:</dt>
	 * 	 <dd>リスト[0]</dd>
	 *   <dd>リスト[1]</dd>
	 *   <dd>リスト[2]</dd>
	 *   <dd>　 ：　</dd>
	 * </dl>
	 *
	 * @param item 項目名
	 * @param list リスト
	 * @return 項目一覧の文字列
	 */
	private String getItemListString(String item, ArrayList<String> list){

		int length = list.size();
		if(length > 0){
			StringBuffer result = new StringBuffer();
			result.append(item + ":" + "\n");
			for (int i = 0; i < length; i++) {
				result.append("\t" + list.get(i));
				if(i < length-1){
					result.append("\n");
				}
			}
			return result.toString();
		}
		else{
			return "";
		}
	}

	/**
	 * 監視実行の初期処理を行います。
	 *
	 * 監視を実行するrunメソッドの最初の部分でcallしてください。
	 * runメソッド終了部分で、terminatteメソッドをcallし、キューのコネクションをクローズする必要があります。
	 *
	 */
	private void initialize(String monitorTypeId) {
	}

	/**
	 * 監視実行の終了処理を行います。
	 *
	 */
	private void terminate() {
	}
}
