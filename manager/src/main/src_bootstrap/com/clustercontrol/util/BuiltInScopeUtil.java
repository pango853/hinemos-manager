package com.clustercontrol.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;

public class BuiltInScopeUtil {
	public static final Log log = LogFactory.getLog(BuiltInScopeUtil.class);

	private static final Set<String> builtInScopeIDs = new HashSet<String>();
	static {
		builtInScopeIDs.add(FacilityTreeAttributeConstant.INTERNAL_SCOPE);
		builtInScopeIDs.add(FacilityTreeAttributeConstant.REGISTERED_SCOPE);
		builtInScopeIDs.add(FacilityTreeAttributeConstant.UNREGISTERED_SCOPE);
		builtInScopeIDs.add(FacilityTreeAttributeConstant.OWNER_SCOPE);
		builtInScopeIDs.add(FacilityTreeAttributeConstant.OS_PARENT_SCOPE);
	}

	public static synchronized void add(String ID) {
		log.debug("add " + ID);
		builtInScopeIDs.add(ID);
	}

	public static synchronized void addAll(Set<String> IDs) {
		log.debug("addAll " + IDs);
		builtInScopeIDs.addAll(IDs);
	}

	public static Set<String> getBuiltInScopeIDs() {
		return new HashSet<String>(builtInScopeIDs);
	}
}
