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
package de.fhkn.in.uce.connectivitymanager.manager;

import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.connectivitymanager.connection.configuration.ConnectionConfiguration;
import de.fhkn.in.uce.connectivitymanager.connection.configuration.DefaultConnectionConfiguration;

public final class ConnectivityManager {
    private final Logger logger = LoggerFactory.getLogger(ConnectivityManager.class);
    private final String targetId;
    private final ConnectionEstablishment connectionEstablishment;
    private final ConnectionConfiguration config;

    public ConnectivityManager(final String targetId, final ConnectionEstablishment connectionEstablishment,
            ConnectionConfiguration config) {
        this.targetId = targetId;
        this.connectionEstablishment = connectionEstablishment;
        this.config = config;
    }

    public ConnectivityManager(final String targetId, final ConnectionEstablishment connectionEstablishment) {
        this.targetId = targetId;
        this.connectionEstablishment = connectionEstablishment;
        this.config = DefaultConnectionConfiguration.getInstance();
    }

    public Socket establishConnection() {
        Socket connectedSocket = this.connectionEstablishment.establishConnection(this.targetId, config);
        if (null == connectedSocket) {
            logger.error("socket for target {} is null", this.targetId);
        } else if (!connectedSocket.isConnected()) {
            logger.error("socket for target {} is not connected: {}", this.targetId, connectedSocket);
        } else {
            logger.debug("Connection to {} established: {}", this.targetId, connectedSocket); // $NON-NLS-1$
        }
        return connectedSocket;
    }

    public String getTargetId() {
        return this.targetId;
    }
}
