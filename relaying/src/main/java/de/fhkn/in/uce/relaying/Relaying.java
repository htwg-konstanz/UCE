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
package de.fhkn.in.uce.relaying;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.plugininterface.ConnectionNotEstablishedException;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;
import de.fhkn.in.uce.plugininterface.NATTraversalTechniqueMetaData;
import de.fhkn.in.uce.relaying.core.RelayingClient;
import de.fhkn.in.uce.relaying.message.RelayingAttribute;
import de.fhkn.in.uce.stun.attribute.Username;
import de.fhkn.in.uce.stun.attribute.XorMappedAddress;
import de.fhkn.in.uce.stun.header.STUNMessageClass;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageReader;
import de.fhkn.in.uce.stun.message.MessageStaticFactory;

/**
 * Implementation of {@link NATTraversalTechnique} which realizes a indirect
 * connection by using a relay server.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class Relaying implements NATTraversalTechnique {
    private static final Logger logger = LoggerFactory.getLogger(Relaying.class);
    private static final String BUNDLE_NAME_RELAYING_PROPERTIES = "de.fhkn.in.uce.relaying.relaying"; //$NON-NLS-1$
    private final NATTraversalTechniqueMetaData metaData;
    private final InetSocketAddress relayAddress;
    private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME_RELAYING_PROPERTIES);

    // private RelayingClient targetRelayClient = null;

    // private volatile boolean isRegistered = false;

    public Relaying() {
        try {
            this.metaData = new RelayingMetaData();
            this.relayAddress = this.getRelayServerAddressFromBundle();
        } catch (final Exception e) {
            logger.error("Exception occured while creating relaying connection object.", e); //$NON-NLS-1$
            throw new RuntimeException("Could not create relaying connection object.", e); //$NON-NLS-1$
        }
    }

    public Relaying(final Relaying toCopy) {
        try {
            this.metaData = new RelayingMetaData((RelayingMetaData) toCopy.getMetaData());
            this.relayAddress = toCopy.relayAddress;
        } catch (final Exception e) {
            logger.error("Exception occured while creating relaying connection object.", e); //$NON-NLS-1$
            throw new RuntimeException("Could not create relaying connection object.", e); //$NON-NLS-1$
        }
    }

    private InetSocketAddress getRelayServerAddressFromBundle() throws Exception {
        final String host = this.bundle.getString("relaying.server.ip"); //$NON-NLS-1$
        final String port = this.bundle.getString("relaying.server.port"); //$NON-NLS-1$
        return new InetSocketAddress(host, Integer.valueOf(port));
    }

    @Override
    public Socket createSourceSideConnection(final String targetId, final Socket controlConnection)
            throws ConnectionNotEstablishedException {
        try {
            this.sendConnectionRequest(targetId, controlConnection);
            final Message responseMessage = this.receiveConnectionResponse(controlConnection);
            final InetSocketAddress endpointAtRelayServer = this.getEndpointFromMessage(responseMessage);
            return this.connectToTargetEndpoint(endpointAtRelayServer);
        } catch (final Exception e) {
            logger.error(e.getMessage());
            throw new ConnectionNotEstablishedException(this.metaData.getTraversalTechniqueName(),
                    "Source-side socket could not be created.", e); //$NON-NLS-1$
        }
    }

    private void sendConnectionRequest(final String targetId, final Socket controlConnection) throws Exception {
        logger.debug("Sending connection request"); //$NON-NLS-1$
        final Message requestConnectionMessage = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                STUNMessageMethod.CONNECTION_REQUEST);
        requestConnectionMessage.addAttribute(new Username(targetId));
        requestConnectionMessage.addAttribute(new RelayingAttribute());
        requestConnectionMessage.writeTo(controlConnection.getOutputStream());
    }

    private Message receiveConnectionResponse(final Socket controlConnection) throws IOException {
        final MessageReader messageReader = MessageReader.createMessageReader();
        return messageReader.readSTUNMessage(controlConnection.getInputStream());
    }

    private InetSocketAddress getEndpointFromMessage(final Message msg) throws Exception {
        InetSocketAddress result = null;
        if (msg.hasAttribute(XorMappedAddress.class)) {
            result = msg.getAttribute(XorMappedAddress.class).getEndpoint();
        } else {
            final String errorMessage = "The target endpoint at relay is not returned by the mediator."; //$NON-NLS-1$
            logger.debug(errorMessage);
            throw new ConnectionNotEstablishedException(this.metaData.getTraversalTechniqueName(), errorMessage, null);
        }
        return result;
    }

    private Socket connectToTargetEndpoint(final InetSocketAddress endpoint) throws Exception {
        final Socket socket = new Socket();
        socket.setReuseAddress(true);
        socket.connect(endpoint, endpoint.getPort());
        return socket;
    }

    @Override
    public Socket createTargetSideConnection(final String targetId, final Socket controlConnection,
            final Message connectioRequestMessage) throws ConnectionNotEstablishedException {
        Socket socket = new Socket();
        try {
            final RelayingClient targetRelayClient = new RelayingClient(this.relayAddress);
            final InetSocketAddress endpointAtRelay = this.createAllocationAtRelayServer(targetRelayClient);
            logger.debug("Allocation at relay server created: {}", endpointAtRelay.toString());
            this.sendConnectionRequestResponse(controlConnection, connectioRequestMessage, endpointAtRelay);
            socket = targetRelayClient.accept();
        } catch (final Exception e) {
            logger.error(e.getMessage());
            throw new ConnectionNotEstablishedException(this.metaData.getTraversalTechniqueName(),
                    "Could not create target-side conenction.", e); //$NON-NLS-1$
        }
        return socket;
    }

    private void sendConnectionRequestResponse(final Socket controlConnection, final Message connectionRequest,
            final InetSocketAddress endpointAtRelay) throws IOException {
        final Message response = connectionRequest.buildSuccessResponse();
        response.addAttribute(new RelayingAttribute());
        XorMappedAddress endpointAtRelayAttribute;
        if (endpointAtRelay.getAddress() instanceof Inet6Address) {
            endpointAtRelayAttribute = new XorMappedAddress(endpointAtRelay, ByteBuffer.wrap(
                    response.getHeader().getTransactionId()).getInt());
        } else {
            endpointAtRelayAttribute = new XorMappedAddress(endpointAtRelay);
        }
        response.addAttribute(endpointAtRelayAttribute);
        response.writeTo(controlConnection.getOutputStream());
    }

    @Override
    public void registerTargetAtMediator(final String targetId, final Socket controlConnection) throws Exception {
        // can be used to use traversal technique without connectivity manager
    }

    private InetSocketAddress createAllocationAtRelayServer(final RelayingClient relayingClient) throws Exception {
        logger.debug("Creating allocation at relay server");
        InetSocketAddress result = relayingClient.createAllocation();
        if (result.getAddress().isAnyLocalAddress()) {
            result = new InetSocketAddress(this.relayAddress.getAddress(), result.getPort());
        }
        logger.debug("Allocation at relay server created: {}", result.toString());
        return result;
    }

    @Override
    public void deregisterTargetAtMediator(final String targetId, final Socket controlConnection) throws Exception {
        // can be used to use traversal technique without connectivity manager
    }

    @Override
    public NATTraversalTechniqueMetaData getMetaData() {
        return this.metaData;
    }

    @Override
    public NATTraversalTechnique copy() {
        return new Relaying(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.metaData == null) ? 0 : this.metaData.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final Relaying other = (Relaying) obj;
        if (this.metaData == null) {
            if (other.metaData != null) {
                return false;
            }
        } else if (!this.metaData.equals(other.metaData)) {
            return false;
        }
        return true;
    }
}
