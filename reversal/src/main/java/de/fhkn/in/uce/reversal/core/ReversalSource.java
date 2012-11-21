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
    private final Socket controlConnection;
    private final int socketTimeoutInMillis = 10000;

    /**
     * Creates a {@link ReversalSource} with the given mediator address.
     * 
     * @param mediatorAddress
     *            the mediator address
     */
    public ReversalSource(final InetSocketAddress mediatorAddress) {
        try {
            this.controlConnection = new Socket();
            this.controlConnection.setReuseAddress(true);
            this.controlConnection.connect(mediatorAddress);
        } catch (final Exception e) {
            logger.error("Exception occured while creating connection reversal source object.", e); //$NON-NLS-1$
            throw new RuntimeException("Could not create connection reversal source object.", e); //$NON-NLS-1$
        }
    }

    /**
     * Method to request a connection. Creates a server socket the source
     * listens and waits for a connection.
     * 
     * @param uniqueUserName
     *            the name of the target
     * @return the server socket the target connects to
     * @throws IOException
     */
    private ServerSocket requestConnection(final String uniqueUserName) throws IOException {
        final ServerSocket serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(this.controlConnection.getLocalPort()));
        final Message requestMessage = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                STUNMessageMethod.CONNECTION_REQUEST);
        requestMessage.addAttribute(new Username(uniqueUserName));
        logger.info("send connection request for target {}", uniqueUserName); //$NON-NLS-1$
        requestMessage.writeTo(this.controlConnection.getOutputStream());
        serverSocket.setSoTimeout(this.socketTimeoutInMillis);
        return serverSocket;
    }

    /**
     * Connects to the given target name and returns the according socket.
     * 
     * @param uniqueUserName
     *            the name of the target
     * @return the socket to the target
     * @throws IOException
     */
    public Socket connect(final String uniqueUserName) throws IOException {
        final ServerSocket serverSocket = this.requestConnection(uniqueUserName);
        logger.info("listen on serverSocket {}", serverSocket); //$NON-NLS-1$
        final Socket socket = serverSocket.accept();
        serverSocket.close();
        return socket;
    }
}
