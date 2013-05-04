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

import org.slf4j.Logger;

import de.fhkn.in.uce.mediator.Mediator;
import de.fhkn.in.uce.relaying.server.RelayServer;
import de.fhkn.in.uce.stun.server.StunServer;

/**
 * This class reads the given command line arguments and parses them to the
 * corresponding arguments lists for {@link Mediator}, {@link StunServer} and
 * {@link RelayServer}.
 * Furthermore it extends {@link AbstractReader} for common functions.
 *
 * @author Robert Danczak
 */
public class CmdReader extends AbstractReader {

    private String[] args;

    public CmdReader(final Logger logger, final String[] args) {
        super(logger);
        this.args = args;
    }

    @Override
    public void readArguments(List<String> stunArgs, List<String> relayArgs, List<String> mediatorArgs) {
        for (String arg : args) {
            if (arg.startsWith(stunFirstIP) || arg.startsWith("-" + stunFirstIP)) {
                processStunFirstIP(stunArgs, arg);
            }
            else if (arg.startsWith(stunSecondIP) || arg.startsWith("-" + stunSecondIP)) {
                processStunSecondIP(stunArgs, arg);
            }
            else if (arg.startsWith(relayPort) || arg.startsWith("-" + relayPort)) {
                processRelayPort(relayArgs, arg);
            }
            else if (arg.startsWith(mediatorPort) || arg.startsWith("-" + mediatorPort)) {
                processMediatorPort(mediatorArgs, arg);
            }
            else if (arg.startsWith(mediatorIteration) || arg.startsWith("-" + mediatorIteration)) {
                processMediatorIteration(mediatorArgs, arg);
            }
            else if (arg.startsWith(mediatorLifeTime) || arg.startsWith("-" + mediatorLifeTime)) {
                processMediatorLifetime(mediatorArgs, arg);
            }
            else {
                logInfo("Argument \"" + arg + "\" not recognized");
            }
        }
    }
}
