/**
 * Copyright (C) 2011 Daniel Maier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.fhkn.in.uce.messages;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum to represent common uce methods.
 * 
 * @author Daniel Maier
 * 
 */
public enum CommonUceMethod implements UceMethod {
    AUTHENTICATE(0x0), LIST(0x1), REGISTER(0x2), DEREGISTER(0x3), CONNECTION_REQUEST(0x4), KEEP_ALIVE(
            0x5);

    private static final Map<Integer, CommonUceMethod> intToEnum = new HashMap<Integer, CommonUceMethod>();

    static {
        for (CommonUceMethod l : values()) {
            intToEnum.put(l.encoded, l);
        }
    }

    private final int encoded;

    /**
     * Creates a new {@link CommonUceMethod}.
     * 
     * @param encoded
     *            the encoded representation of the uce method
     */
    private CommonUceMethod(int encoded) {
        this.encoded = encoded;
    }

    public int encode() {
        return encoded;
    }

    /**
     * Decodes the specified byte encoded uce method.
     * 
     * @param encoded
     *            the encoded uce method
     * @return the decoded {@link CommonUceMethod}
     */
    static CommonUceMethod fromEncoded(int encoded) {
        return intToEnum.get(encoded);
    }

}
