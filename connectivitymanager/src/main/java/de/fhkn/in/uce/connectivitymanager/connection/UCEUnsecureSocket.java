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
package de.fhkn.in.uce.connectivitymanager.connection;

import de.fhkn.in.uce.connectivitymanager.connection.configuration.ConnectionConfiguration;
import de.fhkn.in.uce.connectivitymanager.connection.configuration.DefaultConnectionConfiguration;
import de.fhkn.in.uce.connectivitymanager.manager.ConnectionEstablishment;
import de.fhkn.in.uce.connectivitymanager.manager.ConnectivityManager;

final class UCEUnsecureSocket extends UCESocket {

    public UCEUnsecureSocket(final String targetId, final ConnectionConfiguration configuration,
            final ConnectionEstablishment connectionEstablishment) {
        this.connectivityManager = new ConnectivityManager(targetId, connectionEstablishment, configuration);
        // this.establishConnection();
    }

    public UCEUnsecureSocket(final String targetId, final ConnectionEstablishment connectionEstablishment) {
        this(targetId, DefaultConnectionConfiguration.getInstance(), connectionEstablishment);
    }
}
