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

import java.net.InetSocketAddress;

import net.jcip.annotations.Immutable;
import de.fhkn.in.uce.stun.attribute.EndpointClass.EndpointCategory;

/**
 * The class represents an endpoint which consists of an endpoint address and an
 * endpoint category.
 *
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 *
 */
@Immutable
public final class Endpoint {
    private final InetSocketAddress endpointAddress;
    private final EndpointCategory category;

    /**
     * Creates a new {@link Endpoint} with the given data.
     *
     * @param endpointAddress
     *            the address of the endpoint
     * @param category
     *            the {@link EndpointCategory} the endpoints belongs to
     */
    public Endpoint(final InetSocketAddress endpointAddress, final EndpointCategory category) {
        this.category = category;
        this.endpointAddress = endpointAddress;
    }

    /**
     * Returns the address of the endpoint.
     *
     * @return the address of the endpoint
     */
    public InetSocketAddress getEndpointAddress() {
        return endpointAddress;
    }

    /**
     * Returns the {@link EndpointCategory} the endpoint belongs to.
     *
     * @return the {@link EndpointCategory} of the endpoint
     */
    public EndpointCategory getCategory() {
        return category;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((category == null) ? 0 : category.hashCode());
        result = (prime * result) + ((endpointAddress == null) ? 0 : endpointAddress.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Endpoint other = (Endpoint) obj;
        if (category != other.category) {
            return false;
        }
        if (endpointAddress == null) {
            if (other.endpointAddress != null) {
                return false;
            }
        } else if (!endpointAddress.equals(other.endpointAddress)) {
            return false;
        }
        return true;
    }
}
