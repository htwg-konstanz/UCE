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
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.core.socketlistener.SocketListener;
import de.fhkn.in.uce.relaying.message.RelayingAttributeTypeDecoder;
import de.fhkn.in.uce.relaying.message.RelayingConstants;
import de.fhkn.in.uce.relaying.message.RelayingLifetime;
import de.fhkn.in.uce.stun.attribute.ErrorCode.STUNErrorCode;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageReader;
import de.fhkn.in.uce.stun.message.MessageWriter;

/**
 * Task that handles refresh messages of client.
 * 
 * @author thomas zink, daniel maier, Alexander Diener
 *         (aldiener@htwg-konstanz.de)
 * 
 */
public final class RefreshMessageHandlerTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RefreshMessageHandlerTask.class);

    private final Socket controlConnection;
    private final MessageWriter controlConnectionWriter;
    private final int initRefreshInterval;
    private final Thread peerListener;

    /**
     * Creates a new {@link RefreshMessageHandlerTask}.
     * 
     * @param controlConnection
     *            control connection to the client
     * @param controlConnectionWriter
     *            a {@link MessageWriter} to the control connection
     * @param lifetime
     *            the lifetime of the control connection without refresh
     *            messages. If lifetime is 0,
     *            {@link Constants#DEFAULT_ALLOCATION_REFRESH_INTERVAL} is used.
     *            If lifetime is greater than
     *            {@link Constants#ALLOCATION_LIFETIME_MAX},
     *            {@link Constants#ALLOCATION_LIFETIME_MAX} is used.
     * @param peerListener
     *            listener thread that waits for peer connections to the
     *            relevant client
     */
    public RefreshMessageHandlerTask(Socket controlConnection, MessageWriter controlConnectionWriter, int lifetime,
            SocketListener peerListener) {
        this.controlConnection = controlConnection;
        this.controlConnectionWriter = controlConnectionWriter;
        this.peerListener = peerListener;
        if (lifetime == 0) {
            this.initRefreshInterval = RelayingConstants.DEFAULT_ALLOCATION_REFRESH_INTERVAL;
        } else {
            this.initRefreshInterval = (lifetime < RelayingConstants.ALLOCATION_LIFETIME_MAX) ? lifetime
                    : RelayingConstants.ALLOCATION_LIFETIME_MAX;
        }
    }

    /**
     * Waits for refresh messages from the client. If no refresh message arrives
     * during the specified lifetime, the task gets stopped and the given
     * listener thread for peer connections gets stopped too. Moreover if
     * lifetime of a refresh message is equal or less than 0, the task and the
     * listener thread get stopped.
     */
    private void handle() {
        int refreshInterval = initRefreshInterval;
        try {
            while (true) {
                controlConnection.setSoTimeout(refreshInterval * 1000);
                final MessageReader messageReader = this.createCustomRelayingMessageReader();
                final Message message = messageReader.readSTUNMessage(controlConnection.getInputStream());
                // check if it is refresh message (atm the only one that is
                // allowed)
                if (message == null) {
                    // connection closed
                    logger.error("Received message was null");
                    break;
                }
                if (message.isMethod(STUNMessageMethod.KEEP_ALIVE) && message.isRequest()) {
                    int lifetime = message.getAttribute(RelayingLifetime.class).getLifeTime();
                    logger.info("Received refresh request with lifetime {}", lifetime);
                    if (lifetime > 0) {
                        refreshInterval = (lifetime < RelayingConstants.ALLOCATION_LIFETIME_MAX) ? lifetime
                                : RelayingConstants.ALLOCATION_LIFETIME_MAX;
                        // send refresh response
                        Message successResponse = message.buildSuccessResponse();
                        successResponse.addAttribute(new RelayingLifetime(refreshInterval));
                        controlConnectionWriter.writeMessage(successResponse);
                    } else {
                        // send refresh response (unallocate)
                        Message successResponse = message.buildSuccessResponse();
                        successResponse.addAttribute(new RelayingLifetime(0));
                        controlConnectionWriter.writeMessage(successResponse);
                        break;
                    }
                } else {
                    logger.error("Received unexpected message: {}", message.getMessageMethod());
                    Message errorResponse = message.buildFailureResponse(STUNErrorCode.BAD_REQUEST,
                            "Expected refresh request");
                    controlConnectionWriter.writeMessage(errorResponse);
                }
            }
        } catch (SocketTimeoutException e) {
            logger.info("No refresh request within specified timeout. Closing control conection and "
                    + "destroy allocation");
        } catch (IOException e) {
            logger.error("IOException while reading from control connection. Closing control "
                    + "conection and destroy allocation");
            e.printStackTrace();
        } finally {
            // something went wrong -> destroy allocation
            peerListener.interrupt();
            try {
                controlConnection.close();
            } catch (IOException ignore) {
            }
        }

    }

    public void run() {
        handle();
    }

    private MessageReader createCustomRelayingMessageReader() {
        return MessageReader.createMessageReaderWithCustomAttributeTypeDecoder(new RelayingAttributeTypeDecoder());
    }
}
