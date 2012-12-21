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
package de.fhkn.in.uce.connectivitymanager.selector.weighting;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.fhkn.in.uce.connectivitymanager.NATTraversalTechniqueMock;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;

public class TestConnectionSetupTimeComparator {

    List<NATTraversalTechnique> actualResult = Collections.emptyList();
    List<NATTraversalTechnique> expectedResult = Collections.emptyList();

    @Before
    public void setUp() {
        this.actualResult = new ArrayList<NATTraversalTechnique>();
        this.expectedResult = new ArrayList<NATTraversalTechnique>();
    }

    @Test
    public void testSortedConnectionSetupTime() {
        this.expectedResult.add(new NATTraversalTechniqueMock("ConnectionReversal", 3, true));
        this.expectedResult.add(new NATTraversalTechniqueMock("HolePunching", 5, true));
        this.expectedResult.add(new NATTraversalTechniqueMock("Relaying", 0, false));

        this.actualResult.add(new NATTraversalTechniqueMock("Relaying", 0, false));
        this.actualResult.add(new NATTraversalTechniqueMock("HolePunching", 5, true));
        this.actualResult.add(new NATTraversalTechniqueMock("ConnectionReversal", 3, true));

        Collections.sort(this.actualResult, ConnectionSetupTimeComparator.getInstance());

        assertEquals("The list has to be sorted by the ConnectionSetupTime correctly.", this.expectedResult,
                this.actualResult);
    }

    @Test
    public void testSortedConnectionSetupTimeWithFallback() {
        this.expectedResult.add(new NATTraversalTechniqueMock("ConnectionReversal", 3, true));
        this.expectedResult.add(new NATTraversalTechniqueMock("HolePunching", 5, true));
        this.expectedResult.add(new NATTraversalTechniqueMock("Relaying", 0, false));
        this.expectedResult.add(new NATTraversalTechniqueMock("Relaying2", 7, false));

        this.actualResult.add(new NATTraversalTechniqueMock("Relaying2", 7, false));
        this.actualResult.add(new NATTraversalTechniqueMock("HolePunching", 5, true));
        this.actualResult.add(new NATTraversalTechniqueMock("Relaying", 0, false));
        this.actualResult.add(new NATTraversalTechniqueMock("ConnectionReversal", 3, true));

        Collections.sort(this.actualResult, ConnectionSetupTimeComparator.getInstance());

        assertEquals("The list has to be sorted by the ConnectionSetupTime and fallback technique correctly.",
                this.expectedResult, this.actualResult);
    }
}
