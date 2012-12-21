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
package de.fhkn.in.uce.connectivitymanager.selector.strategy;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.fhkn.in.uce.connectivitymanager.NATTraversalTechniqueMock;
import de.fhkn.in.uce.connectivitymanager.TestUtilImpl;
import de.fhkn.in.uce.connectivitymanager.registry.NATTraversalRegistry;
import de.fhkn.in.uce.connectivitymanager.registry.NATTraversalRegistryImpl;
import de.fhkn.in.uce.connectivitymanager.selector.NATTraversalSelection;
import de.fhkn.in.uce.connectivitymanager.selector.decisiontree.DecisionTree;
import de.fhkn.in.uce.connectivitymanager.selector.decisiontree.MapDT;
import de.fhkn.in.uce.plugininterface.NATFeatureRealization;
import de.fhkn.in.uce.plugininterface.NATSituation;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;

public final class TestConnectionSetupTimeSelection {
    private NATTraversalSelection selection;
    private NATTraversalRegistry registry;
    private DecisionTree initializedDecisionTree;

    @Before
    public void setUp() {
        this.registry = NATTraversalRegistryImpl.getInstance();
        this.initializedDecisionTree = new MapDT();
        this.initializedDecisionTree.buildDecisionTree(TestUtilImpl.getInstance().getRulesForDecisionTreeLearning());
        this.selection = new ConnectionSetupTimeSelection(this.registry, this.initializedDecisionTree);
    }

    @Test
    public void testCreateSelection() {
        NATTraversalSelection actual = new ConnectionSetupTimeSelection(this.registry, this.initializedDecisionTree);

        assertEquals(this.selection, actual);
    }

    @Test
    public void testGetNATTraversalTechniquesForNATSituation() {
        final List<NATTraversalTechnique> expected = new ArrayList<NATTraversalTechnique>();
        expected.add(new NATTraversalTechniqueMock("Relaying", 7, false));
        final NATSituation natSituation = new NATSituation(NATFeatureRealization.CONNECTION_DEPENDENT,
                NATFeatureRealization.CONNECTION_DEPENDENT, NATFeatureRealization.CONNECTION_DEPENDENT,
                NATFeatureRealization.CONNECTION_DEPENDENT);
        final List<NATTraversalTechnique> actual = this.selection
                .getNATTraversalTechniquesForNATSituation(natSituation);

        assertEquals(expected, actual);
    }
}
