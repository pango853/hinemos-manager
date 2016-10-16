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

package com.clustercontrol.notify.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.util.SendMail;
import com.clustercontrol.plugin.api.AsyncTaskFactory;

public class NotifyMailTaskFactory implements AsyncTaskFactory {

	public static final Log log = LogFactory.getLog(NotifyMailTaskFactory.class);

	private static final SendMail _sendMail = new SendMail();
	
	private static SendMail _reportingSendMail = null;

	@Override
	public Runnable createTask(Object param) {
		return new NotifyMailTask(param);
	}

	public class NotifyMailTask implements Runnable {

		private final NotifyRequestMessage msg;

		public NotifyMailTask(Object param) {
			if (param instanceof NotifyRequestMessage) {
				msg = (NotifyRequestMessage)param;
			} else {
				msg = null;
			}
		}

		@Override
		public void run() {

			if (msg == null) {
				log.warn("message is not assigned.");
				return;
			}
			log.debug("run() message : " + msg);

			JpaTransactionManager jtm = null;

			try {
				jtm = new JpaTransactionManager();
				jtm.begin();

				// レポーティングオプション用のメール処理への分岐
				if(msg.getOutputInfo().getPluginId().equals(HinemosModuleConstant.REPORTING) 
						&& msg.getOutputInfo().getSubKey() != null) {
					
					// 最初の1回だけ実行
					if(_reportingSendMail == null) {
						
						String sendMailClass = "com.clustercontrol.notify.util.ReportingSendMail";
						try {
							Class<? extends SendMail> clazz = (Class<? extends SendMail>) Class.forName(sendMailClass);
							_reportingSendMail = clazz.newInstance();
							log.info("load " + sendMailClass + ".");
						} catch (Exception e) {
							throw new HinemosUnknown(e.getMessage(), e);
						}
					}
					
					log.debug("_reportingSendMail.notify");
					_reportingSendMail.notify(msg);
				} else {
					log.debug("_sendMail.notify");
					_sendMail.notify(msg);
				}
				
				jtm.commit();
			} catch (Exception e) {
				jtm.rollback();
				log.warn("asynchronous task failure.", e);
			} finally {
				jtm.close();
			}

		}

		@Override
		public String toString() {
			if (msg == null) {
				return this.getClass().getSimpleName() + "[NotifyRequestMessage = null]";
			} else {
				return this.getClass().getSimpleName() + "[NotifyRequestMessage = " + msg
						+ "[" + msg.getOutputInfo() + "]]";
			}

		}

	}

}
