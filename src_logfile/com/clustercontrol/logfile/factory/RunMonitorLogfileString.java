package com.clustercontrol.logfile.factory;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.logfile.bean.LogfileResultDTO;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.session.RepositoryControllerBean;

public class RunMonitorLogfileString {
	
	public static final Log _log = LogFactory.getLog(RunMonitorLogfileString.class);
	
	public void run(String facilityId, LogfileResultDTO result) throws HinemosUnknown {
		
		List<String> facilityIdList = FacilitySelector.getFacilityIdList(result.monitorInfo.getFacilityId(), result.monitorInfo.getOwnerRoleId(), 0, false, false);
		if (_log.isDebugEnabled()) {
			_log.debug(result.monitorInfo.getFacilityId() + " contains : " + facilityIdList);
		}
		if (! facilityIdList.contains(facilityId)) {
			_log.debug("facilityId is not contained " + facilityId + " in " + facilityIdList);
			return;
		}
		
		String origMessage = "log.file=" + result.monitorInfo.getLogfileCheckInfo().getLogfile() + "\n"
				+ "pattern=" + result.monitorStrValueInfo.getPattern() + "\n" 
				+ "log.line=" + result.message;
		
		OutputBasicInfo output = new OutputBasicInfo();

		output.setMonitorId(result.monitorStrValueInfo.getMonitorId());
		output.setFacilityId(facilityId);
		output.setPluginId(HinemosModuleConstant.MONITOR_LOGFILE);
		output.setSubKey(result.monitorStrValueInfo.getPattern());
		
		if (FacilityTreeAttributeConstant.UNREGISTEREFD_SCOPE.equals(facilityId)) {
			output.setScopeText(result.msgInfo.getHostName());
		} else {
			String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
			output.setScopeText(facilityPath);
		}
		
		output.setApplication(result.monitorInfo.getApplication());
		output.setMessageId(result.monitorStrValueInfo.getMessageId());
		
		if (result.monitorStrValueInfo.getMessage() != null) {
			String str = result.monitorStrValueInfo.getMessage().replace("#[LOG_LINE]", result.message);
			int maxLen = HinemosPropertyUtil.getHinemosPropertyNum("monitor.log.line.max.length", 256);
			if (str.length() > maxLen) {
				str = str.substring(0, maxLen);
			}
			output.setMessage(str);
		}

		output.setMessageOrg(origMessage);
		output.setPriority(result.monitorStrValueInfo.getPriority());
		output.setGenerationDate(result.msgInfo.getGenerationDate());
		
		output.setMultiId(HinemosPropertyUtil.getHinemosPropertyStr("monitor.systemlog.receiverid", System.getProperty("hinemos.manager.nodename")));
		
		new NotifyControllerBean().notify(output, NotifyGroupIdGenerator.generate(result.monitorInfo));
	}
	
}
