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

package com.clustercontrol.poller.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.util.AbstractSnmpUtility;
import org.snmp4j.util.DefaultPDUFactory;

public class MultipleOidsUtils extends AbstractSnmpUtility {
	
	private final static Log log = LogFactory.getLog( MultipleOidsUtils.class );

	private Snmp snmp;
	private DefaultPDUFactory factory;

	public MultipleOidsUtils(Snmp snmp, DefaultPDUFactory factory) {
		super(snmp, factory);
		this.snmp = snmp;
		this.factory = factory;
	}

	public Collection<VariableBinding> query(Target target, OID[] rootOids) {
		HashMap<OID, VariableBinding> result = new HashMap<OID, VariableBinding>();

		if ((rootOids == null) || (rootOids.length == 0)) {
			throw new IllegalArgumentException("No OIDs specified");
		}

		PDU request = pduFactory.createPDU(target);
		// Bulkの場合のみMaxRepetitionsを指定
		if(request.getType() == PDU.GETBULK){
			// DefaultPDUFactoryに設定しても反映されなかったため、PDUに直接指定する
			request.setMaxRepetitions(this.factory.getMaxRepetitions());
		}
		
		RootOidAndOidMapping mapping = new RootOidAndOidMapping(rootOids);
		int requestCounter = 0;
		int responseCounter = 0;
		while (!mapping.isEmpty()) {
			ArrayList<OID> oidList = mapping.getOidList();
			log.debug(target.getAddress() + " oidList.size=" + oidList.size());
			RootOidAndOidMapping oldMapping = new RootOidAndOidMapping(mapping);

			PDU response = sendRequest(request, target, oidList);
			requestCounter ++;
			if (response == null) {
				log.debug(target.getAddress() + " response is null : result.size=" + result.values().size());
				return result.values();
			}
			
			Vector<? extends VariableBinding> vbs = response.getVariableBindings();
			int requestOidSize = request.getVariableBindings().size();//requestOidSize <= oidList.size()
			
			for (int i = 0; i < vbs.size(); i++) {
				responseCounter ++;
				VariableBinding vb = vbs.get(i);
				log.trace("oid=" + vb.getOid() + ", " + vb.toString());
				
				int colIndex = i % requestOidSize;
				OID oldOid = oidList.get(colIndex);
				OID rootOid = oldMapping.getRootOidByOid(oldOid);
				if (rootOid == null) {
					continue;
				}

				mapping.removeByRootOid(rootOid);

				if (vb.isException()) {
					log.debug("exception " + target.getAddress() + ", "+ vb.toString()); // endOfMibView
					continue;
				}

				OID oid = vb.getOid();
				if (!oid.startsWith(rootOid)) {
					continue;
				}

				if (result.containsKey(oid)) {
					continue;
				}

				result.put(oid, vb);
				mapping.put(rootOid, oid);
			}
		}
		
		// SNMPのやりとり回数が多い場合はログ出力する。
		String message = target.getAddress() + ", requestCounter=" + requestCounter + ", responseCounter=" + responseCounter;
		if (requestCounter > 200 || responseCounter > 10000) {
			log.warn(message);
		} else if (requestCounter > 100 || 	responseCounter > 5000) {
			log.info(message);
		} else {
			log.debug(message);
		}

		return result.values();
	}

	private PDU sendRequest(PDU request, Target target, ArrayList<OID> oidList) {
		request.clear();
		for (OID oid : oidList) {
			VariableBinding vb = new VariableBinding(oid);
			request.add(vb);
			if (request.getBERLength() > target.getMaxSizeRequestPDU()) {
				request.trim();
				break;
			}
		}

		try {
			ResponseEvent event = session.send(request, target);
			if (checkResponse(event, target)) {
				PDU response = event.getResponse();
				return response;
			}
		} catch (IOException e) {
			log.warn(target.getAddress() + " sendRequest : " + e.getMessage());
		} catch (Exception e) {
			log.warn(target.getAddress() + " sendRequest : " + e.getClass().getName() + ", " + e.getMessage());
		}

		return null;
	}

	private boolean checkResponse(ResponseEvent event, Target target) {
		if (event.getError() != null) {
			log.warn(target.getAddress() + " checkResponse : error : " + event.getError().getMessage());
			return false;
		}

		PDU response = event.getResponse();
		if (response == null) {
			log.debug(target.getAddress() + " checkResponse : response is null");
			return false;
		}

		if (response.getType() == PDU.REPORT) {
			log.warn(target.getAddress() + " checkResponse : REPORT");
			return false;
		}

		if (response.getErrorStatus() != PDU.noError) {
			log.warn(target.getAddress() + " checkResponse : ERROR : " + event.getResponse().getErrorStatus() + ", "
					+ event.getResponse().getErrorStatusText());
			return false;
		}
		
		if (log.isDebugEnabled()) {
			log.debug(target.getAddress() + " response : " +
					"errorIndex=" + response.getErrorIndex() +
					", error=" + response.getErrorStatusText() + "(" + response.getErrorStatus() + ")" +
					", type=" + PDU.getTypeString(response.getType()) + "(" + response.getType() + ")" + 
					", repeater=" + response.getNonRepeaters() +
					", length=" + response.getBERLength() + "(" + response.getBERPayloadLength() + ")" +
					", size=" + response.size());
		}
		
		return true;
	}
}
