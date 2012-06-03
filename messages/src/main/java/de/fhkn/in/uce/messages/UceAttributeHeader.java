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

/**
 * Decoded representation of a uce attribute header.
 * 
 * @author Daniel Maier
 * 
 */
public interface UceAttributeHeader {
    /**
     * Returns the type of the uce attribute.
     * 
     * @return the type of the uce attribute
     */
    UceAttributeType getType();

    /**
     * Returns the length of the uce attribute (without header).
     * 
     * @return the length of the uce attribute
     */
    int getLength();

    /**
     * Writes the {@link UceAttributeHeader} to the specified output stream.
     * 
     * @param out
     *            the output stream to that the header gets written to.
     * @throws IOException
     *             if an I/O error occurs
     */
    void writeTo(OutputStream out) throws IOException;
}
