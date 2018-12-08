package com.clustercontrol.repository.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.poller.PollingController;

/**
 * リポジトリ情報変更時に、ノードの管理対象フラグの無効から有効への変化タイミングでの不明通知を抑制するための
 * コントロールを行うためのコールバッククラス
 */
public class NotifySkipControlCallback implements JpaTransactionCallback {

	private static final Log log = LogFactory.getLog(NotifySkipControlCallback.class);
	
	public enum Mode {
		CHANGE,
		REMOVE
	}
	
	private final String facilityId;
	private final Mode mode;
	public NotifySkipControlCallback(String facilityId, Mode mode) {
		super();
		this.facilityId = facilityId;
		this.mode = mode;
	}
	
	@Override
	public void preBegin() {
	}

	@Override
	public void postBegin() {
	}

	@Override
	public void preFlush() {
	}

	@Override
	public void postFlush() {
	}

	@Override
	public void preCommit() {
	}

	@Override
	public void postCommit() {
		switch (mode) {
		case CHANGE:
			PollingController.setLastPollingSkipTime(facilityId);
			if (log.isDebugEnabled()) {
				log.debug("NotifySkipControlCallback() : call PollingController.setPollingSkipTime. facilityId = " + facilityId);
			}
			break;
		case REMOVE:
			PollingController.removeLastPollingSkipTime(facilityId);
			if (log.isDebugEnabled()) {
				log.debug("NotifySkipControlCallback() : call PollingController.removeLastPollingSkipTime. facilityId = " + facilityId);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void preRollback() {
	}

	@Override
	public void postRollback() {
	}

	@Override
	public void preClose() {
	}

	@Override
	public void postClose() {
	}

}
