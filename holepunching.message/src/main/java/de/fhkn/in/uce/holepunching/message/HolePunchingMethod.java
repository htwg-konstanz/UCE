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
package de.fhkn.in.uce.holepunching.message;

import java.util.HashMap;
import java.util.Map;

import de.fhkn.in.uce.stun.header.MessageMethod;

/**
 * Enum for specific relay methods.
 * 
 * @author Daniel Maier, Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public enum HolePunchingMethod implements MessageMethod {
    FORWARDED_ENDPOINTS(0x37);

    private static final Map<Integer, HolePunchingMethod> intToEnum = new HashMap<Integer, HolePunchingMethod>();

    static {
        for (HolePunchingMethod l : values()) {
            intToEnum.put(l.encoded, l);
        }
    }

    private final int encoded;

    /**
     * Creates a new {@link HolePunchingMethod}.
     * 
     * @param encoded
     *            the encoded method
     */
    private HolePunchingMethod(int encoded) {
        this.encoded = encoded;
    }

    public int encode() {
        return encoded;
    }

    /**
     * Decodes an encoded relay method.
     * 
     * @param encoded
     *            the encoded relay method
     * @return the decoded relay method
     */
    public static HolePunchingMethod fromEncoded(int encoded) {
        return intToEnum.get(encoded);
    }

}
