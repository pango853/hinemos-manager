package com.clustercontrol.monitor.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;


/**
 * 
 * ステータス情報を保持するDTOです。<BR>
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class StatusDataInfo implements Serializable {

	private static final long serialVersionUID = 5615298892458986612L;
	private String monitorId = null;
	private String monitorDetailId = null;
	private String pluginId = null;
	private String facilityId = null;
	private String application = null;
	private Long expirationDate = null;
	private Integer expirationFlg = null;
	private Long generationDate = null;
	private String message = null;
	private String messageId = null;
	private Long outputDate = null;
	private Integer priority = null;
	private String facilityPath = null;
	private String ownerRoleId = null;

	public StatusDataInfo() {
		super();
	}

	public StatusDataInfo(String monitorId,
			String pluginId, String monitorDetailId, String facilityId,
			String application, Long expirationDate,
			Integer expirationFlg, Long generationDate,
			String message, String messageId,
			Long outputDate, Integer priority,
			String ownerRoleId) {
		setMonitorId(monitorId);
		setMonitorDetailId(monitorDetailId);
		setPluginId(pluginId);
		setFacilityId(facilityId);
		setApplication(application);
		setExpirationDate(expirationDate);
		setExpirationFlg(expirationFlg);
		setGenerationDate(generationDate);
		setMessage(message);
		setMessageId(messageId);
		setOutputDate(outputDate);
		setPriority(priority);
		setOwnerRoleId(ownerRoleId);
	}

	public StatusDataInfo(StatusDataInfo otherData) {
		setMonitorId(otherData.getMonitorId());
		setMonitorDetailId(otherData.getMonitorDetailId());
		setPluginId(otherData.getPluginId());
		setFacilityId(otherData.getFacilityId());
		setApplication(otherData.getApplication());
		setExpirationDate(otherData.getExpirationDate());
		setExpirationFlg(otherData.getExpirationFlg());
		setGenerationDate(otherData.getGenerationDate());
		setMessage(otherData.getMessage());
		setMessageId(otherData.getMessageId());
		setOutputDate(otherData.getOutputDate());
		setPriority(otherData.getPriority());
		setOwnerRoleId(otherData.getOwnerRoleId());
	}

	public String getMonitorId() {
		return this.monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getMonitorDetailId() {
		return monitorDetailId;
	}

	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	public String getPluginId() {
		return this.pluginId;
	}

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	public String getFacilityId() {
		return this.facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getApplication() {
		return this.application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public Long getExpirationDate() {
		return this.expirationDate;
	}

	public void setExpirationDate(Long expirationDate) {
		this.expirationDate = expirationDate;
	}

	public Integer getExpirationFlg() {
		return this.expirationFlg;
	}

	public void setExpirationFlg(Integer expirationFlg) {
		this.expirationFlg = expirationFlg;
	}

	public Long getGenerationDate() {
		return this.generationDate;
	}

	public void setGenerationDate(Long generationDate) {
		this.generationDate = generationDate;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessageId() {
		return this.messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public Long getOutputDate() {
		return this.outputDate;
	}

	public void setOutputDate(Long outputDate) {
		this.outputDate = outputDate;
	}

	public Integer getPriority() {
		return this.priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer("{");

		str.append("monitorId=" + getMonitorId() + " " + "monitorDetailId=" + getMonitorDetailId() + " " + "pluginId="
				+ getPluginId() + " " + "facilityId=" + getFacilityId() + " "
				+ "application=" + getApplication() + " " + "expirationDate="
				+ getExpirationDate() + " " + "expirationFlg="
				+ getExpirationFlg() + " " + "generationDate="
				+ getGenerationDate() + " " + "message=" + getMessage() + " "
				+ "messageId=" + getMessageId() + " " + "outputDate="
				+ getOutputDate() + " " + "priority=" + getPriority()
				+ " " + "ownerRoleID=" + getOwnerRoleId());
		str.append('}');

		return (str.toString());
	}

	@Override
	public boolean equals(Object pOther) {
		if (pOther instanceof StatusDataInfo) {
			StatusDataInfo lTest = (StatusDataInfo) pOther;
			boolean lEquals = true;

			if (this.monitorId == null) {
				lEquals = lEquals && (lTest.monitorId == null);
			} else {
				lEquals = lEquals && this.monitorId.equals(lTest.monitorId);
			}
			if (this.monitorDetailId == null) {
				lEquals = lEquals && (lTest.monitorDetailId == null);
			} else {
				lEquals = lEquals && this.monitorDetailId.equals(lTest.monitorDetailId);
			}
			if (this.pluginId == null) {
				lEquals = lEquals && (lTest.pluginId == null);
			} else {
				lEquals = lEquals && this.pluginId.equals(lTest.pluginId);
			}
			if (this.facilityId == null) {
				lEquals = lEquals && (lTest.facilityId == null);
			} else {
				lEquals = lEquals && this.facilityId.equals(lTest.facilityId);
			}
			if (this.application == null) {
				lEquals = lEquals && (lTest.application == null);
			} else {
				lEquals = lEquals && this.application.equals(lTest.application);
			}
			if (this.expirationDate == null) {
				lEquals = lEquals && (lTest.expirationDate == null);
			} else {
				lEquals = lEquals
						&& this.expirationDate.equals(lTest.expirationDate);
			}
			if (this.expirationFlg == null) {
				lEquals = lEquals && (lTest.expirationFlg == null);
			} else {
				lEquals = lEquals
						&& this.expirationFlg.equals(lTest.expirationFlg);
			}
			if (this.generationDate == null) {
				lEquals = lEquals && (lTest.generationDate == null);
			} else {
				lEquals = lEquals
						&& this.generationDate.equals(lTest.generationDate);
			}
			if (this.message == null) {
				lEquals = lEquals && (lTest.message == null);
			} else {
				lEquals = lEquals && this.message.equals(lTest.message);
			}
			if (this.messageId == null) {
				lEquals = lEquals && (lTest.messageId == null);
			} else {
				lEquals = lEquals && this.messageId.equals(lTest.messageId);
			}
			if (this.outputDate == null) {
				lEquals = lEquals && (lTest.outputDate == null);
			} else {
				lEquals = lEquals && this.outputDate.equals(lTest.outputDate);
			}
			if (this.priority == null) {
				lEquals = lEquals && (lTest.priority == null);
			} else {
				lEquals = lEquals && this.priority.equals(lTest.priority);
			}
			if (this.ownerRoleId == null) {
				lEquals = lEquals && (lTest.ownerRoleId == null);
			} else {
				lEquals = lEquals && this.ownerRoleId.equals(lTest.ownerRoleId);
			}

			return lEquals;
		} else {
			return false;
		}
	}

	/**
	 * ファシリティパスを返します
	 * 
	 * @return 重要度
	 * @ejb.interface-method
	 * 
	 */
	public String getFacilityPath() {
		return facilityPath;
	}

	/**
	 * ファシリティパスを設定します
	 * 
	 * @param facilityPath
	 * @ejb.interface-method
	 * 
	 */
	public void setFacilityPath(String facilityPath) {
		this.facilityPath = facilityPath;
	}

	/**
	 * オーナーロールIDを返します
	 * 
	 * @return オーナーロールID
	 * 
	 */
	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	/**
	 * オーナーロールIDを設定します
	 * 
	 * @param ownerRoleId
	 * 
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}


}
