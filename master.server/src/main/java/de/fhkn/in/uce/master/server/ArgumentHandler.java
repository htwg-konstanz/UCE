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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import de.fhkn.in.uce.master.server.util.CmdReader;
import de.fhkn.in.uce.master.server.util.FilePropertyReader;
import de.fhkn.in.uce.master.server.util.SystemPropertyReader;

/**
 * Class to handle arguments from file, system prpoerties and command line args.
 *
 * @author Robert Danczak
 *
 */
public class ArgumentHandler {

    private Logger logger;

    private final Pattern ipPattern = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    private final FilePropertyReader filePropsReader;
    private final SystemPropertyReader sysPropsReader;
    private final CmdReader cmdReader;

    // possible command line args.
    public final String stunFirstIP = "StunFirstIP";
    public final String stunSecondIP = "StunSecondIP";
    public final String relayPort = "RelayPort";
    public final String mediatorPort = "MediatorPort";
    public final String mediatorIteration = "MediatorIteration";
    public final String mediatorLifeTime = "MediatorLifeTime";

    private List<String> stunArgs;
    private List<String> relayArgs;
    private List<String> mediatorArgs;

    private final int relayArgCount = 1;
    private final int stunArgCount = 2;
    private final int mediatorArgCount = 3;

    /**
     * Creates the ArgumentHandler.
     *
     * @param logger
     *            reference to a logger
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public ArgumentHandler(Logger logger) throws FileNotFoundException, IOException {
        this.logger = logger;

        filePropsReader = new FilePropertyReader();
        sysPropsReader = new SystemPropertyReader();
        cmdReader = new CmdReader();

        relayArgs = new ArrayList<String>(relayArgCount);
        for(int i=0; i < relayArgCount; i++) {
            relayArgs.add("");
        }
        stunArgs = new ArrayList<String>(stunArgCount);
        for(int i=0; i < stunArgCount; i++) {
            stunArgs.add("");
        }
        mediatorArgs = new ArrayList<String>(mediatorArgCount);
        for(int i=0; i < mediatorArgCount; i++) {
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

    }

    private void setArgsFromSystemProperties() {

    }

    private void setArgsFromCommandLine(final String[] args) throws IllegalArgumentException {
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
    }

    private void checkArgs() {
        for(int i=0; i < relayArgCount; i++) {
            if(relayArgs.get(i).equals("")) {
                throw new IllegalArgumentException("Parameters are missing!");
            }
        }
        for(int i=0; i < stunArgCount; i++) {
            if(stunArgs.get(i).equals("")) {
                throw new IllegalArgumentException("Parameters are missing!");
            }
        }
        for(int i=0; i < mediatorArgCount; i++) {
            if(mediatorArgs.get(i).equals("")) {
                throw new IllegalArgumentException("Parameters are missing!");
            }
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
}
