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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration to represent service classes of connections.
 *
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 *
 */
public enum ServiceClass {
    /**
     * Default behavior.
     */
    DEFAULT(0x00),

    /**
     * see RFC3246.
     */
    EXPEDITED_FORWARDING(0xB8),

    /**
     * see RFC2597: AF32 is used.
     */
    ASSURED_FORWARDING(0x70),

    /**
     * see RFC5865.
     */
    VOICE_ADMIT(0xB0);

    private static final Map<ServiceClass, Integer> serviceClassToTos;

    static {
        final Map<ServiceClass, Integer> tmp = new HashMap<ServiceClass, Integer>();
        for (final ServiceClass sc : values()) {
            tmp.put(sc, sc.correspondingTos);
        }
        serviceClassToTos = Collections.unmodifiableMap(tmp);
    }

    private int correspondingTos;

    private ServiceClass(final int corrTos) {
        this.correspondingTos = corrTos;
    }

    /**
     * Returns for a given service class the corresponding type-of-service
     * value.
     *
     * @param serviceClass
     *            the service class
     * @return the corresponding type-of-service value
     */
    public static int getTosForServiceClass(final ServiceClass serviceClass) {
        return serviceClassToTos.get(serviceClass);
    }
}
