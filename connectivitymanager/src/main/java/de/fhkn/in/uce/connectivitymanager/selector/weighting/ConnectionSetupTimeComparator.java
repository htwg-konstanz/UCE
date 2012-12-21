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
package de.fhkn.in.uce.connectivitymanager.selector.weighting;

import java.io.Serializable;
import java.util.Comparator;

import net.jcip.annotations.Immutable;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;
import de.fhkn.in.uce.plugininterface.NATTraversalTechniqueMetaData;

/**
 * This comparator compares the ConnectionSetupTime in the worst case of
 * {@link NATTraversalTechnique} with ascending order.
 * 
 * <pre>
 * The parameters for
 * comparing the {@link NATTraversalTechnique}s is
 * - direct/indirect connection: a connection without any third party instance which forwards message is preferred (direct)
 * - ConnectionSetupTime: the number of messages in the worst case is used in ascending order
 * </pre>
 * 
 * Note: this comparator imposes orderings that are inconsistent with equals.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
@Immutable
public final class ConnectionSetupTimeComparator implements Comparator<NATTraversalTechnique>, Serializable {
    private static final Comparator<NATTraversalTechnique> INSTANCE = new ConnectionSetupTimeComparator();

    /**
     * The version of this comparator.
     */
    private static final long serialVersionUID = -1639426284815405215L;

    @Override
    public int compare(NATTraversalTechnique tech1, NATTraversalTechnique tech2) {
        NATTraversalTechniqueMetaData o1 = tech1.getMetaData();
        NATTraversalTechniqueMetaData o2 = tech2.getMetaData();
        // compare direct - indirect connection
        if (o1.providesDirectConnection() && !o2.providesDirectConnection()) {
            return -1;
        } else if (!o1.providesDirectConnection() && o2.providesDirectConnection()) {
            return 1;
        }
        // compare maximal connection setup time
        if (o1.getMaxConnectionSetupTime() < o2.getMaxConnectionSetupTime()) {
            return -1;
        } else if (o1.getMaxConnectionSetupTime() > o2.getMaxConnectionSetupTime()) {
            return 1;
        }
        // compare name
        return o1.getTraversalTechniqueName().compareTo(o2.getTraversalTechniqueName());

        // if (o1.getMinConnectionSetupTime() < o2.getMinConnectionSetupTime())
        // {
        // return -1;
        // } else {
        // if (o1.getMinConnectionSetupTime() > o2.getMinConnectionSetupTime())
        // {
        // return 1;
        // } else if (o1.getMaxConnectionSetupTime() <
        // o2.getMaxConnectionSetupTime()) {
        // return -1;
        // } else if (o1.getMaxConnectionSetupTime() >
        // o2.getMaxConnectionSetupTime()) {
        // return 1;
        // } else {
        // return
        // o1.getTraversalTechniqueName().compareTo(o2.getTraversalTechniqueName());
        // }
        // }
    }

    private ConnectionSetupTimeComparator() {
        // private constructor
    }

    /**
     * Returns the sole instance of {@link ConnectionSetupTimeComparator}.
     * 
     * @return the sole instance of {@link ConnectionSetupTimeComparator}
     */
    public static Comparator<NATTraversalTechnique> getInstance() {
        return INSTANCE;
    }
}
