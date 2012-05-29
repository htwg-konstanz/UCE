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

import java.util.UUID;

/**
 * The class {@link UceMessageStaticFactory} provides static factory methods to
 * create instances of {@link UceMessage}.
 * 
 * @author Daniel Maier
 * 
 */
public final class UceMessageStaticFactory {

    /**
     * Returns a {@link UceMessage} that is configured with the given
     * parameters.
     * 
     * @param method
     *            the method of the message
     * @param semanticLevel
     *            the semantic level of the message
     * @param transactionId
     *            the transaction id of the message
     * @return a {@link UceMessage} that is configured with the given parameters
     */
    public static UceMessage newUceMessageInstance(final UceMethod method,
            final SemanticLevel semanticLevel, final UUID transactionId) {
        if (method == null || semanticLevel == null || transactionId == null) {
            throw new NullPointerException();
        }
        return new UceMessageImpl(method, semanticLevel, transactionId);
    }

    /**
     * Returns a {@link UceMessage} that is configured with the given
     * parameters. Additionally it sets a random transaction id for the message.
     * 
     * @param method
     *            the method of the message
     * @param semanticLevel
     *            the semantic level of the message
     * @return a {@link UceMessage} that is configured with the given parameters
     *         and a random transaction id
     */
    public static UceMessage newUceMessageInstance(final UceMethod method,
            final SemanticLevel semanticLevel) {
        if (method == null || semanticLevel == null) {
            throw new NullPointerException();
        }
        return newUceMessageInstance(method, semanticLevel, UUID.randomUUID());
    }
}
