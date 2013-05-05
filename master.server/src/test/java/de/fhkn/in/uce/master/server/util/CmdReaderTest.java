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
        final String[] argsComplete = {"StunFirstIP=127.0.0.2", "-StunSecondIP=127.0.0.3", "RelayPort=14100", "-MediatorPort=14101", "MediatorIteration=1", "MediatorLifeTime=1"};

        CmdReader cmdReaderCompleteArgs = new CmdReader(argsComplete);
        try {
            cmdReaderCompleteArgs.readArguments(stunArgs, relayArgs, mediatorArgs);
        } catch (IllegalArgumentException e) {
            fail("Should not be here!");
        }
    }

    /**
     * Test method for {@link CmdReader#readArguments(java.util.List, java.util.List, java.util.List)} with wrong/undefined args.
     */
    @Test
    public final void testReadArguments3() {
        final String[] argsUndefined = {"", "-test=wrong", "blah"};

        CmdReader cmdReaderUndefinedArgs = new CmdReader(argsUndefined);
        try {
            cmdReaderUndefinedArgs.readArguments(stunArgs, relayArgs, mediatorArgs);
        } catch (IllegalArgumentException e) {
            fail("Should not be here!");
        }
    }

    /**
     * Test method for {@link CmdReader#readArguments(java.util.List, java.util.List, java.util.List)} with all args but random order.
     */
    @Test
    public final void testReadArguments4() {
        final String[] argsComplete = { "-StunSecondIP=127.0.0.3", "StunFirstIP=127.0.0.2", "RelayPort=15100", "MediatorLifeTime=1", "-MediatorPort=15101", "MediatorIteration=1"};

        CmdReader cmdReaderCompleteArgs = new CmdReader(argsComplete);
        try {
            cmdReaderCompleteArgs.readArguments(stunArgs, relayArgs, mediatorArgs);

        } catch (IllegalArgumentException e) {
            fail("Should not be here!");
        }
    }
}
