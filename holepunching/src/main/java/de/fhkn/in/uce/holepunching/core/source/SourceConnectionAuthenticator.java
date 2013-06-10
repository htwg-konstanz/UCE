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
package de.fhkn.in.uce.holepunching.core.source;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.holepunching.core.CancelableTask;
import de.fhkn.in.uce.holepunching.core.ConnectionAuthenticator;
import de.fhkn.in.uce.holepunching.core.HolePunchingUtil;
import de.fhkn.in.uce.stun.attribute.Token;
import de.fhkn.in.uce.stun.header.STUNMessageClass;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageReader;
import de.fhkn.in.uce.stun.message.MessageStaticFactory;
import de.fhkn.in.uce.stun.message.MessageWriter;

/**
 * Implementation of {@link ConnectionAuthenticator} on the source side.
 *
 * @author Daniel Maier
 *
 */
public class SourceConnectionAuthenticator implements ConnectionAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(SourceConnectionAuthenticator.class);
    private final HolePunchingUtil hpUtil;
    private final UUID authentikationToken;

    public SourceConnectionAuthenticator(final UUID authentikationToken) {
        this.hpUtil = HolePunchingUtil.getInstance();
        this.authentikationToken = authentikationToken;
    }

    /**
     * Authentication mechanism on the source side. It first sends an
     * AuthenticationMessage. Then it waits for the AuthenticationAckMessage of
     * the target. If AuthenticationAckMessage was positive it sends
     * AuthenticationAckMessage to the target. If authentication is successful
     * all hole punching threads get stopped.
     */
    @Override
    public boolean authenticate(final Socket toBeAuthenticated, final Set<CancelableTask> relatedHolePunchingTasks,
            final CancelableTask ownTask, final Object sharedLock) throws IOException {
        boolean result = false;
        logger.info("Trying to authenticate socket: {}", toBeAuthenticated); //$NON-NLS-1$
        final MessageWriter messageWriter = new MessageWriter(toBeAuthenticated.getOutputStream());
        final MessageReader messageReader = this.hpUtil.getCustomHolePunchingMessageReader();
        // sending authentication message
        final Message authenticationMessage = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                STUNMessageMethod.AUTHENTICATE);
        authenticationMessage.addAttribute(new Token(this.authentikationToken));
        logger.info("Sending AuthenticationMessage: {}", authenticationMessage); //$NON-NLS-1$
        messageWriter.writeMessage(authenticationMessage);
        // receiving authentication acknowledgment message
        final Message reveivedAckMessage = messageReader.readSTUNMessage(toBeAuthenticated.getInputStream());
        if (this.hpUtil.isAuthenticationAcknowledgmentMessage(reveivedAckMessage)) {
            synchronized (sharedLock) {
                // sending second authentication acknowledgment message
                final Message secondAckMessage = reveivedAckMessage.buildSuccessResponse();
                messageWriter.writeMessage(secondAckMessage);
                logger.debug("Authentication successfull, stopping hole punching threads"); //$NON-NLS-1$
                this.hpUtil.stopCancableTasksExceptOwnTask(relatedHolePunchingTasks, ownTask);
                result = true;
            }
        }
        logger.debug("Authentication failed"); //$NON-NLS-1$
        return result;
    }
}
