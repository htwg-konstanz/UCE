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
package de.fhkn.in.uce.core.socketlistener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import javax.net.ServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link SocketListener} is a thread that waits for incoming connections over
 * a server socket. If such a connection is established, it starts a task via the
 * executor framework to handle it. The server socket, executor and a factory
 * for handling tasks are configurable.
 *
 * @author thomas zink, daniel maier
 */
public class SocketListener extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(SocketListener.class);
    private final ServerSocket server;
    private final ExecutorService executor;
    private final SocketTaskFactory tasks;

    /**
     * Creates a new {@link SocketListener}.
     *
     * @param server
     *            the server socket on that this {@link ListenerThread} should
     *            wait for new connections
     * @param executor
     *            the executor that is used to execute the handling tasks
     * @param taskFactory
     *            factory that is used to create the handling tasks
     * @throws NullPointerException
     *             if one of the parameters is null
     */
    public SocketListener(
            ServerSocket server,
            final ExecutorService executor,
            final SocketTaskFactory taskFactory) {
        if ((executor == null) || (taskFactory  == null) || (server == null)) {
            throw new NullPointerException();
        }
        this.server = server;
        this.executor = executor;
        this.tasks = taskFactory;
    }


    /**
     * Creates a new {@link SocketListener}. The server socket is created by
     * the passed server socket factory.
     *
     * @param bindingPort
     *            the local port to that the server socket gets bound to
     * @param serverSocketFactory
     *            the server socket factory that is used to create the server
     *            socket
     * @param executor
     *            factory that is used to create the handling tasks
     * @param taskFactory
     *            factory that is used to create the handling tasks
     * @throws IOException
     *             if the server socket cannot be created, or if the bind
     *             operation of the server socket fails
     * @throws NullPointerException
     *             if one of the non-primitive parameters is null
     */
    public SocketListener(
            final int bindingPort,
            final ServerSocketFactory serverSocketFactory,
            final ExecutorService executor,
            final SocketTaskFactory taskFactory)
            throws IOException {
        this(serverSocketFactory.createServerSocket(), executor, taskFactory);
        this.server.bind(new InetSocketAddress(bindingPort));
    }

    /**
     * Runs in a loop until the interrupt status of this thread is set or an
     * {@link IOException} occurs. Waits for a new connection via the server socket
     * and then executes a task to handle it via the given executor. The task is
     * created by the {@link SocketTaskFactory} member and the accepted socket
     * gets handed over to it. Before the thread terminates the given
     * executor is shutdown.
     */
    @Override
    public final void run() {
        try {
            while (!isInterrupted()) {
                Socket s = server.accept();
                logger.info("New connection from: {}", s);
                executor.execute(tasks.getTask(s));
            }
        } catch (IOException e) {
            logger.error("IOException while accepting connection: {}", e.getMessage());
        } finally {
            logger.info("entered finally block. interrupt status is: {}", isInterrupted());
            executor.shutdownNow();
        }
    }

    /**
     * Terminates this thread by closing the given server socket.
     */
    @Override
    public final void interrupt() {
        try {
            server.close();
        } catch (IOException ignore) {
            /* nop */
        } finally {
            super.interrupt();
        }
    }
}
