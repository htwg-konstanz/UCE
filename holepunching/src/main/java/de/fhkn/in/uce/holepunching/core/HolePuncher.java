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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.core.concurrent.ThreadGroupThreadFactory;

/**
 * Class to establish a hole punching connection to a given endpoint. Therefore
 * it uses up to four threads to connect to the given endpoints and to listen
 * for incoming connections.
 *
 * @author Daniel Maier
 *
 */
public final class HolePuncher {
    private static final Logger logger = LoggerFactory.getLogger(HolePuncher.class);
    private static final Socket POISON_PILL = new Socket();
    private static final long CONNECT_INTERVAL = 100;
    private static final int SINGLE_CONNECTION_ATTEMPT_LIMIT = 5000;
    private static final int AUTHENTICATION_READ_TIMEOUT = 2000;
    private static final int TOTAL_TIME_LIMIT = 30000;
    private final ConnectionListener connectionListener;
    private final SocketAddress localBinding;
    protected final BlockingQueue<Socket> socketQueue;
    private final ExecutorService executor;

    /**
     * Creates a new HolePuncher.
     *
     * @param connectionListener
     *            ConnectionListener object to get incoming connections
     * @param localBinding
     *            local endpoint of all outgoing connections
     * @param socketQueue
     *            queue to put the established connection
     */
    public HolePuncher(final ConnectionListener connectionListener, final SocketAddress localBinding,
            final BlockingQueue<Socket> socketQueue) {
        this.connectionListener = connectionListener;
        this.localBinding = localBinding;
        this.socketQueue = socketQueue;
        this.executor = Executors.newCachedThreadPool(new ThreadGroupThreadFactory());
    }

    /**
     * Shuts down the underlying executor. This object can't be used any more
     * after invoking this method.
     */
    public void shutdownNow() {
        this.executor.shutdownNow();
    }

    /**
     * Tries to establish a new hole punching connection. Strictly speaking it
     * starts the connector and listener tasks. It starts a {@link ListenerTask}
     * and a {@link ConnectorTask} for each endpoint. If private and public
     * endpoints are the same only tasks for one endpoint get started. If the
     * connection establishment was successful the resulting socket is put into
     * the {@link HolePuncher#socketQueue socketQueue} given in the
     * {@link HolePuncher#HolePuncher(ConnectionListener, SocketAddress, BlockingQueue)
     * ) constructor}. If the no connection could be established an unconnected
     * socket is put into the {@link HolePuncher#socketQueue socketQueue}.
     *
     * @param privateIP
     *            IP of the private endpoint of the destination
     * @param privatePort
     *            port of the private endpoint of the destination
     * @param publicIP
     *            IP of the public endpoint of the destination
     * @param publicPort
     *            port of the public endpoint of the destination
     * @param authenticator
     *            the {@link ConnectionAuthenticator} object that gets used to
     *            authenticate an established connection
     */
    public final void establishHolePunchingConnection(final InetAddress privateIP, final int privatePort, final InetAddress publicIP,
            final int publicPort, ConnectionAuthenticator authenticator) {
        final SocketAddress privateSocketAddress = new InetSocketAddress(privateIP, privatePort);
        final InetSocketAddress publicSocketAddress = new InetSocketAddress(publicIP, publicPort);
        logger.info("Private Endpoint is: {}", privateSocketAddress);
        logger.info("Public Endpoint is: {}", publicSocketAddress);
        // use authentication with timeout
        authenticator = new TimeLimitConnectionAuthenticator(authenticator, AUTHENTICATION_READ_TIMEOUT);
        final Object sharedLock = new Object();
        final Set<CancelableTask> tasks = new HashSet<CancelableTask>();
        final ListenerTask listenerPrivate = new ListenerTask(privateSocketAddress, authenticator, tasks, sharedLock);
        final ConnectorTask connectorPrivate = new ConnectorTask(privateSocketAddress, authenticator, tasks, sharedLock);
        tasks.add(listenerPrivate);
        tasks.add(connectorPrivate);
        if (!privateSocketAddress.equals(publicSocketAddress)) {
            logger.info("Public and private Endpoints are different.");
            final ListenerTask listenerPublic = new ListenerTask(publicSocketAddress, authenticator, tasks, sharedLock);
            final ConnectorTask connectorPublic = new ConnectorTask(publicSocketAddress, authenticator, tasks, sharedLock);
            tasks.add(listenerPublic);
            tasks.add(connectorPublic);
            logger.info("Start ListenerThread for public endpoint");
            this.executor.execute(listenerPublic);
            logger.info("Start ConnectorThread for public endpoint");
            this.executor.execute(connectorPublic);
        }
        logger.info("Start ListenerThread for private endpoint");
        this.executor.execute(listenerPrivate);
        logger.info("Start ConnectorThread for private endpoint");
        this.executor.execute(connectorPrivate);
    }

    /**
     * Class that listens for incoming connections from the destination with the
     * help of the ConnectionListener.
     *
     * @author Daniel Maier
     *
     */
    private final class ListenerTask implements CancelableTask {

        private final SocketAddress origin;
        private final BlockingQueue<Socket> exchanger;
        private final ConnectionAuthenticator authenticator;
        private final Set<CancelableTask> tasks;
        private final Object sharedLock;
        private volatile boolean canceled = false;

        /**
         * Creates a new {@link ListenerTask} and registers for the given
         * originator on the {@link ConnectionListener} object.
         *
         * @param origin
         *            the remote endpoint of the desired connection.
         * @param sharedLock
         *            the lock object all involved tasks share
         * @param tasks
         *            all tasks that are involved in the hole punching process
         *            regarding this connection
         * @param authenticator
         *            the {@link ConnectionAuthenticator} object that gets used
         *            to authenticate an established connection
         */
        public ListenerTask(final SocketAddress origin, final ConnectionAuthenticator authenticator, final Set<CancelableTask> tasks,
                final Object sharedLock) {
            this.origin = origin;
            this.authenticator = authenticator;
            this.tasks = tasks;
            this.sharedLock = sharedLock;
            this.exchanger = new ArrayBlockingQueue<Socket>(1);
            logger.info("Register on ConnectionListener for target: {}", origin);
            HolePuncher.this.connectionListener.registerForOriginator(origin, this.exchanger);
        }

        /**
         * Waits for connection returned by the {@link ConnectionListener}
         * object an then tries to authenticate the connection with the given
         * {@link ConnectionAuthenticator} object.
         */
        @Override
        public void run() {
            while (!this.canceled) {
                Socket s = null;
                try {
                    s = this.exchanger.take();
                    if (s == POISON_PILL) {
                        logger.info("Got poison pill, terminating task.");
                        return;
                    }
                    logger.info("Got socket from ConnectionListener: {}", s);
                    if (this.authenticator.authenticate(s, this.tasks, this, this.sharedLock)) {
                        HolePuncher.this.socketQueue.add(s);
                        return;
                    }
                    s.close();
                } catch (final InterruptedException e) {
                    // tzn: dead code
                    /*try {
                        if (s != null) {
                            s.close();
                        }
                    } catch (final IOException ignore) {
                    }*/
                    Thread.currentThread().interrupt();
                    logger.debug("InterruptedException. Interrupt Status is restored");
                    // respect interruption and cancel task
                    return;
                } catch (final IOException e) {
                    logger.info("IOException in ListenerThread: " + e.getMessage());
                    try {
                        s.close();
                    } catch (final IOException ignore) {
                    }
                } finally {
                    HolePuncher.this.connectionListener.deregisterForOriginator(this.origin);
                    logger.debug("Exit run method");
                }
            }
        }

        @Override
        public void cancel() {
            logger.debug("Cancel ListenerThread");
            this.canceled = true;
            final boolean offer = this.exchanger.offer(POISON_PILL);
            if (!offer) {
                logger.debug("Offer was: {}", offer);
            }
        }

        @Override
        public String toString() {
            return "ListenerThread [target=" + this.origin + "]";
        }
    }

    /**
     * Class to establish a connection to the desired destination.
     *
     * @author Daniel Maier
     *
     */
    private final class ConnectorTask extends SocketUsingTask {

        private final SocketAddress destination;
        private final ConnectionAuthenticator authenticator;
        private final Set<CancelableTask> tasks;
        private final Object sharedLock;

        /**
         * Creates a new {@link ConnectorTask}.
         *
         * @param destination
         *            the destination to establish the connection.
         * @param sharedLock
         *            the lock object all involved tasks share
         * @param tasks
         *            all tasks that are involved in the hole punching process
         *            regarding this connection
         * @param authenticator
         *            the {@link ConnectionAuthenticator} object that gets used
         *            to authenticate an established connection
         */
        public ConnectorTask(final SocketAddress destination, final ConnectionAuthenticator authenticator,
                final Set<CancelableTask> tasks, final Object sharedLock) {
            this.destination = destination;
            this.authenticator = authenticator;
            this.tasks = tasks;
            this.sharedLock = sharedLock;
        }

        /**
         * Tries to establish a connection and then authenticates it with the
         * help of the given {@link ConnectionAuthenticator} object. Before
         * binding the socket it shutdowns the {@link ConnectionListener} and
         * restarts it afterwards. Retries the connection establishment until
         * the task canceled or the time limit expires.
         */
        @Override
        public void run() {
            final long begin = System.currentTimeMillis();
            while (!this.isCanceled()) {
                final long now = System.currentTimeMillis();
                if ((now - begin) >= TOTAL_TIME_LIMIT) {
                    this.timeLimitExceeded();
                    return;
                }
                Socket s;
                synchronized (this) {
                    s = new Socket();
                    logger.info("Socket {}", s.toString());
                    this.setSocket(s);
                }
                try {
                    HolePuncher.this.connectionListener.stop();
                    s.setReuseAddress(true);
                    s.bind(HolePuncher.this.localBinding);
                    try {
                        HolePuncher.this.connectionListener.start();
                    } catch (final IllegalStateException e) {
                        // This could happen if thread is still running while
                        // ConnectionListener gets stopped by another thread.
                        // Consequence: Stop thread.
                        logger.info("ConnectionListener was shutdown from outside. Stop " + "ConnectorThread");
                        return;
                    }
                    logger.info("Connecting to: {}", this.destination);
                    s.connect(this.destination, SINGLE_CONNECTION_ATTEMPT_LIMIT);
                    logger.info("Connection to {} established", this.destination);
                    if (this.authenticator.authenticate(s, this.tasks, this, this.sharedLock)) {
                        HolePuncher.this.socketQueue.add(s);
                        return;
                    }
                    s.close();
                } catch (final IOException e) {
                    logger.info("IOException in ConnectorThread: {}", e.getMessage());
                    try {
                        s.close();
                    } catch (final IOException ignore) {
                    }
                    try {
                        Thread.sleep(CONNECT_INTERVAL);
                    } catch (final InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        /**
         * Stops all tasks that are involved in the hole punching process
         * regarding this connection.
         */
        private void timeLimitExceeded() {
            synchronized (this.sharedLock) {
                logger.info("Start stopping tasks...");
                for (final CancelableTask t : this.tasks) {
                    logger.debug("Stop task: {}", t);
                    t.cancel();
                }
                HolePuncher.this.socketQueue.add(new Socket());
            }
        }

        @Override
        public synchronized void cancel() {
            logger.info("Cancel ConnectorThread");
            super.cancel();
        }

        @Override
        public String toString() {
            return "ConnectorThread [target=" + this.destination + "]";
        }
    }
}
