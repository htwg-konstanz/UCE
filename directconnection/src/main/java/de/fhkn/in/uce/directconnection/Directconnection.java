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

import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.directconnection.core.DirectconnectionSource;
import de.fhkn.in.uce.directconnection.core.DirectconnectionTarget;
import de.fhkn.in.uce.plugininterface.ConnectionNotEstablishedException;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;
import de.fhkn.in.uce.plugininterface.NATTraversalTechniqueMetaData;
import de.fhkn.in.uce.stun.message.Message;

/**
 * Implementation of {@link NATTraversalTechnique} which establishes a direct
 * connection without using any NAT traversal technique.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class Directconnection implements NATTraversalTechnique {
    private static final Logger logger = LoggerFactory.getLogger(Directconnection.class);
    private final NATTraversalTechniqueMetaData metaData;
    private final DirectconnectionSource source;
    private final DirectconnectionTarget target;

    /**
     * Creates a {@link Directconnection} object with the corresponding
     * {@link NATTraversalTechniqueMetaData}.
     */
    public Directconnection() {
        try {
            this.metaData = new DirectconnectionMetaData();
            this.source = new DirectconnectionSource();
            this.target = new DirectconnectionTarget();
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
            this.source = new DirectconnectionSource();
            this.target = new DirectconnectionTarget();
        } catch (final Exception e) {
            logger.error("Exception occured while creating direct connection object.", e); //$NON-NLS-1$
            throw new RuntimeException("Could not create direct connection object.", e); //$NON-NLS-1$
        }
    }

    @Override
    public Socket createSourceSideConnection(final String targetId, final Socket controlConnection)
            throws ConnectionNotEstablishedException {
        try {
            return this.source.establishSourceSideConnection(targetId, controlConnection);
        } catch (final Exception e) {
            logger.error(e.getMessage());
            throw new ConnectionNotEstablishedException(this.metaData.getTraversalTechniqueName(),
                    "Source-side socket could not be created.", e); //$NON-NLS-1$
        }
    }

    @Override
    public Socket createTargetSideConnection(final String targetId, final Socket controlConnection,
            final Message connectionRequestMessage) throws ConnectionNotEstablishedException {
        try {
            logger.debug("Establishing target-side conenction via directconnection"); //$NON-NLS-1$
            return this.target.establishTargetSideConnection(controlConnection, connectionRequestMessage);
        } catch (final Exception e) {
            logger.error(e.getMessage());
            throw new ConnectionNotEstablishedException(this.metaData.getTraversalTechniqueName(),
                    "Target-side socket could not be created.", e); //$NON-NLS-1$
        }
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
    public NATTraversalTechniqueMetaData getMetaData() {
        return this.metaData;
    }

    @Override
    public NATTraversalTechnique copy() {
        return new Directconnection(this);
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
