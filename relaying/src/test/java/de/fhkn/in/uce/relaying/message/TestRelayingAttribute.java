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
package de.fhkn.in.uce.relaying.message;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;

public final class TestRelayingAttribute {
    private static final int RELAYING_ENCODED = 0x3;
    private NATTraversalTechniqueAttribute attr;

    @Before
    public void setUp() {
        this.attr = new RelayingAttribute();
    }

    @Test
    public void testGetEncoded() {
        assertEquals(RELAYING_ENCODED, this.attr.getEncoded());
    }
}
