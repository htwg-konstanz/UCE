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

import java.net.InetAddress;
import java.util.List;

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

    protected final Logger logger;

    // possible args.
    protected static final String STUN_FIRST_IP = "StunFirstIP";
    protected static final String STUN_SECOND_IP = "StunSecondIP";
    protected static final String RELAY_PORT = "RelayPort";
    protected static final String MEDIATOR_PORT = "MediatorPort";
    protected static final String MEDIATOR_ITERATION = "MediatorIteration";
    protected static final String MEDIATOR_LIFETIME = "MediatorLifeTime";

    /**
     * Creates an AbstractReader.
     *
     * @param logger
     *            where to log to.
     */
    public AbstractReader(final Logger logger) {
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
     *             if arguments are invalid
     */
    public abstract void readArguments(List<String> stunArgs, List<String> relayArgs, List<String> mediatorArgs) throws IllegalArgumentException;

    /**
     * Logs {@code msg} as info in logger and sysout.
     *
     * @param msg
     *            what to log.
     */
    protected void logInfo(final String msg) {
        System.out.println(msg);
        logger.info(msg);
    }

    /**
     * Logs {@code msg} as error in logger and syserr.
     *
     * @param msg
     *            what to log.
     */
    protected void logError(final String msg) {
        System.err.println(msg);
        logger.error(msg);
    }

    /**
     * Checks if the given string is an IPv4 address.
     *
     * @param toCheck
     *            String to check.
     * @return true if valid IP else false.
     */
    protected boolean isIP(final String toCheck) {
        String tmp = toCheck.trim();
        if (!tmp.isEmpty()) {
            try {
                InetAddress.getByName(tmp);
                return true;
            } catch (Exception e) {
                // return false if we get a UnknownHostException or a SecurityException.
                return false;
            }
        }
        return false;
    }

    /**
     * Checks if the given string is a valid port in [1024;65536].
     *
     * @param port
     *            String to check.
     * @return true if port is between 1024 and 65536 else false.
     */
    protected boolean isPort(final String port) {
        String tmp = port.trim();
        int result = Integer.parseInt(tmp);
        if ((result >= 1024) && (result < 65536)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the given argument {@code arg} is valid and writes it to
     * {@code mediatorArgs}.
     *
     * @param mediatorArgs
     *            where to write the argument to.
     * @param arg
     *            which argument to write.
     * @throws IllegalArgumentException
     *             If argument {@code arg} is null or empty.
     */
    protected void processMediatorLifeTime(List<String> mediatorArgs, final String arg) throws IllegalArgumentException {
        if ((arg == null) || "".equals(arg)) {
            throw new IllegalArgumentException(MEDIATOR_LIFETIME);
        }
        logInfo("added max lifetime \"" + arg + "\" to mediator arguments");
        mediatorArgs.set(2, arg);
    }

    /**
     * Checks if the given argument {@code arg} is valid and writes it to
     * {@code mediatorArgs}.
     *
     * @param mediatorArgs
     *            where to write the argument to.
     * @param arg
     *            which argument to write.
     * @throws IllegalArgumentException
     *             If argument {@code arg} is null or empty.
     */
    protected void processMediatorIteration(List<String> mediatorArgs, final String arg) throws IllegalArgumentException {
        if ((arg == null) || "".equals(arg)) {
            throw new IllegalArgumentException(MEDIATOR_ITERATION);
        }
        logInfo("added iteration time \"" + arg + "\" to mediator arguments");
        mediatorArgs.set(1, arg);
    }

    /**
     * Checks if the given argument {@code arg} is a valid port and writes it to
     * {@code mediatorArgs}.
     *
     * @param mediatorArgs
     *            where to write the argument to.
     * @param arg
     *            which argument to write.
     * @throws IllegalArgumentException
     *             If argument {@code arg} is null, empty or not a port.
     */
    protected void processMediatorPort(List<String> mediatorArgs, final String arg) throws IllegalArgumentException {
        if ((arg == null) || "".equals(arg) || !isPort(arg)) {
            throw new IllegalArgumentException(MEDIATOR_PORT);
        }
        logInfo("added port \"" + arg + "\" to mediator arguments");
        mediatorArgs.set(0, arg);
    }

    /**
     * Checks if the given argument {@code arg} is a valid port and writes it to
     * {@code relayArgs}. If the argument is null or empty, no value will be written.
     *
     * @param relayArgs
     *            where to write the argument to.
     * @param arg
     *            which argument to write.
     * @throws IllegalArgumentException
     *             If argument {@code arg} is not a port.
     */
    protected void processRelayPort(List<String> relayArgs, final String arg) throws IllegalArgumentException {
        if ((arg == null) || "".equals(arg)) {
            relayArgs.clear();
            return;
        }
        else if (!isPort(arg)) {
            throw new IllegalArgumentException(RELAY_PORT);
        }
        logInfo("added port \"" + arg + "\" to relay arguments");
        relayArgs.set(0, arg);
    }

    /**
     * Checks if the given argument {@code arg} is a valid IP and writes it to
     * {@code stunArgs} in index 0.
     *
     * @param stunArgs
     *            where to write the argument to.
     * @param arg
     *            which argument to write.
     * @throws IllegalArgumentException
     *             If argument {@code arg} is null, empty or not an IP.
     */
    protected void processStunFirstIP(List<String> stunArgs, final String arg) throws IllegalArgumentException {
        if ((arg == null) || "".equals(arg) || !isIP(arg)) {
            throw new IllegalArgumentException(STUN_FIRST_IP);
        }
        logInfo("added first IP \"" + arg + "\" to stun arguments");
        stunArgs.set(0, arg);
    }

    /**
     * Checks if the given argument {@code arg} is a valid IP and writes it to
     * {@code stunArgs} in index 1.
     *
     * @param stunArgs
     *            where to write the argument to.
     * @param arg
     *            which argument to write.
     * @throws IllegalArgumentException
     *             If argument {@code arg} is null, empty or not an IP.
     */
    protected void processStunSecondIP(List<String> stunArgs, final String arg) throws IllegalArgumentException {
        if ((arg == null) || "".equals(arg) || !isIP(arg)) {
            throw new IllegalArgumentException(STUN_SECOND_IP);
        }
        logInfo("added second IP \"" + arg + "\" to stun arguments");
        stunArgs.set(1, arg);
    }

    /**
     * @return the string "StunFirstIP"
     */
    public static String getStunFirstIP() {
        return STUN_FIRST_IP;
    }

    /**
     * @return the string "StunSecondIP"
     */
    public static String getStunSecondIP() {
        return STUN_SECOND_IP;
    }

    /**
     * @return the string "RelayPort"
     */
    public static String getRelayPort() {
        return RELAY_PORT;
    }

    /**
     * @return the string "MediatorPort"
     */
    public static String getMediatorPort() {
        return MEDIATOR_PORT;
    }

    /**
     * @return the string "MediatorIteration"
     */
    public static String getMediatorIteration() {
        return MEDIATOR_ITERATION;
    }

    /**
     * @return the string "MediatorLifeTime"
     */
    public static String getMediatorLifeTime() {
        return MEDIATOR_LIFETIME;
    }
}
