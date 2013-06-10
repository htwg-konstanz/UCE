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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import de.fhkn.in.uce.connectivitymanager.registry.NATTraversalRegistry;
import de.fhkn.in.uce.connectivitymanager.selector.NATTraversalSelection;
import de.fhkn.in.uce.connectivitymanager.selector.decisiontree.DecisionTree;
import de.fhkn.in.uce.connectivitymanager.selector.decisiontree.MapDT;
import de.fhkn.in.uce.connectivitymanager.selector.decisiontree.NATTraversalRule;
import de.fhkn.in.uce.connectivitymanager.selector.weighting.ConnectionSetupTimeComparator;
import de.fhkn.in.uce.plugininterface.NATSituation;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;

public final class ConnectionSetupTimeSelection implements NATTraversalSelection {
    // tzn: there is no logging here curently
    //private final Logger logger = LoggerFactory.getLogger(ConnectionSetupTimeSelection.class);
    private final DecisionTree decisionTree;
    private final Comparator<NATTraversalTechnique> comparator;
    private final NATTraversalRegistry registry;

    public ConnectionSetupTimeSelection(final NATTraversalRegistry natTraversalRegistry) {
        this.comparator = ConnectionSetupTimeComparator.getInstance();
        this.registry = natTraversalRegistry;
        this.decisionTree = this.getInitializedDecisionTree();
    }

    public ConnectionSetupTimeSelection(final NATTraversalRegistry registry, final DecisionTree initializedDecisionTree) {
        this.comparator = ConnectionSetupTimeComparator.getInstance();
        this.registry = registry;
        this.decisionTree = initializedDecisionTree;
    }

    private DecisionTree getInitializedDecisionTree() {
        DecisionTree result = new MapDT();
        this.initializeDecisionTree(result);
        return result;
    }

    // private DecisionTree chooseDecisionTree() {
    // DecisionTree decisionTree;
    // if (this.registry.hasOnlyFallbackTechniques()) {
    // decisionTree = FallbackDecisionTree.getInstance();
    // } else {
    // decisionTree = QuickdtDecisionTree.getInstance();
    // }
    //
    // return decisionTree;
    // }

    private void initializeDecisionTree(final DecisionTree decisionTree) {
        final Set<NATTraversalRule> rulesForLearning = this.registry.getRulesForDecisionTreeLearning();
        decisionTree.buildDecisionTree(rulesForLearning);
    }

    @Override
    public List<NATTraversalTechnique> getNATTraversalTechniquesForNATSituation(final NATSituation natSituation) {
        List<NATTraversalTechnique> natTraversalTechniques = new ArrayList<NATTraversalTechnique>();
        // if (!this.isUnknownNATSituation(natSituation)) {
        natTraversalTechniques.addAll(this.determineAppropriateNATTraversalTechniques(natSituation));
        // natTraversalTechniques.addAll(this.registry.getAllFallbackTechniques());
        natTraversalTechniques = this.sortNATTraversalTechniquesByConnectionSetupTime(natTraversalTechniques);
        // } else {
        // natTraversalTechniques = this.getAllTraversalTechniquesInOrder();
        // }
        return Collections.unmodifiableList(natTraversalTechniques);
    }

    // private boolean isUnknownNATSituation(final NATSituation natSituation) {
    // NATBehavior client = natSituation.getClientNATBehavior();
    // NATBehavior service = natSituation.getServiceNATBehavior();
    //
    // return isUnknownNATBehavior(client) && isUnknownNATBehavior(service);
    // }
    //
    // private boolean isUnknownNATBehavior(final NATBehavior natBehavior) {
    // boolean result = true;
    // for (final NATFeature feature : natBehavior.getNATFeatures()) {
    // if
    // (!natBehavior.getFeatureRealization(feature).equals(NATFeatureRealization.UNKNOWN))
    // {
    // result &= false;
    // break;
    // }
    // }
    //
    // return result;
    // }

    private List<NATTraversalTechnique> determineAppropriateNATTraversalTechniques(final NATSituation natSituation) {
        List<NATTraversalTechnique> result = new ArrayList<NATTraversalTechnique>();
        result.addAll(decisionTree.getAppropriateNATTraversalTechniques(natSituation));
        return result;
    }

    private List<NATTraversalTechnique> sortNATTraversalTechniquesByConnectionSetupTime(
            final List<NATTraversalTechnique> unorderedTechniques) {
        final List<NATTraversalTechnique> modifiableList = new ArrayList<NATTraversalTechnique>(unorderedTechniques);
        Collections.sort(modifiableList, this.comparator);
        return Collections.unmodifiableList(modifiableList);
    }

    // private List<NATTraversalTechnique> getAllTraversalTechniquesInOrder() {
    // final List<NATTraversalTechnique> allSupportedTraversalTechniques =
    // this.registry
    // .getAllSupportedNATTraversalTechniques();
    // return
    // this.sortNATTraversalTechniquesByConnectionSetupTime(allSupportedTraversalTechniques);
    // }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((comparator == null) ? 0 : comparator.hashCode());
        result = (prime * result) + ((decisionTree == null) ? 0 : decisionTree.hashCode());
        result = (prime * result) + ((registry == null) ? 0 : registry.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ConnectionSetupTimeSelection other = (ConnectionSetupTimeSelection) obj;
        if (comparator == null) {
            if (other.comparator != null) {
                return false;
            }
        } else if (!comparator.equals(other.comparator)) {
            return false;
        }
        if (decisionTree == null) {
            if (other.decisionTree != null) {
                return false;
            }
        } else if (!decisionTree.equals(other.decisionTree)) {
            return false;
        }
        if (registry == null) {
            if (other.registry != null) {
                return false;
            }
        } else if (!registry.equals(other.registry)) {
            return false;
        }
        return true;
    }
}
