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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.fhkn.in.uce.connectivitymanager.selector.decisiontree.NATTraversalRule;
import de.fhkn.in.uce.plugininterface.NATSituation;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;
import de.fhkn.in.uce.plugininterface.util.NATTraversalTechniqueUtil;

// TODO make thread-safe
public final class NATTraversalRegistryImpl implements NATTraversalRegistry {
    private static final NATTraversalRegistry INSTANCE = new NATTraversalRegistryImpl();
    private final PluginLoader pluginLoader = PluginLoaderImpl.getInstance();
    private final NATTraversalTechniqueUtil travTechUtil = NATTraversalTechniqueUtil.getInstance();
    private final int nubmerOfPossibleNatSituation;

    @Override
    public List<NATTraversalTechnique> getAllSupportedNATTraversalTechniques() {
        final List<NATTraversalTechnique> result = new ArrayList<NATTraversalTechnique>();
        final Iterator<NATTraversalTechnique> iterator = this.pluginLoader.getPluginIterator();
        while (iterator.hasNext()) {
            result.add(iterator.next().copy());
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public NATTraversalTechnique getNATTraversalTechniqueByName(final String name)
            throws NATTraversalTechniqueNotFoundException {
        NATTraversalTechnique result = null;
        final Iterator<NATTraversalTechnique> iterator = this.pluginLoader.getPluginIterator();
        while (iterator.hasNext()) {
            final NATTraversalTechnique travTech = iterator.next();
            if (name.equals(travTech.getMetaData().getTraversalTechniqueName())) {
                result = travTech;
                break;
            }
        }
        if (result == null) {
            throw new NATTraversalTechniqueNotFoundException(name);
        }
        return result.copy();
    }

    @Override
    public Set<NATTraversalRule> getRulesForDecisionTreeLearning() {
        return this.mergeAllRulesForSupportedNATTraversalTechniques();
    }

    private Set<NATTraversalRule> mergeAllRulesForSupportedNATTraversalTechniques() {
        final Set<NATTraversalRule> result = new HashSet<NATTraversalRule>();
        final List<NATTraversalTechnique> supportedTraversalTechniques = this.getAllSupportedNATTraversalTechniques();
        final Set<NATSituation> occuringNatSituations = this.getOccuringNATSituations(supportedTraversalTechniques);
        for (final NATSituation natSituation : occuringNatSituations) {
            final List<NATTraversalTechnique> appropriateNATTraversalTechniques = this
                    .getAppropriateNATTraversalTechniquesForNATSituation(natSituation);
            result.add(new NATTraversalRule(natSituation, appropriateNATTraversalTechniques));
        }
        return Collections.unmodifiableSet(result);
    }

    private Set<NATSituation> getOccuringNATSituations(final List<NATTraversalTechnique> traversalTachniques) {
        final Set<NATSituation> result = new HashSet<NATSituation>();
        for (final NATTraversalTechnique travTech : traversalTachniques) {
            result.addAll(travTech.getMetaData().getTraversaledNATSituations());
        }
        return Collections.unmodifiableSet(result);
    }

    private List<NATTraversalTechnique> getAppropriateNATTraversalTechniquesForNATSituation(
            final NATSituation natSituation) {
        final List<NATTraversalTechnique> result = new ArrayList<NATTraversalTechnique>();
        final List<NATTraversalTechnique> supportedTraversalTechniques = this.getAllSupportedNATTraversalTechniques();
        for (final NATTraversalTechnique natTraversalTechnique : supportedTraversalTechniques) {
            if (this.traversesNATSituation(natSituation, natTraversalTechnique)) {
                result.add(natTraversalTechnique.copy());
            }
        }
        return Collections.unmodifiableList(result);
    }

    private boolean traversesNATSituation(final NATSituation situation, final NATTraversalTechnique technique) {
        return technique.getMetaData().getTraversaledNATSituations().contains(situation);
    }

    @Override
    public List<NATTraversalTechnique> getAllFallbackTechniques() {
        List<NATTraversalTechnique> result = new ArrayList<NATTraversalTechnique>();
        List<NATTraversalTechnique> all = this.getAllSupportedNATTraversalTechniques();
        for (NATTraversalTechnique natTraversalTechnique : all) {
            if (natTraversalTechnique.getMetaData().getTraversaledNATSituations().size() == this.nubmerOfPossibleNatSituation) {
                result.add(natTraversalTechnique.copy());
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public NATTraversalTechnique getNATTraversalTechniqueByEncoding(final int encoding)
            throws NATTraversalTechniqueNotFoundException {
        NATTraversalTechnique result = null;
        final List<NATTraversalTechnique> availableTechniques = this.getAllSupportedNATTraversalTechniques();
        for (NATTraversalTechnique natTraversalTechnique : availableTechniques) {
            if (natTraversalTechnique.getMetaData().getAttribute().getEncoded() == encoding) {
                result = natTraversalTechnique;
                break;
            }
        }
        if (result != null) {
            return result;
        } else {
            throw new NATTraversalTechniqueNotFoundException(String.valueOf(encoding));
        }
    }

    // @Override
    // public boolean hasOnlyFallbackTechniques() {
    // boolean result = true;
    // List<NATTraversalTechnique> all =
    // this.getAllSupportedNATTraversalTechniques();
    // for (NATTraversalTechnique natTraversalTechnique : all) {
    // if (!natTraversalTechnique.getMetaData().isFallbackTechnique()) {
    // result = false;
    // break;
    // }
    // }
    // return result;
    // }

    private NATTraversalRegistryImpl() {
        this.nubmerOfPossibleNatSituation = this.travTechUtil.getAllPossibleNATSituations().size();
    }

    /**
     * Delivers the sole instance of the implementation of
     * {@code NATTraversalRegistryImpl}.
     *
     * @return the sole instance of the implementation of
     *         {@code NATTraversalRegistryImpl}.
     */
    public static NATTraversalRegistry getInstance() {
        return INSTANCE;
    }
}
