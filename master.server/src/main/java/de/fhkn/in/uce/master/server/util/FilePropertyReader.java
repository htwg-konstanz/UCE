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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.slf4j.LoggerFactory;

/**
 * This class tries to read from the properties file and parses the contents
 * to the corresponding arguments lists for
 * {@link de.fhkn.in.uce.mediator.Mediator Mediator},
 * {@link de.fhkn.in.uce.stun.server.StunServer StunServer} and
 * {@link de.fhkn.in.uce.relaying.server.RelayServer RelayServer}.
 * Furthermore it extends {@link AbstractReader} for common functions.
 *
 * @author Robert Danczak
 */
public class FilePropertyReader extends AbstractReader {


    /**
     * Creates a file property reader to read from
     * 'config/master.server.porperties' and processes them.
     */
    public FilePropertyReader() {
        super(LoggerFactory.getLogger(FilePropertyReader.class));
    }

    @Override
    public void readArguments(List<String> stunArgs, List<String> relayArgs, List<String> mediatorArgs) throws IllegalArgumentException {
        try {
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream("config/master.server.properties");

            props.load(fis);
            Enumeration<?> propEnumeration = props.propertyNames();

            while (propEnumeration.hasMoreElements()) {
                try {
                    String key = propEnumeration.nextElement().toString();
                    if (key.equals(STUN_FIRST_IP)) {
                        String value = props.getProperty(key);
                        processStunFirstIP(stunArgs, value);
                    }
                    else if (key.equals(STUN_SECOND_IP)) {
                        String value = props.getProperty(key);
                        processStunSecondIP(stunArgs, value);
                    }
                    else if (key.equals(RELAY_PORT)) {
                        String value = props.getProperty(key);
                        processRelayPort(relayArgs, value);
                    }
                    else if (key.equals(MEDIATOR_PORT)) {
                        String value = props.getProperty(key);
                        processMediatorPort(mediatorArgs, value);
                    }
                    else if (key.equals(MEDIATOR_ITERATION)) {
                        String value = props.getProperty(key);
                        processMediatorIteration(mediatorArgs, value);
                    }
                    else if (key.equals(MEDIATOR_LIFETIME)) {
                        String value = props.getProperty(key);
                        processMediatorLifeTime(mediatorArgs, value);
                    }
                    else {
                        logInfo("Key \"" + key + "\" not recognized");
                    }
                } catch (IllegalArgumentException e) {
                    logError("FilePropertyReader: Argument \"" + e.getMessage() + "\" is empty or invalid");
                    continue;
                }
            }
            fis.close();
        } catch (IOException e) {
            logError("Config file not found!");
        }

    }
}
