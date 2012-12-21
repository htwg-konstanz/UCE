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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import de.fhkn.in.uce.connectivitymanager.connection.configuration.ConnectionConfiguration;
import de.fhkn.in.uce.connectivitymanager.connection.configuration.DefaultConnectionConfiguration;

public final class TestConnectivityManager {
    private static final String LOCALHOST_NAME = "localhost";
    private static final int PORT = 57845;
    private static final String TARGET_NAME = "sheldor";
    private ConnectivityManager sourceManager;
    private ConnectivityManager targetManager;
    private ExecutorService executor;

    @Before
    public void setUp() {
        this.executor = Executors.newFixedThreadPool(2);
        this.sourceManager = new ConnectivityManager(TARGET_NAME, new SimpleSourceCE(),
                DefaultConnectionConfiguration.getInstance());
        this.targetManager = new ConnectivityManager(TARGET_NAME, new SimpleTargetCE(),
                DefaultConnectionConfiguration.getInstance());
    }

    @Test
    public void testGetTargetName() {
        assertEquals(TARGET_NAME, this.sourceManager.getTargetId());
    }

    @Test
    public void testEstablishConenction() throws Exception {
        Future<Socket> sourceResult = this.executor.submit(new Callable<Socket>() {

            @Override
            public Socket call() throws Exception {
                return sourceManager.establishConnection();
            }
        });

        Future<Socket> targetResult = this.executor.submit(new Callable<Socket>() {

            @Override
            public Socket call() throws Exception {
                return targetManager.establishConnection();
            }
        });

        final Socket sourceSocket = sourceResult.get(60, TimeUnit.SECONDS);
        final Socket targetSocket = targetResult.get(60, TimeUnit.SECONDS);

        final boolean result = sourceSocket.isConnected() && targetSocket.isConnected();

        sourceSocket.close();
        targetSocket.close();

        assertTrue(result);
    }

    private final class SimpleSourceCE implements ConnectionEstablishment {

        @Override
        public Socket establishConnection(String targetId, ConnectionConfiguration config) {
            try {
                return new Socket(LOCALHOST_NAME, PORT);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final class SimpleTargetCE implements ConnectionEstablishment {

        @Override
        public Socket establishConnection(String targetId, ConnectionConfiguration config) {
            try {
                final ServerSocket serverSocket = new ServerSocket(PORT);
                return serverSocket.accept();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
