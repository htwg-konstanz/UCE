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
import java.io.InputStream;
import java.util.List;

/**
 * A {@link UceMessageReader} is used to read and decode an encoded uce message.
 * 
 * @author Daniel Maier
 * 
 */
public final class UceMessageReader {

    private final UceMessageHeaderDecoder headerDecoder;
    private final UceAttributeDecoder attributeDecoder;

    /**
     * Creates a new {@link UceMessageReader} that is able to decode messages
     * with {@link CommonUceMethod CommonUceMethods} and
     * {@link CommonUceAttributeType CommonUceAttributeTypes}.
     */
    public UceMessageReader() {
        headerDecoder = new UceMessageHeaderDecoder();
        attributeDecoder = new UceAttributeDecoder();
    }

    /**
     * Creates a new {@link UceMessageReader} that uses the
     * <code>customMethodDecoder</code> additionally to decode custom methods of
     * the message.
     * 
     * @param customMethodDecoder
     *            the decoder that gets used to decode custom uce methods.
     * @throws NullPointerException
     *             if the <code>customMethodDecoder</code> is null
     */
    public UceMessageReader(UceMethodDecoder customMethodDecoder) {
        if (customMethodDecoder == null) {
            throw new NullPointerException();
        }
        headerDecoder = new UceMessageHeaderDecoder(customMethodDecoder);
        attributeDecoder = new UceAttributeDecoder();
    }

    /**
     * Creates a new {@link UceMessageReader} that uses the
     * <code>customAttributeTypeDecoder</code> additionally to decode custom
     * attribute types.
     * 
     * @param customAttributeTypeDecoder
     *            the decoder that gets used to decode custom attribute types.
     * @throws NullPointerException
     *             if the <code>customAttributeTypeDecoder</code> is null
     */
    public UceMessageReader(UceAttributeTypeDecoder customAttributeTypeDecoder) {
        if (customAttributeTypeDecoder == null) {
            throw new NullPointerException();
        }
        headerDecoder = new UceMessageHeaderDecoder();
        attributeDecoder = new UceAttributeDecoder(customAttributeTypeDecoder);
    }

    /**
     * 
     * Creates a new {@link UceMessageReader} that uses the
     * <code>customMethodDecoder</code> and the
     * <code>customAttributeTypeDecoder</code> additionally to decode custom
     * methods of the message and custom attribute types.
     * 
     * @param customMethodDecoder
     *            the decoder that gets used to decode custom uce methods.
     * @param customAttributeTypeDecoder
     *            the decoder that gets used to decode custom attribute types.
     * 
     * @throws NullPointerException
     *             if the <code>customMethodDecoder</code> or the
     *             <code>customAttributeTypeDecoder</code> is null
     */
    public UceMessageReader(UceMethodDecoder customMethodDecoder,
            UceAttributeTypeDecoder customAttributeTypeDecoder) {
        if (customMethodDecoder == null || customAttributeTypeDecoder == null) {
            throw new NullPointerException();
        }
        headerDecoder = new UceMessageHeaderDecoder(customMethodDecoder);
        attributeDecoder = new UceAttributeDecoder(customAttributeTypeDecoder);
    }

    /**
     * Decodes a given byte encoded uce message.
     * 
     * @param encoded
     *            the byte encoded uce message
     * @return the decoded uce message
     * @throws IOException
     *             if an I/O error occurs
     */
    public UceMessage readUceMessage(byte[] encoded) throws IOException {
        return readUceMessage(new ByteArrayInputStream(encoded));
    }

    /**
     * Reads and decodes a uce message from the given input stream.
     * 
     * @param in
     *            the input stream from that the message gets read
     * @return the decoded uce message
     * @throws IOException
     *             if an I/O error occurs
     */
    public UceMessage readUceMessage(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        // read 20 bytes for message header
        byte[] headerBytes = new byte[20];
        dis.readFully(headerBytes);
        UceMessageHeader header = headerDecoder.decodeUceMessageHeader(headerBytes);
        // read rest of message
        byte[] attributeBytes = new byte[header.getLength()];
        dis.readFully(attributeBytes);
        List<UceAttribute> attributes = attributeDecoder.decodeUceAttributes(attributeBytes);
        UceMessage message = new UceMessageImpl(header);
        for (UceAttribute a : attributes) {
            message.addAttribute(a);
        }
        return message;
    }
}
