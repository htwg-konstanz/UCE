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
package de.fhkn.in.uce.relay.server;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.messages.ErrorCode.ErrorCodes;
import de.fhkn.in.uce.messages.MessageWriter;
import de.fhkn.in.uce.messages.UceMessage;
import de.fhkn.in.uce.relay.core.RelayMessageReader;
import de.fhkn.in.uce.relay.core.RelayUceMethod;

/**
 * Task that reads messages from the clients socket and handles them. Can handle
 * allocation requests and and connection bind requests.
 * 
 * @author thomas zink, daniel maier
 * 
 */
public class MessageDispatcherTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MessageDispatcherTask.class);
    private final Socket s;
    private final MessageWriter controlConnectionWriter;
    private final Map<UUID, BlockingQueue<Socket>> connIDToQueue;
    // has to be unbounded
    private final Executor controlConnectionHandlerExecutor;
    // has to be unbounded
    private final Executor relayExecutor;

    /**
     * Creates a new {@link MessageDispatcherTask}.
     * 
     * @param s
     *            the socket to the client
     * @param connIDToQueue
     *            map to match relay connection between client and peers
     * @param controlConnectionHandlerExecutor
     *            the executor that gets used to execute the
     *            {@link RefreshMessageHandlerTask} for the given control
     *            connection
     * @param relayExecutor
     *            the executor that gets used to execute task for the real relay
     *            stuff
     * @throws IOException
     *             if an I/O error occurs while getting the output stream of the
     *             socket to the client
     */
    public MessageDispatcherTask(Socket s, Map<UUID, BlockingQueue<Socket>> connIDToQueue,
            Executor controlConnectionHandlerExecutor, Executor relayExecutor) throws IOException {
        this.s = s;
        this.controlConnectionWriter = new MessageWriter(s.getOutputStream());
        this.connIDToQueue = connIDToQueue;
        this.controlConnectionHandlerExecutor = controlConnectionHandlerExecutor;
        this.relayExecutor = relayExecutor;
    }

    /**
     * Reads the message from the input stream of the socket to the client. Then
     * distinguishes two messages: allocation request and connection bind
     * request. If the message is an allocation request an
     * {@link RelayAllocationHandler} gets used to handle the message. Else if
     * the message is an connection bind request a {@link ConnectionBindHandler}
     * gets used to handle the message. If the message was of unknown type a bad
     * request error is returned to the client.
     */
    public void run() {
        UceMessage message;
        try {
            message = RelayMessageReader.read(s.getInputStream());
        } catch (IOException e) {
            logger.error("IOEXception while receiving message: {}", e);
            return;
        }
        if (message.isMethod(RelayUceMethod.ALLOCATION) && message.isRequest()) {
            logger.info("Received allocation request");
            new RelayAllocationHandler(s, controlConnectionWriter, connIDToQueue, message,
                    controlConnectionHandlerExecutor, relayExecutor).handle();
        } else if (message.isMethod(RelayUceMethod.CONNECTION_BIND) && message.isRequest()) {
            logger.info("Received connection bind");
            new ConnectionBindHandler(s, message, connIDToQueue).handle();
        } else {
            // unknown message
            logger.error("Received wrong message tye {}", message.getMethod());
            try {
                UceMessage errorResponse = message.buildErrorResponse(ErrorCodes.BAD_REQUEST,
                        "Did not expect message " + message.getMethod());
                controlConnectionWriter.writeMessage(errorResponse);
            } catch (IOException e) {
                logger.error("IOException while sending error response");
            }
        }
    }

}
