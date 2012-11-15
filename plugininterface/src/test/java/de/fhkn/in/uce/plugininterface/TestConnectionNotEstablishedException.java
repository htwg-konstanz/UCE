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

import org.junit.Before;
import org.junit.Test;

public final class TestConnectionNotEstablishedException {
    private final String techniqueName = "A traversal technique";
    private final String message = "A useful message text";
    private final Exception cause = new Exception("The cause");
    private ConnectionNotEstablishedException e;

    @Before
    public void setUp() {
        this.e = new ConnectionNotEstablishedException(techniqueName, message, cause);
    }

    @Test
    public void testGetNATTraversalTechniqueName() {
        assertEquals("Traversal technique name must be equals.", e.getNatTraversalTechniqueName(), techniqueName);
    }

    @Test
    public void testGetCause() {
        assertEquals("Cause must be returned.", e.getCause(), cause);
    }

    @Test
    public void testGetMessage() {
        assertEquals("Message must be equals.", e.getMessage(), message);
    }
}
