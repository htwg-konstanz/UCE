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
        for(int i=0; i < RelayArgCount; i++) {
            relayArgs.add("");
        }
        stunArgs = new ArrayList<String>(StunArgCount);
        for(int i=0; i < StunArgCount; i++) {
            stunArgs.add("");
        }
        mediatorArgs = new ArrayList<String>(MediatorArgCount);
        for(int i=0; i < MediatorArgCount; i++) {
            mediatorArgs.add("");
        }
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
            logger.error("Execption:", e);
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
            logError("Not all needed arguments are present!");
            printHelp();
            return;
        }

        stunServerTask();
        relayServerTask();
        mediatorServerTask();

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
                logInfo("Argument \"" + arg + "\" not recognized");
            }
        }

        for(int i=0; i < RelayArgCount; i++) {
            if(relayArgs.get(i).equals("")) {
                throw new IllegalArgumentException("Parameters are missing!");
            }
        }
        for(int i=0; i < StunArgCount; i++) {
            if(stunArgs.get(i).equals("")) {
                throw new IllegalArgumentException("Parameters are missing!");
            }
        }
        for(int i=0; i < MediatorArgCount; i++) {
            if(mediatorArgs.get(i).equals("")) {
                throw new IllegalArgumentException("Parameters are missing!");
            }
        }
    }

    private void shutdown() {
        try {
            executorService.shutdown();
            logger.info("Force shutting down worker threads in {} ms", TERMINATION_TIME);
            if (!executorService.awaitTermination(TERMINATION_TIME, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
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

    private void logInfo(String msg) {
        System.out.println(msg);
        logger.info(msg);
    }

    private void logError(String msg) {
        System.err.println(msg);
        logger.error(msg);
    }

    private void processMediatorLifetime(String arg) {
        String[] splitted = arg.split(mediatorLifeTime + "=");
        String result = splitted[1];
        logInfo("added max lifetime \"" + result + "\" to mediator arguments");
        mediatorArgs.set(2, result);
    }

    private void processMediatorIteration(String arg) {
        String[] splitted = arg.split(mediatorIteration + "=");
        String result = splitted[1];
        logInfo("added iteration time \"" + result + "\" to mediator arguments");
        mediatorArgs.set(1, result);
    }

    private void processMediatorPort(String arg) {
        String[] splitted = arg.split(mediatorPort + "=");
        String result = splitted[1];
        if(!isPort(result)) {
            throw new IllegalArgumentException();
        }
        logInfo("added port \"" + result + "\" to mediator arguments");
        mediatorArgs.set(0, result);
    }

    private void processRelayPort(String arg) {
        String[] splitted = arg.split(relayPort + "=");
        String result = splitted[1];
        if(!isPort(result)) {
            throw new IllegalArgumentException();
        }
        logInfo("added port \"" + result + "\" to relay arguments");
        relayArgs.set(0, result);
    }

    private void processStunSecondIP(String arg) {
        String[] splitted = arg.split(stunSecondIP + "=");
        String result = splitted[1];
        if(!isIP(result)) {
            throw new IllegalArgumentException();
        }
        logInfo("added second IP \"" + result + "\" to stun arguments");
        stunArgs.set(1, result);
    }

    private void processStunFirstIP(String arg) {
        String[] splitted = arg.split(stunFirstIP + "=");
        String result = splitted[1];
        if(!isIP(result)) {
            throw new IllegalArgumentException();
        }
        logInfo("added first IP \"" + result + "\" to stun arguments");
        stunArgs.set(0, result);
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
        logInfo("Starting Relay Server");
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
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
                    Mediator.main(mediatorArgs.toArray(new String[mediatorArgs.size()]));
                    logInfo("Successfully started Mediator Server");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
