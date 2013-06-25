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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import de.fhkn.in.uce.master.server.util.AbstractReader;
import de.fhkn.in.uce.master.server.util.SystemPropertyReader;

public class SystemPropertyReaderTest {

    List<String> stunArgs;
    List<String> relayArgs;
    List<String> mediatorArgs;

    @Before
    public void setUp() throws Exception {
        stunArgs = new ArrayList<String>();
        stunArgs.add("");
        stunArgs.add("");
        relayArgs = new ArrayList<String>();
        relayArgs.add("");
        mediatorArgs = new ArrayList<String>();
        mediatorArgs.add("");
        mediatorArgs.add("");
        mediatorArgs.add("");
    }

    /**
     * Test method for
     * {@link SystemPropertyReader#readArguments(java.util.List, java.util.List, java.util.List)}
     * with no system properties.
     */
    @Test
    public final void testReadArguments1() {
        List<String> emptyList = new ArrayList<String>();
        emptyList.add("");
        SystemPropertyReader sysPropReader = new SystemPropertyReader();
        sysPropReader.readArguments(stunArgs, relayArgs, mediatorArgs);
        assertEquals(emptyList, relayArgs);
    }

    /**
     * Test method for
     * {@link SystemPropertyReader#readArguments(java.util.List, java.util.List, java.util.List)}
     * with wrong properties.
     */
    @Test
    public final void testReadArguments2() {
        System.setProperty(AbstractReader.STUN_FIRST_IP, "wrong");

        SystemPropertyReader sysPropReader = new SystemPropertyReader();
        try {
            sysPropReader.readArguments(stunArgs, relayArgs, mediatorArgs);
        } catch(IllegalArgumentException e) {
            fail("Should not be here!");
        }

        System.clearProperty(AbstractReader.STUN_FIRST_IP);
    }

    /**
     * Test method for
     * {@link SystemPropertyReader#readArguments(java.util.List, java.util.List, java.util.List)}
     * with all system properties.
     */
    @Test
    public final void testReadArguments3() {
        List<String> relayReference = new ArrayList<String>();
        relayReference.add("10400");
        Properties props = new Properties();
        props.setProperty(AbstractReader.STUN_FIRST_IP, "127.0.0.2");
        props.setProperty(AbstractReader.STUN_SECOND_IP, "127.0.0.3");
        props.setProperty(AbstractReader.RELAY_PORT, "10400");
        props.setProperty(AbstractReader.MEDIATOR_PORT, "10401");
        props.setProperty(AbstractReader.MEDIATOR_ITERATION, "1");
        props.setProperty(AbstractReader.MEDIATOR_LIFETIME, "2");
        System.setProperties(props);
        SystemPropertyReader sysPropReader = new SystemPropertyReader();

        sysPropReader.readArguments(stunArgs, relayArgs, mediatorArgs);

        System.clearProperty(AbstractReader.STUN_FIRST_IP);
        System.clearProperty(AbstractReader.STUN_SECOND_IP);
        System.clearProperty(AbstractReader.RELAY_PORT);
        System.clearProperty(AbstractReader.MEDIATOR_PORT);
        System.clearProperty(AbstractReader.MEDIATOR_ITERATION);
        System.clearProperty(AbstractReader.MEDIATOR_LIFETIME);

        assertEquals(relayReference, relayArgs);
    }
}
