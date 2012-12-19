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
package de.fhkn.in.uce.mediator.peerregistry;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import de.fhkn.in.uce.plugininterface.NATBehavior;
import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;
import de.fhkn.in.uce.stun.attribute.EndpointClass.EndpointCategory;

/**
 * The class holds all information about a registered user and provides
 * functionality to use and refresh it. A registered user is the target/server
 * of a connection.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
@ThreadSafe
public final class UserData {
    private final String userId;
    private final List<Endpoint> endpoints;
    private final Lock timestampLock = new ReentrantLock();
    @GuardedBy("timestampLock")
    private long timestamp;
    @GuardedBy("itself")
    private NATBehavior userNat;
    private final Socket socketToUser;
    @GuardedBy("itself")
    private final List<NATTraversalTechniqueAttribute> supportedNatTraversalTechniques;

    /**
     * Creates a new registered user with the given information.
     * 
     * @param userId
     *            the unique user name
     * @param userNat
     *            the {@link NATBehavior} of the user nat device
     * @param socketToUser
     *            the socket to the user
     * @param supportedNatTraversalTechniques
     *            a list of supported nat traversal techniques
     */
    public UserData(final String userId, final NATBehavior userNat, final Socket socketToUser,
            final List<NATTraversalTechniqueAttribute> supportedNatTraversalTechniques) {
        this.userId = userId;
        this.userNat = userNat;
        this.endpoints = Collections.synchronizedList(new ArrayList<Endpoint>());
        this.timestamp = System.currentTimeMillis();
        this.socketToUser = socketToUser;
        final List<NATTraversalTechniqueAttribute> travTechs = new ArrayList<NATTraversalTechniqueAttribute>();
        Collections.copy(travTechs, supportedNatTraversalTechniques);
        this.supportedNatTraversalTechniques = Collections.synchronizedList(travTechs);
    }

    /**
     * Returns the unique user name.
     * 
     * @return the user name
     */
    public String getUserId() {
        return this.userId;
    }

    /**
     * Returns the current timestamp.
     * 
     * @return the current timestamp
     */
    public long getTimestamp() {
        synchronized (this.timestampLock) {
            return this.timestamp;
        }
    }

    /**
     * Returns the control connection associated with the user.
     * 
     * @return the {@link Socket} to the user
     */
    public Socket getSocketToUser() {
        return this.socketToUser;
    }

    /**
     * Returns a list of all registered endpoints which belongs to the user.
     * 
     * @return a list of all registered endpoints
     */
    public List<Endpoint> getAllEndpoints() {
        return Collections.unmodifiableList(this.endpoints);
    }

    /**
     * Returns a list of registered endpoints for the given
     * {@link EndpointCategory}. The categories must exactly match to be
     * returned.
     * 
     * @param category
     *            the requested {@link EndpointCategory}
     * @return a list of {@link Endpoint}s for the given
     *         {@link EndpointCategory}
     */
    public List<Endpoint> getEndpointsForCategory(final EndpointCategory category) {
        final List<Endpoint> result = new ArrayList<Endpoint>();
        for (final Endpoint endpoint : this.endpoints) {
            if (endpoint.getCategory().equals(category)) {
                result.add(endpoint);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Adds a new {@link Endpoint}.
     * 
     * @param toAdd
     *            the {@link Endpoint} to add
     * @return true (as specified by {@link Collection#add(Object)
     *         Collection.add})
     */
    public boolean addEndpoint(final Endpoint toAdd) {
        return this.endpoints.add(toAdd);
    }

    /**
     * Removes an {@link Endpoint}.
     * 
     * @param toRemove
     *            the {@link Endpoint} to remove
     * @return true (as specified by {@link Collection#remove(Object)
     *         Collection.remove})
     */
    public boolean removeEndpoint(final Endpoint toRemove) {
        return this.endpoints.remove(toRemove);
    }

    /**
     * Returns the {@link NATBehavior} of the user.
     * 
     * @return the {@link NATBehavior} of the user
     */
    public NATBehavior getUserNat() {
        synchronized (this.userNat) {
            return this.userNat;
        }
    }

    /**
     * Refreshs the timestamp of the user.
     */
    public void refreshTimestamp() {
        synchronized (this.timestampLock) {
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * Changes the {@link NATBehavior} of the user to the given one.
     * 
     * @param newUserNat
     *            the new {@link NATBehavior} of the user
     */
    public void changeUserNat(final NATBehavior newUserNat) {
        synchronized (this.userNat) {
            this.userNat = newUserNat;
        }
    }

    /**
     * Returns the unmodifiable list of supported NAT traversal techniques of
     * the user.
     * 
     * @return the supported NAT traversal techniques as unmodifiable list
     */
    public List<NATTraversalTechniqueAttribute> getSupportedNatTraversalTechniques() {
        return Collections.unmodifiableList(this.supportedNatTraversalTechniques);
    }

    /**
     * Adds a new nat traversal technique to the list of supported techniques.
     * 
     * @param toAdd
     *            the {@link NATTraversalTechniqueAttribute} of the supported
     *            technique
     * @return see {@link List#add(Object)}
     */
    public boolean addSupportedNatTraversalTechnique(final NATTraversalTechniqueAttribute toAdd) {
        return this.supportedNatTraversalTechniques.add(toAdd);
    }

    /**
     * Removes the given nat traversal technique from the list of supported
     * techniques.
     * 
     * @param toRemove
     *            the {@link NATTraversalTechniqueAttribute} to remove
     * @return see {@link List#remove(Object)}
     */
    public boolean removeSupportedNatTraversalTechnique(final NATTraversalTechniqueAttribute toRemove) {
        return this.supportedNatTraversalTechniques.remove(toRemove);
    }
}
