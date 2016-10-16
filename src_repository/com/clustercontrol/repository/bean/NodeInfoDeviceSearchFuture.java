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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.bind.annotation.XmlType;

/**
 * このクラスはDeviceSearchの更新情報を持つクラスです。
 * DeviceSearchでノード情報を取得した場合に使用します。
 *
 * @since 5.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class NodeInfoDeviceSearchFuture extends NodeInfoDeviceSearch implements Future<NodeInfoDeviceSearch>
{
	/**
	 * シリアルUID
	 */
	private static final long serialVersionUID = 6592679628485761134L;

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public NodeInfoDeviceSearch get() throws InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public NodeInfoDeviceSearch get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		// TODO Auto-generated method stub
		return null;
	}
}
