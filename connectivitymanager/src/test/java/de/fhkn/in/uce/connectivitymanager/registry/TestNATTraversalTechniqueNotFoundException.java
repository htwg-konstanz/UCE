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
package de.fhkn.in.uce.connectivitymanager.registry;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public final class TestNATTraversalTechniqueNotFoundException {
    private final String traversalTechniqueName = "TraversalTechnique";
    private final String message = "The NAT Traversal Technique named " + this.traversalTechniqueName
            + " could not be found.";
    private NATTraversalTechniqueNotFoundException e;

    @Before
    public void setUp() {
        this.e = new NATTraversalTechniqueNotFoundException(this.traversalTechniqueName);
    }

    @Test
    public void testGetTraversalTechniqueName() {
        assertEquals(this.traversalTechniqueName, this.e.getNatTraversalTechniqueName());
    }

    @Test
    public void testGetMessage() {
        assertEquals(this.message, this.e.getMessage());
    }

    @Test
    public void testToString() {
        assertEquals(this.message, this.e.toString());
    }
}
