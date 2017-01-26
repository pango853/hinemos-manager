package com.clustercontrol.repository.bean;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class NodeNetworkInterfaceInfo extends NodeDeviceInfo {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 7967516048451981411L;

	/** メンバ変数 */
	private java.lang.String nicIpAddress = "";
	private java.lang.String nicMacAddress = "";

	/**
	 * 空のコンストラクタです。setterで要素を追加して下さい。
	 */
	public NodeNetworkInterfaceInfo() {
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
	 * @param nicIpAddress
	 * @param nicMacAddress
	 */
	public NodeNetworkInterfaceInfo(
			java.lang.String deviceType,
			java.lang.Integer deviceIndex,
			java.lang.String deviceName,
			java.lang.String deviceDisplayName,
			java.lang.Integer deviceSize,
			java.lang.String deviceSizeUnit,
			java.lang.String deviceDescription,
			java.lang.String nicIpAddress,
			java.lang.String nicMacAddress){

		super(deviceType, deviceIndex, deviceName, deviceDisplayName, deviceSize, deviceSizeUnit, deviceDescription);
		setNicIpAddress(nicIpAddress);
		setNicMacAddress(nicMacAddress);
	}

	/**
	 * NodeNetworkInterfaceInfoインスタンスのコピーを生成する時に利用します。
	 * @param otherData
	 */
	public NodeNetworkInterfaceInfo ( NodeNetworkInterfaceInfo otherData ) {
		super(otherData.getDeviceType(),
				otherData.getDeviceIndex(),
				otherData.getDeviceName(),
				otherData.getDeviceDisplayName(),
				otherData.getDeviceSize(),
				otherData.getDeviceSizeUnit(),
				otherData.getDeviceDescription());
		setNicIpAddress(otherData.getNicIpAddress());
		setNicMacAddress(otherData.getNicMacAddress());
	}

	// Setter / Getter

	public java.lang.String getNicIpAddress() {
		return this.nicIpAddress;
	}

	public void setNicIpAddress(java.lang.String nicIpAddress) {
		this.nicIpAddress = nicIpAddress;
	}

	public java.lang.String getNicMacAddress() {
		return this.nicMacAddress;
	}

	public void setNicMacAddress(java.lang.String nicMacAddress) {
		this.nicMacAddress = nicMacAddress;
	}

	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer("{");

		str.append(
				"nicIpAddress=" + getNicIpAddress() + " " +
						"nicMacAddress=" + getNicMacAddress());
		str.append('}');

		return(super.toString() + str.toString());
	}

	@Override
	public boolean equals( Object pOther )
	{
		boolean ret ;
		if( pOther instanceof NodeNetworkInterfaceInfo )
		{
			NodeNetworkInterfaceInfo lTest = (NodeNetworkInterfaceInfo) pOther;
			boolean lEquals = true;

			if( this.nicIpAddress == null )
			{
				lEquals = lEquals && ( lTest.nicIpAddress == null );
			}
			else
			{
				lEquals = lEquals && this.nicIpAddress.equals( lTest.nicIpAddress );
			}
			if( this.nicMacAddress == null )
			{
				lEquals = lEquals && ( lTest.nicMacAddress == null );
			}
			else
			{
				lEquals = lEquals && this.nicMacAddress.equals( lTest.nicMacAddress );
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

		result = 37*result + ((this.nicIpAddress != null) ? this.nicIpAddress.hashCode() : 0);

		result = 37*result + ((this.nicMacAddress != null) ? this.nicMacAddress.hashCode() : 0);

		return result;
	}

	@Override
	public NodeNetworkInterfaceInfo clone() {

		NodeNetworkInterfaceInfo cloneInfo = new NodeNetworkInterfaceInfo();
		cloneInfo = (NodeNetworkInterfaceInfo)super.clone();
		cloneInfo.nicIpAddress = this.nicIpAddress;
		cloneInfo.nicMacAddress = this.nicMacAddress;

		return cloneInfo;
	}
}
