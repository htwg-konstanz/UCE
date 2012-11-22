/**
 * Copyright (C) 2011 Daniel Maier
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.fhkn.in.uce.holepunching.core;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;

/**
 * The interface for the authentication mechanism objects on the target and
 * source side.
 * 
 * @author Daniel Maier
 * 
 */
public interface ConnectionAuthenticator {
    /**
     * 
     * @param toBeAuthenticated
     *            the socket connection that should get authenticated
     * @param relatedHolePunchingTasks
     *            all tasks that are involved in the hole punching process
     *            regarding this connection
     * @param ownTask
     *            the task that invokes this method
     * @param sharedLock
     *            the lock object all involved tasks share
     * @return true if the authentication was successful, false otherwise
     * @throws IOException
     *             if an I/O error occurs while authentication
     */
    public boolean authenticate(Socket toBeAuthenticated, Set<CancelableTask> relatedHolePunchingTasks,
            CancelableTask ownTask, Object sharedLock) throws IOException;
}
