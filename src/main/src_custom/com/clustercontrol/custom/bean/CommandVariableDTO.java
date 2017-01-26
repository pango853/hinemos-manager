package com.clustercontrol.custom.bean;

import java.util.Map;

import javax.xml.bind.annotation.XmlType;

/**
 * 情報収集に利用するコマンドに埋め込む変数情報の格納クラス<BR />
 * 
 * @since 4.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class CommandVariableDTO {

	private String facilityId;
	private Map<String, String> variables;

	public CommandVariableDTO() {

	}

	public CommandVariableDTO(String facilityId, Map<String, String> variables) {
		this.facilityId = facilityId;
		this.variables = variables;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getFacilityId() {
		return this.facilityId;
	}

	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}

	public Map<String, String> getVariables() {
		return this.variables;
	}

	@Override
	public String toString() {
		String ret = null;
		String variableStr = null;

		if (variables != null) {
			variableStr = "";
			for (String key : variables.keySet()) {
				variableStr += variableStr.length() == 0 ? "" : ", ";
				variableStr += "[key = " + key + ", value = " + variables.get(key) + "]";
			}
		}

		ret = this.getClass().getCanonicalName() + " [facilityId = " + facilityId
				+ ", variables = (" + variableStr + ")"
				+ "]";

		return ret;
	}

}
