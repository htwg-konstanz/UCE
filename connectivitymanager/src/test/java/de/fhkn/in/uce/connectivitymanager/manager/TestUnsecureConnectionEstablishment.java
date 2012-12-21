/*
 * Copyright (c) 2012 Alexander Diener,
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
package de.fhkn.in.uce.connectivitymanager.manager;

import static org.junit.Assert.assertTrue;

import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import de.fhkn.in.uce.connectivitymanager.connection.configuration.DefaultConnectionConfiguration;
import de.fhkn.in.uce.connectivitymanager.manager.source.UnsecureSourceSideConnectionEstablishment;
import de.fhkn.in.uce.connectivitymanager.manager.target.UnsecureTargetSideConnectionEstablishment;

/**
 * For these tests, mediators are required. The mediators are not started during
 * the test. They must be publicly available.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class TestUnsecureConnectionEstablishment {
    private static final String TARGET_ID = "brian";
    private ExecutorService executor;

    @Before
    public void setUp() {
        this.executor = Executors.newFixedThreadPool(2);
    }

    /**
     * This test needs a mediator.
     * 
     * @throws Exception
     */
    @Test
    public void testEstablishConnection() throws Exception {
        Future<Socket> targetResult = this.executor.submit(new Callable<Socket>() {

            @Override
            public Socket call() throws Exception {
                final ConnectionEstablishment targetCe = new UnsecureTargetSideConnectionEstablishment();
                return targetCe.establishConnection(TARGET_ID, DefaultConnectionConfiguration.getInstance());
            }
        });

        Thread.sleep(1000);

        Future<Socket> sourceResult = this.executor.submit(new Callable<Socket>() {

            @Override
            public Socket call() throws Exception {
                final ConnectionEstablishment sourceCe = new UnsecureSourceSideConnectionEstablishment();
                return sourceCe.establishConnection(TARGET_ID, DefaultConnectionConfiguration.getInstance());
            }

        });

        final Socket sourceSocket = sourceResult.get(250, TimeUnit.SECONDS);
        final Socket targetSocket = targetResult.get(250, TimeUnit.SECONDS);

        final boolean result = sourceSocket.isConnected() && targetSocket.isConnected();

        sourceSocket.close();
        targetSocket.close();

        assertTrue(result);
    }
}
