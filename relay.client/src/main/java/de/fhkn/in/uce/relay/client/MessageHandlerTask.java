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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.messages.MessageWriter;
import de.fhkn.in.uce.messages.SemanticLevel;
import de.fhkn.in.uce.messages.UceMessage;
import de.fhkn.in.uce.messages.UceMessageStaticFactory;
import de.fhkn.in.uce.messages.UniqueId;
import de.fhkn.in.uce.relay.core.RelayLifetime;
import de.fhkn.in.uce.relay.core.RelayMessageReader;
import de.fhkn.in.uce.relay.core.RelayUceMethod;
import de.fhkn.in.uce.relay.core.Statics;


/**
 * Task to handle incoming messages from the relay server over the control
 * connection. Can handle connection attempt indications and allocation refresh
 * responses.
 * 
 * @author thomas.zink, daniel maier
 * 
 */
final class MessageHandlerTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandlerTask.class);
    private final Socket controlConnection;
    private final MessageWriter controlConnectionWriter;
    private final BlockingQueue<Socket> socketQueue;
    private final InetSocketAddress relayServerEndpoint;
    private final ScheduledExecutorService refreshExecutor;
    private volatile boolean cancelled;

    /**
     * Creates a new {@link MessageHandlerTask}.
     * 
     * @param controlConnection
     *            socket of the control connection to the relay server
     * @param controlConnectionWriter
     *            {@link MessageWriter} of the control connection
     * @param relayServerEndpoint
     *            endpoint of the relay server on that it is waiting for new
     *            data connections
     * @param socketQueue
     *            queue to put new established data connections to the relay
     *            server
     * @param refreshExecutor
     *            executor that gets used to execute the periodic refresh
     *            request task
     */
    MessageHandlerTask(Socket controlConnection, MessageWriter controlConnectionWriter,
            InetSocketAddress relayServerEndpoint, BlockingQueue<Socket> socketQueue,
            ScheduledExecutorService refreshExecutor) {
        this.controlConnection = controlConnection;
        this.controlConnectionWriter = controlConnectionWriter;
        this.socketQueue = socketQueue;
        this.relayServerEndpoint = relayServerEndpoint;
        this.refreshExecutor = refreshExecutor;
    }

    /**
     * Waits for new messages from the relay server and handles them. It can
     * handle connection attempt indications and allocation refresh responses.
     */
    public void run() {
        UceMessage message;
        while (!cancelled) {
            try {
                message = RelayMessageReader.read(controlConnection.getInputStream());
                if (message == null) {
                    // server closed connection
                    logger.error("IOException while receiving message (message was null)");
                    socketQueue.add(new Socket());
                    return;
                }
                if (message.isMethod(RelayUceMethod.CONNECTION_ATTEMPT) && message.isIndication()) {
                    // Connect to the relay address to establish a
                    // data connection
                    Socket s = new Socket();
                    s.connect(relayServerEndpoint);
                    MessageWriter dataConnectionWriter = new MessageWriter(s.getOutputStream());
                    UceMessage connectionBindRequestMessage = UceMessageStaticFactory
                            .newUceMessageInstance(RelayUceMethod.CONNECTION_BIND,
                                    SemanticLevel.REQUEST, UUID.randomUUID());
                    connectionBindRequestMessage.addAttribute(new UniqueId(message.getAttribute(
                            UniqueId.class).getId()));
                    dataConnectionWriter.writeMessage(connectionBindRequestMessage);
                    // TODO erfolgs oder fehlermeldung abwarten
                    socketQueue.add(s);
                } else if (message.isMethod(RelayUceMethod.REFRESH) && message.isSuccessResponse()) {
                    int lifetime = message.getAttribute(RelayLifetime.class).getLifeTime();
                    logger.debug("Received lifetime response {}", lifetime);
                    refreshExecutor.schedule(new RefreshAllocationTask(controlConnectionWriter,
                            lifetime), Math.max(lifetime - Statics.ALLOCATION_LIFETIME_ADVANCE,
                            Statics.ALLOCATION_LIFETIME_MIN), TimeUnit.SECONDS);
                } else {
                    logger.error("Received unexpected message {}", message.getMethod());
                    socketQueue.add(new Socket());
                }
            } catch (SocketException e) {
                logger.debug("SocketException while receiving message (probably cancelation)");
            } catch (IOException e) {
                logger.error("IOException while receiving message {}", e);
                socketQueue.add(new Socket());
            }
        }
    }

    /**
     * Cancels this task by closing the control connection to the relay server.
     */
    void cancel() {
        logger.debug("Cancel MessageHandlerTask");
        cancelled = true;
        refreshExecutor.shutdownNow();
        try {
            controlConnection.close();
        } catch (IOException ignore) {
        }
    }

}
