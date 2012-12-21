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
package de.fhkn.in.uce.connectivitymanager.security.hmac;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.Key;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.junit.Before;
import org.junit.Test;

public final class TestHmacStreams {
    private static final int PORT = 55973;
    private Key sharedKey = null;

    @Before
    public void setUp() throws Exception {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        DESKeySpec keySpec = new DESKeySpec("vollgeheimerschluesselwirdhiergeneriert".getBytes());
        this.sharedKey = keyFactory.generateSecret(keySpec);
    }

    @Test
    public void testWriteInt() throws Exception {
        final Integer numberToWrite = 42;
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<Integer> serverResult = executor.submit(this.getServerTaskReadInt());
        executor.execute(this.getClientTaskWriteInt(numberToWrite));
        final Integer actualResult = serverResult.get(60, TimeUnit.SECONDS);
        executor.shutdownNow();

        assertEquals(new Integer(numberToWrite), actualResult);
    }

    @Test
    public void testWriteByte() throws Exception {
        final byte[] byteToWrite = ByteBuffer.allocate(4).putInt(42).array();
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<ByteBuffer> serverResult = executor.submit(this.getServerTaskReadByte());
        executor.execute(this.getClientTaskWriteByte(byteToWrite));
        final ByteBuffer actualResult = serverResult.get(60, TimeUnit.SECONDS);
        executor.shutdownNow();

        assertTrue(Arrays.equals(byteToWrite, actualResult.array()));
    }

    @Test
    public void testWriteByteWithOffset() throws Exception {
        final byte[] byteToWrite = ByteBuffer.allocate(4).putInt(42).array();
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<ByteBuffer> serverResult = executor.submit(this.getServerTaskReadByteWithOffset());
        executor.execute(this.getClientTaskWriteByteWithOffset(byteToWrite));
        final ByteBuffer actualResult = serverResult.get(60, TimeUnit.SECONDS);
        executor.shutdownNow();

        assertTrue(Arrays.equals(byteToWrite, actualResult.array()));
    }

    private Callable<Integer> getServerTaskReadInt() {
        return new Callable<Integer>() {

            @Override
            public Integer call() {
                int result = -1;
                ServerSocket serverSocket;
                try {
                    serverSocket = new ServerSocket(PORT);
                    final Socket socket = serverSocket.accept();
                    final HmacInputStream hmacIn = new HmacInputStream(socket.getInputStream(), sharedKey);
                    result = hmacIn.read();
                    hmacIn.close();
                    serverSocket.close();
                } catch (Exception e) {
                    throw new RuntimeException("Exception on server-side.", e);
                }
                return result;
            }
        };
    }

    private Callable<ByteBuffer> getServerTaskReadByte() {
        return new Callable<ByteBuffer>() {

            @Override
            public ByteBuffer call() {
                byte[] tmp = new byte[4];
                ServerSocket serverSocket;
                try {
                    serverSocket = new ServerSocket(PORT);
                    final Socket socket = serverSocket.accept();
                    final HmacInputStream hmacIn = new HmacInputStream(socket.getInputStream(), sharedKey);
                    final int read = hmacIn.read(tmp);
                    hmacIn.close();
                    serverSocket.close();
                } catch (Exception e) {
                    throw new RuntimeException("Exception on server-side.", e);
                }
                return ByteBuffer.wrap(tmp);
            }
        };
    }

    private Callable<ByteBuffer> getServerTaskReadByteWithOffset() {
        return new Callable<ByteBuffer>() {

            @Override
            public ByteBuffer call() {
                byte[] tmp = new byte[4];
                ServerSocket serverSocket;
                try {
                    serverSocket = new ServerSocket(PORT);
                    final Socket socket = serverSocket.accept();
                    final HmacInputStream hmacIn = new HmacInputStream(socket.getInputStream(), sharedKey);
                    final int read = hmacIn.read(tmp, 0, tmp.length);
                    hmacIn.close();
                    serverSocket.close();
                } catch (Exception e) {
                    throw new RuntimeException("Exception on server-side.", e);
                }
                return ByteBuffer.wrap(tmp);
            }
        };
    }

    private Runnable getClientTaskWriteInt(final int toWrite) {
        return new Runnable() {

            @Override
            public void run() {
                try {
                    final Socket socket = new Socket("localhost", PORT);
                    final HmacOutputStream hmacOut = new HmacOutputStream(socket.getOutputStream(), sharedKey);
                    hmacOut.write(toWrite);
                    hmacOut.flush();
                    hmacOut.close();
                    socket.close();
                } catch (Exception e) {
                    throw new RuntimeException("Exception on client-side.", e);
                }
            }
        };
    }

    private Runnable getClientTaskWriteByte(final byte[] toWrite) {
        return new Runnable() {

            @Override
            public void run() {
                try {
                    final Socket socket = new Socket("localhost", PORT);
                    final HmacOutputStream hmacOut = new HmacOutputStream(socket.getOutputStream(), sharedKey);
                    hmacOut.write(toWrite);
                    hmacOut.flush();
                    hmacOut.close();
                    socket.close();
                } catch (Exception e) {
                    throw new RuntimeException("Exception on client-side.", e);
                }
            }
        };
    }

    private Runnable getClientTaskWriteByteWithOffset(final byte[] toWrite) {
        return new Runnable() {

            @Override
            public void run() {
                try {
                    final Socket socket = new Socket("localhost", PORT);
                    final HmacOutputStream hmacOut = new HmacOutputStream(socket.getOutputStream(), sharedKey);
                    hmacOut.write(toWrite, 0, toWrite.length);
                    hmacOut.flush();
                    hmacOut.close();
                    socket.close();
                } catch (Exception e) {
                    throw new RuntimeException("Exception on client-side.", e);
                }
            }
        };
    }
}
