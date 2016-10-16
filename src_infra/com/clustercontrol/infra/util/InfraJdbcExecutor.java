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

package com.clustercontrol.infra.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

import javax.activation.DataHandler;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyInputStream;
import org.postgresql.copy.PGCopyOutputStream;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraFileTooLarge;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;

/**
 * JDBCドライバを用いて、高速にinsertまたはupdateの一括処理を行うクラス
 */
public class InfraJdbcExecutor {
	private static final Log log = LogFactory.getLog(InfraJdbcExecutor.class);
	private static final String MAX_FILE_KEY = "infra.max.file.size";

	/**
	 * クエリを実行する
	 * @param query insertまたはupdate
	 * @throws InfraFileTooLarge 
	 * @throws Exception 
	 */
	public static void insertFileContent(String fileId, DataHandler handler) throws HinemosUnknown, InfraFileTooLarge {
		Connection conn = null;
		long start = System.currentTimeMillis();
		
		JpaTransactionManager tm = null;
		PGCopyOutputStream pgStream = null;
		try {
			tm = new JpaTransactionManager();
			conn = tm.getEntityManager().unwrap(java.sql.Connection.class);
			conn.setAutoCommit(false);
			
			pgStream = new PGCopyOutputStream((PGConnection)conn, 
					"COPY binarydata.cc_infra_file_content(file_id, file_content) FROM STDIN WITH (DELIMITER ',')");
			byte[] fileIdByte = (fileId + ",\\\\x").getBytes();
			pgStream.writeToCopy(fileIdByte, 0, fileIdByte.length);
			
			InputStream is = handler.getInputStream();
			
			byte[] buf = new byte[1024*1024];
			long totalSize = 0;
			while (true) {
				int read = is.read(buf);
				if (read < 0) {
					break;
				}
				totalSize += read;
				int maxSize = HinemosPropertyUtil.getHinemosPropertyNum(MAX_FILE_KEY , 1024 * 1024 * 64); // 64MB
				if (totalSize > maxSize) {
					throw new InfraFileTooLarge(String.format("File size is larger than the limit size(%d)", maxSize));
				}
				
				StringBuffer stringBuffer = new StringBuffer();
				for (int i = 0; i < read; ++i) {
					if ((buf[i] & 0xff) < 0x10) {
						stringBuffer.append("0");
					}
					stringBuffer.append(Integer.toHexString(buf[i] & 0xff));
				}
				
				pgStream.write(stringBuffer.toString().getBytes());
			}
			pgStream.flush();

			if (! tm.isNestedEm()) {
				conn.commit();
			}
		} catch (InfraFileTooLarge e) {
			log.warn(e.getMessage());
			if (pgStream != null) {
				try {
					pgStream.close();
				} catch (IOException e1) {
					log.warn(e1);
				}
			}
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					log.warn(e1);
				}
			}
			throw e;
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			if (pgStream != null) {
				try {
					pgStream.close();
				} catch (IOException e1) {
					log.warn(e1);
				}
			}
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					log.warn(e1);
				}
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (pgStream != null) {
				try {
					pgStream.close();
				} catch (IOException e) {
					log.warn(e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				}
			}
			if (tm != null) {
				tm.close();
			}
		}
		long time = System.currentTimeMillis() - start;
		String message = String.format("Execute [insertFileContent]: %dms",time);
		if (time > 3000) {
			log.warn(message);
		} else if (time > 1000) {
			log.info(message);
		} else {
			log.debug(message);
		}
	}

	public static String selectFileContent(String fileId, String fileName) throws HinemosUnknown {
		Connection conn = null;
		long start = System.currentTimeMillis();
		
		JpaTransactionManager tm = null;
		PGCopyInputStream pgStream = null;
		OutputStream fos = null;
		try {
			tm = new JpaTransactionManager();
			tm.begin();
			conn = tm.getEntityManager().unwrap(java.sql.Connection.class);
			conn.setAutoCommit(false);
			
			String exportDirectory = HinemosPropertyUtil.getHinemosPropertyStr(
					"infra.export.dir", "/opt/hinemos/var/export/");
			String filepath = exportDirectory + "/" + fileName;
			
			pgStream = new PGCopyInputStream((PGConnection)conn, 
					"COPY (select file_content from binarydata.cc_infra_file_content where file_id = '" + fileId +"') TO STDOUT");
			fos = Files.newOutputStream(Paths.get(filepath));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte buf[] = new byte[1024*1024];
			
			pgStream.read();pgStream.read();pgStream.read(); // 最初の"\\x"を破棄
			while (true) {
				int read = pgStream.read(buf);
				if (read <= 0) {
					break;
				}
				if (pgStream.available() == 0) {
					baos.write(buf, 0, read-1); // 最後の改行コードを破棄
				} else {
					baos.write(buf, 0, read);
				}
				String str = baos.toString();
				byte[] result = DatatypeConverter.parseHexBinary(str);
				fos.write(result);
				baos.reset();
			}
			
			
			if (! tm.isNestedEm()) {
				conn.commit();
			}
			tm.commit();
			
			long time = System.currentTimeMillis() - start;
			String message = String.format("Execute [selectFileContent]: %dms",time);
			if (time > 3000) {
				log.warn(message);
			} else if (time > 1000) {
				log.info(message);
			} else {
				log.debug(message);
			}
			
			return filepath;
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					log.warn(e1);
				}
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					log.warn(e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				}
			}
			if (pgStream != null) {
				try {
					pgStream.close();
				} catch (IOException e) {
					log.warn(e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				}
			}
			if (tm != null) {
				tm.close();
			}
		}
	}
}
