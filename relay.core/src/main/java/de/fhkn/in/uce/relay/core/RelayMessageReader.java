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
package de.fhkn.in.uce.relay.core;

import java.io.IOException;
import java.io.InputStream;

import de.fhkn.in.uce.messages.UceMessage;
import de.fhkn.in.uce.messages.UceMessageReader;

/**
 * Helper class to read {@link UceMessage uce messages} from an input stream.
 * 
 * @author Daniel Maier
 * 
 */
public class RelayMessageReader {
    private static UceMessageReader reader = new UceMessageReader(new RelayUceMethodDecoder(),
            new RelayUceAttributeTypeDecoder());

    /**
     * Reads a {@link UceMessage} from the given stream. In addition to the
     * common uce methods and attributes types this method also decodes relay
     * uce methods and attribute types.
     * 
     * @param in
     *            the input stream to be read from
     * @return the decoded {@link UceMessage}
     * @throws IOException
     *             if an I/O error occurs
     */
    public static UceMessage read(InputStream in) throws IOException {
        return reader.readUceMessage(in);
    }
}
