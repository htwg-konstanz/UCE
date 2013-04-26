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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.mediator.Mediator;
import de.fhkn.in.uce.relaying.server.RelayServer;
import de.fhkn.in.uce.stun.server.StunServer;

/**
 * Class to start a main server which starts a stun, relay and mediator server.
 * Furthermore this server waits until the other three servers are shutdown
 * before it quits itself.
 *
 * @author Robert Danczak
 */
public class MasterServer {

    private static final Logger logger = LoggerFactory.getLogger(MasterServer.class);

    private final int EXECUTOR_THREADS = 3;
    private final int TERMINATION_TIME = 100;
    private final long SLEEP_TIME = 100;
    private final Pattern ipPattern = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    private final int RelayArgCount = 1;
    private final int StunArgCount = 2;
    private final int MediatorArgCount = 3;

    // possible command line args.
    private final String stunFirstIP = "StunFirstIP";
    private final String stunSecondIP = "StunSecondIP";
    private final String relayPort = "RelayPort";
    private final String mediatorPort = "MediatorPort";
    private final String mediatorIteration = "MediatorIteration";
    private final String mediatorLifeTime = "MediatorLifeTime";

    private final ExecutorService executorService;
    private final String args[];
    private List<String> stunArgs;
    private List<String> relayArgs;
    private List<String> mediatorArgs;

    private Future<?> stunServerFuture;
    private Future<?> relayServerFuture;
    private Future<?> mediatorServerFuture;

    /**
     * Creates a master server with the given args.
     *
     * @param args
     *            args from command line
     */
    public MasterServer(String[] args) {
        executorService = Executors.newFixedThreadPool(EXECUTOR_THREADS);
        this.args = args;
        relayArgs = new ArrayList<String>(RelayArgCount);
        stunArgs = new ArrayList<String>(StunArgCount);
        mediatorArgs = new ArrayList<String>(MediatorArgCount);
    }

    /**
     * Main method to create and start the master server.
     *
     * @param args
     */
    public static void main(String[] args) {
        MasterServer masterServer = new MasterServer(args);
        try {
            masterServer.run();
        } catch (Exception e) {
            logger.error("An error occured during startup of the master server.");
            e.printStackTrace();
        }
    }

    /**
     * Starts the master server and its children stun, relay and mediator.
     *
     * @throws InterruptedException
     */
    public void run() throws InterruptedException {
        try {
            checkAndSetArgs();
        } catch (IllegalArgumentException e) {
            logger.error("Not all needed arguments are present!");
            System.err.println("Not all needed arguments are present!");
            printHelp();
            return;
        }

        stunServerTask();
        relayServerTask();
        mediatorServerTask();

        // do not accept any more tasks.
        executorService.shutdown();

        while (true) {
            if (relayServerFuture.isCancelled() || relayServerFuture.isDone()) {
                logger.info("Relay Server was shutdown.");
                break;
            }
            if (stunServerFuture.isCancelled() || stunServerFuture.isDone()) {
                logger.info("Stun Server was shutdown.");
                break;
            }
            if (mediatorServerFuture.isCancelled() || mediatorServerFuture.isDone()) {
                logger.info("Mediator Server was shutdown.");
                break;
            }
            if (Thread.currentThread().isInterrupted()) {
                logger.info("Master Server was shutdown.");
                break;
            }
            Thread.currentThread().wait(SLEEP_TIME);
        }

        shutdown();
    }

    private void checkAndSetArgs() throws IllegalArgumentException {
        for (String arg : args) {
            if (arg.startsWith(stunFirstIP) || arg.startsWith("-" + stunFirstIP)) {
                processStunFirstIP(arg);
            }
            else if (arg.startsWith(stunSecondIP) || arg.startsWith("-" + stunSecondIP)) {
                processStunSecondIP(arg);
            }
            else if (arg.startsWith(relayPort) || arg.startsWith("-" + relayPort)) {
                processRelayPort(arg);
            }
            else if (arg.startsWith(mediatorPort) || arg.startsWith("-" + mediatorPort)) {
                processMediatorPort(arg);
            }
            else if (arg.startsWith(mediatorIteration) || arg.startsWith("-" + mediatorIteration)) {
                processMediatorIteration(arg);
            }
            else if (arg.startsWith(mediatorLifeTime) || arg.startsWith("-" + mediatorLifeTime)) {
                processMediatorLifetime(arg);
            }
            else if (arg.contentEquals("?") || arg.contentEquals("-h") || arg.contentEquals("--help")) {
                printHelp();
            }
            else {
                logger.info("Argument \"{}\" not recognized", arg);
            }
        }
        if ((relayArgs.size() != RelayArgCount) || (stunArgs.size() != StunArgCount) || (mediatorArgs.size() != MediatorArgCount)) {
            throw new IllegalArgumentException("Parameters are missing!");
        }
    }


    private void shutdown() {
        try {
            executorService.shutdown();
            logger.info("Force shutting down worker threads in {} ms", TERMINATION_TIME);
            if (!executorService.awaitTermination(TERMINATION_TIME, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
            stunServerFuture.cancel(true);
            relayServerFuture.cancel(true);
            mediatorServerFuture.cancel(true);
        } catch (Exception e) {
            executorService.shutdown();
            Thread.currentThread().interrupt();
        }
    }

    private boolean isIP(String toCheck) {
        toCheck = toCheck.trim();
        Matcher m = ipPattern.matcher(toCheck);
        return m.matches();
    }

    private boolean isPort(String port) {
        port = port.trim();
        int result = Integer.parseInt(port);
        if((result >= 1024) && (result < 65536)) {
            return true;
        }
        return false;
    }

    private void processMediatorLifetime(String arg) {
        String[] splitted = arg.split(mediatorLifeTime + "=");
        String result = splitted[1];
        logger.info("added max lifetime \"{}\" to mediatorArgs", result);
        mediatorArgs.add(2, result);
    }

    private void processMediatorIteration(String arg) {
        String[] splitted = arg.split(mediatorIteration + "=");
        String result = splitted[1];
        logger.info("added iteration time \"{}\" to mediatorArgs",result);
        mediatorArgs.add(1, result);
    }

    private void processMediatorPort(String arg) {
        String[] splitted = arg.split(mediatorPort + "=");
        String result = splitted[1];
        if(!isPort(result)) {
            throw new IllegalArgumentException();
        }
        logger.info("added port \"{}\" to mediatorArgs", result);
        mediatorArgs.add(0, result);
    }

    private void processRelayPort(String arg) {
        String[] splitted = arg.split(relayPort + "=");
        String result = splitted[1];
        if(!isPort(result)) {
            throw new IllegalArgumentException();
        }
        logger.info("added port \"{}\" to relayArgs", result);
        relayArgs.add(result);
    }

    private void processStunSecondIP(String arg) {
        String[] splitted = arg.split(stunSecondIP + "=");
        String result = splitted[1];
        if(!isIP(result)) {
            throw new IllegalArgumentException();
        }
        logger.info("added second IP \"{}\" to stunArgs", result);
        stunArgs.add(1, result);
    }

    private void processStunFirstIP(String arg) {
        String[] splitted = arg.split(stunFirstIP + "=");
        String result = splitted[1];
        if(!isIP(result)) {
            throw new IllegalArgumentException();
        }
        logger.info("added first IP \"{}\" to stunArgs", result);
        stunArgs.add(0, result);
    }

    private void printHelp() {
        String msg = "Please provide the following arguments to start the server:\n"
                   + stunFirstIP + "=, " + stunSecondIP + "=, " + relayPort
                   + "=, " + mediatorPort + "=, " + mediatorIteration + "=, "
                   + mediatorLifeTime + "=";
        logger.error(msg);
        System.err.println(msg);
    }

    private void relayServerTask() {
        relayServerFuture = executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    RelayServer.main(relayArgs.toArray(new String[relayArgs.size()]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void stunServerTask() {
        stunServerFuture = executorService.submit(new Runnable() {
            @Override
            public void run() {
                StunServer.main(stunArgs.toArray(new String[stunArgs.size()]));
            }
        });
    }

    private void mediatorServerTask() {
        mediatorServerFuture = executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Mediator.main(mediatorArgs.toArray(new String[mediatorArgs.size()]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
