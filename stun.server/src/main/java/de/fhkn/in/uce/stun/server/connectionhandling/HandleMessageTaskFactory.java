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
package de.fhkn.in.uce.stun.server.connectionhandling;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import de.fhkn.in.uce.core.socketlistener.SocketTaskFactory;

/**
 * Implementation of {@link SocketTaskFactory} which returns a
 * {@link HandleMessageTask}.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class HandleMessageTaskFactory implements SocketTaskFactory {
    private final InetSocketAddress primaryAddress;
    private final InetSocketAddress secondaryAddress;

    /**
     * Creates a {@link HandleMessageTaskFactory} which returns a
     * {@link HandleMessageTask}.
     * 
     * @param primaryAddress
     *            the primary (local) address of the stun server
     * @param secondaryAddress
     *            the secondary/alternate (local) address of the stun server
     */
    public HandleMessageTaskFactory(final InetSocketAddress primaryAddress, final InetSocketAddress secondaryAddress) {
        this.primaryAddress = primaryAddress;
        this.secondaryAddress = secondaryAddress;
    }

    @Override
    public Runnable getTask(final Socket s) throws IOException {
        return new HandleMessageTask(s, this.primaryAddress, this.secondaryAddress);
    }
}
