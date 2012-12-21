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
package de.fhkn.in.uce.connectivitymanager.security.diffiehellman;

import static org.junit.Assert.assertEquals;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public final class TestKeyExchange {

    @Test
    public void testKeyExchange() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final KeyExchange kxClient = new KeyExchange();
        final KeyExchange kxServer = new KeyExchange();
        Future serverResult = executor.submit(this.getKXServer(kxServer));
        Future clientResult = executor.submit(this.getKXClient(kxClient));
        serverResult.get(60, TimeUnit.SECONDS);
        clientResult.get(60, TimeUnit.SECONDS);
        executor.shutdownNow();

        assertEquals(kxClient.getSharedKey("AES"), kxServer.getSharedKey("AES"));
    }

    private Runnable getKXClient(final KeyExchange kx) {
        return new Runnable() {

            @Override
            public void run() {
                try {
                    final Socket socket = new Socket("localhost", 55872);
                    kx.exchangeKeyClientRole(socket.getInputStream(), socket.getOutputStream());
                } catch (Exception e) {
                    throw new RuntimeException("Exception while key exchange.", e);
                }
            }
        };
    }

    private Runnable getKXServer(final KeyExchange kx) {
        return new Runnable() {

            @Override
            public void run() {
                try {
                    final ServerSocket serverSocket = new ServerSocket(55872);
                    final Socket socket = serverSocket.accept();
                    kx.exchangeKeyServerRole(socket.getInputStream(), socket.getOutputStream());
                } catch (Exception e) {
                    throw new RuntimeException("Exception while key exchange.", e);
                }
            }
        };
    }

}
