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
package de.fhkn.in.uce.connectivitymanager.manager.source;

import java.net.Socket;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.plugininterface.ConnectionNotEstablishedException;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;

final class SocketTask implements Callable<Socket> {
    private final Logger logger = LoggerFactory.getLogger(SocketTask.class);
    private final String targetId;
    private final NATTraversalTechnique natTraversalTechnique;
    private final Socket controlConnection;

    public SocketTask(final String targetId, final NATTraversalTechnique natTraversalTechnique,
            final Socket controlConnection) {
        this.natTraversalTechnique = natTraversalTechnique;
        this.targetId = targetId;
        this.controlConnection = controlConnection;
    }

    @Override
    public Socket call() throws Exception {
        Socket connectedSocket = this.createSource();
        logger.info("Connection with {} created successfully via {}.", this.targetId, //$NON-NLS-1$
                this.natTraversalTechnique.getMetaData().getTraversalTechniqueName());
        return connectedSocket;
    }

    private Socket createSource() throws ConnectionNotEstablishedException {
        logger.debug(
                "Trying to connect to {} via {}.", this.targetId, this.natTraversalTechnique.getMetaData().getTraversalTechniqueName()); //$NON-NLS-1$
        Socket connectedSocket = this.natTraversalTechnique.createSourceSideConnection(this.targetId,
                this.controlConnection);
        if (connectedSocket == null || !connectedSocket.isConnected()) {
            throw new ConnectionNotEstablishedException(this.natTraversalTechnique.getMetaData()
                    .getTraversalTechniqueName(), "Connection attempt was not successful.", null); //$NON-NLS-1$
        }

        return connectedSocket;
    }
}
