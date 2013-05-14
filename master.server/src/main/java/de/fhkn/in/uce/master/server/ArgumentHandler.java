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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import de.fhkn.in.uce.master.server.util.AbstractReader;
import de.fhkn.in.uce.master.server.util.CmdReader;
import de.fhkn.in.uce.master.server.util.FilePropertyReader;
import de.fhkn.in.uce.master.server.util.SystemPropertyReader;

/**
 * Class to handle arguments from file, system prpoerties and command line args.
 *
 * @author Robert Danczak
 */
public class ArgumentHandler {

    private final Logger logger;

    private List<String> stunArgs;
    private List<String> relayArgs;
    private List<String> mediatorArgs;

    private static final int RELAY_ARG_COUNT = 1;
    private static final int STUN_ARG_COUNT = 2;
    private static final int MEDIATOR_ARG_COUNT = 3;

    /**
     * Creates the ArgumentHandler.
     *
     * @param logger
     *            reference to a logger
     * @throws FileNotFoundException
     * @throws IOException
     */
    public ArgumentHandler(final Logger logger) {
        this.logger = logger;

        relayArgs = new ArrayList<String>(RELAY_ARG_COUNT);
        for (int i = 0; i < RELAY_ARG_COUNT; i++) {
            relayArgs.add("");
        }
        stunArgs = new ArrayList<String>(STUN_ARG_COUNT);
        for (int i = 0; i < STUN_ARG_COUNT; i++) {
            stunArgs.add("");
        }
        mediatorArgs = new ArrayList<String>(MEDIATOR_ARG_COUNT);
        for (int i = 0; i < MEDIATOR_ARG_COUNT; i++) {
            mediatorArgs.add("");
        }
    }

    /**
     * @return stun server arguments
     */
    public List<String> getStunArgs() {
        return stunArgs;
    }

    /**
     * @return relay server arguments
     */
    public List<String> getRelayArgs() {
        return relayArgs;
    }

    /**
     * @return mediator server arguments
     */
    public List<String> getMediatorArgs() {
        return mediatorArgs;
    }

    /**
     * Parses arguments first from property file,
     * then from system properties and
     * at last from command line.
     *
     * @param args
     *            arguments from command line
     * @throws IllegalArgumentException
     */
    public void parseArguments(final String[] args) throws IllegalArgumentException {
        for (String arg : args) {
            if (arg.contentEquals("?") || arg.contentEquals("-h") || arg.contentEquals("--help")) {
                printHelp();
                throw new IllegalArgumentException();
            }
        }
        try {
            setArgsFromPropertiesFile();
            setArgsFromSystemProperties();
            setArgsFromCommandLine(args);
            checkArgs();
        } catch (IllegalArgumentException e) {
            logError("Not all needed arguments are present!");
            printHelp();
            throw new IllegalArgumentException();
        }
    }

    private void setArgsFromPropertiesFile() {
        FilePropertyReader filePropsReader = new FilePropertyReader();
        try {
            filePropsReader.readArguments(stunArgs, relayArgs, mediatorArgs);
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    private void setArgsFromSystemProperties() throws IllegalArgumentException {
        SystemPropertyReader sysPropsReader = new SystemPropertyReader();
        sysPropsReader.readArguments(stunArgs, relayArgs, mediatorArgs);
    }

    private void setArgsFromCommandLine(final String[] args) throws IllegalArgumentException {
        CmdReader cmdReader = new CmdReader(args);
        cmdReader.readArguments(stunArgs, relayArgs, mediatorArgs);

    }

    private void checkArgs() {
    //removed check due to default value in relay server
//        for (int i = 0; i < RELAY_ARG_COUNT; i++) {
//            if (relayArgs.get(i).equals("")) {
//                throw new IllegalArgumentException("Parameters are missing!");
//            }
//        }
        for (int i = 0; i < STUN_ARG_COUNT; i++) {
            if (stunArgs.get(i).equals("")) {
                throw new IllegalArgumentException("Parameters are missing!");
            }
        }
        for (int i = 0; i < MEDIATOR_ARG_COUNT; i++) {
            if (mediatorArgs.get(i).equals("")) {
                throw new IllegalArgumentException("Parameters are missing!");
            }
        }
    }

    private void logError(String msg) {
        System.err.println(msg);
        logger.error(msg);
    }

    private void printHelp() {
        String msg = "Please provide the following arguments to start the server:\n"
                   + AbstractReader.getStunFirstIP() + "=, " + AbstractReader.getStunSecondIP() + "=, "
                   + AbstractReader.getRelayPort() + "=  (optional), " + AbstractReader.getMediatorPort() + "=, "
                   + AbstractReader.getMediatorIteration() + "=, " + AbstractReader.getMediatorLifeTime() + "=";
        logError(msg);
    }
}
