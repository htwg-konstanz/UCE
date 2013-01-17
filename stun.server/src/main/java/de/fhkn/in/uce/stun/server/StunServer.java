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
package de.fhkn.in.uce.stun.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.core.socketlistener.SocketListener;
import de.fhkn.in.uce.core.socketlistener.SocketTaskFactory;
import de.fhkn.in.uce.stun.server.connectionhandling.HandleMessageTask;
import de.fhkn.in.uce.stun.server.connectionhandling.HandleMessageTaskFactory;

/**
 * Class to configure and start the modified UCE STUN server. With the given
 * primary and secondary public addresses 4 {@link HandleMessageTask}s are
 * started. According to RFC 5780 the server can be used to determine the TCP
 * mapping and filtering behavior of a NAT device. The server is not compliant
 * completely to RFC 5389 and 5780 because it also instantiates TCP connections
 * with the clients for examining the filtering behavior. IN RFC 5389 7.2.2.
 * this behavior is forbidden.
 * 
 * The server uses the given primary and secondary IP addresses. As primary port
 * the STUN port 3478 is used. The secondary port is 3479. With these
 * information 4 endpoints can be creates to which the server listens.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class StunServer {
    private static final int STUN_SERVER_PORT = 3478;
    private static final Logger logger = LoggerFactory.getLogger(StunServer.class);
    private final InetSocketAddress primaryAddress;
    private final InetSocketAddress secondaryAddress;
    private final SocketTaskFactory handleMessageTaskFactory;
    private final ExecutorService handleExecutor;
    private final ExecutorService socketListenerExecutor;

    /**
     * Creates a modified UCE STUN server with the given public reachable
     * addresses.
     * 
     * @param primaryAddress
     *            the public primary (local) address
     * @param secondaryAddress
     *            the public secondary/alternate (local) address
     */
    public StunServer(final InetSocketAddress primaryAddress, final InetSocketAddress secondaryAddress) {
        this.primaryAddress = primaryAddress;
        this.secondaryAddress = secondaryAddress;
        this.handleMessageTaskFactory = new HandleMessageTaskFactory(this.primaryAddress, this.secondaryAddress);
        this.handleExecutor = Executors.newCachedThreadPool();
        this.socketListenerExecutor = Executors.newCachedThreadPool();
    }

    /**
     * Starts the 4 tasks to handle messages. Have a look at RFC 5780 for the
     * combinations of IP and port the server listens to.
     * 
     * @throws Exception
     */
    public void startStunServer() throws Exception {
        for (SocketListener socketListener : this.getListWithSocketListeners()) {
            this.socketListenerExecutor.execute(socketListener);
        }
    }

    private List<SocketListener> getListWithSocketListeners() throws IOException {
        final List<SocketListener> result = new ArrayList<SocketListener>();
        result.add(this.createSocketListener(this.primaryAddress));
        result.add(this.createSocketListener(new InetSocketAddress(this.primaryAddress.getAddress(),
                this.secondaryAddress.getPort())));
        result.add(this.createSocketListener(new InetSocketAddress(this.secondaryAddress.getAddress(),
                this.primaryAddress.getPort())));
        result.add(this.createSocketListener(this.secondaryAddress));
        return result;
    }

    private SocketListener createSocketListener(final InetSocketAddress listenerAddress) throws IOException {
        final ServerSocket serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(listenerAddress);
        return new SocketListener(serverSocket, this.handleExecutor, this.handleMessageTaskFactory);
    }

    /**
     * Main method to create and start a {@link StunServer} with the given IP
     * addresses. Primary port: 3478, secondary port: 3479
     * 
     * @param args
     *            args[0]: primary IP address, args[1]: secondary/alternate IP
     *            address
     */
    public static void main(final String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Arguments: primaryIP secondaryIP"); //$NON-NLS-1$
        }
        final String primaryIp = args[0];
        final String secondaryIp = args[1];
        final int primaryPort = STUN_SERVER_PORT;
        final int secondaryPort = STUN_SERVER_PORT + 1;
        final InetSocketAddress primaryAddress = new InetSocketAddress(primaryIp, primaryPort);
        final InetSocketAddress secondaryAddress = new InetSocketAddress(secondaryIp, secondaryPort);
        final StunServer stunServer = new StunServer(primaryAddress, secondaryAddress);
        try {
            stunServer.startStunServer();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Exception occured while running server", e); //$NON-NLS-1$
        }
    }
}
