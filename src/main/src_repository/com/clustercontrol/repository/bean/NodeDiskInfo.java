package com.clustercontrol.repository.bean;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class NodeDiskInfo extends NodeDeviceInfo {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -3294247788860639239L;

	/** メンバ変数 */
	private java.lang.Integer diskRpm = -1;

	/**
	 * 空のコンストラクタです。setterで要素を追加して下さい。
	 */
	public NodeDiskInfo() {
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
	 * @param diskRpm
	 */
	public NodeDiskInfo(
			java.lang.String deviceType,
			java.lang.Integer deviceIndex,
			java.lang.String deviceName,
			java.lang.String deviceDisplayName,
			java.lang.Integer deviceSize,
			java.lang.String deviceSizeUnit,
			java.lang.String deviceDescription,
			java.lang.Integer diskRpm) {

		super(deviceType, deviceIndex, deviceName, deviceDisplayName, deviceSize, deviceSizeUnit, deviceDescription);
		setDiskRpm(diskRpm);
	}

	/**
	 * NodeDiskInfoインスタンスのコピーを生成する時に利用します。
	 * @param otherData
	 */
	public NodeDiskInfo( NodeDiskInfo otherData ) {
		super(otherData.getDeviceType(),
				otherData.getDeviceIndex(),
				otherData.getDeviceName(),
				otherData.getDeviceDisplayName(),
				otherData.getDeviceSize(),
				otherData.getDeviceSizeUnit(),
				otherData.getDeviceDescription());
		setDiskRpm(otherData.getDiskRpm());
	}

	// Setter / Getter

	public java.lang.Integer getDiskRpm() {
		return diskRpm;
	}

	public void setDiskRpm(java.lang.Integer diskRpm) {
		this.diskRpm = diskRpm;
	}

	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer("{");

		str.append(
				"diskRpm=" + getDiskRpm());
		str.append('}');

		return(super.toString() + str.toString());
	}

	@Override
	public boolean equals( Object pOther )
	{
		boolean ret ;
		if( pOther instanceof NodeDiskInfo )
		{
			NodeDiskInfo lTest = (NodeDiskInfo) pOther;
			boolean lEquals = true;

			if( this.diskRpm == null )
			{
				lEquals = lEquals && ( lTest.diskRpm == null );
			}
			else
			{
				lEquals = lEquals && this.diskRpm.equals( lTest.diskRpm );
			}
			ret = lEquals;
		}
		else
		{
			ret = false;
		}

		return super.equals(pOther) & ret;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();

		result = 37*result + ((this.diskRpm != null) ? this.diskRpm.hashCode() : 0);

		return result;
	}

	@Override
	public NodeDiskInfo clone() {

		NodeDiskInfo cloneInfo = new NodeDiskInfo();
		cloneInfo = (NodeDiskInfo)super.clone();
		cloneInfo.diskRpm = this.diskRpm;

		return cloneInfo;
	}
}
