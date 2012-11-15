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
package de.fhkn.in.uce.plugininterface;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;

import de.fhkn.in.uce.plugininterface.message.NATUCEAttributeType;

public final class TestNATBehavior {
    private NATBehavior behavior;

    @Before
    public void setUp() {
        this.behavior = new NATBehavior();
    }

    @Test
    public void testGetLength() {
        assertEquals("Length must be 4.", this.behavior.getLength(), 4);
    }

    @Test
    public void testGetAttributeType() {
        assertEquals("Type must be correct.", this.behavior.getType(), NATUCEAttributeType.NAT_BEHAVIOR);
    }

    @Test
    public void testGetNATFeatures() {
        Set<NATFeature> expected = new HashSet<NATFeature>();
        expected.add(NATFeature.FILTERING);
        expected.add(NATFeature.MAPPING);

        Set<NATFeature> feature = this.behavior.getNATFeatures();

        assertTrue(feature.containsAll(expected));
    }

    @Test
    public void testNotEqualsWithNull() {
        assertFalse(this.behavior.equals(null));
    }

    @Test
    public void testNotEqualsWithObject() {
        assertFalse(this.behavior.equals(new Object()));
    }

    @Test
    public void testNotEqualsWithOtherBehavior() {
        NATBehavior otherBehavior = new NATBehavior(NATFeatureRealization.CONNECTION_DEPENDENT,
                NATFeatureRealization.CONNECTION_DEPENDENT);
        assertFalse(this.behavior.equals(otherBehavior));
    }

    @Test
    public void testDeEncoding() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        NATBehavior behavior = new NATBehavior();
        Callable<NATBehavior> socketTask = getDecoderTask();
        executor.submit(getEncoderTask(behavior));
        Future<NATBehavior> result = executor.submit(socketTask);
        NATBehavior nb = result.get();
        executor.shutdownNow();
        assertTrue(behavior.equals(nb));
    }

    private Callable<NATBehavior> getDecoderTask() throws Exception {
        return new Callable<NATBehavior>() {

            @Override
            public NATBehavior call() throws Exception {
                Socket s = null;
                try {
                    ServerSocket ss = new ServerSocket(59590);
                    s = ss.accept();
                } catch (Exception e) {
                    throw new RuntimeException("Could not start decoder task", e);
                }
                InputStream in = s.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int next = in.read();
                while (next > -1) {
                    bos.write(next);
                    next = in.read();
                }
                bos.flush();
                byte[] asBytes = bos.toByteArray();
                return NATBehavior.fromBytes(asBytes, null);
            }
        };
    }

    private Runnable getEncoderTask(final NATBehavior toWrite) throws Exception {
        return new Runnable() {

            @Override
            public void run() {
                try {
                    Socket s = new Socket("localhost", 59590);
                    toWrite.writeTo(s.getOutputStream());
                    s.close();
                } catch (Exception e) {
                    throw new RuntimeException("Could not start encoder task", e);
                }
            }
        };
    }
}
