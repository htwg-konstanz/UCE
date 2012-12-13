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
package de.fhkn.in.uce.plugininterface.util;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.fhkn.in.uce.plugininterface.NATFeatureRealization;
import de.fhkn.in.uce.plugininterface.NATSituation;

public final class TestNATTraversalTechniqueUtil {
    private static final String RESOURCE_PREFIX = "de/fhkn/in/uce/plugininterface/util/";
    private static final String RESOURCE_TRAVERSALED_BEHAVIOR = "traversaledNATBehaviorForTesting";
    private static final String RESOURCE_FAULTY_TRAVERSALED_BEHAVIOR = "faultyTraversaledNATBehaviorForTesting";
    private static final String RESOURCE_WILDCARD_TRAVERSALED_BEHAVIOR = "traversaledNATBehaviorWithWildcardsForTesting";
    private NATTraversalTechniqueUtil util;
    private Set<NATSituation> expectedResult;
    private Set<NATSituation> actualResult;

    @Before
    public void setUp() {
        this.util = NATTraversalTechniqueUtil.getInstance();
        this.expectedResult = new HashSet<NATSituation>();
        this.actualResult = new HashSet<NATSituation>();
    }

    @Test
    public void testParseNATBehavior() throws Exception {
        this.expectedResult.add(new NATSituation(NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT, NATFeatureRealization.CONNECTION_DEPENDENT,
                NATFeatureRealization.CONNECTION_DEPENDENT));
        this.expectedResult.add(new NATSituation(NATFeatureRealization.ADDRESS_DEPENDENT,
                NATFeatureRealization.ADDRESS_AND_PORT_DEPENDENT, NATFeatureRealization.NOT_REALIZED,
                NATFeatureRealization.CONNECTION_DEPENDENT));

        this.actualResult = this.util.parseNATSituations(RESOURCE_PREFIX + RESOURCE_TRAVERSALED_BEHAVIOR);

        assertEquals("The NAT behavior was not parsed correctlay.", this.expectedResult, this.actualResult);
    }

    @Test(expected = Exception.class)
    public void testParseFaultyNATBehavior() throws Exception {
        this.util.parseNATSituations(RESOURCE_PREFIX + RESOURCE_FAULTY_TRAVERSALED_BEHAVIOR);
    }

    @Test
    public void testParseNATBehaviorWithWildcards() throws Exception {
        final int actualResult = this.util.parseNATSituations(RESOURCE_PREFIX + RESOURCE_WILDCARD_TRAVERSALED_BEHAVIOR)
                .size();
        final int expectedResult = 26;

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testCountAllNATSituations() {
        final int extectedResult = 625;
        final int actualResult = this.util.getAllPossibleNATSituations().size();

        assertEquals(extectedResult, actualResult);
    }
}
