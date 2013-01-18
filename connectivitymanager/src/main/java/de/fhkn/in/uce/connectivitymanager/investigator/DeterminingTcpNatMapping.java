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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import net.jcip.annotations.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.plugininterface.NATFeatureRealization;
import de.fhkn.in.uce.stun.attribute.OtherAddress;
import de.fhkn.in.uce.stun.attribute.XorMappedAddress;
import de.fhkn.in.uce.stun.header.STUNMessageClass;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageReader;
import de.fhkn.in.uce.stun.message.MessageStaticFactory;
import de.fhkn.in.uce.stun.message.MessageWriter;
import de.fhkn.in.uce.stun.util.MessageFormatException;

/**
 * Implementation of {@link DeterminingNATFeatureRealization} to investigate the
 * mapping behavior in case of TCP connections. This is a implementation of
 * section 4.3 of RFC 5780. The tests are extended by examining if the
 * realization is connection dependent.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
@Immutable
final class DeterminingTcpNatMapping implements DeterminingNATFeatureRealization {
    private final Logger logger = LoggerFactory.getLogger(DeterminingTcpNatMapping.class);
    private final int sourcePort;
    private final InetSocketAddress primaryStunServerAddress;

    /**
     * Creates a {@link DeterminingTcpNatMapping} with the given source port.
     * The source port should be the same as used in the application.
     * 
     * @param sourcePort
     *            the port which is used by the application
     */
    public DeterminingTcpNatMapping(final int sourcePort, final InetSocketAddress primaryStunServerAddress) {
        this.sourcePort = sourcePort;
        this.primaryStunServerAddress = primaryStunServerAddress;
    }

    @Override
    public NATFeatureRealization executeTest() {
        NATFeatureRealization result = NATFeatureRealization.DONT_CARE;
        try {
            final Message responseI = this.executeTestI(this.primaryStunServerAddress.getAddress(),
                    this.primaryStunServerAddress.getPort());
            final XorMappedAddress mappedAddressI = responseI.getAttribute(XorMappedAddress.class);
            final String localAddress = InetAddress.getLocalHost().getHostAddress();
            if (localAddress.equals(mappedAddressI.getEndpoint().getHostName())
                    && this.sourcePort == mappedAddressI.getEndpoint().getPort()) {
                result = NATFeatureRealization.NOT_REALIZED;
            } else {
                final InetSocketAddress alternateAddress = this.getAlternateSTUNServerAddressFromMessage(responseI);
                final Message responseII = this.executeTestII(alternateAddress.getAddress(),
                        this.primaryStunServerAddress.getPort());
                final XorMappedAddress mappedAddressII = responseII.getAttribute(XorMappedAddress.class);
                if (mappedAddressII.equals(mappedAddressI)) {
                    result = NATFeatureRealization.ENDPOINT_INDEPENDENT;
                } else {
                    final Message responseIII = this.executeTestIII(alternateAddress.getAddress(),
                            alternateAddress.getPort());
                    final XorMappedAddress mappedAddressIII = responseIII.getAttribute(XorMappedAddress.class);
                    if (mappedAddressIII.equals(mappedAddressII)) {
                        result = NATFeatureRealization.ADDRESS_DEPENDENT;
                    } else {
                        final Message responseIV = this.executeTestRun(this.primaryStunServerAddress.getAddress(),
                                this.primaryStunServerAddress.getPort());
                        final XorMappedAddress mappedAddressIV = responseIV.getAttribute(XorMappedAddress.class);
                        if (!mappedAddressIV.equals(mappedAddressI)) {
                            result = NATFeatureRealization.CONNECTION_DEPENDENT;
                        } else {
                            result = NATFeatureRealization.ADDRESS_AND_PORT_DEPENDENT;
                        }
                    }
                }
            }
        } catch (final Exception e) {
            this.logger.error("Exception while investigating NAT mapping behavior.", e);
        }

        return result;
    }

    private Message executeTestI(final InetAddress primaryAddress, final int primaryPort) throws Exception {
        return this.executeTestRun(primaryAddress, primaryPort);
    }

    private Message executeTestII(final InetAddress alternateAddress, final int primaryPort) throws Exception {
        return this.executeTestRun(alternateAddress, primaryPort);
    }

    private Message executeTestIII(final InetAddress alternateAddress, final int alternatePort) throws Exception {
        return this.executeTestRun(alternateAddress, alternatePort);
    }

    private Message executeTestRun(final InetAddress stunServerAddress, final int stunServerPort) throws Exception {
        try {
            final Socket socket = new Socket();
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(this.sourcePort));
            socket.connect(new InetSocketAddress(stunServerAddress, stunServerPort));
            this.sendBindingRequestToStunServer(socket.getOutputStream());
            final Message response = this.receiveBindingResponseFromStunServer(socket.getInputStream());
            socket.close();
            return response;
        } catch (final Exception e) {
            this.logger.error("Exception eccured while executing test", e);
            throw e;
        }
    }

    private void sendBindingRequestToStunServer(final OutputStream out) throws Exception {
        final MessageWriter writer = new MessageWriter(out);
        final Message request = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                STUNMessageMethod.BINDING);
        writer.writeMessage(request);
    }

    private Message receiveBindingResponseFromStunServer(final InputStream in) throws Exception {
        final MessageReader reader = MessageReader.createMessageReader();
        return reader.readSTUNMessage(in);
    }

    private InetSocketAddress getAlternateSTUNServerAddressFromMessage(final Message message) throws Exception {
        if (!message.hasAttribute(OtherAddress.class)) {
            throw new MessageFormatException("The required OTHER-ADDRESS attribute is not provided.");
        }
        final OtherAddress otherAddress = message.getAttribute(OtherAddress.class);
        return otherAddress.getEndpoint();
    }

    public static void main(String[] args) {
        final InetSocketAddress stunServerAddress = new InetSocketAddress("134.34.165.164", 3478);
        final DeterminingNATFeatureRealization mapping = new DeterminingTcpNatMapping(55553, stunServerAddress);
        final NATFeatureRealization mappingRealization = mapping.executeTest();
        System.out.println(mappingRealization.toString());
    }
}
