/*

Copyright (C) since 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.monitor.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * Hinemosのイベント情報の検索条件を格納するクラスです。<BR>
 * DTOクラスとしてマネージャ、クライアント間の通信で利用します。
 *
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class EventFilterInfo implements Serializable {

	private static final long serialVersionUID = -8348543802703964223L;
	private Long outputDateFrom = null;		//受信日時（自）
	private Long outputDateTo = null;		//受信日時（至）
	private Long generationDateFrom = null;	//出力日時（自）
	private Long generationDateTo = null;	//出力日時（至）
	private String monitorId = null;		//監視項目ID
	private String monitorDetailId = null;	//監視詳細
	private String facilityType = null;		//対象ファシリティ種別
	private String application = null;		//アプリケーション
	private String message = null;			//メッセージ
	private Integer confirmFlgType = null;	//確認
	private Long outputDate = null;			//受信日時
	private Long generationDate = null;		//出力日時
	private String confirmedUser = null;	//確認ユーザ
	private String comment = null;	//コメント
	private Long commentDate = null;	//コメント更新日時
	private String commentUser = null;	//コメント更新ユーザ
	private String ownerRoleId = null;	//オーナーロールID
	private Integer[] priorityList = null;		//重要度リスト
	
	public void setOutputDateFrom(Long outputDateFrom) {
		this.outputDateFrom = outputDateFrom;
	}
	public Long getOutputDateFrom() {
		return outputDateFrom;
	}
	public void setOutputDateTo(Long outputDateTo) {
		this.outputDateTo = outputDateTo;
	}
	public Long getOutputDateTo() {
		return outputDateTo;
	}
	public void setGenerationDateFrom(Long generationDateFrom) {
		this.generationDateFrom = generationDateFrom;
	}
	public Long getGenerationDateFrom() {
		return generationDateFrom;
	}
	public void setGenerationDateTo(Long generationDateTo) {
		this.generationDateTo = generationDateTo;
	}
	public Long getGenerationDateTo() {
		return generationDateTo;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}
	public String getMonitorId() {
		return monitorId;
	}
	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}
	public String getMonitorDetailId() {
		return monitorDetailId;
	}
	public void setFacilityType(String facilityType) {
		this.facilityType = facilityType;
	}
	public String getFacilityType() {
		return facilityType;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getApplication() {
		return application;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getMessage() {
		return message;
	}
	public void setConfirmFlgType(Integer confirmFlgType) {
		this.confirmFlgType = confirmFlgType;
	}
	public Integer getConfirmFlgType() {
		return confirmFlgType;
	}
	public void setOutputDate(Long outputDate) {
		this.outputDate = outputDate;
	}
	public Long getOutputDate() {
		return outputDate;
	}
	public void setGenerationDate(Long generationDate) {
		this.generationDate = generationDate;
	}
	public Long getGenerationDate() {
		return generationDate;
	}
	public void setConfirmedUser(String confirmedUser) {
		this.confirmedUser = confirmedUser;
	}
	public String getConfirmedUser() {
		return confirmedUser;
	}
	public void setComment(String comment){
		this.comment = comment;
	}
	public String getComment(){
		return comment;
	}
	public void setCommentDate(Long commentDate){
		this.commentDate = commentDate;
	}
	public Long getCommentDate(){
		return commentDate;
	}
	public void setCommentUser(String commentUser){
		this.commentUser = commentUser;
	}
	public String getCommentUser(){
		return commentUser;
	}
	public void setOwnerRoleId(String ownerRoleId){
		this.ownerRoleId = ownerRoleId;
	}
	public String getOwnerRoleId(){
		return ownerRoleId;
	}
	public void setPriorityList(Integer[] priorityList) {
		this.priorityList = priorityList;
	}
	public Integer[] getPriorityList() {
		return priorityList;
	}
}
