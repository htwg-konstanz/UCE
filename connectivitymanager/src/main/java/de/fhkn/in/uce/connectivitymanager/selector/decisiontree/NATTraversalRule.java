/*
    Copyright (c) 2012 Alexander Diener,

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhkn.in.uce.connectivitymanager.selector.decisiontree;

import java.util.Collections;
import java.util.List;

import net.jcip.annotations.Immutable;
import de.fhkn.in.uce.plugininterface.NATSituation;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;

/**
 * A {@code NATTraversalRule} represents a NAT situation and the appropriate NAT
 * traversal techniques which can be used to traverse the described NAT device.
 * The scope of this class is to provide a rule for learning decision trees to
 * generate a tree out of {@code NATTraversalRule}s.
 *
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 *
 */
@Immutable
public final class NATTraversalRule {
    private final NATSituation natSituation;
    private final List<NATTraversalTechnique> appropriateTraversalTechniques;

    /**
     * The public constructor for creating {@code NATTraversalRule}.
     *
     * @param natSituation
     *            The situation which describes the NAT devices.
     * @param appropriateTraversalTechniques
     *            The appropriate NAT traversal techniques for the given NAT
     *            situation.
     */
    public NATTraversalRule(final NATSituation natSituation,
            final List<NATTraversalTechnique> appropriateTraversalTechniques) {
        this.natSituation = natSituation;
        this.appropriateTraversalTechniques = Collections.unmodifiableList(appropriateTraversalTechniques);
    }

    /**
     * Getter for the NAT situation.
     *
     * @return the behavior of the NAT devices on client and service side.
     */
    public NATSituation getNATSituation() {
        return this.natSituation;
    }

    /**
     * Getter for the appropriate NAT traversal techniques. The techniques are
     * in an arbitrary order and it the order of the elements is unpredictable.
     *
     * @return the NAT traversal techniques for the described NAT devices.
     */
    public List<NATTraversalTechnique> getAppropriateTraversalTechniques() {
        return this.appropriateTraversalTechniques;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((natSituation == null) ? 0 : natSituation.hashCode());
        for(NATTraversalTechnique technique: appropriateTraversalTechniques) {
            result = (prime * result) + ((technique == null) ? 0 : technique.hashCode());
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NATTraversalRule)) {
            return false;
        }
        NATTraversalRule other = (NATTraversalRule) obj;
        return this.natSituation.equals(other.natSituation)
                && this.containsAllTechniques(this.appropriateTraversalTechniques, other.appropriateTraversalTechniques);
    }

    private boolean containsAllTechniques(final List<NATTraversalTechnique> list1,
            final List<NATTraversalTechnique> list2) {
        int count = 0;
        for (NATTraversalTechnique tech1 : list1) {
            if (list2.contains(tech1)) {
                count++;
            }
        }

        return (count == list1.size()) && (count == list2.size());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("[NATTraversalRule:\n"); //$NON-NLS-1$
        sb.append(this.natSituation.toString());
        sb.append("\n"); //$NON-NLS-1$
        sb.append("NAT Traversal Techniques="); //$NON-NLS-1$
        int count = 1;
        for (NATTraversalTechnique travTech : this.appropriateTraversalTechniques) {
            sb.append(travTech.getMetaData().getTraversalTechniqueName());
            if (count < this.appropriateTraversalTechniques.size()) {
                sb.append(","); //$NON-NLS-1$
                count++;
            }
        }
        sb.append("]"); //$NON-NLS-1$

        return sb.toString();
    }
}
