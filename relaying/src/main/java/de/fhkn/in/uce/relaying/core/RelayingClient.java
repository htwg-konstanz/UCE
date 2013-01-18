/*
 * Copyright (c) 2012 Thomas Zink,
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
package de.fhkn.in.uce.relaying.core;

import static de.fhkn.in.uce.relaying.message.RelayingConstants.ALLOCATION_LIFETIME;
import static de.fhkn.in.uce.relaying.message.RelayingConstants.ALLOCATION_LIFETIME_ADVANCE;
import static de.fhkn.in.uce.relaying.message.RelayingConstants.ALLOCATION_LIFETIME_MIN;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.core.concurrent.ThreadGroupThreadFactory;
import de.fhkn.in.uce.relaying.message.RelayingAttributeTypeDecoder;
import de.fhkn.in.uce.relaying.message.RelayingLifetime;
import de.fhkn.in.uce.relaying.message.RelayingMethod;
import de.fhkn.in.uce.relaying.message.RelayingMethodDecoder;
import de.fhkn.in.uce.stun.attribute.AttributeTypeDecoder;
import de.fhkn.in.uce.stun.attribute.XorMappedAddress;
import de.fhkn.in.uce.stun.header.MessageMethodDecoder;
import de.fhkn.in.uce.stun.header.STUNMessageClass;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageReader;
import de.fhkn.in.uce.stun.message.MessageStaticFactory;
import de.fhkn.in.uce.stun.message.MessageWriter;

/**
 * A {@link RelayingClient} requests, that another host (the Relay Server) acts
 * as a relay. Information from the TURN RFC: The client can arrange for the
 * server to relay packets to and from certain other hosts (called peers) and
 * can control aspects of how the relaying is done. The client does this by
 * obtaining an IP address and port on the server, called the relayed transport
 * address. When a peer sends a packet to the relayed transport address, the
 * server relays the packet to the client. When the client sends a data packet
 * to the server, the server relays it to the appropriate peer using the relayed
 * transport address as the source.
 * 
 * A peer that wants to connect to the client must somehow have obtained the
 * transport address of the client.
 * 
 * @author thomas zink, daniel maier, Alexander Diener
 *         (aldiener@htwg-konstanz.de)
 */
public final class RelayingClient {

    private static final Logger logger = LoggerFactory.getLogger(RelayingClient.class);
    private final InetSocketAddress relayServerSocketAddress;
    private final InetAddress localAddress;
    private final int localPort;
    private final BlockingQueue<Socket> socketQueue;
    private MessageHandlerTask messageHandlerTask;
    private Socket controlConnection;
    private MessageWriter controlConnectionWriter;
    private volatile boolean successfullAllocation = false;
    private boolean discardedAllocation = false;

    /**
     * Creates a new {@link RelayingClient}.
     * 
     * @param relayServerSocketAddress
     *            the endpoint of the relay server
     */
    public RelayingClient(final InetSocketAddress relayServerSocketAddress) {
        this(relayServerSocketAddress, null, 0);
    }

    /**
     * Creates a new {@link RelayingClient}.
     * 
     * @param relayServerSocketAddress
     *            the endpoint of the relay server
     * @param port
     *            the local port of the control connection to the relay server
     */
    public RelayingClient(final InetSocketAddress relayServerSocketAddress, final int port) {
        this(relayServerSocketAddress, null, port);
    }

    /**
     * Creates a new {@link RelayingClient}.
     * 
     * @param relayServerSocketAddress
     *            the endpoint of the relay server
     * @param localAddress
     *            the local address of the control connection to the relay
     *            server
     * @param localPort
     *            the local port of the control connection to the relay server
     */
    public RelayingClient(final InetSocketAddress relayServerSocketAddress, final InetAddress localAddress,
            final int localPort) {
        this.relayServerSocketAddress = relayServerSocketAddress;
        this.localAddress = localAddress;
        this.localPort = localPort;
        this.socketQueue = new LinkedBlockingQueue<Socket>();
    }

    /**
     * Creates a new allocation on the relay server for this relay client. Also
     * initiates the periodic refresh requests for the allocation. You can only
     * create one allocation with a single {@link RelayingClient} instance.
     * 
     * @return the public endpoint of the allocation on the relay server
     * @throws IOException
     *             if an I/O error occurs
     * @throws IllegalStateException
     *             if you try to create an allocation after you already created
     *             an allocation with this {@link RelayingClient} instance
     *             successfully
     */
    public synchronized InetSocketAddress createAllocation() throws IOException {
        if (this.successfullAllocation || this.discardedAllocation) {
            throw new IllegalStateException("You can create only one allocation with the same Relay Client object"); //$NON-NLS-1$
        }
        this.connectToRelayServerAndInitializeWriter();
        this.sendAllocationRequest();
        final Message response = this.receiveAllocationResponse();
        final InetSocketAddress addressAtRelayServer = this.getAddressAtRelayFromMessage(response);
        final int lifetime = response.getAttribute(RelayingLifetime.class).getLifeTime();
        this.startMessageHandler(lifetime);
        return addressAtRelayServer;
    }

    private synchronized void connectToRelayServerAndInitializeWriter() throws IOException {
        this.controlConnection = new Socket();
        this.controlConnection.bind(new InetSocketAddress(this.localAddress, this.localPort));
        this.controlConnection.connect(this.relayServerSocketAddress);
        this.controlConnectionWriter = new MessageWriter(this.controlConnection.getOutputStream());
    }

    private synchronized void sendAllocationRequest() throws IOException {
        final Message allocationRequest = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                RelayingMethod.ALLOCATION);
        allocationRequest.addAttribute(new RelayingLifetime(ALLOCATION_LIFETIME));
        logger.debug("Sending allocation request to relay server"); //$NON-NLS-1$
        this.controlConnectionWriter.writeMessage(allocationRequest);
    }

    private synchronized Message receiveAllocationResponse() throws IOException {
        final MessageReader messageReader = this.createCustomRelayingMessageReader();
        final Message allocationResponse = messageReader.readSTUNMessage(this.controlConnection.getInputStream());
        if (!allocationResponse.isMethod(RelayingMethod.ALLOCATION) || !allocationResponse.isSuccessResponse()
                || !allocationResponse.hasAttribute(XorMappedAddress.class)
                || !allocationResponse.hasAttribute(RelayingLifetime.class)) {
            throw new IOException("Unexpected response from Relay server"); //$NON-NLS-1$
        }
        return allocationResponse;
    }

    private MessageReader createCustomRelayingMessageReader() {
        logger.debug("Creating custom relaying message reader"); //$NON-NLS-1$
        final List<MessageMethodDecoder> customMethodDecoders = new ArrayList<MessageMethodDecoder>();
        customMethodDecoders.add(new RelayingMethodDecoder());
        final List<AttributeTypeDecoder> customAttributeTypeDecoders = new ArrayList<AttributeTypeDecoder>();
        customAttributeTypeDecoders.add(new RelayingAttributeTypeDecoder());
        return MessageReader.createMessageReaderWithCustomDecoderLists(customMethodDecoders,
                customAttributeTypeDecoders);
    }

    private InetSocketAddress getAddressAtRelayFromMessage(final Message msg) {
        final InetSocketAddress addressAtRelayServer = msg.getAttribute(XorMappedAddress.class).getEndpoint();
        this.successfullAllocation = true;
        return addressAtRelayServer;
    }

    private synchronized void startMessageHandler(final int lifetime) {
        final ThreadFactory specialThreadsFactory = new ThreadGroupThreadFactory();
        final ScheduledExecutorService refreshExecutor = Executors
                .newSingleThreadScheduledExecutor(specialThreadsFactory);
        refreshExecutor.schedule(new RefreshAllocationTask(this.controlConnectionWriter, lifetime),
                Math.max(lifetime - ALLOCATION_LIFETIME_ADVANCE, ALLOCATION_LIFETIME_MIN), TimeUnit.SECONDS);
        this.messageHandlerTask = new MessageHandlerTask(this.controlConnection, this.controlConnectionWriter,
                this.relayServerSocketAddress, this.socketQueue, refreshExecutor);
        specialThreadsFactory.newThread(this.messageHandlerTask).start();
    }

    /**
     * Discards the allocation by this client on the relay server. Also
     * terminates the periodic refresh requests for the allocation.
     * 
     * @throws IOException
     *             if an I/O error occurs
     * @throws IllegalStateException
     *             if the allocation of this client is already discarded, or if
     *             no allocation is created before
     */
    public synchronized void discardAllocation() throws IOException {
        if (this.discardedAllocation) {
            throw new IllegalStateException("Allocation is already discarded"); //$NON-NLS-1$
        } else if (this.successfullAllocation) {
            logger.debug("Discarding allocation"); //$NON-NLS-1$
            this.sendDiscardMessage();
            this.messageHandlerTask.cancel();
            this.successfullAllocation = false;
            this.discardedAllocation = true;
        } else {
            throw new IllegalStateException("You have first to create an allocation."); //$NON-NLS-1$
        }
    }

    private synchronized void sendDiscardMessage() throws IOException {
        final Message refreshRequestMessage = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                STUNMessageMethod.KEEP_ALIVE);
        refreshRequestMessage.addAttribute(new RelayingLifetime(0));
        this.controlConnectionWriter.writeMessage(refreshRequestMessage);
    }

    /**
     * Returns a socket to the relay server to relay data between this client
     * and a peer. This method blocks until a new socket is available or the
     * thread gets interrupted while waiting.
     * 
     * @return a socket to the relay server
     * @throws IOException
     *             if an I/O error occurs
     * @throws InterruptedException
     *             if interrupted while waiting
     * @throws IllegalStateException
     *             if no allocation is created before or the allocation is
     *             discarded
     */
    public Socket accept() throws IOException, InterruptedException {
        Socket result = null;
        if (this.successfullAllocation) {
            result = this.socketQueue.take();
            if (!result.isConnected()) {
                throw new IOException("IOException while accepting socket"); //$NON-NLS-1$
            }
        } else {
            throw new IllegalStateException("You have first to create an allocation."); //$NON-NLS-1$
        }
        return result;
    }
}
