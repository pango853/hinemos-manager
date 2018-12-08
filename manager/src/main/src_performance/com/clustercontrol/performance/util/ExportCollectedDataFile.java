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

package com.clustercontrol.performance.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.performance.bean.CollectorItemParentInfo;
import com.clustercontrol.performance.bean.PerformanceDataSettings;
import com.clustercontrol.performance.model.CalculatedDataEntity;
import com.clustercontrol.performance.model.CalculatedDataEntityPK;
import com.clustercontrol.performance.session.CollectorControllerBean;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.Messages;

/**
 * 性能情報のCSVデータを出力、削除を行うクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class ExportCollectedDataFile {
	private static Log m_log = LogFactory
			.getLog(ExportCollectedDataFile.class);

	public final static String NOT_EXISTS = "";
	public final static String NOT_A_NUMBER = "?";

	/**
	 *
	 * ①1 File / 1 Node でCSV性能データファイルを作成する<BR>
	 * facilityId = Node の場合：1ファイル facilityId = Scope
	 * の場合：配下の(有効な)ノード毎に1ファイルを作成する
	 *
	 * ②header が有効な場合 各CSVファイルに収集項目の項目名をヘッダとして出力する
	 *
	 * ③archiveが有効な場合 facilityId = Node の場合：対象1ファイルをzip圧縮する facilityId = Scope
	 * の場合：対象1ファイル群を1ファイルにzip圧縮する
	 *
	 * @param monitorId
	 * @param facilityId
	 * @param header
	 * @param archive
	 * @return
	 */
	public static synchronized ArrayList<String> create(
			String monitorId,
			String facilityId,
			boolean header,
			boolean archive,
			String userId)
					throws HinemosUnknown {
		m_log.debug("create() monitorId = " + monitorId + ", facilityId = "
				+ facilityId + ", header = " + header + ", archive");

		// null check
		if (monitorId == null || "".equals(monitorId) || facilityId == null
				|| "".equals(facilityId)) {
			m_log.debug("target file is empty");
			return new ArrayList<String>();
		}

		// 対象ファイル名に含めるID(日付)
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");
		String fileId = sdf1.format(new Date(System.currentTimeMillis()));

		ArrayList<String> targetFileNameList = new ArrayList<String>();
		try {
			com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_NONE(monitorId);
			ArrayList<String> targetFacilityIdList
			= new RepositoryControllerBean().getExecTargetFacilityIdList(facilityId, null);

			if(targetFacilityIdList == null || targetFacilityIdList.size() == 0){
				String[] args = { facilityId };
				HinemosUnknown e = new HinemosUnknown(Messages.getString("message.performance.3", args));
				m_log.info("create() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			// 出力ファイル名を設定
			if(archive){
				targetFileNameList.add(createFileName(monitorId, facilityId, fileId, "zip"));
			}else{
				for(String Id : targetFacilityIdList){
					targetFileNameList.add(createFileName(monitorId, Id, fileId, "csv"));
				}
			}

			// 出力ファイル作成スレッドを実行して抜ける
			ExportCollectedDataFile exdf = new ExportCollectedDataFile();
			CreatePerfFileTask task = exdf.new CreatePerfFileTask(monitorId, facilityId, targetFacilityIdList, header, archive, fileId, userId);
			Thread thread = new Thread(task);
			thread.start();

		} catch (HinemosUnknown e) {
		} catch (Exception e) {
			m_log.warn("create() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}

		// for debug
		if(m_log.isDebugEnabled()){
			for(String fileName : targetFileNameList){
				m_log.debug("target file name = " + fileName);
			}
		}

		return targetFileNameList;
	}

	/**
	 *
	 * @param monitorId
	 * @param targetFacilityId
	 * @param fileId
	 * @param extension
	 * @return
	 */
	private static String createFileName(String monitorId, String targetFacilityId, String fileId, String extension){
		return monitorId + "_" + targetFacilityId + "_" + fileId + "." + extension;
	}


	/**
	 * 性能データがない場合の空ファイルを作成する
	 *
	 * @param filepath
	 * @throws HinemosUnknown
	 */
	private static void writeEmptyDataFile(String filepath) throws HinemosUnknown{
		m_log.debug("writeEmptyDataFile() filepath = " + filepath);

		try{
			File file = new File(filepath);

			if (checkBeforeWritefile(file)){
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));

				pw.println("Performance Data has not been collected yet.");

				pw.close();
			}else{
				m_log.info("writeEmptyDataFile() filepath = "
						+ filepath + " write error");
				throw new HinemosUnknown("filepath = " + filepath + " write error");
			}
		}catch(IOException e){
			m_log.warn("writeEmptyDataFile() filepath = "
					+ filepath + " write error : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown("filepath = "
					+ filepath + " write error", e);
		}
	}

	/**
	 *
	 * @param monitorId
	 * @param facilityId
	 * @param filepath
	 * @param dataSettings
	 * @return
	 * @throws HinemosUnknown
	 */
	private static void writeFile(String monitorId, String facilityId, String filepath,
			PerformanceDataSettings dataSettings){
		m_log.debug("writeFile() monitorId = " + monitorId + ", facilityId = " + facilityId + ", filepath = " + filepath);

		PrintWriter pw = null;

		String exportEncode = HinemosPropertyUtil.getHinemosPropertyStr(
				"performance.export.encode", "UTF-8");
		
		String exportLineSeparator = "\r\n";
		String exportLineSeparatorStr = HinemosPropertyUtil.getHinemosPropertyStr(
				"performance.export.line.separator", "CRLF");
		
		if ("CRLF".equals(exportLineSeparatorStr)) {
			exportLineSeparator = "\r\n";
		} else if ("LF".equals(exportLineSeparatorStr)) {
			exportLineSeparator = "\n";
		} else if ("CR".equals(exportLineSeparatorStr)) {
			exportLineSeparator = "\r";
		}
		
		try{
			pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filepath, true),exportEncode)));

			ArrayList<CollectorItemParentInfo> itemInfoList = dataSettings.getItemCodeList();
			int columnNum = itemInfoList.size();
			Double[] values = new Double[columnNum];
			boolean[] nans = new boolean[columnNum];
			for(int i = 0; i < columnNum; i++){
				values[i] = null;
				nans[i] = false;
			}

			long lastDate = 0;
			JpaTransactionManager jtm = null;

			try{
				jtm = new JpaTransactionManager();
				jtm.begin();
				int exportFetchSize = HinemosPropertyUtil.getHinemosPropertyNum("performance.export.fetchsize", 1000);

				List<CalculatedDataEntity> calculatedDataEntityList
				= QueryUtil.getCalculatedDataByCollectoridFacilityidDateTime(monitorId,
						facilityId,
						new Timestamp(dataSettings.getLatestDate()),
						exportFetchSize);
				m_log.debug("writeFile() set statement fetchsize = " + exportFetchSize);

				// 配列への性能値の格納
				for(CalculatedDataEntity calculatedDataEntity : calculatedDataEntityList) {

					// ファイル書き込み

					CalculatedDataEntityPK pk = calculatedDataEntity.getId();
					// for debug
					m_log.debug("writeFile() date = " + lastDate
							+ ", itemCode = " + pk.getItemCode()
							+ ", displayName = " + pk.getDisplayName());

					// 収集時刻が変わった契機で1行writeする。
					if(lastDate != 0 && lastDate != pk.getDateTime().getTime()){
						m_log.debug("writeFile() lastDate = " + lastDate);
						// write
						StringBuffer bs = new StringBuffer((new Date(lastDate)).toString());
						for(int i = 0; i < columnNum; i++){
							if(nans[i]) {
								if(values[i] == null) {
									bs.append("," + NOT_A_NUMBER);
								} else {
									bs.append("," + values[i]);
								}
							} else {
								bs.append("," + NOT_EXISTS);
							}
						}
						m_log.debug("writeFile() write line = " + bs.toString());
						pw.print(bs.toString());
						pw.print(exportLineSeparator);
						pw.flush();
						// reset
						for(int i = 0; i < columnNum; i++){
							values[i] = null;
							nans[i] = false;
						}
					}
					lastDate = pk.getDateTime().getTime();

					// dataInfoがどの列のデータかを判断し、配列にセットする
					for (int i = 0; i < itemInfoList.size(); i++) {
						// i列目のデータか
						if(pk.getItemCode() != null && pk.getItemCode().equals(itemInfoList.get(i).getItemCode()) &&
								pk.getDisplayName() != null && pk.getDisplayName().equals(itemInfoList.get(i).getDisplayName())){
							values[i] = calculatedDataEntity.getValue();
							nans[i] = true;
							break;
						}
					}

				}

				// 最後のデータをフラッシュする
				if(dataSettings.getLatestDate().longValue() == lastDate){
					m_log.debug("writeFile() write last data : lastDate = " + lastDate);
					// write
					StringBuffer bs = new StringBuffer((new Date(lastDate)).toString());
					for(int i = 0; i < columnNum; i++){
						if(nans[i]) {
							if(values[i] == null) {
								bs.append("," + NOT_A_NUMBER);
							} else {
								bs.append("," + values[i]);
							}
						} else {
							bs.append("," + NOT_EXISTS);
						}
					}
					m_log.debug("writeFile() write line = " + bs.toString());
					pw.print(bs.toString());
					pw.print(exportLineSeparator);
					pw.flush();
				}
				jtm.commit();
			} catch (Exception e) {
				m_log.warn("writeFile() write csv file by SQLException monitorId = " + monitorId + ", facilityId = " + facilityId + ", filepath = " + filepath, e);
				jtm.rollback();
			} finally {
				jtm.close();
			}

		} catch (UnsupportedEncodingException e) {
			m_log.warn("writeFile() Unsupported Encoding : exportEncode = " + exportEncode + ". Please modify performance.export.encode : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} catch (FileNotFoundException e) {
			m_log.warn("File does not exists! filepath = " + filepath + " : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			if(pw != null){
				pw.close();
			}
		}

	}

	/**
	 * 指定したファイルにItemCodeのリスト(itemInfoList)のヘッダ情報を出力する。
	 *
	 * @param filepath
	 * @param itemInfoList
	 */
	private static void writeHeader(String filepath, String monitorId,
			String facilityId, long startDate, long lastDate,
			PerformanceDataSettings dataSettings)
					throws HinemosUnknown {

		PrintWriter pw = null;
		try {
			File file = new File(filepath);

			if (checkBeforeWritefile(file)) {
				String exportEncode = HinemosPropertyUtil.getHinemosPropertyStr(
						"performance.export.encode", "UTF-8");
				
				String exportLineSeparator = "\r\n";
				String exportLineSeparatorStr = HinemosPropertyUtil.getHinemosPropertyStr(
						"performance.export.line.separator", "CRLF");
				
				if ("CRLF".equals(exportLineSeparatorStr)) {
					exportLineSeparator = "\r\n";
				} else if ("LF".equals(exportLineSeparatorStr)) {
					exportLineSeparator = "\n";
				} else if ("CR".equals(exportLineSeparatorStr)) {
					exportLineSeparator = "\r";
				}

				pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true),exportEncode)));

				// File Header
				pw.print(Messages.getString("monitor.id") + " : " + monitorId + exportLineSeparator);
				pw.print(Messages.getString("facility.id") + " : " + facilityId + exportLineSeparator);
				pw.print(Messages.getString("collection.oldest.date") + " : " + new Date(startDate) + exportLineSeparator);
				pw.print(Messages.getString("collection.latest.date") + " : " + new Date(lastDate) + exportLineSeparator);
				pw.print(exportLineSeparator);

				// Column Header
				// date
				pw.print(Messages.getString("timestamp"));

				// itemCode毎に出力する
				for (CollectorItemParentInfo info : dataSettings.getItemCodeList()) {

					String itemName = dataSettings.getItemName(info.getItemCode());
					if(m_log.isDebugEnabled()){
						HashMap<String, String> itemNameMap = dataSettings.getItemNameMap();
						if(itemNameMap != null){
							for(String key : itemNameMap.keySet()){
								m_log.debug("writeHeader() itemCode = " + key + ", itemName = " + itemNameMap.get(key));
							}
						}
					}

					if (info.getDisplayName() != null
							&& !"".equals(info.getDisplayName())) {
						itemName = itemName + "[" + info.getDisplayName() + "]";
					}

					pw.print("," + itemName);
				}
				pw.print(exportLineSeparator); // 改行

			} else {
				m_log.info("writeHeader() filepath = " + filepath + " write error");
				throw new HinemosUnknown("filepath = " + filepath + " write error");
			}
		} catch (IOException e) {
			m_log.warn("writeHeader() filepath = " + filepath + " is io error", e);
			throw new HinemosUnknown("filepath = "
					+ filepath + " is io error", e);

		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	/**
	 * 指定したファイル名のリストを削除する。ディレクトリはperformance.export.dirで指定。
	 *
	 * @param filePathList
	 */
	public static void deleteFile(ArrayList<String> fileNameList)
			throws HinemosUnknown {
		m_log.debug("deleteFile()");

		String exportDirectory = HinemosPropertyUtil.getHinemosPropertyStr(
				"performance.export.dir", "/opt/hinemos/var/export/");

		for (String fileName : fileNameList) {
			String filePath = exportDirectory + fileName;
			m_log.debug("deleteFile() targetFileName = " + fileName);
			m_log.debug("deleteFile() targetFilePath = " + filePath);

			File file = new File(filePath);

			if (file.exists()) {
				file.delete();
				m_log.debug("deleteFile() targetFilePath = " + filePath
						+ " : delete succeed.");
			} else {
				m_log.debug("deleteFile() targetFilePath = " + filePath
						+ " : does not exist.");
			}
		}
	}


	/**
	 * 指定したファイルパスのリストを削除する。ディレクトリチェックは行わない。
	 *
	 * @param filePathList
	 */
	private static void deleteFilePath(ArrayList<String> filePathList) {
		m_log.debug("deleteFilePath()");

		for (String filePath : filePathList) {
			m_log.debug("deleteFilePath() targetFilePath = " + filePath);

			File file = new File(filePath);

			if (file.exists()) {
				m_log.info("Delete Performance Export File. file = " + file.getPath());

				file.delete();
				m_log.debug("deleteFilePath() targetFilePath = " + filePath
						+ " : delete succeed.");
			} else {
				m_log.debug("deleteFilePath() targetFilePath = " + filePath
						+ " : does not exist.");
			}
		}
	}

	/**
	 * ファイルパスをfromからtoにリネームする
	 *
	 * @param from
	 * @param to
	 */
	private static void renameFile(String from , String to) {

		File file = new File(from);
		if(file.exists() && file.canWrite()){
			file.renameTo(new File(to));
		}
	}

	/**
	 * 対象ファイルが存在するかをチェックする
	 *
	 * @param fileName
	 * @return
	 */
	public static boolean isCreatedFile(String fileName) {
		String exportDirectory = HinemosPropertyUtil.getHinemosPropertyStr(
				"performance.export.dir", "/opt/hinemos/var/export/");

		String createFilePath = exportDirectory + fileName;
		File file = new File(createFilePath);

		return file.exists();
	}

	/**
	 * 指定したファイルに書き込み可能かをチェックする。ファイルが存在しない場合は、新規作成を試み、その可否を返す。
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static boolean checkBeforeWritefile(File file) throws IOException {
		if (file.exists()) {
			if (file.isFile() && file.canWrite()) {
				return true;
			}
		} else {
			m_log.info("checkBeforeWritefile() Crate Performance Export File. file = " + file.getPath());
			return file.createNewFile();
		}
		return false;
	}

	/**
	 * ダウンロードするファイルを作成する処理クラス
	 *
	 *
	 */
	private class CreatePerfFileTask implements Runnable {

		private String m_monitorId = "";
		private String m_targetFacilityId = "";
		private ArrayList<String> m_facilityIdList = null;
		private boolean m_header = false;
		private boolean m_archive = false;
		private String m_fileId = "";
		private String m_userId = "";

		/**
		 * デフォルトコンストラクタ
		 *
		 * @param monitorId
		 * @param targetFacilityId
		 * @param facilityIdList
		 * @param header
		 * @param archive
		 * @param fileId
		 * @param userId
		 */
		private CreatePerfFileTask(
				String monitorId,
				String targetFacilityId,
				ArrayList<String> facilityIdList,
				boolean header,
				boolean archive,
				String fileId,
				String userId) {

			this.m_monitorId = monitorId;
			this.m_targetFacilityId = targetFacilityId;
			this.m_facilityIdList = facilityIdList;
			this.m_header = header;
			this.m_archive = archive;
			this.m_fileId = fileId;
			this.m_userId = userId;

			m_log.debug("CreatePerfFileTask Create : monitorId = " + monitorId + ", targetFacilityId = " + targetFacilityId + ", header = " + header + ", archive = " + archive + ", fileId");
		}

		/**
		 * ダウンロードデータの作成
		 */
		@Override
		public void run() {
			m_log.info("CreatePerfFileTask start!");

			boolean success = true;
			ArrayList<String> createFilePathList = new ArrayList<String>();

			try {
				// 新たなスレッドとして開始される場合は、ユーザ情報が格納されていないため、ユーザ情報を改めて格納
				HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, m_userId);
				HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR,
						new AccessControllerBean().isAdministrator());

				PerformanceDataSettings dataSettings
				= new CollectorControllerBean().getPerformanceGraphInfo(m_monitorId);

				String exportDirectory = HinemosPropertyUtil.getHinemosPropertyStr(
						"performance.export.dir", "/opt/hinemos/var/export/");

				////
				// CSVファイルの作成
				////
				if(dataSettings.getOldestDate() == null || dataSettings.getLatestDate() == null){
					// 時刻がない = 収集データがない、空ファイルを作成する

					for(String targetFacilityId : m_facilityIdList){
						m_log.debug("run() Create Empty CSV File at targetFacilityId = " + targetFacilityId);

						String createFilePath = exportDirectory + createFileName(m_monitorId, targetFacilityId, m_fileId, "csv");
						createFilePathList.add(createFilePath);
						writeEmptyDataFile(createFilePath);
					}


				} else{
					// 時刻がある = 収集データがある

					long startDate = dataSettings.getOldestDate();
					long lastDate = dataSettings.getLatestDate();
					m_log.debug("run() export start time = " + startDate
							+ ", lastDate = " + lastDate);

					// 対象のFacilityId毎にデータファイルを作成する
					for (String targetFacilityId : m_facilityIdList) {
						m_log.debug("run() Create CSV File at targetFacilityId = " + targetFacilityId);

						String createTmpFilePath = exportDirectory + createFileName(m_monitorId, targetFacilityId, m_fileId, "tmp");
						String createFilePath = exportDirectory + createFileName(m_monitorId, targetFacilityId, m_fileId, "csv");

						// ヘッダの追加
						if (m_header) {
							writeHeader(createTmpFilePath, m_monitorId, targetFacilityId,
									startDate, lastDate, dataSettings);
						}

						// データの追加
						writeFile(m_monitorId, targetFacilityId, createTmpFilePath, dataSettings);

						// 完成したらファイル名を変更
						createFilePathList.add(createFilePath);
						renameFile(createTmpFilePath, createFilePath);
					}
				}

				////
				// 圧縮(有効の場合)
				////
				if (m_archive) {
					// 戻り値として渡すパスの設定
					String createTmpArchiveFilePath = exportDirectory + createFileName(m_monitorId, m_targetFacilityId, m_fileId, "tmp");
					String createArchiveFilePath = exportDirectory + createFileName(m_monitorId, m_targetFacilityId, m_fileId, "zip");
					m_log.debug("run() create archive csv file : " + createArchiveFilePath);

					// 圧縮
					ZipCompresser
					.archive(createFilePathList, createTmpArchiveFilePath);

					// 元ファイルの削除
					deleteFilePath(createFilePathList);
					createFilePathList.clear();

					// 完成したらファイル名を変更
					createFilePathList.add(createArchiveFilePath);
					renameFile(createTmpArchiveFilePath, createArchiveFilePath);
				}

			} catch (HinemosUnknown e) {
				m_log.info("run() " + e.getMessage(), e);
				success = false;
			} catch (Exception e) {
				m_log.info("run() " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				success = false;
			} finally {
				if(!success){
					m_log.debug("ExportCollectedDataFile.run() : Since processing went wrong, a file is deleted.");
					deleteFilePath(createFilePathList);
				}
			}

			m_log.info("run() CreatePerfFileTask finish!");
		}

	}
}
