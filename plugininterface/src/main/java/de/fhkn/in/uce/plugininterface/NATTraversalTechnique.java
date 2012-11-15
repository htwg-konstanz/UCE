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
package de.fhkn.in.uce.plugininterface;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A {@link NATTraversalTechnique} is a technique to traverses a NAT device.
 * There is a difference between the source-side (client) and the target-side
 * (server) of the connection establishment, because they have to behave
 * different. A concrete NAT traversal technique has to implement this
 * interface. It is very important that a concrete implementation of this
 * interface overrides the {@code equals} method. Two
 * {@link NATTraversalTechnique} are equals if the
 * {@link NATTraversalTechniqueMetaData} are equals.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public interface NATTraversalTechnique {
    /**
     * Returns the {@link NATTraversalTechniqueMetaData} of this
     * {@link NATTraversalTechnique}.
     * 
     * @return returns the {@link NATTraversalTechniqueMetaData}
     */
    NATTraversalTechniqueMetaData getMetaData();

    /**
     * Creates a connection on the source-side (client) to the given target.
     * This method has to be called if a client wants to establish a connection
     * to a server with the given {@code targetId}
     * 
     * @param targetId
     *            the target id of the server
     * @param mediatorAddress
     *            the mediator which delivers the target endpoint
     * @return the socket which is connected to the {@code targetId}
     * @throws ConnectionNotEstablishedException
     *             if the connection could not be established
     */
    Socket createSourceSideConnection(final String targetId, final InetSocketAddress mediatorAddress)
            throws ConnectionNotEstablishedException;

    /**
     * Creates a connection on the target-side (server). This method has to be
     * called if a server behind a NAT wants to be available for client
     * requests. The server is available at the mediator with the given
     * {@code targetId}.
     * 
     * @param targetId
     *            the id of the server
     * @param mediatorAddress
     *            the mediator for registering the {@code targetId}
     * @return the socket which is connected to a client
     * @throws ConnectionNotEstablishedException
     *             if the connection could not be established
     */
    Socket createTargetSideConnection(final String targetId, final InetSocketAddress mediatorAddress)
            throws ConnectionNotEstablishedException;

    /**
     * Registers a target with a {@code targetId} at the given mediator.
     * 
     * @param targetId
     *            the unique name of the target
     * @param mediatorAddress
     *            the mediator address to register the target
     * @throws ConnectionNotEstablishedException
     *             if the target could not be registered
     */
    void registerTargetAtMediator(final String targetId, final InetSocketAddress mediatorAddress)
            throws ConnectionNotEstablishedException;

    /**
     * Deregisters a target.
     * 
     * @param targetId
     *            the if of the service which should be deregistered
     * @param mediatorAddress
     *            the {@link InetSocketAddress} of the mediator
     * @throws ConnectionNotEstablishedException
     */
    void deregisterTargetAtMediator(final String targetId, final InetSocketAddress mediatorAddress)
            throws ConnectionNotEstablishedException;

    /**
     * Returns a copy of the {@link NATTraversalTechnique}.
     * 
     * @return the copy of the {@link NATTraversalTechnique}
     */
    NATTraversalTechnique copy();
}
