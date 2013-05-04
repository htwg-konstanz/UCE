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

    private static final int relayArgCount = 1;
    private static final int stunArgCount = 2;
    private static final int mediatorArgCount = 3;

    /**
     * Creates the ArgumentHandler.
     *
     * @param logger
     *            reference to a logger
     * @throws FileNotFoundException
     * @throws IOException
     */
    public ArgumentHandler(Logger logger) {
        this.logger = logger;

        relayArgs = new ArrayList<String>(relayArgCount);
        for (int i = 0; i < relayArgCount; i++) {
            relayArgs.add("");
        }
        stunArgs = new ArrayList<String>(stunArgCount);
        for (int i = 0; i < stunArgCount; i++) {
            stunArgs.add("");
        }
        mediatorArgs = new ArrayList<String>(mediatorArgCount);
        for (int i = 0; i < mediatorArgCount; i++) {
            mediatorArgs.add("");
        }
    }

    public List<String> getStunArgs() {
        return stunArgs;
    }

    public List<String> getRelayArgs() {
        return relayArgs;
    }

    public List<String> getMediatorArgs() {
        return mediatorArgs;
    }

    public void parseArguments(String[] args) {
        for(String arg : args) {
            if (arg.contentEquals("?") || arg.contentEquals("-h") || arg.contentEquals("--help")) {
                printHelp();
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
        } catch (IOException e) {
            // do nothing because we expect the arguments either via system properties
            // or as command line args.
        }
    }

    private void setArgsFromPropertiesFile() throws IOException {
        FilePropertyReader filePropsReader = new FilePropertyReader(logger);
        filePropsReader.readArguments(stunArgs, relayArgs, mediatorArgs);
    }

    private void setArgsFromSystemProperties() {
        SystemPropertyReader sysPropsReader = new SystemPropertyReader(logger);
        sysPropsReader.readArguments(stunArgs, relayArgs, mediatorArgs);
    }

    private void setArgsFromCommandLine(final String[] args) throws IllegalArgumentException {
        CmdReader cmdReader = new CmdReader(logger, args);
        cmdReader.readArguments(stunArgs, relayArgs, mediatorArgs);

    }

    private void checkArgs() {
        for (int i = 0; i < relayArgCount; i++) {
            if (relayArgs.get(i).equals("")) {
                throw new IllegalArgumentException("Parameters are missing!");
            }
        }
        for (int i = 0; i < stunArgCount; i++) {
            if (stunArgs.get(i).equals("")) {
                throw new IllegalArgumentException("Parameters are missing!");
            }
        }
        for (int i = 0; i < mediatorArgCount; i++) {
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
                   + AbstractReader.stunFirstIP + "=, " + AbstractReader.stunSecondIP + "=, "
                   + AbstractReader.relayPort + "=, " + AbstractReader.mediatorPort + "=, "
                   + AbstractReader.mediatorIteration + "=, " + AbstractReader.mediatorLifeTime + "=";
        logError(msg);
    }
}
