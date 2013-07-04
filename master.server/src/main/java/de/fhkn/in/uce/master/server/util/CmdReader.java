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

import java.util.Arrays;
import java.util.List;

import org.slf4j.LoggerFactory;

/**
 * This class reads the given command line arguments and parses them to the
 * corresponding arguments lists for {@link de.fhkn.in.uce.mediator.Mediator Mediator},
 * {@link de.fhkn.in.uce.stun.server.StunServer StunServer} and
 * {@link de.fhkn.in.uce.relaying.server.RelayServer RelayServer}.
 * Furthermore it extends {@link AbstractReader} for common functions.
 *
 * @author Robert Danczak
 */
public class CmdReader extends AbstractReader {

    private List<String> args;

    /**
     * Creates a command line reader and processes the given args.
     *
     * @param args
     *            args to parse.
     */
    public CmdReader(final String[] args) {
        super(LoggerFactory.getLogger(CmdReader.class));
        this.args = Arrays.asList(args);
    }

    @Override
    public void readArguments(List<String> stunArgs, List<String> relayArgs, List<String> mediatorArgs) throws IllegalArgumentException {
        for (String arg : args) {
            try {
                String[] splitted = arg.split("=");
                String result = "";
                if (splitted.length > 1) {
                    result = splitted[1];
                }

                if (arg.startsWith(STUN_FIRST_IP) || arg.startsWith("-" + STUN_FIRST_IP)) {
                    processStunFirstIP(stunArgs, result);
                }
                else if (arg.startsWith(STUN_SECOND_IP) || arg.startsWith("-" + STUN_SECOND_IP)) {
                    processStunSecondIP(stunArgs, result);
                }
                else if (arg.startsWith(RELAY_PORT) || arg.startsWith("-" + RELAY_PORT)) {
                    processRelayPort(relayArgs, result);
                }
                else if (arg.startsWith(MEDIATOR_PORT) || arg.startsWith("-" + MEDIATOR_PORT)) {
                    processMediatorPort(mediatorArgs, result);
                }
                else if (arg.startsWith(MEDIATOR_ITERATION) || arg.startsWith("-" + MEDIATOR_ITERATION)) {
                    processMediatorIteration(mediatorArgs, result);
                }
                else if (arg.startsWith(MEDIATOR_LIFETIME) || arg.startsWith("-" + MEDIATOR_LIFETIME)) {
                    processMediatorLifeTime(mediatorArgs, result);
                }
                else {
                    logInfo("Argument \"" + arg + "\" not recognized");
                }
            } catch (IllegalArgumentException e) {
                logError("CmdReader: Argument \"" + e.getMessage() + "\" is empty or invalid");
                continue;
            }
        }
    }
}
