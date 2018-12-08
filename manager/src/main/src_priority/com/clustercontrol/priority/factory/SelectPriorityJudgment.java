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

package com.clustercontrol.priority.factory;

import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.priority.bean.PriorityJudgmentInfo;
import com.clustercontrol.priority.model.PriorityInfoEntity;
import com.clustercontrol.priority.util.QueryUtil;

/**
 * 重要度判定を検索するクラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class SelectPriorityJudgment {

	/**
	 * 重要度判定情報を取得します。<BR>
	 * 
	 * @return
	 * @throws MonitorNotFound
	 */
	public PriorityJudgmentInfo getPriorityJudgment(String judgmentId) throws MonitorNotFound {

		PriorityJudgmentInfo info = null;

		//重要度判定情報を検索し取得
		PriorityInfoEntity priority = QueryUtil.getPriorityInfoPK(judgmentId);
		info =
				new PriorityJudgmentInfo(
						priority.getJudgmentId(),
						priority.getDescription(),
						priority.getPattern01(),
						priority.getPattern02(),
						priority.getPattern03(),
						priority.getPattern04(),
						priority.getPattern05(),
						priority.getPattern06(),
						priority.getPattern07(),
						priority.getPattern08(),
						priority.getPattern09(),
						priority.getPattern10(),
						priority.getPattern11(),
						priority.getPattern12(),
						priority.getPattern13(),
						priority.getPattern14(),
						priority.getPattern15(),
						priority.getRegDate(),
						priority.getUpdateDate(),
						priority.getRegUser(),
						priority.getUpdateUser());
		return info;
	}
}
