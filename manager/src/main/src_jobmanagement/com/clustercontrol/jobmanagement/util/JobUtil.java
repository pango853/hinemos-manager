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

package com.clustercontrol.jobmanagement.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.notify.bean.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;

/**
 * ジョブユーティリティクラス
 * 
 * 以下ィを提供します。<BR>
 * <li>ジョブツリーアイテムに関するユーティリティ
 * <li>ログインユーザが参照可能なジョブユニットかどうかをチェックするユーティリティ
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
public class JobUtil {
	/** ログ出力のインスタンス<BR> */
	private static Log m_log = LogFactory.getLog( JobUtil.class );

	/**
	 * ジョブの存在チェック
	 * @param item
	 * @param jobId
	 * @return
	 */
	public static boolean isExistJob(JobTreeItem item, String jobId) {
		if(item == null || item.getData() == null){
			return false;
		}
		//ジョブIDをチェック
		JobInfo info = item.getData();
		int type = info.getType();
		if (jobId.equals(info.getId())) {
			if (type == JobConstant.TYPE_JOB || type == JobConstant.TYPE_FILEJOB) {
				return true;
			}
		}
		//子JobTreeItemを取得
		for(JobTreeItem child : item.getChildren()){
			if (isExistJob(child, jobId)){
				return true;
			}
		}
		return false;
	}

	/**
	 * ジョブユニット内の参照ジョブのみを取得する
	 * @param item
	 * @param list
	 * @return
	 */
	public static ArrayList<JobInfo> findReferJob(JobTreeItem item){
		ArrayList<JobInfo> ret = new ArrayList<JobInfo>();
		if(item == null || item.getData() == null){
			return ret;
		}
		//ジョブID取得
		m_log.trace("checkReferJob Id=" + item.getData().getId());
		JobInfo jobInfo = item.getData();
		if (jobInfo.getType() == JobConstant.TYPE_REFERJOB) {
			if (jobInfo.getReferJobUnitId() != null
					&& jobInfo.getReferJobId() != null) {
				ret.add(item.getData());
				m_log.trace("JobId =" + jobInfo.getId() +
					", UnitId =" + jobInfo.getReferJobUnitId() +
					", referJobId =" + jobInfo.getReferJobId());
			}
		}
		//子JobTreeItemを取得
		for(JobTreeItem child : item.getChildren()){
			ret.addAll(findReferJob(child));
		}
		return ret;
	}

	/**
	 * ジョブツリーをソートする
	 * @param item
	 */
	public static void sort(JobTreeItem item) {
		ArrayList<JobTreeItem> children = item.getChildren();
		if (children == null || children.size() == 0) {
			return;
		}
		Collections.sort(item.getChildren(), new JobUtil().new DataComparator());
		for (JobTreeItem child : children) {
			sort(child);
		}
	}

	private class DataComparator implements java.util.Comparator<JobTreeItem>{
		@Override
		public int compare(JobTreeItem o1, JobTreeItem o2){
			String s1 = o1.getData().getId();
			String s2 = o2.getData().getId();
			m_log.trace("s1=" + s1 + ", s2=" + s2);
			return s1.compareTo(s2);
		}
	}

	/**
	 * ジョブセッションが走行中か否かをチェックする
	 * 
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @return ジョブセッションが走行中ならtrueを返す。ジョブセッションが終了しているならfalseを返す
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	public static boolean isRunJob(String sessionId, String jobunitId,
			String jobId) throws JobInfoNotFound, InvalidRole {
		// セッションIDとジョブIDから、セッションジョブを取得
		JpaTransactionManager jtm = new JpaTransactionManager();
		try {
			JobSessionJobEntity sessionJobEntity = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
			if (sessionJobEntity.getStatus() == StatusConstant.TYPE_END) {
				return false;
			} else
				return true;
		}
		finally {
			jtm.close();
		}
	}

	/**
	 * JobNotice関連情報を設定する
	 * 
	 * @param jobInfoEntity
	 * @param parentJobId
	 * @return
	 * @throws InvalidRole
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 */
	public static void copyJobNoticeProperties (JobInfoEntity jobInfoEntity, String parentJobId) throws HinemosUnknown {

		String sessionId = jobInfoEntity.getId().getSessionId();
		String jobunitId = jobInfoEntity.getId().getJobunitId();

		try {
			JobInfoEntity parentJobInfoEntity = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, parentJobId).getJobInfoEntity();

			String infoNotifyGroupId = NotifyGroupIdGenerator.generate(jobInfoEntity);
			jobInfoEntity.setNotifyGroupId(infoNotifyGroupId);

			jobInfoEntity.setBeginPriority(parentJobInfoEntity.getBeginPriority());
			jobInfoEntity.setNormalPriority(parentJobInfoEntity.getNormalPriority());
			jobInfoEntity.setWarnPriority(parentJobInfoEntity.getWarnPriority());
			jobInfoEntity.setAbnormalPriority(parentJobInfoEntity.getAbnormalPriority());

			// 取得したマスタ情報の通知グループIDで、通知関連情報を取得する
			List<NotifyRelationInfo> ct = new NotifyControllerBean()
					.getNotifyRelation(NotifyGroupIdGenerator
							.generate(jobInfoEntity));
			// JobNoticeInfo用の通知グループIDで、通知関連テーブルのコピーを作成する
			for (NotifyRelationInfo relation : ct) {
				relation.setNotifyGroupId(infoNotifyGroupId);
			}
			// JobからNotifyRelationInfoは１件のみ登録すればよい。
			new NotifyControllerBean().addNotifyRelation(ct);
		} catch (InvalidRole e) {
			throw new HinemosUnknown(e.getMessage());
		} catch (JobInfoNotFound e) {
			throw new HinemosUnknown(e.getMessage());
		}
	}
}