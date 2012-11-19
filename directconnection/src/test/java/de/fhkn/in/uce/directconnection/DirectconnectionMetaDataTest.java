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
package de.fhkn.in.uce.directconnection;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.fhkn.in.uce.directconnection.message.DirectconnectionAttribute;
import de.fhkn.in.uce.plugininterface.NATFeatureRealization;
import de.fhkn.in.uce.plugininterface.NATSituation;
import de.fhkn.in.uce.plugininterface.NATTraversalTechniqueMetaData;
import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;

public final class DirectconnectionMetaDataTest {
    private NATTraversalTechniqueMetaData metaData;

    @Before
    public void setUp() {
        this.metaData = new Directconnection().getMetaData();
    }

    @Test
    public void testGetTraversaledNatBehavior() throws Exception {
        final Set<NATSituation> expectedResult = new HashSet<NATSituation>();
        expectedResult.add(new NATSituation(NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT, NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT));
        expectedResult.add(new NATSituation(NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ADDRESS_DEPENDENT, NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT));
        expectedResult.add(new NATSituation(NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ADDRESS_AND_PORT_DEPENDENT, NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT));
        expectedResult.add(new NATSituation(NATFeatureRealization.CONNECTION_DEPENDENT,
                NATFeatureRealization.CONNECTION_DEPENDENT, NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT));
        expectedResult.add(new NATSituation(NATFeatureRealization.NOT_REALIZED, NATFeatureRealization.NOT_REALIZED,
                NATFeatureRealization.ENDPOINT_INDEPENDENT, NATFeatureRealization.ENDPOINT_INDEPENDENT));
        expectedResult.add(new NATSituation(NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ENDPOINT_INDEPENDENT, NATFeatureRealization.NOT_REALIZED,
                NATFeatureRealization.NOT_REALIZED));
        expectedResult.add(new NATSituation(NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ADDRESS_DEPENDENT, NATFeatureRealization.NOT_REALIZED,
                NATFeatureRealization.NOT_REALIZED));
        expectedResult.add(new NATSituation(NATFeatureRealization.ENDPOINT_INDEPENDENT,
                NATFeatureRealization.ADDRESS_AND_PORT_DEPENDENT, NATFeatureRealization.NOT_REALIZED,
                NATFeatureRealization.NOT_REALIZED));
        expectedResult.add(new NATSituation(NATFeatureRealization.CONNECTION_DEPENDENT,
                NATFeatureRealization.CONNECTION_DEPENDENT, NATFeatureRealization.NOT_REALIZED,
                NATFeatureRealization.NOT_REALIZED));

        final Set<NATSituation> actualResult = this.metaData.getTraversaledNATSituations();

        assertEquals("The traversaled NAT behavior should be parsed correctly.", expectedResult, actualResult);
    }

    @Test
    public void testGetEncoded() {
        NATTraversalTechniqueAttribute attr = new DirectconnectionAttribute();
        assertEquals(attr, this.metaData.getAttribute());
    }
}
