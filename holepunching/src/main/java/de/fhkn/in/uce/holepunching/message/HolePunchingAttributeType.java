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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.fhkn.in.uce.stun.MessageFormatException;
import de.fhkn.in.uce.stun.attribute.Attribute;
import de.fhkn.in.uce.stun.attribute.AttributeHeader;
import de.fhkn.in.uce.stun.attribute.AttributeType;
import de.fhkn.in.uce.stun.header.MessageHeader;

/**
 * Enum for specific hole punching attribute types.
 * 
 * @author Daniel Maier, Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public enum HolePunchingAttributeType implements AttributeType {
    TOKEN(0x34) {

        public Attribute fromBytes(byte[] encoded, AttributeHeader header, MessageHeader msgHeader)
                throws MessageFormatException, IOException {
            return Token.fromBytes(encoded);
        }
    };

    private static final Map<Integer, HolePunchingAttributeType> intToEnum = new HashMap<Integer, HolePunchingAttributeType>();

    static {
        for (HolePunchingAttributeType l : values()) {
            intToEnum.put(l.encoded, l);
        }
    }

    private final int encoded;

    /**
     * Creates a new {@link HolePunchingAttributeType}.
     * 
     * @param encoded
     *            the encoded attribute type
     */
    private HolePunchingAttributeType(int encoded) {
        this.encoded = encoded;
    }

    public int encode() {
        return encoded;
    }

    /**
     * Decodes a given encoded attribute type.
     * 
     * @param encoded
     *            the encoded attribute type
     * @return the decoded attribute type, or null if the attribute type is
     *         unknown.
     */
    public static HolePunchingAttributeType fromEncoded(int encoded) {
        return intToEnum.get(encoded);
    }
}
