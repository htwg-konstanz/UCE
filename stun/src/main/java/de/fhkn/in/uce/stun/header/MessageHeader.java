/*
    Copyright (c) 2012 Alexander Diener,

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
package de.fhkn.in.uce.stun.header;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The STUN message header is built as follows (according to RFC 5389):
 *
 * <pre>
 *   0                   1                   2                   3
 *   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |0 0|     STUN Message Type     |         Message Length        |
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |                         Magic Cookie                          |
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |                                                               |
 *  |                     Transaction ID (96 bits)                  |
 *  |                                                               |
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 *
 * The two most significant bits must be zero. The STUN message type contains
 * the message class and the method of the message. Message length contains the
 * size of the message without the header. The magic cookie is a fixed value
 * which is defined in the STUN RFC 5389. The transaction id is for matching
 * request/response messages.
 *
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 *
 */
public interface MessageHeader {
    /**
     * The magic cookie is a fixed value which is defined by RFC 5389.
     */
    public static final int MAGIC_COOKIE = 0x2112A442;

    /**
     * Returns the class of a message.
     *
     * @return the class of a message
     */
    MessageClass getMessageClass();

    /**
     * Returns the method of a message.
     *
     * @return the method of a message
     */
    MessageMethod getMethod();

    /**
     * Returns the length of the message without header.
     *
     * @return the length of the message without header.
     */
    int getLength();

    /**
     * Returns the transaction id of the message.
     *
     * @return the transaction id of the message.
     */
    byte[] getTransactionId();

    /**
     * Writes this STUN message header byte encoded to the given output stream.
     *
     * @param out
     *            the output stream to that the message gets written to
     * @throws IOException
     *             if an I/O error occurs
     */
    void writeTo(OutputStream out) throws IOException;
}
