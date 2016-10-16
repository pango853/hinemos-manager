package com.clustercontrol.ws.util;

import java.util.HashMap;

import com.clustercontrol.performance.util.code.CollectorItemTreeItem;

/*
 * JAXBではWebMethodの引数、戻り値にはHashMapが使えない。
 * メンバ変数として定義されている場合は、独自クラスとしてクライアントから利用できるため、
 * メンバ変数にHashMapを持つクラスを用意する。
 */
public class HashMapInfo {
	private HashMap<String, String> map1 = new HashMap<String, String>();
	private HashMap<String, CollectorItemTreeItem> map2 = new HashMap<String, CollectorItemTreeItem>();
	public HashMapInfo(){}

	public HashMap<String, String> getMap1() {
		return map1;
	}
	public void setMap1(HashMap<String, String> map1) {
		this.map1 = map1;
	}
	public HashMap<String, CollectorItemTreeItem> getMap2() {
		return map2;
	}
	public void setMap2(HashMap<String, CollectorItemTreeItem> map2) {
		this.map2 = map2;
	}
}
