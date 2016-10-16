/*

Copyright (C) since 2009 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.repository.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 *
 *
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class NodeVariableInfo implements Serializable, Cloneable
{

	private static final long serialVersionUID = -8463836300078935000L;
	private java.lang.String nodeVariableName = "";
	private java.lang.String nodeVariableValue = "";

	/**
	 * 空のコンストラクタです。setterで要素を追加して下さい。
	 */
	public NodeVariableInfo()
	{
	}

	/**
	 * コンストラクトする際に、値をセットする事ができます。
	 * @param noteId
	 * @param note
	 */
	public NodeVariableInfo(java.lang.String nodeVariableName,java.lang.String nodeVariableValue )
	{
		setNodeVariableName(nodeVariableName);
		setNodeVariableValue(nodeVariableValue);
	}

	/**
	 * NodeVariableInfoインスタンスのコピーを生成する時に利用します。
	 * @param otherData
	 */
	public NodeVariableInfo( NodeVariableInfo otherData )
	{
		setNodeVariableName(otherData.getNodeVariableName());
		setNodeVariableValue(otherData.getNodeVariableValue());

	}


	/**
	 * ノード変数名のgetter
	 * @return
	 */
	public java.lang.String getNodeVariableName() {
		return this.nodeVariableName;
	}

	/**
	 * ノード変数名のsetter
	 * @param nodeVariableName
	 */
	public void setNodeVariableName(java.lang.String nodeVariableName) {
		this.nodeVariableName = nodeVariableName;
	}

	/**
	 * ノード変数値のgetter
	 * @return
	 */
	public java.lang.String getNodeVariableValue() {
		return this.nodeVariableValue;
	}

	/**
	 * ノード変数値のsetter
	 * @param nodeVariableValue
	 */
	public void setNodeVariableValue(java.lang.String nodeVariableValue) {
		this.nodeVariableValue = nodeVariableValue;
	}

	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer("{");

		str.append("nodeVariableName=" + getNodeVariableName() + " " + "nodeVariableValue=" + getNodeVariableValue());
		str.append('}');

		return(str.toString());
	}

	@Override
	public boolean equals( Object pOther )
	{
		if( pOther instanceof NodeVariableInfo )
		{
			NodeVariableInfo lTest = (NodeVariableInfo) pOther;
			boolean lEquals = true;

			if( this.nodeVariableName == null )
			{
				lEquals = lEquals && ( lTest.nodeVariableName == null );
			}
			else
			{
				lEquals = lEquals && this.nodeVariableName.equals( lTest.nodeVariableName );
			}
			if( this.nodeVariableValue == null )
			{
				lEquals = lEquals && ( lTest.nodeVariableValue == null );
			}
			else
			{
				lEquals = lEquals && this.nodeVariableValue.equals( lTest.nodeVariableValue );
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

		result = 37*result + ((this.nodeVariableName != null) ? this.nodeVariableName.hashCode() : 0);

		result = 37*result + ((this.nodeVariableValue != null) ? this.nodeVariableValue.hashCode() : 0);

		return result;
	}

	@Override
	public NodeVariableInfo clone() {

		NodeVariableInfo cloneInfo = new NodeVariableInfo();
		try {
			cloneInfo = (NodeVariableInfo)super.clone();
			cloneInfo.nodeVariableName = this.nodeVariableName;
			cloneInfo.nodeVariableValue = this.nodeVariableValue;
		} catch (CloneNotSupportedException e) {
			//do nothing
		}

		return cloneInfo;
	}

}
