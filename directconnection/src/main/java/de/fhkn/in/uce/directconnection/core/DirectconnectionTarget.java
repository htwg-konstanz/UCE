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
package de.fhkn.in.uce.directconnection.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.directconnection.message.DirectconnectionAttribute;
import de.fhkn.in.uce.stun.message.Message;

/**
 * Target of direct connection. No specific NAT traversal technique is used.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class DirectconnectionTarget {
    private static final Logger logger = LoggerFactory.getLogger(DirectconnectionTarget.class);

    /**
     * Creates a {@link DirectconnectionTarget} object.
     */
    public DirectconnectionTarget() {
        super();
    }

    /**
     * Creates a server socket and waits for incoming connections. The local
     * address of the control connection is used to bind the server socket.
     * 
     * @param controlConnection
     *            the control connection to the mediator
     * @return the socket which is connected to the source
     * @throws Exception
     */
    public Socket establishTargetSideConnection(final Socket controlConnection, final Message connectionRequestMessage)
            throws Exception {
        final InetSocketAddress localAddress = new InetSocketAddress(controlConnection.getLocalAddress(),
                controlConnection.getLocalPort());
        logger.debug("LocalAddress={}", localAddress.toString()); //$NON-NLS-1$
        final ServerSocket serverSocket = this.createBoundServerSocket(localAddress);
        logger.debug(
                "Server socket listening on {}:{}", serverSocket.getLocalSocketAddress().toString(), serverSocket.getLocalPort()); //$NON-NLS-1$
        this.sendResponseForTargetIsReady(controlConnection, connectionRequestMessage);
        logger.debug("response sent and waiting for incoming connection"); //$NON-NLS-1$
        final Socket result = serverSocket.accept();
        logger.debug("Returning connected socket"); //$NON-NLS-1$
        return result;
    }

    private ServerSocket createBoundServerSocket(final InetSocketAddress bindAddress) throws IOException {
        final ServerSocket serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(bindAddress);
        return serverSocket;
    }

    private void sendResponseForTargetIsReady(final Socket controlConnection, final Message connectionRequestMessage)
            throws IOException {
        final Message response = connectionRequestMessage.buildSuccessResponse();
        response.addAttribute(new DirectconnectionAttribute());
        response.writeTo(controlConnection.getOutputStream());
    }
}
