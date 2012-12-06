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
    private final Socket socket;
    private final InetSocketAddress primaryAddress;
    private final InetSocketAddress secondaryAddress;
    private final MessageReader messageReader;

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
    }

    @Override
    public void run() {
        while (this.socket.isConnected()) {
            try {
                final Message inMessage = this.receiveMessage();
                logger.debug("received message at local {}:{}", this.socket.getLocalAddress(), //$NON-NLS-1$
                        this.socket.getLocalPort());
                this.handleMessage(inMessage);
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
                logger.debug("Closing socket if not already done"); //$NON-NLS-1$
                if (!this.socket.isClosed()) {
                    try {
                        this.socket.close();
                    } catch (final IOException e1) {
                        logger.error(e.getMessage(), e);
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
            this.handleSimpleBindingRequest(toHandle);
        } else if (this.isPrimaryAddress() && toHandle.hasAttribute(ChangeRequest.class)) {
            this.handleMessageWithChangeRequestAttribute(toHandle);
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

    private void handleMessageWithChangeRequestAttribute(final Message toHandle) throws Exception {
        final ChangeRequest changeRequest = toHandle.getAttribute(ChangeRequest.class);
        if (changeRequest.isChangeIp() && changeRequest.isChangePort()) {
            this.sendIndicationViaNewSocket(this.secondaryAddress, toHandle);
        } else if (!changeRequest.isChangeIp() && changeRequest.isChangePort()) {
            this.sendIndicationViaNewSocket(new InetSocketAddress(this.primaryAddress.getAddress(),
                    this.secondaryAddress.getPort()), toHandle);
        } else if (!changeRequest.isChangeIp() && !changeRequest.isChangePort()) {
            // for checking connection dependent filtering, not part of RFC 5780
            this.sendIndicationViaNewSocket(this.primaryAddress, toHandle);
        }
    }

    private void sendIndicationViaNewSocket(final InetSocketAddress bindAddress, final Message message)
            throws Exception {
        final Message indication = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.INDICATION,
                STUNMessageMethod.BINDING);
        indication.addAttribute(this.getPublicClientAddressAsAttribute(message));
        final Socket newSocket = new Socket();
        newSocket.bind(new InetSocketAddress(this.primaryAddress.getAddress(), this.secondaryAddress.getPort()));
        newSocket.setReuseAddress(true);
        newSocket.connect(this.socket.getRemoteSocketAddress());
        indication.writeTo(newSocket.getOutputStream());
        newSocket.close();
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
