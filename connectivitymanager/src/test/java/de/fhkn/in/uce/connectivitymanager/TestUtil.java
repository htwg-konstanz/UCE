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
package de.fhkn.in.uce.connectivitymanager;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;

import de.fhkn.in.uce.connectivitymanager.selector.decisiontree.NATTraversalRule;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;

public interface TestUtil {
    NATTraversalTechnique getNATTraversalTechniqueForName(final String name);

    Set<NATTraversalRule> getRulesForDecisionTreeLearning();

    void setRegistryDirectory(String directory) throws Exception;

    InetSocketAddress getEndpointfromProperty(String ipProperty, String portProperty);

    boolean compareLists(List<NATTraversalTechnique> list1, List<NATTraversalTechnique> list2);
}
