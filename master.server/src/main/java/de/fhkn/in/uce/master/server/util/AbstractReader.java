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
package de.fhkn.in.uce.master.server.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

/**
 * Abstract Class which holds common functions needed by
 * {@link de.fhkn.in.uce.master.server.util.FilePropertyReader FilePropertyReader},
 * {@link de.fhkn.in.uce.master.server.util.CmdReader CmdReader} and
 * {@link de.fhkn.in.uce.master.server.util.SystemPropertyReader SystemPropertyReader}.
 *
 * @author Robert Danczak
 */
public abstract class AbstractReader {

    protected static final Pattern ipPattern = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    protected final Logger logger;

    // possible args.
    protected static final String stunFirstIP = "StunFirstIP";
    protected static final String stunSecondIP = "StunSecondIP";
    protected static final String relayPort = "RelayPort";
    protected static final String mediatorPort = "MediatorPort";
    protected static final String mediatorIteration = "MediatorIteration";
    protected static final String mediatorLifeTime = "MediatorLifeTime";

    /**
     * Creates an AbstractReader.
     */
    public AbstractReader(Logger logger) {
        this.logger = logger;
    }

    /**
     * Abstract method for parsing arguments to
     * {@link de.fhkn.in.uce.mediator.Mediator Mediator},
     * {@link de.fhkn.in.uce.stun.server.StunServer StunServer} and
     * {@link de.fhkn.in.uce.relaying.server.RelayServer RelayServer}.
     *
     * @param stunArgs
     *            Arguments for the stun server.
     * @param relayArgs
     *            Arguments for the relay server.
     * @param mediatorArgs
     *            Arguments for the mediator server.
     * @throws IllegalArgumentException
     */
    public abstract void readArguments(List<String> stunArgs, List<String> relayArgs, List<String> mediatorArgs) throws IllegalArgumentException;

    protected void logInfo(final String msg) {
        System.out.println(msg);
        logger.info(msg);
    }

    protected void logError(String msg) {
        System.err.println(msg);
        logger.error(msg);
    }

    protected boolean isIP(final String toCheck) {
        String tmp = toCheck.trim();
        Matcher m = ipPattern.matcher(tmp);
        return m.matches();
    }

    protected boolean isPort(final String port) {
        String tmp = port.trim();
        int result = Integer.parseInt(tmp);
        if ((result >= 1024) && (result < 65536)) {
            return true;
        }
        return false;
    }

    protected void processMediatorLifeTime(List<String> mediatorArgs, final String arg) throws IllegalArgumentException {
        if ((arg == null) || "".equals(arg)) {
            throw new IllegalArgumentException();
        }
        logInfo("added max lifetime \"" + arg + "\" to mediator arguments");
        mediatorArgs.set(2, arg);
    }

    protected void processMediatorIteration(List<String> mediatorArgs, final String arg) throws IllegalArgumentException {
        if ((arg == null) || "".equals(arg)) {
            throw new IllegalArgumentException();
        }
        logInfo("added iteration time \"" + arg + "\" to mediator arguments");
        mediatorArgs.set(1, arg);
    }

    protected void processMediatorPort(List<String> mediatorArgs, final String arg) throws IllegalArgumentException {
        if ((arg == null) || ("".equals(arg) || !isPort(arg))) {
            throw new IllegalArgumentException();
        }
        logInfo("added port \"" + arg + "\" to mediator arguments");
        mediatorArgs.set(0, arg);
    }

    protected void processRelayPort(List<String> relayArgs, final String arg) throws IllegalArgumentException {
        if ((arg == null) || ("".equals(arg) || !isPort(arg))) {
            throw new IllegalArgumentException();
        }
        logInfo("added port \"" + arg + "\" to relay arguments");
        relayArgs.set(0, arg);
    }

    protected void processStunSecondIP(List<String> stunArgs, final String arg) throws IllegalArgumentException {
        if ((arg == null) || "".equals(arg) || !isIP(arg)) {
            throw new IllegalArgumentException();
        }
        logInfo("added second IP \"" + arg + "\" to stun arguments");
        stunArgs.set(1, arg);
    }

    protected void processStunFirstIP(List<String> stunArgs, final String arg) throws IllegalArgumentException {
        if ((arg == null) || "".equals(arg) || !isIP(arg)) {
            throw new IllegalArgumentException();
        }
        logInfo("added first IP \"" + arg + "\" to stun arguments");
        stunArgs.set(0, arg);
    }

    /**
     * @return the string "StunFirstIP"
     */
    public static String getStunFirstIP() {
        return stunFirstIP;
    }

    /**
     * @return the string "StunSecondIP"
     */
    public static String getStunSecondIP() {
        return stunSecondIP;
    }

    /**
     * @return the string "RelayPort"
     */
    public static String getRelayPort() {
        return relayPort;
    }

    /**
     * @return the string "MediatorPort"
     */
    public static String getMediatorPort() {
        return mediatorPort;
    }

    /**
     * @return the string "MediatorIteration"
     */
    public static String getMediatorIteration() {
        return mediatorIteration;
    }

    /**
     * @return the string "MediatorLifeTime"
     */
    public static String getMediatorLifeTime() {
        return mediatorLifeTime;
    }
}
