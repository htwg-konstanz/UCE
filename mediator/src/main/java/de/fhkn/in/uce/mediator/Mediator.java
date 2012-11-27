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
package de.fhkn.in.uce.mediator;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.core.socketlistener.SocketListener;
import de.fhkn.in.uce.core.socketlistener.SocketTaskFactory;
import de.fhkn.in.uce.mediator.connectionhandling.HandleMessageTaskFactory;
import de.fhkn.in.uce.mediator.peerregistry.UserCleanerTask;

/**
 * The mediator is a public accessibly instance that mediate between a client
 * (source of the connection) and a server (target of a connection). Targets can
 * register at the mediator and sources can request connection information
 * (endpoints) of a target.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class Mediator {
    private static final Logger logger = LoggerFactory.getLogger(Mediator.class);
    private final int listenerPort;
    private final int iterationTimeInSeconds;
    private final int maxLifetimeInSeconds;
    private final ExecutorService handlerExecutor;
    private final ExecutorService cleanerExecutor;
    private final ExecutorService socketListenerExecutor;
    private final SocketTaskFactory handleMessageTaskFactory;

    /**
     * Creates a mediator that handles messages.
     * 
     * @param listenerPort
     *            the port the mediator listens to
     * @param iterationTimeInSeconds
     *            the time interval in seconds the mediator checks for expired
     *            registrations
     * @param maxLifetimeInSeconds
     *            the maximal time in seconds a target can be registered without
     *            regenerating the registration
     */
    public Mediator(final int listenerPort, final int iterationTimeInSeconds, final int maxLifetimeInSeconds) {
        this.handlerExecutor = Executors.newCachedThreadPool();
        this.cleanerExecutor = Executors.newSingleThreadExecutor();
        this.socketListenerExecutor = Executors.newSingleThreadExecutor();
        this.listenerPort = listenerPort;
        this.iterationTimeInSeconds = iterationTimeInSeconds;
        this.maxLifetimeInSeconds = maxLifetimeInSeconds;
        this.handleMessageTaskFactory = new HandleMessageTaskFactory();
    }

    /**
     * Starts the threads to handle messages and to check for expired
     * registrations.
     * 
     * @throws Exception
     */
    public void startMediator() throws Exception {
        this.startMessageHandler();
        this.startUserCleaner();
    }

    private void startMessageHandler() throws Exception {
        final ServerSocket listenerSocket = new ServerSocket(this.listenerPort);
        final SocketListener socketListener = new SocketListener(listenerSocket, this.handlerExecutor,
                this.handleMessageTaskFactory);
        this.socketListenerExecutor.execute(socketListener);
        logger.info("Message handling started, mediator is listening on port {}", this.listenerPort); //$NON-NLS-1$
    }

    private void startUserCleaner() {
        final UserCleanerTask userCleanerTask = new UserCleanerTask(this.iterationTimeInSeconds,
                this.maxLifetimeInSeconds);
        this.cleanerExecutor.execute(userCleanerTask);
        logger.info(
                "User cleaner started with iteration {} and max lifetime {}", this.iterationTimeInSeconds, this.maxLifetimeInSeconds); //$NON-NLS-1$
    }

    private static void checkArgumentsCount(final String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException(
                    "Illegal count of arguments, arguments: listenerPort iterationTimeInSeconds maxLifetimeInSeconds"); //$NON-NLS-1$
        }
    }

    private static int parseNumber(final String[] args, final int indexToParse) {
        try {
            return Integer.parseInt(args[indexToParse]);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Illegal argument, number expected"); //$NON-NLS-1$
        }
    }

    /**
     * Main method to create a mediator and start it.
     * 
     * @param args
     *            args[0] listener port, args[1] iteration time in seconds,
     *            args[2] maximal lifetime in seconds
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        checkArgumentsCount(args);
        final int listenerPort = parseNumber(args, 0);
        final int iterationTimeInSeconds = parseNumber(args, 1);
        final int maxLifetimeInSeconds = parseNumber(args, 2);
        final Mediator allInOneMediator = new Mediator(listenerPort, iterationTimeInSeconds, maxLifetimeInSeconds);
        allInOneMediator.startMediator();
    }
}
