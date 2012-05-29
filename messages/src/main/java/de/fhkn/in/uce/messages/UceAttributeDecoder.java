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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

/**
 * Class to decode byte encoded uce attributes. A {@link UceAttribute} consists
 * of a header and a value.
 * 
 * @author Daniel Maier
 * 
 */
final class UceAttributeDecoder {

    private final UceAttributeHeaderDecoder headerDecoder;

    /**
     * Creates a new {@link UceAttributeDecoder}.
     */
    UceAttributeDecoder() {
        this.headerDecoder = new UceAttributeHeaderDecoder();
    }

    /**
     * Creates a new {@link UceAttributeDecoder} that uses additionally the
     * specified {@link UceAttributeTypeDecoder} to decode custom uce attribute
     * types.
     * 
     * @param customAttributeTypeDecoder
     *            the {@link UceAttributeTypeDecoder} that gets used to decode
     *            custom uce attribute types
     * @throws NullPointerException
     *             if the specified {@link UceAttributeTypeDecoder} is null
     */
    UceAttributeDecoder(UceAttributeTypeDecoder customAttributeTypeDecoder) {
        if (customAttributeTypeDecoder == null) {
            throw new NullPointerException();
        }
        this.headerDecoder = new UceAttributeHeaderDecoder(customAttributeTypeDecoder);
    }

    /**
     * Decodes and returns the uce attributes from an array with byte encoded
     * uce attributes.
     * 
     * @param uceAttributesBytes
     *            the attributes (header and value) byte encoded
     * @return a list of the decoded uce attributes
     * @throws IOException
     *             if an I/O error occurs
     * @throws MessageFormatException
     *             if one of the attributes is malformed
     */
    List<UceAttribute> decodeUceAttributes(byte[] uceAttributesBytes) throws IOException,
            MessageFormatException {
        ByteArrayInputStream bin = new ByteArrayInputStream(uceAttributesBytes);
        DataInputStream din = new DataInputStream(bin);
        List<UceAttribute> attributes = new Vector<UceAttribute>();
        // read all attributes
        while (bin.available() > 0) {
            byte[] headerBits = new byte[UceAttributeHeaderDecoder.HEADER_LENGTH];
            din.readFully(headerBits);
            UceAttributeHeader header = headerDecoder.decodeUceAttributeHeader(headerBits);
            byte[] value = new byte[header.getLength()];
            din.readFully(value);
            UceAttribute attribute = header.getType().fromBytes(value, header);
            attributes.add(attribute);
        }
        return attributes;
    }
}