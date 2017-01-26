package com.clustercontrol.repository.bean;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class NodeFilesystemInfo extends NodeDeviceInfo {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -5222766959586815183L;

	/** ファイルシステム種別 */
	private java.lang.String filesystemType = "";

	/**
	 * 空のコンストラクタです。setterで要素を追加して下さい。
	 */
	public NodeFilesystemInfo() {
	}

	public NodeFilesystemInfo(
			java.lang.String deviceType,
			java.lang.Integer deviceIndex,
			java.lang.String deviceName,
			java.lang.String deviceDisplayName,
			java.lang.Integer deviceSize,
			java.lang.String deviceSizeUnit,
			java.lang.String deviceDescription,
			java.lang.String filesystemType) {

		super(deviceType, deviceIndex, deviceName, deviceDisplayName, deviceSize, deviceSizeUnit, deviceDescription);
		setFilesystemType(filesystemType);
	}

	/**
	 * NodeFilesystemInfoインスタンスのコピーを生成する時に利用します。
	 * @param otherData
	 */
	public NodeFilesystemInfo( NodeFilesystemInfo otherData ) {
		super(otherData.getDeviceType(),
				otherData.getDeviceIndex(),
				otherData.getDeviceName(),
				otherData.getDeviceDisplayName(),
				otherData.getDeviceSize(),
				otherData.getDeviceSizeUnit(),
				otherData.getDeviceDescription());
		setFilesystemType(filesystemType);
	}

	// Setter / Getter

	public java.lang.String getFilesystemType() {
		return filesystemType;
	}

	public void setFilesystemType(java.lang.String filesystemType) {
		this.filesystemType = filesystemType;
	}

	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer("{");

		str.append(
				"filesystemType=" + getFilesystemType());
		str.append('}');

		return(super.toString() + str.toString());
	}

	@Override
	public boolean equals( Object pOther )
	{
		boolean ret ;
		if( pOther instanceof NodeFilesystemInfo )
		{
			NodeFilesystemInfo lTest = (NodeFilesystemInfo) pOther;
			boolean lEquals = true;

			if( this.filesystemType == null )
			{
				lEquals = lEquals && ( lTest.filesystemType == null );
			}
			else
			{
				lEquals = lEquals && this.filesystemType.equals( lTest.filesystemType );
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

		result = 37*result + ((this.filesystemType != null) ? this.filesystemType.hashCode() : 0);

		return result;
	}

	@Override
	public NodeFilesystemInfo clone() {

		NodeFilesystemInfo cloneInfo = new NodeFilesystemInfo();
		cloneInfo = (NodeFilesystemInfo)super.clone();
		cloneInfo.filesystemType = this.filesystemType;

		return cloneInfo;
	}
}
