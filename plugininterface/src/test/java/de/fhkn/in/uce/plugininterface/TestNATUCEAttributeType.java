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

import org.junit.Test;

import de.fhkn.in.uce.plugininterface.message.NATUCEAttributeType;

public final class TestNATUCEAttributeType {
    private final NATUCEAttributeType attribute = NATUCEAttributeType.NAT_BEHAVIOR;
    private final int encoding = 0x1000;

    @Test
    public void testEncoded() {
        assertEquals("The encoding must be " + this.encoding, this.encoding, this.attribute.encode());
    }

    @Test
    public void testDecoding() {
        assertEquals("The decoded attribute must be correct.", this.attribute,
                NATUCEAttributeType.fromEncoded(this.encoding));
    }
}
