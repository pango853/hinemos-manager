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

package com.clustercontrol.jobmanagement.factory;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.bean.YesNoConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.jobmanagement.bean.CommandConstant;
import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.CommandTypeConstant;
import com.clustercontrol.jobmanagement.bean.DelayNotifyConstant;
import com.clustercontrol.jobmanagement.bean.ProcessingMethodConstant;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.bean.RunResultInfo;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.model.JobEndInfoEntity;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntityPK;
import com.clustercontrol.jobmanagement.util.JobMultiplicityCache;
import com.clustercontrol.jobmanagement.util.ParameterUtil;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.jobmanagement.util.SendTopic;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.util.Messages;

public class JobSessionNodeImpl {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( JobSessionNodeImpl.class );


	/**
	 * ノードへの実行指示を行います。
	 *
	 * @param sessionId セッションID
	 * @param jobId ジョブID
	 * @param facilityId ファシリティID
	 * @return true：終了していた、false：実行された
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	public boolean startNode(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole {
		m_log.debug("startNode() : sessionId=" + sessionId + ", jobId=" + jobId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		Collection<JobSessionNodeEntity> jobSessionNodeList = sessionJob.getJobSessionNodeEntities();
		if(jobSessionNodeList == null || jobSessionNodeList.size() == 0){
			//ジョブ終了時関連処理（再帰呼び出し）
			try {
				new JobSessionJobImpl().endJob(sessionId, jobunitId, jobId, null, true);
			} catch (HinemosUnknown e) {
				m_log.warn("startNode() : no node. " + e.getMessage(), e);
			} catch (FacilityNotFound e) {
				m_log.warn("startNode() : no node. " + e.getMessage(), e);
			}
			return false;
		}

		//終了している場合はメソッドから抜ける。
		if (checkAllNodeEnd(sessionJob)) {
			return true;
		}

		//コマンドの実行が正常終了するまで順次リトライの場合
		JobInfoEntity job = sessionJob.getJobInfoEntity();
		if(job.getProcessMode() == ProcessingMethodConstant.TYPE_RETRY){
			//実行中のノードが存在するかチェック
			for (JobSessionNodeEntity sessionNode : jobSessionNodeList) {
				//実行状態が実行中の場合
				//停止処理中(終了遅延等)は後続のノードを実行させないため、「実行された」状態とする
				if (sessionNode.getStatus() == StatusConstant.TYPE_RUNNING
						|| sessionNode.getStatus() == StatusConstant.TYPE_STOPPING) {
					return false;
				}
			}
		}

		ArrayList<JobSessionNodeEntity> orderedNodeList = new ArrayList<JobSessionNodeEntity>();
		ArrayList<String> validNodeList = AgentConnectUtil.getValidAgent();
		ArrayList<JobSessionNodeEntity> invalidNodeList = new ArrayList<JobSessionNodeEntity>();

		// 有効なノードがリストの前方にくるように並び替える
		for (JobSessionNodeEntity sessionNode : jobSessionNodeList) {
			if (validNodeList.contains(sessionNode.getId().getFacilityId())) {
				orderedNodeList.add(sessionNode);
			} else {
				invalidNodeList.add(sessionNode);
			}
		}

		// ノードの優先度順に並び替え
		Collections.sort(orderedNodeList, new JobPriorityComparator());
		Collections.sort(invalidNodeList, new JobPriorityComparator());

		// 有効なノードの末尾に、有効でないノードを挿入する
		orderedNodeList.addAll(invalidNodeList);

		if (m_log.isDebugEnabled()) {
			String str = "";
			for (JobSessionNodeEntity sessionNode : orderedNodeList) {
				str += sessionNode.getNodeName() + " -> ";
			}
			m_log.debug("orderedNodeList: " + str);
		}

		for (JobSessionNodeEntity sessionNode : orderedNodeList) {
			if (checkMultiplicity(sessionNode)) {
				JobMultiplicityCache.toRunning(sessionNode.getId());
				if (job.getProcessMode() == ProcessingMethodConstant.TYPE_RETRY) {
					break;
				}
			}
		}
		return false;
	}

	private boolean checkMultiplicity(JobSessionNodeEntity sessionNode) {
		boolean startFlag = false;
		String facilityId = sessionNode.getId().getFacilityId();
		String sessionId = sessionNode.getId().getSessionId();
		String jobunitId = sessionNode.getId().getJobunitId();
		String jobId = sessionNode.getId().getJobId();
		if (sessionNode.getStatus() != StatusConstant.TYPE_WAIT) {
			return false;
		}
		if (JobMultiplicityCache.isRunNow(facilityId)) {
			return true;
		}
		try {
			JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

			if(sessionJob.getJobInfoEntity().getMultiplicityNotify() == null){
				// ここは通らないはず
				m_log.info("multiplicity notify is null");
				return false;
			}

			// 上限を超えたときは通知する
			m_log.debug("checkMultiplicity " + sessionJob.getJobInfoEntity().getMultiplicityNotify());
			if (sessionJob.getJobInfoEntity().getMultiplicityNotify() == YesNoConstant.TYPE_YES) {
				//通知処理
				new Notice().multiplicityNotify(sessionId, jobunitId, jobId,
						sessionJob.getJobInfoEntity().getMultiplicityOperation());
			}

			// 上限を超えたときは、待機、実行、終了のいずれか。
			int status = sessionJob.getJobInfoEntity().getMultiplicityOperation();
			switch (status) {
			case StatusConstant.TYPE_WAIT:
				startFlag = true;
				break;
			case StatusConstant.TYPE_END:
				//実行状態を終了にする
				startFlag = false;
				sessionNode.setStatus(StatusConstant.TYPE_END);
				sessionNode.setEndValue(sessionJob.getJobInfoEntity().getMultiplicityEndValue());
				setMessage(sessionNode, Messages.getString("message.job.86"));

				if (checkAllNodeEnd(sessionJob)) {
					try {
						new JobSessionJobImpl().endJob(sessionId, jobunitId, jobId, sessionNode.getResult(), true);
					} catch (HinemosUnknown e) {
						m_log.warn("wait2running END " + e.getMessage(), e);
					} catch (FacilityNotFound e) {
						m_log.warn("wait2running END " + e.getMessage(), e);
					}
				}
				break;
			default:
				m_log.warn("wait2running " + status + " is unknown status");
				startFlag = true; // 想定外の値の場合は実行する。
			}
		} catch (InvalidRole e) {
			m_log.warn("wait2running " + e.getMessage());
			startFlag = true; // 想定外の場合は実行する。
		} catch (JobInfoNotFound e) {
			m_log.warn("wait2running " + e.getMessage());
			startFlag = true; // 想定外の場合は実行する。
		}
		return startFlag;
	}

	/**
	 * ノード詳細の中で待機中のものを実行中に遷移させる。
	 * JobMultiplicityCache.kick()以外から呼ばないこと。
	 *
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @param facilityId
	 * @return 0:実行、1:ジョブ詳細が実行中ではない、-1:実行しない
	 * @throws JobInfoNotFound
	 */
	public int wait2running(JobSessionNodeEntityPK pk) {
		int startCommand = -1;

		String sessionId = pk.getSessionId();
		String jobunitId = pk.getJobunitId();
		String jobId = pk.getJobId();
		String facilityId = pk.getFacilityId();

		JobSessionNodeEntity sessionNode = null;
		try {
			sessionNode = QueryUtil.getJobSessionNodePK(sessionId, jobunitId, jobId, facilityId);
		} catch (JobInfoNotFound e) {
			m_log.warn("wait2running " + e.getMessage());
			return -1;
		}

		//ノード詳細の実行状態が待機でない場合
		if(sessionNode.getStatus() != StatusConstant.TYPE_WAIT){
			return -1;
		}

		JobSessionJobEntity sessionJobEntity = null;
		try {
			sessionJobEntity = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		} catch (JobInfoNotFound e) {
			m_log.warn("wait2running " + e.getMessage());
			return -1;
		} catch (InvalidRole e) {
			m_log.warn("wait2running " + e.getMessage());
			return -1;
		}

		//ジョブ詳細の実行状態が実行中でない場合、
		//waitQueueから削除し、false を返す
		if(sessionJobEntity.getStatus() != StatusConstant.TYPE_RUNNING){
			m_log.info("wait2running job detail is not running " + sessionNode.getId().getFacilityId() +
					"," + sessionNode.getId().getSessionId() +
					", status is " + StatusConstant.typeToString(sessionJobEntity.getStatus()));
			
			return 1;
		}
		
		//実行状態を実行中にする
		m_log.info("wait2running " + sessionNode.getId().getFacilityId() +
				"," + sessionNode.getId().getSessionId());
		sessionNode.setStatus(StatusConstant.TYPE_RUNNING);
		setMessage(sessionNode, Messages.getString("wait.agent.response"));

		try {
			//Topicに送信
			m_log.debug("startNode() : send RunInstructionInfo() : " +
					"sessionId=" + sessionId + ", jobId=" + jobId + ", facilityId=" + sessionNode.getId().getFacilityId());
			runJobSessionNode(sessionId, jobunitId, jobId, sessionNode.getId().getFacilityId());
			startCommand = 0;
		} catch (Exception e) {
			m_log.warn("startNode() RunInstructionInfo() send error : " +
					"sessionId=" + sessionId + ", jobId=" + jobId + ", facilityId=" + sessionNode.getId().getFacilityId() + " : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
		return startCommand;
	}

	private void runJobSessionNode(String sessionId, String jobunitId, String jobId, String facilityId) throws JobInfoNotFound, InvalidRole, HinemosUnknown, JobMasterNotFound, FacilityNotFound {
		m_log.debug("runJobSessionNode() : sessionId=" + sessionId + ", jobunitid=" + jobunitId + ", jobId=" + jobId + ", facilityId=" + facilityId);
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		JobInfoEntity job = sessionJob.getJobInfoEntity();
		JobSessionNodeEntity sessionNode = QueryUtil.getJobSessionNodePK(sessionId, jobunitId, jobId, facilityId);

		//実行指示情報を作成
		RunInstructionInfo instructionInfo = new RunInstructionInfo();
		instructionInfo.setSessionId(sessionJob.getId().getSessionId());
		instructionInfo.setJobunitId(sessionJob.getId().getJobunitId());
		instructionInfo.setJobId(sessionJob.getId().getJobId());
		instructionInfo.setFacilityId(sessionNode.getId().getFacilityId());

		String startCommand = job.getStartCommand();

		// ジョブ変数のパラメータを置き換える
		startCommand = ParameterUtil.replaceSessionParameterValue(
				sessionId,
				sessionNode.getId().getFacilityId(),
				job.getStartCommand());
		
		//コマンド内のパラメータを置き換える(#[RETURN:jobid:facilityId])
		startCommand = ParameterUtil.replaceReturnCodeParameter(sessionId, jobunitId, startCommand);
		instructionInfo.setCommand(startCommand);
		instructionInfo.setSpecifyUser(job.getSpecifyUser());
		instructionInfo.setUser(job.getEffectiveUser());
		instructionInfo.setCommandType(CommandTypeConstant.NORMAL);

		//特殊コマンド
		if(instructionInfo.getCommand().equals(CommandConstant.ADD_PUBLIC_KEY) ||
				instructionInfo.getCommand().equals(CommandConstant.DELETE_PUBLIC_KEY)){
			//公開鍵設定

			//セッションIDとジョブIDから、セッションジョブを取得
			JobSessionJobEntity argumentSessionJob = QueryUtil.getJobSessionJobPK(
					sessionId, jobunitId,
					job.getArgumentJobId());
			String result = argumentSessionJob.getResult();

			//設定したい公開鍵のファシリティIDを設定
			instructionInfo.setPublicKey(result);
		}else if(instructionInfo.getCommand().equals(CommandConstant.GET_FILE_LIST)){
			//ファイルリスト取得

			//取得したいファイルリストのパスを設定
			instructionInfo.setFilePath(job.getArgument());
		}else if(instructionInfo.getCommand().equals(CommandConstant.GET_CHECKSUM)){
			//チェックサム取得

			//チェックサムを取得するファイルパスを設定
			instructionInfo.setFilePath(job.getArgument());
		}else if(instructionInfo.getCommand().equals(CommandConstant.CHECK_CHECKSUM)){
			//整合性チェック

			//セッションIDとジョブIDから、セッションジョブを取得
			JobSessionJobEntity argumentSessionJob =
					QueryUtil.getJobSessionJobPK(sessionId, jobunitId, job.getArgumentJobId());
			String result = argumentSessionJob.getResult();

			//チェックサムを設定
			instructionInfo.setCheckSum(result);
			//整合性チェックするファイルパスを設定
			instructionInfo.setFilePath(job.getArgument());
		} else if (instructionInfo.getJobId().endsWith(CreateHulftJob.HULOPLCMD)) {
			//セッションIDとジョブIDから、セッションジョブを取得
			JobSessionJobEntity argumentSessionJob = QueryUtil.getJobSessionJobPK(
					sessionId, jobunitId,
					job.getArgumentJobId());
			String result = argumentSessionJob.getResult();

			// TRIDを指定
			instructionInfo.setCommand(instructionInfo.getCommand() + " " + result);
		}

		try {
			//Topicに送信
			SendTopic.put(instructionInfo);

		} catch (Exception e) {
			m_log.warn("runJobSessionNode() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
	}

	/**
	 * ノード終了処理を行います。
	 *
	 * @param info 実行結果情報
	 * @return コマンドを実行してよい場合はtrue、コマンドを実行しないでほしい場合はfalse
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws FacilityNotFound
	 * @throws EntityExistsException
	 * @throws InvalidRole
	 */
	public boolean endNode(RunResultInfo info) throws HinemosUnknown, JobInfoNotFound, EntityExistsException, FacilityNotFound, InvalidRole {
		m_log.info("endNode() : sessionId=" + info.getSessionId() + ", jobunitId=" + info.getJobunitId() +
				", jobId=" + info.getJobId() + ", facilityId=" + info.getFacilityId() + ", commandType=" + info.getCommandType());

		//コマンドタイプチェック
		//NORMALの結果が返ってくる前に、STOPコマンドを実行すると、
		//STOPコマンドの結果の後にNORMALコマンドの結果が返ってくる。
		//その場合は、NORMALコマンドの結果を無視する。(状態が上書きされてしまうので。)
		JobSessionNodeEntity jobNode = QueryUtil.getJobSessionNodePK(info.getSessionId(), info.getJobunitId(),
				info.getJobId(), info.getFacilityId());
		int status = jobNode.getStatus();
		int commandType = info.getCommandType();
		if ((commandType == CommandTypeConstant.NORMAL && status != StatusConstant.TYPE_RUNNING) ||
				(commandType == CommandTypeConstant.STOP && status != StatusConstant.TYPE_STOPPING)) {
			// 実行終了が返ってきたが、終了遅延等で状態が実行中以外になっていた場合等は、
			// このルートを通る。
			m_log.info("ignore command, commandType=" + commandType + ", status=" + status);
			return false;
		}

		if(commandType == CommandTypeConstant.NORMAL || commandType == CommandTypeConstant.STOP){
			if (!endNodeNormalStop(info)) {
				// ジョブの多重実行の場合はエージェントでジョブを実行させないようにfalseを返す
				return false;
			}
		}

		endNodeFinish(info.getSessionId(), info.getJobunitId(), info.getJobId(), info.getFacilityId(),
				info.getCommand(), info.getFileList());
		return true;
	}

	// 	 * @return コマンドを実行してよい場合はtrue、コマンドを実行しないでほしい場合はfalseを返す
	private boolean endNodeNormalStop(RunResultInfo info) throws JobInfoNotFound, InvalidRole {
		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(info.getSessionId(), info.getJobunitId(), info.getJobId());
		JobInfoEntity job = sessionJob.getJobInfoEntity();
		JobSessionNodeEntity sessionNode = QueryUtil.getJobSessionNodePK(info.getSessionId(),
				info.getJobunitId(), info.getJobId(), info.getFacilityId());

		boolean isSessionNodeRunning = false;
		if (sessionNode.getStatus() == StatusConstant.TYPE_RUNNING) {
			isSessionNodeRunning = true;
		}

		//実行状態で分岐
		if(info.getStatus() == RunStatusConstant.START){
			//開始の場合

			if(sessionNode.getStartDate() == null){
				//開始・再実行日時を設定
				sessionNode.setStartDate(new Timestamp(info.getTime()));
				setMessage(sessionNode, Messages.getString("wait.command.end"));
				
				AgentInfo agentInfo = AgentConnectUtil.getAgentInfo(info.getFacilityId());
				if (agentInfo != null) {
					sessionNode.setStartupTime(agentInfo.getStartupTime());
				} else {
					m_log.warn("agentInfo is null");
				}

				//チェック中の場合
				m_log.debug("agent check OK : status=" + info.getStatus() + ", sessionId=" + info.getSessionId() + ", jobId=" + info.getJobId() + ", facilityId=" + info.getFacilityId());

			}else{
				m_log.info("endNodeSetStatus() : this messsage is already received. drop message." +
						" sessionId=" + info.getSessionId() +
						", jobId=" + info.getJobId() +
						", facilityId=" + info.getFacilityId());
				// 同一のジョブが複数実行されてしまう場合は、後発を実行させないようにfalseを返す
				return false;
			}
		}else{
			boolean retryFlag = true;

			//再実行判定（正常終了時は再実行しない）
			JobEndInfoEntity jobEndInfo = QueryUtil
					.getJobEndInfoPK(info.getSessionId(),
							info.getJobunitId(), info.getJobId(),
							EndStatusConstant.TYPE_NORMAL);

			if (info.getStatus() == RunStatusConstant.END &&
					(jobEndInfo.getEndValueFrom() <= info.getEndValue() &&
					info.getEndValue() <= jobEndInfo.getEndValueTo())) {
				retryFlag = false;
			} else {
				if (job.getCommandRetryFlg() == YesNoConstant.TYPE_NO) {
					retryFlag = false;
				}
			}

			if(info.getStatus() == RunStatusConstant.END){
				//終了の場合
				if (retryFlag && retryJob(sessionNode, sessionJob, info, job.getCommandRetry())) {
					//再実行あり
					return false;
				}

				//開始・再実行日時が設定済みならば、終了処理を行う。
				if(sessionJob.getStartDate() != null || sessionNode.getStartDate() != null){
					if(sessionJob.getEndDate() == null ||
							sessionJob.getStatus() == StatusConstant.TYPE_RUNNING ||
							sessionJob.getStatus() == StatusConstant.TYPE_SUSPEND ||
							sessionJob.getStatus() == StatusConstant.TYPE_STOPPING){

						//実行状態バッファを設定
						if(sessionNode.getStatus() == StatusConstant.TYPE_RUNNING){
							//実行状態が実行中の場合、終了を設定
							sessionNode.setStatus(StatusConstant.TYPE_END);
							//終了・中断日時を設定
							sessionNode.setEndDate(new Timestamp(info.getTime()));
						}else if(sessionNode.getStatus() == StatusConstant.TYPE_STOPPING){
							if(info.getCommandType() == CommandTypeConstant.STOP){
								//実行状態が停止処理中の場合、コマンド停止を設定
								sessionNode.setStatus(StatusConstant.TYPE_STOP);
							}
						}
						if (info.getCommandType() == CommandTypeConstant.STOP &&
								info.getStopType() == CommandStopTypeConstant.DESTROY_PROCESS) {
							// プロセス終了の場合
							//メッセージを設定
							setMessage(sessionNode, Messages.getString("job.process.shutdown"));
						} else {
							// プロセス終了以外の場合
							//メッセージを設定
							setMessage(sessionNode, "stdout=" + info.getMessage() + ", stderr=" + info.getErrorMessage());
						}

						//終了値を設定
						sessionNode.setEndValue(info.getEndValue());

						//特殊コマンド
						if(info.getCommand().equals(CommandConstant.GET_PUBLIC_KEY)){
							//公開鍵取得
							sessionNode.setResult(info.getPublicKey());
						}else if(info.getCommand().equals(CommandConstant.GET_CHECKSUM)){
							//チェックサム取得
							sessionNode.setResult(info.getCheckSum());
						} else if (info.getJobId().endsWith(CreateHulftJob.UTLSEND) && CreateHulftJob.isHulftMode()) {
							//TRID取得
							sessionNode.setResult(info.getMessage());
						}
					}else{
						m_log.debug("endNodeSetStatus() : this messsage is already received. drop message." +
								" sessionId=" + info.getSessionId() +
								", jobId=" + info.getJobId() +
								", facilityId=" + info.getFacilityId());
					}
				}else{
					// 起動時刻が無い状態で停止通知を受信した場合
					m_log.info("endNodeSetStatus() : this messsage does not have start time. drop message." +
							" sessionId=" + info.getSessionId() +
							", jobId=" + info.getJobId() +
							", facilityId=" + info.getFacilityId());
				}
			}else if(info.getStatus() == RunStatusConstant.ERROR){
				//失敗の場合
				if (retryFlag && retryJob(sessionNode, sessionJob, info, job.getCommandRetry())) {
					//再実行あり
					return false;
				}

				//実行状態、終了値、終了・中断日時を設定
				if(sessionNode.getStatus() == StatusConstant.TYPE_RUNNING){
					//エラー時に終了にする
					if(job.getMessageRetryEndFlg() == YesNoConstant.TYPE_YES){
						//実行状態が実行中の場合、実行状態バッファに終了を設定
						sessionNode.setStatus(StatusConstant.TYPE_END);

						//終了・中断日時を設定
						sessionNode.setEndDate(new Timestamp(new Date().getTime()));
						//終了値を設定
						sessionNode.setEndValue(job.getMessageRetryEndValue());
					}else{
						//実行状態が実行中の場合、実行状態バッファに実行失敗を設定
						sessionNode.setStatus(StatusConstant.TYPE_ERROR);
					}
				}else if(sessionNode.getStatus() == StatusConstant.TYPE_STOPPING){
					//実行状態が停止処理中の場合、実行状態バッファにコマンド停止を設定
					sessionNode.setStatus(StatusConstant.TYPE_STOP);
					sessionNode.setEndValue(info.getEndValue());
				}

				//メッセージを設定
				setMessage(sessionNode, info.getMessage() + info.getErrorMessage());
			}

			if (sessionNode.getStartDate() == null) {
				// ジョブ実行命令がノードに届いていない場合
				m_log.debug("set status buffer : status=" + info.getStatus() +
						", sessionId=" + info.getSessionId() + ", jobId=" + info.getJobId() + ", facilityId=" + info.getFacilityId());
			}

			//他の状態に遷移した場合は、キャッシュを更新する。
			if (isSessionNodeRunning &&
					sessionNode.getStatus() != StatusConstant.TYPE_RUNNING) {
				JobMultiplicityCache.fromRunning(sessionNode.getId());
			}

		}
		return true;
	}

	protected void endNodeFinish(String sessionId, String jobunitId, String jobId, String facilityId, String command, List<String> fileList)
			throws JobInfoNotFound, HinemosUnknown, EntityExistsException, FacilityNotFound, InvalidRole {
		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		JobInfoEntity job = sessionJob.getJobInfoEntity();
		JobSessionNodeEntity sessionNode = QueryUtil.getJobSessionNodePK(sessionId, jobunitId, jobId, facilityId);
		m_log.debug("endNodeFinish() : status=" + sessionNode.getStatus() +
				", facilityId=" + sessionNode.getId().getFacilityId());
		//実行状態チェック
		if(sessionNode.getStatus() == StatusConstant.TYPE_STOP){
			//実行状態がコマンド停止の場合
			if(sessionJob.getStatus() == StatusConstant.TYPE_STOPPING &&
					checkAllNodeStop(sessionJob)){
				//全ノード停止の場合
				//実行状態にコマンド停止を設定
				sessionJob.setStatus(StatusConstant.TYPE_STOP);
				//ジョブ停止関連処理
				new OperateStopOfJob().stopJob2(sessionId, jobunitId, jobId);
				//遅延通知状態を取得
				int flg = sessionJob.getDelayNotifyFlg();
				//遅延通知状態から操作済みフラグを取得
				int operationFlg = DelayNotifyConstant.getOperation(flg);
				if(operationFlg == DelayNotifyConstant.STOP_SET_END_VALUE){
					//操作済みフラグが停止[状態指定]の場合、停止[状態変更]を行う
					new OperateMaintenanceOfJob().maintenanceJob(
							sessionId,
							jobunitId,
							jobId,
							StatusConstant.TYPE_END_END_DELAY,
							job.getEndDelayOperationEndStatus(),
							job.getEndDelayOperationEndValue());
				}
			}
		}else{
			if(sessionJob.getJobInfoEntity().getProcessMode() == ProcessingMethodConstant.TYPE_RETRY) {
				//実行状態がコマンド停止以外の場合
				if(sessionJob.getStatus() == StatusConstant.TYPE_SUSPEND) {
					return;
				}
				if(checkAllNodeEnd(sessionJob)){
					m_log.info("endNodeFinish() : all nodes end (type retry) " + facilityId);
					Collection<JobSessionNodeEntity> nodeList = sessionJob.getJobSessionNodeEntities();
					for (JobSessionNodeEntity node : nodeList) {
						// 状態が待機のノードは終了に遷移させる。
						if (node.getStatus() == StatusConstant.TYPE_WAIT) {
							JobMultiplicityCache.removeWait(node.getId());
							node.setStatus(StatusConstant.TYPE_END);
							setMessage(node, "didn't execute");
						}
					}
					//ジョブ終了時関連処理（再帰呼び出し）
					new JobSessionJobImpl().endJob(sessionId, jobunitId, jobId,
							sessionNode.getResult(), true);
				} else {
					m_log.info("endNodeFinish() : next node " + sessionJob.getId());
					// 次のノードを実行させる。
					startNode(sessionJob.getId().getSessionId(),
							sessionJob.getId().getJobunitId(),
							sessionJob.getId().getJobId());
				}
			} else {
				//実行状態がコマンド停止以外の場合
				if(sessionJob.getStatus() != StatusConstant.TYPE_SUSPEND && checkAllNodeEnd(sessionJob)){
					//ジョブ終了の場合
					//ファイル転送ジョブ
					if(CommandConstant.GET_FILE_LIST.equals(command)){
						new CreateFileJob().createFileJobNet(
								sessionJob,
								fileList);
					}
					//ジョブ終了時関連処理（再帰呼び出し）
					new JobSessionJobImpl().endJob(sessionId, jobunitId, jobId, sessionNode.getResult(), true);
				}
			}
		}
	}

	/**
	 * 全ノードの停止チェックを行います。
	 *
	 * @param sessionJob セッションジョブ
	 * @return true：停止、false：未停止あり
	 */
	protected boolean checkAllNodeStop(JobSessionJobEntity sessionJob){
		m_log.debug("checkAllNodeStop() : sessionId=" + sessionJob.getId().getSessionId() +
				", jobId=" + sessionJob.getId().getJobId());

		boolean stop = true;
		for (JobSessionNodeEntity sessionNode : sessionJob.getJobSessionNodeEntities()) {
			//実行状態が停止処理中かチェック
			if(sessionNode.getStatus() == StatusConstant.TYPE_STOPPING ||
					sessionNode.getStatus() == StatusConstant.TYPE_RUNNING){
				stop = false;
				break;
			}
		}

		return stop;
	}

	/**
	 * 全ノードの終了チェックを行います。
	 *
	 * @param sessionJob セッションジョブ
	 * @return true：終了、false：未終了
	 */
	private boolean checkAllNodeEnd(JobSessionJobEntity sessionJob) {
		m_log.debug("checkAllNodeEnd() : sessionId=" + sessionJob.getId().getSessionId() + ", jobId=" + sessionJob.getId().getJobId());

		//終了フラグをfalseにする
		boolean end = false;

		if(sessionJob.getJobInfoEntity().getProcessMode() == ProcessingMethodConstant.TYPE_RETRY){
			//順次リトライの場合は、実行状態が正常終了のものが一つあれば終了とみなす。
			Integer endStatus = null;
			try {
				endStatus = new JobSessionJobImpl().checkEndStatus(sessionJob.getId().getSessionId(),
						sessionJob.getId().getJobunitId(), sessionJob.getId().getJobId());
			} catch (JobInfoNotFound e) {
				// ここは通らない。
				m_log.info("checkAllNodeEnd " + e.getMessage(), e);
			} catch (InvalidRole e) {
				m_log.info("checkAllNodeEnd " + e.getMessage(), e);
			} catch (Exception e) {
				m_log.info("checkAllNodeEnd " + e.getMessage(), e);
			}
			if (endStatus != null && endStatus == EndStatusConstant.TYPE_NORMAL) {
				return true;
			}
		}
		end = true;
		for (JobSessionNodeEntity sessionNode : sessionJob.getJobSessionNodeEntities()) {
			//実行状態が終了または変更済以外がなければOK。
			if(! StatusConstant.isEndGroup(sessionNode.getStatus())){
				end = false;
				break;
			}
		}
		return end;
	}


	/**
	 * エージェントタイムアウトチェックを行います。
	 */
	public HashMap<String, List<JobSessionNodeEntityPK>> checkTimeoutAll() throws JobInfoNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		Collection<JobSessionNodeEntity> collection = null;
		collection = em.createNamedQuery("JobSessionNodeEntity.findByStatusStartIsNull", JobSessionNodeEntity.class)
				.setParameter("status", StatusConstant.TYPE_RUNNING)
				.getResultList();

		HashMap<String, List<JobSessionNodeEntityPK>> map = new HashMap<String, List<JobSessionNodeEntityPK>>();

		for (JobSessionNodeEntity node : collection) {
			if (node.getStartDate() == null) {
				ArrayList<JobSessionNodeEntityPK> list = (ArrayList<JobSessionNodeEntityPK>)map.get(node.getId().getSessionId());
				if (list == null) {
					list = new ArrayList<JobSessionNodeEntityPK>();
				}
				list.add(node.getId());
				map.put(node.getId().getSessionId(), list);
			}
		}

		return map;
	}

	/**
	 * エージェントタイムアウトチェックを行います。
	 *
	 * @param sessionId セッションID
	 * @param jobId ジョブID
	 * @throws JobInfoNotFound
	 */
	public void checkTimeout(JobSessionNodeEntityPK pk) throws JobInfoNotFound {
		String sessionId = pk.getSessionId();
		String jobunitId = pk.getJobunitId();
		String jobId = pk.getJobId();
		String facilityId = pk.getFacilityId();
		JobSessionNodeEntity sessionNode = QueryUtil.getJobSessionNodePK(sessionId, jobunitId, jobId, facilityId);
		m_log.debug("checkTimeout() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", facilityId=" + facilityId);

		//待ち条件ジョブ判定
		if(sessionNode.getStatus() != StatusConstant.TYPE_RUNNING){
			// 1分に1回のタイムアウトチェック中にエージェントの応答があると、
			// このルートを通る。
			m_log.info("checkTimeout() : status is not running");
			return;
		}

		if(sessionNode.getStartDate() != null){
			// エージェントにジョブ実行命令を出した直後に、
			// 1分に1回のcheckTimeoutが走った場合は、このルートを通る。
			m_log.info("checkTimeout() : startDate is not null");
			return;
		}
		int retry = sessionNode.getRetryCount();
		int messageRetry = sessionNode.getJobSessionJobEntity().getJobInfoEntity().getMessageRetry();
		if(retry >= messageRetry){
			//リトライ上限を超えたときは、AgentTimeoutErrorとする。

			m_log.info("checkTimeout() : Agent Check NG : sessionId=" + sessionId + ", jobId=" + jobId + ", facilityId=" + facilityId);

			//実行結果情報を作成
			RunResultInfo info = new RunResultInfo();
			info.setSessionId(sessionId);
			info.setJobunitId(jobunitId);
			info.setJobId(jobId);
			info.setFacilityId(facilityId);
			info.setCommand("");
			info.setCommandType(CommandTypeConstant.NORMAL);
			info.setStatus(RunStatusConstant.ERROR);
			info.setMessage(Messages.getString("agent.timeout.error") + " (" + retry + ")");
			info.setErrorMessage("");
			try {
				endNode(info);
			} catch (InvalidRole | JobInfoNotFound e) {
				m_log.warn("setting status failure. (sessionId = " + info.getSessionId() + ", facilityId = " + info.getFacilityId()
						+ ", status = " + info.getStatus() + ", commandType = " + info.getCommandType() + ")"  + " (" + e.getClass().getName() + ")");
			} catch (Exception e) {
				m_log.warn("checkTimeout() RunresultInfo send error : sessionId=" + sessionId + ", jobId=" + jobId + ", facilityId=" + facilityId + ",  : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage() + " (" + e.getClass().getName() + ")", e);
			}
		}else{
			int retryCount = sessionNode.getRetryCount();
			retryCount++;
			sessionNode.setRetryCount(retryCount);
			try {
				//Topicに送信
				m_log.debug("checkTimeout() : send RunInstructionInfo() : sessionId=" + sessionId +
						", jobId=" + jobId + ", facilityId=" + facilityId + ", retry=" + retryCount);

				runJobSessionNode(sessionId, jobunitId, jobId, facilityId);
			} catch (InvalidRole | HinemosUnknown | JobMasterNotFound | FacilityNotFound e) {
				m_log.warn("checkTimeout() RunInstructionInfo() send error : sessionId=" + sessionId + ", jobId=" + jobId + ", facilityId=" + facilityId + " : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
			} catch (Exception e) {
				m_log.warn("checkTimeout() RunInstructionInfo() send error : sessionId=" + sessionId + ", jobId=" + jobId + ", facilityId=" + facilityId + " : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
		}
	}

	private class JobPriorityComparator implements Comparator<JobSessionNodeEntity> {
		// 二つのJobSessionNodeEntityを受け取り、ノードのジョブ優先度を降順で比較する関数
		// ノードの優先度が同じ場合は、ファシリティID順とする。
		@Override
		public int compare(JobSessionNodeEntity s, JobSessionNodeEntity t) {
			try {
				int priority_s = NodeProperty.getProperty(s.getId().getFacilityId()).getJobPriority();
				int priority_t = NodeProperty.getProperty(t.getId().getFacilityId()).getJobPriority();
				if (priority_s != priority_t) {
					return priority_t - priority_s;
				}
				String facilityId_s = NodeProperty.getProperty(s.getId().getFacilityId()).getFacilityId();
				String facilityId_t = NodeProperty.getProperty(t.getId().getFacilityId()).getFacilityId();
				return facilityId_s.compareTo(facilityId_t);
			} catch (FacilityNotFound e) {
				m_log.warn("NodeComparator " + e.getMessage());
			}
			return 0;
		}
	}

	/**
	 * ノード詳細に表示するメッセージをセットします。
	 * @param sessionNode
	 * @param newMsg
	 */
	public void setMessage(JobSessionNodeEntity sessionNode, String newMsg) {
		String msg;
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String dateStr = df.format(date);
		String header ="["+ dateStr + "] ";
		String oldMsg = sessionNode.getMessage();

		//行末に改行が含まれている場合は除く
		Pattern pattern = Pattern.compile("\r\n$");
		Matcher m = pattern.matcher(newMsg);
		if (m.find() == false) {
			pattern = Pattern.compile("\n$");
			m = pattern.matcher(newMsg);
		}
		newMsg = m.replaceAll("");

		if (oldMsg == null || oldMsg.equals("")) {
			msg = header + newMsg;
		} else {
			//既にメッセージがあれば追記して改行
			msg =  header + newMsg + "\r\n" + oldMsg;
		}

		//設定された文字数で切る
		int msgMaxLen = HinemosPropertyUtil.getHinemosPropertyNum("job.message.max.length", 2048);
		if (msg.length() > msgMaxLen) {
			msg = msg.substring(0, msgMaxLen);
		}
		sessionNode.setMessage(msg);
	}

	private boolean retryJob(JobSessionNodeEntity sessionNode, JobSessionJobEntity sessionJob, RunResultInfo info, int maxRetry) {

		if (sessionNode.getStatus() != StatusConstant.TYPE_RUNNING) {
			return false;
		}

		if (sessionJob.getStatus() == StatusConstant.TYPE_SUSPEND) {
			setMessage(sessionNode, Messages.getString("suspend"));
			sessionNode.setStatus(StatusConstant.TYPE_WAIT);
			sessionNode.setStartDate(null);
			return false;
		} else if (sessionJob.getStatus() != StatusConstant.TYPE_RUNNING) {
			return false;
		}

		//エージェントが終了した場合は再実行を行わない
		if (info.getStatus().equals(RunStatusConstant.ERROR)
				&& info.getMessage().equals(Messages.getString("message.agent.5"))) {
			return false;
		}

		//通算回数
		int errorCount = sessionNode.getErrorRetryCount();
		m_log.debug("maxRetry:" + maxRetry + " errorCount:" + errorCount + " " + (maxRetry > errorCount) + ", " + sessionNode.getId());
		if (maxRetry > errorCount) {
			//再実行前にスリープ処理
			int jobRetryInterval = HinemosPropertyUtil.getHinemosPropertyNum("job.retry.interval", 10 * 1000);
			try {
				Thread.sleep(jobRetryInterval);
			} catch (InterruptedException e) {
				m_log.warn("retryJob() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				return false;
			}

			//上限回数に達してない場合は再実行
			//通算回数を加算
			errorCount++;
			m_log.debug("errRtryCnt++=" + errorCount);

			//DB更新
			String msg =info.getMessage() + info.getErrorMessage();
			setMessage(sessionNode, msg);
			setMessage(sessionNode, Messages.getString("retrying") + "(" + errorCount + ")");
			sessionNode.setErrorRetryCount(errorCount);
			sessionNode.setStatus(StatusConstant.TYPE_WAIT);
			sessionNode.setStartDate(null);

			//再実行
			if(checkMultiplicity(sessionNode)) {
				JobMultiplicityCache.fromRunning(sessionNode.getId());
				JobMultiplicityCache.toRunning(sessionNode.getId());
				return true;
			}
		}

		return false;
	}

	/**
	 * Hinemosエージェントが終了した際に実行中ノードを終了状態へ更新します。
	 * @param facilityId ファシリティID
	 * @param agentInfo エージェント情報
	 * @param isNormalEnd エージェントが正常終了時に呼ばれたかどうか
	 */
	public void endNodeByAgent(String facilityId, AgentInfo agentInfo, boolean isNormalEnd) {

		String queryName = new String();
		if (isNormalEnd) {
			queryName = "JobSessionNodeEntity.findByFacilityIdStatus";
		} else {
			queryName = "JobSessionNodeEntity.findByDifferentStartuptime";
		}

		JpaTransactionManager jtm = new JpaTransactionManager();
		jtm.begin();

		//実行中のジョブ情報を取得
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<JobSessionNodeEntity> list = null;
		list = em.createNamedQuery(queryName, JobSessionNodeEntity.class)
				.setParameter("status", StatusConstant.TYPE_RUNNING)
				.setParameter("facilityId", facilityId)
				.setParameter("startupTime", agentInfo.getStartupTime())
				.getResultList();

		for (JobSessionNodeEntity entity : list) {
			//実行結果情報を作成
				RunResultInfo info = new RunResultInfo();
				info.setSessionId(entity.getId().getSessionId());
				info.setJobunitId(entity.getId().getJobunitId());
				info.setJobId(entity.getId().getJobId());
				info.setFacilityId(facilityId);
				info.setCommand("");
				info.setCommandType(CommandTypeConstant.NORMAL);
				info.setStatus(RunStatusConstant.ERROR);
				info.setMessage(Messages.getString("message.agent.5"));
				info.setErrorMessage("");
				m_log.info("endNodeByAgent " + entity.getId().toString());
				try {
					endNode(info);
				} catch (InvalidRole e) {
					m_log.info("setting status failure. (sessionId = " + info.getSessionId() + ", facilityId = " + info.getFacilityId()
							+ ", status = " + info.getStatus() + ", commandType = " + info.getCommandType() + ")", e);
					jtm.rollback();
					return;
				} catch (JobInfoNotFound e) {
					m_log.info("setting status failure. (sessionId = " + info.getSessionId() + ", facilityId = " + info.getFacilityId()
							+ ", status = " + info.getStatus() + ", commandType = " + info.getCommandType() + ")", e);
					jtm.rollback();
					return;
				} catch (Exception e) {
					m_log.warn(
							"endNodeByAgent() RunresultInfo send error : sessionId=" + info.getSessionId()
									+ ", jobId="+ info.getJobId()
									+ ", facilityId=" + facilityId
									+ ",  : " + e.getClass().getSimpleName()
									+ ", " + e.getMessage(), e);
					jtm.rollback();
					return;
				}
			}
		jtm.commit();
	}
}
