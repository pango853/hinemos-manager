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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.monitor.bean.EventConfirmConstant;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.OutputEvent;
import com.clustercontrol.plugin.api.AsyncTaskFactory;

public class NotifyEventTaskFactory implements AsyncTaskFactory {

	public static final Log log = LogFactory.getLog(NotifyEventTaskFactory.class);

	private static final OutputEvent _outputStatus = new OutputEvent();

	@Override
	public Runnable createTask(Object param) {
		return new NotifyEventTask(param);
	}

	public class NotifyEventTask implements Runnable {

		private final Object msg;

		public NotifyEventTask(Object param) {
			msg = param;
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

				if (msg instanceof NotifyRequestMessage) {
					_outputStatus.notify((NotifyRequestMessage)msg);
				} else if (msg instanceof OutputBasicInfo) {
					_outputStatus.insertEventLog((OutputBasicInfo)msg, EventConfirmConstant.TYPE_UNCONFIRMED);
				} else if (msg instanceof List) {
					List<NotifyRequestMessage> msgList = (List<NotifyRequestMessage>)msg;
					_outputStatus.notify(msgList);
				} else {
					log.warn("message type is not expected : " + msg.getClass().getName());
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
				if (msg instanceof NotifyRequestMessage) {
					NotifyRequestMessage msg2 = (NotifyRequestMessage)msg;
					return this.getClass().getSimpleName() + "[NotifyRequestMessage = " + msg2
							+ "[" + msg2.getOutputInfo() + "]]";
				} else if (msg instanceof OutputBasicInfo) {
					OutputBasicInfo msg2 = (OutputBasicInfo)msg;
					return this.getClass().getSimpleName() + "[" + msg2 + "]";
				} else {
					return this.getClass().getSimpleName() + "[NotifyRequestMessage = null]";
				}
			}
		}

	}

}
