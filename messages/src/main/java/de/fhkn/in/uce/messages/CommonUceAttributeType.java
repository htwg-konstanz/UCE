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
package de.fhkn.in.uce.messages;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum to represent common uce attribute types.
 * 
 * @author Daniel Maier
 * 
 */
public enum CommonUceAttributeType implements UceAttributeType {
    /**
     * The payload of this {@link UceAttributeType} is represented by the class
     * {@link SocketEndpoint}.
     */
    SOCKET_ENDPOINT(0x0) {

        public UceAttribute fromBytes(byte[] encoded, UceAttributeHeader header)
                throws MessageFormatException, IOException {
            return SocketEndpoint.fromBytes(encoded, header);
        }
    },

    /**
     * The payload of this {@link UceAttributeType} is represented by the class
     * {@link UniqueId}.
     */
    UNIQUE_ID(0x1) {

        public UceAttribute fromBytes(byte[] encoded, UceAttributeHeader header)
                throws MessageFormatException, IOException {
            return UniqueId.fromBytes(encoded, header);
        }
    },

    /**
     * The payload of this {@link UceAttributeType} is represented by the class
     * {@link ErrorCode}.
     */
    ERROR_CODE(0x2) {

        public UceAttribute fromBytes(byte[] encoded, UceAttributeHeader header)
                throws MessageFormatException, IOException {
            return ErrorCode.fromBytes(encoded, header);
        }
    },

    /**
     * The payload of this {@link UceAttributeType} is represented by the class
     * {@link StringList}.
     */
    STRING_LIST(0x3) {

        public UceAttribute fromBytes(byte[] encoded, UceAttributeHeader header)
                throws MessageFormatException, IOException {
            return StringList.fromBytes(encoded, header);
        }
    },

    /**
     * The payload of this {@link UceAttributeType} is represented by the class
     * {@link UniqueUserName}.
     */
    UNIQUE_USER_NAME(0x4) {

        public UceAttribute fromBytes(byte[] encoded, UceAttributeHeader header)
                throws MessageFormatException, IOException {
            return UniqueUserName.fromBytes(encoded, header);
        }

    };

    private static final Map<Integer, CommonUceAttributeType> intToEnum = new HashMap<Integer, CommonUceAttributeType>();

    static {
        for (CommonUceAttributeType l : values()) {
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
    private CommonUceAttributeType(int encoded) {
        this.encoded = encoded;
    }

    public int encode() {
        return encoded;
    }

    /**
     * Decodes the specified byte encoded {@link CommonUceAttributeType}.
     * 
     * @param encoded
     *            the byte encoded common uce attribute type
     * @return the decoded {@link CommonUceAttributeType}
     */
    static CommonUceAttributeType fromEncoded(int encoded) {
        return intToEnum.get(encoded);
    }
}
