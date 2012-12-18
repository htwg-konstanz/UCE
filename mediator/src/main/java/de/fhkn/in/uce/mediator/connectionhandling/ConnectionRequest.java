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
package de.fhkn.in.uce.mediator.connectionhandling;

import java.net.Socket;

import de.fhkn.in.uce.stun.message.Message;

/**
 * Helper object to remind the connection request message and the control
 * connection of a source. This object can be used to save (temporarily) a
 * connection request.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class ConnectionRequest {
    private final Socket controlConnection;
    private final Message connectionRequestMessage;

    /**
     * Creates a {@link ConnectionRequest} object.
     * 
     * @param controlConnection
     *            the control connection to the source
     * @param connectionRequestMessage
     *            the connection request message from the source
     */
    public ConnectionRequest(final Socket controlConnection, final Message connectionRequestMessage) {
        this.connectionRequestMessage = connectionRequestMessage;
        this.controlConnection = controlConnection;
    }

    /**
     * Returns the control connection to the connection request message.
     * 
     * @return the control connetion to the source
     */
    public Socket getControlConnection() {
        return controlConnection;
    }

    /**
     * Returns the connection request message from a source.
     * 
     * @return the connection request message from a source
     */
    public Message getConnectionRequestMessage() {
        return connectionRequestMessage;
    }
}