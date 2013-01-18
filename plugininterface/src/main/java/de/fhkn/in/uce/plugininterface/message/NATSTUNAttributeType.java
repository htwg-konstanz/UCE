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
package de.fhkn.in.uce.plugininterface.message;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.fhkn.in.uce.plugininterface.NATBehavior;
import de.fhkn.in.uce.stun.attribute.Attribute;
import de.fhkn.in.uce.stun.attribute.AttributeHeader;
import de.fhkn.in.uce.stun.attribute.AttributeType;
import de.fhkn.in.uce.stun.header.MessageHeader;
import de.fhkn.in.uce.stun.util.MessageFormatException;

/**
 * Defines a {@link UceAttributeType} for NAT behavior.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public enum NATSTUNAttributeType implements AttributeType {

    /**
     * Encoding for the {@link NATBehavior} attribute type.
     */
    NAT_BEHAVIOR(0x1000) {

        @Override
        public Attribute fromBytes(byte[] encoded, AttributeHeader header, MessageHeader messageHeader)
                throws MessageFormatException, IOException {
            return NATBehavior.fromBytes(encoded, header);
        }

    },

    NAT_TRAVERSAL_TECHNIQUE(0x1002) {

        @Override
        public Attribute fromBytes(byte[] encoded, AttributeHeader header, MessageHeader messageHeader)
                throws MessageFormatException, IOException {
            return NATTraversalTechniqueAttribute.fromBytes(encoded, header);
        }

    };

    private static final Map<Integer, NATSTUNAttributeType> intToEnum = new HashMap<Integer, NATSTUNAttributeType>();

    static {
        for (NATSTUNAttributeType l : values()) {
            intToEnum.put(l.encoded, l);
        }
    }

    private final int encoded;

    /**
     * Creates a new {@link CommonUceAttributeType}.
     * 
     * @param encoded
     *            the byte encoded uce attribute type
     */
    private NATSTUNAttributeType(int encoded) {
        this.encoded = encoded;
    }

    @Override
    public int encode() {
        return this.encoded;
    }

    /**
     * Returns the {@link NATSTUNAttributeType} for the given encoding.
     * 
     * @param encoded
     *            the encoded {@link NATSTUNAttributeType}
     * @return the {@link NATSTUNAttributeType} for the encoding
     */
    public static NATSTUNAttributeType fromEncoded(int encoded) {
        return intToEnum.get(encoded);
    }
}
