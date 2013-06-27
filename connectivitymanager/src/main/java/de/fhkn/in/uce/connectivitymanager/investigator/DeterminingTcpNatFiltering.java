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
package de.fhkn.in.uce.connectivitymanager.investigator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import net.jcip.annotations.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.plugininterface.NATFeatureRealization;
import de.fhkn.in.uce.stun.attribute.ChangeRequest;
import de.fhkn.in.uce.stun.attribute.OtherAddress;
import de.fhkn.in.uce.stun.attribute.XorMappedAddress;
import de.fhkn.in.uce.stun.header.STUNMessageClass;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageReader;
import de.fhkn.in.uce.stun.message.MessageStaticFactory;

@Immutable
final class DeterminingTcpNatFiltering implements DeterminingNATFeatureRealization {
    private static final int DEFAULT_TIMEOUT_WAITING_FOR_STUN_SERVER_RESPONSE_IN_MILLIS = (int) 39.5 * 1000;
    private final Logger logger = LoggerFactory.getLogger(DeterminingTcpNatFiltering.class);
    private final InetSocketAddress primaryStunServerAddress;
    private final MessageReader messageReader;
    private final int sourcePort;
    private final int responseTimeoutInMillis;

    public DeterminingTcpNatFiltering(final int sourcePort, final InetSocketAddress primaryStunServerAddress) {
        this(sourcePort, primaryStunServerAddress, DEFAULT_TIMEOUT_WAITING_FOR_STUN_SERVER_RESPONSE_IN_MILLIS);
    }

    public DeterminingTcpNatFiltering(final int sourcePort, final InetSocketAddress primaryStunServerAddress,
            final int timeoutForResponseInSeconds) {
        this.sourcePort = sourcePort;
        this.primaryStunServerAddress = primaryStunServerAddress;
        this.messageReader = MessageReader.createMessageReader();
        this.responseTimeoutInMillis = timeoutForResponseInSeconds * 1000;
    }

    @Override
    public NATFeatureRealization executeTest() {
        NATFeatureRealization result = NATFeatureRealization.DONT_CARE;
        Socket toStunServer = null;
        try {
            toStunServer = this.createConnectedSocket(this.primaryStunServerAddress);
            final Message responseTestI = this.executeTestI(toStunServer);
            if (responseTestI.hasAttribute(OtherAddress.class)) {
                try {
                    final Message responseTestII = this.executeTestII(toStunServer);
                    if (responseTestII.hasAttribute(XorMappedAddress.class)) {
                        result = NATFeatureRealization.ENDPOINT_INDEPENDENT;
                    }
                } catch (final SocketTimeoutException eII) {
                    try {
                        final Message responseTestIII = this.executeTestIII(toStunServer);
                        if (responseTestIII.hasAttribute(XorMappedAddress.class)) {
                            result = NATFeatureRealization.ADDRESS_DEPENDENT;
                        }
                    } catch (final SocketTimeoutException eIII) {
                        try {
                            final Message reponseIV = this.executeTestForConnectionDependent(toStunServer);
                            if (reponseIV.hasAttribute(XorMappedAddress.class)) {
                                result = NATFeatureRealization.ADDRESS_AND_PORT_DEPENDENT;
                            }
                        } catch (final SocketTimeoutException eIV) {
                            result = NATFeatureRealization.CONNECTION_DEPENDENT;
                        }
                    }
                }
            }
        } catch (final Exception e) {
            this.logger.error("Exception while executing tests for determining tcp filtering behavior", e);
        } finally {
            try {
                if ((null != toStunServer) && toStunServer.isConnected()) {
                    toStunServer.close();
                }
            } catch (IOException e) {
                // do nothing
            }
        }
        return result;
    }

    private Message executeTestI(final Socket toStunServer) throws IOException {
        final Message bindingRequest = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                STUNMessageMethod.BINDING);
        bindingRequest.writeTo(toStunServer.getOutputStream());
        final Message bindingResponse = this.messageReader.readSTUNMessage(toStunServer.getInputStream());
        return bindingResponse;
    }

    private Message executeTestII(final Socket toStunServer) throws IOException {
        this.sendIndicationWithChangeRequestAttribute(ChangeRequest.CHANGE_IP_AND_PORT, toStunServer);
        final Message receivedMessage = this.receiveMessageFromStunServer();
        return receivedMessage;
    }

    private Message executeTestIII(final Socket toStunServer) throws IOException {
        this.sendIndicationWithChangeRequestAttribute(ChangeRequest.CHANGE_PORT, toStunServer);
        final Message response = this.receiveMessageFromStunServer();
        return response;
    }

    private Message executeTestForConnectionDependent(final Socket toStunServer) throws IOException {
        this.sendIndicationWithChangeRequestAttribute(ChangeRequest.FLAGS_NOT_SET, toStunServer);
        final Message receivedMessage = this.receiveMessageFromStunServer();
        return receivedMessage;
    }

    private void sendIndicationWithChangeRequestAttribute(final int changeRequestFlag, final Socket sendSocket)
            throws IOException {
        logger.debug("Sending indication with change request flag = {}", changeRequestFlag); //$NON-NLS-1$
        final Message indication = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.INDICATION,
                STUNMessageMethod.BINDING);
        indication.addAttribute(new ChangeRequest(changeRequestFlag));
        indication.writeTo(sendSocket.getOutputStream());
    }

    private Message receiveMessageFromStunServer() throws IOException {
        final ServerSocket serverSocket = this.createBoundServerSocket();
        final Socket socket = serverSocket.accept();
        final Message receivedMessage = this.messageReader.readSTUNMessage(socket.getInputStream());
        serverSocket.close();
        socket.close();
        return receivedMessage;
    }

    private ServerSocket createBoundServerSocket() throws IOException {
        final ServerSocket serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.setSoTimeout(this.responseTimeoutInMillis);
        serverSocket.bind(new InetSocketAddress(this.sourcePort));
        return serverSocket;
    }

    private Socket createConnectedSocket(final InetSocketAddress address) throws IOException {
        final Socket socket = new Socket();
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(this.sourcePort));
        logger.debug("connecting to address {}", address); //$NON-NLS-1$
        socket.connect(address);
        return socket;
    }

    public static void main(String[] args) {
        final InetSocketAddress stunServerAddress = new InetSocketAddress("134.34.165.164", 3478);
        final DeterminingNATFeatureRealization filtering = new DeterminingTcpNatFiltering(55554, stunServerAddress, 15);
        final NATFeatureRealization filteringRealization = filtering.executeTest();
        System.out.println(filteringRealization.toString());
    }
}
