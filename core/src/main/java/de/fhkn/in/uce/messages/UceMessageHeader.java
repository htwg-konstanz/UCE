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
import java.io.OutputStream;
import java.util.UUID;

/**
 * The decoded representation of the header for uce messages. This header gets
 * byte encoded as follows (where SL stands for semantic level): <br/>
 * 
 * <pre>
 * 
 *  0        3  4                          13 14 15
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |    MAGIC  |           Method            | SL  |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |                     Length                    |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |                 Transaction ID                |
 * .                    (128 Bit)                  .
 * .                                               .
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * 
 * 
 * </pre>
 * 
 * @author Daniel Maier
 * 
 */
public interface UceMessageHeader {
    /**
     * Returns the method of the message.
     * 
     * @return the method of the message
     */
    UceMethod getMethod();

    /**
     * Returns the semantic level of the message
     * 
     * @return the semantic level of the message
     */
    SemanticLevel getSemanticLevel();

    /**
     * Returns the length of the message (without the message header).
     * 
     * @return the length of the message
     */
    int getLength();

    /**
     * Returns the transaction id of the message.
     * 
     * @return the transaction id of the message
     */
    UUID getTransactionId();

    /**
     * Writes this uce message header byte encoded to the given output stream.
     * 
     * @param out
     *            the output stream to that the message gets written to
     * @throws IOException
     *             if an I/O error occurs
     */
    void writeTo(OutputStream out) throws IOException;
}
