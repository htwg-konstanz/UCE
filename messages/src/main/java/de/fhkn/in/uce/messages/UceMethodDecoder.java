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

/**
 * A {@link UceMethodDecoder} is used to decode the method of a uce message.
 * 
 * @author Daniel Maier
 * 
 */
public interface UceMethodDecoder {
    /**
     * Decodes the given encoded UCE method. Uses only the 16 least significant
     * bits of the given argument.
     * 
     * @param encoded
     *            the encoded UCE method
     * @return the decoded {@link UceMethodDecoder} or null if the method is
     *         unknown
     */
    UceMethod decode(int encoded);
}
