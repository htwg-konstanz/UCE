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
package de.fhkn.in.uce.connectivitymanager.connection.configuration;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public final class TestConnectionConfigurationImpl {
    private ConnectionConfiguration configuration;
    private ConnectionDuration duration = ConnectionDuration.SHORT;
    private ServiceClass serviceClass = ServiceClass.EXPEDITED_FORWARDING;
    private boolean requiresDirectConnection = true;

    @Before
    public void setUp() {
        this.configuration = new ConnectionConfigurationImpl(duration, serviceClass, requiresDirectConnection);
    }

    @Test
    public void testGetConnectionDuration() {
        assertEquals(this.duration, this.configuration.getConnectionDuration());
    }

    @Test
    public void testGetServiceClass() {
        assertEquals(this.serviceClass, this.configuration.getServiceClass());
    }

    @Test
    public void testRequiresDirectConnection() {
        assertEquals(this.requiresDirectConnection, this.configuration.directConnectionRequired());
    }
}
