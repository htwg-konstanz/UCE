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
package de.fhkn.in.uce.mediator.connectionhandling;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages {@link ConnectionRequest}s. The {@link ConnectionRequest} are
 * associated with the transaction of the message.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public enum ConnectionRequestList {
    INSTANCE;

    private final ConcurrentHashMap<String, ConnectionRequest> connectionRequests = new ConcurrentHashMap<String, ConnectionRequest>();

    /**
     * Adds a new {@link ConnectionRequest} to the list. The transaction id to
     * associate a key is extracted from the message which is included in the
     * {@link ConnectionRequest}.
     * 
     * @param connectionRequest
     *            the {@link ConnectionRequest} to add
     */
    public void putConnectionRequest(final ConnectionRequest connectionRequest) {
        this.connectionRequests.put(new String(connectionRequest.getConnectionRequestMessage().getHeader()
                .getTransactionId()), connectionRequest);
    }

    /**
     * Returns the {@link ConnectionRequest} for the given transaction id.
     * 
     * @param transactionId
     *            the transaction id as string.
     * @return the {@link ConnectionRequest} to the given transaction id
     */
    public ConnectionRequest getConnectionRequest(final String transactionId) {
        return this.connectionRequests.get(transactionId);
    }

    /**
     * Removes the {@link ConnectionRequest} to the given transaction id.
     * 
     * @param transactionId
     *            the transaction id to remove
     */
    public void removeConnectionRequest(final String transactionId) {
        this.connectionRequests.remove(transactionId);
    }
}
