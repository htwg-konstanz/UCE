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
package de.fhkn.in.uce.connectivitymanager.selector.decisiontree;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.fhkn.in.uce.connectivitymanager.NATTraversalTechniqueMock;
import de.fhkn.in.uce.connectivitymanager.TestUtil;
import de.fhkn.in.uce.connectivitymanager.TestUtilImpl;
import de.fhkn.in.uce.plugininterface.NATFeatureRealization;
import de.fhkn.in.uce.plugininterface.NATSituation;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;

public final class TestMapDT {
    private TestUtil util;
    private DecisionTree tree;

    @Before
    public void setUp() {
        this.util = TestUtilImpl.getInstance();
        this.tree = new MapDT();
        this.tree.buildDecisionTree(this.util.getRulesForDecisionTreeLearning());
    }

    @Test
    public void testGetAppropriateNATTraversalTechniques() {
        final NATSituation natSituation = new NATSituation(NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT, NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT);
        final List<NATTraversalTechnique> expectedResult = new ArrayList<NATTraversalTechnique>();
        expectedResult.add(new NATTraversalTechniqueMock("Reversal", 0, true));
        expectedResult.add(new NATTraversalTechniqueMock("HolePunching", 0, true));
        expectedResult.add(new NATTraversalTechniqueMock("Relaying", 0, false));
        final List<NATTraversalTechnique> actualResult = this.tree.getAppropriateNATTraversalTechniques(natSituation);
        assertTrue(this.util.compareLists(actualResult, expectedResult));
    }

    @Test
    public void testGetTravTechsForUnknownSituation() {
        final NATSituation natSituation = new NATSituation(NATFeatureRealization.DONT_CARE,
                NATFeatureRealization.DONT_CARE, NATFeatureRealization.DONT_CARE, NATFeatureRealization.DONT_CARE);
        final List<NATTraversalTechnique> expectedResult = new ArrayList<NATTraversalTechnique>();
        expectedResult.add(new NATTraversalTechniqueMock("DirectConnection", 3, true));
        expectedResult.add(new NATTraversalTechniqueMock("Reversal", 3, true));
        expectedResult.add(new NATTraversalTechniqueMock("HolePunching", Integer.MAX_VALUE, true));
        expectedResult.add(new NATTraversalTechniqueMock("Relaying", 7, false));
        final List<NATTraversalTechnique> actualResult = this.tree.getAppropriateNATTraversalTechniques(natSituation);

        assertTrue(this.containsSameElements(expectedResult, actualResult));
    }

    private boolean containsSameElements(final List<NATTraversalTechnique> list1,
            final List<NATTraversalTechnique> list2) {
        boolean result = true;
        if (list1.size() != list2.size()) {
            result = false;
        } else {
            for (NATTraversalTechnique elem1 : list1) {
                if (!list2.contains(elem1)) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }
}
