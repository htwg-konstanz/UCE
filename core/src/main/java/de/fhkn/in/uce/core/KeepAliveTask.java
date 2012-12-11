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
package de.fhkn.in.uce.core;

import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.stun.attribute.Username;
import de.fhkn.in.uce.stun.header.STUNMessageClass;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageStaticFactory;

/**
 * Task which keeps the binding of a target alive. A message with the given
 * target id is sent via the control connection.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class KeepAliveTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(KeepAliveTask.class);
    private final String targetId;
    private final Socket controlConnection;

    /**
     * Creates a {@link KeepAliveTask} with the given target and mediator
     * connection.
     * 
     * @param targetId
     *            the id of the target
     * @param controlConnection
     *            the connection to the mediator
     */
    public KeepAliveTask(final String targetId, final Socket controlConnection) {
        this.targetId = targetId;
        this.controlConnection = controlConnection;
    }

    @Override
    public void run() {
        try {
            final Message keepAliveMessage = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                    STUNMessageMethod.KEEP_ALIVE);
            final Username userNameAttribute = new Username(this.targetId);
            keepAliveMessage.addAttribute(userNameAttribute);
            keepAliveMessage.writeTo(this.controlConnection.getOutputStream());
        } catch (final Exception e) {
            logger.error("Exception while sending keep-alive message: {}", e.getMessage()); //$NON-NLS-1$
        }
    }
}
