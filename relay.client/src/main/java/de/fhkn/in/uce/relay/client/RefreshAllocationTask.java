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
package de.fhkn.in.uce.relay.client;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.messages.MessageWriter;
import de.fhkn.in.uce.messages.SemanticLevel;
import de.fhkn.in.uce.messages.UceMessage;
import de.fhkn.in.uce.messages.UceMessageStaticFactory;
import de.fhkn.in.uce.relay.core.RelayLifetime;
import de.fhkn.in.uce.relay.core.RelayUceMethod;

/**
 * Task to refresh the allocation on the relay server.
 * 
 * @author thomas zink, daniel maier
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
    RefreshAllocationTask(MessageWriter controlConnectionWriter, int lifetime) {
        this.controlConnectionWriter = controlConnectionWriter;
        this.lifetime = lifetime;
    }

    /**
     * Sends a refresh request with the desired lifetime to the relay server.
     */
    public void run() {
        try {
            UceMessage refreshRequestMessage = UceMessageStaticFactory.newUceMessageInstance(
                    RelayUceMethod.REFRESH, SemanticLevel.REQUEST, UUID.randomUUID());
            refreshRequestMessage.addAttribute(new RelayLifetime(lifetime));
            controlConnectionWriter.writeMessage(refreshRequestMessage);
        } catch (IOException e) {
            logger.error("IOException while sending refresh request");
        }
    }

}
