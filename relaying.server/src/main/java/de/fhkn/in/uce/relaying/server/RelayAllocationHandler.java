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

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.core.socketlistener.SocketListener;
import de.fhkn.in.uce.relaying.message.RelayingLifetime;
import de.fhkn.in.uce.stun.attribute.EndpointClass;
import de.fhkn.in.uce.stun.attribute.EndpointClass.EndpointCategory;
import de.fhkn.in.uce.stun.attribute.ErrorCode.STUNErrorCode;
import de.fhkn.in.uce.stun.attribute.XorMappedAddress;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageWriter;

/**
 * Class to handle allocation requests from clients.
 *
 * @author thomas zink, daniel maier, Alexander Diener
 *         (aldiener@htwg-konstanz.de)
 *
 */
final class RelayAllocationHandler {
    private static final Logger logger = LoggerFactory.getLogger(RelayAllocationHandler.class);
    public static final int MIN_PORT = 10150;
    public static final int MAX_PORT = 10160;

    private final Socket controlConnection;
    private final MessageWriter controlConnectionWriter;
    private final Map<UUID, BlockingQueue<Socket>> connIDToQueue;
    private final Message relayAllocationMessage;
    // has to be unbounded
    private final Executor controlConnectionHandlerExecutor;
    // has to be unbounded
    private final Executor relayExecutor;

    /**
     * Creates a new {@link RelayAllocationHandler}.
     *
     * @param controlConnection
     *            the socket of the control connection to the client
     * @param controlConnectionWriter
     *            a {@link MessageWriter} to the control connection
     * @param connIDToQueue
     *            map to match relay connection between client and peers
     * @param relayAllocationMessage
     *            the allocation request message
     * @param controlConnectionHandlerExecutor
     *            the executor that gets used to execute the
     *            {@link RefreshMessageHandlerTask} for the given control
     *            connection
     * @param relayExecutor
     *            the executor that gets used to execute task for the real relay
     *            stuff
     */
    public RelayAllocationHandler(Socket controlConnection, MessageWriter controlConnectionWriter,
            Map<UUID, BlockingQueue<Socket>> connIDToQueue, Message relayAllocationMessage,
            Executor controlConnectionHandlerExecutor, Executor relayExecutor) {
        this.controlConnection = controlConnection;
        this.controlConnectionWriter = controlConnectionWriter;
        this.connIDToQueue = connIDToQueue;
        this.relayAllocationMessage = relayAllocationMessage;
        this.controlConnectionHandlerExecutor = controlConnectionHandlerExecutor;
        this.relayExecutor = relayExecutor;
    }

    /**
     * Handles the allocation request message. Creates a new ServerSocket and
     * listener thread to handle connection requests from peers. If there is no
     * free port available in the specified port range, an
     * "Insufficient Capacity" error is returned to the client.
     */
    public void handle() {
        try {
            // get Lifetime
            int lifetime = 0;
            if (relayAllocationMessage.hasAttribute(RelayingLifetime.class)) {
                lifetime = relayAllocationMessage.getAttribute(RelayingLifetime.class).getLifeTime();
            }
            // Create ServerSocket for peers
            ServerSocket peerSS = createServerSocketWithinPortRange();
            if (peerSS == null) {
                logger.info("Insufficient Capacity");
                Message errorResponse = relayAllocationMessage.buildFailureResponse(
                        STUNErrorCode.INSUFFICIENT_CAPACITY, "Insufficient Capacity");
                controlConnectionWriter.writeMessage(errorResponse);
            } else {
                // TODO listener tasks in threadpool, listener also in pool?
                logger.info("Created allocation on {}", peerSS.getLocalSocketAddress());
                SocketListener peerSocketListener = new SocketListener(peerSS, Executors.newCachedThreadPool(),
                        new PeerHandlerTaskFactory(connIDToQueue, controlConnectionWriter, relayExecutor));
                controlConnectionHandlerExecutor.execute(new RefreshMessageHandlerTask(controlConnection,
                        controlConnectionWriter, lifetime, peerSocketListener));
                peerSocketListener.start();
                Message successResponse = relayAllocationMessage.buildSuccessResponse();
                successResponse.addAttribute(new XorMappedAddress(new InetSocketAddress(peerSS.getInetAddress(), peerSS
                        .getLocalPort())));
                successResponse.addAttribute(new EndpointClass(EndpointCategory.RELAY));
                successResponse.addAttribute(new RelayingLifetime(lifetime));
                controlConnectionWriter.writeMessage(successResponse);
            }
        } catch (IOException e) {
            logger.error("IOException while handling allocation request: {}", e);
        }
    }

    /**
     * Tries to to create and bind a new ServerSocket in the specified port
     * range.
     *
     * @return a new and bound ServerSocket, or null if no ServerSocket could be
     *         bounded in the specified port range.
     * @throws IOException
     *             if an I/O error occurs when opening the socket.
     */
    private ServerSocket createServerSocketWithinPortRange() throws IOException {
        int port = MIN_PORT;
        do {
            try {
                return new ServerSocket(port);
            } catch (BindException be) {
                port++;
            }
        } while (port <= MAX_PORT);
        return null;
    }
}
