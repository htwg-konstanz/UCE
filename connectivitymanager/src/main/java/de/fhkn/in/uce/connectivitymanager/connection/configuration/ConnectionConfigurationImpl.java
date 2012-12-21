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
package de.fhkn.in.uce.connectivitymanager.connection.configuration;

import net.jcip.annotations.Immutable;

/**
 * Immutable implementation of {@link ConnectionConfiguration}.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
@Immutable
public final class ConnectionConfigurationImpl implements ConnectionConfiguration {
    private final ConnectionDuration connectionDuration;
    private final ServiceClass serviceClass;
    private final boolean directConnectionRequired;

    /**
     * Creates a configuration with the given {@link ConnectionDuration} and
     * {@link ServiceClass}.
     * 
     * @param connectionDuration
     *            the {@link ConnectionDuration} of the requested connection
     * @param serviceClass
     *            the {@link ServiceClass} of the requested connection
     */
    public ConnectionConfigurationImpl(final ConnectionDuration connectionDuration, final ServiceClass serviceClass,
            final boolean directConnectionRequired) {
        this.connectionDuration = connectionDuration;
        this.serviceClass = serviceClass;
        this.directConnectionRequired = directConnectionRequired;
    }

    @Override
    public ConnectionDuration getConnectionDuration() {
        return this.connectionDuration;
    }

    @Override
    public ServiceClass getServiceClass() {
        return this.serviceClass;
    }

    @Override
    public boolean directConnectionRequired() {
        return this.directConnectionRequired;
    }
}
