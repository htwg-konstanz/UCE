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
 * The {@code NATFeatureRealization} describes with which concept a
 * {@link NATFeature} is realized.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public enum NATFeatureRealization {
    /**
     * Represents a endpoint independent {@link NATFeatureRealization} of a
     * {@link NATFeature}.
     */
    ENDPOINT_INDEPENDENT(0x1),

    /**
     * Represents a address dependent {@link NATFeatureRealization} of a
     * {@link NATFeature}.
     */
    ADDRESS_DEPENDENT(0x2),

    /**
     * Represents a address and port dependent {@link NATFeatureRealization} of
     * a {@link NATFeature}.
     */
    ADDRESS_AND_PORT_DEPENDENT(0x3),

    /**
     * Represents a connection dependent {@link NATFeatureRealization} of a
     * {@link NATFeature}.
     */
    CONNECTION_DEPENDENT(0x4),

    /**
     * Represents a not realized {@link NATFeatureRealization} of a
     * {@link NATFeature}.
     */
    NOT_REALIZED(0x5),

    // /**
    // * Represents a unknown {@link NATFeatureRealization} of a
    // * {@link NATFeature}.
    // */
    // UNKNOWN(0x6),

    /**
     * Wildcard, in this case all values previously defined values are possible.
     */
    DONT_CARE(0x7);

    private static final Map<Integer, NATFeatureRealization> intToEnum = new ConcurrentHashMap<Integer, NATFeatureRealization>();

    static {
        for (NATFeatureRealization l : values()) {
            intToEnum.put(l.encoded, l);
        }
    }

    private final int encoded;

    private NATFeatureRealization(int encoded) {
        this.encoded = encoded;
    }

    /**
     * Returns the encoding for a {@link NATFeatureRealization}.
     * 
     * @return the encoded {@link NATFeatureRealization}
     */
    public int encode() {
        return encoded;
    }

    /**
     * Returns the {@link NATFeatureRealization} for a given encoding.
     * 
     * @param encoded
     *            the encoded {@link NATFeatureRealization}
     * @return the {@link NATFeatureRealization}
     */
    public static NATFeatureRealization fromEncoded(int encoded) {
        return intToEnum.get(encoded);
    }
}
