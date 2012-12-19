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
package de.fhkn.in.uce.stun.header;

import java.util.HashMap;
import java.util.Map;

/**
 * * The enumeration contains {@link MessageMethod methods} of STUN messages.
 * The {@code BINDING} method is defined in RFC 5389, the other values are
 * common methods in the Universal Connection Establishment context.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public enum STUNMessageMethod implements MessageMethod {
    BINDING(0x001), AUTHENTICATE(0x003), LIST(0x004), REGISTER(0x005), DEREGISTER(0x006), CONNECTION_REQUEST(0x007), KEEP_ALIVE(
            0x008), NAT_REQUEST(0x009), SUPPORTED_TRAV_TECHS_REQUEST(0x00d);

    private final int encoded;

    private STUNMessageMethod(final int encoded) {
        this.encoded = encoded;
    }

    @Override
    public int encode() {
        return this.encoded;
    }

    private static final Map<Integer, STUNMessageMethod> intToEnum = new HashMap<Integer, STUNMessageMethod>();

    static {
        for (final STUNMessageMethod l : values()) {
            intToEnum.put(l.encoded, l);
        }
    }

    /**
     * For the given encoding the corresponding {@link MessageMethod} is
     * returned.
     * 
     * @param encoded
     *            the encoded {@link MessageMethod}
     * @return the {@link MessageMethod} for the given encoding
     */
    public static MessageMethod fromEncoded(final int encoded) {
        return intToEnum.get(encoded);
    }
}
