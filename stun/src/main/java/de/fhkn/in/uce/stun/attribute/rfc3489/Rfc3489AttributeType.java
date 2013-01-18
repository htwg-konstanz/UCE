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
package de.fhkn.in.uce.stun.attribute.rfc3489;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.fhkn.in.uce.stun.attribute.Attribute;
import de.fhkn.in.uce.stun.attribute.AttributeHeader;
import de.fhkn.in.uce.stun.attribute.AttributeType;
import de.fhkn.in.uce.stun.header.MessageHeader;
import de.fhkn.in.uce.stun.util.MessageFormatException;

/**
 * Enumeration to represent attributes of the obsolete RFC 3489. These
 * attributes are used for public STUN-servers if no new STUN-server is known or
 * if the STUN-servers uses some old attributes.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public enum Rfc3489AttributeType implements AttributeType {
    SOURCE_ADDRESS(0x0004) {
        @Override
        public Attribute fromBytes(final byte[] encoded, final AttributeHeader header, final MessageHeader messageHeader)
                throws MessageFormatException, IOException {
            return SourceAddress.fromBytes(encoded, header);
        }
    },

    CHANGED_ADDRESS(0x0005) {
        @Override
        public Attribute fromBytes(final byte[] encoded, final AttributeHeader header, final MessageHeader messageHeader)
                throws MessageFormatException, IOException {
            return ChangedAddress.fromBytes(encoded, header);
        }
    };

    private static final Map<Integer, Rfc3489AttributeType> intToEnum = new HashMap<Integer, Rfc3489AttributeType>();

    static {
        for (final Rfc3489AttributeType l : values()) {
            intToEnum.put(l.encoded, l);
        }
    }

    private final int encoded;

    /**
     * Creates a new {@link Rfc3489AttributeType}.
     * 
     * @param encoded
     *            the byte encoded attribute type
     */
    private Rfc3489AttributeType(final int encoded) {
        this.encoded = encoded;
    }

    @Override
    public int encode() {
        return this.encoded;
    }

    /**
     * Decodes the specified byte encoded {@link Rfc3489AttributeType}.
     * 
     * @param encoded
     *            the byte encoded common attribute type
     * @return the decoded {@link Rfc3489AttributeType}
     */
    static Rfc3489AttributeType fromEncoded(final int encoded) {
        return intToEnum.get(encoded);
    }
}
