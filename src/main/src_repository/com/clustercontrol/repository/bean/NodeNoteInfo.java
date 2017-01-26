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
 * このクラスは、リポジトリプロパティ[備考]のクラスです。
 * NodeDataクラスのメンバ変数として利用されます。
 * @since 0.8
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class NodeNoteInfo implements Serializable, Cloneable
{

	private static final long serialVersionUID = 5099421874861844517L;
	private java.lang.Integer noteId = new Integer(-1);
	private java.lang.String note = "";

	/**
	 * 空のコンストラクタです。setterで要素を追加して下さい。
	 */
	public NodeNoteInfo()
	{
	}

	/**
	 * コンストラクトする際に、値をセットする事ができます。
	 * @param noteId
	 * @param note
	 */
	public NodeNoteInfo(java.lang.Integer noteId,java.lang.String note )
	{
		setNoteId(noteId);
		setNote(note);
	}

	/**
	 * NodeNoteDataインスタンスのコピーを生成する時に利用します。
	 * @param otherData
	 */
	public NodeNoteInfo( NodeNoteInfo otherData )
	{
		setNoteId(otherData.getNoteId());
		setNote(otherData.getNote());

	}

	/**
	 * 備考ID(Hinemosクライアントからは不可視)のgetterです。
	 * @return Integer
	 */
	public java.lang.Integer getNoteId()
	{
		return this.noteId;
	}

	/**
	 * 備考ID(Hinemosクライアントからは不可視)のsetterです。
	 * not nullです。
	 * @param noteId
	 */
	public void setNoteId( java.lang.Integer noteId )
	{
		this.noteId = noteId;
	}

	/**
	 * 備考のgetterです。
	 * @return String
	 */
	public java.lang.String getNote()
	{
		return this.note;
	}

	/**
	 * 備考のsetterです。
	 * not nullです。
	 * @param note
	 */
	public void setNote( java.lang.String note )
	{
		this.note = note;
	}

	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer("{");

		str.append("noteId=" + getNoteId() + " " + "note=" + getNote());
		str.append('}');

		return(str.toString());
	}

	@Override
	public boolean equals( Object pOther )
	{
		if( pOther instanceof NodeNoteInfo )
		{
			NodeNoteInfo lTest = (NodeNoteInfo) pOther;
			boolean lEquals = true;

			if( this.noteId == null )
			{
				lEquals = lEquals && ( lTest.noteId == null );
			}
			else
			{
				lEquals = lEquals && this.noteId.equals( lTest.noteId );
			}
			if( this.note == null )
			{
				lEquals = lEquals && ( lTest.note == null );
			}
			else
			{
				lEquals = lEquals && this.note.equals( lTest.note );
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

		result = 37*result + ((this.noteId != null) ? this.noteId.hashCode() : 0);

		result = 37*result + ((this.note != null) ? this.note.hashCode() : 0);

		return result;
	}

	@Override
	public NodeNoteInfo clone() {

		NodeNoteInfo cloneInfo = new NodeNoteInfo();
		try {
			cloneInfo = (NodeNoteInfo)super.clone();
			cloneInfo.noteId = this.noteId;
			cloneInfo.note = this.note;
		} catch (CloneNotSupportedException e) {
			//do nothing
		}

		return cloneInfo;
	}
}
