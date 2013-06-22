/*
 * Copyright (c) 2013 Robert Danczak,
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
package de.fhkn.in.uce.master.server;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.mediator.Mediator;
import de.fhkn.in.uce.relaying.server.RelayServer;
import de.fhkn.in.uce.stun.server.StunServer;


/**
 * Class to start a main server which starts a stun, relay and mediator server.
 *
 * @author Robert Danczak
 */
public class MasterServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MasterServer.class);

    private final int executorThreads = 3;
    private final int terminationTime = 100;

    private final ExecutorService executorService;
    private ArgumentHandler argHandler;

    /**
     * Creates a master server.
     */
    public MasterServer() {
        executorService = Executors.newFixedThreadPool(executorThreads);
        argHandler = new ArgumentHandler(LOGGER);
    }

    /**
     * Main method to create and start the master server.
     *
     * @param args
     *            command line arguments.
     */
    public static void main(final String[] args) {
        MasterServer masterServer = new MasterServer();
        try {
            masterServer.run(args);
        } catch (Exception e) {
            LOGGER.error("An error occured during startup of the master server.");
            LOGGER.error("Execption: ", e);
            e.printStackTrace();
        }
    }

    /**
     * Starts the master server and its children stun, relay and mediator.
     *
     * @param args
     *            command line arguments.
     */
    public void run(final String[] args) {
        try {
            argHandler.parseArguments(args);
        } catch (IllegalArgumentException e) {
            return;
        }

        stunServerTask();
        relayServerTask();
        mediatorServerTask();

        //sleep one second before shuting down the executors
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //do nothing
        }
        shutdownExecutor();
    }

    private void shutdownExecutor() {
        try {
            executorService.shutdown();
            LOGGER.info("Force shutting down worker threads in {} ms", terminationTime);
            if (!executorService.awaitTermination(terminationTime, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (Exception e) {
            executorService.shutdown();
            Thread.currentThread().interrupt();
        }
    }

    private void logInfo(final String msg) {
        System.out.println(msg);
        LOGGER.info(msg);
    }

    private void relayServerTask() {
        logInfo("Starting Relay Server");
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> relayArgs = argHandler.getRelayArgs();
                    RelayServer.main(relayArgs.toArray(new String[relayArgs.size()]));
                    logInfo("Successfully started Relay Server");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void stunServerTask() {
        logInfo("Starting Stun Server");
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                List<String> stunArgs = argHandler.getStunArgs();
                StunServer.main(stunArgs.toArray(new String[stunArgs.size()]));
                logInfo("Successfully started Stun Server");
            }
        });
    }

    private void mediatorServerTask() {
        logInfo("Starting Mediator Server");
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> mediatorArgs = argHandler.getMediatorArgs();
                    Mediator.main(mediatorArgs.toArray(new String[mediatorArgs.size()]));
                    logInfo("Successfully started Mediator Server");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
