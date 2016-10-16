package com.clustercontrol.notify.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_notify_event_info database table.
 * 
 */
@Embeddable
public class NotifyEventInfoEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String notifyId;

	public NotifyEventInfoEntityPK() {
	}

	public NotifyEventInfoEntityPK(String notifyId) {
		this.setNotifyId(notifyId);
	}

	@Column(name="notify_id")
	public String getNotifyId() {
		return this.notifyId;
	}
	public void setNotifyId(String notifyId) {
		this.notifyId = notifyId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof NotifyEventInfoEntityPK)) {
			return false;
		}
		NotifyEventInfoEntityPK castOther = (NotifyEventInfoEntityPK)other;
		return
				this.notifyId.equals(castOther.notifyId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.notifyId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"notifyId"
		};
		String[] values = {
				this.notifyId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}