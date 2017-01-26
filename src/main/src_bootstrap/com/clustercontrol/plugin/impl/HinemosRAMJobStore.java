/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

 */

package com.clustercontrol.plugin.impl;

import java.util.ArrayList;
import java.util.List;

import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.quartz.simpl.RAMJobStore;
import org.quartz.spi.OperableTrigger;

/**
 * 監視ノードが大量にある場合に、例えば10Kノード、監視項目の登録または削除は
 * 時間が掛かった。その原因はクラスRAMJobStoreのメッセージ getTriggersForJob
 * 実装に性能問題があったため。新しい実装では、HinemosのJobKeyのgroupNameとnameが
 * TriggerKeyのgroupNameとnameと同じであることを考慮して、メソッドretrieveTriggerを
 * 利用して、性能の改善を果たした。
 */
public class HinemosRAMJobStore extends RAMJobStore {

	@Override
	public List<OperableTrigger> getTriggersForJob(JobKey jobKey) {
		List<OperableTrigger> triggerList = new ArrayList<OperableTrigger>(1);
		OperableTrigger trigger = retrieveTrigger(new TriggerKey(jobKey.getName(), jobKey.getGroup()));
		if (trigger != null) {
			triggerList.add(trigger);
		}
		return triggerList;
	}
}
