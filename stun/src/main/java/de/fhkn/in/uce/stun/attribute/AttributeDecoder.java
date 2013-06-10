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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import de.fhkn.in.uce.stun.header.MessageHeader;
import de.fhkn.in.uce.stun.util.MessageFormatException;

/**
 * Class to decode byte encoded STUN attributes. A {@link Attribute} consists of
 * a header and a value.
 * 
 * @author Daniel Maier, Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class AttributeDecoder {

    private final AttributeHeaderDecoder headerDecoder;

    /**
     * Creates a new {@link AttributeDecoder}.
     */
    public AttributeDecoder() {
        this.headerDecoder = new AttributeHeaderDecoder();
    }

    /**
     * Creates a new {@link AttributeDecoder} that uses additionally the
     * specified {@link AttributeTypeDecoder} to decode custom STUN attribute
     * types.
     * 
     * @param customAttributeTypeDecoder
     *            a list of {@link AttributeTypeDecoder} that gets used to
     *            decode custom STUN attribute types
     * @throws NullPointerException
     *             if the specified {@link AttributeTypeDecoder} is null
     */
    public AttributeDecoder(final List<AttributeTypeDecoder> customAttributeTypeDecoders) {
        if (customAttributeTypeDecoders == null) {
            throw new NullPointerException();
        }
        this.headerDecoder = new AttributeHeaderDecoder(customAttributeTypeDecoders);
    }

    /**
     * Decodes and returns the STUN attributes from an array with byte encoded
     * STUN attributes.
     * 
     * @param uceAttributesBytes
     *            the attributes (header and value) byte encoded
     * @return a list of the decoded STUN attributes
     * @throws IOException
     *             if an I/O error occurs
     * @throws MessageFormatException
     *             if one of the attributes is malformed
     */
    public List<Attribute> decodeSTUNAttributes(final byte[] attributesBytes, final MessageHeader messageHeader)
            throws IOException, MessageFormatException {
        final ByteArrayInputStream bin = new ByteArrayInputStream(attributesBytes);
        final DataInputStream din = new DataInputStream(bin);
        final List<Attribute> attributes = new Vector<Attribute>();
        while (bin.available() > 0) {
            final byte[] headerBits = new byte[AttributeHeader.HEADER_LENGTH];
            din.readFully(headerBits);
            final AttributeHeader header = this.headerDecoder.decodeSTUNAttributeHeader(headerBits);
            final byte[] value = new byte[header.getLength()];
            din.readFully(value);
            final Attribute attribute = header.getType().fromBytes(value, header, messageHeader);
            attributes.add(attribute);
        }
        return attributes;
    }
}
