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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.concurrent.ThreadGroupFactory;
import de.fhkn.in.uce.messages.MessageWriter;
import de.fhkn.in.uce.messages.SemanticLevel;
import de.fhkn.in.uce.messages.SocketEndpoint;
import de.fhkn.in.uce.messages.UceMessage;
import de.fhkn.in.uce.messages.UceMessageStaticFactory;
import de.fhkn.in.uce.relay.core.RelayLifetime;
import de.fhkn.in.uce.relay.core.RelayMessageReader;
import de.fhkn.in.uce.relay.core.RelayUceMethod;

import static de.fhkn.in.uce.relay.core.RelayConstants.*;

/**
 * A {@link RelayClient} requests, that another host (the Relay Server) acts
 * as a relay. Information from the TURN RFC:
 * The client can arrange for the server to relay packets to and from certain
 * other hosts (called peers) and can control aspects of how the relaying is
 * done. The client does this by obtaining an IP address and port on the
 * server, called the relayed transport address.  When a peer sends a packet
 * to the relayed transport address, the server relays the packet to the client.
 * When the client sends a data packet to the server, the server relays it to
 * the appropriate peer using the relayed transport address as the source.
 * 
 * A peer that wants to connect to the client must somehow have obtained the
 * transport address of the client.
 * 
 * @author thomas zink, daniel maier 
 */
public final class RelayClient {

    private static final Logger logger = LoggerFactory.getLogger(RelayClient.class);
    private final InetSocketAddress relayServerSocketAddress;
    private final InetAddress localAddress;
    private final int localPort;
    private final BlockingQueue<Socket> socketQueue;
    private MessageHandlerTask messageHandlerTask;
    private Socket controlConnection;
    private MessageWriter controlConnectionWriter;
    private volatile boolean successfulAllocation = false;
    private boolean discardedAllocation = false;

    /**
     * Creates a new {@link RelayClient}.
     * 
     * @param relayServerSocketAddress
     *            the endpoint of the relay server
     */
    public RelayClient(InetSocketAddress relayServerSocketAddress) {
        this(relayServerSocketAddress, null, 0);
    }

    /**
     * Creates a new {@link RelayClient}.
     * 
     * @param relayServerSocketAddress
     *            the endpoint of the relay server
     * @param port
     *            the local port of the control connection to the relay server
     */
    public RelayClient(InetSocketAddress relayServerSocketAddress, int port) {
        this(relayServerSocketAddress, null, port);
    }

    /**
     * Creates a new {@link RelayClient}.
     * 
     * @param relayServerSocketAddress
     *            the endpoint of the relay server
     * @param localAddress
     *            the local address of the control connection to the relay
     *            server
     * @param localPort
     *            the local port of the control connection to the relay server
     */
    public RelayClient(InetSocketAddress relayServerSocketAddress, InetAddress localAddress,
            int localPort) {
        this.relayServerSocketAddress = relayServerSocketAddress;
        this.localAddress = localAddress;
        this.localPort = localPort;
        this.socketQueue = new LinkedBlockingQueue<Socket>();
    }

    /**
     * Creates a new allocation on the relay server for this relay client. Also
     * initiates the periodic refresh requests for the allocation. You can only
     * create one allocation with a single {@link RelayClient} instance.
     * 
     * @return the public endpoint of the allocation on the relay server
     * @throws IOException
     *             if an I/O error occurs
     * @throws IllegalStateException
     *             if you try to create an allocation after you already created
     *             an allocation with this {@link RelayClient} instance
     *             successfully
     */
    public synchronized InetSocketAddress createAllocation() throws IOException {
        if (successfulAllocation || discardedAllocation) {
            throw new IllegalStateException(
                    "You can create only one allocation with the same Relay Client object");
        }
        controlConnection = new Socket();
        controlConnection.bind(new InetSocketAddress(localAddress, localPort));
        controlConnection.connect(relayServerSocketAddress);
        controlConnectionWriter = new MessageWriter(controlConnection.getOutputStream());
        UceMessage allocationRequestMessage = UceMessageStaticFactory.newUceMessageInstance(
                RelayUceMethod.ALLOCATION, SemanticLevel.REQUEST, UUID.randomUUID());
        allocationRequestMessage.addAttribute(new RelayLifetime(ALLOCATION_LIFETIME));
        controlConnectionWriter.writeMessage(allocationRequestMessage);
        UceMessage response = RelayMessageReader.read(controlConnection.getInputStream());
        if (!response.isMethod(RelayUceMethod.ALLOCATION) || !response.isSuccessResponse()
                || !response.hasAttribute(SocketEndpoint.class)
                || !response.hasAttribute(RelayLifetime.class)) {
            throw new IOException("unexpected response from Relay server");
        }
        // special thread group for RMI
        ThreadFactory specialThreadsFactory = new ThreadGroupFactory();
        int lifetime = response.getAttribute(RelayLifetime.class).getLifeTime();
        ScheduledExecutorService refreshExecutor = Executors
                .newSingleThreadScheduledExecutor(specialThreadsFactory);
        refreshExecutor.schedule(new RefreshAllocationTask(controlConnectionWriter, lifetime), Math
                .max(lifetime - ALLOCATION_LIFETIME_ADVANCE,
                        ALLOCATION_LIFETIME_MIN), TimeUnit.SECONDS);
        messageHandlerTask = new MessageHandlerTask(controlConnection, controlConnectionWriter,
                relayServerSocketAddress, socketQueue, refreshExecutor);
        specialThreadsFactory.newThread(messageHandlerTask).start();
        InetSocketAddress peerRelayAddress = response.getAttribute(SocketEndpoint.class)
                .getEndpoint();
        successfulAllocation = true;
        return peerRelayAddress;
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
        if (discardedAllocation) {
            throw new IllegalStateException("Allocation is already discarded.");
        }
        if (successfulAllocation) {
            logger.debug("Discard allocation");
            successfulAllocation = false;
            discardedAllocation = true;
            UceMessage refreshRequestMessage = UceMessageStaticFactory.newUceMessageInstance(
                    RelayUceMethod.REFRESH, SemanticLevel.REQUEST, UUID.randomUUID());
            refreshRequestMessage.addAttribute(new RelayLifetime(0));
            controlConnectionWriter.writeMessage(refreshRequestMessage);
            messageHandlerTask.cancel();
        } else {
            throw new IllegalStateException("You have first to create an allocation.");
        }
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
        if (successfulAllocation) {
            Socket s = socketQueue.take();
            if (s.isConnected()) {
                return s;
            } else {
                throw new IOException("IOException while accepting socket");
            }
        } else {
            throw new IllegalStateException("You have first to create an allocation.");
        }
    }
}
