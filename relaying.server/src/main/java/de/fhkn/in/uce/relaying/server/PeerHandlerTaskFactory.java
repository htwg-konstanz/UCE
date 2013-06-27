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
package de.fhkn.in.uce.relaying.server;

import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

import de.fhkn.in.uce.core.socketlistener.SocketTaskFactory;
import de.fhkn.in.uce.stun.message.MessageWriter;

/**
 * Factory to create new {@link PeerHandlerTask}.
 *
 * @author thomas zink, daniel maier
 *
 */
public class PeerHandlerTaskFactory implements SocketTaskFactory {

    private final Map<UUID, BlockingQueue<Socket>> connIDToQueue;
    private final MessageWriter controlConnection;
    // has to be unbounded
    private final Executor relayExecutor;

    /**
     * Creates a new {@link PeerHandlerTask}.
     *
     * @param connIDToQueue
     *            map to match relay connection between client and peers
     * @param controlConnection
     *            a {@link MessageWriter} to the control connection to the
     *            client
     * @param relayExecutor
     *            the executor that gets used to execute task for the real relay
     *            stuff.
     */
    public PeerHandlerTaskFactory(Map<UUID, BlockingQueue<Socket>> connIDToQueue, MessageWriter controlConnection,
            Executor relayExecutor) {
        this.connIDToQueue = connIDToQueue;
        this.controlConnection = controlConnection;
        this.relayExecutor = relayExecutor;
    }

    /**
     * Returns a new {@link PeerHandlerTask}.
     */
    @Override
    public Runnable getTask(Socket s) {
        return new PeerHandlerTask(s, connIDToQueue, controlConnection, relayExecutor);
    }

}
