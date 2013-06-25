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

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.fhkn.in.uce.master.server.util.AbstractReader;
import de.fhkn.in.uce.master.server.util.CmdReader;

public class CmdReaderTest {

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
     * Test method for {@link CmdReader#readArguments(java.util.List, java.util.List, java.util.List)} with no args.
     */
    @Test
    public final void testReadArguments1() {
        final String[] argsEmpty = new String[0];

        CmdReader cmdReaderNoArgs = new CmdReader(argsEmpty);
        try {
            cmdReaderNoArgs.readArguments(stunArgs, relayArgs, mediatorArgs);
        } catch (Exception e) {
            fail("Should not be here!");
        }
    }

    /**
     * Test method for {@link CmdReader#readArguments(java.util.List, java.util.List, java.util.List)} with all args.
     */
    @Test
    public final void testReadArguments2() {
        final String[] argsComplete = {AbstractReader.STUN_FIRST_IP + "=127.0.0.2",
                                       AbstractReader.STUN_SECOND_IP + "=127.0.0.3",
                                       AbstractReader.RELAY_PORT + "=14100",
                                       AbstractReader.MEDIATOR_PORT + "=14101",
                                       AbstractReader.MEDIATOR_ITERATION + "=1",
                                       AbstractReader.MEDIATOR_LIFETIME + "=1"};

        CmdReader cmdReaderCompleteArgs = new CmdReader(argsComplete);
        try {
            cmdReaderCompleteArgs.readArguments(stunArgs, relayArgs, mediatorArgs);
        } catch (Exception e) {
            fail("Should not be here!");
        }
    }

    /**
     * Test method for {@link CmdReader#readArguments(java.util.List, java.util.List, java.util.List)} with wrong/undefined args.
     */
    @Test
    public final void testReadArguments3() {
        final String[] argsUndefined = {"", "-test=wrong", "blah", AbstractReader.STUN_FIRST_IP + "=wrong"};

        CmdReader cmdReaderUndefinedArgs = new CmdReader(argsUndefined);
        try {
            cmdReaderUndefinedArgs.readArguments(stunArgs, relayArgs, mediatorArgs);
        } catch (Exception e) {
            fail("Should not be here!");
        }
    }

    /**
     * Test method for {@link CmdReader#readArguments(java.util.List, java.util.List, java.util.List)} with all args but random order.
     */
    @Test
    public final void testReadArguments4() {
        final String[] argsComplete = { AbstractReader.STUN_FIRST_IP + "=127.0.0.3",
                                        AbstractReader.STUN_SECOND_IP + "=127.0.0.2",
                                        AbstractReader.RELAY_PORT + "=15100",
                                        AbstractReader.MEDIATOR_LIFETIME + "=1",
                                        AbstractReader.MEDIATOR_PORT + "=15101",
                                        AbstractReader.MEDIATOR_ITERATION + "=1"};

        CmdReader cmdReaderCompleteArgs = new CmdReader(argsComplete);
        try {
            cmdReaderCompleteArgs.readArguments(stunArgs, relayArgs, mediatorArgs);
        } catch (Exception e) {
            fail("Should not be here!");
        }
    }
}
