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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.jcip.annotations.ThreadSafe;
import de.fhkn.in.uce.plugininterface.NATSituation;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;
import de.fhkn.in.uce.plugininterface.util.NATTraversalTechniqueUtil;

/**
 * This implementation of {@link DecisionTree} is not a real decision tree. It
 * uses a {@link Map} with {@link NATSituation} as key and a list of
 * {@link NATTraversalTechnique} as value. Because of this it ensures fast
 * access. The provided {@link NATTraversalRule}s are used to fill the map.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
@ThreadSafe
public final class MapDT implements DecisionTree {
    private final ConcurrentMap<NATSituation, List<NATTraversalTechnique>> data;

    /**
     * Creates a {@link MapDT}.
     */
    public MapDT() {
        this.data = new ConcurrentHashMap<NATSituation, List<NATTraversalTechnique>>();
    }

    @Override
    public void buildDecisionTree(final Set<NATTraversalRule> rules) {
        synchronized (this.data) {
            this.data.clear();
            for (NATTraversalRule rule : rules) {
                if (this.data.containsKey(rule.getNATSituation())) {
                    final List<NATTraversalTechnique> oldValue = this.data.get(rule.getNATSituation());
                    final List<NATTraversalTechnique> newValue = this.mergeLists(oldValue,
                            rule.getAppropriateTraversalTechniques());
                    this.data.put(rule.getNATSituation(), newValue);
                } else {
                    this.data.put(rule.getNATSituation(), rule.getAppropriateTraversalTechniques());
                }
            }
        }
    }

    private List<NATTraversalTechnique> mergeLists(final List<NATTraversalTechnique> list1,
            final List<NATTraversalTechnique> list2) {
        final List<NATTraversalTechnique> result = new ArrayList<NATTraversalTechnique>();
        result.addAll(list1);
        for (NATTraversalTechnique elemList2 : list2) {
            if (!result.contains(elemList2)) {
                result.add(elemList2);
            }
        }
        return result;
    }

    @Override
    public List<NATTraversalTechnique> getAppropriateNATTraversalTechniques(final NATSituation natSituation) {
        List<NATTraversalTechnique> result = this.getTraversalTechniquesForNatSituation(natSituation);
        if (result == null) {
            result = new ArrayList<NATTraversalTechnique>();
        }
        return result;
    }

    private List<NATTraversalTechnique> getTraversalTechniquesForNatSituation(final NATSituation natSituation) {
        final List<NATTraversalTechnique> result = new ArrayList<NATTraversalTechnique>();
        final NATTraversalTechniqueUtil util = NATTraversalTechniqueUtil.getInstance();
        final Set<NATSituation> withoutWildcards = util.resolveWildcards(natSituation);
        for (final NATSituation withoutWildcard : withoutWildcards) {
            final List<NATTraversalTechnique> travTechs = this.data.get(withoutWildcard);
            if (null != travTechs) {
                for (final NATTraversalTechnique travTech : travTechs) {
                    if (!result.contains(travTech)) {
                        result.add(travTech);
                    }
                }
            }
        }
        return Collections.unmodifiableList(result);
    }
}
