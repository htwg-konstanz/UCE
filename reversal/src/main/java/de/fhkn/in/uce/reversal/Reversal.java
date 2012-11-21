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
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.plugininterface.ConnectionNotEstablishedException;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;
import de.fhkn.in.uce.plugininterface.NATTraversalTechniqueMetaData;
import de.fhkn.in.uce.reversal.core.ReversalSource;
import de.fhkn.in.uce.reversal.core.ReversalTarget;

/**
 * Implementation of {@link NATTraversalTechnique} for Connection Reversal.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class Reversal implements NATTraversalTechnique {
    private static final Logger logger = LoggerFactory.getLogger(Reversal.class);
    private final NATTraversalTechniqueMetaData metaData;
    private final Socket controlConnection;
    private ReversalTarget target;

    /**
     * Creates a {@link Reversal} object.
     */
    public Reversal() {
        try {
            this.metaData = new ReversalMetaData();
            this.controlConnection = new Socket();
            this.controlConnection.setReuseAddress(true);
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
            this.controlConnection = new Socket();
            this.controlConnection.setReuseAddress(true);
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
    public Socket createSourceSideConnection(final String targetId, final InetSocketAddress mediatorAddress)
            throws ConnectionNotEstablishedException {
        Socket result = null;
        final ReversalSource source = new ReversalSource(mediatorAddress);
        try {
            result = source.connect(targetId);
        } catch (final IOException e) {
            final String errorMessage = "Source-side connection could not be established"; //$NON-NLS-1$
            logger.error(errorMessage, e);
            throw new ConnectionNotEstablishedException(this.metaData.getTraversalTechniqueName(), errorMessage, e);
        }
        return result;
    }

    @Override
    public Socket createTargetSideConnection(final String targetId, final InetSocketAddress mediatorAddress)
            throws ConnectionNotEstablishedException {
        Socket result = null;
        try {
            result = this.target.accept();
        } catch (final InterruptedException e) {
            final String errorMessage = "Target-side connection could not be established"; //$NON-NLS-1$
            logger.error(errorMessage, e);
            throw new ConnectionNotEstablishedException(this.metaData.getTraversalTechniqueName(), errorMessage, e);
        }
        return result;
    }

    @Override
    public void registerTargetAtMediator(final String targetId, final InetSocketAddress mediatorAddress)
            throws Exception {
        this.initializeReversalTarget(mediatorAddress);
        this.target.register(targetId);
    }

    @Override
    public void deregisterTargetAtMediator(final String targetId, final InetSocketAddress mediatorAddress)
            throws Exception {
        this.initializeReversalTarget(mediatorAddress);
        this.target.deregister(targetId);
    }

    private synchronized void initializeReversalTarget(final InetSocketAddress mediatorAddress) {
        if (this.target == null) {
            this.target = new ReversalTarget(mediatorAddress);
        }
    }

    @Override
    public NATTraversalTechnique copy() {
        return new Reversal(this);
    }
}
