/*
 * Copyright (c) 2012 Thomas Zink,
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
/**
 * 
 */
package de.fhkn.in.uce.core.socketlistener;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ServerSocketFactory;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.fhkn.in.uce.core.test.ThreadedExceptionRunner;

/**
 * @author thomas zink, daniel maier
 * 
 */
@RunWith(value = ThreadedExceptionRunner.class)
public class SocketListenerTest {

    /**
     * Test method for {@link de.fhkn.in.net.SocketListener#run()}.
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testRun() throws IOException, InterruptedException {
        // try to get get free port
        ServerSocket dummy = new ServerSocket(0);
        int listenerPort = dummy.getLocalPort();
        dummy.close();
        SocketTaskFactory tf = mock(SocketTaskFactory.class);
        Runnable task = mock(Runnable.class);
        when(tf.getTask(isA(Socket.class))).thenReturn(task);
        SocketListener lt = new SocketListener(listenerPort, ServerSocketFactory.getDefault(),
                Executors.newCachedThreadPool(), tf);
        lt.start();

        Socket s = new Socket();
        s.connect(new InetSocketAddress(listenerPort));
        Thread.sleep(200);
        lt.interrupt();
        lt.join();
        verify(task).run();
    }

    /**
     * Test method for {@link de.fhkn.in.net.SocketListener#interrupt()}.
     */
    @Test
    public void testInterrupt() throws IOException, InterruptedException {
        // try to get get free port
        ServerSocket dummy = new ServerSocket(0);
        int listenerPort = dummy.getLocalPort();
        dummy.close();
        SocketListener lt = new SocketListener(listenerPort, ServerSocketFactory.getDefault(),
                Executors.newCachedThreadPool(), new SocketTaskFactory() {
                    public Runnable getTask(Socket s) {
                        return null;
                    }
                });
        lt.start();
        Thread.sleep(200);
        lt.interrupt();
        lt.join();

        Socket s = new Socket();
        try {
            s.connect(new InetSocketAddress(listenerPort));
            Assert.fail("Got connection, but listener should be terminated");
        } catch (IOException e) {
            // thread is terminated.
        }
    }

    /**
     * Test method for
     * {@link de.fhkn.in.net.SocketListener#SocketListener(int, javax.net.ServerSocketFactory, java.util.concurrent.ExecutorService, de.fhkn.in.net.SocketTaskFactory)}
     * .
     * 
     * @throws IOException
     */
    @Test(expected = NullPointerException.class)
    public void testSocketListenerNullExecutor() throws IOException {
        int port = 0;
        ServerSocketFactory servFactory = ServerSocketFactory.getDefault();
        ExecutorService executor = null;
        SocketTaskFactory taskFactory = new SocketTaskFactory() {
            @Override
            public Runnable getTask(Socket s) throws IOException {
                return null;
            }
        };

        new SocketListener(port, servFactory, executor, taskFactory);
    }

    @Test(expected = NullPointerException.class)
    public void testSocketListenerNullTasks() throws IOException {
        int port = 0;
        ServerSocketFactory servFactory = ServerSocketFactory.getDefault();
        ExecutorService executor = Executors.newCachedThreadPool();
        SocketTaskFactory taskFactory = null;

        new SocketListener(port, servFactory, executor, taskFactory);
    }

    @Test(expected = NullPointerException.class)
    public void testSocketListenerNullExecutorTasks() throws IOException {
        int port = 0;
        ServerSocketFactory servFactory = ServerSocketFactory.getDefault();
        ExecutorService executor = null;
        SocketTaskFactory taskFactory = null;

        new SocketListener(port, servFactory, executor, taskFactory);
    }

    @Test(expected = NullPointerException.class)
    public void testSocketListenerNullServerExecutorTasks() throws IOException {
        int port = 0;
        ServerSocketFactory servFactory = null;
        ExecutorService executor = null;
        SocketTaskFactory taskFactory = null;

        new SocketListener(port, servFactory, executor, taskFactory);
    }
}
