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

import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.directconnection.message.DirectconnectionAttribute;
import de.fhkn.in.uce.stun.attribute.Username;
import de.fhkn.in.uce.stun.attribute.XorMappedAddress;
import de.fhkn.in.uce.stun.header.STUNMessageClass;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageReader;
import de.fhkn.in.uce.stun.message.MessageStaticFactory;

/**
 * Class to create a source-side direct connection. For that No NAT traversal
 * technique is used.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class DirectconnectionSource {
    private static final Logger logger = LoggerFactory.getLogger(DirectconnectionSource.class);

    /**
     * Creates a {@link DirectconnectionSource} object.
     */
    public DirectconnectionSource() {
        super();
    }

    /**
     * Establishes a source-side connection to the given target. The mediator is
     * asked for the necessary endpoint.
     * 
     * @param targetId
     *            the id of the target to connect
     * @param controlConnection
     *            the control connection to the mediator
     * @return the connected socket
     * @throws Exception
     */
    public Socket establishSourceSideConnection(final String targetId, final Socket controlConnection) throws Exception {
        this.sendConnectionRequest(targetId, controlConnection);
        final InetSocketAddress targetAddress = this.processConnectionRequestResponse(controlConnection);
        logger.debug("Creating socket to {}", targetAddress.toString());
        return this.createSocketToEndpoint(targetAddress, new InetSocketAddress(controlConnection.getLocalAddress(),
                controlConnection.getLocalPort()));
    }

    private void sendConnectionRequest(final String targetId, final Socket controlConnection) throws Exception {
        final Message requestConnectionMessage = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                STUNMessageMethod.CONNECTION_REQUEST);
        requestConnectionMessage.addAttribute(new DirectconnectionAttribute());
        requestConnectionMessage.addAttribute(new Username(targetId));
        requestConnectionMessage.writeTo(controlConnection.getOutputStream());
    }

    private InetSocketAddress processConnectionRequestResponse(final Socket controlConnection) throws Exception {
        InetSocketAddress result;
        final MessageReader messageReader = MessageReader.createMessageReader();
        final Message responseMessage = messageReader.readSTUNMessage(controlConnection.getInputStream());
        if (responseMessage.hasAttribute(XorMappedAddress.class)) {
            final XorMappedAddress xorMappedAddress = responseMessage.getAttribute(XorMappedAddress.class);
            result = xorMappedAddress.getEndpoint();
        } else {
            throw new Exception("The target endpoint is not returned by the mediator"); //$NON-NLS-1$
        }
        return result;
    }

    private Socket createSocketToEndpoint(final InetSocketAddress targetAddress, final InetSocketAddress bindAddress)
            throws Exception {
        final Socket socket = new Socket();
        socket.setReuseAddress(true);
        socket.bind(bindAddress);
        socket.connect(targetAddress);
        return socket;
    }
}
