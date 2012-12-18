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
package de.fhkn.in.uce.relaying.core;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.relaying.message.RelayingLifetime;
import de.fhkn.in.uce.stun.header.STUNMessageClass;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageStaticFactory;
import de.fhkn.in.uce.stun.message.MessageWriter;

/**
 * Task to refresh the allocation on the relay server.
 * 
 * @author thomas zink, daniel maier, Alexander Diener
 *         (aldiener@htwg-konstanz.de)
 * 
 */
final class RefreshAllocationTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RefreshAllocationTask.class);
    private final MessageWriter controlConnectionWriter;
    private final int lifetime;

    /**
     * Creates a new {@link RefreshAllocationTask}.
     * 
     * @param controlConnectionWriter
     *            {@link MessageWriter} of the control connection to the relay
     *            server
     * @param lifetime
     *            the desired lifetime of the allocation until the next refresh
     *            request
     */
    RefreshAllocationTask(final MessageWriter controlConnectionWriter, final int lifetime) {
        this.controlConnectionWriter = controlConnectionWriter;
        this.lifetime = lifetime;
    }

    /**
     * Sends a refresh request with the desired lifetime to the relay server.
     */
    @Override
    public void run() {
        try {
            final Message refreshRequestMessage = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                    STUNMessageMethod.KEEP_ALIVE);
            refreshRequestMessage.addAttribute(new RelayingLifetime(this.lifetime));
            this.controlConnectionWriter.writeMessage(refreshRequestMessage);
        } catch (final IOException e) {
            logger.error("IOException while sending refresh request"); //$NON-NLS-1$
        }
    }
}
