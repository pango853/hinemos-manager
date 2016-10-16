package com.clustercontrol.repository.bean;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class NodeMemoryInfo extends NodeDeviceInfo {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 3451762777396846659L;

	/** メンバ変数 */

	/**
	 * 空のコンストラクタです。setterで要素を追加して下さい。
	 */
	public NodeMemoryInfo() {
	}

	/**
	 * コンストラクトする際に、値をセットする事ができます。
	 * @param deviceType
	 * @param deviceIndex
	 * @param deviceName
	 * @param deviceDisplayName
	 * @param deviceSize
	 * @param deviceSizeUnit
	 * @param deviceDescription
	 */
	public NodeMemoryInfo(
			java.lang.String deviceType,
			java.lang.Integer deviceIndex,
			java.lang.String deviceName,
			java.lang.String deviceDisplayName,
			java.lang.Integer deviceSize,
			java.lang.String deviceSizeUnit,
			java.lang.String deviceDescription) {

		super(deviceType, deviceIndex, deviceName, deviceDisplayName, deviceSize, deviceSizeUnit, deviceDescription);
	}

	/**
	 * NodeMemoryInfoインスタンスのコピーを生成する時に利用します。
	 * @param otherData
	 */
	public NodeMemoryInfo( NodeCpuInfo otherData ) {
		super(otherData.getDeviceType(),
				otherData.getDeviceIndex(),
				otherData.getDeviceName(),
				otherData.getDeviceDisplayName(),
				otherData.getDeviceSize(),
				otherData.getDeviceSizeUnit(),
				otherData.getDeviceDescription());
	}

	// Setter / Getter

	@Override
	public String toString()
	{
		return super.toString();
	}

	@Override
	public boolean equals( Object pOther ) {
		return super.equals(pOther);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public NodeMemoryInfo clone() {

		NodeMemoryInfo cloneInfo = new NodeMemoryInfo();
		cloneInfo = (NodeMemoryInfo)super.clone();

		return cloneInfo;
	}
}
