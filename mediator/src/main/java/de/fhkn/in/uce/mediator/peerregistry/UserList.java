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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.jcip.annotations.ThreadSafe;

/**
 * Manages registered users and provides functionality therefore. The users are
 * mapped by a key-value-pair with the id of the user as key and the
 * {@link UserData} as value. The class is implemented as singleton.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
@ThreadSafe
public enum UserList {
    INSTANCE;

    private final ConcurrentHashMap<String, UserData> users = new ConcurrentHashMap<String, UserData>();

    /**
     * Adds a new user to {@link UserList}. If the user already exists it will
     * not be replaced.
     * 
     * @param newUser
     *            the {@link UserData} to add
     */
    public void addOrUpdateUser(final UserData newUser) {
        if (!this.users.contains(newUser.getUserId())) {
            this.users.put(newUser.getUserId(), newUser);
        } else {
            this.updateUser(newUser);
        }
        this.refreshUserTimestamp(newUser.getUserId());
    }

    private void updateUser(final UserData newUser) {
        final UserData toUpdate = this.users.get(newUser.getUserId());
        toUpdate.changeUserNat(newUser.getUserNat());
        for (final Endpoint newEndpoint : newUser.getAllEndpoints()) {
            final List<Endpoint> existingEndpoints = new ArrayList<Endpoint>();
            Collections.copy(toUpdate.getEndpointsForCategory(newEndpoint.getCategory()), existingEndpoints);
            for (Endpoint toRemove : existingEndpoints) {
                toUpdate.removeEndpoint(toRemove);
            }
            toUpdate.addEndpoint(newEndpoint);
        }
    }

    /**
     * Removes the user with the given name.
     * 
     * @param userId
     *            the name of the user to remove
     * @return true if the user was removed, false else
     */
    public boolean removeUser(final String userId) {
        return (this.users.remove(userId) == null) ? false : true;
    }

    /**
     * Returns the {@link UserData} for the requested user. If no user exists,
     * null is returned.
     * 
     * @param userId
     *            the id of the user
     * @return the {@link UserData} for the user id
     */
    public UserData getUserDataByUserId(final String userId) {
        return this.users.get(userId);
    }

    // /**
    // * Updates the given user id with the new {@link UserData}.
    // *
    // * @param userId
    // * the id of the user to update
    // * @param newUserData
    // * the updated {@link UserData}
    // */
    // public void updateUser(final String userId, final UserData newUserData) {
    // this.users.replace(userId, newUserData);
    // }

    /**
     * Refreshs the timestamp of the given user.
     * 
     * @param userId
     *            the id of the user
     */
    public void refreshUserTimestamp(final String userId) {
        final UserData toRefresh = this.users.get(userId);
        if (toRefresh != null) {
            toRefresh.refreshTimestamp();
        }
    }

    /**
     * Removes users with timestamp older or equals the given timestamp.
     * 
     * @param timestamp
     *            with this timestamp the users will be checked
     */
    public void removeUsersByTimestamp(final long timestamp) {
        final Iterator<UserData> valueIter = this.users.values().iterator();
        while (valueIter.hasNext()) {
            final UserData user = valueIter.next();
            if (user.getTimestamp() <= timestamp) {
                valueIter.remove();
            }
        }
    }

    /**
     * Returns all registered users.
     * 
     * @return a set of all registered user ids
     */
    public Set<String> getUserIds() {
        return this.users.keySet();
    }
}
