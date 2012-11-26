/*
 * Copyright (c) 2012 Alexander Diener,
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhkn.in.uce.mediator.peerregistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread which cleans the list of registered users.
 * 
 * @author thomas zink, stefan lohr, Alexander Diener
 *         (aldiener@htwg-konstanz.de)
 */
public final class UserCleanerTask extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(UserCleanerTask.class);
    private final long iterationTimeInMillis;
    private final long maxLifetimeInMillis;
    private final UserList userList = UserList.INSTANCE;

    /**
     * Initializes the {@link UserCleanerTask} with the given data.
     * 
     * @param iterationTimeInSeconds
     *            the time interval in seconds for checking the users
     * @param maxLifetimeInSeconds
     *            the maximal lifetime in seconds of a user without refreshing
     */
    public UserCleanerTask(final long iterationTimeInSeconds, final long maxLifetimeInSeconds) {
        this.iterationTimeInMillis = iterationTimeInSeconds * 1000;
        this.maxLifetimeInMillis = maxLifetimeInSeconds * 1000;
    }

    /**
     * Checks for users where lifetime is expired. These users are removed from
     * the {@link UserList}.
     */
    public void run() {
        logger.info("UserCleaner started [ iterationTime: {} / maxLifeTime {} ]", //$NON-NLS-1$
                this.iterationTimeInMillis, this.maxLifetimeInMillis);
        while (!isInterrupted()) {
            long timestamp = System.currentTimeMillis() - this.maxLifetimeInMillis;
            this.userList.removeUsersByTimestamp(timestamp);
            try {
                Thread.sleep(this.iterationTimeInMillis);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }
}
