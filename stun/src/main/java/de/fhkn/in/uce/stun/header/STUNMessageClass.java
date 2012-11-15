/*
    Copyright (c) 2012 Alexander Diener, 

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
package de.fhkn.in.uce.stun.header;

import java.util.HashMap;
import java.util.Map;

/**
 * The enumeration contains {@link MessageClass message classes} which are
 * defined in RFC 5389 (STUN).
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public enum STUNMessageClass implements MessageClass {
    REQUEST(0x000),

    SUCCESS_RESPONSE(0x0100),

    FAILURE_RESPONSE(0x0110),

    INDICATION(0x0010);

    private final int encoded;

    private STUNMessageClass(final int encoded) {
        this.encoded = encoded;
    }

    @Override
    public int encode() {
        return this.encoded;
    }

    private static final Map<Integer, STUNMessageClass> intToEnum = new HashMap<Integer, STUNMessageClass>();

    static {
        for (final STUNMessageClass l : values()) {
            intToEnum.put(l.encoded, l);
        }
    }

    /**
     * The method returns for a given encoding the corresponding
     * {@link MessageClass}.
     * 
     * @param encoded
     *            the encoded {@link MessageClass}
     * @return the {@link MessageClass} for the given encoding
     */
    public static MessageClass fromEncoded(final int encoded) {
        return intToEnum.get(encoded);
    }
}
