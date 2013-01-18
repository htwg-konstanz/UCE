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

import de.fhkn.in.uce.stun.header.MessageHeader;
import de.fhkn.in.uce.stun.util.MessageFormatException;

/**
 * The STUN attribute type gets encoded in the STUN Attribute Header to
 * distinguish different attributes.
 * 
 * @author Daniel Maier, Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public interface AttributeType {
    /**
     * Returns the encoded representation of this {@link AttributeType}. Only
     * the least significant 16 Bits get used as a unsigned short.
     * 
     * @return the encoded representation of this {@link AttributeType}.
     */
    int encode();

    /**
     * Decodes a STUN Attribute.
     * 
     * @param encoded
     *            the value of the attribute encoded as a byte array.
     * @param header
     *            the header of the attribute.
     * @return the decoded {@link Attribute}.
     * @throws MessageFormatException
     *             if the message isn't formed properly
     * @throws IOException
     *             if an I/O error occurs
     */
    // TODO is it good to provide the message header to decode the attributes?
    // But some attributes (xor-mapped-address) need header information
    Attribute fromBytes(byte[] encoded, AttributeHeader header, MessageHeader messageHeader)
            throws MessageFormatException, IOException;
}
