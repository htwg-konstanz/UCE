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
package de.fhkn.in.uce.relaying.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.fhkn.in.uce.stun.attribute.AttributeTypeDecoder;
import de.fhkn.in.uce.stun.header.MessageMethodDecoder;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageReader;

/**
 * Helper class to read {@link Message messages} from an input stream.
 * 
 * @author Daniel Maier, Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public class RelayingMessageReader {
    private static MessageReader reader = MessageReader.createMessageReaderWithCustomDecoderLists(
            getCustomRelayingMessageMethodDecoders(), getCustomRelayingAttributeTypeDecoders());

    private static List<MessageMethodDecoder> getCustomRelayingMessageMethodDecoders() {
        final List<MessageMethodDecoder> result = new ArrayList<MessageMethodDecoder>();
        result.add(new RelayingMethodDecoder());
        return result;
    }

    private static List<AttributeTypeDecoder> getCustomRelayingAttributeTypeDecoders() {
        final List<AttributeTypeDecoder> result = new ArrayList<AttributeTypeDecoder>();
        result.add(new RelayingAttributeTypeDecoder());
        return result;
    }

    /**
     * Reads a {@link Message} from the given stream. In addition to the common
     * methods and attributes types this method also decodes relay methods and
     * attribute types.
     * 
     * @param in
     *            the input stream to be read from
     * @return the decoded {@link Message}
     * @throws IOException
     *             if an I/O error occurs
     */
    public static Message read(InputStream in) throws IOException {
        return reader.readSTUNMessage(in);
    }
}
