/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.util;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import mockit.Mock;
import mockit.MockUp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 
public class SystemTimeShifter {
	public static final Log log = LogFactory.getLog(SystemTimeShifter.class);
	
    public static final String PROPERTY_ISO_DATE = "systime.iso";
 
    private static final long INIT_MILLIS = System.currentTimeMillis();
    private static final long INIT_NANOS = System.nanoTime();
    private static long offset;
 
    static {
        String isoDate = System.getProperty(PROPERTY_ISO_DATE);
        if (isoDate != null) {
        	new SystemMock();
            setIsoDate(isoDate);
        }
    }
 
    public static synchronized void setIsoDate(String isoDate) {
        try {
            if (isoDate.indexOf('T') != -1) {
                long wantedMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(isoDate).getTime();
                offset = wantedMillis - millisSinceClassInit() - INIT_MILLIS;
            } else if (isoDate.indexOf(':') != -1) {
                Calendar calx = Calendar.getInstance();
                calx.setTime(new SimpleDateFormat("HH:mm:ss").parse(isoDate));
 
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, calx.get(Calendar.HOUR_OF_DAY));
                cal.set(Calendar.MINUTE, calx.get(Calendar.MINUTE));
                cal.set(Calendar.SECOND, calx.get(Calendar.SECOND));
                offset = cal.getTimeInMillis() - millisSinceClassInit() - INIT_MILLIS;
            } else {
                Calendar calx = Calendar.getInstance();
                calx.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(isoDate));
 
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, calx.get(Calendar.DAY_OF_MONTH));
                cal.set(Calendar.MONTH, calx.get(Calendar.MONTH));
                cal.set(Calendar.YEAR, calx.get(Calendar.YEAR));
                offset = cal.getTimeInMillis() - millisSinceClassInit() - INIT_MILLIS;
            }
        } catch (Exception e) {
        	log.error(e);
        }
    }
 
    public static long currentRealTimeMillis() {
        return INIT_MILLIS + millisSinceClassInit();
    }
 
    private static long millisSinceClassInit() {
        return (System.nanoTime() - INIT_NANOS) / 1000000;
    }
 
    public static class SystemMock extends MockUp<System> {
        @Mock
        public static long currentTimeMillis() {
            return INIT_MILLIS + offset + millisSinceClassInit();
        }
    }
}