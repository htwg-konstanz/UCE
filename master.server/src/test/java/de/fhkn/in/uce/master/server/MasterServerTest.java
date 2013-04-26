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

import static org.junit.Assert.fail;

import org.junit.Test;

public class MasterServerTest {

    /**
     * Test method for {@link MasterServer#run()} with no args.
     */
    @Test
    public final void testRun() {
        final String argsEmpty[] = new String[0];

        MasterServer msNoArgs = new MasterServer(argsEmpty);
        try {
            msNoArgs.run();
        } catch (InterruptedException e) {
            fail("Should not be here!");
        }
    }

    /**
     * Test method for {@link MasterServer#run()} with partial args.
     */
    @Test
    public final void testRun2() {
        final String argsPartial[] = {"-StunFirstIP=127.0.0.2", "StunSecondIP=127.0.0.3"};

        MasterServer msPartialArgs = new MasterServer(argsPartial);
        try {
            msPartialArgs.run();
        } catch(InterruptedException e) {
            fail("Should not be here!");
        }
    }

    /**
     * Test method for {@link MasterServer#run()} with all args.
     */
    @Test
    public final void testRun3() {
        final String argsComplete[] = {"StunFirstIP=127.0.0.2", "-StunSecondIP=127.0.0.3", "RelayPort=14100", "-MediatorPort=14101", "MediatorIteration=1", "MediatorLifeTime=1"};

        MasterServer msSuccess = new MasterServer(argsComplete);
        try {
            msSuccess.run();

        } catch (Exception e) {
        }
    }

    /**
     * Test method for {@link MasterServer#run()} with wrong/undefined args.
     */
    @Test
    public final void testRun4() {
        final String argsComplete[] = {"", "-test=wrong", "blah"};

        MasterServer msWrong = new MasterServer(argsComplete);
        try {
            msWrong.run();

        } catch (Exception e) {
        }
    }
}
