/*
    Copyright (c) 2012 Thomas Zink, 

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhkn.in.uce.relay.core;

import java.util.HashMap;
import java.util.Map;

import de.fhkn.in.uce.messages.UceMethod;

/**
 * Enum for specific relay methods.
 * @author Daniel Maier
 *
 */
public enum RelayUceMethod implements UceMethod {
    ALLOCATION(0x33), REFRESH(0x34), CONNECTION_ATTEMPT(0x35), CONNECTION_BIND(0x36);

    private static final Map<Integer, RelayUceMethod> intToEnum = 
        new HashMap<Integer, RelayUceMethod>();

    static {
        for (RelayUceMethod l : values()) {
            intToEnum.put(l.encoded, l);
        }
    }

    private final int encoded;

    /**
     * Creates a new {@link RelayUceMethod}.
     * 
     * @param encoded the encoded method
     */
    private RelayUceMethod(int encoded) {
        this.encoded = encoded;
    }

    public int encode() {
        return encoded;
    }

    /**
     * Decodes an encoded relay uce method.
     * 
     * @param encoded the encoded relay uce method
     * @return the decoded relay uce method
     */
    public static RelayUceMethod fromEncoded(int encoded) {
        return intToEnum.get(encoded);
    }

}
