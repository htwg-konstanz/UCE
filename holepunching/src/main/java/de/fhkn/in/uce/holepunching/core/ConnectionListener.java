/**
 * Copyright (C) 2011 Daniel Maier
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.fhkn.in.uce.holepunching.core;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.core.concurrent.ThreadGroupThreadFactory;

/**
 * Class to listen for incoming connections. Listener can be stopped and
 * restarted. To get a established connection you have to register for a
 * specific remote endpoint.
 * 
 * @author Daniel Maier
 * 
 */
public final class ConnectionListener {

    private static Logger logger = LoggerFactory.getLogger(ConnectionListener.class);
    private final Map<SocketAddress, BlockingQueue<Socket>> exchangers;
    private final InetAddress bindingAddress;
    private final int bindingPort;
    private final ExecutorService executor;
    private boolean running = false;
    private boolean shutdown = false;
    private ConnectionListenerTask currentTask;

    /**
     * Task that waits for incoming connections until it gets stopped. If there
     * is a registration for the established connection the corresponding socket
     * will be put in the exchanger object.
     * 
     * @author Daniel Maier
     * 
     */
    private class ConnectionListenerTask implements Runnable {

        private final ServerSocket ss;
        private volatile boolean stop = false;

        private ConnectionListenerTask(final ServerSocket ss) {
            this.ss = ss;
        }

        private void stop() {
            try {
                this.stop = true;
                this.ss.close();
            } catch (final IOException ignore) {
                logger.error("IOException while close socket");
            }
        }

        /**
         * Listens for incoming connections until it gets stopped. If there is a
         * registration for the established connection the corresponding socket
         * will be put in the exchanger object.
         */
        @Override
        public void run() {
            try {
                while (!this.stop) {
                    // important to bind in loop, so it gets tried again while
                    // the client starts connecting
                    try {
                        logger.info("Try to bind to: {}:{}", ConnectionListener.this.bindingAddress, ConnectionListener.this.bindingPort);
                        this.ss.bind(new InetSocketAddress(ConnectionListener.this.bindingAddress, ConnectionListener.this.bindingPort));
                    } catch (final IOException ignore) {
                        logger.info("IOException while bind: {}", ignore.getMessage());
                        // only continue on bind exception and not when socket
                        // is closed
                        if (ignore instanceof BindException) {
                            logger.info("Try again ...");
                            continue;
                        }
                    }
                    logger.info("Bound successful");
                    while (!this.stop) {
                        logger.info("Waiting for accept..");
                        final Socket s = this.ss.accept();
                        logger.info("Accepted socket: {}", s);
                        final BlockingQueue<Socket> exchanger = ConnectionListener.this.exchangers.get(s.getRemoteSocketAddress());
                        if (exchanger != null) {
                            exchanger.add(s);
                        } else {
                            logger.error("No one registered for socket: {}", s);
                            try {
                                s.close();
                            } catch (final IOException ignore) {
                            }
                        }
                    }
                }
            } catch (final IOException e) {
                logger.info("IOException in ConnectionListenerTask: {}. Terminating Task.", e.getMessage());
            }

        }

    }

    /**
     * Creates a new ConnectionListener.
     * 
     * @param bindingAddress
     *            local address of the listener
     * @param bindingPort
     *            local port of the listener
     */
    public ConnectionListener(final InetAddress bindingAddress, final int bindingPort) {
        this.bindingAddress = bindingAddress;
        this.bindingPort = bindingPort;
        this.exchangers = new Hashtable<SocketAddress, BlockingQueue<Socket>>();
        this.executor = Executors.newSingleThreadExecutor(new ThreadGroupThreadFactory());
    }

    /**
     * Register for a specific established connection. If the connection gets
     * established the corresponding socket will be put in the given exchanger
     * object.
     * 
     * @param originator
     *            the endpoint of the desired connection
     * @param exchanger
     *            exchanger object in that the connected socket will be put
     */
    public synchronized void registerForOriginator(final SocketAddress originator, final BlockingQueue<Socket> exchanger) {
        logger.info("New registration for: {}", originator);
        this.exchangers.put(originator, exchanger);
    }

    /**
     * Remove registration for a specific originator. Assumes a registration of
     * the desired originator via the
     * {@link de.htwg_konstanz.in.uce.hp.parallel.holepuncher.ConnectionListener# registerForOriginator(SocketAddress, BlockingQueue)
     * registerForOriginator} method before. If no registration is present this
     * method does nothing.
     * 
     * @param originator
     *            the endpoint of the originator its registration should be
     *            removed
     */
    public synchronized void deregisterForOriginator(final SocketAddress originator) {
        logger.info("Deregistration for: {}", originator);
        this.exchangers.remove(originator);
    }

    /**
     * Stops the ConnectionListener. Strictly speaking it closes the underlying
     * server socket.
     */
    public synchronized void stop() {
        logger.info("Stop ConnectionListener...");
        if (this.running) {
            this.currentTask.stop();
            this.running = false;
            logger.info("Stop completed");
        } else {
            logger.info("Stop completed (was not started)");
        }
    }

    /**
     * Starts the ConnectionListener. It can be started as often as you want.
     * 
     * @throws IOException
     *             if the required server socket could not be created.
     * @throws IllegalStateException
     *             if ConnectionListener is shutdown.
     */
    public synchronized void start() throws IOException, IllegalStateException {
        logger.info("Start...");
        if (this.shutdown) {
            throw new IllegalStateException("ConnectionListener is shutdown");
        }
        if (!this.running) {
            final ServerSocket ss = new ServerSocket();
            ss.setReuseAddress(true);
            final ConnectionListenerTask task = new ConnectionListenerTask(ss);
            this.executor.execute(task);
            this.currentTask = task;
            this.running = true;
            logger.info("Started.");
        }
    }

    /**
     * Attempts to stop the needed worker thread.
     * 
     * @throws IllegalStateException
     *             if the ConnectionListener was already shutdown.
     */
    public synchronized void shutdown() throws IllegalStateException {
        logger.info("Shutdown...");
        if (this.shutdown) {
            throw new IllegalStateException("ConnectionListener was already shutdown");
        }
        this.executor.shutdownNow();
        if (this.currentTask != null) {
            this.currentTask.stop();
        }
        this.shutdown = true;
    }
}
