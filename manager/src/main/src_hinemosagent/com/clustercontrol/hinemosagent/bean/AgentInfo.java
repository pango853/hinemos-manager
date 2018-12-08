package com.clustercontrol.hinemosagent.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import com.clustercontrol.maintenance.util.HinemosPropertyUtil;

public class AgentInfo implements Cloneable, Serializable {
	private String facilityId = "";
	private String hostname = "";
	private ArrayList<String> ipAddressList = new ArrayList<String>();
	private int interval = 0;

	// firstLoginはエージェントの起動時刻なので、エージェント側で入力
	// new Date().getTime()を利用する。
	private long startupTime;
	// lastLoginはマネージャ側で入力
	private long lastLogin;

	public void refreshLastLogin() {
		lastLogin = System.currentTimeMillis();
	}
	public boolean isValid() {
		/*
		 * (interval * monitor.agent.valid.multi + monitor.agent.valid.plus) の時間でgetTopicがない場合は、無効とみなす。
		 */
		int intervalMulti = HinemosPropertyUtil.getHinemosPropertyNum("monitor.agent.valid.multi", 2);
		int intervalPlus = HinemosPropertyUtil.getHinemosPropertyNum("monitor.agent.valid.plus", 10 * 1000); // 10sec
		if (interval * intervalMulti + intervalPlus > System.currentTimeMillis() - lastLogin) {
			return true;
		}
		return false;
	}

	/*
	 * getter/setter
	 */
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public ArrayList<String> getIpAddress() {
		return ipAddressList;
	}
	public void setIpAddress(ArrayList<String> ipAddressList) {
		this.ipAddressList = ipAddressList;
	}
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public long getStartupTime() {
		return startupTime;
	}
	public void setStartupTime(long startupTime) {
		this.startupTime = startupTime;
	}
	public long getLastLogin() {
		return lastLogin;
	}

	@Override
	public String toString() {
		String str = new Date(startupTime) + "," + new Date(lastLogin) + ",(" + interval + ")";
		str += "[";
		if (facilityId != null) {
			str += facilityId;
		}
		str += ",";
		if (hostname != null) {
			str += hostname;
		}
		str += "]";
		for (String ipAddress : ipAddressList) {
			str += ipAddress + ",";
		}
		return str;
	}

	@Override
	public AgentInfo clone() {
		try {
			AgentInfo clone = (AgentInfo) super.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}
}
