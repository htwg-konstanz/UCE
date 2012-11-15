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
package de.fhkn.in.uce.directconnection;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.messages.CommonUceMethod;
import de.fhkn.in.uce.messages.SemanticLevel;
import de.fhkn.in.uce.messages.SocketEndpoint;
import de.fhkn.in.uce.messages.UceMessage;
import de.fhkn.in.uce.messages.UceMessageReader;
import de.fhkn.in.uce.messages.UceMessageStaticFactory;
import de.fhkn.in.uce.messages.UniqueUserName;
import de.fhkn.in.uce.plugininterface.ConnectionNotEstablishedException;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;
import de.fhkn.in.uce.plugininterface.NATTraversalTechniqueMetaData;

/**
 * Implementation of {@link NATTraversalTechnique} which establishes a direct
 * connection without using any NAT traversal technique.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class Directconnection implements NATTraversalTechnique {
    private final NATTraversalTechniqueMetaData metaData;
    private final Socket controlConnection;
    private final ScheduledExecutorService keepAliveExecutor = Executors.newSingleThreadScheduledExecutor();
    private static final int ITERATION_TIME_IN_SECONDS = 60;
    private static final Logger logger = LoggerFactory.getLogger(Directconnection.class);

    /**
     * Creates a {@link Directconnection} object with the corresponding
     * {@link NATTraversalTechniqueMetaData}.
     */
    public Directconnection() {
        try {
            this.metaData = new DirectconnectionMetaData();
            this.controlConnection = new Socket();
            this.controlConnection.setReuseAddress(true);
        } catch (final SocketException e) {
            logger.error("Socket option could not be set.", e); //$NON-NLS-1$
            throw new RuntimeException("Socket option could not be set.", e); //$NON-NLS-1$
        } catch (final Exception e) {
            logger.error("Exception occured while creating direct connection object.", e); //$NON-NLS-1$
            throw new RuntimeException("Could not create direct connection object.", e); //$NON-NLS-1$
        }
    }

    /**
     * Creates a copy of the given {@link Directconnection}.
     * 
     * @param toCopy
     *            the {@link Directconnection} to copy
     */
    public Directconnection(final Directconnection toCopy) {
        try {
            this.metaData = new DirectconnectionMetaData((DirectconnectionMetaData) toCopy.metaData);
            this.controlConnection = new Socket();
            this.controlConnection.setReuseAddress(true);
        } catch (final SocketException e) {
            logger.error("Socket option could not be set.", e); //$NON-NLS-1$
            throw new RuntimeException("Socket option could not be set.", e); //$NON-NLS-1$
        } catch (final Exception e) {
            logger.error("Exception occured while creating direct connection object.", e); //$NON-NLS-1$
            throw new RuntimeException("Could not create direct connection object.", e); //$NON-NLS-1$
        }
    }

    @Override
    public Socket createSourceSideConnection(final String targetId, final InetSocketAddress mediatorAddress)
            throws ConnectionNotEstablishedException {
        try {
            this.connectToMediatorIfNotAlreadyConnected(mediatorAddress, 0);
            return this.establishSourceSideConnection(targetId);
        } catch (final Exception e) {
            logger.error(e.getMessage());
            throw new ConnectionNotEstablishedException(this.metaData.getTraversalTechniqueName(),
                    "Source-side socket could not be created.", e); //$NON-NLS-1$
        }
    }

    private Socket establishSourceSideConnection(final String targetId) throws Exception {
        final UceMessage requestConnectionMessage = UceMessageStaticFactory.newUceMessageInstance(
                CommonUceMethod.CONNECTION_REQUEST, SemanticLevel.REQUEST);
        requestConnectionMessage.addAttribute(new UniqueUserName(targetId));
        requestConnectionMessage.writeTo(this.controlConnection.getOutputStream());

        final UceMessageReader messageReader = new UceMessageReader();
        final UceMessage responseMessage = messageReader.readUceMessage(this.controlConnection.getInputStream());

        if (responseMessage.hasAttribute(SocketEndpoint.class)) {
            final SocketEndpoint targetEndpoint = responseMessage.getAttribute(SocketEndpoint.class);
            logger.debug("Connecting to target {}", targetEndpoint.getEndpoint()); //$NON-NLS-1$
            final Socket socket = new Socket();
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(this.controlConnection.getLocalPort()));
            socket.connect(targetEndpoint.getEndpoint());
            return socket;
        } else {
            throw new ConnectionNotEstablishedException(this.metaData.getTraversalTechniqueName(),
                    "The target endpoint is not returned by the mediator.", //$NON-NLS-1$
                    null);
        }
    }

    @Override
    public Socket createTargetSideConnection(final String targetId, final InetSocketAddress mediatorAddress)
            throws ConnectionNotEstablishedException {
        try {
            this.connectToMediatorIfNotAlreadyConnected(mediatorAddress, 0);
            return this.establishTargetSideConnection(targetId);
        } catch (final Exception e) {
            logger.error(e.getMessage());
            throw new ConnectionNotEstablishedException(this.metaData.getTraversalTechniqueName(),
                    "Target-side socket could not be created.", e); //$NON-NLS-1$
        }
    }

    private Socket establishTargetSideConnection(final String targetId) throws Exception {
        Socket socket = null;
        final ServerSocket serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        final SocketAddress localAddress = new InetSocketAddress(this.controlConnection.getLocalPort());
        serverSocket.bind(localAddress);
        logger.info("Waiting for incoming connections on {}", localAddress.toString()); //$NON-NLS-1$
        socket = serverSocket.accept();
        return socket;
    }

    @Override
    public void registerTargetAtMediator(final String targetId, final InetSocketAddress mediatorAddress)
            throws ConnectionNotEstablishedException {
        try {
            this.sendRegisterMessage(targetId, mediatorAddress);
            this.startKeepAliveThread(targetId);
        } catch (final Exception e) {
            final String errorMessage = "Target {} could not be registered successfully."; //$NON-NLS-1$
            logger.error(errorMessage, targetId);
            throw new ConnectionNotEstablishedException(this.metaData.getTraversalTechniqueName(), errorMessage, e);
        }
    }

    private void sendRegisterMessage(final String targetId, final InetSocketAddress mediatorAddress) throws Exception {
        final UceMessage registerMessage = UceMessageStaticFactory.newUceMessageInstance(CommonUceMethod.REGISTER,
                SemanticLevel.REQUEST);
        final UniqueUserName userName = new UniqueUserName(targetId);
        registerMessage.addAttribute(userName);
        this.connectToMediatorIfNotAlreadyConnected(mediatorAddress, 0);
        registerMessage.writeTo(this.controlConnection.getOutputStream());
        // TODO check for success response
    }

    private void startKeepAliveThread(final String targetId) {
        logger.info("Starting keep-alive thread for {}.", this.metaData.getTraversalTechniqueName()); //$NON-NLS-1$
        this.keepAliveExecutor.scheduleAtFixedRate(new KeepAliveTask(targetId, this.controlConnection),
                ITERATION_TIME_IN_SECONDS, ITERATION_TIME_IN_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void deregisterTargetAtMediator(final String targetId, final InetSocketAddress mediatorAddress)
            throws ConnectionNotEstablishedException {
        final UceMessage deregisterMessage = UceMessageStaticFactory.newUceMessageInstance(CommonUceMethod.DEREGISTER,
                SemanticLevel.REQUEST);
        try {
            deregisterMessage.addAttribute(new UniqueUserName(targetId));
            this.connectToMediatorIfNotAlreadyConnected(mediatorAddress, 0);
            deregisterMessage.writeTo(this.controlConnection.getOutputStream());
            // TODO check for success response
        } catch (final Exception e) {
            final String errorMessage = "Exception while deregistering target: {}"; //$NON-NLS-1$
            logger.error(errorMessage, e.getMessage());
            throw new ConnectionNotEstablishedException(this.metaData.getTraversalTechniqueName(), errorMessage, e);
        }
    }

    @Override
    public NATTraversalTechniqueMetaData getMetaData() {
        return this.metaData;
    }

    @Override
    public NATTraversalTechnique copy() {
        return new Directconnection(this);
    }

    private void connectToMediatorIfNotAlreadyConnected(final InetSocketAddress mediatorAddress, final int localPort)
            throws ConnectionNotEstablishedException {
        if (!this.controlConnection.isConnected()) {
            try {
                this.controlConnection.bind(new InetSocketAddress(localPort));
                this.controlConnection.connect(mediatorAddress);
            } catch (final Exception e) {
                final String errorMessage = "Control connection to {}:{} could not be established."; //$NON-NLS-1$
                logger.error(errorMessage, mediatorAddress.getHostName(), mediatorAddress.getPort());
                throw new ConnectionNotEstablishedException(this.metaData.getTraversalTechniqueName(), errorMessage, e);
            }
        }
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
        final Directconnection other = (Directconnection) obj;
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
