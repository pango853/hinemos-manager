/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.priority.bean;

import java.util.Date;

/**
 * 重要度判定情報のBeanクラス<BR>
 * 
 * @version 2.1.0
 * @since 2.1.0
 */
public class PriorityJudgmentInfo implements java.io.Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2506991993295020918L;

	private String judgment_id;
	private String description;
	private Integer pattern_01;
	private Integer pattern_02;
	private Integer pattern_03;
	private Integer pattern_04;
	private Integer pattern_05;
	private Integer pattern_06;
	private Integer pattern_07;
	private Integer pattern_08;
	private Integer pattern_09;
	private Integer pattern_10;
	private Integer pattern_11;
	private Integer pattern_12;
	private Integer pattern_13;
	private Integer pattern_14;
	private Integer pattern_15;
	private Date reg_date;
	private Date update_date;
	private String reg_user;
	private String update_user;

	public PriorityJudgmentInfo(
			String judgment_id,
			String description,
			Integer pattern_01,
			Integer pattern_02,
			Integer pattern_03,
			Integer pattern_04,
			Integer pattern_05,
			Integer pattern_06,
			Integer pattern_07,
			Integer pattern_08,
			Integer pattern_09,
			Integer pattern_10,
			Integer pattern_11,
			Integer pattern_12,
			Integer pattern_13,
			Integer pattern_14,
			Integer pattern_15,
			Date reg_date,
			Date update_date,
			String reg_user,
			String update_user)
	{
		setJudgment_id(judgment_id);
		setDescription(description);
		setPattern_01(pattern_01);
		setPattern_02(pattern_02);
		setPattern_03(pattern_03);
		setPattern_04(pattern_04);
		setPattern_05(pattern_05);
		setPattern_06(pattern_06);
		setPattern_07(pattern_07);
		setPattern_08(pattern_08);
		setPattern_09(pattern_09);
		setPattern_10(pattern_10);
		setPattern_11(pattern_11);
		setPattern_12(pattern_12);
		setPattern_13(pattern_13);
		setPattern_14(pattern_14);
		setPattern_15(pattern_15);
		setReg_date(reg_date);
		setUpdate_date(update_date);
		setReg_user(reg_user);
		setUpdate_user(update_user);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getJudgment_id() {
		return judgment_id;
	}

	public void setJudgment_id(String judgment_id) {
		this.judgment_id = judgment_id;
	}

	public Integer getPattern_01() {
		return pattern_01;
	}

	public void setPattern_01(Integer pattern_01) {
		this.pattern_01 = pattern_01;
	}

	public Integer getPattern_02() {
		return pattern_02;
	}

	public void setPattern_02(Integer pattern_02) {
		this.pattern_02 = pattern_02;
	}

	public Integer getPattern_03() {
		return pattern_03;
	}

	public void setPattern_03(Integer pattern_03) {
		this.pattern_03 = pattern_03;
	}

	public Integer getPattern_04() {
		return pattern_04;
	}

	public void setPattern_04(Integer pattern_04) {
		this.pattern_04 = pattern_04;
	}

	public Integer getPattern_05() {
		return pattern_05;
	}

	public void setPattern_05(Integer pattern_05) {
		this.pattern_05 = pattern_05;
	}

	public Integer getPattern_06() {
		return pattern_06;
	}

	public void setPattern_06(Integer pattern_06) {
		this.pattern_06 = pattern_06;
	}

	public Integer getPattern_07() {
		return pattern_07;
	}

	public void setPattern_07(Integer pattern_07) {
		this.pattern_07 = pattern_07;
	}

	public Integer getPattern_08() {
		return pattern_08;
	}

	public void setPattern_08(Integer pattern_08) {
		this.pattern_08 = pattern_08;
	}

	public Integer getPattern_09() {
		return pattern_09;
	}

	public void setPattern_09(Integer pattern_09) {
		this.pattern_09 = pattern_09;
	}

	public Integer getPattern_10() {
		return pattern_10;
	}

	public void setPattern_10(Integer pattern_10) {
		this.pattern_10 = pattern_10;
	}

	public Integer getPattern_11() {
		return pattern_11;
	}

	public void setPattern_11(Integer pattern_11) {
		this.pattern_11 = pattern_11;
	}

	public Integer getPattern_12() {
		return pattern_12;
	}

	public void setPattern_12(Integer pattern_12) {
		this.pattern_12 = pattern_12;
	}

	public Integer getPattern_13() {
		return pattern_13;
	}

	public void setPattern_13(Integer pattern_13) {
		this.pattern_13 = pattern_13;
	}

	public Integer getPattern_14() {
		return pattern_14;
	}

	public void setPattern_14(Integer pattern_14) {
		this.pattern_14 = pattern_14;
	}

	public Integer getPattern_15() {
		return pattern_15;
	}

	public void setPattern_15(Integer pattern_15) {
		this.pattern_15 = pattern_15;
	}

	public Date getReg_date() {
		return reg_date;
	}

	public void setReg_date(Date reg_date) {
		this.reg_date = reg_date;
	}

	public String getReg_user() {
		return reg_user;
	}

	public void setReg_user(String reg_user) {
		this.reg_user = reg_user;
	}

	public Date getUpdate_date() {
		return update_date;
	}

	public void setUpdate_date(Date update_date) {
		this.update_date = update_date;
	}

	public String getUpdate_user() {
		return update_user;
	}

	public void setUpdate_user(String update_user) {
		this.update_user = update_user;
	}
}
