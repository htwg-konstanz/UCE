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

import de.fhkn.in.uce.stun.message.Message;

/**
 * Target of direct connection. No specific NAT traversal technique is used.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class DirectconnectionTarget {

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
        final ServerSocket serverSocket = this.createBoundServerSocket(localAddress);
        this.sendResponseForTargetIsReady(controlConnection, connectionRequestMessage);
        return serverSocket.accept();
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
        response.writeTo(controlConnection.getOutputStream());
    }

    // /**
    // * Sends a register message to the mediator.
    // *
    // * @param targetId
    // * the id of the target
    // * @param controlConnection
    // * the connection to the mediator
    // * @throws Exception
    // */
    // public void registerTarget(final String targetId, final Socket
    // controlConnection) throws Exception {
    // final Message registerMessage =
    // MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
    // STUNMessageMethod.REGISTER);
    // final Username userName = new Username(targetId);
    // registerMessage.addAttribute(userName);
    // registerMessage.writeTo(controlConnection.getOutputStream());
    // }

    // /**
    // * Sends a deregister message to the mediator.
    // *
    // * @param targetId
    // * the id of the target
    // * @param controlConnection
    // * the control connection to the mediator
    // * @throws Exception
    // */
    // public void deregisterTarget(final String targetId, final Socket
    // controlConnection) throws Exception {
    // final Message deregisterMessage =
    // MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
    // STUNMessageMethod.DEREGISTER);
    // deregisterMessage.addAttribute(new Username(targetId));
    // deregisterMessage.writeTo(controlConnection.getOutputStream());
    // }
}
