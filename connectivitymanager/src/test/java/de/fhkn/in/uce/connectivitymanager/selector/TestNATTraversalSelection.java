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
package de.fhkn.in.uce.connectivitymanager.selector;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.fhkn.in.uce.connectivitymanager.NATTraversalTechniqueMock;
import de.fhkn.in.uce.connectivitymanager.registry.NATTraversalRegistryImpl;
import de.fhkn.in.uce.connectivitymanager.selector.decisiontree.DecisionTree;
import de.fhkn.in.uce.connectivitymanager.selector.decisiontree.MapDT;
import de.fhkn.in.uce.connectivitymanager.selector.strategy.ConnectionSetupTimeSelection;
import de.fhkn.in.uce.plugininterface.NATSituation;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;

public class TestNATTraversalSelection {

    List<NATTraversalTechnique> actualResult = Collections.emptyList();
    List<NATTraversalTechnique> expectedResult = Collections.emptyList();
    NATTraversalSelection selector;

    @Before
    public void setUp() {
        this.actualResult = new ArrayList<NATTraversalTechnique>();
        this.expectedResult = new ArrayList<NATTraversalTechnique>();
        DecisionTree decisionTree = new MapDT();
        decisionTree.buildDecisionTree(NATTraversalRegistryImpl.getInstance().getRulesForDecisionTreeLearning());
        this.selector = new ConnectionSetupTimeSelection(NATTraversalRegistryImpl.getInstance(), decisionTree);
    }

    @Test
    public void testUnknownNATSituation() {
        this.expectedResult.add(new NATTraversalTechniqueMock("DirectConnection", 3, true));
        this.expectedResult.add(new NATTraversalTechniqueMock("Reversal", 3, true));
        this.expectedResult.add(new NATTraversalTechniqueMock("HolePunching", Integer.MAX_VALUE, true));
        this.expectedResult.add(new NATTraversalTechniqueMock("Relaying", 7, false));

        this.actualResult = this.selector.getNATTraversalTechniquesForNATSituation(new NATSituation());

        assertEquals(this.expectedResult, this.actualResult);
    }
}
