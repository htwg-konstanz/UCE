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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class FilePropertyReaderTest {

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
     * {@link FilePropertyReader#readArguments(java.util.List, java.util.List, java.util.List)}.
     */
    @Test
    public final void testReadArguments() {
        FilePropertyReader filePropReader = new FilePropertyReader();
        try {
            filePropReader.readArguments(stunArgs, relayArgs, mediatorArgs);
        } catch (IllegalArgumentException e) {
        }

    }

}
