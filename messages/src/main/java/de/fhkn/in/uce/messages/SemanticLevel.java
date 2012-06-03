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

import java.util.HashMap;
import java.util.Map;

/**
 * Enum to indicate which semantic a {@link UceMessage} has.
 * 
 * @author Daniel Maier
 * 
 */
public enum SemanticLevel {
    /**
     * {@link UceMessage} is a request.
     */
    REQUEST(0x0),
    /**
     * {@link UceMessage} is a response and operation was successful.
     */
    SUCCESS_RESPONSE(0x1),
    /**
     * {@link UceMessage} is a response but an error occurred while operation.
     */
    ERROR_RESPONSE(0x2),
    /**
     * {@link UceMessage} is an indication. There must be no repsponse to an
     * indication.
     */
    INDICATION(0x3);

    private static final Map<Integer, SemanticLevel> intToEnum = new HashMap<Integer, SemanticLevel>();

    static {
        for (SemanticLevel l : values()) {
            intToEnum.put(l.encoded, l);
        }
    }

    private final int encoded;

    /**
     * Creates a new {@link SemanticLevel}.
     * 
     * @param encoded
     *            the byte encoded representation of this {@link SemanticLevel}
     */
    private SemanticLevel(int encoded) {
        this.encoded = encoded;
    }

    /**
     * Returns the byte encoded representation of this {@link SemanticLevel}.
     * 
     * @return the byte encoded representation of this {@link SemanticLevel}
     */
    int encode() {
        return encoded;
    }

    /**
     * Decodes the {@link SemanticLevel} from the specified encoded semantic
     * level.
     * 
     * @param encoded
     *            the byte encoded representation of the {@link SemanticLevel}
     * @return the decoded {@link SemanticLevel}
     */
    static SemanticLevel fromEncoded(int encoded) {
        return intToEnum.get(encoded);
    }
}
