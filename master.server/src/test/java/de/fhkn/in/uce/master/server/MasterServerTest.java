package de.fhkn.in.uce.master.server;

import org.junit.Test;

import de.fhkn.in.uce.master.server.MasterServer;

public class MasterServerTest {

    /**
     * Test method for
     * {@link MasterServer#run(String[])} with no arguments in file, system and command line.
     */
    @Test
    public final void testRun() {
        String[] args = {""};
        MasterServer server = new MasterServer();
        server.run(args);
    }

}
