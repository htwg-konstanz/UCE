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
package de.fhkn.in.uce.holepunching;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.holepunching.core.source.HolePunchingSource;
import de.fhkn.in.uce.holepunching.core.target.HolePunchingTarget;
import de.fhkn.in.uce.holepunching.message.HolePunchingAttribute;
import de.fhkn.in.uce.plugininterface.ConnectionNotEstablishedException;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;
import de.fhkn.in.uce.plugininterface.NATTraversalTechniqueMetaData;
import de.fhkn.in.uce.stun.attribute.XorMappedAddress;
import de.fhkn.in.uce.stun.message.Message;

/**
 * Implementation of {@link NATTraversalTechnique} for parallel hole punching.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class HolePunching implements NATTraversalTechnique {
    private static final Logger logger = LoggerFactory.getLogger(HolePunching.class);
    private final NATTraversalTechniqueMetaData metaData;

    // private HolePunchingTarget target;

    /**
     * Creates a new {@link HolePunching} object.
     */
    public HolePunching() {
        try {
            this.metaData = new HolePunchingMetaData();
        } catch (final Exception e) {
            final String errorMessage = "Could not create HolePunching object"; //$NON-NLS-1$
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    /**
     * Copy constructor for {@link HolePunching}.
     * 
     * @param toCopy
     *            the {@link HolePunching} to copy
     */
    public HolePunching(final HolePunching toCopy) {
        this.metaData = new HolePunchingMetaData((HolePunchingMetaData) toCopy.getMetaData());
    }

    @Override
    public NATTraversalTechniqueMetaData getMetaData() {
        return this.metaData;
    }

    @Override
    public Socket createSourceSideConnection(final String targetId, final Socket controlConnection)
            throws ConnectionNotEstablishedException {
        Socket result = null;
        final HolePunchingSource source = new HolePunchingSource();
        try {
            result = source.establishSourceSideConnection(targetId, controlConnection);
        } catch (final IOException e) {
            final String errorMessage = "Could not create source-side onnection"; //$NON-NLS-1$
            logger.error(errorMessage, e);
            throw new ConnectionNotEstablishedException(this.metaData.getTraversalTechniqueName(), errorMessage, e);
        }
        return result;
    }

    @Override
    public Socket createTargetSideConnection(final String targetId, final Socket controlConnection,
            final Message connectionRequestMessage) throws ConnectionNotEstablishedException {
        // this.checkIfTargetIsInitialized();
        try {
            HolePunchingTarget target = new HolePunchingTarget(controlConnection, targetId);
            logger.debug("Sending connection request response"); //$NON-NLS-1$
            this.sendConnectionRequestResponse(controlConnection, connectionRequestMessage);
            logger.debug("Starting hole punching target"); //$NON-NLS-1$
            target.start(connectionRequestMessage);
            logger.debug("Waiting for accepted socket"); //$NON-NLS-1$
            return target.accept();
        } catch (final Exception e) {
            final String errorMessage = "Could not create target-side connection"; //$NON-NLS-1$
            logger.error(errorMessage, e);
            throw new ConnectionNotEstablishedException(this.metaData.getTraversalTechniqueName(), errorMessage, e);
        }
    }

    private void sendConnectionRequestResponse(final Socket controlConnection, final Message connectionRequest)
            throws Exception {
        final Message response = connectionRequest.buildSuccessResponse();
        response.addAttribute(new HolePunchingAttribute());
        // add private endpoint
        final InetAddress privateAddress = controlConnection.getLocalAddress();
        if (privateAddress instanceof Inet6Address) {
            response.addAttribute(new XorMappedAddress(new InetSocketAddress(controlConnection.getLocalAddress(),
                    controlConnection.getLocalPort()), ByteBuffer.wrap(response.getHeader().getTransactionId())
                    .getInt()));
        } else {
            response.addAttribute(new XorMappedAddress(new InetSocketAddress(controlConnection.getLocalAddress(),
                    controlConnection.getLocalPort())));
        }
        response.writeTo(controlConnection.getOutputStream());
        logger.debug("Connection request response send"); //$NON-NLS-1$
    }

    @Override
    public void registerTargetAtMediator(final String targetId, final Socket controlConnection) throws Exception {
        // can be used to use traversal technique without connectivity manager
    }

    @Override
    public void deregisterTargetAtMediator(final String targetId, final Socket controlConnection) throws Exception {
        // can be used to use traversal technique without connectivity manager
    }

    @Override
    public NATTraversalTechnique copy() {
        return new HolePunching(this);
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
        final HolePunching other = (HolePunching) obj;
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
