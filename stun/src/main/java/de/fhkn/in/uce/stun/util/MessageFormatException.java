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
package de.fhkn.in.uce.stun.util;

import java.io.IOException;

/**
 * Exception that signals that a message was malformed.
 * 
 * @author Daniel Maier
 * 
 */
public final class MessageFormatException extends IOException {

    private static final long serialVersionUID = 6400052438768955799L;

    /**
     * Creates a new MessageFormatException.
     * 
     * @param message
     *            the detail message
     */
    public MessageFormatException(String message) {
        super(message);
    }

    /**
     * Creates a new MessageFormatException.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public MessageFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
