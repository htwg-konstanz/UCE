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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@code NATFeature} describes one aspect of the behavior of a NAT device. A
 * value describes two aspects, the feature (e.g. filtering of incoming
 * connections) of a NAT device and to which party it belongs.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public enum NATFeature {
    /**
     * The encoding for the mapping feature of a NAT device.
     */
    MAPPING(0x1),

    /**
     * The encoding for the filtering feature of a NAT device.
     */
    FILTERING(0x2);

    private static final Map<Integer, NATFeature> intToEnum = new ConcurrentHashMap<Integer, NATFeature>();

    static {
        for (NATFeature l : values()) {
            intToEnum.put(l.encoded, l);
        }
    }

    private final int encoded;

    private NATFeature(int encoded) {
        this.encoded = encoded;
    }

    /**
     * Returns the encoded {@link NATFeature}.
     * 
     * @return the encoded {@link NATFeature}
     */
    public int encode() {
        return encoded;
    }

    /**
     * Returns the {@link NATFeature} for a given encoding.
     * 
     * @param encoded
     *            the encoded {@link NATFeature}
     * @return the {@link NATFeature} for the given encoding
     */
    public static NATFeature fromEncoded(int encoded) {
        return intToEnum.get(encoded);
    }
}
