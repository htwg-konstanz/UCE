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
package de.fhkn.in.uce.stun.message;

import de.fhkn.in.uce.stun.header.MessageClass;
import de.fhkn.in.uce.stun.header.MessageMethod;
import de.fhkn.in.uce.stun.util.MessageUtil;
import de.fhkn.in.uce.stun.util.MessageUtilImpl;

/**
 * The class {@link MessageStaticFactory} provides static factory methods to
 * create instances of {@link Message}.
 * 
 * @author Daniel Maier, Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class MessageStaticFactory {

    /**
     * Creates a implementation of {@link Message}.
     * 
     * @param messageClass
     *            the class of the message
     * @param method
     *            the method of the message
     * @param transactionID
     *            the transaction id of the message
     * @return the created {@link Message}
     */
    public static Message newSTUNMessageInstance(final MessageClass messageClass, final MessageMethod method,
            final byte[] transactionId) {
        if (messageClass == null || method == null || transactionId == null) {
            throw new NullPointerException();
        }
        return new MessageImpl(messageClass, method, transactionId);
    }

    /**
     * Creates a implementation of {@link Message} with a generated transaction
     * id.
     * 
     * @param messageClass
     *            the class of the message
     * @param method
     *            the method of the message
     * @return the created {@link Message}
     */
    public static Message newSTUNMessageInstance(final MessageClass messageClass, final MessageMethod method) {
        if (messageClass == null || method == null) {
            throw new NullPointerException();
        }
        final MessageUtil util = MessageUtilImpl.getInstance();
        return newSTUNMessageInstance(messageClass, method, util.generateSecureTranactionId());
    }
}
