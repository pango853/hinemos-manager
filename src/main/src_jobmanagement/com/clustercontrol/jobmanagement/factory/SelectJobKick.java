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

package com.clustercontrol.jobmanagement.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.scheduler.QuartzUtil;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.jobmanagement.bean.JobFileCheck;
import com.clustercontrol.jobmanagement.bean.JobKick;
import com.clustercontrol.jobmanagement.bean.JobPlan;
import com.clustercontrol.jobmanagement.bean.JobPlanFilter;
import com.clustercontrol.jobmanagement.bean.JobSchedule;
import com.clustercontrol.jobmanagement.model.JobFileCheckEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobScheduleEntity;
import com.clustercontrol.jobmanagement.util.QueryUtil;

/**
 * 
 * 実行契機一覧情報[スケジュール＆ファイルチェック]を検索するクラスです。
 *
 * @version 4.1.0
 * @since 1.0.0
 */
public class SelectJobKick {
	private static Log m_log = LogFactory.getLog( SelectJobKick.class );

	/**
	 * scheduleIdのジョブスケジュールを取得します
	 * 
	 * @param scheduleId
	 * @return
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	public JobSchedule getJobSchedule(String scheduleId) throws JobMasterNotFound, InvalidRole {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		JobSchedule jobSchedule = null;

		m_log.debug("getJobSchedule() scheduleId = " + scheduleId);

		JobScheduleEntity scheduleBean = em.find(JobScheduleEntity.class, scheduleId, ObjectPrivilegeMode.READ);
		if (scheduleBean == null) {
			JobMasterNotFound je = new JobMasterNotFound("JobScheduleEntity.findByPrimaryKey"
					+ ", scheduleId = " + scheduleId);
			m_log.info("getJobSchedule() : "
					+ je.getClass().getSimpleName() + ", " + je.getMessage());
			throw je;
		}
		jobSchedule = createJobScheduleInfo(scheduleBean);
		return jobSchedule;
	}

	/**
	 * scheduleIdのジョブファイルチェックを取得します
	 * 
	 * @param scheduleId
	 * @return
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * 
	 */
	public JobFileCheck getJobFileCheck(String scheduleId) throws JobMasterNotFound, InvalidRole {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		JobFileCheck jobFileCheck = null;

		m_log.debug("getJobFileCheck() scheduleId = " + scheduleId);

		JobFileCheckEntity fileCheckBean = em.find(JobFileCheckEntity.class, scheduleId, ObjectPrivilegeMode.READ);
		if (fileCheckBean == null) {
			JobMasterNotFound je = new JobMasterNotFound("JobFileCheckEntity.findByPrimaryKey"
					+ ", scheduleId = " + scheduleId);
			m_log.info("getJobFileCheck() : "
					+ je.getClass().getSimpleName() + ", " + je.getMessage());
			throw je;
		}
		jobFileCheck = createJobFileCheckInfo(fileCheckBean);
		return jobFileCheck;
	}

	/**
	 * scheduleIdと一致するジョブ[実行契機]（スケジュールまたは、ファイルチェック）を取得します
	 * @param scheduleId
	 * @return
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	public JobKick getJobKick(String scheduleId) throws JobMasterNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		m_log.debug("getJobKick()");

		JobFileCheckEntity fileCheckBean = em.find(JobFileCheckEntity.class, scheduleId, ObjectPrivilegeMode.READ);
		JobScheduleEntity scheduleBean = em.find(JobScheduleEntity.class, scheduleId, ObjectPrivilegeMode.READ);

		JobKick jobKick = new JobKick();
		if(scheduleBean != null){
			m_log.debug("createJobScheduleInfo : scheduleId = " + scheduleId);
			jobKick = createJobScheduleInfo(scheduleBean);
		}
		else if(fileCheckBean != null){
			m_log.debug("createJobFileCheckInfo : scheduleId = " + scheduleId);
			jobKick = createJobFileCheckInfo(fileCheckBean);
		}
		else {
			JobMasterNotFound je = new JobMasterNotFound();
			if (scheduleBean == null) {
				je = new JobMasterNotFound("JobScheduleEntity.findByPrimaryKey"
						+ ", scheduleId = " + scheduleId);
				m_log.info("getJobSchedule() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
			}
			if(fileCheckBean == null){
				je = new JobMasterNotFound("JobFileCheckEntity.findByPrimaryKey"
						+ ", scheduleId = " + scheduleId);
				m_log.info("getJobFileCheck() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
			}
			throw je;
		}
		return jobKick;
	}

	/**
	 * スケジュール一覧情報を取得します。
	 * 
	 * @return スケジュール一覧情報
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	public ArrayList<JobKick> getJobKickList(String userId) throws JobMasterNotFound, InvalidRole {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		m_log.debug("getJobKickList()");

		ArrayList<JobKick> list = new ArrayList<JobKick>();
		//実行契機[スケジュール]情報を取得する
		Collection<JobScheduleEntity> jobScheduleList;
		jobScheduleList = em.createNamedQuery("JobScheduleEntity.findAll", JobScheduleEntity.class).getResultList();
		if (jobScheduleList == null) {
			JobMasterNotFound je = new JobMasterNotFound("JobScheduleEntity.findAll");
			m_log.info("getJobKickList()  JobSchedule : "
					+ je.getClass().getSimpleName() + ", " + je.getMessage());
			throw je;
		}
		for(JobScheduleEntity scheduleBean : jobScheduleList){
			//スケジュールを取得する
			list.add(createJobScheduleInfo(scheduleBean));
		}
		em = new JpaTransactionManager().getEntityManager();
		//実行契機[ファイルチェック]情報を取得する
		Collection<JobFileCheckEntity> jobFileCheckList;
		jobFileCheckList = em.createNamedQuery("JobFileCheckEntity.findAll", JobFileCheckEntity.class).getResultList();
		if (jobFileCheckList == null) {
			JobMasterNotFound je = new JobMasterNotFound("JobFileCheckEntity.findAll");
			m_log.info("getJobKickList()  JobFileCheck : "
					+ je.getClass().getSimpleName() + ", " + je.getMessage());
			throw je;
		}
		for(JobFileCheckEntity fileCheckBean : jobFileCheckList){
			//ファイルチェックを取得する
			list.add(createJobFileCheckInfo(fileCheckBean));
		}
		return list;
	}
	/**
	 * スケジュール[スケジュール予定]一覧情報を取得します。
	 * ・カレンダが設定されていた場合、カレンダの日程を考慮したスケジュールを取得します。
	 * ・フィルタ処理が有効の場合、フィルタの内容を考慮したスケジュールを取得します。
	 * 
	 * @return スケジュール一覧情報
	 * @throws JobMasterNotFound
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public ArrayList<JobPlan> getPlanList(String userId, JobPlanFilter filter,int plans) throws JobMasterNotFound, InvalidSetting, InvalidRole {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		m_log.debug("getPlanList()");

		Collection<JobScheduleEntity> jobScheduleList =
				em.createNamedQuery("JobScheduleEntity.findAll", JobScheduleEntity.class).getResultList();
		if (jobScheduleList == null) {
			JobMasterNotFound je = new JobMasterNotFound("JobScheduleEntity.findAll");
			m_log.info("getPlanList() : "
					+ je.getClass().getSimpleName() + ", " + je.getMessage());
			throw je;
		}

		//ジョブ[スケジュール予定]一覧表示に必要なものだけ抽出
		ArrayList<JobPlan> planList = new ArrayList<JobPlan>();
		for(JobScheduleEntity scheduleBean : jobScheduleList){
			JobSchedule js = createJobScheduleInfo(scheduleBean);
			//スケジュールが有効なら表示 0:無効 1:有効
			if (js.getValid() != ValidConstant.TYPE_VALID) {
				continue;
			}
			String str = QuartzUtil.getCronString(js.getScheduleType(),
					js.getWeek(),js.getHour(),js.getMinute(),
					js.getFromXminutes(),js.getEveryXminutes());
			m_log.debug("Cron =" + str);
			//表示開始日時のデフォルトはマネージャへアクセスしたときの日時
			Long startTime = System.currentTimeMillis();
			//フィルタの開始時間が設定されていたらこれを基準に表示
			if(filter != null && filter.getFromDate() != null){
				startTime = filter.getFromDate();
			}
			JobPlanSchedule planInfo = new JobPlanSchedule(str, startTime, js.getCalendarId());
			//表示件数分繰り返す
			int counter = 0;
			while (counter < plans) {
				Long date = planInfo.getNextPlan();
				if (date == null) {
					break;
				}
				//フィルタ処理
				boolean filterFlg = true;
				if(filter != null){
					filterFlg = filter.filterAction(js.getId(), date);
				}
				//フィルタ処理を通過、または、フィルタ未設定の場合
				if(filterFlg){
					JobPlan plan = new JobPlan();
					plan.setDate(date);
					plan.setJobKickId(js.getId());
					plan.setJobKickName(js.getName());
					plan.setJobunitId(js.getJobunitId());
					plan.setJobId(js.getJobId());
					plan.setJobName(js.getJobName());
					planList.add(plan);
				}
				counter ++;
			}
		}
		m_log.debug("planList.size()=" + planList.size());
		//昇順ソート
		Collections.sort(planList);
		//palns数リストにまとめる
		ArrayList<JobPlan> retList = new ArrayList<JobPlan>();
		int counter = 0;
		//表示数分のみ取得
		while(counter < plans){
			//表示数未満のとき
			if(planList.size() <= counter){
				break;
			}
			retList.add(planList.get(counter));
			counter++;
		}
		return retList;
	}

	/**
	 * JobScheduleEntityよりJobScheduleを作成するクラス
	 * 
	 * @param scheduleBean
	 * @return
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	private JobSchedule createJobScheduleInfo(JobScheduleEntity scheduleBean) throws JobMasterNotFound, InvalidRole {

		JobSchedule info = new JobSchedule();
		//スケジュールIDを取得
		info.setId(scheduleBean.getScheduleId());
		//スケジュール名を取得
		info.setName(scheduleBean.getScheduleName());
		//ジョブIDを取得
		info.setJobId(scheduleBean.getJobId());
		//ジョブ名を取得
		JobMstEntity jobMstEntity = QueryUtil.getJobMstPK_OR(
				scheduleBean.getJobunitId(),
				scheduleBean.getJobId(),
				scheduleBean.getOwnerRoleId());
		String jobName = jobMstEntity.getJobName();
		info.setJobName(jobName);

		//ジョブユニットIDを取得
		info.setJobunitId(scheduleBean.getJobunitId());
		//カレンダIDを取得
		info.setCalendarId(scheduleBean.getCalendarId());

		//スケジュール設定を取得
		info.setScheduleType(scheduleBean.getScheduleType());
		info.setHour(scheduleBean.getHour());
		info.setMinute(scheduleBean.getMinute());
		info.setWeek(scheduleBean.getWeek());
		info.setFromXminutes(scheduleBean.getFromXMinutes());
		info.setEveryXminutes(scheduleBean.getEveryXMinutes());

		//有効/無効を取得
		info.setValid(scheduleBean.getValidFlg());

		//オーナーロールIDを取得
		info.setOwnerRoleId(scheduleBean.getOwnerRoleId());

		//登録者を取得
		info.setCreateUser(scheduleBean.getRegUser());
		//登録日時を取得
		if (scheduleBean.getRegDate() != null) {
			info.setCreateTime(scheduleBean.getRegDate().getTime());
		}
		//更新者を取得
		info.setUpdateUser(scheduleBean.getUpdateUser());
		//更新日時を取得
		if (scheduleBean.getUpdateDate() != null) {
			info.setUpdateTime(scheduleBean.getUpdateDate().getTime());
		}
		return info;
	}

	/**
	 * JobFileCheckEntityよりJobFileCheckを作成するクラス
	 * 
	 * @param fileCheckBean
	 * @return
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	private JobFileCheck createJobFileCheckInfo(JobFileCheckEntity fileCheckBean) throws JobMasterNotFound, InvalidRole {

		JobFileCheck info = new JobFileCheck();
		//スケジュールIDを取得
		info.setId(fileCheckBean.getScheduleId());
		//スケジュール名を取得
		info.setName(fileCheckBean.getScheduleName());
		//ジョブIDを取得
		info.setJobId(fileCheckBean.getJobId());
		//ジョブ名を取得
		JobMstEntity jobMstEntity = QueryUtil.getJobMstPK_OR(
				fileCheckBean.getJobunitId(),
				fileCheckBean.getJobId(),
				fileCheckBean.getOwnerRoleId());
		String jobName = jobMstEntity.getJobName();
		info.setJobName(jobName);

		//ジョブユニットIDを取得
		info.setJobunitId(fileCheckBean.getJobunitId());
		//カレンダIDを取得
		info.setCalendarId(fileCheckBean.getCalendarId());
		//ファシリティID取得
		info.setFacilityId(fileCheckBean.getFacilityId());
		//ディレクトリ取得
		info.setDirectory(fileCheckBean.getDirectory());
		//ファイル名取得
		info.setFileName(fileCheckBean.getFileName());
		//ファイルチェック種別取得
		info.setEventType(fileCheckBean.getEventType());
		//ファイルチェック種別が変更の場合 変更種別取得
		info.setModifyType(fileCheckBean.getModifyType());

		//有効/無効を取得
		info.setValid(fileCheckBean.getValidFlg());

		//オーナーロールIDを取得
		info.setOwnerRoleId(fileCheckBean.getOwnerRoleId());

		//登録者を取得
		info.setCreateUser(fileCheckBean.getRegUser());
		//登録日時を取得
		if (fileCheckBean.getRegDate() != null) {
			info.setCreateTime(fileCheckBean.getRegDate().getTime());
		}
		//更新者を取得
		info.setUpdateUser(fileCheckBean.getUpdateUser());
		//更新日時を取得
		if (fileCheckBean.getUpdateDate() != null) {
			info.setUpdateTime(fileCheckBean.getUpdateDate().getTime());
		}
		return info;
	}
}
