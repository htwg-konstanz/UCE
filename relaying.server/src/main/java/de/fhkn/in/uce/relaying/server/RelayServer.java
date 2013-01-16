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

import static de.fhkn.in.uce.relaying.message.RelayingConstants.RELAYSERVER_DEFAULT_PORT;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.core.socketlistener.SocketListener;

/**
 * A Server that implements TURN-like behavior, to relay TCP data. But it is NOT
 * conform to the TURN standard (RFC 5766).
 * 
 * Hosts that want to allocate relay mappings on the server are called clients
 * and hosts that want to communicate with the clients through the relay server
 * are called peers.
 * 
 * A {@link RelayServer} maintains both control and data connections with its
 * clients over TCP. Control connections are responsible for allocation of relay
 * endpoints and for sending refresh messages. Data Connections are responsible
 * for sending connection bind requests and for the real relaying stuff.
 * 
 * @author Daniel Maier
 * 
 */
public final class RelayServer {
    private static final Logger logger = LoggerFactory.getLogger(RelayServer.class);
    private final Thread socketListener;

    /**
     * Creates a {@link RelayServer}. Has to be started via
     * {@link RelayServer#start() start()} in order that it is able to handle
     * incoming connections.
     * 
     * @param port
     *            the port on which the {@link RelayServer} listens for incoming
     *            control connections from clients
     * @throws IOException
     *             if an I/O error occurs
     */
    public RelayServer(int port) throws IOException {
        Map<UUID, BlockingQueue<Socket>> connIDToQueue = new ConcurrentHashMap<UUID, BlockingQueue<Socket>>();
        // has to be unbounded
        Executor controlConnectionHandlerExecutor = Executors.newCachedThreadPool();
        // has to be unbounded
        Executor relayExecutor = Executors.newCachedThreadPool();
        socketListener = new SocketListener(port, ServerSocketFactory.getDefault(), Executors.newCachedThreadPool(),
                new MessageDispatcherTaskFactory(connIDToQueue, controlConnectionHandlerExecutor, relayExecutor));
    }

    /**
     * Starts this {@link RelayServer}. Can be started only once.
     */
    public void start() {
        socketListener.start();
    }

    /**
     * Stops this {@link RelayServer}.
     */
    public void stop() {
        socketListener.interrupt();
    }

    /**
     * Creates and starts a new {@link RelayServer} instance.
     * 
     * @param args
     *            arguments for the {@link RelayServer}. An array with length of
     *            one is expected. It should contain the following value:
     *            args[0] the port on which the {@link RelayServer} listens for
     *            incoming control connections from clients via TCP. If nothing
     *            is defined, port 10300 is chosen as default.
     * @throws IOException
     *             if an I/O error occurs
     * @throws IllegalArgumentException
     *             if args[0] is set and it is not an integer value
     */
    public static void main(String[] args) throws IOException {
        int port = RELAYSERVER_DEFAULT_PORT;

        if (args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // wrong port format
                throw new IllegalArgumentException("Unrecognized argument " + args[0]
                        + "; you can optionally specify a port number.", e);
            }

        }

        RelayServer relayServer = new RelayServer(port);
        logger.info("Relay-Server is running on port " + port);
        relayServer.start();
    }
}
