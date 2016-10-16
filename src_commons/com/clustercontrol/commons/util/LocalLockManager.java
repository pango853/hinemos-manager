/*

Copyright (C) 2015 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.commons.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LocalLockManager implements ILockManager {
	
	private static final Log _log = LogFactory.getLog(LocalLockManager.class);
	
	private final Map<String, LocalLock> _lockMap = new HashMap<String, LocalLock>();
	
	public LocalLockManager() {	}
	
	/* (non-Javadoc)
	 * @see com.clustercontrol.commons.util.ILockManager#create(java.lang.String)
	 */
	@Override
	public synchronized ILock create(String key) {
		LocalLock lock = _lockMap.get(key);
		if (lock == null) {
			lock = new LocalLock(key);
			_lockMap.put(key, lock);
		}
		return lock;
	}
	
	@Override
	public synchronized boolean delete(String key) {
		return _lockMap.remove(key) != null;
	}
	
	private synchronized void monitor() {
		for (Entry<String, LocalLock> pair : _lockMap.entrySet()) {
			pair.getValue().monitor();
		}
	}
	
	public class LocalLock implements ILock {
		
		private final String _key;
		private final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();
		
		public LocalLock(String key) {
			_key = key;
		}
		
		/* (non-Javadoc)
		 * @see com.clustercontrol.commons.util.ILock#readLock()
		 */
		@Override
		public void readLock() {
			_lock.readLock().lock();
		}
		
		/* (non-Javadoc)
		 * @see com.clustercontrol.commons.util.ILock#readUnlock()
		 */
		@Override
		public void readUnlock() {
			_lock.readLock().unlock();
		}
		
		/* (non-Javadoc)
		 * @see com.clustercontrol.commons.util.ILock#writeLock()
		 */
		@Override
		public void writeLock() {
			_lock.writeLock().lock();
		}
		
		/* (non-Javadoc)
		 * @see com.clustercontrol.commons.util.ILock#writeUnlock()
		 */
		@Override
		public void writeUnlock() {
			_lock.writeLock().unlock();
		}
		
		private void monitor() {
			Set<Lock> lockSet = new HashSet<Lock>(Arrays.asList(_lock.readLock(), _lock.writeLock()));
			
			for (Lock lock : lockSet) {
				try {
					if (lock.tryLock(30000L, TimeUnit.MILLISECONDS)) {
						lock.unlock();
						if (_log.isDebugEnabled()) {
							_log.debug(String.format("%s \"%s\" is available.", LocalLock.class.getName(), _key));
						}
					} else {
						_log.warn(String.format("%s \"%s\" is not available. (locking timeout)", LocalLock.class.getName(), _key));
					}
				} catch (InterruptedException e) {
					_log.warn(String.format("%s \"%s\" is not monitored successfully.", LocalLock.class.getName(), _key), e);
				}
			}
		}
	}
	
}
