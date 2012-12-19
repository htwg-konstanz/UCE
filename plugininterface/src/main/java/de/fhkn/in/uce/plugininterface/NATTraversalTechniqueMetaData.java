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
package de.fhkn.in.uce.plugininterface;

import java.util.Set;

import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;

/**
 * {@link NATTraversalTechniqueMetaData} delivers meta data of a
 * {@link NATTraversalTechnique}. To differentiate {@link NATTraversalTechnique}
 * s a concrete implementation of {@link NATTraversalTechniqueMetaData} must
 * provide a implementation of {@code equals}.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public interface NATTraversalTechniqueMetaData {
    /**
     * Returns the name of the traversal technique.
     * 
     * @return the name of the traversal technique
     */
    String getTraversalTechniqueName();

    /**
     * Returns the version of the traversal technique.
     * 
     * @return the version of the traversal technique
     */
    String getVersion();

    /**
     * Returns the number of messages which are maximal sent to establish a
     * connection. Thus the returned number specifies the worst case.If this
     * number of messages is theoretically infinite, {@code Integer.MAX_VLAUE}
     * can be returned.
     * 
     * @return the maximal number of message for establishing a connection
     */
    int getMaxConnectionSetupTime();

    /**
     * Indicates if a NAT traversal technique provides a direct connection
     * without any third party instance like a relay server which forwards
     * messages between the peers. After establishing the connection with the
     * NAT traversal technique, a direct connection between client and server is
     * made.
     * 
     * @return true if the traversal technique provides a direct connection
     *         without any third party instance, false else
     */
    boolean providesDirectConnection();

    /**
     * Returns a set of {@link NATSituation} which can be traversaled by this
     * {@link NATTraversalTechnique}.
     * 
     * @return a set of {@link NATSituation} which can be traversaled
     */
    Set<NATSituation> getTraversaledNATSituations();

    /**
     * Returns the timeout in milliseconds for the NAT traversal technique. This
     * timeout is used to abort the connection establishment if it is not
     * successful.
     * 
     * @return the timeout in milliseconds for the connection establishment
     */
    long getTimeout();

    /**
     * Returns the {@link NATTraversalTechniqueAttribute} for the NAT traversal
     * technique. It contains the unique encoding for the technique.
     * 
     * @return the {@link NATTraversalTechniqueAttribute} for the NAT traversal
     *         technique
     */
    NATTraversalTechniqueAttribute getAttribute();
}
