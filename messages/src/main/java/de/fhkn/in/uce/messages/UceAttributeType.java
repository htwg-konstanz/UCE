/**
 * Copyright (C) 2011 Daniel Maier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.fhkn.in.uce.messages;

import java.io.IOException;

/**
 * The UCE attribute type gets encoded in the UCE Attribute Header to
 * distinguish different attributes.
 * 
 * @author Daniel Maier
 * 
 */
public interface UceAttributeType {
    /**
     * Returns the encoded representation of this {@link UceAttributeType}. Only
     * the least significant 16 Bits get used as a unsigned short.
     * 
     * @return the encoded representation of this {@link UceAttributeType}.
     */
    int encode();

    /**
     * Decodes a UCE Attribute.
     * 
     * @param encoded the value of the attribute encoded as a byte array.
     * @param header the header of the attribute.
     * @return the decoded {@link UceAttribute}.
     * @throws MessageFormatException if the message isn't formed properly
     * @throws IOException if an I/O error occurs
     */
    UceAttribute fromBytes(byte[] encoded, UceAttributeHeader header)
            throws MessageFormatException, IOException;
}
