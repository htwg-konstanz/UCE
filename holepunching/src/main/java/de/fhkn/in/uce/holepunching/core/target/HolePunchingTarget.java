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
package de.fhkn.in.uce.holepunching.core.target;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.core.concurrent.ThreadGroupThreadFactory;
import de.fhkn.in.uce.holepunching.core.HolePuncher;
import de.fhkn.in.uce.stun.attribute.Token;
import de.fhkn.in.uce.stun.attribute.Username;
import de.fhkn.in.uce.stun.attribute.XorMappedAddress;
import de.fhkn.in.uce.stun.header.STUNMessageClass;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageReader;
import de.fhkn.in.uce.stun.message.MessageStaticFactory;
import de.fhkn.in.uce.stun.message.MessageWriter;

/**
 * Implementation of hole punching target for parallel hole punching. It
 * consists of a task that is listening for forward endpoints messages and
 * keep-live messages from the mediator. If a forward endpoints message arrives
 * it starts tries to establish a connection to the hole punching source with
 * the help of {@link HolePuncher}.
 *
 * @author Daniel Maier, Alexander Diener (aldiener@htwg-konstanz.de)
 *
 */
public final class HolePunchingTarget {
    private static final Logger logger = LoggerFactory.getLogger(HolePunchingTarget.class);
    // private final SocketAddress mediatorSocketAddress;
    private final String targetId;
    private final BlockingQueue<Socket> socketQueue;
    private final ThreadGroupThreadFactory threadFactory;
    private Socket socketToMediator;
    private boolean started;
    private MessageHandlerTask messageHandlerTask;

    /**
     * Creates a new HolePunchingTarget.
     *
     * @param mediatorSocketAddress
     *            endpoint of the mediator on that it listens for registration
     *            messages.
     * @param registrationId
     *            the ID under that the target should get registered.
     */
    public HolePunchingTarget(final Socket controlConnection, final String targetId) {
        // this.mediatorSocketAddress = mediatorSocketAddress;
        this.socketToMediator = controlConnection;
        this.targetId = targetId;
        this.socketQueue = new LinkedBlockingQueue<Socket>();
        this.threadFactory = new ThreadGroupThreadFactory();
        this.started = false;
    }

    /**
     * Starts the HolePunchingTarget. Strictly speaking it starts the
     * {@link MessageHandlerTask} after it has registered the target with the
     * mediator. It can be started only one time.
     *
     * @throws IOException
     *             if an I/O error occurs while registering with the mediator.
     * @throws IllegalStateException
     *             if the target was already started.
     */
    public synchronized void start(final Message connectionRequestMessage) throws IOException, IllegalStateException {
        if (this.started) {
            throw new IllegalStateException("Target is already started"); //$NON-NLS-1$
        }
        this.started = true;
        final List<XorMappedAddress> endpoints = connectionRequestMessage.getAttributes(XorMappedAddress.class);
        final Token authentificationToken = connectionRequestMessage.getAttribute(Token.class);
        this.startMessageHandler(endpoints, authentificationToken);
    }

    @SuppressWarnings("unused")
    private void sendRegisterMessage() throws IOException {
        final Message registerMessage = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                STUNMessageMethod.REGISTER);
        // target id
        registerMessage.addAttribute(new Username(this.targetId));
        // private endpoint
        final InetAddress privateAddress = this.socketToMediator.getLocalAddress();
        if (privateAddress instanceof Inet6Address) {
            registerMessage.addAttribute(new XorMappedAddress(new InetSocketAddress(this.socketToMediator
                    .getLocalAddress(), this.socketToMediator.getLocalPort()), ByteBuffer.wrap(
                    registerMessage.getHeader().getTransactionId()).getInt()));
        } else {
            registerMessage.addAttribute(new XorMappedAddress(new InetSocketAddress(this.socketToMediator
                    .getLocalAddress(), this.socketToMediator.getLocalPort())));
        }
        final MessageWriter messageWriter = new MessageWriter(this.socketToMediator.getOutputStream());
        logger.info("Sending RegisterMessage for {}", this.targetId); //$NON-NLS-1$
        messageWriter.writeMessage(registerMessage);
    }

    @SuppressWarnings("unused")
    private Message receiveMessage() throws IOException {
        final MessageReader messageReader = MessageReader.createMessageReader();
        return messageReader.readSTUNMessage(this.socketToMediator.getInputStream());
    }

    private void startMessageHandler(final List<XorMappedAddress> endpoints, final Token authentificationToken) {
        logger.debug("Starting message handler task"); //$NON-NLS-1$
        this.messageHandlerTask = new MessageHandlerTask(this.socketToMediator, this.socketQueue, 0, endpoints,
                authentificationToken);
        this.threadFactory.newThread(this.messageHandlerTask).start();
    }

    /**
     * Stops the HolePunchingTarget. Strictly speaking it sends an
     * {@link UnregisterMessage} over a new socket connection to the mediator
     * and then closes the both connections to it. Then it stops the
     * {@link MessageHandlerTask}.
     *
     * @throws IOException
     *             if an I/O error occurs while sending
     *             {@link UnregisterMessage} to mediator or when closing the
     *             socket to it.
     * @throws IllegalStateException
     *             if the target was not started yet.
     */
    public synchronized void stop() throws IOException, IllegalStateException {
        if (!this.started) {
            throw new IllegalStateException("Target is not started"); //$NON-NLS-1$
        }
        this.socketToMediator.close();
        this.messageHandlerTask.cancel();
    }

    /**
     * Returns a socket thats connection is established via hole-punching to
     * this target. The method blocks until a connection is made.
     *
     * @return the new socket.
     * @throws IOException
     *             if an I/O error occurs when waiting for a connection.
     * @throws InterruptedException
     *             if the current thread gets interrupted while blocked in
     *             accept().
     */
    // TODO irgendwie io exceptions aus ForwardEndpointsHandlerThread
    // weitergeben
    public Socket accept() throws IOException, InterruptedException {
        try {
            final Socket s = this.socketQueue.take();
            logger.info("Accepting socket: {}", s); //$NON-NLS-1$
            // received dummy socket for indicating time limit exceeded
            if (!s.isConnected()) {
                throw new IOException("IOException while accepting socket"); //$NON-NLS-1$
            }
            return s;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }

    }
}
