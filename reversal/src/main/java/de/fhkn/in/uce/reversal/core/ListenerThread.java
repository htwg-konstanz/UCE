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
package de.fhkn.in.uce.reversal.core;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.stun.attribute.XorMappedAddress;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageReader;

/**
 * Class which contains the listener thread of the connection reversal target.
 * This class is used by ConnectionReversalTarget class as listener thread.
 *
 * Listens for connection request and keep alive messages. Incoming connection
 * request sockets are added to blockingSocketQueue. The content of
 * blockingSocketQueue can be read by calling the accept- Method of
 * {@link ReversalTarget}.
 *
 * @author thomas zink, stefan lohr, Alexander Diener
 *         (aldiener@htwg-konstanz.de)
 */
public final class ListenerThread extends Thread {
    private final BlockingQueue<Socket> blockingSocketQueue;
    private static final Logger logger = LoggerFactory.getLogger(ListenerThread.class);
    private final Socket controlConnection;

    /**
     * Constructor of the ListenerThread which is used by
     * ConnectionReversalTarget for receiving and handling incoming messages for
     * the mediator.
     *
     * @param datagramSocket
     *            Socket for receiving messages from mediator
     * @param blockingSocketQueue
     *            Queue for adding new connection for source
     */
    public ListenerThread(final Socket socketToMediator, final BlockingQueue<Socket> blockingSocketQueue) {
        this.controlConnection = socketToMediator;
        this.blockingSocketQueue = blockingSocketQueue;
    }

    /**
     * Run-Method of the listenerThread. It contains a while-loop for receiving
     * messages.
     */
    @Override
    public void run() {
        try {
            while (!this.isInterrupted()) {
                logger.info("listen on {}:{}", this.controlConnection.getLocalPort(), this.controlConnection //$NON-NLS-1$
                        .getLocalAddress().getHostAddress());
                while (!this.isInterrupted()) {
                    final MessageReader messageReader = MessageReader.createMessageReader();
                    final Message requestMessage = messageReader.readSTUNMessage(this.controlConnection
                            .getInputStream());
                    if (requestMessage.isMethod(STUNMessageMethod.CONNECTION_REQUEST)) {
                        this.handleConnectionRequestMessage(requestMessage);
                    } else if (requestMessage.isMethod(STUNMessageMethod.KEEP_ALIVE)) {
                        this.handleKeepAliveMessage(requestMessage);
                    } else {
                        logger.error("Unknown message"); //$NON-NLS-1$
                    }
                }
            }
        } catch (final Exception e) {
            Thread.currentThread().interrupt();
            logger.error(e.getMessage());
        } finally {
            try {
                this.controlConnection.close();
            } catch (final Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    private void handleKeepAliveMessage(final Message keepAliveMessage) {
        if (keepAliveMessage.isSuccessResponse()) {
            logger.info("KeepAlive response message received"); //$NON-NLS-1$
        } else {
            logger.error("KeepAlive successResponse message expacted"); //$NON-NLS-1$
        }
    }

    /**
     * Private method for handling connection requests.
     */
    private void handleConnectionRequestMessage(final Message connectionRequestMessage) {
        final XorMappedAddress targetAddress = connectionRequestMessage.getAttribute(XorMappedAddress.class);
        logger.info("ConnectionRequest message from {} with id {}", targetAddress.getEndpoint().toString(), //$NON-NLS-1$
                connectionRequestMessage.getTransactionId());
        try {
            final String hostName = targetAddress.getEndpoint().getHostName();
            final int portNumber = targetAddress.getEndpoint().getPort();
            logger.info("create socket connection to {}:{}", hostName, portNumber); //$NON-NLS-1$
            final Socket socket = new Socket();
            socket.setReuseAddress(true);
            socket.connect(new InetSocketAddress(hostName, portNumber));
            this.blockingSocketQueue.add(socket);
        } catch (final Exception e) {
            logger.error(e.getMessage());
        }
    }
}
