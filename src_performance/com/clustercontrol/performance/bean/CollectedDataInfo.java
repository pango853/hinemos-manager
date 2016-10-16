/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

 */

package com.clustercontrol.performance.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * 収集した性能値を格納するDTOクラス
 * 
 * 収集日時と性能値をペアで保持します。
 * 
 * @version 4.0.0
 * @since 4.0.0
 *
 */
@XmlType(namespace = "http://collector.ws.clustercontrol.com")
public class CollectedDataInfo implements Serializable {
	private static final long serialVersionUID = -126607156053910561L;

	private Long m_dateTime;        //収集日時
	private Float m_value = new Float(0);         //性能値

	public CollectedDataInfo(){
		super();
		this.m_dateTime = null;
		this.m_value = Float.NaN;
	}

	/**
	 * 性能値を格納したCollectedDataInfoオブジェクトを生成します。<br>
	 * 
	 * @param date 時刻
	 * @param value 性能値
	 */
	public CollectedDataInfo(final Long date, final Float value) {
		this.m_dateTime = date;
		this.m_value = value;
	}

	/**
	 * 収集日時 を取得します。
	 * @return 収集日時
	 */
	public Long getD() {
		return m_dateTime;
	}

	public void setD(Long time) {
		m_dateTime = time;
	}

	/**
	 * 性能値 を取得します。
	 * @return  性能値
	 */
	public Float getV() {
		return m_value;
	}

	// webサービス(jaxb)のため、setterを用意しておく。
	@Deprecated
	public void setV(Float value){
		m_value = value;
	}

	/**
	 * データの内容を文字列として返します。
	 */
	@Override
	public String toString(){
		String str = m_dateTime + " : " + m_value;
		return str;
	}
}