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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import de.fhkn.in.uce.connectivitymanager.NATTraversalTechniqueMock;
import de.fhkn.in.uce.connectivitymanager.selector.decisiontree.NATTraversalRule;
import de.fhkn.in.uce.plugininterface.NATFeatureRealization;
import de.fhkn.in.uce.plugininterface.NATSituation;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;

public class TestNATTraversalRegistryImpl {

    private final NATTraversalRegistry registry = NATTraversalRegistryImpl.getInstance();

    /**
     * Default constructor.
     */
    public TestNATTraversalRegistryImpl() {
        super();
    }

    @Test(expected = NATTraversalTechniqueNotFoundException.class)
    public void testNoNATTraversalTechniqueFound() throws NATTraversalTechniqueNotFoundException {
        this.registry.getNATTraversalTechniqueByName("This technique not exists!");
    }

    @Test
    public void testGetAllSupportedNATTraversalTechniques() {
        final List<NATTraversalTechnique> expectedResult = new ArrayList<NATTraversalTechnique>();
        expectedResult.add(new NATTraversalTechniqueMock("DirectConnection", 3, true));
        expectedResult.add(new NATTraversalTechniqueMock("Reversal", 3, true));
        expectedResult.add(new NATTraversalTechniqueMock("HolePunching", 3, true));
        expectedResult.add(new NATTraversalTechniqueMock("Relaying", 7, false));
        final List<NATTraversalTechnique> actualResult = this.registry.getAllSupportedNATTraversalTechniques();

        assertTrue(this.containsAll(expectedResult, actualResult));
    }

    @Test
    public void testGetDirectConnectionTechnique() throws Exception {
        final NATTraversalTechnique expectedResult = new NATTraversalTechniqueMock("DirectConnection", 3, true);
        final NATTraversalTechnique actualResult = this.registry.getNATTraversalTechniqueByName("DirectConnection");

        assertEquals("DirectConnection is requested.", expectedResult, actualResult);
    }

    // @Test
    // commented out until dt parameters are correct
    public void testGetRulesForDecisionTree() {
        final List<NATTraversalTechnique> natTravListDV = new ArrayList<NATTraversalTechnique>();
        natTravListDV.add(new NATTraversalTechniqueMock("DirectConnection", 3, true));
        final List<NATTraversalTechnique> natTravListCR = new ArrayList<NATTraversalTechnique>();
        natTravListCR.add(new NATTraversalTechniqueMock("Reversal", 3, true));
        final List<NATTraversalTechnique> natTravListDVCR = new ArrayList<NATTraversalTechnique>();
        natTravListDVCR.add(new NATTraversalTechniqueMock("DirectConnection", 3, true));
        natTravListDVCR.add(new NATTraversalTechniqueMock("Reversal", 3, true));
        final Set<NATTraversalRule> expectedResult = new HashSet<NATTraversalRule>();

        expectedResult.add(new NATTraversalRule(new NATSituation(NATFeatureRealization.NOT_REALIZED,
                NATFeatureRealization.NOT_REALIZED, NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT), natTravListDVCR));
        expectedResult.add(new NATTraversalRule(new NATSituation(NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT, NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT), natTravListDVCR));
        expectedResult.add(new NATTraversalRule(new NATSituation(NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ADDRESS_DEPENDENT, NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT), natTravListDV));
        expectedResult.add(new NATTraversalRule(new NATSituation(NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ADDRESS_AND_PORT_DEPENDENT, NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT), natTravListDV));
        expectedResult.add(new NATTraversalRule(new NATSituation(NATFeatureRealization.DONT_CARE,
                NATFeatureRealization.DONT_CARE, NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT), natTravListDV));
        expectedResult.add(new NATTraversalRule(new NATSituation(NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT, NATFeatureRealization.NOT_REALIZED,
                NATFeatureRealization.NOT_REALIZED), natTravListDVCR));
        expectedResult.add(new NATTraversalRule(new NATSituation(NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ADDRESS_DEPENDENT, NATFeatureRealization.NOT_REALIZED,
                NATFeatureRealization.NOT_REALIZED), natTravListDV));
        expectedResult.add(new NATTraversalRule(new NATSituation(NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ADDRESS_AND_PORT_DEPENDENT, NATFeatureRealization.NOT_REALIZED,
                NATFeatureRealization.NOT_REALIZED), natTravListDV));
        expectedResult.add(new NATTraversalRule(new NATSituation(NATFeatureRealization.CONNECTION_DEPENDENT,
                NATFeatureRealization.CONNECTION_DEPENDENT, NATFeatureRealization.NOT_REALIZED,
                NATFeatureRealization.NOT_REALIZED), natTravListDV));

        expectedResult.add(new NATTraversalRule(new NATSituation(NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT, NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ADDRESS_DEPENDENT), natTravListCR));
        expectedResult.add(new NATTraversalRule(new NATSituation(NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT, NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ADDRESS_AND_PORT_DEPENDENT), natTravListCR));
        expectedResult.add(new NATTraversalRule(new NATSituation(NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT, NATFeatureRealization.CONNECTION_DEPENDENT,
                NATFeatureRealization.CONNECTION_DEPENDENT), natTravListCR));
        expectedResult.add(new NATTraversalRule(new NATSituation(NATFeatureRealization.NOT_REALIZED,
                NATFeatureRealization.NOT_REALIZED, NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ADDRESS_DEPENDENT), natTravListCR));
        expectedResult.add(new NATTraversalRule(new NATSituation(NATFeatureRealization.NOT_REALIZED,
                NATFeatureRealization.NOT_REALIZED, NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ADDRESS_AND_PORT_DEPENDENT), natTravListCR));
        expectedResult.add(new NATTraversalRule(new NATSituation(NATFeatureRealization.NOT_REALIZED,
                NATFeatureRealization.NOT_REALIZED, NATFeatureRealization.CONNECTION_DEPENDENT,
                NATFeatureRealization.CONNECTION_DEPENDENT), natTravListCR));

        expectedResult.add(new NATTraversalRule(new NATSituation(NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT, NATFeatureRealization.DONT_CARE,
                NATFeatureRealization.DONT_CARE), natTravListCR));

        expectedResult.add(new NATTraversalRule(new NATSituation(NATFeatureRealization.NOT_REALIZED,
                NATFeatureRealization.NOT_REALIZED, NATFeatureRealization.DONT_CARE, NATFeatureRealization.DONT_CARE),
                natTravListCR));

        Set<NATTraversalRule> actualResult = this.registry.getRulesForDecisionTreeLearning();

        assertTrue("The elements in the set should be the equal.", this.containsAll(expectedResult, actualResult));
    }

    @SuppressWarnings("rawtypes")
    private boolean containsAll(final Collection col1, final Collection col2) {
        int count = 0;
        for (Object elem1 : col1) {
            for (Object elem2 : col2) {
                if (elem1.equals(elem2)) {
                    count++;
                }
            }
        }

        return count == col1.size() && count == col2.size();
    }

    @Test
    public void testGetAllFallbackTechniques() {
        final List<NATTraversalTechnique> expectedResult = new ArrayList<NATTraversalTechnique>();
        expectedResult.add(new NATTraversalTechniqueMock("Relaying", 6, false));

        final List<NATTraversalTechnique> actualResult = this.registry.getAllFallbackTechniques();

        assertTrue("All fallback techniques should be returned.", this.containsAll(expectedResult, actualResult));
    }
}
