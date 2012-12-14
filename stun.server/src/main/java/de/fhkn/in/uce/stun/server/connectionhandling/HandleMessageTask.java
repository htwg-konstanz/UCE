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
package de.fhkn.in.uce.stun.server.connectionhandling;

import java.io.EOFException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.stun.attribute.ChangeRequest;
import de.fhkn.in.uce.stun.attribute.OtherAddress;
import de.fhkn.in.uce.stun.attribute.XorMappedAddress;
import de.fhkn.in.uce.stun.header.STUNMessageClass;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageReader;
import de.fhkn.in.uce.stun.message.MessageStaticFactory;

/**
 * The {@link HandleMessageTask} handles STUN messages according to RFC 5780 to
 * examine the behavior of a NAT-device. The server only handles TCP connections
 * and instantiates connections to examine the filtering behavior of a NAT. For
 * this purpose the stun server sends indications to the stun client because it
 * is forbidden to send a response via a new connection.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class HandleMessageTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(HandleMessageTask.class);
    private static final int TIMEOUT_IN_SECONDS = 10 * 1000;
    private final Socket socket;
    private final InetSocketAddress primaryAddress;
    private final InetSocketAddress secondaryAddress;
    private final MessageReader messageReader;

    private final int thirdPort;

    /**
     * Creates a {@link HandleMessageTask} to handle STUN messages over TCP.
     * 
     * @param s
     *            the socket to read messages
     * @param primaryAddress
     *            the primary (local) address of the stun server
     * @param secondaryAddress
     *            the secondary/alternate (local) address of the stun server
     */
    public HandleMessageTask(final Socket s, final InetSocketAddress primaryAddress,
            final InetSocketAddress secondaryAddress) {
        this.socket = s;
        this.primaryAddress = primaryAddress;
        this.secondaryAddress = secondaryAddress;
        this.messageReader = MessageReader.createMessageReader();
        this.thirdPort = secondaryAddress.getPort() + 1;
    }

    @Override
    public void run() {
        while (this.socket.isConnected() && !this.socket.isClosed()) {
            try {
                final Message inMessage = this.receiveMessage();
                logger.debug("received message at local {}:{}", this.socket.getLocalAddress(), //$NON-NLS-1$
                        this.socket.getLocalPort());
                this.handleMessage(inMessage);
            } catch (final EOFException eofe) {
                // TODO why is this exception thrown?
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
                if (!this.socket.isClosed()) {
                    logger.debug("Closing socket"); //$NON-NLS-1$
                    try {
                        this.socket.close();
                    } catch (final IOException e1) {
                        // logger.error(e.getMessage(), e);
                    }
                }
                return;
            }
        }
    }

    private Message receiveMessage() throws Exception {
        return this.messageReader.readSTUNMessage(this.socket.getInputStream());
    }

    private final void handleMessage(final Message toHandle) throws Exception {
        if (toHandle.isMethod(STUNMessageMethod.BINDING)) {
            this.handleBindingMessage(toHandle);
        } else {
            logger.debug("Can not handle message with method {}", toHandle.getMessageMethod()); //$NON-NLS-1$
        }
    }

    private void handleBindingMessage(final Message toHandle) throws Exception {
        if (toHandle.isRequest() && toHandle.isMethod(STUNMessageMethod.BINDING)) {
            logger.debug("handling binding request"); //$NON-NLS-1$
            this.handleSimpleBindingRequest(toHandle);
        } else if (this.isPrimaryAddress() && toHandle.hasAttribute(ChangeRequest.class)) {
            final InetSocketAddress remoteAddress = new InetSocketAddress(this.socket.getInetAddress(),
                    this.socket.getPort());
            logger.debug("handling message with change request attribute"); //$NON-NLS-1$
            this.handleMessageWithChangeRequestAttribute(toHandle, remoteAddress);
        }
    }

    private void handleSimpleBindingRequest(final Message toHandle) throws Exception {
        final Message response = toHandle.buildSuccessResponse();
        final OtherAddress otherAddress = new OtherAddress(this.secondaryAddress);
        response.addAttribute(otherAddress);
        final XorMappedAddress clientAddress = this.getPublicClientAddressAsAttribute(toHandle);
        response.addAttribute(clientAddress);
        response.writeTo(this.socket.getOutputStream());
    }

    private void handleMessageWithChangeRequestAttribute(final Message toHandle, final InetSocketAddress remoteAddress)
            throws Exception {
        final ChangeRequest changeRequest = toHandle.getAttribute(ChangeRequest.class);
        logger.debug("Getting indication with change request flag = {}", changeRequest.getFlag()); //$NON-NLS-1$
        switch (changeRequest.getFlag()) {
        case ChangeRequest.CHANGE_IP_AND_PORT:
            this.sendIndicationViaNewSocket(new InetSocketAddress(this.secondaryAddress.getAddress(), this.thirdPort),
                    toHandle, remoteAddress);
            break;
        case ChangeRequest.CHANGE_PORT:
            this.sendIndicationViaNewSocket(new InetSocketAddress(this.primaryAddress.getAddress(), this.thirdPort),
                    toHandle, remoteAddress);
            break;
        case ChangeRequest.FLAGS_NOT_SET:
            // for checking connection dependent filtering, not part of RFC 5780
            // the server should try to establish a connection from the primary
            // address but this does not work under linux
            // this.socket.close();
            // logger.debug("socket to client closed");
            // this.waitForSocketClosed();
            // this.sendIndicationViaNewSocket(this.primaryAddress, toHandle,
            // remoteAddress);
            break;
        case ChangeRequest.CHANGE_IP:
            this.sendIndicationViaNewSocket(new InetSocketAddress(this.secondaryAddress.getAddress(),
                    this.primaryAddress.getPort()), toHandle, remoteAddress);
            break;
        default:
            break;
        }
    }

    private void sendIndicationViaNewSocket(final InetSocketAddress bindAddress, final Message message,
            final InetSocketAddress remoteAddress) {
        try {
            final Message indication = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.INDICATION,
                    STUNMessageMethod.BINDING);
            indication.addAttribute(this.getPublicClientAddressAsAttribute(message));
            final Socket newSocket = new Socket();
            newSocket.setReuseAddress(true);
            logger.debug("Binding new socket to {}", bindAddress); //$NON-NLS-1$
            newSocket.bind(bindAddress);
            logger.debug("Connecting to {}", remoteAddress.toString()); //$NON-NLS-1$
            newSocket.connect(remoteAddress, TIMEOUT_IN_SECONDS);
            logger.debug("Wrtiting indication"); //$NON-NLS-1$
            indication.writeTo(newSocket.getOutputStream());
            newSocket.close();
        } catch (IOException e) {
            logger.error("Connection to client not successfully established: {}", e.getMessage()); //$NON-NLS-1$
        }
    }

    private boolean isPrimaryAddress() {
        return this.socket.getLocalAddress().equals(this.primaryAddress.getAddress())
                && this.socket.getLocalPort() == this.primaryAddress.getPort();
    }

    private XorMappedAddress getPublicClientAddressAsAttribute(final Message message) {
        XorMappedAddress clientAddress;
        final InetSocketAddress publicClientAddress = new InetSocketAddress(this.socket.getInetAddress(),
                this.socket.getPort());
        if (publicClientAddress.getAddress() instanceof Inet4Address) {
            clientAddress = new XorMappedAddress(publicClientAddress);
        } else {
            clientAddress = new XorMappedAddress(publicClientAddress, ByteBuffer.wrap(
                    message.getHeader().getTransactionId()).getInt());
        }
        return clientAddress;
    }
}
