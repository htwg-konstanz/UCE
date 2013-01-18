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
package de.fhkn.in.uce.stun.attribute;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.fhkn.in.uce.stun.header.MessageHeader;
import de.fhkn.in.uce.stun.util.MessageFormatException;

/**
 * Enumeration to represent common stun attribute types. It contains attribute
 * types which are defined in RFC 5389 and attribute types which are used in the
 * Universal Connection Establishment context.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public enum STUNAttributeType implements AttributeType {
    MAPPED_ADDRESS(0x0001) {
        @Override
        public Attribute fromBytes(final byte[] encoded, final AttributeHeader header, final MessageHeader messageHeader)
                throws MessageFormatException, IOException {
            return MappedAddress.fromBytes(encoded, header);
        }
    },

    USERNAME(0x0006) {
        @Override
        public Attribute fromBytes(final byte[] encoded, final AttributeHeader header, final MessageHeader messageHeader)
                throws MessageFormatException, IOException {
            return Username.fromBytes(encoded, header);
        }
    },

    MESSAGE_INTEGRITY(0x0008) {
        @Override
        public Attribute fromBytes(final byte[] encoded, final AttributeHeader header, final MessageHeader messageHeader)
                throws MessageFormatException, IOException {
            return null;
        }
    },

    ERROR_CODE(0x0009) {
        @Override
        public Attribute fromBytes(final byte[] encoded, final AttributeHeader header, final MessageHeader messageHeader)
                throws MessageFormatException, IOException {
            return ErrorCode.fromBytes(encoded, header);
        }
    },

    UNKNOWN_ATTRIBUTES(0x000A) {
        @Override
        public Attribute fromBytes(final byte[] encoded, final AttributeHeader header, final MessageHeader messageHeader)
                throws MessageFormatException, IOException {
            return Unknown.fromBytes(encoded, header);
        }
    },

    REALM(0x0014) {
        @Override
        public Attribute fromBytes(final byte[] encoded, final AttributeHeader header, final MessageHeader messageHeader)
                throws MessageFormatException, IOException {
            return null;
        }
    },

    NONCE(0x0015) {
        @Override
        public Attribute fromBytes(final byte[] encoded, final AttributeHeader header, final MessageHeader messageHeader)
                throws MessageFormatException, IOException {
            return null;
        }
    },

    // in RFC 5389, XOP-MAPPED-ADDRESS is defined as 0x0020 but the most public
    // servers use 0x8020
    XOR_MAPPED_ADDRESS(0x0020) {
        @Override
        public Attribute fromBytes(final byte[] encoded, final AttributeHeader header, final MessageHeader messageHeader)
                throws MessageFormatException, IOException {
            return XorMappedAddress.fromBytes(encoded, header, messageHeader);
        }
    },

    CHANGE_REQUEST(0x0003) {
        @Override
        public Attribute fromBytes(final byte[] encoded, final AttributeHeader header, final MessageHeader messageHeader)
                throws MessageFormatException, IOException {
            return ChangeRequest.fromBytes(encoded, header);
        }
    },

    ENDPOINT_CLASS(0x1001) {
        @Override
        public Attribute fromBytes(final byte[] encoded, final AttributeHeader header, final MessageHeader messageHeader)
                throws MessageFormatException, IOException {
            return EndpointClass.fromBytes(encoded, header);
        }
    },

    TOKEN(0x1003) {
        @Override
        public Attribute fromBytes(final byte[] encoded, final AttributeHeader header, final MessageHeader messageHeader)
                throws MessageFormatException, IOException {
            return Token.fromBytes(encoded);
        }
    },

    SOFTWARE(0x8022) {
        @Override
        public Attribute fromBytes(final byte[] encoded, final AttributeHeader header, final MessageHeader messageHeader)
                throws MessageFormatException, IOException {
            return Software.fromBytes(encoded, header);
        }
    },

    OTHER_ADDRESS(0x802C) {
        @Override
        public Attribute fromBytes(final byte[] encoded, final AttributeHeader header, final MessageHeader messageHeader)
                throws MessageFormatException, IOException {
            return OtherAddress.fromBytes(encoded, header);
        }
    };

    private static final Map<Integer, STUNAttributeType> intToEnum = new HashMap<Integer, STUNAttributeType>();

    static {
        for (final STUNAttributeType l : values()) {
            intToEnum.put(l.encoded, l);
        }
    }

    private final int encoded;

    /**
     * Creates a new {@link STUNAttributeType}.
     * 
     * @param encoded
     *            the byte encoded attribute type
     */
    private STUNAttributeType(final int encoded) {
        this.encoded = encoded;
    }

    @Override
    public int encode() {
        return this.encoded;
    }

    /**
     * Decodes the specified byte encoded {@link STUNAttributeType}.
     * 
     * @param encoded
     *            the byte encoded common attribute type
     * @return the decoded {@link STUNAttributeType}
     */
    static STUNAttributeType fromEncoded(final int encoded) {
        return intToEnum.get(encoded);
    }
}
