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
package de.fhkn.in.uce.plugininterface.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public final class TestNATTraversalTechniqueAttribute {
    private static final int ATTRIBUTE_LENGTH = 4;
    private static final int ENCODED = 0x0;
    private NATTraversalTechniqueAttribute attr;

    @Before
    public void setUp() {
        this.attr = new NATTraversalTechniqueAttribute(ENCODED);
    }

    @Test
    public void testGetType() {
        assertEquals(NATSTUNAttributeType.NAT_TRAVERSAL_TECHNIQUE, this.attr.getType());
    }

    @Test
    public void testGetLength() {
        assertEquals(ATTRIBUTE_LENGTH, this.attr.getLength());
    }

    @Test
    public void testGetEncoded() {
        assertEquals(ENCODED, this.attr.getEncoded());
    }

    @Test
    public void testFromBytes() throws IOException {
        final byte[] encodedAsBytes = getEncodedAsBytes();
        final NATTraversalTechniqueAttribute actualResult = NATTraversalTechniqueAttribute.fromBytes(encodedAsBytes,
                null);
        assertEquals(this.attr, actualResult);
    }

    @Test
    public void testWriteTo() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.attr.writeTo(baos);
        assertTrue(Arrays.equals(this.getEncodedAsBytes(), baos.toByteArray()));
    }

    private byte[] getEncodedAsBytes() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(ENCODED);
        dos.flush();
        return baos.toByteArray();
    }
}
