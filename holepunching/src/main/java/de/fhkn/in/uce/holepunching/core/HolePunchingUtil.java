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
package de.fhkn.in.uce.holepunching.core;

import java.util.Set;

import net.jcip.annotations.Immutable;
import de.fhkn.in.uce.holepunching.message.HolePunchingMethodDecoder;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageReader;

/**
 * Singleton utility class which provides different functionality for hole
 * punching.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
@Immutable
public final class HolePunchingUtil {
    private static final HolePunchingUtil INSTANCE = new HolePunchingUtil();

    /**
     * Returns a {@link MessageReader} which is customized with the specific
     * {@link HolePunchingMethodDecoder} and
     * {@link HolePunchingAttributeTypeDecoder}.
     * 
     * @return the customized {@link MessageReader} for hole punching
     */
    public MessageReader getCustomHolePunchingMessageReader() {
        // final List<MessageMethodDecoder> hpMethodDecoders = new
        // ArrayList<MessageMethodDecoder>();
        // hpMethodDecoders.add(new HolePunchingMethodDecoder());
        // final List<AttributeTypeDecoder> hpAttrTypeDecoders = new
        // ArrayList<AttributeTypeDecoder>();
        // hpAttrTypeDecoders.add(new HolePunchingAttributeTypeDecoder());
        // return
        // MessageReader.createMessageReaderWithCustomDecoderLists(hpMethodDecoders,
        // hpAttrTypeDecoders);
        return MessageReader.createMessageReader();
    }

    /**
     * Checks if a message has {@link STUNMessageMethod} authenticate and is a
     * success response.
     * 
     * @param toCheck
     *            the message to check
     * @return true if the message is a authentification acknowledgment, false
     *         else
     */
    public boolean isAuthenticationAcknowledgmentMessage(final Message toCheck) {
        boolean result = false;
        if (toCheck.getMessageMethod() == STUNMessageMethod.AUTHENTICATE && toCheck.isSuccessResponse()) {
            result = true;
        }
        return result;
    }

    /**
     * Cancels a list of {@link CancelableTask}s but excludes the given one.
     * 
     * @param toCancel
     *            the {@link CancelableTask}s to cancel
     * @param exclude
     *            the excluded {@link CancelableTask}
     */
    public void stopCancableTasksExceptOwnTask(final Set<CancelableTask> toCancel, final CancelableTask exclude) {
        for (final CancelableTask t : toCancel) {
            if (t != exclude) {
                t.cancel();
            }
        }
    }

    /**
     * Returns the sole instance of {@link HolePunchingUtil}.
     * 
     * @return the sole instance of {@link HolePunchingUtil}
     */
    public static HolePunchingUtil getInstance() {
        return INSTANCE;
    }

    private HolePunchingUtil() {
        // private constructor
    }
}
