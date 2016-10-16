/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.bean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.infra.model.FileTransferModuleInfoEntity;
import com.clustercontrol.infra.model.FileTransferVariableInfoEntity;
import com.clustercontrol.infra.model.InfraFileEntity;
import com.clustercontrol.infra.model.InfraManagementInfoEntity;
import com.clustercontrol.infra.util.InfraJdbcExecutor;
import com.clustercontrol.infra.util.JschUtil;
import com.clustercontrol.infra.util.WinRMUtil;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.repository.bean.NodeInfo;
import com.clustercontrol.repository.util.RepositoryUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.StringBinder;

@XmlType(namespace = "http://infra.ws.clustercontrol.com")
public class FileTransferModuleInfo extends InfraModuleInfo<FileTransferModuleInfoEntity> {

	private static Log m_log = LogFactory.getLog( FileTransferModuleInfo.class );
	
	public static String SEPARATOR = File.separator;
	private String destPath;
	private int sendMethodType;
	private String destOwner;
	private String destAttribute;
	private boolean backupIfExistFlg;
	
	private String fileId;
	private List<FileTransferVariableInfo> fileTransferVariableList = new ArrayList<>();
	
	public FileTransferModuleInfo() {
	}
	
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	
	public String getDestPath() {
		return destPath;
	}
	public void setDestPath(String destPath) {
		this.destPath = destPath;
	}
	
	public int getSendMethodType() {
		return sendMethodType;
	}
	public void setSendMethodType(int sendMethodType) {
		this.sendMethodType = sendMethodType;
	}

	public String getDestOwner() {
		return destOwner;
	}
	public void setDestOwner(String destOwner) {
		this.destOwner = destOwner;
	}
	
	public String getDestAttribute() {
		return destAttribute;
	}
	public void setDestAttribute(String destAttribute) {
		this.destAttribute = destAttribute;
	}
	
	public List<FileTransferVariableInfo> getFileTransferVariableList() {
		return this.fileTransferVariableList;
	}
	public void setFileTransferVariableList(List<FileTransferVariableInfo> fileTransferPatternList) {
		this.fileTransferVariableList = fileTransferPatternList;
	}

	public boolean isBackupIfExistFlg() {
		return backupIfExistFlg;
	}
	public void setBackupIfExistFlg(boolean backupIfExistFlg) {
		this.backupIfExistFlg = backupIfExistFlg;
	}

	@Override
	protected Class<FileTransferModuleInfoEntity> getEntityClass() {
		return FileTransferModuleInfoEntity.class;
	}

	@Override
	public boolean canPrecheck(InfraManagementInfo management, NodeInfo node, AccessInfo access, String sessionId) throws HinemosUnknown, InvalidUserPass {
		return this.isPrecheckFlg();
	}

	@Override
	public ModuleNodeResult run(InfraManagementInfo management, NodeInfo node, AccessInfo access, String sessionId, String account) throws HinemosUnknown, InvalidUserPass {
		String postfixStr = node.getFacilityId();
		String infraDirectory = HinemosPropertyUtil.getHinemosPropertyStr("infra.transfer.dir", "/opt/hinemos/var/infra") + SEPARATOR;
		InfraFileEntity fileEntity = getInfraFileEntity();
		String fileName = fileEntity.getFileName();
		String srcDir = infraDirectory + SEPARATOR;
		String srcFile = fileName;
		
		File orgFile = new File(createTempFilePath(sessionId));

		// 一時ファイルの作成(文字列を置換する)
		srcDir += "send" + SEPARATOR;
		srcFile += "." + postfixStr;
		try {
			createFile(orgFile, srcDir + srcFile, fileTransferVariableList, node);
			return send(
					node.getFacilityId(),
					node.getAvailableIpAddress(),
					node.getWinrmProtocol(),
					access,
					srcDir,
					srcFile,
					getDestPath(),
					fileName,
					getDestOwner(),
					getDestAttribute(),
					account
					);
		} finally {
			try {
				Files.delete(Paths.get(srcDir, srcFile));
			} catch (IOException e) {
			}
		}
	}
	
	@Override
	public ModuleNodeResult check(InfraManagementInfo management, NodeInfo node, AccessInfo access, String sessionId, boolean verbose) throws HinemosUnknown, InvalidUserPass {
		ModuleNodeResult ret = new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, "failed checking");
		ret.setFacilityId(node.getFacilityId());

		//// sendフォルダにファイルを生成
		String postfixStr = node.getFacilityId();
		String infraDirectory = HinemosPropertyUtil.getHinemosPropertyStr("infra.transfer.dir", "/opt/hinemos/var/infra") + SEPARATOR;
		InfraFileEntity fileEntity = getInfraFileEntity();
		String fileName = fileEntity.getFileName();
		String srcDir = infraDirectory + SEPARATOR;
		String srcFile = fileName;
		String sendMd5 = null;
		File file = null;
		
		File orgFile = new File(createTempFilePath(sessionId));
		long fileSize = orgFile.length();
		String INFRA_TRANSFER_FILESIZE = "infra.transfer.filesize";
		boolean isDiscarded = false;

		///// New File /////
		// 20481より大きい場合はファイルの中身を比較表示できません。MD5は比較できます。
		// 20481という数字は、クライアントで利用している比較ライブラリ(mergely)に依存しています。
		long maxFilesize = HinemosPropertyUtil.getHinemosPropertyNum(INFRA_TRANSFER_FILESIZE, 20481);
		// 一時ファイルの作成(文字列を置換する)
		srcDir += "send" + SEPARATOR;
		srcFile += "." + postfixStr;

		createFile(orgFile, srcDir + srcFile, fileTransferVariableList, node);
		file = new File(srcDir + srcFile);
		if(file.exists()) {
			FileDataSource source = new FileDataSource(file);
			DataHandler handler = new DataHandler(source);
			ret.setNewFilename(fileName);
			if (fileSize < maxFilesize) {
				ret.setNewFile(handler);
			} else {
				isDiscarded = true;
			}
			sendMd5 = getCheckSum(srcDir + srcFile);
		} else {
			// sendは必ず存在するはずなので、ここには到達しないはず。
			m_log.warn("check : file not found (send)[" + file.getAbsolutePath() + "]");
		}
		
		/*
		 * verbose=true
		 * 		Hinemosクライアントよりチェックをクリックして、差分表示を見たいとき
		 * 		→ファイル配布先から古いファイルを持ってくる必要あり。
		 * 		→持ってきてからこちらでMD5をチェックする。
		 * verbose=false
		 * 		Hinemosクライアントよりチェックをクリックして、差分表示を見ないとき(モジュールビューでOK,NGを更新したいとき)
		 * 		Hinemosクライアントより実行をクリックして、ファイル配布前にMD5にチェックをしたとき
		 * 		→ファイル配布先から古いファイルを持ってこない。
		 * 		→ファイル配布先でMD5をチェックする。
		 */
		m_log.info("managementId=" + management.getManagementId() + ", fileId=" + fileId + ", verbose=" + verbose);
		if (!verbose) {
			return isSameMd5(
							node.getFacilityId(),
							node.getAvailableIpAddress(),
							node.getWinrmProtocol(),
							access,
							srcDir,
							srcFile,
							getDestPath(),
							fileName
							);
		} else {
			///// Old File /////
			// recvフォルダにファイルを生成
			srcDir = infraDirectory + SEPARATOR + "recv" + SEPARATOR;
			ModuleNodeResult ret2 = recv(
				node.getFacilityId(),
				node.getAvailableIpAddress(),
				node.getWinrmProtocol(),
				access,
				getDestPath(),
				fileName,
				srcDir,
				srcFile,
				getDestOwner(),
				getDestAttribute()
				);
			if (ret2.getResult() == OkNgConstant.TYPE_NG) {
				return ret2;
			}
			
			String recvMd5 = null;
			file = new File(srcDir + srcFile);
			if(file.exists()) {
				FileDataSource source = new FileDataSource(file);
				DataHandler handler = new DataHandler(source);
				ret.setOldFilename(fileName);
				if (file.length() < maxFilesize) {
					ret.setOldFile(handler);
				} else {
					isDiscarded = true;
				}
				recvMd5 = getCheckSum(srcDir + srcFile);
			} else {
				m_log.info("check : file not found (recv)[" + file.getAbsolutePath() + "]");
			}
			
			ret.setFileDiscarded(isDiscarded);
			
			//// 中身の比較
			if (sendMd5 != null && recvMd5 != null) {
				if (sendMd5.equals(recvMd5)) {
					ret.setResult(OkNgConstant.TYPE_OK);
					ret.setMessage("equal file. MD5=" + sendMd5);
				} else {
					ret.setMessage("not equal file. MD5(new)=" + sendMd5 + ", MD5(old)=" + recvMd5);
				}
			} else if (sendMd5 == null && recvMd5 == null) {
				ret.setMessage("both files are not found..."); // sendは必ず存在するはずなので、ここには到達しないはず。 
			} else if (sendMd5 == null && recvMd5 != null) {
				ret.setMessage("new file is not found..."); // sendは必ず存在するはずなので、ここには到達しないはず。 
			} else if (sendMd5 != null && recvMd5 == null) {
				ret.setMessage("old file is not found."); 
			}
			return ret;
		}
	}
	
	private static Object replaceFileLock = new Object();
	
	// runとcheckから呼ばれる
	private static void createFile(File orgFile, String dstFilepath, List<FileTransferVariableInfo> list, NodeInfo node) throws HinemosUnknown {
		ArrayList<FileTransferVariableInfo> list2 = new ArrayList<>();
		for (FileTransferVariableInfo info : list) {
			FileTransferVariableInfo info2 = info.clone();
			
			String str = info2.getValue();
			
			Map<String, String> nodeParameter = RepositoryUtil.createNodeParameter(node);
			StringBinder strbinder = new StringBinder(nodeParameter);
			m_log.debug("replaceNodeVariable() before : " + str);
			str = strbinder.bindParam(str);
			m_log.debug("replaceNodeVariable() after : " + str);
			info2.setValue( str );
			list2.add(info2); 
		}
		synchronized (replaceFileLock) {
			replaceFile(orgFile, dstFilepath, list2);
		}
	}
	
	
	/**
	 * ファイルの中身を書き換えて、新しいファイル(dstFilepath)を作成する。
	 * メモリあふれを防ぐために、このメソッドの呼び出し時は、synchronizedすること。
	 * @param srcFilepath
	 * @param dstFilepath
	 * @param list
	 * @return
	 * @throws HinemosUnknown 
	 */
	private static void replaceFile(File orgFile, String dstFilepath, List<FileTransferVariableInfo> list) throws HinemosUnknown {
		OutputStream os = null;
		FileInputStream fis = null;
		m_log.info("replaceFile : " + dstFilepath + ", os.size=" + orgFile.length());
		try {
			fis = new FileInputStream(orgFile);
			os = Files.newOutputStream(Paths.get(dstFilepath));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
			byte[] buf = new byte[1024*1024];
			while (true) {
				int read = fis.read(buf);
				if (read < 0) {
					break;
				}
				for (int i = 0; i < read; ++i) {
					byte b = buf[i];
					if (b == '\r' || b == '\n') {
						if (bos.size() > 0) {
							byte[] byteArray = bos.toByteArray();
							bos.reset();
		
							//replace
							for (FileTransferVariableInfo info : list) {
								byteArray = replace(byteArray, info.getName(), info.getValue());
							}
							bos1.write(byteArray);
						}
	
						//write \r or \n
						bos1.write(b);
					} else {
						bos.write(b);
					}
				}
				if(bos1.size() > 0) {
					os.write(bos1.toByteArray());
					bos1.reset();
				}
				if(bos.size() > 0) {
					byte[] byteArray = bos.toByteArray();
					for (FileTransferVariableInfo info : list) {
						byteArray = replace(byteArray, info.getName(), info.getValue());
					}
					os.write(byteArray);
					bos.reset();
				}
			}
		} catch (IOException e) {
			HinemosUnknown exception = new HinemosUnknown("createFile " + e.getClass().getName() + ", " + e.getMessage(), e);
			m_log.warn(exception.getMessage(), e);
			throw exception;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	private static byte[] replace(byte[] byteArray, String name, String value) throws IOException {
		ByteArrayOutputStream bos = null;
		try {
			bos = new ByteArrayOutputStream();
			byte[] nameByteArray = name.getBytes();
			int i= 0;
			while (i <= byteArray.length - nameByteArray.length) {
				if (compareByteArray(byteArray, i, nameByteArray)) {
					bos.write(value.getBytes());
					i += nameByteArray.length;
				} else {
					bos.write(byteArray[i]);
					i++;
				}
			}
			if (i < byteArray.length) {
				bos.write(byteArray, i, byteArray.length - i);
			}
		
			return bos.toByteArray();
		} catch (IOException e) {
			throw e;
		} finally {
			bos.close();
		}
	}

	private static boolean compareByteArray(byte[] byteArray1, int byteArray1Offset, byte[] byteArray2) {
		if (byteArray1.length - byteArray1Offset < byteArray2.length) {
			return false;
		}
		
		for (int i = 0; i < byteArray2.length; i++) {
			if (byteArray1[byteArray1Offset + i] != byteArray2[i]) {
				return false;
			}
		}
		
		return true;
	}
	
	private ModuleNodeResult send(String facilityId, String host, String protocol, AccessInfo access,
			String srcDir, String srcFile, String dstDir, String dstFile, String owner, String perm, String account) {
		ModuleNodeResult ret = null;
		
		long start = System.currentTimeMillis();
		switch (getSendMethodType()) {
		case SendMethodConstant.TYPE_SCP:
			ret = JschUtil.sendFile(access.getSshUser(), access.getSshPassword(), host, access.getSshPort(), access.getSshTimeout(),
					srcDir, srcFile, dstDir, dstFile,
					owner, perm, isBackupIfExistFlg(), access.getSshPrivateKeyFilepath(), access.getSshPrivateKeyPassphrase());
			break;
		case SendMethodConstant.TYPE_WINRM:
			ret = WinRMUtil.sendFile(access.getWinRmUser(), access.getWinRmPassword(), host, access.getWinRmPort(), protocol,
					srcDir, srcFile, dstDir, dstFile,
					owner, perm, isBackupIfExistFlg(), account);
			break;
		default:
			String msg = String.format("AccessMethodType is invalid. value = %d", getSendMethodType());
			m_log.warn("send : " + msg);
			ret = new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, msg);
		}
		ret.setFacilityId(facilityId);
		
		m_log.info("send facilityId=" + facilityId + ", " + srcDir + "/" + srcFile + " -> " + dstDir + "/" + dstFile +
				", result=" + ret.getResult() +
				", time=" + (System.currentTimeMillis() - start) + "ms" );
		return ret;
	}
	
	private ModuleNodeResult isSameMd5(String facilityId, String host, String protocol, AccessInfo access,
			String srcDir, String srcFile, String dstDir, String dstFile) {
		ModuleNodeResult ret = null;
		switch (getSendMethodType()) {
		case SendMethodConstant.TYPE_SCP:
			ret = JschUtil.isSameMd5(access.getSshUser(), access.getSshPassword(), host, access.getSshPort(), access.getSshTimeout(),
					srcDir, srcFile, dstDir, dstFile, access.getSshPrivateKeyFilepath() , access.getSshPrivateKeyPassphrase());
			break;
		case SendMethodConstant.TYPE_WINRM:
			ret = WinRMUtil.isSameMd5(access.getWinRmUser(), access.getWinRmPassword(), host, access.getWinRmPort(), protocol,
					srcDir, srcFile, dstDir, dstFile);
			break;
		default:
			String msg = String.format("AccessMethodType is invalid. value = %d", getSendMethodType());
			m_log.warn("isSameMd5 : " + msg);
			ret = new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, msg);
		}
		ret.setFacilityId(facilityId);
		return ret;
	}

	private ModuleNodeResult recv(String facilityId, String host, String protocol, AccessInfo access, 
			String srcDir, String srcFile, String dstDir, String dstFile, String owner, String perm) {
		ModuleNodeResult ret = null;
		long start = System.currentTimeMillis();
		switch (getSendMethodType()) {
		case SendMethodConstant.TYPE_SCP:
			ret = JschUtil.recvFile(access.getSshUser(), access.getSshPassword(), host, access.getSshPort(), access.getSshTimeout(),
					srcDir, srcFile, dstDir, dstFile, owner, perm, access.getSshPrivateKeyFilepath(), access.getSshPrivateKeyPassphrase());
			break;
		case SendMethodConstant.TYPE_WINRM:
			ret = WinRMUtil.recvFile(access.getWinRmUser(), access.getWinRmPassword(), host, access.getWinRmPort(), protocol, srcDir, srcFile, dstDir, dstFile, owner, perm);
			break;
		default:
			String msg = String.format("AccessMethodType is invalid. value = %d", getSendMethodType());
			m_log.warn("recv : " + msg);
			ret = new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, msg);
		}
		ret.setFacilityId(facilityId);
		m_log.info("recv facilityId=" + facilityId + ", " + srcDir + "/" + srcFile + " -> " + dstDir + "/" + dstFile +
				", result=" + ret.getResult() +
				", time=" + (System.currentTimeMillis() - start) + "ms" );
		return ret;
	}
		

	
	public static String getCheckSum(String path) {
		String checksum = null;
		try {
			MessageDigest md = MessageDigest.getInstance("md5");
			FileInputStream inputStream = new FileInputStream(path);
			byte[] readData = new byte[256];
			int len;
			while ((len = inputStream.read(readData)) >=0) {
				md.update(readData, 0, len);
			}
			inputStream.close();
			checksum = byte2String(md.digest());
		} catch (Exception e) {
			m_log.warn("getCheckSum error. " + e.getMessage(), e);
		}
		return checksum;
	}
	
	private static String byte2String(byte[] digest) {
		StringBuilder hashString = new StringBuilder();
		for (int i = 0; i < digest.length; i++) {
			int d = digest[i];
			if (d < 0) {//負の値を補正
				d += 256;
			}
			if (d < 16) {//1けたは2けたする
				hashString.append("0");
			}
			hashString.append(Integer.toString(d, 16));//16進数2けたにする
		}
		return hashString.toString();
	}
	
	@Override
	protected void overwriteCounterEntity(InfraManagementInfoEntity management, FileTransferModuleInfoEntity module, HinemosEntityManager em) {
		module.setDestOwner(getDestOwner());
		module.setDestAttribute(getDestAttribute());
		module.setDestPath(getDestPath());
		module.setSendMethodType(getSendMethodType());
		module.setBackupIfExistFlg(ValidConstant.booleanToType(isBackupIfExistFlg()));
		
		module.setInfraFileEntity(getInfraFileEntity());
		List<FileTransferVariableInfo> webVariableList = new ArrayList<FileTransferVariableInfo>(getFileTransferVariableList());
		List<FileTransferVariableInfoEntity> dbVariableList = new ArrayList<FileTransferVariableInfoEntity>(module.getFileTransferVariableInfoEntities());
		
		Iterator<FileTransferVariableInfo> webVariableIter = webVariableList.iterator();
		while (webVariableIter.hasNext()) {
			FileTransferVariableInfo webVariable = webVariableIter.next();
			
			Iterator<FileTransferVariableInfoEntity> dbVariableIter = dbVariableList.iterator();
			while (dbVariableIter.hasNext()) {
				FileTransferVariableInfoEntity dbVariable = dbVariableIter.next();
				if (webVariable.getName().equals(dbVariable.getId().getName())) {
					dbVariable.setValue(webVariable.getValue());
					
					webVariableIter.remove();
					dbVariableIter.remove();
					break;
				}
			}
		}
		
		for (FileTransferVariableInfo webVariable: webVariableList) {
			FileTransferVariableInfoEntity dbVariable = new FileTransferVariableInfoEntity(module, webVariable.getName());
			dbVariable.setValue(webVariable.getValue());
		}
		
		for (FileTransferVariableInfoEntity dbVariable: dbVariableList) {
			module.getFileTransferVariableInfoEntities().remove(dbVariable);
			em.remove(dbVariable);
		}
	}

	private InfraFileEntity getInfraFileEntity() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		return em.find(InfraFileEntity.class, fileId, ObjectPrivilegeMode.MODIFY);
	}

	@Override
	protected void validateSub() throws InvalidSetting, InvalidRole {
		// fileId
		CommonValidator.validateString(Messages.getString("infra.module.placement.file"), getFileId(), false, 0, 256);
		
		// destPath
		CommonValidator.validateString(Messages.getString("infra.module.placement.path"), getDestPath(), false, 0, 1024);
		
		// destOwner
		CommonValidator.validateString(Messages.getString("infra.module.transfer.method.owner"), getDestOwner(), false, 0, 256);
		
		// destAttribute
		CommonValidator.validateString(Messages.getString("infra.module.transfer.method.scp.file.attribute"), getDestAttribute(), false, 0, 64);
		
		// precheckFlg : not backupIfExistFlg
		
		// sendMehodType
		boolean match = false;
		for (int type: SendMethodConstant.getTypeList()) {
			if (type == getSendMethodType()) {
				match = true;
				break;
			}
		}
		if (!match) {
			InvalidSetting e = new InvalidSetting("SendMethodType must be SCP(0) / WinRM(1).");
			Logger.getLogger(this.getClass()).info("validateSub() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		// fileTransferVariableList
		if(getFileTransferVariableList() != null){
			for(FileTransferVariableInfo fileTransferVariableInfo : getFileTransferVariableList()){
				//name
				CommonValidator.validateString(Messages.getString("infra.management.search.word"), fileTransferVariableInfo.getName(), false, 0, 256);
				
				//value
				CommonValidator.validateString(Messages.getString("infra.management.replacement.words"), fileTransferVariableInfo.getValue(), false, 0, 256);
			}
		}
	}

	@Override
	public String getModuleTypeName() {
		return FileTransferModuleInfoEntity.typeName;
	}
	
	@Override
	public void beforeRun(String sessionId) throws HinemosUnknown {
		// 送信するファイルの元になるファイルを作成
		InfraJdbcExecutor.selectFileContent(getFileId(), sessionId + "-" + getModuleId());
	}
	
	@Override
	public void afterRun(String sessionId) {
		// beforeRunで作成したファイルを削除
		File file = new File(createTempFilePath(sessionId));
		file.delete();
	}
	
	private String createTempFilePath(String sessionId) {
		String exportDirectory = HinemosPropertyUtil.getHinemosPropertyStr(
				"infra.export.dir", "/opt/hinemos/var/export/");
		String filepath = exportDirectory + "/" + sessionId + "-" + getModuleId();
		return filepath;
	}
}