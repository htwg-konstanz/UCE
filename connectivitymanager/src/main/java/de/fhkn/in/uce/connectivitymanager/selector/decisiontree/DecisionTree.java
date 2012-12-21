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

import java.util.List;
import java.util.Set;

import de.fhkn.in.uce.plugininterface.NATSituation;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;

/**
 * The interface defines methods to build and use a decision tree. The decision
 * tree is initialized by {@link NATTraversalRule}s which defines NAT situations
 * and the appropriate traversal techniques. To select appropriate techniques
 * for a given NAT situation the {@link DecisionTree} can be used.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public interface DecisionTree {

    /**
     * Builds up the decision tree with the given set of
     * {@link NATTraversalRule}s.
     * 
     * @param rules
     *            a set of {@link NATTraversalRule}s to build up and initialize
     *            the {@link DecisionTree}
     */
    void buildDecisionTree(final Set<NATTraversalRule> rules);

    /**
     * Returns for a given {@link NATSituation} the appropriate
     * {@link NATTraversalTechnique}s.
     * 
     * @param natSituation
     *            the current {@link NATSituation}
     * @return a list of appropriate {@link NATTraversalTechnique}s for the
     *         given {@link NATSituation}
     */
    List<NATTraversalTechnique> getAppropriateNATTraversalTechniques(final NATSituation natSituation);
}
