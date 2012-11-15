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
package de.fhkn.in.uce.stun.message;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.fhkn.in.uce.stun.attribute.Attribute;
import de.fhkn.in.uce.stun.attribute.AttributeDecoder;
import de.fhkn.in.uce.stun.attribute.AttributeTypeDecoder;
import de.fhkn.in.uce.stun.header.MessageHeader;
import de.fhkn.in.uce.stun.header.MessageHeaderDecoder;
import de.fhkn.in.uce.stun.header.MessageMethodDecoder;

/**
 * A {@link MessageReader} is used to read and decode an encoded message.
 * 
 * @author Daniel Maier, Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class MessageReader {

    private final MessageHeaderDecoder headerDecoder;
    private final AttributeDecoder attributeDecoder;

    private MessageReader(final List<MessageMethodDecoder> customMethodDecoders,
            final List<AttributeTypeDecoder> customAttributeTypeDecoders) {
        this.headerDecoder = new MessageHeaderDecoder(customMethodDecoders);
        this.attributeDecoder = new AttributeDecoder(customAttributeTypeDecoders);
    }

    /**
     * Factory method to create a message reader with a single
     * {@link MessageMethodDecoder}.
     * 
     * @param customMethodDecoder
     *            the custom decoder of type {@link MessageMethodDecoder}
     * @return the {@link MessageReader}
     */
    public static MessageReader createMessageReaderWithCustomMethodDecoder(
            final MessageMethodDecoder customMethodDecoder) {
        final List<MessageMethodDecoder> decoders = new ArrayList<MessageMethodDecoder>();
        decoders.add(customMethodDecoder);
        return new MessageReader(decoders, new ArrayList<AttributeTypeDecoder>());
    }

    /**
     * Factory method to create a message reader with a list of
     * {@link MessageMethodDecoder}.
     * 
     * @param customMethodDecoderList
     *            a list with custom decoders of type
     *            {@link MessageMethodDecoder}
     * @return the {@link MessageReader}
     */
    public static MessageReader createMessageReaderWithCustomMethodDecoderList(
            final List<MessageMethodDecoder> customMethodDecoderList) {
        return new MessageReader(customMethodDecoderList, new ArrayList<AttributeTypeDecoder>());
    }

    /**
     * Factory method to create a message reader with a single
     * {@link AttributeTypeDecoder}.
     * 
     * @param customAttributeTypeDecoder
     *            the custom decoder of type {@link AttributeTypeDecoder}
     * @return the {@link MessageReader}
     */
    public static MessageReader createMessageReaderWithCustomAttributeTypeDecoder(
            final AttributeTypeDecoder customAttributeTypeDecoder) {
        final List<AttributeTypeDecoder> decoders = new ArrayList<AttributeTypeDecoder>();
        decoders.add(customAttributeTypeDecoder);
        return new MessageReader(new ArrayList<MessageMethodDecoder>(), decoders);
    }

    /**
     * Factory method to create a message reader with a list of
     * {@link AttributeTypeDecoder}.
     * 
     * @param customAttributeTypeDecoderList
     *            a list with custom decoders of type
     *            {@link AttributeTypeDecoder}
     * @return the {@link MessageReader}
     */
    public static MessageReader createMessageReaderWithCustomAttributeTypeDecoderList(
            final List<AttributeTypeDecoder> customAttributeTypeDecoderList) {
        return new MessageReader(new ArrayList<MessageMethodDecoder>(), customAttributeTypeDecoderList);
    }

    /**
     * Factory method to create a message reader with a list of
     * {@link MessageMethodDecoder} and a list of {@link AttributeTypeDecoder}.
     * 
     * @param customMethodDecoders
     *            a list of custom decoders of type {@link MessageMethodDecoder}
     * @param customAttributeTypeDecoders
     *            a list of custom decoders of type {@link AttributeTypeDecoder}
     * @return the {@link MessageReader}
     */
    public static MessageReader createMessageReaderWithCustomDecoderLists(
            final List<MessageMethodDecoder> customMethodDecoders,
            final List<AttributeTypeDecoder> customAttributeTypeDecoders) {
        return new MessageReader(customMethodDecoders, customAttributeTypeDecoders);
    }

    /**
     * Factory method to create a message Reader.
     * 
     * @return the {@link MessageReader}
     */
    public static MessageReader createMessageReader() {
        return new MessageReader(new ArrayList<MessageMethodDecoder>(), new ArrayList<AttributeTypeDecoder>());
    }

    /**
     * Decodes a given byte encoded message.
     * 
     * @param encoded
     *            the byte encoded message
     * @return the decoded {@link Message}
     * @throws IOException
     *             if an I/O error occurs
     */
    public Message readSTUNMessage(final byte[] encoded) throws IOException {
        return this.readSTUNMessage(new ByteArrayInputStream(encoded));
    }

    /**
     * Reads and decodes a STUN message from the given input stream.
     * 
     * @param in
     *            the input stream from that the message gets read
     * @return the decoded {@link Message}
     * @throws IOException
     *             if an I/O error occurs
     */
    public Message readSTUNMessage(final InputStream in) throws IOException {
        final DataInputStream dis = new DataInputStream(in);
        // read 20 bytes for message header
        final byte[] headerBytes = new byte[20];
        dis.readFully(headerBytes);
        final MessageHeader header = this.headerDecoder.decodeSTUNMessageHeader(headerBytes);
        // read rest of message
        final byte[] attributeBytes = new byte[header.getLength()];
        dis.readFully(attributeBytes);
        final List<Attribute> attributes = this.attributeDecoder.decodeSTUNAttributes(attributeBytes, header);
        final Message message = new MessageImpl(header);
        for (final Attribute a : attributes) {
            message.addAttribute(a);
        }
        return message;
    }
}
