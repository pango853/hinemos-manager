/*

 Copyright (C) 2013 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.run.bean;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.RunIntervalConstant;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.calendar.bean.CalendarInfo;
import com.clustercontrol.custom.bean.CustomCheckInfo;
import com.clustercontrol.http.bean.HttpCheckInfo;
import com.clustercontrol.http.bean.HttpScenarioCheckInfo;
import com.clustercontrol.jmx.bean.JmxCheckInfo;
import com.clustercontrol.logfile.bean.LogfileCheckInfo;
import com.clustercontrol.monitor.plugin.bean.PluginCheckInfo;
import com.clustercontrol.notify.bean.NotifyRelationInfo;
import com.clustercontrol.performance.monitor.bean.PerfCheckInfo;
import com.clustercontrol.ping.bean.PingCheckInfo;
import com.clustercontrol.port.bean.PortCheckInfo;
import com.clustercontrol.process.bean.ProcessCheckInfo;
import com.clustercontrol.snmp.bean.SnmpCheckInfo;
import com.clustercontrol.snmptrap.bean.TrapCheckInfo;
import com.clustercontrol.sql.bean.SqlCheckInfo;
import com.clustercontrol.winevent.bean.WinEventCheckInfo;
import com.clustercontrol.winservice.bean.WinServiceCheckInfo;

/**
 * 監視情報を保持するクラス<BR>
 * jaxbで利用するため、引数なしのコンストラクタが必要。
 * そのため、abstractにしない。
 * 
 * @version 4.1.0
 * @since 2.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class MonitorInfo
implements java.io.Serializable
{
	private static final long serialVersionUID = 8016002145318844928L;

	/** 監視項目ID(主キー)。 */
	private String m_monitorId;

	/** 監視対象ID(PNGなど)。 */
	private String m_monitorTypeId;

	/**
	 * 監視判定タイプ(真偽値/数値/文字列)。
	 * 
	 * @see com.clustercontrol.bean.MonitorBlockConstant
	 */
	private int m_monitorType;

	/** 説明。 */
	private String m_description;

	/** ファシリティID。 */
	private String m_facilityId;

	/** スコープ(表示用)。 */
	private String m_scope;

	/**
	 * 実行間隔（秒）。
	 *
	 * @see com.clustercontrol.bean.RunIntervalConstant
	 */
	private int m_runInterval = RunIntervalConstant.TYPE_MIN_01;

	/** カレンダID。 */
	private String m_calendarId;

	/** カレンダ(エージェントにMonitorInfoを送る時以外はnull) */
	private CalendarInfo m_calendarInfo;

	/**
	 * 値失敗時の重要度。
	 * 
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	private int m_failurePriority = PriorityConstant.TYPE_UNKNOWN;

	/** アプリケーション。 */
	private String m_application;

	/**　通知IDのコレクション*/
	private Collection<NotifyRelationInfo> m_notifyId;

	/** 作成日時。 */
	private long m_regDate;

	/** 最終変更日時。 */
	private long m_updateDate;

	/** 新規作成ユーザ。 */
	private String m_regUser;

	/** 最終変更ユーザ。 */
	private String m_updateUser;

	/**
	 * 監視有効フラグ。
	 * 
	 * @see com.clustercontrol.bean.ValidConstant
	 */
	private int m_monitor_flg = ValidConstant.TYPE_VALID;

	/**
	 * 収集有効フラグ。
	 * 
	 * @see com.clustercontrol.bean.ValidConstant
	 */
	private int m_collector_flg = ValidConstant.TYPE_INVALID;

	/** 収集値表示名。 */
	private String m_itemName;

	/** 収集値単位。 */
	private String m_measure;

	/** オーナーロールID */
	private String m_ownerRoleId;

	/**
	 * 判定情報。
	 * {@link com.clustercontrol.monitor.run.bean.MonitorJudgementInfo}のリスト。
	 */
	private ArrayList<MonitorNumericValueInfo> m_numericValueInfo;
	private ArrayList<MonitorStringValueInfo> m_stringValueInfo;
	private ArrayList<MonitorTruthValueInfo> m_truthValueInfo;

	/** チェック条件情報。 */
	private HttpCheckInfo httpCheckInfo;
	private HttpScenarioCheckInfo httpScenarioCheckInfo;
	private PerfCheckInfo perfCheckInfo;
	private PingCheckInfo pingCheckInfo;
	private PortCheckInfo portCheckInfo;
	private ProcessCheckInfo processCheckInfo;
	private SnmpCheckInfo snmpCheckInfo;
	private SqlCheckInfo sqlCheckInfo;
	private TrapCheckInfo trapCheckInfo;
	private CustomCheckInfo customCheckInfo;
	private LogfileCheckInfo logfileCheckInfo;
	private WinServiceCheckInfo winServiceCheckInfo;
	private WinEventCheckInfo winEventCheckInfo;
	private PluginCheckInfo pluginCheckInfo;
	private JmxCheckInfo jmxCheckInfo;

	/**
	 * コンストラクタ。
	 */
	public MonitorInfo() {
		super();
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param application アプリケーション
	 * @param calendarId カレンダID
	 * @param description 説明
	 * @param scope スコープ
	 * @param facilityId ファシリティID
	 * @param failurePriority 値取得の失敗時の重要度
	 * @param monitorId 監視項目ID
	 * @param monitorTypeId 監視対象ID
	 * @param monitorType 監視種別
	 * @param regDate 作成日時
	 * @param regUser 新規作成ユーザ
	 * @param runInterval 実行間隔（秒）
	 * @param notifyId 通知IDのコレクション
	 * @param updateDate 最終変更日時
	 * @param updateUser 最終変更ユーザ
	 * @param monitor_flg 監視有効/無効
	 * @param collector_flg 収集有効/無効
	 * @param itemName 収集値表示名
	 * @param measure 収集値単位
	 * @param ownerRoleId オーナーロールID
	 */
	public MonitorInfo(
			String application,
			String calendarId,
			String description,
			String scope,
			String facilityId,
			int failurePriority,
			String monitorId,
			String monitorTypeId,
			int monitorType,
			long regDate,
			String regUser,
			int runInterval,
			Collection<NotifyRelationInfo> notifyId,
			long updateDate,
			String updateUser,
			int monitor_flg,
			int collector_flg,
			String itemName,
			String measure,
			String ownerRoleId,

			ArrayList<MonitorNumericValueInfo> numericValueInfo,
			ArrayList<MonitorStringValueInfo> stringValueInfo,
			ArrayList<MonitorTruthValueInfo> truthValueInfo,

			HttpCheckInfo httpCheckInfo,
			HttpScenarioCheckInfo httpScenarioCheckInfo,
			PerfCheckInfo perfCheckInfo,
			PingCheckInfo pingCheckInfo,
			PluginCheckInfo pluginCheckInfo,
			PortCheckInfo portCheckInfo,
			ProcessCheckInfo processCheckInfo,
			SnmpCheckInfo snmpCheckInfo,
			SqlCheckInfo sqlCheckInfo,
			TrapCheckInfo trapCheckInfo,
			CustomCheckInfo commandCheckInfo,
			LogfileCheckInfo logfileCheckInfo,
			WinServiceCheckInfo winServiceCheckInfo,
			WinEventCheckInfo winEventCheckInfo,
			JmxCheckInfo jmxCheckInfo){

		// 共通監視設定
		setApplication(application);
		setCalendarId(calendarId);
		setDescription(description);
		setScope(scope);
		setFacilityId(facilityId);
		setFailurePriority(failurePriority);
		setMonitorId(monitorId);
		setMonitorTypeId(monitorTypeId);
		setMonitorType(monitorType);
		setRegDate(regDate);
		setRegUser(regUser);
		setRunInterval(runInterval);
		setNotifyId(notifyId);
		setUpdateDate(updateDate);
		setUpdateUser(updateUser);
		setMonitorFlg(monitor_flg);
		setCollectorFlg(collector_flg);
		setItemName(itemName);
		setMeasure(measure);
		setOwnerRoleId(ownerRoleId);

		// 判定情報
		setNumericValueInfo(numericValueInfo);
		setStringValueInfo(stringValueInfo);
		setTruthValueInfo(truthValueInfo);

		// チェック条件情報
		setHttpCheckInfo(httpCheckInfo);
		setHttpScenarioCheckInfo(httpScenarioCheckInfo);
		setPerfCheckInfo(perfCheckInfo);
		setPingCheckInfo(pingCheckInfo);
		setPluginCheckInfo(pluginCheckInfo);
		setPortCheckInfo(portCheckInfo);
		setProcessCheckInfo(processCheckInfo);
		setSnmpCheckInfo(snmpCheckInfo);
		setSqlCheckInfo(sqlCheckInfo);
		setTrapCheckInfo(trapCheckInfo);
		setCustomCheckInfo(commandCheckInfo);
		setLogfileCheckInfo(logfileCheckInfo);
		setWinServiceCheckInfo(winServiceCheckInfo);
		setWinEventCheckInfo(winEventCheckInfo);
		setJmxCheckInfo(jmxCheckInfo);
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param otherData コピー元の監視情報
	 */
	public MonitorInfo( MonitorInfo otherData ) {

		// 共通監視設定
		setApplication(otherData.getApplication());
		setCalendarId(otherData.getCalendarId());
		setDescription(otherData.getDescription());
		setScope(otherData.getScope());
		setFacilityId(otherData.getFacilityId());
		setFailurePriority(otherData.getFailurePriority());
		setMonitorId(otherData.getMonitorId());
		setMonitorTypeId(otherData.getMonitorTypeId());
		setMonitorType(otherData.getMonitorType());
		setRegDate(otherData.getRegDate());
		setRegUser(otherData.getRegUser());
		setRunInterval(otherData.getRunInterval());
		setNotifyId(otherData.getNotifyId());
		setUpdateDate(otherData.getUpdateDate());
		setUpdateUser(otherData.getUpdateUser());
		setMonitorFlg(otherData.getMonitorFlg());
		setCollectorFlg(otherData.getCollectorFlg());

		// 判定情報
		setNumericValueInfo(otherData.getNumericValueInfo());
		setStringValueInfo(otherData.getStringValueInfo());
		setTruthValueInfo(otherData.getTruthValueInfo());

		// チェック条件情報
		setHttpCheckInfo(otherData.getHttpCheckInfo());
		setPerfCheckInfo(otherData.getPerfCheckInfo());
		setPingCheckInfo(otherData.getPingCheckInfo());
		setPluginCheckInfo(otherData.getPluginCheckInfo());
		setPortCheckInfo(otherData.getPortCheckInfo());
		setProcessCheckInfo(otherData.getProcessCheckInfo());
		setSnmpCheckInfo(otherData.getSnmpCheckInfo());
		setSqlCheckInfo(otherData.getSqlCheckInfo());
		setTrapCheckInfo(otherData.getTrapCheckInfo());
		setCustomCheckInfo(otherData.getCustomCheckInfo());
		setLogfileCheckInfo(otherData.getLogfileCheckInfo());
		setWinServiceCheckInfo(otherData.getWinServiceCheckInfo());
	}

	/**
	 * アプリケーションを返します。
	 * 
	 * @return アプリケーション
	 */
	public String getApplication(){
		return this.m_application;
	}

	/**
	 * アプリケーションを設定します。
	 * 
	 * @param application アプリケーション
	 */
	public void setApplication( String application ){
		this.m_application = application;
	}

	/**
	 * カレンダIDを返します。
	 * 
	 * @return カレンダID
	 */
	public String getCalendarId(){
		return this.m_calendarId;
	}

	/**
	 * カレンダIDを設定します。
	 * 
	 * @param calendarId カレンダID
	 */
	public void setCalendarId( String calendarId ){
		this.m_calendarId = calendarId;
	}

	/**
	 * カレンダのDTOを返します。
	 * エージェントにMonitorInfoを送る時以外はnullとなります。
	 * @return
	 */
	public CalendarInfo getCalendar() {
		return m_calendarInfo;
	}

	/**
	 * カレンダのDTOを設定します。
	 * エージェントにMonitorInfoを送る時以外はnullとなります。
	 * @param calendarDTO
	 */
	public void setCalendar(CalendarInfo calendarDTO) {
		this.m_calendarInfo = calendarDTO;
	}

	/**
	 * 説明を返します。
	 * 
	 * @return 説明
	 */
	public String getDescription(){
		return this.m_description;
	}

	/**
	 * 説明を設定します。
	 * 
	 * @param description 説明
	 */
	public void setDescription( String description ){
		this.m_description = description;
	}

	/**
	 * ファシリティIDを返します。
	 * 
	 * @return ファシリティID
	 */
	public String getFacilityId(){
		return this.m_facilityId;
	}

	/**
	 * ファシリティIDを設定します。
	 * 
	 * @param facilityId ファシリティID
	 */
	public void setFacilityId( String facilityId ){
		this.m_facilityId = facilityId;
	}

	/**
	 * 値取得の失敗時の重要度を返します。
	 * 
	 * @return 値取得の失敗時の重要度
	 * 
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public int getFailurePriority(){
		return this.m_failurePriority;
	}

	/**
	 * 値取得の失敗時の重要度を設定します。
	 * 
	 * @param failurePriority 値取得の失敗時の重要度
	 * 
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public void setFailurePriority( int failurePriority ){
		this.m_failurePriority = failurePriority;
	}

	/**
	 * 監視項目IDを返します。
	 * 
	 * @return 監視項目ID
	 */
	public String getMonitorId(){
		return this.m_monitorId;
	}

	/**
	 * 監視項目IDを設定します。
	 * 
	 * @param monitorId 監視項目ID
	 */
	public void setMonitorId( String monitorId ){
		this.m_monitorId = monitorId;
	}

	/**
	 * 監視対象IDを返します。
	 * 
	 * @return 監視対象ID
	 */
	public String getMonitorTypeId(){
		return this.m_monitorTypeId;
	}

	/**
	 * 監視対象IDを設定します。
	 * 
	 * @param monitorTypeId 監視対象ID
	 */
	public void setMonitorTypeId( String monitorTypeId ){
		this.m_monitorTypeId = monitorTypeId;
	}

	/**
	 * 通知IDを返します。
	 * 
	 * @return 通知ID
	 */
	public Collection<NotifyRelationInfo> getNotifyId(){
		return this.m_notifyId;
	}

	/**
	 * 通知IDを設定します。
	 * 
	 * @param notifyId 通知ID
	 */
	public void setNotifyId( Collection<NotifyRelationInfo> notifyId ){
		this.m_notifyId = notifyId;
	}

	/**
	 * 作成日時を返します。
	 * 
	 * @return regDate 作成日時
	 */
	public long getRegDate(){
		return this.m_regDate;
	}

	/**
	 * 作成日時を設定します。
	 * 
	 * @param regDate 作成日時
	 */
	public void setRegDate( long regDate ){
		this.m_regDate = regDate;
	}

	/**
	 * 実行間隔を返します。
	 * 
	 * @return 実行間隔（秒）
	 * 
	 * @see com.clustercontrol.bean.RunIntervalConstant
	 */
	public int getRunInterval(){
		return this.m_runInterval;
	}

	/**
	 * 実行間隔を設定します。
	 * 
	 * @param runInterval 実行間隔（秒）
	 * 
	 * @see com.clustercontrol.bean.RunIntervalConstant
	 */
	public void setRunInterval( int runInterval ){
		this.m_runInterval = runInterval;
	}

	/**
	 * スコープを返します。
	 * 
	 * @return スコープ
	 */
	public String getScope(){
		return m_scope;
	}

	/**
	 * スコープを設定します。
	 * 
	 * @param scope スコープ
	 */
	public void setScope(String scope) {
		this.m_scope = scope;
	}

	/**
	 * 最終変更日時を返します。
	 * 
	 * @return 最終変更日時
	 */
	public long getUpdateDate(){
		return this.m_updateDate;
	}

	/**
	 * 最終変更日時を設定します。
	 * 
	 * @param updateDate 最終変更日時
	 */
	public void setUpdateDate( long updateDate ){
		this.m_updateDate = updateDate;
	}

	/**
	 * 新規作成ユーザを返します。
	 * 
	 * @return 新規作成ユーザ
	 */
	public String getRegUser() {
		return m_regUser;
	}

	/**
	 * 新規作成ユーザを設定します。
	 * 
	 * @param user 新規作成ユーザ
	 */
	public void setRegUser(String user) {
		m_regUser = user;
	}

	/**
	 * 最終変更ユーザを返します。
	 * 
	 * @return 最終変更ユーザ
	 */
	public String getUpdateUser() {
		return m_updateUser;
	}

	/**
	 * 最終変更ユーザを設定します。
	 * 
	 * @param user 最終変更ユーザ
	 */
	public void setUpdateUser(String user) {
		m_updateUser = user;
	}

	/**
	 * 監視有効フラグを返します。
	 * 
	 * @return 監視有効フラグ
	 */
	public int getMonitorFlg() {
		return m_monitor_flg;
	}

	/**
	 * 監視有効フラグを設定します。
	 * 
	 * @param user 監視有効フラグ
	 */
	public void setMonitorFlg(int flg) {
		m_monitor_flg = flg;
	}

	/**
	 * 収集有効フラグを返します。
	 * 
	 * @return 収集有効フラグ
	 */
	public int getCollectorFlg() {
		return m_collector_flg;
	}

	/**
	 * 収集有効フラグを設定します。
	 * 
	 * @param user 収集有効フラグ
	 */
	public void setCollectorFlg(int flg) {
		m_collector_flg = flg;
	}

	/**
	 * 収集値表示名を返します。
	 * 
	 * @return 収集値表示名
	 */
	public String getItemName() {
		return m_itemName;
	}

	/**
	 * 収集値表示名を設定します。
	 * 
	 * @param itemName 収集値表示名
	 */
	public void setItemName(String itemName) {
		m_itemName = itemName;
	}

	/**
	 * 収集値単位を返します。
	 * 
	 * @return 収集値単位
	 */
	public String getMeasure() {
		return m_measure;
	}

	/**
	 * 収集値単位を設定します。
	 * 
	 * @param measure 収集値単位
	 */
	public void setMeasure(String measure) {
		m_measure = measure;
	}

	/**
	 * オーナーロールIDを返します。
	 * 
	 * @return オーナーロールID
	 */
	public String getOwnerRoleId() {
		return m_ownerRoleId;
	}

	/**
	 * オーナーロールIDを設定します。
	 * 
	 * @param ownerRoleId オーナーロールID
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		this.m_ownerRoleId = ownerRoleId;
	}

	/**
	 * 監視種別を返します。
	 * 
	 * @return 監視種別
	 * 
	 * @see com.clustercontrol.monitor.run.bean.MonitorTypeConstant
	 */
	public int getMonitorType() {
		return m_monitorType;
	}

	/**
	 * 監視種別を設定します。
	 * 
	 * @param type 監視種別
	 * 
	 * @see com.clustercontrol.monitor.run.bean.MonitorTypeConstant
	 */
	public void setMonitorType(int type) {
		m_monitorType = type;
	}

	/**
	 * 判定情報を返します。
	 */
	public ArrayList<MonitorNumericValueInfo> getNumericValueInfo() {
		return m_numericValueInfo;
	}

	/**
	 * 判定情報を返します。
	 */
	public void setNumericValueInfo(ArrayList<MonitorNumericValueInfo> valueInfo) {
		m_numericValueInfo = valueInfo;
	}

	/**
	 * 判定情報を返します。
	 */
	public ArrayList<MonitorStringValueInfo> getStringValueInfo() {
		return m_stringValueInfo;
	}

	/**
	 * 判定情報を設定します。
	 */
	public void setStringValueInfo(ArrayList<MonitorStringValueInfo> valueInfo) {
		m_stringValueInfo = valueInfo;
	}

	/**
	 * 判定情報を設定します。
	 */
	public ArrayList<MonitorTruthValueInfo> getTruthValueInfo() {
		return m_truthValueInfo;
	}

	/**
	 * 判定情報を設定します。
	 */
	public void setTruthValueInfo(ArrayList<MonitorTruthValueInfo> valueInfo) {
		m_truthValueInfo = valueInfo;
	}

	public HttpCheckInfo getHttpCheckInfo() {
		return httpCheckInfo;
	}

	public void setHttpCheckInfo(HttpCheckInfo httpCheckInfo) {
		this.httpCheckInfo = httpCheckInfo;
	}

	public HttpScenarioCheckInfo getHttpScenarioCheckInfo() {
		return httpScenarioCheckInfo;
	}

	public void setHttpScenarioCheckInfo(HttpScenarioCheckInfo httpScenarioCheckInfo) {
		this.httpScenarioCheckInfo = httpScenarioCheckInfo;
	}

	public JmxCheckInfo getJmxCheckInfo() {
		return jmxCheckInfo;
	}

	public void setJmxCheckInfo(JmxCheckInfo jmxCheckInfo) {
		this.jmxCheckInfo = jmxCheckInfo;
	}

	public PerfCheckInfo getPerfCheckInfo() {
		return perfCheckInfo;
	}

	public void setPerfCheckInfo(PerfCheckInfo perfCheckInfo) {
		this.perfCheckInfo = perfCheckInfo;
	}

	public PingCheckInfo getPingCheckInfo() {
		return pingCheckInfo;
	}

	public void setPingCheckInfo(PingCheckInfo pingCheckInfo) {
		this.pingCheckInfo = pingCheckInfo;
	}

	public PortCheckInfo getPortCheckInfo() {
		return portCheckInfo;
	}

	public void setPortCheckInfo(PortCheckInfo portCheckInfo) {
		this.portCheckInfo = portCheckInfo;
	}

	public ProcessCheckInfo getProcessCheckInfo() {
		return processCheckInfo;
	}

	public void setProcessCheckInfo(ProcessCheckInfo processCheckInfo) {
		this.processCheckInfo = processCheckInfo;
	}

	public SnmpCheckInfo getSnmpCheckInfo() {
		return snmpCheckInfo;
	}

	public void setSnmpCheckInfo(SnmpCheckInfo snmpCheckInfo) {
		this.snmpCheckInfo = snmpCheckInfo;
	}

	public SqlCheckInfo getSqlCheckInfo() {
		return sqlCheckInfo;
	}

	public void setSqlCheckInfo(SqlCheckInfo sqlCheckInfo) {
		this.sqlCheckInfo = sqlCheckInfo;
	}

	public TrapCheckInfo getTrapCheckInfo() {
		return trapCheckInfo;
	}

	public void setTrapCheckInfo(TrapCheckInfo trapCheckInfo) {
		this.trapCheckInfo = trapCheckInfo;
	}

	public CustomCheckInfo getCustomCheckInfo() {
		return customCheckInfo;
	}

	public void setCustomCheckInfo(CustomCheckInfo customCheckInfo) {
		this.customCheckInfo = customCheckInfo;
	}

	public LogfileCheckInfo getLogfileCheckInfo() {
		return logfileCheckInfo;
	}

	public void setLogfileCheckInfo(LogfileCheckInfo logfileCheckInfo) {
		this.logfileCheckInfo = logfileCheckInfo;
	}

	public WinServiceCheckInfo getWinServiceCheckInfo() {
		return winServiceCheckInfo;
	}

	public void setWinServiceCheckInfo(WinServiceCheckInfo winServiceCheckInfo) {
		this.winServiceCheckInfo = winServiceCheckInfo;
	}

	public WinEventCheckInfo getWinEventCheckInfo() {
		return winEventCheckInfo;
	}

	public void setWinEventCheckInfo(WinEventCheckInfo winEventCheckInfo) {
		this.winEventCheckInfo = winEventCheckInfo;
	}

	public PluginCheckInfo getPluginCheckInfo() {
		return pluginCheckInfo;
	}

	public void setPluginCheckInfo(PluginCheckInfo pluginCheckInfo) {
		this.pluginCheckInfo = pluginCheckInfo;
	}
}
