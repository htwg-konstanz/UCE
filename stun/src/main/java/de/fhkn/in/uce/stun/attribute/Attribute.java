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
import java.io.OutputStream;

/**
 * Attributes are the payload of messages. A attribute is TLV encoded with a
 * 16-bit type, a 16-bit length and a value. The value is realized in the
 * different implementations of the attributes. The format of a STUN attribute
 * is a follows:
 * 
 * <pre>
 *      0                   1                   2                   3
 *      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |         Type                  |            Length             |
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |                         Value (variable)                ....
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ *
 * </pre>
 * 
 * @author Daniel Maier, Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public interface Attribute {
    /**
     * Returns the type of this attribute.
     * 
     * @return the type of this attribute
     */
    AttributeType getType();

    /**
     * Returns the length without header of this attribute.
     * 
     * @return the length without header of this attribute
     */
    int getLength();

    /**
     * Writes the value (without header) of this attribute to the given output
     * stream.
     * 
     * @param out
     *            the output stream the attribute gets written to
     * @throws IOException
     *             if an I/O error occurs
     */
    void writeTo(OutputStream out) throws IOException;
}
