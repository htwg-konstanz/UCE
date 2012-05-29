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
import java.io.OutputStream;

/**
 * UCE Attributes are the payload of UCE messages.
 * @author Daniel Maier
 *
 */
public interface UceAttribute {

    /**
     * Returns the type of this attribute.
     * 
     * @return the type of this attribute
     */
    UceAttributeType getType();

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
     * @param out the output stream the attribute gets written to 
     * @throws IOException if an I/O error occurs 
     */
    void writeTo(OutputStream out) throws IOException;
}
