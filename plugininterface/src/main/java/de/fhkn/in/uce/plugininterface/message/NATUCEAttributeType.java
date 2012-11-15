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

import de.fhkn.in.uce.messages.CommonUceAttributeType;
import de.fhkn.in.uce.messages.MessageFormatException;
import de.fhkn.in.uce.messages.UceAttribute;
import de.fhkn.in.uce.messages.UceAttributeHeader;
import de.fhkn.in.uce.messages.UceAttributeType;
import de.fhkn.in.uce.plugininterface.NATBehavior;

/**
 * Defines a {@link UceAttributeType} for NAT behavior.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public enum NATUCEAttributeType implements UceAttributeType {

    /**
     * Encoding for the {@link NATBehavior} attribute type.
     */
    NAT_BEHAVIOR(0x1000) {

        public UceAttribute fromBytes(byte[] encoded, UceAttributeHeader header) throws MessageFormatException,
                IOException {
            return NATBehavior.fromBytes(encoded, header);
        }
    };

    private static final Map<Integer, NATUCEAttributeType> intToEnum = new HashMap<Integer, NATUCEAttributeType>();

    static {
        for (NATUCEAttributeType l : values()) {
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
    private NATUCEAttributeType(int encoded) {
        this.encoded = encoded;
    }

    @Override
    public int encode() {
        return this.encoded;
    }

    /**
     * Returns the {@link NATUCEAttributeType} for the given encoding.
     * 
     * @param encoded
     *            the encoded {@link NATUCEAttributeType}
     * @return the {@link NATUCEAttributeType} for the encoding
     */
    public static NATUCEAttributeType fromEncoded(int encoded) {
        return intToEnum.get(encoded);
    }
}
