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
package de.fhkn.in.uce.reversal;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.plugininterface.ConnectionNotEstablishedException;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;
import de.fhkn.in.uce.plugininterface.NATTraversalTechniqueMetaData;
import de.fhkn.in.uce.reversal.core.ReversalSource;
import de.fhkn.in.uce.reversal.core.ReversalTarget;
import de.fhkn.in.uce.stun.message.Message;

/**
 * Implementation of {@link NATTraversalTechnique} for Connection Reversal.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class Reversal implements NATTraversalTechnique {
    private static final Logger logger = LoggerFactory.getLogger(Reversal.class);
    private final NATTraversalTechniqueMetaData metaData;
    private final ReversalTarget target;
    private final ReversalSource source;

    /**
     * Creates a {@link Reversal} object.
     */
    public Reversal() {
        try {
            this.metaData = new ReversalMetaData();
            this.source = new ReversalSource();
            this.target = new ReversalTarget();
        } catch (final Exception e) {
            logger.error("Exception occured while creating reversal connection object.", e); //$NON-NLS-1$
            throw new RuntimeException("Could not create reversal connection object.", e); //$NON-NLS-1$
        }
    }

    /**
     * Copy constructor for {@link Reversal}.
     * 
     * @param toCopy
     *            the {@link Reversal} to copy
     */
    public Reversal(final Reversal toCopy) {
        try {
            this.metaData = new ReversalMetaData((ReversalMetaData) toCopy.getMetaData());
            this.source = new ReversalSource();
            this.target = new ReversalTarget();
        } catch (final Exception e) {
            logger.error("Exception occured while creating reversal connection object.", e); //$NON-NLS-1$
            throw new RuntimeException("Could not create reversal connection object.", e); //$NON-NLS-1$
        }
    }

    @Override
    public NATTraversalTechniqueMetaData getMetaData() {
        return this.metaData;
    }

    @Override
    public Socket createSourceSideConnection(final String targetId, final Socket controlConnection)
            throws ConnectionNotEstablishedException {
        Socket result = null;
        try {
            result = this.source.establishSourceSideConnection(targetId, controlConnection);
        } catch (final IOException e) {
            final String errorMessage = "Source-side connection could not be established"; //$NON-NLS-1$
            logger.error(errorMessage, e);
            throw new ConnectionNotEstablishedException(this.metaData.getTraversalTechniqueName(), errorMessage, e);
        }
        return result;
    }

    @Override
    public Socket createTargetSideConnection(final String targetId, final Socket controlConnection,
            final Message connectionRequestMessage) throws ConnectionNotEstablishedException {
        try {
            return this.target.establishTargetSideConnection(controlConnection, connectionRequestMessage);
        } catch (final Exception e) {
            final String errorMessage = "Target-side connection could not be established"; //$NON-NLS-1$
            logger.error(errorMessage, e);
            throw new ConnectionNotEstablishedException(this.metaData.getTraversalTechniqueName(), errorMessage, e);
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
    public NATTraversalTechnique copy() {
        return new Reversal(this);
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
        final Reversal other = (Reversal) obj;
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
