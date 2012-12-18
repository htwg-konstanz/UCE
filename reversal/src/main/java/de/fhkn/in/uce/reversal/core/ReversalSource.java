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
package de.fhkn.in.uce.reversal.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.reversal.message.ReversalAttribute;
import de.fhkn.in.uce.stun.attribute.Username;
import de.fhkn.in.uce.stun.header.STUNMessageClass;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageStaticFactory;

/**
 * Class that implements the source of the connection reversal.
 * 
 * @author thomas zink, stefan lohr, Alexander Diener
 *         (aldiener@htwg-konstanz.de)
 */
public final class ReversalSource {
    private static final Logger logger = LoggerFactory.getLogger(ReversalSource.class);
    private final int socketTimeoutInMillis = 60 * 1000;

    /**
     * Creates a {@link ReversalSource} object.
     */
    public ReversalSource() {
        super();
    }

    /**
     * Connects to the given target name and returns the according socket. A
     * server socket is created which listens to incoming connections from the
     * target. To initiate a connection, a connection request is sent over the
     * control connection.
     * 
     * @param uniqueUserName
     *            the name of the target
     * @return the socket to the target
     * @throws IOException
     */
    public Socket establishSourceSideConnection(final String uniqueUserName, final Socket controlConnection)
            throws IOException {
        final ServerSocket serverSocket = this.createBoundServerSocket(uniqueUserName, controlConnection);
        logger.info("listen on serverSocket {}", serverSocket); //$NON-NLS-1$
        this.sendConnectionRequest(uniqueUserName, controlConnection);
        final Socket socket = serverSocket.accept();
        serverSocket.close();
        return socket;
    }

    private ServerSocket createBoundServerSocket(final String uniqueUserName, final Socket controlConnection)
            throws IOException {
        final ServerSocket serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(controlConnection.getLocalAddress(), controlConnection.getLocalPort()));
        serverSocket.setSoTimeout(this.socketTimeoutInMillis);
        return serverSocket;
    }

    private void sendConnectionRequest(final String targetId, final Socket controlConnection) throws IOException {
        final Message requestMessage = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                STUNMessageMethod.CONNECTION_REQUEST);
        requestMessage.addAttribute(new Username(targetId));
        requestMessage.addAttribute(new ReversalAttribute());
        logger.info("send connection request for target {}", targetId); //$NON-NLS-1$
        requestMessage.writeTo(controlConnection.getOutputStream());
    }
}
