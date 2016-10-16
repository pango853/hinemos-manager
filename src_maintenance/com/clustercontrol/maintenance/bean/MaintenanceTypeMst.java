/*

Copyright (C) 2007 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.maintenance.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * メンテナンス種別のデータクラスです。
 * 
 * @version 4.0.0
 * @since 2.2.0
 *
 */
@XmlType(namespace = "http://maintenance.ws.clustercontrol.com")
public class MaintenanceTypeMst implements Serializable{

	private static final long serialVersionUID = 4582474743123026580L;
	private String type_id;
	private String name_id;
	private Integer order_no = new Integer(0);

	public MaintenanceTypeMst() {
		super();
	}

	public MaintenanceTypeMst(
			String type_id,
			String name_id,
			Integer order_no) {
		setType_id(type_id);
		setName_id(name_id);
		setOrder_no(order_no);
	}

	public String getType_id() {
		return type_id;
	}

	public void setType_id(String type_id) {
		this.type_id = type_id;
	}

	public String getName_id() {
		return name_id;
	}

	public void setName_id(String name_id) {
		this.name_id = name_id;
	}

	public Integer getOrder_no() {
		return order_no;
	}

	public void setOrder_no(Integer order_no) {
		this.order_no = order_no;
	}
}
