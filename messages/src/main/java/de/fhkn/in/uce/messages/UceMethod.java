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

/**
 * A {@link UceMethod} represents the method part of a uce message. Predefined
 * uce methods can be found in {@link CommonUceMethod}.
 * 
 * @author Daniel Maier
 * 
 */
public interface UceMethod {
    /**
     * Returns the encoded representation of this {@link UceMethod}. Only the
     * least significant 16 Bits get used as a unsigned short.
     * 
     * @return the encoded representation of this {@link UceMethod}.
     */
    int encode();
}
