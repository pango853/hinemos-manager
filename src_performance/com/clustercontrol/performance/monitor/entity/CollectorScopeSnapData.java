/*
 * Generated by XDoclet - Do not edit!
 */
package com.clustercontrol.performance.monitor.entity;

/**
 * Data object for CollectorScopeSnap.
 * @xdoclet-generated at ${TODAY}
 * @copyright The XDoclet Team
 * @author XDoclet
 * @version 4.0.1
 */
public class CollectorScopeSnapData
extends java.lang.Object
implements java.io.Serializable
{
	private static final long serialVersionUID = -1196126005771158674L;

	private java.lang.String collectorId;
	private java.lang.String facilityId;
	private java.lang.String platformId;
	private java.lang.String subPlatformId;
	private java.lang.String facilityName;
	private java.lang.String facilityType;

	/* begin value object */

	/* end value object */

	public CollectorScopeSnapData()
	{
	}

	public CollectorScopeSnapData( java.lang.String collectorId,java.lang.String facilityId,java.lang.String platformId,java.lang.String subPlatformId,java.lang.String facilityName,java.lang.String facilityType )
	{
		setCollectorId(collectorId);
		setFacilityId(facilityId);
		setPlatformId(platformId);
		setSubPlatformId(subPlatformId);
		setFacilityName(facilityName);
		setFacilityType(facilityType);
	}

	public CollectorScopeSnapData( CollectorScopeSnapData otherData )
	{
		setCollectorId(otherData.getCollectorId());
		setFacilityId(otherData.getFacilityId());
		setPlatformId(otherData.getPlatformId());
		setSubPlatformId(otherData.getSubPlatformId());
		setFacilityName(otherData.getFacilityName());
		setFacilityType(otherData.getFacilityType());

	}

	public com.clustercontrol.performance.monitor.entity.CollectorScopeSnapPK getPrimaryKey() {
		com.clustercontrol.performance.monitor.entity.CollectorScopeSnapPK pk = new com.clustercontrol.performance.monitor.entity.CollectorScopeSnapPK(this.getCollectorId(),this.getFacilityId());
		return pk;
	}

	public java.lang.String getCollectorId()
	{
		return this.collectorId;
	}
	public void setCollectorId( java.lang.String collectorId )
	{
		this.collectorId = collectorId;
	}

	public java.lang.String getFacilityId()
	{
		return this.facilityId;
	}
	public void setFacilityId( java.lang.String facilityId )
	{
		this.facilityId = facilityId;
	}

	public java.lang.String getPlatformId()
	{
		return this.platformId;
	}
	public void setPlatformId( java.lang.String platformId )
	{
		this.platformId = platformId;
	}

	public java.lang.String getSubPlatformId()
	{
		return this.subPlatformId;
	}
	public void setSubPlatformId( java.lang.String subPlatformId )
	{
		this.subPlatformId = subPlatformId;
	}

	public java.lang.String getFacilityName()
	{
		return this.facilityName;
	}
	public void setFacilityName( java.lang.String facilityName )
	{
		this.facilityName = facilityName;
	}

	public java.lang.String getFacilityType()
	{
		return this.facilityType;
	}
	public void setFacilityType( java.lang.String facilityType )
	{
		this.facilityType = facilityType;
	}

	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer("{");

		str.append("collectorId=" + getCollectorId() + " " + "facilityId=" + getFacilityId() + " " + "platformId=" + getPlatformId() + " " + "subPlatformId=" + getSubPlatformId() + " " + "facilityName=" + getFacilityName() + " " + "facilityType=" + getFacilityType());
		str.append('}');

		return(str.toString());
	}

	@Override
	public boolean equals( Object pOther )
	{
		if( pOther instanceof CollectorScopeSnapData )
		{
			CollectorScopeSnapData lTest = (CollectorScopeSnapData) pOther;
			boolean lEquals = true;

			if( this.collectorId == null )
			{
				lEquals = lEquals && ( lTest.collectorId == null );
			}
			else
			{
				lEquals = lEquals && this.collectorId.equals( lTest.collectorId );
			}
			if( this.facilityId == null )
			{
				lEquals = lEquals && ( lTest.facilityId == null );
			}
			else
			{
				lEquals = lEquals && this.facilityId.equals( lTest.facilityId );
			}
			if( this.platformId == null )
			{
				lEquals = lEquals && ( lTest.platformId == null );
			}
			else
			{
				lEquals = lEquals && this.platformId.equals( lTest.platformId );
			}
			if( this.subPlatformId == null )
			{
				lEquals = lEquals && ( lTest.subPlatformId == null );
			}
			else
			{
				lEquals = lEquals && this.subPlatformId.equals( lTest.subPlatformId );
			}
			if( this.facilityName == null )
			{
				lEquals = lEquals && ( lTest.facilityName == null );
			}
			else
			{
				lEquals = lEquals && this.facilityName.equals( lTest.facilityName );
			}
			if( this.facilityType == null )
			{
				lEquals = lEquals && ( lTest.facilityType == null );
			}
			else
			{
				lEquals = lEquals && this.facilityType.equals( lTest.facilityType );
			}

			return lEquals;
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		int result = 17;

		result = 37*result + ((this.collectorId != null) ? this.collectorId.hashCode() : 0);

		result = 37*result + ((this.facilityId != null) ? this.facilityId.hashCode() : 0);

		result = 37*result + ((this.platformId != null) ? this.platformId.hashCode() : 0);

		result = 37*result + ((this.subPlatformId != null) ? this.subPlatformId.hashCode() : 0);

		result = 37*result + ((this.facilityName != null) ? this.facilityName.hashCode() : 0);

		result = 37*result + ((this.facilityType != null) ? this.facilityType.hashCode() : 0);

		return result;
	}

}
