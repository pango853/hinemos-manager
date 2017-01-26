/*

Copyright (C) 2008 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

 */

package com.clustercontrol.poller;

/**
 * 正常に初期化されていない場合にスローされる例外クラス
 * 
 * @version 3.0.0
 * @since 3.0.0
 */
public class NotInitializedException extends Exception {
	private static final long serialVersionUID = 3359847656015171604L;

	public NotInitializedException(String message){
		super(message);
	}
}
