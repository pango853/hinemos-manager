package com.clustercontrol.performance.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://collector.ws.clustercontrol.com")
public class CollectedDataList implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5103961414542068384L;
	List<CollectedDataInfo> list = new ArrayList<CollectedDataInfo>();
	public List<CollectedDataInfo> getList() {
		return list;
	}
	public void setList(List<CollectedDataInfo> list) {
		this.list = list;
	}
	public void add(CollectedDataInfo info) {
		list.add(info);
	}
	public int size() {
		return list.size();
	}
	public CollectedDataInfo get(int index) {
		return list.get(index);
	}
}
