/*
    Copyright (c) 2012 Thomas Zink, 

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhkn.in.uce.reversal.mediator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread which cleans the list of registered users.
 * 
 * @author thomas zink, stefan lohr
 */
public class UserCleaner extends Thread {
	
	private long iterationTime;
	private long maxLifeTime;
	private static final Logger logger = LoggerFactory.getLogger(UserCleaner.class);
	
	/**
	 * Constructor, initializes iterationTimeInSeconds, maxLifeTimeInSeconds, Logger
	 * 
	 * @param iterationTimeInSeconds Cleaning-Interval in seconds 
	 * @param maxLifeTimeInSeconds Maximum lifetime of not updated users
	 */
	public UserCleaner(int iterationTimeInSeconds, int maxLifeTimeInSeconds) {
		this.iterationTime = iterationTimeInSeconds * 1000;
		this.maxLifeTime = maxLifeTimeInSeconds * 1000;
		
		logger.info("UserCleaner started [ iterationTime: {} / maxLifeTime {} ]",
				iterationTimeInSeconds, maxLifeTimeInSeconds);
	}
	
	/**
	 * Removes users with old timestamps in the Singleton UserList
	 * After removing, thread sleeps for iterationTime, thereafter it starts again.
	 * Thread can be stopped by calling interrupt().
	 */
	public void run() {
		while (!isInterrupted()) {
			long timestamp = System.currentTimeMillis() - maxLifeTime;
			UserList.getInstance().removeUsersByTimestamp(timestamp);
			try { Thread.sleep(iterationTime); }
			catch (InterruptedException e) { Thread.interrupted(); }
		}
	}
}
