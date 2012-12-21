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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.jcip.annotations.Immutable;
import de.fhkn.in.uce.plugininterface.NATBehavior;
import de.fhkn.in.uce.plugininterface.NATFeature;
import de.fhkn.in.uce.plugininterface.NATFeatureRealization;
import de.fhkn.in.uce.plugininterface.NATSituation;

/**
 * This utility class provides functionality which is useful for NAT Traversal
 * Techniques.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
@Immutable
public final class NATTraversalTechniqueUtil {
    private static final NATTraversalTechniqueUtil INSTANCE = new NATTraversalTechniqueUtil();
    private static final String VALUE_SEPARATOR = ","; //$NON-NLS-1$

    /**
     * The method parses a resource and creates a set of {@code NATSituation}
     * objects. It can be used to provide the NAT situations which are
     * traversaled by a traversal technique. The resource has to be in a
     * specified format. Each line represents a NAT situation, which consists of
     * four values. These values has to be {@code NATFeatureRealisation} values,
     * which are ordered as follows: client mapping, client filtering, service
     * mapping, service filtering. As separator ',' is used.
     * <p>
     * Example: ADDRESS_DEPENDENT,ADDRESS_AND_PORT_DEPENDENT,NOT_REALIZED,
     * CONNECTION_DEPENDENT
     * </p>
     * 
     * @param resourceName
     *            The name of the resource with the NAT behavior data to parse.
     *            The resource file must be available in the classpath.
     * @return a set of {@code NATSituation}.
     * @throws Exception
     *             If the resource could not be found or a value could not be
     *             converted.
     */
    public Set<NATSituation> parseNATSituations(final String resourceName) throws Exception {
        final Set<NATSituation> result = new HashSet<NATSituation>();
        final Set<NATSituation> tmp = new HashSet<NATSituation>();
        final InputStream resourceAsStream = this.getResourceAsStream(resourceName);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
        String line = "";

        while ((line = reader.readLine()) != null) {
            final String[] lineContent = line.split(VALUE_SEPARATOR);
            final NATSituation traversaledNATBehavior = this.createNATBehaviorFromValues(lineContent);
            tmp.add(traversaledNATBehavior);
        }
        for (NATSituation natSituation : tmp) {
            result.addAll(this.resolveWildcards(natSituation));
        }
        return Collections.unmodifiableSet(result);
    }

    private InputStream getResourceAsStream(final String resourceName) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
    }

    private NATSituation createNATBehaviorFromValues(final String[] values) {
        final NATFeatureRealization clientMapping = NATFeatureRealization.valueOf(values[0].toUpperCase());
        final NATFeatureRealization clientFiltering = NATFeatureRealization.valueOf(values[1].toUpperCase());
        final NATFeatureRealization serviceMapping = NATFeatureRealization.valueOf(values[2].toUpperCase());
        final NATFeatureRealization serviceFiltering = NATFeatureRealization.valueOf(values[3].toUpperCase());

        return new NATSituation(clientMapping, clientFiltering, serviceMapping, serviceFiltering);
    }

    public Set<NATSituation> resolveWildcards(final NATSituation withWildcard) {
        // TODO refactor: divide and conquer
        final Set<NATSituation> result = new HashSet<NATSituation>();
        // resolve client
        final NATBehavior clientNat = withWildcard.getClientNATBehavior();
        final Set<NATBehavior> clientBehaviors = new HashSet<NATBehavior>();
        if (this.hasWildcard(clientNat)) {
            clientBehaviors.addAll(this.resolveWildcardInNatBehavior(clientNat));
        } else {
            clientBehaviors.add(clientNat);
        }
        // resolve server
        final NATBehavior serverNat = withWildcard.getServiceNATBehavior();
        final Set<NATBehavior> serverBehaviors = new HashSet<NATBehavior>();
        if (this.hasWildcard(serverNat)) {
            serverBehaviors.addAll(this.resolveWildcardInNatBehavior(serverNat));
        } else {
            serverBehaviors.add(serverNat);
        }
        // combine
        for (NATBehavior clientBehavior : clientBehaviors) {
            for (NATBehavior serverBehavior : serverBehaviors) {
                result.add(new NATSituation(clientBehavior, serverBehavior));
            }
        }
        return Collections.unmodifiableSet(result);
    }

    private boolean hasWildcard(final NATBehavior nat) {
        final Set<NATFeature> features = nat.getNATFeatures();
        for (NATFeature natFeature : features) {
            if (nat.getFeatureRealization(natFeature).equals(NATFeatureRealization.DONT_CARE)) {
                return true;
            }
        }
        return false;
    }

    private Set<NATBehavior> resolveWildcardInNatBehavior(final NATBehavior nat) {
        // TODO refactor
        final Set<NATBehavior> firstResult = new HashSet<NATBehavior>();
        final Set<NATBehavior> result = new HashSet<NATBehavior>();
        if (nat.getFeatureRealization(NATFeature.MAPPING).equals(NATFeatureRealization.DONT_CARE)) {
            for (NATFeatureRealization nonWildcard : this.getNonWildcardFeatureRealizations()) {
                NATBehavior newNatBehavior = new NATBehavior(nonWildcard,
                        nat.getFeatureRealization(NATFeature.FILTERING));
                firstResult.add(newNatBehavior);
            }
        } else {
            firstResult.add(nat);
        }
        for (NATBehavior natBehavior : firstResult) {
            if (natBehavior.getFeatureRealization(NATFeature.FILTERING).equals(NATFeatureRealization.DONT_CARE)) {
                for (NATFeatureRealization nonWildcard : this.getNonWildcardFeatureRealizations()) {
                    NATBehavior newNatBehavior = new NATBehavior(natBehavior.getFeatureRealization(NATFeature.MAPPING),
                            nonWildcard);
                    result.add(newNatBehavior);
                }
            } else {
                result.add(natBehavior);
            }
        }
        return result;
    }

    private Set<NATFeatureRealization> getNonWildcardFeatureRealizations() {
        final Set<NATFeatureRealization> result = new HashSet<NATFeatureRealization>();
        for (NATFeatureRealization value : NATFeatureRealization.values()) {
            if (!value.equals(NATFeatureRealization.DONT_CARE)) {
                result.add(value);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * The method returns a Set of {@link NATSituation}s which contains all
     * possible combinations.
     * 
     * @return a Set with all possible {@link NATSituation}s
     */
    public Set<NATSituation> getAllPossibleNATSituations() {
        final Set<NATSituation> result = new HashSet<NATSituation>();
        final Set<NATBehavior> client = this.getAllPossibleNATBehaviors();
        final Set<NATBehavior> server = this.getAllPossibleNATBehaviors();

        for (NATBehavior c : client) {
            for (NATBehavior s : server) {
                result.add(new NATSituation(c, s));
            }
        }
        return Collections.unmodifiableSet(result);
    }

    private Set<NATBehavior> getAllPossibleNATBehaviors() {
        final Set<NATBehavior> result = new HashSet<NATBehavior>();
        final Set<NATFeatureRealization> nonWildcards = this.getNonWildcardFeatureRealizations();
        for (NATFeatureRealization a : nonWildcards) {
            for (NATFeatureRealization b : nonWildcards) {
                result.add(new NATBehavior(a, b));
            }
        }
        return Collections.unmodifiableSet(result);
    }

    private NATTraversalTechniqueUtil() {
        // private constructor
    }

    /**
     * Returns the sole instance of {@link NATTraversalTechniqueUtil}.
     * 
     * @return the sole instance of {@link NATTraversalTechniqueUtil}
     */
    public static NATTraversalTechniqueUtil getInstance() {
        return INSTANCE;
    }
}
