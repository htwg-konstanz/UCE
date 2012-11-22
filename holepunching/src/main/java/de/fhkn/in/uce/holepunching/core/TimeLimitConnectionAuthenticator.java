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
package de.fhkn.in.uce.holepunching.core;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;

/**
 * Enhances a {@link ConnectionAuthenticator} object with the ability to perform
 * the authentication with a timeout.
 * 
 * @author Daniel Maier
 * 
 */
final class TimeLimitConnectionAuthenticator implements ConnectionAuthenticator {
    private final ConnectionAuthenticator authenticator;
    private final int timeout;

    /**
     * Creates a {@link TimeLimitConnectionAuthenticator}.
     * 
     * @param authenticator
     *            the used {@link ConnectionAuthenticator}
     * @param timeout
     *            the socket timeout for the authentication
     */
    public TimeLimitConnectionAuthenticator(final ConnectionAuthenticator authenticator, final int timeout) {
        this.authenticator = authenticator;
        this.timeout = timeout;
    }

    /**
     * Authenticates the given socket with the timeout that is specified in in
     * the constructor. Uses the {@link ConnectionAuthenticator} object passed
     * to the constructor for the authentication mechanism.
     */
    @Override
    public boolean authenticate(final Socket toBeAuthenticated, final Set<CancelableTask> relatedHolePunchingTasks,
            final CancelableTask ownTask, final Object sharedLock) throws IOException {
        final int oldTimeout = toBeAuthenticated.getSoTimeout();
        toBeAuthenticated.setSoTimeout(this.timeout);
        boolean auth;
        try {
            auth = this.authenticator.authenticate(toBeAuthenticated, relatedHolePunchingTasks, ownTask, sharedLock);
        } finally {
            toBeAuthenticated.setSoTimeout(oldTimeout);
        }
        return auth;
    }
}
