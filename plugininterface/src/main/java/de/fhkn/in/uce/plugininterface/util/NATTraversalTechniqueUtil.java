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
        final InputStream resourceAsStream = this.getResourceAsStream(resourceName);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
        String line = "";

        while ((line = reader.readLine()) != null) {
            final String[] lineContent = line.split(VALUE_SEPARATOR);
            final NATSituation traversaledNATBehavior = this.createNATBehaviorFromValues(lineContent);
            result.add(traversaledNATBehavior);
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
        for (NATFeatureRealization a : NATFeatureRealization.values()) {
            for (NATFeatureRealization b : NATFeatureRealization.values()) {
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
