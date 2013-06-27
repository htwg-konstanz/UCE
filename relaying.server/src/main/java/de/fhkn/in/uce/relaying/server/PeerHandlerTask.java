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
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.relaying.message.RelayingMethod;
import de.fhkn.in.uce.stun.attribute.Token;
import de.fhkn.in.uce.stun.header.STUNMessageClass;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageStaticFactory;
import de.fhkn.in.uce.stun.message.MessageWriter;

/**
 * Task that handles new connections from peers.
 *
 * @author thomas zink, daniel maier, Alexander Diener
 *         (aldiener@htwg-konstanz.de)
 *
 */
public class PeerHandlerTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PeerHandlerTask.class);
    private static final long CONNECTION_BIND_TIMEOUT = 5000;
    private final Socket socketToPeer;
    private final Map<UUID, BlockingQueue<Socket>> connIDToQueue;
    private final MessageWriter controlConnection;
    // has to be unbounded
    private final Executor relayExecutor;

    /**
     * Creates a new {@link PeerHandlerTask}.
     *
     * @param socketToPeer
     *            socket to the peer
     * @param connIDToQueue
     *            map to match relay connection between client and peers
     * @param controlConnection
     *            a {@link MessageWriter} to the control connection to the
     *            client
     * @param relayExecutor
     *            the executor that gets used to execute task for the real relay
     *            stuff
     */
    public PeerHandlerTask(Socket socketToPeer, Map<UUID, BlockingQueue<Socket>> connIDToQueue,
            MessageWriter controlConnection, Executor relayExecutor) {
        this.socketToPeer = socketToPeer;
        this.connIDToQueue = connIDToQueue;
        this.controlConnection = controlConnection;
        this.relayExecutor = relayExecutor;
    }

    /**
     * Notifies the client that there is a new peer connection and sends a
     * connection attempt message to the client. Waits for a new data connection
     * from the client and than relays data between client and peer.
     */
    @Override
    public void run() {
        logger.debug("Accepted peer connection from: {}", socketToPeer);
        UUID connectionId = UUID.randomUUID();
        BlockingQueue<Socket> connectionBindQueue = new ArrayBlockingQueue<Socket>(1);
        connIDToQueue.put(connectionId, connectionBindQueue);
        try {
            Message connectionAttemptMessage = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.INDICATION,
                    RelayingMethod.CONNECTION_ATTEMPT);
            connectionAttemptMessage.addAttribute(new Token(connectionId));
            controlConnection.writeMessage(connectionAttemptMessage);
        } catch (IOException ex) {
            logger.error("IOException while sending Connection Attempt: {}", ex);
            connIDToQueue.remove(connectionId);
            try {
                socketToPeer.close();
            } catch (IOException ignore) {
            }
            return;
        }
        Socket clientSocket;
        try {
            clientSocket = connectionBindQueue.poll(CONNECTION_BIND_TIMEOUT, TimeUnit.MILLISECONDS);
            if (clientSocket == null) {
                // timeout expired
                connIDToQueue.remove(connectionId);
                // last chance
                clientSocket = connectionBindQueue.poll();
                if (clientSocket == null) {
                    logger.debug("Timeout expired for Connection ID: {}", connectionId);
                    try {
                        socketToPeer.close();
                    } catch (IOException ignore) {
                    }
                    return;
                }
            }
        } catch (InterruptedException e) {
            logger.debug("Got interrupted");
            connIDToQueue.remove(connectionId);
            try {
                socketToPeer.close();
            } catch (IOException ignore) {
            }
            clientSocket = connectionBindQueue.poll();
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException ignore) {
                }
            }
            Thread.currentThread().interrupt();
            return;
        }
        relayData(socketToPeer, clientSocket);
    }

    /**
     * Relays data between one client and one peer in both directions.
     *
     * @param peerSocket
     *            socket to the peer
     * @param clientSocket
     *            socket to the client
     */
    private void relayData(Socket peerSocket, Socket clientSocket) {
        RelayTask peerToClientRelay = new RelayTask(peerSocket, clientSocket);
        RelayTask clientToPeerRelay = new RelayTask(clientSocket, peerSocket);

        relayExecutor.execute(peerToClientRelay);
        relayExecutor.execute(clientToPeerRelay);
    }

}
