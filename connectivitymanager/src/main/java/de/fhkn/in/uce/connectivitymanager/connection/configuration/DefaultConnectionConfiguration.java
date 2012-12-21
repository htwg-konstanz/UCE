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
 * Immutable {@link ConnectionConfiguration} implementation with default values.
 * The following values are set:
 * 
 * <pre>
 * ConnectionDuration=LONG
 * ServiceClass=DEFAULT
 * DirectConnectionRequired=FALSE
 * </pre>
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
@Immutable
public final class DefaultConnectionConfiguration {

    private enum Implementation implements ConnectionConfiguration {
        INSTANCE;

        @Override
        public ConnectionDuration getConnectionDuration() {
            return ConnectionDuration.LONG;
        }

        @Override
        public ServiceClass getServiceClass() {
            return ServiceClass.DEFAULT;
        }

        @Override
        public boolean directConnectionRequired() {
            return false;
        }
    }

    /**
     * Returns the sole instance.
     * 
     * @return the sole instance of {@link DefaultConnectionConfiguration}
     */
    public static ConnectionConfiguration getInstance() {
        return Implementation.INSTANCE;
    }

    private DefaultConnectionConfiguration() {
        throw new AssertionError();
    }
}
