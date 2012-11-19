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
package de.fhkn.in.uce.reversal.mediator;

import java.net.ServerSocket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.net.SocketListener;

/**
 * Main-Class which starts the Mediator functionality
 * 
 * Mediator is an public accessible Server where clients (targets) can be
 * registered. If another client (source) wants to access to a registered client
 * (target) it sends a request to Mediator, which will help to connect these
 * clients.
 * 
 * For connecting the clients it is necessary that one of those clients (target)
 * runs as ConnectionReversalTarget and the other client (source) runs as
 * ConnectionReversalSource.
 * 
 * The target client can be behind a NAT-Router, the source client must be
 * accessible for the public
 * 
 * @author thomas zink, stefan lohr
 */
public class Mediator {

    private static final Logger logger = LoggerFactory.getLogger(Mediator.class);
    private static final Executor executor = Executors.newCachedThreadPool();
    private static final int ITERATION_TIME_IN_SECONDS = 500;
    private static final int MAX_LIFE_TIME_IN_SECONDS = 350;

    public static void main(final String[] args) throws Exception {
        checkArgumentsCount(args, 1);
        final int listenerPort = parsePortNumber(args);
        runListenerThread(listenerPort);
        runCleanerThread();
    }

    private static void checkArgumentsCount(final String[] args, final int count) {
        if (args.length != count) {
            logger.error("Illegal count of arguments, exact {} argument(s) expected.", count); //$NON-NLS-1$
            System.exit(1);
        }

    }

    private static int parsePortNumber(final String[] args) {
        try {
            return Integer.parseInt(args[0]);
        } catch (final Exception e) {
            logger.error("Illegal argument, number expected"); //$NON-NLS-1$
            System.exit(2);
            return 0;
        }
    }

    private static void runListenerThread(final int listenerPort) throws Exception {
        final ServerSocket listenerSocket = new ServerSocket(listenerPort);
        final SocketListener socketListener = new SocketListener(listenerSocket, Executors.newCachedThreadPool(),
                new HandleMessageTaskFactory());
        executor.execute(socketListener);
        logger.info("Mediator for relaying is running on port {}", listenerPort);
    }

    private static void runCleanerThread() {
        UserCleaner cleaner = new UserCleaner(ITERATION_TIME_IN_SECONDS, MAX_LIFE_TIME_IN_SECONDS);
        cleaner.start();
        logger.info("Cleaner thread started.");
    }
    // private static final Logger logger = LoggerFactory.getLogger("Mediator");
    //
    // /**
    // * @param args
    // * arg0: listenerPort; arg1: iterationTimeInSeconds; arg2:
    // * maxLifeTimeInSeconds
    // */
    // public static void main(String[] args) {
    //
    // int listenerPort;
    // int iterationTimeInSeconds = 500;
    // int maxLifeTimeInSeconds = 350;
    //
    // if (args.length != 1) {
    // logger.error("Illegal count of arguments, exact one argument expected");
    // logger.error("listenerPort");
    // System.exit(1);
    // }
    //
    // try {
    // listenerPort = Integer.parseInt(args[0]);
    // // iterationTimeInSeconds = Integer.parseInt(args[1]);
    // // maxLifeTimeInSeconds = Integer.parseInt(args[2]);
    // } catch (Exception e) {
    // logger.error("Illegal arguments, numbers expected");
    // System.exit(2);
    // return;
    // }
    //
    // ListenerThread listenerThread = new ListenerThread(listenerPort);
    // UserCleaner userCleaner = new UserCleaner(iterationTimeInSeconds,
    // maxLifeTimeInSeconds);
    //
    // listenerThread.start();
    // userCleaner.run();
    // }
}
